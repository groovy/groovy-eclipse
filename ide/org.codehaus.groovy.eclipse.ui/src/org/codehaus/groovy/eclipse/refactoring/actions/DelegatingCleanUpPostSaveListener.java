/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.IPostSaveListener;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.SaveParticipantDescriptor;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.SaveParticipantRegistry;
import org.eclipse.jface.text.IRegion;

/**
 * Delegates to either the {@link org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener} or
 * to {@link GroovyCleanUpPostSaveListener} depending on the content type of the file being saved.
 * <p>
 * Ensures that when groovy compilation units are encountered, post save actions
 * are properly executed.
 */
public class DelegatingCleanUpPostSaveListener extends org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener {

    private final IPostSaveListener jdtCleanUp, groovyCleanUp;

    public DelegatingCleanUpPostSaveListener(final IPostSaveListener jdtCleanUp, final IPostSaveListener groovyCleanUp) {
        this.jdtCleanUp = jdtCleanUp;
        this.groovyCleanUp = groovyCleanUp;
    }

    /**
     * Installs a delegating clean-up by replacing the existing JDT clean-up.
     */
    public static void installCleanUp() {
        try {
            SaveParticipantRegistry registry = JavaPlugin.getDefault().getSaveParticipantRegistry();
            // synchronized because we don't want registry being used in the middle of this.
            synchronized (registry) {
                SaveParticipantDescriptor descriptor = registry.getSaveParticipantDescriptor(POSTSAVELISTENER_ID);
                // descriptor shouldn't be null, but if it is, NPE is thrown and we register the exception in the log.
                // also exception will be thrown if the delegating cleanup was already installed
                org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener jdtCleanUp =
                    (org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener) descriptor.getPostSaveListener();
                GroovyCleanUpPostSaveListener groovyCleanUp = new GroovyCleanUpPostSaveListener();
                IPostSaveListener delegatingCleanUp = new DelegatingCleanUpPostSaveListener(jdtCleanUp, groovyCleanUp);
                ReflectionUtils.setPrivateField(SaveParticipantDescriptor.class, "fPostSaveListener", descriptor, delegatingCleanUp);
            }
        } catch (Exception e) {
            // if an exception is thrown, then the groovy post save listener will not be used.
            GroovyCore.logException("Exception thrown while trying to install GroovyCleanUpPostSaveListener", e);
        }
    }

    /**
     * Uninstalls the delegating clean-up and replaces it with the original JDT clean-up.
     */
    public static void uninstallCleanUp() {
        try {
            SaveParticipantRegistry registry = JavaPlugin.getDefault().getSaveParticipantRegistry();
            // synchronized because we don't want registry being used in the middle of this.
            synchronized (registry) {
                SaveParticipantDescriptor descriptor = registry.getSaveParticipantDescriptor(POSTSAVELISTENER_ID);
                DelegatingCleanUpPostSaveListener delegatingCleanUp = (DelegatingCleanUpPostSaveListener) descriptor.getPostSaveListener();
                ReflectionUtils.setPrivateField(SaveParticipantDescriptor.class, "fPostSaveListener", descriptor, delegatingCleanUp.jdtCleanUp);
            }
        } catch (Exception e) {
            // if an exception is thrown, then the groovy post save listener will not be used.
            GroovyCore.logException("Exception thrown while trying to install GroovyCleanUpPostSaveListener", e);
        }
    }

    @Override
    public String getId() {
        return jdtCleanUp.getId();
    }

    @Override
    public String getName() {
        return jdtCleanUp.getName();
    }

    @Override
    public boolean needsChangedRegions(final ICompilationUnit compilationUnit) throws CoreException {
        if (ContentTypeUtils.isGroovyLikeFileName(compilationUnit.getElementName())) {
            return groovyCleanUp.needsChangedRegions(compilationUnit);
        } else {
            return jdtCleanUp.needsChangedRegions(compilationUnit);
        }
    }

    @Override
    public void saved(final ICompilationUnit compilationUnit, final IRegion[] changedRegions, final IProgressMonitor monitor) throws CoreException {
        if (ContentTypeUtils.isGroovyLikeFileName(compilationUnit.getElementName())) {
            groovyCleanUp.saved(compilationUnit, changedRegions, monitor);
        } else {
            jdtCleanUp.saved(compilationUnit, changedRegions, monitor);
        }
    }
}
