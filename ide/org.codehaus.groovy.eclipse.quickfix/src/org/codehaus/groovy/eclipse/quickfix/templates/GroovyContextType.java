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
package org.codehaus.groovy.eclipse.quickfix.templates;

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.StaticImportResolver;
import org.eclipse.jdt.internal.corext.template.java.TypeResolver;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;

public class GroovyContextType extends AbstractJavaContextType {
    public GroovyContextType() {
        setId(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
        setName("Groovy surround-with templates");
        initializeContextTypeResolvers();
    }
    @Override
    protected void initializeContext(JavaContext context) {
        context.addCompatibleContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
    }

    /*
     * Override super class. comment out functionality that doesn't make sense for Groovy
     */
    @Override
    public void initializeContextTypeResolvers() {

        // global
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new SurroundWithLineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());

        // compilation unit
        addResolver(new File());
//        addResolver(new PrimaryTypeName());
//        addResolver(new ReturnType());
//        addResolver(new Method());
        addResolver(new Type());
        addResolver(new Package());
        addResolver(new Project());
//        addResolver(new Arguments());

        // java
        addResolver(new Array());
        addResolver(new ArrayType());
        addResolver(new ArrayElement());
        addResolver(new Index());
        addResolver(new Iterator());
        addResolver(new Collection());
        addResolver(new Iterable());
        addResolver(new IterableType());
        addResolver(new IterableElement());
        addResolver(new Todo());
        
        // Extra
        addResolver(new StaticImportResolver("importStatic", "adds a static import"));
        TypeResolver resolver = new TypeResolver();
        resolver.setType("newType");
        addResolver(resolver);
    }

}