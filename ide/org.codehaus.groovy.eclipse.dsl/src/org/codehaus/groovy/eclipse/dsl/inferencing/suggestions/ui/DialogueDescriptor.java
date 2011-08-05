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
 * @author Nieraj Singh
 * @created 2011-08-05
 */
public class DialogueDescriptor {
    private String message;

    private String title;

    private String iconLocation;

    public DialogueDescriptor(String message, String title, String iconLocation) {
        super();
        this.message = message;
        this.title = title;
        this.iconLocation = iconLocation;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public String getIconLocation() {
        return iconLocation;
    }

}
