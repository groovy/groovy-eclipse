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
package org.codehaus.jdt.groovy.internal.compiler;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector.FileKind;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Copies script files into output directories.
 */
public class ScriptFolderCompilationParticipant extends CompilationParticipant {

    @Override
    public boolean isActive(IJavaProject javaProject) {
        IProject project = javaProject.getProject();
        return GroovyNature.hasGroovyNature(project) && ScriptFolderSelector.isEnabled(project) && LanguageSupportFactory.isGroovyLanguageSupportInstalled();
    }

    @Override
    public void buildStarting(BuildContext[] files, boolean isBatch) {
        if (files == null || files.length < 1) return;
        IProject project = files[0].getFile().getProject();
        try {
            Map<IContainer, IContainer> sourceToOutput = null;
            ScriptFolderSelector selector = new ScriptFolderSelector(project);

            for (BuildContext file : files) {
                if (selector.getFileKind(file.getFile()) == FileKind.SCRIPT) {
                    IPath path = file.getFile().getFullPath();
                    if (sourceToOutput == null) sourceToOutput = createSourceToOutput(project);
                    IContainer sourceFolder = findContainingSourceFolder(sourceToOutput, path);
                    // if null, that means the output folder is the same as the source folder
                    if (sourceFolder != null) {
                        IPath packagePath = findPackagePath(path, sourceFolder);
                        IContainer outputFolder = sourceToOutput.get(sourceFolder);
                        copyFile(file.getFile(), packagePath, outputFolder);
                    }
                }
            }
        } catch (CoreException e) {
            Util.log(e, "Error when copying scripts to output folder");
        }
    }

    private static Map<IContainer, IContainer> createSourceToOutput(IProject project) throws JavaModelException {
        IJavaProject javaProject = JavaCore.create(project);
        IWorkspaceRoot root = (IWorkspaceRoot) project.getParent();
        IClasspathEntry[] classpath = javaProject.getRawClasspath();

        // determine default output folder
        IPath defaultOutputLocation = javaProject.getOutputLocation();
        IContainer defaultOutputFolder = (defaultOutputLocation.segmentCount() > 1 ? root.getFolder(defaultOutputLocation) : project);

        Map<IContainer, IContainer> sourceToOutput = new TreeMap<>(
            // ensure that the longest paths are looked at first so that nested source folders will be appropriately found
            Comparator.comparingInt((IContainer c) -> c.getFullPath().segmentCount()).reversed().thenComparing((IContainer c) -> c.toString()));
        for (IClasspathEntry entry : classpath) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                // determine source folder
                IPath sourcePath = entry.getPath();
                IContainer sourceFolder;
                if (sourcePath.segmentCount() > 1) {
                    sourceFolder = root.getFolder(sourcePath);
                } else {
                    sourceFolder = project;
                }

                // determine out folder
                IPath outputPath = entry.getOutputLocation();
                IContainer outputFolder;
                if (outputPath == null) {
                    outputFolder = defaultOutputFolder;
                } else if (outputPath.segmentCount() > 1) {
                    outputFolder = root.getFolder(outputPath);
                } else {
                    outputFolder = project;
                }

                // if the two containers are equal, that means no copying should be done
                if (!sourceFolder.equals(outputFolder)) {
                    sourceToOutput.put(sourceFolder, outputFolder);
                }
            }
        }
        return sourceToOutput;
    }

    private static IContainer findContainingSourceFolder(Map<IContainer, IContainer> sourceToOutput, IPath filePath) {
        Set<IContainer> sourceFolders = sourceToOutput.keySet();
        for (IContainer sourceFolder : sourceFolders) {
            if (sourceFolder.getFullPath().isPrefixOf(filePath)) {
                return sourceFolder;
            }
        }
        return null;
    }

    private static IPath findPackagePath(IPath filePath, IContainer sourceFolder) {
        filePath = filePath.removeFirstSegments(sourceFolder.getFullPath().segmentCount());
        filePath = filePath.removeLastSegments(1);
        return filePath;
    }

    private static void copyFile(IFile file, IPath packagePath, IContainer outputFolder) throws CoreException {
        IContainer createdFolder = createFolder(packagePath, outputFolder, true);
        IFile toFile = createdFolder.getFile(new Path(file.getName()));
        if (toFile.exists()) {
            toFile.delete(true, null);
        }
        file.copy(toFile.getFullPath(), true, null);
        ResourceAttributes newAttrs = new ResourceAttributes();
        newAttrs.setReadOnly(false);
        newAttrs.setHidden(false);
        toFile.setResourceAttributes(newAttrs);
        toFile.setDerived(true, null);
        toFile.refreshLocal(IResource.DEPTH_ZERO, null);
    }

    /**
     * Creates folder with the given path in the given output folder.
     *
     * @see org.eclipse.jdt.internal.core.builder.AbstractImageBuilder#createFolder(IPath, IContainer)
     */
    private static IContainer createFolder(IPath packagePath, IContainer outputFolder, boolean derived) throws CoreException {
        if (!outputFolder.exists() && outputFolder instanceof IFolder) {
            ((IFolder) outputFolder).create(true, true, null);
        }
        if (packagePath.isEmpty()) {
            return outputFolder;
        }
        IFolder folder = outputFolder.getFolder(packagePath);
        folder.refreshLocal(IResource.DEPTH_ZERO, null);
        if (!folder.exists()) {
            createFolder(packagePath.removeLastSegments(1), outputFolder, derived);
            folder.create(true, true, null);
            folder.setDerived(derived, null);
            folder.refreshLocal(IResource.DEPTH_ZERO, null);
        }
        return folder;
    }
}
