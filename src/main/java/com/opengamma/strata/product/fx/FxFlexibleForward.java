/*
 * Copyright (C) 2018 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.product.fx;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.ImmutablePreBuild;
import org.joda.beans.gen.ImmutableValidator;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.beans.impl.direct.DirectPrivateBeanBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.Resolvable;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.currency.Payment;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConventions;
import com.opengamma.strata.basics.date.HolidayCalendarIds;
import com.opengamma.strata.collect.ArgChecker;

import io.vavr.collection.Stream;

/**
 * @author rlewis
 *
 */
@BeanDefinition(builderScope = "private")
public final class FxFlexibleForward implements FxProduct, Resolvable<ResolvedFxFlexibleForward>, ImmutableBean, Serializable {


  private static final BusinessDayAdjustment DEFAULT_BDA = BusinessDayAdjustment.of(BusinessDayConventions.FOLLOWING,
      HolidayCalendarIds.NO_HOLIDAYS);

  /**
   * The amount in the base currency, positive if receiving, negative if paying.
   * <p>
   * The amount is signed. A positive amount indicates the payment is to be
   * received. A negative amount indicates the payment is to be paid.
   */
  @PropertyDefinition(validate = "notNull")
  private final CurrencyAmount baseCurrencyAmount;

  /**
   * The amount in the counter currency, positive if receiving, negative if
   * paying.
   * <p>
   * The amount is signed. A positive amount indicates the payment is to be
   * received. A negative amount indicates the payment is to be paid.
   */
  @PropertyDefinition(validate = "notNull")
  private final CurrencyAmount counterCurrencyAmount;

  /**
   * The date that the FX settles.
   * <p>
   * On this date, the pay and receive amounts will be exchanged. This date is
   * typically a valid business day, however the {@code businessDayAdjustment}
   * property may be used to adjust it.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate paymentDate;

  /**
   * The payment date adjustment, optional.
   * <p>
   * If present, the adjustment will be applied to the payment date.
   */
  @PropertyDefinition(get = "optional")
  private final BusinessDayAdjustment paymentDateAdjustment;

  /**
   * Start of the window period, optional.
   * <p>
   * If present, must be before the windowEnd and after the trade date.
   */
  @PropertyDefinition(get = "optional")
  private final LocalDate executionPeriodStart;

  /**
   * End of the window period, optional.
   * <p>
   * If present, must be after the windowStart and before the expiry date.
   */
  @PropertyDefinition(get = "optional")
  private final LocalDate executionPeriodEnd;

  /**
   * End of the window period, optional.
   * <p>
   * If present, must be after the windowStart and before the expiry date.
   */
  @PropertyDefinition(get = "optional")
  private final List<WindowDate> executionPeriodDates;

  // -------------------------------------------------------------------------
  /**
   * Creates an {@code FxFlexibleForward} from two amounts, the value date and the
   * window start and end dates
   * <p>
   * The amounts must be of the correct type, one pay and one receive. The
   * currencies of the payments must differ.
   * <p>
   * This factory identifies the currency pair of the exchange and assigns the
   * payments to match the base or counter currency of the standardized currency
   * pair. For example, a EUR/USD exchange always has EUR as the base payment and
   * USD as the counter payment.
   * <p>
   * No payment date adjustments apply.
   *
   * @param amount1
   *          the amount in the first currency
   * @param amount2
   *          the amount in the second currency
   * @param paymentDate
   *          the date that the FX settles
   * @param windowStart
   *          the start of the window period
   * @param windowEnd
   *          the end of the window period
   * @return the FX Flexible Forward
   */
  public static FxFlexibleForward of(CurrencyAmount amount1, CurrencyAmount amount2, LocalDate paymentDate,
      LocalDate windowStart, LocalDate windowEnd) {
    return create(amount1, amount2, paymentDate, null, windowStart, windowEnd);
  }

