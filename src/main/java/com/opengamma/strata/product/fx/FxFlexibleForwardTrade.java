/*
 * Copyright (C) 2018 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.product.fx;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.ImmutableDefaults;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.product.ProductTrade;
import com.opengamma.strata.product.ResolvableTrade;
import com.opengamma.strata.product.TradeInfo;

/**
 * @author rlewis
 *
 */
@BeanDefinition
public final class FxFlexibleForwardTrade
  implements FxTrade, ResolvableTrade<ResolvedFxFlexibleForwardTrade>, ImmutableBean, Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The additional trade information, defaulted to an empty instance.
   * <p>
   * This allows additional information to be attached to the trade.
   */
  @PropertyDefinition(overrideGet = true)
  private final TradeInfo info;
  /**
   * The product that was agreed when the trade occurred.
   * <p>
   * The product captures the contracted financial details of the trade.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final FxFlexibleForward product;

  // -------------------------------------------------------------------------
  /**
   * Obtains an instance of a foreign exchange trade.
   *
   * @param info
   *          the trade info
   * @param product
   *          the product
   * @return the trade
   */
  public static FxFlexibleForwardTrade of(TradeInfo info, FxFlexibleForward product) {
    return new FxFlexibleForwardTrade(info, product);
  }

  @ImmutableDefaults
  private static void applyDefaults(Builder builder) {
    builder.info = TradeInfo.empty();
  }

  // -------------------------------------------------------------------------
  @Override
  public ResolvedFxFlexibleForwardTrade resolve(ReferenceData refData) {
    return ResolvedFxFlexibleForwardTrade.of(info, product.resolve(refData));
  }

  @Override
  public FxFlexibleForwardTrade withInfo(TradeInfo info) {
    return new FxFlexibleForwardTrade(info, product);
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code FxFlexibleForwardTrade}.
   * @return the meta-bean, not null
   */
  public static FxFlexibleForwardTrade.Meta meta() {
    return FxFlexibleForwardTrade.Meta.INSTANCE;
  }

  static {
    MetaBean.register(FxFlexibleForwardTrade.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FxFlexibleForwardTrade.Builder builder() {
    return new FxFlexibleForwardTrade.Builder();
  }

  private FxFlexibleForwardTrade(
      TradeInfo info,
      FxFlexibleForward product) {
    JodaBeanUtils.notNull(product, "product");
    this.info = info;
    this.product = product;
  }

  @Override
  public FxFlexibleForwardTrade.Meta metaBean() {
    return FxFlexibleForwardTrade.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the additional trade information, defaulted to an empty instance.
   * <p>
   * This allows additional information to be attached to the trade.
   * @return the value of the property
   */
  @Override
  public TradeInfo getInfo() {
    return info;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the product that was agreed when the trade occurred.
   * <p>
   * The product captures the contracted financial details of the trade.
   * @return the value of the property, not null
   */
  @Override
  public FxFlexibleForward getProduct() {
    return product;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FxFlexibleForwardTrade other = (FxFlexibleForwardTrade) obj;
      return JodaBeanUtils.equal(info, other.info) &&
          JodaBeanUtils.equal(product, other.product);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(info);
    hash = hash * 31 + JodaBeanUtils.hashCode(product);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("FxFlexibleForwardTrade{");
    buf.append("info").append('=').append(info).append(',').append(' ');
    buf.append("product").append('=').append(JodaBeanUtils.toString(product));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FxFlexibleForwardTrade}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code info} property.
     */
    private final MetaProperty<TradeInfo> info = DirectMetaProperty.ofImmutable(
        this, "info", FxFlexibleForwardTrade.class, TradeInfo.class);
    /**
     * The meta-property for the {@code product} property.
     */
    private final MetaProperty<FxFlexibleForward> product = DirectMetaProperty.ofImmutable(
        this, "product", FxFlexibleForwardTrade.class, FxFlexibleForward.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "info",
        "product");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3237038:  // info
          return info;
        case -309474065:  // product
          return product;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FxFlexibleForwardTrade.Builder builder() {
      return new FxFlexibleForwardTrade.Builder();
    }

    @Override
    public Class<? extends FxFlexibleForwardTrade> beanType() {
      return FxFlexibleForwardTrade.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code info} property.
     * @return the meta-property, not null
     */
    public MetaProperty<TradeInfo> info() {
      return info;
    }

    /**
     * The meta-property for the {@code product} property.
     * @return the meta-property, not null
     */
    public MetaProperty<FxFlexibleForward> product() {
      return product;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3237038:  // info
          return ((FxFlexibleForwardTrade) bean).getInfo();
        case -309474065:  // product
          return ((FxFlexibleForwardTrade) bean).getProduct();
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
   * The bean-builder for {@code FxFlexibleForwardTrade}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<FxFlexibleForwardTrade> {

    private TradeInfo info;
    private FxFlexibleForward product;

    /**
     * Restricted constructor.
     */
    private Builder() {
      applyDefaults(this);
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(FxFlexibleForwardTrade beanToCopy) {
      this.info = beanToCopy.getInfo();
      this.product = beanToCopy.getProduct();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3237038:  // info
          return info;
        case -309474065:  // product
          return product;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3237038:  // info
          this.info = (TradeInfo) newValue;
          break;
        case -309474065:  // product
          this.product = (FxFlexibleForward) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public FxFlexibleForwardTrade build() {
      return new FxFlexibleForwardTrade(
          info,
          product);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the additional trade information, defaulted to an empty instance.
     * <p>
     * This allows additional information to be attached to the trade.
     * @param info  the new value
     * @return this, for chaining, not null
     */
    public Builder info(TradeInfo info) {
      this.info = info;
      return this;
    }

    /**
     * Sets the product that was agreed when the trade occurred.
     * <p>
     * The product captures the contracted financial details of the trade.
     * @param product  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder product(FxFlexibleForward product) {
      JodaBeanUtils.notNull(product, "product");
      this.product = product;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("FxFlexibleForwardTrade.Builder{");
      buf.append("info").append('=').append(JodaBeanUtils.toString(info)).append(',').append(' ');
      buf.append("product").append('=').append(JodaBeanUtils.toString(product));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
