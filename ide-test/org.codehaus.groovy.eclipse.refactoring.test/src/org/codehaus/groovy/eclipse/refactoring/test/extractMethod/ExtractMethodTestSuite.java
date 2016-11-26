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
package org.codehaus.groovy.eclipse.refactoring.test.extractMethod;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;

import org.codehaus.groovy.eclipse.refactoring.test.BaseTestSuite;

public class ExtractMethodTestSuite extends BaseTestSuite {
    public static TestSuite suite() throws Exception {
        TestSuite suite = new ExtractMethodTestSuite();
        List<File> files = getFileList("/ExtractMethod", "ExtractMethod_Test_");
        assert !files.isEmpty();
        for (File file : files) {
            suite.addTest(new ExtractMethodTestCase(file.getName(), file));
        }
        return suite;
    }
}
