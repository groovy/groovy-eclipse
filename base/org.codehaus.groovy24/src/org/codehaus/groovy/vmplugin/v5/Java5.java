/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.vmplugin.v5;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.vmplugin.VMPlugin;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

/**
 * java 5 based functions
 *
 * @author Jochen Theodorou
 */
public class Java5 implements VMPlugin {
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Class[] PLUGIN_DGM = {PluginDefaultGroovyMethods.class};

    public void setAdditionalClassInformation(ClassNode cn) {
        setGenericsTypes(cn);
    }

    private void setGenericsTypes(ClassNode cn) {
        TypeVariable[] tvs = cn.getTypeClass().getTypeParameters();
        GenericsType[] gts = configureTypeVariable(tvs);
        cn.setGenericsTypes(gts);
    }

    private GenericsType[] configureTypeVariable(TypeVariable[] tvs) {
        if (tvs.length == 0) return null;
        GenericsType[] gts = new GenericsType[tvs.length];
        for (int i = 0; i < tvs.length; i++) {
            gts[i] = configureTypeVariableDefinition(tvs[i]);
        }
        return gts;
    }

    private GenericsType configureTypeVariableDefinition(TypeVariable tv) {
        ClassNode base = configureTypeVariableReference(tv);
        ClassNode redirect = base.redirect();
        base.setRedirect(null);
        Type[] tBounds = tv.getBounds();
        GenericsType gt;
        if (tBounds.length == 0) {
            gt = new GenericsType(base);
        } else {
            ClassNode[] cBounds = configureTypes(tBounds);
            gt = new GenericsType(base, cBounds, null);
            gt.setName(base.getName());
            gt.setPlaceholder(true);
        }
        base.setRedirect(redirect);
        return gt;
    }

    private ClassNode[] configureTypes(Type[] types) {
        if (types.length == 0) return null;
        ClassNode[] nodes = new ClassNode[types.length];
        for (int i = 0; i < types.length; i++) {
            nodes[i] = configureType(types[i]);
        }
        return nodes;
    }

    private ClassNode configureType(Type type) {
        if (type instanceof WildcardType) {
            return configureWildcardType((WildcardType) type);
        } else if (type instanceof ParameterizedType) {
            return configureParameterizedType((ParameterizedType) type);
        } else if (type instanceof GenericArrayType) {
            return configureGenericArray((GenericArrayType) type);
        } else if (type instanceof TypeVariable) {
            return configureTypeVariableReference((TypeVariable) type);
        } else if (type instanceof Class) {
            return configureClass((Class) type);
        } else if (type==null) {
            throw new GroovyBugError("Type is null. Most probably you let a transform reuse existing ClassNodes with generics information, that is now used in a wrong context.");
        } else {
            throw new GroovyBugError("unknown type: " + type + " := " + type.getClass());
        }
    }

    private static ClassNode configureClass(Class c) {
        if (c.isPrimitive()) {
            return ClassHelper.make(c);
        } else {
            return ClassHelper.makeWithoutCaching(c, false);
        }
    }

    private ClassNode configureGenericArray(GenericArrayType genericArrayType) {
        Type component = genericArrayType.getGenericComponentType();
        ClassNode node = configureType(component);
        return node.makeArray();
    }

    private ClassNode configureWildcardType(WildcardType wildcardType) {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        base.setRedirect(ClassHelper.OBJECT_TYPE);

        ClassNode[] lowers = configureTypes(wildcardType.getLowerBounds());
        /* GRECLIPSE edit
        ClassNode lower = null;
        if (lowers != null) lower = lowers[0];

        ClassNode[] upper = configureTypes(wildcardType.getUpperBounds());
        GenericsType t = new GenericsType(base, upper, lower);
        */
        ClassNode[] uppers = configureTypes(wildcardType.getUpperBounds());
        // beware of [Object] upper bounds; often it's <?> or <? super T>
        if (lowers != null || wildcardType.getTypeName().equals("?")) {
            uppers = null;
        }

        GenericsType t = new GenericsType(base, uppers, lowers != null ? lowers[0] : null);
        // GRECLIPSE end
        t.setWildcard(true);

        ClassNode ref = ClassHelper.makeWithoutCaching(Object.class, false);
        ref.setGenericsTypes(new GenericsType[]{t});
        return ref;
    }

