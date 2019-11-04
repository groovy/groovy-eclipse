/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.refactoring.formatter.SemicolonRemover;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.groovy.core.util.JavaConstants;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.manipulation.util.Strings;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedConstructorsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedMethodsOperation;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.TokenScanner;
import org.eclipse.jdt.internal.corext.refactoring.TypeContextChecker;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.TextEdit;

public class GroovyTypeBuilder {

    public void setPackageFragmentRoot(IPackageFragmentRoot root) {
        this.root = root;
    }
    private IPackageFragmentRoot root;

    public void setPackageFragment(IPackageFragment pack) {
        this.pack = pack;
    }
    private IPackageFragment pack;

    public void setEnclosingType(IType enclosingType) {
        this.enclosingType = enclosingType;
    }
    private IType enclosingType;

    public void setAddComments(boolean addComments) {
        this.addComments = addComments;
    }
    private boolean addComments;

    public void setTypeFlags(int modifiers) {
        this.modifiers = modifiers;
    }
    private int modifiers;

    public void setTypeKind(String typeKind) {
        this.typeKind = typeKind;
    }
    private String typeKind;

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    private String typeName;

    //

    public void setSuper(String superclass) {
        this.superclass = superclass;
    }
    private String superclass;

    public void setFaces(String... interfaces) {
        this.interfaces = interfaces;
    }
    private String[] interfaces;

    public void setStubs(boolean... methodStubs) {
        this.methodStubs = methodStubs;
    }
    private boolean[] methodStubs;

    //--------------------------------------------------------------------------

    public IType build(IProgressMonitor monitor) throws CoreException, InterruptedException {
        return build(monitor, null);
    }

