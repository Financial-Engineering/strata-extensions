/*
 * Copyright (C) 2018 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.measure.fx;

import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.data.scenario.DoubleScenarioArray;
import com.opengamma.strata.data.scenario.MultiCurrencyScenarioArray;
import com.opengamma.strata.data.scenario.ScenarioArray;
import com.opengamma.strata.data.scenario.ScenarioMarketData;
import com.opengamma.strata.market.param.CurrencyParameterSensitivities;
import com.opengamma.strata.measure.rate.RatesMarketDataLookup;
import com.opengamma.strata.pricer.fx.PriceToWorstFxFlexibleForwardTradePricer;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.product.fx.FxFlexibleForwardTrade;
import com.opengamma.strata.product.fx.ResolvedFxFlexibleForwardTrade;

/**
 * Calculates pricing and risk measures for FX Window Forward trades.
 * <p>
 * This provides a high-level entry point for FX pricing and risk measures.
 * <p>
 * Each method takes a {@link ResolvedFxFlexibleForwardTrade}, whereas
 * application code will typically work with {@link FxFlexibleForwardTrade}.
 * Call
 * {@link FxFlexibleForwardTrade#resolve(com.opengamma.strata.basics.ReferenceData)
 * FxFlexibleForwardTrade::resolve(ReferenceData)} to convert
 * {@code FxFlexibleForwardTrade} to {@code ResolvedFxFlexibleForwardTrade}.
 */
public class FxFlexibleForwardCalculations {

  /**
   * Default implementation.
   */
  public static final FxFlexibleForwardCalculations DEFAULT = new FxFlexibleForwardCalculations(
      PriceToWorstFxFlexibleForwardTradePricer.DEFAULT);

  /**
   * Pricer for {@link ResolvedFxFlexibleForwardTrade}.
   */
  private final FxFlexibleForwardMeasureCalculations calc;

