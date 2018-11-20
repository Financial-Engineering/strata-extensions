package com.opengamma.strata.calc;

import static com.opengamma.strata.basics.currency.Currency.USD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

import org.joda.beans.ser.JodaBeanSer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendarId;
import com.opengamma.strata.basics.date.HolidayCalendarIds;
import com.opengamma.strata.basics.index.FxIndex;
import com.opengamma.strata.basics.index.ImmutableFxIndex;
import com.opengamma.strata.calc.marketdata.BuiltMarketData;
import com.opengamma.strata.calc.runner.CalculationFunctions;
import com.opengamma.strata.market.curve.RatesCurveGroupDefinition;
import com.opengamma.strata.measure.Measures;
import com.opengamma.strata.measure.StandardComponents;
import com.opengamma.strata.measure.fx.FxFlexibleForwardTradeCalculationFunction;
import com.opengamma.strata.measure.fx.FxSingleTradeForwardPointsFunction;
import com.opengamma.strata.measure.rate.RatesMarketDataLookup;
import com.opengamma.strata.product.AttributeType;
import com.opengamma.strata.product.Trade;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.fx.FxNdf;
import com.opengamma.strata.product.fx.FxNdfTrade;
import com.opengamma.strata.product.fx.FxSingle;
import com.opengamma.strata.product.fx.FxSingleTrade;

import io.vavr.collection.List;
import io.vavr.control.Try;

public class FxForwardTest {
	private static final Logger LOG = LoggerFactory.getLogger(FxForwardTest.class);

	private static final double EPSILON = 1.E-15;

	private static final ReferenceData REF_DATA = ReferenceData.standard();

	private static final LocalDate VAL_DATE = LocalDate.of(2018, 7, 30);

	private static final CurrencyPair USD_KRW = CurrencyPair.of(Currency.USD, Currency.KRW);
	private static final CurrencyPair USD_CAD = CurrencyPair.of(Currency.USD, Currency.CAD);
	private static final FxSingleTrade USD_CAD_FWD_6M = createFxForward(USD_CAD, 100000, 0.76, VAL_DATE,
			Period.ofMonths(6));
	private static final FxNdfTrade USD_KRW_NDF_6M = createFxNdf(USD_KRW, 100000, 1129.90, VAL_DATE,
			Period.ofMonths(6));

	private static final LocalDate calcPaymentDate(CurrencyPair pair, LocalDate valueDate, Period tenor) {

		return HolidayCalendarId.defaultByCurrency(pair.getBase()).resolve(REF_DATA).nextOrSame(valueDate.plus(tenor));

	}

	private static final FxSingleTrade createFxForward(CurrencyPair pair, double amount, double rate,
			LocalDate valueDate, Period tenor) {

		LocalDate paymentDate = calcPaymentDate(pair, valueDate, tenor);

		FxSingle fx = FxSingle.of(CurrencyAmount.of(pair.getCounter(), amount), FxRate.of(pair, rate), paymentDate,
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING,
						HolidayCalendarId.defaultByCurrency(pair.getBase())));

