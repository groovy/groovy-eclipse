/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.compiler;

import java.util.List;

import junit.framework.TestCase;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * Testing whether the parser returns AST's that have sensible position information
 * in them.
 *
 * @author kdvolder
 */
public class AstPositionTests extends TestCase {

    public void testASTPositionForBlock() throws Exception {
        String text =
            "def foo() {\n" +
            "    zor(1,2)\n" +
            "    bar(a,b)\n" +
            "}";
        GroovySnippetParser sp = new GroovySnippetParser();
        IDocument doc = new Document(text);
        ModuleNode module = sp.parse(doc.get());
        assertEquals(text, getTextOfNode(module, doc));

        //Check the method node
        ClassNode classNode = module.getClasses().get(0);
        MethodNode fooMethod = classNode.getMethods("foo").get(0);
        assertEquals(text, getTextOfNode(fooMethod, doc));

        //Check the body of method
        BlockStatement bodyNode = (BlockStatement)fooMethod.getCode();
        String body = getTextOfNode(bodyNode, doc);
        assertEquals(
            "{\n" +
            "    zor(1,2)\n" +
            "    bar(a,b)\n" +
            "}",
            body.trim());

        //Check each statement (one per line)
        List<Statement> statements = bodyNode.getStatements();
        assertEquals(2, statements.size());
        int startLine=1;
        for (int i=0; i<statements.size();i++) {
            Statement stmNode = statements.get(i);
            String stm = getTextOfNode(stmNode, doc);
            int line = startLine+i;
            String lineStr = doc.get(doc.getLineOffset(line), doc.getLineLength(line));
            assertEquals(lineStr.trim(), stm.trim());
        }
    }

    private String getTextOfNode(ASTNode node, IDocument doc) throws BadLocationException {
        return doc.get(node.getStart(), node.getLength());
    }
}