    private ClassNode configureParameterizedType(ParameterizedType parameterizedType) {
        ClassNode base = configureType(parameterizedType.getRawType());
        GenericsType[] gts = configureTypeArguments(parameterizedType.getActualTypeArguments());
        base.setGenericsTypes(gts);
        return base;
    }

    private ClassNode configureTypeVariableReference(TypeVariable tv) {
        ClassNode cn = ClassHelper.makeWithoutCaching(tv.getName());
        cn.setGenericsPlaceHolder(true);
        ClassNode cn2 = ClassHelper.makeWithoutCaching(tv.getName());
        cn2.setGenericsPlaceHolder(true);
        GenericsType[] gts = new GenericsType[]{new GenericsType(cn2)};
        cn.setGenericsTypes(gts);
        cn.setRedirect(ClassHelper.OBJECT_TYPE);
        return cn;
    }

    private GenericsType[] configureTypeArguments(Type[] ta) {
        if (ta.length == 0) return null;
        GenericsType[] gts = new GenericsType[ta.length];
        for (int i = 0; i < ta.length; i++) {
            ClassNode t = configureType(ta[i]);
            if (ta[i] instanceof WildcardType) {
                GenericsType[] gen = t.getGenericsTypes();
                gts[i] = gen[0];
            } else {
                gts[i] = new GenericsType(t);
            }
        }
        return gts;
    }

    public Class[] getPluginDefaultGroovyMethods() {
        return PLUGIN_DGM;
    }

    public Class[] getPluginStaticGroovyMethods() {
        return EMPTY_CLASS_ARRAY;
    }

    private void setAnnotationMetaData(Annotation[] annotations, AnnotatedNode an) {
        for (Annotation annotation : annotations) {
            AnnotationNode node = new AnnotationNode(ClassHelper.make(annotation.annotationType()));
            configureAnnotation(node, annotation);
            an.addAnnotation(node);
        }
    }

    private void configureAnnotationFromDefinition(AnnotationNode definition, AnnotationNode root) {
        ClassNode type = definition.getClassNode();
        if (!type.isResolved()) return;
        // GRECLIPSE add
        if (type.hasClass()) {
        // GRECLIPSE end
        Class clazz = type.getTypeClass();
        if (clazz == Retention.class) {
            Expression exp = definition.getMember("value");
            if (!(exp instanceof PropertyExpression)) return;
            PropertyExpression pe = (PropertyExpression) exp;
            String name = pe.getPropertyAsString();
            RetentionPolicy policy = RetentionPolicy.valueOf(name);
            setRetentionPolicy(policy, root);
        } else if (clazz == Target.class) {
            Expression exp = definition.getMember("value");
            if (!(exp instanceof ListExpression)) return;
            ListExpression le = (ListExpression) exp;
            int bitmap = 0;
            for (Expression e : le.getExpressions()) {
                if (!(e instanceof PropertyExpression)) return;
                PropertyExpression element = (PropertyExpression) e;
                String name = element.getPropertyAsString();
                ElementType value = ElementType.valueOf(name);
                bitmap |= getElementCode(value);
            }
            root.setAllowedTargets(bitmap);
        }
        // GRECLIPSE add
        } else {
            String typename = type.getName();
            if (typename.equals("java.lang.annotation.Retention")) {
                Expression exp = definition.getMember("value");
                if (!(exp instanceof PropertyExpression)) return;
                PropertyExpression pe = (PropertyExpression) exp;
                String name = pe.getPropertyAsString();
                RetentionPolicy policy = RetentionPolicy.valueOf(name);
                setRetentionPolicy(policy,root);
            } else if (typename.equals("java.lang.annotation.Target")) {
                Expression exp = definition.getMember("value");
                if (!(exp instanceof ListExpression)) return;
                ListExpression le = (ListExpression) exp;
                int bitmap = 0;
                for (Expression expression: le.getExpressions()) {
                    if (!(expression instanceof PropertyExpression)) return;
                    PropertyExpression element = (PropertyExpression)expression;
                    String name = element.getPropertyAsString();
                    ElementType value = ElementType.valueOf(name);
                    bitmap |= getElementCode(value);
                }
                root.setAllowedTargets(bitmap);
            }
        }
        // GRECLIPSE end
    }

