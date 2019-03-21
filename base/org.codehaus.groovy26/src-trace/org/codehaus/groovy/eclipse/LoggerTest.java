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
package org.codehaus.groovy.eclipse;

import org.junit.Assert;
import org.junit.Test;

/**
 * Ensures that logs are added and removed properly.
 */
public final class LoggerTest {

    @Test
    public void testLoggers() throws Exception {
        DefaultGroovyLogger logger1 = new DefaultGroovyLogger();
        DefaultGroovyLogger logger2 = new DefaultGroovyLogger();
        DefaultGroovyLogger logger3 = new DefaultGroovyLogger();
        DefaultGroovyLogger logger4 = new DefaultGroovyLogger();
        DefaultGroovyLogger logger5 = new DefaultGroovyLogger();

        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger1));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger1));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger2));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger2));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger3));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger3));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger4));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger4));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger5));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger5));

        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger1));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger1));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger2));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger2));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger3));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger3));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger4));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger4));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger5));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger5));

        // now reverse order of removal
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger1));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger1));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger2));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger2));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger3));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger3));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger4));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger4));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger5));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger5));

        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger5));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger5));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger4));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger4));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger3));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger3));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger2));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger2));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger1));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger1));

        // now removal from middle
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger1));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger1));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger2));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger2));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger3));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger3));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger4));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger4));
        Assert.assertTrue(GroovyLogManager.manager.addLogger(logger5));
        Assert.assertFalse(GroovyLogManager.manager.addLogger(logger5));

        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger3));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger3));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger2));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger2));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger4));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger4));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger5));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger5));
        Assert.assertTrue(GroovyLogManager.manager.removeLogger(logger1));
        Assert.assertFalse(GroovyLogManager.manager.removeLogger(logger1));
    }
}
