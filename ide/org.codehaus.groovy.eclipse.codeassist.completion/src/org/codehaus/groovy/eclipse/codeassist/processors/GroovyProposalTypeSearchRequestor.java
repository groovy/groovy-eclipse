/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.processors;

import java.beans.Introspector;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaMethodCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyNamedArgumentProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.codehaus.groovy.eclipse.codeassist.relevance.IRelevanceRule;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.relevance.internal.CompositeRule;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitScope;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.CharArraySequence;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.corext.util.TypeFilter;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * This type requestor searches for groovy type content assist proposals in the
 * current scope.  This class is largely copied from {@link CompletionEngine}.
 * Method names used here are the same as the method names used in the original
 * code Method parts are omitted or commented out when they are not relevant for
 * or not supported by groovy completion.
 */
public class GroovyProposalTypeSearchRequestor implements ISearchRequestor {

    private static final char[] NO_TYPE_NAME = {'.'};
    private static final char[] _AS_ = {' ', 'a', 's', ' '};

    private static final int CHECK_CANCEL_FREQUENCY = 50;
    private static final Pattern CLOSURE_INNER_TYPE = Pattern.compile("_closure\\d+$");

    private int foundTypesCount;
    private int foundConstructorsCount;

    private ObjectVector acceptedTypes;
    private Set<String> acceptedPackages;
    private boolean shouldAcceptConstructors;
    private ObjectVector acceptedConstructors;

    /** Array of simple name, fully-qualified name pairs. Default imports should be included (aka BigDecimal, etc.). */
    private char[][][] imports;
    /** Array of fully-qualified names. Default imports should be included (aka java.lang, groovy.lang, etc.). */
    private char[][] onDemandImports;

    private final int offset;
    private final boolean isImport;
    private final NameLookup nameLookup;
    private final IProgressMonitor monitor;
    private final String completionExpression;

    private final ModuleNode module;
    private final GroovyCompilationUnit unit;
    private GroovyImportRewriteFactory groovyRewriter;
    private ProposalFormattingOptions groovyProposalPrefs;
    private final JavaContentAssistInvocationContext javaContext;

    private IRelevanceRule relevanceRule;

    // use this completion engine only to create parameter names for Constructors
    private CompletionEngine mockEngine;

    // all the types in the target Compilation unit
    private IType[] allTypesInUnit;

    // instead of inserting text, show context information only for constructors
    private boolean contextOnly;

    final ContentAssistContext context;
    final        AssistOptions options;

