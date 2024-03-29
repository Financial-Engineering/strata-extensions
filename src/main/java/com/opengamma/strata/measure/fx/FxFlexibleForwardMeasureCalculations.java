/*
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.measure.fx;

import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.data.scenario.DoubleScenarioArray;
import com.opengamma.strata.data.scenario.MultiCurrencyScenarioArray;
import com.opengamma.strata.data.scenario.ScenarioArray;
import com.opengamma.strata.market.param.CurrencyParameterSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivities;
import com.opengamma.strata.measure.rate.RatesScenarioMarketData;
import com.opengamma.strata.pricer.fx.PriceToWorstFxFlexibleForwardTradePricer;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.pricer.sensitivity.MarketQuoteSensitivityCalculator;
import com.opengamma.strata.product.fx.ResolvedFxFlexibleForwardTrade;
import com.opengamma.strata.product.fx.ResolvedFxSingleTrade;

/**
 * Multi-scenario measure calculations for FX single leg trades.
 * <p>
 * Each method corresponds to a measure, typically calculated by one or more
 * calls to the pricer.
 */
final class FxFlexibleForwardMeasureCalculations {

  /**
   * Default implementation.
   */
  public static final FxFlexibleForwardMeasureCalculations DEFAULT = new FxFlexibleForwardMeasureCalculations(
      PriceToWorstFxFlexibleForwardTradePricer.DEFAULT);
  /**
   * The market quote sensitivity calculator.
   */
  private static final MarketQuoteSensitivityCalculator MARKET_QUOTE_SENS = MarketQuoteSensitivityCalculator.DEFAULT;
  /**
   * One basis point, expressed as a {@code double}.
   */
  private static final double ONE_BASIS_POINT = 1e-4;

  /**
   * Pricer for {@link ResolvedFxFlexibleForwardTrade}.
   */
  private final PriceToWorstFxFlexibleForwardTradePricer tradePricer;

  /**
   * Creates an instance.
   *
   * @param tradePricer
   *          the pricer for {@link ResolvedFxSingleTrade}
   */
  FxFlexibleForwardMeasureCalculations(PriceToWorstFxFlexibleForwardTradePricer tradePricer) {
    this.tradePricer = ArgChecker.notNull(tradePricer, "tradePricer");
  }

  // -------------------------------------------------------------------------
  // calculates present value for all scenarios
  MultiCurrencyScenarioArray presentValue(ResolvedFxFlexibleForwardTrade trade, RatesScenarioMarketData marketData) {

    return MultiCurrencyScenarioArray.of(marketData.getScenarioCount(),
        i -> presentValue(trade, marketData.scenario(i).ratesProvider()));
  }

