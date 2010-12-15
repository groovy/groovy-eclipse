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
package org.codehaus.groovy.eclipse.test.actions;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports
import org.codehaus.groovy.eclipse.test.EclipseTestCase
import org.codehaus.jdt.groovy.model.GroovyNature
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.jdt.core.ISourceRange
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.search.TypeNameMatch
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery
import org.eclipse.text.edits.DeleteEdit
import org.eclipse.text.edits.InsertEdit
import org.eclipse.text.edits.TextEdit

/**
 * 
 * @author Andrew Eisenberg
 * @created Dec 15, 2010
 */
class AbstractOrganizeImportsTest extends EclipseTestCase {
    
    protected void setUp() throws Exception {
        super.setUp()
        testProject.addNature(GroovyNature.GROOVY_NATURE)
        GroovyRuntime.addGroovyRuntime(testProject.getProject())
        testProject.createGroovyTypeAndPackage("other", "Other.groovy", CONTENTS_SUPPORTING)
        testProject.createGroovyTypeAndPackage("other2", "Other.groovy", CONTENTS_SUPPORTING2)
        testProject.createGroovyTypeAndPackage("other3", "Other.groovy", CONTENTS_SUPPORTING2)
        testProject.createJavaTypeAndPackage("other", "Outer.java", CONTENTS_JAVA_SUPPORTING)
    }
    
    final static String CONTENTS_SUPPORTING =
    """
        class FirstClass { }
        class SecondClass { }
        class ThirdClass { }
    """
    final static String CONTENTS_SUPPORTING2 =
    """
        class FourthClass {
            static m() { }
        }
    """
    final static String CONTENTS_JAVA_SUPPORTING =
    """
        public class Outer {
            public static class Inner { }
        }
    """


    
    void doAddImportTest(contents, expectedImports = [ ]) {
        def file = testProject.createGroovyTypeAndPackage("main", "Main.groovy", contents)
        def unit = JavaCore.createCompilationUnitFrom(file)
        testProject.waitForIndexer()
        IChooseImportQuery query = new NoChoiceQuery()
        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, query)
        TextEdit edit = organize.calculateMissingImports()
        if (expectedImports == null) {
            assertNull "Expected null due to a compile error in the contents", edit
        }
        
        def actualContents = unit.contents
        def children = edit.getChildren() as List
        def newChildren = []
        children.each {
            if (it instanceof DeleteEdit) {
                // check to see if the edit is whitespace only
                def del = it as DeleteEdit
                for (i in del.offset..del.inclusiveEnd) {
                    if (! Character.isWhitespace(actualContents[i] as char)) {
                        fail("Found unexpected DeleteEdit: $it [ ${actualContents[del.offset..del.inclusiveEnd]} ]")
                    }
                }
            }
         }
        children.each {
            TextEdit t ->
            if (t instanceof InsertEdit) {
                def insert = t as InsertEdit
                if (insert.text.trim().length() > 0 && insert.text != '\n') {
                    newChildren += insert
                }
            }
        }
        if (expectedImports.size() > 0) {
            assertEquals "Found incorrect imports in text edit: \n$edit\nwith expected imports:\n$expectedImports", expectedImports.size(), newChildren.size()
        } else {
            assertEquals "Found incorrect imports in text edit: \n$edit\nwith expected imports:\n$expectedImports", 0, newChildren.size()
        }
        
        
        def notFound = ""
        for (TextEdit child : newChildren) {
            if (! child instanceof InsertEdit) {
                notFound << "Found an invalid Edit: $child\n"
            } else if (!contents.contains(child.text)) {
                notFound << child.text << "\n"
            }
        }
        
        if (notFound.length() > 0) {
            fail "Did not find the following imports:\n$notFound"
        }
    }
    
    
    void doDeleteImportTest(contents, numDeletes) {
        def file = testProject.createGroovyTypeAndPackage("main", "Main.groovy", contents)
        testProject.project.build(IncrementalProjectBuilder.FULL_BUILD, null)
        def unit = JavaCore.createCompilationUnitFrom(file)
        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, new NoChoiceQuery())
        TextEdit edit = organize.calculateMissingImports()
        TextEdit[] children = edit.getChildren()
        assertEquals("Wrong number of deleted imports:\n$contents\nand edits:\n$edit",
                numDeletes, children.length);
        for (TextEdit child : children) {
            assertTrue("$child is not a delete edit", child instanceof DeleteEdit)
        }
    }
    
    
    void doChoiceTest(contents, expectedChoices) {
        def file = testProject.createGroovyTypeAndPackage("main", "Main.groovy", contents)
        testProject.project.build(IncrementalProjectBuilder.FULL_BUILD, null)
        def unit = JavaCore.createCompilationUnitFrom(file)
        def query = new ChoiceQuery()
        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, query)
        organize.calculateMissingImports()
        for(choice in expectedChoices) {
            assertTrue("Should have found $choice in choices", query.choices.contains(choice))
        }
        assertEquals "Wrong number of choices found.  Expecting:\n$expectedChoices\nFound:\n$query.choices", query.choices.size(), expectedChoices.size()
    }

}

class NoChoiceQuery implements IChooseImportQuery {
    public TypeNameMatch[] chooseImports(TypeNameMatch[][] matches, ISourceRange[] range) {
        throw new Exception("Should not have a choice, but found $matches[0][0] and $matches[0][1]")
        return []
    }
}
class ChoiceQuery implements IChooseImportQuery {
    def choices
    public TypeNameMatch[] chooseImports(TypeNameMatch[][] matches, ISourceRange[] range) {
        choices = matches[0].collect { it.getType().getFullyQualifiedName() }
        return []
    }
} 