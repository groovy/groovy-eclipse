/*
 * Copyright 2009-2025 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug;

import static org.codehaus.jdt.groovy.integration.internal.GroovyLanguageSupport.newGroovyClassLoader;

import java.util.Collections;
import java.util.Map;

import groovy.lang.Script;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.osgi.framework.wiring.BundleWiring;

public class EvaluationEngine implements IAstEvaluationEngine {

    private final IJavaDebugTarget debugTarget;
    private final IJavaProject javaProject;

    public EvaluationEngine(IJavaProject javaProject, IJavaDebugTarget debugTarget) {
        this.debugTarget = debugTarget;
        this.javaProject = javaProject;
    }

    @Override
    public IJavaDebugTarget getDebugTarget() {
        return debugTarget;
    }

    @Override
    public IJavaProject getJavaProject() {
        return javaProject;
    }

    @Override
    public void dispose() {
    }

    //--------------------------------------------------------------------------

    @Override
    public void evaluate(String snippet, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        evaluateExpression(getCompiledExpression(snippet, frame), frame, listener, evaluationDetail, hitBreakpoints);
    }

    @Override
    public void evaluate(String snippet, IJavaObject object, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        evaluateExpression(getCompiledExpression(snippet, object), object, thread, listener, evaluationDetail, hitBreakpoints);
    }

    @Override
    public void evaluateExpression(ICompiledExpression expression, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        evaluateExpression(expression, frame, null, (IJavaThread) frame.getThread(), listener, evaluationDetail, hitBreakpoints);
    }

    @Override
    public void evaluateExpression(ICompiledExpression expression, IJavaObject object, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        evaluateExpression(expression, null, object, thread, listener, evaluationDetail, hitBreakpoints);
    }

    public void evaluateExpression(ICompiledExpression expression, IJavaPrimitiveValue value, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
    }

    private void evaluateExpression(ICompiledExpression expression, IJavaStackFrame stackFrame, IJavaObject thisObject, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
        if (thread.isPerformingEvaluation() && evaluationDetail == DebugEvent.EVALUATION) {
            listener.evaluationComplete(withError("Cannot perform nested evaluations.", thread, expression.getSnippet()));
        } else {
            thread.queueRunnable(() -> {
                class Eval implements IEvaluationRunnable {
                    @Override
                    public void run(IJavaThread javaThread, IProgressMonitor progressMonitor) throws DebugException {
                        JDITargetDelegate delegate = new JDITargetDelegate(debugTarget, javaThread);
                        JDIBinding groovyBinding = new JDIBinding(delegate, stackFrame, thisObject);
                        String groovyScript = expression.toString(); // package? + import* + snippet
                        IJavaValue result; // provide after cleanup
                        try (JDIScriptLoader loader = createScriptLoader()) {
                            Script script = InvokerHelper.newScript(loader.parseClass(groovyScript), groovyBinding);
                            script.setMetaClass(groovyBinding.getMetaClass()); // delegates to stackFrame/thisObject

                            delegate.initialize(loader);
                            Object proxy = script.run();
                            result = delegate.toJDIValue(proxy);
                        } catch (Throwable t) {
                            if (t instanceof DebugException) {
                                throw (DebugException) t;
                            }
                            if (t.getCause() instanceof DebugException) {
                                throw (DebugException) t.getCause();
                            }
                            throw new DebugException(new Status(Status.ERROR, GroovyPlugin.PLUGIN_ID, null, t));
                        } finally {
                            delegate.cleanup();
                        }
                        listener.evaluationComplete(withValue(result, javaThread, expression.getSnippet()));
                    }

                    private JDIScriptLoader createScriptLoader() {
                        var bundle = GroovyPlugin.getDefault().getBundle();
                        var loader = bundle.adapt(BundleWiring.class).getClassLoader();
                        return new JDIScriptLoader(newGroovyClassLoader(javaProject, loader).getParent());
                    }
                }

                try {
                    thread.runEvaluation(new Eval(), null, evaluationDetail, hitBreakpoints);
                } catch (DebugException e) {
                    listener.evaluationComplete(withError(e, thread, expression.getSnippet()));
                }
            });
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public ICompiledExpression getCompiledExpression(String snippet, IJavaObject object) throws DebugException {
        return getCompiledExpression(snippet, (IJavaReferenceType) object.getJavaType());
    }

    @Override
    public ICompiledExpression getCompiledExpression(String snippet, IJavaStackFrame frame) throws DebugException {
        StringBuilder header = new StringBuilder();

        String typeName = frame.getReferenceType().getName();
        int i = typeName.lastIndexOf('.');
        if (i != -1) {
            String packageName = typeName.substring(0, i);
            header.append("package ").append(packageName);
            header.append(";\n");
        }

        Object sourceElement = getDebugTarget().getLaunch().getSourceLocator().getSourceElement(frame);

        var element = Adapters.adapt(sourceElement, IJavaElement.class);
        if (element != null) {
            var compilationUnit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
            if (compilationUnit != null) {
                try {
                    for (var importDeclaration : compilationUnit.getImports()) {
                        String label = importDeclaration.getElementName();
                        if (!importDeclaration.isOnDemand()) {
                            int x = label.lastIndexOf(' '); if (x == -1) x = label.lastIndexOf('.');
                            if (!snippet.contains(x == -1 ? label : label.substring(x+1))) continue;
                        }
                        header.append("import ");
                        if (Flags.isStatic(importDeclaration.getFlags())) {
                            header.append("static ");
                        }
                        header.append(label);
                        header.append(";\n");
                    }
                    header.append("\n");
                } catch (JavaModelException e) {
                    throw new DebugException(new Status(Status.ERROR, GroovyPlugin.PLUGIN_ID, null, e));
                }
            }
        }

        return getCompiledExpression(snippet, frame.getReferenceType(), Collections.singletonMap("header", header.toString()));
    }

    @Override
    public ICompiledExpression getCompiledExpression(String snippet, IJavaReferenceType type) throws DebugException {
        StringBuilder header = new StringBuilder();

        String typeName = type.getName();
        if (!typeName.endsWith("]")) {
            int i = typeName.lastIndexOf('.');
            if (i != -1) {
                String packageName = typeName.substring(0, i);
                if (!"java.lang".equals(packageName)) {
                    header.append("import ").append(packageName);
                    header.append(".*;\n");
                }
            }

            header.append("import static ").append(typeName).append(".*;\n");
        }

        return getCompiledExpression(snippet, type, Collections.singletonMap("header", header.toString()));
    }

    @Override
    public ICompiledExpression getCompiledExpression(String snippet, IJavaReferenceType type, Map<String, String> compileOptions) throws DebugException {
        return new ICompiledExpression() {

            @Override
            public String toString() {
                return compileOptions.getOrDefault("header", "") + snippet;
            }

            @Override
            public String getSnippet() {
                return snippet;
            }

            @Override
            public boolean hasErrors() {
                return false;
            }

            @Override
            public Message[] getErrors() {
                return null;
            }

            @Override
            public String[] getErrorMessages() {
                return null;
            }
        };
    }

    //--------------------------------------------------------------------------

    private IEvaluationResult withError(DebugException exception, IJavaThread thread, String source) {
        EvaluationResult result = new EvaluationResult();
        result.errorMessages = new String[0];
        result.exception = exception;
        result.snippet = source;
        result.thread = thread;
        // com.sun.jdi.InvocationException: retrieve message from target
        result.errorMessages = org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression.getErrorMessages(result);
        result.errorMessages[0] = result.errorMessages[0].replaceFirst("^startup failed:\\s+Script_[0-9a-f]+\\.groovy:\\s\\d+:\\s*", "");
        return result;
    }

    private IEvaluationResult withError(CharSequence message, IJavaThread thread, String source) {
        String[] messageAsArray = new String[] {message.toString()};
        EvaluationResult result = new EvaluationResult();
        result.errorMessages = messageAsArray;
        result.snippet = source;
        result.thread = thread;
        return result;
    }

    private IEvaluationResult withValue(IJavaValue value, IJavaThread thread, String source) {
        EvaluationResult result = new EvaluationResult();
        result.snippet = source;
        result.thread = thread;
        result.value = value;
        return result;
    }

    private class EvaluationResult implements IEvaluationResult {

        DebugException exception;
        String[] errorMessages;
        boolean terminated;
        IJavaThread thread;
        IJavaValue value;
        String snippet;

        @Override
        public IEvaluationEngine getEvaluationEngine() {
            return EvaluationEngine.this;
        }

        @Override
        public DebugException getException() {
            return exception;
        }

        @Override
        public String[] getErrorMessages() {
            return errorMessages;
        }

        @Override
        public Message[] getErrors() {
            return java.util.Arrays.stream(getErrorMessages()).map(it -> new Message(it, -1)).toArray(Message[]::new);
        }

        @Override
        public boolean hasErrors() {
            return org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean(getErrorMessages());
        }

        @Override
        public boolean isTerminated() {
            return terminated;
        }

        @Override
        public IJavaThread getThread() {
            return thread;
        }

        @Override
        public IJavaValue getValue() {
            return value;
        }

        @Override
        public String getSnippet() {
            return snippet;
        }
    }
}
