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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Dec 1, 2009
 * 
 */
public class TypeDeclarationSearchRequestor implements ITypeRequestor, IIndexConstants {

	private final String simpleName;
	private final char typeSuffix;

	private final SearchRequestor requestor;
	private final SearchParticipant participant;

	// no need to search pattern because by the time we get here, the pattern has been checked.
	public TypeDeclarationSearchRequestor(TypeDeclarationPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
		simpleName = new String(pattern.simpleName);
		typeSuffix = pattern.typeSuffix;
		this.requestor = requestor;
		this.participant = participant;
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		if (node instanceof ClassNode) {
			ClassNode orig = (ClassNode) node;
			ClassNode redirect = orig.redirect();
			if (orig == redirect) {
				if (orig.getNameWithoutPackage().equalsIgnoreCase(simpleName)) {
					boolean matchFound;
					switch (typeSuffix) {
						case CLASS_SUFFIX:
							matchFound = isClass(orig);
							break;
						case CLASS_AND_INTERFACE_SUFFIX:
							matchFound = orig.isInterface() || isClass(orig);
							break;
						case CLASS_AND_ENUM_SUFFIX:
							matchFound = orig.isInterface() || orig.isEnum();
							break;
						case INTERFACE_SUFFIX:
							matchFound = orig.isInterface();
							break;
						case INTERFACE_AND_ANNOTATION_SUFFIX:
							matchFound = orig.isInterface() || orig.isAnnotationDefinition();
							break;
						case ENUM_SUFFIX:
							matchFound = orig.isEnum();
							break;
						case ANNOTATION_TYPE_SUFFIX:
							matchFound = orig.isAnnotationDefinition();
							break;
						default:
							matchFound = true;
							break;
					}
					if (matchFound) {
						try {
							requestor.acceptSearchMatch(new TypeDeclarationMatch(enclosingElement, SearchMatch.A_ACCURATE, orig
									.getNameStart(), orig.getNameEnd() - orig.getNameStart() + 1, participant, enclosingElement
									.getResource()));
						} catch (CoreException e) {
							Util.log(e, "Exception with groovy search requestor");
						}
					}
				}
			}
		}
		return VisitStatus.CONTINUE;
	}

	/**
	 * @param orig
	 * @return
	 */
	private boolean isClass(ClassNode orig) {
		return !orig.isInterface() && !orig.isAnnotationDefinition() && !orig.isEnum();
	}
}
