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
package org.codehaus.groovy.eclipse.dsl.classpath;

import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class handles the automatic adding of the classpath container.
 * 
 * Classpath container is automatically added at the following times:
 * 
 * <ol>
 * <li>On startup</li>
 * <li>Whenever a new Groovy project has been added to the workspace</li>
 * <li>Whenever Groovy nature has been added to an existing project</li>
 * <li>Whenever Groovy project is opened</li>
 * </ol>
 * 
 * @author andrew
 * @created May 25, 2011
 */
public class AutoAddClasspathContainer {

    private final IPreferenceStore store = GroovyDSLCoreActivator.getDefault().getPreferenceStore();
    
    
    
    
    
    private boolean shouldAutoAdd() {
        return store.getBoolean(DSLPreferencesInitializer.AUTO_ADD_DSL_SUPPORT);
    }
    
}
