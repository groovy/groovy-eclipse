/*
 * Copyright 2009-2017 the original author or authors.
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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.internal.ui.search.FindOccurrencesEngine;

public class GroovyOccurrencesFinder {

    private AnnotatedNode nodeToLookFor;
    private GroovyCompilationUnit gunit;
    private CompilationUnit cunit;
    private String elementName;

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

    public OccurrenceLocation[] getOccurrences() {
        Map<ASTNode, Integer> occurences = internalFindOccurences();
        OccurrenceLocation[] locations = new OccurrenceLocation[occurences.size()];
        int i = 0;
        for (Entry<ASTNode, Integer> entry : occurences.entrySet()) {
            ASTNode node = entry.getKey();
            int flag = entry.getValue();
            OccurrenceLocation occurrenceLocation;
            if (node instanceof FieldNode) {
                FieldNode c = (FieldNode) node;
                occurrenceLocation = new OccurrenceLocation(c.getNameStart(), c.getNameEnd() - c.getNameStart() + 1, flag, "Occurrence of ''" + getElementName() + "''");
            } else if (node instanceof MethodNode) {
                MethodNode c = (MethodNode) node;
                occurrenceLocation = new OccurrenceLocation(c.getNameStart(), c.getNameEnd() - c.getNameStart() + 1, flag, "Occurrence of ''" + getElementName() + "''");
            } else if (node instanceof Parameter) {
                // should be finding the start and end of the name region only,
                // but this finds the entire declaration
                Parameter c = (Parameter) node;
                int start = c.getNameStart();
                int length = c.getNameEnd() - c.getNameStart();
                occurrenceLocation = new OccurrenceLocation(start, length, flag, "Occurrence of ''" + getElementName() + "''");
            } else if (node instanceof ClassNode && ((ClassNode) node).getNameEnd() > 0) {
                // class declaration
                ClassNode c = (ClassNode) node;
                occurrenceLocation = new OccurrenceLocation(c.getNameStart(), c.getNameEnd() - c.getNameStart() + 1, flag, "Occurrence of ''" + getElementName() + "''");
            } else if (node instanceof StaticMethodCallExpression) {
                // special case...for static method calls, the start and end are
                // of the entire expression, but we just want the name.
                StaticMethodCallExpression smce = (StaticMethodCallExpression) node;
                occurrenceLocation = new OccurrenceLocation(smce.getStart(), Math.min(smce.getLength(), smce.getMethod().length()), flag, "Occurrence of ''" + getElementName() + "''");
            } else {
                SourceRange range = getSourceRange(node);

                occurrenceLocation = new OccurrenceLocation(range.getOffset(), range.getLength(), flag, "Occurrence of ''" + getElementName() + "''");
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

    public String initialize(CompilationUnit root, org.eclipse.jdt.core.dom.ASTNode node) {
        return initialize(root, node.getStartPosition(), node.getLength());
    }

    //--------------------------------------------------------------------------

    // for FindOccurrencesTests only!
    public final void setGroovyCompilationUnit(GroovyCompilationUnit gunit) {
        this.gunit = gunit;
    }

    // for GroovyEditor only!
    public static Object findOccurrences(CompilationUnit cunit, int offset, int length) {
        GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder();
        finder.initialize(cunit, offset, length);
        OccurrenceLocation[] occurrences = finder.getOccurrences();

        // convert to required types reflectively
        try {
            if (OCCURRENCE_LOCATION == null) {
                try {
                    OCCURRENCE_LOCATION = Class.forName("org.eclipse.jdt.internal.ui.search.IOccurrencesFinder$OccurrenceLocation");
                } catch (ClassNotFoundException e) {
                    OCCURRENCE_LOCATION = Class.forName("org.eclipse.jdt.internal.core.manipulation.search.IOccurrencesFinder$OccurrenceLocation");
                }
                OCCURRENCE_LOCATION_CTOR = OCCURRENCE_LOCATION.getConstructor(int.class, int.class, int.class, String.class);
            }

            Object arr = Array.newInstance(OCCURRENCE_LOCATION, occurrences.length);
            for (int idx = 0; idx < occurrences.length; idx += 1) {
                OccurrenceLocation loc = occurrences[idx];
                Array.set(arr, idx, OCCURRENCE_LOCATION_CTOR.newInstance(
                    loc.getOffset(), loc.getLength(), loc.getFlags(), loc.getDescription()));
            }

            return arr;

        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    /**
     * FindOccurrencesEngine expects an instance of IOccurrencesFinder, which
     * has been relocated sometime between Eclipse 4.7m4 and 4.7m5. In order
     * to support both interfaces, a dynamic proxy is supplied to the engine.
     */
    public static FindOccurrencesEngine newFinderEngine() {
        try {
            if (I_OCCURRENCES_FINDER == null) {
                // load the interface
                try {
                    I_OCCURRENCES_FINDER = new Class[] {Class.forName("org.eclipse.jdt.internal.ui.search.IOccurrencesFinder")};
                } catch (ClassNotFoundException e) {
                    I_OCCURRENCES_FINDER = new Class[] {Class.forName("org.eclipse.jdt.internal.core.manipulation.search.IOccurrencesFinder")};
                }
            }
            final GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder();

            // create invocation handler
            InvocationHandler handler = new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String methodName = method.getName();

                    if ("getASTRoot".equals(methodName)) {
                        return finder.cunit;
                    }
                    if ("getID".equals(methodName)) {
                        return "GroovyOccurrencesFinder";
                    }
                    if ("getJobLabel".equals(methodName)) {
                        return "Search for Occurrences in File (Groovy)";
                    }
                    if ("getSearchKind".equals(methodName)) {
                        return 5; //IOccurrencesFinder.K_OCCURRENCE
                    }
                    if ("getUnformattedPluralLabel".equals(methodName)) {
                        return "''{0}'' - {1} occurrences in ''{2}''";
                    }
                    if ("getUnformattedSingularLabel".equals(methodName)) {
                        return "''{0}'' - 1 occurrence in ''{1}''";
                    }

                    return GroovyOccurrencesFinder.class.getMethod(methodName, method.getParameterTypes()).invoke(finder, args);
                }
            };

            // create dynamic proxy to connect IOccurrencesFinder with GroovyOccurrencesFinder
            Object proxy = Proxy.newProxyInstance(GroovyOccurrencesFinder.class.getClassLoader(), I_OCCURRENCES_FINDER, handler);

            //return FindOccurrencesEngine.create(proxy of IOccurrencesFinder);
            return (FindOccurrencesEngine) ReflectionUtils.throwableExecutePrivateMethod(
                FindOccurrencesEngine.class, "create", I_OCCURRENCES_FINDER, FindOccurrencesEngine.class, new Object[] {proxy});

        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    // copied from IOccurrencesFinder
    public static class OccurrenceLocation {
        private final int fOffset;
        private final int fLength;
        private final int fFlags;
        private final String fDescription;

        public OccurrenceLocation(int offset, int length, int flags, String description) {
            fOffset= offset;
            fLength= length;
            fFlags= flags;
            fDescription= description;
        }

        public int getOffset() {
            return fOffset;
        }

        public int getLength() {
            return fLength;
        }

        public int getFlags() {
            return fFlags;
        }

        public String getDescription() {
            return fDescription;
        }

        @Override
        public String toString() {
            return "[" + fOffset + " / " + fLength + "] " + fDescription;
        }
    }

    private static Class<?> OCCURRENCE_LOCATION;
    private static Class<?>[] I_OCCURRENCES_FINDER;
    private static Constructor<?> OCCURRENCE_LOCATION_CTOR;
}
