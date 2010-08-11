 /*
 * Copyright 2003-2009 the original author or authors.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 *
 * @author Andrew Eisenberg
 * @created Oct 27, 2009
 *
 * This type requestor searches for groovy type content assist proposals in the current
 * scope.  This class is largely copied from {@link CompletionEngine}.  Method
 * names used here are the same as the method names used in the original code
 * Method parts are omitted or commented out when they are not relevant for
 * or not supported by groovy completion.
 */
class GroovyProposalTypeSearchRequestor implements ISearchRequestor {

    private static final char[][] DEFAULT_GROOVY_IMPORTS = { "java.math.BigDecimal".toCharArray(), "java.math.BigInteger".toCharArray() };
    private static final char[][] DEFAULT_GROOVY_IMPORTS_SIMPLE_NAMES = { "BigDecimal".toCharArray(), "BigInteger".toCharArray() };
    private static final char[][] DEFAULT_GROOVY_ON_DEMAND_IMPORTS = { "java.io".toCharArray(), "java.net".toCharArray(), "java.util".toCharArray(), "groovy.lang".toCharArray(), "groovy.util".toCharArray() };

    private class AcceptedType {
        public char[] packageName;

        public char[] simpleTypeName;

        public char[][] enclosingTypeNames;

        public int modifiers;

        public int accessibility;

        public boolean mustBeQualified = false;

        public char[] fullyQualifiedName = null;

        public char[] qualifiedTypeName = null;

        AcceptedType(char[] packageName, char[] simpleTypeName,
                char[][] enclosingTypeNames, int modifiers, int accessibility) {
            this.packageName = packageName;
            this.simpleTypeName = simpleTypeName;
            this.enclosingTypeNames = enclosingTypeNames;
            this.modifiers = modifiers;
            this.accessibility = accessibility;
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append('{');
            buffer.append(this.packageName);
            buffer.append(',');
            buffer.append(this.simpleTypeName);
            buffer.append(',');
            buffer.append(CharOperation
                    .concatWith(this.enclosingTypeNames, '.'));
            buffer.append('}');
            return buffer.toString();
        }
    }


    private final static int CHECK_CANCEL_FREQUENCY = 50;

    private int foundTypesCount = 0;

    private final IProgressMonitor monitor;

    private ObjectVector acceptedTypes;

    private Set<String> acceptedPackages;

    private boolean importCachesInitialized;

    private final int offset;

    private final int replaceLength;

    private final int actualCompletionPosition;

    private char[][][] imports; // list of imports. simple name, qualified
                                // name

    private char[][] onDemandimports; // list of imports. qualified package
                                      // name

    private final boolean isImport;

    private final JavaContentAssistInvocationContext javaContext;
    private final ModuleNode module;

    private final GroovyCompilationUnit unit;

    private final NameLookup nameLookup;

    // will be non-null if there is an unrevoverable error in the module node
    private ImportRewrite rewrite;

    // set to true if there is a problem creating the rewrite
    private boolean cantCreateRewrite = false;

