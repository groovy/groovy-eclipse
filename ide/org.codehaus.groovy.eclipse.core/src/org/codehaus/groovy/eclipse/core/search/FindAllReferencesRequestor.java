/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.search;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.EqualityVisitor;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;

/**
 * Finds all references to a particular Declaration in a file
 *
 * @author andrew
 * @created Dec 31, 2010
 */
public class FindAllReferencesRequestor implements ITypeRequestor {

    private final AnnotatedNode declaration;

    private final Map<ASTNode, Integer> references;

    public FindAllReferencesRequestor(AnnotatedNode declaration) {
        this.declaration = declaration;
        this.references = new TreeMap<ASTNode, Integer>(new Comparator<ASTNode>() {

            public int compare(ASTNode o1, ASTNode o2) {
                return o1.getStart() - o2.getStart();
            }
        });
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (node.getLength() == 0) {
            return VisitStatus.CONTINUE;
        }

        if (node instanceof AnnotatedNode) {
            ASTNode maybeDeclaration = result.declaration;
            if (maybeDeclaration == null) {
                return VisitStatus.CONTINUE;
            }
            if (maybeDeclaration instanceof ClassNode) {
                // sometimes generated methods and properties have a classnode
                // as the declaration.
                // we want to ignore these
                if (!(node instanceof ClassExpression || node instanceof ClassNode)) {
                    return VisitStatus.CONTINUE;
                }

                maybeDeclaration = ((ClassNode) maybeDeclaration).redirect();
            }

            if (maybeDeclaration instanceof PropertyNode && ((PropertyNode) maybeDeclaration).getField() != null) {
                maybeDeclaration = ((PropertyNode) maybeDeclaration).getField();
            }

            if (isEquivalent(maybeDeclaration)) {
                int flag = EqualityVisitor.checkForAssignment(node, result.getEnclosingAssignment()) ? F_WRITE_OCCURRENCE
                        : F_READ_OCCURRENCE;
                references.put(node, flag);
            }
        }
        return VisitStatus.CONTINUE;
    }

    // from IOccurrenceFinder
    public static final int F_WRITE_OCCURRENCE = 1;

    public static final int F_READ_OCCURRENCE = 2;

    private boolean isEquivalent(ASTNode maybeDeclaration) {
        if (maybeDeclaration == declaration) {
            return true;
        }
        // here we need to test for dynamically added fields and methods
        // they will not be the same instance, so we need to check
        // for equivalence some other way
        if (maybeDeclaration.getClass() == declaration.getClass()) {
            if (maybeDeclaration instanceof FieldNode) {
                FieldNode maybeField = (FieldNode) maybeDeclaration;
                FieldNode field = (FieldNode) declaration;
                return maybeField.getName().equals(field.getName())
                        && maybeField.getDeclaringClass().equals(field.getDeclaringClass());
            } else if (maybeDeclaration instanceof MethodNode) {
                MethodNode maybeMethod = (MethodNode) maybeDeclaration;
                MethodNode method = (MethodNode) declaration;
                return checkParamLength(maybeMethod, method) && maybeMethod.getName().equals(method.getName())
                        && maybeMethod.getDeclaringClass().equals(method.getDeclaringClass()) && checkParams(maybeMethod, method);
            }
        }

        // here check for inner class nodes
        if ((maybeDeclaration instanceof InnerClassNode && declaration instanceof JDTClassNode)
                || (declaration instanceof InnerClassNode && maybeDeclaration instanceof JDTClassNode)) {
            return ((ClassNode) maybeDeclaration).getName().equals(((ClassNode) declaration).getName());
        }
        return false;

    }

    private boolean checkParams(MethodNode maybeMethod, MethodNode method) {
        Parameter[] maybeParameters = maybeMethod.getParameters();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (!maybeParameters[i].getName().equals(parameters[i].getName()) || !typeEquals(maybeParameters[i], parameters[i])) {
                return false;
            }
        }

        return true;
    }

    private boolean typeEquals(Parameter maybeParameter, Parameter parameter) {
        ClassNode maybeType = maybeParameter.getType();
        ClassNode type = parameter.getType();
        if (maybeType == null) {
            return type == null;
        } else if (type == null) {
            return false;
        }
        return maybeType.getName().equals(type.getName());
    }

    private boolean checkParamLength(MethodNode maybeMethod, MethodNode method) {
        Parameter[] maybeParameters = maybeMethod.getParameters();
        Parameter[] parameters = method.getParameters();
        if (maybeParameters == null) {
            return parameters == null;
        } else if (parameters == null) {
            return false;
        }

        if (maybeParameters.length != parameters.length) {
            return false;
        }

        return true;
    }

    public Map<ASTNode, Integer> getReferences() {
        return references;
    }
}
