/*
 * Copyright 2009-2022 the original author or authors.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.core.JavaModelManager.PerWorkingCopyInfo;

/**
 * Stores module nodes for groovy compilation units.
 * <p>
 * One module node is stored per working copy of a unit. This class is not meant to be accessed externally.
 */
public class ModuleNodeMapper {

    private static final ModuleNodeMapper INSTANCE = new ModuleNodeMapper();

    static ModuleNodeMapper getInstance() {
        return INSTANCE;
    }

    public static boolean isEmpty() {
        return INSTANCE.infoToModuleMap.isEmpty();
    }

    public static int size() {
        return INSTANCE.infoToModuleMap.size();
    }

    //

    private final Map<PerWorkingCopyInfo, ModuleNodeInfo> infoToModuleMap = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock(true);

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    //--------------------------------------------------------------------------

    ModuleNodeInfo get(PerWorkingCopyInfo info) {
        lock.lock();
        try {
            sweepAndPurgeModuleNodes();
            return infoToModuleMap.get(info);
        } finally {
            lock.unlock();
        }
    }

    ModuleNodeInfo put(PerWorkingCopyInfo info, ModuleNodeInfo tuple) {
        lock.lock();
        try {
            sweepAndPurgeModuleNodes();
            return infoToModuleMap.put(info, tuple);
        } finally {
            lock.unlock();
        }
    }

    ModuleNodeInfo remove(PerWorkingCopyInfo info) {
        lock.lock();
        try {
            sweepAndPurgeModuleNodes();
            return infoToModuleMap.remove(info);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cache the module node if this is a working copy.
     */
    void maybeCacheModuleNode(PerWorkingCopyInfo info, GroovyCompilationUnitDeclaration gcud) {
        if (info != null && gcud != null) {
            if (lock.tryLock()) {
                try {
                    ModuleNode module = gcud.getModuleNode();
                    if (module != null) {
                        put(info, new ModuleNodeInfo(module, (JDTResolver) gcud.getCompilationUnit().getResolveVisitor(), gcud.compilationResult()));
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                // lock grabbed by someone else. rerun this operation later
                new Job("Cache module node") {
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        maybeCacheModuleNode(info, gcud);
                        return Status.OK_STATUS;
                    }
                }.schedule();
            }
        }
    }

    // GRECLIPSE-804 check to see that the stored nodes are correct and purge any stale elements
    private void sweepAndPurgeModuleNodes() {
        for (Iterator<PerWorkingCopyInfo> it = infoToModuleMap.keySet().iterator(); it.hasNext();) {
            PerWorkingCopyInfo info = it.next();

            Integer useCount = ReflectionUtils.getPrivateField(PerWorkingCopyInfo.class, "useCount", info);
            if (useCount != null && useCount <= 0) {
                it.remove();
            }
        }
    }

    //--------------------------------------------------------------------------

    public static class ModuleNodeInfo {

        public final ModuleNode module;
        public final JDTResolver resolver;
        public final CompilationResult result;

        public ModuleNodeInfo(final ModuleNode module, final JDTResolver resolver) {
            this(module, resolver, null);
        }

        public ModuleNodeInfo(final ModuleNode module, final JDTResolver resolver, final CompilationResult result) {
            this.module = module;
            this.resolver = resolver;
            this.result = result;
        }

        public final boolean isEmpty() {
            if (module.getClasses().size() > 1 || !module.getMethods().isEmpty() ||
                    !module.getImports().isEmpty() || !module.getStaticImports().isEmpty() ||
                    !module.getStarImports().isEmpty() || !module.getStaticStarImports().isEmpty() ||
                    (module.getPackage() != null && !module.getPackage().getAnnotations().isEmpty())) {
                return false;
            }
            if (module.getClasses().isEmpty() || (module.getClasses().get(0).isScript() &&
                    module.getClasses().get(0).getFields().stream().noneMatch(f -> f.getEnd() > 0) &&
                    (module.getStatementBlock().isEmpty() || isNullReturn(module.getStatementBlock())))) {
                return true;
            }
            return false;
        }

        private static boolean isNullReturn(final BlockStatement blockStatement) {
            List<Statement> statements = blockStatement.getStatements();
            if (statements.size() == 1 && statements.get(0) instanceof ReturnStatement) {
                ReturnStatement returnStatement = (ReturnStatement) statements.get(0);
                if (returnStatement.getExpression() instanceof ConstantExpression) {
                    return ((ConstantExpression) returnStatement.getExpression()).isNullExpression();
                }
            }
            return false;
        }
    }
}
