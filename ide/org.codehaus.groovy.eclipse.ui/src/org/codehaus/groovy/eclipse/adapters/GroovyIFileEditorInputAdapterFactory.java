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
package org.codehaus.groovy.eclipse.adapters;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.groovy.core.GroovyResourceAdapter;
import org.eclipse.ui.IFileEditorInput;

public class GroovyIFileEditorInputAdapterFactory implements IAdapterFactory {

    @Override
    public Class<?>[] getAdapterList() {
        return new GroovyResourceAdapter().getAdapterList();
    }

    @Override
    public <T> T getAdapter(Object adaptable, Class<T> adapterType) {
        T result = null;
        if (adaptable instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) adaptable).getFile();
            result = Adapters.adapt(file, adapterType);
        }
        return result;
    }
}
