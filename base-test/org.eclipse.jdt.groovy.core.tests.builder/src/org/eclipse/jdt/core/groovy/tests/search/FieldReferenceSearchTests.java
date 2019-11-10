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

import static org.junit.Assert.assertEquals;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.groovy.tests.MockPossibleMatch;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeRequestorFactory;
import org.junit.Test;

public final class FieldReferenceSearchTests extends SearchTestSuite {

    @Test
    public void testFieldReferencesInScript1() throws Exception {
        doTestForTwoFieldReferencesInScript("new First().xxx\nnew First()\n.\nxxx");
    }

    @Test
    public void testFieldReferencesInScript2() throws Exception {
        doTestForTwoFieldReferencesInScript("First f = new First()\n f.xxx = f.xxx");
    }

    @Test
    public void testFieldReferencesInScript3() throws Exception {
        doTestForTwoFieldReferencesInScript("First f = new First()\n \"$f.xxx\"\n\"$f.xxx\"");
    }

    @Test
    public void testFieldReferencesInScript4() throws Exception {
        doTestForTwoFieldReferencesInScriptWithQuotes("First f = new First()\n f.'xxx'\nf.'xxx'");
    }

    @Test
    public void testFieldReferencesInScript5() throws Exception {
        doTestForTwoFieldReferencesInScript("First f = new First()\n f.xxx\ndef xxx = 0\nxxx++\nf.xxx");
    }

    @Test
    public void testFieldReferencesInScript6() throws Exception {
        doTestForTwoFieldReferencesInScript("class SubClass extends First { } \n SubClass f = new SubClass()\n f.xxx\ndef xxx = 0\nxxx++\nf.xxx");
    }

    @Test
    public void testFieldReferencesInScript7() throws Exception {
        createUnit("Other.groovy", "class Other { def xxx }");
        doTestForTwoFieldReferencesInScript("class SubClass extends First { } \n SubClass f = new SubClass()\n f.xxx\nnew Other().xxx = 0\nf.xxx");
    }

    @Test
    public void testFieldReferencesInScript8() throws Exception {
        doTestForTwoFieldReferencesInScript(
            "class SubClass extends First { } \n " +
            "def f = new SubClass()\n " +
            "f.xxx\n" + // here
            "f = 9\n" +
            "f.xxx\n" +  // invalid reference
            "f = new SubClass()\n" +
            "f.xxx");  // here
    }

    @Test
    public void testFieldReferencesInClass1() throws Exception {
        doTestForTwoFieldReferencesInClass("class Second extends First { \ndef method() { this.xxx }\ndef xxx() { }\n def method2() { super.xxx }}");
    }

    @Test
    public void testFieldReferencesInClass2() throws Exception {
        doTestForTwoFieldReferencesInClass("class Second extends First { \ndef method() { xxx }\ndef xxxDONT_SHADOW_SUPER_FIELD() { }\n def method2(xxx) { xxx = super.xxx }}");
    }

