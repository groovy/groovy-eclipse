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
package org.codehaus.groovy.eclipse.dsl.contexts;

import org.codehaus.groovy.eclipse.dsl.script.EmptyResult;
import org.codehaus.groovy.eclipse.dsl.script.IContextQuery;
import org.codehaus.groovy.eclipse.dsl.script.IContextQueryResult;


/**
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class Context implements IContext {

    
    private String fileNamePattern;
    private IContextQuery targetTypeQuery;
    private String[] fileExtensions;
    private IContextQuery scope;
    
    private final String containerIdentifier;
    
    public Context(String containerIdentifier) {
        this.containerIdentifier = containerIdentifier;
    }
    
    public IContextQueryResult<?> matches(ContextPattern pattern) {
        if (pattern.fileName != null) {
            if (fileNamePattern != null) {
                if (! pattern.fileName.matches(fileNamePattern)) {
                    return EmptyResult.INSTANCE;
                }
            }
            if (fileExtensions != null) {
                boolean found = false;
                for (String ext : fileExtensions) {
                    if (pattern.fileName.endsWith(ext)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return EmptyResult.INSTANCE;
                }
            }
        }

        // a null result means that this part was not activated.
        // an empty result means there was no match
        IContextQueryResult<?> scopeQueryResult = scope == null ? null : scope.evaluate(null, pattern.currentScope);
        if (scopeQueryResult == EmptyResult.INSTANCE) {
            return EmptyResult.INSTANCE;
        }
        IContextQueryResult<?> typeQueryResult = pattern.matchesType(targetTypeQuery);
        if (typeQueryResult == EmptyResult.INSTANCE) {
            return EmptyResult.INSTANCE;
        } 
        
        return typeQueryResult;
    }

    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }


    public void setTargetTypeQuery(IContextQuery targetTypeQuery) {
        this.targetTypeQuery = targetTypeQuery;
    }


    public void setFileExtensions(String[] fileExtensions) {
        if (fileExtensions != null) {
            this.fileExtensions = new String[fileExtensions.length];
            for (int i = 0; i < fileExtensions.length; i++) {
                if (fileExtensions[i] != null && fileExtensions[i].length() > 0 
                        && fileExtensions[i].charAt(0) != '.') {
                    this.fileExtensions[i] = "." + fileExtensions;
                } else {
                    this.fileExtensions[i] = fileExtensions[i];
                }
            }
        } else {
            this.fileExtensions = null;
        }
    }

    
    public void setScope(IContextQuery scope) {
        this.scope = scope;
    }

    public String getContainerIdentifier() {
        return containerIdentifier;
    }
}
