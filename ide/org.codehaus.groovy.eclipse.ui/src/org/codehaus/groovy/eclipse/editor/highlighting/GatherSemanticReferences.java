/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.Collection;
import java.util.Collections;

import org.codehaus.groovy.eclipse.preferences.PreferenceConstants;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Created and invoked on Groovy compilation units to collect code semantics
 * for use in syntax coloring/highlighting.
 */
public class GatherSemanticReferences {

    private final GroovyCompilationUnit unit;

    public GatherSemanticReferences(GroovyCompilationUnit unit) {
        this.unit = isSemanticHighlightingEnabled() ? unit : null;
    }

    public Collection<HighlightedTypedPosition> findSemanticHighlightingReferences() {
        if (unit != null) {
            SemanticHighlightingReferenceRequestor requestor = new SemanticHighlightingReferenceRequestor(unit);
            TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
            visitor.visitCompilationUnit(requestor);
            return requestor.typedPositions;
        }
        return Collections.emptySet();
    }

    private static boolean isSemanticHighlightingEnabled() {
        IPreferenceStore prefs = PreferenceConstants.getPreferenceStore();
        return prefs.getBoolean(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING);
    }

    private static TypeInferencingVisitorFactory factory = new TypeInferencingVisitorFactory();
}
