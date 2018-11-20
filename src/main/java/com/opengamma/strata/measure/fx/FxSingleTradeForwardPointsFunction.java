package com.opengamma.strata.measure.fx;

import java.util.Map;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.calc.ExtendedMeasures;
import com.opengamma.strata.calc.Measure;
import com.opengamma.strata.calc.runner.AbstractDerivedCalculationFunction;
import com.opengamma.strata.calc.runner.CalculationParameters;
import com.opengamma.strata.data.scenario.ScenarioMarketData;
import com.opengamma.strata.measure.Measures;
import com.opengamma.strata.measure.rate.RatesMarketDataLookup;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.product.fx.FxSingleTrade;
import com.opengamma.strata.product.fx.ResolvedFxSingle;
import com.opengamma.strata.product.fx.ResolvedFxSingleTrade;

/**
 * Perform calculations on a single {@code FxSingleTrade} for each of a set of
 * scenarios.
 * <p>
 * The supported built-in measures are:
 * <ul>
 * <li>{@linkplain Measures#FX_SWAP_RATE Par FX Swap Rate}
 * </ul>
 */
public class FxSingleTradeForwardPointsFunction extends AbstractDerivedCalculationFunction<FxSingleTrade, Double> {

	public FxSingleTradeForwardPointsFunction() {
		super(FxSingleTrade.class, ExtendedMeasures.FX_SWAP_RATE, Measures.RESOLVED_TARGET);
	}

	@Override
	public Double calculate(FxSingleTrade target, Map<Measure, Object> requiredMeasures,
			CalculationParameters parameters, ScenarioMarketData marketData, ReferenceData refData) {
		ResolvedFxSingleTrade resolvedTrade = (ResolvedFxSingleTrade) requiredMeasures.get(Measures.RESOLVED_TARGET);

		// use lookup to query market data
		final RatesMarketDataLookup ratesLookup = parameters.getParameter(RatesMarketDataLookup.class);

		ResolvedFxSingle fx = resolvedTrade.getProduct();

		RatesProvider provider = ratesLookup.ratesProvider(marketData.scenario(0));
		double forwardRate = forwardFxRate(fx, provider).fxRate(fx.getCurrencyPair());
		double spotRate = spotFxRate(fx, provider).fxRate(fx.getCurrencyPair());

		return forwardRate - spotRate;
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

}
