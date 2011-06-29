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
 *
 */
package org.codehaus.groovy.eclipse.refactoring.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.FilePartReader;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;

/**
 * @autor Michael Klenk mklenk@hsr.ch
 */

public abstract class BaseTestCase extends TestCase {

    protected File file;

    private final String newLine;

    private final Pattern origRegExp, expRegExp, propertiesRegExp;

    private final String name;

    private final IDocument doc;

    protected boolean shouldFail = false;

    protected HashMap<String, String> properties;

    protected TextSelection selection;

    protected BaseTestCase(String name, File fileToTest) {
        setFile(fileToTest);
        this.name = name;
        newLine = FilePartReader.getLineDelimiter(file);
        origRegExp = Pattern.compile("###src" + newLine + "(.*)" + newLine + "###exp", Pattern.DOTALL);
        expRegExp = Pattern.compile("###exp" + newLine + "(.*)" + newLine + "###end", Pattern.DOTALL);
        propertiesRegExp = Pattern.compile("###prop" + newLine + "(.*)" + newLine + "###src", Pattern.DOTALL);
        properties = getFileProperties();
        doc = getOrigin();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("------------------------------");
        System.out.println("Starting: " + getName());

        selection = getUserSelection();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected void setFile(File file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return name;
    }

    /*
     * Return a String with the origin source code
     */
    public IDocument getOrigin() {
        return getArea(origRegExp);
    }

    /*
     * Return a String with the expected content
     */
    public IDocument getExpected() {
        return getArea(expRegExp);
    }

    /*
     * returns a certain area of a textfile
     */
    public IDocument getArea(Pattern regExpression) {
        String filecontent = getContents(file);
        Matcher match = regExpression.matcher(filecontent);
        if (match.find()) {
            String fileContent = match.group(1);
            return ASTTools.getDocumentWithSystemLineBreak(fileContent);
        }
        return new Document();
    }

    /*
     * Returns a user selection
     */
    public TextSelection getUserSelection() {
        try {
            selection = getSelection();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        return selection;
    }

    /**
     * @param properties2
     * @param documentProvider
     * @return
     */
    private TextSelection getSelection() {
        try {
            int startLine = getInt("startLine");
            int startColumn = getInt("startColumn");
            int endLine = getInt("endLine");
            int endColumn = getInt("endColumn");
            IRegion startRegion = getDocument().getLineInformation(startLine - 1);
            int offset = startRegion.getOffset() + startColumn - 1;
            IRegion endRegion = getDocument().getLineInformation(endLine - 1);
            int end = endRegion.getOffset() + endColumn - 1;
            return new TextSelection(offset, end - offset);
        } catch (BadLocationException e) {
            return new TextSelection(0, getDocument().getLength());
        }
    }

    /**
     * @param startLine
     * @return
     */
    private int getInt(String startLine) {
        try {
            return Integer.valueOf(properties.get(startLine));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /*
     * reads properties from the testfiles and puts them into
     * the map "properties"
     */
    public HashMap<String, String> getFileProperties() {

        HashMap<String, String> properties = new HashMap<String, String>();
        Matcher propertiesSection = propertiesRegExp.matcher(getContents(file));
        if (propertiesSection.find()) {
            String[] reults = propertiesSection.group(1).split(FilePartReader.getLineDelimiter(file));
            for (String line : reults) {
                String[] prop = line.split("=");
                if (prop.length != 2)
                    fail("Initialisation of testproperties failed! (${prop})");
                properties.put(prop[0], prop[1]);
            }
            try {
                if (properties.get("shouldFail") != null && properties.get("shouldFail").equals("true"))
                    shouldFail = true;
            } catch (Exception e) {
                fail("Initialisation of testproperties failed! " + e.getMessage());
            }
        }
        return properties;
    }

    public IDocument getDocument() {
        return doc;
    }

    public void finalAssert() {
        String expected = getExpected().get();
        String content = getDocument().get();
        assertEquals("Error in File: " + file + " ", expected, content);
    }

    static public String getContents(File aFile) {
        StringBuilder contents = new StringBuilder();

        try {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line = null; // not declared within while loop
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(FilePartReader.getLineDelimiter(aFile));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();
    }

}
