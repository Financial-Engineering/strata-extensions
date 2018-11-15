package com.opengamma.strata.measure.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.calc.ExtendedMeasures;
import com.opengamma.strata.calc.Measure;
import com.opengamma.strata.calc.runner.CalculationParameters;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.data.scenario.ScenarioMarketData;
import com.opengamma.strata.measure.Measures;
import com.opengamma.strata.measure.rate.RatesMarketDataLookup;
import com.opengamma.strata.measure.rate.RatesScenarioMarketData;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.product.fx.FxSingleTrade;
import com.opengamma.strata.product.fx.ResolvedFxSingle;
import com.opengamma.strata.product.fx.ResolvedFxSingleTrade;

import io.vavr.collection.HashSet;

/**
 * Perform calculations on a single {@code FxSingleTrade} for each of a set of
 * scenarios.
 * <p>
 * The supported built-in measures are:
 * <ul>
 * <li>{@linkplain Measures#FX_SWAP_RATE Par FX Swap Rate}
 * </ul>
 */
public class FxSingleTradeForwardPointsFunction extends FxSingleTradeCalculationFunction {

	@Override
	public Map<Measure, Result<?>> calculate(FxSingleTrade target, Set<Measure> measures,
			CalculationParameters parameters, ScenarioMarketData marketData, ReferenceData refData) {
		// resolve the trade once for all measures and all scenarios
		final ResolvedFxSingleTrade resolved = target.resolve(refData);

		// use lookup to query market data
		final RatesMarketDataLookup ratesLookup = parameters.getParameter(RatesMarketDataLookup.class);
		final RatesScenarioMarketData ratesMarketData = ratesLookup.marketDataView(marketData);

		// loop around measures, calculating all scenarios for one measure
		final Map<Measure, Result<?>> results = new HashMap<>();
		for (final Measure measure : measures) {

			// TODO: for now, only include the base scenario
			RatesProvider provider = ratesLookup.ratesProvider(marketData.scenario(0));
			ResolvedFxSingle fx = resolved.getProduct();

			double forwardRate = forwardFxRate(fx, provider).fxRate(fx.getCurrencyPair());
			double spotRate = spotFxRate(fx, provider).fxRate(fx.getCurrencyPair());
			results.put(measure, Result.of(() -> forwardRate - spotRate));
		}
		
		return results;
	}

	private FxRate forwardFxRate(ResolvedFxSingle fx, RatesProvider provider) {
		double forwardRate = provider.fxForwardRates(fx.getCurrencyPair())
				.rate(fx.getBaseCurrencyPayment().getCurrency(), fx.getPaymentDate());
		return FxRate.of(fx.getBaseCurrencyPayment().getCurrency(), fx.getCounterCurrencyPayment().getCurrency(),
				forwardRate);
	}

	private FxRate spotFxRate(ResolvedFxSingle fx, RatesProvider provider) {
		return FxRate.of(fx.getBaseCurrencyPayment().getCurrency(), fx.getCounterCurrencyPayment().getCurrency(),
				provider.fxRate(fx.getCurrencyPair()));
	}

	@Override
	public Set<Measure> supportedMeasures() {
		return HashSet.of(ExtendedMeasures.FX_SWAP_RATE).toJavaSet();
	}

}
