package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.SourceRange;
import org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditVisitor;

/**
 * @author andrew
 * Organize import operation for groovy files
 */
public class OrganizeGroovyImports {
    
    /**
     * From {@link OrganizeImportsOperation.TypeReferenceProcessor.UnresolvedTypeData}
     * 
     */
    static class UnresolvedTypeData {
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
            for (Parameter param : node.getParameters()) {
                handleType(param.getType(), false);
            }
            super.visitClosureExpression(node);
        }

        @Override
        public void visitAnnotations(AnnotatedNode node) {
            for (AnnotationNode an : (Iterable<AnnotationNode>) node.getAnnotations()) {
                handleType(an.getClassNode(), true);
            }
            super.visitAnnotations(node);
        }
        
        
        
        /**
         * Assume dynamic variables are a candidate for Organize imports,
         * but only if name begins with a capital.  This will hopefully 
         * filter out most false positives.
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
            if (!node.isResolved() && node != current) {
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
                
                String nameWithDots = name.replace('$', '.');
                int innerIndex = name.lastIndexOf('$');
                while (innerIndex > -1) {
                    doNotRemoveImport(nameWithDots);
                    nameWithDots = nameWithDots.substring(0, innerIndex);
                    innerIndex = name.lastIndexOf('$', innerIndex-1);
                }
                doNotRemoveImport(nameWithDots);
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
                    if (gen.getType() != null && gen.getType().getName().charAt(0) != '?') {
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
    private HashMap<String, UnresolvedTypeData> missingTypes;
    private HashMap<String, ImportNode> importsSlatedForRemoval;
    
    private IChooseImportQuery query;
    
    public OrganizeGroovyImports(GroovyCompilationUnit unit, IChooseImportQuery query) {
        this.unit = unit;
        this.query = query;
    }
    
    public TextEdit calculateMissingImports() {
        ModuleNode node = unit.getModuleNode();
        if (isEmpty(node)) {
            // no AST probably a syntax error...do nothing
            return new MultiTextEdit();
        }
        
        missingTypes = new HashMap<String,UnresolvedTypeData>();
        importsSlatedForRemoval = new HashMap<String, ImportNode>();
        FindUnresolvedReferencesVisitor visitor = new FindUnresolvedReferencesVisitor();
        
        for (ImportNode imp : (Iterable<ImportNode>) node.getImports()) {
            importsSlatedForRemoval.put(imp.getClassName(), imp);
        }
        

        // find all missing types
        // find all imports that are not referenced
        for (ClassNode clazz : (Iterable<ClassNode>) node.getClasses()) {
            visitor.visitClass(clazz);
        }
        
        
        try {
            ImportRewrite rewriter = ImportRewrite.create(unit, true);

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
            TextEdit edit = rewriter.rewriteImports(null);
            // remove ';' from edits
            edit = removeSemiColons(edit);
            return edit;
        } catch (CoreException e) {
            GroovyCore.logException("Exception thrown when organizing imports for " + unit.getElementName(), e);
        } catch (MalformedTreeException e) {
            GroovyCore.logException("Exception thrown when organizing imports for " + unit.getElementName(), e);
        }
        return null;
    }

    /**
     * Determine if module is empty...not as easy as it sounds
     * @param node
     * @return
     */
    private boolean isEmpty(ModuleNode node) {
        if (node == null || node.getClasses() == null || node.getClasses().size() == 0) {
            return true;
        }
        if (node.getClasses().size() == 1 && ((ClassNode) node.getClasses().get(0)).isScript()) {
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

    /**
     * @param edit
     * @return
     */
    private TextEdit removeSemiColons(TextEdit edit) {
        TextEditVisitor visitor = new TextEditVisitor() {
            @Override
            public boolean visit(InsertEdit edit) {
                String text = edit.getText();
                text = text.replace(';', ' ');
                ReflectionUtils.setPrivateField(InsertEdit.class, "fText", edit, text);
                return super.visit(edit);
            }
            public boolean visit(ReplaceEdit edit) {
                String text = edit.getText();
                text = text.replace(';', ' ');
                ReflectionUtils.setPrivateField(InsertEdit.class, "fText", edit, text);
                return super.visit(edit);
            }
        };
        edit.accept(visitor);
        return edit;
    }

    public void calculateAndApplyMissingImports() throws JavaModelException {
        TextEdit edit = calculateMissingImports();
        if (edit != null) {
            unit.applyTextEdit(edit, null);
        }
    }
    

    private IType[] resolveMissingTypes() throws JavaModelException {
        
        // fill in all the potential matches
        searchForTypes();
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

    
    /**
     * Use a SearchEngine to look for the types
     * This will not find inner types, however
     * @see OrganizeImportsOperation.TypeReferenceProcessor#process(org.eclipse.core.runtime.IProgressMonitor)
     * @param missingType
     * @throws JavaModelException 
     */
    private void searchForTypes() throws JavaModelException {
        char[][] allTypes = new char[missingTypes.size()][];
        int i = 0;
        for (String simpleName : missingTypes.keySet()) {
            allTypes[i++] = simpleName.toCharArray();
        }
        final List<TypeNameMatch> typesFound= new ArrayList<TypeNameMatch>();
        TypeNameMatchCollector collector= new TypeNameMatchCollector(typesFound);
        IJavaSearchScope scope= SearchEngine.createJavaSearchScope(new IJavaElement[] { unit.getJavaProject() });
        new SearchEngine().searchAllTypeNames(null, allTypes, scope, collector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
        
        for (TypeNameMatch match : typesFound) {
            UnresolvedTypeData data = missingTypes.get(match.getSimpleTypeName());
            if (isOfKind(match, data.isAnnotation)) {
                data.addInfo(match);
            }
        }
    }

    /**
     * If looking for an annotation, then filter out non-annoations,
     * otherwise everything is acceptable.
     * @param match
     * @param isAnnotation
     * @return
     */
    private boolean isOfKind(TypeNameMatch match, boolean isAnnotation) {
        return isAnnotation ? Flags.isAnnotation(match.getModifiers()) : true;
    }


}
