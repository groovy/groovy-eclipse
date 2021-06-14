/*
 * Copyright 2009-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeRequestorFactory;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.search.JavaSearchDocument;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.junit.Assert;
import org.junit.Before;

public abstract class SearchTestSuite extends BuilderTestSuite {

    private SearchRequestor searchRequestor = new SearchRequestor() {
        @Override
        public void acceptSearchMatch(final SearchMatch match) {
            matches.add(match);
        }
    };

    private final List<SearchMatch> matches = new ArrayList<>();

    protected IProject project;

    @Before
    public final void setUpSearchTestCase() throws Exception {
        project = createGroovyProject();
    }

    protected IProject createGroovyProject() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addGroovyJars(projectPath);
        env.fullBuild(projectPath);

        return env.getProject("Project");
    }

    protected GroovyCompilationUnit createUnit(final String name, final String contents) {
        IPath path = env.addGroovyClass(project.getFolder("src").getFullPath(), name, contents);
        return (GroovyCompilationUnit) env.getUnit(path);
    }

    protected GroovyCompilationUnit createUnit(final String pack, final String name, final String contents) {
        IFolder folder = project.getFolder("src").getFolder(new Path(pack));
        if (!folder.exists()) {
            try {
                folder.create(true, true, null);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        IPath path = env.addGroovyClass(folder.getFullPath(), name, contents);
        return (GroovyCompilationUnit) env.getUnit(path);
    }

    protected ICompilationUnit createJavaUnit(final String name, final String contents) {
        IPath path = env.addClass(project.getFolder("src").getFullPath(), name, contents);
        return env.getUnit(path);
    }

    protected ICompilationUnit createJavaUnit(final String pack, final String name, final String contents) {
        IPath path = env.addClass(project.getFolder("src").getFullPath(), pack, name, contents);
        return env.getUnit(path);
    }

    //--------------------------------------------------------------------------

    protected List<SearchMatch> search(final SearchPattern pattern, final IJavaSearchScope scope) throws CoreException {
        SearchParticipant[] participants = {SearchEngine.getDefaultSearchParticipant()};
        new SearchEngine().search(pattern, participants, scope, searchRequestor, null);
        matches.sort(new MatchComparator());
        return matches;
    }

    protected List<SearchMatch> search(final SearchPattern pattern, final ICompilationUnit unit) { waitUntilReady(unit);
        SearchDocument searchDocument = new JavaSearchDocument(unit.getResource().getFullPath().toPortableString(), SearchEngine.getDefaultSearchParticipant());
        PossibleMatch possibleMatch = new PossibleMatch(/*matchLocator:*/null, unit.getResource(), (Openable) unit, searchDocument, /*mustResolve:*/false);
        ITypeRequestor requestor = new TypeRequestorFactory().createRequestor(possibleMatch, pattern, searchRequestor);
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(possibleMatch);
        visitor.debug = true; // enable console output and post-visit assertions
        visitor.visitCompilationUnit(requestor);
        matches.sort(new MatchComparator());
        return matches;
    }

    //--------------------------------------------------------------------------

    protected static final String FIRST_CONTENTS_CLASS = "class First {}";
    protected static final String FIRST_CONTENTS_INTERFACE = "interface First {}";
    protected static final String FIRST_CONTENTS_CLASS_FOR_METHODS = "class First { def xxx() {}}";
    protected static final String FIRST_CONTENTS_CLASS_FOR_METHODS2 = "class First { def xxx() {} \n def xxx(arg) {}}";

    protected void doTestForTwoTypeReferences(final String firstContents, final String secondContents, final boolean secondIsScript,
            final int memberIndex) throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        IJavaElement firstMatchEnclosingElement;
        IJavaElement secondMatchEnclosingElement;
        if (secondIsScript) {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[memberIndex];
        } else {
            // if not a script, then the first match is always enclosed in the type
            firstMatchEnclosingElement = findType(secondClassName, second);
        }
        // match is enclosed in run method (for script), or nth method for class
        secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[memberIndex];

        IType firstType = findType(firstClassName, first);
        SearchPattern pattern = SearchPattern.createPattern(firstType, IJavaSearchConstants.REFERENCES);
        checkMatches(secondContents, firstClassName, pattern, second, firstMatchEnclosingElement, secondMatchEnclosingElement);
    }

    protected void doTestForTwoMethodReferences(final String firstContents, final String secondContents, final boolean secondIsScript,
            final int memberIndex, final String matchName) throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);

        IJavaElement firstMatchEnclosingElement;
        IJavaElement secondMatchEnclosingElement;
        if (secondIsScript) {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[memberIndex];
            secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[memberIndex];
        } else {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[memberIndex];
            secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[memberIndex + 2];
        }
        // match is enclosed in run method (for script), or x method for class

        IMethod firstMethod = (IMethod) findType(firstClassName, first).getChildren()[0];
        SearchPattern pattern = SearchPattern.createPattern(firstMethod, IJavaSearchConstants.REFERENCES);
        checkMatches(secondContents, matchName, pattern, second, firstMatchEnclosingElement, secondMatchEnclosingElement);
    }

    protected void checkMatches(final String secondContents, final String matchText, final SearchPattern pattern, final GroovyCompilationUnit second,
            final IJavaElement firstEnclosingElement, final IJavaElement secondEnclosingElement) {
        search(pattern, second);

        Assert.assertEquals("Should have found 2 matches, but found: " + toString(matches), 2, matches.size());

        Assert.assertEquals("Incorrect match in " + toString(matches), firstEnclosingElement, matches.get(0).getElement());
        assertLocation(matches.get(0), secondContents.indexOf(matchText), matchText.length());

        Assert.assertEquals("Incorrect match in " + toString(matches), secondEnclosingElement, matches.get(1).getElement());
        assertLocation(matches.get(1), secondContents.lastIndexOf(matchText), matchText.length());
    }

    protected List<SearchMatch> getAllMatches(final String firstContents, final String secondContents) {
        return getAllMatches(firstContents, secondContents, "", "");
    }

    protected List<SearchMatch> getAllMatches(final String firstContents, final String secondContents, final String firstPackage, final String secondPackage) {
        String firstClassName = "First";
        String secondClassName = "Second";
        GroovyCompilationUnit first = createUnit(firstPackage, firstClassName, firstContents);
        GroovyCompilationUnit second = createUnit(secondPackage, secondClassName, secondContents);
        SearchPattern pattern = SearchPattern.createPattern(findType(firstClassName, first), IJavaSearchConstants.REFERENCES);

        search(pattern, first);
        search(pattern, second);

        return matches;
    }

    protected void doTestForVarReferences(final String contents, final int memberIndex, final int start, final MatchRegion[] matchRegions)
            throws JavaModelException {
        String className = "First";
        String matchedVarName = "xxx";
        int until = start + matchedVarName.length() - 1;
        GroovyCompilationUnit unit = createUnit(className, contents);
        JavaElement parent = (JavaElement) findType(className, unit).getChildren()[memberIndex];
        ILocalVariable var = new LocalVariable(parent, matchedVarName, start, until, start, until, "I", null, 0, false);

        search(SearchPattern.createPattern(var, IJavaSearchConstants.REFERENCES), unit);

        Assert.assertEquals("Should have found " + matchRegions.length + " matches, but found: " + toString(matches), matchRegions.length, matches.size());

        for (int i = 0, n = matchRegions.length; i < n; i += 1) {
            assertLocation(matches.get(i), matchRegions[i].offset, matchRegions[i].length);
        }
    }

    protected static void assertLocation(final SearchMatch match, final int offset, final int length) {
        Assert.assertEquals("Invalid match start for: " + toString(match), offset, match.getOffset());
        Assert.assertEquals("Invalid match length for: " + toString(match), length, match.getLength());
    }

    protected static IType findType(final String typeName, final ICompilationUnit unit) {
        IType type = unit.getType(typeName);
        if (type.exists()) {
            return type;
        }

        try {
            for (IType t : unit.getAllTypes()) {
                if (t.getElementName().equals(typeName)) {
                    return t;
                }
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static final String toString(final List<SearchMatch> matches) {
        return matches.stream().map(SearchTestSuite::toString).collect(java.util.stream.Collectors.joining());
    }

    protected static final String toString(final SearchMatch match) {
        return "Match at: (" + match.getOffset() + ", " + match.getLength() + ")," + " accuracy: " +
            (match.getAccuracy() == SearchMatch.A_ACCURATE ? "ACCURATE" : "INACCURATE") + "\n Matched object: " + match.getElement() + "\n";
    }

    public static void waitUntilReady(final IJavaElement... elems) {
        try {
            new SearchEngine().searchAllTypeNames(
                /*packageName:*/null,
                SearchPattern.R_EXACT_MATCH,
                "XXXXXXXXXXX".toCharArray(),
                SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
                IJavaSearchConstants.CLASS,
                SearchEngine.createJavaSearchScope(elems),
                new TypeNameRequestor() {},
                IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
                /*progressMonitor:*/null);
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
    }

    public static class MatchComparator implements Comparator<SearchMatch> {
        @Override
        public int compare(final SearchMatch m1, final SearchMatch m2) {
            ITypeRoot lTypeRoot = (ITypeRoot) ((IJavaElement) m1.getElement()).getAncestor(IJavaElement.COMPILATION_UNIT);
            if (lTypeRoot == null) {
                lTypeRoot = (ITypeRoot) ((IJavaElement) m1.getElement()).getAncestor(IJavaElement.CLASS_FILE);
            }
            ITypeRoot rTypeRoot = (ITypeRoot) ((IJavaElement) m2.getElement()).getAncestor(IJavaElement.COMPILATION_UNIT);
            if (rTypeRoot == null) {
                rTypeRoot = (ITypeRoot) ((IJavaElement) m2.getElement()).getAncestor(IJavaElement.CLASS_FILE);
            }
            if (!lTypeRoot.equals(rTypeRoot)) {
                return lTypeRoot.getElementName().compareTo(rTypeRoot.getElementName());
            }
            return m1.getOffset() - m2.getOffset();
        }
    }

    public static class MatchRegion {
        final int offset;
        final int length;

        public MatchRegion(final int offset, final int length) {
            this.offset = offset;
            this.length = length;
        }

        @Override
        public String toString() {
            return "[ " + offset + " , " + length + " ]";
        }
    }
}
