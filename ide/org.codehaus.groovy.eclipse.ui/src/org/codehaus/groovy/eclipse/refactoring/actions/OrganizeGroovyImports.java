/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.transform.Field;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.refactoring.actions.TypeSearch.UnresolvedTypeData;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.JavaCoreUtil;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.text.edits.TextEdit;

/**
 * Organize import operation for groovy files
 */
public class OrganizeGroovyImports {

    // TODO: Handle import and package annotations

    private static final Pattern ALIASED_IMPORT = Pattern.compile("\\sas\\s");
    private static final Pattern STATIC_CONSTANT = Pattern.compile("[A-Z][A-Z0-9_]+");
    private static final ClassNode SCRIPT_FIELD_CLASS_NODE = ClassHelper.make(Field.class);
    private static final ImportRewriteContext FORCE_RETENTION = new ImportRewriteContext() {
        @Override
        public int findInContext(String qualifier, String name, int kind) {
            return RES_NAME_UNKNOWN_NEEDS_EXPLICIT_IMPORT;
        }
    };

    private final SubMonitor monitor;
    private IChooseImportQuery query;
    private final GroovyCompilationUnit unit;
    private Map<String, UnresolvedTypeData> missingTypes;
    private Map<String, ImportNode> importsSlatedForRemoval;

    public OrganizeGroovyImports(GroovyCompilationUnit unit, IChooseImportQuery query) {
        this(unit, query, null);
    }

    public OrganizeGroovyImports(GroovyCompilationUnit unit, IChooseImportQuery query, IProgressMonitor monitor) {
        this.unit = unit;
        this.query = query;
        this.monitor = SubMonitor.convert(monitor, "Organize import statements", 7);
    }

    public boolean calculateAndApplyMissingImports() throws JavaModelException {
        TextEdit edit = calculateMissingImports();
        if (edit != null) {
            unit.applyTextEdit(edit, monitor.split(0));
            return true;
        } else {
            return false;
        }
    }

