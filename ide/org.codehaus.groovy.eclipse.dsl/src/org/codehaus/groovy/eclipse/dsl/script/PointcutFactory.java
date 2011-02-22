/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.script;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.FinalPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PrivatePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.PublicPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.StaticPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AbstractModifierPointcut.SynchronizedPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AnnotatedByPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.BindPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.CurrentTypePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingClassPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingMethodPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.EnclosingScriptPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FileExtensionPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindFieldPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindMethodPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.FindPropertyPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.NamePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.OrPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.ProjectNaturePointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.SourceFolderPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.UserExtensiblePointcut;
import org.eclipse.core.resources.IProject;

/**
 * Generates {@link IPointcut} objects
 * @author andrew
 * @created Feb 11, 2011
 */
public class PointcutFactory {

    private static final Map<String, Class<? extends IPointcut>> registry = new HashMap<String, Class<? extends IPointcut>>();
    static {
        // combinatorial pointcuts
        registry.put("and", AndPointcut.class);
        registry.put("or", OrPointcut.class);

        // binding pointcuts
        registry.put("bind", BindPointcut.class);

        // semantic pointcuts
        registry.put("currentType", CurrentTypePointcut.class);

        // filtering pointcuts
        registry.put("annotatedBy", AnnotatedByPointcut.class);
        registry.put("findField", FindFieldPointcut.class);
        registry.put("findMethod", FindMethodPointcut.class);
        registry.put("findProperty", FindPropertyPointcut.class);
        registry.put("name", NamePointcut.class);
        registry.put("isFinal", FinalPointcut.class);
        registry.put("isPrivate", PrivatePointcut.class);
        registry.put("isPublic", PublicPointcut.class);
        registry.put("isStatic", StaticPointcut.class);
        registry.put("isSynchronized", SynchronizedPointcut.class);
        
        // lexical pointcuts
        registry.put("enclosingClass", EnclosingClassPointcut.class);
        registry.put("enclosingField", EnclosingFieldPointcut.class);
        registry.put("enclosingMethod", EnclosingMethodPointcut.class);
        registry.put("enclosingScript", EnclosingScriptPointcut.class);
        
        // structural pointcuts
        registry.put("fileExtension", FileExtensionPointcut.class);
        registry.put("nature", ProjectNaturePointcut.class);
        registry.put("sourceFolder", SourceFolderPointcut.class);
    }
    
    private final Map<String, Closure> localRegistry = new HashMap<String, Closure>();

    private final String uniqueID;

    private final IProject project;
    
    public PointcutFactory(String uniqueID) {
        this.uniqueID = uniqueID;
        this.project = null;
    }
    
    public PointcutFactory(String uniqueID, IProject project) {
        this.uniqueID = uniqueID;
        this.project = project;
    }
    
    

    
    public void registerLocalPointcut(String name, Closure c) {
        localRegistry.put(name, c);
    }
    
    /**
     * creates a pointcut of the given name, or returns null if not registered
     * @param name
     * @return
     */
    public IPointcut createPointcut(String name) {
        Closure c = localRegistry.get(name);
        if (c != null) {
            return new UserExtensiblePointcut(uniqueID, c);
        } 
        
        Class<? extends IPointcut> pc = registry.get(name);
        if (pc != null) {
            try {
                // try the one arg constructor and the no-arg constructor
                try {
                    IPointcut p = pc.getConstructor(String.class).newInstance(uniqueID);
                    p.setProject(project);
                    return p;
                } catch (NoSuchMethodException e) {
                    IPointcut p = pc.getConstructor(String.class).newInstance();
                    if (p instanceof AbstractPointcut) {
                        ((AbstractPointcut) p).setContainerIdentifier(uniqueID);
                        p.setProject(project);
                        return p;
                    }
                    throw e;
                }
            } catch (Exception e) {
                GroovyDSLCoreActivator.logException(e);
            }
        }
        return null;
    }
}
