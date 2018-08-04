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
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

/**
 * Completion is expected to be in an inner type
 */
public class InnerTypeCompletionTest extends AbstractCompletionTest {
public InnerTypeCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(InnerTypeCompletionTest.class);
}
/*
 * Test completion in the first method of an anonymous inner class
 */
public void testAnonymousFirstMethod() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		Object o = new Object() {				\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"			void fuzz() {						\n" +
		"			}									\n" +
		"		};										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object o;\n" +
		"    new Object() {\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"      void fuzz() {\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class in first method>"
	);
}
/*
 * Test completion in anonymous inner class with no statement defined before.
 */
public void testAnonymousNoStatementBefore() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		Object o = new Object() {				\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"		};										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object o;\n" +
		"    new Object() {\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class with no statement before>"
	);
}
/*
 * Test completion in anonymous inner class with one field defined before
 * the method containing the completion.
 */
public void testAnonymousOneFieldBefore() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		Object o = new Object() {				\n" +
		"			int field = 1;						\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"		};										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object o;\n" +
		"    new Object() {\n" +
		"      int field;\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class with one field before>"
	);
}
/*
 * Test completion in anonymous inner class with one statement defined before.
 */
public void testAnonymousOneStatementBefore() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		int i = 1;								\n" +
		"		Object o = new Object() {				\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"		};										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    Object o;\n" +
		"    new Object() {\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class with one statement before>"
	);
}
/*
 * Test completion in the second method of an anonymous inner class
 */
public void testAnonymousSecondMethod() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		Object o = new Object() {				\n" +
		"			void fuzz() {						\n" +
		"			}									\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"		};										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    Object o;\n" +
		"    new Object() {\n" +
		"      void fuzz() {\n" +
		"      }\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"    };\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class in second method>"
	);
}
/*
 * Test completion in the first method of a local type declaration
 */
public void testLocalTypeFirstMethod() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		class InnerBar {						\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"			void fuzz() {						\n" +
		"			}									\n" +
		"		};										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class InnerBar {\n" +
		"      InnerBar() {\n" +
		"      }\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"      void fuzz() {\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration in first method>"
	);
}
/*
 * Test completion in local type declaration with no statement defined before.
 */
public void testLocalTypeNoStatementBefore() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		class InnerBar {						\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class InnerBar {\n" +
		"      InnerBar() {\n" +
		"      }\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration with no statement before>"
	);
}
/*
 * Test completion in local type declaration with one field defined before
 * the method containing the completion.
 */
public void testLocalTypeOneFieldBefore() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		class InnerBar {						\n" +
		"			int field = 1;						\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class InnerBar {\n" +
		"      int field;\n" +
		"      InnerBar() {\n" +
		"      }\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration with one field before>"
	);
}
/*
 * Test completion in local type declaration with one statement defined before.
 */
public void testLocalTypeOneStatementBefore() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		int i = 1;								\n" +
		"		class InnerBar {						\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"		}										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    class InnerBar {\n" +
		"      InnerBar() {\n" +
		"      }\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration with 1 statement before>"
	);
}
/*
 * Test completion in the second method of a local type declaration
 */
public void testLocalTypeSecondMethod() {
	this.runTestCheckMethodParse(
		// compilationUnit:
		"class Bar {									\n" +
		"	void foo() {								\n" +
		"		class InnerBar {						\n" +
		"			void fuzz() {						\n" +
		"			}									\n" +
		"			void buzz() {						\n" +
		"				int i = fred().xyz;				\n" +
		"			}									\n" +
		"		};										\n" +
		"	}											\n" +
		"}												\n",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		// expectedUnitDisplayString:
		"class Bar {\n" +
		"  Bar() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    class InnerBar {\n" +
		"      InnerBar() {\n" +
		"      }\n" +
		"      void fuzz() {\n" +
		"      }\n" +
		"      void buzz() {\n" +
		"        int i = <CompleteOnMemberAccess:fred().x>;\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration in second method>"
	);
}
}