    public void configureAnnotation(AnnotationNode node) {
        ClassNode type = node.getClassNode();
        List<AnnotationNode> annotations = type.getAnnotations();
        for (AnnotationNode an : annotations) {
            configureAnnotationFromDefinition(an, node);
        }
        configureAnnotationFromDefinition(node, node);
    }

    private void configureAnnotation(AnnotationNode node, Annotation annotation) {
        Class type = annotation.annotationType();
        if (type == Retention.class) {
            Retention r = (Retention) annotation;
            RetentionPolicy value = r.value();
            setRetentionPolicy(value, node);
            node.setMember("value", new PropertyExpression(
                    new ClassExpression(ClassHelper.makeWithoutCaching(RetentionPolicy.class, false)),
                    value.toString()));
        } else if (type == Target.class) {
            Target t = (Target) annotation;
            ElementType[] elements = t.value();
            ListExpression elementExprs = new ListExpression();
            for (ElementType element : elements) {
                elementExprs.addExpression(new PropertyExpression(
                        new ClassExpression(ClassHelper.ELEMENT_TYPE_TYPE), element.name()));
            }
            node.setMember("value", elementExprs);
        } else {
            Method[] declaredMethods;
            try {
                declaredMethods = type.getDeclaredMethods();
            } catch (SecurityException se) {
                declaredMethods = new Method[0];
            }
            for (Method declaredMethod : declaredMethods) {
                try {
                    Object value = declaredMethod.invoke(annotation);
                    Expression valueExpression = annotationValueToExpression(value);
                    if (valueExpression == null)
                        continue;
                    node.setMember(declaredMethod.getName(), valueExpression);
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            }
        }
    }

    private Expression annotationValueToExpression (Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Character || value instanceof Boolean)
            return new ConstantExpression(value);

        if (value instanceof Class)
            return new ClassExpression(ClassHelper.makeWithoutCaching((Class)value));

        if (value.getClass().isArray()) {
            ListExpression elementExprs = new ListExpression();
            int len = Array.getLength(value);
            for (int i = 0; i != len; ++i)
                elementExprs.addExpression(annotationValueToExpression(Array.get(value, i)));
            return elementExprs;
        }

        return null;
    }

    private void setRetentionPolicy(RetentionPolicy value, AnnotationNode node) {
        switch (value) {
            case RUNTIME:
                node.setRuntimeRetention(true);
                break;
            case SOURCE:
                node.setSourceRetention(true);
                break;
            case CLASS:
                node.setClassRetention(true);
                break;
            default:
                throw new GroovyBugError("unsupported Retention " + value);
        }
    }

    private int getElementCode(ElementType value) {
        switch (value) {
            case TYPE:
                return AnnotationNode.TYPE_TARGET;
            case CONSTRUCTOR:
                return AnnotationNode.CONSTRUCTOR_TARGET;
            case METHOD:
                return AnnotationNode.METHOD_TARGET;
            case FIELD:
                return AnnotationNode.FIELD_TARGET;
            case PARAMETER:
                return AnnotationNode.PARAMETER_TARGET;
            case LOCAL_VARIABLE:
                return AnnotationNode.LOCAL_VARIABLE_TARGET;
            case ANNOTATION_TYPE:
                return AnnotationNode.ANNOTATION_TARGET;
            case PACKAGE:
                return AnnotationNode.PACKAGE_TARGET;
            default:
                if ("TYPE_USE".equals(value.name()) || "TYPE_PARAMETER".equals(value.name()) || "MODULE".equals(value.name())) {
                    // return 0 value because we are binary or'ing this and want to ignore without failing
                    // later version plugins will override and provide sensible values for jdk8+
                    return 0;
                }
                throw new GroovyBugError("unsupported Target " + value);
        }
    }

    private static void setMethodDefaultValue(MethodNode mn, Method m) {
        Object defaultValue = m.getDefaultValue();
        ConstantExpression cExp = ConstantExpression.NULL;
        if (defaultValue!=null) cExp = new ConstantExpression(defaultValue);
        mn.setCode(new ReturnStatement(cExp));
        mn.setAnnotationDefault(true);
    }

