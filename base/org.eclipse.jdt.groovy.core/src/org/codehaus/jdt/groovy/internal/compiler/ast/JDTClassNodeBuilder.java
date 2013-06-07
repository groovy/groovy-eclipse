/*******************************************************************************
 * Copyright (c) 2012 VMware, and other contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
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
 * 
 * @author Andy Clement
 */
class JDTClassNodeBuilder {

	private JDTResolver resolver;

	public static ClassNode build(JDTResolver resolver, TypeBinding typeBinding) {
		JDTClassNodeBuilder builder = new JDTClassNodeBuilder(resolver);
		return builder.configureType(typeBinding);
	}

	/**
	 * Based on Java5.configureType()
	 */
	public ClassNode configureType(TypeBinding type) {
		// GRECLIPSE-1639 not all TypeBinding have been resolved when we get to this point.
		// See comment on org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment.getTypeFromCompoundName(char[][], boolean,
		// boolean)
		if (type instanceof UnresolvedReferenceBinding) {
			type = resolver.getScope().environment.askForType(((UnresolvedReferenceBinding) type).compoundName);
		}

		if (type instanceof TypeVariableBinding) {
			return configureTypeVariableReference((TypeVariableBinding) type);
		} else if (type instanceof ParameterizedTypeBinding) {
			return configureParameterizedType((ParameterizedTypeBinding) type);
		} else if (type instanceof BinaryTypeBinding) {
			return configureClass((BinaryTypeBinding) type);
		} else if (type instanceof WildcardBinding) {
			return configureWildcardType((WildcardBinding) type);
		} else if (type instanceof ArrayBinding) {
			return configureGenericArray((ArrayBinding) type);
		} else if (type instanceof BaseTypeBinding) {
			return configureBaseTypeBinding((BaseTypeBinding) type);
		} else if (type instanceof SourceTypeBinding) {
			return configureSourceType((SourceTypeBinding) type);
		}
		throw new IllegalStateException("nyi " + type.getClass());
	}

