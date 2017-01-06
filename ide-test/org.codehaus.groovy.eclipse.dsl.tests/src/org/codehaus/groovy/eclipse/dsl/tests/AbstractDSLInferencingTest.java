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
package org.codehaus.groovy.eclipse.dsl.tests;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.IGroovyLogger;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.DSLDStoreManager;
import org.codehaus.groovy.eclipse.dsl.DSLPreferences;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.groovy.tests.search.AbstractInferencingTest;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;

/**
 * @author Andrew Eisenberg
 * @created Feb 19, 2011
 */
public class AbstractDSLInferencingTest extends AbstractInferencingTest {

    public AbstractDSLInferencingTest(String name) {
        super(name);
    }

    protected IGroovyLogger logger = new IGroovyLogger() {
        public boolean isCategoryEnabled(TraceCategory category) {
            return true;
        }
        public void log(TraceCategory category, String message) {
            System.out.println(category.getPaddedLabel() + ": " + message);
        }
    };

    protected boolean doRemoveClasspathContainer = true;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyLogManager.manager.addLogger(logger);
        if (doRemoveClasspathContainer) {
            GroovyRuntime.removeClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, JavaCore.create(project));
        } else {
            refreshExternalFoldersProject();
            GroovyRuntime.addLibraryToClasspath(JavaCore.create(project), GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, false);
            GroovyDSLCoreActivator.getDefault().getContextStoreManager().initialize(project, true);
        }
        GroovyDSLCoreActivator.getDefault().getContainerListener().ignoreProject(project);
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
        return createDsls(startWith, project, dsls);
    }

    protected String[] createDsls(int startWith, IProject theProject, String ... dsls) {
        int i = startWith;
        System.out.println("Now creating " + dsls.length + " DSLD files.");
        for (String dsl : dsls) {
            System.out.println("Creating:\n" + dsl + "\n");
            IPath path = env.addFile(theProject.getFullPath(), "dsl" + i++ + ".dsld", dsl);
            IFile file = env.getWorkspace().getRoot().getFile(path);
            if (!file.exists()) {
                fail("File " + file + " just created, but doesn't exist");
            }
        }
        return dsls;
    }

    protected void deleteDslFile(int fileNum) {
        env.removeFile(project.getFile("dsl" + fileNum + ".dsld").getFullPath());
    }

    protected String[] createDSLsFromFiles(String ... fileNames) throws IOException {
        String[] dslContents = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            dslContents[i] = GroovyDSLDTestsActivator.getDefault().getTestResourceContents(fileNames[i]);
        }
        return createDsls(dslContents);
    }

    protected void addJarToProject(String jarName) throws CoreException, IOException {
        String externalFilePath = findExternalFilePath(jarName);
        env.addExternalJar(project.getFullPath(), externalFilePath);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        env.cleanBuild();
    }

    protected void addGroovyJarToProject(String jarName) throws CoreException, IOException {
        addGroovyJarToProject(jarName, project);
    }

    protected static void addGroovyJarToProject(String jarName, IProject project) throws CoreException, IOException {
        URL url = CompilerUtils.getJarInGroovyLib(jarName);
        if (url != null) {
            env.addExternalJar(project.getFullPath(), url.getFile());
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
            env.cleanBuild();
        } else {
            fail("Could not find file " + jarName + " in org.codehaus.groovy bundle");
        }
    }

    protected String findExternalFilePath(String jarName) throws MalformedURLException, IOException {
        URL url = GroovyDSLDTestsActivator.getDefault().getTestResourceURL(jarName);
        URL resolved = FileLocator.resolve(url);
        String externalFilePath = resolved.getFile();
        return externalFilePath;
    }

    protected void removeJarFromProject(String jarName) throws CoreException, IOException {
        URL url = GroovyDSLDTestsActivator.getDefault().getTestResourceURL(jarName);
        URL resolved = FileLocator.resolve(url);
        env.removeExternalJar(project.getFullPath(), new Path(resolved.getFile()));
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        env.cleanBuild();
    }

    /**
     * @param expectedNumDslFiles  number of dsl files currently registered
     * @param allExpectedPointcuts map: dsl file name -> all pointcuts in that file
     * @param expectedContributionCounts map: pointcut name -> all contribution group associated with
     */
    protected void assertDSLStore(int expectedNumDslFiles, Map<String, List<String>> allExpectedPointcuts, Map<String, Integer> expectedContributionCounts) {
        // ensure DSLDs are refreshed
        // don't schedule. instead run in the same thread.
        System.out.println("About to run RefreshDSLDJob");
        // ensure this classpath container is gone
        GroovyRuntime.removeClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, JavaCore.create(project));
        env.fullBuild();
        DSLDStoreManager manager = GroovyDSLCoreActivator.getDefault().getContextStoreManager();
        manager.initialize(project, true);
        System.out.println("Finished RefreshDSLDJob");

        DSLDStore store = manager.getDSLDStore(project);
        Set<String> disabledScripts = DSLPreferences.getDisabledScriptsAsSet();

        IStorage[] keys = store.getAllContextKeys();
        Arrays.sort(keys, new Comparator<IStorage>() {
            public int compare(IStorage o1, IStorage o2) {
                return o1.getFullPath().toPortableString().compareTo(o2.getFullPath().toPortableString());
            }
        });
        assertEquals(expectedNumDslFiles, keys.length);
        int i = 0;
        for (IStorage key : keys) {
            String uniqueString = DSLDStore.toUniqueString(key);
            // don't check the name for external and binary dslds
            if (key instanceof IFile) {
                assertEquals(project.getFullPath() + "/dsl" + i++ + ".dsld", uniqueString);
            }
            // check to see if the file is disabled.
            if (disabledScripts.contains(uniqueString)) {
                continue;
            }

            // now check the pointcuts in this script
            @SuppressWarnings("unchecked")
            Map<IStorage, Set<IPointcut>> keyContextMap = (Map<IStorage, Set<IPointcut>>)
                    ReflectionUtils.getPrivateField(DSLDStore.class, "keyContextMap", store);
            Set<IPointcut> pcs = keyContextMap.get(key);
            List<String> expectedPcs = allExpectedPointcuts.get(uniqueString);
            for (IPointcut pc : pcs) {
                assertTrue("Didn't find expected Pointcut " + pc + " in\n" + expectedPcs, expectedPcs.contains(createSemiUniqueName(pc)));

                // now check the contributions for each pointcut
                @SuppressWarnings("unchecked")
                Map<IPointcut, List<IContributionGroup>> pointcutContributionMap = (Map<IPointcut, List<IContributionGroup>>)
                        ReflectionUtils.getPrivateField(DSLDStore.class, "pointcutContributionMap", store);
                List<IContributionGroup> group = pointcutContributionMap.get(pc);
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
            String name = DSLDStore.toUniqueString(project.getFile("dsl" + i++ + ".dsld"));
            map.put(name, Arrays.asList(strings));
        }
        return map;
    }

    protected Map<String, List<String>> createExpectedPointcuts(IStorage[] storages, String[]... pointcuts) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        int i = 0;
        for (String[] strings : pointcuts) {
            String name = DSLDStore.toUniqueString(storages[i++]);
            map.put(name, Arrays.asList(strings));
        }
        return map;
    }

    protected String createSemiUniqueName(IPointcut pc) {
        return pc.getClass().getName() + ":" + pc.getContainerIdentifier().getFullPath().lastSegment();
    }

    protected String createSemiUniqueName(Class<? extends IPointcut> pc, int cnt) {
        return pc.getName() + ":" + "dsl" + cnt + ".dsld";
    }

    protected String createSemiUniqueName(Class<? extends IPointcut> pc, IStorage storage) {
        return pc.getName() + ":" + DSLDStore.toUniqueString(storage);
    }

    protected void assertDSLType(String contents, String name) {
        assertDeclaringType(contents, contents.indexOf(name), contents.indexOf(name) + name.length(), "p.IPointcut", true);
    }

    protected void assertUnknownDSLType(String contents, String name) {
        assertUnknownConfidence(contents, contents.indexOf(name), contents.indexOf(name) + name.length(), "Search", true);
    }

    /**
     * Refreshes the external folders project.  It seems that the contents of
     * linked folders are not being refreshed fast enough and we are getting
     * {@link IllegalArgumentException}s because there is a resource that is
     * aliased, but no longer exists.
     */
    protected static void refreshExternalFoldersProject() throws CoreException {
        Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace();
        IProject externalProject = workspace.getRoot().getProject(".org.eclipse.jdt.core.external.folders");
        if (externalProject.exists()) {
            for (IResource member : externalProject.members()) {
                if (member instanceof Folder) {
                    Folder folder = (Folder) member;
                    workspace.getAliasManager().updateAliases(folder, folder.getStore(), IResource.DEPTH_INFINITE, null);
                    if (folder.exists()) {
                        folder.refreshLocal(IResource.DEPTH_INFINITE, null);
                    }
                }
            }
        }
    }
}