    public void configureClassNode(CompileUnit compileUnit, ClassNode classNode) {
        try {
            Class clazz = classNode.getTypeClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                ClassNode ret = makeClassNode(compileUnit, f.getGenericType(), f.getType());
                FieldNode fn = new FieldNode(f.getName(), f.getModifiers(), ret, classNode, null);
                setAnnotationMetaData(f.getAnnotations(), fn);
                classNode.addField(fn);
            }
            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods) {
                ClassNode ret = makeClassNode(compileUnit, m.getGenericReturnType(), m.getReturnType());
                Parameter[] params = makeParameters(compileUnit, m.getGenericParameterTypes(), m.getParameterTypes(), m.getParameterAnnotations(), m);
                ClassNode[] exceptions = makeClassNodes(compileUnit, m.getGenericExceptionTypes(), m.getExceptionTypes());
                MethodNode mn = new MethodNode(m.getName(), m.getModifiers(), ret, params, exceptions, null);
                mn.setSynthetic(m.isSynthetic());
                setMethodDefaultValue(mn, m);
                setAnnotationMetaData(m.getAnnotations(), mn);
                mn.setGenericsTypes(configureTypeVariable(m.getTypeParameters()));
                classNode.addMethod(mn);
            }
            Constructor[] constructors = clazz.getDeclaredConstructors();
            for (Constructor ctor : constructors) {
                Parameter[] params = makeParameters(compileUnit, ctor.getGenericParameterTypes(), ctor.getParameterTypes(), getConstructorParameterAnnotations(ctor), ctor);
                ClassNode[] exceptions = makeClassNodes(compileUnit, ctor.getGenericExceptionTypes(), ctor.getExceptionTypes());
                // GRECLIPSE add
                setAnnotationMetaData(ctor.getAnnotations(),
                // GRECLIPSE end
                classNode.addConstructor(ctor.getModifiers(), params, exceptions, null));
            }

            Class sc = clazz.getSuperclass();
            if (sc != null) classNode.setUnresolvedSuperClass(makeClassNode(compileUnit, clazz.getGenericSuperclass(), sc));
            makeInterfaceTypes(compileUnit, classNode, clazz);
            setAnnotationMetaData(classNode.getTypeClass().getAnnotations(), classNode);