		return FxSingleTrade.builder().product(fx)
				.info(TradeInfo.builder().id(StandardId.of("example", "2")).tradeDate(valueDate)
						.tradeTime(LocalTime.now())
						.addAttribute(AttributeType.DESCRIPTION,
								String.format("%s %f/%s @ %f fwd %s", pair.getCounter().getCode(), amount,
										pair.getCounter().getCode(), rate, tenor))
						.counterparty(StandardId.of("example", "BigBankB")).settlementDate(paymentDate).build())
				.build();
	}

	private static final FxNdfTrade createFxNdf(CurrencyPair pair, double amount, double rate, LocalDate valueDate,
			Period tenor) {
		LocalDate paymentDate = calcPaymentDate(pair, valueDate, tenor);

		FxIndex index = ImmutableFxIndex.builder().name(pair.toString()).currencyPair(pair)
				.fixingCalendar(HolidayCalendarIds.USNY).maturityDateOffset(
						DaysAdjustment.ofBusinessDays(2, HolidayCalendarId.defaultByCurrency(pair.getBase())))
				.build();

		FxNdf product = FxNdf.builder().settlementCurrencyNotional(CurrencyAmount.of(USD, amount))
				.agreedFxRate(FxRate.of(pair, rate)).index(index).paymentDate(paymentDate).build();

		FxNdfTrade trade = FxNdfTrade.of(TradeInfo.empty(), product);

		return trade;
	}

	public final Try<BuiltMarketData> getBuiltMarketData() {
		return readBean(getClass(), "/builtMarketData.json", BuiltMarketData.class);
	}

	public final Try<RatesCurveGroupDefinition> getCurveGroupDefinition() {
		return readBean(getClass(), "/curveGroupDefinition.json", RatesCurveGroupDefinition.class);
	}

	public final Try<CalculationRules> getRules() {
		CalculationFunctions functions = StandardComponents.calculationFunctions()
				.composedWith(CalculationFunctions.of(new FxFlexibleForwardTradeCalculationFunction()))
				.composedWith(new FxSingleTradeForwardPointsFunction());

		return getCurveGroupDefinition().map(def -> CalculationRules.of(functions, RatesMarketDataLookup.of(def)));

	}

	public final Try<Results> price(List<Trade> trades, List<Measure> measures) {

		List<Column> columns = measures.map(Column::of);

		Try<Results> results = getRules()
				.flatMap(r -> getBuiltMarketData().flatMap(m -> Try.withResources(CalculationRunner::ofMultiThreaded)
						.of(runner -> runner.calculate(r, trades.toJavaList(), columns.toJavaList(), m, REF_DATA))));
		return results;
	}

	private final <T, U> Try<T> readBean(Class<U> cls1, String resource, Class<T> cls2) {
		return readBean(cls1.getResource(resource), cls2);
	}

	private final <T> Try<T> readBean(URL url, Class<T> cls) {
		return Try.of(() -> ResourceUtils.getFile(url))
				.flatMap(md -> Try.of(() -> new FileReader(md))
						.map(reader -> JodaBeanSer.COMPACT.jsonReader().read(reader, cls)))
				.onFailure(t -> LOG.error(t.getMessage()));
	}

	@Test
	public void testFxForward() {
		Try<Results> results = price(List.of(USD_CAD_FWD_6M),
				List.of(Measures.PRESENT_VALUE, Measures.FORWARD_FX_RATE));

		Try<CurrencyAmount> npv = results.map(r -> r.get(0, 0)).map(r -> (CurrencyAmount) r.getValue());
		Try<FxRate> fwdRate = results.map(r -> r.get(0, 1)).map(r -> (FxRate) r.getValue());

		npv.onFailure(t -> fail(t.getMessage()))
				.onSuccess(v -> assertEquals(v.getAmount(), -53966.34033830941, EPSILON));
		fwdRate.onFailure(t -> fail(t.getMessage()))
				.onSuccess(r -> assertEquals(r.fxRate(r.getPair()), 1.298282658708429, EPSILON));
	}

	@Test
	public void testFxNdf() {
		Try<Results> results = price(List.of(USD_KRW_NDF_6M), List.of(Measures.PRESENT_VALUE));

		Try<CurrencyAmount> npv = results.map(r -> r.get(0, 0)).map(r -> (CurrencyAmount) r.getValue());

		npv.onFailure(t -> fail(t.getMessage()))
				.onSuccess(v -> assertEquals(v.getAmount(), -53966.34033830941, EPSILON));
	}

	@Test
	public void testFxForwardCustom() {
		Try<Results> results = price(List.of(USD_CAD_FWD_6M), List.of(ExtendedMeasures.FX_SWAP_RATE));

		Try<Double> fwdPts = results.map(r -> r.get(0, 0)).map(r -> (Double) r.getValue());

		fwdPts.onFailure(t -> fail(t.getMessage())).onSuccess(v -> assertEquals(-0.004167341291570814, v, EPSILON));
	}
}