  /**
   * Creates an {@code FxFlexibleForward} from two amounts and the value date,
   * specifying a date adjustment.
   * <p>
   * The amounts must be of the correct type, one pay and one receive. The
   * currencies of the payments must differ.
   * <p>
   * This factory identifies the currency pair of the exchange and assigns the
   * payments to match the base or counter currency of the standardized currency
   * pair. For example, a EUR/USD exchange always has EUR as the base payment and
   * USD as the counter payment.
   *
   * @param amount1
   *          the amount in the first currency
   * @param amount2
   *          the amount in the second currency
   * @param paymentDate
   *          the date that the FX settles
   * @param paymentDateAdjustment
   *          the adjustment to apply to the payment date
   * @param windowStart
   *          the start of the window period
   * @param windowEnd
   *          the end of the window period
   * @return the FX Flexible Forward
   */
  public static FxFlexibleForward of(CurrencyAmount amount1, CurrencyAmount amount2, LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment, LocalDate windowStart, LocalDate windowEnd) {

    ArgChecker.notNull(paymentDateAdjustment, "paymentDateAdjustment");
    return create(amount1, amount2, paymentDate, paymentDateAdjustment, windowStart, windowEnd);
  }

  /**
   * Creates an {@code FxFlexibleForward} using a rate.
   * <p>
   * This creates a single foreign exchange specifying the amount, FX rate and
   * value date. The amount must be specified using one of the currencies of the
   * FX rate.
   * <p>
   * This factory identifies the currency pair of the exchange and assigns the
   * payments to match the base or counter currency of the standardized currency
   * pair. For example, a EUR/USD exchange always has EUR as the base payment and
   * USD as the counter payment.
   * <p>
   * No payment date adjustments apply.
   *
   * @param amount
   *          the amount being exchanged, positive if being received, negative if
   *          being paid
   * @param fxRate
   *          the FX rate
   * @param paymentDate
   *          the date that the FX settles
   * @param windowStart
   *          the start of the window period
   * @param windowEnd
   *          the end of the window period
   * @return the FX
   */
  public static FxFlexibleForward of(CurrencyAmount amount, FxRate fxRate, LocalDate paymentDate, LocalDate windowStart,
      LocalDate windowEnd) {
    return create(amount, fxRate, paymentDate, null, windowStart, windowEnd);
  }

  /**
   * Creates an {@code FxFlexibleForward} using a rate, specifying a date
   * adjustment.
   * <p>
   * This creates a single foreign exchange specifying the amount, FX rate and
   * value date. The amount must be specified using one of the currencies of the
   * FX rate.
   * <p>
   * This factory identifies the currency pair of the exchange and assigns the
   * payments to match the base or counter currency of the standardized currency
   * pair. For example, a EUR/USD exchange always has EUR as the base payment and
   * USD as the counter payment.
   *
   * @param amount
   *          the amount being exchanged, positive if being received, negative if
   *          being paid
   * @param fxRate
   *          the FX rate
   * @param paymentDate
   *          the date that the FX settles
   * @param paymentDateAdjustment
   *          the adjustment to apply to the payment date
   * @param windowStart
   *          the start of the window period
   * @param windowEnd
   *          the end of the window period
   * @return the FX
   */
  public static FxFlexibleForward of(CurrencyAmount amount, FxRate fxRate, LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment, LocalDate windowStart, LocalDate windowEnd) {

    ArgChecker.notNull(paymentDateAdjustment, "paymentDateAdjustment");
    return create(amount, fxRate, paymentDate, paymentDateAdjustment, windowStart, windowEnd);
  }

  /**
   * @param amount
   *          the amount being exchanged, positive if being received, negative if
   *          being paid
   * @param fxRate
   *          the FX rate
   * @param paymentDate
   *          the date that the FX settles
   * @param paymentDateAdjustment
   *          the adjustment to apply to the payment date
   * @param windowDates
   *          array of WindowDate defining window period
   * @return FxFlexibleForward
   */
  public static FxFlexibleForward of(CurrencyAmount amount, FxRate fxRate, LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment, List<WindowDate> windowDates) {

    ArgChecker.notNull(paymentDateAdjustment, "paymentDateAdjustment");
    return create(amount, fxRate, paymentDate, paymentDateAdjustment, windowDates);
  }

  /**
   * @param baseCcyAmount
   *          the amount being exchanged, positive if being received, negative if
   *          being paid
   * @param cntrCcyAmount
   *          the amount being exchanged, positive if being received, negative if
   *          being paid
   * @param paymentDate
   *          the date that the FX settles
   * @param paymentDateAdjustment
   *          the adjustment to apply to the payment date
   * @param windowDates
   *          array of WindowDate defining window period
   * @return FxFlexibleForward
   */
  public static FxFlexibleForward of(CurrencyAmount baseCcyAmount, CurrencyAmount cntrCcyAmount, LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment, List<WindowDate> windowDates) {
    return create(baseCcyAmount, cntrCcyAmount, paymentDate, paymentDateAdjustment, windowDates);
  }