    /**
     * @param replaceLength unused but preserved for API compatibility
     */
    public GroovyProposalTypeSearchRequestor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext,
            int exprStart, int replaceLength, NameLookup nameLookup, IProgressMonitor monitor) {

        this.context = context;
        this.javaContext = javaContext;
        Assert.isNotNull(javaContext.getCoreContext());
        this.offset = exprStart;
        this.nameLookup = nameLookup;
        this.monitor = monitor;
        this.unit = context.unit;
        this.module = context.unit.getModuleNode();
        this.isImport = (context.location == ContentAssistLocation.IMPORT);
        // if contextOnly then do not insert any text, only show context information
        this.contextOnly = (context.location == ContentAssistLocation.METHOD_CONTEXT);
        this.shouldAcceptConstructors = (context.location == ContentAssistLocation.METHOD_CONTEXT || context.location == ContentAssistLocation.CONSTRUCTOR);
        this.completionExpression = (contextOnly ? ((MethodInfoContentAssistContext) context).methodName.replace('$', '.') : context.getQualifiedCompletionExpression());

        this.groovyRewriter = new GroovyImportRewriteFactory(this.unit, this.module);
        this.options = new AssistOptions(javaContext.getProject().getOptions(true));

        try {
            this.allTypesInUnit = unit.getAllTypes();
        } catch (JavaModelException e) {
            GroovyContentAssist.logError(e);
            this.allTypesInUnit = new IType[0];
        }
    }

    @Override
    public void acceptModule(char[] moduleName) {
    }

    @Override
    public void acceptPackage(char[] packageName) {
        checkCancel();

        if (TypeFilter.isFiltered(packageName, NO_TYPE_NAME)) {
            return;
        }

        if (acceptedPackages == null)
            acceptedPackages = new HashSet<>();
        acceptedPackages.add(String.valueOf(packageName));
    }

    @Override
    public void acceptType(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, int modifiers, AccessRestriction accessRestriction) {
        // do not check cancellation for every type to avoid performance loss
        if ((foundTypesCount % CHECK_CANCEL_FREQUENCY) == 0)
            checkCancel();
        foundTypesCount += 1;

        // do not propose synthetic types
        if (CharOperation.contains('$', simpleTypeName) || (DefaultGroovyMethods.asBoolean(enclosingTypeNames) &&
                CLOSURE_INNER_TYPE.matcher(new CharArraySequence(simpleTypeName)).find())) {
            return;
        }

        if (context.location == ContentAssistLocation.EXTENDS && (modifiers & Flags.AccFinal) != 0) {
            return;
        }
        if (options.checkDeprecation && (modifiers & Flags.AccDeprecated) != 0) {
            return;
        }
        if (options.checkVisibility) {
            if ((modifiers & Flags.AccPublic) == 0) {
                if ((modifiers & Flags.AccPrivate) != 0)
                    return;

                if (!CharOperation.equals(packageName, CharOperation.concatWith(unit.getPackageName(), '.')))
                    return;
            }
        }

        if (TypeFilter.isFiltered(packageName, CharOperation.concatWith(enclosingTypeNames, simpleTypeName, '.'))) {
            return;
        }

        int accessibility = IAccessRule.K_ACCESSIBLE;
        if (accessRestriction != null) {
            switch (accessRestriction.getProblemId()) {
            case IProblem.DiscouragedReference:
                if (options.checkDiscouragedReference) {
                    return;
                }
                accessibility = IAccessRule.K_DISCOURAGED;
                break;
            case IProblem.ForbiddenReference:
                if (options.checkForbiddenReference) {
                    return;
                }
                accessibility = IAccessRule.K_NON_ACCESSIBLE;
                break;
            }
        }

        if (acceptedTypes == null)
            acceptedTypes = new ObjectVector();
        acceptedTypes.add(new AcceptedType(packageName, simpleTypeName, enclosingTypeNames, modifiers, accessibility));
    }

    @Override
    public void acceptConstructor(int modifiers, char[] simpleTypeName, int parameterCount, char[] signature, char[][] parameterTypes,
        char[][] parameterNames, int typeModifiers, char[] packageName, int extraFlags, String path, AccessRestriction accessRestriction) {

        if (shouldAcceptConstructors) {
            // do not check cancellation for every ctor to avoid performance loss
            if ((foundConstructorsCount % (CHECK_CANCEL_FREQUENCY)) == 0)
                checkCancel();
            foundConstructorsCount += 1;

            if (Flags.isEnum(typeModifiers) || Flags.isInterface(typeModifiers) || Flags.isAnnotation(typeModifiers)) {
                return;
            }
            if (this.options.checkDeprecation && (typeModifiers & Flags.AccDeprecated) != 0) {
                return;
            }
            if (TypeFilter.isFiltered(packageName, simpleTypeName)) {
                return;
            }

            if (this.options.checkVisibility) {
                if ((typeModifiers & Flags.AccPublic) == 0) {
                    if ((typeModifiers & Flags.AccPrivate) != 0)
                        return;

                    if (!CharOperation.equals(packageName, CharOperation.concatWith(unit.getPackageName(), '.')))
                        return;
                }
            }

            int accessibility = IAccessRule.K_ACCESSIBLE;
            if (accessRestriction != null) {
                switch (accessRestriction.getProblemId()) {
                case IProblem.DiscouragedReference:
                    if (options.checkDiscouragedReference) {
                        return;
                    }
                    accessibility = IAccessRule.K_DISCOURAGED;
                    break;
                case IProblem.ForbiddenReference:
                    if (options.checkForbiddenReference) {
                        return;
                    }
                    accessibility = IAccessRule.K_NON_ACCESSIBLE;
                    break;
                }
            }

            String typeName; // TODO: Figure out why inner types are found from Groovy but not Java
            if (!CharOperation.contains('$', simpleTypeName) &&
                    (path.lastIndexOf(typeName = String.valueOf(simpleTypeName)) < 0 ||
                    !path.matches(".+\\b" + Pattern.quote(typeName) + "(?:\\.\\w+)?"))) {
                try {
                    IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
                    if (r != null) {
                        IJavaElement el = r.getAdapter(IJavaElement.class);
                        if (el != null && el.getElementType() == IJavaElement.COMPILATION_UNIT) {
                            ICompilationUnit cu = (ICompilationUnit) el;
                            for (IType type : cu.getAllTypes()) {
                                if (type.getElementName().equals(typeName)) {
                                    if (type.isMember()) {
                                        simpleTypeName = type.getTypeQualifiedName().toCharArray();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    GroovyContentAssist.logError(e);
                }
            }

            if (acceptedConstructors == null)
                acceptedConstructors = new ObjectVector();
            acceptedConstructors.add(new AcceptedCtor(modifiers, simpleTypeName, parameterCount, signature, parameterTypes, parameterNames, typeModifiers, packageName, extraFlags, accessibility));
        }
    }

    private void checkCancel() {
        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    //--------------------------------------------------------------------------

    List<ICompletionProposal> processAcceptedPackages() {
        checkCancel();
        List<ICompletionProposal> proposals = new LinkedList<>();
        if (acceptedPackages != null && !acceptedPackages.isEmpty()) {
            for (String packageNameStr : acceptedPackages) {
                char[] packageName = packageNameStr.toCharArray();
                GroovyCompletionProposal proposal = createProposal(CompletionProposal.PACKAGE_REF, context.completionLocation);
                proposal.setDeclarationSignature(packageName);
                proposal.setPackageName(packageName);
                proposal.setCompletion(packageName);
                proposal.setReplaceRange(offset, context.completionLocation);
                proposal.setTokenRange(offset, context.completionLocation);
                proposal.setRelevance(Relevance.LOWEST.getRelevance());

                LazyJavaCompletionProposal javaProposal = new LazyJavaCompletionProposal(proposal, javaContext);
                javaProposal.setTriggerCharacters(ProposalUtils.TYPE_TRIGGERS);
                javaProposal.setRelevance(proposal.getRelevance());
                proposals.add(javaProposal);
            }
        }
        return proposals;
    }

    /**
     * Called after all types have been accepted by this requestor.  Converts each type into an {@link ICompletionProposal}.
     *
     * @return list of all {@link ICompletionProposal}s applicable for this content assist request
     */
    List<ICompletionProposal> processAcceptedTypes(JDTResolver resolver) {
        checkCancel();

        int n;
        if (acceptedTypes == null || (n = acceptedTypes.size()) == 0) {
            return Collections.emptyList();
        }

        initializeRelevanceRule(resolver);

        List<ICompletionProposal> proposals = new LinkedList<>();
        boolean qualified = (completionExpression.indexOf('.') > 0);
        try {
            HashtableOfObject onDemandFound = new HashtableOfObject();

            next: for (int i = 0; i < n; i += 1) {
                // does not check cancellation for every type to avoid performance loss
                if ((i % CHECK_CANCEL_FREQUENCY) == 0) {
                    checkCancel();
                }

                AcceptedType type = (AcceptedType) acceptedTypes.elementAt(i);
                char[] packageName = type.packageName;
                char[] simpleTypeName = type.simpleTypeName;
                char[][] enclosingTypeNames = type.enclosingTypeNames;

                if (enclosingTypeNames == null || enclosingTypeNames.length == 0) {
                    type.qualifiedTypeName = simpleTypeName;
                } else {
                    type.qualifiedTypeName = CharOperation.concatWith(type.enclosingTypeNames, simpleTypeName, '$');
                }
                type.fullyQualifiedName = CharOperation.concat(packageName, type.qualifiedTypeName, '.');

                if (isImport) {
                    proposals.add(proposeType(type));
                    continue next;
                }

                if (imports == null && resolver.getScope() != null) {
                    initializeImportArrays(resolver.getScope());
                }

                if (imports != null && !qualified) {
                    char[] fullName = CharOperation.replaceOnCopy(type.fullyQualifiedName, '$', '.');
                    for (char[][] importSpec : imports) {
                        // check to see if this type name is imported explicitly
                        if (CharOperation.equals(simpleTypeName, importSpec[0])) {
                            int end = CharOperation.indexOf(_AS_, importSpec[1], true);
                            // use qualified name if there is already something with the same simple name imported
                            type.mustBeQualified = !CharOperation.equals(fullName, importSpec[1], 0, end > 0 ? end : importSpec[1].length);
                            proposals.add(proposeType(type));
                            continue next;
                        }
                    }
                }

                // check for star import if expression is not qualified and type name not seen already
                if (!qualified && onDemandImports != null && !onDemandFound.containsKey(simpleTypeName)) {
                    char[] qualifier = packageName;
                    if (enclosingTypeNames != null && enclosingTypeNames.length > 0) {
                        qualifier = CharOperation.concatWith(packageName, type.enclosingTypeNames, '.');
                    }

                    for (char[] importName : onDemandImports) {
                        if (CharOperation.equals(qualifier, importName)) {
                            onDemandFound.put(simpleTypeName, type);
                            continue next;
                        }
                    }

                    type.mustBeQualified = true;
                }
                proposals.add(proposeType(type));
            }

            char[][] keys = onDemandFound.keyTable;
            Object[] vals = onDemandFound.valueTable;
            for (int i = 0; i < keys.length; i += 1) {
                if ((i % CHECK_CANCEL_FREQUENCY) == 0)
                    checkCancel();
                if (keys[i] != null) {
                    AcceptedType value = (AcceptedType) vals[i];
                    if (value != null) {
                        proposals.add(proposeType(value));
                    }
                }
            }
        } finally {
            acceptedTypes = null;
            relevanceRule = null;
        }
        return proposals;
    }

    private ICompletionProposal proposeType(AcceptedType type) {
        int completionOffset = (isImport || type.mustBeQualified ? offset : context.completionLocation - context.getPerceivedCompletionExpression().length());

        GroovyCompletionProposal proposal = createProposal(CompletionProposal.TYPE_REF, completionOffset);
        proposal.setAccessibility(type.accessibility);
        proposal.setCompletion(!type.mustBeQualified ? type.simpleTypeName : CharOperation.replaceOnCopy(type.fullyQualifiedName, '$', '.'));
        proposal.setDeclarationSignature(type.packageName);
        proposal.setFlags(type.modifiers);
        proposal.setPackageName(type.packageName);
        proposal.setRelevance(computeRelevanceForTypeProposal(type.fullyQualifiedName, type.accessibility, type.modifiers));
        proposal.setReplaceRange(completionOffset, context.completionLocation);
        proposal.setSignature(Signature.createCharArrayTypeSignature(type.fullyQualifiedName, true));
        proposal.setTokenRange(completionOffset, context.completionEnd);
        proposal.setTypeName(type.qualifiedTypeName);

        if (type.qualifiedTypeName.length != type.simpleTypeName.length) {
            char[] outerTypeName = CharOperation.subarray(type.qualifiedTypeName, 0, type.qualifiedTypeName.length - type.simpleTypeName.length - 1);
            proposal.setDeclarationSignature(Signature.createCharArrayTypeSignature(CharOperation.concat(type.packageName, outerTypeName, '.'), true));
            proposal.setDeclarationPackageName(type.packageName);
            proposal.setDeclarationTypeName(outerTypeName);
        }

        AbstractJavaCompletionProposal javaProposal;
        if (isImport) {
            String fullyQualifiedName = String.valueOf(type.fullyQualifiedName).replace('$', '.'); // as it would appear in an import statement
            javaProposal = new JavaTypeCompletionProposal(fullyQualifiedName, null, completionOffset, context.completionLocation - completionOffset,
                ProposalUtils.getImage(proposal), ProposalUtils.createDisplayString(proposal), proposal.getRelevance(), fullyQualifiedName, javaContext);
        } else {
            javaProposal = new LazyJavaTypeCompletionProposal(proposal, javaContext);

            // check for required supporting type reference
            if (type.qualifiedTypeName.length != type.simpleTypeName.length) {
                int lastDotIndex = context.fullCompletionExpression.lastIndexOf('.');
                if (lastDotIndex > 0 && !context.getQualifiedCompletionExpression().startsWith(String.valueOf(firstSegment(type.packageName, '.')) + '.')) {
                    char[] typeName = CharOperation.subarray(type.qualifiedTypeName, 0, CharOperation.lastIndexOf('$', type.qualifiedTypeName));

                    // expression is partially-qualified; check type name availability
                    char[][] parts = qualifierAndSimpleTypeName(type.packageName, typeName);
                    if (!isImported(parts[0], parts[1])) {
                        GroovyCompletionProposal typeProposal = createProposal(CompletionProposal.TYPE_REF, offset);
                        typeProposal.setCompletion(CharOperation.concat(type.packageName, typeName, '.'));
                        typeProposal.setSignature(Signature.createCharArrayTypeSignature(typeProposal.getCompletion(), true));
                        typeProposal.setReplaceRange(offset, context.completionLocation - (context.fullCompletionExpression.length() - lastDotIndex));

                        proposal.setRequiredProposals(new CompletionProposal[] {typeProposal});
                    }
                }
            }

            if (context.location == ContentAssistLocation.EXCEPTIONS && isThrowableType((IType) javaProposal.getJavaElement())) {
                proposal.setRelevance(proposal.getRelevance() + 50);
            }
        }

        javaProposal.setTriggerCharacters(ProposalUtils.TYPE_TRIGGERS);
        javaProposal.setRelevance(proposal.getRelevance());
        return javaProposal;
    }

    List<ICompletionProposal> processAcceptedConstructors(Set<String> usedParams, JDTResolver resolver) {
        int n;
        if (acceptedConstructors == null || (n = acceptedConstructors.size()) == 0) {
            return Collections.emptyList();
        }

        checkCancel();

        List<ICompletionProposal> proposals = new LinkedList<>();
        try {
            for (int i = 0; i < n; i += 1) {
                // does not check cancellation for every types to avoid performance loss
                if ((i % CHECK_CANCEL_FREQUENCY) == 0) {
                    checkCancel();
                }

                AcceptedCtor ctor = (AcceptedCtor) acceptedConstructors.elementAt(i);

                if (imports == null && resolver.getScope() != null) {
                    initializeImportArrays(resolver.getScope());
                }

                ICompletionProposal constructorProposal = proposeConstructor(ctor);
                if (constructorProposal != null) {
                    proposals.add(constructorProposal);

                    if (contextOnly) {
                        // also add all of the constructor arguments for constructors with no args and when it is the only constructor in the class
                        ClassNode resolved = resolver.resolve(String.valueOf(ctor.fullyQualifiedName));
                        if (resolved != null) {
                            List<ConstructorNode> constructors = resolved.getDeclaredConstructors();
                            if (constructors != null && constructors.size() == 1) {
                                ConstructorNode constructor = constructors.get(0);
                                Parameter[] parameters = constructor.getParameters();
                                if (parameters == null || parameters.length == 0) {
                                    // instead of proposing no-arg constructor, propose type's properties as named arguments
                                    proposals.remove(constructorProposal);
                                    for (PropertyNode prop : resolved.getProperties()) { String name = prop.getName();
                                        if (!"metaClass".equals(name) && !usedParams.contains(name) && isCamelCaseMatch(context.completionExpression, name)) {
                                            GroovyNamedArgumentProposal namedArgument = new GroovyNamedArgumentProposal(name, prop.getType(), null, String.valueOf(ctor.simpleTypeName));
                                            proposals.add(namedArgument.createJavaProposal(context, javaContext));
                                        }
                                    }
                                    for (MethodNode meth : resolved.getMethods()) {
                                        if (!meth.isStatic() && AccessorSupport.isSetter(meth)) { String name = Introspector.decapitalize(meth.getName().substring(3));
                                            if (!"metaClass".equals(name) && !usedParams.contains(name) && resolved.getProperty(name) == null && isCamelCaseMatch(context.completionExpression, name)) {
                                                GroovyNamedArgumentProposal namedArgument = new GroovyNamedArgumentProposal(name, meth.getParameters()[0].getType(), null, String.valueOf(ctor.simpleTypeName));
                                                proposals.add(namedArgument.createJavaProposal(context, javaContext));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            acceptedTypes = null;
            relevanceRule = null;
        }
        return proposals;
    }

    private ICompletionProposal proposeConstructor(AcceptedCtor ctor) {
        char[] completionExpressionChars = completionExpression.toCharArray();
        int completionOffset = offset, kind = CompletionProposal.CONSTRUCTOR_INVOCATION;
        if (contextOnly) {
            // only show context information and only for constructors that exactly match
            if (!CharOperation.equals(ctor.simpleTypeName, CharOperation.lastSegment(completionExpressionChars, '.'))) {
                return null;
            }
            kind = CompletionProposal.METHOD_REF;
            completionOffset = ((MethodInfoContentAssistContext) context).methodNameEnd;
        }

        GroovyCompletionProposal proposal = createProposal(kind, completionOffset);
        proposal.setIsContructor(true);
        proposal.setName(ctor.simpleTypeName);
        proposal.setPackageName(ctor.packageName);
        proposal.setTypeName(ctor.qualifiedTypeName);
        proposal.setDeclarationPackageName(ctor.packageName);
        proposal.setDeclarationTypeName(ctor.qualifiedTypeName);
        proposal.setDeclarationSignature(CompletionEngine.createNonGenericTypeSignature(ctor.packageName, ctor.qualifiedTypeName));
        proposal.setFlags(Flags.isDeprecated(ctor.typeModifiers) ? ctor.modifiers | Flags.AccDeprecated : ctor.modifiers);
        proposal.setAccessibility(ctor.accessibility);

        if (contextOnly) {
            proposal.setCompletion(CharOperation.NO_CHAR);
            proposal.setReplaceRange(context.completionLocation, context.completionLocation);
        } else {
            proposal.setCompletion(new char[] {'(', ')'});
            proposal.setTokenRange(offset, context.completionEnd);
            proposal.setReplaceRange(context.completionEnd, context.completionEnd);

            // create nested completion for type name (and possible qualifier or import statement)
            GroovyCompletionProposal typeProposal = createProposal(CompletionProposal.TYPE_REF, -1);
            typeProposal.setName(ctor.simpleTypeName);
            typeProposal.setPackageName(ctor.packageName);
            typeProposal.setTypeName(ctor.qualifiedTypeName);
            typeProposal.setSignature(proposal.getDeclarationSignature());
            typeProposal.setCompletion(typeProposal.getName()); // replace only the last segment (i.e. "Ent" in "Map.Ent" or "java.util.Map.Ent")
            typeProposal.setReplaceRange(context.completionLocation - CharOperation.lastSegment(completionExpressionChars, '.').length, context.completionEnd);

            proposal.setRequiredProposals(new CompletionProposal[] {typeProposal});

            // check for qualified expression
            int lastDotIndex = context.fullCompletionExpression.lastIndexOf('.');
            if (lastDotIndex > 0 && ctor.qualifiedTypeName.length != ctor.simpleTypeName.length) {
                char[] outerTypeName = CharOperation.subarray(ctor.qualifiedTypeName, 0,
                                CharOperation.lastIndexOf('$', ctor.qualifiedTypeName));
                char[] plainTypeName = CharOperation.lastSegment(outerTypeName, '$');

                GroovyCompletionProposal outerTypeProposal = createProposal(CompletionProposal.TYPE_REF, -1);
                outerTypeProposal.setName(plainTypeName);
                outerTypeProposal.setTypeName(outerTypeName);
                outerTypeProposal.setPackageName(ctor.packageName);
                outerTypeProposal.setSignature(CompletionEngine.createNonGenericTypeSignature(ctor.packageName, outerTypeName));

                // replace only the penultimate segment (i.e. "Map" in "Map.Ent" or "java.util.Map.Ent")
                int idx = context.fullCompletionExpression.lastIndexOf(String.valueOf(plainTypeName), lastDotIndex);
                int loc = (context.completionLocation - (context.fullCompletionExpression.length() - idx));
                outerTypeProposal.setReplaceRange(loc, loc + outerTypeProposal.getName().length);
                outerTypeProposal.setCompletion(outerTypeProposal.getName());

                typeProposal.setRequiredProposals(new CompletionProposal[] {outerTypeProposal});

                // check for required qualifier or import statement insertion
                if (CharOperation.equals(plainTypeName, completionExpressionChars, 0, plainTypeName.length)) {
                    if (!isImported(qualifierAndSimpleTypeName(ctor.packageName, outerTypeName)[0], plainTypeName)) {
                        outerTypeProposal.setCompletion(Signature.toCharArray(outerTypeProposal.getSignature()));
                    }
                }
            } else if (lastDotIndex < 0 && !isImported(qualifierAndSimpleTypeName(ctor.packageName, ctor.qualifiedTypeName)[0], ctor.simpleTypeName)) {
                typeProposal.setCompletion(Signature.toCharArray(typeProposal.getSignature()));
            }
        }
        populateParameterInfo(proposal, ctor.parameterCount, ctor.parameterNames, ctor.parameterTypes, ctor.signature);

        // TODO: Leverage IRelevanceRule for this?
        float relevanceMultiplier = (ctor.accessibility == IAccessRule.K_ACCESSIBLE) ? 3 : 0;
        relevanceMultiplier += computeRelevanceForCaseMatching(completionExpressionChars, ctor.simpleTypeName);
        proposal.setRelevance(Relevance.MEDIUM_HIGH.getRelevance(relevanceMultiplier));

        GroovyJavaMethodCompletionProposal lazyProposal = new GroovyJavaMethodCompletionProposal(proposal, getProposalOptions(), javaContext, null);
        lazyProposal.setImportRewite(groovyRewriter.getImportRewrite(monitor));
        return lazyProposal;
    }

    private void populateParameterInfo(GroovyCompletionProposal proposal, int parameterCount, char[][] parameterNames, char[][] parameterTypes, char[] signature) {
        if (parameterCount == -1) {
            // default constructor
            parameterNames = CharOperation.NO_CHAR_CHAR;
            parameterTypes = CharOperation.NO_CHAR_CHAR;
        } else {
            int parameterNamesLength = parameterNames == null ? 0 : parameterNames.length;
            if (parameterCount != parameterNamesLength) {
                parameterNames = null;
            }
        }
        if (signature == null) {
            proposal.setSignature(createConstructorSignature(parameterTypes, true));
        } else {
            proposal.setSignature(CharOperation.replaceOnCopy(signature, '/', '.'));
        }
        if (parameterNames != null) {
            proposal.setParameterNames(parameterNames);
        } else {
            proposal.setHasNoParameterNamesFromIndex(true);
            if (mockEngine == null) {
                // used for caching types only
                mockEngine = new CompletionEngine(null, new CompletionRequestor() { @Override public void accept(CompletionProposal proposal) {} }, null, javaContext.getProject(), null, null);
            }
            proposal.setCompletionEngine(mockEngine);
        }
        if (parameterTypes == null) {
            parameterTypes = new char[parameterCount][];
            for (int i = 0; i < parameterCount; i += 1) {
                parameterTypes[i] = "def".toCharArray();
            }
        }
        proposal.setParameterTypeNames(parameterTypes);
    }

    private char[] createConstructorSignature(char[][] parameterTypes, boolean isQualified) {
        char[][] parameterTypeSigs;
        if (parameterTypes == null) {
            parameterTypeSigs = CharOperation.NO_CHAR_CHAR;
        } else {
            parameterTypeSigs = new char[parameterTypes.length][];
            for (int i = 0; i < parameterTypes.length; i++) {
                char[] copy = new char[parameterTypes[i].length];
                System.arraycopy(parameterTypes[i], 0, copy, 0, copy.length);
                CharOperation.replace(copy, '/', '.');
                parameterTypeSigs[i] = Signature.createCharArrayTypeSignature(copy, isQualified);
            }
        }
        return Signature.createMethodSignature(parameterTypeSigs, new char[] {'V'});
    }

    protected final GroovyCompletionProposal createProposal(int kind, int completionOffset) {
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(kind, completionOffset);
        proposal.setNameLookup(nameLookup);
        return proposal;
    }

    private ProposalFormattingOptions getProposalOptions() {
        if (groovyProposalPrefs == null) {
            groovyProposalPrefs = ProposalFormattingOptions.newFromOptions();
        }
        return groovyProposalPrefs;
    }

    private boolean isThrowableType(IType type) {
        try {
            if (type != null && type.isClass() && type.newSupertypeHierarchy(null)
                    .contains(unit.getJavaProject().findType("java.lang.Throwable"))) {
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    private int computeRelevanceForCaseMatching(char[] token, char[] proposalName) {
        if (CharOperation.equals(token, proposalName, true /* do not ignore case */)) {
            return RelevanceConstants.R_CASE + RelevanceConstants.R_EXACT_NAME;
        } else if (CharOperation.equals(token, proposalName, false /* ignore case */)) {
            return RelevanceConstants.R_EXACT_NAME;
        }
        return 0;
    }

    private int computeRelevanceForTypeProposal(char[] fullyQualifiedName, int accessibility, int modifiers) {
        IRelevanceRule rule = Optional.ofNullable(relevanceRule).orElse(IRelevanceRule.DEFAULT);
        return rule.getRelevance(fullyQualifiedName, allTypesInUnit, accessibility, modifiers);
    }

    private void initializeRelevanceRule(JDTResolver resolver) {
        if (context.lhsNode instanceof Variable) {
            ClassNode lhsType = ((Variable) context.lhsNode).getType();
            if (VariableScope.CLASS_CLASS_NODE.equals(lhsType) && lhsType.isUsingGenerics()) {
                GenericsType target = lhsType.getGenericsTypes()[0];
                if (target.getLowerBound() == null && target.getUpperBounds() == null ||
                        (target.getUpperBounds().length == 1 && VariableScope.OBJECT_CLASS_NODE.equals(target.getUpperBounds()[0]))) {
                    return;
                }
                // create a relevance rule that will boost types compatible with the target type
                IRelevanceRule rule = (char[] fullyQualifiedName, IType[] contextTypes, int accessibility, int modifiers) -> {
                    try {
                        ClassNode sourceType = resolver.resolve(String.valueOf(fullyQualifiedName));
                        if (target.isCompatibleWith(sourceType)) return 10;
                    } catch (RuntimeException e) {
                        if (GroovyLogManager.manager.hasLoggers()) {
                            GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST, e.getMessage());
                        } else {
                            System.err.println(getClass().getSimpleName() + ": " + e.getMessage());
                        }
                    }
                    return 0;
                };

                relevanceRule = CompositeRule.of(1.0, IRelevanceRule.DEFAULT, 2.0, rule);
            }
        }
    }

    /**
     * Fills in {@link #imports} and {@link #onDemandimports} from the compilation unit.
     */
    private void initializeImportArrays(GroovyCompilationUnitScope scope) {
        int i, n = (scope.imports != null) ? scope.imports.length : 0, s, t;
        for (i = 0, s = 0, t = 0; i < n; i += 1) {
            if (!scope.imports[i].isStatic()) {
                if (scope.imports[i].onDemand) {
                    s += 1;
                } else {
                    t += 1;
                }
            }
        }
        s += 1;

        char[][] starImports = new char[s][];
        char[][][] typeImports = new char[t][][];
        for (i = 0, s = 0, t = 0; i < n; i += 1) {
            if (!scope.imports[i].isStatic()) {
                if (scope.imports[i].onDemand) {
                    starImports[s++] = getImportName(scope.imports[i]);
                } else {
                    typeImports[t++] = new char[][] {getSimpleName(scope.imports[i]), getImportName(scope.imports[i])};
                }
            }
        }
        starImports[s++] = CharOperation.concatWith(scope.currentPackageName, '.');

        // add star import for each enclosing type to prevent unnecessary import/qualifier insertions
        ClassNode enclosingType = context.getEnclosingGroovyType();
        while (enclosingType != null) {
            char[] typeName = enclosingType.getName().replace('$', '.').toCharArray();
            starImports = (char[][]) ArrayUtils.add(starImports, s, typeName);
            enclosingType = enclosingType.getOuterClass();
        }

        imports = typeImports;
        onDemandImports = starImports;
    }

    private boolean isImported(char[] packName, char[] typeName) {
        boolean imported = false, conflict = false;
        if (imports != null) {
            for (char[][] importSpec : imports) {
                if (CharOperation.equals(typeName, importSpec[0])) {
                    if (CharOperation.equals(packName, importSpec[1], 0, CharOperation.lastIndexOf('.', importSpec[1]))) {
                        imported = true;
                    } else {
                        conflict = true;
                    }
                    break;
                }
            }
        }
        if (!imported && !conflict && onDemandImports != null) {
            for (char[] importName : onDemandImports) {
                if (CharOperation.equals(packName, importName)) {
                    imported = true;
                    break;
                }
            }
        }
        return imported;
    }

    private static char[] getImportName(ImportBinding binding) {
        if (binding.reference != null) {
            return CharOperation.concatWith(binding.reference.getImportName(), '.');
        }
        return CharOperation.concatWith(binding.compoundName, '.');
    }

    private static char[] getSimpleName(ImportBinding binding) {
        if (binding.reference != null) {
            return binding.reference.getSimpleName();
        }
        return binding.compoundName[binding.compoundName.length - 1];
    }

    private static boolean isCamelCaseMatch(String pattern, String candidate) {
        if (pattern == null || pattern.length() == 0) {
            return true;
        }
        return SearchPattern.camelCaseMatch(pattern, candidate);
    }

    /**
     * @see CharOperation#lastSegment
     */
    private static char[] firstSegment(char[] arr, char sep) {
        int pos = CharOperation.indexOf(sep, arr);
        if (pos < 0) {
            return arr;
        }
        return CharOperation.subarray(arr, 0, pos);
    }

    /**
     * "java.lang", "Object" -> "java.lang", "Object"
     * "java.util", "Map$Entry" -> "java.util.Map", "Entry"
     */
    private static char[][] qualifierAndSimpleTypeName(char[] packName, char[] typeName) {
        int pos = CharOperation.lastIndexOf('$', typeName);
        if (pos > 0) {
            char[] typeQual = CharOperation.subarray(typeName, 0, pos);
            typeName = CharOperation.subarray(typeName, pos + 1, typeName.length);
            packName = CharOperation.concat(packName, CharOperation.replaceOnCopy(typeQual, '$', '.'), '.');
        }
        return new char[][] {packName, typeName};
    }

    //--------------------------------------------------------------------------

    private static class AcceptedCtor {
        public int modifiers;
        public char[] simpleTypeName;
        public int parameterCount;
        public char[] signature;
        public char[][] parameterTypes;
        public char[][] parameterNames;
        public int typeModifiers;
        public char[] packageName;
        //public int extraFlags;
        public int accessibility;

        public final char[] qualifiedTypeName;
        public final char[] fullyQualifiedName;

        AcceptedCtor(
            int modifiers,
            char[] simpleTypeName,
            int parameterCount,
            char[] signature,
            char[][] parameterTypes,
            char[][] parameterNames,
            int typeModifiers,
            char[] packageName,
            int extraFlags,
            int accessibility) {

            this.modifiers = modifiers;
            this.simpleTypeName = CharOperation.lastSegment(simpleTypeName, '$');
            this.parameterCount = parameterCount;
            this.signature = signature;
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.typeModifiers = typeModifiers;
            this.packageName = packageName;
            //this.extraFlags = extraFlags;
            this.accessibility = accessibility;

            this.qualifiedTypeName = simpleTypeName;
            this.fullyQualifiedName = CharOperation.concat(packageName, qualifiedTypeName, '.');
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');
            buffer.append(fullyQualifiedName);
            buffer.append('}');

            return buffer.toString();
        }
    }

    private static class AcceptedType {
        public char[] packageName;
        public char[] simpleTypeName;
        public char[][] enclosingTypeNames;
        public int modifiers;
        public int accessibility;
        public boolean mustBeQualified;
        public char[] fullyQualifiedName;
        public char[] qualifiedTypeName;

        AcceptedType(
            char[] packageName,
            char[] simpleTypeName,
            char[][] enclosingTypeNames,
            int modifiers,
            int accessibility) {

            this.packageName = packageName;
            this.simpleTypeName = simpleTypeName;
            this.enclosingTypeNames = enclosingTypeNames;
            this.modifiers = modifiers;
            this.accessibility = accessibility;
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');
            if (fullyQualifiedName != null) {
                buffer.append(fullyQualifiedName);
            } else {
                buffer.append(packageName).append('.');
                buffer.append(CharOperation.concatWith(enclosingTypeNames, simpleTypeName, '$'));
            }
            buffer.append('}');

            return buffer.toString();
        }
    }
}
