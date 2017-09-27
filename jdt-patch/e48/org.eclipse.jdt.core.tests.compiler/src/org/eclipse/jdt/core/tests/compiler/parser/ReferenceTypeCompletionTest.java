/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

/**
 * Completion is expected to be a ReferenceType.
 */
public class ReferenceTypeCompletionTest extends AbstractCompletionTest {
public ReferenceTypeCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(ReferenceTypeCompletionTest.class);
}
/*
 * Regression test for 1FTZCIG.
 */
public void test1FTZCIG() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new X() {							\n" +
		"			protected void bar() {			\n" +
		"			}								\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"p",
		// expectedCompletionNodeToString:
		"<CompleteOnType:p>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new X() {\n" +
		"      <CompleteOnType:p>;\n" +
		"      void bar() {\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"p",
		// expectedReplacedSource:
		"protected",
		// test name
		"<1FTZCIG>"
	);
}
/*
 * Block ::= OpenBlock '{' <BlockStatementsopt> '}'
 */
public void testBlock() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		try {								\n" +
		"			Xxx o = new Y();				\n" +
		"		} catch (Exception e) {				\n"	+
		"		}									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:X>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on block>"
	);
}
/*
 * BlockStatements ::= BlockStatements <BlockStatement>
 */
public void testBlockStatements() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		Xxx o = new Y();					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    <CompleteOnName:X>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on block statements>"
	);
}
/*
 * CatchClause ::= 'catch' '(' <FormalParameter> ')' Block
 */
public void testCatchClause1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		try {								\n" +
		"			fred();							\n" +
		"		} catch (Xxx e) {					\n"	+
		"		}									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    try\n" +
		"      {\n" +
		"        fred();\n" +
		"      }\n" +
		"    catch (<CompleteOnException:X>  )\n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on catch clause 1>"
	);
}
/*
 * CatchClause ::= 'catch' '(' <FormalParameter> ')' Block
 */
public void testCatchClause2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		try {								\n" +
		"			fred();							\n" +
		"		} catch (final Xxx e) {				\n"	+
		"		}									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    try\n" +
		"      {\n" +
		"        fred();\n" +
		"      }\n" +
		"    catch (<CompleteOnException:X>  )\n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on catch clause 2>"
	);
}
/*
 * CatchClause ::= 'catch' '(' <FormalParameter> ')' Block
 */
public void testCatchClause3() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		try {								\n" +
		"			fred();							\n" +
		"		} catch (x.y.Xxx e) {				\n"	+
		"		}									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"x.y.X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:x.y.X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    try\n" +
		"      {\n" +
		"        fred();\n" +
		"      }\n" +
		"    catch (<CompleteOnException:x.y.X>  )\n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"x.y.Xxx",
		// test name
		"<complete on catch clause 3>"
	);
}
/*
 * ClassBody ::= '{' <ClassBodyDeclarationsopt> '}'
 */
public void testClassBody() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	Xxx foo() {								\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  <CompleteOnType:X>\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on  class body>"
	);
}
/*
 * ClassBodyDeclarations ::= ClassBodyDeclarations <ClassBodyDeclaration>
 */
