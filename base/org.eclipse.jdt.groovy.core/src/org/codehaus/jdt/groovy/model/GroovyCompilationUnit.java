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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.jdt.groovy.integration.internal.MultiplexingSourceElementRequestorParser;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.ASTHolderCUInfo;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.ReconcileWorkingCopyOperation;
import org.eclipse.jdt.internal.core.util.Util;

public class GroovyCompilationUnit extends CompilationUnit {

    public GroovyCompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner) {
        super(parent, name, owner);
    }

    private String mainTypeName;

    @Override
    public char[] getMainTypeName() {
        if (this.mainTypeName == null) {
            String unitName = Util.getNameWithoutJavaLikeExtension(this.name);
            this.mainTypeName = GeneratorContext.encodeAsValidClassName(unitName);
        }
        return this.mainTypeName.toCharArray();
    }

    /**
     * Returns the module node for this GroovyCompilationUnit creates one if one doesn't exist.
     *
     * This is potentially a long running operation. This method ensures that this CompilationUnit is a working copy and that it is
     * consistent (if not a reconcile operation is performed).
     */
    public ModuleNode getModuleNode() {
        ModuleNodeInfo moduleInfo = this.getModuleInfo(true);
        return moduleInfo != null ? moduleInfo.module : null;
    }

    /**
     * Gets the module info for this compilation unit
     *
     * @param force if true, then a module info is created even if not a working copy. This occurs by temporarily turning the
     *        compilation unit into a working copy and then discarding it.
     * @return the {@link ModuleNodeInfo} for this compilation unit. Will be null if force is set to false and this unit is not a
     *         working copy. Also will be null if a problem occurs
     */
    public ModuleNodeInfo getModuleInfo(boolean force) {
        try {
            if (!this.isConsistent()) {
                this.makeConsistent(null);
            }
            boolean becameWorkingCopy = false;
            ModuleNodeMapper.getInstance().lock();
            // discard the working copy after finishing
            // if there was no working copy to begin with
            try {
                becameWorkingCopy = (force && !this.isWorkingCopy());
                if (becameWorkingCopy) {
                    this.becomeWorkingCopy(null);
                }
                JavaModelManager.PerWorkingCopyInfo info = this.getPerWorkingCopyInfo();
                if (info != null) {
                    return ModuleNodeMapper.getInstance().get(info);
                }
            } finally {
                try {
                    if (becameWorkingCopy) {
                        this.discardWorkingCopy();
                    }
                } finally {
                    ModuleNodeMapper.getInstance().unlock();
                }
            }
        } catch (JavaModelException e) {
            Util.log(e, "Exception thrown when trying to get Groovy module node for " + this.name);
        }
        // return null if not found. Means that there was a problem with build structure
        return null;
    }

    /**
     * Gets the module node for this compilation unit. Bypasses the cached module node and creates a new one, which is then placed
     * in the cache
     */
    public ModuleNodeInfo getNewModuleInfo() {
        try {
            this.openWhenClosed(this.createElementInfo(), false/* or should it be true... ? */, new NullProgressMonitor());
        } catch (JavaModelException e) {
            Util.log(e, "Exception thrown when trying to get Groovy module node for " + this.name);
        }
        return this.getModuleInfo(true);
    }

    @Override
    public void discardWorkingCopy() throws JavaModelException {
        // GRECLIPSE-804 must synchronize
        ModuleNodeMapper.getInstance().lock();
        try {
            JavaModelManager.PerWorkingCopyInfo info = this.getPerWorkingCopyInfo();
            if (this.workingCopyInfoWillBeDiscarded(info)) {
                ModuleNodeMapper.getInstance().remove(info);
            }
            super.discardWorkingCopy();
        } finally {
            ModuleNodeMapper.getInstance().unlock();
        }
    }

    /**
     * working copy info is about to be discared if useCount <= 1
     */
    private boolean workingCopyInfoWillBeDiscarded(JavaModelManager.PerWorkingCopyInfo info) {
        return (info != null && ((Integer) ReflectionUtils.getPrivateField(JavaModelManager.PerWorkingCopyInfo.class, "useCount", info)).intValue() <= 1);
    }

    /**
     * Tracks how deep we are in recursive calls to {@link #buildStructure}.
     */
    private static final ThreadLocalAtomicInteger depth = new ThreadLocalAtomicInteger();

    @Override
    protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
            throws JavaModelException {
        depth.increment();
        try {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.COMPILER, "Build Structure starting for " + name);
                GroovyLogManager.manager.logStart("Build structure: " + name + " : " + Thread.currentThread().getName());
            }

            // ensure buffer is opened
            IBuffer buffer = getBufferManager().getBuffer(this);
            if (buffer == null) {
                openBuffer(pm, info); // open buffer independently from the info, since we are building the info
            }

            // generate structure and compute syntax problems if needed
            GroovyCompilationUnitStructureRequestor requestor = new GroovyCompilationUnitStructureRequestor(this, (CompilationUnitElementInfo) info, newElements);
            JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = getPerWorkingCopyInfo();
            IJavaProject project = getJavaProject();

            boolean createAST;
            boolean resolveBindings;
            int reconcileFlags;
            HashMap problems;
            if (info instanceof ASTHolderCUInfo) {
                ASTHolderCUInfo astHolder = (ASTHolderCUInfo) info;
                createAST = astHolder.astLevel != NO_AST;
                resolveBindings = astHolder.resolveBindings;
                reconcileFlags = astHolder.reconcileFlags;
                problems = astHolder.problems;
            } else {
                createAST = false;
                resolveBindings = false;
                reconcileFlags = 0;
                problems = null;
            }
            boolean hasJavaNature = (project != null && JavaProject.hasJavaNature(project.getProject()));
            boolean computeProblems = (hasJavaNature && perWorkingCopyInfo != null && perWorkingCopyInfo.isActive());

            Map<String, String> options = (hasJavaNature ? project.getOptions(true) : JavaCore.getOptions());
            options.put(CompilerOptions.OPTIONG_GroovyProjectName, project.getElementName());
            options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
            if (!computeProblems) {
                // disable task tags checking for faster parsing
                options.remove(CompilerOptions.OPTION_TaskTags);
            }
            options.putAll(getCustomOptions());

            CompilerOptions compilerOptions = new CompilerOptions(options);
            compilerOptions.ignoreMethodBodies = (reconcileFlags & IGNORE_METHOD_BODIES) != 0;

            ProblemReporter reporter = new ProblemReporter(new GroovyErrorHandlingPolicy(!computeProblems), compilerOptions, new DefaultProblemFactory());

            SourceElementParser parser = new MultiplexingSourceElementRequestorParser(
                reporter,
                requestor,
                reporter.problemFactory,
                compilerOptions,
                true, // report local declarations
                !createAST // optimize string literals only if not creating a DOM AST
            );
            parser.reportOnlyOneSyntaxError = !computeProblems;
            // maybe not needed for groovy, but I don't want to find out.
            parser.setMethodsFullRecovery(true);
            parser.setStatementsRecovery((reconcileFlags & ENABLE_STATEMENTS_RECOVERY) != 0);

            if (!computeProblems && !resolveBindings && !createAST) // disable javadoc parsing if not computing problems, not resolving and not creating ast
                parser.javadocParser.checkDocComment = false;
            requestor.setParser(parser);

            // update timestamp (might be IResource.NULL_STAMP if original does not exist)
            if (underlyingResource == null) {
                underlyingResource = getResource();
            }
            // underlying resource is null in the case of a working copy on a class file in a jar
            if (underlyingResource != null) {
                ReflectionUtils.setPrivateField(CompilationUnitElementInfo.class, "timestamp", info, underlyingResource.getModificationStamp());
            }

            GroovyCompilationUnitDeclaration compilationUnitDeclaration = null;
            CompilationUnit source = cloneCachingContents();
            try {
                // note that this is a slightly different approach than taken by super.buildStructure,
                // where there is a test here to see if computeProblems is true.
                // if false, then parser.parserCompilationUnit is called.
                // this will not work for Groovy because we need to ensure bindings are resolved
                // for many operations (content assist and code select) to work.
                // So, for groovy, always use CompilationUnitProblemFinder.process and then process problems
                // separately only if necessary
                // addendum (GRECLIPSE-942). The testcase for that bug includes a package with 200
                // types in that refer to each other in a chain, through field references. If a reconcile
                // references the top of the chain we can go through a massive number of recursive calls into
                // this buildStructure for each one. The 'full' parse (with bindings) is only required for
                // the top most (regardless of the computeProblems setting) and so we track how many recursive
                // calls we have made - if we are at depth 2 we do what JDT was going to do (the quick thing).
                if (computeProblems || depth.intValue() < 2) {
                    if (problems == null) {
                        // report problems to the problem requestor
                        problems = new HashMap<>();
                        compilationUnitDeclaration =
                            (GroovyCompilationUnitDeclaration) CompilationUnitProblemFinder.process(
                                source,
                                parser,
                                this.owner,
                                problems,
                                createAST,
                                reconcileFlags,
                                pm);
                        if (computeProblems) {
                            try {
                                perWorkingCopyInfo.beginReporting();
                                for (Iterator<CategorizedProblem[]> iterator = problems.values().iterator(); iterator.hasNext();) {
                                    CategorizedProblem[] categorizedProblems = iterator.next();
                                    if (categorizedProblems == null) continue;
                                    for (int i = 0, n = categorizedProblems.length; i < n; i += 1) {
                                        perWorkingCopyInfo.acceptProblem(categorizedProblems[i]);
                                    }
                                }
                            } finally {
                                perWorkingCopyInfo.endReporting();
                            }
                        }
                    } else {
                        // collect problems
                        compilationUnitDeclaration =
                            (GroovyCompilationUnitDeclaration) CompilationUnitProblemFinder.process(
                                source,
                                parser,
                                this.owner,
                                problems,
                                createAST,
                                reconcileFlags,
                                pm);
                    }
                } else {
                    compilationUnitDeclaration =
                        (GroovyCompilationUnitDeclaration) parser.parseCompilationUnit(
                            source,
                            true, // full parse to find local elements
                            pm);
                }

                // if this is a working copy, then we have more work to do
                maybeCacheModuleNode(perWorkingCopyInfo, compilationUnitDeclaration);

                // create the DOM AST from the compiler AST
                if (createAST) {
                    try {
                        ASTHolderCUInfo astHolder = (ASTHolderCUInfo) info;
                        astHolder.ast = AST.convertCompilationUnit(astHolder.astLevel, compilationUnitDeclaration, options, computeProblems, source, reconcileFlags, pm);
                    } catch (OperationCanceledException e) {
                        // don't enter the catch(Exception e) block below
                        // might need to do the same for AbortCompilation
                        throw e;
                    } catch (IllegalArgumentException e) {
                        // if necessary, we can do some better reporting here
                        Util.log(e, "Problem with build structure: Offset for AST node is incorrect in " + getParent().getElementName() + "." + this.name);
                    } catch (Exception e) {
                        Util.log(e, "Problem with build structure for " + this.name);
                    }
                }
            } catch (OperationCanceledException e) {
                // don't enter the catch(Exception e) block below
                // might need to do the same for AbortCompilation
                throw e;
            } catch (JavaModelException e) {
                // GRECLIPSE-1480 don't log element does not exist exceptions for non-java project
                if (e.getStatus().getCode() != IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST || getJavaProject().exists()) {
                    Util.log(e, "Problem with build structure for " + this.name);
                }
            } catch (Exception e) {
                // The groovy compiler doesn't handle broken code well in many situations
                // use this general catch clause so that exceptions thrown by broken code
                // do not bubble up the stack.
                Util.log(e, "Problem with build structure for " + this.name);
            } finally {
                if (compilationUnitDeclaration != null) {
                    ((CompilationUnitElementInfo) info).hasFunctionalTypes = compilationUnitDeclaration.hasFunctionalTypes();
                    compilationUnitDeclaration.cleanUp();
                }
            }
            return info.isStructureKnown();
        } finally {
            depth.decrement();
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.logEnd("Build structure: " + name + " : " + Thread.currentThread().getName(), TraceCategory.COMPILER);
            }
        }
    }

    protected void maybeCacheModuleNode(JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo, GroovyCompilationUnitDeclaration compilationUnitDeclaration) {
        ModuleNodeMapper.getInstance().maybeCacheModuleNode(perWorkingCopyInfo, compilationUnitDeclaration);
    }

    @Override
    public org.eclipse.jdt.core.dom.CompilationUnit reconcile(int astLevel, int reconcileFlags, WorkingCopyOwner workingCopyOwner, IProgressMonitor monitor)
            throws JavaModelException {
        if (!this.isWorkingCopy() || (monitor != null && monitor.isCanceled()))
            return null; // reconciling is not supported on non-working copies

        PerformanceStats stats = null;
        if (ReconcileWorkingCopyOperation.PERF) {
            stats = PerformanceStats.getStats(JavaModelManager.RECONCILE_PERF, this);
            stats.startRun(String.valueOf(this.getFileName()));
        }
        ReconcileWorkingCopyOperation op = new GroovyReconcileWorkingCopyOperation(this, astLevel,
            reconcileFlags, workingCopyOwner != null ? workingCopyOwner : DefaultWorkingCopyOwner.PRIMARY);
        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        try {
            manager.cacheZipFiles(this); // cache zip files for performance (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=134172)
            if (monitor == null || !monitor.isCanceled()) {
                op.runOperation(monitor);
            }
        } finally {
            manager.flushZipFiles(this);
        }
        if (ReconcileWorkingCopyOperation.PERF) {
            stats.endRun();
        }
        return op.ast;
    }

    /**
     * There is no such thing as a primary type in Groovy. First look for a type
     * of the same name as the CU, else get the first type in getAllTypes().
     */
    @Override
    public IType findPrimaryType() {
        if (this.mainTypeName == null) this.getMainTypeName();
        IType type = this.getType(this.mainTypeName);
        if (type.exists()) {
            return type;
        }
        try {
            if (this.getResource().exists()) {
                IType[] types = this.getTypes();
                if (types != null && types.length > 0) {
                    return types[0];
                }
            }
        } catch (JavaModelException e) {
            // can ignore situations when trying to find types that are not on the classpath
            if (e.getStatus().getCode() != IJavaModelStatusConstants.ELEMENT_NOT_ON_CLASSPATH) {
                Util.log(e, "Error finding all types of " + this.name);
            }
        }
        return null;
    }

    public boolean isOnBuildPath() {
        // fix for bug http://dev.eclipse.org/bugs/show_bug.cgi?id=20051
        IJavaProject project = this.getJavaProject();
        if (!project.isOnClasspath(this)) {
            return false;
        }
        IProject resourceProject = project.getProject();
        if (resourceProject == null || !resourceProject.isAccessible() || !GroovyNature.hasGroovyNature(resourceProject)) {
            return false;
        }

        return true;
    }

    @Override
    public IResource getUnderlyingResource()
            throws JavaModelException {
        if (this.isOnBuildPath()) {
            return super.getUnderlyingResource();
        } else {
            // Super class method appears to only work correctly when we are on the build
            // path. Otherwise parent, which is a package, is seen as non-existent.
            // This causes a JavaModel exception.
            IResource rsrc = this.getResource();
            // I think that getResource *should* just return a path to the .groovy file
            // under these circumstances.
            try {
                // What we did is rather hacky, double check result with this assert:
                Assert.isTrue(rsrc.getFullPath().toString().endsWith(name));
            } catch (Throwable e) {
                Util.log(e);
                return super.getUnderlyingResource();
            }
            return rsrc;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> adapter) {
        if (GroovyCompilationUnit.class.equals(adapter)) {
            return (T) this;
        }
        if (ModuleNode.class.equals(adapter)) {
            return (T) this.getModuleNode();
        }
        return super.getAdapter(adapter);
    }

    //--------------------------------------------------------------------------

    @Override
    protected void codeComplete(ICompilationUnit unit, ICompilationUnit skip, int position, CompletionRequestor requestor, WorkingCopyOwner owner, ITypeRoot typeRoot, IProgressMonitor monitor)
            throws JavaModelException {
        // allow a delegate to perform completion if required
        // this is used by the grails plugin when editing in gsp editor
        ICodeCompletionDelegate delegate = Adapters.adapt(this, ICodeCompletionDelegate.class);
        if (delegate != null && delegate.shouldCodeComplete(requestor, typeRoot)) {
            delegate.codeComplete(unit, skip, position, requestor, owner, typeRoot, monitor);
        } else {
            super.codeComplete(unit, skip, position, requestor, owner, typeRoot, monitor);
        }
    }

    @Override
    protected IJavaElement[] codeSelect(ICompilationUnit unit, int offset, int length, WorkingCopyOwner owner)
            throws JavaModelException {
        if (CodeSelectHelperFactory.selectHelper != null) {
            return CodeSelectHelperFactory.selectHelper.select(this, offset, length);
        }
        return new IJavaElement[0];
    }

    @Override
    public IJavaElement[] codeSelect(int offset, int length, WorkingCopyOwner owner)
            throws JavaModelException {
        return codeSelect(this, offset, length, owner);
    }

    @Override
    public IJavaElement[] codeSelect(int offset, int length)
            throws JavaModelException {
        return codeSelect(offset, length, DefaultWorkingCopyOwner.PRIMARY);
    }

    //--------------------------------------------------------------------------

    @Override
    public GroovyCompilationUnit cloneCachingContents() {
        return new CompilationUnitClone();
    }

    public GroovyCompilationUnit cloneCachingContents(char[] source) {
        return new CompilationUnitClone(source);
    }

    private class CompilationUnitClone extends GroovyCompilationUnit {

        private char[] cachedContents;

        CompilationUnitClone(char[] cachedContents) {
            this();
            this.cachedContents = cachedContents;
        }

        CompilationUnitClone() {
            super((PackageFragment) GroovyCompilationUnit.this.getParent(), GroovyCompilationUnit.this.name, GroovyCompilationUnit.this.owner);
        }

        @Override
        public char[] getContents() {
            if (this.cachedContents == null)
                this.cachedContents = GroovyCompilationUnit.this.getContents();
            return this.cachedContents;
        }

        @Override
        public CompilationUnit originalFromClone() {
            return GroovyCompilationUnit.this;
        }

        @Override
        public char[] getFileName() {
            return GroovyCompilationUnit.this.getFileName();
        }
    }

    private static class GroovyErrorHandlingPolicy implements IErrorHandlingPolicy {

        private final boolean stopOnFirst;

        GroovyErrorHandlingPolicy(final boolean stopOnFirst) {
            this.stopOnFirst = stopOnFirst;
        }

        @Override
        public boolean stopOnFirstError() {
            return stopOnFirst;
        }

        @Override
        public boolean proceedOnErrors() {
            return !stopOnFirst;
        }

        @Override
        public boolean ignoreAllErrors() {
            return false;
        }
    }

    private static class ThreadLocalAtomicInteger extends ThreadLocal<AtomicInteger> {

        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }

        int intValue() {
            return get().get();
        }

        void increment() {
            get().incrementAndGet();
        }

        void decrement() {
            get().decrementAndGet();
        }
    }
}
