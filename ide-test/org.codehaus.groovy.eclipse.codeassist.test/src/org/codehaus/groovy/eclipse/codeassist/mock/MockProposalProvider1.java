/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.mock;

import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.processors.IProposalProvider;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;

public class MockProposalProvider1 implements IProposalProvider {

    private static boolean providerCalled;

    public static boolean wasProviderCalled() {
        return providerCalled;
    }

    public static void reset() {
        providerCalled = false;
    }

    @Override
    public List<IGroovyProposal> getStatementAndExpressionProposals(final ContentAssistContext context,
            final ClassNode completionType, final boolean isStatic, final Set<ClassNode> categories) {
        providerCalled = true;
        return null;
    }

    @Override
    public List<MethodNode> getNewMethodProposals(final ContentAssistContext context) {
        providerCalled = true;
        return null;
    }

    @Override
    public List<String> getNewFieldProposals(final ContentAssistContext context) {
        providerCalled = true;
        return null;
    }
}
