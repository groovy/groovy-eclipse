package org.codehaus.jdt.groovy.internal.compiler.ast;

import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor;

/**
 * {@code GroovyTypeInferenceVisitor} class helps to infer types in Groovy editors.
 *
 * @author denis_murashev
 */
public class GroovyTypeInferenceHelperVisitor extends StaticCompilationVisitor {

	private static final ClassNode COMPILE_STATIC = ClassHelper.make(CompileStatic.class);
	private static final ClassNode TYPE_CHECKED = ClassHelper.make(TypeChecked.class);

	public GroovyTypeInferenceHelperVisitor(SourceUnit unit, ClassNode node) {
		super(unit, node);
	}

	@Override
	protected void addError(String msg, ASTNode expr) {
		// No need to raise error
	}

	@Override
	protected void addStaticTypeError(String msg, ASTNode expr) {
		// No need to raise error
	}

	@Override
	protected boolean shouldSkipClassNode(ClassNode node) {
		return !(node.getAnnotations(COMPILE_STATIC).isEmpty() && node.getAnnotations(TYPE_CHECKED).isEmpty());
	}

	@Override
	protected boolean shouldSkipMethodNode(MethodNode node) {
		return !(node.getAnnotations(COMPILE_STATIC).isEmpty() && node.getAnnotations(TYPE_CHECKED).isEmpty());
	}
}