            PackageNode packageNode = classNode.getPackage();
            if (packageNode != null) {
                setAnnotationMetaData(classNode.getTypeClass().getPackage().getAnnotations(), packageNode);
            }
        } catch (NoClassDefFoundError e) {
            throw new NoClassDefFoundError("Unable to load class "+classNode.toString(false)+" due to missing dependency "+e.getMessage());
        } catch (MalformedParameterizedTypeException e) {
            throw new RuntimeException("Unable to configure class node for class "+classNode.toString(false)+" due to malformed parameterized types", e);
        }
    }

    /**
     * Synthetic parameters such as those added for inner class constructors may
     * not be included in the parameter annotations array. This is the case when
     * at least one parameter of an inner class constructor has an annotation with
     * a RUNTIME retention (this occurs for JDK8 and below). This method will
     * normalize the annotations array so that it contains the same number of
     * elements as the array returned from {@link Constructor#getParameterTypes()}.
     *
     * If adjustment is required, the adjusted array will be prepended with a
     * zero-length element. If no adjustment is required, the original array
     * from {@link Constructor#getParameterAnnotations()} will be returned.
     *
     * @param constructor the Constructor for which to return parameter annotations
     * @return array of arrays containing the annotations on the parameters of the given Constructor
     */
    private Annotation[][] getConstructorParameterAnnotations(Constructor<?> constructor) {
        /*
         * TODO: Remove after JDK9 is the minimum JDK supported
         *
         * JDK9+ correctly accounts for the synthetic parameter and when it becomes
         * the minimum version this method should no longer be required.
         */
        int parameterCount = constructor.getParameterTypes().length;
        Annotation[][] annotations = constructor.getParameterAnnotations();
        int diff = parameterCount - annotations.length;
        if (diff > 0) {
            // May happen on JDK8 and below. We add elements to the front of the array to account for the synthetic params:
            // - for an inner class we expect one param to account for the synthetic outer reference
            // - for an enum we expect two params to account for the synthetic name and ordinal
            if ((!constructor.getDeclaringClass().isEnum() && diff > 1) || diff > 2) {
                throw new GroovyBugError(
                        "Constructor parameter annotations length [" + annotations.length + "] " +
                        "does not match the parameter length: " + constructor
                );
            }
            Annotation[][] adjusted = new Annotation[parameterCount][];
            for (int i = 0; i < diff; i++) {
                adjusted[i] = new Annotation[0];
            }
            System.arraycopy(annotations, 0, adjusted, diff, annotations.length);
            return adjusted;
        }
        return annotations;
    }

    private void makeInterfaceTypes(CompileUnit cu, ClassNode classNode, Class clazz) {
        Type[] interfaceTypes = clazz.getGenericInterfaces();
        if (interfaceTypes.length == 0) {
            classNode.setInterfaces(ClassNode.EMPTY_ARRAY);
        } else {
            ClassNode[] ret = new ClassNode[interfaceTypes.length];
            for (int i = 0; i < interfaceTypes.length; i++) {
                Type type = interfaceTypes[i];
                while (!(type instanceof Class)) {
                    ParameterizedType pt = (ParameterizedType) type;
                    Type t2 = pt.getRawType();
                    if (t2==type) {
                        throw new GroovyBugError("Cannot transform generic signature of "+clazz+
                                " with generic interface "+interfaceTypes[i]+" to a class.");
                    }
                    type = t2;
                }
                ret[i] = makeClassNode(cu, interfaceTypes[i], (Class) type);
            }
            classNode.setInterfaces(ret);
        }
    }

    private ClassNode[] makeClassNodes(CompileUnit cu, Type[] types, Class[] cls) {
        ClassNode[] nodes = new ClassNode[types.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = makeClassNode(cu, types[i], cls[i]);
        }
        return nodes;
    }

    private ClassNode makeClassNode(CompileUnit cu, Type t, Class c) {
        ClassNode back = null;
        if (cu != null) back = cu.getClass(c.getName());
        if (back == null) back = ClassHelper.make(c);
        if (!(t instanceof Class)) {
            ClassNode front = configureType(t);
            front.setRedirect(back);
            return front;
        }
        return back.getPlainNodeReference();
    }

    /* GRECLIPSE edit
    private Parameter[] makeParameters(CompileUnit cu, Type[] types, Class[] cls, Annotation[][] parameterAnnotations) {
        Parameter[] params = Parameter.EMPTY_ARRAY;
        if (types.length > 0) {
            params = new Parameter[types.length];
            for (int i = 0; i < params.length; i++) {
                params[i] = makeParameter(cu, types[i], cls[i], parameterAnnotations[i], i);
            }
        }
        return params;
    }

    private Parameter makeParameter(CompileUnit cu, Type type, Class cl, Annotation[] annotations, int idx) {
        ClassNode cn = makeClassNode(cu, type, cl);
        Parameter parameter = new Parameter(cn, "param" + idx);
        setAnnotationMetaData(annotations, parameter);
        return parameter;
    }
    */
    private Parameter[] makeParameters(CompileUnit cu, Type[] types, Class[] cls, Annotation[][] parameterAnnotations, Member member) {
        Parameter[] params = Parameter.EMPTY_ARRAY;
        final int n;
        if ((n = types.length) > 0) {
            params = new Parameter[n];
            String[] names = new String[n];
            fillParameterNames(names, member);
            for (int i = 0; i < n; i += 1) {
                setAnnotationMetaData(parameterAnnotations[i],
                    params[i] = new Parameter(makeClassNode(cu, types[i], cls[i]), names[i]));
            }
        }
        return params;
    }

    protected void fillParameterNames(String[] names, Member member) {
        for (int i = 0, n = names.length; i < n; i += 1) {
            names[i] = (i < ARGS.length ? ARGS[i] : "arg" + i);
        }
    }

    // arbitrary choice of first ten; maintaining this array prevents many thousands of "argN" string/char[] instances
    public static final String[] ARGS = {"arg0", "arg1", "arg2", "arg3", "arg4", "arg5", "arg6", "arg7", "arg8", "arg9"};
    // GRECLIPSE end

    public void invalidateCallSites() {}

    @Override
    public Object getInvokeSpecialHandle(Method m, Object receiver){
        throw new GroovyBugError("getInvokeSpecialHandle requires at least JDK 7 wot private access to Lookup");
    }

    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public Object invokeHandle(Object handle, Object[] args) throws Throwable {
        throw new GroovyBugError("invokeHandle requires at least JDK 7");
    }
}

