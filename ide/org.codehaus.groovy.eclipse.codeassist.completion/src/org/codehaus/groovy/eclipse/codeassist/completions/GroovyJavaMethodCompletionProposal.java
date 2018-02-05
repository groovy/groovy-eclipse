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
package org.codehaus.groovy.eclipse.codeassist.completions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.ProposalContextInformation;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

public class GroovyJavaMethodCompletionProposal extends JavaMethodCompletionProposal {

    public GroovyJavaMethodCompletionProposal(CompletionProposal proposal, ProposalFormattingOptions options, JavaContentAssistInvocationContext context, String contributor) {
        super(proposal, context);
        fContributor = (contributor == null ? "" : contributor.trim());
        fPreferences = new ReplacementPreferences(options, getFormatterPrefs(), context.getProject());
    }

    protected final String fContributor;
    protected final ReplacementPreferences fPreferences;

    public final void setImportRewite(ImportRewrite importRewite) {
        fImportRewite = importRewite;
    }
    protected ImportRewrite fImportRewite;

    // initialized during application:
    protected IRegion fSelectedRegion;
    protected String fPositionCategory;
    protected List<Position> fPositions;
    private   IPositionUpdater fUpdater;
    protected List<ICompletionProposal[]> fProposals;

    //--------------------------------------------------------------------------

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        try {
            if (trigger == '{' && (fProposal.getCompletion() != null && fProposal.getCompletion().length > 0)) {
                String replacement = getReplacementString();
                if (replacement.endsWith("}") || fProposal.getKind() == CompletionProposal.METHOD_NAME_REFERENCE) {
                    trigger = 0; // disable insertion of trailing '{'

                } else if (fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION ||
                        !fPreferences.isEnabled(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES) ||
                        !lastParamAcceptsClosure(Signature.getParameterTypes(fProposal.getSignature()))) {
                    // prepare for insertion of new block after replacement
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK)) {
                        setReplacementString(replacement + SPACE);
                    }
                } else {
                    // replace the last argument with a closure literal
                    setReplacementString(recomputeReplacementString());
                    trigger = 0; // disable insertion of trailing '{'
                }
            }

            super.apply(document, trigger, offset);

            if (fContextInformationPosition > 0) {
                // change offset from relative to absolute
                setContextInformationPosition(getReplacementOffset() + fContextInformationPosition);

                // coordinate editor selection with context display and linking mode
                if (fPositions == null || fPositions.isEmpty()) {
                    fSelectedRegion = new Region(fContextInformationPosition, 0);
                } else {
                    fSelectedRegion = new Region(fPositions.get(0).getOffset(), fPositions.get(0).getLength());
                }
            }
        } catch (Exception e) {
            GroovyContentAssist.logError(e);
            ensurePositionCategoryRemoved(document);
        }
    }

    @Override
    protected IContextInformation computeContextInformation() {
        if (hasParameters() && (fProposal.getKind() == CompletionProposal.METHOD_REF ||
                                fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION)) {
            ProposalContextInformation contextInformation = new ProposalContextInformation(fProposal);
            if (fContextInformationPosition != 0 && fProposal.getCompletion().length == 0)
                contextInformation.setContextInformationPosition(fContextInformationPosition);
            return contextInformation;
        }
        return super.computeContextInformation();
    }

    @Override
    protected final StyledString computeDisplayString() {
        StyledString displayString = super.computeDisplayString();
        if (!fContributor.isEmpty()) {
            displayString.append(new StyledString(" (" + fContributor + ")", StyledString.DECORATIONS_STYLER));
        }
        return displayString;
    }

    @Override
    protected ProposalInfo computeProposalInfo() {
        return new MethodProposalInfo(fInvocationContext.getProject(), fProposal);
    }

    @Override
    protected int computeRelevance() {
        return fProposal.getRelevance();
    }

    @Override
    protected String computeReplacementString() {
        if (fProposal.getCompletion() == null || fProposal.getCompletion().length == 0) {
            return "";
        }

        char[] proposalName = fProposal.getName();
        boolean hasWhitespace = ProposalUtils.hasWhitespace(proposalName);

        if (fProposal.getKind() == CompletionProposal.METHOD_NAME_REFERENCE) {
            // complete the name only for a method pointer or static import expression
            return String.valueOf(!hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"'));
        }

        // if no whitespace in the method name and no arguments, there is nothing groovy to do
        if (!hasWhitespace && (!hasParameters() || !hasArgumentList())) {
            String replacementString = super.computeReplacementString();
            if (replacementString.endsWith(");")) {
                replacementString = replacementString.substring(0, replacementString.length() - 1);
            }
            return replacementString;
        }

        //
        StringBuffer buffer = new StringBuffer();

        fProposal.setName(!hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"'));
        appendMethodNameReplacement(buffer);
        fProposal.setName(proposalName);

        if (!hasParameters()) {
            while (Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
            if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION)) {
                buffer.append(SPACE);
            }
            buffer.append(RPAREN);

        } else if (!fPreferences.isEnabled(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES)) {
            if (fPreferences.bCommandChaining) {
                int i = buffer.lastIndexOf(LPAREN);
                while (Character.isWhitespace(buffer.charAt(i - 1))) {
                    i -= 1;
                }
                buffer.replace(i, buffer.length(), SPACE);
            } else if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION)) {
                buffer.append(SPACE);
            }

            setContextInformationPosition(buffer.length());

            if (!fPreferences.bCommandChaining)
                buffer.append(RPAREN);

        } else {
            int indexOfLastClosure = -1;
            char[][] namedParameterTypes = ((GroovyCompletionProposal) fProposal).getNamedParameterTypeNames();
            char[][] regularParameterTypes = ((GroovyCompletionProposal) fProposal).getRegularParameterTypeNames();

            if (fPreferences.bCommandChaining) {
                int i = buffer.lastIndexOf(LPAREN);
                while (Character.isWhitespace(buffer.charAt(i - 1))) {
                    i -= 1;
                }
                buffer.replace(i, buffer.length(), SPACE);
            } else if (fPreferences.isEnabled(GroovyContentAssist.CLOSURE_NOPARENS)) {
                // need to check both regular and named parameters for closure
                if (lastParamIsClosure(regularParameterTypes, CharOperation.NO_CHAR_CHAR)) {
                    indexOfLastClosure = namedParameterTypes.length + regularParameterTypes.length - 1;
                }

                // remove the opening paren only if there is a single closure parameter
                if (indexOfLastClosure == 0) {
                    buffer.deleteCharAt(buffer.length() - 1);

                    // add space if not already there would be added by call to appendMethodNameReplacement
                    if (!fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION)) {
                        buffer.append(SPACE);
                    }
                }
            } else if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION)) {
                buffer.append(SPACE);
            }

            setContextInformationPosition(buffer.length());

            // add the parameters; named parameters precede positional parameters
            char[][] namedParameterNames = ((GroovyCompletionProposal) fProposal).getNamedParameterNames();
            char[][] regularParameterNames = ((GroovyCompletionProposal) fProposal).getRegularParameterNames();
            int namedCount = namedParameterNames.length, totalCount = regularParameterNames.length + namedCount;

            // initialize fProposals and fPositions
            computeReplacementProposals(namedParameterNames, regularParameterNames, indexOfLastClosure);

            for (int i = 0; i < totalCount; i += 1) {
                @SuppressWarnings("unused")
                char[] nextName, nextType;
                if (i < namedCount) {
                    nextName = namedParameterNames[i];
                    nextType = namedParameterTypes[i];
                } else {
                    nextName = regularParameterNames[i - namedCount];
                    nextType = regularParameterTypes[i - namedCount];
                }
                String nextValue = fProposals.get(i)[0].getDisplayString();

                if ((fPreferences.isEnabled(GroovyContentAssist.NAMED_ARGUMENTS) || i < namedCount) && i != indexOfLastClosure) {
                    buffer.append(nextName);
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT)) {
                        buffer.append(SPACE);
                    }
                    buffer.append(":");
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT)) {
                        buffer.append(SPACE);
                    }
                }

                fPositions.get(i).setLength(nextValue.length());
                fPositions.get(i).setOffset(buffer.length());
                buffer.append(nextValue);

                if (i == (indexOfLastClosure - 1) || (i != indexOfLastClosure && i == (totalCount - 1) && !fPreferences.bCommandChaining)) {
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION)) {
                        buffer.append(SPACE);
                    }
                    buffer.append(RPAREN);
                    if (i == (indexOfLastClosure - 1) && (!fPreferences.isEnabled(GroovyContentAssist.CLOSURE_BRACKETS) ||
                            fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK))) {
                        buffer.append(SPACE);
                    }
                } else if (i < (totalCount - 1)) {
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS)) {
                        buffer.append(SPACE);
                    }
                    buffer.append(COMMA);
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS)) {
                        buffer.append(SPACE);
                    }
                }
            }
        }

        return buffer.toString();
    }

    protected String recomputeReplacementString() throws JavaModelException {
        int last = (fPositions.size() - 1);
        // disable guessing for literal
        fProposals.remove(last);

        String head = getReplacementString().substring(0, fPositions.get(last).getOffset());
        String tail = getReplacementString().substring((fPositions.get(last).getOffset()) + fPositions.get(last).getLength());

        if (fPreferences.isEnabled(GroovyContentAssist.CLOSURE_NOPARENS)) {
            if (!tail.isEmpty()) {
                // remove opening paren if there is one param
                if (last == 0) {
                    head = head.substring(0, head.indexOf('('));
                } else {
                    head = head.substring(0, head.lastIndexOf(',')) + tail;
                }
                if (!head.endsWith(SPACE) && fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK)) {
                    head += SPACE;
                }
                tail = "";
            } else if (head.endsWith(SPACE) && !fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK)) {
                head = head.substring(head.length() - 1);
            }
        }

        StringBuffer buffer = new StringBuffer(head);
        buffer.append("{");
        if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER)) {
            buffer.append(SPACE);
        }

        char[][] paramTypes = Signature.getParameterTypes(fProposal.getSignature());
        if (lastParamIsClosure(paramTypes)) {
            fPositions.get(last).setLength(2); // select "it"
            fPositions.get(last).setOffset(buffer.length());
            buffer.append("it");
        } else {
            // insert closure params for the abstract method
            IMethod sam = findSingleAbstractMethod(paramTypes[paramTypes.length - 1]);
            String[] names = sam.getParameterNames();

            int n = names.length;
            for (int i = 0; i < n; i += 1) {
                if (i > 0) {
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER)) {
                        buffer.append(SPACE);
                    }
                    buffer.append(COMMA);
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER)) {
                        buffer.append(SPACE);
                    }
                }
                // add position information for each closure argument to enhance linked mode
                fPositions.add(last, new Position(buffer.length(), names[i].length()));
                buffer.append(names[i]);
                last += 1;
            }
            if (n > 0) {
                buffer.append(SPACE);
            }

            buffer.append("->");
            fPositions.get(last).setLength(0);
            fPositions.get(last).setOffset(buffer.length());
        }

        if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER)) {
            buffer.append(SPACE);
        }
        buffer.append("}");
        buffer.append(tail);

        return buffer.toString();
    }

    @Override
    protected LazyJavaCompletionProposal createRequiredTypeCompletionProposal(
            CompletionProposal completionProposal, JavaContentAssistInvocationContext invocationContext) {
        LazyJavaCompletionProposal requiredProposal = super.createRequiredTypeCompletionProposal(completionProposal, invocationContext);
        if (fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION && requiredProposal instanceof LazyJavaTypeCompletionProposal) {
            if (fImportRewite != null) {
                ReflectionUtils.setPrivateField(LazyJavaTypeCompletionProposal.class, "fImportRewrite", requiredProposal, fImportRewite);
            }
            if (requiredProposal instanceof LazyGenericTypeProposal) {
                // disable generics completion for constructors (i.e. complete expression as "new ArrayList()" instead of "new ArrayList<E>()"
                try {
                    //requiredProposal.fTypeArgumentProposals = new LazyGenericTypeProposal.TypeArgumentProposal[0];
                    ReflectionUtils.setPrivateField(LazyGenericTypeProposal.class, "fTypeArgumentProposals", requiredProposal,
                        Array.newInstance(Class.forName("org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal$TypeArgumentProposal"), 0));
                } catch (Exception e) {
                    GroovyContentAssist.logError(e);
                }
            }
        }
        return requiredProposal;
    }

    @Override
    protected char[] computeTriggerCharacters() {
        if (fProposal instanceof GroovyCompletionProposal) {
            boolean hasParameters = ((GroovyCompletionProposal) fProposal).hasParameters();
            return (!hasParameters ? ProposalUtils.METHOD_TRIGGERS : ProposalUtils.METHOD_WITH_ARGUMENTS_TRIGGERS);
        }
        return super.computeTriggerCharacters();
    }

    @Override
    public int getContextInformationPosition() {
        return fContextInformationPosition;
    }

    @Override
    public Point getSelection(IDocument document) {
        if (fSelectedRegion != null) {
            return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
        }
        return super.getSelection(document);
    }

    @Override
    protected void setUpLinkedMode(IDocument document, char closingCharacter) {
        if (getTextViewer() != null) {
            try {
                int baseOffset = getReplacementOffset();
                LinkedModeModel model = new LinkedModeModel();
                if (fPositions == null) {
                    LinkedPositionGroup group = new LinkedPositionGroup();
                    group.addPosition(new LinkedPosition(document, baseOffset + fContextInformationPosition, 0));
                    model.addGroup(group);
                } else {
                    for (int i = 0, n = fPositions.size(); i < n; i += 1) {
                        Position position = fPositions.get(i);
                        // change offset from relative to absolute
                        position.setOffset(baseOffset + position.getOffset());
                        LinkedPositionGroup group = new LinkedPositionGroup();
                        if (fProposals.size() <= i || fProposals.get(i).length <= 1) {
                            group.addPosition(new LinkedPosition(document, position.getOffset(), position.getLength(), LinkedPositionGroup.NO_STOP));
                        } else {
                            ensurePositionCategoryInstalled(document, model);
                            document.addPosition(fPositionCategory, position);
                            group.addPosition(new ProposalPosition(document, position.getOffset(), position.getLength(), LinkedPositionGroup.NO_STOP, fProposals.get(i)));
                        }
                        model.addGroup(group);
                    }
                }
                JavaEditor editor = getJavaEditor();
                if (editor != null) {
                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
                }
                model.forceInstall();

                LinkedModeUI ui = new EditorLinkedModeUI(model, getTextViewer());
                ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER/*WHEN_NO_PARENT*/);
                ui.setDoContextInfo(true); // displays above the argument list
                ui.setExitPolicy(new ExitPolicy(closingCharacter, document));
                ui.setExitPosition(getTextViewer(), baseOffset + getCursorPosition(), 0, Integer.MAX_VALUE);
                ui.enter();

            } catch (BadLocationException | BadPositionCategoryException e) {
                GroovyContentAssist.logError(e);
            }
        }
    }

    //--------------------------------------------------------------------------

    protected void computeReplacementProposals(char[][] namedParameterNames, char[][] positionalParameterNames, int indexOfLastClosure) {
        boolean guess = (fInvocationContext.getCoreContext().isExtended() &&
            fPreferences.isEnabled(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS));
        char[][] parameterTypes = Signature.getParameterTypes(SignatureUtil.fix83600(fProposal.getSignature()));

        int npc = namedParameterNames.length, n = npc + positionalParameterNames.length;

        fPositions = new ArrayList<>(n);
        fProposals = new ArrayList<>(n);

        for (int i = 0; i < n; i += 1) {
            fPositions.add(new Position(0));

            char[] name = (i < npc ? namedParameterNames[i] : positionalParameterNames[i - npc]);
            // NOTE: named parameters come after positional parameters in the parameterTypes array
            char[] type = (i < npc ? parameterTypes[i + positionalParameterNames.length] : parameterTypes[i - npc]);

            ICompletionProposal[] vals;
            if (guess) {
                boolean fillBestGuess = true;
                String typeSignature = String.valueOf(type);
                IJavaElement[] visibleElements = fInvocationContext.getCoreContext().getVisibleElements(typeSignature);

                vals = new ParameterGuesserDelegate(getEnclosingElement(), fInvocationContext).parameterProposals(
                    Signature.toString(typeSignature), String.valueOf(name), fPositions.get(i), visibleElements, fillBestGuess);
            } else {
                StringBuilder buffer = new StringBuilder();

                if (fPreferences.isEnabled(GroovyContentAssist.CLOSURE_BRACKETS) && CharOperation.equals(type, CLOSURE_TYPE_SIGNATURE)) {
                    buffer.append("{");
                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER)) {
                        buffer.append(SPACE);
                    }

                    buffer.append("it");

                    if (fPreferences.isEnabled(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER)) {
                        buffer.append(SPACE);
                    }
                    buffer.append("}");
                } else {
                    buffer.append(name);
                }

                vals = new ICompletionProposal[] {
                    new JavaCompletionProposal(buffer.toString(), 0, buffer.length(), null, buffer.toString(), 1)
                };
            }

            fProposals.add(vals);
        }
    }

    private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
        fPositionCategory = "ParameterGuessingProposal_" + toString();
        if (!document.containsPositionCategory(fPositionCategory)) {
            fUpdater = new InclusivePositionUpdater(fPositionCategory);
            document.addPositionCategory(fPositionCategory);
            document.addPositionUpdater(fUpdater);

            model.addLinkingListener(new ILinkedModeListener() {
                @Override
                public void left(LinkedModeModel environment, int flags) {
                    ensurePositionCategoryRemoved(document);
                }

                @Override
                public void suspend(LinkedModeModel environment) {
                }

                @Override
                public void resume(LinkedModeModel environment, int flags) {
                }
            });
        }
    }

    private void ensurePositionCategoryRemoved(IDocument document) {
        if (document.containsPositionCategory(fPositionCategory)) {
            try {
                document.removePositionCategory(fPositionCategory);
            } catch (BadPositionCategoryException e) {
                // ignore
            }
            document.removePositionUpdater(fUpdater);
        }
        fUpdater = null;
    }

    protected final IMethod findSingleAbstractMethod(char[] typeSignature) throws JavaModelException {
        char[] name = CharOperation.concat(Signature.getSignatureQualifier(typeSignature), Signature.getSignatureSimpleName(typeSignature), '.');
        IType type = getJavaElement().getJavaProject().findType(String.valueOf(name));
        if (type.exists()) {
            for (IMethod m : type.getMethods()) {
                if (Flags.isAbstract(m.getFlags())) {
                    return m;
                }
            }
        }
        return null;
    }

    protected final IJavaElement getEnclosingElement() {
        if (fInvocationContext.getCoreContext().isExtended()) {
            return fInvocationContext.getCoreContext().getEnclosingElement();
        }
        return null;
    }

    /**
     * @returns currently active Java editor, or {@code null} if not available
     */
    protected final JavaEditor getJavaEditor() {
        IEditorPart part = JavaPlugin.getActivePage().getActiveEditor();
        if (part instanceof JavaEditor) {
            return (JavaEditor) part;
        }
        return null;
    }

    protected final boolean lastParamAcceptsClosure(char[][] parameterSignatures) {
        if (lastParamIsClosure(parameterSignatures)) {
            return true;
        }
        int n = parameterSignatures.length;
        if (n > 0 && fInvocationContext.getCoreContext().isExtended()) {
            char[] lastType = Signature.getTypeErasure(parameterSignatures[n - 1]);
            if (Signature.getArrayCount(lastType) == 0) {
                GroovyExtendedCompletionContext groovyContext = (GroovyExtendedCompletionContext)
                    ReflectionUtils.getPrivateField(InternalCompletionContext.class, "extendedContext", fInvocationContext.getCoreContext());

                return ClassHelper.isSAMType(groovyContext.toClassNode(lastType));
            }
        }
        return false;
    }

    protected final boolean lastParamIsClosure(char[][] parameterSignatures) {
        int n = parameterSignatures.length;
        if (n > 0) {
            char[] lastType = Signature.getTypeErasure(parameterSignatures[n - 1]);
            if (CharOperation.equals(lastType, CLOSURE_TYPE_SIGNATURE)) {
                return true;
            }
        }
        return false;
    }

    protected final boolean lastParamIsClosure(char[][] parameterTypeNames,
            char[][] namedParameterTypeNames) {
        char[] lastTypeName;
        if (parameterTypeNames != null && parameterTypeNames.length > 0) {
            lastTypeName = parameterTypeNames[parameterTypeNames.length - 1];
        }/* else if (namedParameterTypeNames != null && namedParameterTypeNames.length > 0) {
            lastTypeName = namedParameterTypeNames[namedParameterTypeNames.length - 1];
        }*/ else {
            return false;
        }
        // we should be comparing against a fully qualified type name, but it is not always available so a simple name is close enough
        return CharOperation.equals(lastTypeName, CLOSURE_TYPE_NAME);
    }

    protected static final char[] CLOSURE_TYPE_NAME = "Closure".toCharArray();
    protected static final char[] CLOSURE_TYPE_SIGNATURE = "Lgroovy.lang.Closure;".toCharArray();

    //--------------------------------------------------------------------------

    /**
     * @see org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions
     * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal.FormatterPrefs
     */
    protected static class ReplacementPreferences {

        public ReplacementPreferences(ProposalFormattingOptions opts, FormatterPrefs prefs, IJavaProject project) {

            bCommandChaining = opts.noParens; // no preference exists for this
            cache.put(GroovyContentAssist.NAMED_ARGUMENTS, opts.useNamedArguments);
            cache.put(GroovyContentAssist.CLOSURE_BRACKETS, opts.useBracketsForClosures);
            cache.put(GroovyContentAssist.CLOSURE_NOPARENS, opts.noParensAroundClosures);

            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION, prefs.beforeOpeningParen);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION, prefs.afterOpeningParen);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS, prefs.beforeComma);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS, prefs.afterComma);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION, prefs.beforeClosingParen);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION, prefs.inEmptyList);

            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_PARAMETERIZED_TYPE_REFERENCE, prefs.beforeTypeArgumentComma);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_PARAMETERIZED_TYPE_REFERENCE, prefs.afterTypeArgumentComma);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_PARAMETERIZED_TYPE_REFERENCE, prefs.beforeOpeningBracket);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_PARAMETERIZED_TYPE_REFERENCE, prefs.afterOpeningBracket);
            cache.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_PARAMETERIZED_TYPE_REFERENCE, prefs.beforeClosingBracket);

            IPreferenceStore uiPrefs = JavaPlugin.getDefault().getPreferenceStore(); boolean fillArgs;
            cache.put(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, (fillArgs = uiPrefs.getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES)));
            cache.put(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, (fillArgs && uiPrefs.getBoolean(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS)));

            computer = key -> {
                boolean def = getDefaultOptions().get(key).matches(JavaCore.ENABLED + "|" + JavaCore.INSERT);
                Boolean val = (Boolean) ReflectionUtils.executePrivateMethod(FormatterPrefs.class, "getCoreOption",
                    new Class[] {IJavaProject.class, String.class, boolean.class}, prefs, new Object[] {project, key, def});
                return val;
            };
        }

        public final boolean bCommandChaining;

        private final Map<String, Boolean> cache = new HashMap<>(32);
        private final Function<String, Boolean> computer;
        private Map<String, String> defaults;

        private Map<String, String> getDefaultOptions() {
            if (defaults == null) {
                defaults = JavaCore.getDefaultOptions();
            }
            return defaults;
        }

        public final boolean isEnabled(String key) {
            return cache.computeIfAbsent(key, computer).booleanValue();
        }
    }

    /**
     * Not API; for testing only!
     *
     * @return the guessed parameter proposals
     */
    public List<ICompletionProposal[]> getChoices() {
        return fProposals;
    }
}
