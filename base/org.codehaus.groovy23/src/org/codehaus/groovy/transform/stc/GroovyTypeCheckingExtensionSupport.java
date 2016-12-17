/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform.stc;

import groovy.lang.*;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.runtime.InvokerHelper;

import groovyjarjarasm.asm.Opcodes;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Base class for type checking extensions written in Groovy. Compared to its superclass, {@link TypeCheckingExtension},
 * this class adds a number of utility methods aimed at leveraging the syntax of the Groovy language to improve
 * expressivity and conciseness.
 *
 * @author Cedric Champeau
 * @since 2.1.0
 */
public class GroovyTypeCheckingExtensionSupport extends AbstractTypeCheckingExtension {

    // method name to DSL name
    private static final Map<String, String> METHOD_ALIASES;
    static {
        final Map<String, String> aliases = new HashMap<String, String>(20);
        aliases.put("onMethodSelection", "onMethodSelection");
        aliases.put("afterMethodCall", "afterMethodCall");
        aliases.put("beforeMethodCall", "beforeMethodCall");
        aliases.put("unresolvedVariable", "handleUnresolvedVariableExpression");
        aliases.put("unresolvedProperty", "handleUnresolvedProperty");
        aliases.put("unresolvedAttribute", "handleUnresolvedAttribute");
        aliases.put("ambiguousMethods", "handleAmbiguousMethods");
        aliases.put("methodNotFound", "handleMissingMethod");
        aliases.put("afterVisitMethod", "afterVisitMethod");
        aliases.put("beforeVisitMethod", "beforeVisitMethod");
        aliases.put("afterVisitClass", "afterVisitClass");
        aliases.put("beforeVisitClass", "beforeVisitClass");
        aliases.put("incompatibleAssignment", "handleIncompatibleAssignment");
        aliases.put("incompatibleReturnType", "handleIncompatibleReturnType");
        aliases.put("setup","setup");
        aliases.put("finish", "finish");

        METHOD_ALIASES = Collections.unmodifiableMap(aliases);
    }

    // the following fields are closures executed in event-based methods
    private final Map<String, List<Closure>> eventHandlers = new HashMap<String, List<Closure>>();

    private final String scriptPath;

    private final CompilationUnit compilationUnit;

    /**
     * Builds a type checking extension relying on a Groovy script (type checking DSL).
     *
     * @param typeCheckingVisitor the type checking visitor
     * @param scriptPath the path to the type checking script (in classpath)
     * @param compilationUnit
     */
    public GroovyTypeCheckingExtensionSupport(
            final StaticTypeCheckingVisitor typeCheckingVisitor,
            final String scriptPath, final CompilationUnit compilationUnit) {
        super(typeCheckingVisitor);
        this.scriptPath = scriptPath;
        this.compilationUnit = compilationUnit;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    @Override
    public void setup() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass("org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport.TypeCheckingDSL");
        ImportCustomizer ic = new ImportCustomizer();
        ic.addStarImports("org.codehaus.groovy.ast.expr");
        ic.addStaticStars("org.codehaus.groovy.ast.ClassHelper");
        ic.addStaticStars("org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport");
        config.addCompilationCustomizers(ic);
        final GroovyClassLoader transformLoader = compilationUnit!=null?compilationUnit.getTransformLoader():typeCheckingVisitor.getSourceUnit().getClassLoader();

        // since Groovy 2.2, it is possible to use FQCN for type checking extension scripts
        TypeCheckingDSL script = null;
        try {
            Class<?> clazz = transformLoader.loadClass(scriptPath, false, true);
            if (TypeCheckingDSL.class.isAssignableFrom(clazz)) {
                script = (TypeCheckingDSL) clazz.newInstance();
            }
        } catch (ClassNotFoundException e) {
            // silent
        } catch (InstantiationException e) {
            context.getErrorCollector().addFatalError(
                    new SimpleMessage("Static type checking extension '" + scriptPath + "' could not be loaded.",
                            config.getDebug(), typeCheckingVisitor.getSourceUnit()));
        } catch (IllegalAccessException e) {
            context.getErrorCollector().addFatalError(
                    new SimpleMessage("Static type checking extension '" + scriptPath + "' could not be loaded.",
                            config.getDebug(), typeCheckingVisitor.getSourceUnit()));
        }
        if (script==null) {
        ClassLoader cl = typeCheckingVisitor.getSourceUnit().getClassLoader();
        // cast to prevent incorrect @since 1.7 warning
        InputStream is = ((ClassLoader)transformLoader).getResourceAsStream(scriptPath);
        if (is == null) {
            // fallback to the source unit classloader
            is = cl.getResourceAsStream(scriptPath);
        }
        if (is == null) {
            // fallback to the compiler classloader
            cl = GroovyTypeCheckingExtensionSupport.class.getClassLoader();
            is = cl.getResourceAsStream(scriptPath);
        }
        // GRECLIPSE
        // don't place an error here if file not found during reconcile.
        // might be because build hasn't happened yet.
//        /*old{
        if (is == null) {
//        }new*/
//        if (is == null && !typeCheckingVisitor.getSourceUnit().isReconcile) {
            //GRECLIPSE end
            // if the input stream is still null, we've not found the extension
            context.getErrorCollector().addFatalError(
                    new SimpleMessage("Static type checking extension '" + scriptPath + "' was not found on the classpath.",
                            config.getDebug(), typeCheckingVisitor.getSourceUnit()));
        }
        try {
            GroovyShell shell = new GroovyShell(transformLoader, new Binding(), config);
                script = (TypeCheckingDSL) shell.parse(
                    new InputStreamReader(is, typeCheckingVisitor.getSourceUnit().getConfiguration().getSourceEncoding())
            );
            } catch (CompilationFailedException e) {
                throw new GroovyBugError("An unexpected error was thrown during custom type checking", e);
            } catch (UnsupportedEncodingException e) {
                throw new GroovyBugError("Unsupported encoding found in compiler configuration", e);
            }
        }
        if (script!=null) {
            script.extension = this;
            script.run();
            List<Closure> list = eventHandlers.get("setup");
            if (list != null) {
                for (Closure closure : list) {
                    safeCall(closure);
                }
            }
        }
    }

