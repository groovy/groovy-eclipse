/*
 * Copyright 2009-2023 the original author or authors.
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

import java.util.List;

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import groovy.lang.Script;

import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * Delegates to a {@link MetaClass} running in the debugged application.
 *
 * @author Andrew Eisenberg
 */
public class JDIMetaClass implements MetaClass {

    private final MetaClass theMetaClass;
    private final IJavaObject jdiMetaClass;
    private final JDITargetDelegate delegate;

    public JDIMetaClass(IJavaObject object, JDITargetDelegate delegate) throws DebugException {
        this(null, delegate.getMetaClass(object), delegate);
    }

    public JDIMetaClass(MetaClass metaClass, JDITargetDelegate delegate) throws DebugException {
        this(metaClass, delegate.createMetaClassInDebuggedApplication(metaClass), delegate);
    }

    public JDIMetaClass(MetaClass theMetaClass, IJavaObject jdiMetaClass, JDITargetDelegate delegate) {
        this.theMetaClass = theMetaClass;
        this.jdiMetaClass = jdiMetaClass;
        this.delegate = delegate;
    }

    //--------------------------------------------------------------------------

    @Override
    public Object getAttribute(Class sender, Object receiver, String messageName, boolean useSuper) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public Object getAttribute(Object object, String attribute) {
        try {
            IJavaValue attributeValue = sendMessage(jdiMetaClass, "getAttribute",
                "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;", delegate.toJDIObject(object), delegate.getTarget().newValue(attribute));
            return delegate.createProxyFor(attributeValue);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ClassNode getClassNode() {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public MetaMethod getMetaMethod(String name, Object[] args) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public List<MetaMethod> getMetaMethods() {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public MetaProperty getMetaProperty(String name) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public List<MetaMethod> getMethods() {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public List<MetaProperty> getProperties() {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public Object getProperty(Class sender, Object receiver, String property, boolean isCallToSuper, boolean fromInsideClass) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public Object getProperty(Object object, String property) {
        try {
            IJavaValue target = delegate.toJDIObject(object);
            // if we are using a closure, then we should
            // try the delegate first instead of the current type
            // except owner and delegates should be grabbed from the actual closure object
            if (!"owner".equals(property) && !"delegate".equals(property) && (target instanceof IJavaObject && !target.isNull() &&
                    (target.getJavaType().getName().contains("$_closure") || target.getJavaType().getName().contains("$_run_closure")))) {
                try {
                    return getProperty(getClosureDelegate(target), property);
                } catch (RuntimeException e) {
                    // ignore since it could be thrown for normal reasons and if
                    // there really is a problem then we will hit it again below
                }
            }

            if (target instanceof IJavaObject && !(target instanceof IJavaClassObject)) {
                try { // see if there is an explicit getProperty(String) method
                    IJavaValue propertyValue = sendMessage((IJavaObject) target, "getProperty",
                        "(Ljava/lang/String;)Ljava/lang/Object;", delegate.getTarget().newValue(property));
                    return delegate.createProxyFor(propertyValue);
                } catch (DebugException e) {
                    // likely the method just isn't there
                }
            }

            IJavaValue propertyValue = sendMessage(jdiMetaClass, "getProperty",
                "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;", target, delegate.getTarget().newValue(property));
            return delegate.createProxyFor(propertyValue);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MetaMethod getStaticMetaMethod(String name, Object[] args) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public Class getTheClass() {
        return theMetaClass != null ? theMetaClass.getTheClass() : Object.class;
    }

    @Override
    public MetaProperty hasProperty(Object obj, String name) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public void initialize() {
    }

    @Override
    public Object invokeConstructor(Object[] arguments) {
        try {
            IJavaValue newObject = sendMessage(jdiMetaClass, "invokeConstructor",
                "([Ljava/lang/Object;)Ljava/lang/Object;", delegate.toJDIObject(arguments));
            return delegate.createProxyFor(newObject);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public Object invokeMethod(Object object, String methodName, Object arguments) {
        IJavaValue jdiMethodTarget;
        try {
            jdiMethodTarget = delegate.toJDIObject(object);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }

        // invoke static method if target is the running script and "this" is static
        if (object instanceof Script && jdiMethodTarget instanceof IJavaClassObject) {
            return invokeStaticMethod(object, methodName, arguments instanceof Object[] ? (Object[]) arguments : new Object[] {arguments});
        }

        IJavaValue jdiClosureDelegate, jdiMethodName, jdiMethodArgs;
        try {
            jdiClosureDelegate = getClosureDelegate(jdiMethodTarget);
            jdiMethodName = delegate.getTarget().newValue(methodName);
            jdiMethodArgs = delegate.toJDIObject(arguments);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }

        // must try to invoke the method twice -- first using the closure delegate, and second
        // not first will handle method invocations in most situations, but will fail on calls
        // to 'print' or other DGMs when inside of closures
        try {
            IJavaValue returnValue = sendMessage(jdiMetaClass, "invokeMethod",
                "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", jdiClosureDelegate, jdiMethodName, jdiMethodArgs);
            return delegate.createProxyFor(returnValue);
        } catch (DebugException e) {
            try {
                if (jdiClosureDelegate != jdiMethodTarget) {
                    IJavaValue returnValue = sendMessage(jdiMetaClass, "invokeMethod",
                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", jdiMethodTarget, jdiMethodName, jdiMethodArgs);
                    return delegate.createProxyFor(returnValue);
                }
            } catch (DebugException ignore) {
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object invokeMethod(Object object, String methodName, Object[] arguments) {
        try {
            IJavaValue returnValue = sendMessage(jdiMetaClass, "invokeMethod",
                "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                getClosureDelegate(delegate.toJDIObject(object)), delegate.getTarget().newValue(methodName), delegate.toJDIObject(arguments));
            if (!returnValue.isNull()) {
                if (returnValue.getJavaType().getName().equals("java.lang.Boolean")) { // "asBoolean", etc.
                    return delegate.convertToBoolean(returnValue);
                }
                if (returnValue.getJavaType().getName().equals("java.lang.Integer")) { // "compareTo", etc.
                    return delegate.convertToInteger(returnValue);
                }
                if ("iterator".equals(methodName)) { // part of a for-each loop?
                    return delegate.convertToIterator(returnValue, new JDIMetaClass((IJavaObject) returnValue, delegate));
                }
            }
            return delegate.createProxyFor(returnValue);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        try {
            IJavaValue returnValue = sendMessage(jdiMetaClass, "invokeStaticMethod",
                "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                getClosureDelegate(delegate.toJDIObject(object)), delegate.getTarget().newValue(methodName), delegate.toJDIObject(arguments));
            return delegate.createProxyFor(returnValue);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MetaMethod pickMethod(String methodName, Class[] arguments) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public List<MetaMethod> respondsTo(Object obj, String name) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public List<MetaMethod> respondsTo(Object obj, String name, Object[] argTypes) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public int selectConstructorAndTransformArguments(int numberOfConstructors, Object[] arguments) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public void setAttribute(Class sender, Object receiver, String messageName, Object messageValue, boolean useSuper, boolean fromInsideClass) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public void setAttribute(Object object, String attribute, Object newValue) {
        try {
            sendMessage(jdiMetaClass, "setAttribute", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V",
                delegate.toJDIObject(object), delegate.getTarget().newValue(attribute), delegate.toJDIObject(newValue));
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setProperty(Class sender, Object receiver, String property, Object value, boolean isCallToSuper, boolean fromInsideClass) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @Override
    public void setProperty(Object object, String property, Object newValue) {
        try {
            sendMessage(jdiMetaClass, "setProperty", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V",
                delegate.toJDIObject(object), delegate.getTarget().newValue(property), delegate.toJDIObject(newValue));
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the closure delegate if the jdiValue represents a closure object
     * on the running app, or else return the value.
     */
    private static IJavaValue getClosureDelegate(IJavaValue jdiValue) throws DebugException {
        if (jdiValue instanceof IJavaObject) {
            IJavaFieldVariable delegateField = ((IJavaObject) jdiValue).getField("delegate", false);
            if (delegateField != null) {
                jdiValue = (IJavaValue) delegateField.getValue();
            }
        }
        return jdiValue;
    }

    private IJavaValue sendMessage(IJavaObject receiver, String methodName, String signature, IJavaValue... arguments) throws DebugException {
        return receiver.sendMessage(methodName, signature, arguments, delegate.getThread(), false);
    }
}
