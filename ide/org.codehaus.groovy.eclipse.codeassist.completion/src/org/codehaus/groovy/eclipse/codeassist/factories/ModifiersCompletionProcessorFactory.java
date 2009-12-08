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

package org.codehaus.groovy.eclipse.codeassist.factories;

import org.codehaus.groovy.eclipse.codeassist.processors.IGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.processors.ModifiersCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

/**
 * @author Andrew Eisenberg
 * @created Nov 10, 2009
 *
 */
public class ModifiersCompletionProcessorFactory implements IGroovyCompletionProcessorFactory {
    public IGroovyCompletionProcessor createProcessor(
            ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        return new ModifiersCompletionProcessor(context, javaContext, nameEnvironment);
    }

}
