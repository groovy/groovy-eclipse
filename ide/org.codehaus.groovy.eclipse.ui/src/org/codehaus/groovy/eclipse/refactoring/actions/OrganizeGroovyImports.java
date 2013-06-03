/*
 * Copyright 2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import greclipse.org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import greclipse.org.eclipse.jdt.ui.CodeStyleConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author andrew
 * Organize import operation for groovy files
 */
public class OrganizeGroovyImports {

    /**
     * From {@link OrganizeImportsOperation.TypeReferenceProcessor.UnresolvedTypeData}
     */
    public static class UnresolvedTypeData {
        final String ref;
        final boolean isAnnotation;
        final List<TypeNameMatch> foundInfos;
        final ISourceRange range;

        public UnresolvedTypeData(String ref, boolean annotation, ISourceRange range) {
            this.ref = ref;
            this.isAnnotation = annotation;
            this.foundInfos = new LinkedList<TypeNameMatch>();
            this.range = range;
        }
        public void addInfo(TypeNameMatch info) {
            for (int i= this.foundInfos.size() - 1; i >= 0; i--) {
                TypeNameMatch curr= (TypeNameMatch) this.foundInfos.get(i);
                if (curr.getTypeContainerName().equals(info.getTypeContainerName())) {
                    return; // not added. already contains type with same name
                }
            }
            foundInfos.add(info);
        }

        public List<TypeNameMatch> getFoundInfos() {
            return foundInfos;
        }
    }

    private class FindUnresolvedReferencesVisitor extends ClassCodeVisitorSupport {

        private ClassNode current;

        // not needed
        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public void visitCastExpression(CastExpression expression) {
            handleType(expression.getType(), false);
            super.visitCastExpression(expression);
        }

        @Override
        public void visitClassExpression(ClassExpression expression) {
            handleType(expression.getType(), false);
        }

        @Override
        public void visitConstructorCallExpression(
                ConstructorCallExpression call) {
            handleType(call.getType(), false);
            super.visitConstructorCallExpression(call);
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            handleType(expression.getType(), false);
            if (expression.getAccessedVariable() instanceof DynamicVariable) {
                handleVariableExpression(expression);
            }
        }

        @Override
        public void visitField(FieldNode node) {
            // can't check for synthetic here because
            // it seems that non-synthetic nodes are being marked as synthetic
            if (! node.getName().startsWith("_") && !node.getName().startsWith("$")) {
                handleType(node.getType(), false);
            }
            super.visitField(node);
        }

        @Override
        public void visitConstructor(ConstructorNode node) {
            if (!node.isSynthetic()) {
                for (Parameter param : node.getParameters()) {
                    handleType(param.getType(), false);
                }
            }
            super.visitConstructor(node);
        }

        @Override
        public void visitMethod(MethodNode node) {
            if (!node.isSynthetic()) {
                handleType(node.getReturnType(), false);
                for (Parameter param : node.getParameters()) {
                    handleType(param.getType(), false);
                }
                ClassNode[] thrownExceptions = node.getExceptions();
                if (thrownExceptions != null) {
                    for (ClassNode thrownException : thrownExceptions) {
                        handleType(thrownException, false);
                    }
                }
            }
            super.visitMethod(node);
        }

        @Override
        public void visitClass(ClassNode node) {
            current = node;
            if (!node.isSynthetic()) {
                handleType(node.getSuperClass(), false);
                for (ClassNode impls : node.getInterfaces()) {
                    handleType(impls, false);
                }
            }
            super.visitClass(node);
        }

        @Override
        public void visitClosureExpression(ClosureExpression node) {
            Parameter[] parameters = node.getParameters();
            if (parameters != null) {
                for (Parameter param : parameters) {
                    handleType(param.getType(), false);
                }
            }
            super.visitClosureExpression(node);
        }

