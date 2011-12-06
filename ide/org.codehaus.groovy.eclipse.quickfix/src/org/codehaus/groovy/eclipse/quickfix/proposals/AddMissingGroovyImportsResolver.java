/*
 * Copyright 2010-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.codeassist.relevance.RelevanceRules;
import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports;
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports.UnresolvedTypeData;
import org.codehaus.groovy.eclipse.refactoring.actions.TypeSearch;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Generates quick fix proposals for a unresolved type in a Groovy class. Each
 * proposal is associated with a Java IType that can be imported to resolve the
 * unresolved type. Therefore, if the resolver finds n types that can possibly
 * be imported, the resolver will generate 5 different proposals, one for each
 * suggested type. Each proposal has its own display string indicating which
 * type will be imported.
 * 
 * @author Nieraj Singh
 * 
 */
public class AddMissingGroovyImportsResolver extends AbstractQuickFixResolver {

	public AddMissingGroovyImportsResolver(QuickFixProblemContext problem) {
		super(problem);
	}

	public static class AddMissingImportProposal extends
			AbstractGroovyQuickFixProposal {

		private IType resolvedSuggestedType;

		private GroovyCompilationUnit unit;

		public AddMissingImportProposal(IType resolvedSuggestedType,
				GroovyCompilationUnit unit, QuickFixProblemContext problem,
				int relevance) {
			super(problem, relevance);
			this.resolvedSuggestedType = resolvedSuggestedType;
			this.unit = unit;
		}

		public IType getSuggestedJavaType() {
			return resolvedSuggestedType;
		}

		protected String getImageBundleLocation() {
			return org.eclipse.jdt.internal.ui.JavaPluginImages.IMG_OBJS_IMPDECL;
		}

		protected greclipse.org.eclipse.jdt.core.dom.rewrite.ImportRewrite getImportRewrite() {
			greclipse.org.eclipse.jdt.core.dom.rewrite.ImportRewrite rewriter = null;
			try {
				rewriter = greclipse.org.eclipse.jdt.core.dom.rewrite.ImportRewrite
						.create(unit, true);
			} catch (JavaModelException e) {
				GroovyQuickFixPlugin.log(e);
			}
			return rewriter;
		}

		public void apply(IDocument document) {

			greclipse.org.eclipse.jdt.core.dom.rewrite.ImportRewrite rewrite = getImportRewrite();

			if (rewrite != null) {
				rewrite.addImport(getSuggestedJavaType().getFullyQualifiedName(
						'.'));
				try {
					TextEdit edit = rewrite.rewriteImports(null);

					if (edit != null) {
						unit.applyTextEdit(edit, null);
					}
				} catch (JavaModelException e) {
					GroovyQuickFixPlugin.log(e);
				} catch (CoreException e) {
					GroovyQuickFixPlugin.log(e);
				}
			}
		}

		public String getDisplayString() {
			IType declaringType = getSuggestedJavaType().getDeclaringType();
			// For inner types, display the fully qualified top-level type as
			// the declaration for the suggested type
			String declaration = declaringType != null ? declaringType
					.getFullyQualifiedName().replace('$', '.')
					: getSuggestedJavaType().getPackageFragment()
							.getElementName();
			return "Import '" + getSuggestedJavaType().getElementName() + "' ("
					+ declaration + ")";
		}
	}

	protected ProblemType[] getTypes() {
		return new ProblemType[] { ProblemType.MISSING_IMPORTS_TYPE };
	}

	/**
	 * Return the type suggestions that may resolve the unresolved type problem.
	 * 
	 * @return list of type suggestions for the unresolved type, or null if
	 *         nothing is found
	 */
	protected List<IType> getImportTypeSuggestions() {
		int offset = getQuickFixProblem().getOffset();
		try {

			String simpleTypeName = getUnresolvedSimpleName();
			if (simpleTypeName != null) {
				Map<String, OrganizeGroovyImports.UnresolvedTypeData> unresolvedTypes = new HashMap<String, OrganizeGroovyImports.UnresolvedTypeData>();
				unresolvedTypes.put(simpleTypeName, new UnresolvedTypeData(
						simpleTypeName, false, new SourceRange(offset,
								simpleTypeName.length())));

				new TypeSearch().searchForTypes(getGroovyCompilationUnit(),
						unresolvedTypes);

				UnresolvedTypeData foundData = unresolvedTypes
						.get(simpleTypeName);

				List<TypeNameMatch> matches = foundData.getFoundInfos();
				if (matches != null) {
					List<IType> suggestions = new ArrayList<IType>();
					for (TypeNameMatch match : matches) {
						suggestions.add(match.getType());
					}
					return suggestions;
				}
			}

		} catch (JavaModelException e) {
			GroovyQuickFixPlugin.log(e);
		}
		return null;

	}

