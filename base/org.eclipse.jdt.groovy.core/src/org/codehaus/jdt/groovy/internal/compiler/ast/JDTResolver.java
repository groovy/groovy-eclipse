/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import groovy.lang.GroovyClassLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser.GrapeAwareGroovyClassLoader;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * An extension to the standard groovy ResolveVisitor that can ask JDT for types when groovy cannot find them. A groovy project in
 * Eclipse is typically configured with very limited knowledge of its dependencies so most lookups are through JDT.
 * 
 * Resolver lifecycle:<br>
 * The JDTResolver is created at the same time as the (Groovy) CompilationUnit. The CompilationUnit knows about all the code that is
 * to be compiled together. The resolver maintains a cache from Binding to JDTClassNode and the cache contents have the same
 * lifetime as the JDTResolver. The resolver does type lookups through the currently active scope - the active scope is set when the
 * method 'commencingResolution()' is called. This is called by the superclass (ResolveVisitor) when it is about to start resolving
 * every reference in a type.
 * 
 * @author Andy Clement
 */
public class JDTResolver extends ResolveVisitor {

	/**
	 * length of the boolean type. any type name that is equal to or shorter than this is likely to be a primitive type.
	 */
	private static final int BOOLEAN_LENGTH = "boolean".length();

	// For resolver debugging
	private static final boolean debug = false;

	// Arbitrary selection of common types
	private static Map<String, ClassNode> commonTypes = new HashMap<String, ClassNode>();

	// So that testcases can quiz a resolver instance
	public static boolean recordInstances = false;
	public static List<JDTResolver> instances = null;

	static {
		commonTypes.put("java.lang.Object", ClassHelper.OBJECT_TYPE);
		commonTypes.put("java.lang.String", ClassHelper.STRING_TYPE);
		commonTypes.put("java.lang.Class", ClassHelper.CLASS_Type);

		commonTypes.put("java.lang.Boolean", ClassHelper.Boolean_TYPE);
		commonTypes.put("java.lang.Byte", ClassHelper.Byte_TYPE);
		commonTypes.put("java.lang.Character", ClassHelper.Character_TYPE);
		commonTypes.put("java.lang.Double", ClassHelper.Double_TYPE);
		commonTypes.put("java.lang.Float", ClassHelper.Float_TYPE);
		commonTypes.put("java.lang.Integer", ClassHelper.Integer_TYPE);
		commonTypes.put("java.lang.Long", ClassHelper.Long_TYPE);
		commonTypes.put("java.lang.Short", ClassHelper.Short_TYPE);

		commonTypes.put("boolean", ClassHelper.boolean_TYPE);
		commonTypes.put("byte", ClassHelper.byte_TYPE);
		commonTypes.put("char", ClassHelper.char_TYPE);
		commonTypes.put("double", ClassHelper.double_TYPE);
		commonTypes.put("float", ClassHelper.float_TYPE);
		commonTypes.put("int", ClassHelper.int_TYPE);
		commonTypes.put("long", ClassHelper.long_TYPE);
		commonTypes.put("short", ClassHelper.short_TYPE);
	}

	// By recording what is currently in progress in terms of creation, we avoid recursive problems (like Enum<E extends Enum<E>>)
	private Map<TypeBinding, JDTClassNode> inProgress = new HashMap<TypeBinding, JDTClassNode>();

	// Type references are resolved through the 'activeScope'. This ensures visibility rules are obeyed - just because a
	// type exists does not mean it is visible to some other type and scope lookups verify this.
	private GroovyCompilationUnitScope activeScope = null;

	// map of scopes in which resolution can happen
	private Map<ClassNode, GroovyTypeDeclaration> scopes = new HashMap<ClassNode, GroovyTypeDeclaration>();

	private List<ClassNode> haveBeenResolved = new ArrayList<ClassNode>();

	// Cache from bindings to JDTClassNodes to avoid unnecessary JDTClassNode creation
	private Map<Binding, JDTClassNode> nodeCache = Collections.synchronizedMap(new WeakHashMap<Binding, JDTClassNode>());

