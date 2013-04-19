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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;

/**
 * 
 * @author Andrew Eisenberg
 * @created 2013-03-31
 */
public class GroovyContext extends JavaContext {

    public GroovyContext(TemplateContextType type, IDocument document,
            int completionOffset, int completionLength,
            ICompilationUnit compilationUnit) {
        super(type, document, completionOffset, completionLength, compilationUnit);
    }
    
    @Override
    public TemplateBuffer evaluate(Template template)
            throws BadLocationException, TemplateException {
        TemplateBuffer buffer = super.evaluate(template);
        
        // TODO must reformat since Java formatter may not have worked
        // but also must update the variable positions
//        IDocument doc = new Document(buffer.getString());
//        int indentLevel = (Integer) ReflectionUtils.executePrivateMethod(JavaContext.class, "getIndentation", new Class[0], this, new Object[0]);
//        DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(doc, new FormatterPreferences(this.getJavaProject()), indentLevel);
//        try {
//            formatter.format().apply(doc);
//        } catch (MalformedTreeException e) {
//            GroovyQuickFixPlugin.log("Exception during extract local variable refactoring", e);
//        } catch (BadLocationException e) {
//            GroovyQuickFixPlugin.log("Exception during extract local variable refactoring", e);
//        }
//
//        buffer.setContent(doc.get(), buffer.getVariables());
        return buffer;
    }

}
