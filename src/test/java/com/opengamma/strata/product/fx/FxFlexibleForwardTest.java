package com.opengamma.strata.product.fx;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.currency.Currency.USD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendarIds;

import io.vavr.collection.List;
import io.vavr.collection.Stream;

public class FxFlexibleForwardTest {

	private static final ReferenceData REF_DATA = ReferenceData.standard();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEndAfterPayment() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(is("executionPeriodEnd must be before paymentDate"));

		FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY),
				LocalDate.of(2018, 3, 30), LocalDate.of(2018, 7, 30));
	}

	@Test
	public void testNoEnd() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(is("executionPeriodEnd must be provided"));

		FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY), LocalDate.of(2018, 3, 30), null);
	}
	
	@Test
	public void testNoPeriodDates() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(is("Window dates must be provided"));

		FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY), null);
	}
		
	@Test
	public void testNoStart() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(is("executionPeriodStart must be provided"));

		FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY), null, LocalDate.of(2018, 6, 30));
	}
	
	@Test
	public void testNoStartEndDates() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(is("Window dates must be provided"));

		FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY), null, null);
	}
	
	@Test
	public void testPeriodDatesOrdered() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(is("executionPeriodDates must be in ascending order"));
		
		java.util.List<WindowDate> dates = List
				.of(WindowDate.of(LocalDate.of(2018, 6, 30)), WindowDate.of(LocalDate.of(2018, 5, 30)),
						WindowDate.of(LocalDate.of(2018, 4, 30)), WindowDate.of(LocalDate.of(2018, 3, 30)))
				.toJavaList();
		
		FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY), dates);
	}

	@Test
	public void testResolved() {
		java.util.List<WindowDate> dates = List
				.of(WindowDate.of(LocalDate.of(2018, 3, 30)), WindowDate.of(LocalDate.of(2018, 4, 30)),
						WindowDate.of(LocalDate.of(2018, 5, 30)), WindowDate.of(LocalDate.of(2018, 6, 30)))
				.toJavaList();
		
		FxFlexibleForward fxFwd = FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY), dates);

		ResolvedFxFlexibleForward resolvedFxFwd = fxFwd.resolve(REF_DATA);

		assertEquals(Stream.ofAll(dates).map(WindowDate::getWindowDate).toJavaList(), resolvedFxFwd.getWindowDates());
		
		fxFwd = FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY),
				LocalDate.of(2018, 3, 30), LocalDate.of(2018, 6, 30));

		resolvedFxFwd = fxFwd.resolve(REF_DATA);

		assertEquals(resolvedFxFwd.getWindowDates().size(), 65);
	}

	@Test
	public void testStartAfterEnd() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(is("executionPeriodStart must be before executionPeriodEnd"));

		FxFlexibleForward.of(CurrencyAmount.of(USD, 150000), FxRate.of(EUR, USD, 1.20),
				LocalDate.of(2018, 6, 30),
				BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING, HolidayCalendarIds.USNY),
				LocalDate.of(2018, 6, 30), LocalDate.of(2018, 3, 30));
	}

}
