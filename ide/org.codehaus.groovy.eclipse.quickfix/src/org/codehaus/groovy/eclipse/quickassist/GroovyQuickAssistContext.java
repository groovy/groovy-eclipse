/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Groovy-specific quick assist context, similar to {@link IInvocationContext}.
 */
public class GroovyQuickAssistContext {

    private final IInvocationContext context;
    private ASTNodeFinder finder;
    private ASTNode coveredNode;

    public GroovyQuickAssistContext(IInvocationContext context) {
        Assert.isNotNull(context);
        this.context = context;
    }

    public GroovyCompilationUnit getCompilationUnit() {
        return (GroovyCompilationUnit) context.getCompilationUnit();
    }

    public <T extends ITypeRequestor> T visitCompilationUnit(T requestor) {
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(getCompilationUnit());
        visitor.visitCompilationUnit(requestor);
        return requestor;
    }

    public ASTNode getCoveredNode() {
        if (finder == null) {
            Region region = new Region(getSelectionOffset(), getSelectionLength());
            finder = new ASTNodeFinder(region) {
                @Override
                public void visitGStringExpression(GStringExpression expr) {
                    // skip GString fragments, i.e. expr.getStrings()
                    visitExpressions(expr.getValues());
                    check(expr);
                }
            };
            coveredNode = finder.doVisit(getCompilationUnit().getModuleNode());
        }
        return coveredNode;
    }

    /**
     * Returns the line delimiter of the specified line or a sensible default if
     * the line is not closed with a line delimiter.
     *
     * @param document can be {@code null}
     */
    public String getLineDelimiter(IDocument document, int line) {
        if (document == null) document = newTempDocument();
        String ending = null;
        try {
            while ((ending = document.getLineDelimiter(line)) == null && line > 1) {
                line -= 1;
            }
        } catch (BadLocationException ignore) {
        }
        return ending != null ? ending : ASTTools.getLineDelimeter(getCompilationUnit());
    }

    public String getNodeText(ASTNode node) {
        int offset = node.getStart(),
            length = node.getLength();
        if (offset >= 0 && length > 0) {
            char[] contents = getCompilationUnit().getContents();
            if (contents != null && contents.length >= offset + length) {
                return String.valueOf(contents, offset, length);
            }
        }
        return null;
    }

    public TypeLookupResult getNodeType(final ASTNode node) {
        TypeLookupResult[] ref = {new TypeLookupResult(null, null, node, TypeLookupResult.TypeConfidence.UNKNOWN, null)};
        visitCompilationUnit((ASTNode n, TypeLookupResult r, IJavaElement e) -> {
            if (n == node) {
                ref[0] = r;
                return ITypeRequestor.VisitStatus.STOP_VISIT;
            }
            return ITypeRequestor.VisitStatus.CONTINUE;
        });
        return ref[0];
    }

    public int getSelectionOffset() {
        return context.getSelectionOffset();
    }

    public int getSelectionLength() {
        return context.getSelectionLength();
    }

    public IProject getProject() {
        return context.getCompilationUnit().getResource().getProject();
    }

    public Shell getShell() {
        Shell shell = null;

        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
        }
        if (display != null) {
            shell = display.getActiveShell();
            if (shell == null || shell.isDisposed()) {
                for (Shell sh : display.getShells()) {
                    if (sh != null && !sh.isDisposed()) {
                        shell = sh;
                        break;
                    }
                }
            }
        }
        return shell;
    }

    public IDocument newTempDocument() {
        return new Document(String.valueOf(getCompilationUnit().getContents()));
    }
}
