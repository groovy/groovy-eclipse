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

package org.eclipse.jdt.core.groovy.tests.search;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeRequestorFactory;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * @author Andrew Eisenberg
 * @created Sep 2, 2009
 *
 */
public abstract class AbstractGroovySearchTest extends BuilderTests {
    MockSearchRequestor searchRequestor;
    IProject project;
    TypeInferencingVisitorFactory factory = new TypeInferencingVisitorFactory();

    public AbstractGroovySearchTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        searchRequestor = new MockSearchRequestor();
        project = createSimpleGroovyProject();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ICompilationUnit[] units = JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true);
        for (int i = 0; i < units.length; i++) {
            units[i].discardWorkingCopy();
        }
    }
    


    protected IProject createSimpleGroovyProject() throws JavaModelException {
        IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        fullBuild(projectPath);
        
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
        return env.getProject("Project");
    }

    protected GroovyCompilationUnit createUnit(String name, String contents) {
    	IPath path = env.addGroovyClass(project.getFile("src").getFullPath(), name, contents);
    	return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }

    protected void assertLocation(SearchMatch match, int start, int length) {
        assertEquals("Invalid match start for: " + MockPossibleMatch.printMatch(match), start, match.getOffset());
        assertEquals("Invalid match length for: " + MockPossibleMatch.printMatch(match), length, match.getLength());
    }
    
    
    
    protected final static String FIRST_CONTENTS_CLASS = "class First {}";
    protected final static String FIRST_CONTENTS_INTERFACE = "interface First {}";

    protected void doTestForTwoTypeReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent) throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IType firstType = first.getType(firstClassName);
        SearchPattern pattern = SearchPattern.createPattern(firstType, IJavaSearchConstants.REFERENCES);
        
        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        IJavaElement firstMatchEnclosingElement;
        IJavaElement secondMatchEnclosingElement;
        if (contentsIsScript) {
            firstMatchEnclosingElement = second.getType(secondClassName).getChildren()[offsetInParent];
        } else {
            // if not a script, then the first match is always enclosed in the type, 
            firstMatchEnclosingElement = second.getType(secondClassName);
        }
        // match is enclosed in run method (for script), or x method for class
        secondMatchEnclosingElement = second.getType(secondClassName).getChildren()[offsetInParent];
        
        checkMatches(secondContents, firstClassName, pattern, second,
                firstMatchEnclosingElement, secondMatchEnclosingElement);
    }

    protected final static String FIRST_CONTENTS_CLASS_FOR_FIELDS = "class First { def xxx }";

    protected void doTestForTwoFieldReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent, String matchName) throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        String matchedFieldName = "xxx";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IField firstField = first.getType(firstClassName).getField(matchedFieldName);
        SearchPattern pattern = SearchPattern.createPattern(firstField, IJavaSearchConstants.REFERENCES);

        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        IJavaElement firstMatchEnclosingElement;
        IJavaElement secondMatchEnclosingElement;
        if (contentsIsScript) {
            firstMatchEnclosingElement = second.getType(secondClassName).getChildren()[offsetInParent];
            secondMatchEnclosingElement = second.getType(secondClassName).getChildren()[offsetInParent];
        } else {
            firstMatchEnclosingElement = second.getType(secondClassName).getChildren()[offsetInParent];
            secondMatchEnclosingElement = second.getType(secondClassName).getChildren()[offsetInParent+2];
        }
        // match is enclosed in run method (for script), or x method for class

        checkMatches(secondContents, matchName, pattern, second,
                firstMatchEnclosingElement, secondMatchEnclosingElement);
    }

    
    /**
     * @param secondContents
     * @param matchText
     * @param pattern
     * @param second
     * @param firstMatchEnclosingElement
     * @param secondMatchEnclosingElement
     */
    private void checkMatches(String secondContents, String matchText,
            SearchPattern pattern, GroovyCompilationUnit second,
            IJavaElement firstMatchEnclosingElement, IJavaElement secondMatchEnclosingElement) {
        MockPossibleMatch match = new MockPossibleMatch(second);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(typeRequestor, match, pattern, searchRequestor);
        
        visitor.visitCompilationUnit(typeRequestor);
        
        assertEquals("Should have found 2 matches, but found: " + searchRequestor.printMatches(), 
                2, searchRequestor.matches.size());
        
        assertEquals("Incorrect match in " + searchRequestor.printMatches(), firstMatchEnclosingElement, searchRequestor.getElementNumber(0));
        assertLocation(searchRequestor.getMatch(0), secondContents.indexOf(matchText), matchText.length());
        assertEquals("Incorrect match in " + searchRequestor.printMatches(), secondMatchEnclosingElement, searchRequestor.getElementNumber(1));
        assertLocation(searchRequestor.getMatch(1), secondContents.lastIndexOf(matchText), matchText.length());
    }
    
}
