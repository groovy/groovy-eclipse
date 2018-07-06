/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.eclipse.jface.preference.IPreferenceStore;

public class ProposalFormattingOptions {

    public static ProposalFormattingOptions newFromOptions() {
        IPreferenceStore prefs = GroovyContentAssist.getDefault().getPreferenceStore();

        final boolean noParensAroundClosures = prefs.getBoolean(GroovyContentAssist.CLOSURE_NOPARENS);
        final boolean useBracketsForClosures = prefs.getBoolean(GroovyContentAssist.CLOSURE_BRACKETS);
        final boolean useNamedArguments = prefs.getBoolean(GroovyContentAssist.NAMED_ARGUMENTS);
        final boolean noParensInChains = false;

        return new ProposalFormattingOptions(noParensAroundClosures, useBracketsForClosures, useNamedArguments, noParensInChains, false);
    }

    public final boolean noParensAroundClosures;

    public final boolean useBracketsForClosures;

    public final boolean useNamedArguments;

    // used for DSL command expressions
    public final boolean noParens;
    public final boolean isBuilder;

    private ProposalFormattingOptions(
            boolean noParensAroundArgs,
            boolean useBracketsForClosures,
            boolean useNamedArguments,
            boolean noParens,
            boolean isBuilder) {
        this.noParensAroundClosures = noParensAroundArgs;
        this.useBracketsForClosures = useBracketsForClosures;
        this.useNamedArguments = useNamedArguments;
        this.noParens = noParens;
        this.isBuilder = isBuilder;
    }

    public ProposalFormattingOptions newFromExisting(boolean overrideUseNamedArgs, boolean overrideNoParens, boolean isBuilder, MethodNode method) {
        // For named args if overridden, always use named args
        // if not a constructor and not overridden, never use named args
        if (isBuilder) {
            //builders force CLOSURE_NOPARENS and CLOSURE_BRACKETS preferences
            return new ProposalFormattingOptions(true, true, useNamedArguments, overrideNoParens, true);
        } else if (overrideUseNamedArgs || overrideNoParens) {
            return new ProposalFormattingOptions(noParensAroundClosures, useBracketsForClosures, overrideUseNamedArgs, overrideNoParens, false);
        } else if (useNamedArguments && !(method instanceof ConstructorNode)) {
            return new ProposalFormattingOptions(noParensAroundClosures, useBracketsForClosures, false, overrideNoParens, false);
        } else {
            return this;
        }
    }
}
