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
package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryField;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryMethod;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryType;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceField;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceMethod;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceType;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTFieldNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTMethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyProjectFacade;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.groovy.search.GenericsMapper;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Type requestor for code selection (i.e., hovers and open declaration).
 */
public class CodeSelectRequestor implements ITypeRequestor {

    /** The AST node of interest. */
    private final ASTNode nodeToLookFor;

    private final Region nodeRegion;
    private final Region selectRegion;
    private final GroovyCompilationUnit gunit;
    private final GroovyProjectFacade project;

    private ASTNode requestedNode;
    private IJavaElement requestedElement;

    public CodeSelectRequestor(ASTNode node, GroovyCompilationUnit unit) {
        this(node, null, new Region(Integer.MIN_VALUE, 0), unit);
    }

    public CodeSelectRequestor(ASTNode node, Region nodeRegion, Region selectRegion, GroovyCompilationUnit unit) {
        nodeToLookFor = node;
        this.nodeRegion = nodeRegion;
        this.selectRegion = selectRegion;

        gunit = unit;
        project = new GroovyProjectFacade(unit);
    }

    public ASTNode getRequestedNode() {
        return requestedNode;
    }

    public IJavaElement getRequestedElement() {
        return requestedElement;
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        boolean found = false;
        try {
            // check if enclosingElement does not enclose nodeToLookFor
            if (!interestingElement(enclosingElement)) {
                return VisitStatus.CANCEL_MEMBER;
            }

            if (node instanceof ImportNode && node != nodeToLookFor) {
                node = ((ImportNode) node).getType();
                if (node == null) { // wildcard?
                    return VisitStatus.CONTINUE;
                }
            }

            found = (node == nodeToLookFor);
            if (!found && node.getEnd() > 0) {
                found = (node.getStart() == nodeToLookFor.getStart()) &&
                        (node.getEnd() == nodeToLookFor.getEnd()) &&
                        (typeOf(node).equals(typeOf(nodeToLookFor)));
            }
            if (found) {
                handleMatch(result, enclosingElement);
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Problem with code selection for ASTNode: " + node, e);
        }
        return !found ? VisitStatus.CONTINUE : VisitStatus.STOP_VISIT;
    }

    /**
     * @return {@code true} iff {@code enclosingElement}'s source location
     *     contains the source location of {@link #nodeToLookFor}
     */
    private boolean interestingElement(IJavaElement enclosingElement) throws JavaModelException {
        // the clinit is always interesting since the clinit contains static initializers
        if (enclosingElement.getElementName().equals("<clinit>")) {
            return true;
        }
        if (enclosingElement instanceof ISourceReference) {
            ISourceRange range = ((ISourceReference) enclosingElement).getSourceRange();
            int start = range.getOffset(), until = range.getOffset() + range.getLength();
            boolean covers = start <= nodeToLookFor.getStart() && until >= nodeToLookFor.getEnd();
            covers = covers || (start <= selectRegion.getOffset() && until >= selectRegion.getEnd());
            return covers;
        }
        return false;
    }

    /**
     * Fills in {@link #requestedNode} and {@link #requestedElement} from the
     * specified type lookup result and enclosing element that match up with
     * {@link #nodeToLookFor}.
     */
    private void handleMatch(TypeLookupResult result, IJavaElement enclosingElement) throws JavaModelException {
        requestedNode = result.declaration;
        if (requestedNode instanceof ClassNode) {
            ClassNode classNode = (ClassNode) requestedNode;
            if (!GroovyUtils.getBaseType(classNode).isGenericsPlaceHolder()) {
                requestedNode = classNode.redirect();
            } else {
                requestedElement = findTypeParam(
                    GroovyUtils.getBaseType(classNode).getUnresolvedName(), enclosingElement);
                return;
            }
        } else if (requestedNode instanceof ConstructorNode) {
            if (nodeToLookFor instanceof ConstructorCallExpression &&
                    selectRegion.getOffset() >= ((ConstructorCallExpression) nodeToLookFor).getArguments().getStart()) {
                requestedNode = null; // ignore selections beyond the type name
            } else {
                requestedNode = ((ConstructorNode) requestedNode).getDeclaringClass();
            }
        }

        if (requestedNode != null) {
            if (result.declaration instanceof VariableExpression) {
                VariableExpression varExp = (VariableExpression) result.declaration;
                // look in the local scope
                requestedElement = createLocalVariable(result, enclosingElement, varExp);

            } else if (result.declaration instanceof Parameter) {
                Parameter param = (Parameter) result.declaration;
                // look in the local scope
                int position = param.getStart() - 1;
                if (position < 0) {
                    // could be implicit parameter like 'it'
                    position = nodeToLookFor.getStart() - 1;
                }
                try {
                    requestedElement = createLocalVariable(result, gunit.getElementAt(position), param);
                } catch (JavaModelException e) {
                    Util.log(e, "Problem getting element at " + position + " for file " + gunit.getElementName());
                }

            } else if (nodeToLookFor instanceof PackageNode) {
                int start = nodeToLookFor.getStart(), until = selectRegion.getEnd();
                if (start < until) {
                    String pack = gunit.getSource().substring(start, until);
                    IPackageFragmentRoot root = gunit.getPackageFragmentRoot();
                    requestedElement = root.getPackageFragment(pack);
                }

            } else if (nodeToLookFor instanceof ImportNode &&
                    ((ImportNode) nodeToLookFor).isStar() && !((ImportNode) nodeToLookFor).isStatic()) {
                int start = nodeToLookFor.getStart(), until = selectRegion.getEnd();
                if (start < until) {
                    String pack = gunit.getSource().substring(start, until).replaceFirst("^import\\s+", "");
                    for (IPackageFragmentRoot root : gunit.getJavaProject().getPackageFragmentRoots()) {
                        IPackageFragment frag = root.getPackageFragment(pack);
                        if (frag != null && frag.exists()) {
                            requestedElement = frag;
                            break;
                        }
                    }
                }
                requestedNode = nodeToLookFor; // result.declaration should be java.lang.Object here

            } else {
                String qualifier = checkQualifiedType(result, enclosingElement);
                ClassNode declaringType = findDeclaringType(result);
                if (declaringType != null) {
                    // find it in the java model
                    IType type = project.groovyClassToJavaType(declaringType);
                    if (type == null && !gunit.isOnBuildPath()) {
                        // try to find it in the current compilation unit
                        type = gunit.getType(declaringType.getNameWithoutPackage());
                        if (!type.exists()) {
                            type = null;
                        }
                    }
                    if (type != null) {
                        if (qualifier == null) {
                            // find the requested java element
                            IJavaElement maybeRequested = findRequestedElement(result.declaration, declaringType, type);
                            // try to resolve the type of the requested element; this will add the proper metadata to the hover
                            requestedElement = resolveRequestedElement(maybeRequested, result);
                        } else {
                            // try to resolve as a type (outer class) then as a package
                            IType candidate = gunit.getJavaProject().findType(qualifier);
                            if (candidate != null) {
                                requestedElement = candidate;
                            } else {
                                IPackageFragmentRoot root;
                                if (type instanceof BinaryType) {
                                    root = (IPackageFragmentRoot) ((BinaryType) type).getPackageFragment().getParent();
                                } else {
                                    root = (IPackageFragmentRoot) ((SourceType) type).getPackageFragment().getParent();
                                }
                                requestedElement = root.getPackageFragment(qualifier);
                            }
                            requestedNode = nodeToLookFor;
                        }
                    }
                } else {
                    String message = "Could not proceed due to null declaring type for " + requestedNode;
                    if (GroovyLogManager.manager.hasLoggers()) {
                        GroovyLogManager.manager.log(TraceCategory.CODE_SELECT, message);
                    } else {
                        System.err.println(getClass().getSimpleName() + ": " + message);
                    }
                }
            }
        }
    }

    private LocalVariable createLocalVariable(TypeLookupResult result, IJavaElement enclosingElement, Variable var) {
        int start;
        if (var instanceof Parameter) {
            start = ((Parameter) var).getNameStart();
        } else {
            start = ((VariableExpression) var).getStart();
        }
        int until = start + var.getName().length() - 1;
        ClassNode type = result.type != null ? result.type : var.getType();
        String signature = GroovyUtils.getTypeSignature(type, /*fully-qualified:*/ true, false);
        return new LocalVariable((JavaElement) enclosingElement, var.getName(), start, until, start, until, signature, null, 0, false);
    }

    private String checkQualifiedType(TypeLookupResult result, IJavaElement enclosingElement) throws JavaModelException {
        if (result.declaration instanceof ClassNode ||
            result.declaration instanceof ConstructorNode /*||
            result.declaration instanceof DeclarationExpression*/) {

            ClassNode type = result.type;
            if (type == null) type = result.declaringType;
            if (type == null) type = (ClassNode) result.declaration;
            int typeStart = startOffset(type), typeEnd = endOffset(type);
            type = GroovyUtils.getBaseType(type); // unpack type now that position is known

            if (typeStart >= 0 && typeEnd > typeStart) {
                String gunitSource = gunit.getSource();
                if (typeEnd == gunitSource.length() + 1)
                    typeEnd = gunitSource.length(); // off by one?
                else if (typeEnd > gunitSource.length()) return null;
                String source = gunitSource.substring(typeStart, typeEnd);
                int nameStart = typeStart + source.indexOf(GroovyUtils.splitName(type)[1]);

                // check for code selection on the type name's qualifier string
                if (nameStart > typeStart && nameStart > selectRegion.getEnd() && selectRegion.getEnd() > typeStart) {
                    String selected = gunitSource.substring(typeStart, selectRegion.getEnd());
                    selected = selected.replaceAll("\\.$", ""); // remove any trailing dot
                    String qualifier = GroovyUtils.splitName(type)[0].replace('$', '.');

                    // check for selection in fully-qualified name like 'java.lang.String'
                    Pattern pattern = Pattern.compile("^" + Pattern.quote(selected) + "\\p{javaJavaIdentifierPart}*");
                    Matcher matcher = pattern.matcher(qualifier);
                    if (matcher.find()) {
                        return matcher.group();
                    }
                    // check for selection in qualified name like 'Map.Entry'
                    pattern = Pattern.compile("\\b" + Pattern.quote(selected) + "(?:\\b|$)");
                    matcher = pattern.matcher(qualifier);
                    if (matcher.find()) {
                        return qualifier.substring(0, matcher.end());
                    }
                    // check for selection in aliased name like 'Foo.Entry' with 'import java.util.Map as Foo'
                    ImportNode alias = findImportAlias(selected, enclosingElement);
                    if (alias != null) {
                        // decode 'Foo' to 'Map' and try again, because qualifier could be 'java.util.Map'
                        selected = selected.replace(alias.getAlias(), alias.getType().getNameWithoutPackage());
                        pattern = Pattern.compile("\\b" + Pattern.quote(selected) + "(?:\\b|$)");
                        matcher = pattern.matcher(qualifier);
                        if (matcher.find()) {
                            return qualifier.substring(0, matcher.end());
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds the declaring type of the specified lookup result.
     */
    private ClassNode findDeclaringType(TypeLookupResult result) {
        ClassNode declaringType = null;
        if (result.declaringType != null) {
            declaringType = GroovyUtils.getBaseType(result.declaringType);
        } else if (result.declaration instanceof ClassNode) {
            declaringType = GroovyUtils.getBaseType((ClassNode) result.declaration);
        } else if (result.declaration instanceof FieldNode) {
            declaringType = ((FieldNode) result.declaration).getDeclaringClass();
        } else if (result.declaration instanceof MethodNode) {
            declaringType = ((MethodNode) result.declaration).getDeclaringClass();
        } else if (result.declaration instanceof PropertyNode) {
            declaringType = ((PropertyNode) result.declaration).getDeclaringClass();
        } else if (result.declaration instanceof DeclarationExpression) {
            declaringType = GroovyUtils.getBaseType(((DeclarationExpression) result.declaration).getLeftExpression().getType());
        }
        return declaringType;
    }

    private ImportNode findImportAlias(String name, IJavaElement enclosingElement) {
        IJavaElement elem = enclosingElement;
        while (!(elem instanceof GroovyCompilationUnit)) {
            elem = elem.getParent();
        }
        int dot = name.indexOf('.');
        return ((GroovyCompilationUnit) elem).getModuleNode().getImport(dot < 0 ? name : name.substring(0, dot));
    }

    private ITypeParameter findTypeParam(String name, IJavaElement enclosingElement) throws JavaModelException {
        ITypeParameter typeParam = null;
        if (enclosingElement instanceof IType) {
            for (ITypeParameter tp : ((IType) enclosingElement).getTypeParameters()) {
                if (tp.getElementName().equals(name)) {
                    typeParam = tp;
                    break;
                }
            }
        } else if (enclosingElement instanceof IMethod) {
            for (ITypeParameter tp : ((IMethod) enclosingElement).getTypeParameters()) {
                if (tp.getElementName().equals(name)) {
                    typeParam = tp;
                    break;
                }
            }
        }
        if (typeParam == null && enclosingElement.getParent() != null) {
            typeParam = findTypeParam(name, enclosingElement.getParent());
        }
        return typeParam;
    }

    private IJavaElement findRequestedElement(ASTNode declaration, ClassNode declaringType, IType jdtDeclaringType) throws JavaModelException {
        IJavaElement maybeRequested = null;
        if (declaration instanceof ClassNode) {
            maybeRequested = jdtDeclaringType;
        } else if (jdtDeclaringType.getTypeRoot() != null) {
            String name = null;
            if (declaration instanceof PropertyNode) {
                PropertyNode node = (PropertyNode) declaration;
                name = node.getName();
                // check for possible property node redirection
                if (nodeToLookFor instanceof VariableExpression &&
                        ((VariableExpression) nodeToLookFor).getAccessedVariable() != declaration &&
                        ((VariableExpression) nodeToLookFor).getAccessedVariable() instanceof ASTNode) {
                    declaration = (ASTNode) ((VariableExpression) nodeToLookFor).getAccessedVariable();
                }
                // short-circuit if declaration exists in the Groovy model but not in the Java model
                else if (existsOnlyInGroovyModel(node.getField(), name, declaringType, jdtDeclaringType)) {
                    assert jdtDeclaringType instanceof SourceType;
                    return jdtDeclaringType.getField(name);
                }
            }
            if (declaration instanceof FieldNode) {
                FieldNode node = (FieldNode) declaration;
                name = node.getName();
                // check for @Lazy field
                for (AnnotationNode anno : node.getAnnotations()) {
                    if (anno.getClassNode().getNameWithoutPackage().equals("Lazy") && name.charAt(0) == '$') {
                        name = name.substring(1); // strip the leading $
                    }
                }
                // short-circuit if declaration exists in the Groovy model but not in the Java model
                if (existsOnlyInGroovyModel(node, name, declaringType, jdtDeclaringType)) {
                    assert jdtDeclaringType instanceof SourceType;
                    return jdtDeclaringType.getField(name);
                }
            } else if (declaration instanceof MethodNode) {
                MethodNode node = (MethodNode) declaration;
                name = node.getName();
                if (name.equals("<init>")) {
                    name = jdtDeclaringType.getElementName();
                }
                // short-circuit if declaration exists in the Groovy model but not in the Java model
                if (existsOnlyInGroovyModel(node, name, declaringType, jdtDeclaringType)) {
                    assert jdtDeclaringType instanceof SourceType;
                    return jdtDeclaringType.getMethod(name, GroovyUtils.getParameterTypeSignatures(node, true));
                }
            }

            if (declaration.getEnd() > 0 && name != null) {
                int start = declaration.getStart(), until = declaration.getEnd();
                for (IJavaElement child : jdtDeclaringType.getChildren()) {
                    ISourceRange range = ((ISourceReference) child).getSourceRange();
                    if (range.getOffset() <= start && (range.getOffset() + range.getLength()) >= until && child.getElementName().equals(name)) {
                        maybeRequested = child;
                        break;
                    } else if (start + until < range.getOffset()) {
                        // since children are listed incrementally no need to go further
                        break;
                    }
                }
            }
            if (maybeRequested == null) { // try something else because source location not set right
                if (name != null) {
                    Parameter[] parameters = null;
                    if (declaration instanceof MethodNode) {
                        name = ((MethodNode) declaration).getName();
                        parameters = ((MethodNode) declaration).getParameters();
                    }
                    maybeRequested = findElement(jdtDeclaringType, name, parameters);
                }
                if (maybeRequested == null) {
                    // still couldn't find anything
                    maybeRequested = jdtDeclaringType;
                }
            }
        }
        return maybeRequested;
    }

    private boolean existsOnlyInGroovyModel(FieldNode node, String name, ClassNode declaringType, IType jdtDeclaringType) throws JavaModelException {
        // check for @Field field
        if (node.getEnd() > 0 && node.getDeclaringClass().isScript()) {
            return true;
        }
        // check for @Trait field
        @SuppressWarnings("unchecked")
        List<FieldNode> traitFields = (List<FieldNode>) declaringType.getNodeMetaData("trait.fields");
        if (traitFields != null) {
            for (FieldNode traitField : traitFields) {
                if (traitField == node) {
                    return true;
                }
            }
        }
        // check for @Log, @Singleton, etc. field -- generalized to synthetic field that exists on declaring class
        if (node.getEnd() < 1 && !(node instanceof JDTFieldNode) && declaringType.getField(name) != null && findElement(jdtDeclaringType, name, null) == null) {
            return true;
        }

        return GroovyUtils.isAnonymous(node.getDeclaringClass());
    }

    private boolean existsOnlyInGroovyModel(MethodNode node, String name, ClassNode declaringType, IType jdtDeclaringType) throws JavaModelException {
        // check for @Trait method
        @SuppressWarnings("unchecked")
        List<MethodNode> traitMethods = (List<MethodNode>) declaringType.getNodeMetaData("trait.methods");
        if (traitMethods != null) {
            for (MethodNode traitMethod : traitMethods) {
                if (traitMethod == node) {
                    return true;
                }
            }
        }
        // check for @Newify, @Sortable, @Singleton, etc. -- synthetic method that exists on declaring class
        if (node.getEnd() < 1 && !(node instanceof JDTMethodNode) && !declaringType.getMethods(name).isEmpty() && findElement(jdtDeclaringType, name, node.getParameters()) == null) {
            return true;
        }

        return GroovyUtils.isAnonymous(node.getDeclaringClass());
    }

    /**
     * Converts the requested element into a resolved element by creating a unique key for it.
     */
    private IJavaElement resolveRequestedElement(IJavaElement maybeRequested, TypeLookupResult result) {
        AnnotatedNode declaration = (AnnotatedNode) result.declaration;
        if (declaration instanceof PropertyNode) {
            if (maybeRequested instanceof IField) {
                declaration = ((PropertyNode) declaration).getField();
            } else if (maybeRequested instanceof IMethod) {
                String methodName = maybeRequested.getElementName();
                MethodNode methodNode = declaration.getDeclaringClass().getMethods(methodName).get(0);
                if (methodNode != null) declaration = methodNode;
            }
        } else if (declaration instanceof ConstructorNode && maybeRequested.getElementType() == IJavaElement.TYPE) {
            // implicit default constructor; use type instead
            declaration = declaration.getDeclaringClass();
        }

        String uniqueKey = createUniqueKey(declaration, result.type, result.declaringType, maybeRequested);

        IJavaElement candidate;
        // Create the Groovy Resolved Element, which is like a resolved element, but contains extraDoc, as
        // well as the inferred declaration (which may not be the same as the actual declaration)
        switch (maybeRequested.getElementType()) {
            case IJavaElement.FIELD:
                if (maybeRequested.isReadOnly()) {
                    candidate = new GroovyResolvedBinaryField((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc, declaration);
                } else {
                    candidate = new GroovyResolvedSourceField((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc, declaration);
                }
                break;
            case IJavaElement.METHOD:
                if (maybeRequested.isReadOnly()) {
                    candidate = new GroovyResolvedBinaryMethod((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), ((IMethod) maybeRequested).getParameterTypes(), uniqueKey, result.extraDoc, declaration);
                } else {
                    candidate = new GroovyResolvedSourceMethod((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), ((IMethod) maybeRequested).getParameterTypes(), uniqueKey, result.extraDoc, declaration);
                }
                break;
            case IJavaElement.TYPE:
                if (maybeRequested.isReadOnly()) {
                    candidate = new GroovyResolvedBinaryType((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc, declaration);
                } else {
                    candidate = new GroovyResolvedSourceType((JavaElement) maybeRequested.getParent(), maybeRequested.getElementName(), uniqueKey, result.extraDoc, declaration);
                }
                break;
            default:
                candidate = maybeRequested;
        }
        requestedElement = candidate;
        return requestedElement;
    }

    /**
     * Creates the unique key for classes, fields and methods.
     */
    private String createUniqueKey(AnnotatedNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType, IJavaElement maybeRequested) {
        if (resolvedDeclaringType == null) {
            resolvedDeclaringType = node.getDeclaringClass();
            if (resolvedDeclaringType == null) {
                resolvedDeclaringType = VariableScope.OBJECT_CLASS_NODE;
            }
        }
        if (node instanceof PropertyNode) {
            node = ((PropertyNode) node).getField();
        }

        StringBuilder sb = new StringBuilder();

        if (node instanceof FieldNode) {
            appendUniqueKeyForField(sb, (FieldNode) node, resolvedType, resolvedDeclaringType);
        } else if (node instanceof MethodNode) {
            if (maybeRequested.getElementType() == IJavaElement.FIELD) {
                // this is likely a generated getter or setter
                appendUniqueKeyForGeneratedAccessor(sb, (MethodNode) node, resolvedType, resolvedDeclaringType, (IField) maybeRequested);
            } else {
                appendUniqueKeyForMethod(sb, (MethodNode) node, resolvedType, resolvedDeclaringType);
            }
        } else if (node instanceof ClassNode) {
            appendUniqueKeyForClass(sb, resolvedType, resolvedDeclaringType);
        }

        return sb.toString();
    }

    private void appendUniqueKeyForField(StringBuilder sb, FieldNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType) {
        appendUniqueKeyForClass(sb, node.getDeclaringClass(), resolvedDeclaringType);
        sb.append('.').append(node.getName()).append(')');
        appendUniqueKeyForResolvedClass(sb, resolvedType);
    }

    private void appendUniqueKeyForMethod(StringBuilder sb, MethodNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType) {
        // TODO: This method does not handle capture types like Java does; example for Plain.class.newInstance():
        // LPlain;&Ljava/lang/Class<!Ljava/lang/Class;{0}+LPlain;152;>;.newInstance()!+LPlain;|Ljava/lang/InstantiationException;|Ljava/lang/IllegalAccessException;

        // declaring type
        appendUniqueKeyForClass(sb, node.getDeclaringClass(), resolvedDeclaringType);

        // method name
        String methodName = node.getName();
        if (methodName.equals("<init>"))
            methodName = node.getDeclaringClass().getNameWithoutPackage();
        // BindingKeyParser fails if method name has 2 or more $s
        if (methodName.indexOf('$') != methodName.lastIndexOf('$')) {
            methodName = methodName.replace('$', '_');
        }
        sb.append(Signature.C_DOT).append(methodName);

        // generic types
        GenericsType[] generics = GroovyUtils.getGenericsTypes(node);
        if (generics.length > 0) {
            sb.append(Signature.C_GENERIC_START);
            for (GenericsType gt : generics) {
                appendUniqueKeyForGenericsType(sb, gt);
            }
            sb.append(Signature.C_GENERIC_END);
        }

        // parameters
        sb.append(Signature.C_PARAM_START);
        Parameter[] parameters = node.getOriginal().getParameters();
        if (parameters != null) {
            for (Parameter param : parameters) {
                ClassNode paramType = param.getType();
                appendUniqueKeyForClass(sb, paramType, resolvedDeclaringType);
            }
        }
        sb.append(Signature.C_PARAM_END);

        // return type
        appendUniqueKeyForClass(sb, node.getOriginal().getReturnType(), resolvedDeclaringType);

        // generic type resolution
        if (generics.length > 0) {
            GenericsMapper mapper = GenericsMapper.gatherGenerics(GroovyUtils.getParameterTypes(node.getParameters()), resolvedDeclaringType, node.getOriginal());

            sb.append('%');
            sb.append(Signature.C_GENERIC_START);
            for (GenericsType gt : generics) {
                gt = VariableScope.clone(gt, 0);
                ClassNode rt = VariableScope.resolveTypeParameterization(mapper, gt, gt.getType());
                sb.append(GroovyUtils.getTypeSignatureWithoutGenerics(rt, true, true).replace('.', '/'));
            }
            sb.append(Signature.C_GENERIC_END);
        }

        // exceptions
        if (node.getExceptions() != null) {
            for (ClassNode exception : node.getExceptions()) {
                sb.append(Signature.C_INTERSECTION);
                appendUniqueKeyForClass(sb, exception, resolvedDeclaringType);
            }
        }
    }

    private void appendUniqueKeyForGeneratedAccessor(StringBuilder sb, MethodNode node, ClassNode resolvedType, ClassNode resolvedDeclaringType, IField actualField) {
        appendUniqueKeyForClass(sb, node.getDeclaringClass(), resolvedDeclaringType);
        sb.append('.').append(actualField.getElementName()).append(')');
        ClassNode typeOfField = node.getName().startsWith("set")  && node.getParameters() != null && node.getParameters().length > 0 ? node.getParameters()[0].getType(): resolvedType;
        appendUniqueKeyForResolvedClass(sb, typeOfField);
    }

    /**
     * Tries to resolve any type parameters in unresolvedType based on those in resolvedDeclaringType.
     *
     * @param unresolvedType unresolved type whose type parameters need to be resolved
     * @param resolvedDeclaringType the resolved type that is the context in which to resolve it.
     */
    private void appendUniqueKeyForClass(StringBuilder sb, ClassNode unresolvedType, ClassNode resolvedDeclaringType) {
        GenericsMapper mapper = GenericsMapper.gatherGenerics(resolvedDeclaringType, resolvedDeclaringType.redirect());
        ClassNode resolvedType = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(unresolvedType));
        appendUniqueKeyForResolvedClass(sb, resolvedType);
    }

    private void appendUniqueKeyForResolvedClass(StringBuilder sb, ClassNode resolvedType) {
        String signature = GroovyUtils.getTypeSignature(resolvedType, true, true);

        sb.append(signature.replace('.', '/'));
    }

    private void appendUniqueKeyForGenericsType(StringBuilder sb, GenericsType gt) {
        String[] bounds = CharOperation.NO_STRINGS;
        if (gt.getLowerBound() != null) {
            ClassNode lb = gt.getLowerBound();
            bounds = (String[]) ArrayUtils.add(bounds, GroovyUtils.getTypeSignature(lb, true, true));
        } else if (gt.getUpperBounds() != null) {
            for (ClassNode ub : gt.getUpperBounds()) {
                bounds = (String[]) ArrayUtils.add(bounds, GroovyUtils.getTypeSignature(ub, true, true));
            }
        }
        String signature = Signature.createTypeParameterSignature(gt.getName(), bounds);

        sb.append(signature.replace('.', '/'));
    }

    private IJavaElement findElement(IType type, String text, Parameter[] parameters) throws JavaModelException {
        if (text.equals(type.getElementName())) {
            return type;
        }
        if (text.equals("<init>")) {
            text = type.getElementName();
        }

        // check for methods first, then fields, and finally accessor variants of the name

StringBuilder checked = new StringBuilder();
        IMethod closestMatch = null;
        next_method: for (IMethod method : type.getMethods()) {
            if (method.getElementName().equals(text)) {
                closestMatch = method;
                // prefer methods with the same parameter list
                if (parameters != null && parameters.length == method.getParameterTypes().length) {
checked.append("\n\t").append(text).append('(');
                    for (int i = 0, n = parameters.length; i < n; i += 1) {
                        // remove generics from the type signatures to make matching simpler
                        String jdtMethodParam = removeGenerics(method.getParameterTypes()[i]);
                        String astMethodParam = GroovyUtils.getTypeSignatureWithoutGenerics(
                            parameters[i].getOriginType(), jdtMethodParam.indexOf('.') > 0, type.isBinary());
checked.append(jdtMethodParam).append(' ');
                        if (!astMethodParam.equals(jdtMethodParam)) {
                            continue next_method;
                        }
                    }
                    return method;
                }
            }
        }
        if (closestMatch != null) {
String message = String.format("%s.findElement: no exact match found for %s(%s); options considered:%s",
    getClass().getSimpleName(), text, parameters == null ? "" : GroovyUtils.getParameterTypes(parameters), checked);
if (GroovyLogManager.manager.hasLoggers()) {
    GroovyLogManager.manager.log(TraceCategory.CODE_SELECT, message);
} else {
    System.err.println(getClass().getSimpleName() + ": " + message);
}
            return closestMatch;
        }

        IField field = type.getField(text);
        String prefix;
        if (!field.exists() && (prefix = extractPrefix(text)) != null) {
            // try as a property
            String newName = Character.toLowerCase(text.charAt(prefix.length())) + text.substring(prefix.length() + 1);
            field = type.getField(newName);
        }
        if (field.exists()) {
            return field;
        }

        String setMethod = AccessorSupport.SETTER.createAccessorName(text);
        String getMethod = AccessorSupport.GETTER.createAccessorName(text);
        String isMethod = AccessorSupport.ISSER.createAccessorName(text);

        for (IMethod method : type.getMethods()) {
            String methodName = method.getElementName();
            if (methodName.equals(setMethod) || methodName.equals(getMethod) || methodName.equals(isMethod)) {
                return method;
            }
        }

        return null;
    }

    private static String removeGenerics(String param) {
        // TODO: Check for nested generics
        int genericStart = param.indexOf('<');
        if (genericStart > 0) {
            param = param.substring(0, genericStart) + param.substring(param.indexOf('>') + 1, param.length());
        }
        return param;
    }

    private static String extractPrefix(String text) {
        if (text.startsWith("is")) {
            if (text.length() > 2) {
                return "is";
            }
        } else if (text.startsWith("get")) {
            if (text.length() > 3) {
                return "get";
            }
        } else if (text.startsWith("set")) {
            if (text.length() > 3) {
                return "set";
            }
        }
        return null;
    }

    private int startOffset(ClassNode type) {
        int start = type.getStart();
        if (nodeToLookFor instanceof ImportNode) {
            // recover the qualifier position for imports
            start = endOffset(type) - type.getName().length();
        } else if (type.getEnd() < 1) {
            if (nodeRegion != null) {
                start = nodeRegion.getOffset();
            } else {
                start = nodeToLookFor.getStart();
            }
        }
        return start;
    }

    private int endOffset(ClassNode type) {
        int end = type.getEnd();
        if (nodeToLookFor instanceof ImportNode) {
            end = ((ImportNode) nodeToLookFor).getTypeEnd();
        } else if (end < 1) {
            if (nodeRegion != null) {
                end = nodeRegion.getEnd();
            } else {
                end = nodeToLookFor.getEnd();
            }
        }
        return end;
    }

    private Object typeOf(ASTNode node) {
        if (node instanceof ClassNode) {
            return ((ClassNode) node).getNameWithoutPackage();
        }
        return node.getClass();
    }
}
