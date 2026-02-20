/*
 * Copyright 2009-2026 the original author or authors.
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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.Script;
import groovy.util.Proxy;

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
import org.codehaus.groovy.eclipse.GroovyPlugin;
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
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * Some useful operations to perform on the {@link IJavaDebugTarget}.
 *
 * @author Andrew Eisenberg
 */
public class JDITargetDelegate {

    private final IJavaDebugTarget target;
    private final IJavaThread thread;

    private String syntheticScriptName;
    /** tracks values with disabled garbage collection */
    private List<IJavaObject> permStorage = new ArrayList<>();
    private final Set<MetaClass> originalMetaClasses = new HashSet<>();

    public JDITargetDelegate(IJavaDebugTarget target, IJavaThread thread) {
        this.target = target;
        this.thread = thread;
    }

    public IJavaDebugTarget getTarget() {
        return target;
    }

    public IJavaThread getThread() {
        return thread;
    }

    //--------------------------------------------------------------------------

    public void initialize(JDIScriptLoader loader) throws DebugException {
        ClassNode scriptClass = loader.getTheClass();
        this.syntheticScriptName = scriptClass.getName();

        // need to determine which classes are statically referenced, so visit types
        Set<Class<?>> allClasses = new ClassStoreVisitor(loader).doVisit(scriptClass);

        // force MetaClass for each of the types that can be referenced via literals
        for (Class<?> clazz : allClasses) {
            createMetaClassFor(loader, clazz);
        }
    }

