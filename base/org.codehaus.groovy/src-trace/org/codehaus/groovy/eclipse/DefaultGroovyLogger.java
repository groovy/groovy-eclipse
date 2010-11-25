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
package org.codehaus.groovy.eclipse;

import java.util.Date;

/**
 * Default logger logs to sysout and includes a timestamp
 * @author Andrew Eisenberg
 * @created Nov 24, 2010
 */
@SuppressWarnings("nls")
public class DefaultGroovyLogger implements IGroovyLogger {

    public void log(TraceCategory category, String message) {
        System.out.println(category.label + " : " + new Date() + " : " + message);
    }

    public boolean isCategoryEnabled(TraceCategory category) {
        return true;
    }

}
