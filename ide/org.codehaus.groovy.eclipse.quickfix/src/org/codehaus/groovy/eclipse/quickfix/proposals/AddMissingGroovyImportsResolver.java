/*
 * Copyright 2009-2017 the original author or authors.
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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.codeassist.relevance.IRelevanceRule;
import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.codehaus.groovy.eclipse.refactoring.actions.TypeSearch;
import org.codehaus.groovy.eclipse.refactoring.actions.TypeSearch.UnresolvedTypeData;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
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
 */
public class AddMissingGroovyImportsResolver extends AbstractQuickFixResolver {

    public AddMissingGroovyImportsResolver(QuickFixProblemContext problem) {
        super(problem);
    }

    public static class AddMissingImportProposal extends AbstractGroovyQuickFixProposal {

        private IType resolvedSuggestedType;
        private GroovyCompilationUnit unit;

        public AddMissingImportProposal(IType resolvedSuggestedType, GroovyCompilationUnit unit, QuickFixProblemContext problem, int relevance) {
            super(problem, relevance);
            this.resolvedSuggestedType = resolvedSuggestedType;
            this.unit = unit;
        }

        public IType getSuggestedJavaType() {
            return resolvedSuggestedType;
        }

        protected String getImageBundleLocation() {
            return JavaPluginImages.IMG_OBJS_IMPDECL;
        }

        protected ImportRewrite getImportRewrite() {
            ImportRewrite rewriter = null;
            try {
                rewriter = ImportRewrite.create(unit, true);
            } catch (Exception e) {
                GroovyQuickFixPlugin.log(e);
            }
            return rewriter;
        }

        public void apply(IDocument document) {
            ImportRewrite rewrite = getImportRewrite();
            if (rewrite != null) {
                rewrite.addImport(getSuggestedJavaType().getFullyQualifiedName('.'));
                try {
                    TextEdit edit = rewrite.rewriteImports(null);
                    if (edit != null) {
                        unit.applyTextEdit(edit, null);
                    }
                } catch (Exception e) {
                    GroovyQuickFixPlugin.log(e);
                }
            }
        }

        public String getDisplayString() {
            IType declaringType = getSuggestedJavaType().getDeclaringType();
            // For inner types, display the fully qualified top-level type as
            // the declaration for the suggested type
            String declaration = declaringType != null
                ? declaringType.getFullyQualifiedName().replace('$', '.')
                : getSuggestedJavaType().getPackageFragment().getElementName();
            return "Import '" + getSuggestedJavaType().getElementName() + "' (" + declaration + ")";
        }
    }

    protected ProblemType[] getTypes() {
        return new ProblemType[] {ProblemType.MISSING_IMPORTS_TYPE};
    }

    /**
     * Returns the type suggestions that may resolve the unresolved type problem.
     *
     * @return list of type suggestions for the unresolved type, or null if nothing is found
     */
    protected List<IType> getImportTypeSuggestions() {
        int offset = getQuickFixProblem().getOffset();
        try {
            String simpleTypeName = getUnresolvedSimpleName();
            if (simpleTypeName != null) {
                boolean isAnnotation = getQuickFixProblem().getProblemDescriptor().getMarkerMessages()[0].contains("@" + simpleTypeName);
                UnresolvedTypeData data = new UnresolvedTypeData(simpleTypeName, isAnnotation, new SourceRange(offset, simpleTypeName.length()));

                new TypeSearch().searchForTypes(getGroovyCompilationUnit(), Collections.singletonMap(simpleTypeName, data), null);

                List<TypeNameMatch> matches = data.getFoundInfos();
                if (matches != null && !matches.isEmpty()) {
                    List<IType> suggestions = new ArrayList<IType>(matches.size());
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
     * Obtains the simple name of the unresolved type from the quick fix problem
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
        String[] messages = getQuickFixProblem().getProblemDescriptor().getMarkerMessages();
        if (messages == null || messages.length == 0) {
            return null;
        }

        for (String text : ProblemType.MISSING_IMPORTS_TYPE.groovyProblemSnippets) {
            int startIndex = messages[0].indexOf(text);
            if (startIndex >= 0) {
                Pattern pattern = Pattern.compile("\\b\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\b");
                Matcher matcher = pattern.matcher(messages[0].substring(startIndex + text.length()));
                if (matcher.find()) {
                    return getTopLevelType(matcher.group());
                }
            }
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

    public List<IJavaCompletionProposal> getQuickFixProposals() {
        List<IType> suggestions = getImportTypeSuggestions();
        if (suggestions != null && !suggestions.isEmpty()) {
            List<IJavaCompletionProposal> fixes = new ArrayList<IJavaCompletionProposal>(suggestions.size());
            for (IType type : suggestions) {
                fixes.add(new AddMissingImportProposal(type, getGroovyCompilationUnit(), getQuickFixProblem(), getRelevance(type)));
            }
            return fixes;
        }
        return null;
    }

    /**
     * @return non-null Groovy compilation unit containing the problem that this
     *         resolver should fix.
     */
    protected GroovyCompilationUnit getGroovyCompilationUnit() {
        return (GroovyCompilationUnit) getQuickFixProblem().getCompilationUnit();
    }

    protected int getRelevance(IType type) {
        return (type == null ? 0 : IRelevanceRule.DEFAULT.getRelevance(type, getContextTypes()));
    }
}
