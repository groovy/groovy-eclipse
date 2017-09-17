/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.util;

public class MathUtils {
	/**
	 * Rounds the one number up to the nearest multiple of another
	 *
	 * @param numberToRound
	 *            number to round
	 * @param toMultipleOfThis the result will be divisible by this number
	 * @return the result will be the smallest multiple of toMultipleOfThis that is no smaller than numberToRound
	 */
	public static int roundUpToNearestMultiple(int numberToRound, int toMultipleOfThis) {
		return ((numberToRound + toMultipleOfThis - 1) / toMultipleOfThis) * toMultipleOfThis;
	}

	/**
	 * Rounds the one number up to the nearest multiple of another, where the second number is a power of two.
	 *
	 * @param numberToRound
	 *            number to round
	 * @param aPowerOfTwo
	 *            the result will be divisible by this
	 * @return the result will be the smallest multiple of aPowerOfTwo that is no smaller than numberToRound
	 */
	public static int roundUpToNearestMultipleOfPowerOfTwo(int numberToRound, int aPowerOfTwo) {
		return ((numberToRound + aPowerOfTwo - 1) & ~(aPowerOfTwo - 1));
	}
}
