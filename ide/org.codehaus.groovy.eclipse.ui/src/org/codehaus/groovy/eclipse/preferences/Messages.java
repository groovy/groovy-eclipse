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
package org.codehaus.groovy.eclipse.preferences;

import java.util.ResourceBundle;

public class Messages {
    public final static String RESOURCE_BUNDLE = Messages.class.getPackage()
            .getName()
            + ".Messages";//$NON-NLS-1$

    private static ResourceBundle resourceBundle = null;

    private static boolean notRead = true;

    public Messages() {}

    public static ResourceBundle getResourceBundle() {
        if (!notRead)
            return resourceBundle;
        notRead = false;
        try {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
            return resourceBundle;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getString(final String key) {
        try {
            return getResourceBundle().getString(key);
        } catch (final Exception e) {
            return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
        }
    }
}
