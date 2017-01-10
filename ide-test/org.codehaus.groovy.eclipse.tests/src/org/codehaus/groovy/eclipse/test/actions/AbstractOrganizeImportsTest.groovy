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
package org.codehaus.groovy.eclipse.test.actions

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports
import org.codehaus.groovy.eclipse.test.EclipseTestSetup
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.ISourceRange
import org.eclipse.jdt.core.search.TypeNameMatch
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.Document
import org.eclipse.text.edits.DeleteEdit
import org.eclipse.text.edits.InsertEdit
import org.eclipse.text.edits.TextEdit

abstract class AbstractOrganizeImportsTest extends TestCase {

    protected static final String LINE_SEPARATOR = System.getProperty('line.separator')

    protected static Test newTestSuite(Class<? extends Test> test) {
        new EclipseTestSetup(new TestSuite(test))
    }

    @Override
    protected void tearDown() throws Exception {
        EclipseTestSetup.removeSources()
    }

    @Override
    protected void setUp() {
        EclipseTestSetup.addJavaSource(CONTENTS_JAVA_SUPPORTING, 'Outer', 'other')
        EclipseTestSetup.addGroovySource(CONTENTS_SUPPORTING,    'Other', 'other')
        EclipseTestSetup.addGroovySource(CONTENTS_SUPPORTING2,   'Other', 'other2')
        EclipseTestSetup.addGroovySource(CONTENTS_SUPPORTING2,   'Other', 'other3')
        EclipseTestSetup.addGroovySource(CONTENTS_SUPPORTING2,   'Other', 'other4')

        // ensure consistent ordering of imports regardless of the target platform's defaults
        EclipseTestSetup.setJavaPreference(PreferenceConstants.ORGIMPORTS_IMPORTORDER, '\\#;java;javax;groovy;groovyx;;')
    }

    private static final String CONTENTS_SUPPORTING = '''
        class FirstClass { }
        class SecondClass { }
        class ThirdClass { }
        '''
    private static final String CONTENTS_SUPPORTING2 = '''
        class FourthClass {
          static m() { }
        }
        '''
    private static final String CONTENTS_JAVA_SUPPORTING = '''
        public class Outer {
          public static class Inner { }
        }
        '''

    protected void doAddImportTest(CharSequence contents, List<String> expectedImports = []) {
        doAddImportTest('main', 'Main', contents, expectedImports)
    }

    protected void doAddImportTest(String pkgName, String resourceName, CharSequence contents, List<String> expectedImports) {
        def unit = EclipseTestSetup.addGroovySource(contents, resourceName, pkgName)
        EclipseTestSetup.waitForIndex()
        IChooseImportQuery query = new NoChoiceQuery()
        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, query)
        TextEdit edit = organize.calculateMissingImports()
        if (expectedImports == null) {
            assertNull('Expected null due to a compile error in the contents', edit)
        }

        def actualContents = unit.contents
        def children = edit?.children as List
        def newChildren = []
        children?.each {
            if (it instanceof DeleteEdit) {
                // check to see if the edit is whitespace only
                def del = it as DeleteEdit
                for (i in del.offset..del.inclusiveEnd) {
                    if (!Character.isWhitespace(actualContents[i] as char)) {
                        fail("Found unexpected DeleteEdit: $it [ ${actualContents[del.offset..del.inclusiveEnd]} ]")
                    }
                }
            }
            if (it instanceof InsertEdit) {
                def insert = it as InsertEdit
                if (!insert.text.trim().empty && insert.text != '\n') {
                    newChildren += insert
                }
            }
        }
        if (expectedImports) {
            assertEquals("Found incorrect imports in text edit:\n$edit\nwith expected imports:\n$expectedImports", expectedImports.size(), newChildren.size())
        } else {
            assertTrue("Found incorrect imports in text edit:\n$edit\nwith expected imports:\n$expectedImports", newChildren.empty)
        }

        def notFound = ''
        for (TextEdit child : newChildren) {
            if (!child instanceof InsertEdit) {
                notFound << "Found an invalid Edit: $child\n"
            } else if (!contents.contains(child.text)) {
                notFound << child.text << '\n'
            }
        }

        if (notFound) {
            fail("Did not find the following imports:\n$notFound")
        }
    }

    protected void doContentsCompareTest(CharSequence originalContents, CharSequence expectedContents) {
        def unit = createGroovyType('main', 'Main', originalContents)
        EclipseTestSetup.buildProject()
        EclipseTestSetup.waitForIndex()

        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, new NoChoiceQuery())
        TextEdit edit = organize.calculateMissingImports()
        // TODO: Must match TestProject.createGroovyType()!
        String prefix = "package main;$LINE_SEPARATOR$LINE_SEPARATOR"

        Document doc = new Document(prefix + normalizeLineEndings(originalContents))
        if (edit != null) edit.apply(doc)

        assertEquals(prefix + normalizeLineEndings(expectedContents), normalizeLineEndings(doc.get()))
    }

    protected void doChoiceTest(CharSequence contents, List expectedChoices) {
        def unit = createGroovyType('main', 'Main', contents)
        EclipseTestSetup.buildProject()

        def query = new ChoiceQuery()
        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, query)
        organize.calculateMissingImports()
        for (choice in expectedChoices) {
            assertTrue("Should have found $choice in choices", query.choices.contains(choice))
        }
        assertEquals("Wrong number of choices found.  Expecting:\n$expectedChoices\nFound:\n$query.choices", query.choices.size(), expectedChoices.size())
    }

    protected ICompilationUnit createGroovyType(String pack, String name, CharSequence contents) {
        EclipseTestSetup.addGroovySource(normalizeLineEndings(contents), name, pack)
    }

    protected String normalizeLineEndings(CharSequence contents) {
        contents.stripIndent().replaceAll('\r?\n', LINE_SEPARATOR)
    }
}

class NoChoiceQuery implements IChooseImportQuery {
    public TypeNameMatch[] chooseImports(TypeNameMatch[][] matches, ISourceRange[] range) {
        TestCase.fail("Should not have a choice, but found $matches[0][0] and $matches[0][1]")
    }
}

class ChoiceQuery implements IChooseImportQuery {
    List choices
    public TypeNameMatch[] chooseImports(TypeNameMatch[][] matches, ISourceRange[] range) {
        choices = matches[0].collect { it.type.fullyQualifiedName }
        return []
    }
}
