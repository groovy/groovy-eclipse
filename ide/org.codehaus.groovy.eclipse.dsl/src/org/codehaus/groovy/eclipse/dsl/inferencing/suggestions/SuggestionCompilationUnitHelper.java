/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Creates a suggestion given an offset and length and a Groovy
 * compilation unit. Editing an existing property or method is NOT yet supported
 * This is typically invoked with a quick assist or quick fix operation on a
 * selection in a Groovy editor
 * 
 * @author Nieraj Singh
 * @created 2011-09-16
 */
public class SuggestionCompilationUnitHelper {

    private int length;

    private int offset;

    private GroovyCompilationUnit unit;

    private IProject project;

    /**
     * Project must not be null
     */
    public SuggestionCompilationUnitHelper(int length, int offset, GroovyCompilationUnit unit, IProject project) {
        this.length = length;
        this.offset = offset;
        this.unit = unit;
        this.project = project;
    }

    public IGroovySuggestion addSuggestion() {
        IGroovySuggestion suggestion = null;

        ASTNode node = findValidASTNode();

        if (node != null) {
            SuggestionsRequestor requestor = new SuggestionsRequestor(node);
            TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
            visitor.visitCompilationUnit(requestor);
            SuggestionDescriptor descriptor = requestor.getSuggestionDescriptor();
            suggestion = createSuggestion(descriptor);
        }

        return suggestion;
    }

    public boolean canAddSuggestion() {
        return findValidASTNode() != null;
    }

    /**
     * Returns a valid ASTNode that can be added as a suggestion. Or null if
     * none are found.
     * 
     * @return
     */
    protected ASTNode findValidASTNode() {
    	if (unit == null) {
    		return null;
    	}
        Region region = new Region(offset, length);
        ASTNodeFinder finder = new ASTNodeFinder(region);
        ASTNode node = finder.doVisit(unit.getModuleNode());
        return SuggestionsRequestor.isValidNode(node) ? node : null;
    }

    protected IGroovySuggestion createSuggestion(SuggestionDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        Shell shell = getShell();
        IGroovySuggestion suggestion = null;
        if (shell != null) {
            suggestion = new OperationManager().addGroovySuggestion(project, descriptor, shell);
            InferencingSuggestionsManager.getInstance().commitChanges(project);
        } else {
            GroovyDSLCoreActivator.logException("Unable to open Inferencing Suggestions dialogue. No shell found.", new Exception());
        }
        return suggestion;
    }

    protected Shell getShell() {
        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
        }
        if (display == null) {
            return null;
        }
        Shell shell = display.getActiveShell();
        if (shell == null || shell.isDisposed()) {
            for (Shell shll : display.getShells()) {
                if (shll != null && !shll.isDisposed()) {
                    shell = shll;
                    break;
                }
            }
        }
        return shell;
    }
}
