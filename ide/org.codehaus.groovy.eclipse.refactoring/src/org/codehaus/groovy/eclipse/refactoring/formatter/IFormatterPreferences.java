/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

/**
 * @author Kris De Volder
 * @created 2010-05-21
 */
public interface IFormatterPreferences {
    int getBracesEnd();
    int getBracesStart();
    boolean useTabs();
    int getIndentationSize();
    int getTabSize();
    int getIndentationMultiline();
    int getMaxLineLength();
    boolean isSmartPaste();
    boolean isIndentEmptyLines();
    boolean isRemoveUnnecessarySemicolons();

    int getLongListLength();
}
