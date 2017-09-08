/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.groovy.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyTypeDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.core.runtime.CoreException;
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

    protected final String name;
    protected char[][] parameterSimpleNames;
    protected char[][] parameterQualifications;
    protected final int declaredParameterCount;
    protected final String declaringQualifiedName;

    protected final boolean findDeclarations;
    protected final boolean findReferences;
    protected static final int MAX_PARAMS = 10;
    protected final Set<Position> acceptedPositions = new HashSet<Position>();
    protected final Map<ClassNode, boolean[]> cachedParameterCounts = new HashMap<ClassNode, boolean[]>();
    protected final Map<ClassNode, Boolean> cachedDeclaringNameMatches = new HashMap<ClassNode, Boolean>();

    public MethodReferenceSearchRequestor(MethodPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
        this.requestor = requestor;
        this.participant = participant;

        name = String.valueOf(pattern.selector);
        parameterSimpleNames = pattern.parameterSimpleNames;
        parameterQualifications = pattern.parameterQualifications;
        declaredParameterCount = parameterSimpleNames == null ? 0 : parameterSimpleNames.length;

        IType declaringType = (IType) ReflectionUtils.getPrivateField(MethodPattern.class, "declaringType", pattern);

        char[] declaringQualifiedName = null;

        try { // search super types for original declaration of the method -- TODO: Is there a service/utility to perform this search? MethodOverrideTester.findOverriddenMethod(IMethod, boolean)?
            if (pattern.focus instanceof IMethod && supportsOverride((IMethod) pattern.focus)) {
                LinkedList<IMethod> methods = new LinkedList<IMethod>();
                if (declaringType == null) declaringType = ((IMethod) pattern.focus).getDeclaringType();
                for (IType superType : declaringType.newSupertypeHierarchy(null).getAllSupertypes(declaringType)) {
                    next: for (IMethod superMeth : superType.getMethods()) {
                        if (supportsOverride(superMeth) && superMeth.getElementName().equals(name)) {
                            String[] paramTypes = superMeth.getParameterTypes();
                            if (paramTypes.length == declaredParameterCount) {
                                for (int i = 0; i < declaredParameterCount; i += 1) {
                                    if (!equal(parameterSimpleNames[i], Signature.getSimpleName(Signature.toString(paramTypes[i])))) {
                                        continue next;
                                    }
                                }
                                methods.add(superMeth);
                            }
                        }
                    }
                }
                if (!methods.isEmpty()) {
                    IType decl = methods.getLast().getDeclaringType();
                    char[] superTypeName = decl.getElementName().toCharArray();
                    char[] packageName = decl.getPackageFragment().getElementName().toCharArray();
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
        this.declaringQualifiedName = String.valueOf(declaringQualifiedName);

        findDeclarations = ((Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findDeclarations", pattern)).booleanValue();
        findReferences = ((Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findReferences", pattern)).booleanValue();
    }

    //--------------------------------------------------------------------------

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (result.declaringType == null) {
            // GRECLIPSE-1180: probably a literal of some kind
            return VisitStatus.CONTINUE;
        }

        boolean doCheck = false;
        boolean isDeclaration = false;
        boolean isConstructorCall = false; // FIXADE hmmm...not capturing constructor calls here.
        int start = 0;
        int end = 0;

        if (node instanceof ConstantExpression) {
            String cName = ((ConstantExpression) node).getText();
            if (name.equals(cName)) {
                start = node.getStart();
                end = node.getEnd();
                doCheck = end > 0; // avoid synthetic references
            }
        } else if (node instanceof FieldExpression) {
            if (name.equals(((FieldExpression) node).getFieldName())) {
                start = node.getStart();
                end = node.getEnd();
                doCheck = end > 0; // avoid synthetic references
            }
        } else if (node instanceof MethodNode) {
            MethodNode mnode = (MethodNode) node;
            if (name.equals(mnode.getName())) {
                isDeclaration = true;
                start = mnode.getNameStart();
                end = mnode.getNameEnd() + 1; // arrrgh...why +1?
                doCheck = true;
            }
        } else if (node instanceof VariableExpression) {
            VariableExpression vnode = (VariableExpression) node;
            if (name.equals(vnode.getName())) {
                start = vnode.getStart();
                end = start + vnode.getName().length();
                doCheck = true;
            }
        } else if (node instanceof StaticMethodCallExpression) {
            StaticMethodCallExpression smnode = (StaticMethodCallExpression) node;
            if (name.equals(smnode.getMethod())) {
                start = smnode.getStart();
                end = start + name.length();
                doCheck = true;
            }
        }

        // at this point, if doCheck is true, then we know that the method name matches
        if (doCheck && end > 0) {
            // don't want to double accept nodes. This could happen with field and object initializers can get pushed into multiple constructors
            Position position = new Position(start, end - start);
            if (!acceptedPositions.contains(position)) {
                int numberOfParameters = findNumberOfParameters(node, result);
                boolean isCompleteMatch = nameAndArgsMatch(GroovyUtils.getBaseType(result.declaringType), numberOfParameters);
                if (isCompleteMatch) {
                    IJavaElement realElement = enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy ? ((GroovyClassFileWorkingCopy) enclosingElement.getOpenable()).convertToBinary(enclosingElement) : enclosingElement;
                    SearchMatch match = null;
                    if (isDeclaration && findDeclarations) {
                        match = new MethodDeclarationMatch(realElement, getAccuracy(result.confidence, isCompleteMatch), start, end - start, participant, realElement.getResource());
                    } else if (!isDeclaration && findReferences) {
                        match = new MethodReferenceMatch(realElement, getAccuracy(result.confidence, isCompleteMatch), start, end - start, isConstructorCall, false, false, false, participant, realElement.getResource());
                    }
                    if (match != null) {
                        try {
                            requestor.acceptSearchMatch(match);
                            acceptedPositions.add(position);
                        } catch (CoreException e) {
                            Util.log(e, "Error reporting search match inside of " + realElement + " in resource " + realElement.getResource());
                        }
                    }
                }
            }
        }
        return VisitStatus.CONTINUE;
    }

    /**
     * @return finds the number of parameters in the method reference/declaration currently being analyzed.
     */
    private int findNumberOfParameters(ASTNode node, TypeLookupResult result) {
        return (node instanceof MethodNode && ((MethodNode) node).getParameters() != null)
            ? ((MethodNode) node).getParameters().length : Math.max(0, result.scope.getMethodCallNumberOfArguments());
    }

    /**
     * Recursively checks the hierarchy for matching names
     */
    private boolean nameAndArgsMatch(ClassNode declaringType, int currentCallCount) {
        return matchOnName(declaringType) && matchOnNumberOfParameters(declaringType, currentCallCount);
    }

    private boolean matchOnName(ClassNode declaringType) {
        if (declaringType == null) {
            return false;
        }
        String declaringTypeName = declaringType.getName();
        if (declaringTypeName.equals("java.lang.Object") && declaringType.getDeclaredMethods(name).isEmpty()) {
            // local variables have a declaring type of Object; don't accidentally return them as a match
            return false;
        }
        if (declaringQualifiedName == null || declaringQualifiedName.equals("")) {
            // no type specified, accept all
            return true;
        }

        declaringTypeName = declaringTypeName.replace('$', '.');

        Boolean maybeMatch = cachedDeclaringNameMatches.get(declaringType);
        if (maybeMatch != null) {
            return maybeMatch;
        }

        if (declaringTypeName.equals(declaringQualifiedName)) {
            cachedDeclaringNameMatches.put(declaringType, true);
            return true;
        } else { // check the supers
            maybeMatch = matchOnName(declaringType.getSuperClass());
            if (!maybeMatch) {
                for (ClassNode iface : declaringType.getInterfaces()) {
                    maybeMatch = matchOnName(iface);
                    if (maybeMatch) {
                        break;
                    }
                }
            }
            cachedDeclaringNameMatches.put(declaringType, maybeMatch);
            return maybeMatch;
        }
    }

    /**
     * When matching method references and declarations, we can't actually match
     * on parameter types. Instead, we match on the number of parameterrs and
     * assume that it is slightly more preceise than just matching on name.
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
     *
     * @return true if there is a precise match between number of arguments and
     *  numner of parameters. false if there exists a different method with same
     *  number of arguments in current type, or true otherwise
     */
    private boolean matchOnNumberOfParameters(ClassNode declaringType, int currentCallCount) {
        boolean methodParamNumberMatch;
        if (currentCallCount == declaredParameterCount) {
            // precise match
            methodParamNumberMatch = true;
        } else {
            boolean[] foundParameterNumbers = cachedParameterCounts.get(declaringType);
            if (foundParameterNumbers == null) {
                foundParameterNumbers = new boolean[MAX_PARAMS + 1];
                gatherParameters(declaringType, foundParameterNumbers);
                cachedParameterCounts.put(declaringType, foundParameterNumbers);
            }
            // now, if we find a method that has the same number of parameters in the call,
            // then assume the call is for this target method (and therefore there is no match)
            methodParamNumberMatch = !foundParameterNumbers[Math.min(MAX_PARAMS, currentCallCount)];
        }
        return methodParamNumberMatch;
    }

    private void gatherParameters(ClassNode declaringType, boolean[] foundParameterNumbers) {
        if (declaringType == null) {
            return;
        }
        declaringType = findWrappedNode(declaringType.redirect());
        List<MethodNode> methods = declaringType.getMethods(name);
        for (MethodNode method : methods) {
            // GRECLIPSE-1233: ensure default parameters are ignored
            method = method.getOriginal();
            foundParameterNumbers[Math.min(method.getParameters().length, MAX_PARAMS)] = true;
        }
        gatherParameters(declaringType.getSuperClass(), foundParameterNumbers);
        for (ClassNode iface : declaringType.getInterfaces()) {
            gatherParameters(iface, foundParameterNumbers);
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
        return wrappedNode == null ? declaringType : wrappedNode;
    }

    private int getAccuracy(TypeConfidence confidence, boolean isCompleteMatch) {
        if (shouldAlwaysBeAccurate()) {
            return SearchMatch.A_ACCURATE;
        }
        if (!isCompleteMatch) {
            return SearchMatch.A_INACCURATE;
        }
        switch (confidence) {
        case EXACT:
            return SearchMatch.A_ACCURATE;
        default:
            return SearchMatch.A_INACCURATE;
        }
    }

    /**
     * Checks to see if this requestor has something to do with refactoring, if
     * so, we always want an accurate match otherwise we get complaints in the
     * refactoring wizard of "possible matches"
     */
    private boolean shouldAlwaysBeAccurate() {
        return requestor.getClass().getPackage().getName().indexOf("refactoring") != -1;
    }

    private static boolean supportsOverride(IMethod method)
        throws JavaModelException {
        int flags = method.getFlags();
        return !(Flags.isPrivate(flags) || Flags.isStatic(flags));
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
