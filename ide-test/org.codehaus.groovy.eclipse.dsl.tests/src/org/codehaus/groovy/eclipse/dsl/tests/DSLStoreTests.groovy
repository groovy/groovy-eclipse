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
import org.codehaus.groovy.eclipse.dsl.DSLDStore
import org.codehaus.groovy.eclipse.dsl.DSLDStoreManager
import org.codehaus.groovy.eclipse.dsl.DSLPreferences
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypePointcut
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindFieldPointcut
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IStorage
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor
import org.eclipse.jdt.internal.core.JavaModelManager
import org.junit.Test

final class DSLStoreTests extends DSLInferencingTestSuite {

    /**
     * @param expectedNumDslFiles  number of dsl files currently registered
     * @param allExpectedPointcuts map: dsl file name -> all pointcuts in that file
     * @param expectedContributionCounts map: pointcut name -> all contribution group associated with
     */
    private void assertDSLStore(int expectedNumDslFiles, Map<String, List<String>> allExpectedPointcuts, Map<String, Integer> expectedContributionCounts) {
        // ensure DSLDs are refreshed
        // don't schedule. instead run in the same thread.
        println 'About to run RefreshDSLDJob'

        SimpleProgressMonitor spm = new SimpleProgressMonitor('clean build')
        project.build(IncrementalProjectBuilder.CLEAN_BUILD, spm)
        spm.waitForCompletion()

        JavaModelManager.indexManager.removeIndex(project.location)
        JavaModelManager.indexManager.cleanUpIndexes()
        SynchronizationUtils.waitForIndexingToComplete(javaProject)

        DSLDStoreManager manager = GroovyDSLCoreActivator.default.contextStoreManager
        manager.initialize(project, true)
        println 'Finished RefreshDSLDJob'

        DSLDStore store = manager.getDSLDStore(project)
        Set<String> disabledScripts = DSLPreferences.disabledScriptsAsSet

        IStorage[] keys = store.allContextKeys.sort { IStorage o1, IStorage o2 ->
            o1.fullPath.toPortableString() <=> o2.fullPath.toPortableString()
        }
        assert keys.length == expectedNumDslFiles
        keys.eachWithIndex { IStorage key, int i ->
            String uniqueString = DSLDStore.toUniqueString(key)
            // don't check the name for external and binary dslds
            if (key instanceof IFile) {
                assert uniqueString == javaProject.project.fullPath.toString() + '/dsl' + i + '.dsld'
            }
            // check to see if the file is disabled.
            if (disabledScripts.contains(uniqueString)) {
                return
            }
            // now check the pointcuts in this script
            Map<IStorage, Set<IPointcut>> keyContextMap = store.@keyContextMap
            Set<IPointcut> pcs = keyContextMap.get(key)
            List<String> expectedPcs = allExpectedPointcuts.get(uniqueString)
            for (pc in pcs) {
                assert expectedPcs.contains(createSemiUniqueName(pc)) : "Didn't find expected Pointcut $pc in\n$expectedPcs"

                // now check the contributions for each pointcut
                Map<IPointcut, List<IContributionGroup>> pointcutContributionMap = store.@pointcutContributionMap
                List<IContributionGroup> group = pointcutContributionMap.get(pc)
                int groupSize = group.size()
                int expectedSize = expectedContributionCounts.get(createSemiUniqueName(pc))
                assert groupSize == expectedSize : "Didn't find expected number of contributions for $pc"
            }
            assert pcs.size() == expectedPcs.size() : "Wrong number of pointcuts in store:\nExpected: $expectedPcs\nActual: $pcs"
        }
    }

    private Map<String, Integer> createExpectedContributionCount(List<String> pcs, List<Integer> counts) {
        (0..<pcs.size()).collectEntries { i ->
            [pcs[i], counts[i]]
        }
    }

    @SafeVarargs
    private final Map<String, List<String>> createExpectedPointcuts(List<String>... pointcuts) {
        int i = 0
        pointcuts.collectEntries { List<String> values ->
            String key = DSLDStore.toUniqueString(project.getFile("dsl${i++}.dsld"))
            [key, values]
        }
    }

    private String createSemiUniqueName(IPointcut pc) {
        return pc.class.name + ':' + pc.containerIdentifier.fullPath.lastSegment()
    }

    private String createSemiUniqueName(Class<? extends IPointcut> pc, int cnt) {
        return pc.name + ':' + 'dsl' + cnt + '.dsld'
    }

    private String createSemiUniqueName(Class<? extends IPointcut> pc, IStorage storage) {
        return pc.name + ':' + DSLDStore.toUniqueString(storage)
    }

    private void setDisabledScripts(String... scripts) {
        println "Setting disabled scripts to: $scripts"
        DSLPreferences.setDisabledScripts(scripts)
    }

    //--------------------------------------------------------------------------

    @Test
    void testNothing() {
        assertDSLStore(0, [:], [:])
    }

