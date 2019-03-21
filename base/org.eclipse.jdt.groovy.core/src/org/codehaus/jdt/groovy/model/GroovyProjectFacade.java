/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.jdt.groovy.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.core.SourceType;
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

    /**
     * best effort to map a groovy node to a JavaElement
     * If the groovy element is not a declaration (eg- an expression or statement)
     * returns instead the closest enclosing element that can be converted
     * to an IJavaElement
     *
     * If node is a local variable declaration, then returns a LocalVariable
     */
    public IJavaElement groovyNodeToJavaElement(ASTNode node, IFile file) {
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        if (!(unit instanceof GroovyCompilationUnit)) {
            // can't go any further, just return the unit instead
            Util.log(IStatus.WARNING, "Trying to get a groovy element from a non-groovy file: " + file.getName());
            return unit;
        }

        try {
            int start = node.getStart();
            IJavaElement elt = unit.getElementAt(start);
            if (node instanceof DeclarationExpression) {
                String var = ((DeclarationExpression) node).getVariableExpression().getName();
                int end = start + var.length() - 1;
                String sig = Signature.createTypeSignature(((DeclarationExpression) node).getVariableExpression().getType().getName(), false);

                return new LocalVariable((JavaElement) elt, var, start, end, start, end, sig, null, 0, false);
            } else {
                return elt;
            }
        } catch (Exception e) {
            Util.log(e, "Error converting from Groovy Element to Java Element: " + node.getText());
        }
        return null;
    }

    public IType groovyClassToJavaType(ClassNode node) {
        if (parent != null && parent.getFullyQualifiedName().equals(node.getName())) {
            return parent;
        }
        try {
            ClassNode toLookFor = node;
            if (GroovyUtils.isAnonymous(node)) {
                toLookFor = node.getOuterClass();
                IType enclosing = groovyClassToJavaType(toLookFor);
                if (enclosing != null && !enclosing.isBinary()) {
                    return fakeAnonymousInnerClass(enclosing, (InnerClassNode) node);
                } else {
                    // if the 'enclosing' is binary we may assume this one is also binary,
                    // so we should just be able to look for it with the type name (including the $ etc.)
                    return project.findType(node.getName(), (IProgressMonitor) null);
                }
            }

            // GRECLIPSE-800: ensure that inner class nodes are handled properly
            String name = toLookFor.getName().replace('$', '.');
            IType type = project.findType(name, (IProgressMonitor) null);
            if (type != null && toLookFor != node) {
                type = type.getType("", 1);
                if (!type.exists()) {
                    type = null;
                }
            }
            return type;
        } catch (JavaModelException e) {
            Util.log(e, "Error converting from Groovy Element to Java Element: " + node.getName());
            return null;
        }
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

    GroovyCompilationUnit groovyModuleToCompilationUnit(ModuleNode node) {
        List<ClassNode> classes = node.getClasses();
        ClassNode classNode = classes.size() > 0 ? (ClassNode) classes.get(0) : null;
        if (classNode != null) {
            IType type = groovyClassToJavaType(classNode);
            if (type instanceof SourceType) {
                return (GroovyCompilationUnit) type.getCompilationUnit();
            }
        }
        Util.log(IStatus.WARNING, "Trying to get GroovyCompilationUnit for non-groovy module: " + node.getDescription());
        return null;
    }

    /**
     * If this fully qualified name is in a groovy file, then return the
     * ClassNode.
     *
     * If this is not a groovy file, then return null
     */
    public ClassNode getClassNodeForName(String name) {
        try {
            IType type = project.findType(name, (IProgressMonitor) null);
            if (type instanceof SourceType) {
                return javaTypeToGroovyClass(type);
            }
        } catch (JavaModelException e) {
            Util.log(e);
        }
        return null;
    }

    private Parameter[] getParametersForTypes(String[] signatures) {
        int n = signatures.length;
        Parameter[] parameters = new Parameter[n];
        for (int i = 0; i < n; i += 1) {
            parameters[i] = new Parameter(ClassHelper.makeWithoutCaching(Signature.toString(signatures[i])), null);
        }
        return parameters;
    }

    private ClassNode javaTypeToGroovyClass(IType type) {
        ICompilationUnit unit = type.getCompilationUnit();
        if (unit instanceof GroovyCompilationUnit) {
            ModuleNode module = ((GroovyCompilationUnit) unit).getModuleNode();
            List<ClassNode> classes = module.getClasses();
            for (ClassNode classNode : classes) {
                if (classNode.getNameWithoutPackage().equals(type.getElementName())) {
                    return classNode;
                }
            }
        }
        return null;
    }

    public List<IType> findAllRunnableTypes() throws JavaModelException {
        final List<IType> results = new ArrayList<IType>();
        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
        for (IPackageFragmentRoot root : roots) {
            if (!root.isReadOnly()) {
                IJavaElement[] children = root.getChildren();
                for (IJavaElement child : children) {
                    if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                        ICompilationUnit[] units = ((IPackageFragment) child).getCompilationUnits();
                        for (ICompilationUnit unit : units) {
                            results.addAll(findAllRunnableTypes(unit));
                        }
                    }
                }
            }
        }
        return results;
    }

    public static List<IType> findAllRunnableTypes(ICompilationUnit unit) throws JavaModelException {
        List<IType> results = new LinkedList<IType>();
        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            if (hasRunnableMain(type)) {
                results.add(type);
            }
        }
        return results;
    }

    public static boolean hasRunnableMain(IType type) {
        try {
            IMethod[] allMethods = type.getMethods();
            for (IMethod method : allMethods) {
                if (method.getElementName().equals("main") &&
                        Flags.isStatic(method.getFlags()) &&
                        // void or Object are valid return types
                        (method.getReturnType().equals("V") || method.getReturnType().endsWith("java.lang.Object;")) &&
                        hasAppropriateArrayArgsForMain(method.getParameterTypes())) {
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
        return (name.equals(typeName)) && (qual == null || qual.equals("java.lang") || qual.equals(""));
    }

    public boolean isGroovyScript(IType type) {
        ClassNode node = javaTypeToGroovyClass(type);
        if (node != null) {
            return node.isScript();
        }
        return false;
    }

    public List<IType> findAllScripts() throws JavaModelException {
        final List<IType> results = new ArrayList<IType>();
        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
        for (IPackageFragmentRoot root : roots) {
            if (!root.isReadOnly()) {
                IJavaElement[] children = root.getChildren();
                for (IJavaElement child : children) {
                    if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                        ICompilationUnit[] units = ((IPackageFragment) child).getCompilationUnits();
                        for (ICompilationUnit unit : units) {
                            if (unit instanceof GroovyCompilationUnit) {
                                for (IType type : unit.getTypes()) {
                                    if (isGroovyScript(type)) {
                                        results.add(type);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    public boolean isGroovyScript(ICompilationUnit unit) {
        if (unit instanceof GroovyCompilationUnit) {
            GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;
            ModuleNode module = gunit.getModuleNode();
            if (module != null) {
                for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
                    if (clazz.isScript()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
