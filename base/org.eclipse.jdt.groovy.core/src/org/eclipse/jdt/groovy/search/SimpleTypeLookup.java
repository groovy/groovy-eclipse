/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.jdt.groovy.search;

import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.EXACT;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.INFERRED;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.UNKNOWN;
import static org.eclipse.jdt.groovy.search.VariableScope.NO_GENERICS;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.objectweb.asm.Opcodes;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 *          Looks at the type associated with the ASTNode for the type <br>
 */
@SuppressWarnings("nls")
public class SimpleTypeLookup implements ITypeLookup {

	private GroovyCompilationUnit unit;

	public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
		this.unit = unit;
	}

	public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {

		TypeConfidence[] confidence = new TypeConfidence[] { EXACT };
		ClassNode declaringType = objectExpressionType != null ? objectExpressionType : findDeclaringType(node, scope, confidence);

		TypeLookupResult result = findType(node, objectExpressionType, declaringType, scope, confidence[0]);

		return result;
	}

	public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
		return new TypeLookupResult(node.getType(), node.getDeclaringClass(), node, EXACT, scope);
	}

	public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
		return new TypeLookupResult(node.getReturnType(), node.getDeclaringClass(), node, EXACT, scope);
	}

	public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
		ClassNode baseType = node.getClassNode();
		return new TypeLookupResult(baseType, baseType, baseType, EXACT, scope);
	}

	public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
		ClassNode baseType = node.getType();
		if (baseType != null) {
			return new TypeLookupResult(baseType, baseType, baseType, EXACT, scope);
		} else {
			// * import
			return new TypeLookupResult(VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE,
					VariableScope.OBJECT_CLASS_NODE, INFERRED, scope);
		}
	}

	/**
	 * always return the passed in node
	 */
	public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
		return new TypeLookupResult(node, node, node, EXACT, scope);
	}

	public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
		// look up the type in the current scope to see if the type has
		// has been predetermined (eg- for loop variables)
		VariableInfo info = scope.lookupNameInCurrentScope(node.getName());
		ClassNode type;
		if (info != null) {
			type = info.type;
		} else {
			type = node.getType();
		}
		return new TypeLookupResult(type, scope.getEnclosingTypeDeclaration(), node /* should be methodnode? */, EXACT, scope);
	}

	/**
	 * @param node
	 * @param scope
	 * @return
	 */
	private ClassNode findDeclaringType(Expression node, VariableScope scope, TypeConfidence[] confidence) {
		if (node instanceof ClassExpression || node instanceof ConstructorCallExpression) {
			return node.getType();

		} else if (node instanceof FieldExpression) {
			return ((FieldExpression) node).getField().getDeclaringClass();

		} else if (node instanceof MethodCallExpression) {
			return ((MethodCallExpression) node).getObjectExpression().getType();

		} else if (node instanceof StaticMethodCallExpression) {
			return ((StaticMethodCallExpression) node).getOwnerType();

		} else if (node instanceof VariableExpression) {
			Variable var = ((VariableExpression) node).getAccessedVariable();
			if (var instanceof DynamicVariable) {
				// search type hierarchy for declaration
				ASTNode declaration = findDeclaration(var.getName(), scope.getEnclosingTypeDeclaration());
				ClassNode type;
				if (declaration == null) {
					// this is a dynamic variable that doesn't seem to have a declaration
					// it might be an unknown and a mistake, but it could also be declared by 'this'
					VariableInfo info = scope.lookupName("this");
					type = info == null ? VariableScope.OBJECT_CLASS_NODE : info.declaringType;
				} else {
					type = declaringTypeFromDeclaration(declaration, var.getType());
				}
				confidence[0] = TypeConfidence.findLessPrecise(confidence[0], INFERRED);
				return type;
			} else if (var instanceof FieldNode) {
				return ((FieldNode) var).getDeclaringClass();
			} else if (var instanceof PropertyNode) {
				return ((PropertyNode) var).getDeclaringClass();
			} else if (scope.isThisOrSuper((VariableExpression) node)) { // use 'node' because 'var' may be null
				// this or super expression, but it is not bound,
				// probably because concrete ast was requested
				return scope.lookupName(((VariableExpression) node).getName()).declaringType;
			} else {
				// local variable, no declaring type
				// fall through
			}

		} else if (node instanceof DeclarationExpression) {
			// the type declaration of the DeclarationExpression is considered to be the
			// declaring type. This ensures that type declarations are treated as
			// to be type references.
			return ((DeclarationExpression) node).getLeftExpression().getType();
		}
		return VariableScope.OBJECT_CLASS_NODE;
	}

	/**
	 * @param node
	 * @param scope
	 * @return
	 */
	private TypeLookupResult findType(Expression node, ClassNode objectExpressionType, ClassNode declaringType,
			VariableScope scope, TypeConfidence confidence) {

		// check first to see if we have this type inferred
		if (node instanceof VariableExpression) {
			return findTypeForVariable((VariableExpression) node, scope, confidence, declaringType);
		}

		// if the object type is not null, then we base the
		// type of this node on the object type
		ClassNode nodeType = node.getType();
		if (objectExpressionType != null) {
			// lookup the type based on the object's expression type
			// assume it is a method/property/field in the object expression type's hierarchy

			if (node instanceof ConstantExpression) {
				return findTypeForNameWithKnownObjectExpression(((ConstantExpression) node).getText(), nodeType,
						objectExpressionType, scope, confidence);

			} else if (node instanceof BinaryExpression && ((BinaryExpression) node).getOperation().getType() == Types.EQUALS) {
				// this is an assignment expression, return the object expression, which is the right hand side
				return new TypeLookupResult(objectExpressionType, declaringType, null, confidence, scope);
			} else if (node instanceof TernaryExpression) {
				// return the object expression type
				return new TypeLookupResult(objectExpressionType, declaringType, null, confidence, scope);
			}
		}

		// no object expression, look at the kind of expression
		// the following expression kinds have a type that is constant
		// no matter what their contents are.
		if (node instanceof ConstantExpression) {
			// here, we know that since there is no object expression, this is not part
			// of a dotted anything, so we can safely assume that it is a quoted string or
			// some other constant
			ConstantExpression constExpr = (ConstantExpression) node;

			if (constExpr.isTrueExpression() || constExpr.isFalseExpression()) {
				return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);
			} else if (constExpr.isNullExpression()) {
				return new TypeLookupResult(VariableScope.VOID_CLASS_NODE, null, null, confidence, scope);
			} else if (constExpr.isEmptyStringExpression()) {
				return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, null, confidence, scope);
			} else if (ClassHelper.isNumberType(nodeType) || nodeType == ClassHelper.BigDecimal_TYPE
					|| nodeType == ClassHelper.BigInteger_TYPE) {
				return new TypeLookupResult(nodeType, null, null, confidence, scope);
			} else {
				// there is a possibility that this is a constant expression inside a GString.
				// check for a '$' as a start.
				if (node.getText().startsWith("$")) { //$NON-NLS-1$
					String realName = node.getText().substring(1);
					if (realName.startsWith("{") && realName.endsWith("}")) {
						realName = realName.substring(1, realName.length() - 1);
					}
					return findTypeForNameWithKnownObjectExpression(realName, nodeType, scope.getEnclosingTypeDeclaration(), scope,
							confidence);
				}
				if (nodeType.equals(VariableScope.STRING_CLASS_NODE)) {
					// likely a proper quoted string constant
					return new TypeLookupResult(nodeType, null, node, confidence, scope);
				} else {
					return new TypeLookupResult(nodeType, null, null, UNKNOWN, scope);
				}
			}

		} else if (node instanceof TupleExpression || node instanceof ListExpression || node instanceof RangeExpression) {
			// some variant of List. Try to find the parameterization by peeking at the first element.
			ClassNode parameterized = parameterizeThisList(node);
			return new TypeLookupResult(parameterized, null, null, confidence, scope);

		} else if (node instanceof BinaryExpression) {
			// Object expression was null, so go for the left expression.
			// The final type of this BinaryExpression could be more complicated. Have to look at the operation and
			// the left and right sides. This happens in the TypeInferencingVisitorWithRequestor
			return new TypeLookupResult(((BinaryExpression) node).getLeftExpression().getType(), null, null, confidence, scope);

		} else if (node instanceof BooleanExpression || node instanceof NotExpression) {
			return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof GStringExpression) {
			// note that we return String type here, not GString so that DGMs will apply
			return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof MapExpression) {
			ClassNode parameterized = parameterizeThisMap((MapExpression) node);
			return new TypeLookupResult(parameterized, null, null, confidence, scope);

		} else if (node instanceof PostfixExpression || node instanceof PrefixExpression) {
			// because of operator overloading, we should be looking at the type
			// of the inner expression, but Integer will be safe for most of the time.
			return new TypeLookupResult(VariableScope.INTEGER_CLASS_NODE, null, null, confidence, scope);

		} else if (node instanceof BitwiseNegationExpression) {
			ClassNode type = ((BitwiseNegationExpression) node).getExpression().getType();
			if (type.getName().equals(VariableScope.STRING_CLASS_NODE.getName())) {
				return new TypeLookupResult(VariableScope.PATTERN_CLASS_NODE, null, null, confidence, scope);
			} else {
				return new TypeLookupResult(type, null, null, confidence, scope);
			}
		} else if (node instanceof ClassExpression) {
			// check for special case...a bit crude...determine if the actual reference is to Foo.class or to Foo
			if (nodeIsDotClassReference(node)) {
				return new TypeLookupResult(VariableScope.CLASS_CLASS_NODE, VariableScope.CLASS_CLASS_NODE,
						VariableScope.CLASS_CLASS_NODE, TypeConfidence.EXACT, scope);
			} else {
				return new TypeLookupResult(nodeType, declaringType, nodeType, confidence, scope);
			}
		} else if (node instanceof StaticMethodCallExpression) {
			StaticMethodCallExpression expr = (StaticMethodCallExpression) node;
			List<MethodNode> methods = expr.getOwnerType().getMethods(expr.getMethod());
			if (methods.size() > 0) {
				MethodNode method = methods.get(0);
				return new TypeLookupResult(method.getReturnType(), declaringType, method, confidence, scope);
			}
		}

		// if we get here, then we can't infer the type. Set to unknown if required.
		if (!(node instanceof MethodCallExpression) && !(node instanceof ConstructorCallExpression)
				&& !(node instanceof MapEntryExpression) && !(node instanceof PropertyExpression)
				&& !(node instanceof TupleExpression) && nodeType.equals(VariableScope.OBJECT_CLASS_NODE)) {
			confidence = UNKNOWN;
		}

		// don't know
		return new TypeLookupResult(nodeType, declaringType, null, confidence, scope);
	}

	/**
	 * A simple approach to parameterizing a map. Look at the first entry and if the types are statically known, use those to
	 * parameterize the map
	 * 
	 * @param node
	 * @return a parameterized map
	 */
	private ClassNode parameterizeThisMap(MapExpression node) {
		if (node.getMapEntryExpressions().size() > 0) {
			// must keep the cast for Groovy 1.6.
			@SuppressWarnings("cast")
			MapEntryExpression entry = (MapEntryExpression) node.getMapEntryExpressions().get(0);
			ClassNode map = VariableScope.clone(VariableScope.MAP_CLASS_NODE);
			GenericsType[] unresolvedGenericsForMap = unresolvedGenericsForType(map);
			unresolvedGenericsForMap[0].setType(entry.getKeyExpression().getType());
			unresolvedGenericsForMap[0].setName(entry.getKeyExpression().getType().getName());
			unresolvedGenericsForMap[1].setType(entry.getValueExpression().getType());
			unresolvedGenericsForMap[1].setName(entry.getValueExpression().getType().getName());
			return map;
		}
		return VariableScope.clone(VariableScope.MAP_CLASS_NODE);
	}

	/**
	 * A simple approach to parameterizing a list. Look at the first entry and if the type is statically known, use that to
	 * parameterize the map
	 * 
	 * @param node
	 * @return a parameterized list
	 */
	private ClassNode parameterizeThisList(Expression node) {
		if (node instanceof TupleExpression) {
			TupleExpression tuple = (TupleExpression) node;
			if (tuple.getExpressions().size() > 0) {
				ClassNode list = VariableScope.clone(VariableScope.LIST_CLASS_NODE);
				GenericsType[] unresolvedGenericsForList = unresolvedGenericsForType(list);
				unresolvedGenericsForList[0].setType(tuple.getExpression(0).getType());
				unresolvedGenericsForList[0].setName(tuple.getExpression(0).getType().getName());
				return list;
			}
		} else if (node instanceof ListExpression) {
			ListExpression listExpr = (ListExpression) node;
			if (listExpr.getExpressions().size() > 0) {
				ClassNode list = VariableScope.clone(VariableScope.LIST_CLASS_NODE);
				GenericsType[] unresolvedGenericsForList = unresolvedGenericsForType(list);
				unresolvedGenericsForList[0].setType(listExpr.getExpression(0).getType());
				unresolvedGenericsForList[0].setName(listExpr.getExpression(0).getType().getName());
				return list;
			}
		} else if (node instanceof RangeExpression) {
			RangeExpression rangeExpr = (RangeExpression) node;
			Expression expr = rangeExpr.getFrom() != null ? rangeExpr.getFrom() : rangeExpr.getTo();
			if (expr != null) {
				ClassNode list = VariableScope.clone(VariableScope.LIST_CLASS_NODE);
				GenericsType[] unresolvedGenericsForList = unresolvedGenericsForType(list);
				unresolvedGenericsForList[0].setType(expr.getType());
				unresolvedGenericsForList[0].setName(expr.getType().getName());
				return list;
			}
		}
		return VariableScope.LIST_CLASS_NODE;
	}

	/**
	 * a little crude because will not find if there are spaces between '.' and 'class'
	 * 
	 * @param node
	 * @return
	 */
	private boolean nodeIsDotClassReference(Expression node) {
		int end = node.getEnd();
		int start = node.getStart();
		char[] contents = unit.getContents();
		if (contents.length >= end) {
			char[] realText = new char[end - start];
			System.arraycopy(contents, start, realText, 0, end - start);
			String realTextStr = String.valueOf(realText).trim();
			return realTextStr.endsWith(".class") || realTextStr.endsWith(".class."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return false;
	}

	/**
	 * look for a name within an object expression. It is either in the hierarchy, it is in the variable scope, or it is unknown.
	 * 
	 * @return
	 */
	private TypeLookupResult findTypeForNameWithKnownObjectExpression(String name, ClassNode type, ClassNode declaringType,
			VariableScope scope, TypeConfidence confidence) {
		ClassNode realDeclaringType;
		VariableInfo varInfo;
		ASTNode declaration = findDeclaration(name, declaringType);
		if (declaration != null) {
			type = typeFromDeclaration(declaration, declaringType);
			realDeclaringType = declaringTypeFromDeclaration(declaration, declaringType);
		} else if (checkDeclaringType(declaringType, scope) &&
		// make everything from the scopes available
				(varInfo = scope.lookupName(name)) != null) {

			// now try to find the declaration again
			type = varInfo.type;
			realDeclaringType = varInfo.declaringType;
			declaration = findDeclaration(name, realDeclaringType);
			if (declaration == null) {
				declaration = varInfo.declaringType;
			}
		} else if (name.equals("call")) {
			// assume that this is a synthetic call method for calling a closure
			declaration = realDeclaringType = declaringType;
		} else {
			realDeclaringType = declaringType;
			confidence = UNKNOWN;
		}
		return new TypeLookupResult(type, realDeclaringType, declaration, confidence, scope);
	}

	/**
	 * @param declaringType
	 * @param scope
	 * @return
	 */
	private boolean checkDeclaringType(ClassNode declaringType, VariableScope scope) {
		if (declaringType.equals(scope.getEnclosingTypeDeclaration())) {
			// this or implicit this
			return true;
		}

		if (scope.getEnclosingClosure() != null) {
			CallAndType callAndType = scope.getEnclosingMethodCallExpression();
			if (callAndType != null && declaringType.equals(callAndType.declaringType)) {
				// 'this' inside of a closure
				return true;
			}
		}
		return false;
	}

	private TypeLookupResult findTypeForVariable(VariableExpression var, VariableScope scope, TypeConfidence confidence,
			ClassNode declaringType) {
		ASTNode declaration = var;
		Variable accessedVar = var.getAccessedVariable();
		if (accessedVar instanceof ASTNode) {
			// not a DynamicVariable
			declaration = (ASTNode) accessedVar;
		}

		VariableInfo info = scope.lookupName(var.getName());
		TypeConfidence origConfidence = confidence;
		if (accessedVar instanceof DynamicVariable) {
			// this is likely a reference to a field or method in a type in the hierarchy
			// find the declaration
			ASTNode maybeDeclaration = findDeclaration(accessedVar.getName(), getMorePreciseType(declaringType, info));
			if (maybeDeclaration != null) {
				declaration = maybeDeclaration;
				// declaring type may have changed
				declaringType = declaringTypeFromDeclaration(declaration, info != null ? info.declaringType
						: VariableScope.OBJECT_CLASS_NODE);
			} else {
				confidence = UNKNOWN;
			}
		}

		ClassNode type;
		if (info != null) {
			confidence = TypeConfidence.findLessPrecise(origConfidence, INFERRED);
			type = info.type;
			declaringType = getMorePreciseType(declaringType, info);
			if (scope.isThisOrSuper(var)) {
				declaration = type;
			}
		} else {

			// we have a variable expression, but it is not
			// declared anywhere in the scope. It is probably a DynamicVariable
			if (accessedVar instanceof DynamicVariable) {
				type = typeFromDeclaration(declaration, declaringType);
			} else {
				type = var.getType();
			}
		}
		return new TypeLookupResult(type, declaringType, declaration, confidence, scope);
	}

	/**
	 * @param declaringType
	 * @param info
	 * @return
	 */
	private ClassNode getMorePreciseType(ClassNode declaringType, VariableInfo info) {
		ClassNode maybeDeclaringType = info != null ? info.declaringType : VariableScope.OBJECT_CLASS_NODE;
		if (maybeDeclaringType.equals(VariableScope.OBJECT_CLASS_NODE) && !VariableScope.OBJECT_CLASS_NODE.equals(declaringType)) {
			return declaringType;
		} else {
			return maybeDeclaringType;
		}
	}

	/**
	 * @param declaration
	 * @return
	 */
	private ClassNode declaringTypeFromDeclaration(ASTNode declaration, ClassNode resolvedTypeOfDeclaration) {
		ClassNode typeOfDeclaration;
		if (declaration instanceof FieldNode) {
			typeOfDeclaration = ((FieldNode) declaration).getDeclaringClass();
		} else if (declaration instanceof MethodNode) {
			typeOfDeclaration = ((MethodNode) declaration).getDeclaringClass();
		} else if (declaration instanceof PropertyNode) {
			typeOfDeclaration = ((PropertyNode) declaration).getDeclaringClass();
		} else {
			typeOfDeclaration = VariableScope.OBJECT_CLASS_NODE;
		}
		// don't necessarily use the typeOfDeclaration. the resolvedTypeOfDeclaration includes the types of generics
		// so if the names are the same, then used the resolved version
		if (typeOfDeclaration.getName().equals(resolvedTypeOfDeclaration.getName())) {
			return resolvedTypeOfDeclaration;
		} else {
			return typeOfDeclaration;
		}
	}

	/**
	 * @param declaration the declaration to look up
	 * @param resolvedType the unredirected type that declares this declaration somewhere in its hierarchy
	 * @return class node with generics replaced by actual types
	 */
	private ClassNode typeFromDeclaration(ASTNode declaration, ClassNode resolvedType) {
		ClassNode typeOfDeclaration, declaringType = declaringTypeFromDeclaration(declaration, resolvedType);
		if (declaration instanceof FieldNode) {
			typeOfDeclaration = ((FieldNode) declaration).getType();
		} else if (declaration instanceof MethodNode) {
			typeOfDeclaration = ((MethodNode) declaration).getReturnType();
		} else if (declaration instanceof PropertyNode) {
			typeOfDeclaration = ((PropertyNode) declaration).getType();
		} else if (declaration instanceof Expression) {
			typeOfDeclaration = ((Expression) declaration).getType();
		} else {
			typeOfDeclaration = VariableScope.OBJECT_CLASS_NODE;
		}

		// now try to resolve generics
		// travel up the hierarchy and look for more generics
		// also look for generics on methods...(not doing this yet...)
		GenericsMapper mapper = GenericsMapper.gatherGenerics(resolvedType, declaringType.redirect());
		ClassNode resolvedTypeOfDeclaration = VariableScope.resolveTypeParameterization(mapper,
				VariableScope.clone(typeOfDeclaration));
		return resolvedTypeOfDeclaration;
	}

	protected GenericsType[] unresolvedGenericsForType(ClassNode unresolvedType) {
		ClassNode candidate = unresolvedType;
		GenericsType[] gts = candidate.getGenericsTypes();
		gts = gts == null ? NO_GENERICS : gts;
		List<GenericsType> allGs = new ArrayList<GenericsType>(2);
		while (candidate != null) {
			gts = candidate.getGenericsTypes();
			gts = gts == null ? NO_GENERICS : gts;
			for (GenericsType gt : gts) {
				allGs.add(gt);
			}
			candidate = candidate.getSuperClass();
		}
		return allGs.toArray(NO_GENERICS);
	}

	// FIXADE consider deleting
	// protected GenericsType[] resolvedGenericsForType(ClassNode unresolvedType) {
	// ClassNode candidate = unresolvedType;
	// GenericsType[] gts = candidate.getGenericsTypes();
	// gts = gts == null ? NO_GENERICS : gts;
	// List<GenericsType> allGs = new ArrayList<GenericsType>(2);
	// while (candidate != null) {
	// gts = candidate.getGenericsTypes();
	// gts = gts == null ? NO_GENERICS : gts;
	// for (GenericsType gt : gts) {
	// allGs.add(gt);
	// }
	// candidate = candidate.getUnresolvedSuperClass();
	// }
	// return allGs.toArray(NO_GENERICS);
	// }

	/**
	 * Looks for the named member in the declaring type. Also searches super types. The result can be a field, method, or property
	 * 
	 * @param name
	 * @param declaringType
	 * @return
	 */
	private ASTNode findDeclaration(String name, ClassNode declaringType) {
		if (declaringType.isArray()) {
			// only length exists on array type
			if (name.equals("length")) {
				return createLengthField(declaringType);
			} else {
				// otherwise search on object
				return findDeclaration(name, VariableScope.OBJECT_CLASS_NODE);
			}
		}

		Set<ClassNode> allClasses = new LinkedHashSet<ClassNode>();
		createTypeHierarchy(declaringType, allClasses);

		AnnotatedNode maybe = findPropertyInClass(name, allClasses);
		if (maybe != null) {
			return maybe;
		}

		// look at methods first because it is more likely people would
		// want to call the method than a field of the same name.
		List<MethodNode> maybeMethods = declaringType.getMethods(name);
		if (maybeMethods != null && maybeMethods.size() > 0) {
			return maybeMethods.get(0);
		}

		maybe = declaringType.getField(name);
		if (maybe != null) {
			return maybe;
		}
		if (declaringType.isInterface()) {
			Set<ClassNode> allInterfaces = new LinkedHashSet<ClassNode>();
			findAllInterfaces(declaringType, allInterfaces);

			// super interface methods on an interface are not returned by getMethods(), so must explicitly look for them
			MethodNode interfaceMethod = findMethodInInterface(name, allInterfaces);
			if (interfaceMethod != null) {
				return interfaceMethod;
			}

			// do the same for properties
			PropertyNode interfaceProperty = findPropertyInInterface(name, allInterfaces);
			if (interfaceProperty != null) {
				return interfaceProperty;
			}
		}

		// look for constants declared in super class
		FieldNode constantFromSuper = findConstantInClass(name, allClasses);
		if (constantFromSuper != null) {
			return constantFromSuper;
		}

		// lastly, try converting to a getter and see if the getter version of the method exists
		// hmmmm...should we do the same for set?
		if (!name.startsWith("get") && name.length() > 0) {
			String getter = "get" + Character.toUpperCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
			maybeMethods = declaringType.getMethods(getter);
			if (maybeMethods != null && maybeMethods.size() > 0) {
				return maybeMethods.get(0);
			}
		}

		return null;
	}

	/**
	 * @param declaringType
	 * @return
	 */
	private ASTNode createLengthField(ClassNode declaringType) {
		FieldNode lengthField = new FieldNode("length", Opcodes.ACC_PUBLIC, VariableScope.INTEGER_CLASS_NODE, declaringType, null);
		lengthField.setType(VariableScope.INTEGER_CLASS_NODE);
		lengthField.setDeclaringClass(declaringType);
		return lengthField;
	}

	/**
	 * @param objectExpressionType
	 * @param name
	 * @return
	 */
	private MethodNode findMethodInInterface(String name, Set<ClassNode> allInterfaces) {
		for (ClassNode interf : allInterfaces) {
			List<MethodNode> methods = interf.getDeclaredMethods(name);
			if (methods != null && methods.size() > 0) {
				return methods.get(0);
			}
		}
		return null;
	}

	private PropertyNode findPropertyInClass(String name, Set<ClassNode> allClasses) {
		for (ClassNode clazz : allClasses) {
			PropertyNode prop = clazz.getProperty(name);
			if (prop != null) {
				return prop;
			}
		}
		return null;
	}

	private FieldNode findConstantInClass(String name, Set<ClassNode> allClasses) {
		for (ClassNode clazz : allClasses) {
			FieldNode field = clazz.getField(name);
			if (field != null && Flags.isFinal(field.getModifiers()) && field.isStatic()) {
				return field;
			}
		}
		return null;
	}

	private PropertyNode findPropertyInInterface(String name, Set<ClassNode> allInterfaces) {
		for (ClassNode interf : allInterfaces) {
			PropertyNode prop = interf.getProperty(name);
			if (prop != null) {
				return prop;
			}
		}
		return null;
	}

	private void findAllInterfaces(ClassNode interf, Set<ClassNode> allInterfaces) {
		if (!allInterfaces.contains(interf) && interf.getInterfaces() != null) {
			allInterfaces.add(interf);
			for (ClassNode superInterface : interf.getInterfaces()) {
				findAllInterfaces(superInterface, allInterfaces);
			}
		}
	}

	private void createTypeHierarchy(ClassNode clazz, Set<ClassNode> allClasses) {
		if (!allClasses.contains(clazz)) {
			allClasses.add(clazz);
			if (clazz.getSuperClass() != null) {
				createTypeHierarchy(clazz.getSuperClass(), allClasses);
			}
			if (clazz.getInterfaces() != null) {
				for (ClassNode superInterface : clazz.getInterfaces()) {
					findAllInterfaces(superInterface, allClasses);
				}
			}
		}
	}
}