/*
 * Copyright 2009-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import groovy.transform.Field;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyClassVisitor;
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
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.ArrayUtils;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Organize import operation for groovy files
 */
public class OrganizeGroovyImports {

    private static final ClassNode CLASS_NODE_FIELD = ClassHelper.make(Field.class);

    /**
     * From {@link OrganizeImportsOperation.TypeReferenceProcessor.UnresolvedTypeData}
     */
    public static class UnresolvedTypeData {
        final String ref;
        final boolean isAnnotation;
        final ISourceRange range;
        final List<TypeNameMatch> foundInfos = new LinkedList<TypeNameMatch>();

        public UnresolvedTypeData(String ref, boolean annotation, ISourceRange range) {
            this.ref = ref;
            this.isAnnotation = annotation;
            this.range = range;
        }

        public void addInfo(TypeNameMatch info) {
            for (int i = foundInfos.size() - 1; i >= 0; i -= 1) {
                TypeNameMatch curr= foundInfos.get(i);
                if (curr.getTypeContainerName().equals(info.getTypeContainerName())) {
                    return; // not added; already contains type with same name
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

        @Override
        protected void visitAnnotation(AnnotationNode annotation) {
            // skip nodes added by an annotation collector transformation
            if (annotation.getNodeMetaData("AnnotationCollector") == null) {
                handleType(annotation.getClassNode(), true);
            }
            super.visitAnnotation(annotation);
        }

        @Override
        public void visitCastExpression(CastExpression expression) {
            handleType(expression.getType(), false);
            super.visitCastExpression(expression);
        }

        @Override
        public void visitClassExpression(ClassExpression expression) {
            if (expression.getEnd() > 0) {
                handleType(expression.getType(), false);
            }
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            Parameter[] parameters = expression.getParameters();
            if (parameters != null) {
                for (Parameter param : parameters) {
                    handleType(param.getType(), false);
                }
            }
            super.visitClosureExpression(expression);
        }

        @Override
        public void visitConstantExpression(ConstantExpression expression) {
            if (expression instanceof AnnotationConstantExpression) {
                handleType(expression.getType(), true);
            }
        }

        @Override
        public void visitPropertyExpression(PropertyExpression expression) {
            if (!expression.isStatic() && !expression.isSynthetic()) {
                String alias = (String) expression.getNodeMetaData("static.import.alias");
                if (alias != null) {
                    String staticImport = expression.getText().replace('$', '.');
                    if (!alias.equals(expression.getPropertyAsString())) {
                        staticImport += " as " + alias;
                    }
                    doNotRemoveImport(staticImport);
                }
            }
            super.visitPropertyExpression(expression);
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            if (expression.getAccessedVariable() == expression) {
                handleType(expression.getType(), false);
            }
            if (expression.getAccessedVariable() instanceof DynamicVariable || expression.isDynamicTyped()) {
                if (!checkRetainImport(expression.getName())) { // could it be static?
                    handleVariable(expression);
                }
            }
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (!call.isSynthetic() && call.getStart() > 0) {
                if (call.isImplicitThis()) {
                    checkRetainImport(call.getMethodAsString()); // could it be static?
                } else if (call.getGenericsTypes() != null) {
                    for (GenericsType type : call.getGenericsTypes()) {
                        handleType(type.getType(), false);
                    }
                }
            }
            super.visitMethodCallExpression(call);
        }

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
            if (!call.isSynthetic() && call.getStart() > 0) {
                String method = call.getOwnerType().getName().replace('$', '.') + '.' + call.getMethod();
                String alias = (String) call.getNodeMetaData("static.import.alias");
                if (alias != null) {
                    method += " as " + alias;
                }
                doNotRemoveImport(method);
            }
            super.visitStaticMethodCallExpression(call);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression call) {
            handleType(call.getType(), false);
            super.visitConstructorCallExpression(call);
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
        public void visitField(FieldNode node) {
            // can't check for synthetic here because it seems that non-synthetic nodes are being marked as synthetic
            if (node.getEnd() > 0) {
                handleType(node.getType(), false);
                // fields in a script would have Field annotation
                if (node.getOwner().isScript()) {
                    handleType(CLASS_NODE_FIELD, true);
                }
            }
            super.visitField(node);
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
                // GRECLIPSE-1693
                GenericsType[] generics = node.getUnresolvedSuperClass().getGenericsTypes();
                if (generics != null) {
                    for (GenericsType generic : generics) {
                        handleType(generic.getType(), false);
                    }
                }
            }
            super.visitClass(node);
        }

        @Override
        public void visitCatchStatement(CatchStatement node) {
            handleType(node.getVariable().getType(), false);
            super.visitCatchStatement(node);
        }

        @Override
        public void visitForLoop(ForStatement node) {
            // check the type node of "for (Item i in x)" but skip "for (i in x)"
            Parameter parm = node.getVariable(); ClassNode type = parm.getType();
            if (type.getStart() > 0 && type.getEnd() < parm.getStart()) {
                handleType(type, false);
            }
            super.visitForLoop(node);
        }

        //

        /**
         * Assume dynamic variables are a candidate for organize imports, but
         * only if name begins with a capital letter and does not match the
         * idiomatic static constant naming.  This will hopefully filter out
         * most false positives, but will miss types that start with lower case.
         */
        private void handleVariable(VariableExpression expr) {
            String name = expr.getName();
            if (!missingTypes.containsKey(name) &&
                    Character.isUpperCase(name.charAt(0)) &&
                    !STATIC_CONSTANT.matcher(name).matches()) {
                missingTypes.put(name, new UnresolvedTypeData(name, false,
                        new SourceRange(expr.getStart(), expr.getEnd() - expr.getStart())));
            }
        }

        /**
         * Adds the type name to missingTypes if it is not resolved or ensures
         * that the import will be retained if the type is resolved.
         */
        private void handleType(ClassNode node, boolean isAnnotation) {
            if (getBaseType(node).isPrimitive()) {
                return;
            }

            int start = Math.max(node.getNameStart(), node.getStart()),
                until = Math.max(node.getNameEnd(), node.getEnd());

            if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
                for (GenericsType gen : node.getGenericsTypes()) {
                    if (gen.getStart() > 0) until = gen.getStart() - 1;
                    if (!gen.isPlaceholder() && !gen.isWildcard()) {
                        handleType(gen.getType(), false);
                    }
                    if (gen.getLowerBound() != null) {
                        handleType(gen.getLowerBound(), false);
                    } else if (gen.getUpperBounds() != null) {
                        for (ClassNode upper : gen.getUpperBounds()) {
                            // handle enums where the upper bound is the same as the type
                            if (!upper.getName().equals(node.getName())) {
                                handleType(upper, false);
                            }
                        }
                    }
                }
            } else if (node.isArray() && getBaseType(node).getEnd() > 0) {
assert start <= getBaseType(node).getStart();
assert until <= 0 || getBaseType(node).getEnd() < until;

                start = getBaseType(node).getStart();
                until = getBaseType(node).getEnd();
            }

            int length = until - start;
            String name = getTypeName(node);

            if (!node.isResolved() && node.redirect() != current) {
                // aliases come through as unresolved types
                if (ALIASED_IMPORT.matcher(name).find()) {
                    doNotRemoveImport(name);
                    return;
                }
                String[] parts = name.split("\\.");
                if (Character.isUpperCase(name.charAt(0))) {
                    name = parts[0]; // Map.Entry -> Map
                } else if (length < name.length()) {
                    // name range too small to include the full name
                    doNotRemoveImport(name); // keep import
                    name = ArrayUtils.lastElement(parts); // foo.Bar -> Bar
                }
                if (!missingTypes.containsKey(name)) {
                    SourceRange range = new SourceRange(node.getStart(), node.getEnd() - node.getStart());
                    missingTypes.put(name, new UnresolvedTypeData(name, isAnnotation, range));
                }
            } else if (length < name.length()) {
                // We don't know exactly what the
                // text is.  We just know how it resolves
                // This can be a problem if an inner class.
                // We don't really know what is in the text
                // and we don't really know what is the import
                // So, just ensure that none are slated for removal
                String partialName = name.replace('$', '.');
                int innerIndex = name.lastIndexOf('$');
                while (innerIndex > -1) {
                    doNotRemoveImport(partialName);
                    partialName = partialName.substring(0, innerIndex);
                    innerIndex = name.lastIndexOf('$', innerIndex - 1);
                }
                doNotRemoveImport(partialName);
            }
        }

        private ClassNode getBaseType(ClassNode node) {
            while (node.isArray()) node = node.getComponentType();
            return node;
        }

        private String getTypeName(ClassNode node) {
            ClassNode type = getBaseType(node);
            if (!type.getName().matches(".*\\b" + type.getUnresolvedName())) {
                return type.getName() + " as " + type.getUnresolvedName();
            }
            return type.getName();
        }

        private boolean checkRetainImport(String name) {
            if (!importsSlatedForRemoval.isEmpty() && !"this".equals(name) && !"super".equals(name)) {
                String suffix = '.' + name;
                for (Map.Entry<String, ImportNode> entry : importsSlatedForRemoval.entrySet()) {
                    if (entry.getValue().isStatic() && entry.getKey().endsWith(suffix)) {
                        doNotRemoveImport(entry.getKey());
                        return true;
                    }
                }
            }
            return false;
        }

        private void doNotRemoveImport(String name) {
            importsSlatedForRemoval.remove(name);
        }
    }

