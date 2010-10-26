/*
 * Copyright 2003-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.eclipse.jdt.core.groovy.tests.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * 
 * @author Andrew Eisenberg
 * @created Oct 25, 2010
 */
public class AbstractGroovyTypeRootTests extends BuilderTests {

    public AbstractGroovyTypeRootTests(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        // discard all working copies in case a previous test has
        // failed
        ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(null, true);
        if (workingCopies != null) {
    	    for (ICompilationUnit workingCopy : workingCopies) {
                while (workingCopy.isWorkingCopy()) {
                    workingCopy.discardWorkingCopy();
                }
            }
        }
        super.tearDown();
    }

    protected IFile getFile(String path) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
    }

    protected IFile createProject(boolean isGroovy) throws JavaModelException {
        IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
        if (!isGroovy) {
            env.removeGroovyNature("Project");
        }
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        fullBuild(projectPath);
        
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        
        IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
    
        if (isGroovy) {
            env.addGroovyClass(root, "p1", "Hello",
                "package p1;\n"+
                "public class Hello {\n"+
                "   static def main(String[] args) {\n"+
                "      print \"Hello world\"\n"+
                "   }\n"+
                "}\n"
                );
        }
        
        IFile groovyFile = getFile("Project/src/p1/Hello.groovy");
    	return groovyFile;
    }
    
    protected IFile createSimpleGroovyProject() throws JavaModelException {
        return createProject(true);
    }


    protected IFile createSimpleJavaProject() throws JavaModelException {
        return createProject(false);
    }

    
    
    protected IPath createAnnotationGroovyProject() throws Exception {
    	IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyNature("Project");
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        
        IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
    
        env.addClass(root, "p", "Anno1.java",
        		"package p;\n"+
        		"import java.lang.annotation.*;\n"+
        		"@Retention(RetentionPolicy.RUNTIME)\n"+
        		"@interface Anno1 { Class<?> value(); }\n");
    
        env.addClass(root, "p", "Anno2.java",
        		"package p;\n"+
        		"import java.lang.annotation.*;\n"+
        		"@Retention(RetentionPolicy.RUNTIME)\n"+
        		"@interface Anno2 { }\n");
        
        env.addClass(root, "p", "Anno3.java",
        		"package p;\n"+
        		"import java.lang.annotation.*;\n"+
        		"@Retention(RetentionPolicy.RUNTIME)\n"+
        		"@interface Anno3 { String value(); }\n");
        
        env.addClass(root, "p", "Anno4.java",
        		"package p;\n"+
        		"import java.lang.annotation.*;\n"+
        		"@Retention(RetentionPolicy.RUNTIME)\n"+
        		"@interface Anno4 { Class<?> value1(); }\n");
        
        env.addClass(root, "p", "Target.java",
        		"package p;\n"+
        		"class Target { }");
        return root;
    }

}