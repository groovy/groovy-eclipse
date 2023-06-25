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

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.Script;
import groovy.util.Proxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.grails.ide.eclipse.groovy.debug.core.GroovyDebugCoreActivator;

import com.sun.jdi.VMDisconnectedException;

/**
 * This class provides some useful operations to perform on
 * the {@link IJavaDebugTarget}.
 * @author Andrew Eisenberg
 * @since 2.5.1
 */
public class JDITargetDelegate {
    
    /**
     * 
     * @author Andrew Eisenberg
     * @created Nov 12, 2010
     */
    private final class JDIProxy extends Proxy {
        @Override
           public String toString() {
               // strip quotes if exists
               // not right...what if quotes really exist???
               String s = this.getAdaptee().toString();
               if (s.charAt(0) == '"') {
                   s = s.substring(1);
               }
               if (s.charAt(s.length()-1) == '"') {
                   s = s.substring(0, s.length()-1);
               }
               return s;
           }

    }

    /**
     * Visits the script used for evaluation and looks for all class references
     * these references will then have their meta classes changed.
     */
    class ClassStoreVisitor extends ClassCodeVisitorSupport {
        
        Set<Class<?>> referencedClasses;
        private final JDIGroovyClassLoader loader;
        
        ClassStoreVisitor(JDIGroovyClassLoader loader) {
            this.loader = loader;
        }
        
        Set<Class<?>> doVisit(ClassNode node) {
            referencedClasses = new HashSet<Class<?>>();
            node.visitContents(this);
            return referencedClasses;
        }
    
        protected SourceUnit getSourceUnit() {
            return null;
        }
        
        private void storeClass(ClassNode classNode) {
            try {
                Class<?> typeClass = classNode.getTypeClass();
                if (typeClass != null) {
                    referencedClasses.add(typeClass);
                } 
            } catch (GroovyBugError e) {
                // could be that this is a class reference of a class defined in the
                // script.
                if (classNode.getModule().getDescription().equals(GroovyJDIEvaluator.SCRIPT_FILE_NAME) &&
                        ! classNode.getName().equals(syntheticScriptName)) {
                    // try to load it (but don't try to load the generated script class)
                    try {
                        Class<?> typeClass = loader.loadClass(classNode.getName());
                        if (typeClass != null) {
                            referencedClasses.add(typeClass);
                        }
                    } catch (ClassNotFoundException cnfe) {
                        // can ignore
                    }
                }
            }
        }
        
        public void visitStaticMethodCallExpression(
                StaticMethodCallExpression expression) {
            storeClass(expression.getType());
            super.visitStaticMethodCallExpression(expression);
        }
    
        public void visitClassExpression(ClassExpression expression) {
            storeClass(expression.getType());
            super.visitClassExpression(expression);
        }
        
        public void visitConstantExpression(ConstantExpression expression) {
            storeClass(expression.getType());
            super.visitConstantExpression(expression);
        }
        
        public void visitConstructorCallExpression(
                ConstructorCallExpression expression) {
            storeClass(expression.getType());
            super.visitConstructorCallExpression(expression);
        }
        
        public void visitMapExpression(MapExpression expression) {
            storeClass(expression.getType());
            super.visitMapExpression(expression);
        }
        
        public void visitMapEntryExpression(MapEntryExpression expression) {
            storeClass(expression.getType());
            super.visitMapEntryExpression(expression);
        }
        
        public void visitListExpression(ListExpression expression) {
            storeClass(expression.getType());
            super.visitListExpression(expression);
        }
        
    }

    private final IJavaDebugTarget target;
    private final IJavaThread thread;
    
    private Set<MetaClass> cachedOriginalMetaClasses;
    
    private String syntheticScriptName;
    
    // keep track of values that we have turned off garbage collection for
    private List<IJavaObject> permStorage;

    public JDITargetDelegate(IJavaDebugTarget target, IJavaThread thread) {
        super();
        this.target = target;
        this.thread = thread;
        this.permStorage = new ArrayList<IJavaObject>();
    }
    
    public void initialize(JDIGroovyClassLoader loader, String syntheticScriptName) throws DebugException {
        this.syntheticScriptName = syntheticScriptName;
        cachedOriginalMetaClasses = new HashSet<MetaClass>();
        
        // need to determine which classes are statically referenced
        // so parse the class and visit all types
        // do we need to visit imports?
        
        ClassStoreVisitor visitor = new ClassStoreVisitor(loader);
        Set<Class<?>> allClasses = visitor.doVisit(loader.getTheClass());
        
        // force metaclasses for each of the types that can be referenced as literals
        for (Class<?> clazz : allClasses) {
            createMetaClassFor(loader, clazz);
        }
    }