  private static FxFlexibleForward create(CurrencyAmount amount, FxRate fxRate, LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment, LocalDate windowStart, LocalDate windowEnd) {

    final CurrencyPair pair = fxRate.getPair();
    ArgChecker.isTrue(pair.contains(amount.getCurrency()));
    final Currency currency2 = pair.getBase().equals(amount.getCurrency()) ? pair.getCounter() : pair.getBase();
    final CurrencyAmount amountCurrency2 = amount.convertedTo(currency2, fxRate).negated();
    return create(amount, amountCurrency2, paymentDate, paymentDateAdjustment, windowStart, windowEnd);
  }

  private static FxFlexibleForward create(CurrencyAmount amount, FxRate fxRate, LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment, List<WindowDate> windowDates) {

    final CurrencyPair pair = fxRate.getPair();
    ArgChecker.isTrue(pair.contains(amount.getCurrency()));
    final Currency currency2 = pair.getBase().equals(amount.getCurrency()) ? pair.getCounter() : pair.getBase();
    final CurrencyAmount amountCurrency2 = amount.convertedTo(currency2, fxRate).negated();
    return create(amount, amountCurrency2, paymentDate, paymentDateAdjustment, windowDates);
  }

  // internal method where adjustment may be null
  private static FxFlexibleForward create(CurrencyAmount amount1, CurrencyAmount amount2, LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment, LocalDate windowStart, LocalDate windowEnd) {

    final CurrencyPair pair = CurrencyPair.of(amount2.getCurrency(), amount1.getCurrency());
    if (pair.isConventional()) {
      return new FxFlexibleForward(amount2, amount1, paymentDate, paymentDateAdjustment, windowStart, windowEnd, null);
    } else {
      return new FxFlexibleForward(amount1, amount2, paymentDate, paymentDateAdjustment, windowStart, windowEnd, null);
    }
  }

  // internal method where adjustment may be null
  private static FxFlexibleForward create(CurrencyAmount amount1, CurrencyAmount amount2, LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment, List<WindowDate> windowDates) {

    final CurrencyPair pair = CurrencyPair.of(amount2.getCurrency(), amount1.getCurrency());
    if (pair.isConventional()) {
      return new FxFlexibleForward(amount2, amount1, paymentDate, paymentDateAdjustment, null, null, windowDates);
    } else {
      return new FxFlexibleForward(amount1, amount2, paymentDate, paymentDateAdjustment, null, null, windowDates);
    }
  }

  private static <T> boolean compare(T a, T b, BiFunction<T, T, Boolean> compFunc) {
    return Optional.ofNullable(a).flatMap(s -> Optional.ofNullable(b).map(e -> compFunc.apply(s, e))).orElse(false);
  }

  private static <T extends Comparable<T>> boolean isOrdered(List<T> elements) {
    return elements == null
        || IntStream.range(0, elements.size() - 1).allMatch(i -> elements.get(i).compareTo(elements.get(i + 1)) <= 0);
  }

  // -------------------------------------------------------------------------
  @ImmutableValidator
  private void validate() {
    if (baseCurrencyAmount.getCurrency().equals(counterCurrencyAmount.getCurrency())) {
      throw new IllegalArgumentException("Amounts must have different currencies");
    }

    if ((baseCurrencyAmount.getAmount() != 0d || counterCurrencyAmount.getAmount() != 0d)
        && Math.signum(baseCurrencyAmount.getAmount()) != -Math.signum(counterCurrencyAmount.getAmount())) {
      throw new IllegalArgumentException("Amounts must have different signs");
    }

    if (executionPeriodStart == null && executionPeriodEnd == null
        && (executionPeriodDates == null || executionPeriodDates.isEmpty())) {
      throw new IllegalArgumentException("Window dates must be provided");
    }

    if (executionPeriodStart == null && executionPeriodEnd != null
        && (executionPeriodDates == null || executionPeriodDates.isEmpty())) {
      throw new IllegalArgumentException("executionPeriodStart must be provided");
    }

    if (executionPeriodStart != null && executionPeriodEnd == null
        && (executionPeriodDates == null || executionPeriodDates.isEmpty())) {
      throw new IllegalArgumentException("executionPeriodEnd must be provided");
    }

    if (compare(executionPeriodStart, executionPeriodEnd, LocalDate::isAfter)) {
      throw new IllegalArgumentException("executionPeriodStart must be before executionPeriodEnd");
    }

    if (compare(executionPeriodEnd, paymentDate, LocalDate::isAfter)) {
      throw new IllegalArgumentException("executionPeriodEnd must be before paymentDate");
    }

    if (!isOrdered(executionPeriodDates)) {
      throw new IllegalArgumentException("executionPeriodDates must be in ascending order");
    }
  }