    private void createMetaClassFor(ClassLoader loader, Class<?> clazz) throws DebugException {
        MetaClass meta = GroovySystem.getMetaClassRegistry().getMetaClass(clazz);
        JDIMetaClass jidMetaClass = new JDIMetaClass(meta, this);
        GroovySystem.getMetaClassRegistry().setMetaClass(clazz, jidMetaClass);
        originalMetaClasses.add(meta);
        if (clazz.isPrimitive()) {
            // also create MetaClass for wrapper type
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
        if (!originalMetaClasses.isEmpty()) {
            for (MetaClass mc : originalMetaClasses) {
                GroovySystem.getMetaClassRegistry().removeMetaClass(mc.getTheClass());
            }
            originalMetaClasses.clear();
        }

        // re-enable collection
        if (permStorage != null) {
            for (IJavaObject object : permStorage) {
                try {
                    object.enableCollection();
                } catch (DebugException e) {
                    // don't worry about GC if the VM has terminated
                    if ((e.getStatus().getException().getClass().getSimpleName().equals("VMDisconnectedException"))) {
                        break;
                    }
                }
            }
            permStorage = null;
        }
    }

    public void disableCollection(IJavaObject object) {
        if (permStorage != null) {
            try {
                object.disableCollection();
                permStorage.add(object);
            } catch (DebugException e) {
                // don't worry about ...
            }
        }
    }

    public Proxy createProxyFor(IJavaValue value) throws DebugException {
        if (value.isNull()) return null;
        Proxy jdiProxy = new JDIProxy();
        if (value instanceof IJavaPrimitiveValue) {
            value = toJDIObject(value); // box value
        } else if (value.getJavaType().getName().equals("groovy.lang.Reference")) {
            value = ((IJavaObject) value).sendMessage("get", "()Ljava/lang/Object;", new IJavaValue[0], getThread(), false);
        }
        jdiProxy.setMetaClass(new JDIMetaClass(null, getMetaClass(value), this));
        jdiProxy.setAdaptee(value);
        return jdiProxy;
    }

    public IJavaObject getMetaClass(IJavaValue object) throws DebugException {
        if (object.isNull()) {
            object = ((IJavaClassType) getType("org.codehaus.groovy.runtime.NullObject")).getClassObject();
        } else if (object instanceof IJavaObject) {
            IJavaFieldVariable metaClassField = ((IJavaObject) object).getField("metaClass", false);
            if (metaClassField != null && metaClassField.getSignature().equals("Lgroovy/lang/MetaClass;")) {
                return (IJavaObject) metaClassField.getValue();
            }
        }
        // probably does not have groovy object support -- use InvokerHelper
        IJavaClassType invoker = (IJavaClassType) getType("org.codehaus.groovy.runtime.InvokerHelper");
        return (IJavaObject) invoker.sendMessage("getMetaClass", "(Ljava/lang/Object;)Lgroovy/lang/MetaClass;", new IJavaValue[] {object}, getThread());
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
        return classClass; // array, class, interface or (unlikely) void
    }

    private IJavaClassType getWrapperType(String className) throws DebugException {
        return (IJavaClassType) getType(getWrapperTypeName(className));
    }

    private static String  getWrapperTypeName(String className) {
        switch (className) {
        case "boolean":
            return "java.lang.Boolean";
        case "byte":
            return "java.lang.Byte";
        case "char":
            return "java.lang.Character";
        case "double":
            return "java.lang.Double";
        case "float":
            return "java.lang.Float";
        case "int":
            return "java.lang.Integer";
        case "long":
            return "java.lang.Long";
        case "short":
            return "java.lang.Short";
        case "void":
            return "java.lang.Void";
        }
        throw new IllegalArgumentException(className);
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
        IJavaObject object = (IJavaObject) classObject.sendMessage("newInstance", "()Ljava/lang/Object;", null, thread, false);
        return object;
    }

    protected IJavaArray newArrayInstance(String className, int size) throws DebugException {
        IJavaClassObject classObject = getClassObject(className);
        IJavaArrayType arrayType = (IJavaArrayType) classObject.getInstanceType();
        IJavaArray object = arrayType.newInstance(size);
        disableCollection(object);
        return object;
    }

    /**
     * Loads class using {@code Class.forName("name")}.
     */
    private IJavaClassObject getClassObject(String className) throws DebugException {
        IJavaClassType classClass = (IJavaClassType) getType("java.lang.Class");
        IJavaValue obj = classClass.sendMessage("forName", "(Ljava/lang/String;)Ljava/lang/Class;", new IJavaValue[] {target.newValue(className)}, thread);
        return (IJavaClassObject) obj;
    }

    /**
     * Recursively converts an object to its JDI equivalent.
     * <br> - {@link Proxy} objects return their adaptee
     * <br> - Array objects return a JDI array where each array element is converted to a JDI Object
     * <br> - Literals (eg- primitives, strings, lists, and maps) are also converted
     *
     * @throws DebugException if the object passed in is not a valid object that can be converted to JDI
     */
    public IJavaObject toJDIObject(Object o) throws DebugException {
        if (o == null) return (IJavaObject) target.nullValue();
        if (o instanceof Proxy) o = ((Proxy) o).getAdaptee();

        IJavaValue v = null;
        if (o instanceof IJavaValue) {
            v = (IJavaValue) o;
        } else if (o instanceof Integer) {
            v = target.newValue((Integer) o);
        } else if (o instanceof Boolean) {
            v = target.newValue((Boolean) o);
        } else if (o instanceof Double) {
            v = target.newValue((Double) o);
        } else if (o instanceof Float) {
            v = target.newValue((Float) o);
        } else if (o instanceof Long) {
            v = target.newValue((Long) o);
        } else if (o instanceof Short) {
            v = target.newValue((Short) o);
        } else if (o instanceof Byte) {
            v = target.newValue((Byte) o);
        } else if (o instanceof Character) {
            v = target.newValue((Character) o);
        }
        if (v != null) {
            if (v instanceof IJavaPrimitiveValue) {
                String signature = null;
                switch (v.getSignature()) {
                case Signature.SIG_INT    :
                    signature = "(I)Ljava/lang/Integer;";
                    break;
                case Signature.SIG_BOOLEAN:
                    signature = "(Z)Ljava/lang/Boolean;";
                    break;
                case Signature.SIG_DOUBLE :
                    signature = "(D)Ljava/lang/Double;";
                    break;
                case Signature.SIG_FLOAT  :
                    signature = "(F)Ljava/lang/Float;";
                    break;
                case Signature.SIG_LONG   :
                    signature = "(J)Ljava/lang/Long;";
                    break;
                case Signature.SIG_BYTE   :
                    signature = "(B)Ljava/lang/Byte;";
                    break;
                case Signature.SIG_SHORT  :
                    signature = "(S)Ljava/lang/Short;";
                    break;
                case Signature.SIG_CHAR   :
                    signature = "(C)Ljava/lang/Character;";
                    break;
                case Signature.SIG_VOID   :
                    return (IJavaObject) target.nullValue();
                }
                return (IJavaObject) getWrapperType(v.getJavaType().getName()).sendMessage("valueOf", signature, new IJavaValue[] {v}, thread);
            } else {
                return (IJavaObject) v;
            }
        }

        if (o instanceof String) {
            return (IJavaObject) target.newValue((String) o);
        }
        if (o instanceof Script) {
            // return the currently executing object
            return ((JDIBinding) ((Script) o).getBinding()).getThis();
        }
        if (o instanceof Class) {
            String name = ((Class<?>) o).getName();
            var type = getType(name);
            if (type instanceof IJavaReferenceType) {
                return ((IJavaReferenceType) type).getClassObject();
            }
            return (IJavaClassObject) getWrapperType(name).getField("TYPE").getValue();
        }
        if (o.getClass().isArray()) {
            int length = Array.getLength(o);
            boolean object = (o instanceof Object[]);
            IJavaArray array = newArrayInstance(o.getClass().getName(), length);
            for (int i = 0; i < length; i += 1) {
                array.setValue(i, object ? toJDIObject(((Object[]) o)[i]) : toJDIValue(Array.get(o, i)));
            }
            return array;
        }
        if (o instanceof List) {
            // create an ArrayList in the debugged application and add each of the elements
            return newJDIList((List<?>) o);
        }
        if (o instanceof Map) {
            // create a HashMap in the debugged application and put each of the entries in it
            return newJDIMap((Map<?, ?>) o);
        }
        if (o instanceof GString) {
            // create a GString in the debugged application
            return newJDIGString((GString) o);
        }
        if (o instanceof Closure) {
            // create a Closure in the debugged application
            return newJDIClosure((Closure<?>) o);
        }
        return throwInvalidObjectException(o);
    }

    public IJavaValue toJDIValue(Object o) throws DebugException {
        if (o == null) return target.nullValue();
        if (o instanceof Proxy) o = ((Proxy) o).getAdaptee();

        if (o instanceof Integer) {
            return target.newValue(((Integer) o).intValue());
        }
        if (o instanceof Boolean) {
            return target.newValue(((Boolean) o).booleanValue());
        }
        if (o instanceof Byte) {
            return target.newValue(((Byte) o).byteValue());
        }
        if (o instanceof Character) {
            return target.newValue(((Character) o).charValue());
        }
        if (o instanceof Double) {
            return target.newValue(((Double) o).doubleValue());
        }
        if (o instanceof Float) {
            return target.newValue(((Float) o).floatValue());
        }
        if (o instanceof Long) {
            return target.newValue(((Long) o).longValue());
        }
        if (o instanceof Short) {
            return target.newValue(((Short) o).shortValue());
        }
        if (o instanceof String) {
            return target.newValue((String) o);
        }

        if (o instanceof IJavaValue) {
            IJavaValue v = (IJavaValue) o;
            switch (v.getSignature()) {
            case Signature.SIG_INT    :
            case Signature.SIG_BOOLEAN:
            case Signature.SIG_BYTE   :
            case Signature.SIG_CHAR   :
            case Signature.SIG_DOUBLE :
            case Signature.SIG_FLOAT  :
            case Signature.SIG_LONG   :
            case Signature.SIG_SHORT  :
            case Signature.SIG_VOID   :
                break;
            case "Ljava/lang/Integer;":
                return ((IJavaObject) v).sendMessage("intValue",     "()I", new IJavaValue[0], thread, false);
            case "Ljava/lang/Boolean;":
                return ((IJavaObject) v).sendMessage("booleanValue", "()Z", new IJavaValue[0], thread, false);
            case "Ljava/lang/Byte;"   :
                return ((IJavaObject) v).sendMessage("byteValue",    "()B", new IJavaValue[0], thread, false);
            case "Ljava/lang/Character;":
                return ((IJavaObject) v).sendMessage("charValue",    "()C", new IJavaValue[0], thread, false);
            case "Ljava/lang/Double;" :
                return ((IJavaObject) v).sendMessage("doubleValue",  "()D", new IJavaValue[0], thread, false);
            case "Ljava/lang/Float;"  :
                return ((IJavaObject) v).sendMessage("floatValue",   "()F", new IJavaValue[0], thread, false);
            case "Ljava/lang/Long;"   :
                return ((IJavaObject) v).sendMessage("longValue",    "()J", new IJavaValue[0], thread, false);
            case "Ljava/lang/Short;"  :
                return ((IJavaObject) v).sendMessage("shortValue",   "()S", new IJavaValue[0], thread, false);
            }
            return v;
        }

        return toJDIObject(o);
    }

    /**
     * Creates a GString in the debugged application.
     */
    private IJavaObject newJDIGString(GString gstring) throws DebugException {
        String[] strings = gstring.getStrings();
        Object[] values = gstring.getValues();
        IJavaClassObject classObject = getClassObject("org.codehaus.groovy.runtime.GStringImpl");
        // returns the 2-arg constructor
        IJavaArray constructors = (IJavaArray) classObject.sendMessage("getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;", null, thread, false);
        IJavaObject constructor = (IJavaObject) constructors.getValue(0);
        IJavaArray args = newArrayInstance("[Ljava.lang.Object;", 2);  // note that here  we need to use '.' instead of '/' to create the array
        args.setValue(0, toJDIObject(values));
        args.setValue(1, toJDIObject(strings));

        return (IJavaObject) constructor.sendMessage("newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", new IJavaValue[] {args}, thread, false);
    }

    // this is not working
    private IJavaObject newJDIClosure(Closure<?> c) throws DebugException {
        IJavaClassType methodClosureClass = (IJavaClassType) getType("org.codehaus.groovy.runtime.MethodClosure");
        IJavaClassType closureClass = (IJavaClassType) getType("groovy.lang.Closure");
        IJavaObject jdiOwner = closureClass.getClassObject();
        IJavaValue jdiMethodName = toJDIObject("call");
        return methodClosureClass.newInstance("(Ljava/lang/Object;Ljava/lang/String;)V", new IJavaValue[] {jdiOwner, jdiMethodName}, thread);
    }

    private IJavaObject newJDIList(List<?> list) throws DebugException {
        IJavaObject jdiList = newInstance("java.util.ArrayList");
        for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();

            jdiList.sendMessage("add", "(Ljava/lang/Object;)Z", new IJavaValue[] {toJDIObject(obj)}, thread, false);
        }
        return jdiList;
    }

