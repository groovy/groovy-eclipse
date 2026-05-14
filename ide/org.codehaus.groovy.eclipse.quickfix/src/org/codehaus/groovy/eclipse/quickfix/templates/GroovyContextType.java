/*
 * Copyright 2009-2024 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.templates;

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContext;
import org.eclipse.jdt.internal.corext.template.java.ImportsResolver;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.LinkResolver;
import org.eclipse.jdt.internal.corext.template.java.StaticImportResolver;
import org.eclipse.jdt.internal.corext.template.java.TypeResolver;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;

public class GroovyContextType extends AbstractJavaContextType {

    public GroovyContextType() {
        setId(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
        setName("Groovy code templates");
        initializeContextTypeResolvers();
    }

    @Override
    public CompilationUnitContext createContext(IDocument document, int offset, int length, ICompilationUnit compilationUnit) {
        JavaContext javaContext = new JavaContext(this, document, offset, length, compilationUnit);
        javaContext.addCompatibleContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
        return javaContext;
    }

    @Override
    public CompilationUnitContext createContext(IDocument document, Position completionPosition, ICompilationUnit compilationUnit) {
        JavaContext javaContext = new JavaContext(this, document, completionPosition, compilationUnit);
        javaContext.addCompatibleContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
        return javaContext;
    }

    /*
     * Override super class. comment out functionality that doesn't make sense for Groovy
     */
    @Override
    public void initializeContextTypeResolvers() {
        // global
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.Selection(GlobalTemplateVariables.LineSelection.NAME,
            //org.eclipse.jdt.internal.corext.template.java.JavaTemplateMessages.CompilationUnitContextType_variable_description_line_selection:
            "<b>${id\\:line_selection[(default)]}</b><br>Evaluates to the selected text for multiple lines. 'default' is an optional parameter, which specifies the text if the selected text is empty.<br><br>Templates that contain this variable will also be shown in the 'Source &gt; Surround With > ...' menu.<br><br><b>Examples:</b><br><code>${line_selection}</code><br><code>${currentLine:line_selection(myStringVariable)}</code><br><code>${currentLine:line_selection('\"A default text\"')}</code>"));
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());

        // compilation unit
        addResolver(new File());
        addResolver(new PrimaryTypeName());
        addResolver(new ReturnType());
        addResolver(new Method());
        addResolver(new Type());
        addResolver(new Package());
        addResolver(new Project());
        addResolver(new Arguments());

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

        // groovy
        addResolver(new StaticImportResolver("importStatic", "adds static import(s)"));
        addResolver(new ImportsResolver("import", "adds type import(s)"));
        addResolver(new LinkResolver("link", "list of choices"));
        TypeResolver resolver = new TypeResolver();
        resolver.setType("newType");
        addResolver(resolver);
    }
}
