/*
 * Copyright 2009-2020 the original author or authors.
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
import org.junit.Assert
import org.junit.Test

/**
 * Testing whether the parser returns AST's that have sensible position information in them.
 */
final class AstPositionTests {

    private String getTextOfNode(ASTNode node, IDocument doc) {
        doc.get(node.start, node.length)
    }

    @Test
    void testPositionForBlock() {
        String text = '''\
            |def foo() {
            |    zor(1,2)
            |    bar(a,b)
            |}'''.stripMargin()
        def sp = new GroovySnippetParser()
        IDocument doc = new Document(text)
        ModuleNode module = sp.parse(doc.get())
        Assert.assertEquals(text, getTextOfNode(module, doc))

        // check the method node
        ClassNode classNode = module.classes[0]
        MethodNode fooMethod = classNode.getMethods('foo')[0]
        Assert.assertEquals(text, getTextOfNode(fooMethod, doc))

        // check the body of method
        BlockStatement bodyNode = (BlockStatement) fooMethod.code
        String body = getTextOfNode(bodyNode, doc)
        assert body.trim() == '''\
            |{
            |    zor(1,2)
            |    bar(a,b)
            |}'''.stripMargin()

        // check each statement (one per line)
        List<Statement> statements = bodyNode.statements
        assert statements.size() == 2
        int startLine = 1
        for (i in 0..1) {
            Statement statement = statements[i]
            String statementText = getTextOfNode(statement, doc)
            int line = startLine + i
            String lineStr = doc.get(doc.getLineOffset(line), doc.getLineLength(line))
            Assert.assertEquals(lineStr.trim(), statementText.trim())
        }
    }
}
