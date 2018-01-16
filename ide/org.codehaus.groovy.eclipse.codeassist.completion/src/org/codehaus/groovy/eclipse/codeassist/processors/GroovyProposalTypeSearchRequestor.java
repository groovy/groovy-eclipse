/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.processors;

import java.beans.Introspector;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitScope;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
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
import org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
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
    private static final int CHECK_CANCEL_FREQUENCY = 50;

    private int foundTypesCount = 0;
    private int foundConstructorsCount = 0;

    private ObjectVector acceptedTypes;
    private Set<String> acceptedPackages;
    private boolean shouldAcceptConstructors;
    private ObjectVector acceptedConstructors;

    private final int offset;
    private final int replaceLength;

    /** Array of simple name, fully-qualified name pairs. Default imports should be included (aka BigDecimal, etc.). */
    private char[][][] imports;
    /** Array of fully-qualified names. Default imports should be included (aka java.lang, groovy.lang, etc.). */
    private char[][] onDemandImports;

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

    private final ContentAssistContext context;
    private final        AssistOptions options;

    public GroovyProposalTypeSearchRequestor(
            ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext,
            int exprStart,
            int replaceLength,
            NameLookup nameLookup,
            IProgressMonitor monitor) {

        this.context = context;
        this.javaContext = javaContext;
        Assert.isNotNull(javaContext.getCoreContext());
        this.offset = exprStart;
        this.replaceLength = replaceLength;
        this.nameLookup = nameLookup;
        this.monitor = monitor;
        this.unit = context.unit;
        this.module = context.unit.getModuleNode();
        this.isImport = (context.location == ContentAssistLocation.IMPORT);
        // if contextOnly then do not insert any text, only show context information
        this.contextOnly = (context.location == ContentAssistLocation.METHOD_CONTEXT);
        this.shouldAcceptConstructors = (context.location == ContentAssistLocation.METHOD_CONTEXT || context.location == ContentAssistLocation.CONSTRUCTOR);
        this.completionExpression = (contextOnly ? ((MethodInfoContentAssistContext) context).methodName : context.fullCompletionExpression.replaceFirst("^new\\s+", ""));

        this.groovyRewriter = new GroovyImportRewriteFactory(this.unit, this.module);
        this.options = new AssistOptions(javaContext.getProject().getOptions(true));

        try {
            this.allTypesInUnit = this.unit.getAllTypes();
        } catch (JavaModelException e) {
            GroovyContentAssist.logError("Problem with type completion", e);
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
    public void acceptConstructor(
            int modifiers,
            char[] simpleTypeName,
            int parameterCount,
            char[] signature,
            char[][] parameterTypes,
            char[][] parameterNames,
            int typeModifiers,
            char[] packageName,
            int extraFlags,
            String path,
            AccessRestriction accessRestriction) {

        if (shouldAcceptConstructors) {
            // do not check cancellation for every ctor to avoid performance loss
            if ((foundConstructorsCount % (CHECK_CANCEL_FREQUENCY)) == 0)
                checkCancel();
            foundConstructorsCount += 1;

            // do not propose enum constructors
            if (Flags.isEnum(typeModifiers)) {
                return;
            }
            if (TypeFilter.isFiltered(packageName, simpleTypeName)) {
                return;
            }
            if (this.options.checkDeprecation && (typeModifiers & Flags.AccDeprecated) != 0) {
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

            if (signature == null) {
                //signature = Signature.createArraySignature(typeSignature, arrayCount)
            }

            if (acceptedConstructors == null)
                acceptedConstructors = new ObjectVector();
            acceptedConstructors.add(new AcceptedCtor(modifiers, simpleTypeName, parameterCount, signature, parameterTypes, parameterNames, typeModifiers, packageName, extraFlags, accessibility));
        }
    }

    @Override
    public void acceptType(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, int modifiers, AccessRestriction accessRestriction) {
        // do not check cancellation for every type to avoid performance loss
        if ((foundTypesCount % CHECK_CANCEL_FREQUENCY) == 0)
            checkCancel();
        foundTypesCount += 1;

        // do not propose synthetic types
        if (CharOperation.contains('$', simpleTypeName)) {
            return;
        }
        if (TypeFilter.isFiltered(packageName, simpleTypeName)) {
            return;
        }
        if (options.checkDeprecation && (modifiers & Flags.AccDeprecated) != 0) {
            return;
        }
        if (context.location == ContentAssistLocation.EXTENDS && (modifiers & Flags.AccFinal) != 0) {
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

    private void checkCancel() {
        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
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
        try {
            HashtableOfObject onDemandFound = new HashtableOfObject();

            next: for (int i = 0; i < n; i += 1) {
                // does not check cancellation for every type to avoid performance loss
                if ((i % CHECK_CANCEL_FREQUENCY) == 0) {
                    checkCancel();
                }

                AcceptedType acceptedType = (AcceptedType) acceptedTypes.elementAt(i);
                char[] packageName = acceptedType.packageName;
                char[] simpleTypeName = acceptedType.simpleTypeName;
                char[][] enclosingTypeNames = acceptedType.enclosingTypeNames;
                int modifiers = acceptedType.modifiers;
                int accessibility = acceptedType.accessibility;

                char[] typeName;
                char[] flatEnclosingTypeNames;
                if (enclosingTypeNames == null || enclosingTypeNames.length == 0) {
                    flatEnclosingTypeNames = null;
                    typeName = simpleTypeName;
                } else {
                    flatEnclosingTypeNames = CharOperation.concatWith(acceptedType.enclosingTypeNames, '$');
                    typeName = CharOperation.concat(flatEnclosingTypeNames, simpleTypeName, '$');
                }
                char[] fullyQualifiedName = CharOperation.concat(packageName, typeName, '.');

                // get this imports from the module node
                if (imports == null && resolver.getScope() != null) {
                    initializeImportArrays(resolver.getScope());
                }

                if (imports != null) {
                    // check to see if this type is imported explicitly
                    for (char[][] importName : imports) {
                        if (CharOperation.equals(typeName, importName[0])) {
                            // potentially use fully qualified type name if there is already something else with the same simple name imported
                            proposals.add(proposeType(packageName, simpleTypeName, modifiers, accessibility, typeName, fullyQualifiedName, !CharOperation.equals(fullyQualifiedName, importName[1])));
                            continue next;
                        }
                    }
                }

                if ((enclosingTypeNames == null || enclosingTypeNames.length == 0) && isCurrentPackage(packageName)) {
                    proposals.add(proposeType(packageName, simpleTypeName, modifiers, accessibility, typeName, fullyQualifiedName, false));
                } else if (((AcceptedType) onDemandFound.get(simpleTypeName)) == null && onDemandImports != null) {
                    char[] fullyQualifiedEnclosingTypeOrPackageName = null;
                    for (char[] importFlatName : onDemandImports) {
                        if (fullyQualifiedEnclosingTypeOrPackageName == null) {
                            if (enclosingTypeNames != null && enclosingTypeNames.length != 0) {
                                fullyQualifiedEnclosingTypeOrPackageName = CharOperation.concat(packageName, flatEnclosingTypeNames, '.');
                            } else {
                                fullyQualifiedEnclosingTypeOrPackageName = packageName;
                            }
                        }
                        if (CharOperation.equals(fullyQualifiedEnclosingTypeOrPackageName, importFlatName)) {
                            acceptedType.qualifiedTypeName = typeName;
                            acceptedType.fullyQualifiedName = fullyQualifiedName;
                            onDemandFound.put(simpleTypeName, acceptedType);
                            continue next;
                        }
                    }
                    char[] packName = fullyQualifiedEnclosingTypeOrPackageName != null ? fullyQualifiedEnclosingTypeOrPackageName : packageName;
                    proposals.add(proposeType(packName, simpleTypeName, modifiers, accessibility, typeName, fullyQualifiedName, true));
                }
            }

            char[][] keys = onDemandFound.keyTable;
            Object[] vals = onDemandFound.valueTable;
            for (int i = 0; i < keys.length; i += 1) {
                if ((i % CHECK_CANCEL_FREQUENCY) == 0)
                    checkCancel();
                if (keys[i] != null) {
                    AcceptedType value = (AcceptedType) vals[i];
                    if (value != null) {
                        proposals.add(proposeType(value.packageName, value.simpleTypeName, value.modifiers, value.accessibility, value.qualifiedTypeName, value.fullyQualifiedName, value.mustBeQualified));
                    }
                }
            }
        } finally {
            acceptedTypes = null;
            relevanceRule = null;
        }
        return proposals;
    }

    private ICompletionProposal proposeType(char[] packageName, char[] simpleTypeName, int modifiers, int accessibility, char[] qualifiedTypeName, char[] fullyQualifiedName, boolean isQualified) {
        return isImport
            ? proposeNoImportType(packageName, simpleTypeName, modifiers, accessibility, qualifiedTypeName, fullyQualifiedName, isQualified)
            : proposeImportableType(packageName, simpleTypeName, modifiers, accessibility, qualifiedTypeName, fullyQualifiedName, isQualified);
    }

    private ICompletionProposal proposeNoImportType(char[] packageName, char[] simpleTypeName, int modifiers, int accessibility, char[] qualifiedTypeName, char[] fullyQualifiedName, boolean isQualified) {
        char[] completion = isQualified ? fullyQualifiedName : simpleTypeName;
        String completionString = String.valueOf(completion);

        GroovyCompletionProposal proposal = createProposal(CompletionProposal.TYPE_REF, context.completionLocation - offset);
        proposal.setAccessibility(accessibility);
        proposal.setCompletion(completion);
        proposal.setDeclarationSignature(packageName);
        proposal.setFlags(modifiers);
        proposal.setPackageName(packageName);
        proposal.setRelevance(computeRelevanceForTypeProposal(fullyQualifiedName, accessibility, modifiers));
        proposal.setReplaceRange(offset, offset + replaceLength);
        proposal.setSignature(CompletionEngine.createNonGenericTypeSignature(packageName, simpleTypeName));
        proposal.setTokenRange(offset, context.completionLocation);
        proposal.setTypeName(simpleTypeName);

        JavaTypeCompletionProposal javaProposal = new JavaTypeCompletionProposal(completionString, null, offset, replaceLength, ProposalUtils.getImage(proposal), ProposalUtils.createDisplayString(proposal), proposal.getRelevance(), completionString, javaContext);
        javaProposal.setTriggerCharacters(ProposalUtils.TYPE_TRIGGERS);
        javaProposal.setRelevance(proposal.getRelevance());
        return javaProposal;
    }

    private ICompletionProposal proposeImportableType(char[] packageName, char[] simpleTypeName, int modifiers, int accessibility, char[] qualifiedTypeName, char[] fullyQualifiedName, boolean isQualified) {
        GroovyCompletionProposal proposal = createProposal(CompletionProposal.TYPE_REF, context.completionLocation - offset);
        proposal.setAccessibility(accessibility);
        proposal.setCompletion(isQualified ? fullyQualifiedName : simpleTypeName);
        proposal.setDeclarationSignature(packageName);
        proposal.setFlags(modifiers);
        proposal.setPackageName(packageName);
        proposal.setRelevance(computeRelevanceForTypeProposal(fullyQualifiedName, accessibility, modifiers));
        proposal.setReplaceRange(offset, offset + replaceLength);
        proposal.setSignature(CompletionEngine.createNonGenericTypeSignature(packageName, simpleTypeName));
        proposal.setTokenRange(offset, context.completionLocation);
        proposal.setTypeName(simpleTypeName);

        LazyJavaTypeCompletionProposal javaProposal = new LazyJavaTypeCompletionProposal(proposal, javaContext);
        javaProposal.setTriggerCharacters(ProposalUtils.TYPE_TRIGGERS);
        javaProposal.setRelevance(proposal.getRelevance());
        ImportRewrite r = groovyRewriter.getImportRewrite(monitor);
        if (r != null) {
            ReflectionUtils.setPrivateField(LazyJavaTypeCompletionProposal.class, "fImportRewrite", javaProposal, r);
        }
        return javaProposal;
    }

    List<ICompletionProposal> processAcceptedPackages() {
        checkCancel();
        List<ICompletionProposal> proposals = new LinkedList<>();
        if (acceptedPackages != null && acceptedPackages.size() > 0) {
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

    List<ICompletionProposal> processAcceptedConstructors(Set<String> usedParams, JDTResolver resolver) {
        int n;
        if (acceptedConstructors == null || (n = acceptedConstructors.size()) == 0) {
            return Collections.emptyList();
        }

        checkCancel();

        String currentPackageNameStr = module.getPackageName();
        char[] currentPackageName;
        if (currentPackageNameStr == null) {
            currentPackageName = CharOperation.NO_CHAR;
        } else {
            currentPackageName = currentPackageNameStr.toCharArray();
            if (currentPackageName[currentPackageName.length - 1] == '.') {
                char[] newPackageName = new char[currentPackageName.length - 1];
                System.arraycopy(currentPackageName, 0, newPackageName, 0, newPackageName.length);
                currentPackageName = newPackageName;
            }
        }

        List<ICompletionProposal> proposals = new LinkedList<>();
        try {
            for (int i = 0; i < n; i += 1) {
                // does not check cancellation for every types to avoid performance loss
                if ((i % CHECK_CANCEL_FREQUENCY) == 0) {
                    checkCancel();
                }

                AcceptedCtor acceptedConstructor = (AcceptedCtor) acceptedConstructors.elementAt(i);

                final int typeModifiers = acceptedConstructor.typeModifiers;
                if (Flags.isInterface(typeModifiers) || Flags.isAnnotation(typeModifiers) || Flags.isEnum(typeModifiers)) {
                    continue;
                }

                final char[] packageName = acceptedConstructor.packageName;
                final char[] simpleTypeName = acceptedConstructor.simpleTypeName;
                final int modifiers = acceptedConstructor.modifiers;
                final int parameterCount = acceptedConstructor.parameterCount;
                final char[] signature = acceptedConstructor.signature;
                final char[][] parameterTypes = acceptedConstructor.parameterTypes;
                final char[][] parameterNames = acceptedConstructor.parameterNames;
                final int extraFlags = acceptedConstructor.extraFlags;
                final int accessibility = acceptedConstructor.accessibility;
                char[] fullyQualifiedName = CharOperation.concat(packageName, simpleTypeName, '.');

                if (imports == null && resolver.getScope() != null) {
                    initializeImportArrays(resolver.getScope());
                }

                // propose all constructors regardless of package, but ignore enums
                if (!Flags.isEnum(typeModifiers)) {
                    ICompletionProposal constructorProposal = proposeConstructor(simpleTypeName, parameterCount, signature, parameterTypes, parameterNames, modifiers, packageName, typeModifiers, accessibility, fullyQualifiedName, false, extraFlags);
                    if (constructorProposal != null) {
                        proposals.add(constructorProposal);

                        if (contextOnly) {
                            // also add all of the constructor arguments for constructors with no args and when it is the only constructor in the class
                            ClassNode resolved = resolver.resolve(String.valueOf(fullyQualifiedName));
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
                                                GroovyNamedArgumentProposal namedArgument = new GroovyNamedArgumentProposal(name, prop.getType(), null, String.valueOf(simpleTypeName));
                                                proposals.add(namedArgument.createJavaProposal(context, javaContext));
                                            }
                                        }
                                        for (MethodNode meth : resolved.getMethods()) {
                                            if (!meth.isStatic() && AccessorSupport.isSetter(meth)) { String name = Introspector.decapitalize(meth.getName().substring(3));
                                                if (!"metaClass".equals(name) && !usedParams.contains(name) && resolved.getProperty(name) == null && isCamelCaseMatch(context.completionExpression, name)) {
                                                    GroovyNamedArgumentProposal namedArgument = new GroovyNamedArgumentProposal(name, meth.getParameters()[0].getType(), null, String.valueOf(simpleTypeName));
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
            }

        } finally {
            acceptedTypes = null;
            relevanceRule = null;
        }
        return proposals;
    }

    private ICompletionProposal proposeConstructor(char[] simpleTypeName, int parameterCount, char[] signature, char[][] parameterTypes, char[][] parameterNames, int modifiers, char[] packageName, int typeModifiers, int accessibility, char[] fullyQualifiedName, boolean isQualified, int extraFlags) {
        char[] completionExpressionChars = completionExpression.toCharArray();
        // only show context information and only for constructors that exactly match the name
        if (contextOnly && !CharOperation.equals(completionExpressionChars, simpleTypeName) && !CharOperation.equals(completionExpressionChars, fullyQualifiedName)) {
            return null;
        }

        GroovyCompletionProposal proposal = createProposal(contextOnly ? CompletionProposal.METHOD_REF : CompletionProposal.CONSTRUCTOR_INVOCATION, offset - 1);
        proposal.setIsContructor(true);
        proposal.setName(simpleTypeName);
        proposal.setTypeName(simpleTypeName);
        proposal.setPackageName(packageName);
        proposal.setDeclarationTypeName(simpleTypeName);
        proposal.setDeclarationPackageName(packageName);
        proposal.setDeclarationSignature(CompletionEngine.createNonGenericTypeSignature(packageName, simpleTypeName));
        proposal.setFlags(Flags.isDeprecated(typeModifiers) ? modifiers | Flags.AccDeprecated : modifiers);
        proposal.setAdditionalFlags(extraFlags);
        proposal.setAccessibility(accessibility);

        populateParameterInfo(proposal, parameterCount, parameterNames, parameterTypes, signature, isQualified);
        populateReplacementInfo(proposal, packageName, simpleTypeName, fullyQualifiedName); // deals with context-only and imports

        // TODO: Leverage IRelevanceRule for this?
        float relevanceMultiplier = (accessibility == IAccessRule.K_ACCESSIBLE) ? 3 : 0;
        relevanceMultiplier += computeRelevanceForCaseMatching(completionExpressionChars, simpleTypeName);
        proposal.setRelevance(Relevance.MEDIUM_HIGH.getRelevance(relevanceMultiplier));

        GroovyJavaMethodCompletionProposal lazyProposal = new GroovyJavaMethodCompletionProposal(proposal, javaContext, getProposalOptions());
        lazyProposal.setImportRewite(groovyRewriter.getImportRewrite(monitor));
        if (contextOnly) {
            lazyProposal.contextOnly();
        }
        if (proposal.hasParameters()) {
            lazyProposal.setTriggerCharacters(ProposalUtils.METHOD_WITH_ARGUMENTS_TRIGGERS);
        } else {
            lazyProposal.setTriggerCharacters(ProposalUtils.METHOD_TRIGGERS);
        }
        return lazyProposal;
    }

    private void populateParameterInfo(GroovyCompletionProposal proposal, int parameterCount, char[][] parameterNames, char[][] parameterTypes, char[] signature, boolean isQualified) {
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
            proposal.setSignature(createConstructorSignature(parameterTypes, isQualified));
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

    private void populateReplacementInfo(GroovyCompletionProposal proposal, char[] packageName, char[] simpleTypeName, char[] fullyQualifiedName) {
        if (contextOnly) {
            proposal.setReplaceRange(context.completionLocation, context.completionLocation);
            proposal.setTokenRange(context.completionLocation, context.completionLocation);
            proposal.setCompletion(CharOperation.NO_CHAR);
        } else {
            proposal.setReplaceRange(offset + replaceLength, offset + replaceLength);
            proposal.setTokenRange(offset, context.completionLocation);
            char[] completion = new char[] {'(', ')'};
            try {
                // try not to insert an extra set of parentheses when completing the constructor name
                if (javaContext.getDocument().getChar(context.completionLocation) == '(') {
                    completion = CharOperation.NO_CHAR;
                }
            } catch (BadLocationException ignored) {
            }
            proposal.setCompletion(completion);

            // create nested completion for type name (and possibly import statement)

            char[] typeCompletion = simpleTypeName;
            if (context.fullCompletionExpression.indexOf('.') == -1 && !isCurrentPackage(packageName)) {
                boolean imported = false;
                if (imports != null) {
                    // check to see if this type is imported explicitly
                    for (char[][] importName : imports) {
                        if (CharOperation.equals(typeCompletion, importName[0])) {
                            imported = true;
                            break;
                        }
                    }
                    if (!imported && onDemandImports != null) {
                        for (char[] importPack : onDemandImports) {
                            if (CharOperation.equals(fullyQualifiedName, CharOperation.concat(importPack, simpleTypeName, '.'))) {
                                imported = true;
                                break;
                            }
                        }
                    }
                }
                if (!imported) typeCompletion = fullyQualifiedName;
            }

            GroovyCompletionProposal typeProposal = createProposal(CompletionProposal.TYPE_REF, context.completionLocation - 1);
            typeProposal.setSignature(proposal.getDeclarationSignature());
            typeProposal.setReplaceRange(offset, offset + replaceLength);
            typeProposal.setTokenRange(offset, offset + replaceLength);
            typeProposal.setCompletion(typeCompletion);

            //typeProposal.setFlags(typeModifiers);
            //typeProposal.setTypeName(simpleTypeName);
            //typeProposal.setPackageName(packageName);
            //typeProposal.setDeclarationSignature(declarationSignature);
            //typeProposal.setRelevance(computeRelevanceForTypeProposal(fullyQualifiedName, accessibility, augmentedModifiers));

            proposal.setRequiredProposals(new CompletionProposal[] {typeProposal});
        }
    }

    private ProposalFormattingOptions getProposalOptions() {
        if (groovyProposalPrefs == null) {
            groovyProposalPrefs = ProposalFormattingOptions.newFromOptions();
        }
        return groovyProposalPrefs;
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
        return Signature.createMethodSignature(parameterTypeSigs, new char[] { 'V' });
    }

    protected final GroovyCompletionProposal createProposal(int kind, int completionOffset) {
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(kind, completionOffset);
        proposal.setNameLookup(nameLookup);
        return proposal;
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
                if (target.getLowerBound() == null && target.getUpperBounds().length == 1 &&
                        VariableScope.OBJECT_CLASS_NODE.equals(target.getUpperBounds()[0])) {
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
     *
     * NOTE: The original implementation of this method did not add "java.lang" to star
     * imports. Adding it to the array may result in extra type proposals. Not sure...
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

        imports = typeImports;
        onDemandImports = starImports;
    }

    private static boolean isCamelCaseMatch(String pattern, String candidate) {
        if (pattern == null || pattern.length() == 0) {
            return true;
        }
        return SearchPattern.camelCaseMatch(pattern, candidate);
    }

    private boolean isCurrentPackage(char[] packageName) {
        boolean isCurrentPackage = false;

        String currentPackage = module.getPackageName();
        if (currentPackage == null) {
            isCurrentPackage = (packageName == null || packageName.length == 0);
        } else if ((currentPackage.length() - 1) == packageName.length) { // getPackageName() includes trailing '.'
            isCurrentPackage = CharOperation.equals(packageName, currentPackage.toCharArray(), 0, currentPackage.length() - 1);
        }

        return isCurrentPackage;
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
        public int extraFlags;
        public int accessibility;

        public AcceptedCtor(
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
            this.simpleTypeName = simpleTypeName;
            this.parameterCount = parameterCount;
            this.signature = signature;
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.typeModifiers = typeModifiers;
            this.packageName = packageName;
            this.extraFlags = extraFlags;
            this.accessibility = accessibility;
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');
            buffer.append(packageName);
            buffer.append(',');
            buffer.append(simpleTypeName);
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
        public boolean mustBeQualified = false;
        public char[] fullyQualifiedName = null;
        public char[] qualifiedTypeName = null;

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
            buffer.append(packageName);
            buffer.append(',');
            buffer.append(simpleTypeName);
            buffer.append(',');
            buffer.append(CharOperation.concatWith(enclosingTypeNames, '$'));
            buffer.append('}');
            return buffer.toString();
        }
    }
}