  /**
   * Creates an instance.
   * <p>
   * In most cases, applications should use the {@link #DEFAULT} instance.
   *
   * @param tradePricer
   *          the pricer for {@link ResolvedFxFlexibleForwardTrade}
   */
  public FxFlexibleForwardCalculations(PriceToWorstFxFlexibleForwardTradePricer tradePricer) {
    this.calc = new FxFlexibleForwardMeasureCalculations(tradePricer);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates present value across one or more scenarios.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the present value, one entry per scenario
   */
  public MultiCurrencyScenarioArray presentValue(ResolvedFxFlexibleForwardTrade trade, RatesMarketDataLookup lookup,
      ScenarioMarketData marketData) {

    return calc.presentValue(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates present value for a single set of market data.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the present value
   */
  public MultiCurrencyAmount presentValue(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return calc.presentValue(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates present value sensitivity across one or more scenarios.
   * <p>
   * This is the sensitivity of
   * {@linkplain #presentValue(ResolvedFxFlexibleForwardTrade, RatesMarketDataLookup, ScenarioMarketData)
   * present value} to a one basis point shift in the calibrated curves. The
   * result is the sum of the sensitivities of all affected curves.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the present value sensitivity, one entry per scenario
   */
  public MultiCurrencyScenarioArray pv01CalibratedSum(ResolvedFxFlexibleForwardTrade trade,
      RatesMarketDataLookup lookup, ScenarioMarketData marketData) {

    return calc.pv01CalibratedSum(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates present value sensitivity for a single set of market data.
   * <p>
   * This is the sensitivity of
   * {@linkplain #presentValue(ResolvedFxFlexibleForwardTrade, RatesMarketDataLookup, ScenarioMarketData)
   * present value} to a one basis point shift in the calibrated curves. The
   * result is the sum of the sensitivities of all affected curves.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the present value sensitivity
   */
  public MultiCurrencyAmount pv01CalibratedSum(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return calc.pv01CalibratedSum(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates present value sensitivity across one or more scenarios.
   * <p>
   * This is the sensitivity of
   * {@linkplain #presentValue(ResolvedFxFlexibleForwardTrade, RatesMarketDataLookup, ScenarioMarketData)
   * present value} to a one basis point shift in the calibrated curves. The
   * result is provided for each affected curve and currency, bucketed by curve
   * node.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the present value sensitivity, one entry per scenario
   */
  public ScenarioArray<CurrencyParameterSensitivities> pv01CalibratedBucketed(ResolvedFxFlexibleForwardTrade trade,
      RatesMarketDataLookup lookup, ScenarioMarketData marketData) {

    return calc.pv01CalibratedBucketed(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates present value sensitivity for a single set of market data.
   * <p>
   * This is the sensitivity of
   * {@linkplain #presentValue(ResolvedFxFlexibleForwardTrade, RatesMarketDataLookup, ScenarioMarketData)
   * present value} to a one basis point shift in the calibrated curves. The
   * result is provided for each affected curve and currency, bucketed by curve
   * node.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the present value sensitivity
   */
  public CurrencyParameterSensitivities pv01CalibratedBucketed(ResolvedFxFlexibleForwardTrade trade,
      RatesProvider ratesProvider) {

    return calc.pv01CalibratedBucketed(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates present value sensitivity across one or more scenarios.
   * <p>
   * This is the sensitivity of
   * {@linkplain #presentValue(ResolvedFxFlexibleForwardTrade, RatesMarketDataLookup, ScenarioMarketData)
   * present value} to a one basis point shift in the market quotes used to
   * calibrate the curves. The result is the sum of the sensitivities of all
   * affected curves.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the present value sensitivity, one entry per scenario
   */
  public MultiCurrencyScenarioArray pv01MarketQuoteSum(ResolvedFxFlexibleForwardTrade trade,
      RatesMarketDataLookup lookup, ScenarioMarketData marketData) {

    return calc.pv01MarketQuoteSum(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates present value sensitivity for a single set of market data.
   * <p>
   * This is the sensitivity of
   * {@linkplain #presentValue(ResolvedFxFlexibleForwardTrade, RatesMarketDataLookup, ScenarioMarketData)
   * present value} to a one basis point shift in the market quotes used to
   * calibrate the curves. The result is the sum of the sensitivities of all
   * affected curves.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the present value sensitivity
   */
  public MultiCurrencyAmount pv01MarketQuoteSum(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return calc.pv01MarketQuoteSum(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates present value sensitivity across one or more scenarios.
   * <p>
   * This is the sensitivity of
   * {@linkplain #presentValue(ResolvedFxFlexibleForwardTrade, RatesMarketDataLookup, ScenarioMarketData)
   * present value} to a one basis point shift in the market quotes used to
   * calibrate the curves. The result is provided for each affected curve and
   * currency, bucketed by curve node.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the present value sensitivity, one entry per scenario
   */
  public ScenarioArray<CurrencyParameterSensitivities> pv01MarketQuoteBucketed(ResolvedFxFlexibleForwardTrade trade,
      RatesMarketDataLookup lookup, ScenarioMarketData marketData) {

    return calc.pv01MarketQuoteBucketed(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates present value sensitivity for a single set of market data.
   * <p>
   * This is the sensitivity of
   * {@linkplain #presentValue(ResolvedFxFlexibleForwardTrade, RatesMarketDataLookup, ScenarioMarketData)
   * present value} to a one basis point shift in the market quotes used to
   * calibrate the curves. The result is provided for each affected curve and
   * currency, bucketed by curve node.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the present value sensitivity
   */
  public CurrencyParameterSensitivities pv01MarketQuoteBucketed(ResolvedFxFlexibleForwardTrade trade,
      RatesProvider ratesProvider) {

    return calc.pv01MarketQuoteBucketed(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates par spread across one or more scenarios.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the par spread, one entry per scenario
   */
  public DoubleScenarioArray parSpread(ResolvedFxFlexibleForwardTrade trade, RatesMarketDataLookup lookup,
      ScenarioMarketData marketData) {

    return calc.parSpread(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates par spread for a single set of market data.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the par spread
   */
  public double parSpread(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return calc.parSpread(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates currency exposure across one or more scenarios.
   * <p>
   * The currency risk, expressed as the equivalent amount in each currency.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the currency exposure, one entry per scenario
   */
  public MultiCurrencyScenarioArray currencyExposure(ResolvedFxFlexibleForwardTrade trade, RatesMarketDataLookup lookup,
      ScenarioMarketData marketData) {

    return calc.currencyExposure(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates currency exposure for a single set of market data.
   * <p>
   * The currency risk, expressed as the equivalent amount in each currency.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the currency exposure
   */
  public MultiCurrencyAmount currencyExposure(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return calc.currencyExposure(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates current cash across one or more scenarios.
   * <p>
   * The sum of all cash flows paid on the valuation date.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the current cash, one entry per scenario
   */
  public MultiCurrencyScenarioArray currentCash(ResolvedFxFlexibleForwardTrade trade, RatesMarketDataLookup lookup,
      ScenarioMarketData marketData) {

    return calc.currentCash(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates current cash for a single set of market data.
   * <p>
   * The sum of all cash flows paid on the valuation date.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the current cash
   */
  public MultiCurrencyAmount currentCash(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return calc.currentCash(trade, ratesProvider);
  }

  // -------------------------------------------------------------------------
  /**
   * Calculates the forward FX rate across one or more scenarios.
   *
   * @param trade
   *          the trade
   * @param lookup
   *          the lookup used to query the market data
   * @param marketData
   *          the market data
   * @return the current cash, one entry per scenario
   */
  public ScenarioArray<FxRate> forwardFxRate(ResolvedFxFlexibleForwardTrade trade, RatesMarketDataLookup lookup,
      ScenarioMarketData marketData) {

    return calc.forwardFxRate(trade, lookup.marketDataView(marketData));
  }

  /**
   * Calculates the forward FX rate for a single set of market data.
   *
   * @param trade
   *          the trade
   * @param ratesProvider
   *          the market data
   * @return the current cash
   */
  public FxRate forwardFxRate(ResolvedFxFlexibleForwardTrade trade, RatesProvider ratesProvider) {

    return calc.forwardFxRate(trade, ratesProvider);
  }

}
