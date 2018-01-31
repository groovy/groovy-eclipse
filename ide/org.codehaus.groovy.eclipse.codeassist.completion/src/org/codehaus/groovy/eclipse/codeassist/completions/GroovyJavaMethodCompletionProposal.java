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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MethodProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.ProposalContextInformation;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

public class GroovyJavaMethodCompletionProposal extends JavaMethodCompletionProposal {

    protected final String contributor;

    protected final ReplacementPreferences preferences;

    public GroovyJavaMethodCompletionProposal(GroovyCompletionProposal proposal, JavaContentAssistInvocationContext context, ProposalFormattingOptions options) {
        this(proposal, context, options, null);
    }

    public GroovyJavaMethodCompletionProposal(GroovyCompletionProposal proposal, JavaContentAssistInvocationContext context, ProposalFormattingOptions options, String contributor) {
        super(proposal, context);

        this.contributor = contributor;
        this.preferences = new ReplacementPreferences(context.getProject(), getFormatterPrefs(), options);

        this.setRelevance(proposal.getRelevance());
        this.setProposalInfo(new MethodProposalInfo(context.getProject(), proposal));
        this.setTriggerCharacters(!proposal.hasParameters() ? ProposalUtils.METHOD_TRIGGERS : ProposalUtils.METHOD_WITH_ARGUMENTS_TRIGGERS);
    }

    protected ImportRewrite fImportRewite;

    public void setImportRewite(ImportRewrite importRewite) {
        fImportRewite = importRewite;
    }

    /** If {@code true}, shows the context only and does not perform completion. */
    protected boolean fContextOnly;

    public void contextOnly() {
        fContextOnly = true;
    }

    // initialized during application
    protected boolean fMethodPointer;
    protected int[] fArgumentOffsets;
    protected int[] fArgumentLengths;
    protected IRegion fSelectedRegion;

    //--------------------------------------------------------------------------

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        fMethodPointer = ProposalUtils.isMethodPointerCompletion(document, getReplacementOffset());
        ReflectionUtils.setPrivateField(LazyJavaCompletionProposal.class, "fReplacementStringComputed", this, Boolean.FALSE);

        if (trigger == '{' && !fContextOnly) {
            String replacement = getReplacementString();
            if (replacement.endsWith("}") || fMethodPointer) {
                trigger = 0; // disable insertion of trailing '{'
            } else if (fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION ||
                    !lastParamAcceptsClosure(Signature.getParameterTypes(fProposal.getSignature()), fInvocationContext)) {
                // prepare for insertion of new block after replacement
                if (preferences.insertSpaceBeforeOpeningBraceInBlock) {
                    setReplacementString(replacement + SPACE);
                }
            } else {
                trigger = 0; // disable insertion of trailing '{'

                if (!preferences.useClosureLiteral || !lastParamIsClosure(Signature.getParameterTypes(fProposal.getSignature()))) {
                    // replace the last argument with a closure literal
                    setReplacementString(recomputeReplacementString());
                }
            }
        }

        super.apply(document, trigger, offset);

