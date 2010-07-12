/*******************************************************************************
 * Copyright (c) 2007, 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.ant;


import java.io.File;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.SourceFileScanner;
import org.eclipse.jdt.core.JDTCompilerAdapter;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jul 10, 2010
 */
public class GroovyCompilerAdapter extends JDTCompilerAdapter implements CompilerAdapter {
    
    @Override
    public void setJavac(Javac javac) {
        super.setJavac(javac);
        // now ensure that Groovy files are included
        File[] groovyFiles = getGroovyFiles(javac);
        if (groovyFiles.length > 0) {
            // now log...
            for (int i = 0; i < groovyFiles.length; i++) {
                javac.log("Compiling " + groovyFiles.length + " groovy source file"
                        + (groovyFiles.length == 1 ? "" : "s")
                        + (destDir != null ? " to " + destDir : ""));
                String filename = groovyFiles[i].getAbsolutePath();
                javac.log(filename);
            }
            
            File[] newCompileList = new File[groovyFiles.length + compileList.length];
            System.arraycopy(compileList, 0, newCompileList, 0, compileList.length);
            System.arraycopy(groovyFiles, 0, newCompileList, compileList.length, groovyFiles.length);
            compileList = newCompileList;
        }
    }
    
    protected File[] getGroovyFiles(Javac javac) {
        String[] list = javac.getSrcdir().list();
        File destDir = javac.getDestdir();
        File[] sourceFiles = new File[0]; 
        for (int i = 0; i < list.length; i++) {
            File srcDir = javac.getProject().resolveFile(list[i]);
            if (!srcDir.exists()) {
                throw new BuildException("srcdir \""
                                         + srcDir.getPath()
                                         + "\" does not exist!", javac.getLocation());
            }

            DirectoryScanner ds = getDirectoryScanner(srcDir, javac);
            String[] files = ds.getIncludedFiles();

            GroovyFileNameMapper m = new GroovyFileNameMapper();
            SourceFileScanner sfs = new SourceFileScanner(javac);
            File[] moreFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
            if (moreFiles != null) {
                File[] origFiles = sourceFiles;
                sourceFiles = new File[origFiles.length + moreFiles.length];
                System.arraycopy(origFiles, 0, sourceFiles, 0, origFiles.length);
                System.arraycopy(moreFiles, 0, sourceFiles, origFiles.length, moreFiles.length);
            }
        }
        return sourceFiles;
    }
    
    /**
     * @param srcDir
     * @return
     */
    private DirectoryScanner getDirectoryScanner(File srcDir, Javac javac) {
        try {
            Method getDirectoryScannerMethod = MatchingTask.class.getDeclaredMethod("getDirectoryScanner", File.class);
            getDirectoryScannerMethod.setAccessible(true);
            return (DirectoryScanner) getDirectoryScannerMethod.invoke(javac, srcDir);
        } catch (Exception e) {
            throw new BuildException("Problem finding directory scanner for srcdir \""
                    + srcDir.getPath()
                    + "\"", e);
        }
    }

}


class GroovyFileNameMapper implements FileNameMapper {

    public String[] mapFileName(String sourceFileName) {
        if (sourceFileName != null) {
            if (sourceFileName.endsWith(".groovy")) {
                return new String[] {
                        extractVariablePart(sourceFileName, ".groovy".length())
                        + ".class" };
            }
        }
        return null;
    }
    
    private String extractVariablePart(String name, int postfixLength) {
        return name.substring(0,
                name.length() - postfixLength);
    }


    public void setFrom(String from) {
        // noop
    }

    public void setTo(String to) {
        // noop
    }

}
