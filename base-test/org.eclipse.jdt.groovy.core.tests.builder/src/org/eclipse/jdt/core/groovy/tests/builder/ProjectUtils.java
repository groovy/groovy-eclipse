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
package org.eclipse.jdt.core.groovy.tests.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Project creation utilities taken from AJDT: 
 *   org.eclipse.ajdt.core.tests.AJDTCoreTestCase
 * @author Andrew Eisenberg
 * @created Oct 6, 2010
 */
public class ProjectUtils {
    private ProjectUtils() { }

    public static String getSourceWorkspacePath() {
        return getPluginDirectoryPath() +  java.io.File.separator + "workspace"; //$NON-NLS-1$
    }
    
    protected static String getTestBundleName() {
        return "org.eclipse.jdt.groovy.core.tests.builder";
    }
    
    /**
     * Returns the OS path to the directory that contains this plugin.
     */
    protected static String getPluginDirectoryPath() {
        try {
            URL platformURL = Platform.getBundle(getTestBundleName()).getEntry("/"); //$NON-NLS-1$ //$NON-NLS-2$
            return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the IWorkspace this test suite is running on.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }
    
    public static IWorkspaceRoot getWorkspaceRoot() {
        return getWorkspace().getRoot();
    }

    
    protected static IJavaProject setUpJavaProject(final String projectName) throws CoreException, IOException {
        return setUpJavaProject(projectName, "1.5"); //$NON-NLS-1$
    }
    
    protected static IJavaProject setUpJavaProject(final String projectName, String compliance) throws CoreException, IOException {
        // copy files in project from source workspace to target workspace
        String sourceWorkspacePath = getSourceWorkspacePath();
        String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
        
        // return null if source directory does not exist
        if (! copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName))) {
            return null;
        }
        
        // create project
        final IProject project = getWorkspaceRoot().getProject(projectName);
        if (! project.exists()) {
            IWorkspaceRunnable populate = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    project.create(null);
                }
            };
            getWorkspace().run(populate, null);
        }       
        // ensure open
        project.open(null);
        
        IJavaProject javaProject = JavaCore.create(project);
        return javaProject;
    }

    public static IProject createPredefinedProject(final String projectName) throws CoreException, RuntimeException {
        IJavaProject jp;
        try {
            jp = setUpJavaProject(projectName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (jp == null) {
            // project was not found
            return null;
        }
        
        try {
            jp.setOption("org.eclipse.jdt.core.compiler.problem.missingSerialVersion", "ignore"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (NullPointerException npe) {
        }
        return jp.getProject();
    }

    /**
     * Copy the given source directory (and all its contents) to the given target directory.
     */
    protected static boolean copyDirectory(File source, File target) throws IOException {
        if (! source.exists()) {
            return false;
        }
        if (!target.exists()) {
            target.mkdirs();
        }
        File[] files = source.listFiles();
        if (files == null) return true;
        for (int i = 0; i < files.length; i++) {
            File sourceChild = files[i];
            String name =  sourceChild.getName();
            if (name.equals("CVS")) continue; //$NON-NLS-1$
            File targetChild = new File(target, name);
            if (sourceChild.isDirectory()) {
                copyDirectory(sourceChild, targetChild);
            } else {
                copy(sourceChild, targetChild);
            }
        }
        return true;
    }
    
    /**
     * Copy file from src (path to the original file) to dest (path to the destination file).
     */
    public static void copy(File src, File dest) throws IOException {
        // read source bytes
        byte[] srcBytes = read(src);
        
        if (convertToIndependantLineDelimiter(src)) {
            String contents = new String(srcBytes);
            contents = convertToIndependantLineDelimiter(contents);
            srcBytes = contents.getBytes();
        }
    
        // write bytes to dest
        FileOutputStream out = new FileOutputStream(dest);
        out.write(srcBytes);
        out.close();
    }
    
    public static byte[] read(java.io.File file) throws java.io.IOException {
        int fileLength;
        byte[] fileBytes = new byte[fileLength = (int) file.length()];
        java.io.FileInputStream stream = new java.io.FileInputStream(file);
        int bytesRead = 0;
        int lastReadSize = 0;
        while ((lastReadSize != -1) && (bytesRead != fileLength)) {
            lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
            bytesRead += lastReadSize;
        }
        stream.close();
        return fileBytes;
    }

    public static boolean convertToIndependantLineDelimiter(File file) {
        return SOURCE_FILTER.accept(file.getName());
    }
    
    public static final FilenameFilter SOURCE_FILTER = new FilenameFilter() {
        public boolean accept(String name) {
            return (name.endsWith(".java") || name.endsWith(".aj"));  //$NON-NLS-1$ //$NON-NLS-2$
        }
    };
    
    public static interface FilenameFilter {
        public boolean accept(String name);
    }


    public static String convertToIndependantLineDelimiter(String source) {
        if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1) return source;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, length = source.length(); i < length; i++) {
            char car = source.charAt(i);
            if (car == '\r') {
                buffer.append('\n');
                if (i < length-1 && source.charAt(i+1) == '\n') {
                    i++; // skip \n after \r
                }
            } else {
                buffer.append(car);
            }
        }
        return buffer.toString();
    }
}
