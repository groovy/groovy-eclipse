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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-08-09
 */
public class TransformElement {

    private String elementName;

    private String value;

    private List<TransformElementProperty> properties = new ArrayList<TransformElementProperty>();

    private List<TransformElement> children = new ArrayList<TransformElement>();

    public TransformElement(String name, String value) {
        this.elementName = name;
        this.value = value;
    }

    public String getElementName() {
        return elementName;
    }

    public String getValue() {
        return value;
    }

    public boolean addProperty(TransformElementProperty property) {
        return properties.add(property);
    }

    public boolean addProperty(String propertyName, String propertyValue) {
        TransformElementProperty property = new TransformElementProperty(propertyName, propertyValue);
        return addProperty(property);
    }

    public boolean addChild(TransformElement element) {
        return children.add(element);
    }

    /**
     * Returns an unmodifiable copy of the current list
     * 
     * @return
     */
    public List<TransformElementProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    /**
     * Returns an unmodifiable copy of the current list
     * 
     * @return
     */
    public List<TransformElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

}
