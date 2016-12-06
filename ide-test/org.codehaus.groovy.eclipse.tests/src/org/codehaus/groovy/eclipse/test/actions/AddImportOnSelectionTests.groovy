/*
 * Copyright 2009-2016 the original author or authors.
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

import org.codehaus.groovy.eclipse.refactoring.actions.AddImportOnSelectionAction
import org.codehaus.groovy.eclipse.test.ui.GroovyEditorTest

final class AddImportOnSelectionTests extends GroovyEditorTest {

    private void addImportOnSelection(CharSequence sourceCode) {
        makeEditor(sourceCode.stripIndent().toString())
        new AddImportOnSelectionAction(editor).run()
    }

    void testAddImportOnScriptVarType() {
        addImportOnSelection "java.util.regex.P${CARET}attern p = ~/123/"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern p = ~/123/"
    }

    void testAddImportOnScriptVarType2() {
        addImportOnSelection "java.util.regex.P${CARET}attern[] p = [~/123/]"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern[] p = [~/123/]"
    }

    void testAddImportOnScriptVarValue() {
        addImportOnSelection "def p = java.util.regex.P${CARET}attern.compile('123')"
        assertEditorContents "import java.util.regex.Pattern\n\ndef p = Pattern.compile('123')"
    }

    void testAddImportOnScriptVarValue2() {
        addImportOnSelection "def p = java.util.regex.Pattern.c${CARET}ompile('123')"
        assertEditorContents "import static java.util.regex.Pattern.compile\n\ndef p = compile('123')"
    }

    // types

    void testAddImportOnSuperType() {
        addImportOnSelection "class B extends java.io.B${CARET}ufferedReader {}"
        assertEditorContents "import java.io.BufferedReader\n\nclass B extends BufferedReader {}"
    }

    void testAddImportOnSuperInterface() {
        addImportOnSelection "class C implements java.util.concurrent.C${CARET}allable {}"
        assertEditorContents "import java.util.concurrent.Callable\n\nclass C implements Callable {}"
    }

    void testAddImportOnSuperInterfaceGenerics() {
        addImportOnSelection "class C implements java.util.concurrent.Callable<java.util.regex.P${CARET}attern> {}"
        assertEditorContents "import java.util.regex.Pattern\n\nclass C implements java.util.concurrent.Callable<Pattern> {}"
    }

    void testAddImportOnClassAnnotation() {
        addImportOnSelection "@groovy.transform.T${CARET}ypeChecked class C {}"
        assertEditorContents "import groovy.transform.TypeChecked\n\n@TypeChecked class C {}"
    }

    void testAddImportOnImportAnnotation() {
        addImportOnSelection "@javax.annotation.G${CARET}enerated import java.lang.StringBuffer"
        assertEditorContents "@Generated import java.lang.StringBuffer\n\nimport javax.annotation.Generated"
    }

    void testAddImportOnPackageAnnotation() {
        addImportOnSelection "@javax.annotation.G${CARET}enerated package a.b.c.d;"
        assertEditorContents "@Generated package a.b.c.d;\n\nimport javax.annotation.Generated\n"
    }

    void testAddImportOnAnnotationAnnotation() {
        addImportOnSelection "@java.lang.annotation.D${CARET}ocumented @interface Tag {}"
        assertEditorContents "import java.lang.annotation.Documented\n\n@Documented @interface Tag {}"
    }

    // constructors/initializers

    void testAddImportOnConstructorParam() {
        addImportOnSelection """\
            class C {
              C(java.util.regex.P${CARET}attern p) {}
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              C(Pattern p) {}
            }
            """
    }

    void testAddImportOnConstructorParamGenerics() {
        addImportOnSelection """\
            class C {
              C(List<java.util.regex.P${CARET}attern> pats) {}
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              C(List<Pattern> pats) {}
            }
            """
    }

    void testAddImportOnConstructorBody() {
        addImportOnSelection """\
            class C {
              C() {
                java.util.regex.P${CARET}attern p = ~/123/
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              C() {
                Pattern p = ~/123/
              }
            }
            """
    }

    void testAddImportOnTypeInClassInit() {
        addImportOnSelection """\
            class C {
              {
                java.util.regex.P${CARET}attern p = ~/123/
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              {
                Pattern p = ~/123/
              }
            }
            """
    }

    void testAddImportOnTypeInStaticInit() {
        addImportOnSelection """\
            class C {
              static {
                java.util.regex.P${CARET}attern p = ~/123/
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              static {
                Pattern p = ~/123/
              }
            }
            """
    }

    // fields

    void testAddImportOnFieldType() {
        addImportOnSelection """\
            class C {
              java.util.regex.P${CARET}attern p = ~/123/
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              Pattern p = ~/123/
            }
            """
    }

    void testAddImportOnLazyFieldType() {
        addImportOnSelection """\
            class C {
              @Lazy java.util.regex.P${CARET}attern p = ~/123/
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              @Lazy Pattern p = ~/123/
            }
            """
    }

    void testAddImportOnFieldTypeGenerics() {
        addImportOnSelection """\
            class C {
              List<java.util.regex.P${CARET}attern> pats
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              List<Pattern> pats
            }
            """
    }

    void testAddImportOnFieldAnnotation() {
        addImportOnSelection """\
            class C {
              @javax.annotation.G${CARET}enerated Object o
            }
            """
        assertEditorContents """\
            import javax.annotation.Generated

            class C {
              @Generated Object o
            }
            """
    }

    void testAddImportOnFieldInit() {
        addImportOnSelection """\
            class C {
              def p = java.util.regex.P${CARET}attern.compile('123')
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              def p = Pattern.compile('123')
            }
            """
    }

    void testAddImportOnFieldInit2() {
        addImportOnSelection """\
            class C {
              def p = java.util.regex.Pattern.c${CARET}ompile('123')
            }
            """
        assertEditorContents """\
            import static java.util.regex.Pattern.compile

            class C {
              def p = compile('123')
            }
            """
    }

    // methods

    void testAddImportOnMethodReturnType() {
        addImportOnSelection """\
            class C {
              java.util.regex.P${CARET}attern meth() {}
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              Pattern meth() {}
            }
            """
    }

    void testAddImportOnMethodReturnTypeGenerics() {
        addImportOnSelection """\
            import java.util.concurrent.Callable
            class C {
              Callable<java.util.regex.P${CARET}attern> meth() {}
            }
            """
        assertEditorContents """\
            import java.util.concurrent.Callable
            import java.util.regex.Pattern
            class C {
              Callable<Pattern> meth() {}
            }
            """
    }

    void testAddImportOnMethodGenerics() {
        addImportOnSelection """\
            class C {
              def <T extends java.util.concurrent.C${CARET}allable> T meth(T parm) {}
            }
            """
        assertEditorContents """\
            import java.util.concurrent.Callable

            class C {
              def <T extends Callable> T meth(T parm) {}
            }
            """
    }

    void testAddImportOnMethodAnnotation() {
        addImportOnSelection """\
            class C {
              @javax.annotation.G${CARET}enerated
              def meth() {}
            }
            """
        assertEditorContents """\
            import javax.annotation.Generated

            class C {
              @Generated
              def meth() {}
            }
            """
    }

    void testAddImportOnMethodParams() {
        addImportOnSelection """\
            class C {
              def meth(java.util.regex.P${CARET}attern p) {
              }
            }
            """
            assertEditorContents """\
            import java.util.regex.Pattern
            class C {
              def meth(Pattern p) {
              }
            }
            """
    }

    void testAddImportOnMethodParamGenerics() {
        addImportOnSelection """\
            class C {
              def meth(List<java.util.regex.P${CARET}attern> pats) {}
            }
            """
            assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              def meth(List<Pattern> pats) {}
            }
            """
    }

    void testAddImportOnMethodParamAnnotation() {
        addImportOnSelection """\
            class C {
              def meth(@javax.annotation.G${CARET}enerated Object o) {}
            }
            """
            assertEditorContents """\
            import javax.annotation.Generated

            class C {
              def meth(@Generated Object o) {}
            }
            """
    }

    // coercion, typecast, method call generics, for params, catch params, closure params, closure body, method pointer
    // inner class type, anon inner class type, inner class body
}
