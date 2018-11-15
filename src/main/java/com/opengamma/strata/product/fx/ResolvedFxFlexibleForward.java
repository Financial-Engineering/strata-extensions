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
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.currency.Payment;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.product.ResolvedProduct;

/**
 * A single FX Flexible Forward transaction, resolved for pricing.
 * <p>
 * This is the resolved form of {@link FxFlexibleForward} and is an input to the
 * pricers. Applications will typically create a
 * {@code ResolvedFxFlexibleForward} from a {@code FxFlexibleForward} using
 * {@link FxFlexibleForward#resolve(ReferenceData)}.
 * <p>
 * The two payments are identified as the base and counter currencies in a
 * standardized currency pair. For example, a EUR/USD exchange always has EUR as
 * the base payment and USD as the counter payment. See {@link CurrencyPair} for
 * details of the configuration that determines the ordering.
 * <p>
 * A {@code ResolvedFxFlexibleForward} is bound to data that changes over time,
 * such as holiday calendars. If the data changes, such as the addition of a new
 * holiday, the resolved form will not be updated. Care must be taken when
 * placing the resolved form in a cache or persistence layer.
 */
@BeanDefinition(builderScope = "private")
public final class ResolvedFxFlexibleForward implements ResolvedProduct, ImmutableBean, Serializable {

  /**
   * The payment in the base currency, positive if receiving, negative if paying.
   * <p>
   * The payment amount is signed. A positive amount indicates the payment is to
   * be received. A negative amount indicates the payment is to be paid.
   */
  @PropertyDefinition(validate = "notNull")
  private final Payment baseCurrencyPayment;
  /**
   * The payment in the counter currency, positive if receiving, negative if
   * paying.
   * <p>
   * The payment amount is signed. A positive amount indicates the payment is to
   * be received. A negative amount indicates the payment is to be paid.
   */
  @PropertyDefinition(validate = "notNull")
  private final Payment counterCurrencyPayment;

  @PropertyDefinition(validate = "notNull")
  private final List<LocalDate> windowDates;

  // -------------------------------------------------------------------------
  /**
   * Creates an {@code ResolvedFxFlexibleForward} from two equivalent payments in
   * different currencies.
   * <p>
   * The payments must be of the correct type, one pay and one receive. The
   * currencies of the payments must differ.
   * <p>
   * This factory identifies the currency pair of the exchange and assigns the
   * payments to match the base or counter currency of the standardized currency
   * pair. For example, a EUR/USD exchange always has EUR as the base payment and
   * USD as the counter payment.
   *
   * @param payment1
   *          the first payment
   * @param payment2
   *          the second payment
   * @param windowDates
   *          an array of windows observation dates
   * @return the resolved foreign exchange transaction
   */
  public static ResolvedFxFlexibleForward of(Payment payment1, Payment payment2, List<LocalDate> windowDates) {
    final CurrencyPair pair = CurrencyPair.of(payment2.getCurrency(), payment1.getCurrency());
    if (pair.isConventional()) {
      return new ResolvedFxFlexibleForward(payment2, payment1, windowDates);
    } else {
      return new ResolvedFxFlexibleForward(payment1, payment2, windowDates);
    }
  }

  /**
   * Creates an {@code ResolvedFxFlexibleForward} from two amounts and the value
   * date.
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
   * @param valueDate
   *          the value date
   * @param windowDates
   *          an array of windows observation dates
   * @return the resolved foreign exchange transaction
   */
  public static ResolvedFxFlexibleForward of(CurrencyAmount amount1, CurrencyAmount amount2, LocalDate valueDate,
      List<LocalDate> windowDates) {
    return ResolvedFxFlexibleForward.of(Payment.of(amount1, valueDate), Payment.of(amount2, valueDate), windowDates);
  }

  /**
   * Creates an {@code ResolvedFxFlexibleForward} using a rate.
   * <p>
   * This create an FX specifying a value date, notional in one currency, the
   * second currency and the FX rate between the two. The currencies of the
   * payments must differ.
   * <p>
   * This factory identifies the currency pair of the exchange and assigns the
   * payments to match the base or counter currency of the standardized currency
   * pair. For example, a EUR/USD exchange always has EUR as the base payment and
   * USD as the counter payment.
   * <p>
   * No payment date adjustments apply.
   *
   * @param amountCurrency1
   *          the amount of the near leg in the first currency
   * @param fxRate
   *          the near FX rate
   * @param paymentDate
   *          date that the FX settles
   * @param windowDates
   *          an array of windows observation dates
   * @return the resolved foreign exchange transaction
   */
  public static ResolvedFxFlexibleForward of(CurrencyAmount amountCurrency1, FxRate fxRate, LocalDate paymentDate,
      List<LocalDate> windowDates) {
    final CurrencyPair pair = fxRate.getPair();
    ArgChecker.isTrue(pair.contains(amountCurrency1.getCurrency()));
    final Currency currency2 = pair.getBase().equals(amountCurrency1.getCurrency()) ? pair.getCounter()
        : pair.getBase();
    final CurrencyAmount amountCurrency2 = amountCurrency1.convertedTo(currency2, fxRate).negated();
    return ResolvedFxFlexibleForward.of(Payment.of(amountCurrency1, paymentDate),
        Payment.of(amountCurrency2, paymentDate), windowDates);
  }

