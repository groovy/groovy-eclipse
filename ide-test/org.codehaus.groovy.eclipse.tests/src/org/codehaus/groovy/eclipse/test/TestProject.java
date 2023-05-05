/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.test;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.launching.JavaRuntime;

public class TestProject {

    private final IProject project;

    private final IJavaProject javaProject;

    private IPackageFragmentRoot sourceFolder;

    /*package*/ TestProject() throws CoreException {
        this("TestProject");
    }

    public TestProject(final String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        project = root.getProject(name);
        assertFalse(project.exists());
        project.create(null);
        project.open(null);

        CoreUtility.setAutoBuilding(false);
        IFolder binFolder = project.getFolder("bin");
        IFolder srcFolder = project.getFolder("src");
        CoreUtility.createFolder(srcFolder, true, true, null);

        addNature(JavaCore.NATURE_ID);
        javaProject = JavaCore.create(project);
        javaProject.setOption(CompilerOptions.OPTION_Source, "11");
        javaProject.setOption(CompilerOptions.OPTION_Compliance, "11");
        javaProject.setOption(CompilerOptions.OPTION_TargetPlatform, "11");

        javaProject.setRawClasspath(new IClasspathEntry[] {
            JavaCore.newSourceEntry(srcFolder.getFullPath()),
            GroovyRuntime.newGroovyClasspathContainerEntry(false, false, false),
            JavaRuntime.getDefaultJREContainerEntry()}, binFolder.getFullPath(), null);

        sourceFolder = javaProject.findPackageFragmentRoot(srcFolder.getFullPath());

        addNature(GroovyNature.GROOVY_NATURE); // <-- schedules a job for clean and build
        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (job.getName().startsWith("Cleaning/Rebuilding")) {
                    SynchronizationUtils.joinUninterruptibly(job);
                }
            }
        }
        SynchronizationUtils.waitForIndexingToComplete();
    }

    public void addNature(final String natureId) throws CoreException {
        final IProjectDescription description = project.getDescription();
        description.setNatureIds((String[]) ArrayUtils.add(description.getNatureIds(), 0, natureId));
        project.setDescription(description, null);
    }

    public void removeNature(final String natureId) throws CoreException {
        final IProjectDescription description = project.getDescription();
        final String[] ids = description.getNatureIds();
        for (int i = 0, n = ids.length; i < n; i += 1) {
            if (ids[i].equals(natureId)) {
                description.setNatureIds((String[]) ArrayUtils.remove(ids, i));
                project.setDescription(description, null);
                return;
            }
        }
    }

    public void addBuilder(final String newBuilder) throws CoreException {
        ICommand buildCommand = new BuildCommand();
        buildCommand.setBuilderName(newBuilder);

        final IProjectDescription description = project.getDescription();
        description.setBuildSpec((ICommand[]) ArrayUtils.add(description.getBuildSpec(), 0, buildCommand));
        project.setDescription(description, null);
    }

    public void addClasspathEntry(final IClasspathEntry entry) throws JavaModelException {
        if (!GroovyRuntime.findClasspathEntry(javaProject, entry::equals).isPresent()) {
            GroovyRuntime.appendClasspathEntry(javaProject, entry);
        }
    }

    public void addExternalLibrary(final IPath libraryPath) throws JavaModelException {
        addClasspathEntry(JavaCore.newLibraryEntry(libraryPath, null, null));
    }

    public void addProjectReference(final IJavaProject referent) throws JavaModelException {
        addClasspathEntry(JavaCore.newProjectEntry(referent.getPath()));
    }

    public IPackageFragment createPackage(final String packageName) throws JavaModelException {
        return sourceFolder.createPackageFragment(packageName, true, null);
    }

    public ICompilationUnit createJavaType(final IPackageFragment packageFrag, final String fileName, final CharSequence source) throws JavaModelException {
        StringBuilder buf = new StringBuilder();
        appendPackage(buf, packageFrag, source);
        buf.append(source);

        ICompilationUnit unit = packageFrag.createCompilationUnit(fileName, buf.toString(), false, null);
        unit.becomeWorkingCopy(null);
        return unit;
    }

    public ICompilationUnit createJavaTypeAndPackage(final String packageName, final String fileName, final CharSequence source) throws JavaModelException {
        return createJavaType(createPackage(packageName), fileName, source);
    }

    public ICompilationUnit createGroovyTypeAndPackage(final String packageName, final String fileName, final InputStream source) throws JavaModelException {
        String fileText;
        try {
            fileText = IOGroovyMethods.getText(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return createGroovyType(createPackage(packageName), fileName, fileText);
    }

    public ICompilationUnit createGroovyTypeAndPackage(final String packageName, final String fileName, final CharSequence source) throws JavaModelException {
        return createGroovyType(createPackage(packageName), fileName, source);
    }

    public ICompilationUnit createGroovyType(final IPackageFragment packageFrag, final String fileName, final CharSequence source) throws JavaModelException {
        StringBuilder buf = new StringBuilder();
        appendPackage(buf, packageFrag, source);
        buf.append(source);

        ICompilationUnit unit = packageFrag.createCompilationUnit(fileName, buf.toString(), true, null);
        unit.becomeWorkingCopy(null);
        return unit;
    }

    private static void appendPackage(final StringBuilder sb, final IPackageFragment pf, final CharSequence cs) {
        if (!pf.isDefaultPackage() && (cs.length() < 8 || !cs.subSequence(0, 7).toString().equals("package"))) {
            sb.append("package ");
            sb.append(pf.getElementName());
            sb.append(";\n\n");
        }
    }

    /**
     * Creates a text file within the project's source folder.
     *
     * @param path relative file path
     */
    public IFile createFile(final String path, final String contents) throws CoreException, UnsupportedEncodingException {
        IFile file = project.getFolder("src").getFile(new Path(path));
        if (!file.getParent().exists()) {
            CoreUtility.createFolder((IFolder) file.getParent(), true, true, null);
        }
        String encoding = project.getDefaultCharset();
        InputStream stream = new java.io.ByteArrayInputStream(encoding == null ? contents.getBytes() : contents.getBytes(encoding));
        file.create(stream, true, null);
        return file;
    }

    /**
     * Creates a source folder within the project.
     *
     * @param path relative folder path
     */
    public IPackageFragmentRoot createSourceFolder(final String path, /*@Nullable*/ final String outPath, final IPath... exclusions) throws CoreException {
        IFolder folder = project.getFolder(path);
        if (!folder.exists()) {
            CoreUtility.createFolder(folder, true, true, null);
        }
        IClasspathEntry sourceFolderEntry = null;
        for (IClasspathEntry entry : javaProject.getRawClasspath()) {
            if (entry.getPath().equals(folder.getFullPath())) {
                sourceFolderEntry = entry;
                break;
            }
        }
        if (sourceFolderEntry == null) {
            IPath outPathPath = (outPath == null ? null : project.getFullPath().append(outPath).makeAbsolute());
            sourceFolderEntry = JavaCore.newSourceEntry(folder.getFullPath(), exclusions, outPathPath);
            addClasspathEntry(sourceFolderEntry);
        }
        return javaProject.findPackageFragmentRoots(sourceFolderEntry)[0];
    }

    //--------------------------------------------------------------------------

    public IProject getProject() {
        return project;
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }

    public IPackageFragmentRoot getSourceFolder() {
        return sourceFolder;
    }

    //--------------------------------------------------------------------------

    public void dispose() {
        SynchronizationUtils.joinBackgroundActivities();
        discardWorkingCopies();
        Util.delete(project);
    }

    /*package*/ void discardWorkingCopies() {
        ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true);
        if (workingCopies != null && workingCopies.length > 0) {
            for (ICompilationUnit workingCopy : workingCopies) {
                if (workingCopy.isWorkingCopy()) {
                    try {
                        workingCopy.discardWorkingCopy();
                    } catch (JavaModelException ignore) {
                    }
                }
            }
        }
    }
}
