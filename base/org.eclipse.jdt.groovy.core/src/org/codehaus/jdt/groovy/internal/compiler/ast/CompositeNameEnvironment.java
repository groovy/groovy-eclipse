/*
 * Copyright 2009-2026 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import static java.util.Collections.addAll;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.INameEnvironmentWithProgress;

class CompositeNameEnvironment implements INameEnvironment, IModuleAwareNameEnvironment, INameEnvironmentWithProgress {

    private final INameEnvironment[] delegates;

    CompositeNameEnvironment(INameEnvironment... delegates) {
        this.delegates = delegates;
    }

    @Override
    public void cleanup() {
        for (int i = 0; i < delegates.length; ++i) {
            delegates[i].cleanup();
        }
    }

    @Override
    public boolean isPackage(char[][] qual, char[] name) {
        for (int i = 0; i < delegates.length; ++i) {
            if (delegates[i].isPackage(qual, name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NameEnvironmentAnswer findType(char[][] name) {
        for (int i = 0; i < delegates.length; ++i) {
            NameEnvironmentAnswer answer = delegates[i].findType(name);
            if (answer != null) {
                return answer;
            }
        }
        return null;
    }

    @Override
    public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
        for (int i = 0; i < delegates.length; ++i) {
            NameEnvironmentAnswer answer = delegates[i].findType(typeName, packageName);
            if (answer != null) {
                return answer;
            }
        }
        return null;
    }

    @Override
    public NameEnvironmentAnswer findType(char[][] fullTypeName, char[] moduleName) {
        return first(env -> env.findType(fullTypeName, moduleName));
    }

    @Override
    public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
        return first(env -> env.findType(typeName, packageName, moduleName));
    }

    @Override
    public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, boolean secondaryTypes, char[] moduleName) {
        for (int i = 0; i < delegates.length; ++i) {
            NameEnvironmentAnswer answer = null;
            if (delegates[i] instanceof INameEnvironmentWithProgress env) {
                answer = env.findType(typeName, packageName, secondaryTypes, moduleName);
            } else if (delegates[i] instanceof IModuleAwareNameEnvironment env) {
                answer = env.findType(typeName, packageName, moduleName);
            } else if (moduleName == null || moduleName.length == 0) {
                answer = delegates[i].findType(typeName, packageName);
            }
            if (answer != null) {
                return answer;
            }
        }
        return null;
    }

    //

    @Override
    public char[][] getModulesDeclaringPackage(char[][] packageName, char[] moduleName) {
        return every(env -> env.getModulesDeclaringPackage(packageName, moduleName), char[][]::new);
    }

    @Override
    public boolean hasCompilationUnit(char[][] packageName, char[] moduleName, boolean checkUnits) {
        return Boolean.TRUE == first(env -> env.hasCompilationUnit(packageName, moduleName, checkUnits) ? Boolean.TRUE : null);
    }

    @Override
    public char[][] getAllAutomaticModules() {
        return every(IModuleAwareNameEnvironment::getAllAutomaticModules, char[][]::new);
    }

    @Override
    public IModule getModule(char[] moduleName) {
        return first(env -> env.getModule(moduleName));
    }

    @Override
    public char[][] listPackages(char[] moduleName) {
        return every(env -> env.listPackages(moduleName), char[][]::new);
    }

    @Override
    public void setMonitor(IProgressMonitor monitor) {
        for (int i = 0; i < delegates.length; ++i) {
            if (delegates[i] instanceof INameEnvironmentWithProgress env) {
                env.setMonitor(monitor);
            }
        }
    }

    //

    private <T> T first(Function<IModuleAwareNameEnvironment, T> function) {
        for (int i = 0; i < delegates.length; ++i) {
            if (delegates[i] instanceof IModuleAwareNameEnvironment env) {
                T answer = function.apply(env);
                if (answer != null) {
                    return answer;
                }
            }
        }
        return null;
    }

    private <T> T[] every(Function<IModuleAwareNameEnvironment, T[]> function, IntFunction<T[]> arrayMaker) {
        Set<T> all = new HashSet<>();
        for (int i = 0; i < delegates.length; ++i) {
            if (delegates[i] instanceof IModuleAwareNameEnvironment env) {
                T[] some = function.apply(env);
                if (some != null) {
                    addAll(all, some);
                }
            }
        }
        return all.toArray(arrayMaker);
    }
}
