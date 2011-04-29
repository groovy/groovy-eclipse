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
package org.eclipse.jdt.core.groovy.tests.search;

/**
 * Tests that the inferred declaration is correct
 * @author Andrew Eisenberg
 * @created Apr 28, 2011
 */
public class DeclarationInferencingTests extends AbstractInferencingTest {

    
    public DeclarationInferencingTests(String name) {
        super(name);
    }

    // GRECLIPSE-1042
    public void testGettersAndField1() throws Exception {
        createUnit("Other", 
                "class Other {\n" +
                "  String xxx\n" +
                "  public getXxx() { xxx }\n" +
                "}");
        
        String contents = "new Other().xxx";
        int start =  contents.indexOf("xxx");
        int end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.PROPERTY);
    }

    // GRECLIPSE-1042
    public void testGettersAndField2() throws Exception {
        createUnit("Other", 
                "class Other {\n" +
                "  String xxx\n" +
                "  public getXxx() { xxx }\n" +
        "}");
        
        String contents = "new Other().getXxx";
        int start =  contents.indexOf("getXxx");
        int end = start + "getXxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }
    
    // GRECLIPSE-1042
    public void testGettersAndField3() throws Exception {
        createUnit("Other", 
                "class Other {\n" +
                "  String xxx\n" +
        "}");
        
        String contents = "new Other().getXxx";
        int start =  contents.indexOf("getXxx");
        int end = start + "getXxx".length();
        assertDeclaration(contents, start, end, "Other", "getXxx", DeclarationKind.METHOD);
    }
    
    
    // GRECLIPSE-1042
    public void testGettersAndField4() throws Exception {
        createUnit("Other", 
                "class Other {\n" +
                "  public getXxx() { xxx }\n" +
                "}");
        
        String contents = "new Other().xxx";
        int start =  contents.indexOf("xxx");
        int end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.PROPERTY);
    }
    
    // GRECLIPSE-1042
    public void testGettersAndField5() throws Exception {
        createUnit("Other", 
                "class Other {\n" +
                "  String xxx\n" +
                "  public getXxx() { xxx }\n" +
                "}");
        
        String contents = "class Other {\n" +
        "  String xxx\n" +
        "  public getXxx() { xxx }\n" +
        "}\n" + 
        "new Other().xxx";
        int start =  contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.PROPERTY);
    }

    // GRECLIPSE-1042
    public void testGettersAndField6() throws Exception {
        createUnit("Other", 
                "class Other {\n" +
                "  public String xxx\n" +
                "  public getXxx() { xxx }\n" +
        "}");
        
        String contents = "class Other {\n" +
        "  String xxx\n" +
        "  public getXxx() { xxx }\n" +
        "}\n" + 
        "new Other().xxx";
        int start =  contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertDeclaration(contents, start, end, "Other", "xxx", DeclarationKind.PROPERTY);
    }
    

}
