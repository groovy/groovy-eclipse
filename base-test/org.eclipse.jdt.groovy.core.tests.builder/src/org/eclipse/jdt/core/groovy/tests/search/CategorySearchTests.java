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

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeRequestorFactory;

/**
 * @author Andrew Eisenberg
 * @created Oct 29, 2009
 *
 * searches that make use of categories
 */
public class CategorySearchTests extends AbstractGroovySearchTest {

    public CategorySearchTests(String name) {
        super(name);
    }
    
    public static Test suite() {
        return buildTestSuite(CategorySearchTests.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    private static String CATEGORY_DEFN = 
        "class Cat {\n" +
        "  static String doNothing(CatTarget e, msg) {\n" +
        "    print msg\n" +
        "  }\n" +
        "}\n" +
        "class CatTarget {\n" +
        "  CatTarget self\n" +
        "}\n" +
        "class Cat2 {\n" +
        "  static String doNothing2(CatTarget e, msg) {\n" +
        "    print msg\n" +
        "  }\n" +
        "}";
    
    private static String SIMPLE_CATEGORY = 
        "use (Cat) {\n" +
        "  new CatTarget().doNothing 'jello'\n" +
        "  def x = new CatTarget()\n" +
        "  x.doNothing 'jello'\n" +
        "  x.self = x\n" +
        "  x.doNothing 'jello'\n" +
        "  Cat.doNothing x, 'jello'\n" +
        "}";

    private static String CATEGORY_WITH_SUBTYPE =
        "class Sub extends CatTarget { }\n" +
        "use (Cat) {\n" +
        "  new Sub().doNothing 'jello'\n" +
        "  def x = new Sub()\n" +
        "  x.doNothing 'jello'\n" +
        "  x.self = x\n" +
        "  x.doNothing 'jello'\n" +
        "  Cat.doNothing x, 'jello'\n" +
        "}";

    private static String CATEGORY_ASSIGNED = 
        "def y = Cat\n" +
        "use (y) {\n" +
        "  new CatTarget().doNothing 'jello'\n" +
        "  def x = new CatTarget()\n" +
        "  x.doNothing 'jello'\n" +
        "  x.self = x\n" +
        "  x.doNothing 'jello'\n" +
        "  y.doNothing x, 'jello'\n" +
        "}";

    private static String CATEGORY_MULTIPLE_OUTER = 
        "use (Cat) { use (Cat2) {\n" +
        "  new CatTarget().doNothing 'jello'\n" +
        "  def x = new CatTarget()\n" +
        "  x.doNothing 'jello'\n" +
        "  x.self = x\n" +
        "  x.doNothing 'jello'\n" +
        "  Cat.doNothing x, 'jello'\n" +
        "} }";

    private static String CATEGORY_MULTIPLE_INNER = 
        "use (Cat2) { use (Cat) {\n" +
        "  new CatTarget().doNothing 'jello'\n" +
        "  def x = new CatTarget()\n" +
        "  x.doNothing 'jello'\n" +
        "  x.self = x\n" +
        "  x.doNothing 'jello'\n" +
        "  Cat.doNothing x, 'jello'\n" +
        "} }";
    

    private static String NO_CATEGORY = 
        "use (Cat) {\n" +
        "}\n" +
        "new CatTarget().doNothing 'jello'\n";
    
    
    public void testCategorySearch1() throws Exception {
        doCategorySearchTest(SIMPLE_CATEGORY, 4);
    }
    
    public void testCategorySearch2() throws Exception {
        doCategorySearchTest(CATEGORY_WITH_SUBTYPE, 4);
    }
    
    public void testCategorySearch3() throws Exception {
        doCategorySearchTest(CATEGORY_ASSIGNED, 4);
    }
    
    public void testCategorySearch4() throws Exception {
        doCategorySearchTest(CATEGORY_MULTIPLE_INNER, 4);
    }
    
    public void testCategorySearch5() throws Exception {
        doCategorySearchTest(CATEGORY_MULTIPLE_OUTER, 4);
    }
    
    public void testCategorySearch6() throws Exception {
        doCategorySearchTest(NO_CATEGORY, 0);
    }
    
    void doCategorySearchTest(String contents, int numMatches) throws JavaModelException {
        checkMatches(findMatches(contents), numMatches, contents);
    }
    
    List<SearchMatch> findMatches(String contents) throws JavaModelException {
        GroovyCompilationUnit catUnit = createUnit("Cat", CATEGORY_DEFN);
        GroovyCompilationUnit unit = createUnit("Other", contents);
        env.waitForAutoBuild();
        expectingNoProblems();
        MockPossibleMatch match = new MockPossibleMatch(unit);
        IMethod searchFor = (IMethod) catUnit.getElementAt(CATEGORY_DEFN.indexOf("doNothing"));
        assertEquals("Wrong IJavaElement found: " + searchFor, "doNothing", searchFor.getElementName());
        SearchPattern pattern = SearchPattern.createPattern(searchFor, IJavaSearchConstants.REFERENCES);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(match);
        visitor.visitCompilationUnit(typeRequestor);
        
        System.out.println("Matches found:\n" + searchRequestor.printMatches());
        
        return searchRequestor.getMatches();
    }
    
    void checkMatches(List<SearchMatch> matches, int numExpected, String contents) {
        assertEquals("Wrong number matches found:\n" + searchRequestor.printMatches(), numExpected, matches.size());
        if (numExpected == 0) {
            return;
        }
        
        Pattern p = Pattern.compile("doNothing");
        Matcher m = p.matcher(contents);
        Iterator<SearchMatch> matchIter = matches.iterator();
        while (m.find()) {
            SearchMatch match = matchIter.next();
            assertEquals("Wrong starting location for " + MockPossibleMatch.printMatch(match), m.start(), match.getOffset());
            assertEquals("Wrong length for " + MockPossibleMatch.printMatch(match), "doNothing".length(), match.getLength());
        }
    }
    
}
