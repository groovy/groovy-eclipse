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
import org.codehaus.groovy.ast.ConstructorNode;
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
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.groovy.search.GenericsMapper;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
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
        
        // check to see if the enclosing element does not enclose the nodeToLookFor
        if (! interestingElement(enclosingElement)) {
            return VisitStatus.CANCEL_MEMBER;
        }
        
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
                    	ClassNode effectiveDeclaringType = declaringType;
                        // find it in the java model
                        if (declaringType.getEnclosingMethod() != null) {
                        	// inner class, assume anonymous
                        	effectiveDeclaringType = declaringType.getEnclosingMethod().getDeclaringClass();
                        }
                        IType type = project.groovyClassToJavaType(declaringType);
                        if (type == null && !unit.isOnBuildPath()) {
                            // try to find it in the current compilation unit
                            type = unit.getType(effectiveDeclaringType.getNameWithoutPackage());
                            if (! type.exists()) {
                                type = null;
                            }
                        }
                        
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
     * @param enclosingElement
     * @return true iff enclosingElement's source location contains the source location of {@link #nodeToLookFor} 
     */
    private boolean interestingElement(IJavaElement enclosingElement) {
        // the clinit is always interesting since the clinit contains static initializers
        if (enclosingElement.getElementName().equals("<clinit>")) {
            return true;
        }
        
        if (enclosingElement instanceof ISourceReference) {
            try {
                ISourceRange range = ((ISourceReference) enclosingElement).getSourceRange();
                return range.getOffset() <= nodeToLookFor.getStart() && range.getOffset() + range.getLength() >= nodeToLookFor.getEnd();
            } catch (JavaModelException e) {
                Util.log(e);
            }
        }
        return false;
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
        ASTNode declaration = result.declaration;
        if (declaration instanceof ClassNode) {
            maybeRequested = type;
        } else if (type.getTypeRoot() != null) {
            if (declaration.getEnd() > 0) {
                // GRECLIPSE-1233 can't use getEltAt because of default parameters.
                // instead, just iterate through children.  Method variants 
                // are always after the original method
                IJavaElement[] children = type.getChildren();
                int start = declaration.getStart();
                int end = declaration.getEnd();
                String name; 
                if (declaration instanceof MethodNode) {
                    name = ((MethodNode) declaration).getName();
                    if (name.equals("<init>")) {
                        name = type.getElementName();
                    }
                } else if (declaration instanceof FieldNode) {
                    name = ((FieldNode) declaration).getName();
                } else if (declaration instanceof PropertyNode) {
                    name = ((PropertyNode) declaration).getName();
                } else {
                    name = declaration.getText();
                }
                for (IJavaElement child : children) {
                    ISourceRange range = ((ISourceReference) child).getSourceRange();
                    if (range.getOffset() <= start && range.getOffset() + range.getLength() >= end && child.getElementName().equals(name)) {
                        maybeRequested = child;
                        break;
                    } else if (start + end < range.getOffset()) {
                        // since children are listed incrementally no need to go further
                        break;
                    }
                }
            }
            if (maybeRequested == null) {
                // try something else because source location not set right
                String name = null;
                int preferredParamNumber = -1;
                if (declaration instanceof MethodNode) {
                    name = ((MethodNode) declaration).getName();
                    Parameter[] parameters = ((MethodNode) declaration).getParameters();
                    preferredParamNumber = parameters == null ? 0 : parameters.length;
                } else if (declaration instanceof PropertyNode) {
                    name = ((PropertyNode) declaration).getName();
                } else if (declaration instanceof FieldNode) {
                    name = ((FieldNode) declaration).getName();
                }
                if (name != null) {
                    maybeRequested = findElement(type, name, preferredParamNumber);
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
        if (declaration instanceof ConstructorNode && maybeRequested.getElementType() == IJavaElement.TYPE) {
            // implicit default constructor. use type instead
            declaration = declaration.getDeclaringClass();
        }
        
        String uniqueKey = createUniqueKey(declaration, result.type, result.declaringType, maybeRequested);
        IJavaElement candidate;
        
        // Create the Groovy Resolved Element, which is like a resolved element, but contains extraDoc, as
        // well as the inferred declaration (which may not be the same as the actual declaration)
        switch (maybeRequested.getElementType()) {
            case IJavaElement.FIELD:
                if (maybeRequested.isReadOnly()) {
                    candidate = new GroovyResolvedBinaryField((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc, result.declaration);
                } else {
                    candidate = new GroovyResolvedSourceField((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc, result.declaration);
                }
                break;
            case IJavaElement.METHOD:
                if (maybeRequested.isReadOnly()) {
                    candidate = new GroovyResolvedBinaryMethod((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), ((IMethod) maybeRequested).getParameterTypes(), uniqueKey, result.extraDoc, result.declaration);
                } else {
                    candidate = new GroovyResolvedSourceMethod((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), ((IMethod) maybeRequested).getParameterTypes(), uniqueKey, result.extraDoc, result.declaration);
                }
                break;
            case IJavaElement.TYPE:
                if (maybeRequested.isReadOnly()) {
                    candidate = new GroovyResolvedBinaryType((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc, result.declaration);
                } else {
                    candidate = new GroovyResolvedSourceType((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc, result.declaration);
                }
                break;
            default:
                candidate = maybeRequested;
        }
        requestedElement = candidate;
        return requestedElement;
    }


    private LocalVariable createLocalVariable(TypeLookupResult result,
            IJavaElement enclosingElement, Variable var) {
        ASTNode node = (ASTNode) var;
        ClassNode type = result.type != null ? result.type : var.getType();
        
        int start;
        if (node instanceof Parameter) {
            start = ((Parameter) node).getNameStart();
        } else {
            start = node.getStart();
        }
        
        // be compatible between 3.6 and 3.7+
        return ReflectionUtils.createLocalVariable(enclosingElement, var.getName(), start, Signature.createTypeSignature(createGenericsAwareName(type, true), false));
    }


    /**
     * Creates the type signature for local variables
     * @param node
     * @return
     */
    private String createGenericsAwareName(ClassNode node, boolean useSimple) {
        StringBuilder sb = new StringBuilder();
        String name = node.getName();
        StringBuilder sbArr;
        if (name.charAt(0) == '[') {
            sbArr = new StringBuilder();
            int arrayCount = 0;
            while (name.charAt(arrayCount) == '[') {
                sbArr.append("[]");
                node = node.getComponentType();
                arrayCount++;
            }
        } else {
            sbArr = null;
        }
            
        if (useSimple) {
            sb.append(node.getNameWithoutPackage());
        } else {
            sb.append(node.getName());
        }

        // recur down the generics
        GenericsType[] genericsTypes = node.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0) {
            sb.append('<');
            for (GenericsType gt : genericsTypes) {
                ClassNode genericsType = gt.getType();
                // determine whether we should use the name of the type parameter
                // or the name of the resolved type
                if (genericsType == null || !genericsType.getName().equals(gt.getName())) {
                    sb.append(useSimple? genericsType.getNameWithoutPackage() : genericsType.getName());
                } else {
                    sb.append(createGenericsAwareName(genericsType, useSimple));
                }
                sb.append(',');
            }
            sb.replace(sb.length()-1, sb.length(), ">");
        }
        if (sbArr != null) {
            sb.append(sbArr);
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
        if (resolvedDeclaringType == null) {
            resolvedDeclaringType = node.getDeclaringClass();
            if (resolvedDeclaringType == null) {
                resolvedDeclaringType = VariableScope.OBJECT_CLASS_NODE;
            }
        }
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
        ClassNode declaringClass = node.getDeclaringClass();
        sb.append(createUniqueKeyForClass(declaringClass, resolvedDeclaringType));
        String name = node.getName();
        sb.append('.').append(name.equals("<init>") ? declaringClass.getNameWithoutPackage() : name);
        sb.append('(');
        if (node.getParameters() != null) {
            for (Parameter param : node.getParameters()) {
                ClassNode paramType = param.getType() != null ? param.getType() : VariableScope.OBJECT_CLASS_NODE;
                sb.append(createUniqueKeyForClass(paramType, resolvedDeclaringType));
            }
        }
        sb.append(')');
        sb.append(createUniqueKeyForResolvedClass(resolvedType));
        return sb;
    }
    
    private StringBuilder createUniqueKeyForField(FieldNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType) {
        StringBuilder sb = new StringBuilder();
        sb.append(createUniqueKeyForClass(node.getDeclaringClass(), resolvedDeclaringType));
        sb.append('.').append(node.getName()).append(')');
        sb.append(createUniqueKeyForResolvedClass(resolvedType));
        return sb;
    }
    
    private StringBuilder createUniqueKeyForGeneratedAccessor(MethodNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType, IField actualField) {
        StringBuilder sb = new StringBuilder();
        sb.append(createUniqueKeyForClass(node.getDeclaringClass(), resolvedDeclaringType));
        sb.append('.').append(actualField.getElementName()).append(')');
        ClassNode typeOfField = node.getName().startsWith("set")  && node.getParameters() != null && node.getParameters().length > 0 ? node.getParameters()[0].getType(): resolvedType;
        sb.append(createUniqueKeyForResolvedClass(typeOfField));
        return sb;
    }
    
    private StringBuilder createUniqueKeyForResolvedClass(ClassNode resolvedType) {
        if (resolvedType.getName().equals("java.lang.Void")) {
            resolvedType = VariableScope.VOID_CLASS_NODE;
        }
        return new StringBuilder(Signature.createTypeSignature(createGenericsAwareName(resolvedType, false/*fully qualified*/), true/*must resolve*/).replace('.', '/'));
    }
    /**
     * tries to resolve any type parameters in unresolvedType based on those in resolvedDeclaringType 
     * @param unresolvedType unresolved type whose type parameters need to be resolved
     * @param resolvedDeclaringType the resolved type that is the context in which to resolve it.
     * @return
     */
    private StringBuilder createUniqueKeyForClass(ClassNode unresolvedType, ClassNode resolvedDeclaringType) {
    	GenericsMapper mapper = GenericsMapper.gatherGenerics(resolvedDeclaringType, resolvedDeclaringType.redirect());
    	ClassNode resolvedType = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(unresolvedType));
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
     * @param preferredParamNumber
     * @return
     * @throws JavaModelException 
     */
    private IJavaElement findElement(IType type, String text, int preferredParamNumber) throws JavaModelException {
        if (text.equals(type.getElementName())) {
            return type;
        }
        if (text.equals("<init>")) {
            text = type.getElementName();
        }
        
        // check for methods first, then fields, and then getter/setter variants of the name
        // these values might be null
        String setMethod = AccessorSupport.SETTER.createAccessorName(text);
        String getMethod = AccessorSupport.GETTER.createAccessorName(text);
        String isMethod = AccessorSupport.ISSER.createAccessorName(text);
        
        IMethod lastFound = null;
        for (IMethod method : type.getMethods()) {
            if (method.getElementName().equals(text)) {
                // prefer methods with the appropriate number of parameters
                // GRECLIPSE-1233 this is not quite right since we really should be
                // trying to find the original methods when default parameters are used
                if (method.getParameterTypes().length == preferredParamNumber) {
                    return method;
                } else {
                    lastFound = method;
                }
            }
        }
        if (lastFound != null) {
            return lastFound;
        }
        
        IField field = type.getField(text);
        String prefix;
        if (!field.exists() && (prefix = extractPrefix(text)) != null) {
            // this is a property
            String newName = Character.toLowerCase(text.charAt(prefix.length())) + text.substring(prefix.length()+1);
            field = type.getField(newName);
        }
        if (field.exists()) {
            return field;
        }
        
        for (IMethod method : type.getMethods()) {
            if (method.getElementName().equals(setMethod) ||
                    method.getElementName().equals(getMethod) ||
                    method.getElementName().equals(isMethod)) {
                return method;
            }
        }
        return null;
    }
    
    private String extractPrefix(String text) {
        if (text.startsWith("is")) {
            if (text.length() > 2) {
                return "is";
            }
        } else if (text.startsWith("get")) {
            if (text.length() > 3) {
                return "get";
            }
        } else if (text.startsWith("set")) {
            if (text.length() > 3) {
                return "set";
            }
        }
        return null;
    }


    public ASTNode getRequestedNode() {
        return requestedNode;
    }
    
    public IJavaElement getRequestedElement() {
        return requestedElement;
    }
}