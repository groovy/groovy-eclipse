/*
 * Copyright 2009-2021 the original author or authors.
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

import static java.beans.Introspector.decapitalize;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
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
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTFieldNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTMethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.JavaCoreUtil;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
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

/**
 * Type requestor for code selection (i.e., hovers and open declaration).
 */
public class CodeSelectRequestor implements ITypeRequestor {

    /** The AST node of interest. */
    private ASTNode nodeToLookFor;

    private final Region nodeRegion;
    private final Region selectRegion;
    private final GroovyCompilationUnit gunit;

    private ASTNode requestedNode;
    private final List<IJavaElement> requestedElements = new java.util.ArrayList<>();

    public CodeSelectRequestor(final ASTNode node, final GroovyCompilationUnit unit) {
        this(node, null, new Region(Integer.MIN_VALUE, 0), unit);
    }

    public CodeSelectRequestor(final ASTNode node, final Region nodeRegion, final Region selectRegion, final GroovyCompilationUnit unit) {
        nodeToLookFor = node;
        this.nodeRegion = nodeRegion;
        this.selectRegion = selectRegion;

        gunit = unit;
    }

    public ASTNode getRequestedNode() {
        return requestedNode;
    }

    public IJavaElement[] getRequestedElements() {
        return requestedElements.toArray(new IJavaElement[requestedElements.size()]);
    }

    private void putRequestedElement(final IJavaElement requestedElement) {
        requestedElements.add(Objects.requireNonNull(requestedElement));
    }

