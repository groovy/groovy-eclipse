/*
 * Copyright 2009-2016 the original author or authors.
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

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

public abstract class AbstractGroovyRegressionTest extends AbstractRegressionTest {

    public AbstractGroovyRegressionTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        GroovyCompilationUnitDeclaration.defaultCheckGenerics = true;
        GroovyParser.debugRequestor = new DebugRequestor();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        GroovyCompilationUnitDeclaration.defaultCheckGenerics = false;
        GroovyParser.debugRequestor = null;
    }

    /**
     * Include the groovy runtime jars on the classpath that is used.
     * Other classpath issues can be seen in TestVerifier/VerifyTests and only when
     * the right prefixes are registered in there will it use the classloader with this
     * classpath rather than the one it conjures up just to load the built code.
     */
    protected String[] getDefaultClassPaths() {
        String[] cps = super.getDefaultClassPaths();
        String[] newcps = new String[cps.length+2];
        System.arraycopy(cps,0,newcps,0,cps.length);

        String[] groovyVersions = {"2.4.3", "2.3.10", "2.2.2", "2.1.8", "2.0.7", "1.8.6"};
        try {
            URL groovyJar=null;
            for (String groovyVer : groovyVersions) {
                groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-all-"+groovyVer+".jar");
                if (groovyJar!=null) break;
            }
            newcps[newcps.length-1] = FileLocator.resolve(groovyJar).getFile();
            // FIXASC think more about why this is here... the tests that need it specify the option but that is just for
            // the groovy class loader to access it.  The annotation within this jar needs to be resolvable by the compiler when
            // building the annotated source - and so I suspect that the groovyclassloaderpath does need merging onto the project
            // classpath for just this reason, hmm.
            newcps[newcps.length-2] = FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms.jar")).getFile();
            // newcps[newcps.length-4] = new File("astTransformations/spock-core-0.1.jar").getAbsolutePath();
        } catch (IOException e) {
            fail("IOException thrown " + e.getMessage());
        }
        return newcps;
    }

    protected static GroovyCompilationUnitDeclaration getCUDeclFor(String filename) {
        Map declarations = ((DebugRequestor) GroovyParser.debugRequestor).declarations;
        return (GroovyCompilationUnitDeclaration) declarations.get(filename);
    }

    protected static ModuleNode getModuleNode(String filename) {
        GroovyCompilationUnitDeclaration decl = getCUDeclFor(filename);
        if (decl != null) {
            return decl.getModuleNode();
        } else {
            return null;
        }
    }

    protected static void checkGCUDeclaration(String filename, String expectedOutput) {
        GroovyCompilationUnitDeclaration decl = (GroovyCompilationUnitDeclaration)((DebugRequestor)GroovyParser.debugRequestor).declarations.get(filename);
        String declarationContents = decl.print();
        if (expectedOutput==null || expectedOutput.length()==0) {
            System.out.println(Util.displayString(declarationContents,2));
        } else {
            int foundIndex = declarationContents.indexOf(expectedOutput);
            if (foundIndex==-1) {
                fail("Did not find expected output:\n"+expectedOutput+"\nin actual output:\n"+declarationContents);
            }
        }
    }

    protected static FieldDeclaration grabField(GroovyCompilationUnitDeclaration decl, String fieldname) {
        FieldDeclaration[] fDecls = decl.types[0].fields;
        for (int i = 0, n = fDecls.length; i < n; i += 1) {
            if (new String(fDecls[i].name).equals(fieldname)) {
                return fDecls[i];
            }
        }
        return null;
    }

    protected static String stringify(TypeReference type) {
        StringBuilder sb = new StringBuilder();
        stringify(type,sb);
        return sb.toString();
    }

    protected static void stringify(TypeReference type, StringBuilder sb) {
        if (type.getClass()==ParameterizedSingleTypeReference.class) {
            ParameterizedSingleTypeReference pstr = (ParameterizedSingleTypeReference)type;
            sb.append("("+pstr.sourceStart+">"+pstr.sourceEnd+")").append(pstr.token);
            TypeReference[] typeArgs = pstr.typeArguments;
            sb.append("<");
            for (int t=0;t<typeArgs.length;t++) {
                stringify(typeArgs[t],sb);
            }
            sb.append(">");
        } else if (type.getClass()==ParameterizedQualifiedTypeReference.class) {
            ParameterizedQualifiedTypeReference pqtr = (ParameterizedQualifiedTypeReference)type;
            sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
            long[] positions = pqtr.sourcePositions;
            TypeReference[][] allTypeArgs = pqtr.typeArguments;
            for (int i=0;i<pqtr.tokens.length;i++) {
                if (i>0) {
                    sb.append('.');
                }
                sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(pqtr.tokens[i]);
                if (allTypeArgs[i]!=null) {
                    sb.append("<");
                    for (int t=0;t<allTypeArgs[i].length;t++) {
                        stringify(allTypeArgs[i][t],sb);
                    }
                    sb.append(">");
                }
            }

        } else if (type.getClass()==ArrayTypeReference.class) {
            ArrayTypeReference atr = (ArrayTypeReference)type;
            // for a reference 'String[]' sourceStart='S' sourceEnd=']' originalSourceEnd='g'
            sb.append("("+atr.sourceStart+">"+atr.sourceEnd+" ose:"+atr.originalSourceEnd+")").append(atr.token);
            for (int d=0;d<atr.dimensions;d++) {
                sb.append("[]");
            }
        } else if (type.getClass()==Wildcard.class) {
            Wildcard w = (Wildcard)type;
            if (w.kind== Wildcard.UNBOUND) {
                sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append('?');
            } else if (w.kind==Wildcard.SUPER) {
                sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append("? super ");
                stringify(w.bound,sb);
            } else if (w.kind==Wildcard.EXTENDS) {
                sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append("? extends ");
                stringify(w.bound,sb);
            }
        } else if (type.getClass()== SingleTypeReference.class) {
            sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append(((SingleTypeReference)type).token);
        } else if (type instanceof ArrayQualifiedTypeReference) {
            ArrayQualifiedTypeReference aqtr = (ArrayQualifiedTypeReference)type;
            sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
            long[] positions = aqtr.sourcePositions;
            for (int i=0;i<aqtr.tokens.length;i++) {
                if (i>0) {
                    sb.append('.');
                }
                sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(aqtr.tokens[i]);
            }
            for (int i=0;i<aqtr.dimensions();i++) { sb.append("[]"); }
        } else if (type.getClass()== QualifiedTypeReference.class) {
            QualifiedTypeReference qtr = (QualifiedTypeReference)type;
            sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
            long[] positions = qtr.sourcePositions;
            for (int i=0;i<qtr.tokens.length;i++) {
                if (i>0) {
                    sb.append('.');
                }
                sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(qtr.tokens[i]);
            }
        } else {
            throw new RuntimeException("Dont know how to print "+type.getClass());
        }
    }
}
