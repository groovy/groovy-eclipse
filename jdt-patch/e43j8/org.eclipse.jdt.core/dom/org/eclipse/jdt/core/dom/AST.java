/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Umbrella owner and abstract syntax tree node factory.
 * An <code>AST</code> instance serves as the common owner of any number of
 * AST nodes, and as the factory for creating new AST nodes owned by that
 * instance.
 * <p>
 * Abstract syntax trees may be hand constructed by clients, using the
 * <code>new<i>TYPE</i></code> factory methods to create new nodes, and the
 * various <code>set<i>CHILD</i></code> methods
 * (see {@link org.eclipse.jdt.core.dom.ASTNode ASTNode} and its subclasses)
 * to connect them together.
 * </p>
 * <p>
 * Each AST node belongs to a unique AST instance, called the owning AST.
 * The children of an AST node always have the same owner as their parent node.
 * If a node from one AST is to be added to a different AST, the subtree must
 * be cloned first to ensures that the added nodes have the correct owning AST.
 * </p>
 * <p>
 * There can be any number of AST nodes owned by a single AST instance that are
 * unparented. Each of these nodes is the root of a separate little tree of nodes.
 * The method <code>ASTNode.getRoot()</code> navigates from any node to the root
 * of the tree that it is contained in. Ordinarily, an AST instance has one main
 * tree (rooted at a <code>CompilationUnit</code>), with newly-created nodes appearing
 * as additional roots until they are parented somewhere under the main tree.
 * One can navigate from any node to its AST instance, but not conversely.
 * </p>
 * <p>
 * The class {@link ASTParser} parses a string
 * containing a Java source code and returns an abstract syntax tree
 * for it. The resulting nodes carry source ranges relating the node back to
 * the original source characters.
 * </p>
 * <p>
 * Compilation units created by <code>ASTParser</code> from a
 * source document can be serialized after arbitrary modifications
 * with minimal loss of original formatting. Here is an example:
 * <pre>
 * Document doc = new Document("import java.util.List;\nclass X {}\n");
 * ASTParser parser = ASTParser.newParser(AST.JLS3);
 * parser.setSource(doc.get().toCharArray());
 * CompilationUnit cu = (CompilationUnit) parser.createAST(null);
 * cu.recordModifications();
 * AST ast = cu.getAST();
 * ImportDeclaration id = ast.newImportDeclaration();
 * id.setName(ast.newName(new String[] {"java", "util", "Set"});
 * cu.imports().add(id); // add import declaration at end
 * TextEdit edits = cu.rewrite(document, null);
 * UndoEdit undo = edits.apply(document);
 * </pre>
 * See also {@link org.eclipse.jdt.core.dom.rewrite.ASTRewrite} for
 * an alternative way to describe and serialize changes to a
 * read-only AST.
 * </p>
 * <p>
 * Clients may create instances of this class using {@link #newAST(int)},
 * but this class is not intended to be subclassed.
 * </p>
 *
 * @see ASTParser
 * @see ASTNode
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class AST {
	/**
	 * new Class[] {AST.class}
	 * @since 3.0
	 */
	private static final Class[] AST_CLASS = new Class[] {AST.class};

	/**
	 * Constant for indicating the AST API that handles JLS2.
	 * <p>
	 * This API is capable of handling all constructs
	 * in the Java language as described in the Java Language
     * Specification, Second Edition (JLS2).
     * JLS2 is a superset of all earlier versions of the
     * Java language, and the JLS2 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including J2SE 1.4.
     * </p>
     *
	 * @since 3.0
	 * @deprecated Clients should use the {@link #JLS8} AST API instead.
	 */
	public static final int JLS2 = 2;

	/**
	 * Internal synonym for {@link #JLS2}. Use to alleviate
	 * deprecation warnings.
	 * @since 3.1
	 */
	/*package*/ static final int JLS2_INTERNAL = JLS2;
	
	/**
	 * Constant for indicating the AST API that handles JLS3.
	 * <p>
	 * This API is capable of handling all constructs in the
	 * Java language as described in the Java Language
	 * Specification, Third Edition (JLS3).
     * JLS3 is a superset of all earlier versions of the
     * Java language, and the JLS3 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including J2SE 5 (aka JDK 1.5).
     * </p>
     *
	 * @since 3.1
	 * @deprecated Clients should use the {@link #JLS8} AST API instead.
	 */
	public static final int JLS3 = 3;
	
	/**
	 * Internal synonym for {@link #JLS3}. Use to alleviate
	 * deprecation warnings.
	 * @since 3.8
	 */
	/*package*/ static final int JLS3_INTERNAL = JLS3;

	/**
	 * Constant for indicating the AST API that handles JLS4 (aka JLS7).
	 * <p>
	 * This API is capable of handling all constructs in the
	 * Java language as described in the Java Language
	 * Specification, Java SE 7 Edition (JLS7) as specified by JSR336.
	 * JLS4 is a superset of all earlier versions of the
	 * Java language, and the JLS4 API can be used to manipulate
	 * programs written in all versions of the Java language
	 * up to and including Java SE 7 (aka JDK 1.7).
	 * </p>
	 *
	 * @since 3.7.1
	 * @deprecated Clients should use the {@link #JLS8} AST API instead.
	 */
	public static final int JLS4 = 4;
	
	/**
	 * Internal synonym for {@link #JLS4}. Use to alleviate
	 * deprecation warnings.
	 * @since 3.10
	 */
	/*package*/ static final int JLS4_INTERNAL = JLS4;
	
	/**
	 * Constant for indicating the AST API that handles JLS8.
	 * <p>
	 * This API is capable of handling all constructs in the
	 * Java language as described in the Java Language
	 * Specification, Java SE 8 Edition (JLS8) as specified by JSR337.
	 * JLS8 is a superset of all earlier versions of the
	 * Java language, and the JLS8 API can be used to manipulate
	 * programs written in all versions of the Java language
	 * up to and including Java SE 8 (aka JDK 1.8).
	 * </p>
	 *
	 * @since 3.10
	 */
	public static final int JLS8 = 8;

	/*
	 * Must not collide with a value for ICompilationUnit constants
	 */
	static final int RESOLVED_BINDINGS = 0x80000000;

	/**
	 * Internal method.
	 * <p>
	 * This method converts the given internal compiler AST for the given source string
	 * into a compilation unit. This method is not intended to be called by clients.
	 * </p>
	 *
 	 * @param level the API level; one of the <code>JLS*</code> level constants
	 * @param compilationUnitDeclaration an internal AST node for a compilation unit declaration
	 * @param source the string of the Java compilation unit
	 * @param options compiler options
	 * @param workingCopy the working copy that the AST is created from
	 * @param monitor the progress monitor used to report progress and request cancellation,
	 *     or <code>null</code> if none
	 * @param isResolved whether the given compilation unit declaration is resolved
	 * @return the compilation unit node
	 * @deprecated Use org.eclipse.jdt.core.dom.AST.convertCompilationUnit(int, CompilationUnitDeclaration, Map, boolean, CompilationUnit, int, IProgressMonitor) instead
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static CompilationUnit convertCompilationUnit(
			int level,
			org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration compilationUnitDeclaration,
			char[] source,
			Map options,
			boolean isResolved,
			org.eclipse.jdt.internal.core.CompilationUnit workingCopy,
			int reconcileFlags,
			IProgressMonitor monitor) {
		return null;
	}

	/**
	 * Internal method.
	 * <p>
	 * This method converts the given internal compiler AST for the given source string
	 * into a compilation unit. This method is not intended to be called by clients.
	 * </p>
	 *
 	 * @param level the API level; one of the <code>JLS*</code> level constants
	 * @param compilationUnitDeclaration an internal AST node for a compilation unit declaration
	 * @param options compiler options
	 * @param workingCopy the working copy that the AST is created from
	 * @param monitor the progress monitor used to report progress and request cancellation,
	 *     or <code>null</code> if none
	 * @param isResolved whether the given compilation unit declaration is resolved
	 * @return the compilation unit node
	 * @since 3.4
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static CompilationUnit convertCompilationUnit(
		int level,
		org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration compilationUnitDeclaration,
		Map options,
		boolean isResolved,
		org.eclipse.jdt.internal.core.CompilationUnit workingCopy,
		int reconcileFlags,
		IProgressMonitor monitor) {

		ASTConverter converter = new ASTConverter(options, isResolved, monitor);
		AST ast = AST.newAST(level);
		int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
		BindingResolver resolver = null;
		if (isResolved) {
			resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope, workingCopy.owner, new DefaultBindingResolver.BindingTables(), false, true);
			((DefaultBindingResolver) resolver).isRecoveringBindings = (reconcileFlags & ICompilationUnit.ENABLE_BINDINGS_RECOVERY) != 0;
			ast.setFlag(AST.RESOLVED_BINDINGS);
		} else {
			resolver = new BindingResolver();
		}
		ast.setFlag(reconcileFlags);
		ast.setBindingResolver(resolver);
		converter.setAST(ast);

		CompilationUnit unit = converter.convert(compilationUnitDeclaration, workingCopy.getContents());
		unit.setLineEndTable(compilationUnitDeclaration.compilationResult.getLineSeparatorPositions());
		unit.setTypeRoot(workingCopy.originalFromClone());
		ast.setDefaultNodeFlag(savedDefaultNodeFlag);
		return unit;
	}

	/**
	 * Creates a new Java abstract syntax tree
     * (AST) following the specified set of API rules.
     * <p>
     * Clients should use this method specifying {@link #JLS8} as the
     * AST level in all cases, even when dealing with source of earlier JDK versions like 1.3 or 1.4.
     * </p>
     *
 	 * @param level the API level; one of the <code>JLS*</code> level constants
	 * @return new AST instance following the specified set of API rules.
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the API level is not one of the <code>JLS*</code> level constants</li>
	 * </ul>
     * @since 3.0
	 */
	public static AST newAST(int level) {
		return new AST(level);
	}

	/**
	 * Parses the given string as a Java compilation unit and creates and
	 * returns a corresponding abstract syntax tree.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the given source string (the given source string itself
	 * is not remembered with the AST).
	 * The source range usually begins at the first character of the first token
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including compilation units and the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * This method does not compute binding information; all <code>resolveBinding</code>
	 * methods applied to nodes of the resulting AST return <code>null</code>.
	 * </p>
	 *
	 * @param source the string to be parsed as a Java compilation unit
	 * @return the compilation unit node
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @since 2.0
	 * @deprecated Use {@link ASTParser} instead.
	 */
	public static CompilationUnit parseCompilationUnit(char[] source) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		ASTParser c = ASTParser.newParser(AST.JLS2);
		c.setSource(source);
		ASTNode result = c.createAST(null);
		return (CompilationUnit) result;
	}

	/**
	 * Parses the given string as the hypothetical contents of the named
	 * compilation unit and creates and returns a corresponding abstract syntax tree.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the given source string (the given source string itself
	 * is not remembered with the AST).
	 * The source range usually begins at the first character of the first token
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including compilation units and the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * If the given project is not <code>null</code>, the various names
	 * and types appearing in the compilation unit can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings
	 * draw connections between the different parts of a program, and
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Bindings are resolved at the time the AST is created. Subsequent
	 * modifications to the AST do not affect the bindings returned by
	 * <code>resolveBinding</code> methods in any way; these methods return the
	 * same binding as before the AST was modified (including modifications
	 * that rearrange subtrees by reparenting nodes).
	 * If the given project is <code>null</code>, the analysis
	 * does not go beyond parsing and building the tree, and all
	 * <code>resolveBinding</code> methods return <code>null</code> from the
	 * outset.
	 * </p>
	 * <p>
	 * The name of the compilation unit must be supplied for resolving bindings.
	 * This name should be suffixed by a dot ('.') followed by one of the
	 * {@link JavaCore#getJavaLikeExtensions() Java-like extensions}
	 * and match the name of the main
	 * (public) class or interface declared in the source. For example, if the source
	 * declares a public class named "Foo", the name of the compilation can be
	 * "Foo.java". For the purposes of resolving bindings, types declared in the
	 * source string hide types by the same name available through the classpath
	 * of the given project.
	 * </p>
	 *
	 * @param source the string to be parsed as a Java compilation unit
	 * @param unitName the name of the compilation unit that would contain the source
	 *    string, or <code>null</code> if <code>javaProject</code> is also <code>null</code>
	 * @param project the Java project used to resolve names, or
	 *    <code>null</code> if bindings are not resolved
	 * @return the compilation unit node
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @since 2.0
	 * @deprecated Use {@link ASTParser} instead.
	 */
	public static CompilationUnit parseCompilationUnit(
		char[] source,
		String unitName,
		IJavaProject project) {

		if (source == null) {
			throw new IllegalArgumentException();
		}
		ASTParser astParser = ASTParser.newParser(AST.JLS2);
		astParser.setSource(source);
		astParser.setUnitName(unitName);
		astParser.setProject(project);
		astParser.setResolveBindings(project != null);
		ASTNode result = astParser.createAST(null);
		return (CompilationUnit) result;
	}

	/**
	 * Parses the source string corresponding to the given Java class file
	 * element and creates and returns a corresponding abstract syntax tree.
	 * The source string is obtained from the Java model element using
	 * <code>IClassFile.getSource()</code>, and is only available for a class
	 * files with attached source.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the source string (the source string is not remembered
	 * with the AST).
	 * The source range usually begins at the first character of the first token
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including compilation units and the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * If <code>resolveBindings</code> is <code>true</code>, the various names
	 * and types appearing in the compilation unit can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings
	 * draw connections between the different parts of a program, and
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Bindings are resolved at the time the AST is created. Subsequent
	 * modifications to the AST do not affect the bindings returned by
	 * <code>resolveBinding</code> methods in any way; these methods return the
	 * same binding as before the AST was modified (including modifications
	 * that rearrange subtrees by reparenting nodes).
	 * If <code>resolveBindings</code> is <code>false</code>, the analysis
	 * does not go beyond parsing and building the tree, and all
	 * <code>resolveBinding</code> methods return <code>null</code> from the
	 * outset.
	 * </p>
	 *
	 * @param classFile the Java model class file whose corresponding source code is to be parsed
	 * @param resolveBindings <code>true</code> if bindings are wanted,
	 *   and <code>false</code> if bindings are not of interest
	 * @return the compilation unit node
	 * @exception IllegalArgumentException if the given Java element does not
	 * exist or if its source string cannot be obtained
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @since 2.1
	 * @deprecated Use {@link ASTParser} instead.
	 */
	public static CompilationUnit parseCompilationUnit(
		IClassFile classFile,
		boolean resolveBindings) {

		if (classFile == null) {
			throw new IllegalArgumentException();
		}
		try {
			ASTParser c = ASTParser.newParser(AST.JLS2);
			c.setSource(classFile);
			c.setResolveBindings(resolveBindings);
			ASTNode result = c.createAST(null);
			return (CompilationUnit) result;
		} catch (IllegalStateException e) {
			// convert ASTParser's complaints into old form
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Parses the source string of the given Java model compilation unit element
	 * and creates and returns a corresponding abstract syntax tree. The source
	 * string is obtained from the Java model element using
	 * <code>ICompilationUnit.getSource()</code>.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the source string (the source string is not remembered
	 * with the AST).
	 * The source range usually begins at the first character of the first token
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including compilation units and the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * If <code>resolveBindings</code> is <code>true</code>, the various names
	 * and types appearing in the compilation unit can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings
	 * draw connections between the different parts of a program, and
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Bindings are resolved at the time the AST is created. Subsequent
	 * modifications to the AST do not affect the bindings returned by
	 * <code>resolveBinding</code> methods in any way; these methods return the
	 * same binding as before the AST was modified (including modifications
	 * that rearrange subtrees by reparenting nodes).
	 * If <code>resolveBindings</code> is <code>false</code>, the analysis
	 * does not go beyond parsing and building the tree, and all
	 * <code>resolveBinding</code> methods return <code>null</code> from the
	 * outset.
	 * </p>
	 *
	 * @param unit the Java model compilation unit whose source code is to be parsed
	 * @param resolveBindings <code>true</code> if bindings are wanted,
	 *   and <code>false</code> if bindings are not of interest
	 * @return the compilation unit node
	 * @exception IllegalArgumentException if the given Java element does not
	 * exist or if its source string cannot be obtained
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @since 2.0
	 * @deprecated Use {@link ASTParser} instead.
	 */
	public static CompilationUnit parseCompilationUnit(
		ICompilationUnit unit,
		boolean resolveBindings) {

		try {
			ASTParser c = ASTParser.newParser(AST.JLS2);
			c.setSource(unit);
			c.setResolveBindings(resolveBindings);
			ASTNode result = c.createAST(null);
			return (CompilationUnit) result;
		} catch (IllegalStateException e) {
			// convert ASTParser's complaints into old form
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Level of AST API supported by this AST.
	 * @since 3.0
	 */
	int apiLevel;

	/**
	 * Tag bit value. This represents internal state of the tree.
	 */
	private int bits;

	/**
	 * Default value of <code>flag<code> when a new node is created.
	 */
	private int defaultNodeFlag = 0;

	/**
	 * When disableEvents > 0, events are not reported and
	 * the modification count stays fixed.
	 * <p>
	 * This mechanism is used in lazy initialization of a node
	 * to prevent events from being reported for the modification
	 * of the node as well as for the creation of the missing child.
	 * </p>
	 * @since 3.0
	 */
	private int disableEvents = 0;

	/**
	 * The event handler for this AST.
	 * Initially an event handler that does not nothing.
	 * @since 3.0
	 */
	private NodeEventHandler eventHandler = new NodeEventHandler();

	/**
	 * Internal object unique to the AST instance. Readers must synchronize on
	 * this object when the modifying instance fields.
	 * @since 3.0
	 */
	private final Object internalASTLock = new Object();

	/**
	 * Internal modification count; initially 0; increases monotonically
	 * <b>by one or more</b> as the AST is successively modified.
	 */
	private long modificationCount = 0;

	/**
	 * Internal original modification count; value is equals to <code>
	 * modificationCount</code> at the end of the parse (<code>ASTParser
	 * </code>). If this ast is not created with a parser then value is 0.
	 * @since 3.0
	 */
	private long originalModificationCount = 0;

	/**
	 * The binding resolver for this AST. Initially a binding resolver that
	 * does not resolve names at all.
	 */
	private BindingResolver resolver = new BindingResolver();

	/**
	 * Internal ast rewriter used to record ast modification when record mode is enabled.
	 */
	InternalASTRewrite rewriter;

	/**
	 * Java Scanner used to validate preconditions for the creation of specific nodes
	 * like CharacterLiteral, NumberLiteral, StringLiteral or SimpleName.
	 */
	Scanner scanner;

	/**
	 * new Object[] {this}
	 * @since 3.0
	 */
	private final Object[] THIS_AST= new Object[] {this};

	/**
	 * Creates a new, empty abstract syntax tree using default options.
	 *
	 * @see JavaCore#getDefaultOptions()
	 * @deprecated Clients should port their code to use the new JLS4 AST API and call
	 *    {@link #newAST(int) AST.newAST(AST.JLS4)} instead of using this constructor.
	 */
	public AST() {
		this(JavaCore.getDefaultOptions());
	}

	/**
	 * Creates a new Java abstract syntax tree
     * (AST) following the specified set of API rules.
     *
 	 * @param level the API level; one of the <code>JLS*</code> level constants
     * @since 3.0
	 */
	private AST(int level) {
		switch(level) {
			case JLS2_INTERNAL :
			case JLS3_INTERNAL :
				this.apiLevel = level;
				// initialize a scanner
				this.scanner = new Scanner(
						true /*comment*/,
						true /*whitespace*/,
						false /*nls*/,
						ClassFileConstants.JDK1_3 /*sourceLevel*/,
						ClassFileConstants.JDK1_5 /*complianceLevel*/,
						null/*taskTag*/,
						null/*taskPriorities*/,
						true/*taskCaseSensitive*/);
				break;
			case JLS4_INTERNAL :
				this.apiLevel = level;
				// initialize a scanner
				this.scanner = new Scanner(
						true /*comment*/,
						true /*whitespace*/,
						false /*nls*/,
						ClassFileConstants.JDK1_7 /*sourceLevel*/,
						ClassFileConstants.JDK1_7 /*complianceLevel*/,
						null/*taskTag*/,
						null/*taskPriorities*/,
						true/*taskCaseSensitive*/);
				break;
			case JLS8 :
				this.apiLevel = level;
				// initialize a scanner
				this.scanner = new Scanner(
						true /*comment*/,
						true /*whitespace*/,
						false /*nls*/,
						ClassFileConstants.JDK1_8 /*sourceLevel*/,
						ClassFileConstants.JDK1_8 /*complianceLevel*/,
						null/*taskTag*/,
						null/*taskPriorities*/,
						true/*taskCaseSensitive*/);
				break;	
			default:
				throw new IllegalArgumentException("Unsupported JLS level"); //$NON-NLS-1$
		}
	}

	/**
	 * Creates a new, empty abstract syntax tree using the given options.
	 * <p>
	 * Following option keys are significant:
	 * <ul>
	 * <li><code>"org.eclipse.jdt.core.compiler.source"</code> -
	 *    indicates source compatibility mode (as per <code>JavaCore</code>);
	 *    <code>"1.3"</code> means the source code is as per JDK 1.3;
	 *    <code>"1.4"</code> means the source code is as per JDK 1.4
	 *    (<code>"assert"</code> is now a keyword);
	 *    <code>"1.5"</code> means the source code is as per JDK 1.5
	 *    (<code>"enum"</code> is now a keyword);
	 *    <code>"1.7"</code> means the source code is as per JDK 1.7;
	 *    additional legal values may be added later. </li>
	 * </ul>
	 * Options other than the above are ignored.
	 * </p>
	 *
	 * @param options the table of options (key type: <code>String</code>;
	 *    value type: <code>String</code>)
	 * @see JavaCore#getDefaultOptions()
	 * @deprecated Clients should port their code to use the new JLS4 AST API and call
	 *    {@link #newAST(int) AST.newAST(AST.JLS4)} instead of using this constructor.
	 */
	public AST(Map options) {
		this(JLS2);
		Object sourceLevelOption = options.get(JavaCore.COMPILER_SOURCE);
		long sourceLevel = ClassFileConstants.JDK1_3;
		if (JavaCore.VERSION_1_4.equals(sourceLevelOption)) {
			sourceLevel = ClassFileConstants.JDK1_4;
		} else if (JavaCore.VERSION_1_5.equals(sourceLevelOption)) {
			sourceLevel = ClassFileConstants.JDK1_5;
		} else if (JavaCore.VERSION_1_7.equals(sourceLevelOption)) {
			sourceLevel = ClassFileConstants.JDK1_7;
		}
		Object complianceLevelOption = options.get(JavaCore.COMPILER_COMPLIANCE);
		long complianceLevel = ClassFileConstants.JDK1_3;
		if (JavaCore.VERSION_1_4.equals(complianceLevelOption)) {
			complianceLevel = ClassFileConstants.JDK1_4;
		} else if (JavaCore.VERSION_1_5.equals(complianceLevelOption)) {
			complianceLevel = ClassFileConstants.JDK1_5;
		} else if (JavaCore.VERSION_1_7.equals(complianceLevelOption)) {
			complianceLevel = ClassFileConstants.JDK1_7;
		}
		// override scanner if 1.4 or 1.5 asked for
		this.scanner = new Scanner(
			true /*comment*/,
			true /*whitespace*/,
			false /*nls*/,
			sourceLevel /*sourceLevel*/,
			complianceLevel /*complianceLevel*/,
			null/*taskTag*/,
			null/*taskPriorities*/,
			true/*taskCaseSensitive*/);
	}

	/**
	 * Return the API level supported by this AST.
	 *
	 * @return level the API level; one of the <code>JLS*</code> level constants
     * declared on <code>AST</code>; assume this set is open-ended
     * @since 3.0
	 */
	public int apiLevel() {
		return this.apiLevel;
	}

	/**
	 * Creates an unparented node of the given node class
	 * (non-abstract subclass of {@link ASTNode}).
	 *
	 * @param nodeClass AST node class
	 * @return a new unparented node owned by this AST
	 * @exception IllegalArgumentException if <code>nodeClass</code> is
	 * <code>null</code> or is not a concrete node type class
	 * or is not supported for this AST's API level
	 * @since 3.0
	 */
	public ASTNode createInstance(Class nodeClass) {
		if (nodeClass == null) {
			throw new IllegalArgumentException();
		}
		try {
			// invoke constructor with signature Foo(AST)
			Constructor c = nodeClass.getDeclaredConstructor(AST_CLASS);
			Object result = c.newInstance(this.THIS_AST);
			return (ASTNode) result;
		} catch (NoSuchMethodException e) {
			// all AST node classes have a Foo(AST) constructor
			// therefore nodeClass is not legit
			throw new IllegalArgumentException();
		} catch (InstantiationException e) {
			// all concrete AST node classes can be instantiated
			// therefore nodeClass is not legit
			throw new IllegalArgumentException();
		} catch (IllegalAccessException e) {
			// all AST node classes have an accessible Foo(AST) constructor
			// therefore nodeClass is not legit
			throw new IllegalArgumentException();
		} catch (InvocationTargetException e) {
			// concrete AST node classes do not die in the constructor
			// therefore nodeClass is not legit
			IllegalArgumentException iae = new IllegalArgumentException();
			iae.initCause(e.getCause());
			throw iae;
		}
	}

	/**
	 * Creates an unparented node of the given node type.
	 * This convenience method is equivalent to:
	 * <pre>
	 * createInstance(ASTNode.nodeClassForType(nodeType))
	 * </pre>
	 *
	 * @param nodeType AST node type, one of the node type
	 * constants declared on {@link ASTNode}
	 * @return a new unparented node owned by this AST
	 * @exception IllegalArgumentException if <code>nodeType</code> is
	 * not a legal AST node type or if it's not supported for this AST's API level
	 * @since 3.0
	 */
	public ASTNode createInstance(int nodeType) {
		// nodeClassForType throws IllegalArgumentException if nodeType is bogus
		Class nodeClass = ASTNode.nodeClassForType(nodeType);
		return createInstance(nodeClass);
	}

	/**
     * Disable events.
	 * This method is thread-safe for AST readers.
	 *
	 * @see #reenableEvents()
     * @since 3.0
     */
	final void disableEvents() {
		synchronized (this.internalASTLock) {
			// guard against concurrent access by another reader
			this.disableEvents++;
		}
		// while disableEvents > 0 no events will be reported, and mod count will stay fixed
	}

	/**
	 * Returns the binding resolver for this AST.
	 *
	 * @return the binding resolver for this AST
	 */
	BindingResolver getBindingResolver() {
		return this.resolver;
	}

	/**
	 * Returns default node flags of new nodes of this AST.
	 *
	 * @return the default node flags of new nodes of this AST
	 * @since 3.0
	 */
	int getDefaultNodeFlag() {
		return this.defaultNodeFlag;
	}

	/**
	 * Returns the event handler for this AST.
	 *
	 * @return the event handler for this AST
	 * @since 3.0
	 */
	NodeEventHandler getEventHandler() {
		return this.eventHandler;
	}

	/**
	 * Returns true if the ast tree was created with bindings recovery, false otherwise
	 *
	 * @return true if the ast tree was created with bindings recovery, false otherwise
	 * @since 3.3
	 */
	public boolean hasBindingsRecovery() {
		return (this.bits & ICompilationUnit.ENABLE_BINDINGS_RECOVERY) != 0;
	}

	/**
	 * Returns true if the ast tree was created with bindings, false otherwise
	 *
	 * @return true if the ast tree was created with bindings, false otherwise
	 * @since 3.3
	 */
	public boolean hasResolvedBindings() {
		return (this.bits & RESOLVED_BINDINGS) != 0;
	}

	/**
	 * Returns true if the ast tree was created with statements recovery, false otherwise
	 *
	 * @return true if the ast tree was created with statements recovery, false otherwise
	 * @since 3.3
	 */
	public boolean hasStatementsRecovery() {
		return (this.bits & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0;
	}

	/* (omit javadoc for this method)
	 * This method is a copy of setName(String[]) that doesn't do any validation.
	 */
	Name internalNewName(String[] identifiers) {
		int count = identifiers.length;
		if (count == 0) {
			throw new IllegalArgumentException();
		}
		final SimpleName simpleName = new SimpleName(this);
		simpleName.internalSetIdentifier(identifiers[0]);
		Name result = simpleName;
		for (int i = 1; i < count; i++) {
			SimpleName name = new SimpleName(this);
			name.internalSetIdentifier(identifiers[i]);
			result = newQualifiedName(result, name);
		}
		return result;
	}

	/**
	 * Returns the modification count for this AST. The modification count
	 * is a non-negative value that increases (by 1 or perhaps by more) as
	 * this AST or its nodes are changed. The initial value is unspecified.
	 * <p>
	 * The following things count as modifying an AST:
	 * <ul>
	 * <li>creating a new node owned by this AST,</li>
	 * <li>adding a child to a node owned by this AST,</li>
	 * <li>removing a child from a node owned by this AST,</li>
	 * <li>setting a non-node attribute of a node owned by this AST.</li>
	 * </ul>
	 * </p>
	 * Operations which do not entail creating or modifying existing nodes
	 * do not increase the modification count.
	 * <p>
	 * N.B. This method may be called several times in the course
	 * of a single client operation. The only promise is that the modification
	 * count increases monotonically as the AST or its nodes change; there is
	 * no promise that a modifying operation increases the count by exactly 1.
	 * </p>
	 *
	 * @return the current value (non-negative) of the modification counter of
	 *    this AST
	 */
	public long modificationCount() {
		return this.modificationCount;
	}

	/**
	 * Indicates that this AST is about to be modified.
	 * <p>
	 * The following things count as modifying an AST:
	 * <ul>
	 * <li>creating a new node owned by this AST</li>
	 * <li>adding a child to a node owned by this AST</li>
	 * <li>removing a child from a node owned by this AST</li>
	 * <li>setting a non-node attribute of a node owned by this AST</li>.
	 * </ul>
	 * </p>
	 * <p>
	 * N.B. This method may be called several times in the course
	 * of a single client operation.
	 * </p>
	 */
	void modifying() {
		// when this method is called during lazy init, events are disabled
		// and the modification count will not be increased
		if (this.disableEvents > 0) {
			return;
		}
		// increase the modification count
		this.modificationCount++;
	}

	/**
	 * A local method to workaround calling deprecated method in array type.
	 * @deprecated
	 */
	private void setArrayComponentType(ArrayType arrayType, Type type) {
		arrayType.setComponentType(type);
	}

	/**
	 * Creates and returns a new unparented annotation type declaration
	 * node for an unspecified, but legal, name; no modifiers; no javadoc;
	 * and an empty list of member declarations.
	 *
	 * @return a new unparented annotation type declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public AnnotationTypeDeclaration newAnnotationTypeDeclaration() {
		AnnotationTypeDeclaration result = new AnnotationTypeDeclaration(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented annotation type
	 * member declaration node for an unspecified, but legal,
	 * member name and type; no modifiers; no javadoc;
	 * and no default value.
	 *
	 * @return a new unparented annotation type member declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public AnnotationTypeMemberDeclaration newAnnotationTypeMemberDeclaration() {
		AnnotationTypeMemberDeclaration result = new AnnotationTypeMemberDeclaration(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented anonymous class declaration
	 * node owned by this AST. By default, the body declaration list is empty.
	 *
	 * @return a new unparented anonymous class declaration node
	 */
	public AnonymousClassDeclaration newAnonymousClassDeclaration() {
		AnonymousClassDeclaration result = new AnonymousClassDeclaration(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented array access expression node
	 * owned by this AST. By default, the array and index expression are
	 * both unspecified (but legal).
	 *
	 * @return a new unparented array access expression node
	 */
	public ArrayAccess newArrayAccess() {
		ArrayAccess result = new ArrayAccess(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented array creation expression node
	 * owned by this AST. By default, the array type is an unspecified
	 * 1-dimensional array, the list of dimensions is empty, and there is no
	 * array initializer.
	 * <p>
	 * Examples:
	 * <code>
	 * <pre>
	 * // new String[len]
	 * ArrayCreation ac1 = ast.newArrayCreation();
	 * ac1.setType(
	 *    ast.newArrayType(
	 *       ast.newSimpleType(ast.newSimpleName("String"))));
	 * ac1.dimensions().add(ast.newSimpleName("len"));
     *
	 * // new double[7][24][]
	 * ArrayCreation ac2 = ast.newArrayCreation();
	 * ac2.setType(
	 *    ast.newArrayType(
	 *       ast.newPrimitiveType(PrimitiveType.DOUBLE), 3));
	 * ac2.dimensions().add(ast.newNumberLiteral("7"));
	 * ac2.dimensions().add(ast.newNumberLiteral("24"));
	 *
	 * // new int[] {1, 2}
	 * ArrayCreation ac3 = ast.newArrayCreation();
	 * ac3.setType(
	 *    ast.newArrayType(
	 *       ast.newPrimitiveType(PrimitiveType.INT)));
	 * ArrayInitializer ai = ast.newArrayInitializer();
	 * ac3.setInitializer(ai);
	 * ai.expressions().add(ast.newNumberLiteral("1"));
	 * ai.expressions().add(ast.newNumberLiteral("2"));
	 * </pre>
	 * </code>
	 * </p>
	 *
	 * @return a new unparented array creation expression node
	 */
	public ArrayCreation newArrayCreation() {
		ArrayCreation result = new ArrayCreation(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented array initializer node
	 * owned by this AST. By default, the initializer has no expressions.
	 *
	 * @return a new unparented array initializer node
	 */
	public ArrayInitializer newArrayInitializer() {
		ArrayInitializer result = new ArrayInitializer(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented array type node with the given
	 * element type, which cannot be an array type for API levels JLS8 and later.
	 * By default, the array type has one non-annotated dimension.
	 * <p>
	 * For JLS4 and before, the given component type may be another array type.
	 *
	 * @param elementType element type for API level JLS8 and later, or the
	 * component type (possibly another array type) for levels less than JLS8
	 * @return a new unparented array type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>API level is JLS8 or later and type is an array type</li>
	 * </ul>
	 */
	public ArrayType newArrayType(Type elementType) {
		ArrayType result;
		if (this.apiLevel < AST.JLS8) {
			result = new ArrayType(this);
			setArrayComponentType(result, elementType);
			return result;
		}
		if (elementType.isArrayType()) {
			throw new IllegalArgumentException();
		}
		result = new ArrayType(this);
		result.setElementType(elementType);
		return result;
	}

	/**
	 * Creates and returns a new unparented array type node with the given
	 * element type and number of dimensions.
	 * <p>
	 * For JLS4 and before, the element type passed in can be an array type, but in that case, the
	 * element type of the result will not be the same as what was passed in.
	 * For JLS4 and before, the dimensions cannot be 0.
	 * </p>
	 *
	 * @param elementType the element type (cannot be an array type for JLS8 and later)
	 * @param dimensions the number of dimensions, a non-negative number
	 * @return a new unparented array type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>the element type is null</li>
	 * <li>the number of dimensions is lower than 0 (for JLS4 and before: lower than 1)</li>
	 * <li>the number of dimensions is greater than 255</li>
	 * <li>for levels from JLS8 and later, if the element type is an array type </li>
	 * </ul>
	 */
	public ArrayType newArrayType(Type elementType, int dimensions) {
		if (elementType == null) {
			throw new IllegalArgumentException();
		}
		if (dimensions < 0 || dimensions > 255) {
			// max as per Java VM spec
			throw new IllegalArgumentException();
		}
		ArrayType result;
		if (this.apiLevel < AST.JLS8) {
			if (dimensions < 1) {
				throw new IllegalArgumentException();
			}
			result = new ArrayType(this);
			setArrayComponentType(result, elementType);
			for (int i = 2; i <= dimensions; i++) {
				result = newArrayType(result);
			}
			return result;
		}
		//level >= JLS8
		if (elementType.isArrayType()) {
			throw new IllegalArgumentException();
		}
		result = new ArrayType(this, 0);
		result.setElementType(elementType);
		for (int i = 0; i < dimensions; ++i) {
			result.dimensions().add(new Dimension(this));
		}
		return result;

	}

	/**
	 * Creates a new unparented assert statement node owned by this AST.
	 * By default, the first expression is unspecified, but legal, and has no
	 * message expression.
	 *
	 * @return a new unparented assert statement node
	 */
	public AssertStatement newAssertStatement() {
		return new AssertStatement(this);
	}

	/**
	 * Creates and returns a new unparented assignment expression node
	 * owned by this AST. By default, the assignment operator is "=" and
	 * the left and right hand side expressions are unspecified, but
	 * legal, names.
	 *
	 * @return a new unparented assignment expression node
	 */
	public Assignment newAssignment() {
		Assignment result = new Assignment(this);
		return result;
	}

	/**
	 * Creates an unparented block node owned by this AST, for an empty list
	 * of statements.
	 *
	 * @return a new unparented, empty block node
	 */
	public Block newBlock() {
		return new Block(this);
	}

	/**
	 * Creates and returns a new block comment placeholder node.
	 * <p>
	 * Note that this node type is used to recording the source
	 * range where a comment was found in the source string.
	 * These comment nodes are normally found (only) in
	 * {@linkplain CompilationUnit#getCommentList()
	 * the comment table} for parsed compilation units.
	 * </p>
	 *
	 * @return a new unparented block comment node
	 * @since 3.0
	 */
	public BlockComment newBlockComment() {
		BlockComment result = new BlockComment(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented boolean literal node.
	 * <p>
	 * For example, the assignment expression <code>foo = true</code>
	 * is generated by the following snippet:
	 * <code>
	 * <pre>
	 * Assignment e= ast.newAssignment();
	 * e.setLeftHandSide(ast.newSimpleName("foo"));
	 * e.setRightHandSide(ast.newBooleanLiteral(true));
	 * </pre>
	 * </code>
	 * </p>
	 *
	 * @param value the boolean value
	 * @return a new unparented boolean literal node
	 */
	public BooleanLiteral newBooleanLiteral(boolean value) {
		BooleanLiteral result = new BooleanLiteral(this);
		result.setBooleanValue(value);
		return result;
	}

	/**
	 * Creates an unparented break statement node owned by this AST.
	 * The break statement has no label.
	 *
	 * @return a new unparented break statement node
	 */
	public BreakStatement newBreakStatement() {
		return new BreakStatement(this);
	}

	/**
	 * Creates and returns a new unparented cast expression node
	 * owned by this AST. By default, the type and expression are unspecified
	 * (but legal).
	 *
	 * @return a new unparented cast expression node
	 */
	public CastExpression newCastExpression() {
		CastExpression result = new CastExpression(this);
		return result;
	}

	/**
	 * Creates a new unparented catch clause node owned by this AST.
	 * By default, the catch clause declares an unspecified, but legal,
	 * exception declaration and has an empty block.
	 *
	 * @return a new unparented catch clause node
	 */
	public CatchClause newCatchClause() {
		return new CatchClause(this);
	}

	/**
	 * Creates and returns a new unparented character literal node.
	 * Initially the node has an unspecified character literal.
	 *
	 * @return a new unparented character literal node
	 */
	public CharacterLiteral newCharacterLiteral() {
		return new CharacterLiteral(this);
	}

	/**
	 * Creates and returns a new unparented class instance creation
	 * ("new") expression node owned by this AST. By default,
	 * there is no qualifying expression, no type parameters,
	 * an unspecified (but legal) type name, an empty list of
	 * arguments, and does not declare an anonymous class declaration.
	 *
	 * @return a new unparented class instance creation expression node
	 */
	public ClassInstanceCreation newClassInstanceCreation() {
		ClassInstanceCreation result = new ClassInstanceCreation(this);
		return result;
	}

	//=============================== DECLARATIONS ===========================
	/**
	 * Creates an unparented compilation unit node owned by this AST.
	 * The compilation unit initially has no package declaration, no
	 * import declarations, and no type declarations.
	 *
	 * @return the new unparented compilation unit node
	 */
	public CompilationUnit newCompilationUnit() {
		return new CompilationUnit(this);
	}

	/**
	 * Creates and returns a new unparented conditional expression node
	 * owned by this AST. By default, the condition and both expressions
	 * are unspecified (but legal).
	 *
	 * @return a new unparented array conditional expression node
	 */
	public ConditionalExpression newConditionalExpression() {
		ConditionalExpression result = new ConditionalExpression(this);
		return result;
	}

	/**
	 * Creates an unparented alternate constructor ("this(...);") invocation
	 * statement node owned by this AST. By default, the lists of arguments
	 * and type arguments are both empty.
	 * <p>
	 * Note that this type of node is a Statement, whereas a regular
	 * method invocation is an Expression. The only valid use of these
	 * statements are as the first statement of a constructor body.
	 * </p>
	 *
	 * @return a new unparented alternate constructor invocation statement node
	 */
	public ConstructorInvocation newConstructorInvocation() {
		ConstructorInvocation result = new ConstructorInvocation(this);
		return result;
	}

	/**
	 * Creates an unparented continue statement node owned by this AST.
	 * The continue statement has no label.
	 *
	 * @return a new unparented continue statement node
	 */
	public ContinueStatement newContinueStatement() {
		return new ContinueStatement(this);
	}

	/**
	 * Creates an unparented creation reference node owned by this AST.
	 * By default, the type is unspecified (but legal), and there are no type arguments.
	 * 
	 * @return a new unparented creation reference expression node
	 * @exception UnsupportedOperationException if this operation is used in a JLS2, JLS3 or JLS4 AST
	 * @since 3.10
	 */
	public CreationReference newCreationReference() {
		CreationReference result = new CreationReference(this);
		return result;
	}

	/**
	 * Creates a new unparented do statement node owned by this AST.
	 * By default, the expression is unspecified (but legal), and
	 * the body statement is an empty block.
	 *
	 * @return a new unparented do statement node
	 */
	public DoStatement newDoStatement() {
		return new DoStatement(this);
	}

	/**
	 * Creates a new unparented empty statement node owned by this AST.
	 *
	 * @return a new unparented empty statement node
	 */
	public EmptyStatement newEmptyStatement() {
		return new EmptyStatement(this);
	}

	/**
	 * Creates a new unparented enhanced for statement node owned by this AST.
	 * By default, the paramter and expression are unspecified
	 * but legal subtrees, and the body is an empty block.
	 *
	 * @return a new unparented throw statement node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public EnhancedForStatement newEnhancedForStatement() {
		return new EnhancedForStatement(this);
	}

	/**
	 * Creates an unparented enum constant declaration node owned by this AST.
	 * The name of the constant is an unspecified, but legal, name;
	 * no doc comment; no modifiers or annotations; no arguments;
	 * and does not declare an anonymous class.
	 *
	 * @return a new unparented enum constant declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public EnumConstantDeclaration newEnumConstantDeclaration() {
		EnumConstantDeclaration result = new EnumConstantDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented enum declaration node owned by this AST.
	 * The name of the enum is an unspecified, but legal, name;
	 * no doc comment; no modifiers or annotations;
	 * no superinterfaces; and empty lists of enum constants
	 * and body declarations.
	 *
	 * @return a new unparented enum declaration node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public EnumDeclaration newEnumDeclaration() {
		EnumDeclaration result = new EnumDeclaration(this);
		return result;
	}

	/**
	 * Creates an unparented expression method reference node owned by this AST.
	 * By default, the expression and method name are unspecified (but legal),
	 * and there are no type arguments.
	 * 
	 * @return a new unparented expression method reference expression node
	 * @exception UnsupportedOperationException if this operation is used in a JLS2, JLS3 or JLS4 AST
	 * @since 3.10
	 */
	public ExpressionMethodReference newExpressionMethodReference() {
		ExpressionMethodReference result = new ExpressionMethodReference(this);
		return result;
	}

	/**
	 * Creates a new unparented expression statement node owned by this AST,
	 * for the given expression.
	 * <p>
	 * This method can be used to convert an expression
	 * (<code>Expression</code>) into a statement (<code>Type</code>)
	 * by wrapping it. Note, however, that the result is only legal for
	 * limited expression types, including method invocations, assignments,
	 * and increment/decrement operations.
	 * </p>
	 *
	 * @param expression the expression
	 * @return a new unparented statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public ExpressionStatement newExpressionStatement(Expression expression) {
		ExpressionStatement result = new ExpressionStatement(this);
		result.setExpression(expression);
		return result;
	}

	/**
	 * Creates and returns a new unparented annotatable dimension node
	 * (Supported only in JLS8 level).
	 *
	 * @return a new unparented annotatable dimension node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used
	 *            in a JLS2, JLS3 or JLS4 AST
	 * @since 3.10
	 */
	public Dimension newDimension() {
		Dimension result = new Dimension(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented field access expression node
	 * owned by this AST. By default, the expression and field are both
	 * unspecified, but legal, names.
	 *
	 * @return a new unparented field access expression node
	 */
	public FieldAccess newFieldAccess() {
		FieldAccess result = new FieldAccess(this);
		return result;
	}

	/**
	 * Creates a new unparented field declaration node owned by this AST,
	 * for the given variable declaration fragment. By default, there are no
	 * modifiers, no doc comment, and the base type is unspecified
	 * (but legal).
	 * <p>
	 * This method can be used to wrap a variable declaration fragment
	 * (<code>VariableDeclarationFragment</code>) into a field declaration
	 * suitable for inclusion in the body of a type declaration
	 * (<code>FieldDeclaration</code> implements <code>BodyDeclaration</code>).
	 * Additional variable declaration fragments can be added afterwards.
	 * </p>
	 *
	 * @param fragment the variable declaration fragment
	 * @return a new unparented field declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>the given fragment is null</li>
	 * </ul>
	 */
	public FieldDeclaration newFieldDeclaration(VariableDeclarationFragment fragment) {
		if (fragment == null) {
			throw new IllegalArgumentException();
		}
		FieldDeclaration result = new FieldDeclaration(this);
		result.fragments().add(fragment);
		return result;
	}

	/**
	 * Creates a new unparented for statement node owned by this AST.
	 * By default, there are no initializers, no condition expression,
	 * no updaters, and the body is an empty block.
	 *
	 * @return a new unparented for statement node
	 */
	public ForStatement newForStatement() {
		return new ForStatement(this);
	}

	/**
	 * Creates a new unparented if statement node owned by this AST.
	 * By default, the expression is unspecified (but legal),
	 * the then statement is an empty block, and there is no else statement.
	 *
	 * @return a new unparented if statement node
	 */
	public IfStatement newIfStatement() {
		return new IfStatement(this);
	}

	/**
	 * Creates an unparented import declaration node owned by this AST.
	 * The import declaration initially contains a single-type import
	 * of a type with an unspecified name.
	 *
	 * @return the new unparented import declaration node
	 */
	public ImportDeclaration newImportDeclaration() {
		ImportDeclaration result = new ImportDeclaration(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented infix expression node
	 * owned by this AST. By default, the operator and left and right
	 * operand are unspecified (but legal), and there are no extended
	 * operands.
	 *
	 * @return a new unparented infix expression node
	 */
	public InfixExpression newInfixExpression() {
		InfixExpression result = new InfixExpression(this);
		return result;
	}

	/**
	 * Creates an unparented initializer node owned by this AST, with an
	 * empty block. By default, the initializer has no modifiers and
	 * an empty block.
	 *
	 * @return a new unparented initializer node
	 */
	public Initializer newInitializer() {
		Initializer result = new Initializer(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented instanceof expression node
	 * owned by this AST. By default, the operator and left and right
	 * operand are unspecified (but legal).
	 *
	 * @return a new unparented instanceof expression node
	 */
	public InstanceofExpression newInstanceofExpression() {
		InstanceofExpression result = new InstanceofExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new doc comment node.
	 * Initially the new node has an empty list of tag elements
	 * (and, for backwards compatability, an unspecified, but legal,
	 * doc comment string)
	 *
	 * @return a new unparented doc comment node
	 */
	public Javadoc newJavadoc() {
		Javadoc result = new Javadoc(this);
		return result;
	}

	/**
	 * Creates a new unparented labeled statement node owned by this AST.
	 * By default, the label and statement are both unspecified, but legal.
	 *
	 * @return a new unparented labeled statement node
	 */
	public LabeledStatement newLabeledStatement() {
		return new LabeledStatement(this);
	}

	/**
	 * Creates an unparented lambda expression node owned by this AST.
	 * By default, the new lambda expression has parentheses enabled, contains an empty argument
	 * list, and the body is an empty block.
	 * 
	 * @return a new unparented lambda expression node
	 * @exception UnsupportedOperationException if this operation is used in a JLS2, JLS3 or JLS4 AST
	 * @since 3.10
	 */
	public LambdaExpression newLambdaExpression() {
		LambdaExpression result = new LambdaExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new line comment placeholder node.
	 * <p>
	 * Note that this node type is used to recording the source
	 * range where a comment was found in the source string.
	 * These comment nodes are normally found (only) in
	 * {@linkplain CompilationUnit#getCommentList()
	 * the comment table} for parsed compilation units.
	 * </p>
	 *
	 * @return a new unparented line comment node
	 * @since 3.0
	 */
	public LineComment newLineComment() {
		LineComment result = new LineComment(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented marker annotation node with
	 * an unspecified type name.
	 *
	 * @return a new unparented marker annotation node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public MarkerAnnotation newMarkerAnnotation() {
		MarkerAnnotation result = new MarkerAnnotation(this);
		return result;
	}

	/**
	 * Creates and returns a new member reference node.
	 * Initially the new node has no qualifier name and
	 * an unspecified, but legal, member name.
	 * <p>
	 * Note that this node type is used only inside doc comments
	 * ({@link Javadoc}).
	 * </p>
	 *
	 * @return a new unparented member reference node
	 * @since 3.0
	 */
	public MemberRef newMemberRef() {
		MemberRef result = new MemberRef(this);
		return result;
	}

	//=============================== COMMENTS ===========================

	/**
	 * Creates and returns a new unparented member value pair node with
	 * an unspecified member name and value.
	 *
	 * @return a new unparented member value pair node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public MemberValuePair newMemberValuePair() {
		MemberValuePair result = new MemberValuePair(this);
		return result;
	}

	/**
	 * Creates an unparented method declaration node owned by this AST.
	 * By default, the declaration is for a method of an unspecified, but
	 * legal, name; no modifiers; no doc comment; no parameters; return
	 * type void; no extra array dimensions; no thrown exceptions; and no
	 * body (as opposed to an empty body).
	 * <p>
	 * To create a constructor, use this method and then call
	 * <code>MethodDeclaration.setConstructor(true)</code> and
	 * <code>MethodDeclaration.setName(className)</code>.
	 * </p>
	 *
	 * @return a new unparented method declaration node
	 */
	public MethodDeclaration newMethodDeclaration() {
		MethodDeclaration result = new MethodDeclaration(this);
		result.setConstructor(false);
		return result;
	}

	/**
	 * Creates an unparented method invocation expression node owned by this
	 * AST. By default, the name of the method is unspecified (but legal)
	 * there is no receiver expression, no type arguments, and the list of
	 * arguments is empty.
	 *
	 * @return a new unparented method invocation expression node
	 */
	public MethodInvocation newMethodInvocation() {
		MethodInvocation result = new MethodInvocation(this);
		return result;
	}

	/**
	 * Creates and returns a new method reference node.
	 * Initially the new node has no qualifier name,
	 * an unspecified, but legal, method name, and an
	 * empty parameter list.
	 * <p>
	 * Note that this node type is used only inside doc comments
	 * ({@link Javadoc Javadoc}).
	 * </p>
	 *
	 * @return a new unparented method reference node
	 * @since 3.0
	 */
	public MethodRef newMethodRef() {
		MethodRef result = new MethodRef(this);
		return result;
	}

	/**
	 * Creates and returns a new method reference node.
	 * Initially the new node has an unspecified, but legal,
	 * type, not variable arity, and no parameter name.
	 * <p>
	 * Note that this node type is used only inside doc comments
	 * ({@link Javadoc}).
	 * </p>
	 *
	 * @return a new unparented method reference parameter node
	 * @since 3.0
	 */
	public MethodRefParameter newMethodRefParameter() {
		MethodRefParameter result = new MethodRefParameter(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented modifier node for the given
	 * modifier.
	 *
	 * @param keyword one of the modifier keyword constants
	 * @return a new unparented modifier node
	 * @exception IllegalArgumentException if the primitive type code is invalid
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public Modifier newModifier(Modifier.ModifierKeyword keyword) {
		Modifier result = new Modifier(this);
		result.setKeyword(keyword);
		return result;
	}

	/**
	 * Creates and returns a list of new unparented modifier nodes
	 * for the given modifier flags. When multiple modifiers are
	 * requested, the modifier nodes will appear in the following order:
	 * <pre> public protected private
	 * abstract default static final synchronized native strictfp transient volatile</pre>
	 * <p>
	 * This order is consistent with the recommendations in JLS8 ("*Modifier:" rules in chapters 8 and 9).
	 * </p>
	 *
	 * @param flags bitwise or of modifier flags declared on {@link Modifier}
	 * @return a possibly empty list of new unparented modifier nodes
	 *   (element type <code>Modifier</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public List newModifiers(int flags) {
		if (this.apiLevel == AST.JLS2) {
			unsupportedIn2();
		}
		List result = new ArrayList(3); // 3 modifiers is more than average
		if (Modifier.isPublic(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		}
		if (Modifier.isProtected(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD));
		}
		if (Modifier.isPrivate(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		}
		if (Modifier.isAbstract(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD));
		}
		if (Modifier.isDefault(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.DEFAULT_KEYWORD));
		}
		if (Modifier.isStatic(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
		}
		if (Modifier.isFinal(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
		}
		if (Modifier.isSynchronized(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD));
		}
		if (Modifier.isNative(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD));
		}
		if (Modifier.isStrictfp(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD));
		}
		if (Modifier.isTransient(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD));
		}
		if (Modifier.isVolatile(flags)) {
			result.add(newModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD));
		}
		return result;
	}

	/**
	 * Creates and returns a new unparented name node for the given name.
	 * The name string must consist of 1 or more name segments separated
	 * by single dots '.'. Returns a {@link QualifiedName} if the name has
	 * dots, and a {@link SimpleName} otherwise. Each of the name
	 * segments should be legal Java identifiers (this constraint may or may
	 * not be enforced), and there must be at least one name segment.
	 * The string must not contains white space, '&lt;', '&gt;',
	 * '[', ']', or other any other characters that are not
	 * part of the Java identifiers or separating '.'s.
	 *
	 * @param qualifiedName string consisting of 1 or more name segments,
	 * each of which is a legal Java identifier, separated  by single dots '.'
	 * @return a new unparented name node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the string is empty</li>
	 * <li>the string begins or ends in a '.'</li>
	 * <li>the string has adjacent '.'s</li>
	 * <li>the segments between the '.'s are not valid Java identifiers</li>
	 * </ul>
	 * @since 3.1
	 */
	public Name newName(String qualifiedName) {
		StringTokenizer t = new StringTokenizer(qualifiedName, ".", true); //$NON-NLS-1$
		Name result = null;
		// balance is # of name tokens - # of period tokens seen so far
		// initially 0; finally 1; should never drop < 0 or > 1
		int balance = 0;
		while(t.hasMoreTokens()) {
			String s = t.nextToken();
			if (s.indexOf('.') >= 0) {
				// this is a delimiter
				if (s.length() > 1) {
					// too many dots in a row
					throw new IllegalArgumentException();
				}
				balance--;
				if (balance < 0) {
					throw new IllegalArgumentException();
				}
			} else {
				// this is an identifier segment
				balance++;
				SimpleName name = newSimpleName(s);
				if (result == null) {
					result = name;
				} else {
					result = newQualifiedName(result, name);
				}
			}
		}
		if (balance != 1) {
			throw new IllegalArgumentException();
		}
		return result;
	}

	/**
	 * Creates and returns a new unparented name node for the given name
	 * segments. Returns a simple name if there is only one name segment, and
	 * a qualified name if there are multiple name segments. Each of the name
	 * segments should be legal Java identifiers (this constraint may or may
	 * not be enforced), and there must be at least one name segment.
	 *
	 * @param identifiers a list of 1 or more name segments, each of which
	 *    is a legal Java identifier
	 * @return a new unparented name node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the identifier is invalid</li>
	 * <li>the list of identifiers is empty</li>
	 * </ul>
	 */
	public Name newName(String[] identifiers) {
		// update internalSetName(String[] if changed
		int count = identifiers.length;
		if (count == 0) {
			throw new IllegalArgumentException();
		}
		Name result = newSimpleName(identifiers[0]);
		for (int i = 1; i < count; i++) {
			SimpleName name = newSimpleName(identifiers[i]);
			result = newQualifiedName(result, name);
		}
		return result;
	}

	/**
	 * Creates and returns a new unparented name qualified type node with
	 * the given qualifier and name.
	 *
	 * @param qualifier the name qualifier name node
	 * @param name the simple name being qualified
	 * @return a new unparented qualified type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2, JLS3 and JLS4 AST
	 * @since 3.10
	 */
	public NameQualifiedType newNameQualifiedType(Name qualifier, SimpleName name) {
		NameQualifiedType result = new NameQualifiedType(this);
		result.setQualifier(qualifier);
		result.setName(name);
		return result;
	}

	/**
	 * Creates and returns a new unparented normal annotation node with
	 * an unspecified type name and an empty list of member value
	 * pairs.
	 *
	 * @return a new unparented normal annotation node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public NormalAnnotation newNormalAnnotation() {
		NormalAnnotation result = new NormalAnnotation(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented null literal node.
	 *
	 * @return a new unparented null literal node
	 */
	public NullLiteral newNullLiteral() {
		return new NullLiteral(this);
	}

	/**
	 * Creates and returns a new unparented number literal node.
	 * Initially the number literal token is <code>"0"</code>.
	 *
	 * @return a new unparented number literal node
	 */
	public NumberLiteral newNumberLiteral() {
		NumberLiteral result = new NumberLiteral(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented number literal node.
	 *
	 * @param literal the token for the numeric literal as it would
	 *    appear in Java source code
	 * @return a new unparented number literal node
	 * @exception IllegalArgumentException if the literal is null
	 */
	public NumberLiteral newNumberLiteral(String literal) {
		if (literal == null) {
			throw new IllegalArgumentException();
		}
		NumberLiteral result = new NumberLiteral(this);
		result.setToken(literal);
		return result;
	}

	/**
	 * Creates an unparented package declaration node owned by this AST.
	 * The package declaration initially declares a package with an
	 * unspecified name.
	 *
	 * @return the new unparented package declaration node
	 */
	public PackageDeclaration newPackageDeclaration() {
		PackageDeclaration result = new PackageDeclaration(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented parameterized type node with the
	 * given type and an empty list of type arguments.
	 *
	 * @param type the type that is parameterized
	 * @return a new unparented parameterized type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public ParameterizedType newParameterizedType(Type type) {
		ParameterizedType result = new ParameterizedType(this);
		result.setType(type);
		return result;
	}

	/**
	 * Creates and returns a new unparented parenthesized expression node
	 * owned by this AST. By default, the expression is unspecified (but legal).
	 *
	 * @return a new unparented parenthesized expression node
	 */
	public ParenthesizedExpression newParenthesizedExpression() {
		ParenthesizedExpression result = new ParenthesizedExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented postfix expression node
	 * owned by this AST. By default, the operator and operand are
	 * unspecified (but legal).
	 *
	 * @return a new unparented postfix expression node
	 */
	public PostfixExpression newPostfixExpression() {
		PostfixExpression result = new PostfixExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented prefix expression node
	 * owned by this AST. By default, the operator and operand are
	 * unspecified (but legal).
	 *
	 * @return a new unparented prefix expression node
	 */
	public PrefixExpression newPrefixExpression() {
		PrefixExpression result = new PrefixExpression(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented primitive type node with the given
	 * type code.
	 *
	 * @param typeCode one of the primitive type code constants declared in
	 *    <code>PrimitiveType</code>
	 * @return a new unparented primitive type node
	 * @exception IllegalArgumentException if the primitive type code is invalid
	 */
	public PrimitiveType newPrimitiveType(PrimitiveType.Code typeCode) {
		PrimitiveType result = new PrimitiveType(this);
		result.setPrimitiveTypeCode(typeCode);
		return result;
	}

	/**
	 * Creates and returns a new unparented qualified name node for the given
	 * qualifier and simple name child node.
	 *
	 * @param qualifier the qualifier name node
	 * @param name the simple name being qualified
	 * @return a new unparented qualified name node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public QualifiedName newQualifiedName(
		Name qualifier,
		SimpleName name) {
		QualifiedName result = new QualifiedName(this);
		result.setQualifier(qualifier);
		result.setName(name);
		return result;

	}

	/**
	 * Creates and returns a new unparented qualified type node with
	 * the given qualifier type and name.
	 *
	 * @param qualifier the qualifier type node
	 * @param name the simple name being qualified
	 * @return a new unparented qualified type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public QualifiedType newQualifiedType(Type qualifier, SimpleName name) {
		QualifiedType result = new QualifiedType(this);
		result.setQualifier(qualifier);
		result.setName(name);
		return result;
	}

	/**
	 * Creates a new unparented return statement node owned by this AST.
	 * By default, the return statement has no expression.
	 *
	 * @return a new unparented return statement node
	 */
	public ReturnStatement newReturnStatement() {
		return new ReturnStatement(this);
	}

	//=============================== NAMES ===========================
	/**
	 * Creates and returns a new unparented simple name node for the given
	 * identifier. The identifier should be a legal Java identifier, but not
	 * a keyword, boolean literal ("true", "false") or null literal ("null").
	 *
	 * @param identifier the identifier
	 * @return a new unparented simple name node
	 * @exception IllegalArgumentException if the identifier is invalid
	 */
	public SimpleName newSimpleName(String identifier) {
		if (identifier == null) {
			throw new IllegalArgumentException();
		}
		SimpleName result = new SimpleName(this);
		result.setIdentifier(identifier);
		return result;
	}

	//=============================== TYPES ===========================
	/**
	 * Creates and returns a new unparented simple type node with the given
	 * type name.
	 * <p>
	 * This method can be used to convert a name (<code>Name</code>) into a
	 * type (<code>Type</code>) by wrapping it.
	 * </p>
	 *
	 * @param typeName the name of the class or interface
	 * @return a new unparented simple type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public SimpleType newSimpleType(Name typeName) {
		SimpleType result = new SimpleType(this);
		result.setName(typeName);
		return result;
	}

	/**
	 * Creates and returns a new unparented single member annotation node with
	 * an unspecified type name and value.
	 *
	 * @return a new unparented single member annotation node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public SingleMemberAnnotation newSingleMemberAnnotation() {
		SingleMemberAnnotation result = new SingleMemberAnnotation(this);
		return result;
	}

	/**
	 * Creates an unparented single variable declaration node owned by this AST.
	 * By default, the declaration is for a variable with an unspecified, but
	 * legal, name and type; no modifiers; no array dimensions after the
	 * variable; no initializer; not variable arity.
	 *
	 * @return a new unparented single variable declaration node
	 */
	public SingleVariableDeclaration newSingleVariableDeclaration() {
		SingleVariableDeclaration result = new SingleVariableDeclaration(this);
		return result;
	}

	//=============================== EXPRESSIONS ===========================
	/**
	 * Creates and returns a new unparented string literal node for
	 * the empty string literal.
	 *
	 * @return a new unparented string literal node
	 */
	public StringLiteral newStringLiteral() {
		return new StringLiteral(this);
	}

	/**
	 * Creates an unparented alternate super constructor ("super(...);")
	 * invocation statement node owned by this AST. By default, there is no
	 * qualifier, no type arguments, and the list of arguments is empty.
	 * <p>
	 * Note that this type of node is a Statement, whereas a regular
	 * super method invocation is an Expression. The only valid use of these
	 * statements are as the first statement of a constructor body.
	 * </p>
	 *
	 * @return a new unparented super constructor invocation statement node
	 */
	public SuperConstructorInvocation newSuperConstructorInvocation() {
		SuperConstructorInvocation result =
			new SuperConstructorInvocation(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented super field access expression node
	 * owned by this AST. By default, the expression and field are both
	 * unspecified, but legal, names.
	 *
	 * @return a new unparented super field access expression node
	 */
	public SuperFieldAccess newSuperFieldAccess() {
		SuperFieldAccess result = new SuperFieldAccess(this);
		return result;
	}

	/**
	 * Creates an unparented "super" method invocation expression node owned by
	 * this AST. By default, the name of the method is unspecified (but legal),
	 * there is no qualifier, no type arguments, and the list of arguments is empty.
	 *
	 * @return a new unparented  "super" method invocation
	 *    expression node
	 */
	public SuperMethodInvocation newSuperMethodInvocation() {
		SuperMethodInvocation result = new SuperMethodInvocation(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented super method reference node owned by
	 * this AST. By default, the name of the method is unspecified (but legal),
	 * and there is no qualifier and no type arguments.
	 *
	 * @return a new unparented super method reference node
	 * @since 3.10
	 */
	public SuperMethodReference newSuperMethodReference() {
		SuperMethodReference result = new SuperMethodReference(this);
		return result;
	}

	/**
	 * Creates a new unparented switch case statement node owned by
	 * this AST. By default, the expression is unspecified, but legal.
	 *
	 * @return a new unparented switch case node
	 */
	public SwitchCase newSwitchCase() {
		return new SwitchCase(this);
	}


	/**
	 * Creates a new unparented switch statement node owned by this AST.
	 * By default, the expression is unspecified, but legal, and there are
	 * no statements or switch cases.
	 *
	 * @return a new unparented labeled statement node
	 */
	public SwitchStatement newSwitchStatement() {
		return new SwitchStatement(this);
	}

	/**
	 * Creates a new unparented synchronized statement node owned by this AST.
	 * By default, the expression is unspecified, but legal, and the body is
	 * an empty block.
	 *
	 * @return a new unparented synchronized statement node
	 */
	public SynchronizedStatement newSynchronizedStatement() {
		return new SynchronizedStatement(this);
	}

	/**
	 * Creates and returns a new tag element node.
	 * Initially the new node has no tag name and an empty list of fragments.
	 * <p>
	 * Note that this node type is used only inside doc comments
	 * ({@link Javadoc}).
	 * </p>
	 *
	 * @return a new unparented tag element node
	 * @since 3.0
	 */
	public TagElement newTagElement() {
		TagElement result = new TagElement(this);
		return result;
	}

	/**
	 * Creates and returns a new text element node.
	 * Initially the new node has an empty text string.
	 * <p>
	 * Note that this node type is used only inside doc comments
	 * ({@link Javadoc Javadoc}).
	 * </p>
	 *
	 * @return a new unparented text element node
	 * @since 3.0
	 */
	public TextElement newTextElement() {
		TextElement result = new TextElement(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented "this" expression node
	 * owned by this AST. By default, there is no qualifier.
	 *
	 * @return a new unparented "this" expression node
	 */
	public ThisExpression newThisExpression() {
		ThisExpression result = new ThisExpression(this);
		return result;
	}

	/**
	 * Creates a new unparented throw statement node owned by this AST.
	 * By default, the expression is unspecified, but legal.
	 *
	 * @return a new unparented throw statement node
	 */
	public ThrowStatement newThrowStatement() {
		return new ThrowStatement(this);
	}

	/**
	 * Creates a new unparented try statement node owned by this AST.
	 * By default, the try statement has no resources, an empty block, no catch
	 * clauses, and no finally block.
	 *
	 * @return a new unparented try statement node
	 */
	public TryStatement newTryStatement() {
		return new TryStatement(this);
	}

	/**
	 * Creates an unparented class declaration node owned by this AST.
	 * The name of the class is an unspecified, but legal, name;
	 * no modifiers; no doc comment; no superclass or superinterfaces;
	 * and an empty class body.
	 * <p>
	 * To create an interface, use this method and then call
	 * <code>TypeDeclaration.setInterface(true)</code>.
	 * </p>
	 *
	 * @return a new unparented type declaration node
	 */
	public TypeDeclaration newTypeDeclaration() {
		TypeDeclaration result = new TypeDeclaration(this);
		result.setInterface(false);
		return result;
	}

	/**
	 * Creates a new unparented local type declaration statement node
	 * owned by this AST, for the given type declaration.
	 * <p>
	 * This method can be used to convert any kind of type declaration
	 * (<code>AbstractTypeDeclaration</code>) into a statement
	 * (<code>Statement</code>) by wrapping it.
	 * </p>
	 *
	 * @param decl the type declaration
	 * @return a new unparented local type declaration statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @since 3.0
	 */
	public TypeDeclarationStatement
			newTypeDeclarationStatement(AbstractTypeDeclaration decl) {
		TypeDeclarationStatement result = new TypeDeclarationStatement(this);
		if (this.apiLevel == AST.JLS2) {
			result.internalSetTypeDeclaration((TypeDeclaration) decl);
		}
		if (this.apiLevel >= AST.JLS3) {
			result.setDeclaration(decl);
		}
		return result;
	}

	/**
	 * Creates a new unparented local type declaration statement node
	 * owned by this AST, for the given type declaration.
	 * <p>
	 * This method can be used to convert a type declaration
	 * (<code>TypeDeclaration</code>) into a statement
	 * (<code>Statement</code>) by wrapping it.
	 * </p>
	 *
	 * @param decl the type declaration
	 * @return a new unparented local type declaration statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public TypeDeclarationStatement
			newTypeDeclarationStatement(TypeDeclaration decl) {
		TypeDeclarationStatement result = new TypeDeclarationStatement(this);
		result.setDeclaration(decl);
		return result;
	}

	/**
	 * Creates and returns a new unparented type literal expression node
	 * owned by this AST. By default, the type is unspecified (but legal).
	 *
	 * @return a new unparented type literal node
	 */
	public TypeLiteral newTypeLiteral() {
		TypeLiteral result = new TypeLiteral(this);
		return result;
	}

	/**
	 * Creates an unparented type method reference node owned by this AST.
	 * By default, the type and method name are unspecified (but legal),
	 * and there are no type arguments.
	 * 
	 * @return a new unparented type method reference node
	 * @exception UnsupportedOperationException if this operation is used in a JLS2, JLS3 or JLS4 AST
	 * @since 3.10
	 */
	public TypeMethodReference newTypeMethodReference() {
		TypeMethodReference result = new TypeMethodReference(this);
		return result;
	}

	/**
	 * Creates and returns a new unparented type parameter type node with an
	 * unspecified type variable name and an empty list of type bounds.
	 *
	 * @return a new unparented type parameter node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public TypeParameter newTypeParameter() {
		TypeParameter result = new TypeParameter(this);
		return result;
	}

	/**
	 * Creates a new unparented union type node owned by this AST.
	 * By default, the union type has no types.
	 *
	 * @return a new unparented UnionType node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 or JLS3 AST
	 * @since 3.7.1
	 */
	public UnionType newUnionType() {
		return new UnionType(this);
	}

	/**
	 * Creates a new unparented intersection type node owned by this AST.
	 * By default, the intersection type has no types.
	 *
	 * @return a new unparented IntersectionType node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2, JLS3 or JLS4 AST
	 * @since 3.10
	 */
	public IntersectionType newIntersectionType() {
		return new IntersectionType(this);
	}

	/**
	 * Creates a new unparented local variable declaration expression node
	 * owned by this AST, for the given variable declaration fragment. By
	 * default, there are no modifiers and the base type is unspecified
	 * (but legal).
	 * <p>
	 * This method can be used to convert a variable declaration fragment
	 * (<code>VariableDeclarationFragment</code>) into an expression
	 * (<code>Expression</code>) by wrapping it. Additional variable
	 * declaration fragments can be added afterwards.
	 * </p>
	 *
	 * @param fragment the first variable declaration fragment
	 * @return a new unparented variable declaration expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>the given fragment is null</li>
	 * </ul>
	 */
	public VariableDeclarationExpression
			newVariableDeclarationExpression(VariableDeclarationFragment fragment) {
		if (fragment == null) {
			throw new IllegalArgumentException();
		}
		VariableDeclarationExpression result =
			new VariableDeclarationExpression(this);
		result.fragments().add(fragment);
		return result;
	}

	/**
	 * Creates an unparented variable declaration fragment node owned by this
	 * AST. By default, the fragment is for a variable with an unspecified, but
	 * legal, name; no extra array dimensions; and no initializer.
	 *
	 * @return a new unparented variable declaration fragment node
	 */
	public VariableDeclarationFragment newVariableDeclarationFragment() {
		VariableDeclarationFragment result = new VariableDeclarationFragment(this);
		return result;
	}

	//=============================== STATEMENTS ===========================
	/**
	 * Creates a new unparented local variable declaration statement node
	 * owned by this AST, for the given variable declaration fragment.
	 * By default, there are no modifiers and the base type is unspecified
	 * (but legal).
	 * <p>
	 * This method can be used to convert a variable declaration fragment
	 * (<code>VariableDeclarationFragment</code>) into a statement
	 * (<code>Statement</code>) by wrapping it. Additional variable
	 * declaration fragments can be added afterwards.
	 * </p>
	 *
	 * @param fragment the variable declaration fragment
	 * @return a new unparented variable declaration statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>the variable declaration fragment is null</li>
	 * </ul>
	 */
	public VariableDeclarationStatement
			newVariableDeclarationStatement(VariableDeclarationFragment fragment) {
		if (fragment == null) {
			throw new IllegalArgumentException();
		}
		VariableDeclarationStatement result =
			new VariableDeclarationStatement(this);
		result.fragments().add(fragment);
		return result;
	}

	/**
	 * Creates a new unparented while statement node owned by this AST.
	 * By default, the expression is unspecified (but legal), and
	 * the body statement is an empty block.
	 *
	 * @return a new unparented while statement node
	 */
	public WhileStatement newWhileStatement() {
		return new WhileStatement(this);
	}

	/**
	 * Creates and returns a new unparented wildcard type node with no
	 * type bound.
	 *
	 * @return a new unparented wildcard type node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public WildcardType newWildcardType() {
		WildcardType result = new WildcardType(this);
		return result;
	}

	/**
	 * Reports that the given node has just gained a child.
	 *
	 * @param node the node that was modified
	 * @param child the node that was added as a child
	 * @param property the child or child list property descriptor
	 * @since 3.0
	 */
	void postAddChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
		// IMPORTANT: this method is called by readers during lazy init
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE ADD]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.postAddChildEvent(node, child, property);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has already been changed
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node has just been cloned.
	 *
	 * @param node the node that was cloned
	 * @param clone the clone of <code>node</code>
	 * @since 3.0
	 */
	void postCloneNodeEvent(ASTNode node, ASTNode clone) {
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE CLONE]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.postCloneNodeEvent(node, clone);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has already been changed
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node jsut lost a child.
	 *
	 * @param node the node that was modified
	 * @param child the child node that was removed
	 * @param property the child or child list property descriptor
	 * @since 3.0
	 */
	void postRemoveChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
		// IMPORTANT: this method is called by readers during lazy init
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE DEL]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.postRemoveChildEvent(node, child, property);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has not been changed yet
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node has just had a child replaced.
	 *
	 * @param node the node modified
	 * @param child the child removed
	 * @param newChild the replacement child
	 * @param property the child or child list property descriptor
	 * @since 3.0
	 */
	void postReplaceChildEvent(ASTNode node, ASTNode child, ASTNode newChild, StructuralPropertyDescriptor property) {
		// IMPORTANT: this method is called by readers during lazy init
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE REP]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.postReplaceChildEvent(node, child, newChild, property);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has not been changed yet
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node has just changed the value of a
	 * non-child property.
	 *
	 * @param node the node that was modified
	 * @param property the property descriptor
	 * @since 3.0
	 */
	void postValueChangeEvent(ASTNode node, SimplePropertyDescriptor property) {
		// IMPORTANT: this method is called by readers during lazy init
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE CHANGE]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.postValueChangeEvent(node, property);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has already been changed
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node is about to gain a child.
	 *
	 * @param node the node that to be modified
	 * @param child the node that to be added as a child
	 * @param property the child or child list property descriptor
	 * @since 3.0
	 */
	void preAddChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
		// IMPORTANT: this method is called by readers during lazy init
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE ADD]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.preAddChildEvent(node, child, property);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has already been changed
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node is about to be cloned.
	 *
	 * @param node the node to be cloned
	 * @since 3.0
	 */
	void preCloneNodeEvent(ASTNode node) {
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE CLONE]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.preCloneNodeEvent(node);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has already been changed
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node is about to lose a child.
	 *
	 * @param node the node about to be modified
	 * @param child the node about to be removed
	 * @param property the child or child list property descriptor
	 * @since 3.0
	 */
	void preRemoveChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
		// IMPORTANT: this method is called by readers during lazy init
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE DEL]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.preRemoveChildEvent(node, child, property);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has not been changed yet
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node is about have a child replaced.
	 *
	 * @param node the node about to be modified
	 * @param child the child node about to be removed
	 * @param newChild the replacement child
	 * @param property the child or child list property descriptor
	 * @since 3.0
	 */
	void preReplaceChildEvent(ASTNode node, ASTNode child, ASTNode newChild, StructuralPropertyDescriptor property) {
		// IMPORTANT: this method is called by readers during lazy init
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE REP]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.preReplaceChildEvent(node, child, newChild, property);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has not been changed yet
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Reports that the given node is about to change the value of a
	 * non-child property.
	 *
	 * @param node the node to be modified
	 * @param property the property descriptor
	 * @since 3.0
	 */
	void preValueChangeEvent(ASTNode node, SimplePropertyDescriptor property) {
		// IMPORTANT: this method is called by readers during lazy init
		synchronized (this.internalASTLock) {
			// guard against concurrent access by a reader doing lazy init
			if (this.disableEvents > 0) {
				// doing lazy init OR already processing an event
				// System.out.println("[BOUNCE CHANGE]");
				return;
			} else {
				disableEvents();
			}
		}
		try {
			this.eventHandler.preValueChangeEvent(node, property);
			// N.B. even if event handler blows up, the AST is not
			// corrupted since node has already been changed
		} finally {
			reenableEvents();
		}
	}

	/**
	 * Enables the recording of changes to the given compilation
	 * unit and its descendents. The compilation unit must have
	 * been created by <code>ASTParser</code> and still be in
	 * its original state. Once recording is on,
	 * arbitrary changes to the subtree rooted at the compilation
	 * unit are recorded internally. Once the modification has
	 * been completed, call <code>rewrite</code> to get an object
	 * representing the corresponding edits to the original
	 * source code string.
	 *
	 * @exception IllegalArgumentException if this compilation unit is
	 * marked as unmodifiable, or if this compilation unit has already
	 * been tampered with, or if recording has already been enabled,
	 * or if <code>root</code> is not owned by this AST
	 * @see CompilationUnit#recordModifications()
	 * @since 3.0
	 */
	void recordModifications(CompilationUnit root) {
		if(this.modificationCount != this.originalModificationCount) {
			throw new IllegalArgumentException("AST is already modified"); //$NON-NLS-1$
		} else if(this.rewriter  != null) {
			throw new IllegalArgumentException("AST modifications are already recorded"); //$NON-NLS-1$
		} else if((root.getFlags() & ASTNode.PROTECT) != 0) {
			throw new IllegalArgumentException("Root node is unmodifiable"); //$NON-NLS-1$
		} else if(root.getAST() != this) {
			throw new IllegalArgumentException("Root node is not owned by this ast"); //$NON-NLS-1$
		}

		this.rewriter = new InternalASTRewrite(root);
		setEventHandler(this.rewriter);
	}

	//=============================== ANNOTATIONS ====================

	/**
     * Reenable events.
	 * This method is thread-safe for AST readers.
	 *
	 * @see #disableEvents()
     * @since 3.0
     */
	final void reenableEvents() {
		synchronized (this.internalASTLock) {
			// guard against concurrent access by another reader
			this.disableEvents--;
		}
	}

	/**
	 * Returns the type binding for a "well known" type.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * <p>
	 * The following type names are supported:
	 * <ul>
	 * <li><code>"boolean"</code></li>
	 * <li><code>"byte"</code></li>
	 * <li><code>"char"</code></li>
	 * <li><code>"double"</code></li>
	 * <li><code>"float"</code></li>
	 * <li><code>"int"</code></li>
	 * <li><code>"long"</code></li>
	 * <li><code>"short"</code></li>
	 * <li><code>"void"</code></li>
	 * <li><code>"java.lang.AssertionError"</code> (since 3.7)</li>
	 * <li><code>"java.lang.Boolean"</code> (since 3.1)</li>
	 * <li><code>"java.lang.Byte"</code> (since 3.1)</li>
	 * <li><code>"java.lang.Character"</code> (since 3.1)</li>
	 * <li><code>"java.lang.Class"</code></li>
	 * <li><code>"java.lang.Cloneable"</code></li>
	 * <li><code>"java.lang.Double"</code> (since 3.1)</li>
	 * <li><code>"java.lang.Error"</code></li>
	 * <li><code>"java.lang.Exception"</code></li>
	 * <li><code>"java.lang.Float"</code> (since 3.1)</li>
	 * <li><code>"java.lang.Integer"</code> (since 3.1)</li>
	 * <li><code>"java.lang.Long"</code> (since 3.1)</li>
	 * <li><code>"java.lang.Object"</code></li>
	 * <li><code>"java.lang.RuntimeException"</code></li>
	 * <li><code>"java.lang.Short"</code> (since 3.1)</li>
	 * <li><code>"java.lang.String"</code></li>
	 * <li><code>"java.lang.StringBuffer"</code></li>
	 * <li><code>"java.lang.Throwable"</code></li>
	 * <li><code>"java.lang.Void"</code> (since 3.1)</li>
	 * <li><code>"java.io.Serializable"</code></li>
	 * </ul>
	 * </p>
	 *
	 * @param name the name of a well known type
	 * @return the corresponding type binding, or <code>null</code> if the
	 *   named type is not considered well known or if no binding can be found
	 *   for it
	 */
	public ITypeBinding resolveWellKnownType(String name) {
		if (name == null) {
			return null;
		}
		return getBindingResolver().resolveWellKnownType(name);
	}

	/**
	 * Converts all modifications recorded into an object
	 * representing the corresponding text edits to the
	 * given document containing the original source
	 * code for the compilation unit that gave rise to
	 * this AST.
	 *
	 * @param document original document containing source code
	 * for the compilation unit
	 * @param options the table of formatter options
	 * (key type: <code>String</code>; value type: <code>String</code>);
	 * or <code>null</code> to use the standard global options
	 * {@link JavaCore#getOptions() JavaCore.getOptions()}.
	 * @return text edit object describing the changes to the
	 * document corresponding to the recorded AST modifications
	 * @exception IllegalArgumentException if the document passed is
	 * <code>null</code> or does not correspond to this AST
	 * @exception IllegalStateException if <code>recordModifications</code>
	 * was not called to enable recording
	 * @see CompilationUnit#rewrite(IDocument, Map)
	 * @since 3.0
	 */
	TextEdit rewrite(IDocument document, Map options) {
		if (document == null) {
			throw new IllegalArgumentException();
		}
		if (this.rewriter  == null) {
			throw new IllegalStateException("Modifications record is not enabled"); //$NON-NLS-1$
		}
		return this.rewriter.rewriteAST(document, options);
	}

	/**
	 * Sets the binding resolver for this AST.
	 *
	 * @param resolver the new binding resolver for this AST
	 */
	void setBindingResolver(BindingResolver resolver) {
		if (resolver == null) {
			throw new IllegalArgumentException();
		}
		this.resolver = resolver;
	}

	/**
	 * Sets default node flags of new nodes of this AST.
	 *
	 * @param flag node flags of new nodes of this AST
	 * @since 3.0
	 */
	void setDefaultNodeFlag(int flag) {
		this.defaultNodeFlag = flag;
	}

	/**
	 * Sets the event handler for this AST.
	 *
	 * @param eventHandler the event handler for this AST
	 * @since 3.0
	 */
	void setEventHandler(NodeEventHandler eventHandler) {
		if (this.eventHandler == null) {
			throw new IllegalArgumentException();
		}
		this.eventHandler = eventHandler;
	}

	void setFlag(int newValue) {
		this.bits |= newValue;
	}

	/**
	 * Set <code>originalModificationCount</code> to the current modification count
	 *
	 * @since 3.0
	 */
	void setOriginalModificationCount(long count) {
		this.originalModificationCount = count;
	}

	/**
     * Checks that this AST operation is only used when
     * building level JLS2 ASTs.

     * @exception UnsupportedOperationException
	 * @since 3.0
     */
	void supportedOnlyIn2() {
	  if (this.apiLevel != AST.JLS2) {
	  	throw new UnsupportedOperationException("Operation not supported in JLS2 AST"); //$NON-NLS-1$
	  }
	}

	/**
     * Checks that this AST operation is not used when
     * building level JLS2 ASTs.

     * @exception UnsupportedOperationException
	 * @since 3.0
     */
	void unsupportedIn2() {
	  if (this.apiLevel == AST.JLS2) {
	  	throw new UnsupportedOperationException("Operation not supported in JLS2 AST"); //$NON-NLS-1$
	  }
	}
}

