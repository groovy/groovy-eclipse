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
package org.codehaus.groovy.eclipse.adapters;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.SharedASTProvider;

public class ClassFileEditorAdapterFactory implements IAdapterFactory {

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] {ClassNode.class, ClassNode[].class, ModuleNode.class};
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptable, Class<T> adapterType) {
        T result = null;
        if (adaptable instanceof ClassFileEditor && Arrays.asList(getAdapterList()).contains(adapterType)) {
            ClassFileEditor editor = (ClassFileEditor) adaptable;
            // trigger discriminator in MultiplexingCommentRecorderParser
            ITypeRoot root = EditorUtility.getEditorInputJavaElement(editor, false);
            CompilationUnit unit = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
            if (unit.getClass().getName().equals("org.codehaus.jdt.groovy.core.dom.GroovyCompilationUnit")) {
                // TODO: Is there a better way to get from GroovyCompilationUnit to GroovyCompilationUnitDeclaration?
                Object resolver = ReflectionUtils.executeNoArgPrivateMethod(org.eclipse.jdt.core.dom.AST.class, "getBindingResolver", unit.getAST());
                CompilationUnitScope scope = (CompilationUnitScope) ReflectionUtils.executeNoArgPrivateMethod(resolver.getClass(), "scope", resolver);

                ModuleNode moduleNode = ((GroovyCompilationUnitDeclaration) scope.referenceContext).getModuleNode();
                if (adapterType.equals(ModuleNode.class)) {
                    result = (T) moduleNode;
                } else if (moduleNode != null) {
                    List<ClassNode> classNodes = moduleNode.getClasses();
                    if (classNodes != null && !classNodes.isEmpty()) {
                        if (adapterType.equals(ClassNode.class)) {
                            result = (T) classNodes.get(0);
                        } else if (adapterType.equals(ClassNode[].class)) {
                            result = (T) classNodes.toArray();
                        }
                    }
                }
            }
        }
        return result;
    }
}
