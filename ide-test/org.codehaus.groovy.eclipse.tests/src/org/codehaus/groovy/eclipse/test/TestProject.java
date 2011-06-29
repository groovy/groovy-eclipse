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
package org.codehaus.groovy.eclipse.test;

import static org.eclipse.jdt.core.search.IJavaSearchConstants.CLASS;
import static org.eclipse.jdt.core.search.IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH;
import static org.eclipse.jdt.core.search.SearchEngine.createJavaSearchScope;
import static org.eclipse.jdt.core.search.SearchPattern.R_CASE_SENSITIVE;
import static org.eclipse.jdt.core.search.SearchPattern.R_EXACT_MATCH;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.launching.JavaRuntime;

public class TestProject {
    public static final String TEST_PROJECT_NAME = "TestProject";

    private final IProject project;

    private final IJavaProject javaProject;

    private IPackageFragmentRoot sourceFolder;

    public TestProject(String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        project = root.getProject(name);
        project.create(null);
        project.open(null);
        javaProject = JavaCore.create(project);

        IFolder binFolder = createBinFolder();

        setJavaNature();
        javaProject.setRawClasspath(new IClasspathEntry[0], null);

        createOutputFolder(binFolder);
        sourceFolder = createSourceFolder();
        addSystemLibraries();
        
        javaProject.setOption(CompilerOptions.OPTION_Compliance, "1.5");
        javaProject.setOption(CompilerOptions.OPTION_Source, "1.5");
        javaProject.setOption(CompilerOptions.OPTION_TargetPlatform, "1.5");
    }
    
    public TestProject() throws CoreException {
        this(TEST_PROJECT_NAME);
    }

    public IProject getProject() {
        return project;
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }
    
    public GroovyProjectFacade getGroovyProjectFacade() {
        return new GroovyProjectFacade(javaProject);
    }

