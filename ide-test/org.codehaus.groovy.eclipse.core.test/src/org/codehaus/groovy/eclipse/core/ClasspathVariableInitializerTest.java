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
package org.codehaus.groovy.eclipse.core;

import java.io.File;
import java.util.Properties;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;

public class ClasspathVariableInitializerTest extends TestCase {
    private final String newGroovyHome = "/tmp/"
            + ClasspathVariableInitializerTest.class.getSimpleName()
            + "/GroovyHomeTest";

    private Properties oldProperties = null;

    private IPath oldCPVariable = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        oldProperties = System.getProperties();
        final Properties newProperties = new Properties(oldProperties);
        newProperties.remove(ClasspathVariableInitializer.VARIABLE_ID);
        System.setProperties(newProperties);
        oldCPVariable = JavaCore
                .getClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID);
        JavaCore.removeClasspathVariable(
                ClasspathVariableInitializer.VARIABLE_ID, null);
        // At this point there should be no system property for GROOVY_HOME nor
        // should there be
        // a classpath variable entry for GROOVY_HOME... perfect for testing...
        assertNull(System.getProperty(ClasspathVariableInitializer.VARIABLE_ID));
        assertNull(JavaCore
                .getClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID));
        // Deleting the newGroovyHome directory
        final File groovyHome = new Path(newGroovyHome).toFile();
        if (groovyHome.exists())
            FileUtils.deleteDirectory(groovyHome);
        if (groovyHome.getParentFile().exists())
            FileUtils.deleteDirectory(groovyHome.getParentFile());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperties(oldProperties);
        if (oldCPVariable != null)
            JavaCore.setClasspathVariable(
                    ClasspathVariableInitializer.VARIABLE_ID, oldCPVariable,
                    null);
        else
            JavaCore.removeClasspathVariable(
                    ClasspathVariableInitializer.VARIABLE_ID, null);
        // Deleting the newGroovyHome directory
        final File groovyHome = new Path(newGroovyHome).toFile();
        if (groovyHome.exists())
            FileUtils.deleteDirectory(groovyHome);
        if (groovyHome.getParentFile().exists())
            FileUtils.deleteDirectory(groovyHome.getParentFile());
    }

    public void testInitializeStringNoGroovyHomeSet() throws Exception {
        new ClasspathVariableInitializer()
                .initialize(ClasspathVariableInitializer.VARIABLE_ID);
        final IPath newGroovyHome = GroovyCore.getEmbeddedGroovyRuntimeHome();
        assertEquals(newGroovyHome, JavaCore
                .getClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID));
        assertEquals(newGroovyHome, ClasspathVariableInitializer
                .getCPVarEmbeddablePath());
    }

    public void testInitializeStringGroovyHomePropertySet() throws Exception {
        System.setProperty(ClasspathVariableInitializer.VARIABLE_ID,
                newGroovyHome);
        new ClasspathVariableInitializer()
                .initialize(ClasspathVariableInitializer.VARIABLE_ID);
        assertEquals(new Path(newGroovyHome), JavaCore
                .getClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID));
        assertEquals(new Path(newGroovyHome), ClasspathVariableInitializer
                .getCPVarEmbeddablePath());
    }

    public void testInitializeStringGroovyHomePropertySetEmbeddedExists()
            throws Exception {
        FileUtils.forceMkdir(new Path(newGroovyHome).append("embeddable")
                .toFile());
        System.setProperty(ClasspathVariableInitializer.VARIABLE_ID,
                newGroovyHome);
        new ClasspathVariableInitializer()
                .initialize(ClasspathVariableInitializer.VARIABLE_ID);
        assertEquals(new Path(newGroovyHome), JavaCore
                .getClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID));
        assertEquals(new Path(newGroovyHome).append("embeddable"),
                ClasspathVariableInitializer.getCPVarEmbeddablePath());
    }

    public void testInitializeStringGroovyHomeVarSet() throws Exception {
        JavaCore.setClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID,
                new Path(newGroovyHome), null);
        new ClasspathVariableInitializer()
                .initialize(ClasspathVariableInitializer.VARIABLE_ID);
        assertEquals(new Path(newGroovyHome), JavaCore
                .getClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID));
        assertEquals(new Path(newGroovyHome), ClasspathVariableInitializer
                .getCPVarEmbeddablePath());
    }

    public void testInitializeStringGroovyHomeVarSetEmbeddedExists()
            throws Exception {
        FileUtils.forceMkdir(new Path(newGroovyHome).append("embeddable")
                .toFile());
        JavaCore.setClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID,
                new Path(newGroovyHome), null);
        new ClasspathVariableInitializer()
                .initialize(ClasspathVariableInitializer.VARIABLE_ID);
        assertEquals(new Path(newGroovyHome), JavaCore
                .getClasspathVariable(ClasspathVariableInitializer.VARIABLE_ID));
        assertEquals(new Path(newGroovyHome).append("embeddable"),
                ClasspathVariableInitializer.getCPVarEmbeddablePath());
    }
}