    private IJavaObject newJDIMap(Map<?, ?> map) throws DebugException {
        IJavaObject jdiMap = newInstance("java.util.LinkedHashMap");
        for (Iterator<?> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iterator.next();

            jdiMap.sendMessage("put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                new IJavaValue[] {toJDIObject(entry.getKey()), toJDIObject(entry.getValue())}, thread, false);
        }
        return jdiMap;
    }

    private IJavaObject throwInvalidObjectException(Object o) throws DebugException {
        throw new DebugException(new Status(Status.ERROR, GroovyPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "(Groovy) Invalid object " + o, null));
    }

    /**
     * Converts a meta class over here to a meta class over there.
     */
    public IJavaObject createMetaClassInDebuggedApplication(MetaClass metaClass) throws DebugException {
        Class<?> clazz = metaClass.getTheClass();
        IJavaClassObject jdiClass = (clazz.isPrimitive() ? getWrapperType(clazz.getName()) : (IJavaReferenceType) getType(clazz.getName())).getClassObject();

        IJavaClassType groovySystem = (IJavaClassType) getType("groovy.lang.GroovySystem");
        IJavaObject    metaRegistry = (IJavaObject) groovySystem.getField("META_CLASS_REGISTRY").getValue();
        return (IJavaObject) metaRegistry.sendMessage("getMetaClass", "(Ljava/lang/Class;)Lgroovy/lang/MetaClass;", new IJavaValue[] {jdiClass}, thread, false);
    }

    /**
     * When the asBoolean method is called, it is likely an implicit conversion to
     * Boolean as part of an if statement, etc.
     */
    public Boolean convertToBoolean(IJavaValue result) throws DebugException {
        try {
            if (result instanceof IJavaPrimitiveValue) {
                return ((IJavaPrimitiveValue) result).getBooleanValue();
            } else {
                return Boolean.valueOf(((IJavaObject) result).getField("value", false).getValue().getValueString());
            }
        } catch (Exception e) {
            if (!(e instanceof DebugException)) {
                throw new DebugException(new Status(Status.ERROR, GroovyPlugin.PLUGIN_ID, "(Groovy) Should have had a Boolean result", e));
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
                return Integer.valueOf(((IJavaObject) result).getField("value", false).getValue().getValueString());
            }
        } catch (Exception e) {
            if (!(e instanceof DebugException)) {
                throw new DebugException(new Status(Status.ERROR, GroovyPlugin.PLUGIN_ID, "(Groovy) Should have had a Boolean result", e));
            } else {
                throw (DebugException) e;
            }
        }
    }

    /**
     * Likely an implicit call to iterator as part of a for loop
     */
    public Iterator<Proxy> convertToIterator(IJavaValue result) throws DebugException {
        return new JDIIterator((IJavaObject) result, this);
    }

    //--------------------------------------------------------------------------

    private static final class JDIProxy extends Proxy {

        @Override
        public void setAdaptee(Object object) {
            super.setAdaptee(requireNonNull(object));
        }

        @Override
        public String toString() {
            return getAdaptee().toString();
        }
    }

    /**
     * Visits the script used for evaluation and looks for all class references
     * these references will then have their meta classes changed.
     */
    class ClassStoreVisitor extends ClassCodeVisitorSupport {

        Set<Class<?>> referencedClasses;
        private final ClassLoader loader;

        ClassStoreVisitor(ClassLoader loader) {
            this.loader = loader;
        }

        Set<Class<?>> doVisit(ClassNode node) {
            referencedClasses = new HashSet<>();
            node.visitContents(this);
            return referencedClasses;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        private void storeClass(ClassNode classNode) {
            try {
                Class<?> typeClass = classNode.getTypeClass();
                if (typeClass != null) {
                    referencedClasses.add(typeClass);
                }
            } catch (GroovyBugError | GroovyRuntimeException e) {
                // could be that this is a class reference of a class defined in the script
                if (classNode.getModule().getDescription().equals("____Eval.groovy") && !classNode.getName().equals(syntheticScriptName)) {
                    // try to load it, but don't try to load the generated script class
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

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
            storeClass(expression.getType());
            super.visitStaticMethodCallExpression(expression);
        }

        @Override
        public void visitClassExpression(ClassExpression expression) {
            storeClass(expression.getType());
            super.visitClassExpression(expression);
        }

        @Override
        public void visitConstantExpression(ConstantExpression expression) {
            storeClass(expression.getType());
            super.visitConstantExpression(expression);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression expression) {
            storeClass(expression.getType());
            super.visitConstructorCallExpression(expression);
        }

        @Override
        public void visitListExpression(ListExpression expression) {
            storeClass(expression.getType());
            super.visitListExpression(expression);
        }

        @Override
        public void visitMapExpression(MapExpression expression) {
            storeClass(expression.getType());
            super.visitMapExpression(expression);
        }

        @Override
        public void visitMapEntryExpression(MapEntryExpression expression) {
            storeClass(expression.getType());
            super.visitMapEntryExpression(expression);
        }
    }
}
