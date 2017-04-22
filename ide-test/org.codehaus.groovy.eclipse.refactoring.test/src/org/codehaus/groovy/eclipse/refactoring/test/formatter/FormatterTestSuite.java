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
package org.codehaus.groovy.eclipse.refactoring.test.formatter;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import org.codehaus.groovy.eclipse.refactoring.test.BaseTestSuite;

public final class FormatterTestSuite extends BaseTestSuite {
    public static TestSuite suite() throws Exception {
        TestSuite ts = new FormatterTestSuite();
        List<File> files = getFileList("/Formatter", "Formatter_Test_");
        for (File file : files) {
            ts.addTest(new FormatterTestCase(file.getName(), file));
        }
        return ts;
    }
}