public void testClassBodyDeclarations() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	int i = 0;								\n" +
		"	Xxx foo() {								\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  int i;\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  <CompleteOnType:X>\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on  class body declarations>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' <ClassType> '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new Xxx().zzz();					\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new <CompleteOnType:X>();\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression 1>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' <ClassType> '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new Y(new Xxx()).zzz();				\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new Y(new <CompleteOnType:X>());\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression 2>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' <ClassType> '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression3() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		new Y(1, true, new Xxx()).zzz();	\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    new Y(1, true, new <CompleteOnType:X>());\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression 3>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' <ClassType> '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression4() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		fred().new Y(new Xxx()).zzz();		\n" +
		"	}										\n" +
		"}\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    fred().new Y(new <CompleteOnType:X>());\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression 4>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Bar {								\n" +
		"	static Bar baz;								\n" +
		"	public class X {							\n" +
		"		void foo() {							\n" +
		"			Bar.baz.new Xxx();					\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"public class Bar {\n" +
		"  public class X {\n" +
		"    public X() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      Bar.baz.new <CompleteOnType:X>();\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar baz;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with name 1>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Bar {								\n" +
		"	static Bar baz;								\n" +
		"	public class X {							\n" +
		"		void foo() {							\n" +
		"			new Y(Bar.baz.new Xxx());			\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"public class Bar {\n" +
		"  public class X {\n" +
		"    public X() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      new Y(Bar.baz.new <CompleteOnType:X>());\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar baz;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with name 2>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName3() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Bar {								\n" +
		"	static Bar baz;								\n" +
		"	public class X {							\n" +
		"		void foo() {							\n" +
		"			new Y(1, true, Bar.baz.new Xxx());	\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"public class Bar {\n" +
		"  public class X {\n" +
		"    public X() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      new Y(1, true, Bar.baz.new <CompleteOnType:X>());\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar baz;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with name 3>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName4() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Bar {								\n" +
		"	static Bar baz;								\n" +
		"	public class X {							\n" +
		"		void foo() {							\n" +
		"			fred().new Y(Bar.baz.new Xxx());		\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"public class Bar {\n" +
		"  public class X {\n" +
		"    public X() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      fred().new Y(Bar.baz.new <CompleteOnType:X>());\n" +
		"    }\n" +
		"  }\n" +
		"  static Bar baz;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with name 4>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary1() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Bar {								\n" +
		"	public class X {							\n" +
		"		void foo() {							\n" +
		"			new Bar().new Xxx();				\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"public class Bar {\n" +
		"  public class X {\n" +
		"    public X() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      new Bar().new <CompleteOnType:X>();\n" +
		"    }\n" +
		"  }\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with primary 1>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary2() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Bar {								\n" +
		"	public class X {							\n" +
		"		void foo() {							\n" +
		"			new Y(new Bar().new Xxx());			\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"public class Bar {\n" +
		"  public class X {\n" +
		"    public X() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      new Y(new Bar().new <CompleteOnType:X>());\n" +
		"    }\n" +
		"  }\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with primary 2>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary3() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Bar {								\n" +
		"	public class X {							\n" +
		"		void foo() {							\n" +
		"			fred().new Y(new Bar().new Xxx());	\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"public class Bar {\n" +
		"  public class X {\n" +
		"    public X() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      fred().new Y(new Bar().new <CompleteOnType:X>());\n" +
		"    }\n" +
		"  }\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with primary 3>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary4() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"public class Bar {								\n" +
		"	public class X {							\n" +
		"		void foo() {							\n" +
		"			new Y(1, true, new Bar().new Xxx());\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"public class Bar {\n" +
		"  public class X {\n" +
		"    public X() {\n" +
		"    }\n" +
		"    void foo() {\n" +
		"      new Y(1, true, new Bar().new <CompleteOnType:X>());\n" +
		"    }\n" +
		"  }\n" +
		"  public Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with primary 4>"
	);
}
/*
 * ClassTypeList ::= ClassTypeList ',' <ClassTypeElt>
 */
public void testClassTypeList() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() throws Exception, Xxx {		\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() throws Exception, <CompleteOnException:X> {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class type list>"
	);
}
/*
 * ConstructorBody ::= NestedMethod '{' <BlockStatementsopt> '}'
 */
public void testConstructorBody() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	Bar() {									\n" +
		"		Xxx o = new Y();					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"    super();\n" +
		"    <CompleteOnName:X>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on constructor body>"
	);
}
/*
 * ConstructorDeclarator ::= 'Identifier' '(' <FormalParameterListopt> ')'
 */
public void testConstructorDeclarator() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	Bar(Xxx o) {							\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar(<CompleteOnType:X> o) {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on constructor declarator>"
	);
}
/*
 * The reference type is burried in several blocks
 */
public void testDeepReference() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		if (a == 2) {						\n" +
		"		}									\n" +
		"		try {								\n" +
		"		} finally {							\n" +
		"			if (1 == fgh) {					\n" +
		"				Xxx o = null;				\n" +
		"			}								\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      {\n" +
		"        if ((1 == fgh))\n" +
		"            <CompleteOnName:X>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on deep type>"
	);
}
/*
 * Super ::= 'extends' <ClassType>
 */
public void testExtendsClass() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar extends Xxx {					\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnClass:X>",
		// expectedUnitDisplayString:
		"class Bar extends <CompleteOnClass:X> {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on extends>"
	);
}
/*
 * ExtendsInterfaces ::= 'extends' <InterfaceTypeList>
 */
public void testExtendsInterface() {
	runTestCheckDietParse(
		// compilationUnit:
		"interface Bar extends Xxx {				\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnInterface:X>",
		// expectedUnitDisplayString:
		"interface Bar extends <CompleteOnInterface:X> {\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on extends>"
	);
}
/*
 * FieldDeclaration ::= Modifiersopt <Type> VariableDeclarators ';'
 * where Modifiersopt is not empty
 */
public void testFieldDeclarationWithModifiers() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	public final Xxx foo;					\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>;",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  <CompleteOnType:X>;\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on field declaration with modifiers>"
	);
}
/*
 * FieldDeclaration ::= Modifiersopt <Type> VariableDeclarators ';'
 * where Modifiersopt is empty
 */
public void testFieldDeclarationWithoutModifiers() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	Xxx foo;								\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>;",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  <CompleteOnType:X>;\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on field declaration without modifiers>"
	);
}
/*
 * FormalParameter ::= Modifiers <Type> VariableDeclaratorId
 */
