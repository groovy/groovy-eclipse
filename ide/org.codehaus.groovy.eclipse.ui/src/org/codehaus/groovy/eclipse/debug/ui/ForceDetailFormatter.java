/*
 * Copyright 2009-2019 the original author or authors.
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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.ui.DetailFormatter;
import org.eclipse.jdt.internal.debug.ui.JavaDetailFormattersManager;

/**
 * Forces {@link groovy.lang.Reference} to have a custom format in the
 * details formatter page.
 */
public class ForceDetailFormatter {

    private boolean referenceAlreadyExists(String typeName) {
        JavaDetailFormattersManager manager = JavaDetailFormattersManager.getDefault();
        return manager.hasAssociatedDetailFormatter(new DetailType(typeName));
    }

    private void addFormatter(String typeName, String snippet) {
        DetailFormatter formatter = new DetailFormatter(typeName, snippet, true);
        JavaDetailFormattersManager manager = JavaDetailFormattersManager.getDefault();
        manager.setAssociatedDetailFormatter(formatter);
    }

    public void forceReferenceFormatter() {
        String typeName = "groovy.lang.Reference";
        if (!referenceAlreadyExists(typeName)) {
            addFormatter(typeName, "get()");
        }
    }

    class DetailType implements IJavaType {
        final String name;

        DetailType(String name) {
            this.name = name;
        }

        @Override
        public String getName() throws DebugException {
            return name;
        }

        @Override
        public String getSignature() throws DebugException {
            return Signature.createTypeSignature(name, true);
        }

        @Override
        public IDebugTarget getDebugTarget() {
            return null;
        }

        @Override
        public ILaunch getLaunch() {
            return null;
        }

        @Override
        public String getModelIdentifier() {
            return null;
        }

        @Override
        public <T> T getAdapter(Class<T> adapter) {
            return null;
        }
    }
}
