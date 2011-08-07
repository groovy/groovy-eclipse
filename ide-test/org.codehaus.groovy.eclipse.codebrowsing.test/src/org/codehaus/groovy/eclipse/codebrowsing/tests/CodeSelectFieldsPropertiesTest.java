/*******************************************************************************
 * Copyright (c) 2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codebrowsing.tests;


/**
 * @author Andrew Eisenberg
 * @created Apr 29, 2011
 *
 */
//@formatter:off
public class CodeSelectFieldsPropertiesTest extends BrowsingTestCase {

    public CodeSelectFieldsPropertiesTest() {
        super(CodeSelectFieldsPropertiesTest.class.getName());
    }

    // GRECLIPSE-1042
    public void testGettersAndField1() throws Exception {
        createUnit("Other",
                "class Other {\n" +
                "  String xxx\n" +
                "  public getXxx() { xxx }\n" +
                "}");

        String contents = "new Other().xxx";
        String toFind = "xxx";
        String elementName = toFind;
        assertCodeSelect(null, null, contents, toFind, elementName);

    }

    // GRECLIPSE-1042
    public void testGettersAndField2() throws Exception {
        createUnit("Other",
                "class Other {\n" +
                "  String xxx\n" +
                "  public getXxx() { xxx }\n" +
        "}");

        String contents = "new Other().getXxx()";
        String toFind = "getXxx";
        String elementName = toFind;
        assertCodeSelect(null, null, contents, toFind, elementName);
    }

    // GRECLIPSE-1042
    public void testGettersAndField3() throws Exception {
        createUnit("Other",
                "class Other {\n" +
                "  String xxx\n" +
        "}");

        String contents = "new Other().getXxx()";
        String toFind = "getXxx";
        String elementName = "xxx";
        assertCodeSelect(null, null, contents, toFind, elementName);
    }


    // GRECLIPSE-1042
    public void testGettersAndField4() throws Exception {
        createUnit("Other",
                "class Other {\n" +
                "  public getXxx() { xxx }\n" +
                "}");

        String contents = "new Other().xxx";
        String toFind = "xxx";
        String elementName = "getXxx";
        assertCodeSelect(null, null, contents, toFind, elementName);
    }

    // GRECLIPSE-1042
    public void testGettersAndField5() throws Exception {
        String contents = "class Other {\n" +
        "  String xxx\n" +
        "  public getXxx() { xxx }\n" +
        "}\n" +
        "new Other().xxx";
        String toFind = "xxx";
        String elementName = toFind;
        assertCodeSelect(null, null, contents, toFind, elementName);
    }

    // GRECLIPSE-1042
    public void testGettersAndField6() throws Exception {
        String contents = "class Other {\n" +
        "  String xxx\n" +
        "  public getXxx() { xxx }\n" +
        "}\n" +
        "new Other().getXxx";
        String toFind = "getXxx";
        String elementName = toFind;
        assertCodeSelect(null, null, contents, toFind, elementName);
    }
    // GRECLIPSE-1042
    public void testGettersAndField7() throws Exception {
        String contents = "class Other {\n" +
        "  public getXxx() { xxx }\n" +
        "}\n" +
        "new Other().xxx";
        String toFind = "xxx";
        String elementName = "getXxx";
        assertCodeSelect(null, null, contents, toFind, elementName);
    }

    // GRECLIPSE-1042
    public void testGettersAndField8() throws Exception {
        String contents = "class Other {\n" +
        "  String xxx\n" +
        "}\n" +
        "new Other().getXxx";
        String toFind = "getXxx";
        String elementName = "xxx";
        assertCodeSelect(null, null, contents, toFind, elementName);
    }

    // GRECLIPSE-1162
    public void testIsGetter1() throws Exception {
        String contents = "class Other {\n" +
                "  boolean xxx\n" +
                "}\n" +
                "new Other().isXxx";
        String toFind = "isXxx";
        String elementName = "xxx";
        assertCodeSelect(null, null, contents, toFind, elementName);
    }
}