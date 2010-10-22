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
package org.codehaus.groovy.eclipse.test.ui;

import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.DEPRECATED;
import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.FIELD;
import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.REGEX;
import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.STATIC;
import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.UNKNOWN;

import java.util.Arrays;
import java.util.Comparator;

import org.codehaus.groovy.eclipse.editor.highlighting.GatherSemanticReferences;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PartInitException;

/**
 * @author Andrew Eisenberg
 * @created Oct 22, 2010
 */
public class SemanticHighlightingTests extends EclipseTestCase {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testProject.addNature(GroovyNature.GROOVY_NATURE);
        testProject.createJavaTypeAndPackage("other", "Java.java", "public @Deprecated class Java { \n @Deprecated public static final String CONST = \"\"; }");
    }
    
    public void testStaticFieldRanges() throws Exception {
        String contents = "class X { static FOO }";
        assertHighlighting(contents, 
                new HighlightedTypedPosition(contents.indexOf("FOO"), "FOO".length(), FIELD),
                new HighlightedTypedPosition(contents.indexOf("FOO"), "FOO".length(), STATIC));
    }
    
    public void testStaticMethodRanges() throws Exception {
        String contents = "class X { static FOO() {} }";
        assertHighlighting(contents, 
                new HighlightedTypedPosition(contents.indexOf("FOO"), "FOO".length(), STATIC));
    }
    
    public void testRegex() throws Exception {
        String contents = "/fdsfasdfas/";
        assertHighlighting(contents, 
                new HighlightedTypedPosition(contents.indexOf("/fdsfasdfas/"), "/fdsfasdfas/".length(), REGEX));
    }
    
    public void testUnknown() throws Exception {
        String contents = "unknown";
        assertHighlighting(contents, 
                new HighlightedTypedPosition(contents.indexOf("unknown"), "unknown".length(), UNKNOWN));
    }
    
    public void testDeprecated() throws Exception {
        String contents = "import other.Java\nJava.CONST";
        assertHighlighting(contents, 
                new HighlightedTypedPosition(contents.indexOf("Java"), "Java".length(), DEPRECATED),
                new HighlightedTypedPosition(contents.lastIndexOf("Java"), "Java".length(), DEPRECATED),
                new HighlightedTypedPosition(contents.indexOf("CONST"), "CONST".length(), DEPRECATED),
                new HighlightedTypedPosition(contents.indexOf("CONST"), "CONST".length(), FIELD),
                new HighlightedTypedPosition(contents.indexOf("CONST"), "CONST".length(), STATIC));
    }
    
    
    private void assertHighlighting(String contents, HighlightedTypedPosition... expectedPositions) throws Exception {
        GroovyCompilationUnit unit = openFile(contents);
        checkStyles(unit, expectedPositions);
    }



    /**
     * @param contents
     * @return
     * @throws CoreException
     * @throws PartInitException
     */
    private GroovyCompilationUnit openFile(String contents) throws CoreException,
            PartInitException {
        IFile file = testProject.createGroovyTypeAndPackage("", "Highlighting.groovy", contents);
        return (GroovyCompilationUnit) JavaCore.create(file);
    }

    /**
     * @param text
     */
    private void checkStyles(GroovyCompilationUnit unit, HighlightedTypedPosition[] expectedPositions) {
        GatherSemanticReferences references = new GatherSemanticReferences(unit);
        HighlightedTypedPosition[] actualPositions = (HighlightedTypedPosition[]) references.findSemanticHighlightingReferences().toArray(new HighlightedTypedPosition[0]);
        Arrays.sort(actualPositions, new Comparator<HighlightedTypedPosition>() {
            public int compare(HighlightedTypedPosition h1, HighlightedTypedPosition h2) {
                if (h1.offset == h2.offset) {
                    return h1.kind.ordinal() - h2.kind.ordinal();
                }
                return h1.offset - h2.offset;
            }
        });
        assertEquals("All expected:\n" + Arrays.toString(expectedPositions) + "\n\nAllActual:\n" + Arrays.toString(actualPositions), expectedPositions.length, actualPositions.length);
        for (int i = 0; i < actualPositions.length; i++) {
            assertEquals("Should have had equal positions.\nAll expected:\n" + Arrays.toString(expectedPositions) + "\n\nAllActual:\n" + Arrays.toString(actualPositions), expectedPositions[i], actualPositions[i]);
        }
    }
}

