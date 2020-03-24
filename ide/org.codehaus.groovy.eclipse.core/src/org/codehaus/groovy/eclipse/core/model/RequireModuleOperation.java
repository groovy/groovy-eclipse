/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.model;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.RequiresDirective;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.core.CreateElementInCUOperation;
import org.eclipse.jdt.launching.JavaRuntime;

public class RequireModuleOperation extends CreateElementInCUOperation {

    public static void requireModule(final IJavaProject javaProject, final String moduleName) throws CoreException {
        IModuleDescription moduleDesc = javaProject.getModuleDescription();
        if (moduleDesc != null && !Arrays.asList(moduleDesc.getRequiredModuleNames()).contains(moduleName)) {
            new RequireModuleOperation(moduleDesc, moduleName).run(null);
        }
    }

    public static void requireModule(final IJavaProject javaProject, final IType type) throws CoreException {
        IPackageFragmentRoot root = (IPackageFragmentRoot) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        if (JavaRuntime.isModule(root.getResolvedClasspathEntry(), javaProject)) {
            IModuleDescription moduleDescription = root.getModuleDescription();
            if (moduleDescription == null) {
                moduleDescription = JavaCore.getAutomaticModuleDescription(root);
            }
            if (moduleDescription != null) {
                requireModule(javaProject, moduleDescription.getElementName());
            }
        }
    }

    //--------------------------------------------------------------------------

    private final String moduleName;

    private RequireModuleOperation(final IModuleDescription moduleDesc, final String moduleName) {
        super(moduleDesc);
        this.moduleName = moduleName;
    }

    @Override
    public String getMainTaskName() {
        return "Add \"requires " + moduleName + ";\"";
    }

    @Override
    protected StructuralPropertyDescriptor getChildPropertyDescriptor(final ASTNode parent) {
        return ModuleDeclaration.MODULE_DIRECTIVES_PROPERTY;
    }

    @Override
    protected ASTNode generateElementAST(final ASTRewrite rewriter, final ICompilationUnit cu) throws JavaModelException {
        AST ast = cuAST.getAST();

        RequiresDirective requires = ast.newRequiresDirective();
        requires.setName(ast.newName(moduleName));
        return requires;
    }

    @Override
    protected IJavaElement generateResultHandle() {
        return getParentElement();
    }
}
