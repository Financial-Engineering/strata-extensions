/*
 * Copyright (C) 2018 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.product.fx.type;

import static com.opengamma.strata.basics.currency.Currency.AUD;
import static com.opengamma.strata.basics.currency.Currency.BRL;
import static com.opengamma.strata.basics.currency.Currency.CAD;
import static com.opengamma.strata.basics.currency.Currency.CHF;
import static com.opengamma.strata.basics.currency.Currency.DKK;
import static com.opengamma.strata.basics.currency.Currency.HKD;
import static com.opengamma.strata.basics.currency.Currency.INR;
import static com.opengamma.strata.basics.currency.Currency.JPY;
import static com.opengamma.strata.basics.currency.Currency.MXN;
import static com.opengamma.strata.basics.currency.Currency.MYR;
import static com.opengamma.strata.basics.currency.Currency.NOK;
import static com.opengamma.strata.basics.currency.Currency.NZD;
import static com.opengamma.strata.basics.currency.Currency.SEK;
import static com.opengamma.strata.basics.currency.Currency.USD;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.AUSY;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.BRBD;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.CATO;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.CHZU;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.DKCO;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.JPTO;

import static com.opengamma.strata.basics.date.HolidayCalendarIds.MXMC;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.NOOS;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.NZAU;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.SEST;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.USNY;
import static com.opengamma.strata.basics.date.ExtendedHolidayCalendarIds.HKHK;
import static com.opengamma.strata.basics.date.ExtendedHolidayCalendarIds.INMU;
import static com.opengamma.strata.basics.date.ExtendedHolidayCalendarIds.MYKL;

import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendarId;

public final class ExtendedFxSwapConventions {

  private static final HolidayCalendarId AUSY_USNY = AUSY.combinedWith(USNY);
  private static final HolidayCalendarId USNY_BRBD = USNY.combinedWith(BRBD);
  private static final HolidayCalendarId USNY_CATO = USNY.combinedWith(CATO);
  private static final HolidayCalendarId USNY_CHZU = USNY.combinedWith(CHZU);
  private static final HolidayCalendarId USNY_DKCO = USNY.combinedWith(DKCO);
  private static final HolidayCalendarId USNY_HKHK = USNY.combinedWith(HKHK);
  private static final HolidayCalendarId USNY_INMU = USNY.combinedWith(INMU);
  private static final HolidayCalendarId USNY_JPTO = USNY.combinedWith(JPTO);
  private static final HolidayCalendarId USNY_MXMC = USNY.combinedWith(MXMC);
  private static final HolidayCalendarId USNY_MYKL = USNY.combinedWith(MYKL);
  private static final HolidayCalendarId NZAU_USNY = NZAU.combinedWith(USNY);
  private static final HolidayCalendarId USNY_NOOS = USNY.combinedWith(NOOS);
  private static final HolidayCalendarId USNY_SEST = USNY.combinedWith(SEST);

  public static final FxSwapConvention AUD_USD = ImmutableFxSwapConvention.of(CurrencyPair.of(AUD, USD),
      DaysAdjustment.ofBusinessDays(2, AUSY_USNY),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, AUSY_USNY));

  public static final FxSwapConvention USD_BRL = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, BRL),
      DaysAdjustment.ofBusinessDays(2, USNY_BRBD),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_BRBD));

  public static final FxSwapConvention USD_CAD = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, CAD),
      DaysAdjustment.ofBusinessDays(1, USNY_CATO),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_CATO));

  public static final FxSwapConvention USD_CHF = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, CHF),
      DaysAdjustment.ofBusinessDays(2, USNY_CHZU),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_CHZU));

  public static final FxSwapConvention USD_DKK = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, DKK),
      DaysAdjustment.ofBusinessDays(2, USNY_DKCO),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_DKCO));

  public static final FxSwapConvention USD_JPY = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, JPY),
      DaysAdjustment.ofBusinessDays(2, USNY_JPTO),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_JPTO));

  public static final FxSwapConvention USD_HKD = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, HKD),
      DaysAdjustment.ofBusinessDays(2, USNY_HKHK),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_HKHK));

  public static final FxSwapConvention USD_INR = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, INR),
      DaysAdjustment.ofBusinessDays(2, USNY_INMU),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_INMU));

  public static final FxSwapConvention USD_MXN = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, MXN),
      DaysAdjustment.ofBusinessDays(2, USNY_MXMC),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_MXMC));

  public static final FxSwapConvention USD_MYR = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, MYR),
      DaysAdjustment.ofBusinessDays(2, USNY_MYKL),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_MYKL));

  public static final FxSwapConvention USD_NOK = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, NOK),
      DaysAdjustment.ofBusinessDays(2, USNY_NOOS),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_NOOS));

  public static final FxSwapConvention USD_SEK = ImmutableFxSwapConvention.of(CurrencyPair.of(USD, SEK),
      DaysAdjustment.ofBusinessDays(2, USNY_SEST),
      BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, USNY_SEST));

  public static final FxSwapConvention NZD_USD = ImmutableFxSwapConvention.of(CurrencyPair.of(NZD, USD),
       DaysAdjustment.ofBusinessDays(2, NZAU_USNY),
       BusinessDayAdjustment.of(BusinessDayConventions.MODIFIED_FOLLOWING, NZAU_USNY));

  // -------------------------------------------------------------------------
  /**
   * * Restricted constructor.
  */
  private ExtendedFxSwapConventions() {
  }

}