    @Override
    public void finish() {
        List<Closure> list = eventHandlers.get("finish");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure);
            }
        }
    }

    @Override
    public void onMethodSelection(final Expression expression, final MethodNode target) {
        List<Closure> onMethodSelection = eventHandlers.get("onMethodSelection");
        if (onMethodSelection != null) {
            for (Closure closure : onMethodSelection) {
                safeCall(closure, expression, target);
            }
        }
    }

    @Override
    public void afterMethodCall(final MethodCall call) {
        List<Closure> onMethodSelection = eventHandlers.get("afterMethodCall");
        if (onMethodSelection != null) {
            for (Closure closure : onMethodSelection) {
                safeCall(closure, call);
            }
        }
    }

   @Override
    public boolean beforeMethodCall(final MethodCall call) {
       setHandled(false);
       List<Closure> onMethodSelection = eventHandlers.get("beforeMethodCall");
       if (onMethodSelection != null) {
           for (Closure closure : onMethodSelection) {
               safeCall(closure, call);
           }
       }
       return handled;
    }

    @Override
    public boolean handleUnresolvedVariableExpression(final VariableExpression vexp) {
        setHandled(false);
        List<Closure> onMethodSelection = eventHandlers.get("handleUnresolvedVariableExpression");
        if (onMethodSelection != null) {
            for (Closure closure : onMethodSelection) {
                safeCall(closure, vexp);
            }
        }
        return handled;
    }

    @Override
    public boolean handleUnresolvedProperty(final PropertyExpression pexp) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("handleUnresolvedProperty");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, pexp);
            }
        }
        return handled;
    }

    @Override
    public boolean handleUnresolvedAttribute(final AttributeExpression aexp) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("handleUnresolvedAttribute");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, aexp);
            }
        }
        return handled;
    }

    @Override
    public void afterVisitMethod(final MethodNode node) {
        List<Closure> list = eventHandlers.get("afterVisitMethod");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, node);
            }
        }
    }

    @Override
    public boolean beforeVisitClass(final ClassNode node) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("beforeVisitClass");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, node);
            }
        }
        return handled;
    }

    @Override
    public void afterVisitClass(final ClassNode node) {
        List<Closure> list = eventHandlers.get("afterVisitClass");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, node);
            }
        }
    }

    @Override
    public boolean beforeVisitMethod(final MethodNode node) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("beforeVisitMethod");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, node);
            }
        }
        return handled;
    }

    @Override
    public boolean handleIncompatibleAssignment(final ClassNode lhsType, final ClassNode rhsType, final Expression assignmentExpression) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("handleIncompatibleAssignment");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, lhsType, rhsType, assignmentExpression);
            }
        }
        return handled;
    }

    @Override
    public boolean handleIncompatibleReturnType(final ReturnStatement returnStatement, ClassNode inferredReturnType) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("handleIncompatibleReturnType");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, returnStatement, inferredReturnType);
            }
        }
        return handled;
    }

    @Override
    public List<MethodNode> handleMissingMethod(final ClassNode receiver, final String name, final ArgumentListExpression argumentList, final ClassNode[] argumentTypes, final MethodCall call) {
        List<Closure> onMethodSelection = eventHandlers.get("handleMissingMethod");
        List<MethodNode> methodList = new LinkedList<MethodNode>();
        if (onMethodSelection != null) {
            for (Closure closure : onMethodSelection) {
                Object result = safeCall(closure, receiver, name, argumentList, argumentTypes, call);
                if (result != null) {
                    if (result instanceof MethodNode) {
                        methodList.add((MethodNode) result);
                    } else if (result instanceof Collection) {
                        methodList.addAll((Collection<? extends MethodNode>) result);
                    } else {
                        throw new GroovyBugError("Type checking extension returned unexpected method list: " + result);
                    }
                }
            }
        }
        return methodList;
    }

    @Override
    public List<MethodNode> handleAmbiguousMethods(final List<MethodNode> nodes, final Expression origin) {
        List<Closure> onMethodSelection = eventHandlers.get("handleAmbiguousMethods");
        List<MethodNode> methodList = nodes;
        if (onMethodSelection != null) {
            Iterator<Closure> iterator = onMethodSelection.iterator();
            while (methodList.size()>1 && iterator.hasNext() ) {
                final Closure closure = iterator.next();
                Object result = safeCall(closure, methodList, origin);
                if (result != null) {
                    if (result instanceof MethodNode) {
                        methodList = Collections.singletonList((MethodNode) result);
                    } else if (result instanceof Collection) {
                        methodList = new LinkedList<MethodNode>((Collection<? extends MethodNode>) result);
                    } else {
                        throw new GroovyBugError("Type checking extension returned unexpected method list: " + result);
                    }
                }
            }
        }
        return methodList;
    }

    // -------------------------------------
    // delegate to the type checking context
    // -------------------------------------

    // --------------------------------------------
    // end of delegate to the type checking context
    // --------------------------------------------

    public abstract static class TypeCheckingDSL extends Script {
        private GroovyTypeCheckingExtensionSupport extension;

        @Override
        public Object getProperty(final String property) {
            try {
                return InvokerHelper.getProperty(extension, property);
            } catch (Exception e) {
                return super.getProperty(property);
            }
        }

        @Override
        public void setProperty(final String property, final Object newValue) {
            try {
                InvokerHelper.setProperty(extension, property, newValue);
            } catch (Exception e) {
                super.setProperty(property, newValue);
            }
        }

        @Override
        public Object invokeMethod(final String name, final Object args) {
            if (name.startsWith("is") && name.endsWith("Expression") && args instanceof Object[] && ((Object[]) args).length == 1) {
                String type = name.substring(2);
                Object target = ((Object[]) args)[0];
                if (target==null) return false;
                try {
                    Class typeClass = Class.forName("org.codehaus.groovy.ast.expr."+type);
                    return typeClass.isAssignableFrom(target.getClass());
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
            if (args instanceof Object[] && ((Object[]) args).length == 1 && ((Object[]) args)[0] instanceof Closure) {
                Object[] argsArray = (Object[]) args;
                String methodName = METHOD_ALIASES.get(name);
                if (methodName == null) {
                    return InvokerHelper.invokeMethod(extension, name, args);
                }
                List<Closure> closures = extension.eventHandlers.get(methodName);
                if (closures == null) {
                    closures = new LinkedList<Closure>();
                    extension.eventHandlers.put(methodName, closures);
                }
                closures.add((Closure) argsArray[0]);
                return null;
            } else {
                return InvokerHelper.invokeMethod(extension, name, args);
            }
        }
    }
}
