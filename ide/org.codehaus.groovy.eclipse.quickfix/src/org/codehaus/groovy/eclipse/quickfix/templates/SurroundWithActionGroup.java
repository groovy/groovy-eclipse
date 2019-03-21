/*
 * Copyright 2009-2017 the original author or authors.
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
// Copied from org.eclipse.jdt.internal.ui.actions.SurroundWithActionGroup version 3.8.2
/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     AndrewEisenberg - Adapted for Groovy-Eclipse
 *******************************************************************************/

package org.codehaus.groovy.eclipse.quickfix.templates;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.actions.ActionGroup;

public class SurroundWithActionGroup extends ActionGroup {

    private GroovyEditor fEditor;
    private final String fGroup;

    public SurroundWithActionGroup(GroovyEditor editor, String group) {
        fEditor = editor;
        fGroup = group;
    }

    /**
     * The Menu to show when right click on the editor
     * {@inheritDoc}
     */
    @Override
    public void fillContextMenu(IMenuManager menu) {
        ISelectionProvider selectionProvider = fEditor.getSelectionProvider();
        if (selectionProvider == null)
            return;

        ISelection selection = selectionProvider.getSelection();
        if (!(selection instanceof ITextSelection))
            return;

        String menuText = ActionMessages.SurroundWithTemplateMenuAction_SurroundWithTemplateSubMenuName;

        MenuManager subMenu = new MenuManager(menuText, SurroundWithTemplateMenuAction.SURROUND_WITH_QUICK_MENU_ACTION_ID);
        subMenu.setActionDefinitionId(SurroundWithTemplateMenuAction.SURROUND_WITH_QUICK_MENU_ACTION_ID);
        menu.appendToGroup(fGroup, subMenu);
        subMenu.add(new Action() {
        });
        subMenu.addMenuListener(manager -> {
            manager.removeAll();
            SurroundWithTemplateMenuAction.fillMenu(manager, fEditor);
        });
    }
}
