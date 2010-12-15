
package org.codehaus.groovy.eclipse.test.actions

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports;
import org.codehaus.groovy.eclipse.test.EclipseTestCase 
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.jdt.core.ISourceRange 
import org.eclipse.jdt.core.search.TypeNameMatch 
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery 
import org.eclipse.text.edits.DeleteEdit 
import org.eclipse.text.edits.InsertEdit 
import org.eclipse.text.edits.TextEdit 
import org.eclipse.jdt.core.JavaCore
import org.eclipse.core.resources.IncrementalProjectBuilder

/**
 * @author Andrew Eisenberg
 * @created Aug 18, 2009
 *
 */
public class OrganizeImportsTest extends AbstractOrganizeImportsTest {
    
    void testAddImport1() {
        String contents = 
                """ 
                FirstClass x
                """
        def expectedImports = ["import other.FirstClass\n"]
        doAddImportTest(contents, expectedImports)
    }
    
    void testAddImport2() {
        String contents = 
                """ 
                def x = new FirstClass()
                """
        def expectedImports = ["import other.FirstClass\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddImport3() {
        String contents = 
                """ 
                def x(FirstClass y) { }
                """
        def expectedImports = ["import other.FirstClass\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddImport4() {
        String contents = 
                """ 
            def x = { FirstClass y -> print "" }
            """
        def expectedImports = ["import other.FirstClass\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddImport5() {
        String contents = 
                """ 
            class Main {
                FirstClass x
            }
            """
        def expectedImports = ["import other.FirstClass\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddImport6() {
        String contents = 
                """ 
            class Main {
            FirstClass x() { }
            }
            """
        def expectedImports = ["import other.FirstClass\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddImport7() {
        String contents = 
                """ 
            class Main {
            def x(FirstClass f) { }
            }
            """
        def expectedImports = ["import other.FirstClass\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddImport8() {
        String contents = 
                """ 
            class Main {
            def x(FirstClass[] f) { }
            }
            """
        def expectedImports = ["import other.FirstClass\n"]
        doAddImportTest(contents, expectedImports)
    }
    
    void testAddImport9() {
        String contents = 
                """
        import javax.swing.text.html.HTML
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
        def expectedImports = ["import other.Outer.Inner\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddInnerImport2() {
        String contents = 
                """ 
            class Main {
            def x(Outer.Inner f) { }
            }
            """
        def expectedImports = ["import other.Outer\n"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddInnerImport3() {
        String contents = 
                """ 
        import other.Outer
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
        def expectedImports = ["import javax.swing.text.html.HTML.UnknownTag"]
        doAddImportTest(contents, expectedImports)
    }
    void testAddInnerImport5() {
        String contents = 
                """ 
            class Main {
            def x(HTML.UnknownTag f) { }
            }
            """
        def expectedImports = ["import javax.swing.text.html.HTML"]
        doAddImportTest(contents, expectedImports)
    }
    
    // test that 'as' keyword works in list expressions
    void testGRECLIPSE470a() {
        String contents ="""
	        import javax.xml.XMLConstants
	        ['value':XMLConstants.XML_NS_URI] as Map
	        """
        doAddImportTest(contents)
    }
    
    void testGenerics() {
        String contents ="""
            import java.util.Map.Entry
		    Entry<SecondClass, HTML> entry
            """
        def expectedImports = [ "import javax.swing.text.html.HTML", "import other.SecondClass"]
        doAddImportTest(contents, expectedImports)
    }
    
    void testRemoveImport1() {
        // should not remove import because module is empty
        String contents = 
                """ 
            import other.SecondClass
            """
        doDeleteImportTest(contents, 0)
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
    
    void testGRECLISPE506() {
        String contents = 
                """ 
            import java.text.DateFormat
        
            new String(DateFormat.getDateInstance())
            """
        doAddImportTest(contents)
    }
    
    void testGRECLISPE546a() {
        String contents = 
                """ 
            import java.text.DateFormat
            
            class Foo {
                Foo(DateFormat arg) { }
            }
            """
        doAddImportTest(contents)
    }
    
    void testGRECLISPE546b() {
        String contents = 
                """ 
            
            class Foo {
            Foo(DateFormat arg) { }
            }
            """
        def expectedImports = ['java.text.DateFormat']
        doAddImportTest(contents, expectedImports)
    }
    
    // should not have a stack overflow
    void testGRECLISPE643() {
        String contents = 
                """ 
            enum MyEnum {
                ONE_VALUE, ANOTHER_VALUE
            }
            """
        doAddImportTest(contents, [ ])
    }
    
    
    void testDynamicVariable1() {
        String contents = 
                """
		    HTML.NULL_ATTRIBUTE_VALUE
            """
        def expectedImports = [ 'javax.swing.text.html.HTML']
        doAddImportTest(contents, expectedImports)
    }
    void testDynamicVariable2() {
        String contents = 
                """
	        nothing.HTML.NULL_ATTRIBUTE_VALUE
	        """
        doAddImportTest(contents)
    }
    
    void testDynamicVariable3() {
        String contents = 
                """ 
		    new String(DateFormat.getDateInstance())
		    """
        def expectedImports = [ 'java.text.DateFormat' ] 
        doAddImportTest(contents, expectedImports)
    }
    
    // Test GRECLISPE-823
    void testThrownExceptions() {
        String contents = 
                """ 
            import java.util.zip.ZipException
            
            def x() throws BadLocationException {
            }
            def y() throws ZipException {
            }
            """
        def expectedImports = [ 'javax.swing.text.BadLocationException' ] 
        doAddImportTest(contents, expectedImports)
    }
    
    // Test GRECLIPSE-895
    void testCatchClausesExceptions() {
        String contents =
                """
            import java.util.zip.ZipException
            
            try {
                nothing
            } catch (ZipException e1) {
            
            } catch (BadLocationException e2) {
            
            }
            """
        def expectedImports = [ 'javax.swing.text.BadLocationException' ]
        doAddImportTest(contents, expectedImports)
    }
    
    // Test GRECLIPSE-600
    void testNestedAnnotations1() {
        testProject.createGroovyTypeAndPackage "anns", "Annotations.groovy", 
                """
            @interface NamedQueries {
              NamedQuery value();
            }
            
            @interface NamedQuery {
            }"""
        
        String contents = """
            @NamedQueries(
                @NamedQuery 
            )
            class MyEntity {  }
        """
        def expectedImports = ['anns.NamedQueries', 'anns.NamedQuery']
        doAddImportTest(contents, expectedImports)
    }
    
    // Test GRECLIPSE-600
    void testNestedAnnotations2() {
        testProject.createGroovyTypeAndPackage "anns", "Annotations.groovy", 
            """
            @interface NamedQueries {
              NamedQuery value();
            }
            
            @interface NamedQuery {
            }"""
        
        String contents = """
            import anns.NamedQueries
            import anns.NamedQuery
            
            @NamedQueries(
                @NamedQuery 
            )
            class MyEntity {  }
        """
        doAddImportTest(contents)
    }
    
    // Test GRECLIPSE-600
    void testNestedAnnotations3() {
        testProject.createGroovyTypeAndPackage "anns", "Annotations.groovy",
                """
            @interface NamedQueries {
              NamedQuery[] value();
            }
            
            @interface NamedQuery {
            }"""
        
        String contents = """
            @NamedQueries(
                [@NamedQuery]
            )
            class MyEntity {  }
        """
        def expectedImports = ['anns.NamedQueries', 'anns.NamedQuery']
        doAddImportTest(contents, expectedImports)
    }
    
    // Test GRECLIPSE-600
    void testNestedAnnotations4() {
        testProject.createGroovyTypeAndPackage "anns", "Annotations.groovy",
            """
            @interface NamedQueries {
              NamedQuery[] value();
            }
            
            @interface NamedQuery {
            }"""
        
        String contents = """
            import anns.NamedQueries
            import anns.NamedQuery
            
            @NamedQueries(
                [@NamedQuery]
            )
            class MyEntity {  }
        """
        doAddImportTest(contents)
    }
    
    void testInnerClass1() {
        testProject.createGroovyTypeAndPackage "inner", "HasInner.groovy",
                """
                class HasInner {
                  class InnerInner { }
                }
                """
        
        String contents =
            """
            InnerInner f
            """
        def expectedImports = [ 'inner.HasInner.InnerInner']
        doAddImportTest(contents, expectedImports)
    }
    
    void testInnerClass2() {
        testProject.createGroovyTypeAndPackage "inner", "HasInner.groovy",
                """
                class HasInner {
                  class InnerInner { }
                }
                """
        String contents =
                """
            import inner.HasInner.InnerInner\n
            InnerInner f
            """
        doAddImportTest(contents)
    }
    
    void testInnerClass3() {
        testProject.createGroovyTypeAndPackage "inner", "HasInner.groovy",
                """
                class HasInner {
                  class InnerInner { }
                }
                """
        String contents =
            """
            HasInner.InnerInner f
            """
            def expectedImports = [ 'inner.HasInner']
        doAddImportTest(contents, expectedImports)
    }
    
    void testInnerClass4() {
        testProject.createGroovyTypeAndPackage "inner", "HasInner.groovy",
                """
                class HasInner {
                  class InnerInner { }
                }
                """
        String contents =
            """
            import inner.HasInner\n
            HasInner.InnerInner f
            """
        doAddImportTest(contents)
    }
    
    
    // GRECLIPSE-929
    void testStaticImport() {
        String contents =
                """
                import static java.lang.String.format
                format
                """
        doAddImportTest(contents)
    }

    // GRECLIPSE-929
    void testStaticImport2() {
        // never remove static imports
        String contents =
            """
            import static java.lang.String.format
            """
            doAddImportTest(contents)
    }
    
    // GRECLIPSE-929
    void testStaticStarImport() {
        String contents =
            """
            import static java.lang.String.*
            format
            """
            doAddImportTest(contents)
    }
    
    // GRECLIPSE-929
    void testStaticStarImport2() {
        // never remove static star imports
        String contents =
            """
            import static java.lang.String.*
            """
            doAddImportTest(contents)
    }
    
    // GRECLIPSE-929
    void testStarImport() {
        String contents =
            """
            import javax.swing.text.html.*
            HTML
            """
            doAddImportTest(contents)
    }
    
    // GRECLIPSE-929
    void testStarImport2() {
        // never remove star imports
        String contents =
            """
            import javax.swing.text.html.*
            """
            doAddImportTest(contents)
    }
}

