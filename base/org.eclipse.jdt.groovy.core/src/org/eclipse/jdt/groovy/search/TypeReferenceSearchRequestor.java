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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.search.matching.DeclarationOfReferencedTypesPattern;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchPattern;
import org.eclipse.jdt.internal.core.search.matching.TypeReferencePattern;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Position;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 * 
 */
public class TypeReferenceSearchRequestor implements ITypeRequestor {
	/**
	 * 
	 */
	private static final String DOT = ".";
	private final SearchRequestor requestor;
	private final SearchParticipant participant;

	private final char[] qualificationPattern;
	private final char[] namePattern;
	private final boolean isCaseSensitive;
	private final boolean isCamelCase;
	private final boolean findDeclaration;

	private final Set<Position> acceptedPositions = new HashSet<Position>();
	private char[] cachedContents;

	@SuppressWarnings("nls")
	public TypeReferenceSearchRequestor(TypeReferencePattern pattern, SearchRequestor requestor, SearchParticipant participant) {
		this.requestor = requestor;
		this.participant = participant;
		this.isCaseSensitive = ((Boolean) ReflectionUtils.getPrivateField(JavaSearchPattern.class, "isCaseSensitive", pattern))
				.booleanValue();

		this.namePattern = extractArray(pattern, "simpleName");
		this.qualificationPattern = extractArray(pattern, "qualification");
		this.isCamelCase = ((Boolean) ReflectionUtils.getPrivateField(JavaSearchPattern.class, "isCamelCase", pattern))
				.booleanValue();

		this.findDeclaration = pattern instanceof DeclarationOfReferencedTypesPattern;
	}

	protected char[] extractArray(TypeReferencePattern pattern, String fieldName) {
		char[] arr;
		arr = (char[]) ReflectionUtils.getPrivateField(TypeReferencePattern.class, fieldName, pattern);
		if (!isCaseSensitive) {
			arr = CharOperation.toLowerCase(arr);
		}
		return arr;
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		// don't do constructor calls. They are found through the class node inside of it
		if (node instanceof ClassExpression || node instanceof ClassNode || node instanceof ImportNode
				|| node instanceof AnnotationNode /* || node instanceof ConstructorNode */) {

			// the type variable may not have correct source location
			ClassNode type;
			if (node instanceof ConstructorNode) {
				type = ((ConstructorNode) node).getDeclaringClass();
			} else if (node instanceof AnnotationNode) {
				type = ((AnnotationNode) node).getClassNode();
			} else {
				type = result.type;
			}
			if (node instanceof ClassExpression && type.equals(VariableScope.CLASS_CLASS_NODE)) {
				// special case...there is a Foo.class expression.
				// the difference between Foo.class and Foo does not appear in the AST.
				// The type of the expression is considered to be Class, but we still need to
				// look for a reference for Foo
				type = ((ClassExpression) node).getType();
			}

			if (type != null) {
				type = removeArray(type);
				if (qualifiedNameMatches(type) && hasValidSourceLocation(node)) {
					int start = -1;
					int end = -1;

					boolean startEndFound = false;
					if (node instanceof ImportNode) {
						if (((ImportNode) node).getType() == null) {
							// if the import node's type is not null, then the type will be visited later anyway.
							// so don't visit it here.
							end = node.getEnd();
							start = node.getStart();
						}
					} else if (node instanceof ClassExpression) {
						end = node.getEnd();
						start = node.getStart();
					} else if (node instanceof ClassNode) {
						ClassNode classNode = (ClassNode) node;
						if (classNode.getNameEnd() > 0) {
							// we are actually dealing with a declaration
							// start = classNode.getNameStart();
							// end = classNode.getNameEnd() + 1;
							// startEndFound = true;
						} else if (classNode.redirect() == classNode) {
							// this is a script declaration... ignore
							start = end = -1;
							startEndFound = true;
						} else {
							// ensure classNode has proper source location
							classNode = maybeGetComponentType(classNode);
							end = classNode.getEnd();
							start = classNode.getStart();
						}
					} else if (node instanceof ConstructorNode) {
						start = ((ConstructorNode) node).getNameStart();
						end = ((ConstructorNode) node).getNameEnd() + 1;
						if (start == 0 && end == 1) {
							// synthetic constructor from script
							start = end = -1;
							startEndFound = true;
						}
					} else if (node instanceof AnnotationNode) {
						type = ((AnnotationNode) node).getClassNode();
						end = type.getEnd();
						start = type.getStart();
					}

					if (!startEndFound) {
						// we have a little more work to do before finding the real
						// offset in the text
						StartEnd startEnd = getMatchLocation(type, enclosingElement, start, end);
						if (startEnd != null) {
							start = startEnd.start;
							end = startEnd.end;
						} else {
							// match really wasn't found
							start = end = -1;
						}
					}

					if (start >= 0 && end >= 0) {
						// don't want to double accept nodes. This could happen with field and object initializers can get pushed
						// into multiple
						// constructors
						Position position = new Position(start, end - start);
						if (!acceptedPositions.contains(position)) {
							IJavaElement realElement = enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy ? ((GroovyClassFileWorkingCopy) enclosingElement
									.getOpenable()).convertToBinary(enclosingElement) : enclosingElement;

							try {
								requestor.acceptSearchMatch(createMatch(result, realElement, start, end));
								acceptedPositions.add(position);
							} catch (CoreException e) {
								Util.log(e, "Error accepting search match for " + realElement); //$NON-NLS-1$
							}
						}
					}
				}
			}
		}
		return VisitStatus.CONTINUE;
	}

