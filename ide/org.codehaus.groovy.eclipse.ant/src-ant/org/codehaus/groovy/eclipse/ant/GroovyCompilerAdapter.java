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

public class GroovyCompilerAdapter extends JDTCompilerAdapter implements CompilerAdapter {

    @Override
    public void setJavac(Javac javac) {
        super.setJavac(javac);
        // now ensure that Groovy files are included
        File[] groovyFiles = getGroovyFiles(javac);
        if (groovyFiles.length > 0) {
            // now log...
            for (int i = 0, n = groovyFiles.length; i < n; i += 1) {
                javac.log("Compiling " + groovyFiles.length + " groovy source file" + (groovyFiles.length == 1 ? "" : "s") +
                    (destDir != null ? " to " + destDir : ""));
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
                throw new BuildException(
                    "srcdir \"" + srcDir.getPath() + "\" does not exist!", javac.getLocation());
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

    private DirectoryScanner getDirectoryScanner(File srcDir, Javac javac) {
        try {
            Method getDirectoryScannerMethod = MatchingTask.class.getDeclaredMethod("getDirectoryScanner", File.class);
            getDirectoryScannerMethod.setAccessible(true);
            return (DirectoryScanner) getDirectoryScannerMethod.invoke(javac, srcDir);
        } catch (Exception e) {
            throw new BuildException("Problem finding directory scanner for srcdir \"" + srcDir.getPath() + "\"", e);
        }
    }
}

class GroovyFileNameMapper implements FileNameMapper {

    @Override
    public String[] mapFileName(String sourceFileName) {
        if (sourceFileName != null) {
            if (sourceFileName.endsWith(".groovy")) {
                return new String[] {extractVariablePart(sourceFileName, ".groovy".length()) + ".class"};
            }
        }
        return null;
    }

    private String extractVariablePart(String name, int postfixLength) {
        return name.substring(0, name.length() - postfixLength);
    }

    @Override
    public void setFrom(String from) {
        // no-op
    }

    @Override
    public void setTo(String to) {
        // no-op
    }
}
