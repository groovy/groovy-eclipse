/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

/**
 * 
 * 
 * @author Nieraj Singh
 * @created 2011-08-05
 */
public enum ControlTypes implements IDialogueControlDescriptor {
    NAME("Name", "Enter a type name"),

    DECLARING_TYPE("Declaring Type", "Enter or browse for a declaring type"),

    TYPE("Type", "Enter or browse for a type"),

    IS_STATIC("  Is static", "Is the type static"),

    PROPERTY("Property", "Is type a property"),

    METHOD("Method", "Is type a method"),

    PARAMETERS("Parameters", "Add, remove or edit method parameters"),

    USE_NAMED_ARGUMENTS("  Use named arguments", "Should use named arguments?"),

    DOC("Doc", "Enter documentation for the suggestion"),

    ADD("Add", "Add item"),

    REMOVE("Remove", "Remove selected item"),

    EDIT("Edit", "Edit selected item"),

    UP("Up", "Move selected item up"),

    DOWN("Down", "Move selected item down");

    private String label;

    private String toolTipText;

    private ControlTypes(String label, String toolTipText) {
        this.label = label;
        this.toolTipText = toolTipText;
    }

    public String getLabel() {
        return label;
    }

    public String getToolTipText() {
        return toolTipText;
    }

}