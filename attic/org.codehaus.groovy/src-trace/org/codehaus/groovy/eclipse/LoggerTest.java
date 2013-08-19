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

import junit.framework.TestCase;

/**
 * Simple test to make sure that logs are added and removed properly
 * @author Andrew Eisenberg
 * @created Nov 24, 2010
 */
public class LoggerTest extends TestCase {

    public void testLoggers() throws Exception {
        DefaultGroovyLogger l1 = new DefaultGroovyLogger();
        DefaultGroovyLogger l2 = new DefaultGroovyLogger();
        DefaultGroovyLogger l3 = new DefaultGroovyLogger();
        DefaultGroovyLogger l4 = new DefaultGroovyLogger();
        DefaultGroovyLogger l5 = new DefaultGroovyLogger();
        
        assertTrue(GroovyLogManager.manager.addLogger(l1));
        assertFalse(GroovyLogManager.manager.addLogger(l1));
        assertTrue(GroovyLogManager.manager.addLogger(l2));
        assertFalse(GroovyLogManager.manager.addLogger(l2));
        assertTrue(GroovyLogManager.manager.addLogger(l3));
        assertFalse(GroovyLogManager.manager.addLogger(l3));
        assertTrue(GroovyLogManager.manager.addLogger(l4));
        assertFalse(GroovyLogManager.manager.addLogger(l4));
        assertTrue(GroovyLogManager.manager.addLogger(l5));
        assertFalse(GroovyLogManager.manager.addLogger(l5));
        
        assertTrue(GroovyLogManager.manager.removeLogger(l1));
        assertFalse(GroovyLogManager.manager.removeLogger(l1));
        assertTrue(GroovyLogManager.manager.removeLogger(l2));
        assertFalse(GroovyLogManager.manager.removeLogger(l2));
        assertTrue(GroovyLogManager.manager.removeLogger(l3));
        assertFalse(GroovyLogManager.manager.removeLogger(l3));
        assertTrue(GroovyLogManager.manager.removeLogger(l4));
        assertFalse(GroovyLogManager.manager.removeLogger(l4));
        assertTrue(GroovyLogManager.manager.removeLogger(l5));
        assertFalse(GroovyLogManager.manager.removeLogger(l5));
        
        // now reverse order of removal
        assertTrue(GroovyLogManager.manager.addLogger(l1));
        assertFalse(GroovyLogManager.manager.addLogger(l1));
        assertTrue(GroovyLogManager.manager.addLogger(l2));
        assertFalse(GroovyLogManager.manager.addLogger(l2));
        assertTrue(GroovyLogManager.manager.addLogger(l3));
        assertFalse(GroovyLogManager.manager.addLogger(l3));
        assertTrue(GroovyLogManager.manager.addLogger(l4));
        assertFalse(GroovyLogManager.manager.addLogger(l4));
        assertTrue(GroovyLogManager.manager.addLogger(l5));
        assertFalse(GroovyLogManager.manager.addLogger(l5));
        
        assertTrue(GroovyLogManager.manager.removeLogger(l5));
        assertFalse(GroovyLogManager.manager.removeLogger(l5));
        assertTrue(GroovyLogManager.manager.removeLogger(l4));
        assertFalse(GroovyLogManager.manager.removeLogger(l4));
        assertTrue(GroovyLogManager.manager.removeLogger(l3));
        assertFalse(GroovyLogManager.manager.removeLogger(l3));
        assertTrue(GroovyLogManager.manager.removeLogger(l2));
        assertFalse(GroovyLogManager.manager.removeLogger(l2));
        assertTrue(GroovyLogManager.manager.removeLogger(l1));
        assertFalse(GroovyLogManager.manager.removeLogger(l1));
        
        // now removal from middle
        assertTrue(GroovyLogManager.manager.addLogger(l1));
        assertFalse(GroovyLogManager.manager.addLogger(l1));
        assertTrue(GroovyLogManager.manager.addLogger(l2));
        assertFalse(GroovyLogManager.manager.addLogger(l2));
        assertTrue(GroovyLogManager.manager.addLogger(l3));
        assertFalse(GroovyLogManager.manager.addLogger(l3));
        assertTrue(GroovyLogManager.manager.addLogger(l4));
        assertFalse(GroovyLogManager.manager.addLogger(l4));
        assertTrue(GroovyLogManager.manager.addLogger(l5));
        assertFalse(GroovyLogManager.manager.addLogger(l5));
        
        assertTrue(GroovyLogManager.manager.removeLogger(l3));
        assertFalse(GroovyLogManager.manager.removeLogger(l3));
        assertTrue(GroovyLogManager.manager.removeLogger(l2));
        assertFalse(GroovyLogManager.manager.removeLogger(l2));
        assertTrue(GroovyLogManager.manager.removeLogger(l4));
        assertFalse(GroovyLogManager.manager.removeLogger(l4));
        assertTrue(GroovyLogManager.manager.removeLogger(l5));
        assertFalse(GroovyLogManager.manager.removeLogger(l5));
        assertTrue(GroovyLogManager.manager.removeLogger(l1));
        assertFalse(GroovyLogManager.manager.removeLogger(l1));
        

        

    }

}
