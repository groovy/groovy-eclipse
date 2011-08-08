/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * Manage a set of related controls displayed in a dialogue. In particular, the
 * disabled and enabled state of
 * the set of controls is managed based on events generated external to the
 * manager. This allows all controls in the manager
 * to be enabled or disabled based on events driven by other parts of a dialogue
 * or underlying model or functionality.
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public interface IDialogueControlManager {

    public Composite createControlArea(Composite parent);

    public void setEnabled(boolean disable);

    public void addSelectionListener(IControlSelectionListener listener);

}
