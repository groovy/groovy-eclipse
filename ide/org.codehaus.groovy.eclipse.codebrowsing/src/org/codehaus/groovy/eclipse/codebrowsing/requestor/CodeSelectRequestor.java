 /*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A type requestor for Code selection (i.e., hovers and open declaration)
 * @author Andrew Eisenberg
 * @created Nov 4, 2009
 *
 */
public class CodeSelectRequestor implements ITypeRequestor {
    
    private final ASTNode nodeToLookFor;
    
    private IJavaElement requestedElement;
    
    private final GroovyProjectFacade project;
    
    private final GroovyCompilationUnit unit;
    
    public CodeSelectRequestor(ASTNode nodeToLookFor, GroovyCompilationUnit unit) {
        this.nodeToLookFor = nodeToLookFor;
        this.unit = unit;
        this.project = new GroovyProjectFacade(unit);
    }
    

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
            IJavaElement enclosingElement) {
        if (node == nodeToLookFor) {
            if (result.declaration != null) {
                if (result.declaration instanceof VariableExpression) {
                    // look in the local scope
                    VariableExpression var = (VariableExpression) result.declaration;
                    requestedElement = 
                        new LocalVariable((JavaElement) enclosingElement, var.getName(), var.getStart(), var.getEnd()-1, var.getStart(), var.getEnd()-1, 
                                Signature.createTypeSignature(var.getType().getName(), false), new Annotation[0]);
                } else if (result.declaration instanceof Parameter) {
                    // look in the local scope
                    Parameter var = (Parameter) result.declaration;
                    try {
                        requestedElement = 
                            new LocalVariable((JavaElement) unit.getElementAt(var.getStart()-1), var.getName(), var.getStart(), var.getEnd()-1, var.getStart(), var.getEnd()-1, 
                                    Signature.createTypeSignature(var.getType().getName(), false), new Annotation[0]);
                    } catch (JavaModelException e) {
                        Util.log(e, "Problem getting element at " + (var.getStart()-1) + " for file " + unit.getElementName());
                    }
                } else {
                    ClassNode declaringType = null;
                    if (result.declaringType != null) {
                        declaringType = removeArray(result.declaringType);
                    } else {
                        if (result.declaration instanceof FieldNode) {
                            declaringType = ((FieldNode) result.declaration).getDeclaringClass();
                        } else if (result.declaration instanceof MethodNode) {
                            declaringType = ((MethodNode) result.declaration).getDeclaringClass();
                        } else if (result.declaration instanceof PropertyNode) {
                            declaringType = ((PropertyNode) result.declaration).getDeclaringClass();
                        } else if (result.declaration instanceof ClassNode) {
                            declaringType = removeArray((ClassNode) result.declaration);
                        }
                    }
                    if (declaringType != null) {
                        // find it in the java model
                        IType type = project.groovyClassToJavaType(declaringType);
                        if (type != null) {
                            try {
                                if (result.declaration instanceof ClassNode) {
                                    requestedElement = type;
                                } else if (type.getTypeRoot() != null) {
                                    if (result.declaration.getEnd() > 0) {
                                        requestedElement = type.getTypeRoot().getElementAt(result.declaration.getStart());
                                    }
                                    if (requestedElement == null) {
                                        // try something else because source location not set right
                                        String name = null;
                                        if (result.declaration instanceof MethodNode) {
                                            name = ((MethodNode) result.declaration).getName();
                                        } else if (result.declaration instanceof PropertyNode) {
                                                name = ((PropertyNode) result.declaration).getName();
                                        } else if (result.declaration instanceof FieldNode) {
                                            name = ((FieldNode) result.declaration).getName();
                                        }
                                        if (name != null) {
                                            requestedElement = findElement(type, name);
                                        }
                                    }
                                }
                            } catch (JavaModelException e) {
                                GroovyCore.logException("Problem with code selection for ASTNode: " + node, e);
                            }
                        }
                    }
                }
            }
            return VisitStatus.STOP_VISIT;
        }
        return VisitStatus.CONTINUE;
    }

    /**
     * @param declaration
     * @return
     */
    private ClassNode removeArray(ClassNode declaration) {
        return declaration.getComponentType() != null ? removeArray(declaration.getComponentType()) : declaration;
    }


    /**
     * @param type
     * @param text
     * @return
     * @throws JavaModelException 
     */
    private IJavaElement findElement(IType type, String text) throws JavaModelException {
        if (text.equals(type.getElementName())) {
            return type;
        }
        
        String capitalized = Character.toTitleCase(text.charAt(0)) + text.substring(1);
        String setMethod = "set" + capitalized;
        String getMethod = "get" + capitalized;
        
        for (IMethod method : type.getMethods()) {
            if (method.getElementName().equals(text) || 
                    method.getElementName().equals(setMethod) ||
                    method.getElementName().equals(getMethod)) {
                return method;
            }
        }
        
        return type.getField(text);
    }


    public IJavaElement getRequestedElement() {
        return requestedElement;
    }
}
