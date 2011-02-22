/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.script;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Executes a GDSL script and collects the results
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLDScriptExecutor {
    
    private final class UnsupportedDSLVersion extends RuntimeException {

        private static final long serialVersionUID = 282885748470678955L;

        public UnsupportedDSLVersion(String why) {
            super(scriptName + " is not supported because:\n" + why);
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
        @Override
        public Object invokeMethod(String name, Object args) {
            if (name.equals("registerPointcut")) {
                return tryRegister(args);
            } else if (name.equals("supportsVersion")) {
                return checkVersion(new Object[] {args});
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
                	public Object call(Object[] arguments) {
                	    return checkVersion(arguments);
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
    private String scriptName;
    
    public DSLDScriptExecutor(IJavaProject project) {
        // FIXADE What should we be using for the parent classloader? 
        gcl = new GroovyClassLoader(GroovyDSLCoreActivator.class.getClassLoader());
        this.project = project;
    }

    public Object executeScript(IFile scriptFile) {
        scriptName = scriptFile.getFullPath().toPortableString();
        GroovyLogManager.manager.log(TraceCategory.DSL, "About to compile script for " + scriptFile);
        String event = "Script creation for " + scriptFile;
        GroovyLogManager.manager.logStart(event);
        factory = new PointcutFactory(scriptName, project.getProject());
        Object result = null;
        try {
            String scriptContents = getContents(scriptFile);
            Class<Script> clazz = null;
            try {
                clazz = gcl.parseClass(scriptContents, scriptName);
            } catch (Exception e) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Attempted to compile " + scriptName + "but failed because:\n" + e.getLocalizedMessage());
                return result;
            }
            Script dsldScript = clazz.newInstance();
            dsldScript.setBinding(new DSLDScriptBinding());
            result = dsldScript.run();
        } catch (UnsupportedDSLVersion e) {
            GroovyLogManager.manager.log(TraceCategory.DSL, e.getMessage());
        } catch (Exception e) {
            GroovyDSLCoreActivator.logException(e);
        }
        GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
        return result;
    }

    public String getContents(IFile file) throws IOException, CoreException {
        BufferedReader br= new BufferedReader(new InputStreamReader(file.getContents()));

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
            GroovyLogManager.manager.log(TraceCategory.DSL, "Cannot register custom pointcut for " + 
                    (args instanceof Object[] ? Arrays.toString((Object[]) args) : args));
            return null;
        }
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
        if (grailsBundle != null) {
            grailsToolingVersion = groovyBundle.getVersion();
        }
    }

    public Object checkVersion(Object[] array) {
    	if (array == null || array.length != 1) {
    		throw new UnsupportedDSLVersion(createInvalidVersionString(array));
    	}
    	Object args = array[0];
    	
        synchronized(versionLock) {
            if (groovyEclipseVersion == null) {
                initializeVersions();
            }
        }
        
        if (! (args instanceof Map<?,?>)) {
            throw new UnsupportedDSLVersion(createInvalidVersionString(args));
        }
        
        Map<?,?> versions = (Map<?,?>) args;
        for (Entry<?,?> entry : versions.entrySet()) {
            if (! (entry.getValue() instanceof String)) {
                throw new UnsupportedDSLVersion(createInvalidVersionString(args));
            }
            Version v = null;
            try {
                v = new Version((String) entry.getValue());
            } catch (IllegalArgumentException e) {
                throw new UnsupportedDSLVersion(e.getMessage());
            }
            if ("groovy".equals(entry.getKey())) {
                if (groovyVersion != null && v.compareTo(groovyVersion) > 0) {
                    throw new UnsupportedDSLVersion("Invalid Groovy version.  Expected: " + v + " Installed: " + groovyVersion);
                } else if (groovyVersion == null) {
                    throw new UnsupportedDSLVersion("Could not find a Groovy version.  Expected: " + groovyVersion);
                }
            } else if ("groovyEclipse".equals(entry.getKey())) {
                if (groovyEclipseVersion != null && v.compareTo(groovyEclipseVersion) > 0) {
                    throw new UnsupportedDSLVersion("Invalid Groovy-Eclipse version.  Expected: " + v + " Installed: " + groovyEclipseVersion);
                } else if (groovyEclipseVersion == null) {
                    throw new UnsupportedDSLVersion("Could not find a Groovy-Eclipse version.  Expected: " + groovyEclipseVersion);
                }
            } else if ("grailsTooling".equals(entry.getKey())) {
                if (grailsToolingVersion != null && v.compareTo(grailsToolingVersion) > 0) {
                    throw new UnsupportedDSLVersion("Invalid Grails Tooling version.  Expected: " + v + " Installed: " + grailsToolingVersion);
                } else if (grailsToolingVersion == null) {
                    throw new UnsupportedDSLVersion("Could not find a Grails Tooling version.  Expected: " + grailsToolingVersion);
                }
            } else {
                throw new UnsupportedDSLVersion(createInvalidVersionString(args));
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