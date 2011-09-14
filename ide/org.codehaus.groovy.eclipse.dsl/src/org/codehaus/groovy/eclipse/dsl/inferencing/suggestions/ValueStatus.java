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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ValueStatus {

    private Object value;

    private IStatus status;

    protected ValueStatus(Object value, IStatus status) {
        this.status = status;
        this.value = value;
    }

    public static ValueStatus getErrorStatus(Object value) {
        return new ValueStatus(value, getErrorStatus(null));
    }

    public static ValueStatus getErrorStatus(Object value, String message) {
        return new ValueStatus(value, getErrorStatus(message));
    }

    protected static IStatus getErrorStatus(String message) {
        return new Status(IStatus.ERROR, GroovyDSLCoreActivator.PLUGIN_ID, message);
    }

    public static ValueStatus getValidStatus(Object value) {
        return new ValueStatus(value, Status.OK_STATUS);
    }

    public String getMessage() {
        return status.getMessage();
    }

    public boolean isError() {
        return status.getSeverity() == IStatus.ERROR;
    }

    public boolean isWarning() {
        return status.getSeverity() == IStatus.WARNING;
    }

    public Object getValue() {
        return value;
    }

    protected static IStatus getWarningStatus(String message) {
        return new Status(IStatus.WARNING, GroovyDSLCoreActivator.PLUGIN_ID, message);
    }

}