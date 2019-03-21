/*
 * Copyright 2009-2018 the original author or authors.
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import groovy.lang.GroovyClassLoader;

import org.apache.xbean.classloader.NonLockingJarFileClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation;
import org.codehaus.groovy.control.CompilationUnit.ProgressListener;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.jdt.groovy.control.EclipseSourceUnit;
import org.codehaus.jdt.groovy.integration.internal.GroovyLanguageSupport;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.CompilerUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.builder.BatchImageBuilder;
import org.eclipse.jdt.internal.core.builder.BuildNotifier;

/**
 * The mapping layer between the groovy parser and the JDT. This class communicates with the groovy parser and translates results
 * back for JDT to consume.
 *
 * @author Andy Clement
 */
public class GroovyParser {

    public static IGroovyDebugRequestor debugRequestor;
    public Object requestor;
    public ProblemReporter problemReporter;
    private JDTResolver resolver;
    private String projectName;
    private String gclClasspath;
    private boolean isReconcile;
    private boolean allowTransforms;
    private CompilationUnit compilationUnit;
    private CompilerOptions compilerOptions;

    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /*
     * Each project is allowed a GroovyClassLoader that will be used to load transform definitions and supporting classes. A cache
     * is maintained from project names to the current classpath and associated loader. If the classpath matches the cached version
     * on a call to build a parser then it is reused. If it does not match then a new loader is created and stored (storing it
     * orphans the previously cached one). When either a full build or a clean or project close occurs, we also discard the loader
     * instances associated with the project.
     */

    private static Map<String, PathLoaderPair> projectToLoaderCache = new ConcurrentHashMap<String, PathLoaderPair>();
    private static Map<String, ScriptFolderSelector> scriptFolderSelectorCache = new ConcurrentHashMap<String, ScriptFolderSelector>();

    static class PathLoaderPair {
        String classpath;
        GroovyClassLoader groovyClassLoader;

        PathLoaderPair(String classpath) {
            this.classpath = classpath;
            this.groovyClassLoader = new GroovyClassLoader(createConfigureLoader(classpath));
        }
    }

    /**
     * Close the jar files that have been kept open by the URLClassLoader
     */
    public static void close(GroovyClassLoader groovyClassLoader) {
        try {
            Object urlClasspath = ReflectionUtils.getPrivateField(java.net.URLClassLoader.class, "ucp", groovyClassLoader);
            Object[] jarLoaders = ((Collection<?>) ReflectionUtils.getPrivateField(urlClasspath.getClass(), "loaders", urlClasspath)).toArray();
            for (Object jarLoader : jarLoaders) {
                try {
                    JarFile jarFile = (JarFile) ReflectionUtils.getPrivateField(jarLoader.getClass(), "jar", jarLoader);

                    String jarFileName = jarFile.getName();
                    if (jarFileName.indexOf("cache") != -1 || jarFileName.indexOf("plugins") != -1) {
                        jarFile.close();
                    }
                } catch (Throwable t) {
                    // Probably not a JarLoader
                }
            }
        } catch (Throwable t) {
            // Not the kind of VM we thought it was...
        }
    }

    /**
     * Remove all cached classloaders for this project
     */
    public static void tidyCache(String projectName) {
        // This will orphan the loader on the heap
        projectToLoaderCache.remove(projectName);
        scriptFolderSelectorCache.remove(projectName);
    }

    public static void closeClassLoader(String projectName) {
        PathLoaderPair pathLoaderPair = projectToLoaderCache.get(projectName);
        if (pathLoaderPair != null) {
            close(pathLoaderPair.groovyClassLoader);
        }
    }

    /**
     * Clears cached class loaders for all caches. It helps to fix problems with cached trait helper classes.
     */
    static void tidyCache() {
        projectToLoaderCache.clear();
    }

    private GroovyClassLoader gclForBatch = null;

