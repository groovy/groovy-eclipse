/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.adapters;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;

/**
 * Adapts an IFile (likely a compilation unit) to Groovy AST types.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GroovyFileAdapterFactory implements IAdapterFactory {

    public Class[] getAdapterList() {
        return new Class[] {ClassNode.class, ClassNode[].class, ModuleNode.class};
    }

    public Object getAdapter(Object adaptable, Class adapterType) {
        Object result = null;
        if (adaptable instanceof IFile && Arrays.asList(getAdapterList()).contains(adapterType)) {
            IFile file = (IFile) adaptable;
            if (ContentTypeUtils.isGroovyLikeFileName(file.getName())) {
                try {
                    GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
                    ModuleNode module = unit.getModuleNode();
                    if (module != null) {
                        if (adapterType.equals(ModuleNode.class)) {
                            result = module;
                        } else {
                            List<ClassNode> classNodes = module.getClasses();
                            if (classNodes != null && !classNodes.isEmpty()) {
                                if (adapterType.equals(ClassNode.class)) {
                                    if (classNodes.size() == 1) {
                                        result = classNodes.get(0);
                                    } else {
                                        String mainClassName = module.getMainClassName();
                                        for (ClassNode classNode : classNodes) {
                                            if (classNode.getName().equals(mainClassName)) {
                                                result = classNode;
                                                break;
                                            }
                                        }
                                    }
                                } else if (adapterType.equals(ClassNode[].class)) {
                                    result = classNodes.toArray(ClassNode.EMPTY_ARRAY);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    GroovyCore.logException("error adapting IFile to ClassNode", e);
                }
            }
        }
        return result;
    }
}
