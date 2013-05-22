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
package org.codehaus.groovy.eclipse.core.model;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.SourceType;
/**
 * @author Andrew Eisenberg
 * @created May 29, 2009
 *
 * This class provides some useful methods for accessing Groovy state
 */
public class GroovyProjectFacade {

    public static boolean isGroovyProject(IProject proj) {
         try {
            return proj.hasNature(GroovyNature.GROOVY_NATURE);
        } catch (CoreException e) {
            GroovyCore.logException("Error getting project nature: " + proj.getName(), e);
            return false;
        }
     }


    private IJavaProject project;

     public GroovyProjectFacade(IJavaProject project) {
         this.project = project;
     }

     public GroovyProjectFacade(IJavaElement elt) {
         this.project = elt.getJavaProject();
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
         if (! (unit instanceof GroovyCompilationUnit)) {
             // can't go any further, just return the unit instead
             GroovyCore.logWarning("Trying to get a groovy element from a non-groovy file: " + file.getName());
             return unit;
         }

         try {
             int start = node.getStart();
             IJavaElement elt = unit.getElementAt(start);

             if (node instanceof DeclarationExpression) {
                 int end = node.getEnd();

                // Local variable signature has changed between 3.6 and 3.7, use
                // reflection to create.
                return ReflectionUtils.createLocalVariable(elt, ((DeclarationExpression) node).getVariableExpression().getName(),
                        start, Signature.createTypeSignature(((DeclarationExpression) node).getVariableExpression().getType()
                                .getName(), false));
             } else {
                 return elt;
             }
         } catch (Exception e) {
             GroovyCore.logException("Error converting from Groovy Element to Java Element: " + node.getText(), e);
         }
         return null;
     }

     public IType groovyClassToJavaType(ClassNode node) {
         try {
            // GRECLIPSE-1628 handle anonymous inner classes. Only go one level
            // deep
            // FIXADE ignore rest
            ClassNode toLookFor = node;
            if (node.getEnclosingMethod() != null) {
                toLookFor = node.getEnclosingMethod().getDeclaringClass();
                IType enclosing = groovyClassToJavaType(node.getEnclosingMethod().getDeclaringClass());
                if (enclosing != null) {
                    return findAnonymousInnerClass(enclosing, (InnerClassNode) node);
                } else {
                    return null;
                }
            }
            // GRECLIPSE-800 Ensure that inner class nodes are handled properly
            String name = toLookFor.getName().replace('$', '.');
            IType type = project.findType(name, new NullProgressMonitor());
            if (type != null && toLookFor != node) {
                type = type.getType("", 1);
                if (!type.exists()) {
                    type = null;
                }
            }
            return type;
        } catch (JavaModelException e) {
            GroovyCore.logException("Error converting from Groovy Element to Java Element: " + node.getName(), e);
            return null;
        }
     }

    private IType findAnonymousInnerClass(IType enclosing, InnerClassNode anon) throws JavaModelException {
        IMethod[] children = enclosing.getMethods();
        MethodNode enclosingMethod = anon.getEnclosingMethod();
        if (enclosingMethod == null) {
            return null;
        }
        Parameter[] parameters = enclosingMethod.getParameters();
        if (parameters == null) {
            parameters = new Parameter[0];
        }
        for (IMethod child : children) {
            if (child.getElementName().equals(enclosingMethod.getName())) {
                String[] names = child.getParameterNames();
                if (names.length == parameters.length) {
                    // FIXADE for now, only look for a single inner type
                    // check the names, not the types since we don't know the
                    // full types of the IMethod.
                    // This could go wrong, but it's safe enoug
                    for (int i = 0; i < names.length; i++) {
                        if (!names[i].equals(parameters[i].getName())) {
                            continue;
                        }
                    }
                    IType found = child.getType("", 1);
                    if (found.exists()) {
                        return found;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

     GroovyCompilationUnit groovyModuleToCompilationUnit(ModuleNode node) {
    	 List classes = node.getClasses();
         ClassNode classNode = classes.size() > 0 ? (ClassNode) classes.get(0) : null;
         if (classNode != null) {
             IType type = groovyClassToJavaType(classNode);
             if (type instanceof SourceType) {
                 return (GroovyCompilationUnit) type.getCompilationUnit();
             }
         }
         GroovyCore.logWarning("Trying to get GroovyCompilationUnit for non-groovy module: " + node.getDescription());
         return null;
     }

    /**
     * If this fully qualified name is in a groovy file, then return the
     * classnode
     *
     * If this is not a groovy file, then return null
     *
     * @param name
     * @return
     */
     public ClassNode getClassNodeForName(String name) {
         try {
            IType type = project.findType(name, new NullProgressMonitor());
             if (type instanceof SourceType) {
                 return javaTypeToGroovyClass(type);
             }
        } catch (JavaModelException e) {
            GroovyCore.logException(e.getMessage(), e);
        }
        return null;
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
        final List<IType> results = newList();
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
                        (method.getReturnType().equals("V") ||
                                method.getReturnType().endsWith("java.lang.Object;")) &&
                        hasAppropriateArrayArgsForMain(method.getParameterTypes())) {
                    return true;
                }
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Exception searching for main method in " + type, e);
        }
        return false;
    }

    private static boolean hasAppropriateArrayArgsForMain(
            final String[] params) {
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
        return (name.equals(typeName)) &&
            (qual == null || qual.equals("java.lang") || qual.equals(""));
    }

    public IJavaProject getProject() {
        return project;
    }

    public boolean isGroovyScript(IType type) {
        ClassNode node = javaTypeToGroovyClass(type);
        if (node != null) {
            return node.isScript();
        }
        return false;
    }

    public List<IType> findAllScripts() throws JavaModelException {
        final List<IType> results = newList();
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
            if (module !=  null) {
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
