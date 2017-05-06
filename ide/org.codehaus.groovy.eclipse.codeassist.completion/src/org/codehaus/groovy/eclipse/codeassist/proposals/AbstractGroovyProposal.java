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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;

public abstract class AbstractGroovyProposal implements IGroovyProposal {

    /**
     * @return the AST node associated with this proposal, or null if there is none
     */
    public AnnotatedNode getAssociatedNode() {
        return null;
    }

    protected int computeRelevance() {
        return Relevance.calculateRelevance(this, relevanceMultiplier);
    }

    private float relevanceMultiplier = 1;

    public void setRelevanceMultiplier(float relevanceMultiplier) {
        this.relevanceMultiplier = relevanceMultiplier;
    }

    protected String requiredStaticImport;

    public void setRequiredStaticImport(String requiredStaticImport) {
        this.requiredStaticImport = requiredStaticImport;
    }

    /*protected Image getImage(CompletionProposal proposal, CompletionProposalLabelProvider labelProvider) {
        return JavaPlugin.getImageDescriptorRegistry().get(labelProvider.createImageDescriptor(proposal));
    }*/

    /**
     * Use {@link ProposalUtils#getImage(CompletionProposal)} instead.
     */
    /*@Deprecated
    protected Image getImageFor(ASTNode node) {
        if (node instanceof FieldNode) {
            int mods = ((FieldNode) node).getModifiers();
            if (test(mods, Opcodes.ACC_PUBLIC)) {
                return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
            } else if (test(mods, Opcodes.ACC_PROTECTED)) {
                return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PROTECTED);
            } else if (test(mods, Opcodes.ACC_PRIVATE)) {
                return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PRIVATE);
            }
            return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_DEFAULT);
        } else if (node instanceof PropertyNode) {
            // property nodes are not really used any more.
            int mods = ((PropertyNode) node).getModifiers();
            if (test(mods, Opcodes.ACC_PUBLIC)) {
                return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
            } else if (test(mods, Opcodes.ACC_PROTECTED)) {
                return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PROTECTED);
            } else if (test(mods, Opcodes.ACC_PRIVATE)) {
                return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PRIVATE);
            }
            return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_DEFAULT);
        }
        return null;
    }*/

    /*private boolean test(int flags, int mask) {
        return (flags & mask) != 0;
    }*/

    /**
     * Use {@link #computeRelevance()} or {@link #computeRelevance(float)} instead.
     */
    /*@Deprecated
    protected int getRelevance(char[] name) {
        switch(name[0]) {
            case '$':
                return 1;
            case '_':
                return 5;
            default:
                return 1000;
        }
    }*/
}