  // -------------------------------------------------------------------------
  @ImmutableValidator
  private void validate() {
    if (baseCurrencyPayment.getCurrency().equals(counterCurrencyPayment.getCurrency())) {
      throw new IllegalArgumentException("Payments must have different currencies");
    }
    if ((baseCurrencyPayment.getAmount() != 0d || counterCurrencyPayment.getAmount() != 0d)
        && Math.signum(baseCurrencyPayment.getAmount()) != -Math.signum(counterCurrencyPayment.getAmount())) {
      throw new IllegalArgumentException("Payments must have different signs");
    }
    ArgChecker.inOrderOrEqual(baseCurrencyPayment.getDate(), counterCurrencyPayment.getDate(),
        "baseCurrencyPayment.date", "counterCurrencyPayment.date");
  }

  @ImmutablePreBuild
  private static void preBuild(Builder builder) {
    // swap order to be base/counter if reverse is conventional
    // this handled deserialization where the base/counter rules differ from those
    // applicable at serialization
    final Payment base = builder.baseCurrencyPayment;
    final Payment counter = builder.counterCurrencyPayment;
    final CurrencyPair pair = CurrencyPair.of(counter.getCurrency(), base.getCurrency());
    if (pair.isConventional()) {
      builder.baseCurrencyPayment = counter;
      builder.counterCurrencyPayment = base;
    }
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
    return CurrencyPair.of(baseCurrencyPayment.getCurrency(), counterCurrencyPayment.getCurrency());
  }

  /**
   * Gets the currency amount in which the amount is received.
   * <p>
   * This returns the currency amount whose amount is non-negative. If both are
   * zero, the currency amount of {@code counterCurrencyPayment} is returned.
   *
   * @return the receive currency amount
   */
  public CurrencyAmount getReceiveCurrencyAmount() {
    if (baseCurrencyPayment.getAmount() > 0d) {
      return CurrencyAmount.of(baseCurrencyPayment.getCurrency(), baseCurrencyPayment.getAmount());
    }
    return CurrencyAmount.of(counterCurrencyPayment.getCurrency(), counterCurrencyPayment.getAmount());
  }

  /**
   * Returns the date that the transaction settles.
   * <p>
   * This returns the settlement date of the base currency.
   *
   * @return the value date
   */
  public LocalDate getPaymentDate() {
    return baseCurrencyPayment.getDate();
  }

  // -------------------------------------------------------------------------
  /**
   * Returns the inverse transaction.
   * <p>
   * The result has the base and counter payments negated.
   *
   * @return the inverse transaction
   */
  public ResolvedFxFlexibleForward inverse() {
    return new ResolvedFxFlexibleForward(baseCurrencyPayment.negated(), counterCurrencyPayment.negated(), windowDates);
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code ResolvedFxFlexibleForward}.
   * @return the meta-bean, not null
   */
  public static ResolvedFxFlexibleForward.Meta meta() {
    return ResolvedFxFlexibleForward.Meta.INSTANCE;
  }

