/*
 * Copyright 2009-2019 the original author or authors.
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

import static org.eclipse.jdt.core.groovy.tests.search.InferencingTestSuite.doVisit
import static org.eclipse.jdt.core.groovy.tests.search.InferencingTestSuite.printTypeName
import static org.junit.Assert.fail
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.GroovyLogManager
import org.codehaus.groovy.eclipse.IGroovyLogger
import org.codehaus.groovy.eclipse.TraceCategory
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.internal.resources.Folder
import org.eclipse.core.internal.resources.Workspace
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.groovy.tests.search.InferencingTestSuite
import org.eclipse.jdt.core.tests.util.Util
import org.eclipse.jdt.groovy.core.util.GroovyUtils
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence
import org.junit.After
import org.junit.Before
import org.osgi.framework.FrameworkUtil

abstract class DSLInferencingTestSuite extends GroovyEclipseTestSuite {

    private final IGroovyLogger logger = new IGroovyLogger() {
        @Override
        boolean isCategoryEnabled(TraceCategory category) {
            true
        }
        @Override
        void log(TraceCategory category, String message) {
            println "$category.paddedLabel: $message"
        }
    }

    protected boolean doRemoveClasspathContainer = true

    @Before
    final void setUpDslTestCase() {
        assumeTrue(!GroovyDSLCoreActivator.default.isDSLDDisabled())

        GroovyLogManager.manager.addLogger(logger)
        if (doRemoveClasspathContainer) {
            GroovyRuntime.removeClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, javaProject)
            GroovyDSLCoreActivator.default.contextStoreManager.getDSLDStore(javaProject).purgeAll()
        } else {
            refreshExternalFoldersProject()
            GroovyRuntime.addLibraryToClasspath(javaProject, GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID, false)
            GroovyDSLCoreActivator.default.contextStoreManager.initialize(project, true)
        }
        javaProject.getProject().refreshLocal(IResource.DEPTH_ONE, null)
    }

    @After
    final void tearDownDslTestCase() {
        GroovyLogManager.manager.removeLogger(logger)
        for (IResource member : project.members()) {
            if (member.name.endsWith('.dsld')) {
                Util.delete(member)
            }
        }
    }

    protected IJavaProject getJavaProject() {
        packageFragmentRoot.javaProject
    }

    protected IProject getProject() {
        javaProject.project
    }

    protected int index

    protected String[] createDsls(String... dsls) {
        for (String dsl : dsls) {
            IFile file = project.getFile('dsl' + (index++) + '.dsld')
            file.create(new ByteArrayInputStream(dsl.getBytes(project.defaultCharset)), true, null)

            assert file.exists() : "File '$file' just created, but doesn't exist"
        }
        return dsls
    }

    protected void deleteDslFile(int i) {
        Util.delete(project.getFile('dsl' + i + '.dsld'))
    }

    protected final void assertDeprecated(String contents, int exprStart, int exprEnd) {
        GroovyCompilationUnit unit = addGroovySource(contents, nextUnitName())
        InferencingTestSuite.SearchRequestor requestor = doVisit(exprStart, exprEnd, unit)
        assert requestor.node != null : 'Did not find expected ASTNode'
        assert GroovyUtils.isDeprecated(requestor.result.declaration) : 'Declaration should be deprecated: ' + requestor.result.declaration
    }

    protected final void assertType(String contents, int exprStart, int exprEnd, String expectedType) {
        GroovyCompilationUnit unit = addGroovySource(contents, nextUnitName())
        InferencingTestSuite.assertType(unit, exprStart, exprEnd, expectedType)
    }

    protected final void assertType(String contents, int exprStart, int exprEnd, String expectedType, String extraJavadoc) {
        GroovyCompilationUnit unit = addGroovySource(contents, nextUnitName())
        InferencingTestSuite.assertType(unit, exprStart, exprEnd, expectedType, extraJavadoc)
    }

    protected final void assertDeclaringType(String contents, int exprStart, int exprEnd, String expectedDeclaringType) {
        assertDeclaringType(contents, exprStart, exprEnd, expectedDeclaringType, false)
    }

    protected final void assertDeclaringType(String contents, int exprStart, int exprEnd, String expectedDeclaringType, boolean expectingUnknown) {
        def unit = addGroovySource(contents, nextUnitName())
        def requestor = doVisit(exprStart, exprEnd, unit)

        assert requestor.node != null : 'Did not find expected ASTNode'
        if (!expectedDeclaringType.equals(requestor.getDeclaringTypeName())) {
            StringBuilder sb = new StringBuilder()
            sb.append('Expected declaring type not found.\n')
            sb.append('\tExpected: ').append(expectedDeclaringType).append('\n')
            sb.append('\tFound type: ').append(printTypeName(requestor.result.type)).append('\n')
            sb.append('\tFound declaring type: ').append(printTypeName(requestor.result.declaringType)).append('\n')
            sb.append('\tASTNode: ').append(requestor.node)
            fail(sb.toString())
        }
        if (expectingUnknown) {
            if (requestor.result.confidence != TypeConfidence.UNKNOWN) {
                StringBuilder sb = new StringBuilder()
                sb.append('Confidence: ').append(requestor.result.confidence).append(' (but expecting UNKNOWN)\n')
                sb.append('\tExpected: ').append(expectedDeclaringType).append('\n')
                sb.append('\tFound: ').append(printTypeName(requestor.result.type)).append('\n')
                sb.append('\tDeclaring type: ').append(printTypeName(requestor.result.declaringType)).append('\n')
                sb.append('\tASTNode: ').append(requestor.node)
                fail(sb.toString())
            }
        } else {
            if (requestor.result.confidence == TypeConfidence.UNKNOWN) {
                StringBuilder sb = new StringBuilder()
                sb.append('Expected Confidence should not have been UNKNOWN, but it was.\n')
                sb.append('\tExpected declaring type: ').append(expectedDeclaringType).append('\n')
                sb.append('\tFound type: ').append(printTypeName(requestor.result.type)).append('\n')
                sb.append('\tFound declaring type: ').append(printTypeName(requestor.result.declaringType)).append('\n')
                sb.append('\tASTNode: ').append(requestor.node)
                fail(sb.toString())
            }
        }
    }

    protected final void assertUnknownConfidence(String contents, int exprStart, int exprEnd, String expectedDeclaringType) {
        GroovyCompilationUnit unit = addGroovySource(contents, nextUnitName())
        InferencingTestSuite.SearchRequestor requestor = doVisit(exprStart, exprEnd, unit)

        assert requestor.node != null : 'Did not find expected ASTNode'
        if (requestor.result.confidence != TypeConfidence.UNKNOWN) {
            StringBuilder sb = new StringBuilder()
            sb.append('Expecting unknown confidentce, but was ' + requestor.result.confidence + '.\n')
            sb.append('Expected: ' + expectedDeclaringType + '\n')
            sb.append('Found: ' + printTypeName(requestor.result.type) + '\n')
            sb.append('Declaring type: ' + printTypeName(requestor.result.declaringType) + '\n')
            sb.append('ASTNode: ' + requestor.node + '\n')
            fail(sb.toString())
        }
    }

    protected static final String getTestResourceContents(String fileName) {
        getTestResourceStream(fileName).text
    }

    protected static final InputStream getTestResourceStream(String fileName) {
        getTestResourceURL(fileName).openStream()
    }

    protected static final URL getTestResourceURL(String fileName) {
        IPath path = new Path('testResources').append(fileName)
        new URL(FrameworkUtil.getBundle(DSLInferencingTestSuite.class).getEntry('/'), path.toString())
    }

    /**
     * Refreshes the external folders project.  It seems that the contents of
     * linked folders are not being refreshed fast enough and we are getting
     * {@link IllegalArgumentException}s because there is a resource that is
     * aliased, but no longer exists.
     */
    protected static final void refreshExternalFoldersProject() {
        Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace()
        IProject externalProject = workspace.root.getProject('.org.eclipse.jdt.core.external.folders')
        if (externalProject.exists()) {
            for (IResource member : externalProject.members()) {
                if (member instanceof Folder) {
                    Folder folder = (Folder) member
                    workspace.getAliasManager().updateAliases(folder, folder.store, IResource.DEPTH_INFINITE, null)
                    if (folder.exists()) {
                        folder.refreshLocal(IResource.DEPTH_INFINITE, null)
                    }
                }
            }
        }
    }
}