    private void createMetaClassFor(ClassLoader loader, Class<?> clazz)
            throws DebugException {
        MetaClass meta = GroovySystem.getMetaClassRegistry().getMetaClass(clazz);
        JDIMetaClass jidMetaClass = new JDIMetaClass(meta, this);
        GroovySystem.getMetaClassRegistry().setMetaClass(clazz, jidMetaClass);
        cachedOriginalMetaClasses.add(meta);
        if (clazz.isPrimitive()) {
            // also create metaclass for boxed type
            if (clazz == int.class) {
                createMetaClassFor(loader, Integer.class);
            } else if (clazz == double.class) {
                createMetaClassFor(loader, Double.class);
            } else if (clazz == boolean.class) {
                createMetaClassFor(loader, Boolean.class);
            } else if (clazz == char.class) {
                createMetaClassFor(loader, Character.class);
            } else if (clazz == short.class) {
                createMetaClassFor(loader, Short.class);
            } else if (clazz == byte.class) {
                createMetaClassFor(loader, Byte.class);
            } else if (clazz == long.class) {
                createMetaClassFor(loader, Long.class);
            } else if (clazz == float.class) {
                createMetaClassFor(loader, Float.class);
            } else if (clazz == void.class) {
                createMetaClassFor(loader, Void.class);
            }
        }
    }
    
    public void cleanup() {
        // will only be null if there was an error during initialization
        if (cachedOriginalMetaClasses != null) {
            for (MetaClass meta : cachedOriginalMetaClasses) {
                GroovySystem.getMetaClassRegistry().removeMetaClass(meta.getTheClass());
            }
        }
        
        // now release garbage collected objects
        if (permStorage != null) {
            for (IJavaObject object : permStorage) {
                try {
                    object.enableCollection();
                } catch (CoreException e) {
                    // don't worry about GC if the VM has terminated 
                    if ((e.getStatus().getException() instanceof VMDisconnectedException)) {
                        break;
                    }
                    GroovyDebugCoreActivator.log(e);
                }
            }
            permStorage = null;
        }
    }
    
    public void disableCollection(IJavaObject object) {
        try {
            object.disableCollection();
            permStorage.add(object);
        } catch (CoreException e) {
            GroovyDebugCoreActivator.log(e);
        }
    }
    
    public Proxy createProxyFor(IJavaValue value) throws DebugException {
        Proxy proxyObj = new JDIProxy();
        if (value instanceof JDIObjectValue) {
            if (!value.isNull() && value.getJavaType().getName().equals("groovy.lang.Reference")) {
                JDIObjectValue object = (JDIObjectValue) value;
                value = object.sendMessage("get", "()Ljava/lang/Object;", new IJavaValue[0], getThread(), false);
            }
        }
        
        proxyObj.setMetaClass(new JDIMetaClass(null, getMetaClass(value), this));
        proxyObj.setAdaptee(value);
        return proxyObj;
    }
    
    public IJavaObject getMetaClass(IJavaValue object) throws DebugException {
        if (object.isNull()) {
            IJavaReferenceType nullObjectClass = (IJavaReferenceType) getType("org.codehaus.groovy.runtime.NullObject");
            return invokeStaticMethod("org.codehaus.groovy.runtime.InvokerHelper", "getMetaClass", "(Ljava/lang/Class;)Lgroovy/lang/MetaClass;",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    new IJavaValue[] { nullObjectClass.getClassObject() }, thread);
        } else if (object instanceof IJavaObject) {
            IJavaFieldVariable var = ((IJavaObject) object).getField("metaClass", false); //$NON-NLS-1$
            if (var != null) {
                return (IJavaObject) var.getValue();
            }
        } 
        // probably doesn't have groovy object support.  use InvokerHelper
        return invokeStaticMethod("org.codehaus.groovy.runtime.InvokerHelper", "getMetaClass", "(Ljava/lang/Object;)Lgroovy/lang/MetaClass;",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                new IJavaValue[] { object }, thread);
    }
    