  @ImmutablePreBuild
  private static void preBuild(Builder builder) {
    // swap order to be base/counter if reverse is conventional
    // this handles deserialization where the base/counter rules differ from those
    // applicable at serialization
    final CurrencyAmount base = builder.baseCurrencyAmount;
    final CurrencyAmount counter = builder.counterCurrencyAmount;
    final CurrencyPair pair = CurrencyPair.of(counter.getCurrency(), base.getCurrency());
    if (pair.isConventional()) {
      builder.baseCurrencyAmount = counter;
      builder.counterCurrencyAmount = base;
    }
  }

  @Override
  public ImmutableSet<Currency> allCurrencies() {
    return ImmutableSet.of(this.baseCurrencyAmount.getCurrency(), this.counterCurrencyAmount.getCurrency());
  }

  // -------------------------------------------------------------------------
  /**
   * Gets currency pair of the base currency and counter currency.
   * <p>
   * This currency pair is conventional, thus indifferent to the direction of FX.
   *
   * @return the currency pair
   */
  public CurrencyPair getCurrencyPair() {
    return CurrencyPair.of(baseCurrencyAmount.getCurrency(), counterCurrencyAmount.getCurrency());
  }

  /**
   * Gets the currency amount in which the amount is received.
   * <p>
   * This returns the currency amount whose amount is non-negative. If both are
   * zero, {@code counterCurrencyAmount} is returned.
   *
   * @return the receive currency amount
   */
  public CurrencyAmount getReceiveCurrencyAmount() {
    if (baseCurrencyAmount.getAmount() > 0d) {
      return baseCurrencyAmount;
    }
    return counterCurrencyAmount;
  }

  // -------------------------------------------------------------------------
  @Override
  public ResolvedFxFlexibleForward resolve(ReferenceData refData) {

    final BusinessDayAdjustment dateAdjustment = getPaymentDateAdjustment().orElse(DEFAULT_BDA);

    final LocalDate date = dateAdjustment.adjust(paymentDate, refData);

    // Generate payment dates from start/end if window dates are not provided
    final List<LocalDate> adjDates = this.getExecutionPeriodDates()
        .flatMap(d -> Optional.of(Stream.ofAll(d).map(WindowDate::getWindowDate).toJavaList()))
        .orElseGet(() -> Stream.iterate(this.getExecutionPeriodStart().get(), dt -> dt.plusDays(1))
            .takeUntil(dt -> dt.isEqual(this.getExecutionPeriodEnd().get()))
            .map(dt -> dateAdjustment.adjust(dt, refData)).distinct().toJavaList());

    return ResolvedFxFlexibleForward.of(Payment.of(baseCurrencyAmount, date), Payment.of(counterCurrencyAmount, date),
        adjDates);
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code FxFlexibleForward}.
   * @return the meta-bean, not null
   */
  public static FxFlexibleForward.Meta meta() {
    return FxFlexibleForward.Meta.INSTANCE;
  }

