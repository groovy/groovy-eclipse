/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
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
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
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
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class GroovyCompilerTestSuite {

    protected static final long JDK7  = ClassFileConstants.JDK1_7;
    protected static final long JDK8  = ClassFileConstants.JDK1_8;
    protected static final long JDK9  = ClassFileConstants.JDK9;
    protected static final long JDK10 = ClassFileConstants.JDK10;
    protected static final long JDK11 = (55L << 16) + ClassFileConstants.MINOR_VERSION_0;
    protected static final long JDK12 = (56L << 16) + ClassFileConstants.MINOR_VERSION_0;
    protected static final long JDK13 = (57L << 16) + ClassFileConstants.MINOR_VERSION_0;
    protected static final long JDK14 = (58L << 16) + ClassFileConstants.MINOR_VERSION_0;
    protected static final List<Long> JDKs = Collections.unmodifiableList(Arrays.asList(JDK7, JDK8, JDK9, JDK10, JDK11, JDK12, JDK13, JDK14));

    @Parameters(name = "Java {1}")
    public static Iterable<Object[]> params() {
        long javaSpec = CompilerOptions.versionToJdkLevel(System.getProperty("java.specification.version"));
        List<Object[]> params = new ArrayList<>();
        for (long jdk : JDKs) {
            if (jdk <= javaSpec) {
                params.add(new Object[] {jdk, CompilerOptions.versionFromJdkLevel(jdk)});
            }
        }
        return params;
    }

    @Parameter(0)
    public long compliance;
    @Parameter(1)
    public String versionString;

    protected String[] vmArguments;

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

                String[] ivyVersions = {"2.5.0", "2.4.0"};
                String[] groovyVersions = {"3.0.5-indy", "2.5.13-indy", "2.4.20"};
                try {
                    URL groovyJar = null;
                    for (String groovyVer : groovyVersions) {
                        groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-" + groovyVer + ".jar");
                        if (groovyJar == null)
                            groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-" + groovyVer + ".jar");
                        if (groovyJar != null)
                            break;
                    }
                    newcps[newcps.length - 3] = resolve(groovyJar);

                    URL ivyJar = null;
                    for (String ivyVer : ivyVersions) {
                        ivyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/ivy-" + ivyVer + ".jar");
                        if (ivyJar != null)
                            break;
                    }
                    newcps[newcps.length - 2] = resolve(ivyJar);

                    // FIXASC think more about why this is here... the tests that need it specify the option but that is just for
                    // the groovy class loader to access it.  The annotation within this jar needs to be resolvable by the compiler when
                    // building the annotated source - and so I suspect that the groovyclassloaderpath does need merging onto the project
                    // classpath for just this reason, hmm.
                    URL xformsJar = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar");
                    newcps[newcps.length - 1] = resolve(xformsJar);
                } catch (IOException e) {
                    Assert.fail("IOException thrown " + e.getMessage());
                }
                return newcps;
            }

            @Override
            public String getName() {
                return testName();
            }

            @Override
            protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths) {
                this.classpaths = (classPaths == null ? getDefaultClassPaths() : classPaths);
                return new InMemoryNameEnvironment(testFiles, getClassLibs(false));
            }

            private String resolve(URL jarRef) throws IOException {
                String jarPath = FileLocator.toFileURL(jarRef).getPath();
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

    protected final Map<String, String> getCompilerOptions() {
        return ReflectionUtils.executePrivateMethod(AbstractRegressionTest.class, "getCompilerOptions", testDriver);
    }

    protected final File createScript(CharSequence name, CharSequence contents) {
        String folder = Util.getOutputDirectory() + File.separator + "resources" + File.separator;
        new File(folder).mkdirs();
        Util.writeToFile(contents.toString(), folder + name);
        return new File(folder + name);
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

    /**
     * @param expectedOutput expected batch compiler output (i.e. errors/warnings)
     */
    protected final void runNegativeTest(String[] sources, String expectedOutput) {
        runNegativeTest(sources, expectedOutput, null);
    }

    /**
     * @param expectedOutput expected batch compiler output (i.e. errors/warnings)
     */
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

    protected static FieldDeclaration findField(CompilationUnitDeclaration decl, String name) {
        for (FieldDeclaration field : decl.types[0].fields) {
            if (name.equals(String.valueOf(field.name))) {
                return field;
            }
        }
        return null;
    }

    protected static MethodDeclaration findMethod(CompilationUnitDeclaration decl, String name) {
        for (AbstractMethodDeclaration method : decl.types[0].methods) {
            if (name.equals(String.valueOf(method.selector)) &&
                    method instanceof MethodDeclaration) {
                return (MethodDeclaration) method;
            }
        }
        return null;
    }

    /**
     * Find the named file (which should have just been compiled) and for the named method determine
     * the ClassNode for the return type and return the name of the classnode.
     */
    protected static String getReturnTypeOfMethod(String filename, String methodname) {
        ModuleNode mn = getModuleNode(filename);
        ClassNode cn = mn.getClasses().get(0);
        MethodNode methodNode = cn.getMethod(methodname,
            org.codehaus.groovy.ast.Parameter.EMPTY_ARRAY);
        ClassNode returnType = methodNode.getReturnType();
        return returnType.getName();
    }

    protected static String stringify(FieldDeclaration decl) {
        StringBuilder sb = new StringBuilder();
        sb.append(decl.name);
        sb.append(" declarationSourceStart:").append(decl.declarationSourceStart); // first char (slash in "/** javadoc */ int x")
        sb.append(" modifiersSourceStart:").append(decl.modifiersSourceStart); // first char of annotation or modifier
        sb.append(" endPart1Position:").append(decl.endPart1Position); // char before first name (space in "int x,y")
        sb.append(" sourceStart:").append(decl.sourceStart); // first char of name
        sb.append(" sourceEnd:").append(decl.sourceEnd); // last char of name
        sb.append(" endPart2Position:").append(decl.endPart2Position); // last char in fragment (comma for 'x' and semicolon for 'y' in "int x,y;")
        sb.append(" declarationEnd:").append(decl.declarationEnd); // last char of declaration (semicolon in "int x = 1; // comment")
        sb.append(" declarationSourceEnd:").append(decl.declarationSourceEnd); // last char (t in "int x = 1; // comment")
        return sb.toString();
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
