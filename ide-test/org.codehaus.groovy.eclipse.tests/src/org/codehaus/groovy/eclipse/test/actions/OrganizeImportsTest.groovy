package org.codehaus.groovy.eclipse.test.actions

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.core.resources.IncrementalProjectBuilder;;
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports;

/**
 * @author Andrew Eisenberg
 * @created Aug 18, 2009
 *
 */
public class OrganizeImportsTest extends EclipseTestCase {

    final static String CONTENTS_SUPPORTING = 
        """
        class FirstClass { }
        class SecondClass { }
        class ThirdClass { }
        """
    final static String CONTENTS_SUPPORTING2 = 
        """
        class FourthClass { }
        """
    final static String CONTENTS_JAVA_SUPPORTING = 
        """
        public class Outer { 
            public static class Inner { }
        }
        """

    protected void setUp() throws Exception {
        super.setUp()
        testProject.addNature(GroovyNature.GROOVY_NATURE)
        GroovyRuntime.addGroovyRuntime(testProject.getProject())
        testProject.createGroovyTypeAndPackage("other", "Other.groovy", CONTENTS_SUPPORTING)
        testProject.createGroovyTypeAndPackage("other2", "Other.groovy", CONTENTS_SUPPORTING2)
        testProject.createGroovyTypeAndPackage("other3", "Other.groovy", CONTENTS_SUPPORTING2)
        testProject.createJavaTypeAndPackage("other", "Outer.java", CONTENTS_JAVA_SUPPORTING)
    }
    
    void testAddImport1() {
        String contents = 
        """ 
        FirstClass x
        """
        def expectedImports = ["import other.FirstClass;\n"]
        doAddImportTest(contents, expectedImports)
    }

    void testAddImport2() {
        String contents = 
        """ 
        def x = new FirstClass()
        """
        def expectedImports = ["import other.FirstClass;\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddImport3() {
        String contents = 
""" 
    def x(FirstClass y) { }
"""
        def expectedImports = ["import other.FirstClass;\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddImport4() {
        String contents = 
            """ 
            def x = { FirstClass y -> print "" }
            """
            def expectedImports = ["import other.FirstClass;\n"]
            doAddImportTest(contents, expectedImports)
    }
    void testAddImport5() {
        String contents = 
            """ 
            class Main {
                FirstClass x
            }
            """
            def expectedImports = ["import other.FirstClass;\n"]
            doAddImportTest(contents, expectedImports)
    }
    void testAddImport6() {
        String contents = 
            """ 
            class Main {
            FirstClass x() { }
            }
            """
            def expectedImports = ["import other.FirstClass;\n"]
            doAddImportTest(contents, expectedImports)
    }
    void testAddImport7() {
        String contents = 
            """ 
            class Main {
            def x(FirstClass f) { }
            }
            """
            def expectedImports = ["import other.FirstClass;\n"]
            doAddImportTest(contents, expectedImports)
    }
    void testAddImport8() {
        String contents = 
            """ 
            class Main {
            def x(FirstClass[] f) { }
            }
            """
            def expectedImports = ["import other.FirstClass;\n"]
            doAddImportTest(contents, expectedImports)
    }

    void testAddImport9() {
        String contents = 
        """
        import javax.swing.text.html.HTML;
        HTML.class
        """
        def expectedImports = []
        doAddImportTest(contents, expectedImports)
    }

    void testAddInnerImport1() {
        String contents = 
            """ 
            class Main {
            def x(Inner f) { }
            }
            """
            def expectedImports = ["import other.Outer.Inner;\n"]
            doAddImportTest(contents, expectedImports)
    }
    void testAddInnerImport2() {
        String contents = 
            """ 
            class Main {
            def x(Outer.Inner f) { }
            }
            """
            def expectedImports = ["import other.Outer;\n"]
            doAddImportTest(contents, expectedImports)
    }
    void testAddInnerImport3() {
        String contents = 
        """ 
        import other.Outer;
        class Main {
        def x(Outer.Inner f) { }
        }
        """
        def expectedImports = []
        doAddImportTest(contents, expectedImports)
    }
    void testAddInnerImport4() {
        String contents = 
            """ 
            class Main {
            def x(UnknownTag f) { }
            }
            """
            def expectedImports = ["import javax.swing.text.html.HTML.UnknownTag;"]
            doAddImportTest(contents, expectedImports)
    }
    void testAddInnerImport5() {
        String contents = 
            """ 
            class Main {
            def x(HTML.UnknownTag f) { }
            }
            """
            def expectedImports = ["import javax.swing.text.html.HTML;"]
                                   doAddImportTest(contents, expectedImports)
    }

    void testRemoveImport1() {
        String contents = 
            """ 
            import other.SecondClass
            """
            doDeleteImportTest(contents, 1)
    }
    void testRemoveImport1a() {
        String contents = 
            """ 
            import other.SecondClass
            a
            """
            doDeleteImportTest(contents, 1)
    }

    void testRemoveImport2() {
        String contents = 
            """ 
            import other.SecondClass
            import javax.swing.text.html.HTML
            class Main {
                HTML f = null
            }
            """
            doDeleteImportTest(contents, 1)
    }

    void testRemoveImport3() {
        String contents = 
            """ 
            import other.ThirdClass
            import javax.swing.text.html.HTML
            import other.SecondClass
            class Main {
                HTML f = null
            }
            """
            doDeleteImportTest(contents, 2)
    }

    void testChoices1() {
        String contents = 
            """ 
            FourthClass f = null
            """
        doChoiceTest(contents, ["other2.FourthClass", "other3.FourthClass"])    
    }
    


    void doAddImportTest(contents, expectedImports) {
        def file = testProject.createGroovyTypeAndPackage("main", "Main.groovy", contents)
        def unit = JavaCore.createCompilationUnitFrom(file)
        OrganizeGroovyImports organize = new OrganizeGroovyImports(unit, new NoChoiceQuery())
        TextEdit edit = organize.calculateMissingImports()
        TextEdit[] children = edit.getChildren()
        if (expectedImports.size() > 0) {
            assertEquals "Found incorrect imports in text edit: \n$edit\nwith expected imports:\n$expectedImports", expectedImports.size(), children.length-1
        } else {
            assertEquals "Found incorrect imports in text edit: \n$edit\nwith expected imports:\n$expectedImports", 0, children.length
        }
        

        def notFound = ""
        for (TextEdit child : children) {
            if (! child instanceof InsertEdit) {
                notFound << "Found an invalid Edit: $child\n"
            } else if (!contents.contains(child.getText())) {
                notFound << child.getText() << "\n"
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