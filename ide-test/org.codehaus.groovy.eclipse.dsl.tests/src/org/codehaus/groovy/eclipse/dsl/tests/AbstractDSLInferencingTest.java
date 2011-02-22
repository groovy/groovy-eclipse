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
package org.codehaus.groovy.eclipse.dsl.tests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.IGroovyLogger;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLDStoreManager;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.groovy.tests.search.AbstractInferencingTest;

/**
 * 
 * @author Andrew Eisenberg
 * @created Feb 19, 2011
 */
public class AbstractDSLInferencingTest extends AbstractInferencingTest {

    public AbstractDSLInferencingTest(String name) {
        super(name);
    }
    
    class TestLogger implements IGroovyLogger {

        public void log(TraceCategory category, String message) {
            System.out.println(category.getPaddedLabel() + ": " + message);
        }

        public boolean isCategoryEnabled(TraceCategory category) {
            return true;
        }
        
    }
    
    TestLogger logger = new TestLogger();
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyLogManager.manager.addLogger(logger);
    }
    
    @Override
    protected void tearDown() throws Exception {
        GroovyLogManager.manager.removeLogger(logger);
        super.tearDown();
    }

    protected String[] createDsls(String ... dsls) {
        return createDsls(0, dsls);
    }
    protected String[] createDsls(int startWith, String ... dsls) {
        int i = startWith;
        for (String dsl : dsls) {
            env.addFile(project.getFullPath(), "dsl" + i++ + ".dsld", dsl);
        }
        return dsls;
    }
    
    protected void deleteDslFile(int fileNum) {
        env.removeFile(project.getFile("dsl" + fileNum + ".dsld").getFullPath());
    }

    /**
     * @param expectedNumDslFiles  number of dsl files currently registered
     * @param allExpectedPointcuts map: dsl file name -> all pointcuts in that file 
     * @param expectedContributionCounts map: pointcut name -> all contribution group associated with
     */
    protected void assertDSLStore(int expectedNumDslFiles, Map<String, List<String>> allExpectedPointcuts, Map<String, Integer> expectedContributionCounts) {
        DSLDStoreManager manager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();
        DSLDStore store = manager.getDSLDStore(project);
        String[] keys = store.getAllContextKeys();
        Arrays.sort(keys);
        assertEquals(expectedNumDslFiles, keys.length);
        int i = 0;
        for (String key : keys) {
            assertEquals(project.getFullPath() + "/dsl" + i++ + ".dsld", key);
            // now check the pointcuts in this script
            Set<IPointcut> pcs = ((Map<String, Set<IPointcut>>) ReflectionUtils.getPrivateField(DSLDStore.class, "keyContextMap", store)).get(key);
            List<String> expectedPcs = allExpectedPointcuts.get(key);
            for (IPointcut pc : pcs) {
                assertTrue("Didn't find expected Pointcut " + pc + " in\n" + expectedPcs, expectedPcs.contains(createSemiUniqueName(pc)));
                
                // now check the contributions for each pointcut
                List<IContributionGroup> group = ((Map<IPointcut, List<IContributionGroup>>) ReflectionUtils.getPrivateField(DSLDStore.class, "contextContributionMap", store)).get(pc);
                int groupSize = group.size();
                int expectedSize = expectedContributionCounts.get(createSemiUniqueName(pc));
                assertEquals("Didn't find expected number of contributions for " + pc, expectedSize, groupSize);
            }
            assertEquals("Wrong number of pointcuts in store:\nExpected: " + expectedPcs + "\nActual: " + pcs, expectedPcs.size(), pcs.size());
        }
    }
    protected Map<String, Integer> createExpectedContributionCount(String[] pcs, Integer[] counts) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < counts.length; i++) {
            map.put(pcs[i], counts[i]);
        }
        return map;
    }

    protected Map<String, List<String>> createExpectedPointcuts(String[]... pointcuts) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        int i = 0;
        for (String[] strings : pointcuts) {
            String name = project.getFile("dsl" + i++ + ".dsld").getFullPath().toPortableString();
            map.put(name, Arrays.asList(strings));
        }
        return map;
    }

    protected String createSemiUniqueName(IPointcut pc) {
        return pc.getClass().getName() + ":" + new Path(pc.getContainerIdentifier()).lastSegment();
    }
    
    protected String createSemiUniqueName(Class<? extends IPointcut> pc, int cnt) {
        return pc.getName() + ":" + "dsl" + cnt + ".dsld";
    }
}