  // present value for one scenario
  MultiCurrencyAmount presentValue(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return tradePricer.presentValue(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  // calculates calibrated sum PV01 for all scenarios
  MultiCurrencyScenarioArray pv01CalibratedSum(ResolvedFxFlexibleForwardTrade trade,
      RatesScenarioMarketData marketData) {

    return MultiCurrencyScenarioArray.of(marketData.getScenarioCount(),
        i -> pv01CalibratedSum(trade, marketData.scenario(i).ratesProvider()));
  }

  // calibrated sum PV01 for one scenario
  MultiCurrencyAmount pv01CalibratedSum(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    final PointSensitivities pointSensitivity = tradePricer.presentValueSensitivity(trade, ratesProvider);
    return ratesProvider.parameterSensitivity(pointSensitivity).total().multipliedBy(ONE_BASIS_POINT);
  }

  // -------------------------------------------------------------------------
  // calculates calibrated bucketed PV01 for all scenarios
  ScenarioArray<CurrencyParameterSensitivities> pv01CalibratedBucketed(ResolvedFxFlexibleForwardTrade trade,
      RatesScenarioMarketData marketData) {

    return ScenarioArray.of(marketData.getScenarioCount(),
        i -> pv01CalibratedBucketed(trade, marketData.scenario(i).ratesProvider()));
  }

  // calibrated bucketed PV01 for one scenario
  CurrencyParameterSensitivities pv01CalibratedBucketed(ResolvedFxFlexibleForwardTrade trade,
      RatesProvider ratesProvider) {

    final PointSensitivities pointSensitivity = tradePricer.presentValueSensitivity(trade, ratesProvider);
    return ratesProvider.parameterSensitivity(pointSensitivity).multipliedBy(ONE_BASIS_POINT);
  }

  // -------------------------------------------------------------------------
  // calculates market quote sum PV01 for all scenarios
  MultiCurrencyScenarioArray pv01MarketQuoteSum(ResolvedFxFlexibleForwardTrade trade,
      RatesScenarioMarketData marketData) {

    return MultiCurrencyScenarioArray.of(marketData.getScenarioCount(),
        i -> pv01MarketQuoteSum(trade, marketData.scenario(i).ratesProvider()));
  }

  // market quote sum PV01 for one scenario
  MultiCurrencyAmount pv01MarketQuoteSum(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    final PointSensitivities pointSensitivity = tradePricer.presentValueSensitivity(trade, ratesProvider);
    final CurrencyParameterSensitivities parameterSensitivity = ratesProvider.parameterSensitivity(pointSensitivity);
    return MARKET_QUOTE_SENS.sensitivity(parameterSensitivity, ratesProvider).total().multipliedBy(ONE_BASIS_POINT);
  }

  // -------------------------------------------------------------------------
  // calculates market quote bucketed PV01 for all scenarios
  ScenarioArray<CurrencyParameterSensitivities> pv01MarketQuoteBucketed(ResolvedFxFlexibleForwardTrade trade,
      RatesScenarioMarketData marketData) {

    return ScenarioArray.of(marketData.getScenarioCount(),
        i -> pv01MarketQuoteBucketed(trade, marketData.scenario(i).ratesProvider()));
  }

  // market quote bucketed PV01 for one scenario
  CurrencyParameterSensitivities pv01MarketQuoteBucketed(ResolvedFxFlexibleForwardTrade trade,
      RatesProvider ratesProvider) {

    final PointSensitivities pointSensitivity = tradePricer.presentValueSensitivity(trade, ratesProvider);
    final CurrencyParameterSensitivities parameterSensitivity = ratesProvider.parameterSensitivity(pointSensitivity);
    return MARKET_QUOTE_SENS.sensitivity(parameterSensitivity, ratesProvider).multipliedBy(ONE_BASIS_POINT);
  }

  // -------------------------------------------------------------------------
  // calculates par spread for all scenarios
  DoubleScenarioArray parSpread(ResolvedFxFlexibleForwardTrade trade, RatesScenarioMarketData marketData) {

    return DoubleScenarioArray.of(marketData.getScenarioCount(),
        i -> parSpread(trade, marketData.scenario(i).ratesProvider()));
  }

  // par spread for one scenario
  double parSpread(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return tradePricer.parSpread(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  // calculates currency exposure for all scenarios
  MultiCurrencyScenarioArray currencyExposure(ResolvedFxFlexibleForwardTrade trade,
      RatesScenarioMarketData marketData) {

    return MultiCurrencyScenarioArray.of(marketData.getScenarioCount(),
        i -> currencyExposure(trade, marketData.scenario(i).ratesProvider()));
  }

  // currency exposure for one scenario
  MultiCurrencyAmount currencyExposure(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return tradePricer.currencyExposure(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  // calculates current cash for all scenarios
  MultiCurrencyScenarioArray currentCash(ResolvedFxFlexibleForwardTrade trade, RatesScenarioMarketData marketData) {

    return MultiCurrencyScenarioArray.of(marketData.getScenarioCount(),
        i -> currentCash(trade, marketData.scenario(i).ratesProvider()));
  }

  // current cash for one scenario
  MultiCurrencyAmount currentCash(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return tradePricer.currentCash(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  // calculates forward FX rate for all scenarios
  ScenarioArray<FxRate> forwardFxRate(ResolvedFxFlexibleForwardTrade trade, RatesScenarioMarketData marketData) {

    return ScenarioArray.of(marketData.getScenarioCount(),
        i -> forwardFxRate(trade, marketData.scenario(i).ratesProvider()));
  }

  // forward FX rate for one scenario
  FxRate forwardFxRate(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return tradePricer.forwardFxRate(trade, ratesProvider);
  }

}