        @Override
        public void visitAnnotations(AnnotatedNode node) {
            List<AnnotationNode> annotations = (List<AnnotationNode>) node.getAnnotations();
            if (annotations != null && !annotations.isEmpty()) {
                for (AnnotationNode an : annotations) {
                    if (an.isBuiltIn()) {
                        continue;
                    }
                    handleType(an.getClassNode(), true);
                    for (Map.Entry<String, Expression> member : an.getMembers().entrySet()) {
                        Expression value = member.getValue();
                        value.visit(this);
                    }
                }
            }
        }

        @Override
        public void visitConstantExpression(ConstantExpression node) {
            if (node instanceof AnnotationConstantExpression) {
                handleType(node.getType(), true);
            }
        }

        @Override
        public void visitCatchStatement(CatchStatement node) {
            handleType(node.getVariable().getType(), false);
            super.visitCatchStatement(node);
        }

        /**
         * Assume dynamic variables are a candidate for Organize imports,
         * but only if name begins with a capital. This will hopefully
         * filter out most false positives, but will miss
         * types that start with lower case.
         *
         * @param expr
         */
        private void handleVariableExpression(VariableExpression expr) {
            if (Character.isUpperCase(expr.getName().charAt(0)) &&
                    !missingTypes.containsKey(expr.getName())) {
                missingTypes.put(expr.getName(),
                        new UnresolvedTypeData(expr.getName(), false,
                                new SourceRange(expr.getStart(), expr.getEnd()-expr.getStart())));
            }
        }

        /**
         * add the type name to missingTypes if it is not resolved
         * ensure that we don't remove the import if the type is resolved
         */
        private void handleType(ClassNode node, boolean isAnnotation) {
            if (!node.isResolved() && node.redirect() != current) {
                // there may be a partial qualifier if
                // the type referenced is an inner type.
                // in this case, only take the first part
                String semiQualifiedName;
                if (node.isArray()) {
                    semiQualifiedName = node.getComponentType().getName();
                } else {
                    semiQualifiedName = node.getName();
                }
                String simpleName = semiQualifiedName.split("\\.")[0];
                if (!missingTypes.containsKey(simpleName)) {
                    missingTypes.put(simpleName,
                            new UnresolvedTypeData(simpleName, isAnnotation,
                                    new SourceRange(node.getStart(), node.getEnd()-node.getStart())));
                }
            } else {
                // We don't know exactly what the
                // text is.  We just know how it resolves
                // This can be a problem if an inner class.
                // We don't really know what is in the text
                // and we don't really know what is the import
                // So, just ensure that none are slated for removal
                String name;
                if (node.isArray()) {
                    name = node.getComponentType().getName();
                } else {
                    name = node.getName();
                }

                String partialName = name.replace('$', '.');
                int innerIndex = name.lastIndexOf('$');
                while (innerIndex > -1) {
                    doNotRemoveImport(partialName);
                    partialName = partialName.substring(0, innerIndex);
                    innerIndex = name.lastIndexOf('$', innerIndex-1);
                }
                doNotRemoveImport(partialName);
            }

            if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
                for (GenericsType gen : node.getGenericsTypes()) {
                    if (gen.getLowerBound() != null) {
                        handleType(gen.getLowerBound(), false);
                    }
                    if (gen.getUpperBounds() != null) {
                        for (ClassNode upper : gen.getUpperBounds()) {
                            // handle enums where the upper bound is the same as the type
                            if (! upper.getName().equals(node.getName())) {
                                handleType(upper, false);
                            }
                        }
                    }
                    if (gen.getType() != null && gen.getType().getName().charAt(0) != '?' && !gen.isPlaceholder()) {
                        handleType(gen.getType(), false);
                    }
                }
            }
        }

