/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.grails.ide.eclipse.groovy.debug.core.evaluation;

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;
import groovy.lang.Script;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * A {@link MetaClass} that delegates all operations to a metaclass
 * running in the debugged application.
 * @author Andrew Eisenberg
 * @since 2.5.1
 */
public class JDIMetaClass implements
        MetaClass {

    private final MetaClass origMetaClass;
    private final IJavaObject jdiMetaClass;
    private final JDITargetDelegate delegate;
    
    public JDIMetaClass(MetaClass origMetaClass, IJavaObject jdiMetaClass, JDITargetDelegate delegate) {
        this.origMetaClass = origMetaClass;
        this.jdiMetaClass = jdiMetaClass;
        this.delegate = delegate;
    }
    
    public JDIMetaClass(IJavaObject object, JDITargetDelegate delegate) throws DebugException {
        this.origMetaClass = null;
        this.jdiMetaClass = delegate.getMetaClass(object);
        this.delegate = delegate;
    }
    
    public JDIMetaClass(MetaClass metaClass,
            JDITargetDelegate delegate) throws DebugException {
        this.origMetaClass = metaClass;
        this.jdiMetaClass = delegate.createMetaClassInDebuggedApplication(metaClass);
        this.delegate = delegate;
    }

    public MetaClass getOrigMetaClass() {
        return origMetaClass;
    }
    
    @SuppressWarnings("rawtypes")
    public List respondsTo(Object obj, String name, Object[] argTypes) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @SuppressWarnings("rawtypes")
    public List respondsTo(Object obj, String name) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");

    }

    public MetaProperty hasProperty(Object obj, String name) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public MetaProperty getMetaProperty(String name) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public MetaMethod getStaticMetaMethod(String name, Object[] args) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public MetaMethod getMetaMethod(String name, Object[] args) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public Class<? extends MetaClass> getTheClass() {
        return origMetaClass != null ? origMetaClass.getTheClass() : Object.class;
    }

    public Object invokeConstructor(Object[] arguments) {
        try {
            IJavaValue result = jdiMetaClass.sendMessage("invokeConstructor",  //$NON-NLS-1$
                    "([Ljava/lang/Object;)Ljava/lang/Object;",  //$NON-NLS-1$
                    new IJavaValue[] { delegate.toJDIObject(arguments) }, delegate.getThread(), false);
            return delegate.createProxyFor(result);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    public Object invokeMethod(Object object, String methodName,
            Object[] arguments) {
        try {
            IJavaValue result = jdiMetaClass.sendMessage("invokeMethod",  //$NON-NLS-1$
                    "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",  //$NON-NLS-1$
                    new IJavaValue[] { getClosureDelegate(delegate.toJDIObject(object)), delegate.getTarget().newValue(methodName), delegate.toJDIObject(arguments) }, delegate.getThread(), false);
            
            
            if (!result.isNull() && methodName.equals("asBoolean") && result.getJavaType().getName().equals("java.lang.Boolean") && arguments.length == 0) {
                // assume this is part of an if, while statement, or an elvis operator, etc
                return JDITargetDelegate.convertToBoolean(result);
            } else if (!result.isNull() && methodName.equals("iterator")) {
                // assume this is part of a for loop
                return delegate.convertToIterator(result, new JDIMetaClass((IJavaObject) result, delegate));
            } else {
                return delegate.createProxyFor(result);
            }
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }


    public Object invokeMethod(Object object, String methodName,
            Object arguments) {
        IJavaValue jdiMethodName = null;
        IJavaValue jdiArguments = null;
        IJavaValue jdiMethodTarget = null;
        try {
            jdiMethodTarget = getClosureDelegate(delegate.toJDIObject(object));
            jdiMethodName = delegate.getTarget().newValue(methodName);
            jdiArguments = delegate.toJDIObject(arguments);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
        
        // if the method target is the running script and current stack frame is static,
        // then must call invoke static method instead
        if (object instanceof Script && jdiMethodTarget instanceof IJavaClassObject) {
            return invokeStaticMethod(object, methodName, arguments instanceof Object[] ? (Object[]) arguments : new Object[] { arguments });
        }
        
        
        // must try to invoke the method twice.  First using the closure delegate, and second not
        // the first will handle method invocations in most situations, but will fail on calls
        // to 'print' or other DGMs when inside of closures
        try {
            IJavaValue result = jdiMetaClass.sendMessage("invokeMethod",  //$NON-NLS-1$
                    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;",  //$NON-NLS-1$
                    new IJavaValue[] { jdiMethodTarget, jdiMethodName, jdiArguments }, delegate.getThread(), false);
            return delegate.createProxyFor(result);
        } catch (DebugException e) {
            try {
                IJavaValue result = jdiMetaClass.sendMessage("invokeMethod",  //$NON-NLS-1$
                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;",  //$NON-NLS-1$
                        new IJavaValue[] { delegate.toJDIObject(object), jdiMethodName, jdiArguments }, delegate.getThread(), false);
                return delegate.createProxyFor(result);
            } catch (DebugException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    public Object invokeStaticMethod(Object object, String methodName,
            Object[] arguments) {
        try {
            IJavaObject newValue = (IJavaObject) delegate.getTarget().newValue(methodName);
            delegate.disableCollection(newValue);
            IJavaValue result = jdiMetaClass.sendMessage("invokeStaticMethod",  //$NON-NLS-1$
                    "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",  //$NON-NLS-1$
                    new IJavaValue[] { getClosureDelegate(delegate.toJDIObject(object)), newValue, delegate.toJDIObject(arguments) }, delegate.getThread(), false);
            return delegate.createProxyFor(result);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getProperty(Object object, String property) {
        try {
            
            IJavaValue target = delegate.toJDIObject(object);
            
            IJavaObject targetMetaClass = jdiMetaClass;
            // if we are using a closure, then we should
            // try the delegate first instead of the current type
            // except owner and delegates should be grabbed from the actual closure object
            if (!property.equals("owner") && !property.equals("delegate") && isClosureType(target)) {
                IJavaValue closureTarget = getClosureDelegate(target);
                try {
                    Object maybeProperty = getProperty(closureTarget, property);
                    if (maybeProperty != null) {
                        return maybeProperty;
                    }
                } catch (RuntimeException e) {
                    // ignore since could be thrown for normal reasons
                    // and if there really is a problem, then we will hit it
                    // again below
                    // probably a MissingPropertyException
                }
            }
            
            if (target instanceof IJavaObject) {
                // try to shortcut and grab the field directly if it exists
                IJavaObject jdiObject = (IJavaObject) target;
                IJavaFieldVariable field = jdiObject.getField(property, false);
                if (field != null) {
                    return delegate.createProxyFor((IJavaValue) field.getValue());
                }
                
                // now try to see if there is a getProperty method that is explicitly defined
                try {
                    return delegate.createProxyFor(jdiObject.sendMessage("getProperty", "(Ljava/lang/String;)Ljava/lang/Object;", new IJavaValue[] { delegate.getTarget().newValue(property) }, delegate.getThread(), false));
                } catch (DebugException e) {
                    // continue on our merry way.  Likely that the method just isn't there
                }
            }
            
            IJavaValue propertyValue = targetMetaClass.sendMessage("getProperty", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;",  //$NON-NLS-1$ //$NON-NLS-2$
                    new IJavaValue[] { target, delegate.getTarget().newValue(property) }, delegate.getThread(), false);
            return delegate.createProxyFor(propertyValue);
        } catch (DebugException e) {
            // probably a MissingPropertyException
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the closure owner if this jdoObject represents a closure object 
     * on the running app, or else return the object
     * @param jdiObject
     * @return
     * @throws DebugException 
     */
    private IJavaValue getClosureDelegate(IJavaValue jdiValue) throws DebugException {
        IJavaObject jdiObject = (IJavaObject) jdiValue;
        IJavaFieldVariable delegateField = jdiObject.getField("delegate", false);
        if (delegateField != null) {
            jdiValue = (IJavaValue) delegateField.getValue();
        }
        return jdiValue;
    }

    /**
     * @param jdiValue
     * @return
     * @throws DebugException
     */
    protected boolean isClosureType(IJavaValue jdiValue) throws DebugException {
        return jdiValue instanceof IJavaObject && !jdiValue.isNull() && (
                jdiValue.getJavaType().getName().indexOf("$_closure") > 0 ||
                jdiValue.getJavaType().getName().indexOf("$_run_closure") > 0
                );
    }

    // FIXADE in static frames, this must behave differently
    public void setProperty(Object object, String property, Object newValue) {
        try {
            IJavaObject propertyValue = (IJavaObject) delegate.getTarget().newValue(property);
            delegate.disableCollection(propertyValue);
            IJavaObject jdiObject = (IJavaObject) delegate.toJDIObject(newValue);
            delegate.disableCollection(jdiObject);
            jdiMetaClass.sendMessage("setProperty", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V",  //$NON-NLS-1$ //$NON-NLS-2$
                    new IJavaValue[] { delegate.toJDIObject(object), propertyValue, jdiObject }, delegate.getThread(), false);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getAttribute(Object object, String attribute) {
        try {
            IJavaValue value = jdiMetaClass.sendMessage("getAttribute", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;",  //$NON-NLS-1$ //$NON-NLS-2$
                    new IJavaValue[] { delegate.toJDIObject(object), delegate.getTarget().newValue(attribute) }, delegate.getThread(), false);
            return delegate.createProxyFor(value);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAttribute(Object object, String attribute, Object newValue) {
        try {
            jdiMetaClass.sendMessage("setAttribute", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V",  //$NON-NLS-1$ //$NON-NLS-2$
                    new IJavaValue[] { delegate.toJDIObject(object), delegate.getTarget().newValue(attribute), delegate.toJDIObject(newValue) }, delegate.getThread(), false);
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }

    public Object invokeMethod(@SuppressWarnings("rawtypes") Class sender, Object receiver,
            String methodName, Object[] arguments, boolean isCallToSuper,
            boolean fromInsideClass) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public Object getProperty(@SuppressWarnings("rawtypes") Class sender, Object receiver, String property,
            boolean isCallToSuper, boolean fromInsideClass) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public void setProperty(@SuppressWarnings("rawtypes") Class sender, Object receiver, String property,
            Object value, boolean isCallToSuper, boolean fromInsideClass) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public Object invokeMissingMethod(Object instance, String methodName,
            Object[] arguments) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public Object invokeMissingProperty(Object instance, String propertyName,
            Object optionalValue, boolean isGetter) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public Object getAttribute(@SuppressWarnings("rawtypes") Class sender, Object receiver,
            String messageName, boolean useSuper) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public void setAttribute(@SuppressWarnings("rawtypes") Class sender, Object receiver, String messageName,
            Object messageValue, boolean useSuper, boolean fromInsideClass) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public void initialize() {
        // no-op?
    }

    @SuppressWarnings("rawtypes")
    public List getProperties() {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @SuppressWarnings("rawtypes")
    public List getMethods() {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public ClassNode getClassNode() {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    @SuppressWarnings("rawtypes")
    public List getMetaMethods() {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public int selectConstructorAndTransformArguments(int numberOfConstructors,
            Object[] arguments) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }

    public MetaMethod pickMethod(String methodName, @SuppressWarnings("rawtypes") Class[] arguments) {
        throw new IllegalArgumentException("Not implemented (Groovy-Eclipse)");
    }
}
