/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.jdt.groovy.model;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.core.util.Util;

public class GroovyProjectFacade {

    private IType parent;
    private IJavaProject project;

    public GroovyProjectFacade(IJavaElement element) {
        this(element.getJavaProject());
        if (element instanceof IType) {
            parent = (IType) element;
        } else {
            parent = (IType) element.getAncestor(IJavaElement.TYPE);
        }
    }

    public GroovyProjectFacade(IJavaProject project) {
        this.project = project;
    }

    public IJavaProject getProject() {
        return project;
    }

    @Deprecated // use org.codehaus.jdt.groovy.model.JavaCoreUtil.findType(String, IJavaElement)
    public IType groovyClassToJavaType(ClassNode node) {
        if (parent != null && parent.getFullyQualifiedName().equals(node.getName())) {
            return parent;
        }
        IType type = null;
        if (GroovyUtils.isAnonymous(node)) {
            type = JavaCoreUtil.findType(node.getOuterClass().getName(), project);
            if (type != null && type.exists() && !type.isBinary()) {
                type = fakeAnonymousInnerClass(type, (InnerClassNode) node);
            } else {
                type = null;
            }
        }
        if (type == null) {
            type = JavaCoreUtil.findType(node.getName(), project);
        }
        return type;
    }

    private IType fakeAnonymousInnerClass(IType outer, final InnerClassNode inner) {
        return new ResolvedSourceType((JavaElement) outer, inner.getName(), GroovyUtils.getTypeSignature(inner, true, true)) {
            @Override
            public Object getElementInfo() throws JavaModelException {
                try {
                    return super.getElementInfo();
                } catch (JavaModelException jme) {
                    if (!jme.getJavaModelStatus().isDoesNotExist()) {
                        throw jme;
                    }
                    final IType anonType = this;
                    return new org.eclipse.jdt.internal.core.SourceTypeElementInfo() {{
                        setHandle(anonType);
                        setFlags(inner.getModifiers());

                        ClassNode[] faces = inner.getInterfaces();
                        char[][] names = new char[faces.length][];
                        for (int i = 0; i < faces.length; i += 1) {
                            names[i] = faces[i].getName().toCharArray();
                        }
                        setSuperInterfaceNames(names);
                        setSuperclassName(inner.getUnresolvedSuperClass().getName().toCharArray());

                        setNameSourceStart(inner.getNameStart());
                        setNameSourceEnd(inner.getNameEnd());
                        setSourceRangeStart(inner.getStart());
                        setSourceRangeEnd(inner.getEnd());
                    }};
                }
            }

            @Override
            public IField getField(String fieldName) {
                final FieldNode fieldNode = inner.getDeclaredField(fieldName);
                if (fieldNode == null) {
                    return super.getField(fieldName);
                }
                String uniqueKey = GroovyUtils.getTypeSignature(fieldNode.getDeclaringClass(), true, true) +
                    Signature.C_DOT + fieldName + ")" + GroovyUtils.getTypeSignature(fieldNode.getType(), true, true);
                return new ResolvedSourceField(this, fieldName, uniqueKey) {
                    @Override // NOTE: Copied from GroovyResolvedSourceField:
                    public Object getElementInfo() throws JavaModelException {
                        try {
                            return super.getElementInfo();
                        } catch (JavaModelException jme) {
                            if (!jme.getJavaModelStatus().isDoesNotExist()) {
                                throw jme;
                            }
                            return new org.eclipse.jdt.internal.core.SourceFieldElementInfo() {{
                                setTypeName(fieldNode.getType().toString(false).toCharArray());
                                setNameSourceStart(fieldNode.getNameStart());
                                setNameSourceEnd(fieldNode.getNameEnd());
                                setSourceRangeStart(fieldNode.getStart());
                                setSourceRangeEnd(fieldNode.getEnd());
                                setFlags(fieldNode.getModifiers());
                            }};
                        }
                    }
                };
            }

            @Override
            public IMethod getMethod(String methodName, String[] parameterTypeSignatures) {
                final MethodNode methodNode = inner.getDeclaredMethod(methodName, getParametersForTypes(parameterTypeSignatures));
                if (methodNode == null) {
                    return super.getMethod(methodName, parameterTypeSignatures);
                }
                String uniqueKey = GroovyUtils.getTypeSignature(methodNode.getDeclaringClass(), true, true) +
                    Signature.C_DOT + methodName + Signature.C_PARAM_START /*+ type of each param*/ + Signature.C_PARAM_END +
                    GroovyUtils.getTypeSignature(methodNode.getReturnType(), true, true); // if exceptions, Signature.C_INTERSECTION + exception type
                return new ResolvedSourceMethod(this, methodName, parameterTypeSignatures, uniqueKey) {
                    @Override // NOTE: Copied from GroovyResolvedSourceMethod:
                    public Object getElementInfo() throws JavaModelException {
                        try {
                            return super.getElementInfo();
                        } catch (JavaModelException jme) {
                            if (!jme.getJavaModelStatus().isDoesNotExist()) {
                                throw jme;
                            }
                            return new org.eclipse.jdt.internal.core.SourceMethodInfo() {{
                                setReturnType(methodNode.getReturnType().getNameWithoutPackage().toCharArray());
                                //setExceptionTypeNames(buildExceptionTypeNames(methodNode.getExceptions()));
                                //setArgumentNames(buildArgumentNames(methodNode.getParameters()));
                                setNameSourceStart(methodNode.getNameStart());
                                setNameSourceEnd(methodNode.getNameEnd());
                                setSourceRangeStart(methodNode.getStart());
                                setSourceRangeEnd(methodNode.getEnd());
                                setFlags(methodNode.getModifiers());
                            }};
                        }
                    }
                };
            }
        };
    }

