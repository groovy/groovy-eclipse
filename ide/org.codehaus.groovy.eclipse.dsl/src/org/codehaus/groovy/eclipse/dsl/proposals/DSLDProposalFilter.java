/*
 * Copyright 2009-2024 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.proposals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.codeassist.processors.IProposalFilterExtension;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class DSLDProposalFilter implements IProposalFilterExtension {

    @Override
    public List<ICompletionProposal> filterExtendedProposals(final List<ICompletionProposal> proposals,
            final ContentAssistContext context, final JavaContentAssistInvocationContext javaContext) {
        if (!org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator.getDefault().isDSLDDisabled()) {
            Map<String, ICompletionProposal> map = new LinkedHashMap<>(proposals.size() * 3 / 2);

            for (ICompletionProposal proposal : proposals) {
                String key = getKeyString(proposal);
                ICompletionProposal previous = map.put(key, proposal);
                if (previous instanceof IJavaCompletionProposal && proposal instanceof IJavaCompletionProposal) {
                    int r1 = ((IJavaCompletionProposal) previous).getRelevance();
                    int r2 = ((IJavaCompletionProposal) proposal).getRelevance();
                    if (r1 > r2) {
                        map.put(key, previous);
                    }
                }
            }

            if (map.size() != proposals.size()) {
                return new ArrayList<>(map.values());
            }
        }
        return null;
    }

    private static String getKeyString(final ICompletionProposal proposal) {
        String key = proposal.getDisplayString();
        Matcher m = BASE_DESC.matcher(key);
        if (m.find()) key = m.group();

        // key for method: "printf(String format, Object[] values) : void - DefaultGroovyMethods"
        int lparen = key.indexOf('('), rparen = key.indexOf(')', lparen);
        if (lparen > 0 && rparen > lparen + 1) {
            StringBuilder buf = new StringBuilder(key.length()).append(key, 0, lparen + 1);
            String[] tokens = key.substring(lparen + 1, rparen).split(", ");
            for (int i = 0; i < tokens.length; ++i) {
                if (i != 0) buf.append(',');
                // remove parameter name (if present)
                int end = tokens[i].lastIndexOf(' ');
                if (end < 0) end= tokens[i].length();
                buf.append(tokens[i], 0, end);
            }
            buf.append(key, rparen, key.length());
            key = buf.toString();
        }
        // key for method: "printf(String,Object[]) : void - DefaultGroovyMethods"

        return key;
    }

    private static final Pattern BASE_DESC = Pattern.compile("^.* - " +
        "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");
}
