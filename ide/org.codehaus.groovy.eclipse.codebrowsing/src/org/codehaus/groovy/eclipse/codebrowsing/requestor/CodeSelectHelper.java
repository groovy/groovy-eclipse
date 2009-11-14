 /*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ICodeSelectHelper;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jface.text.IRegion;

/**
 * @author Andrew Eisenberg
 * @created Nov 4, 2009
 *
 */
public class CodeSelectHelper implements ICodeSelectHelper {
    
    public IJavaElement[] select(GroovyCompilationUnit unit, IRegion r) {
        ModuleNode module = unit.getModuleNode();
        if (module != null) {
            ASTNode nodeToLookFor = findASTNodeAt(module, r);
            if (nodeToLookFor != null) {
                CodeSelectRequestor requestor = new CodeSelectRequestor(nodeToLookFor, unit);
                TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                visitor.visitCompilationUnit(requestor);
                return requestor.getRequestedElement() != null ? new IJavaElement[] { requestor.getRequestedElement() } : new IJavaElement[0];
            }
        }
        return new IJavaElement[0];
    }

    /**
     * @param r
     * @return
     */
    private ASTNode findASTNodeAt(ModuleNode module, IRegion r) {
        ASTNodeFinder finder = new ASTNodeFinder(r);
        return finder.doVisit(module);
    }
}