    private static final Pattern ALIASED_IMPORT = Pattern.compile("\\sas\\s");
    private static final Pattern STATIC_CONSTANT = Pattern.compile("[A-Z][A-Z0-9_]+");

    //--------------------------------------------------------------------------

    private IChooseImportQuery query;
    private final GroovyCompilationUnit unit;
    private Map<String, UnresolvedTypeData> missingTypes;
    private Map<String, ImportNode> importsSlatedForRemoval;

    public OrganizeGroovyImports(GroovyCompilationUnit unit, IChooseImportQuery query) {
        this.unit = unit;
        this.query = query;
    }

    public boolean calculateAndApplyMissingImports() throws JavaModelException {
        TextEdit edit = calculateMissingImports();
        if (edit != null) {
            unit.applyTextEdit(edit, null);
            return true;
        } else {
            return false;
        }
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

        missingTypes = new HashMap<String, UnresolvedTypeData>();
        importsSlatedForRemoval = new HashMap<String, ImportNode>();

        try {
            // Configure the import rewriter to keep all existing imports. This is different from how
            // JDT does organize imports, but this prevents annotations on imports from being removed.
            // However, this leads to GRECLIPSE-1390 where imports are no longer reordered and sorted.
            Iterable<ImportNode> allImports = new ImportNodeCompatibilityWrapper(node).getAllImportNodes();
            ImportRewrite rewriter = CodeStyleConfiguration.createImportRewrite(unit, !isSafeToReorganize(allImports));

            for (ImportNode imp : allImports) {
                if (imp.isStar()) {
                    if (!imp.isStatic()) {
                        rewriter.addImport(imp.getPackageName() + "*");
                    } else {
                        rewriter.addStaticImport(imp.getClassName().replace('$', '.'), "*", true);
                    }
                    // GRECLIPSE-929: ensure that on-demand (i.e. star) imports are never removed
                } else {
                    String className = imp.getClassName().replace('$', '.');
                    if (!imp.isStatic()) {
                        if (!isAliased(imp)) {
                            rewriter.addImport(className);
                            importsSlatedForRemoval.put(className, imp);
                        } else {
                            String alias = className + " as " + imp.getAlias();
                            rewriter.addImport(alias);
                            importsSlatedForRemoval.put(alias, imp);
                        }
                    } else {
                        if (!isAliased(imp)) {
                            rewriter.addStaticImport(className, imp.getFieldName(), true);
                            importsSlatedForRemoval.put(className + '.' + imp.getFieldName(), imp);
                        } else {
                            rewriter.addStaticImport(className, imp.getFieldName() + " as " + imp.getAlias(), true);
                            importsSlatedForRemoval.put(className + '.' + imp.getFieldName() + " as " + imp.getAlias(), imp);
                        }
                    }
                }
            }

            // scan for imports that are not referenced
            for (ClassNode clazz : (Iterable<ClassNode>) node.getClasses()) {
                GroovyClassVisitor visitor = new FindUnresolvedReferencesVisitor();
                visitor.visitClass(clazz); // modifies missingTypes and importsSlatedForRemoval
            }

            // remove all default imports
            for (ImportNode imp : allImports) {
                if (isDefaultImport(imp)) {
                    // remove default imports
                    String key;
                    // not removing static imports
                    if (imp.getClassName() != null) {
                        key = imp.getClassName();
                    } else {
                        // an on-demand/star import
                        key = imp.getPackageName();
                        if (key.endsWith(".")) {
                            key += "*";
                        }
                    }
                    importsSlatedForRemoval.put(key, imp);
                }
            }

            // remove imports that were not matched to a source element
            for (Map.Entry<String, ImportNode> entry : importsSlatedForRemoval.entrySet()) {
                if (!entry.getValue().isStatic()) {
                    rewriter.removeImport(entry.getKey());
                } else {
                    rewriter.removeStaticImport(entry.getKey());
                }
            }

            // deal with the missing types
            if (!missingTypes.isEmpty()) {
                pruneMissingTypes(allImports);
                if (!missingTypes.isEmpty()) {
                    for (IType type : resolveMissingTypes()) {
                        rewriter.addImport(type.getFullyQualifiedName('.'));
                    }
                }
            }

            return rewriter.rewriteImports(null);

        } catch (Exception e) {
            GroovyCore.logException("Exception thrown when organizing imports for " + unit.getElementName(), e);
        } finally {
            importsSlatedForRemoval = null;
            missingTypes = null;
        }
        return null;
    }