    public TextEdit calculateMissingImports() {
        String event = null;
        if (GroovyLogManager.manager.hasLoggers()) {
            event = unit.getElementName();
            GroovyLogManager.manager.logStart(event);
            GroovyLogManager.manager.log(TraceCategory.ORGANIZE_IMPORTS, event);
        }
        try {
            ModuleNodeInfo info = unit.getModuleInfo(true);
            if (info.isEmpty() || isUnclean(info, unit)) {
                return null;
            }

            missingTypes = new HashMap<>();
            importsSlatedForRemoval = new HashMap<>();

            try {
                Iterable<ImportNode> allImports = GroovyUtils.getAllImportNodes(info.module);
                // Configure the import rewriter to keep all existing imports. This is different from how
                // JDT does organize imports, but this prevents annotations on imports from being removed.
                // However, this leads to GRECLIPSE-1390 where imports are no longer reordered and sorted.
                ImportRewrite rewriter = CodeStyleConfiguration.createImportRewrite(unit, !isSafeToReorganize(allImports));

                for (ImportNode imp : allImports) {
                    if (imp.isStar()) {
                        if (!imp.isStatic()) {
                            rewriter.addImport(imp.getPackageName() + "*");
                            importsSlatedForRemoval.put(imp.getPackageName() + "*", imp);
                        } else {
                            rewriter.addStaticImport(imp.getClassName().replace('$', '.'), "*", true);
                            importsSlatedForRemoval.put(imp.getClassName().replace('$', '.') + ".*", imp);
                        }
                    } else {
                        String className = imp.getClassName().replace('$', '.');
                        if (!imp.isStatic()) {
                            if (!isAliased(imp)) {
                                rewriter.addImport(className);
                                importsSlatedForRemoval.put(className, imp);
                            } else {
                                rewriter.addImport(className + " as " + imp.getAlias(),
                                                    imp.getEnd() > 0 ? FORCE_RETENTION : null);
                                importsSlatedForRemoval.put(className + " as " + imp.getAlias(), imp);
                            }
                        } else {
                            if (!isAliased(imp)) {
                                rewriter.addStaticImport(className, imp.getFieldName(), false);
                                importsSlatedForRemoval.put(className + "." + imp.getFieldName(), imp);
                            } else {
                                ImportRewriteContext context = (imp.getEnd() > 0 ? FORCE_RETENTION : null);
                                rewriter.addStaticImport(className, imp.getFieldName() + " as " + imp.getAlias(), true, context);
                                importsSlatedForRemoval.put(className + "." + imp.getFieldName() + " as " + imp.getAlias(), imp);
                            }
                        }
                    }
                }

                monitor.worked(1);

                // scan for imports that are not referenced
                for (ClassNode clazz : (Iterable<ClassNode>) info.module.getClasses()) {
                    GroovyClassVisitor visitor = new FindUnresolvedReferencesVisitor();
                    visitor.visitClass(clazz); // modifies missingTypes and importsSlatedForRemoval
                }

                monitor.worked(4);

                // implicit type/static imports are not handled by ImportRewrite
                for (ImportNode imp : allImports) {
                    boolean isDefault = (!imp.isStatic() && !isAliased(imp) && (
                        ClassHelper.BigDecimal_TYPE.equals(imp.getType()) ||
                        ClassHelper.BigInteger_TYPE.equals(imp.getType())));
                    if (isDefault || (imp.getEnd() < 1 && !(imp.isStar() && !imp.isStatic()))) {
                        String key;
                        if (imp.isStar()) {
                            if (!imp.isStatic()) {
                                key = imp.getPackageName() + "*";
                            } else {
                                key = imp.getClassName().replace('$', '.') + ".*";
                            }
                        } else {
                            key = imp.getClassName().replace('$', '.');
                            if (imp.isStatic()) {
                                key += "." + imp.getFieldName();
                            }
                            if (isAliased(imp)) {
                                key += " as " + imp.getAlias();
                            }
                        }
                        importsSlatedForRemoval.put(key, imp);
                    }
                }
                // sub-type static-star imports are not handled by ImportRewrite
                for (ImportNode imp : allImports) {
                    if (imp.isStatic() && imp.isStar()) {
                        // "import static Type.*" covers "import static Super.*" or "import static Super.member"
                        for (ImportNode i : allImports) {
                            if (i != imp && i.isStatic() && !isAliased(i) &&
                                    !imp.getType().equals(i.getType()) && imp.getType().isDerivedFrom(i.getType())) {

                                String className = i.getClassName().replace('$', '.');
                                if (i.isStar()) {
                                    importsSlatedForRemoval.put(className + ".*", i);
                                } else {
                                    importsSlatedForRemoval.put(className + "." + i.getFieldName(), i);
                                }
                                if (imp.getEnd() > 0)
                                    importsSlatedForRemoval.remove(imp.getClassName().replace('$', '.') + ".*");
                            }
                        }
                    }
                }

                // remove imports that were not matched to a source element
                for (Map.Entry<String, ImportNode> entry : importsSlatedForRemoval.entrySet()) {
                    trace("Remove import '%s'", entry.getKey());
                    if (!entry.getValue().isStatic()) {
                        rewriter.removeImport(entry.getKey());
                    } else {
                        rewriter.removeStaticImport(entry.getKey());
                    }
                }

                monitor.worked(1);

                // deal with the missing types
                if (!missingTypes.isEmpty()) {
                    pruneMissingTypes(allImports);
                    if (!missingTypes.isEmpty()) {
                        monitor.subTask("Resolve missing types");
                        monitor.setWorkRemaining(missingTypes.size() + 1);
                        for (IType type : resolveMissingTypes(monitor.split(1))) {
                            trace("Missing type '%s'", type);
                            rewriter.addImport(type.getFullyQualifiedName('.'));
                        }
                    }
                }

                TextEdit rewrite = rewriter.rewriteImports(monitor.split(1));
                trace("%s", rewrite);
                return rewrite;
            } catch (Exception e) {
                GroovyPlugin.getDefault().logError("Exception thrown when organizing imports for " + unit.getElementName(), e);
            } finally {
                importsSlatedForRemoval = null;
                missingTypes = null;
            }
            return null;
        } finally {
            if (event != null) {
                GroovyLogManager.manager.logEnd(event, TraceCategory.ORGANIZE_IMPORTS);
            }
        }
    }

