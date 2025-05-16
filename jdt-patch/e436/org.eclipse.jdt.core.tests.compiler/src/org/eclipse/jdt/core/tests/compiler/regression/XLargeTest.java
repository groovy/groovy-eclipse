/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
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
 *     Jesper Steen Moller - Contributions for
 *								bug 404146 - [1.7][compiler] nested try-catch-finally-blocks leads to unrunnable Java byte code
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import junit.framework.Test;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class XLargeTest extends AbstractRegressionTest {
	static {
//		TESTS_NUMBERS = new int[] { 17 };
//		TESTS_NAMES = new String[] { "testBug519070" };
	}

public XLargeTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=368435
public void test368435() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	StringBuilder sourceCode = new StringBuilder(
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"    public void print() {\n" +
			"        int i = 0;\n" +
			"        if (System.currentTimeMillis() > 17000L) {\n" +
			"            System.out.println(i++);\n");

	for (int i = 0; i < 5000; i++) {
		sourceCode.append("\t\t		System.out.println(\"xyz\");\n");
	}
	sourceCode.append("}\n}\n}\n");

	this.runConformTest(
			new String[] {
					"X.java",
					sourceCode.toString()
			},
			"SUCCESS",
			null,
			true,
			null,
			settings,
			null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=368435
public void test368435b() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	StringBuilder sourceCode = new StringBuilder(
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"    public X() {\n" +
			"        int i = 0;\n" +
			"        if (System.currentTimeMillis() > 17000L) {\n" +
			"            System.out.println(i++);\n");

	for (int i = 0; i < 5000; i++) {
		sourceCode.append("\t\t		System.out.println(\"xyz\");\n");
	}
	sourceCode.append("}\n}\n}\n");

	this.runConformTest(
			new String[] {
					"X.java",
					sourceCode.toString()
			},
			"SUCCESS",
			null,
			true,
			null,
			settings,
			null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=368435
public void test368435c() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	StringBuilder sourceCode = new StringBuilder(
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"    {\n" +
			"        int i = 0;\n" +
			"        if (System.currentTimeMillis() > 17000L) {\n" +
			"            System.out.println(i++);\n");

	for (int i = 0; i < 5000; i++) {
		sourceCode.append("\t\t		System.out.println(\"xyz\");\n");
	}
	sourceCode.append("}\n}\n}\n");

	this.runConformTest(
			new String[] {
					"X.java",
					sourceCode.toString()
			},
			"SUCCESS",
			null,
			true,
			null,
			settings,
			null);
}

public void test001() {
	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""

	public class X {
	    public static int i,j;
	    public static long l;

	    public static void main(String args[]) {
	    	foo();
	    }

	    public static void foo() {
		byte b = 0;
		while ( b < 4 ) {
	""");

	for (int i = 0; i < 160; i++) {
		fileContents.append("\t    i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;\n");
	}
	fileContents.append("""
			b++;
		}
		if (b == 4 && i == 0) System.out.println(\"SUCCESS\");
		else System.out.println(\"FAILED\");
	   }
	}
	""");

	this.runConformTest(
		new String[] {
			"X.java",
			fileContents.toString()
		},
		"SUCCESS");
}

public void test002() {
	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	public class X2 {
		public static boolean b = false;
		public static int i, l, j;

		public static void main(String args[]) {
		}

		static {
		while (b) {
	""");
	for (int i = 0; i < 160; i++) {
		fileContents.append("\t\ti*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;\n");
	}
	fileContents.append("""
			b = false;
		}
		if (i == 0) {
			System.out.println(\"SUCCESS\");
		} else {
			System.out.println(\"FAILED\");
		}
		}
	}
	""");
	this.runConformTest(
		new String[] {
			"X2.java",
			fileContents.toString()
		},
		"SUCCESS");
}

public void test003() {
	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	public class X3 {
		public int i,j;
		public long l;

		public static void main(String args[]) {
			X3 x = new X3();
		}

		public X3() {
		byte b = 0;
		i = j = 0;
		l = 0L;
		while ( b < 4 ) {
	""");
	for (int i = 0 ; i < 160; i++) {
		fileContents.append("\t\ti*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;\n");
	}
	fileContents.append("""
			b++;
		}
		if (b == 4 && i == 0) {
			System.out.println(\"SUCCESS\");
		} else {
			System.out.println(\"FAILED\");
		}
		}
	}
	""");
	this.runConformTest(
		new String[] {
			"X3.java",
			fileContents.toString()
		},
		"SUCCESS");
}

public void test004() {
	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	public class X {
		public static int i,j;
		public static long l;

		public static void main(String args[]) {
			foo();
		}

		public static void foo() {
		byte b = 0;
		for (int i = 0; i < 1; i++) {
		while ( b < 4 ) {
	""");
	for (int i = 0; i < 160; i ++) {
		fileContents.append("\t\ti*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;\n");
	}
	fileContents.append("""
				b++;
		}
		}
		if (b == 4 && i == 0) System.out.println(\"SUCCESS\");
		else System.out.println(\"FAILED\");
		}
	}
	""");
	this.runConformTest(
		new String[] {
			"X.java",
			fileContents.toString()
		},
		"SUCCESS");
}

public void test005() {
	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	package p;
	public class X {
	  public static void main(String args[]) {
	    System.out.println(\"\" + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\'
	""");
	for (int i = 0; i < 496; i++) {
		fileContents.append("      + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' \n");
	}
	fileContents.append("""
	      + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\' + \'a\');
	  }
	}
	""");
	runConformTest(
		true,
		new String[] {
		"p/X.java",
		fileContents.toString(),
	},
	"",
	null,
	null,
	JavacTestOptions.JavacHasABug.JavacThrowsAnException /* stack overflow */); // transient, platform-dependent
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=26129
 */
public void test006() {

	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	public class A {
	    public static void main(String[] args) {
	        int i = 1;
	        try {
	            if (i == 0)
	                throw new Exception();
	            return;
	        } catch (Exception e) {
	        	i = 366 * i % 534;
	"""); // $NON-NLS-1$

	for (int i = 0 ; i < 350; i++) {
		fileContents.append("        	i = 366 * i % 534;i = 366 * i % 534;i = 366 * i % 534;i = 366 * i % 534;i = 366 * i % 534;i = 366 * i % 534;i = 366 * i % 534;i = 366 * i % 534;i = 366 * i % 534;i = 366 * i % 534;"); // $NON-NLS-1$
	}

	fileContents.append("""
			} finally {
	            if (i == 1)
	                System.out.print(\"OK\");
	            else
	                System.out.print(\"FAIL\");
	        }
	    }
	}
	"""); // $NON-NLS-1$

	this.runConformTest(
		new String[] {
			"A.java",
			fileContents.toString()
		},
		"OK");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=31811
 */
public void test007() {
	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	public class X {
		public static int i,j;
		public static long l;

		public static void main(String args[]) {
			foo();
			System.out.println(\"SUCCESS\");
		}

		public static void foo() {
		byte b = 0;
		 for(;;) {
	""");
	for (int i = 0; i < 160; i++) {
		fileContents.append("\t\ti*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;i*=l+j;\n");
	}
	fileContents.append("""
			b++;
			if (b > 1) {
				break;
			};
		};
		}
	}
	""");

	this.runConformTest(
		new String[] {
			"X.java",
			fileContents.toString()
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=115408
public void test008() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.DO_NOT_GENERATE);
	options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
	options.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.DO_NOT_GENERATE);
	this.runConformTest(new String[] {
		"X.java",
		"public class X extends B implements IToken {\n" +
		"	public X( int t, int endOffset, char [] filename, int line  ) {\n" +
		"		super( t, filename, line );\n" +
		"		setOffsetAndLength( endOffset );\n" +
		"	}\n" +
		"	protected int offset;\n" +
		"	public int getOffset() { \n" +
		"		return offset; \n" +
		"	}\n" +
		"	public int getLength() {\n" +
		"		return getCharImage().length;\n" +
		"	}\n" +
		"	protected void setOffsetAndLength( int endOffset ) {\n" +
		"		this.offset = endOffset - getLength();\n" +
		"	}\n" +
		"	public String foo() { \n" +
		"		switch ( getType() ) {\n" +
		"				case IToken.tCOLONCOLON :\n" +
		"					return \"::\" ; //$NON-NLS-1$\n" +
		"				case IToken.tCOLON :\n" +
		"					return \":\" ; //$NON-NLS-1$\n" +
		"				case IToken.tSEMI :\n" +
		"					return \";\" ; //$NON-NLS-1$\n" +
		"				case IToken.tCOMMA :\n" +
		"					return \",\" ; //$NON-NLS-1$\n" +
		"				case IToken.tQUESTION :\n" +
		"					return \"?\" ; //$NON-NLS-1$\n" +
		"				case IToken.tLPAREN  :\n" +
		"					return \"(\" ; //$NON-NLS-1$\n" +
		"				case IToken.tRPAREN  :\n" +
		"					return \")\" ; //$NON-NLS-1$\n" +
		"				case IToken.tLBRACKET :\n" +
		"					return \"[\" ; //$NON-NLS-1$\n" +
		"				case IToken.tRBRACKET :\n" +
		"					return \"]\" ; //$NON-NLS-1$\n" +
		"				case IToken.tLBRACE :\n" +
		"					return \"{\" ; //$NON-NLS-1$\n" +
		"				case IToken.tRBRACE :\n" +
		"					return \"}\"; //$NON-NLS-1$\n" +
		"				case IToken.tPLUSASSIGN :\n" +
		"					return \"+=\"; //$NON-NLS-1$\n" +
		"				case IToken.tINCR :\n" +
		"					return \"++\" ; //$NON-NLS-1$\n" +
		"				case IToken.tPLUS :\n" +
		"					return \"+\"; //$NON-NLS-1$\n" +
		"				case IToken.tMINUSASSIGN :\n" +
		"					return \"-=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tDECR :\n" +
		"					return \"--\" ; //$NON-NLS-1$\n" +
		"				case IToken.tARROWSTAR :\n" +
		"					return \"->*\" ; //$NON-NLS-1$\n" +
		"				case IToken.tARROW :\n" +
		"					return \"->\" ; //$NON-NLS-1$\n" +
		"				case IToken.tMINUS :\n" +
		"					return \"-\" ; //$NON-NLS-1$\n" +
		"				case IToken.tSTARASSIGN :\n" +
		"					return \"*=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tSTAR :\n" +
		"					return \"*\" ; //$NON-NLS-1$\n" +
		"				case IToken.tMODASSIGN :\n" +
		"					return \"%=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tMOD :\n" +
		"					return \"%\" ; //$NON-NLS-1$\n" +
		"				case IToken.tXORASSIGN :\n" +
		"					return \"^=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tXOR :\n" +
		"					return \"^\" ; //$NON-NLS-1$\n" +
		"				case IToken.tAMPERASSIGN :\n" +
		"					return \"&=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tAND :\n" +
		"					return \"&&\" ; //$NON-NLS-1$\n" +
		"				case IToken.tAMPER :\n" +
		"					return \"&\" ; //$NON-NLS-1$\n" +
		"				case IToken.tBITORASSIGN :\n" +
		"					return \"|=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tOR :\n" +
		"					return \"||\" ; //$NON-NLS-1$\n" +
		"				case IToken.tBITOR :\n" +
		"					return \"|\" ; //$NON-NLS-1$\n" +
		"				case IToken.tCOMPL :\n" +
		"					return \"~\" ; //$NON-NLS-1$\n" +
		"				case IToken.tNOTEQUAL :\n" +
		"					return \"!=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tNOT :\n" +
		"					return \"!\" ; //$NON-NLS-1$\n" +
		"				case IToken.tEQUAL :\n" +
		"					return \"==\" ; //$NON-NLS-1$\n" +
		"				case IToken.tASSIGN :\n" +
		"					return \"=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tSHIFTL :\n" +
		"					return \"<<\" ; //$NON-NLS-1$\n" +
		"				case IToken.tLTEQUAL :\n" +
		"					return \"<=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tLT :\n" +
		"					return \"<\"; //$NON-NLS-1$\n" +
		"				case IToken.tSHIFTRASSIGN :\n" +
		"					return \">>=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tSHIFTR :\n" +
		"					return \">>\" ; //$NON-NLS-1$\n" +
		"				case IToken.tGTEQUAL :\n" +
		"					return \">=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tGT :\n" +
		"					return \">\" ; //$NON-NLS-1$\n" +
		"				case IToken.tSHIFTLASSIGN :\n" +
		"					return \"<<=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tELLIPSIS :\n" +
		"					return \"...\" ; //$NON-NLS-1$\n" +
		"				case IToken.tDOTSTAR :\n" +
		"					return \".*\" ; //$NON-NLS-1$\n" +
		"				case IToken.tDOT :\n" +
		"					return \".\" ; //$NON-NLS-1$\n" +
		"				case IToken.tDIVASSIGN :\n" +
		"					return \"/=\" ; //$NON-NLS-1$\n" +
		"				case IToken.tDIV :\n" +
		"					return \"/\" ; //$NON-NLS-1$\n" +
		"				case IToken.t_and :\n" +
		"					return Keywords.AND;\n" +
		"				case IToken.t_and_eq :\n" +
		"					return Keywords.AND_EQ ;\n" +
		"				case IToken.t_asm :\n" +
		"					return Keywords.ASM ;\n" +
		"				case IToken.t_auto :\n" +
		"					return Keywords.AUTO ;\n" +
		"				case IToken.t_bitand :\n" +
		"					return Keywords.BITAND ;\n" +
		"				case IToken.t_bitor :\n" +
		"					return Keywords.BITOR ;\n" +
		"				case IToken.t_bool :\n" +
		"					return Keywords.BOOL ;\n" +
		"				case IToken.t_break :\n" +
		"					return Keywords.BREAK ;\n" +
		"				case IToken.t_case :\n" +
		"					return Keywords.CASE ;\n" +
		"				case IToken.t_catch :\n" +
		"					return Keywords.CATCH ;\n" +
		"				case IToken.t_char :\n" +
		"					return Keywords.CHAR ;\n" +
		"				case IToken.t_class :\n" +
		"					return Keywords.CLASS ;\n" +
		"				case IToken.t_compl :\n" +
		"					return Keywords.COMPL ;\n" +
		"				case IToken.t_const :\n" +
		"					return Keywords.CONST ;\n" +
		"				case IToken.t_const_cast :\n" +
		"					return Keywords.CONST_CAST ;\n" +
		"				case IToken.t_continue :\n" +
		"					return Keywords.CONTINUE ;\n" +
		"				case IToken.t_default :\n" +
		"					return Keywords.DEFAULT ;\n" +
		"				case IToken.t_delete :\n" +
		"					return Keywords.DELETE ;\n" +
		"				case IToken.t_do :\n" +
		"					return Keywords.DO;\n" +
		"				case IToken.t_double :\n" +
		"					return Keywords.DOUBLE ;\n" +
		"				case IToken.t_dynamic_cast :\n" +
		"					return Keywords.DYNAMIC_CAST ;\n" +
		"				case IToken.t_else :\n" +
		"					return Keywords.ELSE;\n" +
		"				case IToken.t_enum :\n" +
		"					return Keywords.ENUM ;\n" +
		"				case IToken.t_explicit :\n" +
		"					return Keywords.EXPLICIT ;\n" +
		"				case IToken.t_export :\n" +
		"					return Keywords.EXPORT ;\n" +
		"				case IToken.t_extern :\n" +
		"					return Keywords.EXTERN;\n" +
		"				case IToken.t_false :\n" +
		"					return Keywords.FALSE;\n" +
		"				case IToken.t_float :\n" +
		"					return Keywords.FLOAT;\n" +
		"				case IToken.t_for :\n" +
		"					return Keywords.FOR;\n" +
		"				case IToken.t_friend :\n" +
		"					return Keywords.FRIEND;\n" +
		"				case IToken.t_goto :\n" +
		"					return Keywords.GOTO;\n" +
		"				case IToken.t_if :\n" +
		"					return Keywords.IF ;\n" +
		"				case IToken.t_inline :\n" +
		"					return Keywords.INLINE ;\n" +
		"				case IToken.t_int :\n" +
		"					return Keywords.INT ;\n" +
		"				case IToken.t_long :\n" +
		"					return Keywords.LONG ;\n" +
		"				case IToken.t_mutable :\n" +
		"					return Keywords.MUTABLE ;\n" +
		"				case IToken.t_namespace :\n" +
		"					return Keywords.NAMESPACE ;\n" +
		"				case IToken.t_new :\n" +
		"					return Keywords.NEW ;\n" +
		"				case IToken.t_not :\n" +
		"					return Keywords.NOT ;\n" +
		"				case IToken.t_not_eq :\n" +
		"					return Keywords.NOT_EQ; \n" +
		"				case IToken.t_operator :\n" +
		"					return Keywords.OPERATOR ;\n" +
		"				case IToken.t_or :\n" +
		"					return Keywords.OR ;\n" +
		"				case IToken.t_or_eq :\n" +
		"					return Keywords.OR_EQ;\n" +
		"				case IToken.t_private :\n" +
		"					return Keywords.PRIVATE ;\n" +
		"				case IToken.t_protected :\n" +
		"					return Keywords.PROTECTED ;\n" +
		"				case IToken.t_public :\n" +
		"					return Keywords.PUBLIC ;\n" +
		"				case IToken.t_register :\n" +
		"					return Keywords.REGISTER ;\n" +
		"				case IToken.t_reinterpret_cast :\n" +
		"					return Keywords.REINTERPRET_CAST ;\n" +
		"				case IToken.t_return :\n" +
		"					return Keywords.RETURN ;\n" +
		"				case IToken.t_short :\n" +
		"					return Keywords.SHORT ;\n" +
		"				case IToken.t_sizeof :\n" +
		"					return Keywords.SIZEOF ;\n" +
		"				case IToken.t_static :\n" +
		"					return Keywords.STATIC ;\n" +
		"				case IToken.t_static_cast :\n" +
		"					return Keywords.STATIC_CAST ;\n" +
		"				case IToken.t_signed :\n" +
		"					return Keywords.SIGNED ;\n" +
		"				case IToken.t_struct :\n" +
		"					return Keywords.STRUCT ;\n" +
		"				case IToken.t_switch :\n" +
		"					return Keywords.SWITCH ;\n" +
		"				case IToken.t_template :\n" +
		"					return Keywords.TEMPLATE ;\n" +
		"				case IToken.t_this :\n" +
		"					return Keywords.THIS ;\n" +
		"				case IToken.t_throw :\n" +
		"					return Keywords.THROW ;\n" +
		"				case IToken.t_true :\n" +
		"					return Keywords.TRUE ;\n" +
		"				case IToken.t_try :\n" +
		"					return Keywords.TRY ;\n" +
		"				case IToken.t_typedef :\n" +
		"					return Keywords.TYPEDEF ;\n" +
		"				case IToken.t_typeid :\n" +
		"					return Keywords.TYPEID ;\n" +
		"				case IToken.t_typename :\n" +
		"					return Keywords.TYPENAME ;\n" +
		"				case IToken.t_union :\n" +
		"					return Keywords.UNION ;\n" +
		"				case IToken.t_unsigned :\n" +
		"					return Keywords.UNSIGNED ;\n" +
		"				case IToken.t_using :\n" +
		"					return Keywords.USING ;\n" +
		"				case IToken.t_virtual :\n" +
		"					return Keywords.VIRTUAL ;\n" +
		"				case IToken.t_void :\n" +
		"					return Keywords.VOID ;\n" +
		"				case IToken.t_volatile :\n" +
		"					return Keywords.VOLATILE;\n" +
		"				case IToken.t_wchar_t :\n" +
		"					return Keywords.WCHAR_T ;\n" +
		"				case IToken.t_while :\n" +
		"					return Keywords.WHILE ;\n" +
		"				case IToken.t_xor :\n" +
		"					return Keywords.XOR ;\n" +
		"				case IToken.t_xor_eq :\n" +
		"					return Keywords.XOR_EQ ;\n" +
		"				case IToken.t__Bool :\n" +
		"					return Keywords._BOOL ;\n" +
		"				case IToken.t__Complex :\n" +
		"					return Keywords._COMPLEX ;\n" +
		"				case IToken.t__Imaginary :\n" +
		"					return Keywords._IMAGINARY ;\n" +
		"				case IToken.t_restrict :\n" +
		"					return Keywords.RESTRICT ;\n" +
		"				case IScanner.tPOUND:\n" +
		"					return \"#\"; //$NON-NLS-1$\n" +
		"				case IScanner.tPOUNDPOUND:\n" +
		"					return \"##\"; //$NON-NLS-1$\n" +
		"				case IToken.tEOC:\n" +
		"					return \"EOC\"; //$NON-NLS-1$\n" +
		"				default :\n" +
		"					return \"\"; //$NON-NLS-1$ \n" +
		"		}			\n" +
		"	}\n" +
		"	public char[] getCharImage() {\n" +
		"	    return getCharImage( getType() );\n" +
		"	}\n" +
		"	static public char[] getCharImage( int type ){\n" +
		"		return null;\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		System.out.println(\"SUCCESS\");\n" +
		"	}\n" +
		"}\n" +
		"interface IToken {\n" +
		"	static public final int tIDENTIFIER = 1;\n" +
		"	static public final int tINTEGER = 2;\n" +
		"	static public final int tCOLONCOLON = 3;\n" +
		"	static public final int tCOLON = 4;\n" +
		"	static public final int tSEMI = 5;\n" +
		"	static public final int tCOMMA = 6;\n" +
		"	static public final int tQUESTION = 7;\n" +
		"	static public final int tLPAREN = 8;\n" +
		"	static public final int tRPAREN = 9;\n" +
		"	static public final int tLBRACKET = 10;\n" +
		"	static public final int tRBRACKET = 11;\n" +
		"	static public final int tLBRACE = 12;\n" +
		"	static public final int tRBRACE = 13;\n" +
		"	static public final int tPLUSASSIGN = 14;\n" +
		"	static public final int tINCR = 15;\n" +
		"	static public final int tPLUS = 16;\n" +
		"	static public final int tMINUSASSIGN = 17;\n" +
		"	static public final int tDECR = 18;\n" +
		"	static public final int tARROWSTAR = 19;\n" +
		"	static public final int tARROW = 20;\n" +
		"	static public final int tMINUS = 21;\n" +
		"	static public final int tSTARASSIGN = 22;\n" +
		"	static public final int tSTAR = 23;\n" +
		"	static public final int tMODASSIGN = 24;\n" +
		"	static public final int tMOD = 25;\n" +
		"	static public final int tXORASSIGN = 26;\n" +
		"	static public final int tXOR = 27;\n" +
		"	static public final int tAMPERASSIGN = 28;\n" +
		"	static public final int tAND = 29;\n" +
		"	static public final int tAMPER = 30;\n" +
		"	static public final int tBITORASSIGN = 31;\n" +
		"	static public final int tOR = 32;\n" +
		"	static public final int tBITOR = 33;\n" +
		"	static public final int tCOMPL = 34;\n" +
		"	static public final int tNOTEQUAL = 35;\n" +
		"	static public final int tNOT = 36;\n" +
		"	static public final int tEQUAL = 37;\n" +
		"	static public final int tASSIGN = 38;\n" +
		"	static public final int tSHIFTL = 40;\n" +
		"	static public final int tLTEQUAL = 41;\n" +
		"	static public final int tLT = 42;\n" +
		"	static public final int tSHIFTRASSIGN = 43;\n" +
		"	static public final int tSHIFTR = 44;\n" +
		"	static public final int tGTEQUAL = 45;\n" +
		"	static public final int tGT = 46;\n" +
		"	static public final int tSHIFTLASSIGN = 47;\n" +
		"	static public final int tELLIPSIS = 48;\n" +
		"	static public final int tDOTSTAR = 49;\n" +
		"	static public final int tDOT = 50;\n" +
		"	static public final int tDIVASSIGN = 51;\n" +
		"	static public final int tDIV = 52;\n" +
		"	static public final int t_and = 54;\n" +
		"	static public final int t_and_eq = 55;\n" +
		"	static public final int t_asm = 56;\n" +
		"	static public final int t_auto = 57;\n" +
		"	static public final int t_bitand = 58;\n" +
		"	static public final int t_bitor = 59;\n" +
		"	static public final int t_bool = 60;\n" +
		"	static public final int t_break = 61;\n" +
		"	static public final int t_case = 62;\n" +
		"	static public final int t_catch = 63;\n" +
		"	static public final int t_char = 64;\n" +
		"	static public final int t_class = 65;\n" +
		"	static public final int t_compl = 66;\n" +
		"	static public final int t_const = 67;\n" +
		"	static public final int t_const_cast = 69;\n" +
		"	static public final int t_continue = 70;\n" +
		"	static public final int t_default = 71;\n" +
		"	static public final int t_delete = 72;\n" +
		"	static public final int t_do = 73;\n" +
		"	static public final int t_double = 74;\n" +
		"	static public final int t_dynamic_cast = 75;\n" +
		"	static public final int t_else = 76;\n" +
		"	static public final int t_enum = 77;\n" +
		"	static public final int t_explicit = 78;\n" +
		"	static public final int t_export = 79;\n" +
		"	static public final int t_extern = 80;\n" +
		"	static public final int t_false = 81;\n" +
		"	static public final int t_float = 82;\n" +
		"	static public final int t_for = 83;\n" +
		"	static public final int t_friend = 84;\n" +
		"	static public final int t_goto = 85;\n" +
		"	static public final int t_if = 86;\n" +
		"	static public final int t_inline = 87;\n" +
		"	static public final int t_int = 88;\n" +
		"	static public final int t_long = 89;\n" +
		"	static public final int t_mutable = 90;\n" +
		"	static public final int t_namespace = 91;\n" +
		"	static public final int t_new = 92;\n" +
		"	static public final int t_not = 93;\n" +
		"	static public final int t_not_eq = 94;\n" +
		"	static public final int t_operator = 95;\n" +
		"	static public final int t_or = 96;\n" +
		"	static public final int t_or_eq = 97;\n" +
		"	static public final int t_private = 98;\n" +
		"	static public final int t_protected = 99;\n" +
		"	static public final int t_public = 100;\n" +
		"	static public final int t_register = 101;\n" +
		"	static public final int t_reinterpret_cast = 102;\n" +
		"	static public final int t_return = 103;\n" +
		"	static public final int t_short = 104;\n" +
		"	static public final int t_sizeof = 105;\n" +
		"	static public final int t_static = 106;\n" +
		"	static public final int t_static_cast = 107;\n" +
		"	static public final int t_signed = 108;\n" +
		"	static public final int t_struct = 109;\n" +
		"	static public final int t_switch = 110;\n" +
		"	static public final int t_template = 111;\n" +
		"	static public final int t_this = 112;\n" +
		"	static public final int t_throw = 113;\n" +
		"	static public final int t_true = 114;\n" +
		"	static public final int t_try = 115;\n" +
		"	static public final int t_typedef = 116;\n" +
		"	static public final int t_typeid = 117;\n" +
		"	static public final int t_typename = 118;\n" +
		"	static public final int t_union = 119;\n" +
		"	static public final int t_unsigned = 120;\n" +
		"	static public final int t_using = 121;\n" +
		"	static public final int t_virtual = 122;\n" +
		"	static public final int t_void = 123;\n" +
		"	static public final int t_volatile = 124;\n" +
		"	static public final int t_wchar_t = 125;\n" +
		"	static public final int t_while = 126;\n" +
		"	static public final int t_xor = 127;\n" +
		"	static public final int t_xor_eq = 128;\n" +
		"	static public final int tFLOATINGPT = 129;\n" +
		"	static public final int tSTRING = 130;\n" +
		"	static public final int tLSTRING = 131;\n" +
		"	static public final int tCHAR = 132;\n" +
		"	static public final int tLCHAR = 133;\n" +
		"	static public final int t__Bool = 134;\n" +
		"	static public final int t__Complex = 135;\n" +
		"	static public final int t__Imaginary = 136;\n" +
		"	static public final int t_restrict = 137;\n" +
		"	static public final int tMACROEXP = 138;\n" +
		"	static public final int tPOUNDPOUND = 139;\n" +
		"	static public final int tCOMPLETION = 140;\n" +
		"	static public final int tEOC = 141; // End of Completion\" + \n" +
		"	static public final int tLAST = 141;\n" +
		"}\n" +
		"class Keywords {\n" +
		"	public static final String CAST = \"cast\"; //$NON-NLS-1$\n" +
		"	public static final String ALIGNOF = \"alignof\"; //$NON-NLS-1$\n" +
		"	public static final String TYPEOF = \"typeof\"; //$NON-NLS-1$\n" +
		"	public static final String cpMIN = \"<?\"; //$NON-NLS-1$\n" +
		"	public static final String cpMAX = \">?\"; //$NON-NLS-1$\n" +
		"	public static final String _BOOL = \"_Bool\"; //$NON-NLS-1$\n" +
		"	public static final String _COMPLEX = \"_Complex\"; //$NON-NLS-1$\n" +
		"	public static final String _IMAGINARY = \"_Imaginary\"; //$NON-NLS-1$\n" +
		"	public static final String AND = \"and\"; //$NON-NLS-1$\n" +
		"	public static final String AND_EQ = \"and_eq\"; //$NON-NLS-1$\n" +
		"	public static final String ASM = \"asm\"; //$NON-NLS-1$\n" +
		"	public static final String AUTO = \"auto\"; //$NON-NLS-1$\n" +
		"	public static final String BITAND = \"bitand\"; //$NON-NLS-1$\n" +
		"	public static final String BITOR = \"bitor\"; //$NON-NLS-1$\n" +
		"	public static final String BOOL = \"bool\"; //$NON-NLS-1$\n" +
		"	public static final String BREAK = \"break\"; //$NON-NLS-1$\n" +
		"	public static final String CASE = \"case\"; //$NON-NLS-1$\n" +
		"	public static final String CATCH = \"catch\"; //$NON-NLS-1$\n" +
		"	public static final String CHAR = \"char\"; //$NON-NLS-1$\n" +
		"	public static final String CLASS = \"class\"; //$NON-NLS-1$\n" +
		"	public static final String COMPL = \"compl\"; //$NON-NLS-1$\n" +
		"	public static final String CONST = \"const\"; //$NON-NLS-1$\n" +
		"	public static final String CONST_CAST = \"const_cast\"; //$NON-NLS-1$\n" +
		"	public static final String CONTINUE = \"continue\"; //$NON-NLS-1$\n" +
		"	public static final String DEFAULT = \"default\"; //$NON-NLS-1$\n" +
		"	public static final String DELETE = \"delete\"; //$NON-NLS-1$\n" +
		"	public static final String DO = \"do\"; //$NON-NLS-1$\n" +
		"	public static final String DOUBLE = \"double\"; //$NON-NLS-1$\n" +
		"	public static final String DYNAMIC_CAST = \"dynamic_cast\"; //$NON-NLS-1$\n" +
		"	public static final String ELSE = \"else\"; //$NON-NLS-1$\n" +
		"	public static final String ENUM = \"enum\"; //$NON-NLS-1$\n" +
		"	public static final String EXPLICIT = \"explicit\"; //$NON-NLS-1$\n" +
		"	public static final String EXPORT = \"export\"; //$NON-NLS-1$\n" +
		"	public static final String EXTERN = \"extern\"; //$NON-NLS-1$\n" +
		"	public static final String FALSE = \"false\"; //$NON-NLS-1$\n" +
		"	public static final String FLOAT = \"float\"; //$NON-NLS-1$\n" +
		"	public static final String FOR = \"for\"; //$NON-NLS-1$\n" +
		"	public static final String FRIEND = \"friend\"; //$NON-NLS-1$\n" +
		"	public static final String GOTO = \"goto\"; //$NON-NLS-1$\n" +
		"	public static final String IF = \"if\"; //$NON-NLS-1$\n" +
		"	public static final String INLINE = \"inline\"; //$NON-NLS-1$\n" +
		"	public static final String INT = \"int\"; //$NON-NLS-1$\n" +
		"	public static final String LONG = \"long\"; //$NON-NLS-1$\n" +
		"	public static final String LONG_LONG = \"long long\"; //$NON-NLS-1$\n" +
		"	public static final String MUTABLE = \"mutable\"; //$NON-NLS-1$\n" +
		"	public static final String NAMESPACE = \"namespace\"; //$NON-NLS-1$\n" +
		"	public static final String NEW = \"new\"; //$NON-NLS-1$\n" +
		"	public static final String NOT = \"not\"; //$NON-NLS-1$\n" +
		"	public static final String NOT_EQ = \"not_eq\"; //$NON-NLS-1$\n" +
		"	public static final String OPERATOR = \"operator\"; //$NON-NLS-1$\n" +
		"	public static final String OR = \"or\"; //$NON-NLS-1$\n" +
		"	public static final String OR_EQ = \"or_eq\"; //$NON-NLS-1$\n" +
		"	public static final String PRIVATE = \"private\"; //$NON-NLS-1$\n" +
		"	public static final String PROTECTED = \"protected\"; //$NON-NLS-1$\n" +
		"	public static final String PUBLIC = \"public\"; //$NON-NLS-1$\n" +
		"	public static final String REGISTER = \"register\"; //$NON-NLS-1$\n" +
		"	public static final String REINTERPRET_CAST = \"reinterpret_cast\"; //$NON-NLS-1$\n" +
		"	public static final String RESTRICT = \"restrict\"; //$NON-NLS-1$\n" +
		"	public static final String RETURN = \"return\"; //$NON-NLS-1$\n" +
		"	public static final String SHORT = \"short\"; //$NON-NLS-1$\n" +
		"	public static final String SIGNED = \"signed\"; //$NON-NLS-1$\n" +
		"	public static final String SIZEOF = \"sizeof\"; //$NON-NLS-1$\n" +
		"	public static final String STATIC = \"static\"; //$NON-NLS-1$\n" +
		"	public static final String STATIC_CAST = \"static_cast\"; //$NON-NLS-1$\n" +
		"	public static final String STRUCT = \"struct\"; //$NON-NLS-1$\n" +
		"	public static final String SWITCH = \"switch\"; //$NON-NLS-1$\n" +
		"	public static final String TEMPLATE = \"template\"; //$NON-NLS-1$\n" +
		"	public static final String THIS = \"this\"; //$NON-NLS-1$\n" +
		"	public static final String THROW = \"throw\"; //$NON-NLS-1$\n" +
		"	public static final String TRUE = \"true\"; //$NON-NLS-1$\n" +
		"	public static final String TRY = \"try\"; //$NON-NLS-1$\n" +
		"	public static final String TYPEDEF = \"typedef\"; //$NON-NLS-1$\n" +
		"	public static final String TYPEID = \"typeid\"; //$NON-NLS-1$\n" +
		"	public static final String TYPENAME = \"typename\"; //$NON-NLS-1$\n" +
		"	public static final String UNION = \"union\"; //$NON-NLS-1$\n" +
		"	public static final String UNSIGNED = \"unsigned\"; //$NON-NLS-1$\n" +
		"	public static final String USING = \"using\"; //$NON-NLS-1$\n" +
		"	public static final String VIRTUAL = \"virtual\"; //$NON-NLS-1$\n" +
		"	public static final String VOID = \"void\"; //$NON-NLS-1$\n" +
		"	public static final String VOLATILE = \"volatile\"; //$NON-NLS-1$\n" +
		"	public static final String WCHAR_T = \"wchar_t\"; //$NON-NLS-1$\n" +
		"	public static final String WHILE = \"while\"; //$NON-NLS-1$\n" +
		"	public static final String XOR = \"xor\"; //$NON-NLS-1$\n" +
		"	public static final String XOR_EQ = \"xor_eq\"; //$NON-NLS-1$\n" +
		"	public static final char[] c_BOOL = \"_Bool\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] c_COMPLEX = \"_Complex\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] c_IMAGINARY = \"_Imaginary\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cAND = \"and\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cAND_EQ = \"and_eq\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cASM = \"asm\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cAUTO = \"auto\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cBITAND = \"bitand\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cBITOR = \"bitor\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cBOOL = \"bool\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cBREAK = \"break\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cCASE = \"case\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cCATCH = \"catch\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cCHAR = \"char\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cCLASS = \"class\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cCOMPL = \"compl\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cCONST = \"const\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cCONST_CAST = \"const_cast\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cCONTINUE = \"continue\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cDEFAULT = \"default\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cDELETE = \"delete\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cDO = \"do\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cDOUBLE = \"double\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cDYNAMIC_CAST = \"dynamic_cast\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cELSE = \"else\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cENUM = \"enum\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cEXPLICIT = \"explicit\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cEXPORT = \"export\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cEXTERN = \"extern\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cFALSE = \"false\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cFLOAT = \"float\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cFOR = \"for\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cFRIEND = \"friend\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cGOTO = \"goto\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cIF = \"if\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cINLINE = \"inline\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cINT = \"int\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cLONG = \"long\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cMUTABLE = \"mutable\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cNAMESPACE = \"namespace\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cNEW = \"new\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cNOT = \"not\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cNOT_EQ = \"not_eq\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cOPERATOR = \"operator\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cOR = \"or\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cOR_EQ = \"or_eq\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cPRIVATE = \"private\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cPROTECTED = \"protected\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cPUBLIC = \"public\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cREGISTER = \"register\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cREINTERPRET_CAST = \"reinterpret_cast\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cRESTRICT = \"restrict\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cRETURN = \"return\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cSHORT = \"short\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cSIGNED = \"signed\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cSIZEOF = \"sizeof\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cSTATIC = \"static\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cSTATIC_CAST = \"static_cast\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cSTRUCT = \"struct\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cSWITCH = \"switch\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cTEMPLATE = \"template\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cTHIS = \"this\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cTHROW = \"throw\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cTRUE = \"true\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cTRY = \"try\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cTYPEDEF = \"typedef\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cTYPEID = \"typeid\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cTYPENAME = \"typename\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cUNION = \"union\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cUNSIGNED = \"unsigned\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cUSING = \"using\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cVIRTUAL = \"virtual\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cVOID = \"void\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cVOLATILE = \"volatile\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cWCHAR_T = \"wchar_t\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cWHILE = \"while\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cXOR = \"xor\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cXOR_EQ = \"xor_eq\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpCOLONCOLON = \"::\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpCOLON = \":\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpSEMI = \";\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpCOMMA =	\",\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpQUESTION = \"?\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpLPAREN  = \"(\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpRPAREN  = \")\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpLBRACKET = \"[\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpRBRACKET = \"]\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpLBRACE = \"{\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpRBRACE = \"}\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpPLUSASSIGN =	\"+=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpINCR = 	\"++\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpPLUS = 	\"+\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpMINUSASSIGN =	\"-=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpDECR = 	\"--\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpARROWSTAR =	\"->*\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpARROW = 	\"->\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpMINUS = 	\"-\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpSTARASSIGN =	\"*=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpSTAR = 	\"*\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpMODASSIGN =	\"%=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpMOD = 	\"%\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpXORASSIGN =	\"^=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpXOR = 	\"^\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpAMPERASSIGN =	\"&=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpAND = 	\"&&\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpAMPER =	\"&\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpBITORASSIGN =	\"|=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpOR = 	\"||\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpBITOR =	\"|\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpCOMPL =	\"~\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpNOTEQUAL =	\"!=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpNOT = 	\"!\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpEQUAL =	\"==\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpASSIGN =\"=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpSHIFTL =	\"<<\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpLTEQUAL =	\"<=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpLT = 	\"<\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpSHIFTRASSIGN =	\">>=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpSHIFTR = 	\">>\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpGTEQUAL = 	\">=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpGT = 	\">\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpSHIFTLASSIGN =	\"<<=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpELLIPSIS = 	\"...\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpDOTSTAR = 	\".*\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpDOT = 	\".\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpDIVASSIGN =	\"/=\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpDIV = 	\"/\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpPOUND = \"#\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cpPOUNDPOUND = \"##\".toCharArray(); //$NON-NLS-1$\n" +
		"	// preprocessor keywords\" + \n" +
		"	public static final char[] cIFDEF = \"ifdef\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cIFNDEF = \"ifndef\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cELIF = \"elif\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cENDIF = \"endif\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cINCLUDE = \"include\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cDEFINE = \"define\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cUNDEF = \"undef\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cERROR = \"error\".toCharArray(); //$NON-NLS-1$\n" +
		"	public static final char[] cINCLUDE_NEXT = \"include_next\".toCharArray(); //$NON-NLS-1$\n" +
		"}\n" +
		"interface IScanner  {\n" +
		"	public static final int tPOUNDPOUND = -6;\n" +
		"	public static final int tPOUND      = -7;\n" +
		"}\n" +
		"abstract class B  {\n" +
		"	public B( int type, char [] filename, int lineNumber ) {\n" +
		"	}\n" +
		"	public int getType() { return 0; }\n" +
		"}",
	},
	"SUCCESS",
	null,
	true,
	null,
	options,
	null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126744
public void test009() {

	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	public class X {
	    public static String CONSTANT =\s
	""");

	for (int i = 0 ; i < 1029; i++) {
		fileContents.append("        \"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz\" +\n");
	}
	fileContents.append("""
	    	\"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxy12\";

	    public static void main(String[] args) {
	    	System.out.print(CONSTANT == CONSTANT);
	    }
	}
	""");

	runConformTest(
		true,
		new String[] {
			"X.java",
			fileContents.toString()
		},
		null,
		"true",
		null,
		JavacTestOptions.EclipseJustification.EclipseBug126744);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Failed before using a non recursive implementation of deep binary
// expressions.
public void test010() {
	StringBuilder sourceCode = new StringBuilder(
			"public class X {\n" +
			"  void foo(String a, String b, String c, String d, String e) {\n" +
			"    String s = \n");
	for (int i = 0; i < 350; i++) {
		sourceCode.append(
			"    	\"abcdef\" + a + b + c + d + e + " +
			"\" ghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmno" +
			"pqrstuvwxyzabcdefghijklmnopqrstuvwxyz\" +\n");
	}
	sourceCode.append(
			"    	\"abcdef\" + a + b + c + d + e + \" ghijklmnopqrstuvwxyz" +
			"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
			"abcdefghijklmnopqrstuvwxy12\";\n" +
			"    }\n" +
			"}");
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			sourceCode.toString()
		},
		null,
		"",
		null,
		JavacTestOptions.JavacHasABug.JavacThrowsAnException /* stack overflow */);  // transient, platform-dependent
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// check if we hit the 64Kb limit on method code lenth in class files before
// filling the stack
// need to use a computed string (else this source file will get blown away
// as well)
public void test011() {
	if (this.complianceLevel >= ClassFileConstants.JDK9)
		return;
	int length = 3 * 54 * 1000;
		// the longer the slower, but still needs to reach the limit...
	StringBuilder veryLongString = new StringBuilder(length + 20);
	veryLongString.append('"');
	Random random = new Random();
	while (veryLongString.length() < length) {
		veryLongString.append("\"+a+\"");
		veryLongString.append(random.nextLong());
	}
	veryLongString.append('"');
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(String a, String b, String c, String d, String e) {\n" +
			"    String s = \n" +
			veryLongString.toString() +
			"    	+ \"abcdef\" + a + b + c + d + e + \" ghiABCDEFGHIJKLMNOPQRSTUVWXYZjklmnopqrstuvwxyzabcdefghiABCDEFGHIJKLMNOPQRSTUVWXYZjklmnopqrstuvwxyzabcdefghiABCDEFGHIJKLMNOPQRSTUVWXYZjklmnopqrstuvwxyzabcdefghiABCDEFGHIJKLMNOPQRSTUVWXYZjklmnopqrstuvwxy12\";\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(String a, String b, String c, String d, String e) {\n" +
		"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The code of method foo(String, String, String, String, String) is " +
			"exceeding the 65535 bytes limit\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// variant: right member of the topmost expression is left-deep
public void test012() {
	StringBuilder sourceCode = new StringBuilder(
			"public class X {\n" +
			"  void foo(String a, String b, String c, String d, String e) {\n" +
			"    String s = a + (\n");
	for (int i = 0; i < 1000; i++) {
		sourceCode.append(
			"    	\"abcdef\" + a + b + c + d + e + " +
			"\" ghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmno" +
			"pqrstuvwxyzabcdefghijklmnopqrstuvwxyz\" +\n");
	}
	sourceCode.append(
			"    	\"abcdef\" + a + b + c + d + e + \" ghijklmnopqrstuvwxyz" +
			"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
			"abcdefghijklmnopqrstuvwxy12\");\n" +
			"    }\n" +
			"}");
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			sourceCode.toString()
		},
		null,
		"",
		null,
		JavacTestOptions.JavacHasABug.JavacThrowsAnException /* stack overflow */); // transient, platform-dependent
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
//variant: right member of the topmost expression is left-deep
public void test013() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	// left to right marker\n" +
			"	protected static char LRM = \'\\u200e\';\n" +
			"	// left to right embedding\n" +
			"	protected static char LRE = \'\\u202a\';\n" +
			"	// pop directional format	\n" +
			"	protected static char PDF = \'\\u202c\';\n" +
			"\n" +
			"	private static String PATH_1_RESULT = LRE + \"d\" + PDF + \":\" + LRM + \"\\\\\" + LRM + LRE + \"test\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05d0\\u05d1\\u05d2\\u05d3 \\u05d4\\u05d5\" + PDF + \"\\\\\" + LRM + LRE + \"segment\" + PDF;\n" +
			"	private static String PATH_2_RESULT = LRM + \"\\\\\" + LRM + LRE + \"test\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05d0\\u05d1\\u05d2\\u05d3 \\u05d4\\u05d5\" + PDF + \"\\\\\" + LRM + LRE + \"segment\" + PDF;\n" +
			"	private static String PATH_3_RESULT = LRE + \"d\" + PDF + \":\" + LRM + \"\\\\\" + LRM + LRE + \"\\u05ea\\u05e9\\u05e8\\u05e7\\u05e6 abcdef-\\u05e5\\u05e4\\u05e3\" + PDF + \"\\\\\" + LRM + LRE + \"xyz\" + PDF + \"\\\\\" + LRM + LRE + \"abcdef\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05e2\\u05e1\\u05e0\" + PDF;\n" +
			"	private static String PATH_4_RESULT = LRM + \"\\\\\" + LRM + LRE + \"\\u05ea\\u05e9\\u05e8\\u05e7\\u05e6 abcdef-\\u05e5\\u05e4\\u05e3\" + PDF + \"\\\\\" + LRM + LRE + \"xyz\" + PDF + \"\\\\\" + LRM + LRE + \"abcdef\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05e2\\u05e1\\05e0\" + PDF;\n" +
			"	private static String PATH_5_RESULT = LRE + \"d\" + PDF + \":\" + LRM + \"\\\\\" + LRM + LRE + \"\\u05ea\\u05e9\\u05e8\\u05e7\\u05e6 abcdef-\\u05e5\\u05e4\\u05e3\" + PDF + \"\\\\\" + LRM + LRE + \"xyz\" + PDF + \"\\\\\" + LRM + LRE + \"abcdef\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05e2\\u05e1\\05e0\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05df\\u05fd\\u05dd\" + PDF + \".\" + LRM + LRE + \"java\" + PDF;\n" +
			"	private static String PATH_6_RESULT = LRE + \"d\" + PDF + \":\" + LRM + \"\\\\\" + LRM + LRE + \"\\u05ea\\u05e9\\u05e8\\u05e7\\u05e6 abcdef-\\u05e5\\u05e4\\u05e3\" + PDF + \"\\\\\" + LRM + LRE + \"xyz\" + PDF + \"\\\\\" + LRM + LRE + \"abcdef\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05e2\\u05e1\\05e0\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05df\\u05fd\\u05dd\" + PDF + \".\" + LRM + LRE + \"\\u05dc\\u05db\\u05da\" + PDF;\n" +
			"	private static String PATH_7_RESULT = LRE + \"d\" + PDF + \":\" + LRM + \"\\\\\" + LRM + LRE + \"\\u05ea\\u05e9\\u05e8\\u05e7\\u05e6 abcdef-\\u05e5\\u05e4\\u05e3\" + PDF + \"\\\\\" + LRM + LRE + \"xyz\" + PDF + \"\\\\\" + LRM + LRE + \"abcdef\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05e2\\u05e1\\05e0\" + PDF + \"\\\\\" + LRM + LRE + \"Test\" + PDF + \".\" + LRM + LRE + \"java\" + PDF;\n" +
			"	private static String PATH_8_RESULT = LRM + \"\\\\\" + LRM + LRE + \"test\" + PDF + \"\\\\\" + LRM + LRE + \"jkl\\u05d0\\u05d1\\u05d2\\u05d3 \\u05d4\\u05d5\" + PDF + \"\\\\\" + LRM + LRE + \"segment\" + PDF;\n" +
			"	private static String PATH_9_RESULT = LRM + \"\\\\\" + LRM + LRE + \"test\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05d0\\u05d1\\u05d2\\u05d3 \\u05d4\\u05d5jkl\" + PDF + \"\\\\\" + LRM + LRE + \"segment\" + PDF;\n" +
			"	private static String PATH_10_RESULT = LRE + \"d\" + PDF + \":\" + LRM + \"\\\\\" + LRM + LRE + \"t\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05d0\" + PDF + \"\\\\\" + LRM + LRE + \"segment\" + PDF;\n" +
			"	private static String PATH_11_RESULT = \"\\\\\" + LRM + LRE + \"t\" + PDF + \"\\\\\" + LRM + LRE + \"\\u05d0\" + PDF + \"\\\\\" + LRM + LRE + \"segment\" + PDF;\n" +
			"	private static String PATH_12_RESULT = LRE + \"d\" + PDF + \":\" + LRM + \"\\\\\" + LRM;\n" +
			"	private static String PATH_13_RESULT = LRM + \"\\\\\" + LRM + LRE + \"test\" + PDF;\n" +
			"\n" +
			"	private static String OTHER_STRING_NO_DELIM = \"\\u05ea\\u05e9\\u05e8\\u05e7\\u05e6 abcdef-\\u05e5\\u05e4\\u05e3\";\n" +
			"\n" +
			"	private static String OTHER_STRING_1_RESULT = LRM + \"*\" + LRM + \".\" + LRM + LRE + \"java\" + PDF;\n" +
			"	private static String OTHER_STRING_2_RESULT = LRM + \"*\" + LRM + \".\" + LRM + LRE + \"\\u05d0\\u05d1\\u05d2\" + PDF;\n" +
			"	private static String OTHER_STRING_3_RESULT = LRE + \"\\u05d0\\u05d1\\u05d2 \" + PDF + \"=\" + LRM + LRE + \" \\u05ea\\u05e9\\u05e8\\u05e7\\u05e6\" + PDF;\n" +
			"	// result strings if null delimiter is passed for *.<string> texts\n" +
			"	private static String OTHER_STRING_1_ND_RESULT = LRE + \"*\" + PDF + \".\" + LRM + LRE + \"java\" + PDF;\n" +
			"	private static String OTHER_STRING_2_ND_RESULT = LRE + \"*\" + PDF + \".\" + LRM + LRE + \"\\u05d0\\u05d1\\u05d2\" + PDF;\n" +
			"\n" +
			"	private static String[] RESULT_DEFAULT_PATHS = {PATH_1_RESULT, PATH_2_RESULT, PATH_3_RESULT, PATH_4_RESULT, PATH_5_RESULT, PATH_6_RESULT, PATH_7_RESULT, PATH_8_RESULT, PATH_9_RESULT, PATH_10_RESULT, PATH_11_RESULT, PATH_12_RESULT, PATH_13_RESULT};\n" +
			"\n" +
			"	private static String[] RESULT_STAR_PATHS = {OTHER_STRING_1_RESULT, OTHER_STRING_2_RESULT};\n" +
			"	private static String[] RESULT_EQUALS_PATHS = {OTHER_STRING_3_RESULT};\n" +
			"	private static String[] RESULT_STAR_PATHS_ND = {OTHER_STRING_1_ND_RESULT, OTHER_STRING_2_ND_RESULT};\n" +
			"\n" +
			"	/**\n" +
			"	 * Constructor.\n" +
			"	 * \n" +
			"	 * @param name test name\n" +
			"	 */\n" +
			"	public X(String name) {\n" +
			"	}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n"
		},
		"SUCCESS");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124099
// Undue partial reset of receiver in
// UnconditionalFlowInfo#addInitializationsFrom.
public void test014() {
	this.runConformTest(new String[] {
		"X.java",
			"class X {\n" +
			"    int      i01, i02, i03, i04, i05, i06, i07, i08, i09,\n" +
			"        i10, i11, i12, i13, i14, i15, i16, i17, i18, i19,\n" +
			"        i20, i21, i22, i23, i24, i25, i26, i27, i28, i29,\n" +
			"        i30, i31, i32, i33, i34, i35, i36, i37, i38, i39,\n" +
			"        i40, i41, i42, i43, i44, i45, i46, i47, i48, i49,\n" +
			"        i50, i51, i52, i53, i54, i55, i56, i57, i58, i59,\n" +
			"        i60, i61, i62, i63,    i64, i65 = 1;\n" +
			"public X() {\n" +
			"    new Object() {\n" +
			"        int      \n" +
			"            k01, k02, k03, k04, k05, k06, k07, k08, k09,\n" +
			"            k10, k11, k12, k13, k14, k15, k16, k17, k18, k19,\n" +
			"            k20, k21, k22, k23, k24, k25, k26, k27, k28, k29,\n" +
			"            k30, k31, k32, k33, k34, k35, k36, k37, k38, k39,\n" +
			"            k40, k41, k42, k43, k44, k45, k46, k47, k48, k49,\n" +
			"            k50, k51, k52, k53, k54, k55, k56, k57, k58, k59,\n" +
			"            k60, k61, k62, k63, k64;\n" +
			"        int      \n" +
			"            k101, k102, k103, k104, k105, k106, k107, k108, k109,\n" +
			"            k110, k111, k112, k113, k114, k115, k116, k117, k118, k119,\n" +
			"            k120, k121, k122, k123, k124, k125, k126, k127, k128, k129,\n" +
			"            k130, k131, k132, k133, k134, k135, k136, k137, k138, k139,\n" +
			"            k140, k141, k142, k143, k144, k145, k146, k147, k148, k149,\n" +
			"            k150, k151, k152, k153, k154, k155, k156, k157, k158, k159,\n" +
			"            k160, k161, k162, k163, k164;\n" +
			"        final int l = 1;\n" +
			"        public int hashCode() {\n" +
			"            return\n" +
			"                k01 + k02 + k03 + k04 + k05 + k06 + k07 + k08 + k09 +\n" +
			"                k10 + k11 + k12 + k13 + k14 + k15 + k16 + k17 + k18 + k19 +\n" +
			"                k20 + k21 + k22 + k23 + k24 + k25 + k26 + k27 + k28 + k29 +\n" +
			"                k30 + k31 + k32 + k33 + k34 + k35 + k36 + k37 + k38 + k39 +\n" +
			"                k40 + k41 + k42 + k43 + k44 + k45 + k46 + k47 + k48 + k49 +\n" +
			"                k50 + k51 + k52 + k53 + k54 + k55 + k56 + k57 + k58 + k59 +\n" +
			"                k60 + k61 + k62 + k63 + k64 +\n" +
			"                k101 + k102 + k103 + k104 + k105 + k106 + k107 + k108 + k109 +\n" +
			"                k110 + k111 + k112 + k113 + k114 + k115 + k116 + k117 + k118 + k119 +\n" +
			"                k120 + k121 + k122 + k123 + k124 + k125 + k126 + k127 + k128 + k129 +\n" +
			"                k130 + k131 + k132 + k133 + k134 + k135 + k136 + k137 + k138 + k139 +\n" +
			"                k140 + k141 + k142 + k143 + k144 + k145 + k146 + k147 + k148 + k149 +\n" +
			"                k150 + k151 + k152 + k153 + k154 + k155 + k156 + k157 + k158 + k159 +\n" +
			"                k160 + k161 + k162 + k163 + k164 +\n" +
			"                l;\n" +
			"        }\n" +
			"    };\n" +
			"}\n" +
			"\n" +
			"}\n" +
			"\n",
	},
	"");
}
public void _test015() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_ShareCommonFinallyBlocks, CompilerOptions.ENABLED);

	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	public class X {
		public static int foo(int i) {
			try {
				switch(i) {
	""");
	for (int i = 0; i < 4000; i ++) {
		fileContents.append("				case " + i + " :\n");
		fileContents.append("					return 3;\n");
	}
	fileContents.append("""
					default:
						return -1;
				}
			} catch(Exception e) {
				//ignore
			} finally {
				System.out.println(\"Enter finally block\");
				System.out.println(\"Inside finally block\");
				System.out.println(\"Leave finally block\");
			}
			return -1;
		}
		public static void main(String[] args) {
			System.out.println(foo(1));
		}
	}
	""");

	runConformTest(
		true,
		new String[] {
		"X.java",
		fileContents.toString()},
		null,
		settings,
		null,
		"Enter finally block\n" +
		"Inside finally block\n" +
		"Leave finally block\n" +
		"3",
		null,
		JavacTestOptions.EclipseJustification.EclipseBug169017);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=350095
public void test0016() {
	// only run in 1.5 or above
	StringBuilder buffer = new StringBuilder();
	buffer
		.append("0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119")
		.append("1201211221231241251261271281291301311321331341351361371381391401411421431441451461471481491501511521531541551561571581591601611621631641651661671681691701711721731741751761771781791801811821831841851861871881891901911921931941951961971981992002012022")
		.append("0320420520620720820921021121221321421521621721821922022122222322422522622722822923023123223323423523623723823924024124224324424524624724824925025125225325425525625725825926026126226326426526626726826927027127227327427527627727827928028128228328428528")
		.append("6287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369")
		.append("3703713723733743753763773783793803813823833843853863873883893903913923933943953963973983994004014024034044054064074084094104114124134144154164174184194204214224234244254264274284294304314324334344354364374384394404414424434444454464474484494504514524")
		.append("5345445545645745845946046146246346446546646746846947047147247347447547647747847948048148248348448548648748848949049149249349449549649749849950050150250350450550650750850951051151251351451551651751851952052152252352452552652752852953053153253353453553")
		.append("6537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619")
		.append("6206216226236246256266276286296306316326336346356366376386396406416426436446456466476486496506516526536546556566576586596606616626636646656666676686696706716726736746756766776786796806816826836846856866876886896906916926936946956966976986997007017027")
		.append("0370470570670770870971071171271371471571671771871972072172272372472572672772872973073173273373473573673773873974074174274374474574674774874975075175275375475575675775875976076176276376476576676776876977077177277377477577677777877978078178278378478578")
		.append("6787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869")
		.append("8708718728738748758768778788798808818828838848858868878888898908918928938948958968978988999009019029039049059069079089099109119129139149159169179189199209219229239249259269279289299309319329339349359369379389399409419429439449459469479489499509519529")
		.append("5395495595695795895996096196296396496596696796896997097197297397497597697797897998098198298398498598698798898999099199299399499599699799899910001001100210031004100510061007100810091010101110121013101410151016101710181019102010211022102310241025102610")
		.append("2710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089")
		.append("1090109110921093109410951096109710981099110011011102110311041105110611071108110911101111111211131114111511161117111811191120112111221123112411251126112711281129113011311132113311341135113611371138113911401141114211431144114511461147114811491150115111")
		.append("5211531154115511561157115811591160116111621163116411651166116711681169117011711172117311741175117611771178117911801181118211831184118511861187118811891190119111921193119411951196119711981199120012011202120312041205120612071208120912101211121212131214")
		.append("1215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612")
		.append("7712781279128012811282128312841285128612871288128912901291129212931294129512961297129812991300130113021303130413051306130713081309131013111312131313141315131613171318131913201321132213231324132513261327132813291330133113321333133413351336133713381339")
		.append("1340134113421343134413451346134713481349135013511352135313541355135613571358135913601361136213631364136513661367136813691370137113721373137413751376137713781379138013811382138313841385138613871388138913901391139213931394139513961397139813991400140114")
		.append("0214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464")
		.append("1465146614671468146914701471147214731474147514761477147814791480148114821483148414851486148714881489149014911492149314941495149614971498149915001501150215031504150515061507150815091510151115121513151415151516151715181519152015211522152315241525152615")
		.append("2715281529153015311532153315341535153615371538153915401541154215431544154515461547154815491550155115521553155415551556155715581559156015611562156315641565156615671568156915701571157215731574157515761577157815791580158115821583158415851586158715881589")
		.append("1590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116")
		.append("5216531654165516561657165816591660166116621663166416651666166716681669167016711672167316741675167616771678167916801681168216831684168516861687168816891690169116921693169416951696169716981699170017011702170317041705170617071708170917101711171217131714")
		.append("1715171617171718171917201721172217231724172517261727172817291730173117321733173417351736173717381739174017411742174317441745174617471748174917501751175217531754175517561757175817591760176117621763176417651766176717681769177017711772177317741775177617")
		.append("7717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839")
		.append("1840184118421843184418451846184718481849185018511852185318541855185618571858185918601861186218631864186518661867186818691870187118721873187418751876187718781879188018811882188318841885188618871888188918901891189218931894189518961897189818991900190119")
		.append("0219031904190519061907190819091910191119121913191419151916191719181919192019211922192319241925192619271928192919301931193219331934193519361937193819391940194119421943194419451946194719481949195019511952195319541955195619571958195919601961196219631964")
		.append("1965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620")
		.append("2720282029203020312032203320342035203620372038203920402041204220432044204520462047204820492050205120522053205420552056205720582059206020612062206320642065206620672068206920702071207220732074207520762077207820792080208120822083208420852086208720882089")
		.append("2090209120922093209420952096209720982099210021012102210321042105210621072108210921102111211221132114211521162117211821192120212121222123212421252126212721282129213021312132213321342135213621372138213921402141214221432144214521462147214821492150215121")
		.append("5221532154215521562157215821592160216121622163216421652166216721682169217021712172217321742175217621772178217921802181218221832184218521862187218821892190219121922193219421952196219721982199220022012202220322042205220622072208220922102211221222132214")
		.append("2215221622172218221922202221222222232224222522262227222822292230223122322233223422352236223722382239224022412242224322442245224622472248224922502251225222532254225522562257225822592260226122622263226422652266226722682269227022712272227322742275227622")
		.append("7722782279228022812282228322842285228622872288228922902291229222932294229522962297229822992300230123022303230423052306230723082309231023112312231323142315231623172318231923202321232223232324232523262327232823292330233123322333233423352336233723382339")
		.append("2340234123422343234423452346234723482349235023512352235323542355235623572358235923602361236223632364236523662367236823692370237123722373237423752376237723782379238023812382238323842385238623872388238923902391239223932394239523962397239823992400240124")
		.append("0224032404240524062407240824092410241124122413241424152416241724182419242024212422242324242425242624272428242924302431243224332434243524362437243824392440244124422443244424452446244724482449245024512452245324542455245624572458245924602461246224632464")
		.append("246524662467246824692470247124722473247424752476247724782479248024812482248324842485248624872488248924902491249224932494249524962497249824992500");

	StringBuilder fileContents = new StringBuilder();
	fileContents.append("public enum X {\n");
	for (int i = 0; i < 2500; i++) {
		fileContents.append("\tX" + i + "(" + i + "),\n");
	}
	fileContents.append("""
		;

		private int value;
		X(int i) {
			this.value = i;
		}

		public static void main(String[] args) {
			int i = 0;
			for (X x : X.values()) {
				i++;
				System.out.print(x);
			}
			System.out.print(i);
		}

		public String toString() {
			return Integer.toString(this.value);
		}
	}
	""");
	String[] src = new String[] { "X.java", fileContents.toString()};

	if (this.complianceLevel < ClassFileConstants.JDK9) {
		this.runConformTest(src, buffer.toString());
	} else {
		this.runNegativeTest(src,
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public enum X {\n" +
			"	            ^\n" +
			"The code for the static initializer is exceeding the 65535 bytes limit\n" +
			"----------\n");
	}
}
public void test0017() {
	// only run in 1.5 or above
	StringBuilder buffer = new StringBuilder();
	buffer
		.append("123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119")
		.append("1201211221231241251261271281291301311321331341351361371381391401411421431441451461471481491501511521531541551561571581591601611621631641651661671681691701711721731741751761771781791801811821831841851861871881891901911921931941951961971981992002012022")
		.append("0320420520620720820921021121221321421521621721821922022122222322422522622722822923023123223323423523623723823924024124224324424524624724824925025125225325425525625725825926026126226326426526626726826927027127227327427527627727827928028128228328428528")
		.append("6287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369")
		.append("3703713723733743753763773783793803813823833843853863873883893903913923933943953963973983994004014024034044054064074084094104114124134144154164174184194204214224234244254264274284294304314324334344354364374384394404414424434444454464474484494504514524")
		.append("5345445545645745845946046146246346446546646746846947047147247347447547647747847948048148248348448548648748848949049149249349449549649749849950050150250350450550650750850951051151251351451551651751851952052152252352452552652752852953053153253353453553")
		.append("6537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619")
		.append("6206216226236246256266276286296306316326336346356366376386396406416426436446456466476486496506516526536546556566576586596606616626636646656666676686696706716726736746756766776786796806816826836846856866876886896906916926936946956966976986997007017027")
		.append("0370470570670770870971071171271371471571671771871972072172272372472572672772872973073173273373473573673773873974074174274374474574674774874975075175275375475575675775875976076176276376476576676776876977077177277377477577677777877978078178278378478578")
		.append("6787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869")
		.append("8708718728738748758768778788798808818828838848858868878888898908918928938948958968978988999009019029039049059069079089099109119129139149159169179189199209219229239249259269279289299309319329339349359369379389399409419429439449459469479489499509519529")
		.append("5395495595695795895996096196296396496596696796896997097197297397497597697797897998098198298398498598698798898999099199299399499599699799899910001001100210031004100510061007100810091010101110121013101410151016101710181019102010211022102310241025102610")
		.append("2710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089")
		.append("1090109110921093109410951096109710981099110011011102110311041105110611071108110911101111111211131114111511161117111811191120112111221123112411251126112711281129113011311132113311341135113611371138113911401141114211431144114511461147114811491150115111")
		.append("5211531154115511561157115811591160116111621163116411651166116711681169117011711172117311741175117611771178117911801181118211831184118511861187118811891190119111921193119411951196119711981199120012011202120312041205120612071208120912101211121212131214")
		.append("1215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612")
		.append("7712781279128012811282128312841285128612871288128912901291129212931294129512961297129812991300130113021303130413051306130713081309131013111312131313141315131613171318131913201321132213231324132513261327132813291330133113321333133413351336133713381339")
		.append("1340134113421343134413451346134713481349135013511352135313541355135613571358135913601361136213631364136513661367136813691370137113721373137413751376137713781379138013811382138313841385138613871388138913901391139213931394139513961397139813991400140114")
		.append("0214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464")
		.append("1465146614671468146914701471147214731474147514761477147814791480148114821483148414851486148714881489149014911492149314941495149614971498149915001501150215031504150515061507150815091510151115121513151415151516151715181519152015211522152315241525152615")
		.append("2715281529153015311532153315341535153615371538153915401541154215431544154515461547154815491550155115521553155415551556155715581559156015611562156315641565156615671568156915701571157215731574157515761577157815791580158115821583158415851586158715881589")
		.append("1590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116")
		.append("5216531654165516561657165816591660166116621663166416651666166716681669167016711672167316741675167616771678167916801681168216831684168516861687168816891690169116921693169416951696169716981699170017011702170317041705170617071708170917101711171217131714")
		.append("1715171617171718171917201721172217231724172517261727172817291730173117321733173417351736173717381739174017411742174317441745174617471748174917501751175217531754175517561757175817591760176117621763176417651766176717681769177017711772177317741775177617")
		.append("7717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839")
		.append("1840184118421843184418451846184718481849185018511852185318541855185618571858185918601861186218631864186518661867186818691870187118721873187418751876187718781879188018811882188318841885188618871888188918901891189218931894189518961897189818991900190119")
		.append("0219031904190519061907190819091910191119121913191419151916191719181919192019211922192319241925192619271928192919301931193219331934193519361937193819391940194119421943194419451946194719481949195019511952195319541955195619571958195919601961196219631964")
		.append("19651966196719681969197019711972197319741975197619771978197919801981198219831984198519861987198819891990199119921993199419951996199719981999200020012001");

	StringBuilder fileContents = new StringBuilder();
	fileContents.append("public enum X {\n");
	for (int i = 1; i < 2002; i++) {
		fileContents.append("\tX" + i + "(" + i + "),\n");
	}
	fileContents.append("""
		;

		private int value;
		X(int i) {
			this.value = i;
		}

		public static void main(String[] args) {
			int i = 0;
			for (X x : X.values()) {
				i++;
				System.out.print(x);
			}
			System.out.print(i);
		}

		public String toString() {
			return Integer.toString(this.value);
		}
	}
	""");


	this.runConformTest(
		new String[] {
			"X.java",
			fileContents.toString()
		},
		buffer.toString());
}
public void test0018() {
	// only run in 1.5 or above
	StringBuilder buffer = new StringBuilder();
	buffer
		.append("123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119")
		.append("1201211221231241251261271281291301311321331341351361371381391401411421431441451461471481491501511521531541551561571581591601611621631641651661671681691701711721731741751761771781791801811821831841851861871881891901911921931941951961971981992002012022")
		.append("0320420520620720820921021121221321421521621721821922022122222322422522622722822923023123223323423523623723823924024124224324424524624724824925025125225325425525625725825926026126226326426526626726826927027127227327427527627727827928028128228328428528")
		.append("6287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369")
		.append("3703713723733743753763773783793803813823833843853863873883893903913923933943953963973983994004014024034044054064074084094104114124134144154164174184194204214224234244254264274284294304314324334344354364374384394404414424434444454464474484494504514524")
		.append("5345445545645745845946046146246346446546646746846947047147247347447547647747847948048148248348448548648748848949049149249349449549649749849950050150250350450550650750850951051151251351451551651751851952052152252352452552652752852953053153253353453553")
		.append("6537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619")
		.append("6206216226236246256266276286296306316326336346356366376386396406416426436446456466476486496506516526536546556566576586596606616626636646656666676686696706716726736746756766776786796806816826836846856866876886896906916926936946956966976986997007017027")
		.append("0370470570670770870971071171271371471571671771871972072172272372472572672772872973073173273373473573673773873974074174274374474574674774874975075175275375475575675775875976076176276376476576676776876977077177277377477577677777877978078178278378478578")
		.append("6787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869")
		.append("8708718728738748758768778788798808818828838848858868878888898908918928938948958968978988999009019029039049059069079089099109119129139149159169179189199209219229239249259269279289299309319329339349359369379389399409419429439449459469479489499509519529")
		.append("5395495595695795895996096196296396496596696796896997097197297397497597697797897998098198298398498598698798898999099199299399499599699799899910001001100210031004100510061007100810091010101110121013101410151016101710181019102010211022102310241025102610")
		.append("2710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089")
		.append("1090109110921093109410951096109710981099110011011102110311041105110611071108110911101111111211131114111511161117111811191120112111221123112411251126112711281129113011311132113311341135113611371138113911401141114211431144114511461147114811491150115111")
		.append("5211531154115511561157115811591160116111621163116411651166116711681169117011711172117311741175117611771178117911801181118211831184118511861187118811891190119111921193119411951196119711981199120012011202120312041205120612071208120912101211121212131214")
		.append("1215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612")
		.append("7712781279128012811282128312841285128612871288128912901291129212931294129512961297129812991300130113021303130413051306130713081309131013111312131313141315131613171318131913201321132213231324132513261327132813291330133113321333133413351336133713381339")
		.append("1340134113421343134413451346134713481349135013511352135313541355135613571358135913601361136213631364136513661367136813691370137113721373137413751376137713781379138013811382138313841385138613871388138913901391139213931394139513961397139813991400140114")
		.append("0214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464")
		.append("1465146614671468146914701471147214731474147514761477147814791480148114821483148414851486148714881489149014911492149314941495149614971498149915001501150215031504150515061507150815091510151115121513151415151516151715181519152015211522152315241525152615")
		.append("2715281529153015311532153315341535153615371538153915401541154215431544154515461547154815491550155115521553155415551556155715581559156015611562156315641565156615671568156915701571157215731574157515761577157815791580158115821583158415851586158715881589")
		.append("1590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116")
		.append("5216531654165516561657165816591660166116621663166416651666166716681669167016711672167316741675167616771678167916801681168216831684168516861687168816891690169116921693169416951696169716981699170017011702170317041705170617071708170917101711171217131714")
		.append("1715171617171718171917201721172217231724172517261727172817291730173117321733173417351736173717381739174017411742174317441745174617471748174917501751175217531754175517561757175817591760176117621763176417651766176717681769177017711772177317741775177617")
		.append("7717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839")
		.append("1840184118421843184418451846184718481849185018511852185318541855185618571858185918601861186218631864186518661867186818691870187118721873187418751876187718781879188018811882188318841885188618871888188918901891189218931894189518961897189818991900190119")
		.append("0219031904190519061907190819091910191119121913191419151916191719181919192019211922192319241925192619271928192919301931193219331934193519361937193819391940194119421943194419451946194719481949195019511952195319541955195619571958195919601961196219631964")
		.append("196519661967196819691970197119721973197419751976197719781979198019811982198319841985198619871988198919901991199219931994199519961997199819992000200120022002");
	StringBuilder fileContents = new StringBuilder();
	fileContents.append("public enum X {\n");
	for (int i = 1; i < 2003; i++) {
		fileContents.append("\tX" + i + "(" + i + "),\n");
	}
	fileContents.append("""
		;

		private int value;
		X(int i) {
			this.value = i;
		}

		public static void main(String[] args) {
			int i = 0;
			for (X x : X.values()) {
				i++;
				System.out.print(x);
			}
			System.out.print(i);
		}

		public String toString() {
			return Integer.toString(this.value);
		}
	}
	""");
	this.runConformTest(
		new String[] {
			"X.java",
			fileContents.toString()
		},
		buffer.toString());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=393749
public void test0019() {
	// only run in 1.5 or above

	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	import java.util.HashMap;
	import java.util.Map;

	public enum X {
	""");
	for (int i = 0; i < 2005; i++) {
		fileContents.append("\tC" + i + ",\n");
	}
	fileContents.append("""
		;

	    private static Map<String, X> nameToInstanceMap = new HashMap<String, X>();

	    static {
	        for (X b : values()) {
	            nameToInstanceMap.put(b.name(), b);
	        }
	    }

	    public static X fromName(String n) {
	        X b = nameToInstanceMap.get(n);

	        return b;
	    }
	    public static void main(String[] args) {
			System.out.println(fromName(\"C0\"));
		}
	}
	""");

	this.runConformTest(
		new String[] {
			"X.java",
			fileContents.toString()
		},
		"C0");
}
public void testBug519070() {
	int N = 1000;
	StringBuilder sourceCode = new StringBuilder(
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n");
	for (int m = 0; m < N; m++) {
		sourceCode.append("\tvoid test"+m+"() {\n");
		for (int i = 0; i < N; i++)
			sourceCode.append("\t\tSystem.out.println(\"xyz\");\n");
		sourceCode.append("\t}\n");
	}
	sourceCode.append("}\n");

	this.runConformTest(
			new String[] {
					"X.java",
					sourceCode.toString()
			},
			"SUCCESS");
}
public void testIssue1164a() throws ClassFormatException, IOException {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return;
	StringBuilder sourceCode = new StringBuilder(
			"public class X {\n" +
			"  void foo(String a, String b, String c, String d, String e, String f) {\n" +
			"    String s = a + (\n");
	for (int i = 0; i < 1000; i++) {
		sourceCode.append(
			"    	\"abcdef\" + a + b + c + d + e + f + " +
			"\" ghijk" +
			"pqrstu\" +\n");
	}
	sourceCode.append(
			"    	\"abcdef\" + a + b + c + d + e + \" ghijk" +
			"abcdefgh" +
			"abcdefgh\");\n" +
			"    }\n" +
			"}");
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			sourceCode.toString()
		},
		null,
		"",
		null,
		JavacTestOptions.JavacHasABug.JavacThrowsAnException /* stack overflow */); // transient, platform-dependent
	String expectedOutput =
			"  void foo(String a, String b, String c, String d, String e, String f);\n"
			+ "       0  aload_1 [a]\n"
			+ "       1  aload_1 [a]\n"
			+ "       2  aload_2 [b]\n"
			+ "       3  aload_3 [c]\n"
			+ "       4  aload 4 [d]\n"
			+ "       6  aload 5 [e]\n"
			+ "       8  aload 6 [f]\n"
			+ "      10  aload_1 [a]\n"
			+ "      11  aload_2 [b]\n"
			+ "      12  aload_3 [c]\n"
			+ "      13  aload 4 [d]\n"
			+ "      15  aload 5 [e]\n"
			+ "      17  aload 6 [f]\n"
			+ "      19  aload_1 [a]\n"
			+ "      20  aload_2 [b]\n"
			+ "      21  aload_3 [c]\n"
			+ "      22  aload 4 [d]\n"
			+ "      24  aload 5 [e]\n"
			+ "      26  aload 6 [f]\n"
			+ "      28  aload_1 [a]\n"
			+ "      29  aload_2 [b]\n"
			+ "      30  aload_3 [c]\n"
			+ "      31  aload 4 [d]\n"
			+ "      33  aload 5 [e]\n"
			+ "      35  aload 6 [f]\n"
			+ "      37  aload_1 [a]\n"
			+ "      38  aload_2 [b]\n"
			+ "      39  aload_3 [c]\n"
			+ "      40  aload 4 [d]\n"
			+ "      42  aload 5 [e]\n"
			+ "      44  aload 6 [f]\n"
			+ "      46  aload_1 [a]\n"
			+ "      47  aload_2 [b]\n"
			+ "      48  aload_3 [c]\n"
			+ "      49  aload 4 [d]\n"
			+ "      51  aload 5 [e]\n"
			+ "      53  aload 6 [f]\n"
			+ "      55  aload_1 [a]\n"
			+ "      56  aload_2 [b]\n"
			+ "      57  aload_3 [c]\n"
			+ "      58  aload 4 [d]\n"
			+ "      60  aload 5 [e]\n"
			+ "      62  aload 6 [f]\n"
			+ "      64  aload_1 [a]\n"
			+ "      65  aload_2 [b]\n"
			+ "      66  aload_3 [c]\n"
			+ "      67  aload 4 [d]\n"
			+ "      69  aload 5 [e]\n"
			+ "      71  aload 6 [f]\n"
			+ "      73  aload_1 [a]\n"
			+ "      74  aload_2 [b]\n"
			+ "      75  aload_3 [c]\n"
			+ "      76  aload 4 [d]\n"
			+ "      78  aload 5 [e]\n"
			+ "      80  aload 6 [f]\n"
			+ "      82  aload_1 [a]\n"
			+ "      83  aload_2 [b]\n"
			+ "      84  aload_3 [c]\n"
			+ "      85  aload 4 [d]\n"
			+ "      87  aload 5 [e]\n"
			+ "      89  aload 6 [f]\n"
			+ "      91  aload_1 [a]\n"
			+ "      92  aload_2 [b]\n"
			+ "      93  aload_3 [c]\n"
			+ "      94  aload 4 [d]\n"
			+ "      96  aload 5 [e]\n"
			+ "      98  aload 6 [f]\n"
			+ "     100  aload_1 [a]\n"
			+ "     101  aload_2 [b]\n"
			+ "     102  aload_3 [c]\n"
			+ "     103  aload 4 [d]\n"
			+ "     105  aload 5 [e]\n"
			+ "     107  aload 6 [f]\n"
			+ "     109  aload_1 [a]\n"
			+ "     110  aload_2 [b]\n"
			+ "     111  aload_3 [c]\n"
			+ "     112  aload 4 [d]\n"
			+ "     114  aload 5 [e]\n"
			+ "     116  aload 6 [f]\n"
			+ "     118  aload_1 [a]\n"
			+ "     119  aload_2 [b]\n"
			+ "     120  aload_3 [c]\n"
			+ "     121  aload 4 [d]\n"
			+ "     123  aload 5 [e]\n"
			+ "     125  aload 6 [f]\n"
			+ "     127  aload_1 [a]\n"
			+ "     128  aload_2 [b]\n"
			+ "     129  aload_3 [c]\n"
			+ "     130  aload 4 [d]\n"
			+ "     132  aload 5 [e]\n"
			+ "     134  aload 6 [f]\n"
			+ "     136  aload_1 [a]\n"
			+ "     137  aload_2 [b]\n"
			+ "     138  aload_3 [c]\n"
			+ "     139  aload 4 [d]\n"
			+ "     141  aload 5 [e]\n"
			+ "     143  aload 6 [f]\n"
			+ "     145  aload_1 [a]\n"
			+ "     146  aload_2 [b]\n"
			+ "     147  aload_3 [c]\n"
			+ "     148  aload 4 [d]\n"
			+ "     150  aload 5 [e]\n"
			+ "     152  aload 6 [f]\n"
			+ "     154  aload_1 [a]\n"
			+ "     155  aload_2 [b]\n"
			+ "     156  aload_3 [c]\n"
			+ "     157  aload 4 [d]\n"
			+ "     159  aload 5 [e]\n"
			+ "     161  aload 6 [f]\n"
			+ "     163  aload_1 [a]\n"
			+ "     164  aload_2 [b]\n"
			+ "     165  aload_3 [c]\n"
			+ "     166  aload 4 [d]\n"
			+ "     168  aload 5 [e]\n"
			+ "     170  aload 6 [f]\n"
			+ "     172  aload_1 [a]\n"
			+ "     173  aload_2 [b]\n"
			+ "     174  aload_3 [c]\n"
			+ "     175  aload 4 [d]\n"
			+ "     177  aload 5 [e]\n"
			+ "     179  aload 6 [f]\n"
			+ "     181  aload_1 [a]\n"
			+ "     182  aload_2 [b]\n"
			+ "     183  aload_3 [c]\n"
			+ "     184  aload 4 [d]\n"
			+ "     186  aload 5 [e]\n"
			+ "     188  aload 6 [f]\n"
			+ "     190  aload_1 [a]\n"
			+ "     191  aload_2 [b]\n"
			+ "     192  aload_3 [c]\n"
			+ "     193  aload 4 [d]\n"
			+ "     195  aload 5 [e]\n"
			+ "     197  aload 6 [f]\n"
			+ "     199  aload_1 [a]\n"
			+ "     200  aload_2 [b]\n"
			+ "     201  aload_3 [c]\n"
			+ "     202  aload 4 [d]\n"
			+ "     204  aload 5 [e]\n"
			+ "     206  aload 6 [f]\n"
			+ "     208  aload_1 [a]\n"
			+ "     209  aload_2 [b]\n"
			+ "     210  aload_3 [c]\n"
			+ "     211  aload 4 [d]\n"
			+ "     213  aload 5 [e]\n"
			+ "     215  aload 6 [f]\n"
			+ "     217  aload_1 [a]\n"
			+ "     218  aload_2 [b]\n"
			+ "     219  aload_3 [c]\n"
			+ "     220  aload 4 [d]\n"
			+ "     222  aload 5 [e]\n"
			+ "     224  aload 6 [f]\n"
			+ "     226  aload_1 [a]\n"
			+ "     227  aload_2 [b]\n"
			+ "     228  aload_3 [c]\n"
			+ "     229  aload 4 [d]\n"
			+ "     231  aload 5 [e]\n"
			+ "     233  aload 6 [f]\n"
			+ "     235  aload_1 [a]\n"
			+ "     236  aload_2 [b]\n"
			+ "     237  aload_3 [c]\n"
			+ "     238  aload 4 [d]\n"
			+ "     240  aload 5 [e]\n"
			+ "     242  aload 6 [f]\n"
			+ "     244  aload_1 [a]\n"
			+ "     245  aload_2 [b]\n"
			+ "     246  aload_3 [c]\n"
			+ "     247  aload 4 [d]\n"
			+ "     249  aload 5 [e]\n"
			+ "     251  aload 6 [f]\n"
			+ "     253  aload_1 [a]\n"
			+ "     254  aload_2 [b]\n"
			+ "     255  aload_3 [c]\n"
			+ "     256  aload 4 [d]\n"
			+ "     258  aload 5 [e]\n"
			+ "     260  aload 6 [f]\n"
			+ "     262  aload_1 [a]\n"
			+ "     263  aload_2 [b]\n"
			+ "     264  aload_3 [c]\n"
			+ "     265  aload 4 [d]\n"
			+ "     267  aload 5 [e]\n"
			+ "     269  aload 6 [f]\n"
			+ "     271  aload_1 [a]\n"
			+ "     272  aload_2 [b]\n"
			+ "     273  aload_3 [c]\n"
			+ "     274  aload 4 [d]\n"
			+ "     276  aload 5 [e]\n"
			+ "     278  aload 6 [f]\n"
			+ "     280  aload_1 [a]\n"
			+ "     281  aload_2 [b]\n"
			+ "     282  aload_3 [c]\n"
			+ "     283  aload 4 [d]\n"
			+ "     285  invokedynamic 0 makeConcatWithConstants(String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String) : String [16]\n";
	checkClassFile("X", sourceCode.toString(), expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	expectedOutput = "  31 : # 69 invokestatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#78  ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkpqrstuabcdef ghijkabcdefghabcdefgh\n" +
			"}";
	checkClassFile("X", sourceCode.toString(), expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
}
public void testIssue1164b() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return;
	StringBuilder sourceCode = new StringBuilder(
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    (new X()).foo(\"a\", \"b\", \"c\", \"d\", \"e\", \"fa\");\n" +
			"  }\n" +
			"  void foo(String a, String b, String c, String d, String e, String f) {\n" +
			"    String s = a + (\n");
	for (int i = 0; i < 200; i++) {
		sourceCode.append(
			"    	\"abcdef\" + a + b + c + d + e + f + " +
			"\" ghijk" +
			"pqrstu\" +\n");
	}
	sourceCode.append(
			"    \"abcdef\" + a + b + c + d + e + \" ghijk" +
			"abcdefgh" +
			"abcdefgh\");\n" +
			"  System.out.println(s);\n" +
			"    }\n" +
			"}");
	String output = "aabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa ghijkpqrstuabcdefabcdefa "
			+ "ghijkpqrstuabcdefabcde ghijkabcdefghabcdefgh";
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			sourceCode.toString()
		},
		null,
		output,
		null,
		JavacTestOptions.JavacHasABug.JavacThrowsAnException /* stack overflow */); // transient, platform-dependent
}
public void testIssue1359() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return;

	StringBuilder fileContents = new StringBuilder();
	fileContents.append("""
	public class X
	{
		public static void main(String[] args)
		{
			System.out.println(test(2));
		}

		private static int test(long l)
		{
			// Each line: 128 (2^7) longs = 256 (2^8) stack elements,
			// each block: 16 (2^4) lines = 4096 (2^12) stack elements,
			// each superblock: 4 (2^2) blocks = 16384 (2^14) stack elements.
			// So, to reach 65536 (2^16), we need 4 (2^2) superblocks.
			// One of the longs is absent, so we are at 65534 elements,
			// and the "innermost" int 0 is the 65535th element.
			// When the "0 +" before the huge expression is present, that int 0 is the 65536th element.
			return 0 + (
			//@formatter:off
					        methodWithManyArguments(l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,
	""");
	for (int i = 0; i < 4; i++) {
		for (int j = 0; j < 4; j++) {
			for (int k = 0; k < 16; k++) {
				fileContents.append("(int)(l+methodWithManyArguments(l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,l,\n");
			}
			fileContents.append("\n");
		}
		fileContents.append("// --- SUPERBLOCK ---\n");
		fileContents.append("\n");
	}
	fileContents.append("""
					0

					)))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))) ))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))
					)))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))) ))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))
					)))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))) ))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))
					)))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))) )))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))
					//@formatter:on
			);
		}

		private static int methodWithManyArguments(
				long p00, long p01, long p02, long p03, long p04, long p05, long p06, long p07, long p08, long p09, long p0a, long p0b, long p0c, long p0d, long p0e, long p0f,
				long p10, long p11, long p12, long p13, long p14, long p15, long p16, long p17, long p18, long p19, long p1a, long p1b, long p1c, long p1d, long p1e, long p1f,
				long p20, long p21, long p22, long p23, long p24, long p25, long p26, long p27, long p28, long p29, long p2a, long p2b, long p2c, long p2d, long p2e, long p2f,
				long p30, long p31, long p32, long p33, long p34, long p35, long p36, long p37, long p38, long p39, long p3a, long p3b, long p3c, long p3d, long p3e, long p3f,
				long p40, long p41, long p42, long p43, long p44, long p45, long p46, long p47, long p48, long p49, long p4a, long p4b, long p4c, long p4d, long p4e, long p4f,
				long p50, long p51, long p52, long p53, long p54, long p55, long p56, long p57, long p58, long p59, long p5a, long p5b, long p5c, long p5d, long p5e, long p5f,
				long p60, long p61, long p62, long p63, long p64, long p65, long p66, long p67, long p68, long p69, long p6a, long p6b, long p6c, long p6d, long p6e, long p6f,
				long p70, long p71, long p72, long p73, long p74, long p75, long p76, long p77, long p78, long p79, long p7a, long p7b, long p7c, long p7d, long p7e, int p7f)
		{
			return (int) (0 +
					p00 + p01 + p02 + p03 + p04 + p05 + p06 + p07 + p08 + p09 + p0a + p0b + p0c + p0d + p0e + p0f +
					p10 + p11 + p12 + p13 + p14 + p15 + p16 + p17 + p18 + p19 + p1a + p1b + p1c + p1d + p1e + p1f +
					p20 + p21 + p22 + p23 + p24 + p25 + p26 + p27 + p28 + p29 + p2a + p2b + p2c + p2d + p2e + p2f +
					p30 + p31 + p32 + p33 + p34 + p35 + p36 + p37 + p38 + p39 + p3a + p3b + p3c + p3d + p3e + p3f +
					p40 + p41 + p42 + p43 + p44 + p45 + p46 + p47 + p48 + p49 + p4a + p4b + p4c + p4d + p4e + p4f +
					p50 + p51 + p52 + p53 + p54 + p55 + p56 + p57 + p58 + p59 + p5a + p5b + p5c + p5d + p5e + p5f +
					p60 + p61 + p62 + p63 + p64 + p65 + p66 + p67 + p68 + p69 + p6a + p6b + p6c + p6d + p6e + p6f +
					p70 + p71 + p72 + p73 + p74 + p75 + p76 + p77 + p78 + p79 + p7a + p7b + p7c + p7d + p7e + p7f +
					0);
		}
	}
	""");

	this.runNegativeTest(
			new String[] {
				"X.java",
				fileContents.toString()
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	private static int test(long l)\n" +
			"	                   ^^^^^^^^^^^^\n" +
			"The operand stack is exceeding the 65535 bytes limit\n" +
			"----------\n");
}
public static Class testClass() {
	return XLargeTest.class;
}
}
