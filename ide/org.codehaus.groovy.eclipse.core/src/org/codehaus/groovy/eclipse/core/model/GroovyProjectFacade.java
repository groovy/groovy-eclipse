/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.model;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;
import static org.codehaus.groovy.eclipse.core.util.SetUtil.linkedSet;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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
             int line = node.getLineNumber();
             int col = node.getColumnNumber();
             IDocument doc = new Document(new String(((GroovyCompilationUnit) unit).getContents()));
             int start = node.getStart();
             IJavaElement elt = unit.getElementAt(start);
             
             if (node instanceof DeclarationExpression) {
                 int end = node.getEnd();
                 return new LocalVariable(
                         (JavaElement) elt, ((DeclarationExpression) node).getVariableExpression().getName(),
                         start, end, start, end, 
                         Signature.createTypeSignature(((DeclarationExpression) node).getVariableExpression().getType().getName(), false),
                         new org.eclipse.jdt.internal.compiler.ast.Annotation[0]);
             } else {
                 return elt;
             }
         } catch (Exception e) {
             GroovyCore.logException("Error converting from Groovy Element to Java Element: " + node.getText(), e);
         }
         return null;
     }
     
//     public ASTNode javaElementToGroovyNode(IJavaElement elt) {
//         ICompilationUnit unit = (ICompilationUnit) 
//                 elt.getAncestor(IJavaElement.COMPILATION_UNIT);
//         
//     }
     
     public IType groovyClassToJavaType(ClassNode node) {
         try {
            return project.findType(node.getName(), new NullProgressMonitor());
        } catch (JavaModelException e) {
            GroovyCore.logException("Error converting from Groovy Element to Java Element: " + node.getName(), e);
            return null;
        }
     }
     
     GroovyCompilationUnit groovyModuleToCompilationUnit(ModuleNode node) {
         ClassNode classNode = node.getClasses().get(0);
         IType type = groovyClassToJavaType(classNode);
         if (type instanceof SourceType) {
             return (GroovyCompilationUnit) type.getCompilationUnit();
         } else {
             GroovyCore.logWarning("Trying to get GroovyCompilationUnit for non-groovy module: " + classNode.getName());
             return null;
         } 
     }
     
     /**
      * @return The class loader used by the project for compiling, or null if
      *         one is not available.
      * @throws CoreException
      */
     public ClassLoader getProjectClassLoader() {
         try {
             return newProjectClassLoader();
         } catch (CoreException e) {
             GroovyCore.logException("Exception creating project classpath for " + project, e);
             return new ClassLoader() {};
         }
     }

     /**
      * @return A new class loader for the project.
      * @throws CoreException
      */
     public ClassLoader newProjectClassLoader() throws CoreException {
 		return new URLClassLoader(getClassPathAsUrls());
     }
     /**
      * @return The class path as URLs, useful for creating class loaders.
      * @param excludeGroovyRuntime Flag to exclude the runtime. The runtime
      *        should be excuded for IDE code, e.g. completion and building, but
      *        included for launchers etc.
      * @throws CoreException
      */
     public URL[] getClassPathAsUrls() throws CoreException {
         final List<String> classPath = newList(getOSClassPath().split(File.pathSeparator));
         final List<URL> classpathUrls = new LinkedList<URL>();
         for (String urlString : classPath) {
             try {
                 classpathUrls.add(new File(urlString).toURI().toURL());
             } catch(final MalformedURLException e) {
                 String msg = "Error converting File to URL: " + urlString;
                 GroovyCore.logException(msg, e);
                 throw new CoreException(new Status(IStatus.ERROR, GroovyCoreActivator.PLUGIN_ID, msg, e));
             }
         }
         return classpathUrls.toArray(new URL[classpathUrls.size()]);
     }
     
     public ClassNode getClassNodeForName(String name) {
         try {
            IType type = project.findType(name, new NullProgressMonitor());
             if (type instanceof SourceType) { 
                 ICompilationUnit unit = type.getCompilationUnit();
                 if (unit instanceof GroovyCompilationUnit) {
                     ModuleNode module = ((GroovyCompilationUnit) unit).getModuleNode();
                     List<ClassNode> classes = module.getClasses();
                     for (ClassNode classNode : classes) {
                        if (classNode.getName().equals(name)) {
                            return classNode;
                        }
                    }
                 }
             }
        } catch (JavaModelException e) {
            GroovyCore.logException(e.getMessage(), e);
        }
         return null;
     }
     
     // XXX This whole way of gathering the classpath is wrong.  Should be using the resolvedClasspath of the project
     /**
      * Get class path string specified using the OS specific path separator.
      * 
      * @return The class path or an empty string if no class path is defined.
      * @throws CoreException
      */
     public String getOSClassPath() throws CoreException {
        final Set<String> setOfClassPath = getClasspath(project,
                newList(new IJavaProject[0]));
        if (setOfClassPath.size() == 0)
            return "";
        final StringBuffer classPath = new StringBuffer();
        final Iterator<String> iter = setOfClassPath.iterator();
        while (iter.hasNext()) {
            classPath.append(iter.next().toString()).append(File.pathSeparator);
        }
        return classPath.substring(0, classPath.length() - 1);
    }

     public static Set<String> getClasspath(final IJavaProject project,
            final List<IJavaProject> visited) throws CoreException {
        final Set<String> set = linkedSet();
        if (project == null || !project.exists())
            return set;
        if (visited.contains(project))
            return set;
        visited.add(project);
        collectPackageFragmentRootPaths(set, project, visited);
        collectClassPathEntryPaths(set, project, visited);
        if (!project.getProject().hasNature(GroovyNature.GROOVY_NATURE))
            return set;
        final String outputPath = getAbsoluteOutPath(project);
        if (!outputPath.trim().equals(""))
            set.add(outputPath);
        return set;
    }

    private static String getAbsoluteOutPath(final IJavaProject project)
            throws JavaModelException {
        return project.getProject().getWorkspace().getRoot().getFolder(project.getOutputLocation()).getLocation().toOSString();
    }

    private static void collectPackageFragmentRootPaths(
            final Set<String> results, final IJavaProject project,
            final List visited) throws JavaModelException {
        final IPackageFragmentRoot[] fragRoots = project
                .getPackageFragmentRoots();
        for (final IPackageFragmentRoot fragRoot : fragRoots) {
            final IResource resource = fragRoot.getCorrespondingResource();
            // Fix for: GROOVY-1825 - emp
            // The first project visited is the source or the compile request.
            // Its source folder is placed on the
            // class path.
            // External project source folders must not be placed on the class
            // path as the source code will be compiled,
            // however their classes will appear in this projects output folder
            // which is incorrect behaviour.
            // So check visit count == 1 to prevent other source paths from
            // being added to the class path.
            if (resource != null && visited.size() == 1) {
                results.add(resource.getLocation().toOSString());
            } else {
                results.add(fragRoot.getPath().toOSString());
            }
        }
    }

    private static void collectClassPathEntryPaths(final Set<String> results,
            final IJavaProject project, final List<IJavaProject> visited)
            throws CoreException, JavaModelException {
        final IWorkspaceRoot root = getWorkspace().getRoot();
        for (final IClasspathEntry entry : project.getResolvedClasspath(false)) {
            final IResource resource = root.findMember(entry.getPath());
            switch (entry.getEntryKind()) {
                case IClasspathEntry.CPE_LIBRARY:
                    collectLibraryPaths(results, entry, resource);
                    break;
                case IClasspathEntry.CPE_PROJECT:
                    collectDependentProjectPaths(results, resource, visited);
                    break;
                case IClasspathEntry.CPE_SOURCE:
                    collectOutputLocations(results, project, root, entry);
                    break;
            }
        }
    }

    private static void collectOutputLocations(final Set<String> results,
            final IJavaProject project, final IWorkspaceRoot root,
            final IClasspathEntry entry) throws JavaModelException {
        if (entry.getOutputLocation() != null) {
            results.add(root.getFolder(entry.getOutputLocation())
                    .getRawLocation().toOSString());
            return;
        }
        if (project.exists()) {
            final IPath projectOutputLocation = project.getOutputLocation();
            final IResource member = root.findMember(projectOutputLocation);
            if (member != null && member.exists()) {
                final IPath location = member.getLocation();
                results.add(location.toOSString());
            }
        }
    }

    private static void collectDependentProjectPaths(final Set<String> results,
            final IResource resource, final List<IJavaProject> visited)
            throws CoreException {
        final IJavaProject referencedProject = JavaCore
                .create((IProject) resource);
        if (referencedProject != null
                && referencedProject.getProject().exists())
            results.addAll(getClasspath(referencedProject, visited));
    }

    private static void collectLibraryPaths(final Set<String> results,
            final IClasspathEntry entry, final IResource resource) {
        if (resource != null) {
            results.add(resource.getLocation().toOSString());
        } else {
            results.add(entry.getPath().toOSString());
        }
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
    
    /**
     * Evaluates the class to determine if is an JUnit test
     * 
     * TODO: Subclasses of these two don't seem work with this logic.
     * Parent is returning Object instead of the superclass.
     * 
     * @param classNode
     * @return
     */
    public static boolean isTestCaseClass(final IType type) {
        try {
            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
            IType[] classes = hierarchy.getAllSuperclasses(type);
            for (IType clazz : classes) {
                if (clazz.getFullyQualifiedName().equals("org.junit.TestCase") || 
                        clazz.getElementName().equals("GroovyTestCase")) {
                    return true;
                }
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Error computing hierarchy for " + type, e);
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

}