    /**
     * There are cases where a type is seen as unresolved but can be found
     * amongst the imports of the module.
     * <p>
     * One such case is the use of a parameterized type, but not all type
     * params have been satisfied correctly.  Another involves annotation
     * types that have not been identified correctly as annotations.
     */
    private void pruneMissingTypes(Iterable<ImportNode> imports) throws JavaModelException {
        Set<String> starImports = new LinkedHashSet<>();
        Set<String> typeImports = new LinkedHashSet<>();

        if (unit.getModuleNode().getPackageName() != null) {
            starImports.add(unit.getModuleNode().getPackageName());
        } else {
            starImports.add("");
        }
        for (ImportNode imp : imports) {
            if (!imp.isStatic()) {
                if (imp.isStar()) {
                    starImports.add(imp.getPackageName());
                } else {
                    typeImports.add(imp.getText());
                }
            }
        }

        // check each missing type against the module's single-type and on-demand imports
        on: for (Iterator<String> it = missingTypes.keySet().iterator(); it.hasNext();) {
            String typeName = it.next();
            for (String imp : typeImports) {
                if (imp.endsWith(' ' + typeName)) {
                    it.remove();
                    continue on;
                }
            }
            for (String imp : starImports) {
                IType type = JavaCoreUtil.findType(imp + typeName, unit);
                if (type != null) {
                    it.remove();
                    continue on;
                }
            }
        }
    }