  static {
    MetaBean.register(ResolvedFxFlexibleForward.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private ResolvedFxFlexibleForward(
      Payment baseCurrencyPayment,
      Payment counterCurrencyPayment,
      List<LocalDate> windowDates) {
    JodaBeanUtils.notNull(baseCurrencyPayment, "baseCurrencyPayment");
    JodaBeanUtils.notNull(counterCurrencyPayment, "counterCurrencyPayment");
    JodaBeanUtils.notNull(windowDates, "windowDates");
    this.baseCurrencyPayment = baseCurrencyPayment;
    this.counterCurrencyPayment = counterCurrencyPayment;
    this.windowDates = ImmutableList.copyOf(windowDates);
    validate();
  }

  @Override
  public ResolvedFxFlexibleForward.Meta metaBean() {
    return ResolvedFxFlexibleForward.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment in the base currency, positive if receiving, negative if paying.
   * <p>
   * The payment amount is signed. A positive amount indicates the payment is to
   * be received. A negative amount indicates the payment is to be paid.
   * @return the value of the property, not null
   */
  public Payment getBaseCurrencyPayment() {
    return baseCurrencyPayment;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment in the counter currency, positive if receiving, negative if
   * paying.
   * <p>
   * The payment amount is signed. A positive amount indicates the payment is to
   * be received. A negative amount indicates the payment is to be paid.
   * @return the value of the property, not null
   */
  public Payment getCounterCurrencyPayment() {
    return counterCurrencyPayment;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the windowDates.
   * @return the value of the property, not null
   */
  public List<LocalDate> getWindowDates() {
    return windowDates;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ResolvedFxFlexibleForward other = (ResolvedFxFlexibleForward) obj;
      return JodaBeanUtils.equal(baseCurrencyPayment, other.baseCurrencyPayment) &&
          JodaBeanUtils.equal(counterCurrencyPayment, other.counterCurrencyPayment) &&
          JodaBeanUtils.equal(windowDates, other.windowDates);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(baseCurrencyPayment);
    hash = hash * 31 + JodaBeanUtils.hashCode(counterCurrencyPayment);
    hash = hash * 31 + JodaBeanUtils.hashCode(windowDates);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("ResolvedFxFlexibleForward{");
    buf.append("baseCurrencyPayment").append('=').append(baseCurrencyPayment).append(',').append(' ');
    buf.append("counterCurrencyPayment").append('=').append(counterCurrencyPayment).append(',').append(' ');
    buf.append("windowDates").append('=').append(JodaBeanUtils.toString(windowDates));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ResolvedFxFlexibleForward}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code baseCurrencyPayment} property.
     */
    private final MetaProperty<Payment> baseCurrencyPayment = DirectMetaProperty.ofImmutable(
        this, "baseCurrencyPayment", ResolvedFxFlexibleForward.class, Payment.class);
    /**
     * The meta-property for the {@code counterCurrencyPayment} property.
     */
    private final MetaProperty<Payment> counterCurrencyPayment = DirectMetaProperty.ofImmutable(
        this, "counterCurrencyPayment", ResolvedFxFlexibleForward.class, Payment.class);
    /**
     * The meta-property for the {@code windowDates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> windowDates = DirectMetaProperty.ofImmutable(
        this, "windowDates", ResolvedFxFlexibleForward.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "baseCurrencyPayment",
        "counterCurrencyPayment",
        "windowDates");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 765258148:  // baseCurrencyPayment
          return baseCurrencyPayment;
        case -863240423:  // counterCurrencyPayment
          return counterCurrencyPayment;
        case 1889284213:  // windowDates
          return windowDates;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ResolvedFxFlexibleForward> builder() {
      return new ResolvedFxFlexibleForward.Builder();
    }

    @Override
    public Class<? extends ResolvedFxFlexibleForward> beanType() {
      return ResolvedFxFlexibleForward.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code baseCurrencyPayment} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Payment> baseCurrencyPayment() {
      return baseCurrencyPayment;
    }

    /**
     * The meta-property for the {@code counterCurrencyPayment} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Payment> counterCurrencyPayment() {
      return counterCurrencyPayment;
    }

    /**
     * The meta-property for the {@code windowDates} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<LocalDate>> windowDates() {
      return windowDates;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 765258148:  // baseCurrencyPayment
          return ((ResolvedFxFlexibleForward) bean).getBaseCurrencyPayment();
        case -863240423:  // counterCurrencyPayment
          return ((ResolvedFxFlexibleForward) bean).getCounterCurrencyPayment();
        case 1889284213:  // windowDates
          return ((ResolvedFxFlexibleForward) bean).getWindowDates();
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
   * The bean-builder for {@code ResolvedFxFlexibleForward}.
   */
  private static final class Builder extends DirectPrivateBeanBuilder<ResolvedFxFlexibleForward> {

    private Payment baseCurrencyPayment;
    private Payment counterCurrencyPayment;
    private List<LocalDate> windowDates = ImmutableList.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 765258148:  // baseCurrencyPayment
          return baseCurrencyPayment;
        case -863240423:  // counterCurrencyPayment
          return counterCurrencyPayment;
        case 1889284213:  // windowDates
          return windowDates;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 765258148:  // baseCurrencyPayment
          this.baseCurrencyPayment = (Payment) newValue;
          break;
        case -863240423:  // counterCurrencyPayment
          this.counterCurrencyPayment = (Payment) newValue;
          break;
        case 1889284213:  // windowDates
          this.windowDates = (List<LocalDate>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public ResolvedFxFlexibleForward build() {
      preBuild(this);
      return new ResolvedFxFlexibleForward(
          baseCurrencyPayment,
          counterCurrencyPayment,
          windowDates);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("ResolvedFxFlexibleForward.Builder{");
      buf.append("baseCurrencyPayment").append('=').append(JodaBeanUtils.toString(baseCurrencyPayment)).append(',').append(' ');
      buf.append("counterCurrencyPayment").append('=').append(JodaBeanUtils.toString(counterCurrencyPayment)).append(',').append(' ');
      buf.append("windowDates").append('=').append(JodaBeanUtils.toString(windowDates));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
