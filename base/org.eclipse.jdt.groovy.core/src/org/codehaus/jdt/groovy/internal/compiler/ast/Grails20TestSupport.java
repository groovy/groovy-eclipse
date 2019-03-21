/*
 * Copyright 2009-2017 the original author or authors.
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
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.jdt.groovy.control.EclipseSourceUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Utility class, containing methods related to Grails 2.0 test support. This code should really be part of Grails support, but
 * lives here for now.
 */
public class Grails20TestSupport {

    private static Object getField(Object o, String name) throws Exception {
        Class<?> c = o.getClass();
        Field f = lookupField(c, name);
        f.setAccessible(true);
        return f.get(o);
    }

    /**
     * So we can get the field even if dealing with a subclass and irrespective of whether the class was loaded by our own
     * classloader (if not, its hard for us to directly get a reference to the class object.
     */
    private static Field lookupField(Class<?> c, String name) throws Exception {
        if (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                Class<?> parent = c.getSuperclass();
                if (parent != null) {
                    return lookupField(parent, name);
                }
            }
        }
        return null;
    }

    /**
     * Helper to cleanup after bad code that creates ThreadLocals but doesn't remove them. We protect against such code by grabbing
     * the current set of thread locals when the ThreadLocalCleaner is instantiated and then sometime later, when 'cleanup' is
     * called, we remove any ThreadLocals that weren't there before.
     */
    public static class ThreadLocalCleaner {

        private Set<ThreadLocal<?>> initialSet;

        public ThreadLocalCleaner() {
            try {
                this.initialSet = currentThreadLocals();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        private Set<ThreadLocal<?>> currentThreadLocals() throws Exception {
            Set<ThreadLocal<?>> initialSet = new HashSet<>();

            Object threadLocalMap = getField(Thread.currentThread(), "threadLocals");
            if (threadLocalMap != null) {
                Object[] entries = (Object[]) getField(threadLocalMap, "table");
                if (entries != null) {
                    for (Object object : entries) {
                        @SuppressWarnings("unchecked")
                        WeakReference<ThreadLocal<?>> ref = (WeakReference<ThreadLocal<?>>) object;
                        if (ref != null) {
                            ThreadLocal<?> tl = ref.get();
                            if (tl != null) {
                                initialSet.add(tl);
                            }
                        }
                    }
                }
            }
            return initialSet;
        }

        /**
         * When called, will remove any threadlocals from the current thread that were not there when the ThreadLocalCleaner
         * instance was first created. It is assumed this will be called in the same thread that created the instance. Typically it
         * will be called in a finally block to ensure the cleanup happens.
         */
        public void cleanup() {
            if (initialSet != null) {
                try {
                    for (ThreadLocal<?> tl : currentThreadLocals()) {
                        if (!initialSet.contains(tl)) {
                            tl.remove();
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean DEBUG = false;

    private static void debug(String msg) {
        if (DEBUG) {
            System.out.println("Grails20TestSupport: " + msg);
        }
    }

    private static final String GRAILS_UTIL_BUILD_SETTINGS = "grails.util.BuildSettings";
    private static final String GRAILS_UTIL_BUILD_SETTINGS_HOLDER = "grails.util.BuildSettingsHolder";
    CompilerOptions options;
    GroovyClassLoader gcl;

    public Grails20TestSupport(CompilerOptions options, GroovyClassLoader gcl) {
        this.options = options;
        this.gcl = gcl;
    }

    /**
     * Grails 2.0 adds automatic imports to classes in test/unit folder. See
     * org.codehaus.groovy.grails.test.compiler.GrailsTestCompiler and the _TestApp.groovy script.
     */
    public void addGrailsTestCompilerCustomizers(CompilationUnit groovyCompilationUnit) {
        org.osgi.framework.Version v = GroovyUtils.getGroovyVersion();
        if ((v.getMajor() == 1 && v.getMinor() >= 8) || v.getMajor() > 1) {
            // The assumption is that only Grails 2.0 projects will be affected, because 1.3.7 projects require 1.7 compiler.
            ImportCustomizer importCustomizer = new ImportCustomizer() {
                @Override
                public void call(SourceUnit source, GeneratorContext context, ClassNode classNode)
                        throws CompilationFailedException {
                    if (isInGrailsUnitTestSourceFolder(source)) {
                        super.call(source, context, classNode);
                    }
                }
            };
            importCustomizer.addStarImports("grails.test.mixin");
            importCustomizer.addStarImports("org.junit");
            importCustomizer.addStaticStars("org.junit.Assert");
            groovyCompilationUnit.addPhaseOperation(importCustomizer, importCustomizer.getPhase().getPhaseNumber());

            try {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> testForClass = (Class<? extends Annotation>) Class.forName("grails.test.mixin.TestFor", false, gcl);
                if (testForClass != null) {
                    ASTTransformationCustomizer astTransformationCustomizer = new ASTTransformationCustomizer(testForClass) {
                        @Override
                        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                            if (isInGrailsUnitTestSourceFolder(source)) {
                                super.call(source, context, classNode);
                            }
                        }
                    };
                    groovyCompilationUnit.addPhaseOperation(
                            astTransformationCustomizer, astTransformationCustomizer.getPhase().getPhaseNumber());
                    ensureGrailsBuildSettings();
                }
            } catch (ClassNotFoundException ignored) {
            } catch (LinkageError ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Attempts to create and initialise a BuildSettings instance for
     */
    void ensureGrailsBuildSettings() {
        debug("entering ensureGrailsBuildSettings");
        ThreadLocalCleaner cleaner = new ThreadLocalCleaner();
        try {
            String projectName = options.groovyProjectName;
            debug("projectName = " + projectName);
            if (projectName != null) {
                Class<?> buildSettingsHolder = gcl.loadClass(GRAILS_UTIL_BUILD_SETTINGS_HOLDER);
                debug("buildSettingsHolder = " + buildSettingsHolder);
                Object buildSettings = getBuildSettings(buildSettingsHolder);
                debug("buildSettings = " + buildSettings);
                if (buildSettings == null) {
                    debug("Creating buildSettings");
                    buildSettings = createBuildSettings();
                    debug("created buildSettings = " + buildSettingsHolder);
                    setBuildSettings(buildSettingsHolder, buildSettings);
                    Object checkit = getBuildSettings(buildSettingsHolder);
                    debug("set and get buildsettings = " + checkit);
                }
            }
        } catch (Exception e) {
            debug("FAILED ensureGrailsBuildSettings");
            e.printStackTrace();
            // ignore ... classpath doesn't have what we expect.
        } finally {
            cleaner.cleanup();
        }
        debug("exiting ensureGrailsBuildSettings");
    }

    private Object createBuildSettings() throws ClassNotFoundException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> buildSettingsClass = gcl.loadClass(GRAILS_UTIL_BUILD_SETTINGS);
        debug("BuildSettingsClass = " + buildSettingsClass);
        Constructor<?> constructor = buildSettingsClass.getConstructor(File.class, File.class);
        debug("Constructor = " + constructor);
        Object grailsHome = getGrailsHome();
        debug("grailsHome = " + grailsHome);
        File projectHome = getProjectHome();
        debug("projectHome = " + projectHome);
        return constructor.newInstance(grailsHome, projectHome);
    }

    private Object getGrailsHome() {
        return null; // not computed for now... for the current use case it doesn't seem needed so why bother.
    }

    private File getProjectHome() {
        String projectName = options.groovyProjectName;
        if (projectName != null) {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            IPath location = project.getLocation();
            if (location != null) {
                return location.toFile();
            }
        }
        return null;
    }

    private static Object getBuildSettings(Class<?> buildSettingsHolder) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method m = buildSettingsHolder.getMethod("getSettings");
        return m.invoke(null);
    }

    private static synchronized void setBuildSettings(Class<?> buildSettingsHolder, Object buildSettings) throws SecurityException,
            NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Method m = buildSettingsHolder.getMethod("setSettings",
                buildSettingsHolder.getClassLoader().loadClass(GRAILS_UTIL_BUILD_SETTINGS));
        m.invoke(null, buildSettings);
        Assert.isTrue(getBuildSettings(buildSettingsHolder) == buildSettings);
    }

    static boolean isInGrailsUnitTestSourceFolder(SourceUnit source) {
        if (source instanceof EclipseSourceUnit) {
            EclipseSourceUnit eclipseSource = (EclipseSourceUnit) source;
            IFile file = eclipseSource.getEclipseFile();
            if (file != null) {
                IPath path = file.getProjectRelativePath();
                return new Path("test/unit").isPrefixOf(path);
            }
        }
        return false;
    }
}
