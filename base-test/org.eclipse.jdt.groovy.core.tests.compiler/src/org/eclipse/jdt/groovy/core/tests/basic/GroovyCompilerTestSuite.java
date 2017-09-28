/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.groovy.core.tests.basic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.core.tests.compiler.regression.InMemoryNameEnvironment;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class GroovyCompilerTestSuite {

    //CompilerOptions.versionFromJdkLevel(_)
    protected static final long JDK5 = ClassFileConstants.JDK1_5;
    protected static final long JDK6 = ClassFileConstants.JDK1_6;
    protected static final long JDK7 = ClassFileConstants.JDK1_7;
    protected static final long JDK8 = (52 << 16) + ClassFileConstants.MINOR_VERSION_0;
    protected static final long JDK9 = (53 << 16) + ClassFileConstants.MINOR_VERSION_0;
    protected static final List<Long> JDKs = Collections.unmodifiableList(Arrays.asList(JDK5, JDK6, JDK7, JDK8, JDK9));

    @Parameterized.Parameters
    public static Iterable<Object[]> params() {
        long javaSpec = CompilerOptions.versionToJdkLevel(System.getProperty("java.specification.version"));
        List<Object[]> params = new ArrayList<Object[]>();
        for (long jdk : JDKs) {
            if (jdk <= javaSpec) {
                params.add(new Object[] {jdk});
            }
        }
        return params;
    }

    private final long compliance;
    protected String[] vmArguments;

    public GroovyCompilerTestSuite(long compliance) {
        this.compliance = compliance;
    }

    @Rule
    public TestName test = new TestName();

    private AbstractRegressionTest testDriver;

    @Before
    public final void setUpTestCase() throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + test.getMethodName());

        GroovyCompilationUnitDeclaration.defaultCheckGenerics = true;
        GroovyParser.debugRequestor = new DebugRequestor();

        testDriver = new AbstractRegressionTest(test.getMethodName()) {
            @Override
            protected Map<String, String> getCompilerOptions() {
                Map<String, String> options = super.getCompilerOptions();
                options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
                return options;
            }

            /**
             * Include the groovy runtime jars on the classpath that is used.
             * Other classpath issues can be seen in TestVerifier/VerifyTests and only when
             * the right prefixes are registered in there will it use the classloader with this
             * classpath rather than the one it conjures up just to load the built code.
             */
            @Override
            protected String[] getDefaultClassPaths() {
                String[] cps = super.getDefaultClassPaths();
                String[] newcps = new String[cps.length + 3];
                System.arraycopy(cps, 0, newcps, 0, cps.length);

                String[] ivyVersions = {"2.4.0", "2.3.0", "2.2.0"};
                String[] groovyVersions = {"2.5.0", "2.4.12", "2.3.11", "2.2.2", "2.1.9", "2.0.8", "1.8.9"};
                try {
                    URL groovyJar = null;
                    for (String groovyVer : groovyVersions) {
                        groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-" + groovyVer + ".jar");
                        if (groovyJar != null) break;
                    }
                    newcps[newcps.length-3] = resolve(groovyJar);

                    URL ivyJar = null;
                    for (String ivyVer : ivyVersions) {
                        ivyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/ivy-" + ivyVer + ".jar");
                        if (ivyJar != null) break;
                    }
                    newcps[newcps.length-2] = resolve(ivyJar);

                    // FIXASC think more about why this is here... the tests that need it specify the option but that is just for
                    // the groovy class loader to access it.  The annotation within this jar needs to be resolvable by the compiler when
                    // building the annotated source - and so I suspect that the groovyclassloaderpath does need merging onto the project
                    // classpath for just this reason, hmm.
                    newcps[newcps.length-1] = resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar"));
                } catch (IOException e) {
                    Assert.fail("IOException thrown " + e.getMessage());
                }
                return newcps;
            }

            @Override
            protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths) {
                this.classpaths = (classPaths == null ? getDefaultClassPaths() : classPaths);
                return new InMemoryNameEnvironment(testFiles, getClassLibs(false));
            }

            private String resolve(URL jarRef) throws IOException {
                String jarPath = FileLocator.resolve(jarRef).getFile();
                return new File(jarPath).getAbsolutePath();
            }
        };
        testDriver.initialize(new CompilerTestSetup(compliance));
        ReflectionUtils.throwableExecutePrivateMethod(AbstractRegressionTest.class, "setUp", new Class[0], testDriver, new Object[0]);
    }

    @After
    public void tearDownTestCase() throws Exception {
        ReflectionUtils.throwableExecutePrivateMethod(AbstractRegressionTest.class, "tearDown", new Class[0], testDriver, new Object[0]);
        GroovyCompilationUnitDeclaration.defaultCheckGenerics = false;
        GroovyParser.debugRequestor = null;
    }

    protected final boolean isAtLeastJava(long level) {
        final long complianceLevel = (Long) ReflectionUtils.getPrivateField(AbstractCompilerTest.class, "complianceLevel", testDriver);
        return complianceLevel >= level;
    }

    @SuppressWarnings("unchecked")
    protected final Map<String, String> getCompilerOptions() {
        return (Map<String, String>) ReflectionUtils.executeNoArgPrivateMethod(AbstractRegressionTest.class, "getCompilerOptions", testDriver);
    }

    protected final void runConformTest(String[] sources) {
        runConformTest(sources, (String) null, (String) null);
    }

    protected final void runConformTest(String[] sources, String expectedStdout) {
        runConformTest(sources, expectedStdout, (String) null);
    }

    protected final void runConformTest(String[] sources, String expectedStdout, String expectedStderr) {
        testDriver.runTest(
            sources,
            false, // expectingCompilerErrors
            null,  // expectedCompilerLog
            expectedStdout,
            expectedStderr,
            false, // forceExecution
            null,  // classLibraries
            true,  // shouldFlushOutputDirectory
            vmArguments,
            null,  // customOptions
            null,  // customRequestor
            false  // skipJavac
        );
    }

    protected final void runConformTest(String[] sources, String expectedStdout, Map<String, String> compilerOptions) {
        testDriver.runTest(
            sources,
            false, // expectingCompilerErrors
            null,  // expectedCompilerLog
            expectedStdout,
            null,  // expectedErrorString
            false, // forceExecution
            null,  // classLibraries
            true,  // shouldFlushOutputDirectory
            vmArguments,
            compilerOptions,
            null,  // customRequestor
            false  // skipJavac
        );
    }

    protected final void runNegativeTest(String[] sources, String expectedOutput) {
        runNegativeTest(sources, expectedOutput, null);
    }

    protected final void runNegativeTest(String[] sources, String expectedOutput, Map<String, String> compilerOptions) {
        testDriver.runTest(
            sources,
            expectedOutput.contains("ERROR"),
            expectedOutput,
            null,  // expectedOutputString
            null,  // expectedErrorString
            false, // forceExecution
            null,  // classLibraries
            true,  // shouldFlushOutputDirectory
            vmArguments,
            compilerOptions,
            null,  // customRequestor
            false  // skipJavac
        );
    }

    //--------------------------------------------------------------------------

    protected static GroovyCompilationUnitDeclaration getCUDeclFor(String filename) {
        return ((DebugRequestor) GroovyParser.debugRequestor).declarations.get(filename);
    }

    protected static ModuleNode getModuleNode(String filename) {
        GroovyCompilationUnitDeclaration decl = getCUDeclFor(filename);
        if (decl != null) {
            return decl.getModuleNode();
        } else {
            return null;
        }
    }

    protected static void checkDisassemblyFor(String filename, String expectedOutput) {
        checkDisassemblyFor(filename, expectedOutput, ClassFileBytesDisassembler.DETAILED);
    }

    /**
     * Check the disassembly of a .class file for a particular piece of text
     */
    protected static void checkDisassemblyFor(String filename, String expectedOutput, int detail) {
        try {
            File f = new File(AbstractRegressionTest.OUTPUT_DIR + File.separator + filename);
            byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
            ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
            String result = disassembler.disassemble(classFileBytes, "\n", detail);
            int index = result.indexOf(expectedOutput);
            if (index == -1 || expectedOutput.length() == 0) {
                System.out.println(Util.displayString(result, 3));
            }
            if (index == -1) {
                Assert.assertEquals("Wrong contents", expectedOutput, result);
            }
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    protected static void checkGCUDeclaration(String filename, String expectedOutput) {
        GroovyCompilationUnitDeclaration decl = ((DebugRequestor) GroovyParser.debugRequestor).declarations.get(filename);
        String declarationContents = decl.print();
        if (expectedOutput == null || expectedOutput.length() == 0) {
            System.out.println(Util.displayString(declarationContents, 2));
        } else {
            if (declarationContents.indexOf(expectedOutput) == -1) {
                Assert.assertEquals("Did not find expected output", expectedOutput, declarationContents);
            }
        }
    }

    protected static FieldDeclaration findField(GroovyCompilationUnitDeclaration decl, String name) {
        for (FieldDeclaration field : decl.types[0].fields) {
            if (name.equals(String.valueOf(field.name))) {
                return field;
            }
        }
        return null;
    }

    protected static MethodDeclaration findMethod(GroovyCompilationUnitDeclaration decl, String name) {
        for (AbstractMethodDeclaration method : decl.types[0].methods) {
            if (name.equals(String.valueOf(method.selector)) &&
                    method instanceof MethodDeclaration) {
                return (MethodDeclaration) method;
            }
        }
        return null;
    }

    protected static String stringify(TypeReference type) {
        StringBuilder sb = new StringBuilder();
        stringify(type, sb);
        return sb.toString();
    }

    protected static void stringify(TypeReference type, StringBuilder sb) {
        if (type.getClass() == ParameterizedSingleTypeReference.class) {
            ParameterizedSingleTypeReference pstr = (ParameterizedSingleTypeReference) type;
            sb.append("(" + pstr.sourceStart + ">" + pstr.sourceEnd + ")").append(pstr.token);
            TypeReference[] typeArgs = pstr.typeArguments;
            sb.append("<");
            for (int t = 0; t < typeArgs.length; t++) {
                stringify(typeArgs[t], sb);
            }
            sb.append(">");
        } else if (type.getClass() == ParameterizedQualifiedTypeReference.class) {
            ParameterizedQualifiedTypeReference pqtr = (ParameterizedQualifiedTypeReference) type;
            sb.append("(" + type.sourceStart + ">" + type.sourceEnd + ")");
            long[] positions = pqtr.sourcePositions;
            TypeReference[][] allTypeArgs = pqtr.typeArguments;
            for (int i = 0; i < pqtr.tokens.length; i++) {
                if (i > 0) {
                    sb.append('.');
                }
                sb.append("(" + (int) (positions[i] >>> 32) + ">" + (int) (positions[i] & 0x00000000FFFFFFFFL) + ")").append(pqtr.tokens[i]);
                if (allTypeArgs[i] != null) {
                    sb.append("<");
                    for (int t = 0; t < allTypeArgs[i].length; t++) {
                        stringify(allTypeArgs[i][t], sb);
                    }
                    sb.append(">");
                }
            }
        } else if (type.getClass() == ArrayTypeReference.class) {
            ArrayTypeReference atr = (ArrayTypeReference) type;
            // for a reference 'String[]' sourceStart='S' sourceEnd=']' originalSourceEnd='g'
            sb.append("(" + atr.sourceStart + ">" + atr.sourceEnd + " ose:" + atr.originalSourceEnd + ")")
                .append(atr.token);
            for (int d = 0; d < atr.dimensions; d++) {
                sb.append("[]");
            }
        } else if (type.getClass() == Wildcard.class) {
            Wildcard w = (Wildcard) type;
            if (w.kind == Wildcard.UNBOUND) {
                sb.append("(" + type.sourceStart + ">" + type.sourceEnd + ")").append('?');
            } else if (w.kind == Wildcard.SUPER) {
                sb.append("(" + type.sourceStart + ">" + type.sourceEnd + ")").append("? super ");
                stringify(w.bound, sb);
            } else if (w.kind == Wildcard.EXTENDS) {
                sb.append("(" + type.sourceStart + ">" + type.sourceEnd + ")").append("? extends ");
                stringify(w.bound, sb);
            }
        } else if (type.getClass() == SingleTypeReference.class) {
            sb.append("(" + type.sourceStart + ">" + type.sourceEnd + ")").append(((SingleTypeReference) type).token);
        } else if (type instanceof ArrayQualifiedTypeReference) {
            ArrayQualifiedTypeReference aqtr = (ArrayQualifiedTypeReference) type;
            sb.append("(" + type.sourceStart + ">" + type.sourceEnd + ")");
            long[] positions = aqtr.sourcePositions;
            for (int i = 0; i < aqtr.tokens.length; i++) {
                if (i > 0) {
                    sb.append('.');
                }
                sb.append("(" + (int) (positions[i] >>> 32) + ">" + (int) (positions[i] & 0x00000000FFFFFFFFL) + ")").append(aqtr.tokens[i]);
            }
            for (int i = 0; i < aqtr.dimensions(); i++) {
                sb.append("[]");
            }
        } else if (type.getClass() == QualifiedTypeReference.class) {
            QualifiedTypeReference qtr = (QualifiedTypeReference) type;
            sb.append("(" + type.sourceStart + ">" + type.sourceEnd + ")");
            long[] positions = qtr.sourcePositions;
            for (int i = 0; i < qtr.tokens.length; i++) {
                if (i > 0) {
                    sb.append('.');
                }
                sb.append("(" + (int) (positions[i] >>> 32) + ">" + (int) (positions[i] & 0x00000000FFFFFFFFL) + ")").append(qtr.tokens[i]);
            }
        } else {
            throw new RuntimeException("Dont know how to print " + type.getClass());
        }
    }
}
