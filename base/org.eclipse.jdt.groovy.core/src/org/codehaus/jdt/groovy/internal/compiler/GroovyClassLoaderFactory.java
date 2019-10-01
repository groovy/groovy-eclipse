/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner;
import org.codehaus.groovy.runtime.m12n.SimpleExtensionModule;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

public final class GroovyClassLoaderFactory {

    /*
     * Each project is allowed a GroovyClassLoader that will be used to load transform definitions and supporting classes. A cache
     * is maintained from project names to the current classpath and associated loader. If the classpath matches the cached version
     * on a call to build a parser then it is reused. If it does not match then a new loader is created and stored (storing it
     * orphans the previously cached one). When either a full build or a clean or project close occurs, we also discard the loader
     * instances associated with the project.
     */
    private static Map<String, Map.Entry<IClasspathEntry[], GroovyClassLoader[]>> projectClassLoaderCache = new ConcurrentHashMap<>();

    public static void clearCache() {
        projectClassLoaderCache.clear(); // TODO: Close class loaders?
    }

    public static void clearCache(String projectName) {
        Map.Entry<?, GroovyClassLoader[]> entry = projectClassLoaderCache.remove(projectName);
        if (entry != null) {
            Stream.of(entry.getValue()).filter(Objects::nonNull).forEach(GroovyClassLoaderFactory::close);
        }
    }

    private static void close(ClassLoader classLoader) {
        if (classLoader instanceof Closeable) {
            try {
                ((Closeable) classLoader).close();
            } catch (IOException e) {
                Util.log(e);
            }
        }
        if (classLoader instanceof GroovyClassLoader) {
            ((GroovyClassLoader) classLoader).clearCache();
            // parent was created by newClassLoader(...)
            close(classLoader.getParent());
        }
    }

    //--------------------------------------------------------------------------

    private GroovyClassLoader batchLoader;
    private final CompilerOptions compilerOptions;
    private final LookupEnvironment lookupEnvironment;

    public GroovyClassLoaderFactory(CompilerOptions compilerOptions, Object requestor) {
        this.compilerOptions = compilerOptions;
        this.lookupEnvironment = (requestor instanceof Compiler ? ((Compiler) requestor).lookupEnvironment : null);
    }

    public GroovyClassLoader[] getGroovyClassLoaders(CompilerConfiguration compilerConfiguration) {
        if (compilerOptions.groovyProjectName == null) {
            return getBatchGroovyClassLoaders(compilerConfiguration);
        } else {
            return getProjectGroovyClassLoaders(compilerConfiguration);
        }
    }

    private GroovyClassLoader[] getBatchGroovyClassLoaders(CompilerConfiguration compilerConfiguration) {
        if (batchLoader == null && lookupEnvironment != null) {
            try {
                INameEnvironment nameEnvironment = lookupEnvironment.nameEnvironment;
                if (nameEnvironment.getClass().getName().endsWith("tests.compiler.regression.InMemoryNameEnvironment")) {
                    nameEnvironment = ((INameEnvironment[]) ReflectionUtils.getPrivateField(nameEnvironment.getClass(), "classLibs", nameEnvironment))[0];
                }
                if (nameEnvironment instanceof FileSystem) {
                    FileSystem.Classpath[] classpaths = ReflectionUtils.getPrivateField(FileSystem.class, "classpaths", nameEnvironment);
                    if (classpaths != null) {
                        batchLoader = new GroovyClassLoader();
                        for (FileSystem.Classpath classpath : classpaths) {
                            batchLoader.addClasspath(classpath.getPath());
                        }
                    }
                }
            } catch (Exception e) {
                Util.log(e, "Unexpected problem computing classpath for batch compiler");
            }
        }
        return new GroovyClassLoader[] {new GrapeAwareGroovyClassLoader(batchLoader, compilerConfiguration), batchLoader};
    }

    private GroovyClassLoader[] getProjectGroovyClassLoaders(CompilerConfiguration compilerConfiguration) {
        String projectName = compilerOptions.groovyProjectName;
        IProject project = findProject(projectName);
        try {
            IJavaProject javaProject = JavaCore.create(project);
            IClasspathEntry[] classpathEntries = javaProject.exists() ? javaProject.getResolvedClasspath(true) : new IClasspathEntry[0];

            Map.Entry<IClasspathEntry[], GroovyClassLoader[]> entry = projectClassLoaderCache.computeIfAbsent(projectName, key -> {
                Set<String> classPaths = new LinkedHashSet<>(), xformPaths = new LinkedHashSet<>();
                if (javaProject.exists()) calculateClasspath(javaProject, classPaths, xformPaths);

                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.AST_TRANSFORM,
                        "Transform classpath: " + String.join(File.pathSeparator, xformPaths));
                }

                ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();

                return new java.util.AbstractMap.SimpleEntry<>(classpathEntries, new GroovyClassLoader[] {
                    new GrapeAwareGroovyClassLoader(newClassLoader(classPaths, parentClassLoader), compilerConfiguration),
                    new GroovyClassLoader(newClassLoader(xformPaths, getClass().getClassLoader())/*, compilerConfiguration*/),
                });
            });

