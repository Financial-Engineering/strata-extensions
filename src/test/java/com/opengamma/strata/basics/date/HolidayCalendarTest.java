package com.opengamma.strata.basics.date;

import static com.opengamma.strata.basics.date.ExtendedHolidayCalendarIds.HKHK;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import com.opengamma.strata.basics.ReferenceData;

public class HolidayCalendarTest {

	//@Test
	public void testHKHK() {
		HolidayCalendar HKCAL = HKHK.resolve(ReferenceData.standard());
		
		assertTrue(HKCAL.isHoliday(LocalDate.of(2018, 2, 16)));
		assertTrue(HKCAL.isHoliday(LocalDate.of(2018, 2, 19)));
		assertFalse(HKCAL.isHoliday(LocalDate.of(2018, 2, 20)));
	}

	//@Test
	public void testKRSE() {
		HolidayCalendar HKCAL = HKHK.resolve(ReferenceData.standard());
		
		assertTrue(HKCAL.isHoliday(LocalDate.of(2018, 2, 4)));
		assertFalse(HKCAL.isHoliday(LocalDate.of(2018, 2, 6)));
	}
}
