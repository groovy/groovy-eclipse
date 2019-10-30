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
package org.eclipse.jdt.groovy.search;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyTypeDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Position;

public class MethodReferenceSearchRequestor implements ITypeRequestor {

    protected final SearchRequestor requestor;
    protected final SearchParticipant participant;

    protected final String methodName;
    protected final String declaringTypeName;
    protected final String[] parameterTypeNames, parameterTypeSignatures;
    protected final boolean findReferences, findDeclarations, skipPseudoProperties;

    protected final Set<Position> acceptedPositions = new HashSet<>();
    protected static final int MAX_PARAMS = 20; // indexes available in each of:
    protected final Map<ClassNode, BitSet> cachedParameterCounts = new HashMap<>();
    protected final Map<ClassNode, Boolean> cachedDeclaringNameMatches = new HashMap<>();

    public MethodReferenceSearchRequestor(MethodPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
        this.requestor = requestor;
        this.participant = participant;

        this.methodName = String.valueOf(pattern.selector);
        parameterTypeSignatures = getParameterTypeSignatures(pattern);
        IType declaringType = ReflectionUtils.getPrivateField(MethodPattern.class, "declaringType", pattern);

        char[] declaringQualifiedName = null;

        try { // search super types for original declaration of the method -- TODO: Is there a service/utility to perform this search? MethodOverrideTester.findOverriddenMethod(IMethod, boolean)?
            if (pattern.focus instanceof IMethod && supportsOverride((IMethod) pattern.focus)) {
                LinkedList<IMethod> methods = new LinkedList<>();
                if (declaringType == null) declaringType = ((IMethod) pattern.focus).getDeclaringType();
                for (IType superType : declaringType.newSupertypeHierarchy(null).getAllSupertypes(declaringType)) {
                    IMethod superMeth = superType.getMethod(methodName, parameterTypeSignatures);
                    if (superMeth.exists() && supportsOverride(superMeth)) {
                        methods.add(superMeth);
                    }
                }
                if (!methods.isEmpty()) {
                    IType type = methods.getLast().getDeclaringType();
                    char[] superTypeName = type.getElementName().toCharArray();
                    char[] packageName = type.getPackageFragment().getElementName().toCharArray();
                    declaringQualifiedName = CharOperation.concat(packageName, superTypeName, '.');
                }
            }
        } catch (Exception e) {
            Util.log(e);
        }

        if (declaringQualifiedName == null) {
            declaringQualifiedName = CharOperation.concat(pattern.declaringQualification, pattern.declaringSimpleName, '.');
            if (declaringQualifiedName == null) {
                if (declaringType != null) {
                    declaringQualifiedName = CharOperation.concat(declaringType.getPackageFragment().getElementName().toCharArray(), declaringType.getElementName().toCharArray(), '.');
                } else {
                    declaringQualifiedName = CharOperation.NO_CHAR; // match the method signature in any type; checked within matchOnName(ClassNode)
                }
            }
        }
        declaringTypeName = String.valueOf(declaringQualifiedName);
        parameterTypeNames = getParameterTypeNames(pattern, parameterTypeSignatures, declaringType);

        findReferences = (Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findReferences", pattern);
        findDeclarations = (Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findDeclarations", pattern);
        skipPseudoProperties = requestor.getClass().getName().equals("org.eclipse.jdt.internal.corext.refactoring.rename.MethodOccurenceCollector");
    }

    protected static String[] getParameterTypeNames(MethodPattern pattern, String[] parameterTypeSignatures, IType declaringType) {
        int n = parameterTypeSignatures.length;
        String[] typeNames = new String[n];
        if (declaringType != null) // TODO: Should searches like "main(String[])", which have null declaring type, check param types?
        try {
            int candidates = 0;
            if (n > 0) { // check for method overloads
                for (IMethod m : declaringType.getMethods()) {
                    if (equal(pattern.selector, m.getElementName()) && n == m.getNumberOfParameters()) { // TODO: What about variadic methods?
                        candidates += 1;
                    }
                }
            }
            if (candidates > 1) {
                for (int i = 0; i < n; i += 1) {
                    if (pattern.parameterQualifications[i] != null || isPrimitiveType(pattern.parameterSimpleNames[i])) {
                        typeNames[i] = String.valueOf(CharOperation.concat(pattern.parameterQualifications[i], pattern.parameterSimpleNames[i], '.'));
                    } else {
                        int arrayCount = Signature.getArrayCount(parameterTypeSignatures[i]);
                        String[][] resolved = declaringType.resolveType(String.valueOf(pattern.parameterSimpleNames[i], 0, pattern.parameterSimpleNames[i].length - (2*arrayCount)));
                        if (resolved != null) {
                            typeNames[i] = Signature.toQualifiedName(resolved[0]);
                            if (typeNames[i].charAt(0) == '.') { // default pkg
                                typeNames[i] = typeNames[i].substring(1);
                            }
                        } else {
                            // something unresolvable like a parameterized type
                            typeNames[i] = String.valueOf(pattern.parameterSimpleNames[i]);
                        }
                        while (arrayCount-- > 0) {
                            typeNames[i] += "[]";
                        }
                    }
                }
            }
        } catch (Exception e) {
            Util.log(e);
        }
        return typeNames;
    }

    protected static String[] getParameterTypeSignatures(MethodPattern pattern) {
        char[][][] signatures = ReflectionUtils.getPrivateField(MethodPattern.class, "parametersTypeSignatures", pattern);
        int n = (signatures == null ? 0 : signatures.length);
        String[] parameterTypeSignatures = new String[n];
        for (int i = 0; i < n; i += 1) {
            parameterTypeSignatures[i] = String.valueOf(signatures[i][0]);
        }
        return parameterTypeSignatures;
    }

    protected static boolean isPrimitiveType(char[] name) {
        // adapted from ASTConverter
        switch (name[0]) {
        case 'i':
            return (name.length >= 3 && name[1] == 'n' && name[2] == 't' && (name.length == 3 || name[3] == '['));
        case 'l':
            return (name.length >= 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g' && (name.length == 4 || name[4] == '['));
        case 'c':
            return (name.length >= 4 && name[1] == 'h' && name[2] == 'a' && name[3] == 'r' && (name.length == 4 || name[4] == '['));
        case 's':
            return (name.length >= 5 && name[1] == 'h' && name[2] == 'o' && name[3] == 'r' && name[4] == 't' && (name.length == 5 || name[5] == '['));
        case 'f':
            return (name.length >= 5 && name[1] == 'l' && name[2] == 'o' && name[3] == 'a' && name[4] == 't' && (name.length == 5 || name[5] == '['));
        case 'd':
            return (name.length >= 6 && name[1] == 'o' && name[2] == 'u' && name[3] == 'b' && name[4] == 'l' && name[5] == 'e' && (name.length == 6 || name[6] == '['));
        case 'b':
            if (name.length >= 4 && name[1] == 'y' && name[2] == 't' && name[3] == 'e' && (name.length == 4 || name[4] == '[')) {
                return true;
            }
            if (name.length >= 7 && name[1] == 'o' && name[2] == 'o' && name[3] == 'l' && name[4] == 'e' && name[5] == 'a' && name[6] == 'n' && (name.length == 7 || name[7] == '[')) {
                return true;
            }
            break;
        case 'v':
            return (name.length == 4 && name[1] == 'o' && name[2] == 'i' && name[3] == 'd');
        }
        return false;
    }

    //--------------------------------------------------------------------------

    @Override
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (result.declaringType == null || !(result.declaration instanceof MethodNode)) {
            return VisitStatus.CONTINUE;
        }

        boolean isDeclaration = (node instanceof MethodNode);
        int start = 0;
        int end = 0;

        if (methodName.equals(((MethodNode) result.declaration).getName())) {
            if (isDeclaration || node instanceof StaticMethodCallExpression) {
                start = ((AnnotatedNode) node).getNameStart();
                end = ((AnnotatedNode) node).getNameEnd() + 1;

                // check for "foo.bar" where "bar" refers to "getBar()", "isBar()" or "setBar(...)"
                if (!isDeclaration && (end - start) < ((StaticMethodCallExpression) node).getMethod().length() && skipPseudoProperties) {
                    start = 0;
                    end = 0;
                } else if (end < 1 && isDeclaration && ((MethodNode) node).isStatic() && "main".equals(methodName)) {
                    end = 1;
                }

            // check for non-synthetic match; SyntheticAccessorSearchRequestor matches "foo.bar" to "getBar()", etc.
            } else if (methodName.equals(node.getText()) || (isNotSynthetic(node.getText(), result.declaringType, (MethodNode) result.declaration) && !skipPseudoProperties)) {
                start = node.getStart();
                end = node.getEnd();
            }

        // check for pseudo-properties on behalf of SyntheticAccessorsRenameParticipant
        } else if ((node instanceof ConstantExpression || node instanceof VariableExpression) && methodName.equals(node.getText())) {
            if (parameterTypesMatch((MethodNode) result.declaration)) {
                start = node.getStart();
                end = node.getEnd();
            }
        }

        if (end > 0) { // name matches, now check declaring type and parameter types
            // don't want to double accept nodes; this could happen if field and object initializers get pushed into multiple constructors
            Position position = new Position(start, end - start);
            if (!acceptedPositions.contains(position) && declaringTypeMatches(result.declaringType)) {

                boolean acceptable;
                if (isDeclaration || result.confidence == TypeConfidence.EXACT) {
                    acceptable = parameterTypesMatch((MethodNode) result.declaration);
                } else {
                    acceptable = argumentTypesMatch(result.scope.getMethodCallArgumentTypes(), result.declaringType);
                }
                if (acceptable) {
                    if (enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy) {
                        enclosingElement = ((GroovyClassFileWorkingCopy) enclosingElement.getOpenable()).convertToBinary(enclosingElement);
                    }

                    SearchMatch match = null;
                    if (isDeclaration && findDeclarations) {
                        match = new MethodDeclarationMatch(enclosingElement, getAccuracy(result.confidence), start, end - start, participant, enclosingElement.getResource());
                    } else if (!isDeclaration && findReferences) {
                        match = new MethodReferenceMatch(enclosingElement, getAccuracy(result.confidence), start, end - start, false, false, false, false, participant, enclosingElement.getResource());
                    }
                    if (match != null) {
                        try {
                            acceptedPositions.add(position);
                            requestor.acceptSearchMatch(match);
                        } catch (Exception e) {
                            Util.log(e, "Error reporting search match inside of " + enclosingElement + " in resource " + enclosingElement.getResource());
                        }
                    }
                }
            }
        }
        return VisitStatus.CONTINUE;
    }

    private boolean declaringTypeMatches(ClassNode declaringType) {
        if (declaringType == null) {
            return false;
        }
        String declaringTypeName = declaringType.getName().replace('$', '.');
        if ("java.lang.Object".equals(declaringTypeName) && declaringType.getDeclaredMethods(methodName).isEmpty()) {
            // local variables have a declaring type of Object; don't accidentally return them as a match
            return false;
        }
        if (this.declaringTypeName == null || this.declaringTypeName.isEmpty()) {
            // no type specified, accept all
            return true;
        }

        Boolean maybeMatch = cachedDeclaringNameMatches.get(declaringType);
        if (maybeMatch != null) {
            return maybeMatch;
        }

        if (declaringTypeName.equals(this.declaringTypeName)) {
            cachedDeclaringNameMatches.put(declaringType, Boolean.TRUE);
            return true;
        } else { // check the supers
            maybeMatch = declaringTypeMatches(declaringType.getSuperClass());
            if (!maybeMatch) {
                for (ClassNode face : declaringType.getInterfaces()) {
                    maybeMatch = declaringTypeMatches(face);
                    if (maybeMatch) {
                        break;
                    }
                }
            }
            cachedDeclaringNameMatches.put(declaringType, maybeMatch);
            return maybeMatch;
        }
    }

    private boolean parameterTypesMatch(MethodNode methodNode) {
        Parameter[] parameters = methodNode.getOriginal().getParameters();
        List<ClassNode> parameterTypes = GroovyUtils.getParameterTypes(parameters);
        int n; if ((n = parameterTypes.size()) != parameterTypeSignatures.length) {
            return false;
        }
        for (int i = 0; i < n; i += 1) {
            ClassNode parameterType = parameterTypes.get(i);
            String parameterTypeSignature = Signature.getTypeErasure(parameterTypeSignatures[i]);

            if (GroovyUtils.getTypeSignatureWithoutGenerics(parameterType, false, false).equals(parameterTypeSignature)) {
                continue;
            }
            if (parameterType.isPrimitive() || !GroovyUtils.getTypeSignatureWithoutGenerics(parameterType, true, false).equals(parameterTypeSignature)) {
                return false;
            }
        }
        return true;
    }

    /**
     * When matching method references and declarations, match on the number of
     * parameterrs and assume that it is slightly more accurate than matching on
     * name alone.  If parameters and arguments are equal length, check the type
     * compatibility of each pair.
     *
     * The heuristic that is used in this method is this:
     * <ol>
     * <li>The search pattern expects 'n' parameters
     * <li>the current node has 'm' arguments.
     * <li>if the m == n, then there is a precise match.
     * <li>if not, look at all methods in current type with same name.
     * <li>if there is a method in the current type with the same number of
     *  arguments, then assume the current node matches that other method, and
     *  there is no match.
     * <li>If there are no existing methods with same number of parameters, then
     *  assume that current method call is an alternative way of calling the method
     *  and return a match
     * </ol>
     */
    private boolean argumentTypesMatch(List<ClassNode> argumentTypes, ClassNode declaringType) {
        if (argumentTypes == null) {
            return true;
        }

        if (argumentTypes.size() == parameterTypeNames.length) {
            for (int i = 0; i < parameterTypeNames.length; i += 1) {
                if (parameterTypeNames[i] == null) continue; // skip check
                ClassNode source = argumentTypes.get(i), target = ConstructorReferenceSearchRequestor.makeType(parameterTypeNames[i]);
                if (Boolean.FALSE.equals(SimpleTypeLookup.isTypeCompatible(source, target))) {
                    return false;
                }
            }
            return true;
        }

        BitSet foundParameterCounts = cachedParameterCounts.computeIfAbsent(declaringType, t -> {
            BitSet parameterCounts = new BitSet(MAX_PARAMS + 1);
            gatherParameters(declaringType, parameterCounts);
            return parameterCounts;
        });
        // now, if we find a method that has the same number of parameters in the call,
        // then assume the call is for this target method (and therefore there is no match)
        return !foundParameterCounts.get(Math.min(MAX_PARAMS, argumentTypes.size()));
    }

    private void gatherParameters(ClassNode declaringType, BitSet foundParameterCounts) {
        if (declaringType == null) {
            return;
        }
        declaringType = findWrappedNode(declaringType.redirect());
        List<MethodNode> methods = declaringType.getMethods(methodName);
        for (MethodNode method : methods) {
            Parameter[] parameters = method.getParameters();
            foundParameterCounts.set(Math.min(parameters.length, MAX_PARAMS));
        }
        gatherParameters(declaringType.getSuperClass(), foundParameterCounts);
        for (ClassNode face : declaringType.getInterfaces()) {
            gatherParameters(face, foundParameterCounts);
        }
    }

    /**
     * Converts from a {@link JDTClassNode} to a {@link ClassNode} in order to
     * check default parameters.
     */
    private ClassNode findWrappedNode(ClassNode declaringType) {
        ClassNode wrappedNode = null;
        if (declaringType instanceof JDTClassNode) {
            ReferenceBinding binding = ((JDTClassNode) declaringType).getJdtBinding();
            if (binding instanceof SourceTypeBinding) {
                SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) binding;
                if (sourceTypeBinding.scope != null) {
                    TypeDeclaration typeDeclaration = sourceTypeBinding.scope.referenceContext;
                    if (typeDeclaration instanceof GroovyTypeDeclaration) {
                        GroovyTypeDeclaration groovyTypeDeclaration = (GroovyTypeDeclaration) typeDeclaration;
                        wrappedNode = groovyTypeDeclaration.getClassNode();
                    }
                }
            }
        }
        return (wrappedNode == null ? declaringType : wrappedNode);
    }

    private int getAccuracy(TypeConfidence confidence) {
        //                                        improves call hierarchy and prevents "possible matches" warnings in refactoring wizard
        if (confidence == TypeConfidence.EXACT || ACCURATE_REQUESTOR.matcher(requestor.getClass().getName()).find()) {
            return SearchMatch.A_ACCURATE;
        }
        return SearchMatch.A_INACCURATE;
    }

    private static final Pattern ACCURATE_REQUESTOR = Pattern.compile("\\.(?:callhierarchy|refactoring)\\.");

    private static boolean supportsOverride(IMethod method) throws JavaModelException {
        return !(Flags.isPrivate(method.getFlags()) || Flags.isStatic(method.getFlags()));
    }

    private static boolean isNotSynthetic(String name, ClassNode type, MethodNode method) {
        if (!method.isSynthetic()) {
            if (type.getField(name) != null) {
                return true;
            }
            PropertyNode prop = type.getProperty(name);
            if (prop != null && !prop.isSynthetic()) {
                return true;
            }
        }
        return false;
    }

    private static boolean equal(char[] arr, CharSequence seq) {
        if (arr.length != seq.length()) {
            return false;
        }
        for (int i = 0, n = arr.length; i < n; i += 1) {
            if (arr[i] != seq.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