    private Parameter[] getParametersForTypes(String[] signatures) {
        int n = signatures.length;
        Parameter[] parameters = new Parameter[n];
        for (int i = 0; i < n; i += 1) {
            parameters[i] = new Parameter(javaTypeToGroovyClass(signatures[i]), null);
        }
        return parameters;
    }

    //--------------------------------------------------------------------------

    public static boolean hasRunnableMain(IType type) {
        try {
            IMethod[] allMethods = type.getMethods();
            for (IMethod method : allMethods) {
                if (method.getElementName().equals("main") && Flags.isStatic(method.getFlags()) && // void or Object are valid return types
                    (method.getReturnType().equals("V") || method.getReturnType().endsWith("java.lang.Object;")) && hasAppropriateArrayArgsForMain(method.getParameterTypes())) {

                    return true;
                }
            }
        } catch (JavaModelException e) {
            Util.log(e, "Exception searching for main method in " + type);
        }
        return false;
    }

    private static boolean hasAppropriateArrayArgsForMain(final String[] params) {
        if (params == null || params.length != 1) {
            return false;
        }
        int array = Signature.getArrayCount(params[0]);
        String typeName;
        if (array == 1) {
            typeName = "String";
        } else if (array == 0) {
            typeName = "Object";
        } else {
            return false;
        }

        String sigNoArray = Signature.getElementType(params[0]);
        String name = Signature.getSignatureSimpleName(sigNoArray);
        String qual = Signature.getSignatureQualifier(sigNoArray);
        return (name.equals(typeName)) && (qual == null || qual.isEmpty() || "java.lang".equals(qual));
    }

    public static boolean isGroovyScript(IType type) {
        ClassNode node = javaTypeToGroovyClass(type);
        if (node != null) {
            return node.isScript();
        }
        return false;
    }

    private static ClassNode javaTypeToGroovyClass(IType type) {
        ICompilationUnit unit = type.getCompilationUnit();
        if (unit instanceof GroovyCompilationUnit) {
            ModuleNode module = ((GroovyCompilationUnit) unit).getModuleNode();
            for (ClassNode classNode : module.getClasses()) {
                if (classNode.getNameWithoutPackage().equals(type.getElementName())) {
                    return classNode;
                }
            }
        }
        return null;
    }

    private static ClassNode javaTypeToGroovyClass(String signature) {
        int dims = Signature.getArrayCount(signature); // TODO: handle generics types
        String type = Signature.toString(Signature.getTypeErasure(signature.substring(dims)));

        ClassNode node = ClassHelper.make(type);
        while (dims-- > 0) {
            node = node.makeArray();
        }
        return node;
    }
}
