/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import java.io.File;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
@SuppressWarnings({ "rawtypes" })
public class BatchCompilerTest_16 extends AbstractBatchCompilerTest {
        static {
//              TESTS_NAMES = new String[] { "testBug571454_001" };
//              TESTS_NUMBERS = new int[] { 306 };
//              TESTS_RANGE = new int[] { 298, -1 };
        }
        /**
         * This test suite only needs to be run on one compliance.
         * As it includes some specific 1.5 tests, it must be used with a least a 1.5 VM
         * and not be duplicated in general test suite.
         * @see TestAll
         */
        public static Test suite() {
                return buildMinimalComplianceTestSuite(testClass(), F_16);
        }
        public static Class testClass() {
                return BatchCompilerTest_16.class;
        }
        public BatchCompilerTest_16(String name) {
                super(name);
        }
        public void testBug571454_001(){
        		if (!AbstractBatchCompilerTest.isJREVersionEqualTo(CompilerOptions.VERSION_16))
        			return;
                String currentWorkingDirectoryPath = System.getProperty("user.dir");
                if (currentWorkingDirectoryPath == null) {
                        System.err.println("BatchCompilerTest#testBug564047_001 could not access the current working directory " + currentWorkingDirectoryPath);
                } else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
                        System.err.println("BatchCompilerTest#testBug564047_001 current working directory is not a directory " + currentWorkingDirectoryPath);
                } else {
                        try {
                        this.runNegativeTest(
                                        new String[] {
                                                        "src/X.java",
                                                        "public class X {\n"+
                                                        "    public static void main(String argv[]) {\n"+
                                                        "        R rec = new R(3);\n"+
                                                        "               if (rec.x() == 3) {\n" +
                                                        "                       // do nothing\n" +
                                                        "               }\n" +
                                                        "    }\n"+
                                                        "}\n",
                                                        "src/R.java",
                                                        "record R(int x) {\n"+
                                                        "       R {\n"+
                                                        "               super();\n"+
                                                        "       }\n"+
                                                        "}",
                                                },
                                    "\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
                                    + " \"" + OUTPUT_DIR +  File.separator + "src/R.java\""
                                        + " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
                                + " --release 16 -g -preserveAllLocals"
                                + " -proceedOnError -referenceInfo"
                                + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
                                "",
                                "----------\n" +
                                "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/R.java (at line 3)\n" +
                                "	super();\n" +
                                "	^^^^^^^^\n" +
                                "The body of a compact constructor must not contain an explicit constructor call\n" +
                                "----------\n"
                                + "1 problem (1 error)\n",
                                true);
                        } finally {
                        }
                }
        }
        public void testBug570399(){
        	this.runConformTest(
        		new String[] {
                    "src/X.java",
                    "public class X {\n"+
                    "    public static void main(String argv[]) {\n"+
                    "       new R(3);\n"+
                    "       new R();\n"+
                    "    }\n"+
                    "}\n",
                    "src/R.java",
                    "record R(int x) {\n"+
                    "       R() {\n"+
                    "       this(0);\n"+
                    "       }\n"+
                    "}",
                },
                "\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
                + " \"" + OUTPUT_DIR +  File.separator + "src/R.java\""
                    + " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
            + " --release 16 -g -preserveAllLocals"
            + " -proceedOnError -referenceInfo"
            + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
            "",
                "",
                true);
        }
}
