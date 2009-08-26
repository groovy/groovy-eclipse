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
package org.codehaus.groovy.eclipse.test.actions;

import org.codehaus.groovy.eclipse.refactoring.actions.DelegatingCleanUpPostSaveListener;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.IPostSaveListener;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.SaveParticipantRegistry;

/**
 * Tests that the {@link DelegatingCleanUpPostSaveListener} can be properly 
 * installed and uninstalled
 * 
 * @author Andrew Eisenberg
 * @created Aug 26, 2009
 *
 */
public class SaveParticipantRegistryTest extends EclipseTestCase {
	public void testInstallUninstallPostSaveCleanUp() throws Exception {
	    SaveParticipantRegistry registry = JavaPlugin.getDefault().getSaveParticipantRegistry();
	    
	    // the groovy plugin is already started, so we should have the delegating clean up
	    IPostSaveListener listener = registry.getSaveParticipantDescriptor(CleanUpPostSaveListener.POSTSAVELISTENER_ID).getPostSaveListener();
	    assertTrue("Should have found a DelegatingCleanUpPostSaveListener, but instead found " + 
	            listener.getClass().getCanonicalName() + " this means that the delegating listener was not installed.",
	            listener instanceof DelegatingCleanUpPostSaveListener);
	    
	    try {
	        DelegatingCleanUpPostSaveListener.installCleanUp();
	    } catch (ClassCastException e) {
	        fail("Should not have thrown a class cast exception, but an error should be logged in the error log");
	    }
	    
	    DelegatingCleanUpPostSaveListener.uninstallCleanUp();
	    listener = registry.getSaveParticipantDescriptor(CleanUpPostSaveListener.POSTSAVELISTENER_ID).getPostSaveListener();
        assertTrue("Should have found a DelegatingCleanUpPostSaveListener, but instead found " + 
                listener.getClass().getCanonicalName() + " this means that the delegating listener was not uninstalled.",
                listener instanceof CleanUpPostSaveListener);
	    
        
        DelegatingCleanUpPostSaveListener.installCleanUp();
        listener = registry.getSaveParticipantDescriptor(CleanUpPostSaveListener.POSTSAVELISTENER_ID).getPostSaveListener();
        assertTrue("Should have found a DelegatingCleanUpPostSaveListener, but instead found " + 
                listener.getClass().getCanonicalName() + " this means that the delegating listener was not installed.",
                listener instanceof DelegatingCleanUpPostSaveListener);
    }
}
