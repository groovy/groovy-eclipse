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
package org.codehaus.groovy.eclipse.search;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.internal.ui.search.IOccurrencesFinder;

/**
 *
 * @author Andrew Eisenberg
 * @created Dec 31, 2009
 */
public class GroovyOccurrencesFinder implements IOccurrencesFinder {
    public static final String ID= "GroovyOccurrencesFinder"; //$NON-NLS-1$

    public static final String IS_WRITEACCESS= "writeAccess"; //$NON-NLS-1$
    public static final String IS_VARIABLE= "variable"; //$NON-NLS-1$

    private GroovyCompilationUnit gunit;

    private CompilationUnit cunit;

    private AnnotatedNode nodeToLookFor;

    private String elementName;

    public CompilationUnit getASTRoot() {
        return cunit;
    }

    public String getElementName() {
        if (elementName == null) {
            elementName = internalGetElementName();
        }
        return elementName;
    }

    private String internalGetElementName() {
        if (nodeToLookFor instanceof ClassNode) {
            // handle any potential inner classes
            String name = ((ClassNode) nodeToLookFor).getNameWithoutPackage();
            int lastDollar = name.lastIndexOf('$');
            return name.substring(lastDollar + 1);
        } else if (nodeToLookFor instanceof MethodNode) {
            return ((MethodNode) nodeToLookFor).getName();
        } else if (nodeToLookFor instanceof FieldNode) {
            return ((FieldNode) nodeToLookFor).getName();
        } else if (nodeToLookFor instanceof Variable) {
            return ((Variable) nodeToLookFor).getName();
        }
        return null;
    }

    public String getID() {
        return ID;
    }

    public String getJobLabel() {
        return "Search for Occurrences in File (Groovy)";
    }

    public OccurrenceLocation[] getOccurrences() {
        Map<org.codehaus.groovy.ast.ASTNode, Integer> occurences = internalFindOccurences();
        OccurrenceLocation[] locations = new OccurrenceLocation[occurences.size()];
        int i = 0;
        for (Entry<org.codehaus.groovy.ast.ASTNode, Integer> entry : occurences.entrySet()) {
            org.codehaus.groovy.ast.ASTNode node = entry.getKey();
            int flag = entry.getValue();
            OccurrenceLocation occurrenceLocation;
            if (node instanceof FieldNode) {
                FieldNode c = (FieldNode) node;
                occurrenceLocation = new OccurrenceLocation(c.getNameStart(), c.getNameEnd() - c.getNameStart() + 1, flag,
                        "Occurrence of ''" + getElementName() + "''");
            } else if (node instanceof MethodNode) {
                MethodNode c = (MethodNode) node;
                occurrenceLocation = new OccurrenceLocation(c.getNameStart(), c.getNameEnd() - c.getNameStart() + 1, flag,
                        "Occurrence of ''" + getElementName() + "''");
            } else if (node instanceof Parameter) {
                // should be finding the start and end of the name region only,
                // but this finds the entire declaration
                Parameter c = (Parameter) node;
                int start = c.getNameStart();
                int length = c.getNameEnd() - c.getNameStart();
                occurrenceLocation = new OccurrenceLocation(start, length, flag, "Occurrence of ''" + getElementName()
                        + "''");
            } else if (node instanceof ClassNode && ((ClassNode) node).getNameEnd() > 0) {
                // class declaration
                ClassNode c = (ClassNode) node;
                occurrenceLocation = new OccurrenceLocation(c.getNameStart(), c.getNameEnd() - c.getNameStart() + 1, flag,
                        "Occurrence of ''" + getElementName() + "''");
            } else if (node instanceof StaticMethodCallExpression) {
                // special case...for static method calls, the start and end are
                // of the entire expression, but we just want the name.
                StaticMethodCallExpression smce = (StaticMethodCallExpression) node;
                occurrenceLocation = new OccurrenceLocation(smce.getStart(), Math.min(smce.getLength(), smce.getMethod().length()), flag,
                        "Occurrence of ''" + getElementName() + "''");
            } else {
                SourceRange range = getSourceRange(node);

                occurrenceLocation = new OccurrenceLocation(range.getOffset(), range.getLength(), flag, "Occurrence of ''"
                        + getElementName() + "''");
            }
            locations[i++] = occurrenceLocation;
        }
        return locations;
    }

    private SourceRange getSourceRange(org.codehaus.groovy.ast.ASTNode node) {
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

    private Map<org.codehaus.groovy.ast.ASTNode, Integer> internalFindOccurences() {
        if (nodeToLookFor != null && !(nodeToLookFor instanceof ConstantExpression)
                && !(nodeToLookFor instanceof ClosureExpression) && !(nodeToLookFor instanceof DeclarationExpression)
                && !(nodeToLookFor instanceof BinaryExpression)
                && !(nodeToLookFor instanceof MethodCallExpression)) {
            FindAllReferencesRequestor requestor = new FindAllReferencesRequestor(nodeToLookFor);
            TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(gunit);
            visitor.visitCompilationUnit(requestor);
            Map<org.codehaus.groovy.ast.ASTNode, Integer> occurences = requestor.getReferences();
            return occurences;
        } else {
            return Collections.emptyMap();
        }
    }

    public int getSearchKind() {
        return K_OCCURRENCE;
    }

    public String getUnformattedPluralLabel() {
        return "''{0}'' - {1} occurrences in ''{2}''";
    }

    public String getUnformattedSingularLabel() {
        return "''{0}'' - 1 occurrence in ''{1}''";
    }

    public String initialize(CompilationUnit root, ASTNode node) {
        return initialize(root, node.getStartPosition(), node.getLength());
    }

    /**
     * Finds the {@link org.codehaus.groovy.ast.ASTNode} to look for
     */
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
        org.codehaus.groovy.ast.ASTNode node = helper.selectASTNode(gunit, offset, length);

        if (!(node instanceof AnnotatedNode)) {
            return "Can't find occurrences...invalid selection";
        }
        if (node instanceof PropertyNode) {
            PropertyNode property = (PropertyNode) node;
            // hmmm...how do we handle synthetic field nodes based on a getter
            // or setter?
            node = property.getField();
        }
        nodeToLookFor = (AnnotatedNode) node;
        return null;
    }

    public void setGroovyCompilationUnit(GroovyCompilationUnit gunit) {
        this.gunit = gunit;
    }

    public org.codehaus.groovy.ast.ASTNode getNodeToLookFor() {
        return nodeToLookFor;
    }
}
