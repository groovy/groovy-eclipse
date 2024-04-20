package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * @since 3.38
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractUnnamedTypeDeclaration extends BodyDeclaration {

	/**
	 * The body declarations (element type: {@link BodyDeclaration}).
	 * Defaults to an empty list.
	 * @since 2.0 (originally declared on {@link TypeDeclaration})
	 */
	ASTNode.NodeList bodyDeclarations;

	/**
	 * Returns structural property descriptor for the "bodyDeclarations" property
	 * of this node (element type: {@link BodyDeclaration}).
	 *
	 * @return the property descriptor
	 */
	abstract ChildListPropertyDescriptor internalBodyDeclarationsProperty();

	public AbstractUnnamedTypeDeclaration(AST ast) {
		super(ast);
		this.bodyDeclarations = new ASTNode.NodeList(internalBodyDeclarationsProperty());
	}

	/**
	 * Returns the live ordered list of body declarations of this type
	 * declaration.
	 *
	 * @return the live list of body declarations
	 *    (element type: {@link BodyDeclaration})
	 * @since 2.0 (originally declared on {@link TypeDeclaration})
	 */
	public List bodyDeclarations() {
		return this.bodyDeclarations;
	}

	/**
	 * Creates and returns a structural property descriptor for the
	 * "bodyDeclaration" property declared on the given concrete node type (element type: {@link BodyDeclaration}).
	 *
	 * @return the property descriptor
	 */
	static final ChildListPropertyDescriptor internalBodyDeclarationPropertyFactory(Class nodeClass) {
		return new ChildListPropertyDescriptor(nodeClass, "bodyDeclarations", BodyDeclaration.class, CYCLE_RISK); //$NON-NLS-1$
	}

	/**
	 * Returns structural property descriptor for the "bodyDeclarations" property
	 * of this node (element type: {@link BodyDeclaration}).
	 *
	 * @return the property descriptor
	 * @since 3.1 (originally declared on {@link AbstractTypeDeclaration})
	 */
	public final ChildListPropertyDescriptor getBodyDeclarationsProperty() {
		return internalBodyDeclarationsProperty();
	}

	/**
	 * Returns whether this type declaration is a package member (that is,
	 * a top-level type).
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a compilation unit node.
	 * </p>
	 *
	 * @return <code>true</code> if this type declaration is a child of
	 *   a compilation unit node, and <code>false</code> otherwise
	 * @since 2.0 (originally declared on {@link TypeDeclaration})
	 */
	public boolean isPackageMemberTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof CompilationUnit);
	}

	/**
	 * Returns whether this type declaration is a type member.
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a type declaration node or an anonymous
	 * class declaration.
	 * </p>
	 *
	 * @return <code>true</code> if this type declaration is a child of
	 *   a type declaration node or an anonymous class declaration node,
	 *   and <code>false</code> otherwise
	 * @since 2.0 (originally declared on {@link TypeDeclaration})
	 */
	public boolean isMemberTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof AbstractTypeDeclaration)
			|| (parent instanceof AnonymousClassDeclaration);
	}

	/**
	 * Returns whether this type declaration is a local type.
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node's parent is a type declaration statement node.
	 * </p>
	 *
	 * @return <code>true</code> if this type declaration is a child of
	 *   a type declaration statement node, and <code>false</code> otherwise
	 * @since 2.0 (originally declared on <code>TypeDeclaration</code>)
	 */
	public boolean isLocalTypeDeclaration() {
		ASTNode parent = getParent();
		return (parent instanceof TypeDeclarationStatement);
	}

	@Override
	int memSize() {
		return super.memSize() + 4;
	}

}