        private void doNotRemoveImport(String className) {
            importsSlatedForRemoval.remove(className);
        }
    }

    private final GroovyCompilationUnit unit;
    private Map<String, UnresolvedTypeData> missingTypes;

    private Map<String, ImportNode> importsSlatedForRemoval;

    private IChooseImportQuery query;

    public OrganizeGroovyImports(GroovyCompilationUnit unit, IChooseImportQuery query) {
        this.unit = unit;
        this.query = query;
    }

    public TextEdit calculateMissingImports() {
        ModuleNode node = unit.getModuleNode();
        if (node == null || node.encounteredUnrecoverableError()) {
            // no AST probably a syntax error...do nothing
            return null;
        }

        if (isEmpty(node)) {
            return new MultiTextEdit();
        }

        missingTypes = new HashMap<String,UnresolvedTypeData>();
        importsSlatedForRemoval = new HashMap<String, ImportNode>();
        FindUnresolvedReferencesVisitor visitor = new FindUnresolvedReferencesVisitor();
        Map<String, String> aliases = new HashMap<String, String>();

        try {
            // However, this leads to GRECLIPSE-1390 where imports are no longer reordered and sorted.
            // configure import rewriter to keep all existing imports.  This is different from how
            // JDT does organize imports, but this prevents annotations on imports from being removed
            SortedSet<ImportNode> allImports = new ImportNodeCompatibilityWrapper(node).getAllImportNodes();
            boolean safeToReorganize = isSafeToReorganize(allImports);
            ImportRewrite rewriter = CodeStyleConfiguration.createImportRewrite(unit, !safeToReorganize);

            for (ImportNode imp : allImports) {
                String fieldName = imp.getFieldName();
                if (fieldName == null) {
                    fieldName = "*";
                }

                if (imp.getType() != null) {
                    String className = imp.getClassName();
                    if (className != null) {
                        // GRECLIPSE-929 ensure that statics and on-demand
                        // statics are never removed
                        // FIXADE we should be doing a better job here and can
                        // definitely walk the tree to find if a static is
                        // really being used, but for now, don't
                        String dottedClassName = className.replace('$', '.');
                        String alias = imp.getAlias();
                        if (!imp.isStaticStar() && !imp.isStatic()) {
                            importsSlatedForRemoval.put(dottedClassName, imp);
                            if (isAliased(imp)) {
                                aliases.put("n" + dottedClassName, alias);
                            } else {
                                rewriter.addImport(dottedClassName);
                            }
                        } else {
                            if (safeToReorganize) {
                                rewriter.addStaticImport(dottedClassName, fieldName, true);
                            }
                            if (isAliased(imp)) {
                                aliases.put("s" + dottedClassName + "." + fieldName, alias);
                                if (imp.isStatic()) {
                                    // Static aliased imports have been added as existing imports incorrectly.
                                    // must go remove them
                                    rewriter.removeInvalidStaticAlias(dottedClassName + "." + alias);
                                }
                            }
                        }
                    }
                } else {
                    if (imp.isStatic()) {
                        rewriter.addStaticImport(imp.getPackageName().replace('$', '.'), fieldName, true);
                    } else { // imp.isStar()
                        rewriter.addImport(imp.getPackageName() + "*");
                    }
                }
            }

            // find all missing types
            // find all imports that are not referenced
            for (ClassNode clazz : (Iterable<ClassNode>) node.getClasses()) {
                visitor.visitClass(clazz);
            }

            for (ImportNode imp : allImports) {
                // now remove all default imports
                if (isDefaultImport(imp)) {
                    // remove default imports
                    String key;
                    // not removing static imports
                    String className = imp.getClassName();
                    if (className != null) {
                        key = className;
                    } else {
                        // an on-demand/star import
                        key = imp.getPackageName();
                        if (key.endsWith(".")) {
                            key = key + "*";
                        }
                    }
                    importsSlatedForRemoval.put(key, imp);
                }
            }
            // remove old
            // will not work for aliased imports
            for (String impStr : importsSlatedForRemoval.keySet()) {
                rewriter.removeImport(impStr);
            }

            // resolve them
            IType[] resolvedTypes = resolveMissingTypes();

            for (IType resolved : resolvedTypes) {
                rewriter.addImport(resolved.getFullyQualifiedName('.'));
            }

            // now add aliases back to existing imports
            for (Entry<String, String> alias : aliases.entrySet()) {
                String key = alias.getKey();
                rewriter.addAlias(key, alias.getValue(),
                        !importsSlatedForRemoval.containsKey(key.substring(1)) && safeToReorganize);
            }

            TextEdit edit = rewriter.rewriteImports(null);
            return edit;
        } catch (CoreException e) {
            GroovyCore.logException("Exception thrown when organizing imports for " + unit.getElementName(), e);
        } catch (MalformedTreeException e) {
            GroovyCore.logException("Exception thrown when organizing imports for " + unit.getElementName(), e);
        }
        return null;
    }

    /**
     * GRECLIPSE-1390
     * Reorganizing imports (ie- sorting and grouping them) will remove annotations on import statements
     * In general, we want to reorganize, but it is not safe to do so if the are any annotations on imports
     * @param allImports all the imports in the compilation unit
     * @return true iff it is safe to reorganize imports
     */
    private boolean isSafeToReorganize(SortedSet<ImportNode> allImports) {
        for (ImportNode imp : allImports) {
            if (imp.getAnnotations() != null && imp.getAnnotations().size() > 0) {
                return false;
            }
        }
        return true;
    }

    private static final Set<String> DEFAULT_IMPORTS = new HashSet<String>();
    static {
        DEFAULT_IMPORTS.add("java.lang.");
        DEFAULT_IMPORTS.add("java.util.");
        DEFAULT_IMPORTS.add("java.io.");
        DEFAULT_IMPORTS.add("java.net.");
        DEFAULT_IMPORTS.add("groovy.lang.");
        DEFAULT_IMPORTS.add("groovy.util.");
        DEFAULT_IMPORTS.add("java.math.BigDecimal");
        DEFAULT_IMPORTS.add("java.math.BigInteger");
    }

    /**
     * Checks to see if this import statment is a default import
     * @param imp
     * @return
     */
    private boolean isDefaultImport(ImportNode imp) {
        // not really correct since I think Math.* is a default import. but OK for now
        if (imp.isStatic()) {
            return false;
        }
        // aliased imports are not considered default
        if (imp.getType() != null && !imp.getType().getNameWithoutPackage().equals(imp.getAlias())) {
            return false;
        }

        // now get the package name
        String pkg;
        if (imp.getType() != null) {
            pkg = imp.getType().getPackageName();
            if (pkg == null) {
                pkg = ".";
            } else {
                pkg = pkg + ".";
            }
            if (pkg.equals("java.math.")) {
                pkg = imp.getType().getName();
            }
        } else {
            pkg = imp.getPackageName();
            if (pkg == null) {
                pkg = ".";
            }
        }

        return DEFAULT_IMPORTS.contains(pkg);
    }

    /**
     * @param imp
     * @return
     */
    private boolean isAliased(ImportNode imp) {
        String alias = imp.getAlias();
        if (alias == null) {
            return false;
        }

        String fieldName = imp.getFieldName();
        if (fieldName != null) {
            return !fieldName.equals(alias);
        }
        String className = imp.getClassName();
        if (className != null) {
            // it is possible to import from the default package
            boolean aliasIsSameAsClassName = className.endsWith(alias)
                    && (className.length() == alias.length() || className.endsWith("." + alias) || className.endsWith("$" + alias));
            return !aliasIsSameAsClassName;
        }
        return false;
    }

    /**
     * Determine if module is empty...not as easy as it sounds
     *
     * @param node
     * @return
     */
    private boolean isEmpty(ModuleNode node) {
        if (node == null || node.getClasses() == null || (node.getClasses().size() == 0 && node.getImports().size() == 0)) {
            return true;
        }
        if (node.getClasses().size() == 1 && node.getImports().size() == 0 && ((ClassNode) node.getClasses().get(0)).isScript()) {
            if ((node.getStatementBlock() == null || node.getStatementBlock().isEmpty() || isNullReturn(node.getStatementBlock())) &&
                    (node.getMethods() == null || node.getMethods().size() == 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param statementBlock
     * @return
     */
    private boolean isNullReturn(BlockStatement statementBlock) {
        List<Statement> statements = statementBlock.getStatements();
        if (statements.size() == 1 && statements.get(0) instanceof ReturnStatement) {
            ReturnStatement ret = (ReturnStatement) statements.get(0);
            if (ret.getExpression() instanceof ConstantExpression) {
                return ((ConstantExpression) ret.getExpression()).isNullExpression();
            }
        }
        return false;
    }

    // no longer needed since we have our own import rewriter
    // /**
    // * @param edit
    // * @return
    // */
    // private TextEdit removeSemiColons(TextEdit edit) {
    // TextEditVisitor visitor = new TextEditVisitor() {
    // @Override
    // public boolean visit(InsertEdit edit) {
    // String text = edit.getText();
    // text = text.replace(';', ' ');
    // ReflectionUtils.setPrivateField(InsertEdit.class, "fText", edit, text);
    // return super.visit(edit);
    // }
    // @Override
    // public boolean visit(ReplaceEdit edit) {
    // String text = edit.getText();
    // text = text.replace(';', ' ');
    // ReflectionUtils.setPrivateField(InsertEdit.class, "fText", edit, text);
    // return super.visit(edit);
    // }
    // };
    // edit.accept(visitor);
    // return edit;
    // }

    public boolean calculateAndApplyMissingImports() throws JavaModelException {
        TextEdit edit = calculateMissingImports();
        if (edit != null) {
            unit.applyTextEdit(edit, null);
            return true;
        } else {
            return false;
        }
    }


    private IType[] resolveMissingTypes() throws JavaModelException {

        // fill in all the potential matches
        new TypeSearch().searchForTypes(unit, missingTypes);
        List<TypeNameMatch> missingTypesNoChoiceRequired = new ArrayList<TypeNameMatch>();
        List<TypeNameMatch[]> missingTypesChoiceRequired = new ArrayList<TypeNameMatch[]>();
        List<ISourceRange> ranges = new ArrayList<ISourceRange>();

        // go through all the resovled matches and look for ambiguous matches
        for (UnresolvedTypeData data : missingTypes.values()) {
            if (data.foundInfos.size() > 1) {
                missingTypesChoiceRequired.add(data.foundInfos.toArray(new TypeNameMatch[0]));
                ranges.add(data.range);
            } else if (data.foundInfos.size() == 1) {
                missingTypesNoChoiceRequired.add(data.foundInfos.get(0));
            }
        }


        TypeNameMatch[][] missingTypesArr = missingTypesChoiceRequired.toArray(new TypeNameMatch[0][]);
        TypeNameMatch[] chosen;
        if (missingTypesArr.length > 0) {
            chosen = query.chooseImports(missingTypesArr, ranges.toArray(new ISourceRange[0]));
        } else {
            chosen = new TypeNameMatch[0];
        }

        if (chosen != null) {
            IType[] typeMatches = new IType[missingTypesNoChoiceRequired.size() + chosen.length];
            int cnt = 0;
            for (TypeNameMatch typeNameMatch : missingTypesNoChoiceRequired) {
                typeMatches[cnt++] = typeNameMatch.getType();
            }
            for (int i = 0; i < chosen.length; i++) {
                typeMatches[cnt++] = ((JavaSearchTypeNameMatch) chosen[i]).getType();
            }

            return typeMatches;
        } else {
            // dialog was canceled.  do nothing
            return new IType[0];
        }
    }

}
