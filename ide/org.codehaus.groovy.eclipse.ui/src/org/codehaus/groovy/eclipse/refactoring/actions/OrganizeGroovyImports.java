package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
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
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.builder.GroovyNameLookup;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.IJavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.SourceRange;
import org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
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
        
        
        @Override
        public void visitListExpression(ListExpression node) {
            super.visitListExpression(node);
        }
        
        @Override
        public void visitMapEntryExpression(MapEntryExpression expression) {
            super.visitMapEntryExpression(expression);
        }
        
        @Override
        public void visitMapExpression(MapExpression expression) {
            super.visitMapExpression(expression);
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
                if (isAnnotation) {
                    missingAnnotationTypeNames.put(simpleName, 
                            new SourceRange(node.getStart(), node.getEnd()-node.getStart()));
                } else {
                    missingTypeNames.put(simpleName, 
                            new SourceRange(node.getStart(), node.getEnd()-node.getStart()));
                }
            } else {
                // FIXADE RC1 We don't know exactly what the
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
                            handleType(upper, false);
                        }
                    }
                    if (gen.getType() != null) {
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
    private HashMap<String, ISourceRange> missingTypeNames;
    private HashMap<String, ISourceRange> missingAnnotationTypeNames;
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
        
        missingTypeNames = new HashMap<String,ISourceRange>();
        missingAnnotationTypeNames = new HashMap<String,ISourceRange>();
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
        IJavaProject project = unit.getJavaProject();
        NameLookup lookup = createNameLookup(project);

        List<TypeNameMatch> missingTypesNoChoiceRequired = new ArrayList<TypeNameMatch>();
        List<TypeNameMatch[]> missingTypesChoiceRequired = new ArrayList<TypeNameMatch[]>();
        List<ISourceRange> ranges = new ArrayList<ISourceRange>();
        
        // find regular types
        for (Entry<String, ISourceRange> missingType : missingTypeNames.entrySet()) {
            TypeNameMatch[] resolved = resolveType(missingType.getKey(), lookup, false);
            if (resolved == null || resolved.length == 0) {
                continue;
            } else if (resolved.length == 1) {
                missingTypesNoChoiceRequired.add(resolved[0]);
            } else {
                missingTypesChoiceRequired.add(resolved);
                ranges.add(missingType.getValue());
            }
        }
        // find annotation types
        for (Entry<String, ISourceRange> missingType : missingAnnotationTypeNames.entrySet()) {
            TypeNameMatch[] resolved = resolveType(missingType.getKey(), lookup, true);
            if (resolved == null || resolved.length == 0) {
                continue;
            } else if (resolved.length == 1) {
                missingTypesNoChoiceRequired.add(resolved[0]);
            } else {
                missingTypesChoiceRequired.add(resolved);
                ranges.add(missingType.getValue());
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
     * create a name lookup that can see into secondary types
     */
    private NameLookup createNameLookup(IJavaProject project)
            throws JavaModelException {
        return new GroovyNameLookup(((JavaProject) project).newNameLookup(unit.owner));
    }

    /**
     * Use a NameLookup to look for the types
     * This will not find inner types, however
     * Should use a SearchEngine
     * @see OrganizeImportsOperation.TypeReferenceProcessor#process(org.eclipse.core.runtime.IProgressMonitor)
     * @param missingType
     */
    private TypeNameMatch[] resolveType(String missingType, NameLookup lookup, boolean lookForAnnotation) {
        final List<TypeNameMatch> resolved = new LinkedList<TypeNameMatch>();
        IJavaElementRequestor requestor = new IJavaElementRequestor() {
            boolean canceled = false;
            public boolean isCanceled() {
                return canceled;
            }
            
            public void acceptType(IType type) {
                if (resolved != null) {
                    // look for duplicates
                    // this can happen if type exists as working copy as well
                    // or included from multiple locations
                    for (TypeNameMatch typeNameMatch : resolved) {
                        IType existing = typeNameMatch.getType();
                        if (existing.getFullyQualifiedName().equals(type.getFullyQualifiedName())) {
                            // already accepted
                            return;
                        }
                    }
                    resolved.add(new JavaSearchTypeNameMatch(type, 0));
                }
            }
            
            public void acceptPackageFragment(IPackageFragment packageFragment) { }
            public void acceptMethod(IMethod method) { }
            public void acceptMemberType(IType type) { }
            public void acceptInitializer(IInitializer initializer) { }
            public void acceptField(IField field) { }
        };
       
        int searchFor = lookForAnnotation ? NameLookup.ACCEPT_ANNOTATIONS : NameLookup.ACCEPT_ALL;
        lookup.seekTypes(missingType, null, false, searchFor, requestor);
        return resolved.toArray(new TypeNameMatch[0]);
    }
    

}
