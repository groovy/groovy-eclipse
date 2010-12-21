/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codeassist.tests;




/**
 * @author Andrew Eisenberg
 * @created Dec 20, 2010
 *
 * Tests completions of generic lists, maps, etc.
 */
public class GenericCompletionTests extends CompletionTestCase {


    public GenericCompletionTests(String name) {
        super(name);
    }

    public void testAfterArrayAccesses1() throws Exception {
        String contents = "Map<String, Map<Integer, List<Date>>> dataTyped\ndataTyped      ['foo'].c\nj";
        String expected = "Map<String, Map<Integer, List<Date>>> dataTyped\ndataTyped      ['foo'].clear()\nj";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "['foo'].c"), "clear()");
    }
    public void testAfterArrayAccesses2() throws Exception {
        String contents = "Map<String, Map<Integer, List<Date>>> dataTyped\ndataTyped      ['foo'].";
        String expected = "Map<String, Map<Integer, List<Date>>> dataTyped\ndataTyped      ['foo'].clear()";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "['foo']."), "clear()");
    }

    public void testAfterMultipleArrayAccesses1() throws Exception {
        String contents = "Map<String, Map<Integer, List<Date>>> dataTyped\ndataTyped      ['foo'][5][2].t";
        String expected = "Map<String, Map<Integer, List<Date>>> dataTyped\ndataTyped      ['foo'][5][2].time";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "['foo'][5][2].t"), "time");
    }
    public void testAfterMultipleArrayAccesses2() throws Exception {
        String contents = "Map<String, Map<Integer, List<Date>>> dataTyped\ndataTyped      ['foo'][5][2].";
        String expected = "Map<String, Map<Integer, List<Date>>> dataTyped\ndataTyped      ['foo'][5][2].time";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "['foo'][5][2]."), "time");
    }
}
