/*
 * Copyright 2009-2023 the original author or authors.
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
package org.eclipse.jdt.groovy.core;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;

public class GroovyResourceAdapter implements IAdapterFactory {

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] {GroovyCompilationUnit.class, ICompilationUnit.class, ModuleNode.class, ClassNode[].class, ClassNode.class};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptable, Class<T> adapterType) {
        T result = null;
        if (adaptable instanceof IFile && Arrays.asList(getAdapterList()).contains(adapterType)) {
            if (ContentTypeUtils.isGroovyLikeFileName(((IFile) adaptable).getName())) {
                try {
                    GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom((IFile) adaptable);
                    if (adapterType.isAssignableFrom(GroovyCompilationUnit.class)) {
                        result = (T) unit;
                    } else {
                        ModuleNode module = unit.getModuleNode();
                        if (module != null) {
                            if (adapterType.equals(ModuleNode.class)) {
                                result = (T) module;
                            } else {
                                List<ClassNode> classNodes = module.getClasses();
                                if (classNodes != null && !classNodes.isEmpty()) {
                                    if (adapterType.equals(ClassNode.class)) {
                                        if (classNodes.size() == 1) {
                                            result = (T) classNodes.get(0);
                                        } else {
                                            String mainClassName = module.getMainClassName();
                                            for (ClassNode classNode : classNodes) {
                                                if (classNode.getName().equals(mainClassName)) {
                                                    result = (T) classNode;
                                                    break;
                                                }
                                            }
                                        }
                                    } else if (adapterType.equals(ClassNode[].class)) {
                                        result = (T) classNodes.toArray(ClassNode.EMPTY_ARRAY);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Activator.getDefault().getLog().log(new Status(
                        IStatus.ERROR, Activator.PLUGIN_ID, "error adapting IFile to " + adapterType.getSimpleName(), e));
                }
            }
        }
        return result;
    }
}