    public IType build(IProgressMonitor monitor, BiConsumer<IType, IProgressMonitor> otherSteps) throws CoreException, InterruptedException {
        monitor = SubMonitor.convert(monitor, NewWizardMessages.NewTypeWizardPage_operationdesc, 10);

        IProject project = root.getJavaProject().getProject();
        if (!GroovyNature.hasGroovyNature(project)) {
            GroovyRuntime.addGroovyNature(project);
        }
        monitor.worked(1);

        if (pack == null) {
            pack = root.getPackageFragment("");
        }
        if (pack.exists()) {
            monitor.worked(1);
        } else {
            String packName = pack.getElementName();
            pack = root.createPackageFragment(packName, true, ((SubMonitor) monitor).split(1));
        }

        if (superclass == null) {
            superclass = ("script".equals(typeKind) ? "groovy.lang.Script" : "java.lang.Object");
        }

        ICompilationUnit connectedCU = null;
        try {
            IType createdType;
            boolean needsSave;
            boolean isInnerClass = (enclosingType != null);
            String typeName = getTypeNameWithoutParameters(this.typeName);
            int indent = isInnerClass ? getIndentUsed(enclosingType) + 1 : 0;
            String lineDelimiter = getLineDelimiterUsed(isInnerClass ? enclosingType : pack.getJavaProject());

            if (!isInnerClass) {
                ICompilationUnit cu = pack.createCompilationUnit(getCompilationUnitName(typeName), "", false, ((SubMonitor) monitor).split(2));

                needsSave = true;
                cu.becomeWorkingCopy(((SubMonitor) monitor).split(1));
                connectedCU = cu; // cu is now a (primary) working copy

                IBuffer buffer = cu.getBuffer();

                // generate skeleton: file comment, package statement, type comment, and type placeholder
                String fileComment = null, typeComment = null, typeContent = "class " + typeName + " {}";
                if (addComments) {
                    fileComment = CodeGeneration.getFileComment(cu, lineDelimiter);
                    String comment = CodeGeneration.getTypeComment(cu, typeName, CharOperation.NO_STRINGS, lineDelimiter);
                    if (isValidComment(comment)) {
                        typeComment = comment;
                    }
                }
                String unitContent = CodeGeneration.getCompilationUnitContent(cu, fileComment, typeComment, typeContent, lineDelimiter);
                if (unitContent != null) {
                    ASTParser parser = ASTParser.newParser(JavaConstants.AST_LEVEL);
                    parser.setKind(ASTParser.K_COMPILATION_UNIT);
                    parser.setProject(pack.getJavaProject());
                    parser.setSource(unitContent.toCharArray());
                    parser.setUnitName(cu.getPath().toString());
                    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
                    if ((pack.isDefaultPackage() || unit.getPackage() != null) && !unit.types().isEmpty()) {
                        buffer.setContents(unitContent);
                    }
                }
                if (buffer.getLength() == 0) {
                    StringBuilder content = new StringBuilder();
                    if (fileComment != null && !fileComment.isEmpty()) {
                        content.append(fileComment).append(lineDelimiter);
                    }
                    if (!pack.isDefaultPackage()) {
                        content.append("package ").append(pack.getElementName());
                    }
                    content.append(lineDelimiter).append(lineDelimiter);
                    if (typeComment != null && !typeComment.isEmpty()) {
                        content.append(typeComment).append(lineDelimiter);
                    }
                    content.append(typeContent);
                    buffer.setContents(content.toString());
                }

                captureImports(cu);
                // add an import that will be removed; having this import solves 14661
                addImport(JavaModelUtil.concatenateName(pack.getElementName(), typeName));

                int index = unitContent.lastIndexOf(typeContent);
                if (index == -1) {
                    AbstractTypeDeclaration typeNode = (AbstractTypeDeclaration) astRoot.types().get(0);
                    int start = ((ASTNode) typeNode.modifiers().get(0)).getStartPosition();
                    int end = typeNode.getStartPosition() + typeNode.getLength();

                    buffer.replace(start, end - start, buildTypeStub(cu, lineDelimiter));
                } else {
                    buffer.replace(index, typeContent.length(), buildTypeStub(cu, lineDelimiter));
                }
                JavaModelUtil.reconcile(cu);
                createdType = cu.getType(typeName);
            } else {
                ICompilationUnit cu = enclosingType.getCompilationUnit();

                needsSave = !cu.isWorkingCopy();
                cu.becomeWorkingCopy(((SubMonitor) monitor).split(1));
                connectedCU = cu; // cu is now for sure (primary) a working copy

                captureImports(cu);
                // add imports that will be removed; having the imports solves 14661
                IType[] topLevelTypes = cu.getTypes();
                for (int i = 0, n = topLevelTypes.length; i < n; i += 1) {
                    addImport(topLevelTypes[i].getFullyQualifiedName('.'));
                }

                StringBuilder typeContent = new StringBuilder();
                if (addComments) {
                    String typeComment = CodeGeneration.getTypeComment(cu, JavaModelUtil.concatenateName(enclosingType.getTypeQualifiedName('.'), typeName), CharOperation.NO_STRINGS, lineDelimiter);
                    if (isValidComment(typeComment)) {
                        typeContent.append(typeComment).append(lineDelimiter);
                    }
                }
                typeContent.append(buildTypeStub(cu, lineDelimiter));

                IJavaElement sibling = null;
                if (enclosingType.isEnum()) {
                    IField[] fields = enclosingType.getFields();
                    if (fields.length > 0) {
                        for (int i = 0, n = fields.length; i < n; i += 1) {
                            if (!fields[i].isEnumConstant()) {
                                sibling = fields[i];
                                break;
                            }
                        }
                    }
                } else {
                    IJavaElement[] elems = enclosingType.getChildren();
                    if (elems.length > 0) sibling = elems[0];
                }

                createdType = enclosingType.createType(typeContent.toString(), sibling, false, ((SubMonitor) monitor).split(2));
            }
            if (monitor.isCanceled()) {
                throw new InterruptedException();
            }

            // add imports for superclass/interfaces, so types can be resolved correctly
            ICompilationUnit cu = createdType.getCompilationUnit();
            rewriteImports(false, ((SubMonitor) monitor).split(1));
            JavaModelUtil.reconcile(cu);
            if (monitor.isCanceled()) {
                throw new InterruptedException();
            }

            captureImports(cu);
            // members can be added now that super types are known/resolved
            buildTypeMembers(createdType, indent, ((SubMonitor) monitor).split(1));
            if (otherSteps != null) {
                otherSteps.accept(createdType, ((SubMonitor) monitor).split(1));
            } else {
                monitor.worked(1);
            }
            rewriteImports(true, ((SubMonitor) monitor).split(1));
            JavaModelUtil.reconcile(cu);
            if (monitor.isCanceled()) {
                throw new InterruptedException();
            }

            // format content
            IBuffer buffer = cu.getBuffer();
            ISourceRange range = createdType.getSourceRange();
            String originalContent = buffer.getText(range.getOffset(), range.getLength());
            String formattedContent = CodeFormatterUtil.format(CodeFormatter.K_CLASS_BODY_DECLARATIONS, originalContent, indent, lineDelimiter, pack.getJavaProject());
            formattedContent = Strings.trimLeadingTabsAndSpaces(formattedContent);
            buffer.replace(range.getOffset(), range.getLength(), formattedContent);

            TextSelection selection;
            if (!isInnerClass) {
                selection = new TextSelection(0, buffer.getLength());
            } else {
                JavaModelUtil.reconcile(cu);
                range = createdType.getSourceRange();
                selection = new TextSelection(range.getOffset(), range.getLength());
            }

            // remove semicolons
            TextEdit textEdit = new SemicolonRemover(selection, new Document(buffer.getContents())).format();
            if (textEdit.hasChildren()) {
                cu.applyTextEdit(textEdit, ((SubMonitor) monitor).split(1));
            } else {
                monitor.worked(1);
            }

            // finish
            if (needsSave) {
                cu.commitWorkingCopy(true, ((SubMonitor) monitor).split(1));
            } else {
                monitor.worked(1);
            }

            return createdType;
        } finally {
            astRoot = null;
            importsRewrite = null;
            existingImports = null;
            if (connectedCU != null) {
                connectedCU.discardWorkingCopy();
            }
        }
    }

