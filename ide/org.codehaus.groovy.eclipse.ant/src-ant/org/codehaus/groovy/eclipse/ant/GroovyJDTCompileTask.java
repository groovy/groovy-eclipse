/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
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

import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;

/**
 * The ant task that calls the JDT Compiler for groovy.
 * <p> 
 * This task takes the same arguments as the Javac task
 * 
 * 
 * @author Andrew Eisenberg
 * @created Jul 6, 2009
 *
 */
public class GroovyJDTCompileTask extends Javac {
    public GroovyJDTCompileTask() {
    }

    
    protected void scanDir(File srcDir, File destDir, String[] files) {
        GlobPatternMapper m = new GlobPatternMapper();
        m.setFrom("*.java"); //$NON-NLS-1$
        m.setTo("*.class"); //$NON-NLS-1$
        SourceFileScanner sfs = new SourceFileScanner(this);
        File[] newJavaFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);

        m.setFrom("*.groovy");  //$NON-NLS-1$
        File[] newGroovyFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);

        if (newJavaFiles.length > 0 || newGroovyFiles.length > 0) {
        
            File[] newCompileList
                = new File[compileList.length + newJavaFiles.length + newGroovyFiles.length];
        
            System.arraycopy(compileList, 0, newCompileList, 0,
                    compileList.length);
            System.arraycopy(newJavaFiles, 0, newCompileList,
                    compileList.length, newJavaFiles.length);
            System.arraycopy(newGroovyFiles, 0, newCompileList,
                    compileList.length+newJavaFiles.length, newGroovyFiles.length);
            compileList = newCompileList;
        }

    }
}
