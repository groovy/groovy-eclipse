/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 386181 - [compiler][null] wrong transition in UnconditionalFlowInfo.mergedWith()
 *								Bug 453635 - [compiler][null] Update NullReferenceImplTests and friends
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.eclipse.jdt.core.tests.compiler.regression.NullReferenceImplTests.State;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullReferenceImplTransformations {
	public static final Transformation
		markAsComparedEqualToNonNull =
		// markAsComparedEqualToNonNull DEFINITION START
		// start => prot. non null
		// pot. unknown => pot. nn & prot. nn
		// pot. non null => pot. nn & prot. nn
		// pot. nn & pot. un => pot. nn & prot. nn
		// pot. null => prot. non null
		// pot. n & pot. un => pot. nn & prot. nn
		// pot. n & pot. nn => pot. nn & prot. nn
		// pot. n & pot. nn & pot. un => prot. non null		 // pot. nn & prot. nn could be better?
		// def. unknown => def. non null
		// def. non null => def. non null
		// pot. nn & prot. nn => pot. nn & prot. nn
		// def. null => prot. non null
		// pot. n & prot. n => prot. non null
		// prot. null => prot. non null
		// prot. non null => prot. non null
		// markAsComparedEqualToNonNull DEFINITION END
			new TwoDimensionalTransformation("markAsComparedEqualToNonNull",
					new byte[][] {
					// markAsComparedEqualToNonNull INITIALIZER START
					{0x00,0x3C},
					{0x04,0x2C},
					{0x08,0x2C},
					{0x0C,0x2C},
					{0x10,0x3C},
					{0x14,0x2C},
					{0x18,0x2C},
					{0x1C,0x3C},
					{0x24,0x28},
					{0x28,0x28},
					{0x2C,0x2C},
					{0x30,0x3C},
					{0x34,0x3C},
					{0x38,0x3C},
					{0x3C,0x3C},
					// markAsComparedEqualToNonNull INITIALIZER END
					}) {
				@Override
				UnconditionalFlowInfo output(UnconditionalFlowInfo input,
						TestLocalVariableBinding local) {
					UnconditionalFlowInfo result = (UnconditionalFlowInfo)input.copy();
					result.markAsComparedEqualToNonNull(local);
					return result;
				}
			},
		markAsComparedEqualToNull =
		// markAsComparedEqualToNull DEFINITION START
		// start => prot. null
		// pot. unknown => pot. n & prot. n
		// pot. non null => prot. null
		// pot. nn & pot. un => pot. n & prot. n
		// pot. null => pot. n & prot. n
		// pot. n & pot. un => pot. n & prot. n
		// pot. n & pot. nn => pot. n & prot. n
		// pot. n & pot. nn & pot. un => pot. n & prot. n
		// def. unknown => def. null
		// def. non null => prot. null
		// pot. nn & prot. nn => prot. null
		// def. null => def. null
		// pot. n & prot. n => pot. n & prot. n
		// prot. null => prot. null
		// prot. non null => prot. null
		// markAsComparedEqualToNull DEFINITION END
			new TwoDimensionalTransformation("markAsComparedEqualToNull",
				new byte[][] {
				// markAsComparedEqualToNull INITIALIZER START
				{0x00,0x38},
				{0x04,0x34},
				{0x08,0x38},
				{0x0C,0x34},
				{0x10,0x34},
				{0x14,0x34},
				{0x18,0x34},
				{0x1C,0x34},
				{0x24,0x30},
				{0x28,0x38},
				{0x2C,0x38},
				{0x30,0x30},
				{0x34,0x34},
				{0x38,0x38},
				{0x3C,0x38},
				// markAsComparedEqualToNull INITIALIZER END
				}) {
			@Override
			UnconditionalFlowInfo output(UnconditionalFlowInfo input,
					TestLocalVariableBinding local) {
				UnconditionalFlowInfo result = (UnconditionalFlowInfo)input.copy();
				result.markAsComparedEqualToNull(local);
				return result;
			}
		},
		markAsDefinitelyNonNull =
		// markAsDefinitelyNonNull DEFINITION START
		// start => def. non null
		// pot. unknown => def. non null
		// pot. non null => def. non null
		// pot. nn & pot. un => def. non null
		// pot. null => def. non null
		// pot. n & pot. un => def. non null
		// pot. n & pot. nn => def. non null
		// pot. n & pot. nn & pot. un => def. non null
		// def. unknown => def. non null
		// def. non null => def. non null
		// pot. nn & prot. nn => def. non null
		// def. null => def. non null
		// pot. n & prot. n => def. non null
		// prot. null => def. non null
		// prot. non null => def. non null
		// markAsDefinitelyNonNull DEFINITION END
			new TwoDimensionalTransformation("markAsDefinitelyNonNull",
				new byte[][] {
				// markAsDefinitelyNonNull INITIALIZER START
				{0x00,0x28},
				{0x04,0x28},
				{0x08,0x28},
				{0x0C,0x28},
				{0x10,0x28},
				{0x14,0x28},
				{0x18,0x28},
				{0x1C,0x28},
				{0x24,0x28},
				{0x28,0x28},
				{0x2C,0x28},
				{0x30,0x28},
				{0x34,0x28},
				{0x38,0x28},
				{0x3C,0x28},
				// markAsDefinitelyNonNull INITIALIZER END
				}) {
			@Override
			UnconditionalFlowInfo output(UnconditionalFlowInfo input,
					TestLocalVariableBinding local) {
				UnconditionalFlowInfo result = (UnconditionalFlowInfo)input.copy();
				result.markAsDefinitelyNonNull(local);
				return result;
			}
		},
		markAsDefinitelyNull =
		// markAsDefinitelyNull DEFINITION START
		// start => def. null
		// pot. unknown => def. null
		// pot. non null => def. null
		// pot. nn & pot. un => def. null
		// pot. null => def. null
		// pot. n & pot. un => def. null
		// pot. n & pot. nn => def. null
		// pot. n & pot. nn & pot. un => def. null
		// def. unknown => def. null
		// def. non null => def. null
		// pot. nn & prot. nn => def. null
		// def. null => def. null
		// pot. n & prot. n => def. null
		// prot. null => def. null
		// prot. non null => def. null
		// markAsDefinitelyNull DEFINITION END
			// PREMATURE add 'catch rules'
			new TwoDimensionalTransformation("markAsDefinitelyNull",
				new byte[][] {
				// markAsDefinitelyNull INITIALIZER START
				{0x00,0x30},
				{0x04,0x30},
				{0x08,0x30},
				{0x0C,0x30},
				{0x10,0x30},
				{0x14,0x30},
				{0x18,0x30},
				{0x1C,0x30},
				{0x24,0x30},
				{0x28,0x30},
				{0x2C,0x30},
				{0x30,0x30},
				{0x34,0x30},
				{0x38,0x30},
				{0x3C,0x30},
				// markAsDefinitelyNull INITIALIZER END
				}) {
			@Override
			UnconditionalFlowInfo output(UnconditionalFlowInfo input,
					TestLocalVariableBinding local) {
				UnconditionalFlowInfo result = (UnconditionalFlowInfo)input.copy();
				result.markAsDefinitelyNull(local);
				return result;
			}
		},
		markAsDefinitelyUnknown =
		// markAsDefinitelyUnknown DEFINITION START
		// start => def. unknown
		// pot. unknown => def. unknown
		// pot. non null => def. unknown
		// pot. nn & pot. un => def. unknown
		// pot. null => def. unknown
		// pot. n & pot. un => def. unknown
		// pot. n & pot. nn => def. unknown
		// pot. n & pot. nn & pot. un => def. unknown
		// def. unknown => def. unknown
		// def. non null => def. unknown
		// pot. nn & prot. nn => def. unknown
		// def. null => def. unknown
		// pot. n & prot. n => def. unknown
		// prot. null => def. unknown
		// prot. non null => def. unknown
		// markAsDefinitelyUnknown DEFINITION END
			new TwoDimensionalTransformation("markAsDefinitelyUnknown",
				new byte[][] {
				// markAsDefinitelyUnknown INITIALIZER START
				{0x00,0x24},
				{0x04,0x24},
				{0x08,0x24},
				{0x0C,0x24},
				{0x10,0x24},
				{0x14,0x24},
				{0x18,0x24},
				{0x1C,0x24},
				{0x24,0x24},
				{0x28,0x24},
				{0x2C,0x24},
				{0x30,0x24},
				{0x34,0x24},
				{0x38,0x24},
				{0x3C,0x24},
				// markAsDefinitelyUnknown INITIALIZER END
				}) {
			@Override
			UnconditionalFlowInfo output(UnconditionalFlowInfo input,
					TestLocalVariableBinding local) {
				UnconditionalFlowInfo result = (UnconditionalFlowInfo)input.copy();
				result.markAsDefinitelyUnknown(local);
				return result;
			}
		},
		addInitializationsFrom =
		// addInitializationsFrom DEFINITION START
		// def. non null + def. non null => def. non null
		// def. non null + def. null => def. null
		// def. non null + def. unknown => def. unknown
		// def. non null + pot. n & pot. nn & pot. un => pot. non null // BOGUS
		// def. non null + pot. n & pot. nn => pot. n & pot. nn
		// def. non null + pot. n & pot. un => pot. n & pot. nn
		// def. non null + pot. n & prot. n => pot. n & prot. n
		// def. non null + pot. nn & pot. un => def. unknown
		// def. non null + pot. nn & prot. nn => def. non null
		// def. non null + pot. non null => def. non null
		// def. non null + pot. null => pot. n & pot. nn
		// def. non null + pot. unknown => def. unknown // priv.
		// def. non null + prot. non null => def. non null
		// def. non null + prot. null => prot. null
		// def. non null + start => def. non null
		// def. null + def. non null => def. non null
		// def. null + def. null => def. null
		// def. null + def. unknown => def. unknown
		// def. null + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// def. null + pot. n & pot. nn => pot. n & pot. nn
		// def. null + pot. n & pot. un => pot. n & pot. un
		// def. null + pot. n & prot. n => def. null
		// def. null + pot. nn & pot. un => pot. n & pot. nn
		// def. null + pot. nn & prot. nn => pot. nn & prot. nn
		// def. null + pot. non null => pot. n & pot. nn
		// def. null + pot. null => def. null
		// def. null + pot. unknown => pot. n & pot. un // pot. null privileged over def. unknown
		// def. null + prot. non null => prot. non null
		// def. null + prot. null => def. null
		// def. null + start => def. null
		// def. unknown + def. non null => def. non null
		// def. unknown + def. null => def. null
		// def. unknown + def. unknown => def. unknown
		// def. unknown + pot. n & pot. nn & pot. un => pot. non null // BOGUS
		// def. unknown + pot. n & pot. nn => pot. n & pot. nn
		// def. unknown + pot. n & pot. un => pot. n & pot. un	// we loose the def here, but we need the pot. null
		// def. unknown + pot. n & prot. n => def. null
		// def. unknown + pot. nn & pot. un => def. unknown // priv.
		// def. unknown + pot. nn & prot. nn => def. non null
		// def. unknown + pot. non null => def. unknown	// privileging over pot. nn & pot. un
		// def. unknown + pot. null => pot. n & pot. un
		// def. unknown + pot. unknown => def. unknown
		// def. unknown + prot. non null => def. non null
		// def. unknown + prot. null => def. null
		// def. unknown + start => def. unknown
		// pot. n & pot. nn & pot. un + def. non null => def. non null
		// pot. n & pot. nn & pot. un + def. null => def. null
		// pot. n & pot. nn & pot. un + def. unknown => def. unknown
		// pot. n & pot. nn & pot. un + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. n & pot. un => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. n & prot. n => pot. n & prot. n
		// pot. n & pot. nn & pot. un + pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. n & pot. nn & pot. un + pot. non null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. null => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. unknown => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + prot. non null => pot. nn & prot. nn
		// pot. n & pot. nn & pot. un + prot. null => pot. n & prot. n
		// pot. n & pot. nn & pot. un + start => pot. n & pot. nn & pot. un
		// pot. n & pot. nn + def. non null => def. non null
		// pot. n & pot. nn + def. null => def. null
		// pot. n & pot. nn + def. unknown => def. unknown
		// pot. n & pot. nn + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & pot. un => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & prot. n => pot. n & prot. n
		// pot. n & pot. nn + pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. nn + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. n & pot. nn + pot. non null => pot. n & pot. nn
		// pot. n & pot. nn + pot. null => pot. n & pot. nn
		// pot. n & pot. nn + pot. unknown => pot. n & pot. nn
		// pot. n & pot. nn + prot. non null => pot. nn & prot. nn
		// pot. n & pot. nn + prot. null => pot. n & prot. n
		// pot. n & pot. nn + start => pot. n & pot. nn
		// pot. n & pot. un + def. non null => def. non null
		// pot. n & pot. un + def. null => def. null
		// pot. n & pot. un + def. unknown => def. unknown
		// pot. n & pot. un + pot. n & pot. nn & pot. un => pot. n & pot. nn // should ideally include un
		// pot. n & pot. un + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & pot. un + pot. n & pot. un => pot. n & pot. un
		// pot. n & pot. un + pot. n & prot. n => pot. n & prot. n
		// pot. n & pot. un + pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. un + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. n & pot. un + pot. non null => pot. n & pot. nn
		// pot. n & pot. un + pot. null => pot. n & pot. un
		// pot. n & pot. un + pot. unknown => pot. n & pot. un
		// pot. n & pot. un + prot. non null => pot. nn & prot. nn
		// pot. n & pot. un + prot. null => pot. n & prot. n
		// pot. n & pot. un + start => pot. n & pot. un
		// pot. n & prot. n + def. non null => def. non null
		// pot. n & prot. n + def. null => def. null
		// pot. n & prot. n + def. unknown => def. unknown
		// pot. n & prot. n + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. n & prot. n + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & prot. n + pot. n & pot. un => pot. n & pot. un
		// pot. n & prot. n + pot. n & prot. n => pot. n & prot. n
		// pot. n & prot. n + pot. nn & pot. un => pot. n & pot. nn
		// pot. n & prot. n + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. n & prot. n + pot. non null => pot. n & pot. nn
		// pot. n & prot. n + pot. null => pot. n & prot. n
		// pot. n & prot. n + pot. unknown => pot. n & pot. un
		// pot. n & prot. n + prot. non null => prot. non null
		// pot. n & prot. n + prot. null => pot. n & prot. n
		// pot. n & prot. n + start => pot. n & prot. n
		// pot. nn & pot. un + def. non null => def. non null
		// pot. nn & pot. un + def. null => def. null
		// pot. nn & pot. un + def. unknown => def. unknown
		// pot. nn & pot. un + pot. n & pot. nn & pot. un => pot. non null // should ideally include un
		// pot. nn & pot. un + pot. n & pot. nn => pot. n & pot. nn
		// pot. nn & pot. un + pot. n & pot. un => pot. n & pot. nn
		// pot. nn & pot. un + pot. n & prot. n => pot. n & prot. n
		// pot. nn & pot. un + pot. nn & pot. un => pot. nn & pot. un
		// pot. nn & pot. un + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. nn & pot. un + pot. non null => pot. nn & pot. un
		// pot. nn & pot. un + pot. null => pot. n & pot. nn
		// pot. nn & pot. un + pot. unknown => pot. nn & pot. un
		// pot. nn & pot. un + prot. non null => pot. nn & prot. nn
		// pot. nn & pot. un + prot. null => pot. n & prot. n
		// pot. nn & pot. un + start => pot. nn & pot. un
		// pot. nn & prot. nn + def. non null => def. non null
		// pot. nn & prot. nn + def. null => def. null
		// pot. nn & prot. nn + def. unknown => def. unknown
		// pot. nn & prot. nn + pot. n & pot. nn & pot. un => pot. non null	// must include n
		// pot. nn & prot. nn + pot. n & pot. nn => pot. n & pot. nn
		// pot. nn & prot. nn + pot. n & pot. un => pot. n & pot. nn
		// pot. nn & prot. nn + pot. n & prot. n => pot. n & prot. n
		// pot. nn & prot. nn + pot. nn & pot. un => pot. nn & pot. un
		// pot. nn & prot. nn + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. nn & prot. nn + pot. non null => pot. nn & prot. nn
		// pot. nn & prot. nn + pot. null => pot. n & pot. nn
		// pot. nn & prot. nn + pot. unknown => pot. nn & pot. un
		// pot. nn & prot. nn + prot. non null => pot. nn & prot. nn
		// pot. nn & prot. nn + prot. null => prot. null
		// pot. nn & prot. nn + start => pot. nn & prot. nn
		// pot. non null + def. non null => def. non null
		// pot. non null + def. null => def. null
		// pot. non null + def. unknown => def. unknown
		// pot. non null + pot. n & pot. nn & pot. un => pot. non null // must include n
		// pot. non null + pot. n & pot. nn => pot. n & pot. nn
		// pot. non null + pot. n & pot. un => pot. n & pot. nn
		// pot. non null + pot. n & prot. n => pot. n & prot. n
		// pot. non null + pot. nn & pot. un => pot. nn & pot. un
		// pot. non null + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. non null + pot. non null => pot. non null
		// pot. non null + pot. null => pot. n & pot. nn
		// pot. non null + pot. unknown => pot. nn & pot. un
		// pot. non null + prot. non null => pot. nn & prot. nn
		// pot. non null + prot. null => prot. null
		// pot. non null + start => pot. non null
		// pot. null + def. non null => def. non null
		// pot. null + def. null => def. null
		// pot. null + def. unknown => def. unknown
		// pot. null + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. null + pot. n & pot. nn => pot. n & pot. nn
		// pot. null + pot. n & pot. un => pot. n & pot. un
		// pot. null + pot. n & prot. n => pot. n & prot. n
		// pot. null + pot. nn & pot. un => pot. n & pot. nn
		// pot. null + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. null + pot. non null => pot. n & pot. nn
		// pot. null + pot. null => pot. null
		// pot. null + pot. unknown => pot. n & pot. un
		// pot. null + prot. non null => prot. non null
		// pot. null + prot. null => pot. n & prot. n
		// pot. null + start => pot. null
		// pot. unknown + def. non null => def. non null
		// pot. unknown + def. null => def. null
		// pot. unknown + def. unknown => def. unknown
		// pot. unknown + pot. n & pot. nn & pot. un => pot. non null // must include n
		// pot. unknown + pot. n & pot. nn => pot. n & pot. nn
		// pot. unknown + pot. n & pot. un => pot. n & pot. un
		// pot. unknown + pot. n & prot. n => pot. n & prot. n
		// pot. unknown + pot. nn & pot. un => pot. nn & pot. un
		// pot. unknown + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. unknown + pot. non null => pot. nn & pot. un
		// pot. unknown + pot. null => pot. n & pot. un
		// pot. unknown + pot. unknown => pot. unknown
		// pot. unknown + prot. non null => pot. nn & prot. nn
		// pot. unknown + prot. null => pot. n & prot. n
		// pot. unknown + start => pot. unknown
		// prot. non null + def. non null => def. non null
		// prot. non null + def. null => def. null
		// prot. non null + def. unknown => def. unknown
		// prot. non null + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// prot. non null + pot. n & pot. nn => pot. n & pot. nn
		// prot. non null + pot. n & pot. un => pot. n & pot. un
		// prot. non null + pot. n & prot. n => pot. n & prot. n
		// prot. non null + pot. nn & pot. un => pot. nn & pot. un
		// prot. non null + pot. nn & prot. nn => pot. nn & prot. nn
		// prot. non null + pot. non null => pot. nn & prot. nn
		// prot. non null + pot. null => pot. null
		// prot. non null + pot. unknown => pot. unknown
		// prot. non null + prot. non null => prot. non null
		// prot. non null + prot. null => prot. null
		// prot. non null + start => prot. non null
		// prot. null + def. non null => def. non null
		// prot. null + def. null => def. null
		// prot. null + def. unknown => def. unknown
		// prot. null + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// prot. null + pot. n & pot. nn => pot. n & pot. nn
		// prot. null + pot. n & pot. un => pot. n & pot. un
		// prot. null + pot. n & prot. n => pot. n & prot. n
		// prot. null + pot. nn & pot. un => pot. nn & pot. un
		// prot. null + pot. nn & prot. nn => pot. nn & prot. nn
		// prot. null + pot. non null => pot. non null
		// prot. null + pot. null => pot. n & prot. n
		// prot. null + pot. unknown => pot. unknown
		// prot. null + prot. non null => prot. non null
		// prot. null + prot. null => prot. null
		// prot. null + start => prot. null
		// start + def. non null => def. non null
		// start + def. null => def. null
		// start + def. unknown => def. unknown
		// start + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// start + pot. n & pot. nn => pot. n & pot. nn
		// start + pot. n & pot. un => pot. n & pot. un
		// start + pot. n & prot. n => pot. n & prot. n
		// start + pot. nn & pot. un => pot. nn & pot. un
		// start + pot. nn & prot. nn => pot. nn & prot. nn
		// start + pot. non null => pot. non null
		// start + pot. null => pot. null
		// start + pot. unknown => pot. unknown
		// start + prot. non null => prot. non null
		// start + prot. null => prot. null
		// start + start => start
		// addInitializationsFrom DEFINITION END
			new ThreeDimensionalTransformation("addInitializationsFrom",
				new byte[][] {
				// addInitializationsFrom INITIALIZER START
				{0x00,0x00,0x00},
				{0x00,0x04,0x04},
				{0x00,0x08,0x08},
				{0x00,0x0C,0x0C},
				{0x00,0x10,0x10},
				{0x00,0x14,0x14},
				{0x00,0x18,0x18},
				{0x00,0x1C,0x1C},
				{0x00,0x24,0x24},
				{0x00,0x28,0x28},
				{0x00,0x2C,0x2C},
				{0x00,0x30,0x30},
				{0x00,0x34,0x34},
				{0x00,0x38,0x38},
				{0x00,0x3C,0x3C},
				{0x04,0x00,0x04},
				{0x04,0x04,0x04},
				{0x04,0x08,0x0C},
				{0x04,0x0C,0x0C},
				{0x04,0x10,0x14},
				{0x04,0x14,0x14},
				{0x04,0x18,0x18},
				{0x04,0x1C,0x08},
				{0x04,0x24,0x24},
				{0x04,0x28,0x28},
				{0x04,0x2C,0x2C},
				{0x04,0x30,0x30},
				{0x04,0x34,0x34},
				{0x04,0x38,0x34},
				{0x04,0x3C,0x2C},
				{0x08,0x00,0x08},
				{0x08,0x04,0x0C},
				{0x08,0x08,0x08},
				{0x08,0x0C,0x0C},
				{0x08,0x10,0x18},
				{0x08,0x14,0x18},
				{0x08,0x18,0x18},
				{0x08,0x1C,0x08},
				{0x08,0x24,0x24},
				{0x08,0x28,0x28},
				{0x08,0x2C,0x2C},
				{0x08,0x30,0x30},
				{0x08,0x34,0x34},
				{0x08,0x38,0x38},
				{0x08,0x3C,0x2C},
				{0x0C,0x00,0x0C},
				{0x0C,0x04,0x0C},
				{0x0C,0x08,0x0C},
				{0x0C,0x0C,0x0C},
				{0x0C,0x10,0x18},
				{0x0C,0x14,0x18},
				{0x0C,0x18,0x18},
				{0x0C,0x1C,0x08},
				{0x0C,0x24,0x24},
				{0x0C,0x28,0x28},
				{0x0C,0x2C,0x2C},
				{0x0C,0x30,0x30},
				{0x0C,0x34,0x34},
				{0x0C,0x38,0x34},
				{0x0C,0x3C,0x2C},
				{0x10,0x00,0x10},
				{0x10,0x04,0x14},
				{0x10,0x08,0x18},
				{0x10,0x0C,0x18},
				{0x10,0x10,0x10},
				{0x10,0x14,0x14},
				{0x10,0x18,0x18},
				{0x10,0x1C,0x18},
				{0x10,0x24,0x24},
				{0x10,0x28,0x28},
				{0x10,0x2C,0x2C},
				{0x10,0x30,0x30},
				{0x10,0x34,0x34},
				{0x10,0x38,0x34},
				{0x10,0x3C,0x3C},
				{0x14,0x00,0x14},
				{0x14,0x04,0x14},
				{0x14,0x08,0x18},
				{0x14,0x0C,0x18},
				{0x14,0x10,0x14},
				{0x14,0x14,0x14},
				{0x14,0x18,0x18},
				{0x14,0x1C,0x18},
				{0x14,0x24,0x24},
				{0x14,0x28,0x28},
				{0x14,0x2C,0x2C},
				{0x14,0x30,0x30},
				{0x14,0x34,0x34},
				{0x14,0x38,0x34},
				{0x14,0x3C,0x2C},
				{0x18,0x00,0x18},
				{0x18,0x04,0x18},
				{0x18,0x08,0x18},
				{0x18,0x0C,0x18},
				{0x18,0x10,0x18},
				{0x18,0x14,0x18},
				{0x18,0x18,0x18},
				{0x18,0x1C,0x18},
				{0x18,0x24,0x24},
				{0x18,0x28,0x28},
				{0x18,0x2C,0x2C},
				{0x18,0x30,0x30},
				{0x18,0x34,0x34},
				{0x18,0x38,0x34},
				{0x18,0x3C,0x2C},
				{0x1C,0x00,0x1C},
				{0x1C,0x04,0x1C},
				{0x1C,0x08,0x1C},
				{0x1C,0x0C,0x1C},
				{0x1C,0x10,0x18},
				{0x1C,0x14,0x18},
				{0x1C,0x18,0x18},
				{0x1C,0x1C,0x18},
				{0x1C,0x24,0x24},
				{0x1C,0x28,0x28},
				{0x1C,0x2C,0x2C},
				{0x1C,0x30,0x30},
				{0x1C,0x34,0x34},
				{0x1C,0x38,0x34},
				{0x1C,0x3C,0x2C},
				{0x24,0x00,0x24},
				{0x24,0x04,0x24},
				{0x24,0x08,0x24},
				{0x24,0x0C,0x24},
				{0x24,0x10,0x14},
				{0x24,0x14,0x14},
				{0x24,0x18,0x18},
				{0x24,0x1C,0x08},
				{0x24,0x24,0x24},
				{0x24,0x28,0x28},
				{0x24,0x2C,0x28},
				{0x24,0x30,0x30},
				{0x24,0x34,0x30},
				{0x24,0x38,0x30},
				{0x24,0x3C,0x28},
				{0x28,0x00,0x28},
				{0x28,0x04,0x24},
				{0x28,0x08,0x28},
				{0x28,0x0C,0x24},
				{0x28,0x10,0x18},
				{0x28,0x14,0x18},
				{0x28,0x18,0x18},
				{0x28,0x1C,0x08},
				{0x28,0x24,0x24},
				{0x28,0x28,0x28},
				{0x28,0x2C,0x28},
				{0x28,0x30,0x30},
				{0x28,0x34,0x34},
				{0x28,0x38,0x38},
				{0x28,0x3C,0x28},
				{0x2C,0x00,0x2C},
				{0x2C,0x04,0x0C},
				{0x2C,0x08,0x2C},
				{0x2C,0x0C,0x0C},
				{0x2C,0x10,0x18},
				{0x2C,0x14,0x18},
				{0x2C,0x18,0x18},
				{0x2C,0x1C,0x08},
				{0x2C,0x24,0x24},
				{0x2C,0x28,0x28},
				{0x2C,0x2C,0x2C},
				{0x2C,0x30,0x30},
				{0x2C,0x34,0x34},
				{0x2C,0x38,0x38},
				{0x2C,0x3C,0x2C},
				{0x30,0x00,0x30},
				{0x30,0x04,0x14},
				{0x30,0x08,0x18},
				{0x30,0x0C,0x18},
				{0x30,0x10,0x30},
				{0x30,0x14,0x14},
				{0x30,0x18,0x18},
				{0x30,0x1C,0x1C},
				{0x30,0x24,0x24},
				{0x30,0x28,0x28},
				{0x30,0x2C,0x2C},
				{0x30,0x30,0x30},
				{0x30,0x34,0x30},
				{0x30,0x38,0x30},
				{0x30,0x3C,0x3C},
				{0x34,0x00,0x34},
				{0x34,0x04,0x14},
				{0x34,0x08,0x18},
				{0x34,0x0C,0x18},
				{0x34,0x10,0x34},
				{0x34,0x14,0x14},
				{0x34,0x18,0x18},
				{0x34,0x1C,0x1C},
				{0x34,0x24,0x24},
				{0x34,0x28,0x28},
				{0x34,0x2C,0x2C},
				{0x34,0x30,0x30},
				{0x34,0x34,0x34},
				{0x34,0x38,0x34},
				{0x34,0x3C,0x3C},
				{0x38,0x00,0x38},
				{0x38,0x04,0x04},
				{0x38,0x08,0x08},
				{0x38,0x0C,0x0C},
				{0x38,0x10,0x34},
				{0x38,0x14,0x14},
				{0x38,0x18,0x18},
				{0x38,0x1C,0x1C},
				{0x38,0x24,0x24},
				{0x38,0x28,0x28},
				{0x38,0x2C,0x2C},
				{0x38,0x30,0x30},
				{0x38,0x34,0x34},
				{0x38,0x38,0x38},
				{0x38,0x3C,0x3C},
				{0x3C,0x00,0x3C},
				{0x3C,0x04,0x04},
				{0x3C,0x08,0x2C},
				{0x3C,0x0C,0x0C},
				{0x3C,0x10,0x10},
				{0x3C,0x14,0x14},
				{0x3C,0x18,0x18},
				{0x3C,0x1C,0x1C},
				{0x3C,0x24,0x24},
				{0x3C,0x28,0x28},
				{0x3C,0x2C,0x2C},
				{0x3C,0x30,0x30},
				{0x3C,0x34,0x34},
				{0x3C,0x38,0x38},
				{0x3C,0x3C,0x3C},
				// addInitializationsFrom INITIALIZER END
				}) {
			@Override
			UnconditionalFlowInfo output(UnconditionalFlowInfo input1,
					UnconditionalFlowInfo input2) {
				return (UnconditionalFlowInfo)
					input1.copy().addInitializationsFrom(input2);
			}
		},
		// addPotentialInitializationsFrom DEFINITION START
		// def. non null + def. non null => def. non null
		// def. non null + def. null => pot. n & pot. nn
		// def. non null + def. unknown => def. unknown
		// def. non null + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// def. non null + pot. n & pot. nn => pot. n & pot. nn
		// def. non null + pot. n & pot. un => pot. n & pot. nn
		// def. non null + pot. n & prot. n => pot. n & pot. nn
		// def. non null + pot. nn & pot. un => def. unknown
		// def. non null + pot. nn & prot. nn => def. non null
		// def. non null + pot. non null => def. non null
		// def. non null + pot. null => pot. n & pot. nn
		// def. non null + pot. unknown => def. unknown
		// def. non null + prot. non null => def. non null
		// def. non null + prot. null => def. non null
		// def. non null + start => def. non null
		// def. null + def. non null => pot. n & pot. nn
		// def. null + def. null => def. null
		// def. null + def. unknown => pot. n & pot. un
		// def. null + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// def. null + pot. n & pot. nn => pot. n & pot. nn
		// def. null + pot. n & pot. un => pot. n & pot. un
		// def. null + pot. n & prot. n => def. null
		// def. null + pot. nn & pot. un => pot. n & pot. nn
		// def. null + pot. nn & prot. nn => pot. n & pot. nn
		// def. null + pot. non null => pot. n & pot. nn
		// def. null + pot. null => def. null
		// def. null + pot. unknown => pot. n & pot. un
		// def. null + prot. non null => def. null
		// def. null + prot. null => def. null
		// def. null + start => def. null
		// def. unknown + def. non null => def. unknown
		// def. unknown + def. null => pot. n & pot. un
		// def. unknown + def. unknown => def. unknown
		// def. unknown + pot. n & pot. nn & pot. un => pot. n & pot. nn // should ideally include un
		// def. unknown + pot. n & pot. nn => pot. n & pot. nn
		// def. unknown + pot. n & pot. un => pot. n & pot. un
		// def. unknown + pot. n & prot. n => pot. n & pot. un
		// def. unknown + pot. nn & pot. un => def. unknown
		// def. unknown + pot. nn & prot. nn => def. unknown
		// def. unknown + pot. non null => def. unknown
		// def. unknown + pot. null => pot. n & pot. un
		// def. unknown + pot. unknown => def. unknown
		// def. unknown + prot. non null => def. unknown
		// def. unknown + prot. null => def. unknown
		// def. unknown + start => def. unknown
		// pot. n & pot. nn & pot. un + def. non null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + def. null => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + def. unknown => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. n & pot. un => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. n & prot. n => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. nn & prot. nn => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. non null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. null => pot. n & pot. nn
		// pot. n & pot. nn & pot. un + pot. unknown => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + prot. non null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + prot. null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + start => pot. n & pot. nn & pot. un
		// pot. n & pot. nn + def. non null => pot. n & pot. nn
		// pot. n & pot. nn + def. null => pot. n & pot. nn
		// pot. n & pot. nn + def. unknown => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & pot. un => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & prot. n => pot. n & pot. nn
		// pot. n & pot. nn + pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. nn + pot. nn & prot. nn => pot. n & pot. nn
		// pot. n & pot. nn + pot. non null => pot. n & pot. nn
		// pot. n & pot. nn + pot. null => pot. n & pot. nn
		// pot. n & pot. nn + pot. unknown => pot. n & pot. nn
		// pot. n & pot. nn + prot. non null => pot. n & pot. nn
		// pot. n & pot. nn + prot. null => pot. n & pot. nn
		// pot. n & pot. nn + start => pot. n & pot. nn
		// pot. n & pot. un + def. non null => pot. n & pot. nn
		// pot. n & pot. un + def. null => pot. n & pot. un
		// pot. n & pot. un + def. unknown => pot. n & pot. un
		// pot. n & pot. un + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. un + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & pot. un + pot. n & pot. un => pot. n & pot. un
		// pot. n & pot. un + pot. n & prot. n => pot. n & pot. un
		// pot. n & pot. un + pot. nn & pot. un => pot. n & pot. nn
		// pot. n & pot. un + pot. nn & prot. nn => pot. n & pot. nn
		// pot. n & pot. un + pot. non null => pot. n & pot. nn
		// pot. n & pot. un + pot. null => pot. n & pot. un
		// pot. n & pot. un + pot. unknown => pot. n & pot. un
		// pot. n & pot. un + prot. non null => pot. n & pot. un
		// pot. n & pot. un + prot. null => pot. n & pot. un
		// pot. n & pot. un + start => pot. n & pot. un
		// pot. n & prot. n + def. non null => pot. n & pot. nn
		// pot. n & prot. n + def. null => pot. n & prot. n
		// pot. n & prot. n + def. unknown => pot. n & pot. un
		// pot. n & prot. n + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. n & prot. n + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & prot. n + pot. n & pot. un => pot. n & pot. un
		// pot. n & prot. n + pot. n & prot. n => pot. n & prot. n
		// pot. n & prot. n + pot. nn & pot. un => pot. n & pot. nn
		// pot. n & prot. n + pot. nn & prot. nn => pot. n & pot. nn
		// pot. n & prot. n + pot. non null => pot. n & pot. nn
		// pot. n & prot. n + pot. null => pot. n & prot. n
		// pot. n & prot. n + pot. unknown => pot. n & pot. un
		// pot. n & prot. n + prot. non null => pot. n & prot. n
		// pot. n & prot. n + prot. null => pot. n & prot. n
		// pot. n & prot. n + start => pot. n & prot. n
		// pot. nn & pot. un + def. non null => pot. nn & pot. un
		// pot. nn & pot. un + def. null => pot. n & pot. nn
		// pot. nn & pot. un + def. unknown => pot. nn & pot. un
		// pot. nn & pot. un + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. nn & pot. un + pot. n & pot. nn => pot. n & pot. nn
		// pot. nn & pot. un + pot. n & pot. un => pot. n & pot. nn
		// pot. nn & pot. un + pot. n & prot. n => pot. n & pot. nn
		// pot. nn & pot. un + pot. nn & pot. un => pot. nn & pot. un
		// pot. nn & pot. un + pot. nn & prot. nn => pot. nn & pot. un
		// pot. nn & pot. un + pot. non null => pot. nn & pot. un
		// pot. nn & pot. un + pot. null => pot. n & pot. nn
		// pot. nn & pot. un + pot. unknown => pot. nn & pot. un
		// pot. nn & pot. un + prot. non null => pot. nn & pot. un
		// pot. nn & pot. un + prot. null => pot. nn & pot. un
		// pot. nn & pot. un + start => pot. nn & pot. un
		// pot. nn & prot. nn + def. non null => pot. nn & prot. nn
		// pot. nn & prot. nn + def. null => pot. n & pot. nn
		// pot. nn & prot. nn + def. unknown => pot. nn & pot. un
		// pot. nn & prot. nn + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. nn & prot. nn + pot. n & pot. nn => pot. n & pot. nn
		// pot. nn & prot. nn + pot. n & pot. un => pot. n & pot. nn
		// pot. nn & prot. nn + pot. n & prot. n => pot. n & pot. nn
		// pot. nn & prot. nn + pot. nn & pot. un => pot. nn & pot. un	// see test1501
		// pot. nn & prot. nn + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. nn & prot. nn + pot. non null => pot. nn & prot. nn
		// pot. nn & prot. nn + pot. null => pot. n & pot. nn
		// pot. nn & prot. nn + pot. unknown => pot. nn & pot. un
		// pot. nn & prot. nn + prot. non null => pot. nn & prot. nn
		// pot. nn & prot. nn + prot. null => pot. nn & prot. nn
		// pot. nn & prot. nn + start => pot. nn & prot. nn
		// pot. non null + def. non null => pot. non null
		// pot. non null + def. null => pot. n & pot. nn
		// pot. non null + def. unknown => pot. nn & pot. un
		// pot. non null + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. non null + pot. n & pot. nn => pot. n & pot. nn
		// pot. non null + pot. n & pot. un => pot. n & pot. nn
		// pot. non null + pot. n & prot. n => pot. n & pot. nn
		// pot. non null + pot. nn & pot. un => pot. nn & pot. un
		// pot. non null + pot. nn & prot. nn => pot. non null
		// pot. non null + pot. non null => pot. non null
		// pot. non null + pot. null => pot. n & pot. nn
		// pot. non null + pot. unknown => pot. nn & pot. un
		// pot. non null + prot. non null => pot. non null
		// pot. non null + prot. null => pot. non null
		// pot. non null + start => pot. non null
		// pot. null + def. non null => pot. n & pot. nn
		// pot. null + def. null => pot. null
		// pot. null + def. unknown => pot. n & pot. un
		// pot. null + pot. n & pot. nn & pot. un => pot. n & pot. nn
		// pot. null + pot. n & pot. nn => pot. n & pot. nn
		// pot. null + pot. n & pot. un => pot. n & pot. un
		// pot. null + pot. n & prot. n => pot. null
		// pot. null + pot. nn & pot. un => pot. n & pot. nn
		// pot. null + pot. nn & prot. nn => pot. n & pot. nn
		// pot. null + pot. non null => pot. n & pot. nn
		// pot. null + pot. null => pot. null
		// pot. null + pot. unknown => pot. n & pot. un
		// pot. null + prot. non null => pot. null
		// pot. null + prot. null => pot. null
		// pot. null + start => pot. null
		// pot. unknown + def. non null => pot. nn & pot. un
		// pot. unknown + def. null => pot. n & pot. un
		// pot. unknown + def. unknown => pot. unknown
		// pot. unknown + pot. n & pot. nn & pot. un => pot. n & pot. nn // should ideally include un
		// pot. unknown + pot. n & pot. nn => pot. n & pot. nn
		// pot. unknown + pot. n & pot. un => pot. n & pot. un
		// pot. unknown + pot. n & prot. n => pot. n & pot. un
		// pot. unknown + pot. nn & pot. un => pot. nn & pot. un
		// pot. unknown + pot. nn & prot. nn => pot. nn & pot. un
		// pot. unknown + pot. non null => pot. nn & pot. un
		// pot. unknown + pot. null => pot. n & pot. un
		// pot. unknown + pot. unknown => pot. unknown
		// pot. unknown + prot. non null => pot. unknown
		// pot. unknown + prot. null => pot. unknown
		// pot. unknown + start => pot. unknown
		// prot. non null + def. non null => pot. nn & prot. nn
		// prot. non null + def. null => pot. null
		// prot. non null + def. unknown => pot. unknown
		// prot. non null + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// prot. non null + pot. n & pot. nn => pot. n & pot. nn
		// prot. non null + pot. n & pot. un => pot. n & pot. un
		// prot. non null + pot. n & prot. n => pot. null
		// prot. non null + pot. nn & pot. un => pot. nn & pot. un	// see test1500
		// prot. non null + pot. nn & prot. nn => pot. nn & prot. nn
		// prot. non null + pot. non null => pot. nn & prot. nn
		// prot. non null + pot. null => pot. null
		// prot. non null + pot. unknown => pot. unknown
		// prot. non null + prot. non null => prot. non null
		// prot. non null + prot. null => prot. non null
		// prot. non null + start => prot. non null
		// prot. null + def. non null => pot. non null
		// prot. null + def. null => pot. n & prot. n
		// prot. null + def. unknown => pot. unknown
		// prot. null + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// prot. null + pot. n & pot. nn => pot. n & pot. nn
		// prot. null + pot. n & pot. un => pot. n & pot. un
		// prot. null + pot. n & prot. n => pot. n & prot. n
		// prot. null + pot. nn & pot. un => pot. nn & pot. un
		// prot. null + pot. nn & prot. nn => pot. non null
		// prot. null + pot. non null => pot. non null
		// prot. null + pot. null => pot. n & prot. n
		// prot. null + pot. unknown => pot. unknown
		// prot. null + prot. non null => prot. null
		// prot. null + prot. null => prot. null
		// prot. null + start => prot. null
		// start + def. non null => pot. non null
		// start + def. null => pot. null
		// start + def. unknown => pot. unknown
		// start + pot. n & pot. nn & pot. un => pot. n & pot. nn // un?
		// start + pot. n & pot. nn => pot. n & pot. nn
		// start + pot. n & pot. un => pot. n & pot. un
		// start + pot. n & prot. n => pot. null
		// start + pot. nn & pot. un => pot. nn & pot. un
		// start + pot. nn & prot. nn => pot. non null
		// start + pot. non null => pot. non null
		// start + pot. null => pot. null
		// start + pot. unknown => pot. unknown
		// start + prot. non null => start // PREMATURE may need to be tainted?
		// start + prot. null => start		// PREMATURE may need to be tainted?
		// start + start => start
		// addPotentialInitializationsFrom DEFINITION END
		addPotentialInitializationsFrom =
			new ThreeDimensionalTransformation("addPotentialInitializationsFrom",
				new byte[][] {
				// addPotentialInitializationsFrom INITIALIZER START
				{0x00,0x00,0x00},
				{0x00,0x04,0x04},
				{0x00,0x08,0x08},
				{0x00,0x0C,0x0C},
				{0x00,0x10,0x10},
				{0x00,0x14,0x14},
				{0x00,0x18,0x18},
				{0x00,0x1C,0x18},
				{0x00,0x24,0x04},
				{0x00,0x28,0x08},
				{0x00,0x2C,0x08},
				{0x00,0x30,0x10},
				{0x00,0x34,0x10},
				{0x00,0x38,0x00},
				{0x00,0x3C,0x00},
				{0x04,0x00,0x04},
				{0x04,0x04,0x04},
				{0x04,0x08,0x0C},
				{0x04,0x0C,0x0C},
				{0x04,0x10,0x14},
				{0x04,0x14,0x14},
				{0x04,0x18,0x18},
				{0x04,0x1C,0x18},
				{0x04,0x24,0x04},
				{0x04,0x28,0x0C},
				{0x04,0x2C,0x0C},
				{0x04,0x30,0x14},
				{0x04,0x34,0x14},
				{0x04,0x38,0x04},
				{0x04,0x3C,0x04},
				{0x08,0x00,0x08},
				{0x08,0x04,0x0C},
				{0x08,0x08,0x08},
				{0x08,0x0C,0x0C},
				{0x08,0x10,0x18},
				{0x08,0x14,0x18},
				{0x08,0x18,0x18},
				{0x08,0x1C,0x18},
				{0x08,0x24,0x0C},
				{0x08,0x28,0x08},
				{0x08,0x2C,0x08},
				{0x08,0x30,0x18},
				{0x08,0x34,0x18},
				{0x08,0x38,0x08},
				{0x08,0x3C,0x08},
				{0x0C,0x00,0x0C},
				{0x0C,0x04,0x0C},
				{0x0C,0x08,0x0C},
				{0x0C,0x0C,0x0C},
				{0x0C,0x10,0x18},
				{0x0C,0x14,0x18},
				{0x0C,0x18,0x18},
				{0x0C,0x1C,0x18},
				{0x0C,0x24,0x0C},
				{0x0C,0x28,0x0C},
				{0x0C,0x2C,0x0C},
				{0x0C,0x30,0x18},
				{0x0C,0x34,0x18},
				{0x0C,0x38,0x0C},
				{0x0C,0x3C,0x0C},
				{0x10,0x00,0x10},
				{0x10,0x04,0x14},
				{0x10,0x08,0x18},
				{0x10,0x0C,0x18},
				{0x10,0x10,0x10},
				{0x10,0x14,0x14},
				{0x10,0x18,0x18},
				{0x10,0x1C,0x18},
				{0x10,0x24,0x14},
				{0x10,0x28,0x18},
				{0x10,0x2C,0x18},
				{0x10,0x30,0x10},
				{0x10,0x34,0x10},
				{0x10,0x38,0x10},
				{0x10,0x3C,0x10},
				{0x14,0x00,0x14},
				{0x14,0x04,0x14},
				{0x14,0x08,0x18},
				{0x14,0x0C,0x18},
				{0x14,0x10,0x14},
				{0x14,0x14,0x14},
				{0x14,0x18,0x18},
				{0x14,0x1C,0x18},
				{0x14,0x24,0x14},
				{0x14,0x28,0x18},
				{0x14,0x2C,0x18},
				{0x14,0x30,0x14},
				{0x14,0x34,0x14},
				{0x14,0x38,0x14},
				{0x14,0x3C,0x14},
				{0x18,0x00,0x18},
				{0x18,0x04,0x18},
				{0x18,0x08,0x18},
				{0x18,0x0C,0x18},
				{0x18,0x10,0x18},
				{0x18,0x14,0x18},
				{0x18,0x18,0x18},
				{0x18,0x1C,0x18},
				{0x18,0x24,0x18},
				{0x18,0x28,0x18},
				{0x18,0x2C,0x18},
				{0x18,0x30,0x18},
				{0x18,0x34,0x18},
				{0x18,0x38,0x18},
				{0x18,0x3C,0x18},
				{0x1C,0x00,0x1C},
				{0x1C,0x04,0x1C},
				{0x1C,0x08,0x1C},
				{0x1C,0x0C,0x1C},
				{0x1C,0x10,0x18},
				{0x1C,0x14,0x18},
				{0x1C,0x18,0x18},
				{0x1C,0x1C,0x18},
				{0x1C,0x24,0x1C},
				{0x1C,0x28,0x1C},
				{0x1C,0x2C,0x1C},
				{0x1C,0x30,0x18},
				{0x1C,0x34,0x18},
				{0x1C,0x38,0x1C},
				{0x1C,0x3C,0x1C},
				{0x24,0x00,0x24},
				{0x24,0x04,0x24},
				{0x24,0x08,0x24},
				{0x24,0x0C,0x24},
				{0x24,0x10,0x14},
				{0x24,0x14,0x14},
				{0x24,0x18,0x18},
				{0x24,0x1C,0x18},
				{0x24,0x24,0x24},
				{0x24,0x28,0x24},
				{0x24,0x2C,0x24},
				{0x24,0x30,0x14},
				{0x24,0x34,0x14},
				{0x24,0x38,0x24},
				{0x24,0x3C,0x24},
				{0x28,0x00,0x28},
				{0x28,0x04,0x24},
				{0x28,0x08,0x28},
				{0x28,0x0C,0x24},
				{0x28,0x10,0x18},
				{0x28,0x14,0x18},
				{0x28,0x18,0x18},
				{0x28,0x1C,0x18},
				{0x28,0x24,0x24},
				{0x28,0x28,0x28},
				{0x28,0x2C,0x28},
				{0x28,0x30,0x18},
				{0x28,0x34,0x18},
				{0x28,0x38,0x28},
				{0x28,0x3C,0x28},
				{0x2C,0x00,0x2C},
				{0x2C,0x04,0x0C},
				{0x2C,0x08,0x2C},
				{0x2C,0x0C,0x0C},
				{0x2C,0x10,0x18},
				{0x2C,0x14,0x18},
				{0x2C,0x18,0x18},
				{0x2C,0x1C,0x18},
				{0x2C,0x24,0x0C},
				{0x2C,0x28,0x2C},
				{0x2C,0x2C,0x2C},
				{0x2C,0x30,0x18},
				{0x2C,0x34,0x18},
				{0x2C,0x38,0x2C},
				{0x2C,0x3C,0x2C},
				{0x30,0x00,0x30},
				{0x30,0x04,0x14},
				{0x30,0x08,0x18},
				{0x30,0x0C,0x18},
				{0x30,0x10,0x30},
				{0x30,0x14,0x14},
				{0x30,0x18,0x18},
				{0x30,0x1C,0x18},
				{0x30,0x24,0x14},
				{0x30,0x28,0x18},
				{0x30,0x2C,0x18},
				{0x30,0x30,0x30},
				{0x30,0x34,0x30},
				{0x30,0x38,0x30},
				{0x30,0x3C,0x30},
				{0x34,0x00,0x34},
				{0x34,0x04,0x14},
				{0x34,0x08,0x18},
				{0x34,0x0C,0x18},
				{0x34,0x10,0x34},
				{0x34,0x14,0x14},
				{0x34,0x18,0x18},
				{0x34,0x1C,0x18},
				{0x34,0x24,0x14},
				{0x34,0x28,0x18},
				{0x34,0x2C,0x18},
				{0x34,0x30,0x34},
				{0x34,0x34,0x34},
				{0x34,0x38,0x34},
				{0x34,0x3C,0x34},
				{0x38,0x00,0x38},
				{0x38,0x04,0x04},
				{0x38,0x08,0x08},
				{0x38,0x0C,0x0C},
				{0x38,0x10,0x34},
				{0x38,0x14,0x14},
				{0x38,0x18,0x18},
				{0x38,0x1C,0x1C},
				{0x38,0x24,0x04},
				{0x38,0x28,0x08},
				{0x38,0x2C,0x08},
				{0x38,0x30,0x34},
				{0x38,0x34,0x34},
				{0x38,0x38,0x38},
				{0x38,0x3C,0x38},
				{0x3C,0x00,0x3C},
				{0x3C,0x04,0x04},
				{0x3C,0x08,0x2C},
				{0x3C,0x0C,0x0C},
				{0x3C,0x10,0x10},
				{0x3C,0x14,0x14},
				{0x3C,0x18,0x18},
				{0x3C,0x1C,0x1C},
				{0x3C,0x24,0x04},
				{0x3C,0x28,0x2C},
				{0x3C,0x2C,0x2C},
				{0x3C,0x30,0x10},
				{0x3C,0x34,0x10},
				{0x3C,0x38,0x3C},
				{0x3C,0x3C,0x3C},
				// addPotentialInitializationsFrom INITIALIZER END
				}) {
			@Override
			UnconditionalFlowInfo output(UnconditionalFlowInfo input1,
					UnconditionalFlowInfo input2) {
				return (UnconditionalFlowInfo)
					input1.copy().addPotentialInitializationsFrom(input2);
			}
		},
		mergedWith =
		// mergedWith DEFINITION START
		// def. non null + def. non null => def. non null
		// def. non null + def. null => pot. n & pot. nn
		// def. non null + pot. n & prot. n => pot. n & pot. nn
		// def. non null + pot. nn & prot. nn => pot. nn & prot. nn
		// def. non null + prot. non null => pot. nn & prot. nn
		// def. non null + prot. null => pot. non null		 // PREMATURE should become tainted null & pot. nn... not really, depends on the three way merge... or even on the conditions that got there (pb with no contrib prot. null branch)
		// def. null + def. null => def. null
		// def. null + pot. n & prot. n => pot. n & prot. n
		// def. null + prot. non null => pot. null
		// def. null + prot. null => pot. n & prot. n
		// def. unknown + def. non null => pot. nn & pot. un
		// def. unknown + def. null => pot. n & pot. un // pot. n priv. over def. unknown
		// def. unknown + def. unknown => def. unknown
		// def. unknown + pot. n & prot. n => pot. n & pot. un
		// def. unknown + pot. nn & prot. nn => pot. nn & pot. un
		// def. unknown + prot. non null => def. unknown // test726
		// def. unknown + prot. null => pot. unknown	// PREMATURE possibly wrong, but no test case yet
		// pot. n & pot. nn & pot. un + def. non null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + def. null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + def. unknown => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. n & prot. n => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + pot. nn & prot. nn => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + prot. non null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn & pot. un + prot. null => pot. n & pot. nn & pot. un
		// pot. n & pot. nn + def. non null => pot. n & pot. nn
		// pot. n & pot. nn + def. null => pot. n & pot. nn
		// pot. n & pot. nn + def. unknown => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. n & pot. nn + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & pot. nn + pot. n & prot. n => pot. n & pot. nn
		// pot. n & pot. nn + pot. nn & prot. nn => pot. n & pot. nn
		// pot. n & pot. nn + prot. non null => pot. n & pot. nn
		// pot. n & pot. nn + prot. null => pot. n & pot. nn
		// pot. n & pot. un + def. non null => pot. n & pot. nn
		// pot. n & pot. un + def. null => pot. n & pot. un
		// pot. n & pot. un + def. unknown => pot. n & pot. un
		// pot. n & pot. un + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. n & pot. un + pot. n & pot. nn => pot. n & pot. nn
		// pot. n & pot. un + pot. n & pot. un => pot. n & pot. un
		// pot. n & pot. un + pot. n & prot. n => pot. n & pot. un
		// pot. n & pot. un + pot. nn & prot. nn => pot. n & pot. nn
		// pot. n & pot. un + prot. non null => pot. n & pot. un
		// pot. n & pot. un + prot. null => pot. n & pot. un
		// pot. n & prot. n + pot. n & prot. n => pot. n & prot. n
		// pot. n & prot. n + prot. non null => pot. null
		// pot. n & prot. n + prot. null => pot. n & prot. n
		// pot. nn & pot. un + def. non null => pot. nn & pot. un
		// pot. nn & pot. un + def. null => pot. n & pot. nn
		// pot. nn & pot. un + def. unknown => pot. nn & pot. un
		// pot. nn & pot. un + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. nn & pot. un + pot. n & pot. nn => pot. n & pot. nn
		// pot. nn & pot. un + pot. n & pot. un => pot. n & pot. nn
		// pot. nn & pot. un + pot. n & prot. n => pot. n & pot. nn
		// pot. nn & pot. un + pot. nn & pot. un => pot. nn & pot. un
		// pot. nn & pot. un + pot. nn & prot. nn => pot. nn & pot. un
		// pot. nn & pot. un + pot. null => pot. n & pot. nn
		// pot. nn & pot. un + prot. non null => pot. nn & pot. un
		// pot. nn & pot. un + prot. null => pot. n & pot. nn
		// pot. nn & prot. nn + def. null => pot. n & pot. nn
		// pot. nn & prot. nn + pot. n & prot. n => pot. n & pot. nn
		// pot. nn & prot. nn + pot. nn & prot. nn => pot. nn & prot. nn
		// pot. nn & prot. nn + prot. non null => pot. nn & prot. nn
		// pot. nn & prot. nn + prot. null => pot. n & pot. nn
		// pot. non null + def. non null => pot. non null
		// pot. non null + def. null => pot. n & pot. nn
		// pot. non null + def. unknown => pot. nn & pot. un
		// pot. non null + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. non null + pot. n & pot. nn => pot. n & pot. nn
		// pot. non null + pot. n & pot. un => pot. n & pot. nn
		// pot. non null + pot. n & prot. n => pot. n & pot. nn
		// pot. non null + pot. nn & pot. un => pot. nn & pot. un
		// pot. non null + pot. nn & prot. nn => pot. non null
		// pot. non null + pot. non null => pot. non null
		// pot. non null + pot. null => pot. n & pot. nn
		// pot. non null + prot. non null => pot. non null
		// pot. non null + prot. null => pot. n & pot. nn
		// pot. null + def. non null => pot. n & pot. nn
		// pot. null + def. null => pot. null
		// pot. null + def. unknown => pot. n & pot. un
		// pot. null + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. null + pot. n & pot. nn => pot. n & pot. nn
		// pot. null + pot. n & pot. un => pot. n & pot. un
		// pot. null + pot. n & prot. n => pot. null
		// pot. null + pot. nn & prot. nn => pot. n & pot. nn
		// pot. null + pot. null => pot. null
		// pot. null + prot. non null => pot. null
		// pot. null + prot. null => pot. null
		// pot. unknown + def. non null => pot. nn & pot. un
		// pot. unknown + def. null => pot. n & pot. un
		// pot. unknown + def. unknown => pot. unknown
		// pot. unknown + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// pot. unknown + pot. n & pot. nn => pot. n & pot. nn
		// pot. unknown + pot. n & pot. un => pot. n & pot. un
		// pot. unknown + pot. n & prot. n => pot. n & pot. un
		// pot. unknown + pot. nn & pot. un => pot. nn & pot. un
		// pot. unknown + pot. nn & prot. nn => pot. nn & pot. un
		// pot. unknown + pot. non null => pot. nn & pot. un
		// pot. unknown + pot. null => pot. n & pot. un
		// pot. unknown + pot. unknown => pot. unknown
		// pot. unknown + prot. non null => pot. unknown
		// pot. unknown + prot. null => pot. unknown // PREMATURE possibly wrong, but no test case yet
		// prot. non null + prot. non null => prot. non null
		// prot. null + prot. non null => pot. null // PREMATURE use tainted instead
		// prot. null + prot. null => prot. null
		// start + def. non null => pot. non null
		// start + def. null => pot. null
		// start + def. unknown => pot. unknown
		// start + pot. n & pot. nn & pot. un => pot. n & pot. nn & pot. un
		// start + pot. n & pot. nn => pot. n & pot. nn
		// start + pot. n & pot. un => pot. n & pot. un
		// start + pot. n & prot. n => pot. null
		// start + pot. nn & pot. un => pot. nn & pot. un
		// start + pot. nn & prot. nn => pot. non null
		// start + pot. non null => pot. non null
		// start + pot. null => pot. null
		// start + pot. unknown => pot. unknown
		// start + prot. non null => start // PREMATURE should it taint?
		// start + prot. null => start
		// start + start => start
		// mergedWith DEFINITION END
			new SymmetricalThreeDimensionalTransformation("mergedWith",
				new byte[][] {
				// mergedWith INITIALIZER START
				{0x00,0x00,0x00},
				{0x00,0x04,0x04},
				{0x00,0x08,0x08},
				{0x00,0x0C,0x0C},
				{0x00,0x10,0x10},
				{0x00,0x14,0x14},
				{0x00,0x18,0x18},
				{0x00,0x1C,0x1C},
				{0x00,0x24,0x04},
				{0x00,0x28,0x08},
				{0x00,0x2C,0x08},
				{0x00,0x30,0x10},
				{0x00,0x34,0x10},
				{0x00,0x38,0x00},
				{0x00,0x3C,0x00},
				{0x04,0x04,0x04},
				{0x04,0x08,0x0C},
				{0x04,0x0C,0x0C},
				{0x04,0x10,0x14},
				{0x04,0x14,0x14},
				{0x04,0x18,0x18},
				{0x04,0x1C,0x1C},
				{0x04,0x24,0x04},
				{0x04,0x28,0x0C},
				{0x04,0x2C,0x0C},
				{0x04,0x30,0x14},
				{0x04,0x34,0x14},
				{0x04,0x38,0x04},
				{0x04,0x3C,0x04},
				{0x08,0x08,0x08},
				{0x08,0x0C,0x0C},
				{0x08,0x10,0x18},
				{0x08,0x14,0x18},
				{0x08,0x18,0x18},
				{0x08,0x1C,0x1C},
				{0x08,0x24,0x0C},
				{0x08,0x28,0x08},
				{0x08,0x2C,0x08},
				{0x08,0x30,0x18},
				{0x08,0x34,0x18},
				{0x08,0x38,0x18},
				{0x08,0x3C,0x08},
				{0x0C,0x0C,0x0C},
				{0x0C,0x10,0x18},
				{0x0C,0x14,0x18},
				{0x0C,0x18,0x18},
				{0x0C,0x1C,0x1C},
				{0x0C,0x24,0x0C},
				{0x0C,0x28,0x0C},
				{0x0C,0x2C,0x0C},
				{0x0C,0x30,0x18},
				{0x0C,0x34,0x18},
				{0x0C,0x38,0x18},
				{0x0C,0x3C,0x0C},
				{0x10,0x10,0x10},
				{0x10,0x14,0x14},
				{0x10,0x18,0x18},
				{0x10,0x1C,0x1C},
				{0x10,0x24,0x14},
				{0x10,0x28,0x18},
				{0x10,0x2C,0x18},
				{0x10,0x30,0x10},
				{0x10,0x34,0x10},
				{0x10,0x38,0x10},
				{0x10,0x3C,0x10},
				{0x14,0x14,0x14},
				{0x14,0x18,0x18},
				{0x14,0x1C,0x1C},
				{0x14,0x24,0x14},
				{0x14,0x28,0x18},
				{0x14,0x2C,0x18},
				{0x14,0x30,0x14},
				{0x14,0x34,0x14},
				{0x14,0x38,0x14},
				{0x14,0x3C,0x14},
				{0x18,0x18,0x18},
				{0x18,0x1C,0x1C},
				{0x18,0x24,0x18},
				{0x18,0x28,0x18},
				{0x18,0x2C,0x18},
				{0x18,0x30,0x18},
				{0x18,0x34,0x18},
				{0x18,0x38,0x18},
				{0x18,0x3C,0x18},
				{0x1C,0x1C,0x1C},
				{0x1C,0x24,0x1C},
				{0x1C,0x28,0x1C},
				{0x1C,0x2C,0x1C},
				{0x1C,0x30,0x1C},
				{0x1C,0x34,0x1C},
				{0x1C,0x38,0x1C},
				{0x1C,0x3C,0x1C},
				{0x24,0x24,0x24},
				{0x24,0x28,0x0C},
				{0x24,0x2C,0x0C},
				{0x24,0x30,0x14},
				{0x24,0x34,0x14},
				{0x24,0x38,0x04},
				{0x24,0x3C,0x24},
				{0x28,0x28,0x28},
				{0x28,0x2C,0x2C},
				{0x28,0x30,0x18},
				{0x28,0x34,0x18},
				{0x28,0x38,0x08},
				{0x28,0x3C,0x2C},
				{0x2C,0x2C,0x2C},
				{0x2C,0x30,0x18},
				{0x2C,0x34,0x18},
				{0x2C,0x38,0x18},
				{0x2C,0x3C,0x2C},
				{0x30,0x30,0x30},
				{0x30,0x34,0x34},
				{0x30,0x38,0x34},
				{0x30,0x3C,0x10},
				{0x34,0x34,0x34},
				{0x34,0x38,0x34},
				{0x34,0x3C,0x10},
				{0x38,0x38,0x38},
				{0x38,0x3C,0x10},
				{0x3C,0x3C,0x3C},
				// mergedWith INITIALIZER END
				}) {
			@Override
			UnconditionalFlowInfo output(UnconditionalFlowInfo input1,
					UnconditionalFlowInfo input2) {
				return input1.copy().mergedWith(input2);
			}
		};
	public static final Transformation[] transformations = {
			markAsComparedEqualToNonNull,
			markAsComparedEqualToNull,
			markAsDefinitelyNonNull,
			markAsDefinitelyNull,
			markAsDefinitelyUnknown,
			addInitializationsFrom,
			addPotentialInitializationsFrom,
			mergedWith
		};
public abstract static class Transformation {
	public String name;
	String	definitionStartMarker, definitionEndMarker,
		initializerStartMarker,	initializerEndMarker;
	int dimension;
	public Map initializedTransitions, computedTransitions;
		// PREMATURE limit public access
	int failuresNb; // transient
// PREMATURE complete the test coverage for NullInfoRegistry (need to consider several classes
//  		 of transitions so as to avoid considering NullInfoRegistry states and
//			 UnconditionalFlowInfo states into the same pool; moreover, cross classes
//			 transformations exist.
Transformation(String name) {
	this.name = name;
	this.definitionStartMarker = "// " + name + " " + CodeAnalysis.definitionStartMarker;
	this.definitionEndMarker = "// " + name + " " + CodeAnalysis.definitionEndMarker;
	this.initializerStartMarker = "// " + name + " " + CodeAnalysis.initializerStartMarker;
	this.initializerEndMarker = "// " + name + " " + CodeAnalysis.initializerEndMarker;
}
abstract State[] computeOutputs(State[] inputs);
abstract void hydrate();
void fail() {
	if (this.failuresNb == 0) {
		System.out.println(this.name + " failures: ");
	}
	this.failuresNb++;
}
abstract void printTruthTables(File outputDirectory);

static boolean checkContiguity(String a, String b) {
	int aLength;
	if ((aLength = a.length()) != b.length()) {
		System.out.println("inappropriate string length: " + a + " vs " + b);
		return false;
	}
	int status = 0;
	for (int i = 0; i < aLength; i++) {
		if (a.charAt(i) != b.charAt(i)) {
			status++;
		}
	}
	if (status != 1) {
		System.out.println("non contiguous strings: " + a + " vs " + b);
		return false;
	}
	return true;
}

final static String truthTableRowNames[] = { // need a specific order to yield simplication opportunities
		"000000",
		"000100",
		"001100",
		"001000",
		"011000",
		"011100",
		"010100",
		"010000",
		"110000",
		"110100",
		"111100",
		"111000",
		"101000",
		"101100",
		"100100",
		"100000",
		// PREMATURE cheated to group first four bits... reconsider
		"000001",
		"000011",
		"000010",
		"000110",
		"000111",
		"000101",
		"001101",
		"001111",
		"001110",
		"001010",
		"001011",
		"001001",
		"011001",
		"011011",
		"011010",
		"011110",
		"011111",
		"011101",
		"010101",
		"010111",
		"010110",
		"010010",
		"010011",
		"010001",
		"110001",
		"110011",
		"110010",
		"110110",
		"110111",
		"110101",
		"111101",
		"111111",
		"111110",
		"111010",
		"111011",
		"111001",
		"101001",
		"101011",
		"101010",
		"101110",
		"101111",
		"101101",
		"100101",
		"100111",
		"100110",
		"100010",
		"100011",
		"100001",
	};

private static Map ranksForStates;
int rankForState(State state) {
	int length;
	if (ranksForStates == null) {
		ranksForStates = new HashMap(length = truthTableRowNames.length);
		for (int i = 0; i < length; i++) {
			ranksForStates.put(truthTableRowNames[i], Integer.valueOf(i));
		}
	}
	Integer rank;
	if ((rank = (Integer) ranksForStates.get(state.printableBitsField)) != null) {
		return rank.intValue();
	}
	return 0;
}

abstract void reinitializeFromComments(BufferedReader input, BufferedWriter output);
abstract void reinitializeFromComputedValues(BufferedReader input, BufferedWriter output,
	State[] consideredStates);
/**
 * Run a test against UnconditionalFlowInfo by comparing the transitions as memorized
 * into the initializer and as delivered by UnconditionalFlowInfo for various positions
 * in the encoding and return the number of failures.
 * @return the number of failures, that is 0 if the results match the expectations
 */
abstract int test();
}
abstract static class TwoDimensionalTransformation extends Transformation {
TwoDimensionalTransformation(String name, byte[][] transitions) {
	super(name);
	this.dimension = 2;
	int length;
	this.initializedTransitions = new HashMap(length = transitions.length);
	State input1;
	for (int i = 0; i < length; i++) {
		if (transitions[i].length != 2) {
			throw new IllegalArgumentException("transitions should have two entries");
		}
		input1 = State.states[transitions[i][0]]; // array out of bounds exception if broken
		if (this.initializedTransitions.get(input1) != null) {
			throw new IllegalArgumentException("duplicate entry");
		}
		this.initializedTransitions.put(input1, State.states[transitions[i][1]]);
	}
}
@Override
State[] computeOutputs(State[] inputs) {
	Map resultAccumulator = new HashMap(State.stateMaxValue + 1);
	hydrate(); // pre-compute all possible combinations, then cache them
	for (int i = 0, inputsLength = inputs.length; i < inputsLength; i++) {
		resultAccumulator.put(this.computedTransitions.get(inputs[i]), null);
	}
	int length;
	State[] result = new State[length = resultAccumulator.size()];
	Iterator resultIterator = resultAccumulator.keySet().iterator();
	for (int j = 0; j < length; j++) {
		result[j] = (State) resultIterator.next();
	}
	return result;
}
@Override
void hydrate() {
	if (this.computedTransitions == null) {
		State input, output;
		this.computedTransitions = new HashMap(State.stateMaxValue + 1);
		for (int i = 0, length = State.states.length; i < length; i++) {
			output = ((UnconditionalFlowInfoTestHarness)
				output(UnconditionalFlowInfoTestHarness.
					testUnconditionalFlowInfo(input = State.states[i]),
						TestLocalVariableBinding.local0)).asState();
			if (input.symbolic && !output.symbolic) {
				System.err.println(this.name + " generates non-symbolic state " +
					output + " upon entry: " + input);
			}
			this.computedTransitions.put(input, output);
		}
	}
}
abstract UnconditionalFlowInfo output(UnconditionalFlowInfo input, TestLocalVariableBinding local);

@Override
void printTruthTables(File outputDirectory) {
	try {
		String outputFileName = outputDirectory.getPath() + File.separator + this.name + ".txt";
		PrintWriter out = new PrintWriter(new FileOutputStream(
				new File(outputFileName)));
		System.out.println("Printing " + outputFileName);
		out.println("======================================================");
		out.println("Truth table for " + this.name );
		char truthValues[][] = new char[State.statesNb][State.stateWidth];
		int row, column;
		for (row = 0; row < State.statesNb; row++) {
			for (column = 0; column < State.stateWidth; column++) {
				truthValues[row][column] = '.';
			}
		}
		boolean keepRow[] = new boolean[State.statesNb];
		Iterator i1 = this.initializedTransitions.entrySet().iterator();
		while (i1.hasNext()) {
			Map.Entry transitionsSet = (Map.Entry) i1.next();
			State input = (State) transitionsSet.getKey();
			keepRow[row = rankForState(input)] = true;
			for (int bit = 0; bit < State.stateWidth; bit++) {
				truthValues[row][bit] =
					((State) transitionsSet.getValue()).printableBitsField.charAt(bit);
			}
		}
		StringBuilder line;
		line = new StringBuilder(140);
		line.append("         ");
		for (int i = 1; i <= State.stateWidth; i++) {
			line.append(i);
			line.append(' ');
		}
		out.println(line);
		line = new StringBuilder(140);
		line.append("       ---------------------");
		out.println(line);
		for (row = 0; row < State.statesNb; row++) {
			if (keepRow[row]) {
				line = new StringBuilder(140);
				line.append(truthTableRowNames[row]);
				line.append(" | ");
				for (int i = 0; i < State.stateWidth; i++) {
					line.append(truthValues[row][i]);
					line.append(' ');
				}
				out.println(line);
			}
		}
		out.println("======================================================");
		out.flush();
		out.close();
	}
	catch (Throwable t) {
		// PREMATURE improve error handling
	}
}

@Override
void reinitializeFromComments(BufferedReader input, BufferedWriter output) {
	String line, tab = "";
	int cursor;
	char c;
	this.initializedTransitions = new HashMap(State.stateMaxValue);
	int lineNumber = 0;
	try {
		while ((line = input.readLine()) != null) {
			lineNumber++;
			output.write(line);
			output.write('\n');
			if ((cursor = line.indexOf(this.definitionStartMarker)) != -1) {
				// check the line format
				boolean reachedStart = true;
				for (int i = 0; i < cursor; i++) {
					if (!Character.isWhitespace(c = line.charAt(i))) {
						reachedStart = false;
						break;
					}
					else {
						tab += c;
					}
				}
				if (reachedStart) {
					while ((line = input.readLine()) != null &&
							line.indexOf(this.definitionEndMarker) == -1) {
						lineNumber++;
						final int
							start = 0,
							commentStart = 1,
							commentFound = 2,
							firstState = 10,
							firstStateWS = 11,
							implies = 20,
							impliesWS = 21,
							secondState = 30,
							secondStateWS = 31, // caveat, multi-state
							error = 99;
						int state = start,
							firstStateStart = 0, firstStateEnd = 0, firstStateWhiteSpace = 0,
							secondStateStart = 0, secondStateEnd = 0, secondStateWhiteSpace = 0;
						char current;
						analysis: for (int i = 0, length = line.length(); i < length; i++) {
							current = line.charAt(i);
							switch (state) {
								case start:
									if (current == '/') {
										state = commentStart;
									} else if (! Character.isWhitespace(current)) {
										state = error;
										break analysis;
									}
									break;
								case commentStart:
									if (current == '/') {
										state = commentFound;
									} else {
										state = error;
										break analysis;
									}
									break;
								case commentFound:
									if (! Character.isWhitespace(current)) {
										firstStateStart = firstStateEnd = i;
										state = firstState;
									}
									break;
								case firstState:
									if (Character.isWhitespace(current)) {
										state = firstStateWS;
										firstStateWhiteSpace = 1;
									} else {
										firstStateEnd++;
									}
									break;
								case firstStateWS:
									if (current == '=') {
										state = implies;
									} else if (Character.isWhitespace(current)) {
										firstStateWhiteSpace++;
									} else {
										state = firstState;
										firstStateEnd += firstStateWhiteSpace + 1;
									}
									break;
								case implies:
									if (current == '>') {
										state = impliesWS;
									} else {
										state = error;
										break analysis;
									}
									break;
								case impliesWS:
									if (! Character.isWhitespace(current)) {
										secondStateStart = secondStateEnd = i;
										state = secondState;
									}
									break;
								case secondState:
									if (current == '/') {
										break analysis;
									} else if (Character.isWhitespace(current)) {
										state = secondStateWS;
										secondStateWhiteSpace = 1;
									} else {
										secondStateEnd++;
									}
									break;
								case secondStateWS:
									if (current == '/') {
										state = secondState;
										break analysis;
									} else if (current == 'C') {
										state++;
									} else if (Character.isWhitespace(current)) {
										secondStateWhiteSpace++;
									} else {
										state = secondState;
										secondStateEnd += secondStateWhiteSpace + 1;
									}
									break;
								case secondStateWS + 1:
									if (current == '/') {
										state = secondState;
										break analysis;
									} else if (current == 'H') {
										state++;
									} else {
										state = secondState;
										secondStateEnd += secondStateWhiteSpace + 2;
									}
									break;
								case secondStateWS + 2:
									if (current == '/') {
										state = secondState;
										break analysis;
									} else if (current == 'E') {
										state++;
									} else {
										state = secondState;
										secondStateEnd += secondStateWhiteSpace + 3;
									}
									break;
								case secondStateWS + 3:
									if (current == '/') {
										state = secondState;
										break analysis;
									} else if (current == 'C') {
										state++;
									} else {
										state = secondState;
										secondStateEnd += secondStateWhiteSpace + 4;
									}
									break;
								case secondStateWS + 4:
									if (current == '/' || current == 'K') {
										state = secondState;
										break analysis;
									} else {
										state = secondState;
										secondStateEnd += secondStateWhiteSpace + 5;
									}
									break;

							}
						}
						if (state == error || state < secondState) {
							System.err.println("Could not interpret comment definition"); // PREMATURE improve diagnostic
						} else {
							if (state > secondStateWS) {
								secondStateEnd += (state - secondState);
							}
							String stateName;
							State first, second;
							if ((first = State.fromSymbolicName(
										stateName = line.substring(firstStateStart, firstStateEnd + 1)))
									== null) {
								System.err.println("Could not find state: " + stateName); // PREMATURE improve diagnostic
							}
							if ((second = State.fromSymbolicName(
										stateName = line.substring(secondStateStart, secondStateEnd + 1)))
									== null) {
								System.err.println("Could not find state: " + stateName); // PREMATURE improve diagnostic
							}
							if (first != null && second != null) {
								if (this.initializedTransitions.get(first) != null) {
									System.err.println("Line " + lineNumber + ": Skipping duplicate entry for state: " + first); // PREMATURE improve diagnostic
								} else {
									this.initializedTransitions.put(first, second);
								}
							}
						}
						output.write(line);
						output.write('\n');
					}
					Iterator firsts = State.symbolicStates();
					State first;
					while (firsts.hasNext()) {
						first = (State) firsts.next();
						if (this.initializedTransitions.get(first) == null) {
								System.err.println("Adding missing transition for state: " + first);
								output.write(tab);
								output.write("// ");
								output.write(first.toString());
								output.write(" => start\t\t CHECK\n");
						}
					}
				}
				output.write(tab + this.definitionEndMarker + "\n");
			}
			if (line != null && (cursor = line.indexOf(this.initializerStartMarker)) != -1) {
				// check the line format
				boolean reachedStart = true;
				tab = "";
				for (int i = 0; i < cursor; i++) {
					if (!Character.isWhitespace(c = line.charAt(i))) {
						reachedStart = false;
						break;
					}
					else {
						tab += c;
					}
				}
				if (reachedStart) {
					while ((line = input.readLine()) != null &&
							line.indexOf(this.initializerEndMarker) == -1) {
						// loop
					}
					SortedMap sorted = new TreeMap(this.initializedTransitions);
					Iterator transitions = sorted.entrySet().iterator();
					Map.Entry transition;
					while (transitions.hasNext()) {
						transition = (Map.Entry) transitions.next();
						output.write(tab);
						output.write('{');
						output.write(((State)transition.getKey()).hexString);
						output.write(',');
						output.write(((State)transition.getValue()).hexString);
						output.write("},");
						output.write('\n');
					}
					output.write(tab + this.initializerEndMarker + "\n");
				}
			}
		}
		output.flush();
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
}

@Override
void reinitializeFromComputedValues(BufferedReader input, BufferedWriter output,
		State[] consideredStates) {
	String line, tab = "";
	int cursor;
	char c;
	State result;
	try {
		while ((line = input.readLine()) != null) {
			output.write(line);
			output.write('\n');
			if ((cursor = line.indexOf(this.definitionStartMarker)) != -1) {
				// check the line format
				boolean reachedStart = true;
				for (int i = 0; i < cursor; i++) {
					if (!Character.isWhitespace(c = line.charAt(i))) {
						reachedStart = false;
						break;
					}
					else {
						tab += c;
					}
				}
				if (reachedStart) {
					while ((line = input.readLine()) != null &&
							line.indexOf(this.definitionEndMarker) == -1) {
						// loop
					}
					int i, length;
					// definitions of two-dim trafos in natural order (by state value):
					for (i = 0, length = consideredStates.length; i < length; i++) {
						output.write(tab);
						output.write("// ");
						output.write(consideredStates[i].name);
						output.write(" => ");
						output.write(
							(result = (State) this.computedTransitions.get(consideredStates[i])).name);
						if (!result.symbolic ||
								result != this.initializedTransitions.get(consideredStates[i])) {
							output.write("\t\t CHECK");
						}
						output.write('\n');
					}
					output.write(tab + this.definitionEndMarker + "\n");
				}
			}
			if (line != null && (cursor = line.indexOf(this.initializerStartMarker)) != -1) {
				// check the line format
				boolean reachedStart = true;
				tab = "";
				for (int i = 0; i < cursor; i++) {
					if (!Character.isWhitespace(c = line.charAt(i))) {
						reachedStart = false;
						break;
					}
					else {
						tab += c;
					}
				}
				if (reachedStart) {
					while ((line = input.readLine()) != null &&
							line.indexOf(this.initializerEndMarker) == -1) {
						// loop
					}
					int i, length;
					for (i = 0, length = consideredStates.length; i < length; i++) {
						output.write(tab);
						output.write('{');
						output.write(consideredStates[i].hexString);
						output.write(',');
						output.write(
							((State) this.computedTransitions.get(consideredStates[i])).hexString);
						output.write("},");
						output.write('\n');
					}
					output.write(tab + this.initializerEndMarker + "\n");
				}
			}
		}
		output.flush();
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
}
@Override
int test() {
	Iterator transitions = this.initializedTransitions.entrySet().iterator();
	State input, expectedOutput, effectiveOutput;
	Map.Entry transition;
	this.failuresNb = 0; // reset
	while (transitions.hasNext()) {
		transition = (Map.Entry) transitions.next();
		input = (State) transition.getKey();
		expectedOutput = (State) transition.getValue();
		effectiveOutput = ((UnconditionalFlowInfoTestHarness)
			output(UnconditionalFlowInfoTestHarness.
				testUnconditionalFlowInfo(input),
					TestLocalVariableBinding.local0)).asState();
		if (effectiveOutput != expectedOutput) {
			fail();
			System.out.println("\t\t" + input.printableBitsField +
				" => " + effectiveOutput.printableBitsField +
				" instead of: " + expectedOutput.printableBitsField);
		}
	}
	transitions = this.initializedTransitions.entrySet().iterator();
	while (transitions.hasNext()) {
		transition = (Map.Entry) transitions.next();
		input = (State) transition.getKey();
		expectedOutput = (State) transition.getValue();
		effectiveOutput = ((UnconditionalFlowInfoTestHarness)
				output(UnconditionalFlowInfoTestHarness.
					testUnconditionalFlowInfo(input, 64),
						TestLocalVariableBinding.local64)).asState(64);
		if (effectiveOutput != expectedOutput) {
			fail();
			System.out.println("\t\t" + input.printableBitsField +
				" => " + effectiveOutput.printableBitsField +
				" (64) instead of: " + expectedOutput.printableBitsField);
		}
		if (input == State.start) {
			effectiveOutput = ((UnconditionalFlowInfoTestHarness)
				output(UnconditionalFlowInfoTestHarness.
					testUnconditionalFlowInfo(expectedOutput),
						TestLocalVariableBinding.local64)).asState(64);
			if (effectiveOutput != expectedOutput) {
				fail();
				System.out.println("\t\t" + input.printableBitsField +
					" => " + effectiveOutput.printableBitsField +
					" (zero 64) instead of: " + expectedOutput.printableBitsField);
			}
		}
	}
	transitions = this.initializedTransitions.entrySet().iterator();
	while (transitions.hasNext()) {
		transition = (Map.Entry) transitions.next();
		input = (State) transition.getKey();
		if (input == State.start) {
			expectedOutput = (State) transition.getValue();
			effectiveOutput = ((UnconditionalFlowInfoTestHarness)
				output(UnconditionalFlowInfoTestHarness.
					testUnconditionalFlowInfo(expectedOutput, 64),
						TestLocalVariableBinding.local128)).asState(128);
			if (effectiveOutput != expectedOutput) {
				fail();
				System.out.println("\t\t" + input.printableBitsField +
					" => " + effectiveOutput.printableBitsField +
					" (zero 128) instead of: " + expectedOutput.printableBitsField);
			}
		}
	}
	return this.failuresNb;
}
}
public abstract static class ThreeDimensionalTransformation extends Transformation {
private static final boolean CHECKING_ROW_NAMES = false;
ThreeDimensionalTransformation(String name) {
	super(name);
	this.dimension = 3;
}
ThreeDimensionalTransformation(String name, byte[][] transitions) {
	super(name);
	this.dimension = 3;
	int length;
	this.initializedTransitions = new HashMap(length = transitions.length);
	State input1, input2;
	for (int i = 0; i < length; i++) {
		if (transitions[i].length != 3) {
			throw new IllegalArgumentException("transitions should have three entries");
		}
		input1 = State.states[transitions[i][0]]; // array out of bounds exception if broken
		input2 = State.states[transitions[i][1]];
		Map transitionsForInput1 = (Map) this.initializedTransitions.get(input1);
		if (transitionsForInput1 == null) {
			transitionsForInput1 = new HashMap(length);
			this.initializedTransitions.put(input1, transitionsForInput1);
		}
		if (transitionsForInput1.get(input2) != null) {
			throw new IllegalArgumentException("duplicate entry");
		}
		transitionsForInput1.put(input2, State.states[transitions[i][2]]);
	}
}
@Override
State[] computeOutputs(State[] inputs) {
	Map resultAccumulator = new HashMap(State.stateMaxValue + 1);
	hydrate(); // pre-compute all possible combinations, then cache them
	for (int i = 0, inputsLength = inputs.length; i < inputsLength; i++) {
		for (int i2 = 0; i2 < inputsLength; i2++) {
			resultAccumulator.put(
				((Map) this.computedTransitions.get(inputs[i])).get(inputs[i2]), null);
		}
	}
	int length;
	State[] result = new State[length = resultAccumulator.size()];
	Iterator resultIterator = resultAccumulator.keySet().iterator();
	for (int j = 0; j < length; j++) {
		result[j] = (State) resultIterator.next();
	}
	return result;
}
@Override
void hydrate() {
	if (this.computedTransitions == null) {
		State input1, input2, output;
		this.computedTransitions = new HashMap(State.stateMaxValue + 1);
		Map entry;
		for (int i = 0, length = State.states.length; i < length; i++) {
			entry = new HashMap(State.stateMaxValue + 1);
			this.computedTransitions.put(input1 = State.states[i], entry);
			for (int j = 0; j < length; j++) {
				output = ((UnconditionalFlowInfoTestHarness)
					output(UnconditionalFlowInfoTestHarness.
						testUnconditionalFlowInfo(input1),
						UnconditionalFlowInfoTestHarness.
						testUnconditionalFlowInfo(input2 = State.states[j]))).asState();
				if (input1.symbolic && input2.symbolic && !output.symbolic) {
					System.err.println(this.name + " generates non-symbolic state " +
						output + " upon entry: " + input1 + " + " + input2);
				}
				entry.put(input2, output);
			}
		}
	}
}
abstract UnconditionalFlowInfo output(UnconditionalFlowInfo input1, UnconditionalFlowInfo input2);
void printDefinitions(BufferedWriter output, State[] consideredStates, String tab)
		throws IOException {
	int i, j, length = consideredStates.length;
	State result;
	// temporary store to support lexical sorting:
	String[] lines = new String[length * length];
	int lCount = 0;
	for (i = 0; i < length; i++) {
		for (j = 0; j < length; j++) {
			StringBuilder line = new StringBuilder();
			line.append(tab);
			line.append("// ");
			line.append(consideredStates[i].name);
			line.append(" + ");
			line.append(consideredStates[j].name);
			line.append(" => ");
			line.append(
				(result = (State) getResult(this.computedTransitions, consideredStates[i], consideredStates[j])).name);
			if (!result.symbolic ||
				result != getResult(this.initializedTransitions, consideredStates[i], consideredStates[j])) {
				line.append("\t\t CHECK");
			}
			line.append('\n');
			lines[lCount++] = line.toString();
		}
	}
	Arrays.sort(lines);
	for (i = 0; i < lCount; i++) {
		output.write(lines[i]);
	}
}
Object getResult(Map transitions, State statei, State statej) {
	Object res1 = transitions.get(statei);
	if (res1 instanceof Map) {
		return ((Map)res1).get(statej);
	}
	return null;
}
void printInitializers(BufferedWriter output, State[] consideredStates, String tab)
		throws IOException {
	int i, j, length;
	for (i = 0, length = consideredStates.length; i < length; i++) {
		for (j = 0; j < length; j++) {
			output.write(tab);
			output.write('{');
			output.write(consideredStates[i].hexString);
			output.write(',');
			output.write(consideredStates[j].hexString);
			output.write(',');
			output.write(
				((State)
					((Map) this.computedTransitions.get(consideredStates[i])).get(consideredStates[j])).hexString);
			output.write("},");
			output.write('\n');
		}
				}
}
void printMissingEntries(BufferedWriter output, String tab) throws IOException {
	Iterator firsts = State.symbolicStates(), seconds;
	State first, second;
	Map transitions;
	while (firsts.hasNext()) {
		first = (State) firsts.next();
		seconds = State.symbolicStates();
		if ((transitions = (Map) this.initializedTransitions.get(first))
				== null) {
			while (seconds.hasNext()) {
				second = (State) seconds.next();
				System.err.println("Adding missing transition for states (" + first + ", " + second + ")");
				output.write(tab);
				output.write("// ");
				output.write(first.toString());
				output.write(" + ");
				output.write(second.toString());
				output.write(" => start\t\t CHECK\n");
			}
		} else {
			while (seconds.hasNext()) {
				second = (State) seconds.next();
				if (transitions.get(second) == null) {
					System.err.println("Adding missing transition for states (" + first + ", " + second + ")");
					output.write(tab);
					output.write("// ");
					output.write(first.toString());
					output.write(" + ");
					output.write(second.toString());
					output.write(" => start\t\t CHECK\n");
				}
			}
		}
	}
}
@Override
void printTruthTables(File outputDirectory) {
	for (int bit = 1; bit <= State.stateWidth; bit++) {
		try {
			String outputFileName = outputDirectory.getPath() + File.separator + this.name + "_" + bit + ".txt";
			PrintWriter out = new PrintWriter(new FileOutputStream(
					new File(outputFileName)));
			System.out.println("Printing " + outputFileName);
			out.println("======================================================");
			out.println("Truth table for " + this.name + " null bit " + bit);
			char truthValues[][] = new char[State.statesNb][State.statesNb];
			int row, column;
			for (row = 0; row < State.statesNb; row++) {
				for (column = 0; column < State.statesNb; column++) {
					truthValues[row][column] = '.';
				}
			}
			if (CHECKING_ROW_NAMES) { // checking row names
				boolean gotProblem = false;
				if (truthTableRowNames.length > State.statesNb) {
					System.out.println("row names table contains too many rows");
					gotProblem = true;
				}
				else if (truthTableRowNames.length < State.statesNb) {
					System.out.println("row names table contains too few rows");
					gotProblem = true;
				}
				Map check = new HashMap(State.statesNb);
				for (row = 0; row < truthTableRowNames.length; row++) {
					if (check.containsKey(truthTableRowNames[row])) {
						System.out.println("duplicate row: " + truthTableRowNames[row]);
						gotProblem = true;
					}
					else {
						check.put(truthTableRowNames[row], null);
					}
					if (row > 0 && !checkContiguity(truthTableRowNames[row - 1], truthTableRowNames[row])) {
						gotProblem = true;
					}
				}
		//		assertFalse("invalid rows table", gotProblem);
			}
			boolean keepRow[] = new boolean[State.statesNb],
				keepColumn[] = new boolean[State.statesNb];
			Iterator i1 = this.initializedTransitions.entrySet().iterator();
			while (i1.hasNext()) {
				Map.Entry transitionsSet = (Map.Entry) i1.next();
				State first = (State) transitionsSet.getKey();
				Iterator i2 = ((Map) transitionsSet.getValue()).entrySet().iterator();
				while (i2.hasNext()) {
					Map.Entry transition = (Map.Entry) i2.next();
					mark(truthValues, keepRow, keepColumn, rankForState(first),
							rankForState((State) transition.getKey()),
							((State) transition.getValue()).printableBitsField.charAt(bit - 1));
				}
			}
			for (row = 0; row < State.statesNb; row += 2) {
				if (keepRow[row]) {
					keepRow[row + 1] = true;
				}
				else if (keepRow[row + 1]) {
					keepRow[row] = true;
				}
				if (keepColumn[row]) {
					keepColumn[row + 1] = true;
				}
				else if (keepColumn[row + 1]) {
					keepColumn[row] = true;
				}
			}
			StringBuilder line;
			for (int i = 0; i < State.stateWidth; i++) {
				line = new StringBuilder(140);
				line.append("         ");
				for (column = 0; column < State.statesNb; column++) {
					if (keepColumn[column]) {
						line.append(truthTableRowNames[column].charAt(i));
						line.append(' ');
					}
				}
				out.println(line);
			}
			line = new StringBuilder(140);
			line.append("       --");
			for (column = 0; column < State.statesNb; column++) {
				if (keepColumn[column]) {
					line.append('-');
					line.append('-');
				}
			}
			out.println(line);
			for (row = 0; row < State.statesNb; row++) {
				if (keepRow[row]) {
					line = new StringBuilder(140);
					line.append(truthTableRowNames[row]);
					line.append(" | ");
					for (column = 0; column < State.statesNb; column++) {
						if (keepColumn[column]) {
							line.append(truthValues[row][column]);
							line.append(' ');
						}
					}
					out.println(line);
				}
			}
			out.println("======================================================");
			out.flush();
			out.close();
		}
		catch (Throwable t) {
			// PREMATURE improve error handling
		}
	}
}

void mark(char truthValues[][], boolean keepRow[], boolean keepColumn[],
		int row, int column, char value) {
	truthValues[row][column] = value;
	keepRow[row] = true;
	keepColumn[column] = true;
}

@Override
void reinitializeFromComments(BufferedReader input, BufferedWriter output) {
	String line, tab = "";
	int cursor;
	char c;
	this.initializedTransitions = new HashMap(State.stateMaxValue);
	int lineNumber = 0;
	try {
		while ((line = input.readLine()) != null) {
			lineNumber++;
			output.write(line);
			output.write('\n');
			if ((cursor = line.indexOf(this.definitionStartMarker)) != -1) {
				// check the line format
				boolean reachedStart = true;
				for (int i = 0; i < cursor; i++) {
					if (!Character.isWhitespace(c = line.charAt(i))) {
						reachedStart = false;
						break;
					}
					else {
						tab += c;
					}
				}
				if (reachedStart) {
					while ((line = input.readLine()) != null &&
							line.indexOf(this.definitionEndMarker) == -1) {
						lineNumber++;
						final int
							start = 0,
							commentStart = 1,
							commentFound = 2,
							firstState = 10,
							firstStateWS = 11,
							plus = 20,
							secondState = 30,
							secondStateWS = 31,
							implies = 40,
							impliesWS = 41,
							thirdState = 50,
							thirdStateWS = 51, // caveat, multi-state
							error = 99;
						int state = start,
							firstStateStart = 0, firstStateEnd = 0, firstStateWhiteSpace = 0,
							secondStateStart = 0, secondStateEnd = 0, secondStateWhiteSpace = 0,
							thirdStateStart = 0, thirdStateEnd = 0, thirdStateWhiteSpace = 0;
						char current;
						analysis: for (int i = 0, length = line.length(); i < length; i++) {
							current = line.charAt(i);
							switch (state) {
								case start:
									if (current == '/') {
										state = commentStart;
									} else if (! Character.isWhitespace(current)) {
										state = error;
										break analysis;
									}
									break;
								case commentStart:
									if (current == '/') {
										state = commentFound;
									} else {
										state = error;
										break analysis;
									}
									break;
								case commentFound:
									if (! Character.isWhitespace(current)) {
										firstStateStart = firstStateEnd = i;
										state = firstState;
									}
									break;
								case firstState:
									if (Character.isWhitespace(current)) {
										state = firstStateWS;
										firstStateWhiteSpace = 1;
									} else {
										firstStateEnd++;
									}
									break;
								case firstStateWS:
									if (current == '+') {
										state = plus;
									} else if (Character.isWhitespace(current)) {
										firstStateWhiteSpace++;
									} else {
										state = firstState;
										firstStateEnd += firstStateWhiteSpace + 1;
									}
									break;
								case plus:
									if (! Character.isWhitespace(current)) {
										secondStateStart = secondStateEnd = i;
										state = secondState;
									}
									break;
								case secondState:
									if (Character.isWhitespace(current)) {
										state = secondStateWS;
										secondStateWhiteSpace = 1;
									} else {
										secondStateEnd++;
									}
									break;
								case secondStateWS:
									if (current == '=') {
										state = implies;
									} else if (Character.isWhitespace(current)) {
										secondStateWhiteSpace++;
									} else {
										state = secondState;
										secondStateEnd += secondStateWhiteSpace + 1;
									}
									break;
								case implies:
									if (current == '>') {
										state = impliesWS;
									} else {
										state = error;
										break analysis;
									}
									break;
								case impliesWS:
									if (! Character.isWhitespace(current)) {
										thirdStateStart = thirdStateEnd = i;
										state = thirdState;
									}
									break;
								case thirdState:
									if (current == '/') {
										break analysis;
									} else if (Character.isWhitespace(current)) {
										state = thirdStateWS;
										thirdStateWhiteSpace = 1;
									} else {
										thirdStateEnd++;
									}
									break;
								case thirdStateWS:
									if (current == '/') {
										state = thirdState;
										break analysis;
									} else if (current == 'C') {
										state++;
									} else if (Character.isWhitespace(current)) {
										thirdStateWhiteSpace++;
									} else {
										state = thirdState;
										thirdStateEnd += thirdStateWhiteSpace + 1;
									}
									break;
								case thirdStateWS + 1:
									if (current == '/') {
										state = thirdState;
										break analysis;
									} else if (current == 'H') {
										state++;
									} else {
										state = thirdState;
										thirdStateEnd += thirdStateWhiteSpace + 2;
									}
									break;
								case thirdStateWS + 2:
									if (current == '/') {
										state = thirdState;
										break analysis;
									} else if (current == 'E') {
										state++;
									} else {
										state = thirdState;
										thirdStateEnd += thirdStateWhiteSpace + 3;
									}
									break;
								case thirdStateWS + 3:
									if (current == '/') {
										state = thirdState;
										break analysis;
									} else if (current == 'C') {
										state++;
									} else {
										state = thirdState;
										thirdStateEnd += thirdStateWhiteSpace + 4;
									}
									break;
								case thirdStateWS + 4:
									if (current == '/' || current == 'K') {
										state = thirdState;
										break analysis;
									} else {
										state = thirdState;
										thirdStateEnd += thirdStateWhiteSpace + 5;
									}
									break;

							}
						}
						if (state == error || state < thirdState) {
							System.err.println("Could not interpret comment definition"); // PREMATURE improve diagnostic
						} else {
							if (state > thirdStateWS) {
								thirdStateEnd += (state - thirdState);
							}
							String stateName;
							State first, second, third;
							if ((first = State.fromSymbolicName(
										stateName = line.substring(firstStateStart, firstStateEnd + 1)))
									== null) {
								System.err.println("Could not find state: " + stateName); // PREMATURE improve diagnostic
							}
							if ((second = State.fromSymbolicName(
										stateName = line.substring(secondStateStart, secondStateEnd + 1)))
									== null) {
								System.err.println("Could not find state: " + stateName); // PREMATURE improve diagnostic
							}
							if ((third = State.fromSymbolicName(
										stateName = line.substring(thirdStateStart, thirdStateEnd + 1)))
									== null) {
								System.err.println("Could not find state: " + stateName); // PREMATURE improve diagnostic
							}
							if (first != null && second != null && third != null) {
								Map transitions;
								if ((transitions = (Map) this.initializedTransitions.get(first)) == null) {
									transitions = new HashMap(State.stateMaxValue + 1);
									this.initializedTransitions.put(first, transitions);
								}
								if (transitions.get(second) != null) {
									System.err.println("Line " + lineNumber + ": Skipping duplicate entry for states: (" + first
											+ ", " + second + ")"); // PREMATURE improve diagnostic
								} else {
									transitions.put(second, third);
								}
							}
						}
						output.write(line);
						output.write('\n');
					}
				}
				printMissingEntries(output, tab);
				output.write(tab + this.definitionEndMarker + "\n");
			}
			if (line != null && (cursor = line.indexOf(this.initializerStartMarker)) != -1) {
				// check the line format
				boolean reachedStart = true;
				tab = "";
				for (int i = 0; i < cursor; i++) {
					if (!Character.isWhitespace(c = line.charAt(i))) {
						reachedStart = false;
						break;
					}
					else {
						tab += c;
					}
				}
				if (reachedStart) {
					while ((line = input.readLine()) != null &&
							line.indexOf(this.initializerEndMarker) == -1) {
						// loop
					}
					SortedMap sortedTransitionsSet = new TreeMap(this.initializedTransitions);
					Iterator transitionsSets = sortedTransitionsSet.entrySet().iterator();
					Map.Entry transitionsSet;
					while (transitionsSets.hasNext()) {
						transitionsSet = (Map.Entry) transitionsSets.next();
						SortedMap sortedTransitions = new TreeMap((Map) transitionsSet.getValue());
						Iterator transitions = sortedTransitions.entrySet().iterator();
						Map.Entry transition;
						while (transitions.hasNext()) {
							transition = (Map.Entry) transitions.next();
							output.write(tab);
							output.write('{');
							output.write(((State)transitionsSet.getKey()).hexString);
							output.write(',');
							output.write(((State)transition.getKey()).hexString);
							output.write(',');
							output.write(((State)transition.getValue()).hexString);
							output.write("},");
							output.write('\n');
						}
					}
					output.write(tab + this.initializerEndMarker + "\n");
				}
			}
		}
		output.flush();
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
}
@Override
void reinitializeFromComputedValues(BufferedReader input, BufferedWriter output,
		State[] consideredStates) {
	String line, tab = "";
	int cursor;
	char c;
	try {
		while ((line = input.readLine()) != null) {
			output.write(line);
			output.write('\n');
			if ((cursor = line.indexOf(this.definitionStartMarker)) != -1) {
				// check the line format
				boolean reachedStart = true;
				for (int i = 0; i < cursor; i++) {
					if (!Character.isWhitespace(c = line.charAt(i))) {
						reachedStart = false;
						break;
					}
					else {
						tab += c;
					}
				}
				if (reachedStart) {
					while ((line = input.readLine()) != null &&
							line.indexOf(this.definitionEndMarker) == -1) {
						// loop
					}
					printDefinitions(output, consideredStates, tab);
					output.write(tab + this.definitionEndMarker + "\n");
				}
			}
			if (line != null && (cursor = line.indexOf(this.initializerStartMarker)) != -1) {
				// check the line format
				boolean reachedStart = true;
				tab = "";
				for (int i = 0; i < cursor; i++) {
					if (!Character.isWhitespace(c = line.charAt(i))) {
						reachedStart = false;
						break;
					}
					else {
						tab += c;
					}
				}
				if (reachedStart) {
					while ((line = input.readLine()) != null &&
							line.indexOf(this.initializerEndMarker) == -1) {
						// loop
					}
					printInitializers(output, consideredStates, tab);
					output.write(tab + this.initializerEndMarker + "\n");
				}
			}
		}
		output.flush();
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
}
@Override
int test() {
	return test(1, false);
}
int test(int combinationTestsLoopsNb, boolean skipHighOrderBits) {
	Iterator transitionsSetsIterator, transitionsIterator;
	State input1, input2, expectedOutput, effectiveOutput;
	Map.Entry transition, transitionsSet;
	this.failuresNb = 0; // reset
	this.failuresNb = 0; // reset
	long start = 0;
	if (combinationTestsLoopsNb > 1) {
		start = System.currentTimeMillis();
	}
	for (int l = 0; l < combinationTestsLoopsNb ; l++) {
		transitionsSetsIterator = this.initializedTransitions.entrySet().iterator();
		while (transitionsSetsIterator.hasNext()) {
			transitionsSet = (Map.Entry) transitionsSetsIterator.next();
			input1 = (State) transitionsSet.getKey();
			transitionsIterator = ((Map) transitionsSet.getValue()).
				entrySet().iterator();
			while (transitionsIterator.hasNext()) {
				transition = (Map.Entry) transitionsIterator.next();
				input2 = (State) transition.getKey();
				expectedOutput = (State) transition.getValue();
				effectiveOutput = ((UnconditionalFlowInfoTestHarness) output(
						UnconditionalFlowInfoTestHarness.testUnconditionalFlowInfo(input1),
						UnconditionalFlowInfoTestHarness.testUnconditionalFlowInfo(input2)))
					.asState();
				if (effectiveOutput != expectedOutput) {
					fail();
					System.out.println("\t\t" + input1.printableBitsField +
						" + " + input2.printableBitsField +
						" => " + effectiveOutput.printableBitsField +
						" instead of: " + expectedOutput.printableBitsField);
				}
			}
		}
	}
	if (combinationTestsLoopsNb > 1) {
		System.out.println(this.name + "...\t\t" + combinationTestsLoopsNb + "\t" +
				(System.currentTimeMillis() - start));
	}
	// PREMATURE optimize test (extraneous allocations and copies)
	// PREMATURE optimize test (extraneous iterations - undup)
	if (!skipHighOrderBits) {
		UnconditionalFlowInfoTestHarness
			zero = UnconditionalFlowInfoTestHarness.
					testUnconditionalFlowInfo(State.start),
			left, right, left64, right64, left128, right128,
			special = (UnconditionalFlowInfoTestHarness) zero.copy();
			special.grow(64); // allocates one extra without adding null info
			transitionsSetsIterator = this.initializedTransitions.entrySet().iterator();
			while (transitionsSetsIterator.hasNext()) {
				transitionsSet = (Map.Entry) transitionsSetsIterator.next();
				input1 = (State) transitionsSet.getKey();
				transitionsIterator = ((Map) transitionsSet.getValue()).
					entrySet().iterator();
				while (transitionsIterator.hasNext()) {
					transition = (Map.Entry) transitionsIterator.next();
					input2 = (State) transition.getKey();
					expectedOutput = (State) transition.getValue();
					left = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input1);
					left64 = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input1, 64);
					left128 = UnconditionalFlowInfoTestHarness.
						testUnconditionalFlowInfo(input1, 128);
					right = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input2);
					right64 = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input2, 64);
					right128 = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input2, 128);
				if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
						output(left64, right64)).asState(64)) !=
							expectedOutput) {
					fail();
					System.out.println("\t\t" + input1.printableBitsField +
						" + " + input2.printableBitsField +
						" => " + effectiveOutput.printableBitsField +
						" (64, 64) - instead of: " + expectedOutput.printableBitsField);
				}
				if (input1 == State.start) {
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right128, right)).asState()) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero 128, 1) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(zero, right64)).asState(64)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero, 64) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right128, right64)).asState(64)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero 128, 64) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(zero, right128)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero, 128) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right64, right128)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero 64, 128) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(special, right128)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (special zero, 128) - instead of: " + expectedOutput.printableBitsField);
					}
				}
				if (input2 == State.start) {
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(left, left128)).asState()) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (1, zero 128) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(left64, zero)).asState(64)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (64, zero) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(left64, left128)).asState(64)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (64, zero 128) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(left128, zero)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (128, zero) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(left128, left64)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (128, zero 64) - instead of: " + expectedOutput.printableBitsField);
					}
				}
			}
		}
	}
	return this.failuresNb;
}
}

