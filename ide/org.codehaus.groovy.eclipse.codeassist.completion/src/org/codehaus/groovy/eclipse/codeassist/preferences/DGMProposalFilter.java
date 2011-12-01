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
package org.codehaus.groovy.eclipse.codeassist.preferences;

import java.util.Set;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssistActivator;

/**
 * Filter for DGM completion proposals. Users specify proposals they want
 * filtered from the preferences page
 * and they no longer show up.
 *
 * @author andrew
 * @created Nov 30, 2011
 */
public class DGMProposalFilter {

    private Set<String> filteredDGMs;

    public DGMProposalFilter() {
        refreshFilter();
    }

    public boolean isFiltered(MethodNode proposal) {
        return filteredDGMs.contains(proposal.getName());
    }

    public void refreshFilter() {
        filteredDGMs = GroovyContentAssistActivator.getDefault().getFilteredDGMs();
    }
}