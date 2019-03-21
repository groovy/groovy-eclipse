/*
 * Copyright 2009-2017 the original author or authors.
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

import java.util.Arrays;

import org.codehaus.jdt.groovy.model.GrabDeclaration;
import org.codehaus.jdt.groovy.model.GrapesContainer;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GrapesWorkbenchAdapter implements IAdapterFactory {

    public Class[] getAdapterList() {
        return new Class[] {IWorkbenchAdapter.class};
    }

    public Object getAdapter(Object adaptable, Class adapterType) {
        if (Arrays.asList(getAdapterList()).contains(adapterType)) {
            final ImageDescriptor imageDescriptor;
            if (adaptable instanceof GrabDeclaration) {
                imageDescriptor = JavaPluginImages.DESC_OBJS_EXTJAR;
            } else if (adaptable instanceof GrapesContainer) {
                imageDescriptor = JavaPluginImages.DESC_OBJS_LIBRARY;
            } else {
                imageDescriptor = null;
            }

            if (imageDescriptor != null) {
                return new WorkbenchAdapter() {
                    public ImageDescriptor getImageDescriptor(Object object) {
                        return imageDescriptor;
                    };
                };
            }
        }
        return null;
    }
}
