/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

public class BaseTestSuite extends TestSuite {

    public static List<File> getFileList(final String search) {
        return createFileList(search, "");
    }

    public static List<File> getFileList(final String subFolder,final String search) {
        return createFileList(search, subFolder);
    }

    private static List<File> createFileList(final String search, final String subFolder) {
        final String TEST_FILES = getPathToTestFiles();
        final File dir = new File(TEST_FILES + subFolder);
        if (!dir.isDirectory()) {
            throw new RuntimeException("The path: " + dir.getAbsolutePath() + " is invalid");
        }
        final ArrayList<File> fl = new ArrayList<File>();
        for (final File f : dir.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.matches("^" + search + ".*");
            }
        })) {
            fl.add(f);
        }
        return fl;
    }

    private static String getPluginDirectoryPath() {
        try {
            URL platformURL = Platform.getBundle("org.codehaus.groovy.eclipse.refactoring.test").getEntry("/");
            return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPathToTestFiles() {
        final String systemSeparator = String.valueOf(IPath.SEPARATOR);
        String folders = "/resources";
        folders = folders.replaceAll("/", systemSeparator);

        return getPluginDirectoryPath() + folders;
    }
}
