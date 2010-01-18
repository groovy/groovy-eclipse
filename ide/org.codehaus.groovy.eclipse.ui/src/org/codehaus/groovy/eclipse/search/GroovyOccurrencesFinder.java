/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.search;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.search.IOccurrencesFinder;

/**
 * 
 * @author Andrew Eisenberg
 * @created Dec 31, 2009
 */
public class GroovyOccurrencesFinder implements IOccurrencesFinder {
    public static final String ID= "GroovyOccurrencesFinder"; //$NON-NLS-1$

    public static final String IS_WRITEACCESS= "writeAccess"; //$NON-NLS-1$
    public static final String IS_VARIABLE= "variable"; //$NON-NLS-1$

    private final GroovyCompilationUnit gunit;
    
    private CompilationUnit cunit;
    
    private final int offset;
    private final int length;
    
    public GroovyOccurrencesFinder(GroovyCompilationUnit gunit, int offset, int length) {
        this.gunit = gunit;
        this.offset = offset;
        this.length = length;
    }

    public CompilationUnit getASTRoot() {
        return null;
    }

    public String getElementName() {
        return "";
    }

    public String getID() {
        return ID;
    }

    public String getJobLabel() {
        return "Search for Occurrences in File (Groovy)";
    }

    public OccurrenceLocation[] getOccurrences() {
        return null;
    }

    public int getSearchKind() {
        return K_OCCURRENCE;
    }

    public String getUnformattedPluralLabel() {
        return "''{0}'' - {1} occurrences in ''{2}''";
    }

    public String getUnformattedSingularLabel() {
        return "''{0}'' - 1 occurrence in ''{1}''";
    }

    public String initialize(CompilationUnit root, ASTNode node) {
        cunit = root;
        return null;
    }

    public String initialize(CompilationUnit root, int offset, int length) {
        cunit = root;
        return null;
    }

}