public void testFormalParameter() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo(final Xxx x) {					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo(final <CompleteOnType:X> x) {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on formal parameter>"
	);
}
/*
 * FormalParameterList ::= FormalParameterList ',' <FormalParameter>
 */
public void testFormalParameterList() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo(int i, final Object o, Xxx x) {\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo(int i, final Object o, <CompleteOnType:X> x) {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on formal parameter list>"
	);
}
/*
 * ForStatement ::= 'for' '(' <ForInitopt> ';' Expressionopt ';' ForUpdateopt ')' Statement
 * or
 * ForStatementNoShortIf ::= 'for' '(' <ForInitopt> ';' Expressionopt ';' ForUpdateopt ')' StatementNoShortIf
 */
public void testForStatement() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		for (Xxx o = new Y(); o.size() < 10; ) {\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:X>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on for statement>"
	);
}
/*
 * Interfaces ::= 'implements' <InterfaceTypeList>
 */
public void testImplements() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar implements Xxx {					\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnInterface:X>",
		// expectedUnitDisplayString:
		"class Bar implements <CompleteOnInterface:X> {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on implements>"
	);
}
/*
 * RelationalExpression ::= RelationalExpression 'instanceof' <ReferenceType>
 */
public void testInstanceOf() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	boolean foo() {								\n" +
		"		return this instanceof Xxx;				\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  boolean foo() {\n" +
		"    (this instanceof <CompleteOnType:X>);\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on instanceof>"
	);
}
/*
 * InterfaceBody ::= '{' <InterfaceMemberDeclarationsopt> '}'
 */
public void testInterfaceBody() {
	runTestCheckDietParse(
		// compilationUnit:
		"interface Bar {							\n" +
		"	Xxx foo();								\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"interface Bar {\n" +
		"  <CompleteOnType:X>\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on interface body>"
	);
}
/*
 * InterfaceMemberDeclarations ::= InterfaceMemberDeclarations <InterfaceMemberDeclaration>
 */
public void testInterfaceMemberDeclarations() {
	runTestCheckDietParse(
		// compilationUnit:
		"interface Bar {							\n" +
		"	int CONSTANT = 0;						\n" +
		"	Xxx foo();								\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"interface Bar {\n" +
		"  int CONSTANT;\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"  <CompleteOnType:X>\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on interface member declarations>"
	);
}
/*
 * InterfaceTypeList ::= InterfaceTypeList ',' <InterfaceType>
 */
public void testInterfaceTypeList() {
	runTestCheckDietParse(
		// compilationUnit:
		"interface Bar extends Comparable, Xxx {	\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnInterface:X>",
		// expectedUnitDisplayString:
		"interface Bar extends Comparable, <CompleteOnInterface:X> {\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on interface list>"
	);
}
/*
 * LocalVariableDeclaration ::= Modifiers <Type> VariableDeclarators
 */
public void testLocalVariableDeclaration() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		final Xxx o = new Y();					\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:X>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on local variable declaration>"
	);
}
/*
 * MethodBody ::= NestedMethod '{' <BlockStatementsopt> '}'
 */
