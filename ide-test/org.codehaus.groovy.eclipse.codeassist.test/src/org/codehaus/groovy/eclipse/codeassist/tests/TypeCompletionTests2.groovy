/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.ui.PreferenceConstants
import org.junit.Test
import org.osgi.framework.Version

/**
 * Ensures type completion is working and that the resulting document remains well-formed.
 */
final class TypeCompletionTests2 extends CompletionTestSuite {

    private void checkProposal(String source, String target, String proposalSite, String proposalName) {
        checkProposalApplicationType(source, target, getIndexOf(source, proposalSite), proposalName)
    }

    //

    @Test
    void testTypeCompletionInScript1() {
        String contents = 'HTML'
        String expected = 'import javax.swing.text.html.HTML\n\nHTML'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript2() {
        String contents = 'import javax.swing.plaf.ButtonUI\n\nHTML\nButtonUI'
        String expected = 'import javax.swing.plaf.ButtonUI\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript3() {
        String contents = '/*header*/\nimport javax.swing.plaf.ButtonUI //note\n\nHTML\nButtonUI'
        String expected = '/*header*/\nimport javax.swing.plaf.ButtonUI //note\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript4() {
        String contents = 'import javax.swing.plaf.ButtonUI\n\nHTML\nButtonUI'
        String expected = 'import javax.swing.plaf.ButtonUI\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript5() {
        String contents = 'import javax.swing.plaf.ButtonUI as Button\n\nHTML\nButton'
        String expected = 'import javax.swing.plaf.ButtonUI as Button\nimport javax.swing.text.html.HTML\n\nHTML\nButton'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript6() {
        String contents = 'import javax.swing.plaf.ButtonUI as Button\n\nHTML\nButton'
        String expected = 'import javax.swing.plaf.ButtonUI as Button\nimport javax.swing.text.html.HTML\n\nHTML\nButton'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript7() {
        String contents = 'import javax.swing.plaf.*\n\nHTML\nButtonUI'
        String expected = 'import javax.swing.plaf.*\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript8() {
        String contents = 'import javax.swing.plaf.*\n\nHTML\nButtonUI'
        String expected = 'import javax.swing.plaf.*\nimport javax.swing.text.html.HTML\n\nHTML\nButtonUI'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript9() {
        String contents = 'import static java.util.Collections.emptyList\n\nHTML\ndef list = emptyList()'
        String expected = 'import static java.util.Collections.emptyList\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = emptyList()'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript10() {
        String contents = 'import static java.util.Collections.emptyList\n\nHTML\ndef list = emptyList()'
        String expected = 'import static java.util.Collections.emptyList\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = emptyList()'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript11() {
        String contents = 'import static java.util.Collections.EMPTY_LIST as EMPTY\n\nHTML\ndef list = EMPTY'
        String expected = 'import static java.util.Collections.EMPTY_LIST as EMPTY\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = EMPTY'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript12() {
        String contents = 'import static java.util.Collections.EMPTY_LIST as EMPTY\n\nHTML\ndef list = EMPTY'
        String expected = 'import static java.util.Collections.EMPTY_LIST as EMPTY\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = EMPTY'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript13() {
        String contents = 'import static java.util.Collections.*\n\nHTML\ndef list = emptyList()'
        String expected = 'import static java.util.Collections.*\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = emptyList()'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript14() {
        String contents = 'import static java.util.Collections.*\n\nHTML\ndef list = emptyList()'
        String expected = 'import static java.util.Collections.*\n\nimport javax.swing.text.html.HTML\n\nHTML\ndef list = emptyList()'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInScript15() {
        String contents = '''\
            |/* header comment */
            |import javax.swing.plaf.ButtonUI /*tag*/ //note
            |  // some single-line comment
            |
            |HTML
            |ButtonUI
            |'''.stripMargin()
        String expected = '''\
            |/* header comment */
            |import javax.swing.plaf.ButtonUI /*tag*/ //note
            |  // some single-line comment
            |import javax.swing.text.html.HTML
            |
            |HTML
            |ButtonUI
            |'''.stripMargin()

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript1() {
        String contents = 'def x(HTML'
        String expected = 'import javax.swing.text.html.HTML\n\ndef x(HTML'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript2() {
        String contents = 'package f\n\ndef x(HTML'
        String expected = 'package f\n\nimport javax.swing.text.html.HTML\n\n\ndef x(HTML'
        // deal with some variance in JDT Core adding first import between package and script body
        if ((JavaCore.plugin.bundle.version <=> new Version(3, 12, 3)) >= 0) {
            expected = expected.replace('\n\n\n', '\n\n')
        }

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript3() {
        String contents = 'package f;\n\ndef x(HTML'
        String expected = 'package f;\n\nimport javax.swing.text.html.HTML\n\n\ndef x(HTML'
        // deal with some variance in JDT Core adding first import between package and script body
        if ((JavaCore.plugin.bundle.version <=> new Version(3, 12, 3)) >= 0) {
            expected = expected.replace('\n\n\n', '\n\n')
        }

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript4() {
        String contents = '/**some stuff*/\npackage f\n\ndef x(HTML'
        String expected = '/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML\n\n\ndef x(HTML'
        // deal with some variance in JDT Core adding first import between package and script body
        if ((JavaCore.plugin.bundle.version <=> new Version(3, 12, 3)) >= 0) {
            expected = expected.replace('\n\n\n', '\n\n')
        }

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript5() {
        String contents = '/**some stuff*/\npackage f;\n\ndef x(HTML'
        String expected = '/**some stuff*/\npackage f;\n\nimport javax.swing.text.html.HTML\n\n\ndef x(HTML'
        // deal with some variance in JDT Core adding first import between package and script body
        if ((JavaCore.plugin.bundle.version <=> new Version(3, 12, 3)) >= 0) {
            expected = expected.replace('\n\n\n', '\n\n')
        }

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript6() {
        String contents = '/**some stuff*/\n\nimport javax.swing.plaf.ButtonUI\n\ndef x(HTML'
        String expected = '/**some stuff*/\n\nimport javax.swing.plaf.ButtonUI\nimport javax.swing.text.html.HTML\n\ndef x(HTML'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript7() {
        String contents = '/**some stuff*/\n\nimport javax.swing.plaf.ButtonUI;\n\ndef x(HTML'
        String expected = '/**some stuff*/\n\nimport javax.swing.plaf.ButtonUI;\nimport javax.swing.text.html.HTML\n\ndef x(HTML'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript8() {
        String contents = '/**some stuff*/\npackage f\n\nimport javax.swing.plaf.ButtonUI\n\ndef x(HTML'
        String expected = '/**some stuff*/\npackage f\n\nimport javax.swing.plaf.ButtonUI\nimport javax.swing.text.html.HTML\n\ndef x(HTML'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript9() {
        String contents = '/**some stuff*/\npackage f;\n\nimport javax.swing.plaf.ButtonUI;\n\ndef x(HTML'
        String expected = '/**some stuff*/\npackage f;\n\nimport javax.swing.plaf.ButtonUI;\nimport javax.swing.text.html.HTML\n\ndef x(HTML'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript10() {
        String contents = '''\
            |/**some stuff*/
            |package f
            |HTML
            |'''.stripMargin()
        String expected = '''\
            |/**some stuff*/
            |package f
            |
            |import javax.swing.text.html.HTML
            |
            |HTML
            |'''.stripMargin()

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript11() {
        String contents = '''\
            |/**some stuff*/
            |package f
            |
            |import javax.swing.plaf.ButtonUI
            |
            |def x(HTML
            |'''.stripMargin()
        String expected = '''\
            |/**some stuff*/
            |package f
            |
            |import javax.swing.plaf.ButtonUI
            |import javax.swing.text.html.HTML
            |
            |def x(HTML
            |'''.stripMargin()

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript12() {
        String contents = '''\
            |/**some stuff*/
            |package f
            |
            |import javax.swing.plaf.ButtonUI
            |
            |def x(HTML
            |'''.stripMargin()
        String expected = '''\
            |/**some stuff*/
            |package f
            |
            |import javax.swing.plaf.ButtonUI
            |import javax.swing.text.html.HTML
            |
            |def x(HTML
            |'''.stripMargin()

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript13() {
        String contents = '''\
            |/**some stuff*/
            |package f
            |
            |import javax.swing.plaf.ColorChooserUI
            |import java.awt.dnd.DropTarget as Foo
            |
            |def x(HTML
            |'''.stripMargin()
        String expected = '''\
            |/**some stuff*/
            |package f
            |
            |import javax.swing.plaf.ColorChooserUI
            |import javax.swing.text.html.HTML
            |
            |import java.awt.dnd.DropTarget as Foo
            |
            |def x(HTML
            |'''.stripMargin()

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenScript14() {
        String contents = '''\
            |/**some stuff*/
            |
            |import javax.swing.plaf.ColorChooserUI
            |import java.awt.dnd.DropTarget as Foo
            |
            |def x(HTML
            |'''.stripMargin()
        String expected = '''\
            |/**some stuff*/
            |
            |import javax.swing.plaf.ColorChooserUI
            |import javax.swing.text.html.HTML
            |
            |import java.awt.dnd.DropTarget as Foo
            |
            |def x(HTML
            |'''.stripMargin()

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test // GRECLIPSE-926
    void testTypeCompletionInBrokenScript15() {
        String contents = '''\
            |package f
            |
            |import javax.swing.text.html.HTML
            |
            |void nuthin() {
            |  if (! (this instanceof HTMLT/*_*/) {
            |    HTML
            |  }
            |}
            |'''.stripMargin()
        String expected = '''\
            |package f
            |
            |import javax.swing.text.html.HTML
            |
            |import org.w3c.dom.html.HTMLTableCaptionElement
            |
            |void nuthin() {
            |  if (! (this instanceof HTMLTableCaptionElement/*_*/) {
            |    HTML
            |  }
            |}
            |'''.stripMargin()

        checkProposal(contents, expected, 'HTMLT', 'HTMLTableCaptionElement - org.w3c.dom.html')
    }

    @Test
    void testTypeCompletionInBrokenClass1() {
        String contents = '/**some stuff*/\npackage f\n\nclass Y {\ndef x(HTML'
        String expected = '/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML\n\nclass Y {\ndef x(HTML'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testTypeCompletionInBrokenClass2() {
        String contents = '/**some stuff*/\npackage f\n\nclass Y extends HTML {\ndef x(H'
        String expected = '/**some stuff*/\npackage f\n\nimport javax.swing.text.html.HTML\n\nclass Y extends HTML {\ndef x(H'

        checkProposal(contents, expected, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/177
    void testTypeCompletionForClassAnnotation() {
        setJavaPreference(PreferenceConstants.ORGIMPORTS_IMPORTORDER, '\\#;java;javax;groovy;groovyx;;')

        String contents = '''\
            |import static org.mockito.Mockito.mock
            |
            |import org.junit.Test
            |
            |@TypeCh
            |final class WeakReferenceSetTests {
            |  @Test
            |  void testAddAndEmpty() {
            |    WeakReferenceSet set = new WeakReferenceSet()
            |    assert set.empty
            |    set << mock(ConcreteType)
            |    assert !set.empty
            |  }
            |}
            |'''.stripMargin()
        String expected = '''\
            |import static org.mockito.Mockito.mock
            |
            |import groovy.transform.TypeChecked
            |
            |import org.junit.Test
            |
            |@TypeChecked
            |final class WeakReferenceSetTests {
            |  @Test
            |  void testAddAndEmpty() {
            |    WeakReferenceSet set = new WeakReferenceSet()
            |    assert set.empty
            |    set << mock(ConcreteType)
            |    assert !set.empty
            |  }
            |}
            |'''.stripMargin()
        checkProposal(contents, expected, '@TypeCh', 'TypeChecked - groovy.transform')
    }
}
