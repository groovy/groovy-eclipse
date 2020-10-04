/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

/**
 * This class is configured with a resolver and then builds ClassNodes for Eclipse TypeBindings. It follows the pattern in the
 * groovy <code>Java5</code> class. See the entry point <code>setAdditionalClassInformation()</code> in that class. By following the
 * code structure from Java5 as closely as we can, we will build ClassNodes that contain the unusual form of generics configuration
 * that the rest of groovy wants to see. (Note: Java5.setAdditionalClassInformation() is used for building ClassNode objects for JVM
 * reflective class objects).
 */
class JDTClassNodeBuilder {

    private final JDTResolver resolver;

    JDTClassNodeBuilder(JDTResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Based on Java5.configureType()
     */
    protected ClassNode configureType(TypeBinding type) {
        // GRECLIPSE-1639: Not all TypeBinding instances have been resolved when we get to this point.
        // See org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment.getTypeFromCompoundName(char[][],boolean,boolean)
        if (type instanceof UnresolvedReferenceBinding) {
            LookupEnvironment environment = resolver.getScope().environment;
            char[][] compoundName = ((UnresolvedReferenceBinding) type).compoundName;
            type = environment.getType(compoundName); // TODO: Use getType(char[][],ModuleBinding)?
            if (type == null || type instanceof ProblemReferenceBinding) {
                throw new IllegalStateException("Unable to resolve type: " + CharOperation.toString(compoundName));
            }
        }

        if (type instanceof BaseTypeBinding) {
            return configureBaseType((BaseTypeBinding) type);
        } else if (type instanceof BinaryTypeBinding) {
            return configureBinaryType((BinaryTypeBinding) type);
        } else if (type instanceof SourceTypeBinding) {
            return configureSourceType((SourceTypeBinding) type);
        } else if (type instanceof ArrayBinding) {
            return configureGenericArray((ArrayBinding) type);
        } else if (type instanceof WildcardBinding) {
            return configureWildcardType((WildcardBinding) type);
        } else if (type instanceof TypeVariableBinding) {
            return configureTypeVariableReference((TypeVariableBinding) type);
        } else if (type instanceof ParameterizedTypeBinding) {
            return configureParameterizedType((ParameterizedTypeBinding) type);
        }
        throw new IllegalStateException("'type' was null or an unhandled type: " + (type == null ? "null" : type.getClass().getName()));
    }

    /**
     * Based on Java5.configureTypes()
     */
    protected ClassNode[] configureTypes(TypeBinding[] bindings) {
        int n;
        if (bindings == null || (n = bindings.length) == 0) {
            return null;
        }
        ClassNode[] nodes = new ClassNode[n];
        for (int i = 0; i < n; i += 1) {
            nodes[i] = configureType(bindings[i]);
        }
        return nodes;
    }

    /**
     * Based on Java5.configureTypeArguments()
     */
    protected GenericsType[] configureTypeArguments(TypeBinding[] bindings) {
        int n;
        if (bindings == null || (n = bindings.length) == 0) {
            return null;
        }
        GenericsType[] gts = new GenericsType[n];
        for (int i = 0; i < n; i += 1) {
            ClassNode t = configureType(bindings[i]);
            if (bindings[i] instanceof WildcardBinding) {
                gts[i] = t.getGenericsTypes()[0];
            } else {
                gts[i] = new GenericsType(t);
            }
        }
        return gts;
    }

    /**
     * Based on Java5.configureTypeVariable()
     */
    protected GenericsType[] configureTypeVariables(TypeVariableBinding[] bindings) {
        int n;
        if (bindings == null || (n = bindings.length) == 0) {
            return null;
        }
        GenericsType[] gts = new GenericsType[n];
        for (int i = 0; i < n; i += 1) {
            gts[i] = configureTypeVariableDefinition(bindings[i]);
        }
        return gts;
    }

    /**
     * Loosely based on Java5.configureGenericArray()
     */
    private ClassNode configureGenericArray(ArrayBinding genericArrayType) {
        TypeBinding component = genericArrayType.leafComponentType;
        ClassNode node = resolver.convertToClassNode(component);
        ClassNode result = node;
        for (int n = genericArrayType.dimensions; n > 0; n -= 1) {
            result = result.makeArray();
        }
        return result;
    }

    private Map<TypeVariableBinding, ClassNode> typeVariableConfigurationInProgress = new HashMap<>();

    /**
     * Based on Java5.configureTypeVariableReference()
     */
    private ClassNode configureTypeVariableReference(TypeVariableBinding tv) {
        ClassNode node = typeVariableConfigurationInProgress.get(tv);
        if (node != null) {
            return node;
        }
        String name = String.valueOf(tv.sourceName);
        if (name.indexOf('@') >= 0) {
            throw new IllegalStateException("Invalid type variable name: " + name);
        }

        ClassNode cn = ClassHelper.makeWithoutCaching(name);
        cn.setGenericsPlaceHolder(true);
        ClassNode cn2 = ClassHelper.makeWithoutCaching(name);
        cn2.setGenericsPlaceHolder(true);
        cn.setGenericsTypes(new GenericsType[] {new GenericsType(cn2)});

        typeVariableConfigurationInProgress.put(tv, cn);
        if (tv.firstBound != null && tv.firstBound.id != TypeIds.T_JavaLangObject) {
            setRedirect(cn, configureType(tv.firstBound));
        } else {
            cn.setRedirect(ClassHelper.OBJECT_TYPE);
        }
        typeVariableConfigurationInProgress.remove(tv);

        return cn;
    }

    /**
     * Based on Java5.configureTypeVariableDefinition()
     */
    private GenericsType configureTypeVariableDefinition(TypeVariableBinding tv) {
        ClassNode cn = configureTypeVariableReference(tv);
        ClassNode redirect = removeRedirect(cn);
        TypeBinding[] tBounds = getBounds(tv);
        GenericsType gt;
        if (tBounds.length == 0) {
            gt = new GenericsType(cn);
        } else {
            ClassNode[] cBounds = configureTypes(tBounds);
            gt = new GenericsType(cn, cBounds, null);
            gt.setName(cn.getName());
            gt.setPlaceholder(true);
        }
        setRedirect(cn, redirect);
        return gt;
    }

    /**
     * Based on Java5.configureWildcardType()
     */
    private ClassNode configureWildcardType(WildcardBinding wildcard) {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        base.setRedirect(ClassHelper.OBJECT_TYPE);

        ClassNode[] uppers = configureTypes(getUpperBounds(wildcard));
        ClassNode[] lowers = configureTypes(getLowerBounds(wildcard));
        GenericsType t = new GenericsType(base, uppers,
            lowers != null && lowers.length > 0 ? lowers[0] : null);
        t.setWildcard(true);

        ClassNode ref = ClassHelper.makeWithoutCaching(Object.class, false);
        ref.setGenericsTypes(new GenericsType[] {t});
        return ref;
    }

    private ClassNode configureParameterizedType(ParameterizedTypeBinding tb) {
        if (tb instanceof RawTypeBinding) {
            JDTClassNode cn = new JDTClassNode(tb, resolver);
            setRedirect(cn, configureType(tb.genericType()));
            return cn;
        }
        TypeBinding rt = toRawType(tb);
        if (rt instanceof ParameterizedTypeBinding && !(rt instanceof RawTypeBinding)) {
            // the type was the inner type of a parameterized type
            return new JDTClassNode((ParameterizedTypeBinding) rt, resolver); // doesn't need generics initializing
        }
        ClassNode cn = configureType(rt);
        if (cn instanceof JDTClassNode) {
            ((JDTClassNode) cn).setJdtBinding(tb);
            // the messing about in here is for a few reasons. Contrast it with the ClassHelper.makeWithoutCaching
            // that code when called for Iterable will set the redirect to point to the generics. That is what
            // we are trying to achieve here.
            if (!(tb instanceof RawTypeBinding)) {
                setRedirect(cn, configureType(tb.genericType()));
            }
        }
        cn.setGenericsTypes(configureTypeArguments(tb.arguments));
        return cn;
    }

    private ClassNode configureBaseType(BaseTypeBinding tb) {
        switch (tb.id) {
        case TypeIds.T_boolean:
            return ClassHelper.boolean_TYPE;
        case TypeIds.T_byte:
            return ClassHelper.byte_TYPE;
        case TypeIds.T_char:
            return ClassHelper.char_TYPE;
        case TypeIds.T_double:
            return ClassHelper.double_TYPE;
        case TypeIds.T_float:
            return ClassHelper.float_TYPE;
        case TypeIds.T_int:
            return ClassHelper.int_TYPE;
        case TypeIds.T_long:
            return ClassHelper.long_TYPE;
        case TypeIds.T_short:
            return ClassHelper.short_TYPE;
        case TypeIds.T_void:
            return ClassHelper.VOID_TYPE;
        case TypeIds.T_null:
            return ClassHelper.DYNAMIC_TYPE;
        default:
            throw new GroovyEclipseBug("Unexpected BaseTypeBinding: " + tb + "(type.id=" + tb.id + ")");
        }
    }

    private ClassNode configureBinaryType(BinaryTypeBinding tb) {
        switch (tb.id) {
        case TypeIds.T_JavaLangBoolean:
            return ClassHelper.Boolean_TYPE;
        case TypeIds.T_JavaLangByte:
            return ClassHelper.Byte_TYPE;
        case TypeIds.T_JavaLangCharacter:
            return ClassHelper.Character_TYPE;
        case TypeIds.T_JavaLangDouble:
            return ClassHelper.Double_TYPE;
        case TypeIds.T_JavaLangFloat:
            return ClassHelper.Float_TYPE;
        case TypeIds.T_JavaLangInteger:
            return ClassHelper.Integer_TYPE;
        case TypeIds.T_JavaLangLong:
            return ClassHelper.Long_TYPE;
        case TypeIds.T_JavaLangShort:
            return ClassHelper.Short_TYPE;
        case TypeIds.T_JavaLangVoid:
            return ClassHelper.void_WRAPPER_TYPE;

        case TypeIds.T_JavaLangObject:
            return ClassHelper.OBJECT_TYPE;
        case TypeIds.T_JavaLangString:
            return ClassHelper.STRING_TYPE;
        /* TODO:
        case TypeIds.T_JavaLangNumber:
            return ClassHelper.Number_TYPE;
        case TypeIds.T_JavaMathBigDecimal:
            return ClassHelper.BigDecimal_TYPE;
        case TypeIds.T_JavaMathBigInteger:
            return ClassHelper.BigInteger_TYPE;
        case TypeIds.T_JavaIoSerializable:
            return ClassHelper.SERIALIZABLE_TYPE;
        */

        default:
            return new JDTClassNode(tb, resolver);
        }
    }

    private ClassNode configureSourceType(SourceTypeBinding tb) {
        return new JDTClassNode(tb, resolver);
    }

    //

    private static TypeBinding[] getLowerBounds(WildcardBinding wildcard) {
        if (wildcard.boundKind == Wildcard.SUPER) {
            return new TypeBinding[] {wildcard.bound};
        }
        return Binding.NO_TYPES;
    }

    private static TypeBinding[] getUpperBounds(WildcardBinding wildcard) {
        if (wildcard.boundKind == Wildcard.EXTENDS) {
            int nBounds = (wildcard.otherBounds == null ? 1 : 1 + wildcard.otherBounds.length);
            TypeBinding[] bounds = new TypeBinding[nBounds];
            bounds[0] = wildcard.bound;
            nBounds -= 1;
            if (nBounds > 0) {
                System.arraycopy(wildcard.otherBounds, 0, bounds, 1, nBounds);
            }
            return bounds;
        }
        return Binding.NO_TYPES;
    }

    private static TypeBinding[] getBounds(TypeVariableBinding tv) {
        if (tv.firstBound == null) {
            return new TypeBinding[] {tv.erasure()};
        } else {
            TypeBinding[] others = tv.otherUpperBounds();
            TypeBinding[] bounds = new TypeBinding[1 + others.length];
            System.arraycopy(others, 0, bounds, 1, others.length);
            bounds[0] = tv.firstBound;
            return bounds;
        }
    }

    private static TypeBinding toRawType(TypeBinding tb) {
        if (tb instanceof RawTypeBinding ||
            tb instanceof BaseTypeBinding ||
          //tb instanceof WildcardBinding ||
          //tb instanceof TypeVariableBinding ||
            tb instanceof ProblemReferenceBinding) {
            return tb;
        } else if (tb instanceof ParameterizedTypeBinding) {
            LookupEnvironment environment = ((ParameterizedTypeBinding) tb).environment();
            return environment.convertToRawType(((ParameterizedTypeBinding) tb).genericType(), false);
        } else if (tb instanceof ArrayBinding) {
            LookupEnvironment environment = ((ArrayBinding) tb).environment();
            return environment.convertToRawType(tb, false); // handles generics and dimensions
        } else if (tb instanceof BinaryTypeBinding ||
                   tb instanceof SourceTypeBinding) {
            if (tb.isGenericType()) {
                Class<?> cl = tb instanceof BinaryTypeBinding ? BinaryTypeBinding.class : SourceTypeBinding.class;
                LookupEnvironment environment = ReflectionUtils.getPrivateField(cl, "environment", tb);
                return environment.convertToRawType(tb, false);
            } else {
                return tb;
            }
        }
        throw new IllegalStateException("nyi " + tb.getClass());
    }

    //--------------------------------------------------------------------------

    static ClassNode removeRedirect(ClassNode node) {
        ClassNode redirect = ReflectionUtils.getPrivateField(ClassNode.class, "redirect", node);
        node.setRedirect(null);
        return redirect;
    }

    static void setRedirect(ClassNode node, ClassNode redirect) {
        if (node.isPrimaryClassNode()) throw new GroovyEclipseBug(
            "Tried to set a redirect for a primary ClassNode (" + node.getName() + "->" + redirect.getName() + ")");
        if (node != redirect)
            ReflectionUtils.setPrivateField(ClassNode.class, "redirect", node, redirect);
    }
}
