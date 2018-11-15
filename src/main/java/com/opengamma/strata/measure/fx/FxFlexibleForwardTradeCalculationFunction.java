/*
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.measure.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.calc.Measure;
import com.opengamma.strata.calc.runner.CalculationFunction;
import com.opengamma.strata.calc.runner.CalculationParameters;
import com.opengamma.strata.calc.runner.FunctionRequirements;
import com.opengamma.strata.collect.result.FailureReason;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.data.scenario.ScenarioMarketData;
import com.opengamma.strata.measure.Measures;
import com.opengamma.strata.measure.rate.RatesMarketDataLookup;
import com.opengamma.strata.measure.rate.RatesScenarioMarketData;
import com.opengamma.strata.product.fx.FxFlexibleForward;
import com.opengamma.strata.product.fx.FxFlexibleForwardTrade;
import com.opengamma.strata.product.fx.ResolvedFxFlexibleForwardTrade;

/**
 * Perform calculations on a single {@code FxFlexibleForwardTrade} for each of a
 * set of scenarios.
 * <p>
 * This uses the standard discounting calculation method. An instance of
 * {@link RatesMarketDataLookup} must be specified. The supported built-in
 * measures are:
 * <ul>
 * <li>{@linkplain Measures#PRESENT_VALUE Present value}
 * <li>{@linkplain Measures#PV01_CALIBRATED_SUM PV01 calibrated sum}
 * <li>{@linkplain Measures#PV01_CALIBRATED_BUCKETED PV01 calibrated bucketed}
 * <li>{@linkplain Measures#PV01_MARKET_QUOTE_SUM PV01 market quote sum}
 * <li>{@linkplain Measures#PV01_MARKET_QUOTE_BUCKETED PV01 market quote
 * bucketed}
 * <li>{@linkplain Measures#PAR_SPREAD Par spread}
 * <li>{@linkplain Measures#CURRENCY_EXPOSURE Currency exposure}
 * <li>{@linkplain Measures#CURRENT_CASH Current cash}
 * <li>{@linkplain Measures#RESOLVED_TARGET Resolved trade}
 * <li>{@linkplain Measures#FORWARD_FX_RATE Forward FX rate}
 * </ul>
 * <p>
 * The "natural" currency is the base currency of the market convention pair of
 * the two trade currencies.
 */
public class FxFlexibleForwardTradeCalculationFunction implements CalculationFunction<FxFlexibleForwardTrade> {

  /**
   * The calculations by measure.
   */
  private static final ImmutableMap<Measure, SingleMeasureCalculation> CALCULATORS = ImmutableMap
      .<Measure, SingleMeasureCalculation>builder()
      .put(Measures.PRESENT_VALUE, FxFlexibleForwardMeasureCalculations.DEFAULT::presentValue)
      .put(Measures.PV01_CALIBRATED_SUM, FxFlexibleForwardMeasureCalculations.DEFAULT::pv01CalibratedSum)
      .put(Measures.PV01_CALIBRATED_BUCKETED, FxFlexibleForwardMeasureCalculations.DEFAULT::pv01CalibratedBucketed)
      .put(Measures.PV01_MARKET_QUOTE_SUM, FxFlexibleForwardMeasureCalculations.DEFAULT::pv01MarketQuoteSum)
      .put(Measures.PV01_MARKET_QUOTE_BUCKETED, FxFlexibleForwardMeasureCalculations.DEFAULT::pv01MarketQuoteBucketed)
      .put(Measures.PAR_SPREAD, FxFlexibleForwardMeasureCalculations.DEFAULT::parSpread)
      .put(Measures.CURRENCY_EXPOSURE, FxFlexibleForwardMeasureCalculations.DEFAULT::currencyExposure)
      .put(Measures.CURRENT_CASH, FxFlexibleForwardMeasureCalculations.DEFAULT::currentCash)
      .put(Measures.FORWARD_FX_RATE, FxFlexibleForwardMeasureCalculations.DEFAULT::forwardFxRate)
      .put(Measures.RESOLVED_TARGET, (rt, smd) -> rt).build();

  private static final ImmutableSet<Measure> MEASURES = CALCULATORS.keySet();

  /**
   * Creates an instance.
   */
  public FxFlexibleForwardTradeCalculationFunction() {
  }

  // -------------------------------------------------------------------------
  @Override
  public Class<FxFlexibleForwardTrade> targetType() {
    return FxFlexibleForwardTrade.class;
  }

  @Override
  public Set<Measure> supportedMeasures() {
    return MEASURES;
  }

  @Override
  public Optional<String> identifier(FxFlexibleForwardTrade target) {
    return target.getInfo().getId().map(id -> id.toString());
  }

  @Override
  public Currency naturalCurrency(FxFlexibleForwardTrade trade, ReferenceData refData) {
    final Currency base = trade.getProduct().getBaseCurrencyAmount().getCurrency();
    final Currency counter = trade.getProduct().getCounterCurrencyAmount().getCurrency();
    final CurrencyPair marketConventionPair = CurrencyPair.of(base, counter).toConventional();
    return marketConventionPair.getBase();
  }

  // -------------------------------------------------------------------------
  @Override
  public FunctionRequirements requirements(FxFlexibleForwardTrade trade, Set<Measure> measures,
      CalculationParameters parameters, ReferenceData refData) {

    // extract data from product
    final FxFlexibleForward fx = trade.getProduct();
    final Currency baseCurrency = fx.getBaseCurrencyAmount().getCurrency();
    final Currency counterCurrency = fx.getCounterCurrencyAmount().getCurrency();
    final ImmutableSet<Currency> currencies = ImmutableSet.of(baseCurrency, counterCurrency);

    // use lookup to build requirements
    final RatesMarketDataLookup ratesLookup = parameters.getParameter(RatesMarketDataLookup.class);
    return ratesLookup.requirements(currencies);
  }

  // -------------------------------------------------------------------------
  @Override
  public Map<Measure, Result<?>> calculate(FxFlexibleForwardTrade trade, Set<Measure> measures,
      CalculationParameters parameters, ScenarioMarketData scenarioMarketData, ReferenceData refData) {

    // resolve the trade once for all measures and all scenarios
    final ResolvedFxFlexibleForwardTrade resolved = trade.resolve(refData);

    // use lookup to query market data
    final RatesMarketDataLookup ratesLookup = parameters.getParameter(RatesMarketDataLookup.class);
    final RatesScenarioMarketData marketData = ratesLookup.marketDataView(scenarioMarketData);

    // loop around measures, calculating all scenarios for one measure
    final Map<Measure, Result<?>> results = new HashMap<>();
    for (final Measure measure : measures) {
      results.put(measure, calculate(measure, resolved, marketData));
    }
    return results;
  }

  // calculate one measure
  private Result<?> calculate(Measure measure, ResolvedFxFlexibleForwardTrade trade,
      RatesScenarioMarketData marketData) {

    final SingleMeasureCalculation calculator = CALCULATORS.get(measure);
    if (calculator == null) {
      return Result.failure(FailureReason.UNSUPPORTED, "Unsupported measure for FxWindowForwardTrade: {}", measure);
    }
    return Result.of(() -> calculator.calculate(trade, marketData));
  }

  // -------------------------------------------------------------------------
  @FunctionalInterface
  interface SingleMeasureCalculation {
    public abstract Object calculate(ResolvedFxFlexibleForwardTrade trade, RatesScenarioMarketData marketData);
  }

}