    private String buildTypeStub(ICompilationUnit cu, String lineDelimiter) throws CoreException {
        StringBuilder typeStub = new StringBuilder();

        if ("script".equals(typeKind)) {
            if (!"groovy.lang.Script".equals(superclass)) {
                typeStub.append("@").append(addImport("groovy.transform.BaseScript")).append(" ").append(addImport(superclass)).append(" self").append(lineDelimiter).append(lineDelimiter);
            }
        } else {
            if ("trait".equals(typeKind) && !"java.lang.Object".equals(superclass)) {
                typeStub.append("@").append(addImport("groovy.transform.SelfType")).append("(").append(addImport(superclass)).append(")").append(lineDelimiter);
            }

            // modifiers
            int groovyModifiers;
            if (Flags.isPackageDefault(modifiers)) {
                groovyModifiers = (modifiers & ~(Flags.AccPublic | Flags.AccPrivate | Flags.AccProtected));
                typeStub.append("@").append(addImport("groovy.transform.PackageScope")).append(" ");
            } else {
                groovyModifiers = (modifiers & ~Flags.AccPublic); // public is default in Groovy
            }
            typeStub.append(Flags.toString(groovyModifiers));
            if (groovyModifiers != 0) typeStub.append(' ');

            // type kind and name
            typeStub.append(typeKind).append(' ').append(typeName);

            // super class
            if ("class".equals(typeKind) && !"java.lang.Object".equals(superclass)) {
                typeStub.append(" extends ");

                ITypeBinding binding = null;
                IType fCurrType = cu.getType(typeName);
                if (fCurrType != null) {
                    try {
                        binding = TypeContextChecker.resolveSuperClass(superclass, fCurrType,
                            TypeContextChecker.createSuperClassStubTypeContext(typeName, enclosingType, pack));
                    } catch (IllegalStateException e) {
                        // probably an ImmutableClassNode (no source position)
                    }
                }
                if (binding != null) {
                    typeStub.append(addImport(binding));
                } else {
                    typeStub.append(addImport(superclass));
                }
            }

            // super interfaces
            if (DefaultGroovyMethods.asBoolean(interfaces)) {
                if ("interface".equals(typeKind)) {
                    typeStub.append(" extends ");
                } else {
                    typeStub.append(" implements ");
                }

                ITypeBinding[] bindings;
                IType fCurrType = cu.getType(typeName);
                if (fCurrType == null) {
                    bindings = new ITypeBinding[interfaces.length];
                } else {
                    bindings = TypeContextChecker.resolveSuperInterfaces(interfaces, fCurrType,
                        TypeContextChecker.createSuperInterfaceStubTypeContext(typeName, enclosingType, pack));
                }

                for (int i = 0, n = interfaces.length; i < n; i += 1) {
                    ITypeBinding binding = bindings[i];
                    if (binding != null) {
                        typeStub.append(addImport(binding));
                    } else {
                        typeStub.append(addImport(interfaces[i]));
                    }
                    if (i < (n - 1)) {
                        typeStub.append(", ");
                    }
                }
            }

            typeStub.append(" {").append(lineDelimiter);

            String templateID = "";
            switch (typeKind) {
            case "class":
            case "trait":
            case "script":
                templateID = CodeGeneration.CLASS_BODY_TEMPLATE_ID;
                break;
            case "enum":
                templateID = CodeGeneration.ENUM_BODY_TEMPLATE_ID;
                break;
            case "interface":
                templateID = CodeGeneration.INTERFACE_BODY_TEMPLATE_ID;
                break;
            case "@interface":
                templateID = CodeGeneration.ANNOTATION_BODY_TEMPLATE_ID;
                break;
            }
            String typeBody = CodeGeneration.getTypeBody(templateID, cu, typeName, lineDelimiter);
            if (typeBody != null) {
                typeStub.append(typeBody);
            }

            typeStub.append('}').append(lineDelimiter);
        }

        return typeStub.toString();
    }

