/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ParserPluginFactory;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * A class loader for the script to be evaluated that has a different parent
 * than the currently running eclipse instance or the debugged. This way
 * classes in the script do not conflict with classes in the dev env.
 *
 * @author Andrew Eisenberg
 */
public class JDIScriptLoader extends GroovyClassLoader {

    public JDIScriptLoader(ClassLoader loader) {
        super(loader);
    }

    private ClassNode theClass;

    public ClassNode getTheClass() {
        return theClass;
    }

    @Override
    protected ClassCollector createCollector(CompilationUnit cu, SourceUnit su) {
        InnerLoader loader = AccessController.doPrivileged((PrivilegedAction<InnerLoader>) () -> new InnerLoader(this));

        return new ClassCollector(loader, cu, su) {
            @Override
            protected Class createClass(byte[] code, ClassNode classNode) {
                theClass = classNode; // save reference to the script class
                return super.createClass(code, classNode);
            }
        };
    }

    @Override
    protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
        config = new CompilerConfiguration(config);
        config.setPluginFactory(ParserPluginFactory.antlr4()); // for java parsing
        config.getCompilationCustomizers().addAll(config.getCompilationCustomizers()); // GROOVY-9585
        CompilationUnit compilationUnit = super.createCompilationUnit(config, source);

        compilationUnit.addPhaseOperation(sourceUnit -> {
            // convert comparison expressions to use JDIComparator instead of ScriptByteCodeAdapter
            new ClassCodeExpressionTransformer() {
                @Override
                protected SourceUnit getSourceUnit() {
                    return sourceUnit;
                }

                @Override
                public Expression transform(Expression expression) {
                    if (expression instanceof BinaryExpression) {
                        BinaryExpression b = (BinaryExpression) expression;
                        String methodName = JDIComparator.methodNameFor(b.getOperation());
                        if (methodName != null) { // delegate to JDIComparator
                            Expression lhs = transform(b.getLeftExpression());
                            Expression rhs = transform(b.getRightExpression());
                            return callX(varX("__comparator"), methodName, args(lhs, rhs));
                        }
                    }
                    if (expression instanceof MethodPointerExpression) {
                        MethodPointerExpression m = (MethodPointerExpression) expression;
                        return callX(classX(ScriptBytecodeAdapter.class), "getMethodPointer",
                            args(transform(m.getExpression()), transform(m.getMethodName())));
                    }
                    if (expression instanceof StaticMethodCallExpression) {
                        // to MethodCallExpression so that JDIMetaClass#invokeStaticMethod runs
                        StaticMethodCallExpression s = (StaticMethodCallExpression) expression;
                        return callX(classX(s.getOwnerType()), s.getMethod(), transform(s.getArguments()));
                    }
                    return super.transform(expression);
                }
            }.visitBlockStatement(sourceUnit.getAST().getStatementBlock());
        }, Phases.CANONICALIZATION);

        return compilationUnit;
    }
}