    /**
     * There are cases where a type is seen as unresolved but can be found
     * amongst the imports of the module or within the default imports.
     * <p>
     * One such case is the use of a parameterized type, but not all type
     * params have been satisfied correctly.  Another involves annotation
     * types that have not been identified correctly as annotations.
     */
    private void pruneMissingTypes(Iterable<ImportNode> imports) throws JavaModelException {
        Set<String> starImports = new LinkedHashSet<String>();
        Set<String> typeImports = new LinkedHashSet<String>();

        if (unit.getModuleNode().getPackageName() != null) {
            starImports.add(unit.getModuleNode().getPackageName());
        } else {
            starImports.add("");
        }
        for (ImportNode in : imports) {
            if (!in.isStatic()) {
                if (in.isStar()) {
                    starImports.add(in.getPackageName());
                } else {
                    typeImports.add(in.getText());
                }
            }
        }
        for (String di : DEFAULT_IMPORTS) {
            if (di.endsWith(".")) {
                starImports.add(di);
            } else {
                typeImports.add(di + " as " + di.substring(di.lastIndexOf('.') + 1));
            }
        }

        // check each missing type against the module's single-type and on-demand imports
        on: for (Iterator<String> it = missingTypes.keySet().iterator(); it.hasNext();) {
            String typeName = it.next();
            for (String ti : typeImports) {
                if (ti.endsWith(' ' + typeName)) {
                    it.remove();
                    continue on;
                }
            }
            for (String si : starImports) {
                IType type = unit.getJavaProject().findType(si + typeName, (IProgressMonitor) null);
                if (type != null) {
                    it.remove();
                    continue on;
                }
            }
        }
    }