    @Override
    public VisitStatus acceptASTNode(ASTNode node, final TypeLookupResult result, final IJavaElement enclosingElement) {
        boolean found = false;
        try {
            // check if enclosingElement does not enclose nodeToLookFor
            if (!isInterestingElement(enclosingElement)) {
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
     * @return {@code true} if {@code enclosingElement} contains {@link #nodeToLookFor} or is necessary for type inference
     */
    private boolean isInterestingElement(final IJavaElement enclosingElement) throws JavaModelException {
        switch (enclosingElement.getElementType()) {
        case IJavaElement.FIELD:
            if ("Qjava.lang.Object;".equals(((IField) enclosingElement).getTypeSignature())) {
                return true;
            }
            break;
        case IJavaElement.METHOD:
            if (isInitializerMethod((IMethod) enclosingElement)) {
                return true;
            }
            break;
        case IJavaElement.INITIALIZER:
            return true;
        }

        if (enclosingElement instanceof ISourceReference) {
            ISourceRange range = ((ISourceReference) enclosingElement).getSourceRange();
            int start = range.getOffset(), until = range.getOffset() + range.getLength();
            boolean covers = (start <= nodeToLookFor.getStart() && until >= nodeToLookFor.getEnd());
            covers = (covers || (start <= selectRegion.getOffset() && until >= selectRegion.getEnd()));
            return covers;
        }

        return false;
    }

    private boolean isInitializerMethod(final IMethod method) throws JavaModelException {
        if (method.isConstructor()) {
            return true;
        }
        if (!Flags.isStatic(method.getFlags())) {
            for (IAnnotation annotation : method.getAnnotations()) {
                String name = annotation.getElementName();
                if (name.endsWith("PostConstruct")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Fills in {@link #requestedNode} and {@link #requestedElement} from the
     * specified type lookup result and enclosing element that match up with
     * {@link #nodeToLookFor}.
     */
    private void handleMatch(final TypeLookupResult result, final IJavaElement enclosingElement) throws JavaModelException {
        requestedNode = result.declaration;
        if (requestedNode instanceof ClassNode) {
            ClassNode classNode = (ClassNode) requestedNode;
            if (!GroovyUtils.getBaseType(classNode).isGenericsPlaceHolder()) {
                requestedNode = classNode.redirect();
            } else {
                putRequestedElement(findTypeParam(GroovyUtils.getBaseType(classNode).getUnresolvedName(), enclosingElement));
                return;
            }
        } else if (requestedNode instanceof ConstructorNode) {
            if (nodeToLookFor instanceof ConstructorCallExpression &&
                    isSelectionInArguments((ConstructorCallExpression) nodeToLookFor)) {
                requestedNode = null; // ignore selections beyond the type name
            } else {
                requestedNode = ((ConstructorNode) requestedNode).getDeclaringClass();
            }
        } else if (requestedNode instanceof PropertyNode) {
            if (((PropertyNode) requestedNode).isSynthetic()) {
                requestedNode = null; // ignore synthetic properties
            }
        } else if (requestedNode instanceof ImportNode) {
            ImportNode importNode = (ImportNode) requestedNode;
            if (importNode.getType() != null) {
                requestedNode = importNode.getType().redirect();
            }
        }

        if (requestedNode != null) {
            if (nodeToLookFor instanceof PackageNode) {
                int start = nodeToLookFor.getStart(), until = selectRegion.getEnd();
                if (start < until) {
                    String pack = gunit.getSource().substring(start, until);
                    IPackageFragmentRoot root = gunit.getPackageFragmentRoot();
                    putRequestedElement(root.getPackageFragment(pack));
                }
            } else if (nodeToLookFor instanceof ImportNode && ((ImportNode) nodeToLookFor).isStar() && !((ImportNode) nodeToLookFor).isStatic()) {
                int start = nodeToLookFor.getStart(), until = selectRegion.getEnd();
                if (start < until) {
                    String pack = gunit.getSource().substring(start, until).replaceFirst("^import\\s+", "");
                    for (IPackageFragmentRoot root : gunit.getJavaProject().getPackageFragmentRoots()) {
                        IPackageFragment fragment = root.getPackageFragment(pack);
                        if (fragment != null && fragment.exists()) {
                            putRequestedElement(fragment);
                            break;
                        }
                    }
                }
            } else if (result.declaration instanceof Parameter) {
                Parameter parameter = (Parameter) result.declaration;
                int start = parameter.getNameStart(), until = parameter.getNameEnd();
                ClassNode type = result.type != null ? result.type : parameter.getType();
                String signature = GroovyUtils.getTypeSignature(type, /*fully-qualified:*/true, false);
                putRequestedElement(new LocalVariable((JavaElement) enclosingElement, parameter.getName(),
                    start, until, start, until, signature, /*annos:*/null, parameter.getModifiers(), true));
                //
            } else if (result.declaration instanceof VariableExpression) {
                VariableExpression variable = (VariableExpression) result.declaration;
                int start = variable.getStart(), until = variable.getEnd() - 1;
                ClassNode type = result.type != null ? result.type : variable.getType();
                String signature = GroovyUtils.getTypeSignature(type, /*fully-qualified:*/true, false);
                putRequestedElement(new LocalVariable((JavaElement) enclosingElement, variable.getName(),
                    start, until, start, until, signature, /*annos:*/null, variable.getModifiers(), false));
                //
            } else {
                String qualifier = checkQualifiedType(result, enclosingElement);
                ClassNode declaringType = findDeclaringType(result);
                if (declaringType != null) {
                    // find it in the java model
                    IType type = (IType) enclosingElement.getAncestor(IJavaElement.TYPE);
                    if (type == null || !type.getFullyQualifiedName().equals(declaringType.getName())) {
                        assert !GroovyUtils.isAnonymous(declaringType) : "Anonymous type here!";
                        type = JavaCoreUtil.findType(declaringType.getName(), enclosingElement);
                    }
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
                            putRequestedElement(resolveRequestedElement(maybeRequested, result));
                        } else {
                            // try to resolve as a type (outer class) then as a package
                            IType candidate = gunit.getJavaProject().findType(qualifier);
                            if (candidate != null) {
                                putRequestedElement(candidate);
                            } else {
                                IPackageFragmentRoot root;
                                if (type instanceof BinaryType) {
                                    root = (IPackageFragmentRoot) ((BinaryType) type).getPackageFragment().getParent();
                                } else {
                                    root = (IPackageFragmentRoot) ((SourceType) type).getPackageFragment().getParent();
                                }
                                putRequestedElement(root.getPackageFragment(qualifier));
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

        //

        if (nodeToLookFor instanceof MethodNode && ((MethodNode) nodeToLookFor).hasDefaultValue()) {
            final ASTNode ntlf = nodeToLookFor, rn = requestedNode;
            try {
                Stream<? extends MethodNode> nodes;
                if (nodeToLookFor instanceof ConstructorNode) {
                    nodes = result.declaringType.getDeclaredConstructors().stream().filter(cn -> cn != ntlf && cn.getOriginal() == ntlf);
                } else {
                    String methodName = ((MethodNode) nodeToLookFor).getName();
                    nodes = result.declaringType.getDeclaredMethods(methodName).stream().filter(mn -> mn != ntlf && mn.getOriginal() == ntlf);
                }

                // find java elements for each generated method variant
                for (TypeLookupResult tlr : nodes.map(mn -> {
                    TypeLookupResult tlr = new TypeLookupResult(result.type, result.declaringType, mn, result.confidence, result.scope);
                    tlr.enclosingAnnotation = result.enclosingAnnotation;
                    tlr.enclosingAssignment = result.enclosingAssignment;
                    tlr.isGroovy = result.isGroovy;
                    return tlr;
                }).toArray(TypeLookupResult[]::new)) {
                    nodeToLookFor = tlr.declaration;
                    handleMatch(tlr, enclosingElement);
                }
            } finally {
                nodeToLookFor = ntlf;
                requestedNode = rn;
            }
        }
    }

    private String checkQualifiedType(final TypeLookupResult result, final IJavaElement enclosingElement) throws JavaModelException {
        if (result.declaration instanceof ClassNode ||
            result.declaration instanceof ImportNode ||
            result.declaration instanceof ConstructorNode /*||
            result.declaration instanceof DeclarationExpression*/) {

            ClassNode type = result.type;
            if (result.declaration instanceof ImportNode)
                type = ((ImportNode) result.declaration).getType();
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
    private ClassNode findDeclaringType(final TypeLookupResult result) {
        ClassNode declaringType = null;
        if (result.declaringType != null) {
            declaringType = result.declaringType;
        } else if (result.declaration instanceof ClassNode) {
            declaringType = GroovyUtils.getBaseType((ClassNode) result.declaration);
        } else if (result.declaration instanceof FieldNode) {
            declaringType = ((FieldNode) result.declaration).getDeclaringClass();
        } else if (result.declaration instanceof MethodNode) {
            declaringType = ((MethodNode) result.declaration).getDeclaringClass();
        } else if (result.declaration instanceof PropertyNode) {
            declaringType = ((PropertyNode) result.declaration).getDeclaringClass();
        } else if (result.declaration instanceof ImportNode) {
            declaringType = ((ImportNode) result.declaration).getType();
        } else if (result.declaration instanceof DeclarationExpression) {
            declaringType = GroovyUtils.getBaseType(((DeclarationExpression) result.declaration).getLeftExpression().getType());
        }
        return declaringType;
    }

    private ImportNode findImportAlias(final String name, final IJavaElement enclosingElement) {
        IJavaElement elem = enclosingElement;
        while (!(elem instanceof GroovyCompilationUnit)) {
            elem = elem.getParent();
        }
        int dot = name.indexOf('.');
        return ((GroovyCompilationUnit) elem).getModuleNode().getImport(dot < 0 ? name : name.substring(0, dot));
    }

    private ITypeParameter findTypeParam(final String name, final IJavaElement enclosingElement) throws JavaModelException {
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

    private IJavaElement findRequestedElement(ASTNode declaration, final ClassNode declaringType, final IType jdtDeclaringType)
            throws JavaModelException {
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
                if ("<init>".equals(name)) {
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

    private boolean existsOnlyInGroovyModel(final FieldNode node, final String name, final ClassNode declaringType, final IType jdtDeclaringType)
            throws JavaModelException {
        // check for @Field field
        if (node.getEnd() > 0 && node.getDeclaringClass().isScript()) {
            return true;
        }
        // check for @Trait field
        if (!node.isFinal() && !node.isStatic() && Traits.isTrait(node.getDeclaringClass())) {
            return true;
        }
        List<FieldNode> traitFields = declaringType.redirect().getNodeMetaData("trait.fields");
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

    private boolean existsOnlyInGroovyModel(final MethodNode node, final String name, final ClassNode declaringType, final IType jdtDeclaringType)
            throws JavaModelException {
        // check for @Trait method
        List<MethodNode> traitMethods = declaringType.redirect().getNodeMetaData("trait.methods");
        if (traitMethods != null) {
            for (MethodNode traitMethod : traitMethods) {
                if (traitMethod == node) {
                    return true;
                }
            }
        }
        // check for @Newify, @Sortable, @Singleton, etc. -- synthetic method that exists on declaring class
        if (node.getEnd() < 1 && !(node instanceof JDTMethodNode) && !declaringType.getMethods(name).isEmpty() &&
                findElement(jdtDeclaringType, name, node.getParameters()) == null) {
            return true;
        }

        return GroovyUtils.isAnonymous(node.getDeclaringClass());
    }

    private boolean isSelectionInArguments(final ConstructorCallExpression expr) {
        int selectOffset = selectRegion.getOffset(), startOffset = Integer.MAX_VALUE;
        if (expr.getArguments().getStart() > 0) {
            startOffset = expr.getArguments().getStart();
        } else if (expr.getArguments() instanceof TupleExpression) {
            List<Expression> args = ((TupleExpression) expr.getArguments()).getExpressions();
            if (!args.isEmpty() && args.get(0).getStart() > 0) {
                startOffset = args.get(0).getStart();
            }
        }
        return (selectOffset >= startOffset);
    }

    /**
     * Converts the requested element into a resolved element by creating a unique key for it.
     */
    private IJavaElement resolveRequestedElement(final IJavaElement maybeRequested, final TypeLookupResult result) {
        AnnotatedNode declaration = (AnnotatedNode) result.declaration;
        if (declaration instanceof PropertyNode) {
            if (maybeRequested.getElementType() == IJavaElement.METHOD) {
                String methodName = maybeRequested.getElementName();
                List<MethodNode> methods = declaration.getDeclaringClass().getMethods(methodName);
                if (!methods.isEmpty()) declaration = methods.get(0);
            } else {
                declaration = ((PropertyNode) declaration).getField();
            }
        } else if (declaration instanceof ConstructorNode && maybeRequested.getElementType() == IJavaElement.TYPE) {
            // implicit default constructor; use type instead
            declaration = declaration.getDeclaringClass();
        }

        String uniqueKey = createUniqueKey(declaration, maybeRequested, result);

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
        return candidate;
    }

    //--------------------------------------------------------------------------

    /**
     * Creates binding keys for types, fields, methods and properties.
     */
    private static String createUniqueKey(final AnnotatedNode declaration, final IJavaElement maybeRequested, final TypeLookupResult result) {
        ClassNode resolvedDeclaringType = result.declaringType;
        if (resolvedDeclaringType == null) {
            resolvedDeclaringType = declaration.getDeclaringClass();
            if (resolvedDeclaringType == null) {
                resolvedDeclaringType = VariableScope.OBJECT_CLASS_NODE;
            }
        }
        if (resolvedDeclaringType.getGenericsTypes() != null) {
            boolean isStatic = false;
            if (declaration instanceof ClassNode) {
                isStatic = Flags.isStatic(
                    ((ClassNode) declaration).getModifiers());
            } else if (declaration instanceof FieldNode) {
                isStatic = ((FieldNode) declaration).isStatic();
            } else if (declaration instanceof MethodNode) {
                isStatic = ((MethodNode) declaration).isStatic();
            }
            if (isStatic) {
                resolvedDeclaringType = resolvedDeclaringType.getPlainNodeReference();
            }
        }

        StringBuilder sb = new StringBuilder();

        if (declaration instanceof ClassNode) {
            appendUniqueKeyForResolvedType(sb, result.type);
        } else {
            appendUniqueKeyForResolvedType(sb, resolvedDeclaringType);
            if (declaration instanceof FieldNode) {
                sb.append(Signature.C_DOT).append(maybeRequested.getElementName()).append(Signature.C_PARAM_END);
                appendUniqueKeyForResolvedType(sb, result.type);
            } else if (declaration instanceof MethodNode) {
                MethodNode node = (MethodNode) declaration;
                if (maybeRequested.getElementType() == IJavaElement.FIELD) {
                    // this is likely a generated getter or setter
                    sb.append(Signature.C_DOT).append(maybeRequested.getElementName()).append(Signature.C_PARAM_END);
                    boolean setter = node.getName().startsWith("set") && node.getParameters() != null && node.getParameters().length > 0;
                    appendUniqueKeyForResolvedType(sb, setter ? node.getParameters()[0].getType() : result.type);
                } else {
                    appendUniqueKeyForMethod(sb, node, result.type, resolvedDeclaringType);
                }
            }
        }

        return sb.toString();
    }

    private static void appendUniqueKeyForMethod(final StringBuilder sb, final MethodNode node, final ClassNode returnType, final ClassNode declaringType) {
        String methodName = node.getName();
        if ("<init>".equals(methodName))
            methodName = node.getDeclaringClass().getNameWithoutPackage();
        // BindingKeyParser fails if method name has 2 or more $s
        if (methodName.indexOf('$') != methodName.lastIndexOf('$')) {
            methodName = methodName.replace('$', '_');
        }
        sb.append(Signature.C_DOT).append(methodName);

        java.util.function.Function<ClassNode, String> signer = type ->
            GroovyUtils.getTypeSignature(type, true, true).replace('.', '/');

        // type parameters
        GenericsType[] generics = GroovyUtils.getGenericsTypes(node);
        if (generics.length > 0) {
            sb.append(Signature.C_GENERIC_START);
            // see org.eclipse.jdt.core.Signature#createTypeParameterSignature(char[],char[][])
            for (GenericsType gt : generics) {
                ClassNode lower = gt.getLowerBound();
                ClassNode[] upper = gt.getUpperBounds();

                sb.append(gt.getName());
                sb.append(Signature.C_COLON);
                sb.append(Signature.C_COLON);
                if (lower != null) {
                    sb.append(signer.apply(lower));
                } else if (upper != null && upper.length > 0) {
                    for (int i = 0; i < upper.length; i += 1) {
                        if (i > 0) sb.append(Signature.C_COLON);
                        sb.append(signer.apply(upper[i]));
                    }
                }
            }
            sb.append(Signature.C_GENERIC_END);
        }

        // call parameters
        sb.append(Signature.C_PARAM_START);
        Parameter[] parameters = node.getParameters();
        if (parameters != null) {
            for (Parameter p : parameters) {
                sb.append(signer.apply(p.getType()));
            }
        }
        sb.append(Signature.C_PARAM_END);

        // return type
        sb.append(signer.apply(returnType));

        // type parameter resolution
        if (generics.length > 0) {
            // generics have been resolved for returnType, declaringType and parameterTypes; mappings can be recovered using original method reference
            GenericsMapper mapper = GenericsMapper.gatherGenerics(GroovyUtils.getParameterTypes(node.getParameters()), declaringType, node.getOriginal());

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
                sb.append(Signature.C_INTERSECTION).append(signer.apply(exception));
            }
        }
    }

    private static void appendUniqueKeyForResolvedType(final StringBuilder sb, final ClassNode resolvedType) {
        String signature = GroovyUtils.getTypeSignatureWithoutGenerics(resolvedType, true, true).replace('.', '/');

        sb.append(signature);

        ClassNode baseType = GroovyUtils.getBaseType(resolvedType);
        if (!baseType.isGenericsPlaceHolder()) {
            GenericsType[] generics = baseType.getGenericsTypes();
            if (generics != null) {
                sb.setCharAt(sb.length() - 1, Signature.C_GENERIC_START);

                for (int i = 0, n = generics.length; i < n; i += 1) {
                    GenericsType gt = generics[i];
                    if (!gt.isWildcard()) {
                        appendUniqueKeyForResolvedType(sb, gt.getType());
                    } else {
                        // see org.eclipse.jdt.core.BindingKey#createWildcardTypeBindingKey(String,char,String,int)
                        sb.append(signature).append('{').append(i).append('}');

                        if (gt.getLowerBound() != null) {
                            sb.append(Signature.C_SUPER);
                            appendUniqueKeyForResolvedType(sb, gt.getLowerBound());
                        } else if (gt.getUpperBounds() != null && gt.getUpperBounds().length == 1) {
                            sb.append(Signature.C_EXTENDS);
                            appendUniqueKeyForResolvedType(sb, gt.getUpperBounds()[0]);
                        } else {
                            sb.append(Signature.C_STAR);
                        }
                    }
                }

                sb.append(Signature.C_GENERIC_END).append(Signature.C_NAME_END);
            }
        }
    }

    private static IJavaElement findElement(final IType type, String text, final Parameter[] parameters) throws JavaModelException {
        if (text.equals(type.getElementName())) {
            return type;
        }
        if ("<init>".equals(text)) {
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
                        String jdtMethodParam = Signature.getTypeErasure(method.getParameterTypes()[i]);
                        String astMethodParam = GroovyUtils.getTypeSignatureWithoutGenerics(parameters[i].getOriginType(), jdtMethodParam.indexOf('.') > 0, type.isBinary());
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
String message = String.format("CodeSelectRequestor.findElement: no exact match found for %s(%s); options considered:%s", text, parameters == null ? "" : GroovyUtils.getParameterTypes(parameters), checked);
if (GroovyLogManager.manager.hasLoggers()) {
    GroovyLogManager.manager.log(TraceCategory.CODE_SELECT, message);
} else {
    System.err.println(message);
}
            return closestMatch;
        }

        IField field = type.getField(text);
        String prefix;
        if (!field.exists() && (prefix = extractPrefix(text)) != null) {
            // try as a property
            String newName = decapitalize(text.substring(prefix.length()));
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

    private static String extractPrefix(final String text) {
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

    private int startOffset(final ClassNode type) {
        int start = type.getStart();
        if (type.getEnd() < 1) {
            if (nodeRegion != null) {
                start = nodeRegion.getOffset();
            } else {
                start = nodeToLookFor.getStart();
            }
        }
        return start;
    }

    private int endOffset(final ClassNode type) {
        int end = type.getEnd();
        if (end < 1) {
            if (nodeRegion != null) {
                end = nodeRegion.getEnd();
            } else {
                end = nodeToLookFor.getEnd();
            }
        }
        return end;
    }

    private Object typeOf(final ASTNode node) {
        if (node instanceof ClassNode) {
            return ((ClassNode) node).getNameWithoutPackage();
        }
        return node.getClass();
    }
}
