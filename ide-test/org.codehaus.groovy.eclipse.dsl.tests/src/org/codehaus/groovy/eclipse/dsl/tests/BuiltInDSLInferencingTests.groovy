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
package org.codehaus.groovy.eclipse.dsl.tests

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.eclipse.core.resources.IResource
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.JavaCore
import org.junit.Test

/**
 * Tests type inferencing for DSL scripts included with Groovy plugin.
 */
final class BuiltInDSLInferencingTests extends DSLInferencingTestSuite {

    BuiltInDSLInferencingTests()
    {
        doRemoveClasspathContainer = false
    }

    @Test
    void testSanity() {
        assert GroovyRuntime.hasClasspathContainer(javaProject, GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID) : 'Should have DSL support classpath container'

        IClasspathContainer container = JavaCore.getClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, javaProject)
        assert container.classpathEntries.length == 2 : "Wrong number of classpath entries found: ${ -> Arrays.toString(container.classpathEntries)}"

        IClasspathEntry pluginEntry = javaProject.getResolvedClasspath(true).find { it.path.toString().contains('plugin_dsld') }
        IPackageFragmentRoot root = javaProject.children.find { it.elementName.contains('plugin_dsld') }
        List<String> elements = javaProject.children*.elementName

        List<String> possibleFrags = javaProject.packageFragments.findAll { it.elementName.equals('dsld') }.collectMany { IPackageFragment frag ->
            def items = [frag.toString()]
            if (frag.children) {
                items << '  ['
                for (IJavaElement child : frag.getChildren()) {
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
    void testSingleton() {
        String contents = '@Singleton class Foo { }\nFoo.instance\nFoo.getInstance()'

        int start = contents.lastIndexOf('instance')
        int end = start + 'instance'.length()
        assertType(contents, start, end, 'Foo', 'Singleton')

        start = contents.lastIndexOf('getInstance')
        end = start + 'getInstance'.length()
        assertType(contents, start, end, 'Foo', 'Singleton')
    }

    @Test
    void testDelegate1() {
        String contents = 'class Foo { @Delegate List<Integer> myList }\nnew Foo().get(0)'

        int start = contents.lastIndexOf('get')
        int end = start + 'get'.length()
        assertType(contents, start, end, 'java.lang.Integer', 'Delegate')
        assertDeclaringType(contents, start, end, 'java.util.List<java.lang.Integer>')
    }

    @Test
    void testDelegate2() {
        String contents = 'class Foo { @Delegate List<Integer> myList\n @Delegate URL myUrl }\nnew Foo().get(0)\nnew Foo().getFile()'

        int start = contents.indexOf('get')
        int end = start + 'get'.length()
        assertType(contents, start, end, 'java.lang.Integer', 'Delegate')
        assertDeclaringType(contents, start, end, 'java.util.List<java.lang.Integer>')

        start = contents.lastIndexOf('getFile')
        end = start + 'getFile'.length()
        assertType(contents, start, end, 'java.lang.String', 'Delegate')
        assertDeclaringType(contents, start, end, 'java.net.URL')
    }

    @Test
    void testMixin() {
        String contents = '''\
            class FlyingAbility {
                String fly() { "I\'m the ${name} and I fly!" }
            }
            class DivingAbility {
                String dive() { "I\'m the ${name} and I dive!" }
            }
            interface Vehicle {
                String getName()
            }
            @Mixin(DivingAbility)
            class Submarine implements Vehicle {
                String getName() { "Yellow Submarine" }
            }
            @Mixin(FlyingAbility)
            class Plane implements Vehicle {
                String getName() { "Concorde" }
            }
            @Mixin([DivingAbility, FlyingAbility])
            class JamesBondVehicle implements Vehicle {
                String getName() { "James Bond\'s vehicle" }
            }

            assert new Plane().fly() == "I\'m the Concorde and I FLY!"
            assert new Submarine().dive() == "I\'m the Yellow Submarine and I DIVE!"
            assert new JamesBondVehicle().fly() == "I\'m the James Bond\'s vehicle and I FLY!"
            assert new JamesBondVehicle().dive() == "I\'m the James Bond\'s vehicle and I DIVE!"
            '''.stripIndent()

        int start = contents.lastIndexOf('dive')
        int end = start + 'dive'.length()
        assertType(contents, start, end, 'java.lang.String', 'Mixin')
        assertDeclaringType(contents, start, end, 'DivingAbility')

        start = contents.lastIndexOf('fly', start)
        end = start + 'fly'.length()
        assertType(contents, start, end, 'java.lang.String', 'Mixin')
        assertDeclaringType(contents, start, end, 'FlyingAbility')

        start = contents.lastIndexOf('dive', start)
        end = start + 'dive'.length()
        assertType(contents, start, end, 'java.lang.String', 'Mixin')
        assertDeclaringType(contents, start, end, 'DivingAbility')

        start = contents.lastIndexOf('fly', start)
        end = start + 'fly'.length()
        assertType(contents, start, end, 'java.lang.String', 'Mixin')
        assertDeclaringType(contents, start, end, 'FlyingAbility')
    }

    @Test
    void testSwingBuilder1() {
        String contents = 'new groovy.swing.SwingBuilder().edt { frame }'

        int start = contents.lastIndexOf('frame')
        int end = start + 'frame'.length()
        assertType(contents, start, end, 'javax.swing.JFrame', 'SwingBuilder')
        assertDeclaringType(contents, start, end, 'groovy.swing.SwingBuilder')
    }

    @Test
    void testSwingBuilder2() {
        String contents = 'groovy.swing.SwingBuilder.edtBuilder { frame }'

        int start = contents.lastIndexOf('frame')
        int end = start + 'frame'.length()
        assertType(contents, start, end, 'javax.swing.JFrame', 'SwingBuilder')
        assertDeclaringType(contents, start, end, 'groovy.swing.SwingBuilder')
    }
}