	/**
	 * @param result
	 * @param enclosingElement
	 * @param start
	 * @param end
	 * @return
	 */
	protected TypeReferenceMatch createMatch(TypeLookupResult result, IJavaElement enclosingElement, int start, int end) {
		IJavaElement element;
		if (findDeclaration) {
			// don't use the enclosing element, but rather use the declaration of the type
			try {
				ClassNode type = result.type;
				while (type.getComponentType() != null) {
					type = type.getComponentType();
				}
				element = enclosingElement.getJavaProject().findType(type.getName().replace('$', '.'), new NullProgressMonitor());
				if (element == null) {
					element = enclosingElement;
				}
			} catch (JavaModelException e) {
				Util.log(e);
				element = enclosingElement;
			}
		} else {
			element = enclosingElement;
		}
		return new TypeReferenceMatch(element, getAccuracy(result.confidence), start, end - start, false, participant,
				element.getResource());
	}

	/**
	 * @param node
	 * @return
	 */
	private boolean hasValidSourceLocation(ASTNode node) {
		// find the correct ast node that has source locations on it
		// sometimes array nodes do not have source locations, so get around that here.
		ASTNode astNodeWithSourceLocation;
		if (node instanceof ClassNode) {
			astNodeWithSourceLocation = maybeGetComponentType((ClassNode) node);
		} else {
			astNodeWithSourceLocation = node;
		}
		return astNodeWithSourceLocation.getEnd() > 0;
	}

	/**
	 * sometimes the underlying component type contains the source location, not the array type
	 * 
	 * @param orig the original class node
	 * @return the component type if that type contains source information, otherwise return the original
	 */
	private ClassNode maybeGetComponentType(ClassNode orig) {
		if (orig.getComponentType() != null) {
			ClassNode componentType = orig.getComponentType();
			if (componentType.getColumnNumber() != -1) {
				return componentType;
			}
		}
		return orig;
	}

	/**
	 * @param declaration
	 * @return
	 */
	private ClassNode removeArray(ClassNode declaration) {
		return declaration.getComponentType() != null ? removeArray(declaration.getComponentType()) : declaration;
	}

	private String[] extractNameAndQualification(ClassNode type) {
		// qualification includes the full package name, plus all enclosing types with '.' separators
		String qualification = type.getPackageName();
		if (qualification == null) {
			qualification = "";
		}

		String semiQualified = type.getNameWithoutPackage();

		String simple;
		int lastDollar = semiQualified.lastIndexOf('$');
		if (lastDollar > 0) {
			simple = semiQualified.substring(lastDollar + 1);
			semiQualified = semiQualified.replace('$', '.').substring(0, lastDollar);
			if (qualification.length() == 0) {
				qualification = semiQualified;
			} else {
				qualification += DOT + semiQualified;
			}
		} else {
			simple = semiQualified;
		}

		return new String[] { qualification, simple };
	}

