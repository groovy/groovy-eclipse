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

import static org.eclipse.jdt.core.search.IJavaSearchConstants.CLASS;
import static org.eclipse.jdt.core.search.IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH;
import static org.eclipse.jdt.core.search.SearchEngine.createJavaSearchScope;
import static org.eclipse.jdt.core.search.SearchPattern.R_CASE_SENSITIVE;
import static org.eclipse.jdt.core.search.SearchPattern.R_EXACT_MATCH;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeRequestorFactory;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.LocalVariable;

/**
 * @author Andrew Eisenberg
 * @created Sep 2, 2009
 *
 */
public abstract class AbstractGroovySearchTest extends BuilderTests {
    
    protected class MatchRegion {
        
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
    
    MockSearchRequestor searchRequestor;
    protected IProject project;
    protected TypeInferencingVisitorFactory factory = new TypeInferencingVisitorFactory();

    /**
     * Controls the file extension of the files that are created by this test class
     */
    protected String defaultFileExtension = "groovy";
    
    public AbstractGroovySearchTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("\n------------------------------");
        System.out.println("Starting: " + this.getClass().getName() + "." + this.getName());
        searchRequestor = new MockSearchRequestor();
        project = createSimpleGroovyProject();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ICompilationUnit[] units = JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true);
        if (units != null) {
            for (int i = 0; i < units.length; i++) {
                units[i].discardWorkingCopy();
            }
        }
        defaultFileExtension = "groovy";
    }
    


    protected IProject createSimpleGroovyProject() throws Exception {
        IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
        env.addGroovyNature("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
        IProject proj = env.getProject("Project");
        IJavaProject javaProject = JavaCore.create(proj);
        javaProject.setOption(CompilerOptions.OPTION_Compliance, "1.5");
        javaProject.setOption(CompilerOptions.OPTION_Source, "1.5");
        javaProject.setOption(CompilerOptions.OPTION_TargetPlatform, "1.5");
        fullBuild(projectPath);
        return proj;
    }

    protected GroovyCompilationUnit createUnit(String name, String contents) {
    	IPath path = env.addGroovyClassExtension(project.getFolder("src").getFullPath(), name, contents, defaultFileExtension);
    	fullBuild(project.getFullPath());
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
    

    protected GroovyCompilationUnit createUnit(String pkg, String name, String contents) throws CoreException {
        IFolder folder = project.getFolder("src").getFolder(new Path(pkg));
        if (!folder.exists()) {
            folder.create(true, true, null);
        }
        IPath path = env.addGroovyClassExtension(folder.getFullPath(), name, contents, defaultFileExtension);
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }
    
    protected void assertLocation(SearchMatch match, int start, int length) {
        assertEquals("Invalid match start for: " + MockPossibleMatch.printMatch(match), start, match.getOffset());
        assertEquals("Invalid match length for: " + MockPossibleMatch.printMatch(match), length, match.getLength());
    }
    
    
    
    protected final static String FIRST_CONTENTS_CLASS = "class First {}";
    protected final static String FIRST_CONTENTS_INTERFACE = "interface First {}";
    protected final static String FIRST_CONTENTS_CLASS_FOR_FIELDS = "class First { def xxx }";
    protected final static String FIRST_CONTENTS_CLASS_FOR_METHODS = "class First { def xxx() { } }";
    protected final static String FIRST_CONTENTS_CLASS_FOR_METHODS2 = "class First { def xxx() { } \n def xxx(arg) { } }";

    
    
    protected void doTestForTwoTypeReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent) throws JavaModelException {
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
            // if not a script, then the first match is always enclosed in the type, 
            firstMatchEnclosingElement = findType(secondClassName, second);
        }
        // match is enclosed in run method (for script), or x method for class
        secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
        
        checkMatches(secondContents, firstClassName, pattern, second,
                firstMatchEnclosingElement, secondMatchEnclosingElement);
    }

    protected void doTestForTwoFieldReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent, String matchName) throws JavaModelException {
        doTestForTwoFieldReferences(firstContents, secondContents, contentsIsScript, offsetInParent, matchName, IJavaSearchConstants.REFERENCES);
    }
    protected void doTestForTwoFieldReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent, String matchName, int searchFlags) throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        String matchedFieldName = "xxx";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IField firstField = findType(firstClassName, first).getField(matchedFieldName);
        SearchPattern pattern = SearchPattern.createPattern(firstField, searchFlags);
        
        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        IJavaElement firstMatchEnclosingElement;
        IJavaElement secondMatchEnclosingElement;
        if (contentsIsScript) {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
            secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
        } else {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
            secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent+2];
        }
        // match is enclosed in run method (for script), or x method for class
        
        checkMatches(secondContents, matchName, pattern, second,
                firstMatchEnclosingElement, secondMatchEnclosingElement);
    }
    
    // as above, but enclosing element is always the first child of the enclosing type
    protected void doTestForTwoFieldReferencesInGString(String firstContents, String secondContents, String matchName) throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        String matchedFieldName = "xxx";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IField firstField = findType(firstClassName, first).getField(matchedFieldName);
        SearchPattern pattern = SearchPattern.createPattern(firstField, IJavaSearchConstants.REFERENCES);

        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        IJavaElement firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[0];
        IJavaElement secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[0];

        checkMatches(secondContents, matchName, pattern, second,
                firstMatchEnclosingElement, secondMatchEnclosingElement);
    }
    protected void doTestForTwoMethodReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent, String matchName) throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        String matchedMethodName = "xxx";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IMethod firstField = findType(firstClassName, first).getMethod(matchedMethodName, new String[0]);
        SearchPattern pattern = SearchPattern.createPattern(firstField, IJavaSearchConstants.REFERENCES);
        
        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        IJavaElement firstMatchEnclosingElement;
        IJavaElement secondMatchEnclosingElement;
        if (contentsIsScript) {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
            secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
        } else {
            firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
            secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent+2];
        }
        // match is enclosed in run method (for script), or x method for class
        
        checkMatches(secondContents, matchName, pattern, second,
                firstMatchEnclosingElement, secondMatchEnclosingElement);
    }
    

    protected List<SearchMatch> getAllMatches(String firstContents, String secondContents) throws JavaModelException {
        return getAllMatches(firstContents, secondContents, false);
    }
    protected List<SearchMatch> getAllMatches(String firstContents, String secondContents, boolean waitForIndexer) throws JavaModelException {
        String firstClassName = "First";
        String secondClassName = "Second";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IType firstType = findType(firstClassName, first);
        SearchPattern pattern = SearchPattern.createPattern(firstType, IJavaSearchConstants.REFERENCES);
        
        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);

        
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
    
    @SuppressWarnings("deprecation")
    private void waitForIndexer() throws JavaModelException {
        final TypeNameRequestor requestor = new TypeNameRequestor() {};
        new SearchEngine().searchAllTypeNames(null, null, R_EXACT_MATCH
                | R_CASE_SENSITIVE, CLASS,
                createJavaSearchScope(new IJavaElement[0]), requestor,
                WAIT_UNTIL_READY_TO_SEARCH, null);
    }


    private IType findType(String firstClassName, GroovyCompilationUnit first) {
        IType type = first.getType(firstClassName);
        if (! type.exists()) {
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
    
    
    protected void doTestForVarReferences(String contents, int offsetInParent, String matchName, int declStart, MatchRegion[] matchLocations) throws JavaModelException {
        String className = "First";
        String matchedVarName = "xxx";
        GroovyCompilationUnit unit = createUnit(className, contents);
        // Will need to call via reflection so can work on either.
        // 3.6 version:
        ILocalVariable var = ReflectionUtils.createLocalVariable(findType(className, unit).getChildren()[offsetInParent], matchedVarName, declStart, Signature.SIG_INT);
        SearchPattern pattern = SearchPattern.createPattern(var, IJavaSearchConstants.REFERENCES);
        
        checkLocalVarMatches(contents, matchName, pattern, unit, matchLocations);
    }


    
    

    /**
     * @param contents
     * @param matchName
     * @param pattern
     * @param unit
     * @param matchLocations
     */
    private void checkLocalVarMatches(String contents, String matchName,
            SearchPattern pattern, GroovyCompilationUnit unit, 
            MatchRegion[] matchLocations) {
        MockPossibleMatch match = new MockPossibleMatch(unit);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(match);
        
        visitor.visitCompilationUnit(typeRequestor);
        
        assertEquals("Should have found " + matchLocations.length + " matches, but found: " + searchRequestor.printMatches(), 
                matchLocations.length, searchRequestor.matches.size());

        for (int i = 0; i < matchLocations.length; i++) {
            assertLocation(searchRequestor.getMatch(i), matchLocations[i].offset, matchLocations[i].length);
        }
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
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(match);
        
        visitor.visitCompilationUnit(typeRequestor);
        
        assertEquals("Should have found 2 matches, but found: " + searchRequestor.printMatches(), 
                2, searchRequestor.matches.size());
        
        assertEquals("Incorrect match in " + searchRequestor.printMatches(), firstMatchEnclosingElement, searchRequestor.getElementNumber(0));
        assertLocation(searchRequestor.getMatch(0), secondContents.indexOf(matchText), matchText.length());
        assertEquals("Incorrect match in " + searchRequestor.printMatches(), secondMatchEnclosingElement, searchRequestor.getElementNumber(1));
        assertLocation(searchRequestor.getMatch(1), secondContents.lastIndexOf(matchText), matchText.length());
    }
}
