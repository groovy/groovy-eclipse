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

import java.util.List;

import org.eclipse.jface.text.rules.IRule;

/**
 * @author Maxime HAMM
 * @created Jully 8, 2013
 */
public interface IHighlightingExtender2 extends IHighlightingExtender {

    /**
     * Provides a list of additional highlighting rules that will be
     * triggered in priority
     */
    public List<IRule> getInitialAdditionalRules();

}
