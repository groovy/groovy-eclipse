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
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryField;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryMethod;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryType;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceField;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceMethod;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceType;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
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
    
    private ASTNode requestedNode;
    
    private final GroovyProjectFacade project;
    
    private final GroovyCompilationUnit unit;
    
    public CodeSelectRequestor(ASTNode nodeToLookFor, GroovyCompilationUnit unit) {
        this.nodeToLookFor = nodeToLookFor;
        this.unit = unit;
        this.project = new GroovyProjectFacade(unit);
    }
    

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
            IJavaElement enclosingElement) {
        if (node instanceof ImportNode) {
            node = ((ImportNode) node).getType();
            if (node == null) {
                return VisitStatus.CONTINUE;
            }
                
        }
        
        if (doTest(node)) {
            requestedNode = result.declaration;
            if (requestedNode instanceof ClassNode) {
                requestedNode = ((ClassNode) requestedNode).redirect();
            }
            if (requestedNode != null) {
                if (result.declaration instanceof VariableExpression) {
                    // look in the local scope
                    VariableExpression var = (VariableExpression) result.declaration;
                    requestedElement = 
                        createLocalVariable(result, enclosingElement, var);
                } else if (result.declaration instanceof Parameter) {
                    // look in the local scope
                    Parameter var = (Parameter) result.declaration;
                    int position = var.getStart()-1;
                    if (position < 0) {
                        // could be an implicit variable like 'it'
                        position = nodeToLookFor.getStart()-1;
                    }
                    try {
                        requestedElement = 
                            createLocalVariable(result, (JavaElement) unit.getElementAt(position), var);
                    } catch (JavaModelException e) {
                        Util.log(e, "Problem getting element at " + position + " for file " + unit.getElementName());
                    }
                } else {
                    ClassNode declaringType = findDeclaringType(result);
                    if (declaringType != null) {
                        // find it in the java model
                        IType type = project.groovyClassToJavaType(declaringType);
                        if (type != null) {
                            try {
                                // find the requested java element
                                IJavaElement maybeRequested = findRequestedElement(result, type);
                                // try to resolve the type of the requested element.  this will add the proper type parameters, etc to the hover
                                requestedElement = resolveRequestedElement(result, maybeRequested);
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
     * find the declaring type, removing any array
     * @param result
     * @return
     */
    private ClassNode findDeclaringType(TypeLookupResult result) {
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
        return declaringType;
    }


    /**
     * @param result
     * @param type
     * @throws JavaModelException
     */
    private IJavaElement findRequestedElement(TypeLookupResult result, IType type)
            throws JavaModelException {
        IJavaElement maybeRequested = null;
        if (result.declaration instanceof ClassNode) {
            maybeRequested = type;
        } else if (type.getTypeRoot() != null) {
            if (result.declaration.getEnd() > 0) {
                maybeRequested = type.getTypeRoot().getElementAt(result.declaration.getStart());
            }
            if (maybeRequested == null) {
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
                    maybeRequested = findElement(type, name);
                }
                if (maybeRequested == null) {
                    // still couldn't find anything
                    maybeRequested = type;
                }
            }
            
        }
        return maybeRequested;
    }


    /**
     * Converts the maybeRequested element into a resolved element by creating a unique key for it.
     */
    private IJavaElement resolveRequestedElement(TypeLookupResult result, IJavaElement maybeRequested) {
        AnnotatedNode declaration = (AnnotatedNode) result.declaration;
        if (declaration instanceof PropertyNode && maybeRequested instanceof IMethod) {
            // the field associated with this property does not exist, use the method instead
            String getterName = maybeRequested.getElementName();
            MethodNode maybeDeclaration = (MethodNode) declaration.getDeclaringClass().getMethods(getterName).get(0);
            declaration = maybeDeclaration == null ? declaration : maybeDeclaration;
        }
        
        String uniqueKey = createUniqueKey(declaration, result.type, result.declaringType, maybeRequested);
        switch (maybeRequested.getElementType()) {
            case IJavaElement.FIELD:
                if (maybeRequested.isReadOnly()) {
                    requestedElement = new GroovyResolvedBinaryField((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc);
                } else {
                    requestedElement = new GroovyResolvedSourceField((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc);
                }
                break;
            case IJavaElement.METHOD:
                if (maybeRequested.isReadOnly()) {
                    requestedElement = new GroovyResolvedBinaryMethod((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), ((IMethod) maybeRequested).getParameterTypes(), uniqueKey, result.extraDoc);
                } else {
                    requestedElement = new GroovyResolvedSourceMethod((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), ((IMethod) maybeRequested).getParameterTypes(), uniqueKey, result.extraDoc);
                }
                break;
            case IJavaElement.TYPE:
                if (maybeRequested.isReadOnly()) {
                    requestedElement = new GroovyResolvedBinaryType((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc);
                } else {
                    requestedElement = new GroovyResolvedSourceType((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc);
                }
                break;
            default:
                requestedElement = maybeRequested;
        }
        return requestedElement;
    }


    private LocalVariable createLocalVariable(TypeLookupResult result,
            IJavaElement enclosingElement, Variable var) {
        ASTNode node = (ASTNode) var;
        ClassNode type = result.type != null ? result.type : var.getType();
        return new LocalVariable((JavaElement) enclosingElement, var.getName(), node.getStart(), node.getEnd()-1, node.getStart(), node.getEnd()-1, 
                Signature.createTypeSignature(createGenericsAwareName(type, true), false), new Annotation[0]);
    }


    /**
     * Creates the type signature for local variables
     * @param node
     * @return
     */
    private String createGenericsAwareName(ClassNode node, boolean useSimple) {
        StringBuilder sb = new StringBuilder();
        String name = node.getName();
        if (name.charAt(0) == '[') {
            int arrayCount = 0;
            while (name.charAt(arrayCount) == '[') {
                sb.append("[]");
                node = node.getComponentType();
                arrayCount++;
            }
        }
            
        if (useSimple) {
            sb.insert(0, node.getNameWithoutPackage());
        } else {
            sb.insert(0, node.getName());
        }

        // recur down the generics
        GenericsType[] genericsTypes = node.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0) {
            sb.append('<');
            for (GenericsType gt : genericsTypes) {
                sb.append(createGenericsAwareName(gt.getType() == null ? VariableScope.OBJECT_CLASS_NODE : gt.getType(), useSimple));
                sb.append(',');
            }
            sb.replace(sb.length()-1, sb.length(), ">");
        }
        return sb.toString();
    }

    
    /**
     * Creates the unique key for classes, fields and methods
     * @param node
     * @param maybeRequested 
     * @return
     */
    private String createUniqueKey(AnnotatedNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType, IJavaElement maybeRequested) {
        StringBuilder sb = new StringBuilder();
        if (node instanceof PropertyNode) {
            node = ((PropertyNode) node).getField();
        }
        if (node instanceof FieldNode) {
            return createUniqueKeyForField((FieldNode) node, resolvedType, resolvedDeclaringType).toString();
        } else if (node instanceof MethodNode) {
            if (maybeRequested.getElementType() == IJavaElement.FIELD) {
                // this is likely a generated getter or setter
                return createUniqueKeyForGeneratedAccessor((MethodNode) node, resolvedType, resolvedDeclaringType, (IField) maybeRequested).toString();
            } else {
                return createUniqueKeyForMethod((MethodNode) node, resolvedType, resolvedDeclaringType).toString();
            }
        } else if (node instanceof ClassNode) {
            return createUniqueKeyForClass(resolvedType, resolvedDeclaringType).toString();
        }
        return sb.toString();
    }

    private StringBuilder createUniqueKeyForMethod(MethodNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType) {
        StringBuilder sb = new StringBuilder();
        sb.append(createUniqueKeyForClass(node.getDeclaringClass(), resolvedDeclaringType));  // won't resolve type params
        sb.append('.').append(node.getName());
        sb.append('(');
        if (node.getParameters() != null) {
            for (Parameter param : node.getParameters()) {
                ClassNode paramType = param.getType() != null ? param.getType() : VariableScope.OBJECT_CLASS_NODE;
                sb.append(createUniqueKeyForClass(paramType, resolvedDeclaringType));  // won't resolve type params
            }
        }
        sb.append(')');
        sb.append(createUniqueKeyForResolvedClass(resolvedType));
        return sb;
    }
    
    private StringBuilder createUniqueKeyForField(FieldNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType) {
        StringBuilder sb = new StringBuilder();
        sb.append(createUniqueKeyForClass(node.getDeclaringClass(), resolvedDeclaringType));  // won't resolve type params
        sb.append('.').append(node.getName()).append(')');
        sb.append(createUniqueKeyForResolvedClass(resolvedType));
        return sb;
    }
    
    private StringBuilder createUniqueKeyForGeneratedAccessor(MethodNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType, IField actualField) {
        StringBuilder sb = new StringBuilder();
        sb.append(createUniqueKeyForClass(node.getDeclaringClass(), resolvedDeclaringType));  // won't resolve type params
        sb.append('.').append(actualField.getElementName()).append(')');
        sb.append(createUniqueKeyForResolvedClass(resolvedType));
        return sb;
    }
    
    private StringBuilder createUniqueKeyForResolvedClass(ClassNode resolvedType) {
        return new StringBuilder(Signature.createTypeSignature(createGenericsAwareName(resolvedType, false/*fully qualified*/), true/*must resolve*/).replace('.', '/'));
    }
    
    // tries to resolve any type parameters in unresolvedType based on those in resolvedDeclaringType
    private StringBuilder createUniqueKeyForClass(ClassNode unresolvedType, ClassNode resolvedDeclaringType) {
        // first try to resolve generics
        ClassNode unresolvedDeclaringType = resolvedDeclaringType.redirect();
        ClassNode resolvedType;
        GenericsType[] resolvedGenerics = resolvedDeclaringType.getGenericsTypes();
        GenericsType[] unresolvedGenerics = unresolvedDeclaringType.getGenericsTypes();
        if (resolvedGenerics != null && unresolvedGenerics != null) {
            resolvedType = VariableScope.resolveTypeParameterization(resolvedGenerics, unresolvedGenerics,
                    VariableScope.clone(unresolvedType));
        } else {
            resolvedType = unresolvedType;
        }
        return createUniqueKeyForResolvedClass(resolvedType);
    }
    
    private boolean doTest(ASTNode node) {
        return node.getClass() == nodeToLookFor.getClass() && nodeToLookFor.getStart() == node.getStart() && nodeToLookFor.getEnd() == node.getEnd();
    }

    /**
     * @param declaration
     * @return
     */
    private ClassNode removeArray(ClassNode declaration) {
        return declaration.getComponentType() != null ? removeArray(declaration.getComponentType()) : declaration;
    }


    /**
     * May return null
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
        
        IField field = type.getField(text);
        if (!field.exists() && text.length() > 3 &&
                (text.startsWith("get") || text.startsWith("set"))) {
            // this is a property
            String newName = Character.toLowerCase(text.charAt(3)) + text.substring(4);
            field = type.getField(newName);
        }
        if (field.exists()) {
            return field;
        } else {
            return null;
        }
    }


    public ASTNode getRequestedNode() {
        return requestedNode;
    }
    
    public IJavaElement getRequestedElement() {
        return requestedElement;
    }
}