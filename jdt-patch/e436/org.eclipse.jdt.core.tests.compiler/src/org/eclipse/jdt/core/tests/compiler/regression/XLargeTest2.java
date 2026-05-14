/*******************************************************************************
 * Copyright (c) 2018, 2020 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class XLargeTest2 extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "testBug550063" };
	}

	public XLargeTest2(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
	}

	public static Class<?> testClass() {
		return XLargeTest2.class;
	}

	/**
	 * Check if we hit the 64Kb limit on generated table switch method code in
	 * class files. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=542084
	 */
	public void testBug542084_error() {

		int enumsCount = getEnumsCountForError();
		StringBuilder lotOfEnums = new StringBuilder(enumsCount * 7);
		for (int i = 0; i < enumsCount; i++) {
			lotOfEnums.append("A").append(i).append(", ");
		}

		String expectedCompilerLog;
		if (this.complianceLevel > ClassFileConstants.JDK1_8) {
			expectedCompilerLog =
					"1. ERROR in X.java (at line 2)\n" +
					"	enum Y {\n" +
					"	     ^\n" +
					"The code for the static initializer is exceeding the 65535 bytes limit\n";
		} else {
			expectedCompilerLog =
					"1. ERROR in X.java (at line 6)\n" +
					"	switch(y){\n" +
					"        case A0:\n" +
					"            System.out.println(\"a\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.println(\"default\");\n" +
					"            break;\n" +
					"        }\n" +
					"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"The code for the switch table on enum X.Y is exceeding the 65535 bytes limit\n";
		}
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    enum Y {\n" +
						 lotOfEnums.toString() +
					"    }\n" +
					"    public static void main(String[] args) {\n" +
					"        X.Y y = X.Y.A0;\n" +
					"        switch(y){\n" + // Reported error should be here
					"        case A0:\n" +
					"            System.out.println(\"a\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.println(\"default\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"    public void z2(Y y) {\n" +  // Should not report error on second switch
					"        switch(y){\n" +
					"        case A0:\n" +
					"            System.out.println(\"a\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.println(\"default\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"}"
				},
				"----------\n" +
				expectedCompilerLog +
				"----------\n");
	}

	/**
	 * Check if we don't hit the 64Kb limit on generated table switch method code in
	 * class files. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=542084
	 */
	public void testBug542084_no_error() {
		int enumsCount = getEnumsCountForSuccess();
		StringBuilder lotOfEnums = new StringBuilder(enumsCount * 7);
		for (int i = 0; i < enumsCount; i++) {
			lotOfEnums.append("A").append(i).append(", ");
		}

		// Javac can't compile such big enums
		runConformTest(
				true,
				JavacTestOptions.SKIP,
				new String[] {
						"X.java",
						"public class X {\n" +
								"    enum Y {\n" +
								lotOfEnums.toString() +
								"    }\n" +
								"    public static void main(String[] args) {\n" +
								"        X.Y y = X.Y.A0;\n" +
								"        switch(y){\n" +
								"        case A0:\n" +
								"            System.out.println(\"SUCCESS\");\n" +
								"            break;\n" +
								"        default:\n" +
								"            System.out.println(\"default\");\n" +
								"            break;\n" +
								"        }\n" +
								"    }\n" +
								"}"
				},
				"SUCCESS");
	}

	/**
	 * @return Generated code for enums that exceeds the limit
	 */
	private int getEnumsCountForError() {
		if(this.complianceLevel > ClassFileConstants.JDK1_8) {
			return 2800;
		}
		return 4500;
	}

	/**
	 * @return Generated code for enums that does not exceeds the limit
	 */
	private int getEnumsCountForSuccess() {
		if(this.complianceLevel > ClassFileConstants.JDK1_8) {
			return 2300;
		}
		return 4300;
	}

	public void testBug550063() {
		runConformTest(
			new String[] {
				"p001/bip.java",
				"package p001;\n" +
				"\n" +
				getManyInterfaceDeclarations() +
				"\n" +
				"class bip implements brj, brk, cem, cen, cey, cez, cfk, cfl, cgu, cgx, che, chh, chq, chr, cji, cjj, ckk, ckl, clb, clc, clf, cli, cnk,\n" +
				"	cnl, cok, cqa, cqd, cqw, cqx, crs, crv, csu, csv, ctq, ctt, cvg, cvj, cvo, cvp, cwk, cwn, cwu, cww, cxh, cxk, daz, dba, dbr, dbu, dck,\n" +
				"	dcl, deh, dei, dep, deq, dff, dfg, dfl, dfo, dsp, dss, dtp, dtq, dtt, dtw, duj, duk, dvm, dvp, dvs, dvv, dwe, dwh, dxd, dxg, dyq, dys,\n" +
				"	dyv, dyw, dzh, dzk, dzn, dzo, dzx, eaa, ecw, ecx, edr, eds, efc, efd, eiw, eiz, ejy, ekb, emi, eml, eor, eou, epe, eph, epk, epl, eqi,\n" +
				"	eqj, erv, erw, etd, etg, etm, eto, fbc, fbd, feu, fev, ffc, fff, fgf, fgh, fgo, fgp, fhm, fhn, fib, fki, fkj, fkw, fkx, fmh, fmk, fnk,\n" +
				"	fnl, fnz, foc, fof, foi, fvk, fvn, fvv, fvw, fwy, fxb, fyb, fye, fyl, fym, fyv, fyy, fzq, fzs, gad, gag, gaq, gas, gav, gax, gbc, gbd,\n" +
				"	gco, gcr, gdc, gdf, gdn, gdq, gei, gej, gih, gik, gku, gkx, gln, glo, gmi, gmj, gmu, gmv, gpx, gpy, gqb, gqe, gqp, gqs, grb, grc, grh,\n" +
				"	gri, grn, gro, grv, grw, gtr, gtu, gxc, gvt, gvw, gwz {\n" +
				"}\n"
			});
	}

	public void testBug550063_b() {
		runNegativeTest(
			new String[] {
				"p001/bip.java",
				"package p001;\n" +
				"\n" +
				getManyInterfaceDeclarations() +
				"\n" +
				"class bop implements missing,\n" +
				"	brj, brk, cem, cen, cey, cez, cfk, cfl, cgu, cgx, che, chh, chq, chr, cji, cjj, ckk, ckl, clb, clc, clf, cli, cnk,\n" +
				"	cnl, cok, cqa, cqd, cqw, cqx, crs, crv, csu, csv, ctq, ctt, cvg, cvj, cvo, cvp, cwk, cwn, cwu, cww, cxh, cxk, daz, dba, dbr, dbu, dck,\n" +
				"	dcl, deh, dei, dep, deq, dff, dfg, dfl, dfo, dsp, dss, dtp, dtq, dtt, dtw, duj, duk, dvm, dvp, dvs, dvv, dwe, dwh, dxd, dxg, dyq, dys,\n" +
				"	dyv, dyw, dzh, dzk, dzn, dzo, dzx, eaa, ecw, ecx, edr, eds, efc, efd, eiw, eiz, ejy, ekb, emi, eml, eor, eou, epe, eph, epk, epl, eqi,\n" +
				"	eqj, erv, erw, etd, etg, etm, eto, fbc, fbd, feu, fev, ffc, fff, fgf, fgh, fgo, fgp, fhm, fhn, fib, fki, fkj, fkw, fkx, fmh, fmk, fnk,\n" +
				"	fnl, fnz, foc, fof, foi, fvk, fvn, fvv, fvw, fwy, fxb, fyb, fye, fyl, fym, fyv, fyy, fzq, fzs, gad, gag, gaq, gas, gav, gax, gbc, gbd,\n" +
				"	gco, gcr, gdc, gdf, gdn, gdq, gei, gej, gih, gik, gku, gkx, gln, glo, gmi, gmj, gmu, gmv, gpx, gpy, gqb, gqe, gqp, gqs, grb, grc, grh,\n" +
				"	gri, grn, gro, grv, grw, gtr, gtu, gxc, gvt, gvw, gwz {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in p001\\bip.java (at line 200)\n" +
			"	class bop implements missing,\n" +
			"	                     ^^^^^^^\n" +
			"missing cannot be resolved to a type\n" +
			"----------\n");
	}

	private String getManyInterfaceDeclarations() {
		return 	"interface brj {}\n" +
				"interface brk {}\n" +
				"interface cem {}\n" +
				"interface cen {}\n" +
				"interface cey {}\n" +
				"interface cez {}\n" +
				"interface cfk {}\n" +
				"interface cfl {}\n" +
				"interface cgu {}\n" +
				"interface cgx {}\n" +
				"interface che {}\n" +
				"interface chh {}\n" +
				"interface chq {}\n" +
				"interface chr {}\n" +
				"interface cji {}\n" +
				"interface cjj {}\n" +
				"interface ckk {}\n" +
				"interface ckl {}\n" +
				"interface clb {}\n" +
				"interface clc {}\n" +
				"interface clf {}\n" +
				"interface cli {}\n" +
				"interface cnk {}\n" +
				"interface cnl {}\n" +
				"interface cok {}\n" +
				"interface cqa {}\n" +
				"interface cqd {}\n" +
				"interface cqw {}\n" +
				"interface cqx {}\n" +
				"interface crs {}\n" +
				"interface crv {}\n" +
				"interface csu {}\n" +
				"interface csv {}\n" +
				"interface ctq {}\n" +
				"interface ctt {}\n" +
				"interface cvg {}\n" +
				"interface cvj {}\n" +
				"interface cvo {}\n" +
				"interface cvp {}\n" +
				"interface cwk {}\n" +
				"interface cwn {}\n" +
				"interface cwu {}\n" +
				"interface cww {}\n" +
				"interface cxh {}\n" +
				"interface cxk {}\n" +
				"interface daz {}\n" +
				"interface dba {}\n" +
				"interface dbr {}\n" +
				"interface dbu {}\n" +
				"interface dck {}\n" +
				"interface dcl {}\n" +
				"interface deh {}\n" +
				"interface dei {}\n" +
				"interface dep {}\n" +
				"interface deq {}\n" +
				"interface dff {}\n" +
				"interface dfg {}\n" +
				"interface dfl {}\n" +
				"interface dfo {}\n" +
				"interface dsp {}\n" +
				"interface dss {}\n" +
				"interface dtp {}\n" +
				"interface dtq {}\n" +
				"interface dtt {}\n" +
				"interface dtw {}\n" +
				"interface duj {}\n" +
				"interface duk {}\n" +
				"interface dvm {}\n" +
				"interface dvp {}\n" +
				"interface dvs {}\n" +
				"interface dvv {}\n" +
				"interface dwe {}\n" +
				"interface dwh {}\n" +
				"interface dxd {}\n" +
				"interface dxg {}\n" +
				"interface dyq {}\n" +
				"interface dys {}\n" +
				"interface dyv {}\n" +
				"interface dyw {}\n" +
				"interface dzh {}\n" +
				"interface dzk {}\n" +
				"interface dzn {}\n" +
				"interface dzo {}\n" +
				"interface dzx {}\n" +
				"interface eaa {}\n" +
				"interface ecw {}\n" +
				"interface ecx {}\n" +
				"interface edr {}\n" +
				"interface eds {}\n" +
				"interface efc {}\n" +
				"interface efd {}\n" +
				"interface eiw {}\n" +
				"interface eiz {}\n" +
				"interface ejy {}\n" +
				"interface ekb {}\n" +
				"interface emi {}\n" +
				"interface eml {}\n" +
				"interface eor {}\n" +
				"interface eou {}\n" +
				"interface epe {}\n" +
				"interface eph {}\n" +
				"interface epk {}\n" +
				"interface epl {}\n" +
				"interface eqi {}\n" +
				"interface eqj {}\n" +
				"interface erv {}\n" +
				"interface erw {}\n" +
				"interface etd {}\n" +
				"interface etg {}\n" +
				"interface etm {}\n" +
				"interface eto {}\n" +
				"interface fbc {}\n" +
				"interface fbd {}\n" +
				"interface feu {}\n" +
				"interface fev {}\n" +
				"interface ffc {}\n" +
				"interface fff {}\n" +
				"interface fgf {}\n" +
				"interface fgh {}\n" +
				"interface fgo {}\n" +
				"interface fgp {}\n" +
				"interface fhm {}\n" +
				"interface fhn {}\n" +
				"interface fib {}\n" +
				"interface fki {}\n" +
				"interface fkj {}\n" +
				"interface fkw {}\n" +
				"interface fkx {}\n" +
				"interface fmh {}\n" +
				"interface fmk {}\n" +
				"interface fnk {}\n" +
				"interface fnl {}\n" +
				"interface fnz {}\n" +
				"interface foc {}\n" +
				"interface fof {}\n" +
				"interface foi {}\n" +
				"interface fvk {}\n" +
				"interface fvn {}\n" +
				"interface fvv {}\n" +
				"interface fvw {}\n" +
				"interface fwy {}\n" +
				"interface fxb {}\n" +
				"interface fyb {}\n" +
				"interface fye {}\n" +
				"interface fyl {}\n" +
				"interface fym {}\n" +
				"interface fyv {}\n" +
				"interface fyy {}\n" +
				"interface fzq {}\n" +
				"interface fzs {}\n" +
				"interface gad {}\n" +
				"interface gag {}\n" +
				"interface gaq {}\n" +
				"interface gas {}\n" +
				"interface gav {}\n" +
				"interface gax {}\n" +
				"interface gbc {}\n" +
				"interface gbd {}\n" +
				"interface gco {}\n" +
				"interface gcr {}\n" +
				"interface gdc {}\n" +
				"interface gdf {}\n" +
				"interface gdn {}\n" +
				"interface gdq {}\n" +
				"interface gei {}\n" +
				"interface gej {}\n" +
				"interface gih {}\n" +
				"interface gik {}\n" +
				"interface gku {}\n" +
				"interface gkx {}\n" +
				"interface gln {}\n" +
				"interface glo {}\n" +
				"interface gmi {}\n" +
				"interface gmj {}\n" +
				"interface gmu {}\n" +
				"interface gmv {}\n" +
				"interface gpx {}\n" +
				"interface gpy {}\n" +
				"interface gqb {}\n" +
				"interface gqe {}\n" +
				"interface gqp {}\n" +
				"interface gqs {}\n" +
				"interface grb {}\n" +
				"interface grc {}\n" +
				"interface grh {}\n" +
				"interface gri {}\n" +
				"interface grn {}\n" +
				"interface gro {}\n" +
				"interface grv {}\n" +
				"interface grw {}\n" +
				"interface gtr {}\n" +
				"interface gtu {}\n" +
				"interface gvt {}\n" +
				"interface gvw {}\n" +
				"interface gwz {}\n" +
				"interface gxc {}\n";
	}
	public void testBug550480() {
		StringBuilder source = new StringBuilder();
		source.append("package p;\n");
		String[] names = new String[571];
		for (int i = 0; i < 571; i++) {
			names[i] = "I"+i;
			source.append("interface ").append(names[i]).append(" {}\n");
		}
		source.append("public abstract class hft implements ");
		source.append(String.join(", ", names));
		source.append("\n{\n}\n");
		runConformTest(
			new String[] {
				"p/hft.java",
				source.toString()
			});
	}

	/**
	 * Test that using many generic type arguments doesn't result in a compiler hang.
	 * See: https://github.com/eclipse-jdt/eclipse.jdt.core/issues/177
	 */
	public void testManyGenericsHangGh177() {
		this.runConformTest(
			new String[] {
				"C0.java",
				"""
				public class C0
				<
				A1 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A2 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A3 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A4 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A5 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A6 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A7 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A8 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A9 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A10 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A11 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A12 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A13 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A14 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A15 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A16 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A17 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A18 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A19 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A20 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>
				>
				{
					public A1 a1 = null;
					public A1 getA1() {
						return a1;
					}
					public static void main (String[] args) {
					}
				}
				"""
			},
			""
		);
	}

}