    private GroovyClassLoader getLoaderFor(String path) {
        GroovyClassLoader gcl = null;
        if (projectName == null && path == null) {
            if (gclForBatch == null) {
                try {
                    // Batch compilation
                    if (requestor instanceof Compiler) {
                        LookupEnvironment lookupEnvironment = ((Compiler) requestor).lookupEnvironment;
                        if (lookupEnvironment != null) {
                            INameEnvironment nameEnvironment = lookupEnvironment.nameEnvironment;
                            if (nameEnvironment instanceof FileSystem) {
                                FileSystem fileSystem = (FileSystem) nameEnvironment;
                                if (fileSystem != null) {
                                    Classpath[] classpaths = (Classpath[]) ReflectionUtils.getPrivateField(FileSystem.class, "classpaths", fileSystem);
                                    if (classpaths != null) {
                                        gclForBatch = new GroovyClassLoader();
                                        for (Classpath classpath : classpaths) {
                                            gclForBatch.addClasspath(classpath.getPath());
                                        }
                                    } else {
                                        System.err.println("Cannot find classpaths field on FileSystem class");
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Unexpected problem computing classpath for ast transform loader:");
                    e.printStackTrace(System.err);
                }
            }
            return gclForBatch;
        }
        if (path != null) {
            if (projectName == null) {
                // throw new IllegalStateException("Cannot build without knowing project name");
            } else {
                PathLoaderPair pathAndLoader = projectToLoaderCache.get(projectName);
                if (pathAndLoader == null) {
                    if (GroovyLogManager.manager.hasLoggers()) {
                        GroovyLogManager.manager.log(TraceCategory.AST_TRANSFORM, "Classpath for GroovyClassLoader (used to discover transforms): " + path);
                    }
                    pathAndLoader = new PathLoaderPair(path);
                    projectToLoaderCache.put(projectName, pathAndLoader);
                } else {
                    if (!path.equals(pathAndLoader.classpath)) {
                        // classpath change detected
                        pathAndLoader = new PathLoaderPair(path);
                        projectToLoaderCache.put(projectName, pathAndLoader);
                    }
                }
                gcl = pathAndLoader.groovyClassLoader;
            }
        }
        return gcl;
    }

    public GroovyParser(CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean allowTransforms, boolean isReconcile) {
        this(null, compilerOptions, problemReporter, allowTransforms, isReconcile);
    }

    public GroovyParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter, boolean allowTransforms, boolean isReconcile) {
        // FIXASC review callers who pass null for options
        // FIXASC set parent of the loader to system or context class loader?

        // record any paths we use for a project so that when the project is cleared,
        // the paths (which point to cached classloaders) can be cleared

        this.requestor = requestor;
        this.compilerOptions = compilerOptions;
        this.problemReporter = problemReporter;
        this.projectName = compilerOptions.groovyProjectName;
        this.gclClasspath = compilerOptions.groovyClassLoaderPath;

        // 2011-10-18: Status of transforms and reconciling
        // Prior to 2.6.0 all transforms were turned OFF for reconciling, and by turned off that meant no phase
        // processing for them was done at all. With 2.6.0 this phase processing is now active during reconciling
        // but it is currently limited to only allowing the Grab (global) transform to run. (Not sure why Grab
        // is a global transform... isn't it always annotation driven). Non-global transforms are all off.
        // This means the transformLoader is setup for the compilation unit but the cu is also told the
        // allowTransforms setting so it can decide what should be allowed through.
        // ---
        // Basic grab support: the design here is that a special classloader is created that will be augmented
        // with URLs when grab processing is running. This classloader is used as a last resort when resolving
        // types and is *only* called if a grab has occurred somewhere during compilation.
        // Currently it is not cached but created each time - we'll have to decide if there is a need to cache

        this.isReconcile = isReconcile;
        this.allowTransforms = allowTransforms;
    }

    public void reset() {
        this.compilationUnit = null;
    }

    static class GrapeAwareGroovyClassLoader extends GroovyClassLoader {

        // Could be prodded to indicate a grab has occurred within this compilation unit

        public boolean grabbed = false; // set to true if any grabbing is done

        public GrapeAwareGroovyClassLoader(ClassLoader parent) {
            super(parent != null ? parent : Thread.currentThread().getContextClassLoader());
        }

        @Override
        public void addURL(URL url) {
            // System.out.println("Grape aware classloader was augmented with " + url);
            this.grabbed = true;
            super.addURL(url);
        }
    }

    private static final boolean NONLOCKING = Boolean.getBoolean("greclipse.nonlocking");
    static {
        if (NONLOCKING) {
            System.out.println("property set: greclipse.nonlocking: will try to avoid locking jars");
        }
    }

    private static URLClassLoader createLoader(URL[] urls, ClassLoader parent) {
        if (NONLOCKING) {
            return new NonLockingJarFileClassLoader("AST Transform loader", urls, parent);
        } else {
            return new URLClassLoader(urls, parent);
        }
    }

    private static URLClassLoader createConfigureLoader(String path) {
        // GRECLIPSE-1090
        ClassLoader pcl = GroovyParser.class.getClassLoader();
        if (path == null) {
            return createLoader(null, pcl);
        }
        List<URL> urls = new ArrayList<URL>();
        if (path.indexOf(File.pathSeparator) != -1) {
            int pos = 0;
            while (pos != -1) {
                int nextSep = path.indexOf(File.pathSeparator, pos);
                if (nextSep == -1) {
                    // last piece
                    addNewURL(path.substring(pos), urls);
                    pos = -1;
                } else {
                    addNewURL(path.substring(pos, nextSep), urls);
                    pos = nextSep + 1;
                }
            }
        } else {
            addNewURL(path, urls);
        }
        return createLoader(urls.toArray(new URL[urls.size()]), pcl);
    }

    private static void addNewURL(String path, List<URL> existingURLs) {
        try {
            File f = new File(path);
            URL newURL = f.toURI().toURL();
            for (URL url : existingURLs) {
                if (url.equals(newURL)) {
                    return;
                }
            }
            existingURLs.add(newURL);
        } catch (MalformedURLException e) {
            // It was a busted URL anyway
        }
    }

    /**
     * Call the groovy parser to drive the first few phases of
     */
    public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
        char[] sourceCode = sourceUnit.getContents();
        if (sourceCode == null) {
            sourceCode = CharOperation.NO_CHAR; // pretend empty from thereon
        }

        String filepath = null;

        // This check is necessary because the filename is short (as in the last part, eg. Foo.groovy) for types coming in
        // from the hierarchy resolver. If there is the same type in two different packages then the compilation process
        // is going to go wrong because the filename is used as a key in some groovy data structures. This can lead to false
        // complaints about the same file defining duplicate types.
        char[] fileName = sourceUnit.getFileName();
        if (sourceUnit instanceof org.eclipse.jdt.internal.compiler.batch.CompilationUnit) {
            filepath = String.valueOf(((org.eclipse.jdt.internal.compiler.batch.CompilationUnit) sourceUnit).fileName);
        } else {
            filepath = String.valueOf(fileName);
        }

        IPath path = new Path(filepath);
        // Try to turn this into a 'real' absolute file system reference (this is because Grails 1.5 expects it).
        IFile eclipseFile = null;
        // GRECLIPSE-1269 ensure get plugin is not null to ensure the workspace is open (ie- not in batch mode)
        // Needs 2 segments: a project and file name or eclipse throws assertion failed here
        if (ResourcesPlugin.getPlugin() != null && path.segmentCount() >= 2) {
            eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            final IPath location = eclipseFile.getLocation();
            if (location != null) {
                filepath = location.toFile().getAbsolutePath();
            }
        }

        if (compilationUnit == null) {
            if (eclipseFile != null && eclipseFile.getProject().isAccessible() &&
                    !JavaCore.create(eclipseFile.getProject()).isOnClasspath(eclipseFile)) {
                compilerOptions.groovyCompilerConfigScript = null;
            }
            GroovyClassLoader gcl = getLoaderFor(gclClasspath);
            compilationUnit = makeCompilationUnit(new GrapeAwareGroovyClassLoader(gcl), gcl, isReconcile, allowTransforms);
            compilationUnit.removeOutputPhaseOperation();
        }

        ErrorCollector errorCollector = new GroovyErrorCollectorForJDT(compilationUnit.getConfiguration());
        SourceUnit groovySourceUnit = new EclipseSourceUnit(eclipseFile, filepath, String.valueOf(sourceCode),
            compilationUnit.getConfiguration(), compilationUnit.getClassLoader(), errorCollector, this.resolver);
        groovySourceUnit.isReconcile = compilationUnit.isReconcile;
        GroovyCompilationUnitDeclaration gcuDeclaration = new GroovyCompilationUnitDeclaration(
            problemReporter, compilationResult, sourceCode.length, compilationUnit, groovySourceUnit, compilerOptions);
        // FIXASC get this from the Antlr parser
        compilationResult.lineSeparatorPositions = GroovyUtils.getSourceLineSeparatorsIn(sourceCode);
        compilationUnit.addSource(groovySourceUnit);

        // Check if it is worth plugging in a callback listener for parse/generation
        if (requestor instanceof Compiler) {
            if (((Compiler) requestor).requestor instanceof BatchImageBuilder) {
                BuildNotifier notifier = ((BatchImageBuilder) ((Compiler) requestor).requestor).notifier;
                if (notifier != null) {
                    compilationUnit.setProgressListener(new ProgressListenerImpl(notifier));
                }
            }
        }
        gcuDeclaration.processToPhase(Phases.CONVERSION);

        // ModuleNode is null when there is a fatal error
        if (gcuDeclaration.getModuleNode() != null) {
            gcuDeclaration.populateCompilationUnitDeclaration();
            for (TypeDeclaration decl : gcuDeclaration.types) {
                resolver.record((GroovyTypeDeclaration) decl);
            }
        }
        // Is this a script?
        // If allowTransforms is TRUE then this is a 'full build' and we should remember which are scripts so that
        // .class file output can be suppressed
        if (projectName != null && eclipseFile != null) {
            ScriptFolderSelector scriptFolderSelector = scriptFolderSelectorCache.get(projectName);
            if (scriptFolderSelector == null) {
                scriptFolderSelector = new ScriptFolderSelector(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
                scriptFolderSelectorCache.put(projectName, scriptFolderSelector);
            }
            if (scriptFolderSelector.isScript(eclipseFile)) {
                gcuDeclaration.tagAsScript();
            }
        }
        if (debugRequestor != null) {
            debugRequestor.acceptCompilationUnitDeclaration(gcuDeclaration);
        }
        return gcuDeclaration;
    }

    /**
     * ProgressListener is called back when parsing of a file or generation of a classfile completes. By calling back to the build
     * notifier we ignore those long pauses where it look likes it has hung!
     *
     * Note: this does not move the progress bar, it merely updates the text
     */
    static class ProgressListenerImpl implements ProgressListener {

        private BuildNotifier notifier;

        public ProgressListenerImpl(BuildNotifier notifier) {
            this.notifier = notifier;
        }

        public void parseComplete(int phase, String sourceUnitName) {
            try {
                // Chop it down to the containing package folder
                int lastSlash = sourceUnitName.lastIndexOf("/");
                if (lastSlash == -1) {
                    lastSlash = sourceUnitName.lastIndexOf("\\");
                }
                if (lastSlash != -1) {
                    StringBuffer msg = new StringBuffer();
                    msg.append("Parsing groovy source in ");
                    msg.append(sourceUnitName, 0, lastSlash);
                    notifier.subTask(msg.toString());
                }
            } catch (Exception e) {
                // doesn't matter
            }
            notifier.checkCancel();
        }

        public void generateComplete(int phase, ClassNode classNode) {
            try {
                String pkgName = classNode.getPackageName();
                if (pkgName != null && pkgName.length() > 0) {
                    StringBuffer msg = new StringBuffer();
                    msg.append("Generating groovy classes in ");
                    msg.append(pkgName);
                    notifier.subTask(msg.toString());
                }
            } catch (Exception e) {
                // doesn't matter
            }
            notifier.checkCancel();
        }
    }

    private CompilationUnit makeCompilationUnit(GroovyClassLoader loader, GroovyClassLoader transformLoader, boolean isReconcile, boolean allowTransforms) {
        CompilerConfiguration compilerConfiguration = GroovyLanguageSupport.newCompilerConfiguration(compilerOptions, problemReporter);

        CompilationUnit cu = new CompilationUnit(
            compilerConfiguration,
            null, // CodeSource
            loader,
            transformLoader,
            allowTransforms,
            compilerOptions.groovyTransformsToRunOnReconcile,
            compilerOptions.groovyExcludeGlobalASTScan);
        this.resolver = new JDTResolver(cu);
        cu.setResolveVisitor(resolver);
        cu.tweak(isReconcile);

        // GRAILS add
        if (allowTransforms && transformLoader != null && compilerOptions != null && (compilerOptions.groovyFlags & CompilerUtils.IsGrails) != 0) {
            cu.addPhaseOperation(new GrailsInjector(transformLoader), Phases.CANONICALIZATION);
            new Grails20TestSupport(compilerOptions, transformLoader).addGrailsTestCompilerCustomizers(cu);
            cu.addPhaseOperation(new GrailsGlobalPluginAwareEntityInjector(transformLoader), Phases.CANONICALIZATION);
            // This code makes Grails 1.4.M1 AST transforms work.
            try {
                Class<?> klass = Class.forName("org.codehaus.groovy.grails.compiler.injection.GrailsAwareInjectionOperation", true, transformLoader);
                if (klass != null) {
                    ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
                    try {
                        Thread.currentThread().setContextClassLoader(transformLoader);
                        PrimaryClassNodeOperation op = (PrimaryClassNodeOperation) klass.newInstance();
                        cu.addPhaseOperation(op, Phases.CANONICALIZATION);
                    } finally {
                        Thread.currentThread().setContextClassLoader(savedLoader);
                    }
                }
            } catch (Throwable t) {
                // Ignore... probably means its not grails 1.4 project
            }
        }
        // GRAILS end

        return cu;
    }
}