	public JDTResolver(CompilationUnit groovyCompilationUnit) {
		super(groovyCompilationUnit);
		if (recordInstances) {
			if (instances == null) {
				instances = new ArrayList<JDTResolver>();
			}
			instances.add(this);
		}
	}

	public static JDTClassNode getCachedNode(JDTResolver instance, String name) {
		for (Map.Entry<Binding, JDTClassNode> nodeFromCache : instance.nodeCache.entrySet()) {
			String nodename = new String(nodeFromCache.getKey().readableName());
			if (nodename.equals(name)) {
				return nodeFromCache.getValue();
			}
		}
		return null;
	}

	public static JDTClassNode getCachedNode(String name) {
		for (JDTResolver resolver : instances) {
			for (Map.Entry<Binding, JDTClassNode> nodeFromCache : resolver.nodeCache.entrySet()) {
				String nodename = new String(nodeFromCache.getKey().readableName());
				System.out.println(nodename);
				if (nodename.equals(name)) {
					return nodeFromCache.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * resolveFromModule() - look at other types in the same source file (no need to talk to JDT)
	 */
	@Override
	protected boolean resolveFromModule(ClassNode type, boolean testModuleImports) {
		boolean foundit = super.resolveFromModule(type, testModuleImports);
		recordDependency(type.getName());
		if (debug) {
			log("resolveFromModule", type, foundit);
		}
		return foundit;
	}

	/**
	 * resolveFromCompileUnit() - look at other source types in this CompilationUnit (ie. this 'project' in JDT terms).
	 */
	@Override
	protected boolean resolveFromCompileUnit(ClassNode type) {
		boolean foundit = super.resolveFromCompileUnit(type);
		recordDependency(type.getName());
		if (debug) {
			log("resolveFromCompileUnit", type, foundit);
		}

		if (foundit) {
			return true;
		}

		// Ask JDT for a source file, visible from this scope

		String typename = type.getName();
		ClassNode node = getScope().lookupClassNodeForSource(typename, this);
		if (debug) {
			log("resolveFromCompileUnit (jdt) ", type, node != null);
		}
		if (node != null) {
			type.setRedirect(node);
			return true;
		} else {
			return false;
		}

		// CHECK_IT(redirect);
	}

	@Override
	protected boolean resolveFromDefaultImports(ClassNode type, boolean testDefaultImports) {
		boolean foundit = super.resolveFromDefaultImports(type, testDefaultImports);
		if (activeScope != null) {
			// TODO need to refactor (duplicated in GroovyCompilationUnitScope)
			boolean b = testDefaultImports & !type.hasPackageName();
			// we do not resolve a vanilla name starting with a lower case letter
			// try to resolve against adefault import, because we know that the
			// default packages do not contain classes like these
			b &= !(type instanceof LowerCaseClass);
			if (b) {
				String extraImports = activeScope.compilerOptions().groovyExtraImports;
				if (extraImports != null) {
					try {
						String filename = new String(activeScope.referenceContext.getFileName());
						// may be something to do
						StringTokenizer st = new StringTokenizer(extraImports, ";");
						// Form would be 'com.foo.*,com.bar.MyType;.gradle=com.this.*,com.foo.Type"
						// If there is no qualifying suffix it applies to all types

						while (st.hasMoreTokens()) {
							String onesuffix = st.nextToken();
							int equals = onesuffix.indexOf('=');
							boolean shouldApply = false;
							String imports = null;
							if (equals == -1) {
								// definetly applies
								shouldApply = true;
								imports = onesuffix;
							} else {
								// need to check the suffix
								String suffix = onesuffix.substring(0, equals);
								shouldApply = filename.endsWith(suffix);
								imports = onesuffix.substring(equals + 1);
							}
							StringTokenizer st2 = new StringTokenizer(imports, ",");
							while (st2.hasMoreTokens()) {
								String nextElement = st2.nextToken();
								// One of two forms: a.b.c.* or a.b.c.Type
								if (nextElement.endsWith(".*")) {
									String withoutStar = nextElement.substring(0, nextElement.length() - 1);
									ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(withoutStar, type.getName());
									if (resolve(tmp, false, false, false)) {
										type.setRedirect(tmp.redirect());
										return true;
									}
								} else {
									String importedTypeName = nextElement;
									int asIndex = importedTypeName.indexOf(" as ");
									String asName = null;

									if (asIndex != -1) {
										asName = importedTypeName.substring(asIndex + 4).trim();
										importedTypeName = importedTypeName.substring(0, asIndex).trim();
									}
									String typeName = type.getName();
									if (importedTypeName.endsWith(typeName) || typeName.equals(asName)) {
										int lastdot = importedTypeName.lastIndexOf('.');
										String importTypeNameChopped = importedTypeName.substring(0, lastdot + 1);
										if (typeName.equals(asName)) {
											typeName = importedTypeName.substring(lastdot + 1);
										}
										ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(importTypeNameChopped,
												typeName);
										if (resolve(tmp, false, false, false)) {
											type.setRedirect(tmp.redirect());
											return true;
										}
									}
								}
							}

						}
					} catch (Exception e) {
						new RuntimeException("Problem processing extraImports: " + extraImports, e).printStackTrace();
					}
				}
			}
		}

		recordDependency(type.getName());
		if (debug) {
			log("resolveFromDefaultImports", type, foundit);
		}
		return foundit;
	}

	@Override
	protected boolean resolveFromStaticInnerClasses(ClassNode type, boolean testStaticInnerClasses) {
		boolean foundit = super.resolveFromStaticInnerClasses(type, testStaticInnerClasses);
		recordDependency(type.getName());
		if (debug) {
			log("resolveFromStaticInnerClasses", type, foundit);
		}
		return foundit;
		// FIXASC (M3) anything special for inner types?
	}

	// @Override
	// protected boolean resolveStaticInner(ClassNode type) {
	// boolean foundit = super.resolveStaticInner(type);
	// recordDependency(type.getName());
	// if (debug) {
	// log("resolveStaticInner", type, foundit);
	// }
	// return foundit;
	// }

	/**
	 * resolveFromClassCache() - no point in asking, the cache does not get populated.
	 */
	@Override
	protected boolean resolveFromClassCache(ClassNode type) {
		return false;
	}

	protected boolean resolveToOuter(ClassNode type) {
		return resolveToClass(type);
	}

	/**
	 * resolveToOuter() - this would normally ask the groovy class loader, but we don't want to do that - let JDT find everything.
	 */
	protected boolean resolveToClass(ClassNode type) {
		String typename = type.getName();
		ClassNode node = getScope().lookupClassNodeForBinary(typename, this);
		if (debug) {
			log("resolveToClass (jdt)", type, node != null);
		}
		if (node != null) {
			type.setRedirect(node);
			return true;
		}
		// Rudimentary grab support - if the compilation unit has our special classloader and a
		// grab has occurred, try and find the class through it
		GroovyClassLoader loader = compilationUnit.getClassLoader();
		if (loader instanceof GrapeAwareGroovyClassLoader) {
			GrapeAwareGroovyClassLoader gagc = (GrapeAwareGroovyClassLoader) loader;
			if (gagc.grabbed) {
				// System.out.println("Checking grabbed loader for " + type.getName());
				Class<?> cls;
				try {
					cls = loader.loadClass(type.getName(), false, true);
				} catch (ClassNotFoundException cnfe) {
					return false;
				} catch (CompilationFailedException cfe) {
					return false;
				}
				if (cls == null) {
					return false;
				}
				node = ClassHelper.make(cls);
				type.setRedirect(node);
				return true;
			}
		}
		return false;
		// boolean foundit = super.resolveToClass(type);
		// if (debug) {
		// log("resolveToClass", type, foundit);
		// }
		// return foundit;
	}

	/**
	 * resolveToScript() - ask the groovy class loader. We don't want to do this - let JDT find everything.
	 */
	@Override
	protected boolean resolveToScript(ClassNode type) {
		return false;
	}

	// Records a list of type names that aren't resolvable for the current resolution (unresolvables is cleared in
	// finishedResolution()). This means we won't constantly attempt to lookup something that is not found through the same routes
	// over and over (GRECLIPSE-870)
	private Set<String> unresolvables = new HashSet<String>();

	@Override
	protected boolean resolve(ClassNode type, boolean testModuleImports, boolean testDefaultImports, boolean testStaticInnerClasses) {
		String name = type.getName();
		// save time by being selective about whether to consult the commonRedirectMap
		if (name.charAt(0) == 'j' || name.length() <= BOOLEAN_LENGTH) {
			ClassNode commonRedirect = commonTypes.get(type.getName());
			if (commonRedirect != null) {
				type.setRedirect(commonRedirect);
				return true;
			}
		}
		if (unresolvables.contains(name)) {
			// System.out.println("Skipping... " + name);
			return false;
		} else {
			boolean b = super.resolve(type, testModuleImports, testDefaultImports, testStaticInnerClasses);
			// System.out.println("resolving... " + type.getName() + " = " + b);
			if (!b) {
				unresolvables.add(name);
			}
			return b;
		}
	}

	public ClassNode resolve(String qualifiedName) {
		ClassNode type = ClassHelper.makeWithoutCaching(qualifiedName);
		if (super.resolve(type)) {
			return type.redirect();
		} else {
			return ClassHelper.DYNAMIC_TYPE;
		}
	}

	// avoiding an inner resolve is dangerous.
	// leave a back door here to turn it back on
	// if no one complains, then safe to remove
	private static boolean doInnerResolve = Boolean.valueOf(System.getProperty("greclipse.doInnerResolve", "false"));

	@Override
	protected boolean resolveToInnerEnum(ClassNode type) {
		if (doInnerResolve) {
			return super.resolveToInnerEnum(type);
		}
		// inner classes are resolved by JDT, so
		// if we get here then the inner class does not exist
		return false;
	}

	@Override
	protected boolean resolveToInner(ClassNode type) {
		if (doInnerResolve) {
			return super.resolveToInner(type);
		}
		// inner classes are resolved by JDT, so
		// if we get here then the inner class does not exist
		return false;
	}

	// FIXASC callers could check if it is a 'funky' type before always recording a depedency
	// by 'funky' I mean that the type was constructed just to try something (org.foo.bar.java$lang$Wibble doesn't want recording!)
	private void recordDependency(String typename) {
		GroovyCompilationUnitScope gcuScope = getScope();
		// if (gcuScope == null) {
		// return;
		// }
		// System.err.println("Recording reference from " + toShortString(gcuScope) + " to " + typename);
		if (typename.indexOf(".") != -1) {
			gcuScope.recordQualifiedReference(CharOperation.splitOn('.', typename.toCharArray()));
		} else {
			gcuScope.recordSimpleReference(typename.toCharArray());
		}
	}

	/**
	 * Convert from a JDT Binding to a Groovy ClassNode
	 */
	ClassNode convertToClassNode(TypeBinding jdtBinding) {
		if (inProgress.containsKey(jdtBinding)) {
			return inProgress.get(jdtBinding);
		}
		JDTClassNode existingNode = nodeCache.get(jdtBinding);
		if (existingNode != null) {
			if (debug) {
				log("Using cached JDTClassNode for binding " + new String(jdtBinding.readableName()));
			}
			return existingNode;
		}

		if (debug) {
			log("createJDTClassNode: Building new JDTClassNode for binding " + new String(jdtBinding.readableName()));
		}

		ClassNode jdtNode = createJDTClassNode(jdtBinding);
		return jdtNode;
	}

	ClassNode makeWithoutCaching(TypeBinding jdtBinding) {
		return createJDTClassNode(jdtBinding);
	}

	/**
	 * Create a Groovy ClassNode that represents the JDT TypeBinding. Build the basic structure, mark it as 'in progress' and then
	 * continue with initialization. This allows self referential generic declarations.
	 * 
	 * @param jdtBinding the JDT binding for which to create a ClassNode
	 * @return the new ClassNode, of type JDTClassNode
	 */
	private ClassNode createJDTClassNode(TypeBinding jdtBinding) {
		// damn that enum type, this will sort it:
		if (inProgress.containsKey(jdtBinding)) {
			return inProgress.get(jdtBinding);
		}
		JDTClassNodeBuilder cnb = new JDTClassNodeBuilder(this);
		ClassNode classNode = cnb.configureType(jdtBinding);// createClassNode(jdtBinding);
		if (classNode instanceof JDTClassNode) {
			JDTClassNode jdtNode = (JDTClassNode) classNode;
			inProgress.put(jdtBinding, jdtNode);
			jdtNode.setupGenerics(); // for a binarytypebinding this fixes up those generics.
			inProgress.remove(jdtBinding);
			nodeCache.put(jdtBinding, jdtNode);
		}
		return classNode;
	}

	/**
	 * Called when a resolvevisitor is commencing resolution for a type - allows us to setup the JDTResolver to point at the right
	 * scope for resolutionification. If not able to find a scope, that is a serious problem!
	 */
	@Override
	protected boolean commencingResolution() {
		GroovyTypeDeclaration gtDeclaration = scopes.get(this.currentClass);
		if (gtDeclaration == null) {
			if (haveBeenResolved.contains(currentClass)) {
				// already resolved!
				return false;
			}
			GroovyEclipseBug geb = new GroovyEclipseBug("commencingResolution failed: no declaration found for class "
					+ currentClass);
			geb.printStackTrace();
			throw geb;
		}
		activeScope = null;
		if (gtDeclaration.scope == null) {
			// The scope may be null if there were errors in the code - let's not freak out the user here
			if (gtDeclaration.hasErrors()) {
				return false;
				// throw new GroovyEclipseBug("commencingResolution failed: aborting resolution, type " + currentClass.getName()
				// + " had earlier problems");
			}
			GroovyEclipseBug geb = new GroovyEclipseBug(
					"commencingResolution failed: declaration found, but unexpectedly found no scope for " + currentClass.getName());
			geb.printStackTrace();
			throw geb;
		}
		activeScope = (GroovyCompilationUnitScope) gtDeclaration.scope.compilationUnitScope();
		if (debug) {
			System.err.println("Resolver: commencing resolution for " + this.currentClass.getName());
		}
		return true;
	}

	@Override
	protected void finishedResolution() {
		scopes.remove(this.currentClass);
		haveBeenResolved.add(currentClass);
		unresolvables.clear();
	}

	public GroovyCompilationUnitScope getScope() {
		return activeScope;
	}

	private void log(String string) {
		System.err.println("Resolver: " + string);
	}

	// FIXASC can the relationship here from classNode to scope be better preserved to remove the need for this map?
	/**
	 * When recorded, the jdt resolver will be able to (later on) navigate from the classnode back to the JDT scope that should be
	 * used.
	 */
	public void record(GroovyTypeDeclaration gtDeclaration) {
		scopes.put(gtDeclaration.getClassNode(), gtDeclaration);
		if (gtDeclaration.memberTypes != null) {
			TypeDeclaration[] members = gtDeclaration.memberTypes;
			for (int m = 0; m < members.length; m++) {
				record((GroovyTypeDeclaration) members[m]);
			}
		}
		GroovyTypeDeclaration[] anonymousTypes = gtDeclaration.getAnonymousTypes();
		if (anonymousTypes != null) {
			for (int m = 0; m < anonymousTypes.length; m++) {
				record((GroovyTypeDeclaration) anonymousTypes[m]);
			}
		}
	}

	private void log(String string, ClassNode type, boolean foundit) {
		System.err.println("Resolver: " + string + " " + type.getName() + "  ?" + foundit);
	}

	public void startResolving(ClassNode node, SourceUnit source) {
		try {
			super.startResolving(node, source);
			unresolvables.clear();
		} catch (AbortResolutionException are) {
			// Can occur if there are other problems with the node (syntax errors) - so don't try resolving it
		}
	}

}