        if (fArgumentOffsets != null && fArgumentOffsets.length > 0 && fArgumentLengths != null && fArgumentLengths.length > 0) {
            fSelectedRegion = new Region(getReplacementOffset() + fArgumentOffsets[0], fArgumentLengths[0]);
        } else {
            fSelectedRegion = new Region(getReplacementOffset() + getReplacementString().length(), 0);
        }
    }

    @Override
    public Point getSelection(IDocument document) {
        return (fSelectedRegion == null ? null : new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength()));
    }

    @Override
    protected StyledString computeDisplayString() {
        StyledString displayString = super.computeDisplayString();
        if (contributor != null && !contributor.trim().isEmpty()) {
            displayString.append(new StyledString(" (" + contributor.trim() + ")", StyledString.DECORATIONS_STYLER));
        }
        return displayString;
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
    protected LazyJavaCompletionProposal createRequiredTypeCompletionProposal(CompletionProposal completionProposal, JavaContentAssistInvocationContext invocationContext) {
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
    protected String computeReplacementString() {
        if (fContextOnly) {
            return "";
        }

        char[] proposalName = fProposal.getName();
        boolean hasWhitespace = ProposalUtils.hasWhitespace(proposalName);

        if (fMethodPointer) {
            // complete the name only for a method pointer expression
            return String.valueOf(!hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"'));
        }

        // with no arguments there is nothing groovy to do
        if ((!hasParameters() || !hasArgumentList()) && !hasWhitespace) {
            String replacementString = super.computeReplacementString();
            if (replacementString.endsWith(");")) {
                replacementString = replacementString.substring(0, replacementString.length() - 1);
            }
            return replacementString;
        }

        StringBuffer buffer = new StringBuffer();
        fProposal.setName(!hasWhitespace ? proposalName : CharOperation.concat('"', proposalName, '"'));
        appendMethodNameReplacement(buffer);
        fProposal.setName(proposalName);

        if (!hasParameters()) {
            if (preferences.insertSpaceBetweenEmptyParensInMethodCall) {
                buffer.append(SPACE);
            }
            buffer.append(RPAREN);
        } else {
            int indexOfLastClosure = -1;
            char[][] regularParameterTypes = ((GroovyCompletionProposal) fProposal).getRegularParameterTypeNames();
            char[][] namedParameterTypes = ((GroovyCompletionProposal) fProposal).getNamedParameterTypeNames();
            if (preferences.insertClosureAfterClosingParenInMethodCall) {
                // need to check both regular and named parameters for closure
                if (lastParamIsClosure(regularParameterTypes, namedParameterTypes)) {
                    indexOfLastClosure = regularParameterTypes.length + namedParameterTypes.length - 1;
                }

                // remove the opening paren only if there is a single closure parameter
                if (indexOfLastClosure == 0) {
                    buffer.deleteCharAt(buffer.length() - 1);

                    // add space if not already there would be added by call to appendMethodNameReplacement
                    if (!preferences.insertSpaceBeforeOpeningParenInMethodCall) {
                        buffer.append(SPACE);
                    }
                }
            } else if (preferences.insertSpaceAfterOpeningParenInMethodCall) {
                buffer.append(SPACE);
            }

            // now add the parameters; named parameters go first
            char[][] namedParameterNames = ((GroovyCompletionProposal) fProposal).getNamedParameterNames();
            char[][] regularParameterNames = ((GroovyCompletionProposal) fProposal).getRegularParameterNames();
            int namedCount = namedParameterNames.length, totalCount = regularParameterNames.length + namedCount;

            fArgumentOffsets = new int[totalCount];
            fArgumentLengths = new int[totalCount];

            for (int i = 0; i < totalCount; i += 1) {
                char[] nextName, nextType;
                // check for named args (either all of them, or the explicitly named ones)
                if (i < namedCount) {
                    nextName = namedParameterNames[i];
                    nextType = namedParameterTypes[i];
                } else {
                    nextName = regularParameterNames[i - namedCount];
                    nextType = regularParameterTypes[i - namedCount];
                }

                if ((preferences.useNamedArguments || i < namedCount) && i != indexOfLastClosure) {
                    buffer.append(nextName);
                    if (preferences.insertSpaceBeforeColonInNamedArgument) {
                        buffer.append(SPACE);
                    }
                    buffer.append(":");
                    if (preferences.insertSpaceAfterColonInNamedArgument) {
                        buffer.append(SPACE);
                    }
                }

                if (preferences.useClosureLiteral && CharOperation.equals(nextType, CLOSURE_TYPE_NAME)) {
                    buffer.append("{");
                    if (preferences.insertSpaceAfterOpeningBraceInClosure) {
                        buffer.append(SPACE);
                    }

                    fArgumentOffsets[i] = buffer.length();
                    fArgumentLengths[i] = 2; // select "it"
                    buffer.append("it");

                    if (preferences.insertSpaceBeforeClosingBraceInClosure) {
                        buffer.append(SPACE);
                    }
                    buffer.append("}");
                } else {
                    fArgumentOffsets[i] = buffer.length();
                    fArgumentLengths[i] = nextName.length;
                    buffer.append(nextName);
                }

                if (i == (indexOfLastClosure - 1) || (i != indexOfLastClosure && i == (totalCount - 1))) {
                    if (preferences.insertSpaceBeforeClosingParenInMethodCall) {
                        buffer.append(SPACE);
                    }
                    buffer.append(RPAREN);
                    if (i == (indexOfLastClosure - 1) && (!preferences.useClosureLiteral ||
                            preferences.insertSpaceBeforeOpeningBraceInBlock)) {
                        buffer.append(SPACE);
                    }
                } else if (i < (totalCount - 1)) {
                    if (preferences.insertSpaceBeforeCommaInMethodCallArgs) {
                        buffer.append(SPACE);
                    }
                    buffer.append(COMMA);
                    if (preferences.insertSpaceAfterCommaInMethodCallArgs) {
                        buffer.append(SPACE);
                    }
                }
            }
        }

        return buffer.toString();
    }

    protected String recomputeReplacementString() {
        int last = (fArgumentOffsets.length - 1);
        String head = getReplacementString().substring(0, fArgumentOffsets[last]);
        String tail = getReplacementString().substring(fArgumentOffsets[last] + fArgumentLengths[last]);

        if (preferences.insertClosureAfterClosingParenInMethodCall) {
            if (!tail.isEmpty()) {
                // remove opening paren if there is one param
                if (last == 0) {
                    head = head.substring(0, head.indexOf('('));
                } else {
                    head = head.substring(0, head.lastIndexOf(',')) + tail;
                }
                if (!head.endsWith(SPACE) && preferences.insertSpaceBeforeOpeningBraceInBlock) {
                    head += SPACE;
                }
                tail = "";
            } else if (head.endsWith(SPACE) && !preferences.insertSpaceBeforeOpeningBraceInBlock) {
                head = head.substring(head.length() - 1);
            }
        }

        StringBuffer buffer = new StringBuffer(head);
        buffer.append("{");
        if (preferences.insertSpaceAfterOpeningBraceInClosure) {
            buffer.append(SPACE);
        }

        char[][] paramTypes = Signature.getParameterTypes(fProposal.getSignature());
        if (lastParamIsClosure(paramTypes)) {
            fArgumentOffsets[last] = buffer.length();
            fArgumentLengths[last] = 2; // select "it"
            buffer.append("it");
        } else /* SAM type */ {
            // insert closure params for the abstract method
            try {
                IMethod sam = findSingleAbstractMethod(paramTypes[paramTypes.length - 1]);
                String[] names = sam.getParameterNames();

                int n = names.length;
                for (int i = 0; i < n; i += 1) {
                    if (i > 0) {
                        if (preferences.insertSpaceBeforeCommaInClosureParams) {
                            buffer.append(SPACE);
                        }
                        buffer.append(COMMA);
                        if (preferences.insertSpaceAfterCommaInClosureParams) {
                            buffer.append(SPACE);
                        }
                    }
                    // add position information for each closure argument to enhance linked mode
                    fArgumentLengths = insert(fArgumentLengths, last, names[i].length());
                    fArgumentOffsets = insert(fArgumentOffsets, last, buffer.length());
                    buffer.append(names[i]);
                    last += 1;
                }
                if (n > 0) {
                    buffer.append(SPACE);
                }
            } catch (Exception e) {
                GroovyContentAssist.logError(e);
            }

            buffer.append("->");
            fArgumentLengths[last] = 0;
            fArgumentOffsets[last] = buffer.length();
        }

        if (preferences.insertSpaceBeforeClosingBraceInClosure) {
            buffer.append(SPACE);
        }
        buffer.append("}");
        buffer.append(tail);

        return buffer.toString();
    }

    @Override
    protected boolean needsLinkedMode() {
        return super.needsLinkedMode();
    }

    @Override
    protected void setUpLinkedMode(IDocument document, char closingCharacter) {
        ITextViewer textViewer = getTextViewer();
        if (textViewer != null && fArgumentOffsets != null) {
            int baseOffset = getReplacementOffset();
            String replacement = getReplacementString();
            try {
                LinkedModeModel model = new LinkedModeModel();
                for (int i = 0, n = fArgumentOffsets.length; i < n; i += 1) {
                    LinkedPositionGroup group = new LinkedPositionGroup();
                    group.addPosition(new LinkedPosition(document, baseOffset + fArgumentOffsets[i], fArgumentLengths[i], LinkedPositionGroup.NO_STOP));
                    model.addGroup(group);
                }
                model.forceInstall();

                JavaEditor editor = getJavaEditor();
                if (editor != null) {
                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
                }

                LinkedModeUI ui = new EditorLinkedModeUI(model, textViewer);
                ui.setExitPosition(textViewer, baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
                ui.setExitPolicy(new ExitPolicy(closingCharacter, document));
                ui.setDoContextInfo(true);
                ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
                ui.enter();

                fSelectedRegion = ui.getSelectedRegion();

            } catch (BadLocationException e) {
                GroovyContentAssist.logError(e);
            }
        }
    }

    protected IMethod findSingleAbstractMethod(char[] typeSignature) throws JavaModelException {
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

    //--------------------------------------------------------------------------

    /**
     * Returns the currently active Java editor, or <code>null</code> if it
     * cannot be determined.
     */
    protected static JavaEditor getJavaEditor() {
        IEditorPart part = JavaPlugin.getActivePage().getActiveEditor();
        if (part instanceof JavaEditor) {
            return (JavaEditor) part;
        }
        return null;
    }

    protected static int[] insert(int[] array, int index, int value) {
        final int length = array.length;
        int[] result = new int[length + 1];
        Assert.isTrue(0 <= index && index <= length);
        System.arraycopy(array, 0, result, 0, index);
        result[index] = value;
        if (length > index) {
            System.arraycopy(array, index, result, index + 1, length - index);
        }
        return result;
    }

    protected static boolean lastParamAcceptsClosure(char[][] parameterSignatures, JavaContentAssistInvocationContext context) {
        if (lastParamIsClosure(parameterSignatures)) {
            return true;
        }
        int n = parameterSignatures.length;
        if (n > 0 && context.getCoreContext().isExtended()) {
            char[] lastType = Signature.getTypeErasure(parameterSignatures[n - 1]);
            if (Signature.getArrayCount(lastType) == 0) {
                GroovyExtendedCompletionContext groovyContext = (GroovyExtendedCompletionContext)
                    ReflectionUtils.getPrivateField(InternalCompletionContext.class, "extendedContext", context.getCoreContext());

                return ClassHelper.isSAMType(groovyContext.toClassNode(lastType));
            }
        }
        return false;
    }

    protected static boolean lastParamIsClosure(char[][] parameterSignatures) {
        int n = parameterSignatures.length;
        if (n > 0) {
            char[] lastType = Signature.getTypeErasure(parameterSignatures[n - 1]);
            if (CharOperation.equals(lastType, CLOSURE_TYPE_SIGNATURE)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean lastParamIsClosure(char[][] parameterTypeNames, char[][] namedParameterTypeNames) {
        char[] lastTypeName;
        if (namedParameterTypeNames != null && namedParameterTypeNames.length > 0) {
            lastTypeName = namedParameterTypeNames[namedParameterTypeNames.length - 1];
        } else if (parameterTypeNames != null && parameterTypeNames.length > 0) {
            lastTypeName = parameterTypeNames[parameterTypeNames.length - 1];
        } else { // no args
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

        public ReplacementPreferences(IJavaProject project, FormatterPrefs prefs, ProposalFormattingOptions opts) {
            insertSpaceBetweenEmptyParensInMethodCall = prefs.inEmptyList;
            insertSpaceBeforeOpeningParenInMethodCall = prefs.beforeOpeningParen;
            insertSpaceAfterOpeningParenInMethodCall  = prefs.afterOpeningParen;
            insertSpaceBeforeClosingParenInMethodCall = prefs.beforeClosingParen;
            insertSpaceBeforeCommaInMethodCallArgs    = prefs.beforeComma;
            insertSpaceAfterCommaInMethodCallArgs     = prefs.afterComma;

            useNamedArguments = opts.useNamedArguments;
            // TODO: Add formatter preferences for Named Argument labels.
            insertSpaceBeforeColonInNamedArgument = getCoreOption(project, prefs, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT, false);
            insertSpaceAfterColonInNamedArgument  = getCoreOption(project, prefs, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT,   true);

            useClosureLiteral = opts.useBracketsForClosures;
            insertClosureAfterClosingParenInMethodCall = opts.noParensAroundClosures;
            // TODO: Add formatter preferences for Closure literals, or switch to Lambda prefs if/when they become available.
            // TODO: Add formatter preferences for inserting space before and after Closure arrow, or switch to Lambda prefs if/when they become available.
            insertSpaceAfterOpeningBraceInClosure  = getCoreOption(project, prefs, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER,  true);
            insertSpaceBeforeClosingBraceInClosure = getCoreOption(project, prefs, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, true);
            insertSpaceBeforeOpeningBraceInBlock   = getCoreOption(project, prefs, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK,             true);
            insertSpaceBeforeCommaInClosureParams  = getCoreOption(project, prefs, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER,        false);
            insertSpaceAfterCommaInClosureParams   = getCoreOption(project, prefs, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER,          true);
        }

        // lift from org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal.FormatterPrefs
        protected boolean getCoreOption(IJavaProject prj, FormatterPrefs ref, String key, boolean def) {
            Boolean val = (Boolean) ReflectionUtils.executePrivateMethod(FormatterPrefs.class, "getCoreOption",
                new Class[] {IJavaProject.class, String.class, boolean.class}, ref, new Object[] {prj, key, def});
            return val.booleanValue();
        }

        public final boolean insertSpaceBetweenEmptyParensInMethodCall;
        public final boolean insertSpaceBeforeOpeningParenInMethodCall;
        public final boolean insertSpaceAfterOpeningParenInMethodCall ;
        public final boolean insertSpaceBeforeClosingParenInMethodCall;
        public final boolean insertSpaceBeforeCommaInMethodCallArgs;
        public final boolean insertSpaceAfterCommaInMethodCallArgs;

        public final boolean useNamedArguments;
        public final boolean insertSpaceBeforeColonInNamedArgument;
        public final boolean insertSpaceAfterColonInNamedArgument;

        public final boolean useClosureLiteral;
        public final boolean insertClosureAfterClosingParenInMethodCall;
        public final boolean insertSpaceAfterOpeningBraceInClosure;
        public final boolean insertSpaceBeforeClosingBraceInClosure;
        public final boolean insertSpaceBeforeOpeningBraceInBlock;
        public final boolean insertSpaceBeforeCommaInClosureParams;
        public final boolean insertSpaceAfterCommaInClosureParams;
    }
}
