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

import org.codehaus.groovy.eclipse.editor.GroovyEditor
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IExtensionPoint
import org.eclipse.core.runtime.IExtensionRegistry
import org.eclipse.core.runtime.Platform
import org.eclipse.ui.internal.WorkbenchPlugin

/**
 * Manages the OutlineExtenderRegistry
 * This class is a singleton and is managed by {@link GroovyTextTools}
 * @author Maxime Hamm
 * @created April 4, 2011
 */
public class OutlineExtenderRegistry {

    public static final String EXTENSION_POINT = 'org.codehaus.groovy.eclipse.ui.outlineExtension'
    public static final String NATURE_ID = 'natureID'
    public static final String EXTENDER = 'extender'

    def natureToExtenderMap

    GroovyScriptOutlineExtender groovyScriptExtender

    def initialize() {
        natureToExtenderMap = [:]
        IExtensionRegistry registry = Platform.getExtensionRegistry()
        IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT)
        extensionPoint.extensions.each {
            it.configurationElements.each {
                def natureid = it.getAttribute(NATURE_ID)
                Object object = WorkbenchPlugin.createExtension(it, EXTENDER)
                if (object instanceof IOutlineExtender) {
                    if (!natureToExtenderMap."${natureid}") {
                        natureToExtenderMap."${natureid}" = []
                    }
                    natureToExtenderMap."${natureid}" << object
                }
            }
        }
        groovyScriptExtender = new GroovyScriptOutlineExtender()
    }

    List<IOutlineExtender> getExtenders(String natureID) {
        natureToExtenderMap."${natureID}"
    }

    GroovyOutlinePage getGroovyOutlinePageForEditor(IProject project, String contextMenuID, GroovyEditor editor) {
        if (!project || !project.isAccessible()) {
            return
        }
        def groovyOutlinePage
        project.description.natureIds.each {
            List<IOutlineExtender> extenders = getExtenders(it)
            if (extenders) {
                for (extender in extenders) {
                    if (extender.appliesTo(editor.getGroovyCompilationUnit())) {
                        def page = extender.getGroovyOutlinePageForEditor(contextMenuID, editor)
                        if (page != null) {
                            groovyOutlinePage = page
                            return
                        }
                    }
                }
            }
        }
        if (groovyOutlinePage != null) {
            return groovyOutlinePage
        } else {
            // Hard code this last extender since it should only apply if all
            // other extenders do not
            if (groovyScriptExtender.appliesTo(editor.getGroovyCompilationUnit())) {
                return groovyScriptExtender.getGroovyOutlinePageForEditor(contextMenuID, editor)
            }
        }
    }
}
