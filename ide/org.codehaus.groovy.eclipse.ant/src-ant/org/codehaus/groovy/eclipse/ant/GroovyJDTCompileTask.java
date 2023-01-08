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

import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;

/**
 * The Ant task that calls the JDT Compiler for Groovy.
 * <p>
 * This task takes the same arguments as the Javac task.
 */
public class GroovyJDTCompileTask extends Javac {

    @Override
    protected void scanDir(File srcDir, File destDir, String[] files) {
        GlobPatternMapper m = new GlobPatternMapper();
        m.setFrom("*.java");
        m.setTo("*.class");
        SourceFileScanner sfs = new SourceFileScanner(this);
        File[] newJavaFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
        m.setFrom("*.groovy");
        File[] newGroovyFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
        if (newJavaFiles.length > 0 || newGroovyFiles.length > 0) {
            File[] newCompileList = new File[compileList.length + newJavaFiles.length + newGroovyFiles.length];
            System.arraycopy(compileList, 0, newCompileList, 0, compileList.length);
            System.arraycopy(newJavaFiles, 0, newCompileList, compileList.length, newJavaFiles.length);
            System.arraycopy(newGroovyFiles, 0, newCompileList, compileList.length + newJavaFiles.length, newGroovyFiles.length);
            compileList = newCompileList;
        }
    }
}
