/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.junit.Assert.*
import static org.junit.Assume.assumeFalse

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.eclipse.core.resources.IResource
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

    List findRange(String source, String target, int length = target.length()) {
        int offset = source.lastIndexOf(target)
        [source, offset, offset + length]
    }

    @Test
    void testBasics() {
        assert GroovyRuntime.hasClasspathContainer(javaProject, GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID) : 'Should have DSL support classpath container'

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
        String contents = 'class Foo { @Delegate List<Integer> myList }\nnew Foo().get(0)'

        assertDeclaringType(*findRange(contents, 'get'), 'java.util.List<java.lang.Integer>')
        assertType(*findRange(contents, 'get'), 'java.lang.Integer', 'Delegate AST transform')
    }

    @Test
    void testDelegate2() {
        String contents = 'class Foo { @Delegate List<Integer> myList\n @Delegate URL myUrl }\nnew Foo().get(0)\nnew Foo().getFile()'

        assertDeclaringType(*findRange(contents, 'get(', 3), 'java.util.List<java.lang.Integer>')
        assertType(*findRange(contents, 'get(', 3), 'java.lang.Integer', 'Delegate AST transform')

        assertDeclaringType(*findRange(contents, 'getFile'), 'java.net.URL')
        assertType(*findRange(contents, 'getFile'), 'java.lang.String', 'Delegate AST transform')
    }

    @Test
    void testMixin() {
        addGroovySource '''\
            class FlyingAbility {
              String fly() { "I'm the ${name} and I fly!" }
            }
            class DivingAbility {
              String dive() { "I'm the ${name} and I dive!" }
            }
            interface Vehicle {
              String getName()
            }
            '''.stripIndent(), 'Abilities'

        String single = '''\
            @Mixin(FlyingAbility)
            class Plane implements Vehicle {
              String getName() { "Concorde" }
            }
            @Mixin(DivingAbility)
            class Submarine implements Vehicle {
              String getName() { "Yellow Submarine" }
            }
            assert new Plane().fly() == "I'm the Concorde and I FLY!"
            assert new Submarine().dive() == "I'm the Yellow Submarine and I DIVE!"
            '''.stripIndent()

        assertDeclaringType(*findRange(single, 'fly'), 'FlyingAbility')
        assertType(*findRange(single, 'fly'), 'java.lang.String', 'Mixin AST transform')

        assertDeclaringType(*findRange(single, 'dive'), 'DivingAbility')
        assertType(*findRange(single, 'dive'), 'java.lang.String', 'Mixin AST transform')


        String multiple = '''\
            @Mixin([DivingAbility, FlyingAbility])
            class JamesBondVehicle implements Vehicle {
              String getName() { "James Bond's vehicle" }
            }
            assert new JamesBondVehicle().fly() == "I'm the James Bond's vehicle and I FLY!"
            assert new JamesBondVehicle().dive() == "I'm the James Bond's vehicle and I DIVE!"
            '''.stripIndent()

        assertDeclaringType(*findRange(multiple, 'fly'), 'FlyingAbility')
        assertType(*findRange(multiple, 'fly'), 'java.lang.String', 'Mixin AST transform')

        assertDeclaringType(*findRange(multiple, 'dive'), 'DivingAbility')
        assertType(*findRange(multiple, 'dive'), 'java.lang.String', 'Mixin AST transform')
    }

    @Test
    void testSingleton1() {
        String contents = '''\
            @Singleton
            class Foo {}
            Foo.instance
            Foo.getInstance()
            '''.stripIndent()

        assertType(*findRange(contents, 'instance'), 'Foo', 'Singleton AST transform')
        assertType(*findRange(contents, 'getInstance'), 'Foo', 'Singleton AST transform')
    }

    @Test
    void testSingleton2() {
        String contents = '''\
            @Singleton(property='thereCanBeOnlyOne')
            class Foo {}
            Foo.thereCanBeOnlyOne
            Foo.getThereCanBeOnlyOne()
            '''.stripIndent()

        assertType(*findRange(contents, 'thereCanBeOnlyOne'), 'Foo', 'Singleton AST transform')
        assertType(*findRange(contents, 'getThereCanBeOnlyOne'), 'Foo', 'Singleton AST transform')
    }

    @Test
    void testSortable1() {
        String contents = '''\
            import groovy.transform.*
            @Sortable
            class Foo {
              String value
            }
            new Foo().compareTo(null)
            '''.stripIndent()

        assertType(*findRange(contents, 'compareTo'), 'java.lang.Integer', 'Sortable AST transform')
    }

    @Test
    void testSortable2() {
        String contents = '''\
            import groovy.transform.*
            @Sortable
            class Foo {
              String value
            }
            Foo.comparatorByValue()
            '''.stripIndent()

        assertType(*findRange(contents, 'comparatorByValue'), 'java.util.Comparator', 'Sortable AST transform')
    }

    @Test
    void testSortable3() {
        String contents = '''\
            import groovy.transform.*
            @Sortable(includes='value')
            class Foo {
              String value
              String zebra
            }
            Foo.comparatorByValue()
            Foo.comparatorByZebra()
            '''.stripIndent()

        assertUnknownConfidence(*findRange(contents, 'comparatorByZebra'), 'Foo')
        assertType(*findRange(contents, 'comparatorByValue'), 'java.util.Comparator', 'Sortable AST transform')
    }

    @Test
    void testSortable4() {
        String contents = '''\
            import groovy.transform.*
            @Sortable(excludes='zebra')
            class Foo {
              String value
              String zebra
            }
            Foo.comparatorByValue()
            Foo.comparatorByZebra()
            '''.stripIndent()

        assertUnknownConfidence(*findRange(contents, 'comparatorByZebra'), 'Foo')
        assertType(*findRange(contents, 'comparatorByValue'), 'java.util.Comparator', 'Sortable AST transform')
    }

    @Test
    void testSwingBuilder1() {
        assumeFalse(isAtLeastGroovy(25)) // groovy-swing not included by default since 2.5

        String contents = 'new groovy.swing.SwingBuilder().edt { frame }'

        assertDeclaringType(*findRange(contents, 'frame'), 'groovy.swing.SwingBuilder')
        assertType(*findRange(contents, 'frame'), 'javax.swing.JFrame', 'SwingBuilder')
    }

    @Test
    void testSwingBuilder2() {
        assumeFalse(isAtLeastGroovy(25)) // groovy-swing not included by default since 2.5

        String contents = 'groovy.swing.SwingBuilder.edtBuilder { frame }'

        assertDeclaringType(*findRange(contents, 'frame'), 'groovy.swing.SwingBuilder')
        assertType(*findRange(contents, 'frame'), 'javax.swing.JFrame', 'SwingBuilder')
    }
}
