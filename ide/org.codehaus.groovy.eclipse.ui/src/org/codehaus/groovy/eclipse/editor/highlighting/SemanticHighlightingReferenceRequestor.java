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
package org.codehaus.groovy.eclipse.editor.highlighting;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Position;

/**
 * Finds deprecated/unknown references, GString expressions, regular expressions,
 * field/method/property references, static references, etc.
 */
public class SemanticHighlightingReferenceRequestor extends SemanticReferenceRequestor {

    private static final Position NO_POSITION;
    static {
        NO_POSITION = new Position(0);
        NO_POSITION.delete();
    }

    private char[] contents;
    private final GroovyCompilationUnit unit;
    private Position lastGString = NO_POSITION;
    private static final boolean DEBUG = false; // TODO: Read value using Platform.getDebugOption

    /** Positions of interesting syntax elements within {@link #unit} in increasing lexical order. */
    protected final SortedSet<HighlightedTypedPosition> typedPositions = new TreeSet<>((p1, p2) -> {
        int result = p1.compareTo(p2);
        if (result == 0) {
            // order matching positions by highlighting style
            int x = p1.kind.ordinal(), y = p2.kind.ordinal();
            result = (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
        return result;
    });

    public SemanticHighlightingReferenceRequestor(GroovyCompilationUnit unit) {
        this.unit = unit;
    }

    // be sure to call this before referencing contents array
    private int unitLength() {
        if (contents == null) {
            contents = unit.getContents();
        }
        return contents.length;
    }

    @Override
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        // ignore statements or nodes with invalid source locations
        if (!(node instanceof AnnotatedNode) || node instanceof ImportNode || endOffset(node, result) < 1) {
            if (DEBUG) System.err.println("skipping: " + node);
            return VisitStatus.CONTINUE;
        }

        HighlightedTypedPosition pos = null;
        if (result.confidence == TypeLookupResult.TypeConfidence.UNKNOWN && node.getEnd() > 0) {
            // GRECLIPSE-1327: check to see if this is a synthetic call() on a closure reference
            if (isRealASTNode(node) || node.getText().contains("trait$super$")) {
                Position p = getPosition(node);
                typedPositions.add(new HighlightedTypedPosition(p, HighlightKind.UNKNOWN));
                // don't continue past an unknown reference
                return VisitStatus.CANCEL_BRANCH;
            }
        } else if (!(node instanceof ClassExpression) && GroovyUtils.isDeprecated(result.declaration)) {
            pos = new HighlightedTypedPosition(getPosition(node), HighlightKind.DEPRECATED);
            if (node instanceof ClassNode && ((ClassNode) node).getNameEnd() < 1) {
                int offset = pos.getOffset(), length = pos.getLength();
                if (offset >= 0 && length > 0 && offset + length < unitLength()) {
                    offset = CharOperation.lastIndexOf('.', contents, offset, offset + length);
                    if (offset++ > pos.getOffset()) {
                        pos.setLength(length - (offset - pos.getOffset())); pos.setOffset(offset);
                    }
                }
            }

        } else if (node instanceof ClassNode) {
            // visit "Map" of "Map.Entry" separately
            if (((ClassNode) node).getNameEnd() < 1) {
                checkOuterClass((ClassNode) node, outer -> {
                    acceptASTNode(outer, new TypeLookupResult(outer, outer, outer, TypeLookupResult.TypeConfidence.EXACT, result.scope), enclosingElement);
                });
            }
            if (!(enclosingElement instanceof IImportDeclaration || ((ClassNode) node).isScriptBody())) {
                pos = handleClassReference((ClassNode) node, result.type);
            }

        } else if (result.declaration instanceof FieldNode) {
            pos = handleFieldOrProperty((AnnotatedNode) node, result.declaration);

        } else if (result.declaration instanceof PropertyNode) {
            if (!((PropertyNode) result.declaration).getField().hasNoRealSourcePosition()) {
                pos = handleFieldOrProperty((AnnotatedNode) node, result.declaration);
            } else {
                HighlightKind kind = ((PropertyNode) result.declaration).isStatic()
                            ? HighlightKind.STATIC_CALL : HighlightKind.METHOD_CALL;
                pos = new HighlightedTypedPosition(node.getStart(), node.getLength(), kind);
            }

        } else if (node instanceof MethodNode) {
            if (result.enclosingAnnotation == null) {
                pos = handleMethodDeclaration((MethodNode) node);
            } else {
                pos = handleAnnotationElement(result.enclosingAnnotation, (MethodNode) node);
            }

        } else if (node instanceof ConstructorCallExpression) {
            pos = handleMethodReference((ConstructorCallExpression) node);

        } else if (node instanceof MethodCallExpression) {
            /*pos = handleMethodReference((MethodCallExpression) node);*/

        } else if (node instanceof StaticMethodCallExpression) {
            pos = handleMethodReference((StaticMethodCallExpression) node);

        } else if (node instanceof MethodPointerExpression) {
            /*pos = handleMethodReference((MethodPointerExpression) node);*/

        } else if (node instanceof Parameter) {
            ASTNode var = node.getNodeMetaData("reserved.type.name");
            if (var != null) {
                typedPositions.add(new HighlightedTypedPosition(var.getStart(), var.getLength(), HighlightKind.RESERVED));
            }
            pos = handleParameterReference((Parameter) node, result.scope);

        } else if (node instanceof VariableExpression) {
            if (result.declaration instanceof MethodNode) {
                pos = handleMethodReference((Expression) node, result, false);
            } else {
                pos = handleVariableExpression((VariableExpression) node, result.scope, enclosingElement);
            }
        } else if (node instanceof ConstantExpression) {
            if (result.declaration instanceof MethodNode) {
                boolean isStaticImport = enclosingElement instanceof ImportDeclaration;
                pos = handleMethodReference((Expression) node, result, isStaticImport);
            } else if (result.declaration instanceof VariableExpression) {
                pos = new HighlightedTypedPosition(node.getStart(), node.getLength(), HighlightKind.VARIABLE);
            } else {
                pos = handleConstantExpression((ConstantExpression) node);
                try { // check for regular expression with inline comments
                if (pos != null && pos.kind == HighlightKind.REGEXP && (Pattern.compile(node.getText()).flags() & Pattern.COMMENTS) != 0) {
                    int idx = (contents[node.getStart()] == '$' ? 2 : 1); // start search after '/' or '$/'
                    String pat = String.valueOf(contents, node.getStart(), node.getLength() - idx);
                    while ((idx = pat.indexOf('#', idx)) > 0) {
                        if (pat.charAt(idx - 1) != '\\') {
                            int i = idx, j = (idx = pat.indexOf('\n', idx));
                            int offset = (node.getStart() + i), length = ((j == -1 ? pat.length() : j) - i);
                            typedPositions.add(new HighlightedTypedPosition(offset, length, HighlightKind.COMMENT));
                        }
                    }
                }
                } catch (PatternSyntaxException ignore) {}
            }
        } else if (node instanceof GStringExpression) {
            pos = handleGStringExpression((GStringExpression) node);

        } else if (node instanceof MapEntryExpression) {
            pos = handleMapEntryExpression((MapEntryExpression) node);

        } else if (node instanceof DeclarationExpression) {
            ASTNode var = node.getNodeMetaData("reserved.type.name");
            if (var != null) {
                pos = new HighlightedTypedPosition(var.getStart(), var.getLength(), HighlightKind.RESERVED);
            }
        } else if (DEBUG) {
            String type = node.getClass().getSimpleName();
            if (!type.matches("(Class|Binary|ArgumentList|Closure(List)?|Property|List|Map)Expression"))
                System.err.println("found: " + type);
        }

        //                                        expression nodes can still be valid and have an offset of 0 and a length of 1
        if (pos != null && pos.getLength() > 0 && (node instanceof Expression || pos.getOffset() > 0 || pos.getLength() > 1)) {
            typedPositions.add(pos);
        }

        return VisitStatus.CONTINUE;
    }

    //--------------------------------------------------------------------------

    private void checkOuterClass(ClassNode node, Consumer<ClassNode> todo) {
        ClassNode outer = node.getOuterClass();
        if (outer != null) {
            int start = node.getStart(), until = node.getEnd();
            if (until < unitLength()) {
                until = CharOperation.lastIndexOf('.', contents, start, until);
                if (until > start) {
                    outer = outer.getPlainNodeReference();
                    outer.setStart(start); outer.setEnd(until);
                    start = CharOperation.lastIndexOf('.', contents, start, until);
                    if (start > 0) {
                        outer.setNameStart2(start + 1);
                    }

                    todo.accept(outer);
                }
            }
        }
    }

    private HighlightedTypedPosition handleClassReference(ClassNode node, ClassNode type) {
        int offset, length;
        if (node.getNameEnd() > 0) {
            offset = node.getNameStart();
            length = node.getNameEnd() - offset + 1;
        } else if (node.getStart() > 0 &&
            node.getStart() < unitLength() &&
            contents[node.getStart() - 1] == '@') {
            return null; // GroovyTagScanner does annotations
        } else {
            offset = node.getNameStart2();
            length = node.getEnd() - offset;
        }

        HighlightKind kind;
        if (type.isEnum()) {
            kind = HighlightKind.ENUMERATION;
        } else if (type.isGenericsPlaceHolder()) {
            kind = HighlightKind.PLACEHOLDER;
        } else if (type.isAnnotationDefinition()) {
            kind = HighlightKind.ANNOTATION;
        } else if (type.isInterface()/* <-- must follow isAnnotationDefinition() */) {
            kind = Traits.isTrait(type) ? HighlightKind.TRAIT : HighlightKind.INTERFACE;
        } else {
            kind = type.isAbstract() ? HighlightKind.ABSTRACT_CLASS : HighlightKind.CLASS;
        }

        return new HighlightedTypedPosition(offset, length, kind);
    }

    // field and property declarations and references are handled the same
    private HighlightedTypedPosition handleFieldOrProperty(AnnotatedNode node, ASTNode decl) {
        HighlightKind kind;
        if (!isStatic(decl)) {
            kind = HighlightKind.FIELD;
        } else if (!isFinal(decl)) {
            kind = HighlightKind.STATIC_FIELD;
        } else /* static & final */ {
            kind = HighlightKind.STATIC_VALUE;
        }

        int offset, length;
        if (node == decl) {
            // declaration offsets include the type and init
            offset = node.getNameStart();
            length = node.getNameEnd() - offset + 1;
        } else {
            offset = node.getStart();
            length = node.getLength();
        }

        return new HighlightedTypedPosition(offset, length, kind);
    }

    private HighlightedTypedPosition handleAnnotationElement(AnnotationNode anno, MethodNode elem) {
        try {
            int start = anno.getStart(), until = anno.getEnd();
            String source = unit.getSource().substring(start, until);

            // search for the element label in the source since no AST node exists for it
            Matcher m = Pattern.compile("\\b" + Pattern.quote(elem.getName()) + "\\b").matcher(source);
            if (m.find()) {
                return new HighlightedTypedPosition(start + m.start(), elem.getName().length(), HighlightKind.TAG_KEY);
            }
        } catch (Exception e) {
            Util.log(e);
        }
        return null;
    }

    private HighlightedTypedPosition handleMethodDeclaration(MethodNode node) {
        HighlightKind kind;
        if (node instanceof ConstructorNode) {
            kind = HighlightKind.CTOR;
        } else if (!isStatic(node)) {
            kind = HighlightKind.METHOD;
        } else {
            kind = HighlightKind.STATIC_METHOD;
        }

        int offset = node.getNameStart(),
            length = node.getNameEnd() - offset + 1;

        // special case: string literal method names
        if (kind != HighlightKind.CTOR && length > node.getName().length()) {
            return null;
        }
        return new HighlightedTypedPosition(offset, length, kind);
    }

    private HighlightedTypedPosition handleMethodReference(MethodCallExpression expr) {
        HighlightKind kind;
        if (expr.getObjectExpression() instanceof ClassExpression) {
            kind = HighlightKind.STATIC_CALL;
        } else {
            kind = HighlightKind.METHOD_CALL;
        }

        int offset = expr.getMethod().getStart(),
            length = expr.getMethod().getLength();

        return new HighlightedTypedPosition(offset, length, kind);
    }

    private HighlightedTypedPosition handleMethodReference(ConstructorCallExpression expr) {
        if (expr.isSpecialCall()) return null; // handled by GroovyTagScanner

        // nameStart works most of the time (incl. @Newify); nameStart2 is for qualified types
        int offset = Math.max(expr.getNameStart(), expr.getType().getNameStart2()),
            length = expr.getNameEnd() - offset + 1;

        return new HighlightedTypedPosition(offset, length, HighlightKind.CTOR_CALL);
    }

    private HighlightedTypedPosition handleMethodReference(StaticMethodCallExpression expr) {
        int offset = expr.getNameStart(),
            length = expr.getNameEnd() - offset + 1;

        return new HighlightedTypedPosition(offset, length, HighlightKind.STATIC_CALL);
    }

    private HighlightedTypedPosition handleMethodReference(MethodPointerExpression expr) {
        HighlightKind kind;
        if (expr.getExpression() instanceof ClassExpression) {
            kind = HighlightKind.STATIC_CALL;
        } else {
            kind = HighlightKind.METHOD_CALL;
        }

        int offset = expr.getMethodName().getStart(),
            length = expr.getMethodName().getLength();

        return new HighlightedTypedPosition(offset, length, kind);
    }

    private HighlightedTypedPosition handleMethodReference(Expression expr, TypeLookupResult result, boolean isStaticImport) {
        HighlightKind kind = HighlightKind.METHOD_CALL;
        if (result.isGroovy) {
            kind = HighlightKind.GROOVY_CALL;
        } else if (isStaticImport || ((MethodNode) result.declaration).isStatic()) {
            kind = HighlightKind.STATIC_CALL;
        }

        int offset, length;
        if (expr.getNameEnd() < 1) {
            offset = expr.getStart();
            length = expr.getLength();
        } else {
            offset = expr.getNameStart();
            length = expr.getNameEnd() - offset + 1;
        }

        return new HighlightedTypedPosition(offset, length, kind);
    }

    private HighlightedTypedPosition handleParameterReference(final Parameter param, final VariableScope scope) {
        HighlightKind kind;
        if (isCatchParam(param, scope) || isForLoopParam(param, scope)) {
            kind = HighlightKind.VARIABLE; // treat block params as vars
        } else {
            kind = HighlightKind.PARAMETER;
        }

        return new HighlightedTypedPosition(param.getNameStart(), param.getNameEnd() - param.getNameStart() + 1, kind);
    }

    // could be local variable declaration, local variable reference, for-each parameter reference, or method parameter reference
    private HighlightedTypedPosition handleVariableExpression(VariableExpression expr, VariableScope scope, IJavaElement source) {
        boolean isParam = (expr.getAccessedVariable() instanceof Parameter &&
                !isForLoopParam(expr.getAccessedVariable(), scope)) &&
                !isCatchParam(expr.getAccessedVariable(), scope);
        boolean isIt = (isParam && "it".equals(expr.getName()) &&
                (((Parameter) expr.getAccessedVariable()).getLineNumber() <= 0));
        boolean isSuperOrThis = "super".equals(expr.getName()) || "this".equals(expr.getName());

        // free vars and loop vars are okay as long as they are not reserved words (this, super); params must refer to "real" declarations
        if (!(isSuperOrThis && !lastGString.includes(expr.getStart())) &&
                (!isParam || isIt || (((Parameter) expr.getAccessedVariable()).getLineNumber() > 0) || source instanceof SourceType)) {
            HighlightKind kind;
            if (isParam) {
                kind = isIt ? HighlightKind.GROOVY_CALL : HighlightKind.PARAMETER;
            } else {
                kind = isSuperOrThis ? HighlightKind.KEYWORD : HighlightKind.VARIABLE;
            }
            return new HighlightedTypedPosition(expr.getStart(), expr.getLength(), kind);
        }
        return null;
    }

    private HighlightedTypedPosition handleConstantExpression(ConstantExpression expr) {
        if (isNumber(expr.getType())) {
            return new HighlightedTypedPosition(expr.getStart(), expr.getLength(), HighlightKind.NUMBER);
        } else if (!lastGString.includes(expr.getStart()) && isSlashy(expr)) {
            return new HighlightedTypedPosition(expr.getStart(), expr.getLength(), HighlightKind.REGEXP);
        }
        return null;
    }

    private HighlightedTypedPosition handleGStringExpression(GStringExpression expr) {
        // save to help deal with forthcoming ConstantExpression nodes
        lastGString = new Position(expr.getStart(), expr.getLength());

        boolean isSlashy = isSlashy(expr);
        boolean isTriple = !isSlashy && isTriple(expr);
        int pre = (isTriple ? 3 : (contents[expr.getStart()] == '$' ? 2 : 1));

        for (ConstantExpression string : expr.getStrings()) {
            int offset = string.getStart() + pre,
                length = string.getLength() - pre;
            if (string != last(expr.getStrings())) {
                pre = (contents[offset + length - 1] == '{' ? 2 : 1);
            } else {
                pre = (isTriple ? 3 : (contents[expr.getStart()] == '$' ? 2 : 1));
            }
            length -= pre; // adjust for trailing "$" or "${" or end quote(s)

            if (length > 0 && lastGString.includes(offset + length)) {
                typedPositions.add(new HighlightedTypedPosition(offset, length,
                        isSlashy ? HighlightKind.REGEXP : HighlightKind.STRING));
            }

            pre -= 1; // prepare for leading "" or "}"
        }

        return new HighlightedTypedPosition(lastGString, HighlightKind.GROOVY_CALL);
    }

    private HighlightedTypedPosition handleMapEntryExpression(MapEntryExpression expr) {
        Expression key = expr.getKeyExpression();
        if (key instanceof ConstantExpression && key.getEnd() > 0 && key.getStart() == expr.getStart()) {
            char c;
            unitLength(); // ensure loaded
            if (key.getStart() < contents.length && (c = contents[key.getStart()]) != '\'' && c != '"' && c != '/') {
                return new HighlightedTypedPosition(key.getStart(), key.getLength(), HighlightKind.MAP_KEY);
            }
        }
        return null;
    }

    private int endOffset(ASTNode node, TypeLookupResult result) {
        int offset = node.getEnd();
        if (result.enclosingAnnotation != null) {
            offset = result.enclosingAnnotation.getEnd();
            // TODO: Probably could be more accurate, but doesn't need to be at the moment...
        }
        return offset;
    }

    /**
     * An AST node is "real" if it is an expression and the
     * text of the expression matches the actual text in the file
     */
    private boolean isRealASTNode(ASTNode node) {
        String text = node.getText();
        if (text.length() != node.getLength()) {
            return false;
        }
        int contentsLength = unitLength();
        char[] textArr = text.toCharArray();
        for (int i = 0, j = node.getStart(); i < textArr.length && j < contentsLength; i += 1, j += 1) {
            if (textArr[i] != contents[j]) {
                return false;
            }
        }
        return true;
    }

    private boolean isSlashy(Expression expr) {
        if (expr.getStart() < expr.getEnd() && expr.getEnd() <= unitLength()) {
            // check for /.../ or $/.../$ form of string literal (usually a regex literal)
            boolean slashy = contents[expr.getStart()] == '/' && contents[expr.getEnd() - 1] == '/';
            boolean dollar = !slashy && contents[expr.getStart()] == '$' && contents[expr.getStart() + 1] == '/' &&
                                        contents[expr.getEnd() - 2] == '/' && contents[expr.getEnd() - 1] == '$';
            if (slashy || dollar) {
                return true;
            }
        }
        return false;
    }

    private boolean isTriple(Expression expr) {
        if (expr.getStart() < expr.getEnd() && expr.getEnd() <= unitLength()) {
            String source = String.valueOf(contents, expr.getStart(), expr.getLength());
            return source.startsWith("\"\"\"");
        }
        return false;
    }
}