    public GroovyProposalTypeSearchRequestor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext, int exprStart, NameLookup nameLookup, IProgressMonitor monitor) {

        this.offset = exprStart;
        this.javaContext = javaContext;
        this.module = context.unit.getModuleNode();
        this.unit = context.unit;
        this.replaceLength = context.completionExpression.length();
        this.actualCompletionPosition = context.completionLocation;
        this.monitor = monitor;
        this.acceptedTypes = new ObjectVector();
        importCachesInitialized = false;
        this.nameLookup = nameLookup;
        this.isImport = context.location == ContentAssistLocation.IMPORT;
    }

    public void acceptConstructor(int modifiers, char[] simpleTypeName,
            int parameterCount, char[] signature, char[][] parameterTypes,
            char[][] parameterNames, int typeModifiers, char[] packageName,
            int extraFlags, String path, AccessRestriction access) { }

    public void acceptPackage(char[] packageName) {
        this.checkCancel();
        if (acceptedPackages == null) {
            acceptedPackages = new HashSet<String>();
        }
        acceptedPackages.add(String.valueOf(packageName));
    }

    public void acceptType(char[] packageName, char[] simpleTypeName,
            char[][] enclosingTypeNames, int modifiers,
            AccessRestriction accessRestriction) {
        // does not check cancellation for every types to avoid performance
        // loss
        if ((this.foundTypesCount % CHECK_CANCEL_FREQUENCY) == 0)
            checkCancel();
        this.foundTypesCount++;

        // ignore synthetic
        if (CharOperation.contains('$', simpleTypeName)) {
            return;
        }

        int accessibility = IAccessRule.K_ACCESSIBLE;
        if (accessRestriction != null) {
            switch (accessRestriction.getProblemId()) {
                case IProblem.ForbiddenReference:
                    // forbidden references are removed
                    return;
                case IProblem.DiscouragedReference:
                    // discouraged references have a lower priority
                    accessibility = IAccessRule.K_DISCOURAGED;
                    break;
            }
        }

        if (this.acceptedTypes == null) {
            this.acceptedTypes = new ObjectVector();
        }
        this.acceptedTypes.add(new AcceptedType(packageName,
                simpleTypeName, enclosingTypeNames, modifiers,
                accessibility));
    }

    private void checkCancel() {
        if (this.monitor != null && this.monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    /**
     * This method is called after all types have been accepted by
     * this requestor.  Converts each type into an {@link ICompletionProposal}
     * @return list of all {@link ICompletionProposal}s applicable for this
     * content assist request.
     */
    List<ICompletionProposal> processAcceptedTypes() {

        this.checkCancel();

        if (this.acceptedTypes == null)
            return Collections.EMPTY_LIST;

        int length = this.acceptedTypes.size();

        if (length == 0)
            return Collections.EMPTY_LIST;

        HashtableOfObject onDemandFound = new HashtableOfObject();
        String thisPackageName = module.getPackageName() == null ? "" : module.getPackageName();

        List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
        try {
            next: for (int i = 0; i < length; i++) {

                // does not check cancellation for every types to avoid
                // performance loss
                if ((i % CHECK_CANCEL_FREQUENCY) == 0)
                    checkCancel();

                AcceptedType acceptedType = (AcceptedType) this.acceptedTypes
                        .elementAt(i);
                char[] packageName = acceptedType.packageName;
                char[] simpleTypeName = acceptedType.simpleTypeName;
                char[][] enclosingTypeNames = acceptedType.enclosingTypeNames;
                int modifiers = acceptedType.modifiers;
                int accessibility = acceptedType.accessibility;

                char[] typeName;
                char[] flatEnclosingTypeNames;
                if (enclosingTypeNames == null
                        || enclosingTypeNames.length == 0) {
                    flatEnclosingTypeNames = null;
                    typeName = simpleTypeName;
                } else {
                    flatEnclosingTypeNames = CharOperation.concatWith(
                            acceptedType.enclosingTypeNames, '.');
                    typeName = CharOperation.concat(flatEnclosingTypeNames,
                            simpleTypeName, '.');
                }
                char[] fullyQualifiedName = CharOperation.concat(
                        packageName, typeName, '.');

                // get this imports from the module node
                if (!this.importCachesInitialized) {
                    initializeImportCaches();
                }

                // check to see if this type is imported explicitly
                for (int j = 0; j < imports.length; j++) {
                    char[][] importName = imports[j];
                    if (CharOperation.equals(typeName, importName[0])) {
                        // potentially use fully qualified type name
                        // if there is already something else with the same
                        // simple
                        // name imported
                        proposals.add(proposeType(packageName,
                                simpleTypeName, modifiers, accessibility,
                                typeName, fullyQualifiedName,
                                !CharOperation.equals(fullyQualifiedName,
                                        importName[1])));
                        continue next;
                    }
                }

                if ((enclosingTypeNames == null || enclosingTypeNames.length == 0)
                        && CharOperation.equals(thisPackageName
                                .toCharArray(), packageName)) {
                    proposals.add(proposeType(packageName, simpleTypeName,
                            modifiers, accessibility, typeName,
                            fullyQualifiedName, false));
                    continue next;
                } else {
                    char[] fullyQualifiedEnclosingTypeOrPackageName = null;

                    if (((AcceptedType) onDemandFound
                            .get(simpleTypeName)) == null) {
                        for (int j = 0; j < this.onDemandimports.length; j++) {
                            char[] importFlatName = onDemandimports[j];

                            if (fullyQualifiedEnclosingTypeOrPackageName == null) {
                                if (enclosingTypeNames != null
                                        && enclosingTypeNames.length != 0) {
                                    fullyQualifiedEnclosingTypeOrPackageName = CharOperation
                                            .concat(packageName,
                                                    flatEnclosingTypeNames,
                                                    '.');
                                } else {
                                    fullyQualifiedEnclosingTypeOrPackageName = packageName;
                                }
                            }
                            if (CharOperation
                                    .equals(
                                            fullyQualifiedEnclosingTypeOrPackageName,
                                            importFlatName)) {
                                // assume not static import
                                // if(importBinding.isStatic()) {
                                // if((modifiers &
                                // ClassFileConstants.AccStatic) != 0) {
                                // acceptedType.qualifiedTypeName =
                                // typeName;
                                // acceptedType.fullyQualifiedName =
                                // fullyQualifiedName;
                                // onDemandFound.put(
                                // simpleTypeName,
                                // acceptedType);
                                // continue next;
                                // }
                                // } else {
                                acceptedType.qualifiedTypeName = typeName;
                                acceptedType.fullyQualifiedName = fullyQualifiedName;
                                onDemandFound.put(simpleTypeName,
                                        acceptedType);
                                continue next;
                                // }
                            }
                        }
                        proposals.add(proposeType(fullyQualifiedEnclosingTypeOrPackageName != null ? fullyQualifiedEnclosingTypeOrPackageName : packageName,
                                simpleTypeName, modifiers, accessibility,
                                typeName, fullyQualifiedName, true));
                    }
                }
            }
            char[][] keys = onDemandFound.keyTable;
            Object[] values = onDemandFound.valueTable;
            int max = keys.length;
            for (int i = 0; i < max; i++) {
                if ((i % CHECK_CANCEL_FREQUENCY) == 0)
                    checkCancel();
                if (keys[i] != null) {
                    AcceptedType value = (AcceptedType) values[i];
                    if (value != null) {
                        proposals.add(proposeType(value.packageName,
                                value.simpleTypeName, value.modifiers,
                                value.accessibility,
                                value.qualifiedTypeName,
                                value.fullyQualifiedName,
                                value.mustBeQualified));
                    }
                }
            }
        } finally {
            this.acceptedTypes = null; // reset
        }
        return proposals;
    }

    private ICompletionProposal proposeNoImportType(char[] packageName,
            char[] simpleTypeName, int modifiers, int accessibility,
            char[] qualifiedTypeName, char[] fullyQualifiedName,
            boolean isQualified) {
        char[] completionName;
        if (isQualified) {
            completionName = fullyQualifiedName;
        } else {
            completionName = simpleTypeName;
        }

        int relMultiplier = 1;
        relMultiplier += accessibility == IAccessRule.K_ACCESSIBLE ? 3 : 0;
        relMultiplier += (modifiers & Flags.AccDefault) != 0 ? 0 : 1;
        relMultiplier += (modifiers & Flags.AccPrivate) != 0 ? 0 : 1;

        GroovyCompletionProposal proposal = createProposal(CompletionProposal.TYPE_REF, this.actualCompletionPosition - this.offset);
        proposal.setDeclarationSignature(packageName);
        proposal.setSignature(CompletionEngine.createNonGenericTypeSignature(packageName, simpleTypeName));
        proposal.setCompletion(completionName);
        proposal.setFlags(modifiers);
        proposal.setReplaceRange(this.offset, this.offset + this.replaceLength);
        proposal.setTokenRange(this.offset, this.actualCompletionPosition);
        proposal.setRelevance(Relevance.LOWEST.getRelavance(relMultiplier));
        proposal.setNameLookup(nameLookup);
        proposal.setTypeName(simpleTypeName);
        proposal.setAccessibility(accessibility);
        proposal.setPackageName(packageName);
        String completionString = new String(completionName);
        JavaTypeCompletionProposal javaCompletionProposal = new JavaTypeCompletionProposal(
                completionString, null, this.offset, this.replaceLength,
                ProposalUtils.getImage(proposal), ProposalUtils.createDisplayString(proposal),
                5, completionString, javaContext);
        javaCompletionProposal.setRelevance(proposal.getRelevance());

        return javaCompletionProposal;
    }


    private ICompletionProposal proposeType(char[] packageName,
            char[] simpleTypeName, int modifiers, int accessibility,
            char[] qualifiedTypeName, char[] fullyQualifiedName,
            boolean isQualified) {
        return isImport ?
                proposeNoImportType(packageName, simpleTypeName, modifiers, accessibility, qualifiedTypeName, fullyQualifiedName, isQualified) :
                proposeImportableType(packageName, simpleTypeName, modifiers, accessibility, qualifiedTypeName, fullyQualifiedName, isQualified);
    }

    private ICompletionProposal proposeImportableType(char[] packageName,
            char[] simpleTypeName, int modifiers, int accessibility,
            char[] qualifiedTypeName, char[] fullyQualifiedName,
            boolean isQualified) {
        char[] completionName;
        if (isQualified) {
            completionName = fullyQualifiedName;
        } else {
            completionName = simpleTypeName;
        }

        int relMultiplier = 1;
        relMultiplier += accessibility == IAccessRule.K_ACCESSIBLE ? 3 : 0;
        relMultiplier += (modifiers & Flags.AccDefault) != 0 ? 0 : 1;
        relMultiplier += (modifiers & Flags.AccPrivate) != 0 ? 0 : 1;

        GroovyCompletionProposal proposal = createProposal(CompletionProposal.TYPE_REF, this.actualCompletionPosition - this.offset);
        proposal.setDeclarationSignature(packageName);
        proposal.setSignature(CompletionEngine.createNonGenericTypeSignature(packageName, simpleTypeName));
        proposal.setCompletion(completionName);
        proposal.setFlags(modifiers);
        proposal.setReplaceRange(this.offset, this.offset + this.replaceLength);
        proposal.setTokenRange(this.offset, this.actualCompletionPosition);
        proposal.setRelevance(Relevance.LOWEST.getRelavance(relMultiplier));
        proposal.setNameLookup(nameLookup);
        proposal.setTypeName(simpleTypeName);
        proposal.setAccessibility(accessibility);
        proposal.setPackageName(packageName);

        LazyGenericTypeProposal javaCompletionProposal = new LazyGenericTypeProposal(proposal, javaContext);
        javaCompletionProposal.setRelevance(proposal.getRelevance());
        ImportRewrite r = forceImportRewrite();
        if (r != null) {
            ReflectionUtils.setPrivateField(
                    LazyJavaTypeCompletionProposal.class, "fImportRewrite",
                    javaCompletionProposal, r);
        }
        return javaCompletionProposal;
    }

    @SuppressWarnings("deprecation")
    private void initializeImportCaches() {
        importCachesInitialized = true;
        List<String> importPackages = (List<String>) module
                .getImportPackages();
        onDemandimports = new char[importPackages.size()+DEFAULT_GROOVY_ON_DEMAND_IMPORTS.length][];
        int i = 0;
        for (String importPackage : importPackages) {
            onDemandimports[i++] = importPackage.toCharArray();
        }
        for (char[] defaultOnDemand : DEFAULT_GROOVY_ON_DEMAND_IMPORTS) {
            onDemandimports[i++] = defaultOnDemand;
        }

        List<ImportNode> importClasses = module.getImports();
        imports = new char[importClasses.size()+DEFAULT_GROOVY_IMPORTS.length][][];
        i = 0;
        for (ImportNode importNode : importClasses) {
            imports[i] = new char[2][];
            imports[i][0] = importNode.getAlias()
                    .toCharArray();
            imports[i][1] = importNode.getType().getName()
                    .toCharArray();
            i++;
        }
        for (int j = 0; j < DEFAULT_GROOVY_IMPORTS.length; j++) {
            imports[i] = new char[2][];
            imports[i][0] = DEFAULT_GROOVY_IMPORTS_SIMPLE_NAMES[j];
            imports[i][1] = DEFAULT_GROOVY_IMPORTS[j];
            i++;
        }
    }

    List<ICompletionProposal> processAcceptedPackages() {
        this.checkCancel();
        List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
        if (acceptedPackages != null && acceptedPackages.size() > 0) {
            for (String packageNameStr : acceptedPackages) {
                char[] packageName = packageNameStr.toCharArray();
                GroovyCompletionProposal proposal = createProposal(CompletionProposal.PACKAGE_REF, this.actualCompletionPosition);
                proposal.setDeclarationSignature(packageName);
                proposal.setPackageName(packageName);
                proposal.setCompletion(packageName);
                proposal.setReplaceRange(this.offset, this.actualCompletionPosition);
                proposal.setTokenRange(this.offset, this.actualCompletionPosition);
                proposal.setRelevance(Relevance.LOWEST.getRelavance());
                LazyJavaCompletionProposal javaProposal = new LazyJavaCompletionProposal(proposal, javaContext);
                proposals.add(javaProposal);
                javaProposal.setRelevance(proposal.getRelevance());
            }
        }
        return proposals;
    }

    protected final GroovyCompletionProposal createProposal(int kind, int completionOffset) {
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(kind, completionOffset);
        proposal.setNameLookup(nameLookup);
        return proposal;
    }

    /**
     * Returns an import rewrite for the module node only if
     * ModuleNode.encounteredUnrecoverableError()
     *
     * Tries to find the start and end locations of the import statements. Makes
     * a best guess using regular expression. This method ensures that even if
     * the ComplationUnit is unparseable, the imports are still placed in the
     * correct location.
     *
     * @return an {@link ImportRewrite} for the ModuleNode if it encountered an
     *         unrecoverable error, or null if no problems.
     */
    private ImportRewrite forceImportRewrite() {

        if (!module.encounteredUnrecoverableError()) {
            return null;
        }

        if (rewrite == null && !cantCreateRewrite) {

            // find a reasonable substring that contains
            // what looks to be the import dependencies
            CharArraySequence contents = new CharArraySequence(
                    unit.getContents());
            CharArraySequence imports = findImportsRegion(contents);

            // Now send this to a parser
            // need to be very careful here that if we can't parse, then
            // don't send to rewriter
            ASTParser parser = ASTParser.newParser(AST.JLS3);
            parser.setSource(unit.cloneCachingContents(CharOperation.concat(
                    imports.chars(), "\nclass X { }".toCharArray())));
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            ASTNode result = null;
            try {
                result = parser.createAST(monitor);
            } catch (IllegalStateException e) {
                GroovyCore.logException("Can't create ImportRewrite for:\n"
                        + imports, e);
            }
            if (result instanceof CompilationUnit) {
                rewrite = ImportRewrite.create((CompilationUnit) result, true);
            } else {
                // something wierd happened.
                // ensure we don't try again
                cantCreateRewrite = true;
            }
        }

        return rewrite;
    }

    /**
     * Convenience methpd for
     * {@link GroovyProposalTypeSearchRequestor#findImportsRegion(CharArraySequence)}
     */
    static CharArraySequence findImportsRegion(String contents) {
        return findImportsRegion(new CharArraySequence(contents));
    }

    private static final Pattern IMPORTS_PATTERN = Pattern
            .compile("(\\A|[\\n\\r])import\\s");

    private static final Pattern PACKAGE_PATTERN = Pattern
            .compile("(\\A|[\\n\\r])package\\s");

    private static final Pattern EOL_PATTERN = Pattern.compile("($|[\\n\\r])");

    /**
     * Finds a region of text that kind of looks like where the imports should
     * be placed. Uses regular expressions.
     *
     * @param contents
     *            the contents of a compilation unit
     * @return a presumed region
     */
    private static CharArraySequence findImportsRegion(
            CharArraySequence contents) {
        // heuristics:
        // look for last index of ^import
        // if that returns -1, then look for ^package
        Matcher matcher = IMPORTS_PATTERN.matcher(contents);
        int importsEnd = 0;
        while (matcher.find(importsEnd)) {
            importsEnd = matcher.end();
        }

        if (importsEnd == 0) {
            // no imports found, look for package declaration
            matcher = PACKAGE_PATTERN.matcher(contents);
            if (matcher.find()) {
                importsEnd = matcher.end();
            }

        }

        if (importsEnd > 0) {
            // look for end of line
            matcher = EOL_PATTERN.matcher(contents);
            if (matcher.find(importsEnd)) {
                importsEnd = matcher.end();
            }
        }

        return contents.subSequence(0, importsEnd);
    }

    /**
     * Made public for testing
     */
    public static class CharArraySequence implements CharSequence {

        private final char[] chars;

        public CharArraySequence(char[] chars) {
            this.chars = chars;
        }

        public CharArraySequence(String contents) {
            this.chars = contents.toCharArray();
        }

        /**
         * @return
         */
        public char[] chars() {
            return chars;
        }

        public int length() {
            return chars.length;
        }

        /**
         * may throw {@link IndexOutOfBoundsException}
         */
        public char charAt(int index) {
            return chars[index];
        }

        public CharArraySequence subSequence(int start, int end) {
            return new CharArraySequence(CharOperation.subarray(chars, start,
                    end));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                sb.append(chars[i]);
            }
            return sb.toString();
        }
    }
}