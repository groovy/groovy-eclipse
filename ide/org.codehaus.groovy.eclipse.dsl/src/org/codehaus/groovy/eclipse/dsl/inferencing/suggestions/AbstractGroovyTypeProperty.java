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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

/**
 * 
 * @author Nieraj Singh
 * @created Apr 19, 2011
 */
public abstract class AbstractGroovyTypeProperty implements IGroovySuggestion {
    private String name;

    private String type;

    private boolean isStatic;

    private String javaDoc;

    private boolean isActive;

    private GroovySuggestionDeclaringType declaringType;

    protected AbstractGroovyTypeProperty(GroovySuggestionDeclaringType declaringType, String name, String type, boolean isStatic,
            String javaDoc, boolean isActive) {
        this.name = name;
        this.type = type;
        this.isStatic = isStatic;
        this.javaDoc = javaDoc;
        this.declaringType = declaringType;
        this.isActive = isActive;
    }

    public GroovySuggestionDeclaringType getDeclaringType() {
        return declaringType;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getJavaDoc() {
        return javaDoc;
    }

    public void changeActiveState(boolean isActive) {
        this.isActive = isActive;
    }

}
