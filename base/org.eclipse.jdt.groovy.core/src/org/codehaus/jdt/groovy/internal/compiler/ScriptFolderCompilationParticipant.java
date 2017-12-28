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
package org.codehaus.jdt.groovy.internal.compiler;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.ICommand;
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
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector.FileKind;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Compilation participant for notification when a compile completes.  Copies
 * specified script files into the output directory.
 */
public class ScriptFolderCompilationParticipant extends CompilationParticipant {

    private IJavaProject project;

    /**
     * We care only about Groovy projects
     */
    @Override
    public boolean isActive(IJavaProject project) {
        boolean hasGroovyNature = GroovyNature.hasGroovyNature(project.getProject());
        if (!hasGroovyNature) {
            return false;
        }
        this.project = project;
        return true;
    }

    @Override
    public void buildStarting(BuildContext[] compiledFiles, boolean isBatch) {
        if (!sanityCheckBuilder(compiledFiles)) {
            // problem happened, do not copy
            return;
        }

        try {
            IProject iproject = project.getProject();
            if (compiledFiles == null || !ScriptFolderSelector.isEnabled(iproject)) {
                return;
            }

            ScriptFolderSelector selector = new ScriptFolderSelector(iproject);
            Map<IContainer, IContainer> sourceToOut = generateSourceToOut(project);
            for (BuildContext compiledFile : compiledFiles) {
                IFile file = compiledFile.getFile();
                if (selector.getFileKind(file) == FileKind.SCRIPT) {
                    IPath filePath = file.getFullPath();
                    IContainer containingSourceFolder = findContainingSourceFolder(sourceToOut, filePath);

                    // if null, that means the out folder is the same as the source folder
                    if (containingSourceFolder != null) {
                        IPath packagePath = findPackagePath(filePath, containingSourceFolder);
                        IContainer out = sourceToOut.get(containingSourceFolder);
                        copyFile(file, packagePath, out);
                    }
                }
            }
        } catch (CoreException e) {
            Util.log(e, "Error when copying scripts to output folder");
        }
    }

    /**
     * Some simple checks that we can do to ensure that the builder is set up properly
     */
    private boolean sanityCheckBuilder(BuildContext[] files) {
        // GRECLIPSE-1230 also do a check to ensure the proper compiler is being used
        if (!LanguageSupportFactory.isGroovyLanguageSupportInstalled()) {
            for (BuildContext buildContext : files) {
                buildContext.recordNewProblems(createProblem(buildContext));
            }
        }
        // also check if this project has the JavaBuilder.
        // note that other builders (like the ajbuilder) may implement the CompilationParticipant API
        try {
            ICommand[] buildSpec = project.getProject().getDescription().getBuildSpec();
            boolean found = false;
            for (ICommand command : buildSpec) {
                if (command.getBuilderName().equals(JavaCore.BUILDER_ID)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (BuildContext buildContext : files) {
                    buildContext.recordNewProblems(createProblem(buildContext));
                }
            }
            return found;
        } catch (CoreException e) {
            Util.log(e);
            return false;
        }

    }

    private CategorizedProblem[] createProblem(BuildContext buildContext) {
        DefaultProblem problem = new DefaultProblem(buildContext.getFile().getFullPath().toOSString().toCharArray(),
                "Error compiling Groovy project.  Either the Groovy-JDT patch is not installed or JavaBuilder is not being used.",
                0, new String[0], ProblemSeverities.Error, 0, 0, 1, 0);
        return new CategorizedProblem[] { problem };
    }

    @Override
    public void buildFinished(IJavaProject project) {
        /*try {
            IProject iproject = project.getProject();
            if (compiledFiles == null || !ScriptFolderSelector.isEnabled(iproject)) {
                return;
            }

            ScriptFolderSelector selector = new ScriptFolderSelector(iproject);
            Map<IContainer, IContainer> sourceToOut = generateSourceToOut(project);
            for (BuildContext compiledFile : compiledFiles) {
                IFile file = compiledFile.getFile();
                if (selector.getFileKind(file) == FileKind.SCRIPT) {
                    IPath filePath = file.getFullPath();
                    IContainer containingSourceFolder = findContainingSourceFolder(sourceToOut, filePath);

                    // if null, that means the out folder is the same as the source folder
                    if (containingSourceFolder != null) {
                        IPath packagePath = findPackagePath(filePath, containingSourceFolder);
                        IContainer out = sourceToOut.get(containingSourceFolder);
                        copyFile(file, packagePath, out);
                    }
                }
            }
        } catch (CoreException e) {
            Util.log(e, "Error in Script folder compilation participant");
        } finally {
            compiledFiles = null;
        }*/
    }

    private IPath findPackagePath(IPath filePath, IContainer containingSourceFolder) {
        IPath containerPath = containingSourceFolder.getFullPath();
        filePath = filePath.removeFirstSegments(containerPath.segmentCount());
        filePath = filePath.removeLastSegments(1);
        return filePath;
    }

    private IContainer findContainingSourceFolder(Map<IContainer, IContainer> sourceToOut, IPath filePath) {
        Set<IContainer> sourceFolders = sourceToOut.keySet();
        for (IContainer container : sourceFolders) {
            if (container.getFullPath().isPrefixOf(filePath)) {
                return container;
            }
        }
        return null;
    }

    private void copyFile(IFile file, IPath packagePath, IContainer outputFolder) throws CoreException {
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
     * Creates folder with the given path in the given output folder. This method is taken from
     * org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.createFolder(..)
     */
    private IContainer createFolder(IPath packagePath, IContainer outputFolder, boolean derived) throws CoreException {
        if (!outputFolder.exists() && outputFolder instanceof IFolder) {
            ((IFolder) outputFolder).create(true, true, null);
        }
        if (packagePath.isEmpty())
            return outputFolder;
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

    private Map<IContainer, IContainer> generateSourceToOut(IJavaProject project) throws JavaModelException {
        IProject p = project.getProject();
        IWorkspaceRoot root = (IWorkspaceRoot) p.getParent();
        IClasspathEntry[] cp = project.getRawClasspath();

        // determine default out folder
        IPath defaultOutPath = project.getOutputLocation();
        IContainer defaultOutContainer;
        if (defaultOutPath.segmentCount() > 1) {
            defaultOutContainer = root.getFolder(defaultOutPath);
        } else {
            defaultOutContainer = p;
        }

        Map<IContainer, IContainer> sourceToOut = new TreeMap<>(
            // ensure that the longest paths are looked at first so that nested source folders will be appropriately found
            Comparator.comparing((IContainer c) -> c.getFullPath().segmentCount()).reversed().thenComparing((IContainer c) -> c.toString()));
        for (IClasspathEntry cpe : cp) {
            if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

                // determine source folder
                IContainer sourceContainer;
                IPath sourcePath = cpe.getPath();
                if (sourcePath.segmentCount() > 1) {
                    sourceContainer = root.getFolder(sourcePath);
                } else {
                    sourceContainer = p;
                }

                // determine out folder
                IPath outPath = cpe.getOutputLocation();
                IContainer outContainer;
                if (outPath == null) {
                    outContainer = defaultOutContainer;
                } else if (outPath.segmentCount() > 1) {
                    outContainer = root.getFolder(outPath);
                } else {
                    outContainer = p;
                }

                // if the two containers are equal, that means no copying should be done
                // do not add to map
                if (!sourceContainer.equals(outContainer)) {
                    sourceToOut.put(sourceContainer, outContainer);
                }
            }
        }
        return sourceToOut;
    }
}