    private IType[] resolveMissingTypes() throws JavaModelException {
        // fill in all the potential matches
        new TypeSearch().searchForTypes(unit, missingTypes); // TODO: This is insanely slow!

        List<TypeNameMatch> missingTypesNoChoiceRequired = new ArrayList<TypeNameMatch>();
        List<TypeNameMatch[]> missingTypesChoiceRequired = new ArrayList<TypeNameMatch[]>();
        List<ISourceRange> ranges = new ArrayList<ISourceRange>();

        // go through all the resovled matches and look for ambiguous matches
        for (UnresolvedTypeData data : missingTypes.values()) {
            int foundInfosSize = data.foundInfos.size();
            if (foundInfosSize == 1) {
                missingTypesNoChoiceRequired.add(data.foundInfos.get(0));
            } else if (foundInfosSize > 1) {
                missingTypesChoiceRequired.add(data.foundInfos.toArray(new TypeNameMatch[foundInfosSize]));
                ranges.add(data.range);
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
            // dialog was canceled; do nothing
            return new IType[0];
        }
    }

    /**
     * GRECLIPSE-1390
     * Reorganizing imports (ie- sorting and grouping them) will remove annotations on import statements
     * In general, we want to reorganize, but it is not safe to do so if the are any annotations on imports
     * @param allImports all the imports in the compilation unit
     * @return true iff it is safe to reorganize imports
     */
    private static boolean isSafeToReorganize(Iterable<ImportNode> allImports) {
        for (ImportNode imp : allImports) {
            if (imp.getAnnotations() != null && !imp.getAnnotations().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static final Set<String> DEFAULT_IMPORTS = new LinkedHashSet<String>();
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
     * Checks to see if this import statment is a default import.
     */
    private static boolean isDefaultImport(ImportNode imp) {
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

    private static boolean isAliased(ImportNode imp) {
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

    private static boolean isEmpty(ModuleNode node) {
        if (node == null || node.getClasses() == null || (node.getClasses().size() == 0 && node.getImports().size() == 0)) {
            return true;
        }
        if (node.getClasses().size() == 1 && node.getImports().size() == 0 && node.getClasses().get(0).isScript()) {
            if ((node.getStatementBlock() == null || node.getStatementBlock().isEmpty() || isNullReturn(node.getStatementBlock())) &&
                    (node.getMethods() == null || node.getMethods().size() == 0)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNullReturn(BlockStatement statementBlock) {
        List<Statement> statements = statementBlock.getStatements();
        if (statements.size() == 1 && statements.get(0) instanceof ReturnStatement) {
            ReturnStatement ret = (ReturnStatement) statements.get(0);
            if (ret.getExpression() instanceof ConstantExpression) {
                return ((ConstantExpression) ret.getExpression()).isNullExpression();
            }
        }
        return false;
    }
}