	JDTClassNodeBuilder(JDTResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Based on Java5.configureTypeVariable()
	 */
	GenericsType[] configureTypeVariables(TypeVariableBinding[] tvs) {
		if (tvs.length == 0)
			return null;
		GenericsType[] gts = new GenericsType[tvs.length];
		for (int i = 0; i < tvs.length; i++) {
			gts[i] = configureTypeVariableDefinition(tvs[i]);
		}
		return gts;
	}

	/**
	 * Based on Java5.configureTypeArguments()
	 */
	GenericsType[] configureTypeArguments(TypeBinding[] ta) {
		if (ta == null) {
			return null;
		}

		if (ta.length == 0) {
			return null;
		}
		GenericsType[] gts = new GenericsType[ta.length];
		for (int i = 0; i < ta.length; i++) {
			ClassNode t = configureType(ta[i]);
			if (ta[i] instanceof WildcardBinding) {
				GenericsType[] gen = t.getGenericsTypes();
				gts[i] = gen[0];
			} else {
				gts[i] = new GenericsType(t);
			}
		}
		return gts;
	}

	// TODO still not 100% confident that the callers of this are doing the right thing or have the right expectations
	TypeBinding toRawType(TypeBinding tb) {
		if (tb instanceof RawTypeBinding) {
			return tb;
		} else if (tb instanceof ParameterizedTypeBinding) {
			ParameterizedTypeBinding ptb = (ParameterizedTypeBinding) tb;
			// resolver.getScope() can return null (if the resolver hasn't yet been used to resolve something) - using
			// the environment on the ptb seems safe. Other occurrences of getScope in this file could feasibly
			// be changed in the same way if NPEs become problems there too
			return ptb.environment().convertToRawType(ptb.genericType(), false);
			// return resolver.getScope().environment.convertToRawType(ptb.genericType(), false);
		} else if (tb instanceof TypeVariableBinding) {
			TypeBinding fb = ((TypeVariableBinding) tb).firstBound;
			if (fb == null) {
				return tb.erasure(); // Should be JLObject
				// return resolver.getScope().getJavaLangObject();
			}
			return fb;
		} else if (tb instanceof BinaryTypeBinding) {
			if (tb.isGenericType()) {
				try {
					Field f = BinaryTypeBinding.class.getDeclaredField("environment");
					f.setAccessible(true);
					LookupEnvironment le = (LookupEnvironment) f.get(tb);
					return le.convertToRawType(tb, false);
					// return resolver.getScope().environment.convertToRawType(tb, false);
				} catch (Exception e) {
					throw new RuntimeException("Problem building rawtype ", e);
				}
			} else {
				return tb;
			}
		} else if (tb instanceof ArrayBinding) {
			return tb;
		} else if (tb instanceof BaseTypeBinding) {
			return tb;
		} else if (tb instanceof SourceTypeBinding) {
			return tb;
		}
		throw new IllegalStateException("nyi " + tb.getClass());
	}

	/**
	 * Based on Java5.configureTypes()
	 */
	private ClassNode[] configureTypes(TypeBinding[] types) {
		if (types.length == 0) {
			return null;
		}
		ClassNode[] nodes = new ClassNode[types.length];
		for (int i = 0; i < types.length; i++) {
			nodes[i] = configureType(types[i]);
		}
		return nodes;
	}

	/**
	 * Return the groovy ClassNode for an Eclipse BaseTypeBinding.
	 */
	private ClassNode configureBaseTypeBinding(BaseTypeBinding type) {
		switch (type.id) {
			case TypeIds.T_boolean:
				return ClassHelper.boolean_TYPE;
			case TypeIds.T_char:
				return ClassHelper.char_TYPE;
			case TypeIds.T_byte:
				return ClassHelper.byte_TYPE;
			case TypeIds.T_short:
				return ClassHelper.short_TYPE;
			case TypeIds.T_int:
				return ClassHelper.int_TYPE;
			case TypeIds.T_long:
				return ClassHelper.long_TYPE;
			case TypeIds.T_double:
				return ClassHelper.double_TYPE;
			case TypeIds.T_float:
				return ClassHelper.float_TYPE;
			case TypeIds.T_void:
				return ClassHelper.VOID_TYPE;
			default:
				throw new GroovyEclipseBug("Unexpected BaseTypeBinding: " + type + "(type.id=" + type.id + ")");
		}
	}

	/**
	 * Loosely based on Java5.configureGenericArray()
	 */
	private ClassNode configureGenericArray(ArrayBinding genericArrayType) {
		TypeBinding component = genericArrayType.leafComponentType;
		ClassNode node = configureType(component);
		int dims = genericArrayType.dimensions;
		ClassNode result = node;
		for (int d = 0; d < dims; d++) {
			result = result.makeArray();
		}
		return result;
	}

	private Map<TypeVariableBinding, ClassNode> typeVariableConfigurationInProgress = new HashMap<TypeVariableBinding, ClassNode>();

	/**
	 * Based on Java5.configureTypeVariableReference()
	 */
	private ClassNode configureTypeVariableReference(TypeVariableBinding tv) {
		ClassNode nodeInProgress = typeVariableConfigurationInProgress.get(tv);
		if (nodeInProgress != null) {
			return nodeInProgress;
		}
		ClassNode cn = ClassHelper.makeWithoutCaching(tv.debugName());
		cn.setGenericsPlaceHolder(true);
		ClassNode cn2 = ClassHelper.makeWithoutCaching(tv.debugName());
		cn2.setGenericsPlaceHolder(true);
		GenericsType[] gts = new GenericsType[] { new GenericsType(cn2) };
		cn.setGenericsTypes(gts);
		cn.setRedirect(ClassHelper.OBJECT_TYPE);
		typeVariableConfigurationInProgress.put(tv, cn);
		// doing a bit of what is in Java5.makeClassNode() where it sorts out its front/back GR1563
		TypeBinding tb = tv.firstBound;
		if (tb != null && !tb.debugName().equals("java.lang.Object")) {
			ClassNode back = configureType(tb);
			cn.setRedirect(back);
		}
		typeVariableConfigurationInProgress.remove(tv);
		return cn;
	}

	/**
	 * Based on Java5.configureTypeVariableDefinition()
	 */
	private GenericsType configureTypeVariableDefinition(TypeVariableBinding tv) {
		ClassNode base = configureTypeVariableReference(tv);
		ClassNode redirect = base.redirect();
		base.setRedirect(null);
		TypeBinding[] tBounds = getBounds(tv); // tv.getBounds()
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

	private TypeBinding[] getBounds(TypeVariableBinding tv) {
		List<TypeBinding> bounds = new ArrayList<TypeBinding>();
		if (tv.firstBound == null) {
			return new TypeBinding[] { tv.erasure() }; // Should be JLObject
			// return new TypeBinding[] { resolver.getScope().getJavaLangObject() };
			// return null;
		}
		bounds.add(tv.firstBound);
		TypeBinding[] obs = tv.otherUpperBounds();
		for (int i = 0; i < obs.length; i++) {
			bounds.add(obs[i]);
		}
		return bounds.toArray(new TypeBinding[bounds.size()]);
	}

	/**
	 * Based on Java5.configureWildcardType()
	 */
	private ClassNode configureWildcardType(WildcardBinding wildcardType) {
		ClassNode base = ClassHelper.makeWithoutCaching("?");
		base.setRedirect(ClassHelper.OBJECT_TYPE);
		// TOO: more than one lower bound for wildcards?
		ClassNode[] lowers = configureTypes(getLowerbounds(wildcardType)); // wildcardType.getLowerBounds()
		ClassNode lower = null;
		// TOO: is it safe to remove this? What was the original intention?
		if (lowers != null) {
			lower = lowers[0];
		}
		ClassNode[] upper = configureTypes(getUpperbounds(wildcardType));// wildcardType.getUpperBounds()
		GenericsType t = new GenericsType(base, upper, lower);
		t.setWildcard(true);

		ClassNode ref = ClassHelper.makeWithoutCaching(Object.class, false);
		ref.setGenericsTypes(new GenericsType[] { t });

		return ref;
	}

	private TypeBinding[] getLowerbounds(WildcardBinding wildcardType) {
		if (wildcardType.boundKind == Wildcard.SUPER) {
			return new TypeBinding[] { wildcardType.bound };
		}
		return TypeBinding.NO_TYPES;
	}

	private TypeBinding[] getUpperbounds(WildcardBinding wildcardType) {
		if (wildcardType.boundKind == Wildcard.EXTENDS) {
			List<TypeBinding> bounds = new ArrayList<TypeBinding>();
			bounds.add(wildcardType.bound);
			if (wildcardType.otherBounds != null) {
				for (int b = 0; b < wildcardType.otherBounds.length; b++) {
					bounds.add(wildcardType.otherBounds[b]);
				}
			}
			return bounds.toArray(new TypeBinding[bounds.size()]);
		}
		return new TypeBinding[] { wildcardType.erasure() };
		// return new TypeBinding[] { resolver.getScope().getJavaLangObject() };
	}

	private ClassNode configureParameterizedType(ParameterizedTypeBinding parameterizedType) {
		if (parameterizedType instanceof RawTypeBinding) { // TODO correct?
			TypeBinding rt = toRawType(parameterizedType);
			if (!(rt instanceof RawTypeBinding)) {
				System.out.println("yikes");
			}
			return new JDTClassNode((RawTypeBinding) rt, resolver); // doesn't need generics initializing
			// return resolver.makeWithoutCaching(toRawType(parameterizedType));// configureType(toRawType(parameterizedType));
		}
		TypeBinding rt = toRawType(parameterizedType);
		if ((rt instanceof ParameterizedTypeBinding) && !(rt instanceof RawTypeBinding)) {
			// the type was the inner type of a parameterized type
			return new JDTClassNode((ParameterizedTypeBinding) rt, resolver); // doesn't need generics initializing

		}
		ClassNode base = configureType(rt);
		if (base instanceof JDTClassNode) {
			((JDTClassNode) base).setJdtBinding(parameterizedType);
			// the messing about in here is for a few reasons. Contrast it with the ClassHelper.makeWithoutCaching
			// that code when called for Iterable will set the redirect to point to the generics. That is what
			// we are trying to achieve here.
			if (/* (parameterizedType instanceof ParameterizedTypeBinding) && */
			!(parameterizedType instanceof RawTypeBinding)) {
				ClassNode cn = configureType(parameterizedType.genericType());
				// ClassNode cn = resolver.createClassNode(parameterizedType.genericType());
				((JDTClassNode) base).setRedirect(cn);
			}
		}
		GenericsType[] gts = configureTypeArguments(parameterizedType.arguments);
		base.setGenericsTypes(gts);
		return base;
	}

	private ClassNode configureClass(BinaryTypeBinding type) {
		if (type.id == TypeIds.T_JavaLangObject) {
			return ClassHelper.OBJECT_TYPE;
			// next two clauses - see GRECLIPSE-1521
		} else if (type.id == TypeIds.T_JavaLangString) {
			return ClassHelper.STRING_TYPE;
		} else if (type.id == TypeIds.T_JavaLangClass) {
			return ClassHelper.CLASS_Type;
		}
		JDTClassNode jcn = new JDTClassNode(type, resolver);
		return jcn;
	}

	private ClassNode configureSourceType(SourceTypeBinding type) {
		JDTClassNode jcn = new JDTClassNode(type, resolver); // TODO not being cached - should we cache it?
		return jcn;
	}

}