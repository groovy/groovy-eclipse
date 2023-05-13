/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.compiler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Adds {@code src/main/groovy} and {@code src/test/groovy} as source folders.
 *
 * @since 2.6.0
 */
@Mojo(name = "add-groovy-build-paths", defaultPhase = LifecyclePhase.INITIALIZE, requiresProject = true, threadSafe = true)
public class AddGroovySourceFolders extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Adding /src/main/groovy to the list of source folders");
        project.addCompileSourceRoot(project.getBasedir() + "/src/main/groovy");
        getLog().info("Adding /src/test/groovy to the list of test source folders");
        project.addTestCompileSourceRoot(project.getBasedir() + "/src/test/groovy");
    }
}