    private IType[] resolveMissingTypes(IProgressMonitor monitor) throws JavaModelException {
        // fill in all the potential matches
        new TypeSearch().searchForTypes(unit, Collections.unmodifiableMap(missingTypes), monitor);

        List<TypeNameMatch> missingTypesNoChoiceRequired = new ArrayList<>();
        List<TypeNameMatch[]> missingTypesChoiceRequired = new ArrayList<>();
        List<ISourceRange> ranges = new ArrayList<>();

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

            int index = 0;
            for (TypeNameMatch typeNameMatch : missingTypesNoChoiceRequired) {
                typeMatches[index++] = typeNameMatch.getType();
            }
            for (int i = 0, n = chosen.length; i < n; i += 1) {
                typeMatches[index++] = ((JavaSearchTypeNameMatch) chosen[i]).getType();
            }

            return typeMatches;
        } else {
            // dialog was canceled; do nothing
            return new IType[0];
        }
    }

    /**
     * Determines if organize imports is unsafe due to syntax errors or other conditions.
     */
    private static boolean isUnclean(ModuleNodeInfo info, GroovyCompilationUnit unit) {
        try {
            if (info.module.encounteredUnrecoverableError() || !unit.isConsistent()) {
                return true;
            }
            CategorizedProblem[] problems = info.result.getProblems();
            if (problems != null && problems.length > 0) {
                for (CategorizedProblem problem : problems) {
                    if (problem.isError() && problem.getCategoryID() == CategorizedProblem.CAT_INTERNAL) {
                        String message = problem.getMessage();
                        if (message.contains("unexpected token")) {
                            trace("Stopping due to error in compilation unit: %s", message);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return true;
        }
        return false;
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
            if (!imp.getAnnotations().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAliased(ImportNode node) {
        String alias = node.getAlias();
        if (alias == null) {
            return false;
        }
        String fieldName = node.getFieldName();
        if (fieldName != null) {
            return !fieldName.equals(alias);
        }
        String className = node.getClassName();
        if (className != null) {
            // it is possible to import from the default package
            boolean aliasIsSameAsClassName = className.endsWith(alias) &&
                (className.length() == alias.length() || className.endsWith("." + alias) || className.endsWith("$" + alias));
            return !aliasIsSameAsClassName;
        }
        return false;
    }

    private static String getTypeName(ClassNode node) {
        ClassNode type = GroovyUtils.getBaseType(node);
        // unresolved name may have dots and/or dollars (e.g. 'a.b.C$D' or 'C$D' or even 'C.D')
        if (!type.getName().matches(".*\\b" + type.getUnresolvedName().replace('$', '.'))) {
            // synch up name and unresolved name (e.g. 'java.util.Map$Entry as Foo$Entry')
            return type.getName() + " as " + type.getUnresolvedName().replace('.', '$');
        }
        return type.getName();
    }

    private static void trace(String message, Object... arguments) {
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.ORGANIZE_IMPORTS, String.format(message, arguments));
        }
    }

    //--------------------------------------------------------------------------

    @FunctionalInterface
    public interface IChooseImportQuery {
        /**
         * Selects imports from a list of choices.
         * @param openChoices From each array, a type reference has to be selected
         * @param ranges For each choice the range of the corresponding  type reference.
         * @return Returns {@code null} to cancel the operation, or the selected imports.
         */
        TypeNameMatch[] chooseImports(TypeNameMatch[][] openChoices, ISourceRange[] ranges);
    }

    private class FindUnresolvedReferencesVisitor extends DepthFirstVisitor {

        private ClassNode current;

        @Override
        public void visitClass(ClassNode node) {
            ClassNode previous = current;
            try {
                current = node;
                if (node.getEnd() > 0) {
                    if (isNotEmpty(node.getGenericsTypes())) {
                        visitTypeParameters(node.getGenericsTypes(), node.getName());
                    }
                    handleTypeReference(node.getUnresolvedSuperClass(), false);
                    for (ClassNode ui : node.getUnresolvedInterfaces()) {
                        handleTypeReference(ui, false);
                    }
                    for (ClassNode ps : node.getPermittedSubclasses()) {
                        handleTypeReference(ps, false);
                    }
                }
                super.visitClass(node);
            } finally {
                current = previous;
            }
        }

        @Override
        public void visitField(FieldNode node) {
            if (node.getEnd() > 0) {
                handleTypeReference(node.getType(), false);
                // fields in a script have a Field annotation
                if (GroovyUtils.isScript(node.getOwner())) {
                    handleTypeReference(SCRIPT_FIELD_CLASS_NODE, true);
                }
            }
            super.visitField(node);
        }

        @Override
        public void visitMethod(MethodNode node) {
            if (node.getEnd() > 0) {
                if (!(node instanceof ConstructorNode) &&
                        isNotEmpty(node.getGenericsTypes())) {
                    visitTypeParameters(node.getGenericsTypes(), null);
                }
                handleTypeReference(node.getReturnType(), false);
                for (ClassNode exception : node.getExceptions()) {
                    handleTypeReference(exception, false);
                }
            }
            super.visitMethod(node);
        }

        //

        @Override
        public void visitArrayExpression(ArrayExpression expression) {
            if (expression.getEnd() > 0) {
                handleTypeReference(expression.getType(), false);
            }
            super.visitArrayExpression(expression);
        }

        @Override
        public void visitCastExpression(CastExpression expression) {
            if (expression.getEnd() > 0) {
                handleTypeReference(expression.getType(), false);
            }
            super.visitCastExpression(expression);
        }

        @Override
        public void visitClassExpression(ClassExpression expression) {
            if (expression.getEnd() > 0) {
                handleTypeReference(expression.getType(), false);
            }
            super.visitClassExpression(expression);
        }

        @Override
        public void visitConstantExpression(ConstantExpression expression) {
            if (expression.getEnd() > 0 && expression instanceof AnnotationConstantExpression) {
                handleTypeReference(expression.getType(), true);
            }
            super.visitConstantExpression(expression);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression expression) {
            if (expression.getEnd() > 0 && !expression.isSpecialCall() && !expression.isUsingAnonymousInnerClass()) {
                handleTypeReference(expression.getType(), false);
            }
            super.visitConstructorCallExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression expression) {
            if (expression.getEnd() > 0) {
                if (expression.isImplicitThis()) {
                    MethodNode methodTarget = expression.getMethodTarget();
                    if (methodTarget == null) { // unresolved type or ???
                        checkRetainImport(expression.getMethodAsString());
                    } else if (methodTarget.isStatic()) {
                        handleStaticCall(methodTarget.getDeclaringClass(), expression);
                    }
                } else if (isNotEmpty(expression.getGenericsTypes())) {
                    visitTypeParameters(expression.getGenericsTypes(), null);
                }
            }
            super.visitMethodCallExpression(expression);
        }

        @Override
        public void visitPropertyExpression(PropertyExpression expression) {
            if (expression.getEnd() > 0 &&
                    expression.getObjectExpression().getEnd() < 1 &&
                    expression.getProperty() instanceof ConstantExpression &&
                    expression.getObjectExpression() instanceof ClassExpression){
                String staticImportText = expression.getText().replace('$', '.');
                Object alias = expression.getNodeMetaData("static.import.alias");
                if (alias != null) staticImportText += " as " + alias;
                doNotRemoveImport(staticImportText);
            }
            super.visitPropertyExpression(expression);
        }

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
            if (expression.getEnd() > 0) {
                handleStaticCall(expression.getOwnerType(), expression);
            }
            super.visitStaticMethodCallExpression(expression);
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            if (expression.getEnd() > 0) {
                if (expression.getAccessedVariable() == expression) {
                    handleTypeReference(expression.getType(), false);
                }
                // Assume dynamic variables are a candidate for organize imports,
                // but only if name begins with a capital letter and does not match
                // the idiomatic static constant naming. This will hopefully filter
                // out false positives but misses types that start with lower case.
                if (expression.getAccessedVariable() instanceof DynamicVariable) {
                    if (!checkRetainImport(expression.getName())) { // could it be static?
                        String name = expression.getName();
                        if (!missingTypes.containsKey(name) &&
                            Character.isUpperCase(name.charAt(0)) &&
                            !STATIC_CONSTANT.matcher(name).matches()) {

                            missingTypes.put(name, new UnresolvedTypeData(name, false,
                                new SourceRange(expression.getStart(), expression.getEnd() - expression.getStart())));
                        }
                    }
                }
            }
            super.visitVariableExpression(expression);
        }

        //

        @Override
        protected void visitAnnotation(AnnotationNode annotation) {
            if (annotation.getEnd() > 0) {
                handleTypeReference(annotation.getClassNode(), true);
            }
            super.visitAnnotation(annotation);
        }

        @Override
        protected void visitParameter(Parameter parameter) {
            if (parameter != null && parameter.getEnd() > 0) {
                handleTypeReference(parameter.getOriginType(), false);
            }
            super.visitParameter(parameter);
        }

        protected void visitTypeParameters(GenericsType[] generics, String typeName) {
            for (GenericsType generic : generics) {
                if (generic.getStart() < 1) {
                    continue;
                }
                if (!generic.isPlaceholder() && !generic.isWildcard()) {
                    handleTypeReference(generic.getType(), false);
                } else {
                    visitAnnotations(generic.getType().getTypeAnnotations());
                }
                if (generic.getLowerBound() != null) {
                    handleTypeReference(generic.getLowerBound(), false);
                }
                if (generic.getUpperBounds() != null) {
                    for (ClassNode upper : generic.getUpperBounds()) {
                        if (!upper.getName().equals(typeName)) {
                            handleTypeReference(upper, false);
                        }
                    }
                }
            }
        }

        private void handleStaticCall(ClassNode declaringClass, MethodCall call) {
            String methodName = call.getMethodAsString();
            String clazz = declaringClass.getName().replace('$', '.') + '.';
            Object alias = ((ASTNode) call).getNodeMetaData("static.import.alias");

            if (alias == null || alias.equals(methodName)) {
                doNotRemoveImport(clazz + methodName);
            } else {
                doNotRemoveImport(clazz + alias); // property reference
                doNotRemoveImport(clazz + methodName + " as " + alias);
            }
        }

        /**
         * Adds the type name to missingTypes if it is not resolved or ensures
         * that the import will be retained if the type is resolved.
         */
        private void handleTypeReference(ClassNode node, boolean isAnnotation) {
            if (!isAnnotation) visitAnnotations(node.getTypeAnnotations());
            ClassNode type = GroovyUtils.getBaseType(node);
            if (ClassHelper.isPrimitiveType(type)) {
                return;
            }
            String name = getTypeName(type);
            GenericsType[] generics = type.getGenericsTypes();

            if (isNotEmpty(generics) && !type.isGenericsPlaceHolder()) {
                visitTypeParameters(generics, name);
            }

            int start = node.getNameStart(),
                until = node.getNameEnd()+1;
            if (until <= 1) {
                start = node.getStart();
                until = node.getEnd();

                // getEnd() includes generics; try to constrain the range
                if (until > 0 && isNotEmpty(generics)) {
                    if (generics[0].getStart() > 0)
                        until = generics[0].getStart() - 1;
                } else if (node.isArray() && type.getEnd() > 0) {
                    assert start <= type.getStart();
                    assert until <= 0 || type.getEnd() < until;

                    start = type.getStart();
                    until = type.getEnd();
                }
            }
            int length = until - start;

            Matcher m = ALIASED_IMPORT.matcher(name);
            if (m.find()) {
                int i = name.indexOf('$', m.end());
                if (i > 0) {
                    // 'java.util.Map$Entry as Alias$Entry' -> 'java.util.Map as Alias'
                    name = name.replaceAll(Pattern.quote(name.substring(i)) + "(?= |$)", "");
                }
                doNotRemoveImport(name.replace('$', '.'));
            } else if (length < 1 || current.getModule().getClasses().contains(node)) {
                // keep in importsSlatedForRemoval and leave out of missingTypes
            } else if (!node.isResolved()) {
                String[] parts = name.split("\\.");
                if (Character.isUpperCase(name.charAt(0))) {
                    name = parts[0]; // 'Map.Entry' -> 'Map'
                } else if (length < name.length()) {
                    // name range too small to include the full name
                    doNotRemoveImport(name); // keep import
                    name = ArrayUtils.lastElement(parts); // 'foo.Bar' -> 'Bar'
                }
                if (!missingTypes.containsKey(name)) {
                    SourceRange range = new SourceRange(node.getStart(), node.getEnd() - node.getStart());
                    missingTypes.put(name, new UnresolvedTypeData(name, isAnnotation, range));
                }
            } else if (length < name.length()) {
                char[] chars = current.getModule().getContext().readSourceRange(start, length);
                if (chars != null) {
                    int i = 0;
                    while (i < chars.length && Character.isJavaIdentifierPart(chars[i])) {
                        i += 1;
                    }
                    // for a 'Map.Entry' reference find '.Map' in 'java.util.Map' or '.Map$' in 'java.util.Map$Entry'
                    m = Pattern.compile("(?:\\A|\\$|\\.)" + String.valueOf(chars, 0, i) + "(?=\\$|$)").matcher(name);
                    if (m.find()) {
                        // 'java.util.Map$Entry' -> 'java.util.Map'
                        String partialName = name.substring(0, m.end());
                        if (!isInnerOfSuper(partialName))
                            doNotRemoveImport(partialName.replace('$', '.'));
                    }
                } else {
                    // We do not know exactly what the text is. We just know how
                    // it resolves. This can be a problem for an inner class. We
                    // don't really know what is in the text nor what the import
                    // is, so just ensure that none are slated for removal.
                    String partialName = name.replace('$', '.');
                    int end = name.length();
                    do {
                        // 'java.util.Map.Entry' -> 'java.util.Map'
                        partialName = partialName.substring(0, end);
                        doNotRemoveImport(partialName);
                    } while ((end = partialName.lastIndexOf('.')) > -1);
                }
            } else if (length > name.length()) {
                GroovyPlugin.getDefault().logError(String.format(
                    "Expected a fully-qualified name for %s at [%d..%d] line %d, but source length (%d) > name length (%d)%n",
                    name, start, until, node.getLineNumber(), length, name.length()), new Exception());
            }
        }

        private boolean isInnerOfSuper(String name) {
            if (name.lastIndexOf('$') > 0) {
                for (ClassNode node = current.getSuperClass(); node != null && !node.equals(ClassHelper.OBJECT_TYPE); node = node.getSuperClass()) {
                    for (Iterator<? extends ClassNode> it = node.redirect().getInnerClasses(); it.hasNext();) {
                        if (it.next().getName().equals(name)) {
                            return true;
                        }
                    }
                }

                java.util.Queue<ClassNode> todo = new java.util.ArrayDeque<>();
                java.util.Collections.addAll(todo, current.getInterfaces());
                Set<ClassNode> done = new LinkedHashSet<>();
                ClassNode node;

                while ((node = todo.poll()) != null) { if (!done.add(node)) continue;
                    for (Iterator<? extends ClassNode> it = node.redirect().getInnerClasses(); it.hasNext();) {
                        if (it.next().getName().equals(name)) {
                            return true;
                        }
                    }
                    java.util.Collections.addAll(todo, node.getInterfaces());
                }
            }
            return false;
        }

        private boolean checkRetainImport(String name) {
            if (!importsSlatedForRemoval.isEmpty() && !"this".equals(name) && !"super".equals(name)) {
                for (Map.Entry<String, ImportNode> entry : importsSlatedForRemoval.entrySet()) {
                    String suffix = (isAliased(entry.getValue()) ? ' ' : '.') + name;
                    if (entry.getValue().isStatic() && entry.getKey().endsWith(suffix)) {
                        importsSlatedForRemoval.remove(entry.getKey());
                        return true;
                    }
                }
            }
            return false;
        }

        private void doNotRemoveImport(String which) {
            importsSlatedForRemoval.remove(which);
            if (!which.contains(" as ")) { // alias
                int index = which.lastIndexOf('.');
                if (index > 0) {
                    importsSlatedForRemoval.remove(which.substring(0, index + 1) + "*");
                }
            }
        }
    }
}