            if (Arrays.equals(classpathEntries, entry.getKey())) {
                return entry.getValue();
            } else {
                // project classpath has changed; remove and reload
                projectClassLoaderCache.remove(projectName);
                return getProjectGroovyClassLoaders(compilerConfiguration);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to bootstrap GroovyClassLoaders for project '" + projectName + "'", e);
        }
    }

    //--------------------------------------------------------------------------

    private static IProject findProject(String projectName) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    }

    private static void calculateClasspath(IJavaProject javaProject, Set<String> classPaths, Set<String> xformPaths) {
        try {
            IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(javaProject); // TODO: Leverage "excludeTestCode" parameter?  http://www.eclipse.org/eclipse/news/4.8/M5/index.html#jdt-test-sources
            Arrays.sort(entries, Comparator.comparing(IRuntimeClasspathEntry::getType));
            for (IRuntimeClasspathEntry unresolved : entries) {
                Set<String> paths = (unresolved.getType() == IRuntimeClasspathEntry.CONTAINER ? classPaths : xformPaths);
                for (IRuntimeClasspathEntry resolved : resolveRuntimeClasspathEntry(unresolved, javaProject)) {
                    paths.add(getAbsoluteLocation(resolved));
                }
            }
            classPaths.addAll(xformPaths);
            assert classPaths.stream().allMatch(path -> new File(path).isAbsolute());
        }
        catch (RuntimeException e) { throw e; } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry classpathEntry, IJavaProject javaProject) throws Exception {
        //return JavaRuntime.resolveRuntimeClasspathEntry(classpathEntry, javaProject); // indirect dependency on org.eclipse.debug.core.ILaunchConfiguration
        return ReflectionUtils.throwableExecutePrivateMethod(JavaRuntime.class, "resolveRuntimeClasspathEntry", new Class[] {IRuntimeClasspathEntry.class, IJavaProject.class}, JavaRuntime.class, new Object[] {classpathEntry, javaProject});
    }

    private static String getAbsoluteLocation(IRuntimeClasspathEntry classpathEntry) {
        String location = classpathEntry.getLocation();

        Path path = new Path(location);
        if (!path.toFile().exists()) {
            IProject project = findProject(path.segment(0));
            IResource resource = (path.segmentCount() == 1 ? project : project.getFile(path.removeFirstSegments(1)));

            IPath rawLocation = resource.getRawLocation();
            if (rawLocation != null) {
                location = rawLocation.toOSString();
            } else if (resource.getLocation() != null) {
                location = resource.getLocation().toOSString();
            }
        }

        return location;
    }

    private static URLClassLoader newClassLoader(Set<String> classpath, ClassLoader parent) {
        URL[] urls = classpath.stream().map(file -> {
            try {
                return new File(file).toURI().toURL();
            } catch (MalformedURLException ignore) {
                return null;
            }
        }).filter(Objects::nonNull).toArray(URL[]::new);

        if (NONLOCKING) {
            if (parent == null) parent = URLClassLoader.newInstance(new URL[0], null);
            return new org.apache.xbean.classloader.NonLockingJarFileClassLoader("GDT non-locking loader", urls, parent);
        } else {
            return URLClassLoader.newInstance(urls, parent);
        }
    }

    private static final boolean NONLOCKING = Boolean.getBoolean("greclipse.nonlocking");
    static {
        if (NONLOCKING) {
            System.out.println("property set: greclipse.nonlocking: will try to avoid locking jars");
        }
    }

    //--------------------------------------------------------------------------

    @SuppressWarnings("rawtypes")
    public static class GrapeAwareGroovyClassLoader extends GroovyClassLoader {

        public GrapeAwareGroovyClassLoader(ClassLoader parent, CompilerConfiguration config) {
            super(parent, config);
        }

        @Override
        public void addURL(URL url) {
            this.grabbed = true;
            super.addURL(url);
        }

        /** {@code true} if any grabbing is done */
        public boolean grabbed;

        private volatile Set<Class> defaultCategories;
        private volatile Set<Class> defaultStaticCategories;

        public Set<Class> getDefaultCategories() {
            if (defaultCategories == null) {
                synchronized (this) {
                    if (defaultCategories == null) {
                        Set<Class> objectCategories = new LinkedHashSet<>(), staticCategories = new LinkedHashSet<>();
                        try {
                            Class dgm = loadClass("org.codehaus.groovy.runtime.DefaultGroovyMethods", false, true);
                            Class dgsm = loadClass("org.codehaus.groovy.runtime.DefaultGroovyStaticMethods", false, true);

                            Collections.addAll(objectCategories, (Class[]) dgm.getField("DGM_LIKE_CLASSES").get(dgm));

                            staticCategories.add(dgsm);

                            new ExtensionModuleScanner(module -> {
                                if (module instanceof SimpleExtensionModule) {
                                    objectCategories.addAll(((SimpleExtensionModule) module).getInstanceMethodsExtensionClasses());
                                    staticCategories.addAll(((SimpleExtensionModule) module).getStaticMethodsExtensionClasses());
                                }
                            }, this).scanClasspathModules();

                            objectCategories.addAll(staticCategories);

                            defaultCategories = objectCategories;
                            defaultStaticCategories = staticCategories;
                        } catch (Exception | NoClassDefFoundError e) {
                            defaultCategories = Collections.EMPTY_SET;
                            defaultStaticCategories = Collections.EMPTY_SET;

                            if (GroovyLogManager.manager.hasLoggers()) {
                                GroovyLogManager.manager.log(TraceCategory.CLASSPATH,
                                    "Failed to find Default Groovy Methods with " + this + "\n\t" + e.getMessage());
                            }
                        }
                    }
                }
            }
            return Collections.unmodifiableSet(defaultCategories);
        }

        public boolean isDefaultStaticCategory(String name) {
            if (defaultStaticCategories == null) getDefaultCategories();
            return defaultStaticCategories.stream().map(Class::getName).anyMatch(name::equals);
        }
    }
}
