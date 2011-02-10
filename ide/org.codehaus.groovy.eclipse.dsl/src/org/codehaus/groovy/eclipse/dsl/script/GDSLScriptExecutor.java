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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLActivator;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Executes a GDSL script and collects the results
 * @author andrew
 * @created Nov 17, 2010
 */
public class GDSLScriptExecutor {
    private final GroovyClassLoader gcl;
    private final IJavaProject project;
    
    public GDSLScriptExecutor(IJavaProject project) {
        gcl = new GroovyClassLoader();
        this.project = project;
    }

    @SuppressWarnings("serial")
    public void executeScript(IFile scriptFile) {
        String scriptName = scriptFile.getName();
        GroovyLogManager.manager.log(TraceCategory.DSL, "About to compile script for " + scriptFile);
        String event = "Script creation for " + scriptFile;
        GroovyLogManager.manager.logStart(event);
        try {
            String scriptContents = getContents(scriptFile);
            Class<Script> clazz = null;
            try {
                clazz = gcl.parseClass(scriptContents, scriptName);
            } catch (Exception e) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Attempted to compile " + scriptName + "but failed because:\n" + e.getLocalizedMessage());
                return ;
            }
            Script gdslScript = clazz.newInstance();
            Map<String, Closure> vars = new HashMap<String, Closure>();
            vars.put("context", new ContextClosure(ContextStore.convertToIdentifier(scriptFile)));
            vars.put("contributor", new ContributorClosure(getStore(project)));

            vars.put("hasField", new Closure(this) {
                @Override
                public Object call(Object arguments) {
                    Object first = getFirst(arguments);
                    if (first instanceof IContextQuery) {
                        return new HasFieldQuery((IContextQuery) first);
                    } else {
                        return new HasFieldQuery(first.toString());
                    }
                }
            });
            vars.put("hasMethod", new Closure(this) {
                @Override
                public Object call(Object[] arguments) {
                    Object first = getFirst(arguments);
                    if (first instanceof IContextQuery) {
                        return new HasMethodQuery((IContextQuery) first);
                    } else {
                        return new HasMethodQuery(first.toString());
                    }
                }
            });
            vars.put("hasAnnotation", new Closure(this) {
                @Override
                public Object call(Object[] arguments) {
                    Object first = getFirst(arguments);
                    return new HasAnnotationQuery(first.toString());
                }
            });
            vars.put("scriptScope", new Closure(this) {
                @Override
                public Object call(Object[] arguments) {
                    return scriptScope(arguments);    
                }
            });
            vars.put("classScope", new Closure(this) {
                @Override
                public Object call(Object[] arguments) {
                    return classScope(arguments);    
                }
            });
            vars.put("natureScope", new Closure(this) {
                @Override
                public Object call(Object[] arguments) {
                    return natureScope(arguments);    
                }
            });
            vars.put("closureScope", new Closure(this) {
                @Override
                public Object call(Object[] arguments) {
                    return closureScope(arguments);    
                }
            });
            Binding binding = new Binding(vars);
            gdslScript.setBinding(binding);
            gdslScript.run();
        } catch (Exception e) {
            GroovyDSLActivator.logException(e);
        }
        GroovyLogManager.manager.logEnd(event, TraceCategory.DSL);
    }

    private ContextStore getStore(IJavaProject project) {
        return GroovyDSLActivator.getDefault().getContextStoreManager().getContextStore(project);
    }
    
    
    public static String getContents(IFile file) throws IOException, CoreException {
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

    
    IContextQuery scriptScope(Object args) {
        // name
        String name = ".*";
        if (args instanceof Map) {
            name = ((Map<String, String>) args).get("name");
        } else {
            name = args.toString();
        }
        return new ScriptScopeQuery(name);
    }
    
    IContextQuery closureScope(Object args) {
        boolean isArg = false;
        if (args instanceof Map) {
            isArg = Boolean.parseBoolean(((Map<String, Object>) args).get("isArg").toString());
        } else if (args != null) {
            isArg = Boolean.parseBoolean(args.toString());
        }
        return new ClosureScopeQuery(isArg);
    }
    
    IContextQuery natureScope(Object args) {
        String nature;
        if (args instanceof Map) {
            nature = ((Map<String, String>) args).get("nature");
        } else {
            nature = args.toString();
        }
        return new NatureScopeQuery(nature);
    }
    
    IContextQuery classScope(Object args) {
        String name;
        if (args instanceof Map) {
            name = ((Map<String, String>) args).get("name");
        } else {
            name = args.toString();
        }
        return new ClassScopeQuery(name);
    }
    
    Object getFirst(Object arguments) {
        if (arguments instanceof Object[] && ((Object[]) arguments).length > 0) {
            return ((Object[]) arguments)[0];
        } else if (arguments instanceof List<?> && ((List<?>) arguments).size() > 0) {
            return ((List<?>) arguments).get(0);
        } else {
            return arguments;
        }
    }

}