  static {
    MetaBean.register(FxFlexibleForward.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private FxFlexibleForward(
      CurrencyAmount baseCurrencyAmount,
      CurrencyAmount counterCurrencyAmount,
      LocalDate paymentDate,
      BusinessDayAdjustment paymentDateAdjustment,
      LocalDate executionPeriodStart,
      LocalDate executionPeriodEnd,
      List<WindowDate> executionPeriodDates) {
    JodaBeanUtils.notNull(baseCurrencyAmount, "baseCurrencyAmount");
    JodaBeanUtils.notNull(counterCurrencyAmount, "counterCurrencyAmount");
    JodaBeanUtils.notNull(paymentDate, "paymentDate");
    this.baseCurrencyAmount = baseCurrencyAmount;
    this.counterCurrencyAmount = counterCurrencyAmount;
    this.paymentDate = paymentDate;
    this.paymentDateAdjustment = paymentDateAdjustment;
    this.executionPeriodStart = executionPeriodStart;
    this.executionPeriodEnd = executionPeriodEnd;
    this.executionPeriodDates = (executionPeriodDates != null ? ImmutableList.copyOf(executionPeriodDates) : null);
    validate();
  }

  @Override
  public FxFlexibleForward.Meta metaBean() {
    return FxFlexibleForward.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount in the base currency, positive if receiving, negative if paying.
   * <p>
   * The amount is signed. A positive amount indicates the payment is to be
   * received. A negative amount indicates the payment is to be paid.
   * @return the value of the property, not null
   */
  public CurrencyAmount getBaseCurrencyAmount() {
    return baseCurrencyAmount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount in the counter currency, positive if receiving, negative if
   * paying.
   * <p>
   * The amount is signed. A positive amount indicates the payment is to be
   * received. A negative amount indicates the payment is to be paid.
   * @return the value of the property, not null
   */
  public CurrencyAmount getCounterCurrencyAmount() {
    return counterCurrencyAmount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date that the FX settles.
   * <p>
   * On this date, the pay and receive amounts will be exchanged. This date is
   * typically a valid business day, however the {@code businessDayAdjustment}
   * property may be used to adjust it.
   * @return the value of the property, not null
   */
  public LocalDate getPaymentDate() {
    return paymentDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment date adjustment, optional.
   * <p>
   * If present, the adjustment will be applied to the payment date.
   * @return the optional value of the property, not null
   */
  public Optional<BusinessDayAdjustment> getPaymentDateAdjustment() {
    return Optional.ofNullable(paymentDateAdjustment);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets start of the window period, optional.
   * <p>
   * If present, must be before the windowEnd and after the trade date.
   * @return the optional value of the property, not null
   */
  public Optional<LocalDate> getExecutionPeriodStart() {
    return Optional.ofNullable(executionPeriodStart);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets end of the window period, optional.
   * <p>
   * If present, must be after the windowStart and before the expiry date.
   * @return the optional value of the property, not null
   */
  public Optional<LocalDate> getExecutionPeriodEnd() {
    return Optional.ofNullable(executionPeriodEnd);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets end of the window period, optional.
   * <p>
   * If present, must be after the windowStart and before the expiry date.
   * @return the optional value of the property, not null
   */
  public Optional<List<WindowDate>> getExecutionPeriodDates() {
    return Optional.ofNullable(executionPeriodDates);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FxFlexibleForward other = (FxFlexibleForward) obj;
      return JodaBeanUtils.equal(baseCurrencyAmount, other.baseCurrencyAmount) &&
          JodaBeanUtils.equal(counterCurrencyAmount, other.counterCurrencyAmount) &&
          JodaBeanUtils.equal(paymentDate, other.paymentDate) &&
          JodaBeanUtils.equal(paymentDateAdjustment, other.paymentDateAdjustment) &&
          JodaBeanUtils.equal(executionPeriodStart, other.executionPeriodStart) &&
          JodaBeanUtils.equal(executionPeriodEnd, other.executionPeriodEnd) &&
          JodaBeanUtils.equal(executionPeriodDates, other.executionPeriodDates);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(baseCurrencyAmount);
    hash = hash * 31 + JodaBeanUtils.hashCode(counterCurrencyAmount);
    hash = hash * 31 + JodaBeanUtils.hashCode(paymentDate);
    hash = hash * 31 + JodaBeanUtils.hashCode(paymentDateAdjustment);
    hash = hash * 31 + JodaBeanUtils.hashCode(executionPeriodStart);
    hash = hash * 31 + JodaBeanUtils.hashCode(executionPeriodEnd);
    hash = hash * 31 + JodaBeanUtils.hashCode(executionPeriodDates);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("FxFlexibleForward{");
    buf.append("baseCurrencyAmount").append('=').append(baseCurrencyAmount).append(',').append(' ');
    buf.append("counterCurrencyAmount").append('=').append(counterCurrencyAmount).append(',').append(' ');
    buf.append("paymentDate").append('=').append(paymentDate).append(',').append(' ');
    buf.append("paymentDateAdjustment").append('=').append(paymentDateAdjustment).append(',').append(' ');
    buf.append("executionPeriodStart").append('=').append(executionPeriodStart).append(',').append(' ');
    buf.append("executionPeriodEnd").append('=').append(executionPeriodEnd).append(',').append(' ');
    buf.append("executionPeriodDates").append('=').append(JodaBeanUtils.toString(executionPeriodDates));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FxFlexibleForward}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code baseCurrencyAmount} property.
     */
    private final MetaProperty<CurrencyAmount> baseCurrencyAmount = DirectMetaProperty.ofImmutable(
        this, "baseCurrencyAmount", FxFlexibleForward.class, CurrencyAmount.class);
    /**
     * The meta-property for the {@code counterCurrencyAmount} property.
     */
    private final MetaProperty<CurrencyAmount> counterCurrencyAmount = DirectMetaProperty.ofImmutable(
        this, "counterCurrencyAmount", FxFlexibleForward.class, CurrencyAmount.class);
    /**
     * The meta-property for the {@code paymentDate} property.
     */
    private final MetaProperty<LocalDate> paymentDate = DirectMetaProperty.ofImmutable(
        this, "paymentDate", FxFlexibleForward.class, LocalDate.class);
    /**
     * The meta-property for the {@code paymentDateAdjustment} property.
     */
    private final MetaProperty<BusinessDayAdjustment> paymentDateAdjustment = DirectMetaProperty.ofImmutable(
        this, "paymentDateAdjustment", FxFlexibleForward.class, BusinessDayAdjustment.class);
    /**
     * The meta-property for the {@code executionPeriodStart} property.
     */
    private final MetaProperty<LocalDate> executionPeriodStart = DirectMetaProperty.ofImmutable(
        this, "executionPeriodStart", FxFlexibleForward.class, LocalDate.class);
    /**
     * The meta-property for the {@code executionPeriodEnd} property.
     */
    private final MetaProperty<LocalDate> executionPeriodEnd = DirectMetaProperty.ofImmutable(
        this, "executionPeriodEnd", FxFlexibleForward.class, LocalDate.class);
    /**
     * The meta-property for the {@code executionPeriodDates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<WindowDate>> executionPeriodDates = DirectMetaProperty.ofImmutable(
        this, "executionPeriodDates", FxFlexibleForward.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "baseCurrencyAmount",
        "counterCurrencyAmount",
        "paymentDate",
        "paymentDateAdjustment",
        "executionPeriodStart",
        "executionPeriodEnd",
        "executionPeriodDates");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 714419450:  // baseCurrencyAmount
          return baseCurrencyAmount;
        case -446491419:  // counterCurrencyAmount
          return counterCurrencyAmount;
        case -1540873516:  // paymentDate
          return paymentDate;
        case 737375073:  // paymentDateAdjustment
          return paymentDateAdjustment;
        case -542110487:  // executionPeriodStart
          return executionPeriodStart;
        case -1323481310:  // executionPeriodEnd
          return executionPeriodEnd;
        case -556511476:  // executionPeriodDates
          return executionPeriodDates;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FxFlexibleForward> builder() {
      return new FxFlexibleForward.Builder();
    }

    @Override
    public Class<? extends FxFlexibleForward> beanType() {
      return FxFlexibleForward.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code baseCurrencyAmount} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CurrencyAmount> baseCurrencyAmount() {
      return baseCurrencyAmount;
    }

    /**
     * The meta-property for the {@code counterCurrencyAmount} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CurrencyAmount> counterCurrencyAmount() {
      return counterCurrencyAmount;
    }

    /**
     * The meta-property for the {@code paymentDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> paymentDate() {
      return paymentDate;
    }

    /**
     * The meta-property for the {@code paymentDateAdjustment} property.
     * @return the meta-property, not null
     */
    public MetaProperty<BusinessDayAdjustment> paymentDateAdjustment() {
      return paymentDateAdjustment;
    }

    /**
     * The meta-property for the {@code executionPeriodStart} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> executionPeriodStart() {
      return executionPeriodStart;
    }

    /**
     * The meta-property for the {@code executionPeriodEnd} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> executionPeriodEnd() {
      return executionPeriodEnd;
    }

    /**
     * The meta-property for the {@code executionPeriodDates} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<WindowDate>> executionPeriodDates() {
      return executionPeriodDates;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 714419450:  // baseCurrencyAmount
          return ((FxFlexibleForward) bean).getBaseCurrencyAmount();
        case -446491419:  // counterCurrencyAmount
          return ((FxFlexibleForward) bean).getCounterCurrencyAmount();
        case -1540873516:  // paymentDate
          return ((FxFlexibleForward) bean).getPaymentDate();
        case 737375073:  // paymentDateAdjustment
          return ((FxFlexibleForward) bean).paymentDateAdjustment;
        case -542110487:  // executionPeriodStart
          return ((FxFlexibleForward) bean).executionPeriodStart;
        case -1323481310:  // executionPeriodEnd
          return ((FxFlexibleForward) bean).executionPeriodEnd;
        case -556511476:  // executionPeriodDates
          return ((FxFlexibleForward) bean).executionPeriodDates;
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code FxFlexibleForward}.
   */
  private static final class Builder extends DirectPrivateBeanBuilder<FxFlexibleForward> {

    private CurrencyAmount baseCurrencyAmount;
    private CurrencyAmount counterCurrencyAmount;
    private LocalDate paymentDate;
    private BusinessDayAdjustment paymentDateAdjustment;
    private LocalDate executionPeriodStart;
    private LocalDate executionPeriodEnd;
    private List<WindowDate> executionPeriodDates;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 714419450:  // baseCurrencyAmount
          return baseCurrencyAmount;
        case -446491419:  // counterCurrencyAmount
          return counterCurrencyAmount;
        case -1540873516:  // paymentDate
          return paymentDate;
        case 737375073:  // paymentDateAdjustment
          return paymentDateAdjustment;
        case -542110487:  // executionPeriodStart
          return executionPeriodStart;
        case -1323481310:  // executionPeriodEnd
          return executionPeriodEnd;
        case -556511476:  // executionPeriodDates
          return executionPeriodDates;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 714419450:  // baseCurrencyAmount
          this.baseCurrencyAmount = (CurrencyAmount) newValue;
          break;
        case -446491419:  // counterCurrencyAmount
          this.counterCurrencyAmount = (CurrencyAmount) newValue;
          break;
        case -1540873516:  // paymentDate
          this.paymentDate = (LocalDate) newValue;
          break;
        case 737375073:  // paymentDateAdjustment
          this.paymentDateAdjustment = (BusinessDayAdjustment) newValue;
          break;
        case -542110487:  // executionPeriodStart
          this.executionPeriodStart = (LocalDate) newValue;
          break;
        case -1323481310:  // executionPeriodEnd
          this.executionPeriodEnd = (LocalDate) newValue;
          break;
        case -556511476:  // executionPeriodDates
          this.executionPeriodDates = (List<WindowDate>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public FxFlexibleForward build() {
      preBuild(this);
      return new FxFlexibleForward(
          baseCurrencyAmount,
          counterCurrencyAmount,
          paymentDate,
          paymentDateAdjustment,
          executionPeriodStart,
          executionPeriodEnd,
          executionPeriodDates);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(256);
      buf.append("FxFlexibleForward.Builder{");
      buf.append("baseCurrencyAmount").append('=').append(JodaBeanUtils.toString(baseCurrencyAmount)).append(',').append(' ');
      buf.append("counterCurrencyAmount").append('=').append(JodaBeanUtils.toString(counterCurrencyAmount)).append(',').append(' ');
      buf.append("paymentDate").append('=').append(JodaBeanUtils.toString(paymentDate)).append(',').append(' ');
      buf.append("paymentDateAdjustment").append('=').append(JodaBeanUtils.toString(paymentDateAdjustment)).append(',').append(' ');
      buf.append("executionPeriodStart").append('=').append(JodaBeanUtils.toString(executionPeriodStart)).append(',').append(' ');
      buf.append("executionPeriodEnd").append('=').append(JodaBeanUtils.toString(executionPeriodEnd)).append(',').append(' ');
      buf.append("executionPeriodDates").append('=').append(JodaBeanUtils.toString(executionPeriodDates));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
