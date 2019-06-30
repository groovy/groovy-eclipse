/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.mining;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.jdt.groovy.ast.MethodNodeWithNamedParams;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.LineContentCodeMining;

public class MethodParameterCodeMiningProvider extends AbstractCodeMiningProvider {

    // TODO: "argN" from org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode.makeParameter(TypeBinding, char[][])
    // TODO: "argN" from org.codehaus.groovy.vmplugin.v5.Java5.makeParameter(CompileUnit, Type, Class, Annotation[], Member)
    // TODO: "pN" org.codehaus.groovy.classgen.InnerClassVisitor.visitConstructorCallExpression(ConstructorCallExpression)

    @Override
    public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
        return CompletableFuture.supplyAsync(() -> {
            if (!monitor.isCanceled()) {
                GroovyCompilationUnit compilationUnit = getAdapter(GroovyCompilationUnit.class);
                if (compilationUnit != null && compilationUnit.isConsistent() /*&& compilationUnit.isStructureKnown()*/) {
                    final int[] lastLine = new int[1];
                    List<ICodeMining> codeMinings = new ArrayList<>();
                    TypeInferencingVisitorWithRequestor codeVisitor = new TypeInferencingVisitorFactory().createVisitor(compilationUnit);
                    codeVisitor.visitCompilationUnit((ASTNode node, TypeLookupResult result, org.eclipse.jdt.core.IJavaElement enclosingElement) -> {
                        if (node.getEnd() > 0 && !(node instanceof MethodNode) && result.declaration instanceof MethodNode && asBoolean(((MethodNode) result.declaration).getParameters())) {
                            MethodCall methodCall = null;
                            if (node instanceof MethodCall) {
                                methodCall = (MethodCall) node;
                            } else if (node instanceof ConstantExpression && result.scope.getEnclosingNode() instanceof MethodCall) {
                                methodCall = (MethodCall) result.scope.getEnclosingNode();
                                assert node.getText().equals(methodCall.getMethodAsString());
                            }

                            if (methodCall != null) {
                                provideMethodCallCodeMinings(codeMinings, methodCall, (MethodNode) result.declaration, result.isGroovy);
                            }

                            // check cancel no more than once per line
                            if (node.getLineNumber() > lastLine[0]) {
                                lastLine[0] = node.getLineNumber();
                                if (monitor.isCanceled()) {
                                    return VisitStatus.STOP_VISIT;
                                }
                            }
                        }
                        return VisitStatus.CONTINUE;
                    });

                    return codeMinings;
                }
            }
            return null;
        });
    }

    private void provideMethodCallCodeMinings(List<ICodeMining> codeMinings, MethodCall methodCall, MethodNode methodNode, boolean isGroovyMethod) {
        if (methodCall.getArguments() instanceof ArgumentListExpression) {
            //System.err.printf("name:%s, text:%s%n", methodCall.getMethodAsString(), methodCall.getText());
            // TODO: Calls to Closures are seen as method calls: "def code = { a, b -> ... }; code(1, 2);"
            // TODO: What about calls w/o parentheses?  "println 'blah'" or "something.collect { ... }"
            // TODO: What about methods with named and positional arguments? "foo.bar(a, b, see: c)"

            int i = (isGroovyMethod ? 1 : 0); // skip "self" parameter
            Parameter[] parameters = (methodNode instanceof MethodNodeWithNamedParams
                ? ((MethodNodeWithNamedParams) methodNode).getPositionalParams() : methodNode.getParameters());
            for (Expression argument : (ArgumentListExpression) methodCall.getArguments()) {
                //if (argument instanceof MapExpression) continue; // try to skip named args
                Parameter parameter = parameters[Math.min(i++, parameters.length - 1)];
                codeMinings.add(newMethodParameterCodeMining(argument, parameter));
            }
        }
    }

    private ICodeMining newMethodParameterCodeMining(Expression argument, Parameter parameter) {
        LineContentCodeMining codeMining = new LineContentCodeMining(new Position(argument.getStart(), 1), this) {};
        codeMining.setLabel(parameter.getName() + ':');
        return codeMining;
    }
}
