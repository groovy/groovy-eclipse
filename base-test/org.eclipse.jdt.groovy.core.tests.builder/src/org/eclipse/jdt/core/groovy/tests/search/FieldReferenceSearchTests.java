/*
 * Copyright 2009-2023 the original author or authors.
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

import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.junit.Test;

public final class FieldReferenceSearchTests extends SearchTestSuite {

    @Test
    public void testFieldReferencesInScript1() {
        doTestForTwoFieldReferencesInScript(
            "new First().xxx\n" +
            "new First()\n" +
            ".\n" +
            "xxx\n");
    }

    @Test
    public void testFieldReferencesInScript2() {
        doTestForTwoFieldReferencesInScript(
            "First f = new First()\n" +
            "f.xxx = f.xxx\n");
    }

    @Test
    public void testFieldReferencesInScript3() {
        doTestForTwoFieldReferencesInScript(
            "First f = new First()\n" +
            "\"$f.xxx\"\n" +
            "\"$f.xxx\"\n");
    }

    @Test
    public void testFieldReferencesInScript4() {
        doTestForTwoFieldReferencesInScriptWithQuotes(
            "First f = new First()\n" +
            "f.'xxx'\n" +
            "f.'xxx'\n");
    }

    @Test
    public void testFieldReferencesInScript5() {
        doTestForTwoFieldReferencesInScript(
            "First f = new First()\n" +
            "f.xxx\n" +
            "def xxx = 0\n" +
            "xxx++\n" +
            "f.xxx\n");
    }

    @Test
    public void testFieldReferencesInScript6() {
        doTestForTwoFieldReferencesInScript(
            "class SubClass extends First {\n" +
            "}\n" +
            "SubClass f = new SubClass()\n" +
            "f.xxx\n" +
            "def xxx = 0\n" +
            "xxx++\n" +
            "f.xxx\n");
    }

    @Test
    public void testFieldReferencesInScript7() {
        createUnit("Other.groovy",
            "class Other {\n" +
            "  def xxx\n" +
            "}\n");
        doTestForTwoFieldReferencesInScript(
            "class SubClass extends First {\n" +
            "}\n" +
            "SubClass f = new SubClass()\n" +
            "f.xxx\n" +
            "new Other().xxx = 0\n" +
            "f.xxx\n");
    }

    @Test
    public void testFieldReferencesInScript8() {
        doTestForTwoFieldReferencesInScript(
            "class SubClass extends First {\n" +
            "}\n" +
            "def f = new SubClass()\n" +
            "f.xxx\n" + // here
            "f = 9\n" +
            "f.xxx\n" +  // invalid reference
            "f = new SubClass()\n" +
            "f.xxx\n");  // here
    }

    @Test
    public void testFieldReferencesInClass1() {
        doTestForTwoFieldReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    this.xxx\n" +
            "  }\n" +
            "  def xxx() {\n" +
            "  }\n" +
            "  def method2() {\n" +
            "    super.xxx\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testFieldReferencesInClass2() {
        doTestForTwoFieldReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    xxx\n" +
            "  }\n" +
            "  def xxxDONT_SHADOW_SUPER_FIELD() {\n" +
            "  }\n" +
            "  def method2(xxx) {\n" +
            "    xxx = super.xxx\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testFieldReferencesInClass3() {
        doTestForTwoFieldReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    this.xxx = 'nothing'\n" + // yes
            "  }\n" +
            "  def xxx() {\n" +  // no
            "  }\n" +
            "  def method2() {\n" +  // no
            "    def nothing = super.xxx()\n" +  // yes...field reference used as a closure
            "  }\n" +
            "}\n");
    }

    @Test
    public void testFieldReferencesInClass4() {
        createUnit("Third",
            "class Third {\n" +
            "  def xxx\n" + // no
            "}\n");
        doTestForTwoFieldReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    this.xxx = 'nothing'\n" + // yes
            "  }\n" +
            "  def xxx() {}\n" +  // no
            "  def method3(xxx) {\n" +  // no
            "    new Third().xxx\n" + // no
            "    xxx()\n" + // no
            "    xxx = xxx\n" +  // no, no
            "    def nothing = super.xxx()\n" +  // yes...field reference used as a closure
            "  }\n" +
            "}\n");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/891
    public void testFieldReferencesInClass5() {
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
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), unit);

        assertEquals("Should have found 3 matches, but found:\n" + toString(matches), 3, matches.size());
        assertLocation(matches.get(0), contents.indexOf("this.flag") + "this.".length(), "flag".length());
        assertLocation(matches.get(1), contents.indexOf("that.flag") + "that.".length(), "flag".length());
        assertLocation(matches.get(2), contents.lastIndexOf("flag"), "flag".length());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/935
    public void testFieldReferencesInClass6() {
        GroovyCompilationUnit pogo = createUnit("Pogo",
            "class Pogo {\n" +
            "  boolean flag\n" +
            "}\n");

        String contents =
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  void meth(Pogo pogo) {\n" +
            "    pogo.flag = false\n" +
            "  }\n" +
            "}\n";

        GroovyCompilationUnit unit = createUnit("C", contents);
        IField field = findType("Pogo", pogo).getField("flag");
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), unit);

        assertEquals("Should have found 1 matches, but found:\n" + toString(matches), 1, matches.size());
        assertLocation(matches.get(0), contents.lastIndexOf("flag"), "flag".length());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/939
    public void testFieldReferencesInClosure() {
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
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), unit);

        assertEquals("Should have found 4 matches, but found:\n" + toString(matches), 4, matches.size());
        assertLocation(matches.get(0), contents.indexOf("flag", contents.indexOf("with")), "flag".length());
        assertLocation(matches.get(1), contents.indexOf("flag", contents.indexOf("def ")), "flag".length());
        assertLocation(matches.get(2), contents.indexOf("flag = false"), "flag".length());
        assertLocation(matches.get(3), contents.lastIndexOf("flag"), "flag".length());
    }

    @Test
    public void testFieldReferenceInGString1() {
        doTestForTwoFieldReferencesInGString(
            "class Second extends First {\n" +
            "  def x() {\n" +
            "    \"${xxx}\"\n" +
            "    \"${xxx.toString()}\"\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testFieldReferenceInGString2() {
        doTestForTwoFieldReferencesInGString(
            "class Second extends First {\n" +
            "  def x() {\n" +
            "    \"${ xxx}\"\n" +
            "    \"${ xxx .toString()}\"\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testFieldReferenceInGString3() {
        doTestForTwoFieldReferencesInGString(
            "class Second extends First {\n" +
            "  def x() {\n" +
            "    \"${xxx} ${xxx.toString()}\"\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testFieldReferenceInGString4() {
        doTestForTwoFieldReferencesInGString(
            "class Second extends First {\n" +
            "  def x() {\n" +
            "    \"${dunno(xxx)} ${super.xxx}\"\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testFieldWritesInScript1() {
        doTestForTwoFieldWritesInScript(
            "new First().xxx = 1\n" +
            "new First().xxx\n" +
            "def f = new First()\n" +
            "def y = f.xxx\n" +
            "f.xxx = 8\n");
    }

    @Test
    public void testFieldReadsInScript1() {
        doTestForTwoFieldReadsInScript(
            "new First().xxx\n" +
            "new First().xxx = 1\n" +
            "def f = new First()\n" +
            "f.xxx = f.xxx\n");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/788
    public void testFindInnerClassField1() {
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
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), unit);

        assertEquals("Should have found 2 matches, but found:\n" + toString(matches), 2, matches.size());
        assertLocation(matches.get(0), contents.indexOf("something", offset), "something".length());
        assertLocation(matches.get(1), contents.indexOf("$something", offset) + 1, "something".length());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/788
    public void testFindInnerClassField2() {
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
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES), unit);

        assertEquals("Should have found 2 matches, but found:\n" + toString(matches), 2, matches.size());
        assertLocation(matches.get(0), contents.indexOf("owner.something", offset) + 6, "something".length());
        assertLocation(matches.get(1), contents.indexOf("$owner.something", offset) + 7, "something".length());
    }

    @Test
    public void testFindReadAccesses1() {
        String contents =
            "class Pogo {\n" +
            "  String string\n" +
            "}\n" +
            "def obj = new Pogo(string: null)\n" +
            "def str = obj.string\n" + // yes
            "obj.string = str\n" +
            "str = obj.@string\n" + // yes
            "obj.@string = str\n" +
            "str += obj.string\n" + // yes
            "obj.string += str\n" + // ???
            "str = obj.getString()\n" +
            "obj.setString( null )\n" +
            "str = obj.'getString'()\n" +
            "obj.'setString'( null )\n" +
            "def fun = obj.&getString\n" +
            "fun = obj.&setString\n";

        GroovyCompilationUnit unit = createUnit("foo", contents);
        IField field = findType("Pogo", unit).getField("string");
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.READ_ACCESSES), unit);

        assertEquals(3, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertLocation(matches.get(0), contents.indexOf(".string") + 1, 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertLocation(matches.get(1), contents.indexOf("@string") + 1, 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(2).getAccuracy());
        assertLocation(matches.get(2), contents.indexOf("+= obj.string") + 7, 6);
    }

    @Test
    public void testFindReadAccesses2() {
        String contents =
            "class Pogo {\n" +
            "  String string\n" +
            "}\n" +
            "new Pogo().with {\n" +
            "  def str = string\n" + // yes
            "  string = str\n" +
            "  str = delegate.@string\n" + // yes
            "  delegate.@string = str\n" +
            "  str += string\n" + // yes
            "  string += str\n" + // ???
            "  str = getString()\n" +
            "  setString( null )\n" +
            "  str = 'getString'()\n" +
            "  'setString'( null )\n" +
            "  def fun = delegate.&getString\n" +
            "  fun = delegate.&setString\n" +
            "}\n";

        GroovyCompilationUnit unit = createUnit("foo", contents);
        IField field = findType("Pogo", unit).getField("string");
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.READ_ACCESSES), unit);

        assertEquals(3, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertLocation(matches.get(0), contents.indexOf("= string") + 2, 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertLocation(matches.get(1), contents.indexOf("@string") + 1, 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(2).getAccuracy());
        assertLocation(matches.get(2), contents.indexOf("+= string") + 3, 6);
    }

    @Test
    public void testFindWriteAccesses1() {
        String contents =
            "class Pogo {\n" +
            "  String string\n" +
            "}\n" +
            "def obj = new Pogo(string: null)\n" +
            "def str = obj.string\n" +
            "obj.string = str\n" + // yes
            "str = obj.@string\n" +
            "obj.@string = str\n" + // yes
            "str += obj.string\n" +
            "obj.string += str\n" + // yes
            "str = obj.getString()\n" +
            "obj.setString( null )\n" +
            "str = obj.'getString'()\n" +
            "obj.'setString'( null )\n" +
            "def fun = obj.&getString\n" +
            "fun = obj.&setString\n";

        GroovyCompilationUnit unit = createUnit("foo", contents);
        IField field = findType("Pogo", unit).getField("string");
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.WRITE_ACCESSES), unit);

        assertEquals(4, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertLocation(matches.get(0), contents.indexOf("string: "), 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertLocation(matches.get(1), contents.indexOf(".string =") + 1, 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(2).getAccuracy());
        assertLocation(matches.get(2), contents.indexOf("@string =") + 1, 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(3).getAccuracy());
        assertLocation(matches.get(3), contents.indexOf(".string +") + 1, 6);
    }

    @Test
    public void testFindWriteAccesses2() {
        String contents =
            "class Pogo {\n" +
            "  String string\n" +
            "}\n" +
            "new Pogo().with {\n" +
            "  def str = string\n" +
            "  string = str\n" + // yes
            "  str = delegate.@string\n" +
            "  delegate.@string = str\n" + // yes
            "  str += string\n" +
            "  string += str\n" + // yes
            "  str = getString()\n" +
            "  setString( null )\n" +
            "  str = 'getString'()\n" +
            "  'setString'( null )\n" +
            "  def fun = delegate.&getString\n" +
            "  fun = delegate.&setString\n" +
            "}\n";

        GroovyCompilationUnit unit = createUnit("foo", contents);
        IField field = findType("Pogo", unit).getField("string");
        List<SearchMatch> matches = search(SearchPattern.createPattern(field, IJavaSearchConstants.WRITE_ACCESSES), unit);

        assertEquals(3, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertLocation(matches.get(0), contents.indexOf("string ="), 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertLocation(matches.get(1), contents.indexOf("@string =") + 1, 6);
        assertEquals(SearchMatch.A_ACCURATE, matches.get(2).getAccuracy());
        assertLocation(matches.get(2), contents.indexOf("string +="), 6);
    }

    //--------------------------------------------------------------------------

    private static final String FIRST_CONTENTS_CLASS_FOR_FIELDS = "class First {\n  def xxx\n}";

    private void doTestForTwoFieldWritesInScript(String secondContents) {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx", IJavaSearchConstants.WRITE_ACCESSES);
    }

    private void doTestForTwoFieldReadsInScript(String secondContents) {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx", IJavaSearchConstants.READ_ACCESSES);
    }

    private void doTestForTwoFieldReferencesInScript(String secondContents) {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx");
    }

    private void doTestForTwoFieldReferencesInScriptWithQuotes(String secondContents) {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "'xxx'");
    }

    private void doTestForTwoFieldReferencesInClass(String secondContents) {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, false, 0, "xxx");
    }

    private void doTestForTwoFieldReferences(String firstContents, String secondContents, boolean isScript, int offsetInParent, String matchName) {
        doTestForTwoFieldReferences(firstContents, secondContents, isScript, offsetInParent, matchName, IJavaSearchConstants.REFERENCES);
    }

    private void doTestForTwoFieldReferences(String firstContents, String secondContents, boolean isScript, int offsetInParent, String matchName, int flags) {
        String firstClassName = "First";
        String secondClassName = "Second";
        String matchedFieldName = "xxx";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IField firstField = findType(firstClassName, first).getField(matchedFieldName);
        SearchPattern pattern = SearchPattern.createPattern(firstField, flags);

        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        try {
            IJavaElement firstMatchEnclosingElement;
            IJavaElement secondMatchEnclosingElement;
            if (isScript) {
                firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
                secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
            } else {
                firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent];
                secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[offsetInParent + 2];
            }
            // match is enclosed in run method (for script), or x method for class

            checkMatches(secondContents, matchName, pattern, second, firstMatchEnclosingElement, secondMatchEnclosingElement);
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }
    }

    private void doTestForTwoFieldReferencesInGString(String secondContents) {
        doTestForTwoFieldReferencesInGString(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, "xxx");
    }

    // as above, but enclosing element is always the first child of the enclosing type
    private void doTestForTwoFieldReferencesInGString(String firstContents, String secondContents, String matchName) {
        String firstClassName = "First";
        String secondClassName = "Second";
        String matchedFieldName = "xxx";
        GroovyCompilationUnit first = createUnit(firstClassName, firstContents);
        IField firstField = findType(firstClassName, first).getField(matchedFieldName);
        SearchPattern pattern = SearchPattern.createPattern(firstField, IJavaSearchConstants.REFERENCES);

        GroovyCompilationUnit second = createUnit(secondClassName, secondContents);
        try {
            IJavaElement firstMatchEnclosingElement = findType(secondClassName, second).getChildren()[0];
            IJavaElement secondMatchEnclosingElement = findType(secondClassName, second).getChildren()[0];
            checkMatches(secondContents, matchName, pattern, second, firstMatchEnclosingElement, secondMatchEnclosingElement);
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }
    }
}
