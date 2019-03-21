/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;

public class GroovyRetargettableActionAdapterFactory implements IAdapterFactory {

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] {IRunToLineTarget.class, IToggleBreakpointsTarget.class};
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adapterType.equals(IToggleBreakpointsTarget.class)) {
            return (T) new ToggleBreakpointAdapter();
        }
        if (adapterType.equals(IRunToLineTarget.class)) {
            return (T) new RunToLineAdapter();
        }
        return null;
    }
}
