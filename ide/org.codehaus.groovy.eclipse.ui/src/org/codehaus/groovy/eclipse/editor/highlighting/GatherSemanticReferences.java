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

import java.util.Collection;
import java.util.Collections;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jface.preference.IPreferenceStore;

public class GatherSemanticReferences {

    private final GroovyCompilationUnit unit;
    private final IPreferenceStore preferences;
    public GatherSemanticReferences(GroovyCompilationUnit unit) {
        this.unit = unit;
        preferences = GroovyPlugin.getDefault().getPreferenceStore();
    }

    public Collection<HighlightedTypedPosition> findSemanticHighlightingReferences() {
        if (preferences.getBoolean(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING) /*
                                                                                      * &&
                                                                                      * unit
                                                                                      * .
                                                                                      * isOnBuildPath
                                                                                      * (
                                                                                      * )
                                                                                      */) {

            try {
                SemanticHighlightingReferenceRequestor typeRequestor = new SemanticHighlightingReferenceRequestor(unit.getContents());
                TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                visitor.visitCompilationUnit(typeRequestor);
                return typeRequestor.typedPosition;
            } catch (Exception e) {
                GroovyCore.logException("Exception with semantic highlighting", e);
            }
        }
        return Collections.emptyList();
    }
}