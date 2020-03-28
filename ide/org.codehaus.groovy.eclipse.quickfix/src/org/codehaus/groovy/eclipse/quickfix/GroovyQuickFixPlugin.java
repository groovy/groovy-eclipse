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
package org.codehaus.groovy.eclipse.quickfix;

import java.io.IOException;
import java.util.Optional;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.service.prefs.BackingStoreException;

public class GroovyQuickFixPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.quickfix";

    public static final String GROOVY_CONTEXT_TYPE = "groovy";

    private static GroovyQuickFixPlugin plugin;

    public static GroovyQuickFixPlugin getDefault() {
        return plugin;
    }

    public static void logWarning(final String message, final Throwable exception) {
        log(createWarningStatus(message, exception));
    }

    public static void log(final String message, final Throwable exception) {
        IStatus status = createErrorStatus(message, exception);
        log(status);
    }

    public static void log(final Throwable exception) {
        log(createErrorStatus("Internal Error", exception));
    }

    public static void log(final IStatus status) {
        getDefault().getLog().log(status);
    }

    public static IStatus createErrorStatus(String message, final Throwable exception) {
        if (message == null) {
            message = "";
        }
        return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
    }

    public static IStatus createWarningStatus(String message, final Throwable exception) {
        if (message == null) {
            message = "";
        }
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, exception);
    }

    //--------------------------------------------------------------------------

    private TemplateStore templateStore;

    private ContextTypeRegistry contextTypeRegistry;

    public GroovyQuickFixPlugin() {
        plugin = this;
    }

    /**
     * Returns the template store for the jsp editor templates.
     *
     * @return the template store for the jsp editor templates
     */
    public TemplateStore getTemplateStore() {
        if (templateStore == null) {
            templateStore = new ContributionTemplateStore(getTemplateContextRegistry(), getPreferenceStore(), GROOVY_CONTEXT_TYPE);
            try {
                templateStore.load();
            } catch (IOException e) {
                log(e);
            }
        }
        return templateStore;
    }

    /**
     * Returns the template context type registry for the jsp plugin.
     *
     * @return the template context type registry for the jsp plugin
     */
    public ContextTypeRegistry getTemplateContextRegistry() {
        if (contextTypeRegistry == null) {
            ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();
            registry.addContextType(GROOVY_CONTEXT_TYPE);
            contextTypeRegistry = registry;
        }
        return contextTypeRegistry;
    }

    public static boolean isGroovyProject(final ICompilationUnit unit) {
        return Optional.ofNullable(unit).map(ICompilationUnit::getResource).map(IResource::getProject)
            .filter(project -> project.isAccessible() && GroovyNature.hasGroovyNature(project)).isPresent();
    }

    public static boolean isGroovyProject(final IInvocationContext context) {
        return Optional.ofNullable(context).map(IInvocationContext::getCompilationUnit).filter(GroovyQuickFixPlugin::isGroovyProject).isPresent();
    }

    public void savePreferences() {
        try {
            InstanceScope.INSTANCE.getNode(PLUGIN_ID).flush();
        } catch (BackingStoreException e) {
            log(e);
        }
    }
}
