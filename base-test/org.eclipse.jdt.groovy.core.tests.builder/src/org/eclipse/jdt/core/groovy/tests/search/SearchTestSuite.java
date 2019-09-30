/*
 * Copyright 2009-2019 the original author or authors.
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

import java.util.Arrays;
import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.groovy.tests.MockPossibleMatch;
import org.eclipse.jdt.core.groovy.tests.MockSearchRequestor;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeRequestorFactory;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.junit.Assert;
import org.junit.Before;

public abstract class SearchTestSuite extends BuilderTestSuite {

    MockSearchRequestor searchRequestor;
    protected IProject project;
    protected static final TypeInferencingVisitorFactory factory = new TypeInferencingVisitorFactory();

    @Before
    public final void setUpSearchTestCase() throws Exception {
        project = createSimpleGroovyProject();
        searchRequestor = new MockSearchRequestor();
    }

    protected IProject createSimpleGroovyProject() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addGroovyNature("Project");
        env.addGroovyJars(projectPath);

        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");
        env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");
        IProject proj = env.getProject("Project");
        IJavaProject javaProject = JavaCore.create(proj);
        javaProject.setOption(CompilerOptions.OPTION_Compliance, "1.6");
        javaProject.setOption(CompilerOptions.OPTION_Source, "1.6");
        javaProject.setOption(CompilerOptions.OPTION_TargetPlatform, "1.6");
        fullBuild(projectPath);
        return proj;
    }

    protected GroovyCompilationUnit createUnit(String name, String contents) {
        IPath path = env.addGroovyClassExtension(project.getFolder("src").getFullPath(), name, contents, null);
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }

    protected GroovyCompilationUnit createUnit(String pkg, String name, String contents) throws CoreException {
        IFolder folder = project.getFolder("src").getFolder(new Path(pkg));
        if (!folder.exists()) folder.create(true, true, null);

        IPath path = env.addGroovyClassExtension(folder.getFullPath(), name, contents, null);
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }

    protected ICompilationUnit createJavaUnit(String name, String contents) {
        IPath path = env.addClass(project.getFolder("src").getFullPath(), name, contents);
        return JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }

    protected ICompilationUnit createJavaUnit(String pack, String name, String contents) {
        IPath path = env.addClass(project.getFolder("src").getFullPath(), pack, name, contents);
        return JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }

    protected void assertLocation(SearchMatch match, int start, int length) {
        Assert.assertEquals("Invalid match start for: " + MockPossibleMatch.printMatch(match), start, match.getOffset());
        Assert.assertEquals("Invalid match length for: " + MockPossibleMatch.printMatch(match), length, match.getLength());
    }

    protected static final String FIRST_CONTENTS_CLASS = "class First {}";
    protected static final String FIRST_CONTENTS_INTERFACE = "interface First {}";
    protected static final String FIRST_CONTENTS_CLASS_FOR_METHODS = "class First { def xxx() {}}";
    protected static final String FIRST_CONTENTS_CLASS_FOR_METHODS2 = "class First { def xxx() {} \n def xxx(arg) {}}";

    protected void doTestForTwoTypeReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent)
            throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IType firstType = findType(firstClassName, first);
        SearchPattern pattern = SearchPattern.createPattern(firstType, IJavaSearchConstants.REFERENCES);

        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        IJavaElement firstMatchEnclosingElement;
        IJavaElement secondMatchEnclosingElement;
        if (contentsIsScript) {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
        } else {
            // if not a script, then the first match is always enclosed in the type
            firstMatchEnclosingElement = findType(secondClassName, second);
        }
        // match is enclosed in run method (for script), or nth method for class
        secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];

        checkMatches(secondContents, firstClassName, pattern, second, firstMatchEnclosingElement, secondMatchEnclosingElement);
    }

    protected void doTestForTwoMethodReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent, String matchName)
            throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IMethod firstMethod = (IMethod) findType(firstClassName, first).getChildren()[0];
        SearchPattern pattern = SearchPattern.createPattern(firstMethod, IJavaSearchConstants.REFERENCES);

        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);

        env.fullBuild();
        IJavaElement firstMatchEnclosingElement;
        IJavaElement secondMatchEnclosingElement;
        if (contentsIsScript) {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
            secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
        } else {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
            secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent + 2];
        }
        // match is enclosed in run method (for script), or x method for class

        checkMatches(secondContents, matchName, pattern, second, firstMatchEnclosingElement, secondMatchEnclosingElement);
    }

    protected List<SearchMatch> getAllMatches(String firstContents, String secondContents)
            throws Exception {
        return getAllMatches(firstContents, secondContents, false);
    }

    protected List<SearchMatch> getAllMatches(String firstContents, String secondContents, boolean waitForIndexer)
            throws Exception {
        return getAllMatches(firstContents, secondContents, "", "", waitForIndexer);
    }

    protected List<SearchMatch> getAllMatches(String firstContents, String secondContents, String firstPackage, String secondPackage, boolean waitForIndexer)
            throws Exception {
        String firstClassName = "First";
        String secondClassName = "Second";
        GroovyCompilationUnit first = createUnit(firstPackage, firstClassName, firstContents);
        IType firstType = findType(firstClassName, first);
        SearchPattern pattern = SearchPattern.createPattern(firstType, IJavaSearchConstants.REFERENCES);

        GroovyCompilationUnit second = createUnit(secondPackage, secondClassName, secondContents);

        // saves time if we don't wait
        // only need to do this if we are referencing inner classes
        if (waitForIndexer) {
            waitForIndexer();
        }

        // search the first
        MockPossibleMatch match1 = new MockPossibleMatch(first);
        ITypeRequestor typeRequestor1 = new TypeRequestorFactory().createRequestor(match1, pattern, searchRequestor);
        TypeInferencingVisitorWithRequestor visitor1 = factory.createVisitor(match1);
        visitor1.visitCompilationUnit(typeRequestor1);

        // search the second
        MockPossibleMatch match2 = new MockPossibleMatch(second);
        ITypeRequestor typeRequestor2 = new TypeRequestorFactory().createRequestor(match2, pattern, searchRequestor);
        TypeInferencingVisitorWithRequestor visitor2 = factory.createVisitor(match2);
        visitor2.visitCompilationUnit(typeRequestor2);

        return searchRequestor.getMatches();
    }

    protected IType findType(String firstClassName, GroovyCompilationUnit first) {
        IType type = first.getType(firstClassName);
        if (!type.exists()) {
            try {
                IType[] allTypes = first.getAllTypes();
                for (IType type2 : allTypes) {
                    if (type2.getElementName().equals(firstClassName)) {
                        return type2;
                    }
                }
            } catch (JavaModelException e) {
                e.printStackTrace();
            }
            return null;
        }
        return type;
    }

    protected void doTestForVarReferences(String contents, int offsetInParent, String matchName, int start, MatchRegion[] matchLocations)
            throws JavaModelException {
        String className = "First";
        String matchedVarName = "xxx";
        int until = start + matchedVarName.length() - 1;
        GroovyCompilationUnit unit = createUnit(className, contents);
        JavaElement parent = (JavaElement) findType(className, unit).getChildren()[offsetInParent];
        ILocalVariable var = new LocalVariable(parent, matchedVarName, start, until, start, until, Signature.SIG_INT, null, 0, false);

        SearchPattern pattern = SearchPattern.createPattern(var, IJavaSearchConstants.REFERENCES);
        checkLocalVarMatches(contents, matchName, pattern, unit, matchLocations);
    }

    protected void checkLocalVarMatches(String contents, String matchName, SearchPattern pattern, GroovyCompilationUnit unit, MatchRegion[] matchLocations) {
        MockPossibleMatch match = new MockPossibleMatch(unit);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(match);

        visitor.visitCompilationUnit(typeRequestor);

        Assert.assertEquals("Should have found " + matchLocations.length + " matches, but found: " + searchRequestor.printMatches(),
            matchLocations.length, searchRequestor.getMatches().size());

        for (int i = 0, n = matchLocations.length; i < n; i += 1) {
            assertLocation(searchRequestor.getMatch(i), matchLocations[i].offset, matchLocations[i].length);
        }
    }

    protected void checkMatches(String secondContents, String matchText, SearchPattern pattern, GroovyCompilationUnit second,
            IJavaElement firstMatchEnclosingElement, IJavaElement secondMatchEnclosingElement) {
        MockPossibleMatch match = new MockPossibleMatch(second);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(match);

        visitor.visitCompilationUnit(typeRequestor);

        Assert.assertEquals("Should have found 2 matches, but found: " + searchRequestor.printMatches(), 2, searchRequestor.getMatches().size());

        Assert.assertEquals("Incorrect match in " + searchRequestor.printMatches(), firstMatchEnclosingElement, searchRequestor.getElementNumber(0));
        assertLocation(searchRequestor.getMatch(0), secondContents.indexOf(matchText), matchText.length());

        Assert.assertEquals("Incorrect match in " + searchRequestor.printMatches(), secondMatchEnclosingElement, searchRequestor.getElementNumber(1));
        assertLocation(searchRequestor.getMatch(1), secondContents.lastIndexOf(matchText), matchText.length());
    }

    // TODO: Merge this with SynchronizationUtils
    protected void waitForIndexer(IJavaElement... elements) throws JavaModelException {
        new SearchEngine().searchAllTypeNames(
            null, 0, // no packages
            "XXXXXXXXX".toCharArray(),
            SearchPattern.R_EXACT_MATCH,
            IJavaSearchConstants.CLASS,
            SearchEngine.createJavaSearchScope(elements),
            new TypeNameRequestor() {},
            IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
            null);

        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (!Arrays.asList(
                        "animation start",
                        "decoration calculation",
                        "flush cache job",
                        "open blocked dialog",
                        "sending problem marker updates...",
                        "update for decoration completion",
                        "update dynamic java sources working sets",
                        "update package explorer",
                        "update progress",
                        "usage data event consumer",
                        "workbench auto-save job"
                ).contains(job.getName().toLowerCase())) {
                    boolean interrupted;
                    do {
                        interrupted = false;
                        try {
                            System.err.println("Waiting for: " + job.getName());
                            job.join();
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    } while (interrupted);
                }
            }
        }
    }

    public static void joinUninterruptibly(Job job) {
        boolean interrupted;
        do {
            interrupted = false;
            try {
                System.err.println("Waiting for: " + job.getName());
                job.join();
            } catch (OperationCanceledException ignore) {

            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);
    }

    protected static class MatchRegion {
        final int offset;
        final int length;
        public MatchRegion(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        @Override
        public String toString() {
            return "[ " + offset + " , " + length + " ]";
        }
    }
}
