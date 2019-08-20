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
package org.codehaus.groovy.eclipse.dsl.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.jdt.groovy.integration.internal.GroovyLanguageSupport;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Executes a DSLD script and collects the results.
 */
public class DSLDScriptExecutor {

    private final IJavaProject project;
    private PointcutFactory factory;
    private String scriptName;

    public DSLDScriptExecutor(IJavaProject project) {
        this.project = project;
    }

    public Object executeScript(IStorage scriptFile) {
        scriptName = scriptFile.getName();
        String event = null;
        try {
            if (GroovyLogManager.manager.hasLoggers()) {
                event = "DSLD processing for " + scriptFile;
                GroovyLogManager.manager.logStart(event);
            }
            factory = new PointcutFactory(scriptFile, project.getProject());
            try {
                GroovyClassLoader classLoader = getGroovyClassLoader();
                String scriptText = getContents(scriptFile);
                @SuppressWarnings("rawtypes")
                Class scriptType = null;
                try {
                    scriptType = classLoader.parseClass(scriptText, scriptName);
                } catch (AssertionError | Exception e) {
                    if (GroovyLogManager.manager.hasLoggers()) {
                        StringWriter writer = new StringWriter();
                        e.printStackTrace(new PrintWriter(writer));
                        GroovyLogManager.manager.log(TraceCategory.DSL, "Attempted to compile " + scriptName + ", but failed because:\n" + writer.getBuffer());
                    }
                    return null;
                }

                if (!Script.class.isAssignableFrom(scriptType)) {
                    // might be some strange compile error or a class is accidentally defined
                    if (GroovyLogManager.manager.hasLoggers()) {
                        GroovyLogManager.manager.log(TraceCategory.DSL, scriptName + " is not a Groovy script.  Can't execute as DSLD.");
                    }
                    return null;
                }
                Script dsldScript = (Script) scriptType.newInstance();
                dsldScript.setBinding(new DSLDScriptBinding(dsldScript));

                return dsldScript.run();

            } catch (UnsupportedDSLVersion e) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.DSL, e.getMessage());
                }
            } catch (AssertionError | Exception e) {
                // log exception to the event console and the error log
                GroovyDSLCoreActivator.logException(e);
            }
            return null;
        } finally {
            if (event != null) {
                GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
            }
        }
    }

    private static String getContents(IStorage file) throws IOException, CoreException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()))) {
            return IOGroovyMethods.getText(br);
        } catch (ResourceException e) {
            if (e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
                // probably not able to access an external file
                return "";
            } else {
                throw e;
            }
        }
    }

    /**
     * Returns loader suitable for loading classes using the project's classpath
     * and the workspace's Eclipse, Groovy and Java runtimes.
     */
    private GroovyClassLoader getGroovyClassLoader() {
        Bundle bundle = GroovyDSLCoreActivator.getDefault().getBundle();
        ClassLoader loader = bundle.adapt(BundleWiring.class).getClassLoader();

        return GroovyLanguageSupport.newGroovyClassLoader(project, loader);
    }

    protected Object tryRegister(Object args) {
        Object[] nameAndClosure = extractArgsForRegister(args);
        if (nameAndClosure != null) {
            factory.registerLocalPointcut((String) nameAndClosure[0], (Closure<?>) nameAndClosure[1]);
            return nameAndClosure[1];
        } else {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.DSL,
                    "Cannot register custom pointcut for " + (args instanceof Object[] ? Arrays.toString((Object[]) args) : args));
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Object[] extractArgsContribution(Object args) {
        if (args instanceof Object[]) {
            Object[] arr = (Object[]) args;
            if (arr.length == 2 && arr[0] instanceof IPointcut && arr[1] instanceof Closure) {
                return arr;
            }
        } else if (args instanceof Collection) {
            Collection<Object> coll = (Collection<Object>) args;
            Object[] arr = new Object[2];
            Iterator<Object> iter = coll.iterator();
            if (iter.hasNext() && (arr[0] = iter.next()) instanceof IPointcut &&
                    iter.hasNext() && (arr[1] = iter.next()) instanceof Closure &&
                    !iter.hasNext()) {
                return arr;
            }
        } else if (args instanceof Map) {
            return extractArgsContribution(((Map<Object, Object>) args).values());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected Object[] extractArgsForRegister(Object args) {
        if (args instanceof Object[]) {
            Object[] arr = (Object[]) args;
            if (arr.length == 2 && arr[0] instanceof String && arr[1] instanceof Closure) {
                return arr;
            }
        } else if (args instanceof Collection) {
            Collection<Object> coll = (Collection<Object>) args;
            Object[] arr = new Object[2];
            Iterator<Object> iter = coll.iterator();
            if (iter.hasNext() && (arr[0] = iter.next()) instanceof String &&
                    iter.hasNext() && (arr[1] = iter.next()) instanceof Closure &&
                    !iter.hasNext()) {
                return arr;
            }
        } else if (args instanceof Map) {
            return extractArgsForRegister(((Map<Object, Object>) args).values());
        }
        return null;
    }

    private static Version groovyVersion;
    private static Version groovyEclipseVersion;
    private static Version grailsToolingVersion;
    private static final Object versionLock = new Object();

    private static void initializeVersions() {
        groovyEclipseVersion = GroovyDSLCoreActivator.getDefault().getBundle().getVersion();
        Bundle groovyBundle = Platform.getBundle("org.codehaus.groovy");
        if (groovyBundle != null) {
            groovyVersion = groovyBundle.getVersion();
        }
        Bundle grailsBundle = Platform.getBundle("com.springsource.sts.grails.core");
        if (grailsBundle == null) {
            grailsBundle = Platform.getBundle("org.grails.ide.eclipse.core");
        }
        if (grailsBundle != null) {
            grailsToolingVersion = grailsBundle.getVersion();
        }
    }

    /**
     * synonym for IPointcut.accept()
     */
    public Object contribution(Object args) {
        Object[] contributionArgs = extractArgsContribution(args);
        if (contributionArgs == null || contributionArgs.length < 2) {
            return null;
        }
        IPointcut p = (IPointcut) contributionArgs[0];
        p.accept((Closure<?>) contributionArgs[1]);
        return Boolean.TRUE;
    }

    public Object checkVersion(Object[] array) {
        if (array == null || array.length != 1) {
            return createInvalidVersionString(array);
        }
        Object args = array[0];

        synchronized (versionLock) {
            if (groovyEclipseVersion == null) {
                initializeVersions();
            }
        }

        if (!(args instanceof Map)) {
            return createInvalidVersionString(args);
        }

        Map<?, ?> versions = (Map<?, ?>) args;
        for (Map.Entry<?, ?> entry : versions.entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                return createInvalidVersionString(args);
            }
            Version v = null;
            try {
                v = new Version((String) entry.getValue());
            } catch (IllegalArgumentException e) {
                throw new UnsupportedDSLVersion(e.getMessage());
            }
            if ("groovy".equals(entry.getKey())) {
                if (groovyVersion != null && v.compareTo(groovyVersion) > 0) {
                    return "Invalid Groovy version.  Expected: " + v + " Installed: " + groovyVersion;
                } else if (groovyVersion == null) {
                    return "Could not find a Groovy version.  Expected: " + groovyVersion;
                }
            } else if ("groovyEclipse".equals(entry.getKey())) {
                if (groovyEclipseVersion != null && v.compareTo(groovyEclipseVersion) > 0) {
                    return "Invalid Groovy-Eclipse version.  Expected: " + v + " Installed: " + groovyEclipseVersion;
                } else if (groovyEclipseVersion == null) {
                    return "Could not find a Groovy-Eclipse version.  Expected: " + groovyEclipseVersion;
                }
            } else if ("grailsTooling".equals(entry.getKey()) || "sts".equals(entry.getKey())) {
                if (grailsToolingVersion != null && v.compareTo(grailsToolingVersion) > 0) {
                    return "Invalid Grails Tooling version.  Expected: " + v + " Installed: " + grailsToolingVersion;
                } else if (grailsToolingVersion == null) {
                    return "Could not find a Grails Tooling version.  Expected: " + grailsToolingVersion;
                }
            } else {
                return createInvalidVersionString(args);
            }
        }

        return null;
    }

    protected String createInvalidVersionString(Object args) {
        return args + " is not a valid version identifier, must be a Map<String, String>.  " +
                "Each value must be a version number X.Y.Z.  " +
                "Supported version checking is: 'groovy', 'grailsTooling', 'groovyEclipse'.";
    }

    private final class UnsupportedDSLVersion extends RuntimeException {
        private static final long serialVersionUID = 1L;

        UnsupportedDSLVersion(String why) {
            super(scriptName + " is not supported because:\n" + why);
        }
    }

    private final class RegisterClosure extends Closure<Object> {
        private static final long serialVersionUID = 1162731585734041055L;

        RegisterClosure(Object owner) {
            super(owner);
        }

        @Override
        public Object call(Object arguments) {
            return tryRegister(arguments);
        }

        @Override
        public Object call(Object... arguments) {
            return tryRegister(arguments);
        }
    }

    private final class DSLDScriptBinding extends Binding {

        DSLDScriptBinding(Script dsldScript) {
            this.dsldScript = dsldScript;
        }
        private final Script dsldScript;

        @Override
        public Object invokeMethod(String name, Object args) {
            Object result;
            switch (name) {
            case "registerPointcut":
                return tryRegister(args);

            case "supportsVersion":
                result = checkVersion(new Object[] {args});
                return (result == null);

            case "assertVersion":
                result = checkVersion(new Object[] {args});
                if (result != null) {
                    throw new UnsupportedDSLVersion(result.toString());
                }
                return null;

            case "contribute":
                result = contribution(args);
                if (result == null) {
                    throw new MissingMethodException(name, dsldScript.getClass(), new Object[] {args});
                }
                return result;

            case "log":
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.DSL, "========== " + args);
                }
                return args;
            }

            IPointcut pc = factory.createPointcut(name);
            if (pc != null) {
                configure(pc, args);
                return pc;
            } else {
                return super.invokeMethod(name, args);
            }
        }

        @Override
        public Object getVariable(String name) {
            switch (name) {
            case "registerPointcut":
                return new RegisterClosure(this);

            case "supportsVersion":
                return new Closure<Object>(this) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Object call(Object... args) {
                        String result = (String) checkVersion(args);
                        return result == null;
                    }
                };

            case "assertVersion":
                return new Closure<Object>(this) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Object call(Object... args) {
                        String result = (String) checkVersion(args);
                        if (result != null) {
                            throw new UnsupportedDSLVersion(result);
                        }
                        return null;
                    }
                };

            case "contribute":
                return new Closure<Object>(this) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Object call(Object... args) {
                        Object result = contribution(args);
                        if (result == null) {
                            throw new MissingMethodException("contribute", dsldScript.getClass(), new Object[] {args});
                        }
                        return result;
                    }
                };

            case "log":
                return new Closure<Object>(this) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Object call(Object... args) {
                        if (GroovyLogManager.manager.hasLoggers()) {
                            String msg;
                            if (args == null || args.length == 0) {
                                msg = "";
                            } else {
                                msg = String.valueOf(args[0]);
                            }
                            GroovyLogManager.manager.log(TraceCategory.DSL, "========== " + msg);
                        }
                        return args;
                    }
                };
            }

            IPointcut pc = factory.createPointcut(name);
            if (pc != null) {
                return new PointcutClosure(this, pc);
            } else {
                return super.getVariable(name);
            }
        }

        @SuppressWarnings("unchecked")
        private void configure(IPointcut pointcut, Object arguments) {
            if (arguments instanceof Map) {
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) arguments).entrySet()) {
                    Object key = entry.getKey();
                    pointcut.addArgument(key == null ? null : key.toString(), entry.getValue());
                }
            } else if (arguments instanceof Collection) {
                for (Object arg : (Collection<Object>) arguments) {
                    pointcut.addArgument(arg);
                }
            } else if (arguments instanceof Object[]) {
                for (Object arg : (Object[]) arguments) {
                    pointcut.addArgument(arg);
                }
            } else if (arguments != null) {
                pointcut.addArgument(arguments);
            }
        }
    }
}
