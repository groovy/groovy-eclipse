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
package org.codehaus.groovy.eclipse.dsl.tests

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.eclipse.core.resources.IResource
import org.eclipse.jdt.core.Flags
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.JavaCore
import org.junit.Test

/**
 * Tests type inferencing for DSL scripts included with Groovy plugin.
 */
final class BuiltInDSLInferencingTests extends DSLInferencingTestSuite {

    BuiltInDSLInferencingTests() {
        doRemoveClasspathContainer = false
    }

    @Test
    void testBasics() {
        assert GroovyRuntime.findClasspathEntry(javaProject) { it.path == GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID } : 'Should have DSL support classpath container'

        IClasspathContainer container = JavaCore.getClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, javaProject)
        assert container.classpathEntries.length == 2 : "Wrong number of classpath entries found: ${ -> Arrays.toString(container.classpathEntries)}"

        IClasspathEntry pluginEntry = javaProject.getResolvedClasspath(true).find { it.path.toString().contains('plugin_dsld') }
        IPackageFragmentRoot root = javaProject.children.find { it.elementName.contains('plugin_dsld') }
        List<String> elements = javaProject.children*.elementName

        List<String> possibleFrags = javaProject.packageFragments.findAll { it.elementName == 'dsld' }.collectMany { frag ->
            def items = [frag.toString()]
            if (frag.children) {
                items << '  ['
                for (child in frag.children) {
                    items << ('    ' + child.elementName)
                }
                items << '  ]'
            }
            return items
        }

        assert pluginEntry != null : "Did not find the Plugin DSLD classpath entry.  Exsting resolved roots: [\n${ -> elements.join(', ')}\n]\nOther DSLD fragments: [\n${ -> possibleFrags.join('\n')}\n]"
        assert root != null : "Plugin DSLD classpath entry should exist.  Exsting resolved roots: [\n${ -> elements.join(', ')}\n]\nOther DSLD fragments: [\n${ -> possibleFrags.join('\n')}\n]"
        assert root.exists() : 'Plugin DSLD classpath entry should exist'

        root.resource().refreshLocal(IResource.DEPTH_INFINITE, null)
        root.close(); root.open(null)

