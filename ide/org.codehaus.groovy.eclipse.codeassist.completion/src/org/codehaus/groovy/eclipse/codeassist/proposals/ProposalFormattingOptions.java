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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class ProposalFormattingOptions {

    public static ProposalFormattingOptions newFromOptions() {
        IPreferenceStore prefs = GroovyPlugin.getDefault().getPreferenceStore();
        return new ProposalFormattingOptions(prefs.getBoolean(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS),
                prefs.getBoolean(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS),
                prefs.getBoolean(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS),
                prefs.getBoolean(PreferenceConstants.GROOVY_CONTENT_PARAMETER_GUESSING));
    }

    public final boolean noParensAroundClosures;

    public final boolean useBracketsForClosures;

    public final boolean useNamedArguments;

    public final boolean doParameterGuessing;

    public ProposalFormattingOptions(boolean noParensAroundArgs,
 boolean useBracketsForClosures, boolean useNamedArguments,
            boolean doParameterGuessing) {
        this.noParensAroundClosures = noParensAroundArgs;
        this.useBracketsForClosures = useBracketsForClosures;
        this.useNamedArguments = useNamedArguments;
        this.doParameterGuessing = doParameterGuessing;
    }

    public ProposalFormattingOptions newFromExisting(boolean overrideUseNamedArgs, MethodNode method) {
        // if overridden, always use named args
        // if not a constructor and not overridden, never use named args
        if (overrideUseNamedArgs) {
            return new ProposalFormattingOptions(noParensAroundClosures, useBracketsForClosures, true, doParameterGuessing);
        } else if (useNamedArguments && !(method instanceof ConstructorNode)) {
            return new ProposalFormattingOptions(noParensAroundClosures, useBracketsForClosures, false, doParameterGuessing);
        } else {
            return this;
        }
    }
}