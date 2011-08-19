/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.test.formatter;

import java.io.File;
import java.util.List;

import org.codehaus.groovy.eclipse.refactoring.test.BaseTestSuite;

import junit.framework.TestSuite;

public class FormatterTestSuite extends BaseTestSuite {

    public static TestSuite suite() {
        TestSuite ts = new TestSuite("Formatter Suite");
        List<File> files = getFileList("/Formatter", "Formatter_Test_");
        for (File file : files) {
            if (file.getName().equals("Formatter_Test_GRE_1137_Spaces.txt"))
            ts.addTest(new FormatterTestCase(file.getName(),file));
        }
        ts.addTestSuite(FindIndentsTests.class);
        ts.addTestSuite(TestFormatterPreferences.class);
        ts.addTestSuite(GroovyDocumentScannerTests.class);
        return ts;
    }
}
