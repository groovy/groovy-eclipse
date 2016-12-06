/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.jdt.groovy.model;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaModelManager.PerWorkingCopyInfo;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Stores module nodes for groovy compilation units This class is not meant to be accessed externally.
 * <p>
 * One module node is stored per working copy of a unit.
 *
 * @author Andrew Eisenberg
 * @created Jun 11, 2009
 */
public class ModuleNodeMapper {

    public static class ModuleNodeInfo {

        public ModuleNodeInfo(ModuleNode module, JDTResolver resolver) {
            this.module = module;
            this.resolver = resolver;
        }

        public final ModuleNode module;
        public CompilationResult result;
        public final JDTResolver resolver;

        public final boolean isEmpty() {
            if (module == null || module.getClasses() == null || (module.getClasses().isEmpty() && module.getImports().isEmpty())) {
                return true;
            }
            if (module.getClasses().size() == 1 && module.getImports().isEmpty() && module.getClasses().get(0).isScript()) {
                if ((module.getStatementBlock() == null || module.getStatementBlock().isEmpty() || isNullReturn(module.getStatementBlock())) &&
                        (module.getMethods() == null || module.getMethods().isEmpty())) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isNullReturn(BlockStatement statementBlock) {
            List<Statement> statements = statementBlock.getStatements();
            if (statements.size() == 1 && statements.get(0) instanceof ReturnStatement) {
                ReturnStatement ret = (ReturnStatement) statements.get(0);
                if (ret.getExpression() instanceof ConstantExpression) {
                    return ((ConstantExpression) ret.getExpression()).isNullExpression();
                }
            }
            return false;
        }
    }

    private static final ModuleNodeMapper INSTANCE = new ModuleNodeMapper();

    static ModuleNodeMapper getInstance() {
        return INSTANCE;
    }

    private final ReentrantLock lock = new ReentrantLock(true);

    private final Map<PerWorkingCopyInfo, ModuleNodeInfo> infoToModuleMap = new HashMap<PerWorkingCopyInfo, ModuleNodeInfo>();

    void store(PerWorkingCopyInfo key, ModuleNodeInfo val) {
        lock.lock();
        try {
            sweepAndPurgeModuleNodes();
            infoToModuleMap.put(key, val);
        } finally {
            lock.unlock();
        }
    }

    private final static boolean DSL_BUNDLE_INSTALLED;
    static {
        boolean result = false;
        try {
            result = Platform.getBundle("org.codehaus.groovy.eclipse.dsl") != null;
        } catch (Exception e) {
            Util.log(e);
        }
        DSL_BUNDLE_INSTALLED = result;
    }

    public static boolean shouldStoreResovler() {
        return DSL_BUNDLE_INSTALLED;
    }

    ModuleNode getModule(PerWorkingCopyInfo info) {
        lock.lock();
        try {
            ModuleNodeInfo moduleNodeInfo = get(info);
            return moduleNodeInfo != null ? moduleNodeInfo.module : null;
        } finally {
            lock.unlock();
        }
    }

    ModuleNodeInfo get(PerWorkingCopyInfo info) {
        lock.lock();
        try {
            sweepAndPurgeModuleNodes();
            return infoToModuleMap.get(info);
        } finally {
            lock.unlock();
        }
    }

    JDTResolver getResolver(PerWorkingCopyInfo info) {
        lock.lock();
        try {
            ModuleNodeInfo moduleNodeInfo = get(info);
            return moduleNodeInfo != null ? moduleNodeInfo.resolver : null;
        } finally {
            lock.unlock();
        }
    }

    ModuleNode remove(PerWorkingCopyInfo info) {
        lock.lock();
        try {
            sweepAndPurgeModuleNodes();
            ModuleNodeInfo removed = infoToModuleMap.remove(info);
            return removed != null ? removed.module : null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cache the module node if this is a working copy.
     */
    protected void maybeCacheModuleNode(
        final JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo,
        final GroovyCompilationUnitDeclaration compilationUnitDeclaration) {

        if (lock.tryLock()) {
            try {
                if (perWorkingCopyInfo != null && compilationUnitDeclaration != null) {
                    ModuleNode module = compilationUnitDeclaration.getModuleNode();
                    if (module != null) {
                        JDTResolver resolver = null;
                        if (shouldStoreResovler()) {
                            resolver = (JDTResolver) compilationUnitDeclaration.getCompilationUnit().getResolveVisitor();
                        }

                        ModuleNodeInfo info = new ModuleNodeInfo(module, resolver);
                        info.result = compilationUnitDeclaration.compilationResult();

                        store(perWorkingCopyInfo, info);
                    }
                }
            } finally {
                lock.unlock();
            }
        } else {
            // lock grabbed by someone else. rerun this operation later
            new Job("Cache module node") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    maybeCacheModuleNode(perWorkingCopyInfo, compilationUnitDeclaration);
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }

    public static boolean isEmpty() {
        return INSTANCE.infoToModuleMap.isEmpty();
    }

    public static int size() {
        return INSTANCE.infoToModuleMap.size();
    }

    // GRECLIPSE-804 check to see that the stored nodes are correct
    // provide info to stdout if not and purge any stale elements
    void sweepAndPurgeModuleNodes() {
        lock.lock();
        try {
            if (System.getProperty("groovy.eclipse.model.purge") == null) {
                return;
            }

            List<PerWorkingCopyInfo> toPurge = new ArrayList<PerWorkingCopyInfo>();
            for (PerWorkingCopyInfo info : infoToModuleMap.keySet()) {
                int useCount = ((Integer) ReflectionUtils.getPrivateField(PerWorkingCopyInfo.class, "useCount", info)).intValue();
                if (useCount <= 0) {
                    String message = "Bad module node map entry: " + info.getWorkingCopy().getElementName();
                    System.out.println(message);
                    Util.log(new RuntimeException(message), message);
                    toPurge.add(info);
                } else if (useCount > 1) {
                    System.out.println(info.getWorkingCopy().getElementName() + " : useCount : " + useCount);
                }
            }

            if (toPurge.size() > 0) {
                for (PerWorkingCopyInfo info : toPurge) {
                    infoToModuleMap.remove(info);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
