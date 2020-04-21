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
package org.codehaus.groovy.eclipse.search;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.CodeSelectHelper;
import org.codehaus.groovy.eclipse.core.search.FindAllReferencesRequestor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.internal.core.manipulation.search.IOccurrencesFinder;
import org.eclipse.jdt.internal.ui.search.FindOccurrencesEngine;

public class GroovyOccurrencesFinder implements IOccurrencesFinder {

    private AnnotatedNode nodeToLookFor;
    private GroovyCompilationUnit gunit;
    private CompilationUnit cunit;
    private String elementName;

    @Override
    public String getElementName() {
        if (elementName == null) {
            if (nodeToLookFor instanceof ClassNode) {
                // handle any potential inner classes
                String name = ((ClassNode) nodeToLookFor).getNameWithoutPackage();
                int lastDollar = name.lastIndexOf('$');
                elementName = name.substring(lastDollar + 1);
            } else if (nodeToLookFor instanceof MethodNode) {
                elementName = ((MethodNode) nodeToLookFor).getName();
            } else if (nodeToLookFor instanceof FieldNode) {
                elementName = ((FieldNode) nodeToLookFor).getName();
            } else if (nodeToLookFor instanceof Variable) {
                elementName = ((Variable) nodeToLookFor).getName();
            }
        }
        return elementName;
    }

    public ASTNode getNodeToLookFor() {
        return nodeToLookFor;
    }

    @Override
    public CompilationUnit getASTRoot() {
        return cunit;
    }

    @Override
    public OccurrenceLocation[] getOccurrences() {
        Map<ASTNode, Integer> occurences = internalFindOccurences();
        OccurrenceLocation[] locations = new OccurrenceLocation[occurences.size()];
        int i = 0;
        for (Entry<ASTNode, Integer> entry : occurences.entrySet()) {
            ASTNode node = entry.getKey();
            int flags = entry.getValue();
            OccurrenceLocation occurrenceLocation;
            if ((node instanceof ClassNode && ((ClassNode) node).getNameEnd() > 0) || node instanceof Parameter || node instanceof FieldNode || node instanceof MethodNode || node instanceof StaticMethodCallExpression) {
                AnnotatedNode n = (AnnotatedNode) node;
                occurrenceLocation = new OccurrenceLocation(n.getNameStart(), n.getNameEnd() - n.getNameStart() + 1, flags, "Occurrence of ''" + getElementName() + "''");
            } else {
                SourceRange r = getSourceRange(node);
                occurrenceLocation = new OccurrenceLocation(r.getOffset(), r.getLength(), flags, "Occurrence of ''" + getElementName() + "''");
            }
            locations[i++] = occurrenceLocation;
        }
        return locations;
    }

    private SourceRange getSourceRange(ASTNode node) {
        if (node instanceof ConstructorCallExpression) {
            // want to select the type name, not the entire expression
            node = ((ConstructorCallExpression) node).getType();
        }
        if (node instanceof ClassNode) {
            // handle inner classes referenced semi-qualified
            String semiQualifiedName = ((ClassNode) node).getNameWithoutPackage();
            if (semiQualifiedName.contains("$")) {
                // it is semiqualified if the length is larger than the name
                String name = getElementName();
                if (name.length() < node.getLength() - 1) {
                    // we know that the name is semiqualified
                    // find the simple name inside the semi-qualified name
                    int simpleNameStart = semiQualifiedName.indexOf(name);
                    if (simpleNameStart >= 0) {
                        return new SourceRange(node.getStart() + simpleNameStart, name.length());
                    }
                }
            }
        }
        return new SourceRange(node.getStart(), node.getLength());
    }

    private Map<ASTNode, Integer> internalFindOccurences() {
        if (nodeToLookFor != null &&
                !(nodeToLookFor instanceof ConstantExpression) &&
                !(nodeToLookFor instanceof ClosureExpression) &&
                !(nodeToLookFor instanceof DeclarationExpression) &&
                !(nodeToLookFor instanceof BinaryExpression) &&
                !(nodeToLookFor instanceof MethodCallExpression)) {
            FindAllReferencesRequestor requestor = new FindAllReferencesRequestor(nodeToLookFor);
            TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(gunit);
            visitor.visitCompilationUnit(requestor);
            Map<ASTNode, Integer> occurences = requestor.getReferences();
            return occurences;
        }
        return Collections.emptyMap();
    }

    /**
     * Finds the {@link ASTNode} to look for.
     */
    @Override
    public String initialize(CompilationUnit root, int offset, int length) {
        cunit = root;
        if (gunit == null) {
            ITypeRoot typeRoot = cunit.getTypeRoot();
            if (!(typeRoot instanceof GroovyCompilationUnit)) {
                return "Can't find occurrenes...not a Groovy file.";
            }
            gunit = (GroovyCompilationUnit) typeRoot;
        }
        ModuleNode moduleNode = gunit.getModuleNode();
        if (moduleNode == null) {
            return "Can't find occurrences...no module node.";
        }

        CodeSelectHelper helper = new CodeSelectHelper();
        ASTNode node = helper.selectASTNode(gunit, offset, length);

        if (!(node instanceof AnnotatedNode)) {
            return "Can't find occurrences...invalid selection";
        }
        if (node instanceof PropertyNode) {
            PropertyNode property = (PropertyNode) node;
            // hmmm...how do we handle synthetic field nodes based on a getter or setter?
            node = property.getField();
        }
        nodeToLookFor = (AnnotatedNode) node;
        return null;
    }

    @Override
    public String initialize(CompilationUnit root, org.eclipse.jdt.core.dom.ASTNode node) {
        return initialize(root, node.getStartPosition(), node.getLength());
    }

    //--------------------------------------------------------------------------

    // for FindOccurrencesTests only!
    public final void setGroovyCompilationUnit(GroovyCompilationUnit gunit) {
        this.gunit = gunit;
    }

    // for GroovyEditor only!
    public static OccurrenceLocation[] findOccurrences(CompilationUnit cunit, int offset, int length) {
        GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder();
        finder.initialize(cunit, offset, length);
        return finder.getOccurrences();
    }

    public static FindOccurrencesEngine newFinderEngine() {
        return FindOccurrencesEngine.create(new GroovyOccurrencesFinder());
    }

    @Override
    public String getID() {
        return "GroovyOccurrencesFinder";
    }

    @Override
    public String getJobLabel() {
        return "Search for Occurrences in File (Groovy)";
    }

    @Override
    public int getSearchKind() {
        return K_OCCURRENCE;
    }

    @Override
    public String getUnformattedSingularLabel() {
        return "''{0}'' - 1 occurrence in ''{1}''";
    }

    @Override
    public String getUnformattedPluralLabel() {
        return "''{0}'' - {1} occurrences in ''{2}''";
    }
}