    private void buildTypeMembers(IType type, int indent, IProgressMonitor monitor) throws CoreException {
        if (methodStubs == null) return;

        boolean buildConstructors = (methodStubs.length > 0 && methodStubs[0]);
        boolean buildUnimplemented = (methodStubs.length > 1 && methodStubs[1]);
        boolean buildStaticVoidMain = (methodStubs.length > 2 && methodStubs[2]);

        if (buildConstructors || buildUnimplemented) {
            monitor.beginTask("Create inherited methods", 2);

            ITypeBinding binding = ASTNodes.getTypeBinding(astRoot, type);
            if (binding != null) {
                if (buildUnimplemented) {
                    AddUnimplementedMethodsOperation operation = new AddUnimplementedMethodsOperation(astRoot, binding, null, -1, false, true, false);
                    operation.setCreateComments(addComments);
                    operation.run(((SubMonitor) monitor).split(1));
                    Arrays.stream(operation.getCreatedImports()).forEach(this::addImport);
                } else {
                    monitor.worked(1);
                }

                if (buildConstructors) {
                    // constructor changes in Eclipse IDE 4.12
                    AddUnimplementedConstructorsOperation operation;
                    Constructor<AddUnimplementedConstructorsOperation> ctor;
                    try {
                        ctor = ReflectionUtils.getConstructor(AddUnimplementedConstructorsOperation.class, new Class[] {CompilationUnit.class, ITypeBinding.class, IMethodBinding[].class, int.class, boolean.class, boolean.class, boolean.class, Map.class});
                        operation = ReflectionUtils.invokeConstructor(ctor, astRoot, binding, null, Integer.valueOf(-1), Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, null);
                    } catch (RuntimeException e) {
                        ctor = ReflectionUtils.getConstructor(AddUnimplementedConstructorsOperation.class, new Class[] {CompilationUnit.class, ITypeBinding.class, IMethodBinding[].class, int.class, boolean.class, boolean.class, boolean.class});
                        operation = ReflectionUtils.invokeConstructor(ctor, astRoot, binding, null, Integer.valueOf(-1), Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
                    }
                    operation.setOmitSuper(false);
                    operation.setCreateComments(addComments);
                    operation.run(((SubMonitor) monitor).split(1));
                    Arrays.stream(operation.getCreatedImports()).forEach(this::addImport);
                } else {
                    monitor.worked(1);
                }
            }
        }

        if (buildStaticVoidMain) {
            monitor.beginTask("Create main method", 2);

            final String endl = "\n";
            StringBuilder stub = new StringBuilder();

            if (addComments) {
                String comment = CodeGeneration.getMethodComment(type.getCompilationUnit(), type.getTypeQualifiedName('.'),
                    "main", new String[] {"args"}, CharOperation.NO_STRINGS, Signature.createTypeSignature("void", true), null, endl);
                if (comment != null) {
                    stub.append(comment).append(endl);
                }
            }

            stub.append("static void main(args) {").append(endl);

            String content = CodeGeneration.getMethodBodyContent(type.getCompilationUnit(), type.getTypeQualifiedName('.'), "main", false, "", endl);
            if (content != null && !content.isEmpty()) {
                String margin = CodeFormatterUtil.createIndentString(indent + 1, type.getJavaProject());
                String source = Strings.changeIndent(content, 0, type.getJavaProject(), margin, endl);

                stub.append(margin).append(source);
            }

            stub.append(endl).append("}");
            monitor.worked(1);

            JavaModelUtil.reconcile(type.getCompilationUnit());
            IJavaElement sibling = type.getChildren().length > 0 ? type.getChildren()[0] : null;
            type.createMethod(stub.toString(), sibling, false, ((SubMonitor) monitor).split(1));
        }
    }

    private void captureImports(ICompilationUnit cu) {
        ASTParser parser = ASTParser.newParser(JavaConstants.AST_LEVEL);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setFocalPosition(0);
        parser.setSource(cu);

        astRoot = (CompilationUnit) parser.createAST(null);

        importsRewrite = CodeStyleConfiguration.createImportRewrite(astRoot, true);

        existingImports = ((List<ImportDeclaration>) astRoot.imports())
            .stream().map(ASTNodes::asString).collect(Collectors.toSet());
    }

    private CompilationUnit astRoot;
    private Set<String> existingImports;
    private ImportRewrite importsRewrite;

    private String addImport(String qualTypeName) {
        return importsRewrite.addImport(qualTypeName);
    }

    private String addImport(ITypeBinding typeBinding) {
        return importsRewrite.addImport(typeBinding);
    }

    private void rewriteImports(boolean removeUnused, IProgressMonitor monitor) throws CoreException {
        // TODO: if (removeUnused) see below

        importsRewrite.getCompilationUnit().applyTextEdit(importsRewrite.rewriteImports(monitor), monitor);
    }

    /*private void removeUnusedImports(ICompilationUnit cu) throws CoreException {
        ASTParser parser= ASTParser.newParser(IASTSharedValues.SHARED_AST_LEVEL);
        parser.setSource(cu);
        parser.setResolveBindings(true);

        CompilationUnit root= (CompilationUnit) parser.createAST(null);
        if (root.getProblems().length == 0) {
            return;
        }

        List<ImportDeclaration> importsDecls= root.imports();
        if (importsDecls.isEmpty()) {
            return;
        }
        ImportsManager imports= new ImportsManager(root);

        int importsEnd= ASTNodes.getExclusiveEnd(importsDecls.get(importsDecls.size() - 1));
        IProblem[] problems= root.getProblems();
        for (int i= 0; i < problems.length; i++) {
            IProblem curr= problems[i];
            if (curr.getSourceEnd() < importsEnd) {
                int id= curr.getID();
                if (id == IProblem.UnusedImport || id == IProblem.NotVisibleType) { // not visible problems hide unused -> remove both
                    int pos= curr.getSourceStart();
                    for (int k= 0; k < importsDecls.size(); k++) {
                        ImportDeclaration decl= importsDecls.get(k);
                        if (decl.getStartPosition() <= pos && pos < decl.getStartPosition() + decl.getLength()) {
                            if (existingImports.isEmpty() || !existingImports.contains(ASTNodes.asString(decl))) {
                                String name= decl.getName().getFullyQualifiedName();
                                if (decl.isOnDemand()) {
                                    name += ".*";
                                }
                                if (decl.isStatic()) {
                                    imports.removeStaticImport(name);
                                } else {
                                    imports.removeImport(name);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }*/

    //

    private static boolean isValidComment(String template) {
        if (template != null) {
            IScanner scanner = ToolFactory.createScanner(true, false, false, false);
            scanner.setSource(template.toCharArray());
            try {
                int next = scanner.getNextToken();
                while (TokenScanner.isComment(next)) {
                    next = scanner.getNextToken();
                }
                return (next == ITerminalSymbols.TokenNameEOF);
            } catch (InvalidInputException ignore) {
            }
        }
        return false;
    }

    private static int getIndentUsed(IJavaElement element) {
        try {
            IOpenable openable = element.getOpenable();
            if (openable instanceof ITypeRoot) {
                IBuffer buffer = openable.getBuffer();
                if (buffer != null) {
                    int offset = ((ISourceReference) element).getSourceRange().getOffset();
                    int i = offset;
                    // find beginning of line
                    while (i > 0 && !IndentManipulation.isLineDelimiterChar(buffer.getChar(i - 1))) {
                        i -= 1;
                    }
                    return Strings.computeIndentUnits(buffer.getText(i, offset - i), element.getJavaProject());
                }
            }
        } catch (JavaModelException ignore) {
        }
        return 0;
    }

    private static String getLineDelimiterUsed(IJavaElement element) {
        try {
            return element.getOpenable().findRecommendedLineSeparator();
        } catch (JavaModelException ignore) {
            return org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR;
        }
    }

    private static String getCompilationUnitName(String typeNameWithoutParameters) {
        return typeNameWithoutParameters + ".groovy";
    }

    private static String getTypeNameWithoutParameters(String typeNameWithParameters) {
        int angleBracketOffset = typeNameWithParameters.indexOf('<');
        if (angleBracketOffset == -1) {
            return typeNameWithParameters;
        } else {
            return typeNameWithParameters.substring(0, angleBracketOffset);
        }
    }
}
