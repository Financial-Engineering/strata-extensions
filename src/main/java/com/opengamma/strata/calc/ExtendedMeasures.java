/*
 * Copyright (C) 2018 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.calc;

public final class ExtendedMeasures {

	// forward FX Par Swap Rate (forward points)
	public static final Measure FX_SWAP_RATE = ImmutableMeasure.of("FxSwapRate", false);

	// -------------------------------------------------------------------------
	/**
	 * * Restricted constructor.
	 */
	private ExtendedMeasures() {
	}

}
