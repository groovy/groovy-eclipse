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
 * @created 2011-09-12
 */
public class GroovySuggestion implements IGroovySuggestion {

    protected String name;

    protected String type;

    protected boolean isStatic;

    protected String javaDoc;

    protected boolean isActive;

    protected GroovySuggestionDeclaringType declaringType;

    public GroovySuggestionDeclaringType getDeclaringType() {
        return declaringType;
    }

    public GroovySuggestion(GroovySuggestionDeclaringType declaringType, String name, String type, boolean isStatic,
            String javaDoc, boolean isActive) {
        this.name = name;
        this.type = type;
        this.isStatic = isStatic;
        this.javaDoc = javaDoc;
        this.isActive = isActive;
        this.declaringType = declaringType;
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

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((declaringType == null) ? 0 : declaringType.hashCode());
        result = prime * result + (isActive ? 1231 : 1237);
        result = prime * result + (isStatic ? 1231 : 1237);
        result = prime * result + ((javaDoc == null) ? 0 : javaDoc.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroovySuggestion other = (GroovySuggestion) obj;
        if (declaringType == null) {
            if (other.declaringType != null)
                return false;
        } else if (!declaringType.equals(other.declaringType))
            return false;
        if (isActive != other.isActive)
            return false;
        if (isStatic != other.isStatic)
            return false;
        if (javaDoc == null) {
            if (other.javaDoc != null)
                return false;
        } else if (!javaDoc.equals(other.javaDoc))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}