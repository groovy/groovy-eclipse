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
package org.codehaus.groovy.eclipse.refactoring;

import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

    public static final String P_PATH = "pathPreference";

    // Formatter
    public static final String GROOVY_FORMATTER_INDENTATION = DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR;

    public static final String GROOVY_FORMATTER_INDENTATION_SIZE = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;

    public static final String GROOVY_FORMATTER_TAB_SIZE = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
    // FIXADE wierd
    // public static final String GROOVY_FORMATTER_INDENTATION_SIZE =
    // DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
    //
    // public static final String GROOVY_FORMATTER_TAB_SIZE =
    // DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
    public static final String GROOVY_FORMATTER_MULTILINE_INDENTATION = "groovy.formatter.multiline.indentation";

    public static final String GROOVY_FORMATTER_BRACES_START = "groovy.formatter.braces.start";
    public static final String GROOVY_FORMATTER_BRACES_END = "groovy.formatter.braces.end";

    public static final String GROOVY_FORMATTER_MAX_LINELENGTH = "groovy.formatter.line.maxlength";

    public static final String GROOVY_FORMATTER_REMOVE_UNNECESSARY_SEMICOLONS = "groovy.formatter.remove.unnecessary.semicolons";

    // Save Actions
    public static final String GROOVY_SAVE_ACTION_REMOVE_UNNECESSARY_SEMICOLONS = "groovy.SaveAction.RemoveUnnecessarySemicolons";
}
