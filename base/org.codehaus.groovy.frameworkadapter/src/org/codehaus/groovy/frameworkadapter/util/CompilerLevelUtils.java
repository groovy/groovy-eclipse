/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.frameworkadapter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.stream.FileImageInputStream;

import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.framework.internal.core.PackageAdminImpl;
import org.eclipse.osgi.internal.resolver.StateHelperImpl;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.BundleContext;

import static org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion.*;

/**
 * 
 * @author Andrew Eisenberg
 * @created Aug 10, 2011
 */
public class CompilerLevelUtils {
    private static final String GROOVY_COMPILER_LEVEL = "groovy.compiler.level";
    private static final String DASH_GROOVY_COMPILER_LEVEL = "-groovy.compiler.level";
    private static final String ECLIPSE_COMMANDS = "eclipse.commands";

    private CompilerLevelUtils() {
        // uninstantiable
    }
    
    /**
     * Finds the compiler version that is specified in the system properties
     */
    public static SpecifiedVersion findSysPropVersion() {
        SpecifiedVersion version = internalFindSysProperVersion(FrameworkProperties.getProperty(GROOVY_COMPILER_LEVEL));
        if (version == UNSPECIFIED) {
            // now look at the non vmwargs
            version = internalFindCommandLineVersion(FrameworkProperties.getProperty(ECLIPSE_COMMANDS));
        }
        return version;
    }

    /**
     * @param property
     * @return
     */
    private static SpecifiedVersion internalFindCommandLineVersion(
            String property) {
        if (property == null) {
            return UNSPECIFIED;
        }
        
        String[] split = property.split("\\\n");
        String versionText = null;
        for (int i = 0; i < split.length; i++) {
            if (DASH_GROOVY_COMPILER_LEVEL.equals(split[i]) && i < split.length-1) {
                versionText = split[i+1];
                break;
            }
        }
        return internalFindSysProperVersion(versionText);
    }

    /**
     * Finds the compiler version that is specified in this plugin's configuration area, if it exists
     * @throws IOException 
     */
    public static SpecifiedVersion findConfigurationVersion(BundleContext context) throws IOException {
        File properties = context.getDataFile("groovy_compiler.properties");
        if (properties == null || !properties.exists()) {
            return UNSPECIFIED;
        }
        
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(properties));
        } catch (FileNotFoundException e) {
            return UNSPECIFIED;
        }
        return internalFindSysProperVersion((String) props.get(GROOVY_COMPILER_LEVEL));
    }
    
    /**
     * @param version the version to switch to
     * @param context  must be the {@link BundleContext} of the system bundle
     * @throws IOException
     */
    public static void writeConfigurationVersion(SpecifiedVersion version, BundleContext context) throws IOException {
        if (context == null) {
            return;
        }
        File properties = context.getDataFile("groovy_compiler.properties");
        if (properties == null) {
            // don't have access to file system
            throw new IOException("Don't have file system access");
        }
        if (!properties.exists() && !properties.createNewFile()) {
            // couldn't create file
            throw new IOException("Could not create file " + properties.getPath());
        }
        
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(properties));
        } catch (FileNotFoundException e) {
        }
        props.setProperty(GROOVY_COMPILER_LEVEL, version.versionName);
        props.store(new FileOutputStream(properties), "The Groovy compiler level to load at startup");
    }

    private static SpecifiedVersion internalFindSysProperVersion(String compilerLevel) {
        if (compilerLevel == null) {
            return UNSPECIFIED;
        }
        
        if ("16".equals(compilerLevel) || "1.6".equals(compilerLevel)) {
            return _16;
        }
        if ("17".equals(compilerLevel) || "1.7".equals(compilerLevel)) {
            return _17;
        }
        if ("18".equals(compilerLevel) || "1.8".equals(compilerLevel)) {
            return _18;
        }
        if ("19".equals(compilerLevel) || "1.9".equals(compilerLevel)) {
            return _19;
        }
        if ("20".equals(compilerLevel) || "2.0".equals(compilerLevel)) {
            return _20;
        }
        if ("0".equals(compilerLevel)) {
            return UNSPECIFIED;
        }
        
        // this is an error prevent startup
        throw new IllegalArgumentException("Invalid Groovy compiler level specified: " + compilerLevel + 
                "\nMust be one of 16, 1.6, 17, 1.7, 18, 1.8, 19, 1.9, 20, or 2.0");
    }
}
