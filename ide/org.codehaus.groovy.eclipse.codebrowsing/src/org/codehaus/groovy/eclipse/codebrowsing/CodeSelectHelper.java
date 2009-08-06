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
package org.codehaus.groovy.eclipse.codebrowsing;

import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ICodeSelectHelper;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.IRegion;

public class CodeSelectHelper implements ICodeSelectHelper {
    
    public IJavaElement[] select(GroovyCompilationUnit unit, IRegion r) {
        List<IJavaElement> proposals = DeclarationSearchAssistant
                .getInstance().getProposals(unit, r);
        if (proposals != null) {
            IJavaElement[] elements = new IJavaElement[proposals.size()];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = proposals.get(i);
            }
            return elements;
        } else {
            return null;
        }
    }

}
