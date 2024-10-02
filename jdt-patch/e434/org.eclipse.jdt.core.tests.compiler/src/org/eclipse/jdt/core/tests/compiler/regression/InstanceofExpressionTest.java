/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
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
 *
 *******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class InstanceofExpressionTest extends AbstractRegressionTest {

	public InstanceofExpressionTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return InstanceofExpressionTest.class;
	}

	static {
	//	TESTS_NAMES = new String [] { "testIssue2101" };
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=341828
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.InputStream;\n" +
				"public class X {\n" +
				"    void foo(InputStream is) {\n" +
				"    if (is instanceof FileInputStream)\n" +
				"        System.out.println(\"Hello\");\n" +
				"    }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (is instanceof FileInputStream)\n" +
			"	                  ^^^^^^^^^^^^^^^\n" +
			"FileInputStream cannot be resolved to a type\n" +
			"----------\n"
		);
	}

    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent= control.getParent();
						while (!(parent instanceof ExpandableComposite) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;\n" +
				"  // Stack: 1, Locals: 3\n" +
				"  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);\n" +
				"     0  aload_1 [control]\n" +
				"     1  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"     4  astore_2 [parent]\n" +
				"     5  goto 13\n" +
				"     8  aload_2 [parent]\n" +
				"     9  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"    12  astore_2 [parent]\n" +
				"    13  aload_2 [parent]\n" +
				"    14  instanceof X$ExpandableComposite [22]\n" +
				"    17  ifne 24\n" +
				"    20  aload_2 [parent]\n" +
				"    21  ifnonnull 8\n" +
				"    24  aload_2 [parent]\n" +
				"    25  instanceof X$ExpandableComposite [22]\n" +
				"    28  ifeq 36\n" +
				"    31  aload_2 [parent]\n" +
				"    32  checkcast X$ExpandableComposite [22]\n" +
				"    35  areturn\n" +
				"    36  aconst_null\n" +
				"    37  areturn\n";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_2() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent= control.getParent();
						if (!(parent instanceof ExpandableComposite) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;\n" +
				"  // Stack: 1, Locals: 3\n" +
				"  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);\n" +
				"     0  aload_1 [control]\n" +
				"     1  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"     4  astore_2 [parent]\n" +
				"     5  aload_2 [parent]\n" +
				"     6  instanceof X$ExpandableComposite [22]\n" +
				"     9  ifne 21\n" +
				"    12  aload_2 [parent]\n" +
				"    13  ifnull 21\n" +
				"    16  aload_2 [parent]\n" +
				"    17  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"    20  astore_2 [parent]\n" +
				"    21  aload_2 [parent]\n" +
				"    22  instanceof X$ExpandableComposite [22]\n" +
				"    25  ifeq 33\n" +
				"    28  aload_2 [parent]\n" +
				"    29  checkcast X$ExpandableComposite [22]\n" +
				"    32  areturn\n" +
				"    33  aconst_null\n" +
				"    34  areturn\n";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_3() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent= control.getParent();
						while ((parent instanceof ExpandableComposite) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;\n" +
				"  // Stack: 1, Locals: 3\n" +
				"  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);\n" +
				"     0  aload_1 [control]\n" +
				"     1  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"     4  astore_2 [parent]\n" +
				"     5  goto 13\n" +
				"     8  aload_2 [parent]\n" +
				"     9  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"    12  astore_2 [parent]\n" +
				"    13  aload_2 [parent]\n" +
				"    14  instanceof X$ExpandableComposite [22]\n" +
				"    17  ifeq 24\n" +
				"    20  aload_2 [parent]\n" +
				"    21  ifnonnull 8\n" +
				"    24  aload_2 [parent]\n" +
				"    25  instanceof X$ExpandableComposite [22]\n" +
				"    28  ifeq 36\n" +
				"    31  aload_2 [parent]\n" +
				"    32  checkcast X$ExpandableComposite [22]\n" +
				"    35  areturn\n" +
				"    36  aconst_null\n" +
				"    37  areturn\n";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_4() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent= control.getParent();
						if ((parent instanceof ExpandableComposite) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;\n" +
				"  // Stack: 1, Locals: 3\n" +
				"  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);\n" +
				"     0  aload_1 [control]\n" +
				"     1  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"     4  astore_2 [parent]\n" +
				"     5  aload_2 [parent]\n" +
				"     6  instanceof X$ExpandableComposite [22]\n" +
				"     9  ifeq 21\n" +
				"    12  aload_2 [parent]\n" +
				"    13  ifnull 21\n" +
				"    16  aload_2 [parent]\n" +
				"    17  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"    20  astore_2 [parent]\n" +
				"    21  aload_2 [parent]\n" +
				"    22  instanceof X$ExpandableComposite [22]\n" +
				"    25  ifeq 33\n" +
				"    28  aload_2 [parent]\n" +
				"    29  checkcast X$ExpandableComposite [22]\n" +
				"    32  areturn\n" +
				"    33  aconst_null\n" +
				"    34  areturn\n";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_5() throws ClassFormatException, IOException {
    	String source =
    			"""
				public class X {

					class ExpandableComposite extends Control {}

					class Control {

						public X.Control getParent() {
							return null;
						}}

					protected ExpandableComposite getParentExpandableComposite(Control control) {
						Control parent = control.getParent();
						boolean b = parent instanceof ExpandableComposite;
						if ((b) && parent != null) {
							parent= parent.getParent();
						}
						if (parent instanceof ExpandableComposite) {
							return (ExpandableComposite) parent;
						}
						return null;
					}
					public static void main(String [] args) {
					    System.out.println("Done!");
					}
				}
				""";

    	String expectedOutput =
    			"  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;\n" +
				"  // Stack: 1, Locals: 4\n" +
				"  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);\n" +
				"     0  aload_1 [control]\n" +
				"     1  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"     4  astore_2 [parent]\n" +
				"     5  aload_2 [parent]\n" +
				"     6  instanceof X$ExpandableComposite [22]\n" +
				"     9  istore_3 [b]\n" +
				"    10  iload_3 [b]\n" +
				"    11  ifeq 23\n" +
				"    14  aload_2 [parent]\n" +
				"    15  ifnull 23\n" +
				"    18  aload_2 [parent]\n" +
				"    19  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"    22  astore_2 [parent]\n" +
				"    23  aload_2 [parent]\n" +
				"    24  instanceof X$ExpandableComposite [22]\n" +
				"    27  ifeq 35\n" +
				"    30  aload_2 [parent]\n" +
				"    31  checkcast X$ExpandableComposite [22]\n" +
				"    34  areturn\n" +
				"    35  aconst_null\n" +
				"    36  areturn\n";
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Done!");
    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_6() throws ClassFormatException, IOException {
     	String source =
     			"""
 				public class X {

 					class ExpandableComposite extends Control {}

 					class Control {

 						public X.Control getParent() {
 							return null;
 						}}

 					protected ExpandableComposite getParentExpandableComposite(Control control) {
 						Control parent = control.getParent();
 						boolean b = !(parent instanceof ExpandableComposite);
 						if ((b) && parent != null) {
 							parent= parent.getParent();
 						}
 						if (parent instanceof ExpandableComposite) {
 							return (ExpandableComposite) parent;
 						}
 						return null;
 					}
 					public static void main(String [] args) {
 					    System.out.println("Done!");
 					}
 				}
 				""";

     	String expectedOutput =
     			"  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;\n" +
				"  // Stack: 1, Locals: 4\n" +
				"  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);\n" +
				"     0  aload_1 [control]\n" +
				"     1  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"     4  astore_2 [parent]\n" +
				"     5  aload_2 [parent]\n" +
				"     6  instanceof X$ExpandableComposite [22]\n" +
				"     9  ifeq 16\n" +
				"    12  iconst_0\n" +
				"    13  goto 17\n" +
				"    16  iconst_1\n" +
				"    17  istore_3 [b]\n" +
				"    18  iload_3 [b]\n" +
				"    19  ifeq 31\n" +
				"    22  aload_2 [parent]\n" +
				"    23  ifnull 31\n" +
				"    26  aload_2 [parent]\n" +
				"    27  invokevirtual X$Control.getParent() : X$Control [16]\n" +
				"    30  astore_2 [parent]\n" +
				"    31  aload_2 [parent]\n" +
				"    32  instanceof X$ExpandableComposite [22]\n" +
				"    35  ifeq 43\n" +
				"    38  aload_2 [parent]\n" +
				"    39  checkcast X$ExpandableComposite [22]\n" +
				"    42  areturn\n" +
				"    43  aconst_null\n" +
				"    44  areturn\n";
     	runConformTest(
                 new String[] {
                         "X.java",
                         source,
                 },
                 "Done!");
     	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_7() throws ClassFormatException, IOException {
      	String source =
      			"""
  				public class X {

  					class ExpandableComposite extends Control {}

  					class Control {

  						public X.Control getParent() {
  							return null;
  						}}

  					protected ExpandableComposite getParentExpandableComposite(Control control) {
  						Control parent = control.getParent();
  						boolean b = !(parent instanceof ExpandableComposite) ? false : true;
  						if ((b) && parent != null) {
  							parent= parent.getParent();
  						}
  						if (parent instanceof ExpandableComposite) {
  							return (ExpandableComposite) parent;
  						}
  						return null;
  					}
  					public static void main(String [] args) {
  					    System.out.println("Done!");
  					}
  				}
  				""";

      	String expectedOutput =
      			"  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;\n" +
 				"  // Stack: 1, Locals: 4\n" +
 				"  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);\n" +
 				"     0  aload_1 [control]\n" +
 				"     1  invokevirtual X$Control.getParent() : X$Control [16]\n" +
 				"     4  astore_2 [parent]\n" +
 				"     5  aload_2 [parent]\n" +
 				"     6  instanceof X$ExpandableComposite [22]\n" +
 				"     9  ifne 16\n" +
 				"    12  iconst_0\n" +
 				"    13  goto 17\n" +
 				"    16  iconst_1\n" +
 				"    17  istore_3 [b]\n" +
 				"    18  iload_3 [b]\n" +
 				"    19  ifeq 31\n" +
 				"    22  aload_2 [parent]\n" +
 				"    23  ifnull 31\n" +
 				"    26  aload_2 [parent]\n" +
 				"    27  invokevirtual X$Control.getParent() : X$Control [16]\n" +
 				"    30  astore_2 [parent]\n" +
 				"    31  aload_2 [parent]\n" +
 				"    32  instanceof X$ExpandableComposite [22]\n" +
 				"    35  ifeq 43\n" +
 				"    38  aload_2 [parent]\n" +
 				"    39  checkcast X$ExpandableComposite [22]\n" +
 				"    42  areturn\n" +
 				"    43  aconst_null\n" +
 				"    44  areturn\n";
      	runConformTest(
                  new String[] {
                          "X.java",
                          source,
                  },
                  "Done!");
      	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_8() throws ClassFormatException, IOException {
      	String source =
      			"""
  				public class X {

  					class ExpandableComposite extends Control {}

  					class Control {

  						public X.Control getParent() {
  							return null;
  						}}

  					protected ExpandableComposite getParentExpandableComposite(Control control) {
  						Control parent = control.getParent();
  						boolean b = (parent instanceof ExpandableComposite) ? false : true;
  						if ((b) && parent != null) {
  							parent= parent.getParent();
  						}
  						if (parent instanceof ExpandableComposite) {
  							return (ExpandableComposite) parent;
  						}
  						return null;
  					}
  					public static void main(String [] args) {
  					    System.out.println("Done!");
  					}
  				}
  				""";

      	String expectedOutput =
      			"  // Method descriptor #15 (LX$Control;)LX$ExpandableComposite;\n" +
 				"  // Stack: 1, Locals: 4\n" +
 				"  protected X.ExpandableComposite getParentExpandableComposite(X.Control control);\n" +
 				"     0  aload_1 [control]\n" +
 				"     1  invokevirtual X$Control.getParent() : X$Control [16]\n" +
 				"     4  astore_2 [parent]\n" +
 				"     5  aload_2 [parent]\n" +
 				"     6  instanceof X$ExpandableComposite [22]\n" +
 				"     9  ifeq 16\n" +
 				"    12  iconst_0\n" +
 				"    13  goto 17\n" +
 				"    16  iconst_1\n" +
 				"    17  istore_3 [b]\n" +
 				"    18  iload_3 [b]\n" +
 				"    19  ifeq 31\n" +
 				"    22  aload_2 [parent]\n" +
 				"    23  ifnull 31\n" +
 				"    26  aload_2 [parent]\n" +
 				"    27  invokevirtual X$Control.getParent() : X$Control [16]\n" +
 				"    30  astore_2 [parent]\n" +
 				"    31  aload_2 [parent]\n" +
 				"    32  instanceof X$ExpandableComposite [22]\n" +
 				"    35  ifeq 43\n" +
 				"    38  aload_2 [parent]\n" +
 				"    39  checkcast X$ExpandableComposite [22]\n" +
 				"    42  areturn\n" +
 				"    43  aconst_null\n" +
 				"    44  areturn\n";
      	runConformTest(
                  new String[] {
                          "X.java",
                          source,
                  },
                  "Done!");
      	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2098
    // Roll back gratuitous changes to !instanceof code generation in the absence of pattern bindings
    public void testIssue2098_9() throws ClassFormatException, IOException {
      	String source =
      			"""
  				public class X {
					public static void main(String[] args) {
						Object o = new Object();
						if (!(o instanceof String) && false) {
							System.out.println("not string");
						}
						if ((o instanceof String) && false) {
							System.out.println("not string");
						}
						System.out.println("Done!");
					}
  				}
  				""";

      	String expectedOutput =
      			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  public static void main(String[] args);\n" +
				"     0  new Object [3]\n" +
				"     3  dup\n" +
				"     4  invokespecial Object() [8]\n" +
				"     7  astore_1 [o]\n" +
				"     8  aload_1 [o]\n" +
				"     9  instanceof String [16]\n" +
				"    12  pop\n" +
				"    13  aload_1 [o]\n" +
				"    14  instanceof String [16]\n" +
				"    17  pop\n" +
				"    18  getstatic System.out : PrintStream [18]\n" +
				"    21  ldc <String \"Done!\"> [24]\n" +
				"    23  invokevirtual PrintStream.println(String) : void [26]\n" +
				"    26  return\n";
      	runConformTest(
                  new String[] {
                          "X.java",
                          source,
                  },
                  "Done!");
      	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2101
    // [Patterns] Secret local variable slots appear to be reaped later than they should be
    public void testIssue2101() throws ClassFormatException, IOException {
    	if (this.complianceLevel < ClassFileConstants.JDK16)
    		return;

      	String source =
      			"""
				public class X {

					Object type;

					public void setParent(String parent) {
						if (type != null) {
							if (type instanceof String ctype) {
								String comp = ctype;
								if (comp != null) {
									System.out.println();
								}
							}
						}

						String[] atts = null;
						for (String attribute : atts) {
							System.out.println();
						}
					}

					public static void main(String [] args) {
					    System.out.println("Done!");
					}

					X() {}

				}
  				""";

      	String expectedOutput =
      			"  // Method descriptor #8 (Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 7\n" +
				"  public void setParent(String parent);\n" +
				"     0  aload_0 [this]\n" +
				"     1  getfield X.type : Object [10]\n" +
				"     4  ifnull 38\n" +
				"     7  aload_0 [this]\n" +
				"     8  getfield X.type : Object [10]\n" +
				"    11  dup\n" +
				"    12  astore_3\n" +
				"    13  instanceof String [12]\n" +
				"    16  ifeq 38\n" +
				"    19  aload_3\n" +
				"    20  checkcast String [12]\n" +
				"    23  astore_2 [ctype]\n" +
				"    24  aload_2 [ctype]\n" +
				"    25  astore 4 [comp]\n" +
				"    27  aload 4 [comp]\n" +
				"    29  ifnull 38\n" +
				"    32  getstatic System.out : PrintStream [14]\n" +
				"    35  invokevirtual PrintStream.println() : void [20]\n" +
				"    38  aconst_null\n" +
				"    39  astore_2 [atts]\n" +
				"    40  aload_2 [atts]\n" +
				"    41  dup\n" +
				"    42  astore 6\n" +
				"    44  arraylength\n" +
				"    45  istore 5\n" +
				"    47  iconst_0\n" +
				"    48  istore 4\n" +
				"    50  goto 68\n" +
				"    53  aload 6\n" +
				"    55  iload 4\n" +
				"    57  aaload\n" +
				"    58  astore_3 [attribute]\n" +
				"    59  getstatic System.out : PrintStream [14]\n" +
				"    62  invokevirtual PrintStream.println() : void [20]\n" +
				"    65  iinc 4 1\n" +
				"    68  iload 4\n" +
				"    70  iload 5\n" +
				"    72  if_icmplt 53\n" +
				"    75  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"        [pc: 7, line: 7]\n" +
				"        [pc: 24, line: 8]\n" +
				"        [pc: 27, line: 9]\n" +
				"        [pc: 32, line: 10]\n" +
				"        [pc: 38, line: 15]\n" +
				"        [pc: 40, line: 16]\n" +
				"        [pc: 59, line: 17]\n" +
				"        [pc: 65, line: 16]\n" +
				"        [pc: 75, line: 19]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 76] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 76] local: parent index: 1 type: String\n" +
				"        [pc: 24, pc: 38] local: ctype index: 2 type: String\n" +
				"        [pc: 27, pc: 38] local: comp index: 4 type: String\n" +
				"        [pc: 40, pc: 76] local: atts index: 2 type: String[]\n" +
				"        [pc: 59, pc: 65] local: attribute index: 3 type: String\n" +
				"      Stack map table: number of frames 3\n" +
				"        [pc: 38, same]\n" +
				"        [pc: 53, full, stack: {}, locals: {X, String, String[], _, int, int, String[]}]\n" +
				"        [pc: 68, same]\n";
      	runConformTest(
                  new String[] {
                          "X.java",
                          source,
                  },
                  "Done!");
      	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    }
}