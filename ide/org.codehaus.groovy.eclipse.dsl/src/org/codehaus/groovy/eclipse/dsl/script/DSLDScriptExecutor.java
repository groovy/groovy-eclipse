/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.script;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Executes a DSLD script and collects the results
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLDScriptExecutor {
    
    private final class UnsupportedDSLVersion extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public UnsupportedDSLVersion(String why) {
            super(scriptFile.getName() + " is not supported because:\n" + why);
        }
        
    }
    
    private final class RegisterClosure extends Closure {
        private static final long serialVersionUID = 1162731585734041055L;

        public RegisterClosure(Object owner) {
            super(owner);
        }
        
        @Override
        public Object call(Object arguments) {
            return tryRegister(arguments);
        }
        
        @Override
        public Object call(Object[] arguments) {
            return tryRegister(arguments);
        }
    }

    
    private final class DSLDScriptBinding extends Binding {
        private final Script dsldScript;

        public DSLDScriptBinding(Script dsldScript) {
            this.dsldScript = dsldScript;
        }


        @Override
        public Object invokeMethod(String name, Object args) {
            if (name.equals("registerPointcut")) {
                return tryRegister(args);
            } else if (name.equals("supportsVersion")) {
                String result = (String) checkVersion(new Object[] {args});
                return result == null;
            } else if (name.equals("assertVersion")) {
                String result = (String) checkVersion(new Object[] {args});
                if (result != null) {
                    throw new UnsupportedDSLVersion(result);
                }
                return null;
            } else if (name.equals("contribute")) {
                Object result = contribution(args);
                if (result == null) {
                    throw new MissingMethodException(name, dsldScript.getClass(), new Object[] { args });
                }
                return result;
            } else if (name.equals("log")) {
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
            if ("registerPointcut".equals(name)) {
                return new RegisterClosure(this);
            } else if ("supportsVersion".equals(name)) {
                return new Closure(this) {
                    private static final long serialVersionUID = 1L;

                    @Override
                	public Object call(Object[] args) {
                        String result = (String) checkVersion(args);
                        return result == null;
                	}
                };
            } else if ("assertVersion".equals(name)) {
                return new Closure(this) {
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public Object call(Object[] args) {
                        String result = (String) checkVersion(args);
                        if (result != null) {
                            throw new UnsupportedDSLVersion(result);
                        }
                        return null;
                    }
                };
            } else if ("contribute".equals(name)) {
                return new Closure(this) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Object call(Object[] args) {
                        Object result = contribution(args);
                        if (result == null) {
                            throw new MissingMethodException("contribute", dsldScript.getClass(), new Object[] { args });
                        }
                        return result;
                    }
                };
            } else if ("log".equals(name)) {
                return new Closure(this) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Object call(Object[] args) {
                        if (GroovyLogManager.manager.hasLoggers()) {
                            String msg;
                            if (args == null) {
                                msg = "null";
                            } else if (args.length == 0) {
                                msg = "";
                            } else {
                                msg = args[0].toString();
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

        private void configure(IPointcut pointcut, Object arguments) {
            if (arguments instanceof Map<?, ?>) {
                for (Entry<Object, Object> entry : ((Map<Object, Object>) arguments).entrySet()) {
                    Object key = entry.getKey();
                    pointcut.addArgument(key == null ? null : key.toString(), entry.getValue());
                }
            } else if (arguments instanceof Collection<?>) {
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
    
    private final GroovyClassLoader gcl;
    private final IJavaProject project;
    private PointcutFactory factory;
    private IStorage scriptFile;
    
    public DSLDScriptExecutor(IJavaProject project) {
        // FIXADE Should have one classloader per project
        gcl = new GroovyClassLoader(GroovyDSLCoreActivator.class.getClassLoader());
        this.project = project;
    }

    public Object executeScript(IStorage scriptFile) {
        this.scriptFile = scriptFile;
        String event = null;
        try {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "About to compile script for " + scriptFile);
                event = "Script creation for " + scriptFile;
                GroovyLogManager.manager.logStart(event);
            }
            factory = new PointcutFactory(scriptFile, project.getProject());
            Object result = null;
            try {
                String scriptContents = getContents(scriptFile);
                Class<Script> clazz = null;
                try {
                    clazz = gcl.parseClass(scriptContents, scriptFile.getName());
                } catch (Exception e) {
                    if (GroovyLogManager.manager.hasLoggers()) {
                        StringWriter writer = new StringWriter();
                        e.printStackTrace(new PrintWriter(writer));
                        GroovyLogManager.manager.log(TraceCategory.DSL, "Attempted to compile " + scriptFile + "but failed because:\n" +
                                writer.getBuffer());
                    }
                    return result;
                }
                
                
                if (!Script.class.isAssignableFrom(clazz)) {
                    // might be some strange compile error
                    // or a class is accidentally defined
                    if (GroovyLogManager.manager.hasLoggers()) {
                        GroovyLogManager.manager.log(TraceCategory.DSL, "Class " + scriptFile + " is not a script.  Can't execute as DSLD.");
                    }
                    return result;
                }
                Script dsldScript = clazz.newInstance();
                dsldScript.setBinding(new DSLDScriptBinding(dsldScript));
                result = dsldScript.run();
            } catch (UnsupportedDSLVersion e) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.DSL, e.getMessage());
                }
            } catch (Exception e) {
                // log this exception both to the event logger and to the error log
                GroovyDSLCoreActivator.logException(e);
            }
            return result;
        } finally {
            if (event != null) {
                GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
            }
        }
    }

    public String getContents(IStorage file) throws IOException, CoreException {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(file.getContents()));
        } catch (ResourceException e) {
            if (e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
                // ignore...probably not able to access an external file
            } else {
                throw e;
            }
            return "";
        }

        StringBuffer sb= new StringBuffer(300);
        try {
            int read= 0;
            while ((read= br.read()) != -1)
                sb.append((char) read);
        } finally {
            br.close();
        }
        return sb.toString();
    }

    protected Object tryRegister(Object args) {
        Object[] nameAndClosure = extractArgsForRegister(args);
        if (nameAndClosure != null) {
            factory.registerLocalPointcut((String) nameAndClosure[0], (Closure) nameAndClosure[1]);
            return nameAndClosure[1];
        } else {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Cannot register custom pointcut for " + 
                        (args instanceof Object[] ? Arrays.toString((Object[]) args) : args));
            }
            return null;
        }
    }
    
    protected Object[] extractArgsContribution(Object args) {
        if (args instanceof Object[]) {
            Object[] arr = (Object[]) args;
            if (arr.length == 2 && arr[0] instanceof IPointcut && arr[1] instanceof Closure) {
                return arr;
            } 
        } else if (args instanceof Collection<?>) {
            Collection<Object> coll = (Collection<Object>) args;
            Object[] arr = new Object[2];
            Iterator<Object> iter = coll.iterator();
            if (iter.hasNext() && (arr[0] = iter.next()) instanceof IPointcut && 
                iter.hasNext() && (arr[1] = iter.next()) instanceof Closure &&
                !iter.hasNext()) {
                return arr;
            }
        } else if (args instanceof Map<?, ?>) {
            return extractArgsContribution(((Map<Object, Object>) args).values());
        }
        return null;
    }

    protected Object[] extractArgsForRegister(Object args) {
        if (args instanceof Object[]) {
            Object[] arr = (Object[]) args;
            if (arr.length == 2 && arr[0] instanceof String && arr[1] instanceof Closure) {
                return arr;
            }
        } else if (args instanceof Collection<?>) {
            Collection<Object> coll = (Collection<Object>) args;
            Object[] arr = new Object[2];
            Iterator<Object> iter = coll.iterator();
            if (iter.hasNext() && (arr[0] = iter.next()) instanceof String && 
                iter.hasNext() && (arr[1] = iter.next()) instanceof Closure &&
                !iter.hasNext()) {
                return arr;
            }
        } else if (args instanceof Map<?, ?>) {
            return extractArgsForRegister(((Map<Object, Object>) args).values());
        }
        return null;
    }

    private static Version groovyEclipseVersion;
    private static Version groovyVersion;
    private static Version grailsToolingVersion;
    private final static Object versionLock = new Object();

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
     * a synonym for IPointcut.accept()
     * @param args
     * @return
     */
    public Object contribution(Object args) {
        Object[] contributionArgs = extractArgsContribution(args);
        if (args == null || contributionArgs.length == 0) {
            return null;
        }
        IPointcut p = (IPointcut) contributionArgs[0];
        p.accept((Closure) contributionArgs[1]);
        return Boolean.TRUE;
    }

    
    public Object checkVersion(Object[] array) {
    	if (array == null || array.length != 1) {
    		return createInvalidVersionString(array);
    	}
    	Object args = array[0];
    	
        synchronized(versionLock) {
            if (groovyEclipseVersion == null) {
                initializeVersions();
            }
        }
        
        if (! (args instanceof Map<?,?>)) {
            return createInvalidVersionString(args);
        }
        
        Map<?,?> versions = (Map<?,?>) args;
        for (Entry<?,?> entry : versions.entrySet()) {
            if (! (entry.getValue() instanceof String)) {
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

}