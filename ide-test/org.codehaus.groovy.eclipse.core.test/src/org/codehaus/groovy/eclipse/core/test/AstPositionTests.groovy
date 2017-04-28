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
package org.codehaus.groovy.eclipse.core.test

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetParser
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.junit.Test

/**
 * Testing whether the parser returns AST's that have sensible position information in them.
 */
final class AstPositionTests {

    private String getTextOfNode(ASTNode node, IDocument doc) {
        doc.get(node.start, node.length)
    }

    @Test
    void testASTPositionForBlock() {
        String text = '''\
            def foo() {
                zor(1,2)
                bar(a,b)
            }'''.stripIndent()
        GroovySnippetParser sp = new GroovySnippetParser()
        IDocument doc = new Document(text)
        ModuleNode module = sp.parse(doc.get())
        assert getTextOfNode(module, doc) == text

        // Check the method node
        ClassNode classNode = module.getClasses().get(0)
        MethodNode fooMethod = classNode.getMethods("foo").get(0)
        assert getTextOfNode(fooMethod, doc) == text

        // Check the body of method
        BlockStatement bodyNode = (BlockStatement)fooMethod.getCode()
        String body = getTextOfNode(bodyNode, doc)
        assert body.trim() == '''\
            {
                zor(1,2)
                bar(a,b)
            }'''.stripIndent()

        // Check each statement (one per line)
        List<Statement> statements = bodyNode.getStatements()
        assert statements.size() == 2
        int startLine = 1
        for (int i = 0; i < statements.size(); i += 1) {
            Statement stmNode = statements.get(i)
            String stm = getTextOfNode(stmNode, doc)
            int line = startLine + i
            String lineStr = doc.get(doc.getLineOffset(line), doc.getLineLength(line))
            assert stm.trim() == lineStr.trim()
        }
    }
}
