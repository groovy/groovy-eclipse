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
package org.codehaus.groovy.eclipse.dsl;

import java.util.Set;

/**
 * 
 * @author andrew
 * @created Feb 28, 2011
 */
public class DisabledScriptsCache {

    private Set<String> disabled;
    
    public Set<String> getDisabled() {
        ensureInitialized();
        return disabled;
    }
    
    public boolean isDisabled(String script) {
        ensureInitialized();
        return disabled.contains(script);
    }
    
    public void setDisabled(Set<String> disabled) {
        this.disabled = disabled;
        DSLPreferences.setDisabledScripts(disabled.toArray(new String[0]));
    }
    
    private void ensureInitialized() {
        if (disabled == null) {
            disabled = DSLPreferences.getDisabledScriptsAsSet();
        }
    }
}
