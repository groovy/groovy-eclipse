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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the current {@link IGroovyLogger} instance.
 * This class is a singleton
 * @author Andrew Eisenberg
 * @created Nov 24, 2010
 */
// Some code here borrowed from org.eclipse.ajdt.core.AJLog under EPL license
// See http://www.eclipse.org/legal/epl-v10.html
public class GroovyLogManager {
    public static final GroovyLogManager manager = new GroovyLogManager();
    
    private GroovyLogManager() {
        // uninstantiable
        defaultLogger = new DefaultGroovyLogger();
        timers = new HashMap<String, Long>();
    }
    
    private IGroovyLogger[] loggers = null;
    
    // only use default logger if no others are registered
    private IGroovyLogger defaultLogger;
    
    private Map<String, Long> timers;

    private boolean useDefaultLogger;
    
    /**
     * @return true if logger was added.  False if not
     * if not added, then this means the exact logger is already in the list
     */
    public boolean addLogger(IGroovyLogger logger) {
        int newIndex;
        if (loggers == null) {
            loggers = new IGroovyLogger[1];
            newIndex = 0;
        } else {
            // check to see if already there
            for (IGroovyLogger l : loggers) {
                if (l == logger) {
                    return false;
                }
            }
            
            newIndex = loggers.length;
            IGroovyLogger[] newLoggers = new IGroovyLogger[newIndex+1];
            System.arraycopy(loggers, 0, newLoggers, 0, newIndex);
            loggers = newLoggers;
        }
        loggers[newIndex] = logger;
        return true;
    }
    
    /**
     * Removes the logger from the logger list
     * @return true iff found and removed
     * false iff nothing found.
     */
    public boolean removeLogger(IGroovyLogger logger) {
        if (logger != null) {
            int foundIndex = -1;
            for (int i = 0; i < loggers.length; i++) {
                if (loggers[i] == logger) {
                    foundIndex = i;
                }
            }
            if (foundIndex >= 0) {
                if (loggers.length > 1) {
                    IGroovyLogger[] newLoggers = new IGroovyLogger[loggers.length - 1];
                    if (foundIndex > 0) {
                        System.arraycopy(loggers, 0, newLoggers, 0, foundIndex);
                    }
                    System.arraycopy(loggers, foundIndex+1, newLoggers, foundIndex, loggers.length-foundIndex-1);
                    loggers = newLoggers;
                } else {
                    loggers = null;
                }
                return true;
            }
        }
        
        // not found
        return false;
    }
    
    
    public void logStart(String event) {
        timers.put(event, System.currentTimeMillis());
    }
    
    public void logEnd(String event, TraceCategory category) {
        logEnd(event, category, null);
    }
    
    public void logEnd(String event, TraceCategory category, String message) {
        Long then = timers.get(event);
        if (then != null) {
            if (hasLoggers()) {
                long now = System.currentTimeMillis();
                long elapsed = now - then.longValue();
                if ((message != null) && (message.length() > 0)) {
                    log(category,"Event complete: "+elapsed + "ms: " + event + " (" + message + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                } else {
                    log(category,"Event complete: "+elapsed + "ms: " + event); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            timers.remove(event);
        }
    }

    public void log(String message) {
        log(TraceCategory.DEFAULT, message);
    }
    
    public void log(TraceCategory category, String message) {
        if (!hasLoggers()) {
            return;
        }
        
        if (loggers != null) {
            for (IGroovyLogger logger : loggers) {
                if (logger.isCategoryEnabled(category)) {
                    logger.log(category, message);
                }
            }
        }
        
        if (useDefaultLogger) {
            defaultLogger.log(category, message);
        }
    }
    
    /**
     * Call this method to check if any loggers are currently 
     * installed.  Doing so can help avoid creating costly 
     * logging messages unless required
     * @return
     */
    public boolean hasLoggers() {
        return loggers != null || useDefaultLogger;
    }
    
    /**
     * enables/disables the default logger (printing to sysout
     * @param useDefaultLogger
     */
    public void setUseDefaultLogger(boolean useDefaultLogger) {
        this.useDefaultLogger = useDefaultLogger;
    }
    
    public void logException(TraceCategory cat, Throwable t) {
        if (hasLoggers()) {
            // only log if logger is available, otherwise, ignore
            StringWriter writer = new StringWriter();
            t.printStackTrace(new PrintWriter(writer));
            log(cat, "Exception caught.\n" +
                    writer.getBuffer());
        }
    }
    
}