    public IJavaObject invokeStaticMethod(String className, String methodName,
            String methodSignature, IJavaValue[] args, IJavaThread thread) throws DebugException {
        IJavaType type = getType(className);
        if (type instanceof IJavaClassType) {
            IJavaValue[] newargs = new IJavaValue[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof IJavaPrimitiveValue) {
                    newargs[i] = convertToJDIBoxedType(args[i]);
                } else {
                    newargs[i] = args[i];
                }
            }
            return (IJavaObject) ((IJavaClassType) type).sendMessage(methodName, methodSignature, newargs, thread);
        } else {
            return throwInvalidObjectException(type);
        }
    }

    public IJavaType getType(String className) throws DebugException {
        IJavaType[] types = target.getJavaTypes(className); 
        IJavaType classClass = null;
        if (types != null && types.length > 0) {
            classClass = types[0]; 
        }
        if (classClass == null) {
            // try to forcibly load the class
            getClassObject(className);
            types = target.getJavaTypes(className); 
            if (types != null && types.length > 0) {
                classClass = types[0]; 
            }
            
            if (classClass == null) {
                throwInvalidObjectException(className);
            }
        }
        return classClass;
    }

    /**
     * Constructs and returns a new instance of the specified
     * class on the  target VM.  Assumes that a no-arg constructor exists
     * 
     * @param className fully qualified class name
     * @return a new instance on the target, as an <code>IJavaValue</code>
     * @exception DebugException if creation fails
     */
    protected IJavaObject newInstance(String className) throws DebugException {
        IJavaClassObject classObject = getClassObject(className);
        IJavaObject object = (IJavaObject) classObject.sendMessage("newInstance", "()Ljava/lang/Object;", null, thread, false); //$NON-NLS-2$ //$NON-NLS-1$
        return object;
    }
    
    protected IJavaArray newArrayInstance(String className, int size) throws DebugException {
        IJavaClassObject classObject = getClassObject(className);
        IJavaArrayType arrayType = (IJavaArrayType) classObject.getInstanceType();
        IJavaArray object = arrayType.newInstance(size);
        disableCollection(object);
        return object;
    }



    public IJavaClassObject getClassObject(String className) throws DebugException {
        // Force the load of the class.
        IJavaClassType classClass = (IJavaClassType) getType("java.lang.Class"); //$NON-NLS-1$
        IJavaValue[] args = new IJavaValue[] { target.newValue(className)};
        disableCollection((IJavaObject) args[0]);
        IJavaClassObject classObject = (IJavaClassObject) classClass.sendMessage("forName", "(Ljava/lang/String;)Ljava/lang/Class;", args, thread); //$NON-NLS-2$ //$NON-NLS-1$
        return classObject;
    }

    /**
     * Recursively converts an object to its JDI equivalent.
     * {@link Proxy} objects return their adaptee
     * Array objects return a JDI array where each array element is converted to a JDI Object
     * Literals (eg- primitives, strings, lists, and maps) are also converted
     * @param o
     * @return
     * @throws DebugException if the object passed in is not a valid object that can be converted to
     * JDI
     */
    public IJavaValue toJDIObject(Object o) throws DebugException {
        if (o instanceof Proxy) {
            Proxy p = (Proxy) o;
            Object adaptee = p.getAdaptee();
            if (adaptee instanceof IJavaObject) {
                IJavaObject adapteeJDI = (IJavaObject) adaptee;
                return adapteeJDI;
            } else if (adaptee instanceof IJavaPrimitiveValue) {
                return convertToJDIBoxedType(adaptee);
            } else {
                return throwInvalidObjectException(o);
            }
        } else if (o instanceof Object[]) {
            Object[] arr = (Object[]) o;
            IJavaArray arrayObject = newArrayInstance(arr.getClass().getName(), arr.length); //$NON-NLS-1$
            for (int i = 0; i < arr.length; i++) {
                arrayObject.setValue(i, toJDIObject(arr[i]));
            }
            return arrayObject;
        } else if (o instanceof IJavaValue) {
            return (IJavaValue) o;
        } else if (o instanceof Script) {
            // return the currently executing object
            // if currently executing frame is static, then return
            // the java.lang.Class for the frame.
            IJavaObject thiz = getThis();
            return thiz;
        } else if (o == null) {
            return target.nullValue();
        } else if (o instanceof Class) {
            return ((IJavaReferenceType) getType(((Class<?>) o).getName())).getClassObject();
        } else {
            // probably coming from a literal
            if (o instanceof Integer ||
                o instanceof Boolean || 
                o instanceof Double ||
                o instanceof Float ||
                o instanceof Byte || 
                o instanceof Character || 
                o instanceof Short ||
                o instanceof Long) {
                return convertToJDIBoxedType(o);

            // other non-primitives that can be specified as constants
            } else if (o instanceof String) {
                return target.newValue((String) o);
            } else if (o instanceof List) {
                // create an ArrayList in the debugged application and add each of the elements
                return newJDIList((List<?>) o);
            } else if (o instanceof Map) {
                // create a HashMap in the debugged application and put each of the entries in it
                return newJDIMap((Map<?, ?>) o);
            } else if (o instanceof GString) {
                // create a GString in the debugged application
                return newJDIGString((GString) o);
            } else if (o instanceof Closure) {
                //  hmmmmm....this is not working
                // create Closure in the debugged application
                return newJDIClosure((Closure) o);
            } else {
                return throwInvalidObjectException(o);
            }
        }
    }

    /**
     * Creates a GString in the debugged application
     * @param strings
     * @param values
     * @return
     * @throws DebugException 
     */
    private IJavaValue newJDIGString(GString gstring) throws DebugException {
        String[] strings = gstring.getStrings();
        Object[] values = gstring.getValues();
        IJavaClassObject classObject = getClassObject("org.codehaus.groovy.runtime.GStringImpl");
        // returns the 2-arg constructor
        IJavaArray constructors = (IJavaArray) classObject.sendMessage("getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;", null, thread, false); //$NON-NLS-2$ //$NON-NLS-1$
        IJavaObject constructor = (IJavaObject) constructors.getValue(0);
        IJavaArray args = newArrayInstance("[Ljava.lang.Object;", 2);  // note that here  we need to use '.' instead of '/' to create the array
        args.setValue(0, toJDIObject(values));
        args.setValue(1, toJDIObject(strings));
        
        return constructor.sendMessage("newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", new IJavaValue[] { args }, thread, false);
    }

    /**
     * @return 'this', the currently executing object on the current stack
     * frame.  Or returns the class object if a static frame.
     * @throws DebugException
     */
    protected IJavaObject getThis() throws DebugException {
        IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
        IJavaObject thisObj = frame.getThis();
        if (thisObj != null) {
            return thisObj;
        } else {
            // in a static frame
            return frame.getReferenceType().getClassObject();
        }
    }

    
    /**
     * Converts a boxed primitive object type to a JDI boxed type
     * @param o the object to convert
     * @return a JDI object corresponding to the boxed type
     * @throws DebugException
     */
    protected IJavaValue convertToJDIBoxedType(Object o) throws DebugException {
        IJavaClassType type;
        if (o instanceof IJavaPrimitiveValue) {
            IJavaPrimitiveValue val = (IJavaPrimitiveValue) o;
            type = getBoxedClassTypeForPrimitive(val.getSignature());
        } else {
            String className = o.getClass().getName();
            type = (IJavaClassType) getType(className);
        }
        
        IJavaValue primitiveValue;
        String sig = null;
        
        // first convert to JDI primitive
        if (o instanceof Integer) {
            primitiveValue = target.newValue((Integer) o);
        } else if (o instanceof Boolean) {
            primitiveValue = target.newValue((Boolean) o);
        } else if (o instanceof Double) {
            primitiveValue = target.newValue((Double) o);
        } else if (o instanceof Float) {
            primitiveValue = target.newValue((Float) o);
        } else if (o instanceof Byte) {
            primitiveValue = target.newValue((Byte) o);
        } else if (o instanceof Character) {
            primitiveValue = target.newValue((Character) o);
        } else if (o instanceof Short) {
            primitiveValue = target.newValue((Short) o);
        } else if (o instanceof Long) {
            primitiveValue = target.newValue((Long) o);
        } else if (o instanceof IJavaPrimitiveValue) {
            primitiveValue = (IJavaPrimitiveValue) o;
        } else {
            // shouldn't get here
            primitiveValue = null;
            throw new DebugException(new Status(IStatus.ERROR, GroovyDebugCoreActivator.PLUGIN_ID, "(Groovy) Cannot convert object " + o + " into a primitive"));
        }
        
        // now find the signature of the valueOf method to use
        if (primitiveValue != null) {
            if (primitiveValue.getSignature().equals("I")) {
                sig = "(I)Ljava/lang/Integer;";    
            } else if (primitiveValue.getSignature().equals("Z")) {
                sig = "(Z)Ljava/lang/Boolean;";
            } else if (primitiveValue.getSignature().equals("D")) {
                sig = "(D)Ljava/lang/Double;";
            } else if (primitiveValue.getSignature().equals("F")) {
                sig = "(F)Ljava/lang/Float;";
            } else if (primitiveValue.getSignature().equals("B")) {
                sig = "(B)Ljava/lang/Byte;";
            } else if (primitiveValue.getSignature().equals("C")) {
                sig = "(C)Ljava/lang/Character;";
            } else if (primitiveValue.getSignature().equals("S")) {
                sig = "(S)Ljava/lang/Short;";
            } else if (primitiveValue.getSignature().equals("J")) {
                sig = "(J)Ljava/lang/Long;";
            }
        }
        
        // now extract the primitive from the boxed value
        return type.sendMessage("valueOf", sig, new IJavaValue[] { primitiveValue }, thread); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public IJavaPrimitiveValue convertToUnboxedType(IJavaValue value) throws DebugException {
        
        if (value instanceof IJavaPrimitiveValue) {
            return (IJavaPrimitiveValue) value;
        }
        
        String methodNameToInvoke;
        String methodSignatureToInvoke;
        if (value.getJavaType().getName().equals("java.lang.Integer")) {
            methodNameToInvoke = "intValue";
            methodSignatureToInvoke = "()I";
        } else if (value.getJavaType().getName().equals("java.lang.Boolean")) {
            methodNameToInvoke = "booleanValue";
            methodSignatureToInvoke = "()Z";
        } else if (value.getJavaType().getName().equals("java.lang.Double")) {
            methodNameToInvoke = "doubleValue";
            methodSignatureToInvoke = "()D";
        } else if (value.getJavaType().getName().equals("java.lang.Float")) {
            methodNameToInvoke = "floatValue";
            methodSignatureToInvoke = "()F";
        } else if (value.getJavaType().getName().equals("java.lang.Byte")) {
            methodNameToInvoke = "byteValue";
            methodSignatureToInvoke = "()B";
        } else if (value.getJavaType().getName().equals("java.lang.Character")) {
            methodNameToInvoke = "charValue";
            methodSignatureToInvoke = "()C";
        } else if (value.getJavaType().getName().equals("java.lang.Short")) {
            methodNameToInvoke = "shortValue";
            methodSignatureToInvoke = "()S";
        } else if (value.getJavaType().getName().equals("java.lang.Long")) {
            methodNameToInvoke = "longValue";
            methodSignatureToInvoke = "()J";
        } else {
            methodNameToInvoke = null;
            methodSignatureToInvoke = null;
            throw new DebugException(new Status(IStatus.ERROR, GroovyDebugCoreActivator.PLUGIN_ID, "(Groovy) Cannot convert object " + value + " into a primitive"));
        }
        
        return (IJavaPrimitiveValue) ((IJavaObject) value).sendMessage(methodNameToInvoke, methodSignatureToInvoke, new IJavaValue[0], thread, false);
    }

    protected IJavaClassType getBoxedClassTypeForPrimitive(
            String primitiveSignature) throws DebugException {
        IJavaClassType type;
        String className;
        if (primitiveSignature.equals("I")) {
            className = "java.lang.Integer";
        } else if (primitiveSignature.equals("Z")) {
            className = "java.lang.Boolean";
        } else if (primitiveSignature.equals("D")) {
            className = "java.lang.Double";
        } else if (primitiveSignature.equals("F")) {
            className = "java.lang.Float";
        } else if (primitiveSignature.equals("B")) {
            className = "java.lang.Byte";
        } else if (primitiveSignature.equals("C")) {
            className = "java.lang.Character";
        } else if (primitiveSignature.equals("S")) {
            className = "java.lang.Short";
        } else if (primitiveSignature.equals("J")) {
            className = "java.lang.Long";
        } else {
            className = "java.lang.Void";
        }
        type = (IJavaClassType) getType(className);
        return type;
    }

    
    // this is not working
    private IJavaObject newJDIClosure(Closure c) throws DebugException {
        IJavaClassType methodClosureClass = (IJavaClassType) getType("org.codehaus.groovy.runtime.MethodClosure"); //$NON-NLS-1$
        IJavaClassType closureClass = (IJavaClassType) getType("groovy.lang.Closure"); //$NON-NLS-1$
        IJavaObject jdiOwner = closureClass.getClassObject();
        IJavaValue jdiMethodName = toJDIObject("call"); //$NON-NLS-1$
        return methodClosureClass.newInstance("(Ljava/lang/Object;Ljava/lang/String;)V",  //$NON-NLS-1$
                new IJavaValue[] { jdiOwner, jdiMethodName}, thread);
    }

    private IJavaObject newJDIList(List<?> list) throws DebugException {
        IJavaObject jdiList = newInstance("java.util.ArrayList"); //$NON-NLS-1$
        for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            IJavaValue jdiObject = toJDIObject(o);
            jdiList.sendMessage("add", "(Ljava/lang/Object;)Z", new IJavaValue[] { jdiObject }, thread, false); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return jdiList;
    }

    private IJavaObject newJDIMap(Map<?, ?> map) throws DebugException {
        IJavaObject jdiMap = newInstance("java.util.HashMap"); //$NON-NLS-1$
        for (Iterator<?> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Entry<?, ?> e = (Entry<?, ?>) iterator.next();
            IJavaValue jdiKey = toJDIObject(e.getKey());
            IJavaValue jdiValue = toJDIObject(e.getValue());
            jdiMap.sendMessage("put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", new IJavaValue[] { jdiKey, jdiValue }, thread, false); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return jdiMap;
    }
    
    /**
     * @param o
     * @throws DebugException
     */
    private IJavaObject throwInvalidObjectException(Object o) throws DebugException {
        throw new DebugException(
        new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
                DebugException.REQUEST_FAILED, "(Groovy) Invalid object " + o, null)  //$NON-NLS-1$
        );
    }

    public IJavaThread getThread() {
        return thread;
    }
    
    public IJavaDebugTarget getTarget() {
        return target;
    }

    /**
     * Converts a meta class over here to a meta class over there
     * @param metaClass
     * @return
     * @throws DebugException 
     */
    public IJavaObject createMetaClassInDebuggedApplication(MetaClass metaClass) throws DebugException {
        IJavaClassType groovySystemType = (IJavaClassType) getType("groovy.lang.GroovySystem"); //$NON-NLS-1$
        IJavaObject registry = (IJavaObject) groovySystemType.getField("META_CLASS_REGISTRY").getValue(); //$NON-NLS-1$

        Class<?> theClass = metaClass.getTheClass();
        IJavaType type;
        if (theClass.isPrimitive()) {
            type = getBoxedClassTypeForPrimitive(Signature.createTypeSignature(theClass.getName(), true));
        } else {
            type = getType(theClass.getName());
        }
        return (IJavaObject) registry.sendMessage("getMetaClass", "(Ljava/lang/Class;)Lgroovy/lang/MetaClass;", new IJavaValue[] { ((IJavaReferenceType) type).getClassObject() }, thread, false); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * When the asBoolean method is called, it is likely an implicit conversion to
     * Boolean as part of an if statement, etc.
     * @param result
     * @return
     * @throws DebugException
     */
    public static Boolean convertToBoolean(IJavaValue result) throws DebugException {
        try {
            if (result instanceof IJavaPrimitiveValue) {
                return ((IJavaPrimitiveValue) result).getBooleanValue();
            } else {
                return Boolean.valueOf(((JDIObjectValue) result).getField("value", false).getValue().getValueString());
            }
        } catch (Exception e) {
            if (!(e instanceof DebugException)) {
                throw new DebugException(new Status(IStatus.ERROR, GroovyDebugCoreActivator.PLUGIN_ID, "(Groovy) Should have had a Boolean result", e));
            } else {
                throw (DebugException) e;
            }
        }
    }
    
    public Integer convertToInteger(IJavaValue result) throws DebugException {
        try {
            if (result instanceof IJavaPrimitiveValue) {
                return ((IJavaPrimitiveValue) result).getIntValue();
            } else {
                return Integer.valueOf(((JDIObjectValue) result).getField("value", false).getValue().getValueString());
            }
        } catch (Exception e) {
            if (!(e instanceof DebugException)) {
                throw new DebugException(new Status(IStatus.ERROR, GroovyDebugCoreActivator.PLUGIN_ID, "(Groovy) Should have had a Boolean result", e));
            } else {
                throw (DebugException) e;
            }
        }
    }
    
    /**
     * Likely an implicit call to iterator as part of a for loop
     * @param result
     * @return
     */
    public Iterator<Proxy> convertToIterator(IJavaValue result, JDIMetaClass metaClass) throws DebugException {
        return new JDIIterator((IJavaObject) result, this);
    }

}
