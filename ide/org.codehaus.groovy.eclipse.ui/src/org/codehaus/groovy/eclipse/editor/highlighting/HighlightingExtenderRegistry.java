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

package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.editor.GroovyTextTools;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Manages the HighlightingExtenderRegistry
 * This class is a singleton and is managed by {@link GroovyTextTools}
 * @author Andrew Eisenberg
 * @created Oct 23, 2009
 */
public class HighlightingExtenderRegistry {

    public static final String EXTENSION_POINT = "org.codehaus.groovy.eclipse.ui.syntaxHighlightingExtension";
    public static final String NATURE_ID = "natureID";
    public static final String EXTENDER = "extender";

    Map<String, IHighlightingExtender> natureToExtenderMap;

    public void initialize() throws CoreException {
        natureToExtenderMap = new HashMap<String, IHighlightingExtender>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension ext : extensions) {
            IConfigurationElement[] configElts = ext.getConfigurationElements();
            for (IConfigurationElement elt : configElts) {
                String natureid = elt.getAttribute(NATURE_ID);
                Object object = WorkbenchPlugin.createExtension(elt, EXTENDER);
                if (object instanceof IHighlightingExtender) {
                    natureToExtenderMap.put(natureid, (IHighlightingExtender) object);
                }
            }
        }
    }

    public IHighlightingExtender getExtender(String natureID) {
        return natureToExtenderMap.get(natureID);
    }

    public List<String> getExtraGroovyKeywordsForProject(IProject project) throws CoreException {
        return internalGetExtraKeywordsForProject(project, false);
    }

    public List<String> getExtraGJDKKeywordsForProject(IProject project) throws CoreException {
        return internalGetExtraKeywordsForProject(project, true);
    }

    public List<String> internalGetExtraKeywordsForProject(IProject project, boolean isGJDK) throws CoreException {
        if (project == null) {
            return null;
        }
        List<String> extraKeywords = new ArrayList<String>();
        String[] natureIds = project.getDescription().getNatureIds();
        for (String natureId : natureIds) {
            IHighlightingExtender extender = getExtender(natureId);
            if (extender != null) {
                List<String> keywords = isGJDK ? extender.getAdditionalGJDKKeywords() : extender.getAdditionalGroovyKeywords();
                if (keywords != null) {
                    extraKeywords.addAll(keywords);
                }
            }
        }
        return extraKeywords;
    }

    public List<IRule> getAdditionalRulesForProject(IProject project) throws CoreException {
        if (project == null) {
            return null;
        }
        List<IRule> extraRules = new ArrayList<IRule>();
        String[] natureIds = project.getDescription().getNatureIds();
        for (String natureId : natureIds) {
            IHighlightingExtender extender = getExtender(natureId);
            if (extender != null) {
                List<IRule> rules = extender.getAdditionalRules();
                if (rules != null) {
                    extraRules.addAll(rules);
                }
            }
        }
        return extraRules;
    }

    public List<IRule> getInitialAdditionalRulesForProject(IProject project) throws CoreException {
        if (project == null) {
            return null;
        }
        List<IRule> extraRules = new ArrayList<IRule>();
        String[] natureIds = project.getDescription().getNatureIds();
        for (String natureId : natureIds) {
            IHighlightingExtender extender = getExtender(natureId);
            if (extender != null && (extender instanceof IHighlightingExtender2)) {
                List<IRule> rules = ((IHighlightingExtender2)extender).getInitialAdditionalRules();
                if (rules != null) {
                    extraRules.addAll(rules);
                }
            }
        }
        return extraRules;
    }
}
