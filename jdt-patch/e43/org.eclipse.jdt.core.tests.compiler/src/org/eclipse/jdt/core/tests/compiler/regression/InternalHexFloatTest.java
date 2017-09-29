/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.util.FloatUtil;

public class InternalHexFloatTest extends AbstractRegressionTest {
	static class DoubleTest {
		String input;
		long output;
		public DoubleTest(String input, long output) {
			this.input = input;
			this.output = output;
		}
	}

	static class FloatTest {
		String input;
		int output;
		public FloatTest(String input, int output) {
			this.input = input;
			this.output = output;
		}
	}

	public InternalHexFloatTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}

	public static Class testClass() {
		return InternalHexFloatTest.class;
	}

	/**
	 */
	public void test001() {
		List x = new ArrayList();

		// various forms of zero
		x.add(new DoubleTest("0x0p0", 0x0L));
		x.add(new DoubleTest("0x0p0d", 0x0L));
		x.add(new DoubleTest("0x0p0D", 0x0L));
		x.add(new DoubleTest("0x0.0p0D", 0x0L));
		x.add(new DoubleTest("0x.0p0D", 0x0L));
		x.add(new DoubleTest("0x0.p0D", 0x0L));
		x.add(new DoubleTest("0x00000.00000000000p0D", 0x0L));
		x.add(new DoubleTest("0x0p99D", 0x0L));
		x.add(new DoubleTest("0x0p-99D", 0x0L));
		x.add(new DoubleTest("0x0p9999999D", 0x0L));
		x.add(new DoubleTest("0x0p-9999999D", 0x0L));

		// small doubles
		x.add(new DoubleTest("0x0.8p0D",       0x3fe0000000000000L));
		x.add(new DoubleTest("0x0.4p0D",       0x3fd0000000000000L));
		x.add(new DoubleTest("0x0.2p0D",       0x3fc0000000000000L));
		x.add(new DoubleTest("0x0.1p0D",       0x3fb0000000000000L));
		x.add(new DoubleTest("0x0.08p0D",      0x3fa0000000000000L));
		x.add(new DoubleTest("0x0.04p0D",      0x3f90000000000000L));
		x.add(new DoubleTest("0x0.02p0D",      0x3f80000000000000L));
		x.add(new DoubleTest("0x0.01p0D",      0x3f70000000000000L));
		x.add(new DoubleTest("0x0.010p0D",     0x3f70000000000000L));
		x.add(new DoubleTest("0x1p0D",         0x3ff0000000000000L));
		x.add(new DoubleTest("0x2p0D",         0x4000000000000000L));
		x.add(new DoubleTest("0x4p0D",         0x4010000000000000L));
		x.add(new DoubleTest("0x8p0D",         0x4020000000000000L));
		x.add(new DoubleTest("0x10p0D",        0x4030000000000000L));
		x.add(new DoubleTest("0x20p0D",        0x4040000000000000L));
		x.add(new DoubleTest("0x40p0D",        0x4050000000000000L));
		x.add(new DoubleTest("0x80p0D",        0x4060000000000000L));
		x.add(new DoubleTest("0x80.p0D",       0x4060000000000000L));
		x.add(new DoubleTest("0x80.8p0D",      0x4060100000000000L));
		x.add(new DoubleTest("0x80.80p0D",     0x4060100000000000L));
		x.add(new DoubleTest("0x123456789p0D", 0x41f2345678900000L));
		x.add(new DoubleTest("0xabcedfp0D",    0x416579dbe0000000L));
		x.add(new DoubleTest("0xABCDEFp0D",    0x416579bde0000000L));

		x.add(new DoubleTest("0x0.0100000000000000000000000000000000000000000000000p0d", 0x3f70000000000000L));
		x.add(new DoubleTest("0x0.0000000000000000000000000000000000000000000000001p0d", 0x33b0000000000000L));
		x.add(new DoubleTest("0x10000000000000000000000000000000000000000000000000000p0d", 0x4cf0000000000000L));

		// rounding to 53 bits
		x.add(new DoubleTest("0x823456789012380p0d", 0x43a0468acf120247L));
		x.add(new DoubleTest("0xFFFFFFFFFFFFF80p0d", 0x43afffffffffffffL));
		x.add(new DoubleTest("0xFFFFFFFFFFFFFC0p0d", 0x43b0000000000000L));
		x.add(new DoubleTest("0xFFFFFFFFFFFFFA0p0d", 0x43afffffffffffffL));
		x.add(new DoubleTest("0xFFFFFFFFFFFFF81p0d", 0x43afffffffffffffL));
		x.add(new DoubleTest("0x123456789abcd10p0d", 0x43723456789abcd1L));
		x.add(new DoubleTest("0x123456789abcd18p0d", 0x43723456789abcd2L));
		x.add(new DoubleTest("0x7FFFFFFFFFFFFC0p0d", 0x439fffffffffffffL));
		x.add(new DoubleTest("0x7FFFFFFFFFFFFE0p0d", 0x43a0000000000000L));
		x.add(new DoubleTest("0x3FFFFFFFFFFFFE0p0d", 0x438fffffffffffffL));
		x.add(new DoubleTest("0x3FFFFFFFFFFFFF0p0d", 0x4390000000000000L));
		x.add(new DoubleTest("0x1FFFFFFFFFFFFF0p0d", 0x437fffffffffffffL));
		x.add(new DoubleTest("0x1FFFFFFFFFFFFF8p0d", 0x4380000000000000L));

		// rounding to overflow at +1024; denormalized at -1022; underflow at -1075
		x.add(new DoubleTest("0x1p5000D",    0x7ff0000000000000L));
		x.add(new DoubleTest("0x1p-5000D",   0x7ff8000000000000L));
		x.add(new DoubleTest("0x1.0p1022d",  0x7fd0000000000000L));
		x.add(new DoubleTest("0x1.0p1023d",  0x7fe0000000000000L));
		x.add(new DoubleTest("0x1.0p1024d",  0x7ff0000000000000L));
		x.add(new DoubleTest("0x1.0p-1022d", 0x0010000000000000L));
		x.add(new DoubleTest("0x1.0p-1023d", 0x0008000000000000L));
		x.add(new DoubleTest("0x1.0p-1024d", 0x0004000000000000L));
		x.add(new DoubleTest("0x1.0p-1074d", 0x0000000000000001L));
		x.add(new DoubleTest("0x1.0p-1075d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x1.0p-1076d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x1.0p-1077d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x1.0p-1078d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.8p1023d",  0x7fd0000000000000L));
		x.add(new DoubleTest("0x0.8p1024d",  0x7fe0000000000000L));
		x.add(new DoubleTest("0x0.8p1025d",  0x7ff0000000000000L));
		x.add(new DoubleTest("0x0.8p-1021d", 0x0010000000000000L));
		x.add(new DoubleTest("0x0.8p-1022d", 0x0008000000000000L));
		x.add(new DoubleTest("0x0.8p-1023d", 0x0004000000000000L));
		x.add(new DoubleTest("0x0.8p-1024d", 0x0002000000000000L));
		x.add(new DoubleTest("0x0.8p-1074d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.8p-1075d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.8p-1076d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.4p-1021d", 0x0008000000000000L));
		x.add(new DoubleTest("0x0.4p-1022d", 0x0004000000000000L));
		x.add(new DoubleTest("0x0.4p-1023d", 0x0002000000000000L));
		x.add(new DoubleTest("0x0.4p-1073d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.4p-1074d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.4p-1075d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.2p-1020d", 0x0008000000000000L));
		x.add(new DoubleTest("0x0.2p-1021d", 0x0004000000000000L));
		x.add(new DoubleTest("0x0.2p-1022d", 0x0002000000000000L));
		x.add(new DoubleTest("0x0.2p-1072d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.2p-1073d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.2p-1074d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.1p-1019d", 0x0008000000000000L));
		x.add(new DoubleTest("0x0.1p-1020d", 0x0004000000000000L));
		x.add(new DoubleTest("0x0.1p-1021d", 0x0002000000000000L));
		x.add(new DoubleTest("0x0.1p-1071d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.1p-1072d", 0x7ff8000000000000L));
		x.add(new DoubleTest("0x0.1p-1073d", 0x7ff8000000000000L));

		for (Iterator it = x.iterator(); it.hasNext();) {
			DoubleTest t = (DoubleTest) it.next();
			String s = t.input;
			long expectedBits = t.output;
			double libExpected = 0.0d;
			boolean isJ2SE5;
			try {
				// note that next line only works with a 1.5 J2SE
				libExpected = Double.parseDouble(s);
				isJ2SE5 = true;
			} catch(NumberFormatException e) {
				isJ2SE5 = false;
			}
			double dActual = FloatUtil.valueOfHexDoubleLiteral(s.toCharArray());
			long actualBits = Double.doubleToLongBits(dActual);
			if (isJ2SE5) {
				// cross-check bits computed by J2SE 1.5 library
				long libExpectedBits = Double.doubleToRawLongBits(libExpected);
				if (expectedBits != libExpectedBits) {
					if (Double.isNaN(Double.longBitsToDouble(expectedBits)) && libExpected == 0.0d) {
						// this is ok - we return NaN where lib quietly underflows to 0
					} else {
						assertEquals("Test has wrong table value for " + s, libExpectedBits, expectedBits);
					}
				}
			}
			assertEquals("Wrong double value for " + s, expectedBits, actualBits);
		}
	}

	/**
	 */
	public void test002() {
		List x = new ArrayList();
		// various forms of zero
		x.add(new FloatTest("0x0p0f", 0x0));
		x.add(new FloatTest("0x0p0F", 0x0));
		x.add(new FloatTest("0x0.0p0F", 0x0));
		x.add(new FloatTest("0x.0p0F", 0x0));
		x.add(new FloatTest("0x0.p0F", 0x0));
		x.add(new FloatTest("0x00000.00000000000p0F", 0x0));
		x.add(new FloatTest("0x0p99F", 0x0));
		x.add(new FloatTest("0x0p-99F", 0x0));
		x.add(new FloatTest("0x0p9999999F", 0x0));
		x.add(new FloatTest("0x0p-9999999F", 0x0));

		// small floats
		x.add(new FloatTest("0x0.8p0F", 0x3f000000));
		x.add(new FloatTest("0x0.4p0F", 0x3e800000));
		x.add(new FloatTest("0x0.2p0F", 0x3e000000));
		x.add(new FloatTest("0x0.1p0F", 0x3d800000));
		x.add(new FloatTest("0x0.08p0F", 0x3d000000));
		x.add(new FloatTest("0x0.04p0F", 0x3c800000));
		x.add(new FloatTest("0x0.02p0F", 0x3c000000));
		x.add(new FloatTest("0x0.01p0F", 0x3b800000));
		x.add(new FloatTest("0x0.010p0F", 0x3b800000));
		x.add(new FloatTest("0x1p0F", 0x3f800000));
		x.add(new FloatTest("0x2p0F", 0x40000000));
		x.add(new FloatTest("0x4p0F", 0x40800000));
		x.add(new FloatTest("0x8p0F", 0x41000000));
		x.add(new FloatTest("0x10p0F", 0x41800000));
		x.add(new FloatTest("0x20p0F", 0x42000000));
		x.add(new FloatTest("0x40p0F", 0x42800000));
		x.add(new FloatTest("0x80p0F", 0x43000000));
		x.add(new FloatTest("0x80.p0F", 0x43000000));
		x.add(new FloatTest("0x80.8p0F", 0x43008000));
		x.add(new FloatTest("0x80.80p0F", 0x43008000));
		x.add(new FloatTest("0x123456789p0F", 0x4f91a2b4));
		x.add(new FloatTest("0xabcedfp0F", 0x4b2bcedf));
		x.add(new FloatTest("0xABCDEFp0F", 0x4b2bcdef));

		x.add(new FloatTest("0x0.000000000000000000000000000001p0f", 0x3800000));
		x.add(new FloatTest("0x10000000000000000000000000000000p0f", 0x7d800000));

		// rounding to 24 bits
		x.add(new FloatTest("0x823456p0f",   0x4b023456));
		x.add(new FloatTest("0xFFFFFF80p0f", 0x4f800000));
		x.add(new FloatTest("0xFFFFFF40p0f", 0x4f7fffff));
		x.add(new FloatTest("0xFFFFFF20p0f", 0x4f7fffff));
		x.add(new FloatTest("0x123456p0f",   0x4991a2b0));
		x.add(new FloatTest("0x7890abp0f",   0x4af12156));
		x.add(new FloatTest("0xcdefABp0f",   0x4b4defab));
		x.add(new FloatTest("0xCDEFdep0f",   0x4b4defde));
		x.add(new FloatTest("0x123456p0f",   0x4991a2b0));
		x.add(new FloatTest("0x7FFFFF8p0f",  0x4cffffff));
		x.add(new FloatTest("0x3FFFFFCp0f",  0x4c7fffff));
		x.add(new FloatTest("0x1FFFFFEp0f",  0x4bffffff));

		// rounding to overflow at +128; denormalized at -126; underflow at -150
		x.add(new FloatTest("0x1p5000F",     0x7f800000));
		x.add(new FloatTest("0x1p-5000F",    0x7fc00000));
		x.add(new FloatTest("0x1.0p126f",    0x7e800000));
		x.add(new FloatTest("0x1.0p127f",    0x7f000000));
		x.add(new FloatTest("0x1.0p128f",    0x7f800000));
		x.add(new FloatTest("0x1.0p129f",    0x7f800000));
		x.add(new FloatTest("0x1.0p-127f",   0x00400000));
		x.add(new FloatTest("0x1.0p-128f",   0x00200000));
		x.add(new FloatTest("0x1.0p-129f",   0x00100000));
		x.add(new FloatTest("0x1.0p-149f",   0x00000001));
		x.add(new FloatTest("0x1.0p-150f",   0x7fc00000));
		x.add(new FloatTest("0x1.0p-151f",   0x7fc00000));
		x.add(new FloatTest("0x0.8p127f",    0x7e800000));
		x.add(new FloatTest("0x0.8p128f",    0x7f000000));
		x.add(new FloatTest("0x0.8p129f",    0x7f800000));
		x.add(new FloatTest("0x0.8p-125f",   0x00800000));
		x.add(new FloatTest("0x0.8p-126f",   0x00400000));
		x.add(new FloatTest("0x0.8p-127f",   0x00200000));
		x.add(new FloatTest("0x0.8p-128f",   0x00100000));
		x.add(new FloatTest("0x0.8p-148f",   0x00000001));
		x.add(new FloatTest("0x0.8p-149f",   0x7fc00000));
		x.add(new FloatTest("0x0.8p-150f",   0x7fc00000));
		x.add(new FloatTest("0x0.4p-124f",   0x00800000));
		x.add(new FloatTest("0x0.4p-125f",   0x00400000));
		x.add(new FloatTest("0x0.4p-126f",   0x00200000));
		x.add(new FloatTest("0x0.4p-147f",   0x00000001));
		x.add(new FloatTest("0x0.4p-148f",   0x7fc00000));
		x.add(new FloatTest("0x0.4p-149f",   0x7fc00000));
		x.add(new FloatTest("0x0.4p-150f",   0x7fc00000));
		x.add(new FloatTest("0x0.2p-123f",   0x00800000));
		x.add(new FloatTest("0x0.2p-124f",   0x00400000));
		x.add(new FloatTest("0x0.2p-125f",   0x00200000));
		x.add(new FloatTest("0x0.2p-126f",   0x00100000));
		x.add(new FloatTest("0x0.2p-146f",   0x00000001));
		x.add(new FloatTest("0x0.2p-147f",   0x7fc00000));
		x.add(new FloatTest("0x0.2p-148f",   0x7fc00000));
		x.add(new FloatTest("0x0.2p-149f",   0x7fc00000));
		x.add(new FloatTest("0x0.1p-122f",   0x00800000));
		x.add(new FloatTest("0x0.1p-123f",   0x00400000));
		x.add(new FloatTest("0x0.1p-124f",   0x00200000));
		x.add(new FloatTest("0x0.1p-145f",   0x00000001));
		x.add(new FloatTest("0x0.1p-146f",   0x7fc00000));
		x.add(new FloatTest("0x0.1p-147f",   0x7fc00000));
		x.add(new FloatTest("0x0.1p-148f",   0x7fc00000));

		for (Iterator it = x.iterator(); it.hasNext();) {
			FloatTest t = (FloatTest) it.next();
			String s = t.input;
			int expectedBits = t.output;
			float libExpected = 0.0f;
			boolean isJ2SE5;
			try {
				// note that next line only works with a 1.5 J2SE
				libExpected = Float.parseFloat(s);
				isJ2SE5 = true;
			} catch(NumberFormatException e) {
				isJ2SE5 = false;
			}
			float dActual = FloatUtil.valueOfHexFloatLiteral(s.toCharArray());
			long actualBits = Float.floatToIntBits(dActual);
			if (isJ2SE5) {
				// cross-check bits computed by J2SE 1.5 library
				int libExpectedBits = Float.floatToRawIntBits(libExpected);
				if (expectedBits != libExpectedBits) {
					if (Float.isNaN(Float.intBitsToFloat(expectedBits)) && libExpected == 0.0f) {
						// this is ok - we return NaN where lib quietly underflows to 0
					} else {
						assertEquals("Test has wrong table value for " + s, libExpectedBits, expectedBits);
					}
				}
			}
			assertEquals("Wrong float value for " + s, expectedBits, actualBits);
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test003() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {	\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(-0Xf.aP1F);\n" +
				"    }\n" +
				"}"
			},
			"-31.25");
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {	\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(0X000.0000P5000);\n" +
				"    }\n" +
				"}"
			},
			"0.0");
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {	\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(-0X000.0000P5000F);\n" +
				"    }\n" +
				"}"
			},
			"-0.0");
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(0X000.eP-5000F);\n" +
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	System.out.println(0X000.eP-5000F);\r\n" +
			"	                   ^^^^^^^^^^^^^^\n" +
			"The literal 0X000.eP-5000F of type float is out of range \n" +
			"----------\n");
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74126
	 */
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(0X000.eP5000F);\n" +
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	System.out.println(0X000.eP5000F);\r\n" +
			"	                   ^^^^^^^^^^^^^\n" +
			"The literal 0X000.eP5000F of type float is out of range \n" +
			"----------\n");
	}
}