	private boolean qualifiedNameMatches(ClassNode type) {
		String[] nameAndQualification = extractNameAndQualification(type);
		String name, qualification;
		qualification = nameAndQualification[0];
		name = nameAndQualification[1];
		if (!isCaseSensitive) {
			name = name.toLowerCase();
			qualification = qualification.toLowerCase();
		}

		boolean match = true;
		if (namePattern != null) { // if pattern is null, then this means '*'
			if (isCamelCase) {
				match = CharOperation.camelCaseMatch(namePattern, name.toCharArray());
			} else {
				match = CharOperation.equals(namePattern, name.toCharArray());
			}
		}

		if (match && qualificationPattern != null) {
			if (isCamelCase) { // if pattern is null, then this means '*'
				match = CharOperation.camelCaseMatch(qualificationPattern, qualification.toCharArray());
			} else {
				match = CharOperation.equals(qualificationPattern, qualification.toCharArray());
			}
		}

		return match;
	}

	private class StartEnd {

		StartEnd(int start, int end) {
			this.start = start;
			this.end = end;
		}

		final int start;
		final int end;
	}

	/**
	 * THe problem that this method gets around is that we can't tell exactly what the text is and exactly where or if there is a
	 * match in the source location. For example, in the text, the type can be fully qualified, or array, or coming from an alias,
	 * or a bound type parameter. On top of that, some source locations are off by one. All these will have location relative to the
	 * offset provided in the start and end fields of the {@link ClassNode}.
	 * 
	 * @param node the class node to find in the source
	 * @param elt the element used to find the text
	 * @return the start and end offsets of the actual match, or null if no match exists.
	 */
	private StartEnd getMatchLocation(ClassNode node, IJavaElement elt, int maybeStart, int maybeEnd) {
		CompilationUnit unit = (CompilationUnit) elt.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (unit != null && cachedContents == null) {
			cachedContents = unit.getContents();
		}

		if (cachedContents != null) {
			int nameLength = maybeEnd - maybeStart;
			int start = -1;
			int end = -1;
			String name = node.getName();
			// handle inner types here
			int dollarIndex = name.lastIndexOf('$');
			name = name.substring(dollarIndex + 1);
			if (name.length() <= nameLength) {
				// might be a qualified name
				start = CharOperation.indexOf(name.toCharArray(), cachedContents, true, maybeStart, maybeEnd + 1);
				end = start + name.length();
			}
			if (start == -1) {
				// check for simple name
				String nameWithoutPackage = node.getNameWithoutPackage();
				start = CharOperation.indexOf(nameWithoutPackage.toCharArray(), cachedContents, true, maybeStart, maybeEnd + 1);
				end = start + nameWithoutPackage.length();
			}
			if (start == -1) {
				// can be an aliased type name
				return null;
			} else {
				return new StartEnd(start, end);
			}
		}
		// shuoldn't happen, but may be a binary match
		return new StartEnd(node.getStart(), node.getEnd());
	}

	/**
	 * check to see if this requestor has something to do with refactoring, if so, we always want an accurate match otherwise we get
	 * complaints in the refactoring wizard of "possible matches"
	 * 
	 * @return
	 */
	private boolean shouldAlwaysBeAccurate() {
		return requestor.getClass().getPackage().getName().indexOf("refactoring") != -1; //$NON-NLS-1$
	}

	private int getAccuracy(TypeConfidence confidence) {
		if (shouldAlwaysBeAccurate()) {
			return SearchMatch.A_ACCURATE;
		}

		switch (confidence) {
			case EXACT:
				return SearchMatch.A_ACCURATE;
			default:
				return SearchMatch.A_INACCURATE;
		}
	}

}
