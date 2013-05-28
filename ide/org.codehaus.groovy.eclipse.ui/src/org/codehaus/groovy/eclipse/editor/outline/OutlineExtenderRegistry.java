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
package org.codehaus.groovy.eclipse.editor.outline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.editor.GroovyTextTools;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Manages the OutlineExtenderRegistry
 * This class is a singleton and is managed by {@link GroovyTextTools}
 * @author Maxime Hamm
 * @created April 4, 2011
 */
public class OutlineExtenderRegistry {

    public static final String EXTENSION_POINT = "org.codehaus.groovy.eclipse.ui.outlineExtension";
    public static final String NATURE_ID = "natureID";
    public static final String EXTENDER = "extender";

    Map<String, List<IOutlineExtender>> natureToExtenderMap;

    GroovyScriptOutlineExtender groovyScriptExtender;

    public void initialize() throws CoreException {
        natureToExtenderMap = new HashMap<String, List<IOutlineExtender>>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension ext : extensions) {
            IConfigurationElement[] configElts = ext.getConfigurationElements();
            for (IConfigurationElement elt : configElts) {
                String natureID = elt.getAttribute(NATURE_ID);
                Object object = WorkbenchPlugin.createExtension(elt, EXTENDER);
                if (object instanceof IOutlineExtender) {
                    List<IOutlineExtender> extenders = natureToExtenderMap.get(natureID);
                    if (extenders == null) {
                        extenders = new ArrayList<IOutlineExtender>(1);
                        natureToExtenderMap.put(natureID, extenders);
                    }
                    extenders.add((IOutlineExtender) object);
                }
            }
        }
        groovyScriptExtender = new GroovyScriptOutlineExtender();
    }

    public List<IOutlineExtender> getExtenders(String natureID) {
        return natureToExtenderMap.get(natureID);
    }

    public GroovyOutlinePage getGroovyOutlinePageForEditor(IProject project, String contextMenuID, GroovyEditor editor) throws CoreException {
        if (project == null || !project.isAccessible()) {
            return null;
        }
        GroovyOutlinePage groovyOutlinePage = null;
        String[] natureIds = project.getDescription().getNatureIds();
        outer: for (String natureId : natureIds) {
            List<IOutlineExtender> extenders = getExtenders(natureId);
            if (extenders != null) {
                for (IOutlineExtender extender : extenders) {
                    if (extender.appliesTo(editor.getGroovyCompilationUnit())) {
                        GroovyOutlinePage page = extender.getGroovyOutlinePageForEditor(contextMenuID, editor);
                        if (page != null) {
                            groovyOutlinePage = page;
                            break outer;
                        }
                    }
                }
            }
        }

        if (groovyOutlinePage != null) {
            return groovyOutlinePage;
        } else {
            // Hard code this last extender since it should only apply if all
            // other extenders do not
            if (groovyScriptExtender.appliesTo(editor.getGroovyCompilationUnit())) {
                return groovyScriptExtender.getGroovyOutlinePageForEditor(contextMenuID, editor);
            }
        }
        return null;
    }
}