public void testMethodBody() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		Xxx o = new Y();					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:X>;\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on method body>"
	);
}
/*
 * MethodDeclarator ::= 'Identifier' '(' <FormalParameterListopt> ')' Dimsopt
 */
public void testMethodDeclarator() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo(Xxx o) {						\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo(<CompleteOnType:X> o) {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on method declarator>"
	);
}
/*
 * MethodHeader ::= Modifiersopt <Type> MethodDeclarator Throwsopt
 * where Modifiersopt is not empty
 */
public void testMethodHeaderWithModifiers() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	public static Xxx foo() {				\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  <CompleteOnType:X>\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on method header with modifiers>"
	);
}
/*
 * MethodHeader ::= Modifiersopt <Type> MethodDeclarator Throwsopt
 * where Modifiersopt is empty
 */
public void testMethodHeaderWithoutModifiers() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	Xxx foo() {								\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  <CompleteOnType:X>\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on method header without modifiers>"
	);
}
/*
 * Completion on a qualified type reference, where the cursor is in the
 * first type reference.
 */
public void testQualifiedTypeReferenceShrinkAll() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		new a.b.c.Xxx();			\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"		new a",
		// expectedCompletionNodeToString:
		"<CompleteOnType:a>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    new <CompleteOnType:a>();\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"a",
		// expectedReplacedSource:
		"a",
		// test name
		"<complete on qualified type reference (shrink all)>"
	);
}
/*
 * Completion on a qualified type reference, where the cursor is right after the first dot.
 */
public void testQualifiedTypeReferenceShrinkAllButOne() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		new a.b.c.Xxx();			\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"a.",
		// expectedCompletionNodeToString:
		"<CompleteOnType:a.>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    new <CompleteOnType:a.>();\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"a.",
		// test name
		"<complete on qualified type reference (shrink all but one)>"
	);
}
/*
 * Completion on a qualified type reference, where the cursor is right after the end
 * of the last type reference.
 */
public void testQualifiedTypeReferenceShrinkNone() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		new a.b.c.Xxx();			\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:a.b.c.X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    new <CompleteOnType:a.b.c.X>();\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"a.b.c.Xxx",
		// test name
		"<complete on qualified type reference (shrink none)>"
	);
}
/*
 * Completion on a qualified type reference, where the cursor is right after the
 * last dot.
 */
public void testQualifiedTypeReferenceShrinkOne() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 0;							\n" +
		"		new a.b.c.Xxx();			\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"a.b.c.",
		// expectedCompletionNodeToString:
		"<CompleteOnType:a.b.c.>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    new <CompleteOnType:a.b.c.>();\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"a.b.c.",
		// test name
		"<complete on qualified type reference (shrink one)>"
	);
}
/*
 * SwitchBlockStatement ::= SwitchLabels <BlockStatements>
 */
public void testSwitchBlockStatement() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() {							\n" +
		"		int i = 1;							\n" +
		"		switch (i) {						\n" +
		"			case 1: 						\n" +
		"				Xxx o = fred(i);			\n" +
		"				break;						\n" +
		"			default:						\n" +
		"		}									\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    {\n" +
		"      <CompleteOnName:X>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on switch block statement>"
	);
}
/*
 * Throws ::= 'throws' <ClassTypeList>
 */
public void testThrows() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar {								\n" +
		"	void foo() throws Xxx {					\n" +
		"	}										\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:X>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() throws <CompleteOnException:X> {\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on throws>"
	);
}
}