	/**
	 * Obtain the simple name of the unresolved type from the quick fix problem
	 * 
	 * @return simple name of the unresolved type.
	 */
	protected String getUnresolvedSimpleName() {
		// NOTE: for now get the type from the message String. Not elegant, but
		// computationally and logically much simpler than checking the AST for
		// the correct declaration node that may contain unresolved type
		// information
		// especially since the location information for the problem does not
		// directly point to the actual unresolved type in source, but rather
		// surrounding identifiers like variable names, method names, and even
		// Java key words.
		String[] messages = getQuickFixProblem().getProblemDescriptor()
				.getMarkerMessages();
		if (messages == null || messages.length == 0) {
			return null;
		}

		StringBuffer errorMessage = new StringBuffer(messages[0]);
		int prefixIndex = errorMessage
				.indexOf(ProblemType.MISSING_IMPORTS_TYPE.groovyProblemSnippets[0]);
		if (prefixIndex >= 0) {
            errorMessage
                    .delete(prefixIndex,
                            prefixIndex
                                    + ProblemType.MISSING_IMPORTS_TYPE.groovyProblemSnippets[0]
                                            .length());

			// Strip starting whitespace
			for (; errorMessage.length() > 0;) {
				if (Character.isWhitespace(errorMessage.charAt(0))) {
					errorMessage.deleteCharAt(0);
				} else {
					break;
				}
			}

			// Strip trailing whitespace
			for (; errorMessage.length() > 0;) {
				int lastIndex = errorMessage.length() - 1;
				if (Character.isWhitespace(errorMessage.charAt(lastIndex))) {
					errorMessage.deleteCharAt(lastIndex);
				} else {
					break;
				}
			}

			return getTopLevelType(errorMessage.toString());
		}
		return null;
	}

	/**
	 * If the simple name is an Inner Type, this will return the top level type.
	 * If it already is a top level type, the name will be returned as is. For
	 * inner types that are declared with the declaring (top level) type (e.g.
	 * Map.Entry), only the top level type is needed to import the inner type.
	 * 
	 * @param simpleName
	 *            whose top level type should be obtained
	 * @return top level simple name of the given simple name. If it already is
	 *         top level, return the same simple name that was passed as a
	 *         argument.
	 */
	protected static String getTopLevelType(String simpleName) {
		int firstIndex = simpleName != null ? simpleName.indexOf('.') : -1;

		if (firstIndex >= 0) {
			simpleName = simpleName.substring(0, firstIndex);
		}
		return simpleName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver#
	 * getQuickFixProposals()
	 */
	public List<IJavaCompletionProposal> getQuickFixProposals() {
		List<IType> suggestions = getImportTypeSuggestions();
		if (suggestions != null) {
			List<IJavaCompletionProposal> fixes = new ArrayList<IJavaCompletionProposal>();
			for (IType type : suggestions) {
				int revelance = getRelevance(type);
				fixes.add(new AddMissingImportProposal(type,
						getGroovyCompilationUnit(), getQuickFixProblem(),
						revelance));
			}
			return fixes;
		}

		return null;
	}

	/**
	 * 
	 * @return non-null Groovy compilation unit containing the problem that this
	 *         resolver should fix.
	 */
	protected GroovyCompilationUnit getGroovyCompilationUnit() {
		return (GroovyCompilationUnit) getQuickFixProblem()
				.getCompilationUnit();
	}

	protected int getRelevance(IType type) {
		if (type == null) {
			return 0;
		}
		return RelevanceRules.ALL_RULES.getRelevance(type, getContextTypes());
	}

}
