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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ICodeSelectHelper;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;

/**
 * @author Andrew Eisenberg
 * @created Nov 4, 2009
 *
 */
public class CodeSelectHelper implements ICodeSelectHelper {
    
    public IJavaElement[] select(GroovyCompilationUnit unit, int start, int length) {
        ModuleNode module = unit.getModuleNode();
        if (module != null) {
            GroovyLogManager.manager.log(TraceCategory.CODESELECT, "Code select starting on " + unit.getElementName() + " at [ " + start + "," + length + " ]");
            String event = "Code select: " + unit.getElementName();
            GroovyLogManager.manager.logStart(event);
            try {
                ASTNode nodeToLookFor = findASTNodeAt(module, new Region(start, length));
                if (nodeToLookFor != null) {
                    // shortcut.  Check to see if we are looking for this type itself
                    if (isTypeDeclaration(module, nodeToLookFor)) {
                        return returnThisNode(unit, nodeToLookFor);
                    }
                    
                    CodeSelectRequestor requestor = new CodeSelectRequestor(nodeToLookFor, unit);
                    TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
                    visitor.visitCompilationUnit(requestor);
                    return requestor.getRequestedElement() != null ? new IJavaElement[] { requestor.getRequestedElement() } : new IJavaElement[0];
                }
            } finally {
                GroovyLogManager.manager.logEnd(event, TraceCategory.CODESELECT);
            }
        }
        return new IJavaElement[0];
    }

    /**
     * @param unit
     * @param nodeToLookFor
     * @return
     */
    private IJavaElement[] returnThisNode(GroovyCompilationUnit unit,
            ASTNode nodeToLookFor) {
        // GRECLIPSE-803 ensure inner classes are handled correctly
        String rawName = ((ClassNode) nodeToLookFor).getNameWithoutPackage();
        String[] enclosingTypes = rawName.split("\\$");
        IType candidate = null;
        for (int i = 0; i < enclosingTypes.length; i++) {
            if (i == 0) {
                candidate = unit.getType(enclosingTypes[i]);
            } else {
                candidate = candidate.getType(enclosingTypes[i]);
            }
        }
        return new IJavaElement[] { candidate };
    }

    /**
     * @param module
     * @param nodeToLookFor
     * @return
     */
    private boolean isTypeDeclaration(ModuleNode module, ASTNode nodeToLookFor) {
        if (nodeToLookFor instanceof ClassNode) {
            for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
                if (clazz == nodeToLookFor) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param r
     * @return
     */
    private ASTNode findASTNodeAt(ModuleNode module, Region r) {
        ASTNodeFinder finder = new ASTNodeFinder(r);
        return finder.doVisit(module);
    }
}
