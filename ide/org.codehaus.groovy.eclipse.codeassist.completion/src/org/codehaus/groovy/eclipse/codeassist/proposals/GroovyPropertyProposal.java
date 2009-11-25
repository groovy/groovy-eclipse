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

package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class GroovyPropertyProposal extends AbstractGroovyProposal {

    private final PropertyNode property;
    
    public GroovyPropertyProposal(PropertyNode property) {
        this.property = property;
    }

    public IJavaCompletionProposal createJavaProposal(
            ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext) {
        
        return new JavaCompletionProposal(property.getName(), context.completionLocation-context.completionExpression.length(),
                property.getName().length(), getImageFor(property),
                createDisplayString(property), 2000);
//                getRelevance(property.getName().toCharArray()));
    }

    protected StyledString createDisplayString(PropertyNode property) {
        StyledString ss = new StyledString();
        
        ss.append(property.getName())
          .append(" : ")
          .append(ProposalUtils.createSimpleTypeName(property.getType()))
          .append(" - ")
          .append(ProposalUtils.createSimpleTypeName(property.getDeclaringClass()), StyledString.QUALIFIER_STYLER)
          .append(" (Groovy)", StyledString.DECORATIONS_STYLER);
        return ss;
    }

}
