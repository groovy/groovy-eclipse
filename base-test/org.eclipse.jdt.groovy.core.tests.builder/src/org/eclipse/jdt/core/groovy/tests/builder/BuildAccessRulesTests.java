/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.builder;

import junit.framework.Test;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.internal.core.ClasspathAccessRule;

/**
 * Tests for build path access rules (i.e. restrictions placed on classpath entries).
 * <p>
 * Example:<pre>
 * &lt;classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER">
 *   &lt;accessrules>
 *     &lt;accessrule kind="nonaccessible" pattern="java/beans/**"/>
 *   &lt;/accessrules>
 * &lt;/classpathentry>
 * </pre>
 */
public final class BuildAccessRulesTests extends BuilderTests {

    public static Test suite() {
        return buildTestSuite(BuildAccessRulesTests.class);
    }

    public BuildAccessRulesTests(String name) {
        super(name);
    }

    protected IPath src;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IPath projectPath = env.addProject("Project", "1.5");
        env.addGroovyNature("Project");
        env.setClasspath(projectPath, new IClasspathEntry[] {
            JavaCore.newSourceEntry(src = projectPath.append("src")),
            JavaCore.newContainerEntry(GroovyClasspathContainer.CONTAINER_ID),
            JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.5"),
                new IAccessRule[] {new ClasspathAccessRule(new Path("java/beans/**"), IAccessRule.K_NON_ACCESSIBLE)}, null, false) // create access restriction
        });
        env.createFolder(src);
    }

    //

    public void testAccessForExtends() {
        env.addGroovyClass(src, "Foo",
            "import java.beans.*\n" +
            "class Foo extends BeanDescriptor {}");
        fullBuild();

        expectingProblemsFor(src.append("Foo.groovy"), "Problem : Access restriction: The type 'BeanDescriptor' is not API" +
            " (restriction on required library '##') [ resource : </Project/src/Foo.groovy> range : <38,52> category : <150> severity : <2>]");
    }

    public void testAccessForImplements() {
        env.addGroovyClass(src, "Foo",
            "import java.beans.*\n" +
            "abstract class Foo implements BeanInfo {}");
        fullBuild();

        expectingProblemsFor(src.append("Foo.groovy"), "Problem : Access restriction: The type 'BeanInfo' is not API" +
            " (restriction on required library '##') [ resource : </Project/src/Foo.groovy> range : <50,59> category : <150> severity : <2>]");
    }
}
