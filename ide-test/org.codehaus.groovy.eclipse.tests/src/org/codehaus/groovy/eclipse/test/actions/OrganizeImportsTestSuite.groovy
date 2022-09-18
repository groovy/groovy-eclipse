/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.actions

import static org.eclipse.jdt.internal.compiler.impl.CompilerOptions.OPTIONG_GroovyCompilerConfigScript

import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.ISourceRange
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.groovy.tests.ReconcilerUtils
import org.eclipse.jdt.core.search.TypeNameMatch
import org.eclipse.jdt.core.tests.util.Util
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.Document
import org.eclipse.text.edits.DeleteEdit
import org.eclipse.text.edits.InsertEdit
import org.eclipse.text.edits.TextEdit
import org.junit.After
import org.junit.Assert
import org.junit.Before

abstract class OrganizeImportsTestSuite extends GroovyEclipseTestSuite {

    @Before
    final void setUpImportsTestCase() {
        // ensure consistent ordering of imports regardless of the target platform's defaults
        setJavaPreference(PreferenceConstants.ORGIMPORTS_IMPORTORDER, '\\#;java;javax;groovy;groovyx;;')

        addJavaSource '''\
            |public class Outer {
            |  public static class Inner { }
            |}
            |'''.stripMargin(), 'Outer', 'other'

        addGroovySource '''\
            |class FirstClass<T> { }
            |class SecondClass { }
            |class ThirdClass { }
            |'''.stripMargin(), 'Other', 'other'

        addGroovySource '''\
            |class FourthClass {
            |  static m() { }
            |}
            |'''.stripMargin(), 'Other', 'other2'

        addGroovySource '''\
            |class FourthClass {
            |  static m() { }
            |}
            |'''.stripMargin(), 'Other', 'other3'

        addGroovySource '''\
            |class FourthClass {
            |  static m() { }
            |}
            |'''.stripMargin(), 'Other', 'other4'
    }

    @After
    final void tearDownImportsTestCase() {
        withProject { IProject project ->
            Util.delete(project.getFile('config.groovy'))
        }

        JavaCore.options = JavaCore.options.tap {
            remove(OPTIONG_GroovyCompilerConfigScript)
        }
    }

    protected void addConfigScript(CharSequence contents) {
        addPlainText(contents.stripMargin(), '../config.groovy')
        setJavaPreference(OPTIONG_GroovyCompilerConfigScript, 'config.groovy')
    }

    protected ICompilationUnit createGroovyType(String pack, String name, CharSequence contents) {
        addGroovySource(contents.stripMargin(), name, pack)
    }

    protected void doAddImportTest(CharSequence contents, List<String> expectedImports = []) {
        def unit = addGroovySource(contents.stripMargin(), nextUnitName())
        ReconcilerUtils.reconcile(unit)

        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, { TypeNameMatch[][] matches, ISourceRange[] range ->
            Assert.fail("Should not have a choice, but found $matches[0][0] and $matches[0][1]")
        })
        TextEdit edit = organize.calculateMissingImports()
        if (expectedImports == null) {
            Assert.assertNull('Expected null due to a compile error in the contents', edit)
        }

        def actualContents = unit.contents
        def children = edit?.children as List<TextEdit>
        List<TextEdit> newChildren = []
        children?.each {
            if (it instanceof DeleteEdit) {
                // check to see if the edit is whitespace only
                def del = it as DeleteEdit
                for (i in del.offset..del.inclusiveEnd) {
                    if (!Character.isWhitespace(actualContents[i] as char)) {
                        Assert.fail("Found unexpected DeleteEdit: $it [ ${actualContents[del.offset..del.inclusiveEnd]} ]")
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
            Assert.assertEquals("Found incorrect imports in text edit:\n$edit\nwith expected imports:\n$expectedImports", expectedImports.size(), newChildren.size())
        } else {
            Assert.assertTrue("Found incorrect imports in text edit:\n$edit\nwith expected imports:\n$expectedImports", newChildren.empty)
        }

        def notFound = ''
        for (child in newChildren) {
            if (!child instanceof InsertEdit) {
                notFound << "Found an invalid Edit: $child\n"
            } else if (!contents.contains(child.text)) {
                notFound << child.text << '\n'
            }
        }

        if (notFound) {
            Assert.fail("Did not find the following imports:\n$notFound")
        }
    }

    protected void doContentsCompareTest(CharSequence originalContents, CharSequence expectedContents = originalContents) {
        def unit = addGroovySource(originalContents.stripMargin(), nextUnitName())
        ReconcilerUtils.reconcile(unit)

        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, { TypeNameMatch[][] matches, ISourceRange[] range ->
            Assert.fail("Should not have a choice, but found $matches[0][0] and $matches[0][1]")
        })
        TextEdit edit = organize.calculateMissingImports()

        Document doc = new Document(originalContents.stripMargin())
        if (edit != null) edit.apply(doc)

        // deal with some variance in JDT Core handling of package only
        String expect = expectedContents.stripMargin()
        String actual = doc.get()
        if (expectedContents.toString().isEmpty()) {
            expect = expect.trim()
            actual = actual.trim()
        }

        Assert.assertEquals(expect, actual)
    }

    protected void doChoiceTest(CharSequence contents, List expectedChoices) {
        def unit = addGroovySource(contents.stripMargin(), nextUnitName())
        ReconcilerUtils.reconcile(unit)

        List<String> choices
        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, { TypeNameMatch[][] matches, ISourceRange[] range ->
            choices = matches[0]*.type*.fullyQualifiedName
            return new TypeNameMatch[0]
        })
        organize.calculateMissingImports()
        for (choice in expectedChoices) {
            Assert.assertTrue("Should have found $choice in choices", choices.contains(choice))
        }
        Assert.assertEquals("Wrong number of choices found.  Expecting:\n$expectedChoices\nFound:\n$choices", choices.size(), expectedChoices.size())
    }
}