    @Test
    public void testFieldReferencesInClass3() throws Exception {
        doTestForTwoFieldReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    this.xxx = 'nothing'\n" + // yes
            "  }\n" +
            "  def xxx() { }\n" +  // no
            "  def method2() {\n" +  // no
            "    def nothing = super.xxx()\n" +  // yes...field reference used as a closure
            "  }\n" +
            "}");
    }

    @Test
    public void testFieldReferencesInClass4() throws Exception {
        createUnit("Third",
            "class Third {\n" +
            "  def xxx\n" + // no
            "}\n");
        doTestForTwoFieldReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    this.xxx = 'nothing'\n" + // yes
            "  }\n" +
            "  def xxx() { }\n" +  // no
            "  def method3(xxx) {\n" +  // no
            "    new Third().xxx\n" + // no
            "    xxx()\n" + // no
            "    xxx = xxx\n" +  // no, no
            "    def nothing = super.xxx()\n" +  // yes...field reference used as a closure
            "  }\n" +
            "}");
    }

    @Test
    public void testFieldReferencesInClass5() throws Exception {
        createUnit("other", "Other",
            "class Third {\n" +
            "  private String xxx\n" +
            "  String getXxx() { xxx }\n" +
            "  void setXxx(String xxx) { this.xxx = xxx }\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Other {" +
            "  Other() {\n" +
            "    def t = new Third(xxx: 'abc')\n" +
            "    def m = t.&setXxx\n" +
            "    m('xyz')\n" +
            "  }\n" +
            "}\n");

        doTestForTwoFieldReferencesInClass(
            "class Second extends First {\n" +
            "  int getXxx() {\n" +
            "    return this.xxx\n" +
            "  }\n" +
            "  def whatever\n" +
            "  void setXxx(int value) {\n" +
            "    this.xxx = value\n" +
            "  }\n" +
            "}\n");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/891
    public void testFieldReferencesInClass6() throws Exception {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "class Pogo {\n" +
            "  boolean flag\n" +
            "  Pogo(Pogo that) {\n" +
            "    this.flag = that.flag\n" +
            "  }\n" +
            "  void setFlag(boolean value) {\n" +
            "    flag = value\n" +
            "  }\n" +
            "}\n";

        GroovyCompilationUnit unit = createUnit("Pogo", contents);
        IField field = findType("Pogo", unit).getField("flag");

        MockPossibleMatch possibleMatch = new MockPossibleMatch(unit);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(possibleMatch,
            SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), searchRequestor);
        factory.createVisitor(possibleMatch).visitCompilationUnit(typeRequestor);

        assertEquals("Should have found 3 matches, but found:\n" + searchRequestor.printMatches(), 3, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("this.flag") + "this.".length(), "flag".length());
        assertLocation(searchRequestor.getMatch(1), contents.indexOf("that.flag") + "that.".length(), "flag".length());
        assertLocation(searchRequestor.getMatch(2), contents.lastIndexOf("flag"), "flag".length());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/939
    public void testFieldReferencesInClosure() throws Exception {
        String contents =
            "class Pogo {\n" +
            "  boolean flag\n" +
            "}\n" +
            "void meth(Pogo pogo) {\n" +
            "  pogo.with {\n" +
            "    flag\n" +
            "    def f = flag\n" +
            "    flag = false\n" +
            "    flag += true\n" +
            "  }\n" +
            "}\n";

        GroovyCompilationUnit unit = createUnit("script", contents);
        IField field = findType("Pogo", unit).getField("flag");

        MockPossibleMatch possibleMatch = new MockPossibleMatch(unit);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(possibleMatch,
            SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), searchRequestor);
        factory.createVisitor(possibleMatch).visitCompilationUnit(typeRequestor);

        assertEquals("Should have found 4 matches, but found:\n" + searchRequestor.printMatches(), 4, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("flag", contents.indexOf("with")), "flag".length());
        assertLocation(searchRequestor.getMatch(1), contents.indexOf("flag", contents.indexOf("def ")), "flag".length());
        assertLocation(searchRequestor.getMatch(2), contents.indexOf("flag = false"), "flag".length());
        assertLocation(searchRequestor.getMatch(3), contents.lastIndexOf("flag"), "flag".length());
    }

    @Test
    public void testFieldReferenceInGString1() throws Exception {
        doTestForTwoFieldReferencesInGString("class Second extends First {\ndef x() { \"${xxx}\"\n\"${xxx.toString()}\" } }");
    }

    @Test
    public void testFieldReferenceInGString2() throws Exception {
        doTestForTwoFieldReferencesInGString("class Second extends First {\ndef x() { \"${ xxx }\"\n\"${ xxx .toString()}\" } }");
    }

    @Test
    public void testFieldReferenceInGString3() throws Exception {
        doTestForTwoFieldReferencesInGString("class Second extends First {\ndef x() { \"${xxx} ${xxx.toString()}\" } }");
    }

    @Test
    public void testFieldReferenceInGString4() throws Exception {
        doTestForTwoFieldReferencesInGString("class Second extends First {\ndef x() { \"${dunno(xxx)} ${super.xxx}\" } }");
    }

    @Test
    public void testFieldWritesInScript1() throws Exception {
        doTestForTwoFieldWritesInScript(
            "new First().xxx = 1\n" +
            "new First().xxx\n" +
            "def f = new First()\n" +
            "def y = f.xxx\n" +
            "f.xxx = 8");
    }

    @Test
    public void testFieldReadsInScript1() throws Exception {
        doTestForTwoFieldReadsInScript(
            "new First().xxx\n" +
            "new First().xxx = 1\n" +
            "def f = new First()\n" +
            "f.xxx = f.xxx");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/788
    public void testFindInnerClassField1() throws Exception {
        String contents =
            "class Outer {\n" +
            "  static class Inner1 {\n" +
            "    String something\n" +
            "  }\n" +
            "  static class Inner2 {\n" +
            "    String something\n" +
            "    void meth(Inner1 param) {\n" +
            "      param.with {\n" +
            "        something\n" +
            "        owner.something\n" +
            "        println \"Something: $something\"\n" +
            "        println \"Something else: $owner.something\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        int offset = contents.indexOf("with {");
        GroovyCompilationUnit unit = createUnit("Outer", contents);
        IField field = findType("Outer", unit).getType("Inner1").getField("something");

        MockPossibleMatch possibleMatch = new MockPossibleMatch(unit);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(possibleMatch,
            SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), searchRequestor);
        factory.createVisitor(possibleMatch).visitCompilationUnit(typeRequestor);

        assertEquals("Should have found 2 matches, but found:\n" + searchRequestor.printMatches(), 2, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("something", offset), "something".length());
        assertLocation(searchRequestor.getMatch(1), contents.indexOf("$something", offset) + 1, "something".length());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/788
    public void testFindInnerClassField2() throws Exception {
        String contents =
            "class Outer {\n" +
            "  static class Inner1 {\n" +
            "    String something\n" +
            "  }\n" +
            "  static class Inner2 {\n" +
            "    String something\n" +
            "    void meth(Inner1 param) {\n" +
            "      param.with {\n" +
            "        something\n" +
            "        owner.something\n" +
            "        println \"Something: $something\"\n" +
            "        println \"Something else: $owner.something\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        int offset = contents.indexOf("with {");
        GroovyCompilationUnit unit = createUnit("Outer", contents);
        IField field = findType("Outer", unit).getType("Inner2").getField("something");

        MockPossibleMatch possibleMatch = new MockPossibleMatch(unit);
        ITypeRequestor typeRequestor = new TypeRequestorFactory().createRequestor(possibleMatch,
            SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), searchRequestor);
        factory.createVisitor(possibleMatch).visitCompilationUnit(typeRequestor);

        assertEquals("Should have found 2 matches, but found:\n" + searchRequestor.printMatches(), 2, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("owner.something", offset) + 6, "something".length());
        assertLocation(searchRequestor.getMatch(1), contents.indexOf("$owner.something", offset) + 7, "something".length());
    }

    //--------------------------------------------------------------------------

    private static final String FIRST_CONTENTS_CLASS_FOR_FIELDS = "class First {\n  def xxx\n}";

    private void doTestForTwoFieldWritesInScript(String secondContents) throws Exception {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx", IJavaSearchConstants.WRITE_ACCESSES);
    }

    private void doTestForTwoFieldReadsInScript(String secondContents) throws Exception {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx", IJavaSearchConstants.READ_ACCESSES);
    }

    private void doTestForTwoFieldReferencesInScript(String secondContents) throws Exception {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx");
    }

    private void doTestForTwoFieldReferencesInScriptWithQuotes(String secondContents) throws Exception {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "'xxx'");
    }

    private void doTestForTwoFieldReferencesInClass(String secondContents) throws Exception {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, false, 0, "xxx");
    }

    private void doTestForTwoFieldReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent, String matchName) throws Exception {
        doTestForTwoFieldReferences(firstContents, secondContents, contentsIsScript, offsetInParent, matchName, IJavaSearchConstants.REFERENCES);
    }

    private void doTestForTwoFieldReferences(String firstContents, String secondContents, boolean contentsIsScript, int offsetInParent, String matchName, int searchFlags) throws Exception {
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

        checkMatches(secondContents, matchName, pattern, second, firstMatchEnclosingElement, secondMatchEnclosingElement);
    }

    private void doTestForTwoFieldReferencesInGString(String secondContents) throws Exception {
        doTestForTwoFieldReferencesInGString(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, "xxx");
    }

    // as above, but enclosing element is always the first child of the enclosing type
    private void doTestForTwoFieldReferencesInGString(String firstContents, String secondContents, String matchName) throws Exception {
        String firstClassName = "First";
        String secondClassName = "Second";
        String matchedFieldName = "xxx";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IField firstField = findType(firstClassName, first).getField(matchedFieldName);
        SearchPattern pattern = SearchPattern.createPattern(firstField, IJavaSearchConstants.REFERENCES);

        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        IJavaElement firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[0];
        IJavaElement secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[0];

        checkMatches(secondContents, matchName, pattern, second, firstMatchEnclosingElement, secondMatchEnclosingElement);
    }
}