public abstract static class SymmetricalThreeDimensionalTransformation
		extends ThreeDimensionalTransformation{
SymmetricalThreeDimensionalTransformation(String name, byte[][] transitions) {
	super(name);
	int length;
	this.initializedTransitions = new HashMap((length = transitions.length) * 2 - 1);
	State input1, input2;
	for (int i = 0; i < length; i++) {
		if (transitions[i].length != 3) {
			throw new IllegalArgumentException("transitions should have three entries");
		}
		if (transitions[i][0] <= transitions[i][1]) {
			input1 = State.states[transitions[i][0]]; // array out of bounds exception if broken
			input2 = State.states[transitions[i][1]];
		}
		else {
			input1 = State.states[transitions[i][1]];
			input2 = State.states[transitions[i][0]];
		}
		Map transitionsForInput1 = (Map) this.initializedTransitions.get(input1);
		if (transitionsForInput1 == null) {
			transitionsForInput1 = new HashMap(length);
			this.initializedTransitions.put(input1, transitionsForInput1);
		}
		if (transitionsForInput1.get(input2) != null) {
			throw new IllegalArgumentException("duplicate entry");
		}
		transitionsForInput1.put(input2, State.states[transitions[i][2]]);
	}
}
@Override
void hydrate() {
	super.hydrate();
	checkSymmetry(this.computedTransitions);
}
private void checkSymmetry(Map map) {
	State input1, input2, result;
	Map.Entry entry1, entry2;
	Map transition;
	Iterator transitions1 = map.entrySet().iterator(), transitions2;
	while (transitions1.hasNext()) {
		entry1 = (Map.Entry) transitions1.next();
		input1 = (State) entry1.getKey();
		transition = (Map) entry1.getValue();
		transitions2 = transition.entrySet().iterator();
		while (transitions2.hasNext()) {
			entry2 = (Map.Entry) transitions2.next();
			input2 = (State) entry2.getKey();
			result = (State) entry2.getValue();
			if (result != ((Map) map.get(input2)).get(input1) && input1.symbolic && input2.symbolic) {
				System.err.println("symmetry mismatch: " + input1 + " + " +
						input2 + " -> " + result + "/" + ((Map) map.get(input2)).get(input1));
			}
		}
	}
}
@Override
void mark(char truthValues[][], boolean keepRow[], boolean keepColumn[],
		int row, int column, char value) {
	truthValues[row][column] = truthValues[column][row] = value;
	keepRow[row] = true;
	keepColumn[column] = true;
	keepRow[column] = true;
	keepColumn[row] = true;
}
@Override
void printDefinitions(BufferedWriter output, State[] consideredStates, String tab)
		throws IOException {
	// only difference with parent is that we print only half of possible
	// combinations
	int i, j, length = consideredStates.length;
	State result;
	// temporary store to support lexical sorting:
	String[] lines = new String[length * (length +1) / 2 ];
	int lCount = 0;
	for (i = 0; i < length; i++) {
		for (j = i; j < length; j++) {
			StringBuilder line = new StringBuilder();
			line.append(tab);
			line.append("// ");
			line.append(consideredStates[i].name);
			line.append(" + ");
			line.append(consideredStates[j].name);
			line.append(" => ");
			line.append(
				(result = (State) getResult(this.computedTransitions, consideredStates[i], consideredStates[j])).name);
			if (!result.symbolic ||
				result != getResult(this.initializedTransitions, consideredStates[i], consideredStates[j])) {
				line.append("\t\t CHECK");
			}
			line.append('\n');
			lines[lCount++] = line.toString();
		}
	}
	Arrays.sort(lines);
	for (i = 0; i < lCount; i++) {
		output.write(lines[i]);
	}
}
@Override
Object getResult(Map transitions, State statei, State statej) {
	Object r = super.getResult(transitions, statei, statej);
	if (r == null)
		r = super.getResult(transitions, statej, statei);
	return r;
}
@Override
void printInitializers(BufferedWriter output, State[] consideredStates, String tab)
		throws IOException {
	// only difference with parent is that we print only half of possible
	// combinations
	int i, j, length;
	for (i = 0, length = consideredStates.length; i < length; i++) {
		for (j = i; j < length; j++) {
			output.write(tab);
			output.write('{');
			output.write(consideredStates[i].hexString);
			output.write(',');
			output.write(consideredStates[j].hexString);
			output.write(',');
			output.write(
				((State)
					((Map) this.computedTransitions.get(consideredStates[i])).get(consideredStates[j])).hexString);
			output.write("},");
			output.write('\n');
		}
	}
}
@Override
void printMissingEntries(BufferedWriter output, String tab) throws IOException {
	Iterator firsts = State.symbolicStates(), seconds;
	State first, second;
	while (firsts.hasNext()) {
		first = (State) firsts.next();
		seconds = State.symbolicStates();
		while (seconds.hasNext()) {
			second = (State) seconds.next();
			if (!checkPair(first, second)) {
				addPair(first, second);
				System.err.println("Adding missing transition for states (" + first + ", " + second + ")");
				output.write(tab);
				output.write("// ");
				output.write(first.toString());
				output.write(" + ");
				output.write(second.toString());
				output.write(" => start\t\t CHECK\n");
			}
		}
	}
}
private boolean checkPair(State s1, State s2) {
	Map transitions;
	if ((transitions = (Map) this.initializedTransitions.get(s1)) != null) {
		if (transitions.get(s2) != null) {
			return true;
		}
	}
	if ((transitions = (Map) this.initializedTransitions.get(s2)) != null) {
		if (transitions.get(s1) != null) {
			return true;
		}
	}
	return false;
}
private void addPair(State s1, State s2) {
	Map transitions;
	if ((transitions = (Map) this.initializedTransitions.get(s1)) == null) {
		transitions = new HashMap();
		this.initializedTransitions.put(s1, transitions);
	}
	transitions.put(s2, s2); // dummy, non null value
}
// PREMATURE factorize upward
@Override
int test(int combinationTestsLoopsNb, boolean skipHighOrderBits) {
	Iterator transitionsSetsIterator, transitionsIterator;
	State input1, input2, expectedOutput, effectiveOutput;
	Map.Entry transition, transitionsSet;
	this.failuresNb = 0; // reset
	this.failuresNb = 0; // reset
	long start = 0;
	if (combinationTestsLoopsNb > 1) {
		start = System.currentTimeMillis();
	}
	for (int l = 0; l < combinationTestsLoopsNb ; l++) {
		transitionsSetsIterator = this.initializedTransitions.entrySet().iterator();
		while (transitionsSetsIterator.hasNext()) {
			transitionsSet = (Map.Entry) transitionsSetsIterator.next();
			input1 = (State) transitionsSet.getKey();
			transitionsIterator = ((Map) transitionsSet.getValue()).
				entrySet().iterator();
			while (transitionsIterator.hasNext()) {
				transition = (Map.Entry) transitionsIterator.next();
				input2 = (State) transition.getKey();
				expectedOutput = (State) transition.getValue();
				effectiveOutput = ((UnconditionalFlowInfoTestHarness) output(
						UnconditionalFlowInfoTestHarness.testUnconditionalFlowInfo(input1),
						UnconditionalFlowInfoTestHarness.testUnconditionalFlowInfo(input2)))
					.asState();
				if (effectiveOutput != expectedOutput) {
					fail();
					System.out.println("\t\t" + input1.printableBitsField +
						" + " + input2.printableBitsField +
						" => " + effectiveOutput.printableBitsField +
						" instead of: " + expectedOutput.printableBitsField);
				}
				effectiveOutput = ((UnconditionalFlowInfoTestHarness) output(
						UnconditionalFlowInfoTestHarness.testUnconditionalFlowInfo(input2),
						UnconditionalFlowInfoTestHarness.testUnconditionalFlowInfo(input1)))
					.asState();
				if (effectiveOutput != expectedOutput) {
					fail();
					System.out.println("\t\t" + input2.printableBitsField +
						" + " + input1.printableBitsField +
						" => " + effectiveOutput.printableBitsField +
						" instead of: " + expectedOutput.printableBitsField);
				}
			}
		}
	}
	if (combinationTestsLoopsNb > 1) {
		System.out.println(this.name + "...\t\t" + combinationTestsLoopsNb + "\t" +
				(System.currentTimeMillis() - start));
	}
	// PREMATURE optimize test (extraneous allocations and copies)
	// PREMATURE optimize test (extraneous iterations - undup)
	if (!skipHighOrderBits) {
		UnconditionalFlowInfoTestHarness
			zero = UnconditionalFlowInfoTestHarness.
					testUnconditionalFlowInfo(State.start),
			right, left64, right64, right128;
			transitionsSetsIterator = this.initializedTransitions.entrySet().iterator();
			while (transitionsSetsIterator.hasNext()) {
				transitionsSet = (Map.Entry) transitionsSetsIterator.next();
				input1 = (State) transitionsSet.getKey();
				transitionsIterator = ((Map) transitionsSet.getValue()).
					entrySet().iterator();
				while (transitionsIterator.hasNext()) {
					transition = (Map.Entry) transitionsIterator.next();
					input2 = (State) transition.getKey();
					expectedOutput = (State) transition.getValue();
					left64 = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input1, 64);
					right = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input2);
					right64 = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input2, 64);
					right128 = UnconditionalFlowInfoTestHarness.
							testUnconditionalFlowInfo(input2, 128);
				if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
						output(left64, right64)).asState(64)) !=
							expectedOutput) {
					fail();
					System.out.println("\t\t" + input1.printableBitsField +
						" + " + input2.printableBitsField +
						" => " + effectiveOutput.printableBitsField +
						" (64, 64) - instead of: " + expectedOutput.printableBitsField);
				}
				if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
						output(right64, left64)).asState(64)) !=
							expectedOutput) {
					fail();
					System.out.println("\t\t" + input2.printableBitsField +
						" + " + input1.printableBitsField +
						" => " + effectiveOutput.printableBitsField +
						" (64, 64) - instead of: " + expectedOutput.printableBitsField);
				}
				if (input1 == State.start) {
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right128, right)).asState()) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero 128, 1) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(zero, right64)).asState(64)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero, 64) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right128, right64)).asState(64)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero 128, 64) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(zero, right128)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero, 128) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right64, right128)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input1.printableBitsField +
							" + " + input2.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (zero 64, 128) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right, right128)).asState()) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input2.printableBitsField +
							" + " + input1.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (1, zero 128) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right64, zero)).asState(64)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input2.printableBitsField +
							" + " + input1.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (64, zero) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right64, right128)).asState(64)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input2.printableBitsField +
							" + " + input1.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (64, zero 128) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right128, zero)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input2.printableBitsField +
							" + " + input1.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (128, zero) - instead of: " + expectedOutput.printableBitsField);
					}
					if ((effectiveOutput = ((UnconditionalFlowInfoTestHarness)
							output(right128, right64)).asState(128)) != expectedOutput) {
						fail();
						System.out.println("\t\t" + input2.printableBitsField +
							" + " + input1.printableBitsField +
							" => " + effectiveOutput.printableBitsField +
							" (128, zero 64) - instead of: " + expectedOutput.printableBitsField);
					}
				}
			}
		}
	}
	return this.failuresNb;
}
}
}