    public boolean hasGroovyContainer() throws JavaModelException {
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER
                    && entries[i].getPath().equals(
                            GroovyClasspathContainer.CONTAINER_ID)) {
                return true;
            }
        }
        return false;
    }

    public IPackageFragment createPackage(String name) throws CoreException {
        if (sourceFolder == null)
            sourceFolder = createSourceFolder();
        return sourceFolder.createPackageFragment(name, false, null);
    }

    public void deletePackage(String name) throws CoreException {
        sourceFolder.getPackageFragment(name).delete(true, null);
    }

    public IType createJavaType(IPackageFragment pack, String cuName,
            String source) throws JavaModelException {
        StringBuffer buf = new StringBuffer();
        if (!pack.isDefaultPackage()) {
            buf.append("package " + pack.getElementName() + ";" + System.getProperty("line.separator"));
        }
        buf.append(System.getProperty("line.separator"));
        buf.append(source);
        ICompilationUnit cu = pack.createCompilationUnit(cuName,
                buf.toString(), false, null);
        return cu.getTypes()[0];
    }

    public IType createJavaTypeAndPackage(String packageName, String fileName,
            String source) throws CoreException {
        return createJavaType(createPackage(packageName), fileName, source);
    }

    public IFile createGroovyTypeAndPackage(String packageName,
            String fileName, InputStream source) throws CoreException,
            IOException {
        return createGroovyType(createPackage(packageName), fileName, IOUtils
                .toString(source));
    }

    public IFile createGroovyTypeAndPackage(String packageName,
            String fileName, String source) throws CoreException {
        return createGroovyType(createPackage(packageName), fileName, source);
    }

    public IFile createGroovyType(IPackageFragment pack, String cuName,
            String source) throws CoreException {
        StringBuffer buf = new StringBuffer();
        if (! pack.getElementName().equals("")) {
            buf.append("package " + pack.getElementName() + ";" + System.getProperty("line.separator"));
            buf.append(System.getProperty("line.separator"));
        }
        buf.append(source);

        IContainer folder = (IContainer) pack.getResource();
        String encoding = javaProject.getOption(JavaCore.CORE_ENCODING, true);
        InputStream stream;
        try {
            stream = new ByteArrayInputStream(encoding == null ? buf.toString()
                    .getBytes() : buf.toString().getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new Status(IStatus.ERROR,
                    "org.codehaus.groovy.eclipse.tests", IStatus.ERROR,
                    "failed to create a groovy type", e));
        }

        return createFile(folder, cuName, stream);
    }

    public void removeNature(String natureId) throws CoreException {
        final IProjectDescription description = project.getDescription();
        final String[] ids = description.getNatureIds();
        for (int i = 0; i < ids.length; ++i) {
            if (ids[i].equals(natureId)) {
                final String[] newIds = remove(ids, i);
                description.setNatureIds(newIds);
                project.setDescription(description, null);
                return;
            }
        }
    }

    private String[] remove(String[] ids, int index) {
        String[] newIds = new String[ids.length-1];
        for (int i = 0, j = 0; i < ids.length; i++) {
            if (i != index) {
                newIds[j] = ids[i];
                j++;
            }
        }
        return newIds;
    }

    public void addBuilder(String newBuilder) throws CoreException {
        final IProjectDescription description = project.getDescription();
        ICommand[] commands = description.getBuildSpec();
        ICommand newCommand = new BuildCommand();
        newCommand.setBuilderName(newBuilder);
        ICommand[] newCommands = new ICommand[commands.length+1];
        newCommands[0] = newCommand;
        System.arraycopy(commands, 0, newCommands, 1, commands.length);
        description.setBuildSpec(newCommands);
        project.setDescription(description, null);
    }

    public void addNature(String natureId) throws CoreException {
        final IProjectDescription description = project.getDescription();
        final String[] ids = description.getNatureIds();
        final String[] newIds = new String[ids.length+1];
        newIds[0] = natureId;
        System.arraycopy(ids, 0, newIds, 1, ids.length);
        description.setNatureIds(newIds);
        project.setDescription(description, null);
    }

    private IFile createFile(IContainer folder, String name,
            InputStream contents) throws JavaModelException {
        IFile file = folder.getFile(new Path(name));
        try {
            if (file.exists()) {
                file.delete(true, null);
            }
            file.create(contents, IResource.FORCE, null);

        } catch (CoreException e) {
            throw new JavaModelException(e);
        }

        return file;
    }

    public void dispose() throws CoreException {
        deleteWorkingCopies();
        project.delete(true, true, null);
    }
    
    public void deleteContents() throws CoreException {
        deleteWorkingCopies();
        IPackageFragment[] frags = javaProject.getPackageFragments();
        for (IPackageFragment frag : frags) {
            if (!frag.isReadOnly()) {
                frag.delete(true, null);
            }
        }
    }

    private void deleteWorkingCopies() throws JavaModelException {
        waitForIndexer();
        // delete all working copies
        ICompilationUnit[] workingCopies = JavaModelManager
                .getJavaModelManager().getWorkingCopies(
                        DefaultWorkingCopyOwner.PRIMARY, true);
        if (workingCopies != null) {
            for (ICompilationUnit workingCopy : workingCopies) {
                if (workingCopy.isWorkingCopy()) {
                    workingCopy.discardWorkingCopy();
                }
            }
        }
        System.gc();
    }
    
    private IFolder createBinFolder() throws CoreException {
        final IFolder binFolder = project.getFolder("bin");
        if (!binFolder.exists())
            ensureExists(binFolder);
        return binFolder;
    }

    private void setJavaNature() throws CoreException {
        IProjectDescription description = project.getDescription();
        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        project.setDescription(description, null);
    }

    private void createOutputFolder(IFolder binFolder)
            throws JavaModelException {
        IPath outputLocation = binFolder.getFullPath();
        javaProject.setOutputLocation(outputLocation, null);
    }

    private IPackageFragmentRoot createSourceFolder() throws CoreException {
        IFolder folder = project.getFolder("src");
        if (!folder.exists())
            ensureExists(folder);
        final IClasspathEntry[] entries = javaProject
                .getResolvedClasspath(false);
        final IPackageFragmentRoot root = javaProject
                .getPackageFragmentRoot(folder);
        for (int i = 0; i < entries.length; i++) {
            final IClasspathEntry entry = entries[i];
            if (entry.getPath().equals(folder.getFullPath()))
                return root;
        }
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
        javaProject.setRawClasspath(newEntries, null);
        return root;
    }
    
    public IPackageFragmentRoot createOtherSourceFolder() throws CoreException {
        return createOtherSourceFolder(null);
    }
    public IPackageFragmentRoot createOtherSourceFolder(String outPath) throws CoreException {
        return createSourceFolder("other", outPath);
    }

    public IPackageFragmentRoot createSourceFolder(String path, String outPath) throws CoreException {
        return createSourceFolder(path, outPath, null);
    }
    
    public IPackageFragmentRoot createSourceFolder(String path, String outPath, IPath[] exclusionPattern) throws CoreException {
        IFolder folder = project.getFolder(path);
        if (!folder.exists()) {
            ensureExists(folder);
        }
        
        final IClasspathEntry[] entries = javaProject
            .getResolvedClasspath(false);
        final IPackageFragmentRoot root = javaProject
            .getPackageFragmentRoot(folder);
        for (int i = 0; i < entries.length; i++) {
            final IClasspathEntry entry = entries[i];
            if (entry.getPath().equals(folder.getFullPath())) {
                return root;
            }
        }
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        IPath outPathPath = outPath == null ? null : getProject().getFullPath().append(outPath).makeAbsolute();
        newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath(), exclusionPattern, outPathPath);
        javaProject.setRawClasspath(newEntries, null);
        return root;

    }

    /**
     * @param folder
     * @throws CoreException
     */
    private void ensureExists(IFolder folder) throws CoreException {
        if (folder.getParent().getType() == IResource.FOLDER && !folder.getParent().exists()) {
            ensureExists((IFolder) folder.getParent());
        }
        folder.create(false, true, null);
    }

    public void addProjectReference(IJavaProject referent) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaCore.newProjectEntry(referent.getPath());
        javaProject.setRawClasspath(newEntries, null);
    }
    
    public void addJarFileToClasspath(IPath path) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaCore.newLibraryEntry(path, null, null);
        javaProject.setRawClasspath(newEntries, null);
    }
    
    private void addSystemLibraries() throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaRuntime
                .getDefaultJREContainerEntry();
        javaProject.setRawClasspath(newEntries, null);
    }

    @SuppressWarnings("deprecation")
    private void waitForIndexer() throws JavaModelException {
        final TypeNameRequestor requestor = new TypeNameRequestor() {};
        new SearchEngine().searchAllTypeNames(null, null, R_EXACT_MATCH
                | R_CASE_SENSITIVE, CLASS,
                createJavaSearchScope(new IJavaElement[0]), requestor,
                WAIT_UNTIL_READY_TO_SEARCH, null);
    }

    
    protected void fullBuild() throws CoreException {
        this.getProject().build(org.eclipse.core.resources.IncrementalProjectBuilder.FULL_BUILD, null);
    }
    
    public String getProblems() throws CoreException {
        IMarker[] markers = getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        StringBuilder sb = new StringBuilder();
        if (markers == null || markers.length == 0) {
            return null;
        }
        boolean errorFound = false;
        sb.append("Problems:\n");
        for (int i = 0; i < markers.length; i++) {
            if (((Integer) markers[i].getAttribute(IMarker.SEVERITY)).intValue() == IMarker.SEVERITY_ERROR) {
                sb.append("  ");
                sb.append(markers[i].getResource().getName()).append(" : ");
                sb.append(markers[i].getAttribute(IMarker.LOCATION)).append(" : ");
                sb.append(markers[i].getAttribute(IMarker.MESSAGE)).append("\n");
                errorFound = true;
            }
        }
        return errorFound ? sb.toString() : null;
    }

    /**
     * Create file at project root.
     * @param name
     * @param contents
     * @return
     * @throws Exception
     */
    public IFile createFile(String name, String contents) throws Exception {
        String encoding = null;
        try {
            encoding = project.getDefaultCharset(); // get project encoding as file is not accessible
        } catch (CoreException ce) {
            // use no encoding
        }
        InputStream stream = new ByteArrayInputStream(encoding == null ? contents.getBytes() : contents.getBytes(encoding));
        IFile file= project.getFile(new Path(name));
        if (!file.getParent().exists()) {
            createFolder(file.getParent());
        }
        file.create(stream, true, null);
        return file;
    }

    private void createFolder(IContainer parent) throws CoreException {
        if (!parent.getParent().exists()) {
            if (parent.getParent().getType() != IResource.FOLDER) {
                Assert.fail("Project doesn't exist " + parent.getParent());
            }
            createFolder(parent.getParent());
        }
        ((IFolder) parent).create(true, true, null);
    }

    public IPackageFragmentRoot getSourceFolder() {
		return sourceFolder;
	}

    public ICompilationUnit createUnit(String pkg, String cuName, String cuContents) throws CoreException {
        IPackageFragment frag = createPackage(pkg);
        ICompilationUnit cu = frag.createCompilationUnit(cuName,
                cuContents, false, null);
        return cu;
    }

    public ICompilationUnit[] createUnits(String[] packages, String[] cuNames,
            String[] cuContents) throws CoreException {
        ICompilationUnit[] units = new ICompilationUnit[packages.length];
        for (int i = 0; i < cuContents.length; i++) {
            units[i] = createUnit(packages[i], cuNames[i], cuContents[i]);
        }
        return units;
    }
}