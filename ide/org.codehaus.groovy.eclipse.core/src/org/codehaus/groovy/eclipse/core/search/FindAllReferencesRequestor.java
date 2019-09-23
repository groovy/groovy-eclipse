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
package org.codehaus.groovy.eclipse.core.search;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.EqualityVisitor;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;

public class FindAllReferencesRequestor implements ITypeRequestor {

    private final AnnotatedNode declaration;

    private final Map<ASTNode, Integer> references;

    public Map<ASTNode, Integer> getReferences() {
        return references;
    }

    public FindAllReferencesRequestor(AnnotatedNode declaration) {
        this.declaration = declaration;
        this.references = new TreeMap<>(Comparator.comparing(ASTNode::getStart));
    }

    @Override
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (node.getLength() < 1) {
            return VisitStatus.CONTINUE;
        }

        if (node instanceof AnnotatedNode && !(node instanceof ImportNode || node instanceof PackageNode)) {
            ASTNode maybeDeclaration = result.declaration;
            if (maybeDeclaration == null) {
                return VisitStatus.CONTINUE;
            }

            if (maybeDeclaration instanceof ClassNode) {
                // sometimes generated methods and properties have a ClassNode as the declaration; ignore these
                if (!(node instanceof ClassExpression || node instanceof ClassNode)) {
                    return VisitStatus.CONTINUE;
                }

                // also ignore sctipt declarations
                if (node instanceof ClassNode) {
                    ClassNode script = (ClassNode) node;
                    if (script.isScript()) {
                        // ugghh...I don't like this: if the length of the node is different from the length
                        // of the name of the script we know that this is the declaration, not a reference
                        if (script.getNameWithoutPackage().length() != script.getLength()) {
                            return VisitStatus.CONTINUE;
                        }
                    }
                }
                maybeDeclaration = ((ClassNode) maybeDeclaration).redirect();
            }

            if (maybeDeclaration instanceof PropertyNode && ((PropertyNode) maybeDeclaration).getField() != null) {
                maybeDeclaration = ((PropertyNode) maybeDeclaration).getField();
            }

            if (isEquivalent(maybeDeclaration)) {
                int flag = EqualityVisitor.checkForAssignment(node, result.enclosingAssignment) ? F_WRITE_OCCURRENCE : F_READ_OCCURRENCE;
                references.put(node, flag);
            }
        }
        return VisitStatus.CONTINUE;
    }

    // from IOccurrenceFinder
    public static final int F_WRITE_OCCURRENCE = 1;
    public static final int F_READ_OCCURRENCE  = 2;

    private boolean isEquivalent(ASTNode maybeDeclaration) {
        if (maybeDeclaration == declaration) {
            return true;
        }
        // test for dynamically added fields and methods; they will not be the same instance, so check for equivalence some other way
        if (maybeDeclaration instanceof FieldNode && declaration instanceof FieldNode) {
            FieldNode maybeField = (FieldNode) maybeDeclaration, field = (FieldNode) declaration;
            return maybeField.getName().equals(field.getName()) && maybeField.getDeclaringClass().equals(field.getDeclaringClass());
        } else if (maybeDeclaration instanceof MethodNode && declaration instanceof MethodNode) {
            MethodNode maybeMethod = (MethodNode) maybeDeclaration, method = (MethodNode) declaration;
            return maybeMethod.getName().equals(method.getName()) &&
                maybeMethod.getDeclaringClass().equals(method.getDeclaringClass()) &&
                checkParams(maybeMethod.getOriginal().getParameters(), method.getParameters());
        }
        // check for inner class nodes
        if ((maybeDeclaration instanceof InnerClassNode && declaration instanceof JDTClassNode) ||
                (declaration instanceof InnerClassNode && maybeDeclaration instanceof JDTClassNode)) {
            return ((ClassNode) maybeDeclaration).getName().equals(((ClassNode) declaration).getName());
        }
        return false;
    }

    private static boolean checkParams(Parameter[] maybeParameters, Parameter[] parameters) {
        if (maybeParameters == null) {
            return parameters == null;
        } else if (parameters == null) {
            return false;
        }
        if (maybeParameters.length != parameters.length) {
            return false;
        }
        for (int i = 0; i < parameters.length; i += 1) {
            if (!typeEquals(maybeParameters[i], parameters[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean typeEquals(Parameter maybeParameter, Parameter parameter) {
        ClassNode maybeType = maybeParameter.getType();
        ClassNode type = parameter.getType();
        if (maybeType == null) {
            return type == null;
        } else if (type == null) {
            return false;
        }
        return maybeType.getName().equals(type.getName());
    }
}