    @Test
    void testSingleSimple() {
        createDsls('currentType().accept { }')

        assertDSLStore(1,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                1
            ])
        )
    }

    @Test // the same pointcut is used twice
    void testSingleTwice() {
        createDsls('def g = currentType()\n' +
                'g.accept { }\n' +
                'g.accept { }')

        assertDSLStore(1,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                2
            ])
        )
    }

    @Test // two pointcuts in same file
    void testTwoPointcuts() {
        createDsls('currentType().accept { }\nfields().accept { }')

        assertDSLStore(1,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0)
            ], [
                1, 1
            ])
        )
    }

    @Test // two pointcuts two files
    void testTwoPointcutsTwoFiles() {
        createDsls('currentType().accept { }', 'fields().accept { }')

        assertDSLStore(2,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                createSemiUniqueName(FindFieldPointcut, 1)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 1)
            ], [
                1, 1
            ])
        )
    }

    @Test
    void testTwoFilesEachWith2Pointcuts() {
        createDsls('currentType().accept { }\nfields().accept { }', 'currentType().accept { }\nfields().accept { }')

        assertDSLStore(2,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0)
            ], [
                createSemiUniqueName(CurrentTypePointcut, 1),
                createSemiUniqueName(FindFieldPointcut, 1)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0),
                createSemiUniqueName(CurrentTypePointcut, 1),
                createSemiUniqueName(FindFieldPointcut, 1)
            ], [
                1, 1, 1, 1
            ])
        )
    }

    @Test
    void testTwoFilesEachWith2PointcutsEachUsedTwice() {
        createDsls('def a = currentType()\ndef b = fields()\na.accept { }\na.accept { }\nb.accept { }\nb.accept { }',
                   'def a = currentType()\ndef b = fields()\na.accept { }\na.accept { }\nb.accept { }\nb.accept { }')

        assertDSLStore(2,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0)
            ], [
                createSemiUniqueName(CurrentTypePointcut, 1),
                createSemiUniqueName(FindFieldPointcut, 1)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0),
                createSemiUniqueName(CurrentTypePointcut, 1),
                createSemiUniqueName(FindFieldPointcut, 1)
            ], [
                2, 2, 2, 2
            ])
        )
    }

    @Test
    void testCraziness() {
        createDsls('def a = currentType()\ndef b = fields()\na.accept { }\na.accept { }\nb.accept { }\nb.accept { }',
                   'def a = currentType()\ndef b = fields()\na.accept { }\na.accept { }\nb.accept { }\nb.accept { }',
                   'def a = currentType()\na.accept { }\na.accept { }\na.accept { }\na.accept { }\na.accept { }',
                   '') // not in store

        assertDSLStore(3,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0)
            ], [
                createSemiUniqueName(CurrentTypePointcut, 1),
                createSemiUniqueName(FindFieldPointcut, 1)
            ], [
                createSemiUniqueName(CurrentTypePointcut, 2)
            ], [
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0),
                createSemiUniqueName(CurrentTypePointcut, 1),
                createSemiUniqueName(FindFieldPointcut, 1),
                createSemiUniqueName(CurrentTypePointcut, 2)
            ], [
                2, 2, 2, 2, 5
            ])
        )
    }

    @Test
    void testAddAndRemove() {
        assertDSLStore(0, [:], [:])

        // add one
        createDsls('currentType().accept { }')
        assertDSLStore(1,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                1
            ])
        )

        // add another
        createDsls('currentType().accept { }')

        assertDSLStore(2,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                createSemiUniqueName(CurrentTypePointcut, 1)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(CurrentTypePointcut, 1)
            ], [
                1, 1
            ])
        )

        // remove second
        deleteDslFile(1)
        assertDSLStore(1,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                1
            ])
        )

        // remove first
        deleteDslFile(0)
        assertDSLStore(0, [:], [:])
    }

    @Test
    void testChange() {
        createDsls('currentType().accept { }')
        assertDSLStore(1,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                1
            ])
        )

        // overwrite the original
        deleteDslFile(0); index = 0
        createDsls('currentType().accept { }\nfields().accept { }')
        assertDSLStore(1,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 0)
            ], [
                1, 1
            ])
        )
    }

    @Test
    void testDisabledOfFile() {
        createDsls('currentType().accept { }', 'fields().accept { }')
        assertDSLStore(2,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                createSemiUniqueName(FindFieldPointcut, 1)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 1)
            ], [
                1, 1
            ])
        )

        // disable script
        setDisabledScripts(DSLDStore.toUniqueString(project.getFile('dsl0.dsld')))

        assertDSLStore(2,
            createExpectedPointcuts([], [
                createSemiUniqueName(FindFieldPointcut, 1)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(FindFieldPointcut, 1)
            ], [
                1
            ])
        )

        // re-enable script
        setDisabledScripts()

        deleteDslFile(0); deleteDslFile(1); index = 0
        createDsls('currentType().accept { }', 'fields().accept { }')

        assertDSLStore(2,
            createExpectedPointcuts([
                createSemiUniqueName(CurrentTypePointcut, 0)
            ], [
                createSemiUniqueName(FindFieldPointcut, 1)
            ]),
            createExpectedContributionCount([
                createSemiUniqueName(CurrentTypePointcut, 0),
                createSemiUniqueName(FindFieldPointcut, 1)
            ], [
                1, 1
            ])
        )
    }

    @Test
    void testDisabledOfJar() {
        def jarPath = FileLocator.resolve(getTestResourceURL('simple_dsld.jar')).file
        def cpEntry = JavaCore.newLibraryEntry(new Path(jarPath), null, null)
        GroovyRuntime.appendClasspathEntry(javaProject, cpEntry)
        project.refreshLocal(IResource.DEPTH_INFINITE, null)
        buildProject()

        IStorage storage = javaProject.getPackageFragmentRoot(jarPath).getPackageFragment('dsld').nonJavaResources[0]
        String dsld = DSLDStore.toUniqueString(storage), pcut = createSemiUniqueName(CurrentTypePointcut, storage)

        assertDSLStore(1, [(dsld): [pcut]], [(pcut): 1])

        // disable script
        setDisabledScripts(dsld)
        assertDSLStore(1, [:], [:])

        // re-enable
        setDisabledScripts()
        assertDSLStore(1, [(dsld): [pcut]], [(pcut): 1])

        // remove from classpath
        GroovyRuntime.removeClasspathEntry(javaProject, cpEntry)
        project.refreshLocal(IResource.DEPTH_INFINITE, null)
        buildProject()

        assertDSLStore(0, [:], [:])
    }
}
