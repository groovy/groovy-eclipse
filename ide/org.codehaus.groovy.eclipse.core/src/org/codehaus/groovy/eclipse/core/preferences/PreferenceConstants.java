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
package org.codehaus.groovy.eclipse.core.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public interface PreferenceConstants {

    /**
     * this preference will add the plugin trace statements to the error in
     * order
     * to make it easier to track down problems at runtime
     */
    public static final String GROOVY_LOG_TRACE_MESSAGES_ENABLED = "groovy.log.trace.messages.enabled";

    /**
     * toggles whether an individual project should add the groovy lib folder to
     * the classpath
     */
    public static final String GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL = "groovy.classpath.use.groovy.lib.global";

    /**
     * toggles whether the global default is to add the groovy lib folder to the
     * classpath
     */
    public static final String GROOVY_CLASSPATH_USE_GROOVY_LIB = "groovy.classpath.use.groovy,lib";

    public static final String GROOVY_JUNIT_MONOSPACE_FONT = "groovy.junit.monospace.font";

    public static final String GROOVY_ASK_TO_CONVERT_LEGACY_PROJECTS = "groovy.plugin.ask.to.convert";

    // if true, then groovy internal stack frames are de-emphasized
    public static final String GROOVY_DEBUG_FILTER_STACK = "groovy.debug.filter.stack";

    // separated list of packages to filter
    public static final String GROOVY_DEBUG_FILTER_LIST = "groovy.debug.filter.list";

    // should the user be prompted to force Groovy-specific debug options?
    public static final String GROOVY_DEBUG_FORCE_DEBUG_OPTIONS_ON_STARTUP = "groovy.debug.force_options";

    // default location for running scripts
    // can be: proj_home, script_loc, eclipse_home
    public static final String GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY = "groovy.scripts.workingdir";

    public static final String GROOVY_SCRIPT_PROJECT_HOME = "proj_home";

    public static final String GROOVY_SCRIPT_SCRIPT_LOC = "script_loc";

    public static final String GROOVY_SCRIPT_ECLIPSE_HOME = "eclipse_home";
}