        IPackageFragment frag = root.getPackageFragment('dsld')
        assert frag.exists() : 'DSLD package fragment should exist'
    }

    @Test
    void testDelegate1() {
        String contents = '''\
            |class Foo {
            |  @Delegate List<Integer> list
            |}
            |new Foo().get(0)
            |'''.stripMargin()

        inferType(contents, 'get').with {
            assert result.extraDoc.replace('}', '') =~ 'Delegate AST transform'
            assert declaringTypeName == 'java.util.List<java.lang.Integer>'
            assert typeName == 'java.lang.Integer'
        }
    }

    @Test
    void testDelegate2() {
        String contents = '''\
            |class Bar {
            |  @Delegate URL url
            |}
            |new Bar().file
            |'''.stripMargin()

        inferType(contents, 'file').with {
            assert result.extraDoc.replace('}', '') =~ 'Delegate AST transform'
            assert declaringTypeName == 'java.net.URL'
            assert typeName == 'java.lang.String'
        }
    }

    @Test
    void testField1() {
        String contents = '''\
            |@groovy.transform.Field def foo
            |setFoo(null)
            |'''.stripMargin()

        inferType(contents, 'setFoo').with {
            assert result.extraDoc.replace('}', '') =~ 'Field AST transform'
            assert declaringTypeName =~ '^TestUnit_'
            assert typeName == 'java.lang.Void'
        }
    }

    @Test
    void testField2() {
        String contents = '''\
            |def foo
            |setFoo(null)
            |'''.stripMargin()

        inferType(contents, 'setFoo').with {
            assert result.confidence.name() == 'UNKNOWN'
        }
    }

    @Test
    void testField3() {
        String contents = '''\
            |class Bar { public foo }
            |new Bar().setFoo(null)
            |'''.stripMargin()

        inferType(contents, 'setFoo').with {
            assert result.confidence.name() == 'UNKNOWN'
        }
    }

    @Test
    void testMixin1() {
        addGroovySource '''\
            |class FlyingAbility {
            |  String fly() { "I'm the ${name} and I fly!" }
            |}
            |class DivingAbility {
            |  String dive() { "I'm the ${name} and I dive!" }
            |}
            |interface Vehicle {
            |  String getName()
            |}
            |'''.stripMargin(), 'Abilities'

        String contents = '''\
            |@Mixin(FlyingAbility)
            |class Plane implements Vehicle {
            |  String getName() { "Concorde" }
            |}
            |assert new Plane().fly() == "I'm the Concorde and I FLY!"
            |'''.stripMargin()

        inferType(contents, 'fly').with {
            assert result.extraDoc.replace('}', '') =~ 'Mixin AST transform'
            assert declaringTypeName == 'FlyingAbility'
            assert typeName == 'java.lang.String'
        }
    }

    @Test
    void testMixin2() {
        addGroovySource '''\
            |class FlyingAbility {
            |  String fly() { "I'm the ${name} and I fly!" }
            |}
            |class DivingAbility {
            |  String dive() { "I'm the ${name} and I dive!" }
            |}
            |interface Vehicle {
            |  String getName()
            |}
            |'''.stripMargin(), 'Abilities'

        String contents = '''\
            |@Mixin(DivingAbility)
            |class Submarine implements Vehicle {
            |  String getName() { "Yellow Submarine" }
            |}
            |assert new Submarine().dive() == "I'm the Yellow Submarine and I DIVE!"
            |'''.stripMargin()

        inferType(contents, 'dive').with {
            assert result.extraDoc.replace('}', '') =~ 'Mixin AST transform'
            assert declaringTypeName == 'DivingAbility'
            assert typeName == 'java.lang.String'
        }
    }

    @Test
    void testMixin3() {
        addGroovySource '''\
            |class FlyingAbility {
            |  String fly() { "I'm the ${name} and I fly!" }
            |}
            |class DivingAbility {
            |  String dive() { "I'm the ${name} and I dive!" }
            |}
            |interface Vehicle {
            |  String getName()
            |}
            |'''.stripMargin(), 'Abilities'

        String contents = '''\
            |@Mixin([DivingAbility, FlyingAbility])
            |class JamesBondVehicle implements Vehicle {
            |  String getName() { "James Bond's vehicle" }
            |}
            |assert new JamesBondVehicle().fly() == "I'm the James Bond's vehicle and I FLY!"
            |'''.stripMargin()

        inferType(contents, 'fly').with {
            assert result.extraDoc.replace('}', '') =~ 'Mixin AST transform'
            assert declaringTypeName == 'FlyingAbility'
            assert typeName == 'java.lang.String'
        }
    }

    @Test
    void testMixin4() {
        addGroovySource '''\
            |class FlyingAbility {
            |  String fly() { "I'm the ${name} and I fly!" }
            |}
            |class DivingAbility {
            |  String dive() { "I'm the ${name} and I dive!" }
            |}
            |interface Vehicle {
            |  String getName()
            |}
            |'''.stripMargin(), 'Abilities'

        String contents = '''\
            |@Mixin([DivingAbility, FlyingAbility])
            |class JamesBondVehicle2 implements Vehicle {
            |  String getName() { "James Bond's vehicle" }
            |}
            |assert new JamesBondVehicle2().dive() == "I'm the James Bond's vehicle and I DIVE!"
            |'''.stripMargin()

        inferType(contents, 'dive').with {
            assert result.extraDoc.replace('}', '') =~ 'Mixin AST transform'
            assert declaringTypeName == 'DivingAbility'
            assert typeName == 'java.lang.String'
        }
    }

    @Test
    void testSingleton1() {
        String contents = '''\
            |@Singleton
            |class A {}
            |A.instance
            |'''.stripMargin()

        inferType(contents, 'instance').with {
            assert result.extraDoc.replace('}', '') =~ 'Singleton AST transform'
            assert Flags.isFinal(result.declaration.modifiers)
            assert declaringTypeName == 'A'
            assert typeName == 'A'
        }
    }

    @Test
    void testSingleton2() {
        String contents = '''\
            |@Singleton
            |class B {}
            |B.getInstance()
            |'''.stripMargin()

        inferType(contents, 'getInstance').with {
            assert result.extraDoc.replace('}', '') =~ 'Singleton AST transform'
            assert declaringTypeName == 'B'
            assert typeName == 'B'
        }
    }

    @Test
    void testSingleton3() {
        String contents = '''\
            |@Singleton(property='thereCanBeOnlyOne')
            |class C {}
            |C.thereCanBeOnlyOne
            |'''.stripMargin()

        inferType(contents, 'thereCanBeOnlyOne').with {
            assert result.extraDoc.replace('}', '') =~ 'Singleton AST transform'
            assert declaringTypeName == 'C'
            assert typeName == 'C'
        }
    }

    @Test
    void testSingleton4() {
        String contents = '''\
            |@Singleton(property='thereCanBeOnlyOne')
            |class D {}
            |D.getThereCanBeOnlyOne()
            |'''.stripMargin()

        inferType(contents, 'getThereCanBeOnlyOne').with {
            assert result.extraDoc.replace('}', '') =~ 'Singleton AST transform'
            assert declaringTypeName == 'D'
            assert typeName == 'D'
        }
    }

    @Test
    void testSortable1() {
        String contents = '''\
            |import groovy.transform.*
            |@Sortable
            |class E {
            |  String value
            |}
            |new E().compareTo(null)
            |'''.stripMargin()

        inferType(contents, 'compareTo').with {
            assert result.extraDoc.replace('}', '') =~ 'Sortable AST transform'
            assert typeName == 'java.lang.Integer'
        }
    }

    @Test
    void testSortable2() {
        String contents = '''\
            |import groovy.transform.*
            |@Sortable
            |class F {
            |  String value
            |}
            |F.comparatorByValue()
            |'''.stripMargin()

        inferType(contents, 'comparatorByValue').with {
            assert result.extraDoc.replace('}', '') =~ 'Sortable AST transform'
            assert typeName == 'java.util.Comparator'
        }
    }

    @Test
    void testSortable3() {
        String contents = '''\
            |import groovy.transform.*
            |@Sortable(includes='value')
            |class G {
            |  String value
            |  String zebra
            |}
            |G.comparatorByValue()
            |'''.stripMargin()

        inferType(contents, 'comparatorByValue').with {
            assert result.extraDoc.replace('}', '') =~ 'Sortable AST transform'
            assert typeName == 'java.util.Comparator'
        }
    }

    @Test
    void testSortable4() {
        String contents = '''\
            |import groovy.transform.*
            |@Sortable(includes='value')
            |class G {
            |  String value
            |  String zebra
            |}
            |G.comparatorByZebra()
            |'''.stripMargin()

        inferType(contents, 'comparatorByZebra').with {
            assert result.confidence.name() == 'UNKNOWN'
        }
    }

    @Test
    void testSortable5() {
        String contents = '''\
            |import groovy.transform.*
            |@Sortable(excludes='zebra')
            |class H {
            |  String value
            |  String zebra
            |}
            |H.comparatorByZebra()
            |'''.stripMargin()

        inferType(contents, 'comparatorByZebra').with {
            assert result.confidence.name() == 'UNKNOWN'
        }
    }

    @Test
    void testSortable6() {
        String contents = '''\
            |import groovy.transform.*
            |@Sortable(excludes='zebra')
            |class I {
            |  String value
            |  String zebra
            |}
            |I.comparatorByValue()
            |'''.stripMargin()

        inferType(contents, 'comparatorByValue').with {
            assert result.extraDoc.replace('}', '') =~ 'Sortable AST transform'
            assert typeName == 'java.util.Comparator'
        }
    }
}
