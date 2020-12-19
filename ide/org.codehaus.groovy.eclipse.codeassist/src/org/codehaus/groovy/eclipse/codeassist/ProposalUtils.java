/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyExtendedCompletionContext;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.jdt.groovy.ast.MethodNodeWithNamedParams;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalLabelProvider;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class ProposalUtils {

    private ProposalUtils() {}

    public static final char[] ARG_ = {'%'};
    public static final char[] ARG0 = "arg0".toCharArray();
    public static final char[] ARG1 = "arg1".toCharArray();

    // See org.eclipse.jdt.ui.text.java.CompletionProposalCollector

    /** Triggers for method proposals without parameters. Do not modify! */
    public static final char[] METHOD_TRIGGERS = {';', ',', '.', '[', ' ', '\t'};

    /** Triggers for method proposals. Do not modify! */
    public static final char[] METHOD_WITH_ARGUMENTS_TRIGGERS = {'(', '{', '-', ' '};

    /** Triggers for types. Do not modify! */
    public static final char[] TYPE_TRIGGERS = {';', '.', '=', '[', '(', ' ', '\t'};

    /** Triggers for variables. Do not modify! */
    public static final char[] VAR_TRIGGER = new char[] {';', '.', '=', '[', '(', '{', ' ', '\t'};

    public static final ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];
    public static final List<IGroovyProposal> NO_PROPOSALS = Collections.EMPTY_LIST;

    private static ImageDescriptorRegistry registry;
    static {
        try {
            registry = JavaPlugin.getImageDescriptorRegistry();
        } catch (Exception e) {
            // exception in initialization when testing
            e.printStackTrace();
            registry = null;
        }
    }

    public static StyledString createDisplayString(CompletionProposal proposal) {
        return labelProvider.createStyledLabel(proposal);
    }

    /**
     * Creates a name for a field if this is a getter or a setter method name.
     */
    public static String createMockFieldName(String methodName) {
        int prefix = methodName.startsWith("is") ? 2 : 3;

        if (methodName.length() > prefix) {
            /*
             * Check if second character of the field name is upper case and
             * then return the field name without converting first character to
             * lower case.
             */
            if (methodName.length() > prefix + 1 && Character.isUpperCase(methodName.charAt(prefix + 1))) {
                return methodName.substring(prefix);
            } else {
                return Character.toLowerCase(methodName.charAt(prefix)) + methodName.substring(prefix + 1);
            }
        } else {
            return "$$$$$";
        }
    }

    /**
     * Creates a name for a field if this is a getter or a setter method name.
     * The resulting name is capitalized.
     */
    public static String createCapitalMockFieldName(String methodName) {
        return methodName.length() > 3 ? methodName.substring(3) : "$$$$$";
    }

    public static char[] createMethodSignature(MethodNode node) {
        return createMethodSignature(node, 0);
    }

    /**
     * @param ignoreParameters number of parameters to ignore at the start
     */
    public static char[] createMethodSignature(MethodNode node, int ignoreParameters) {
        Parameter[] parameters = (node instanceof MethodNodeWithNamedParams ? ((MethodNodeWithNamedParams) node).getVisibleParams() : node.getParameters());
        char[][] parameterTypes = new char[parameters.length - ignoreParameters][];
        for (int i = 0, n = parameterTypes.length; i < n; i += 1) {
            parameterTypes[i] = createTypeSignature(parameters[i + ignoreParameters].getType());
        }
        char[] returnType = createTypeSignature(node.getReturnType());

        StringBuilder signature = new StringBuilder();
        signature.append(node.getName());
        signature.append(Signature.createMethodSignature(parameterTypes, returnType));

        char[] chars = new char[signature.length()];
        signature.getChars(0, chars.length, chars, 0);
        return chars;
    }

    public static char[] createTypeSignature(ClassNode node) {
        return GroovyUtils.getTypeSignature(node, true, true).toCharArray();
    }

    public static char[] createUnresolvedTypeSignature(ClassNode node) {
        return GroovyUtils.getTypeSignature(node, true, false).toCharArray();
    }

    public static char[] createSimpleTypeName(ClassNode node) {
        if (GroovyUtils.getBaseType(node).isGenericsPlaceHolder()) node = node.redirect();
        // see org.eclipse.jdt.ui.text.java.CompletionProposalLabelProvider#createTypeDisplayName(char[])
        return Signature.getSimpleName(Signature.toCharArray(GroovyUtils.getTypeSignatureWithoutGenerics(node, false, true).toCharArray()));
    }

    public static Optional<ContentAssistContext> getContentAssistContext(JavaContentAssistInvocationContext javaContext) {
        if (javaContext.getCoreContext().isExtended()) {
            GroovyExtendedCompletionContext groovyContext = ReflectionUtils.getPrivateField(
                InternalCompletionContext.class, "extendedContext", javaContext.getCoreContext());

            ContentAssistContext context = ReflectionUtils.getPrivateField(
                GroovyExtendedCompletionContext.class, "context", groovyContext);

            return Optional.ofNullable(context);
        }
        return Optional.empty();
    }

    private static final CompletionProposalLabelProvider labelProvider = new CompletionProposalLabelProvider();

    public static Image getImage(CompletionProposal proposal) {
        return registry.get(labelProvider.createImageDescriptor(proposal));
    }

    public static Image getParameterImage() {
        return registry.get(JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE);
    }

    public static char[][] getParameterNames(Parameter[] parameters) {
        int n = parameters.length;
        if (n > 0) {
            char[][] names = new char[n][];
            for (int i = 0; i < n; i += 1) {
                names[i] = parameters[i].getName().toCharArray();
            }
            return names;
        }
        return CharOperation.NO_CHAR_CHAR;
    }

    public static char[][] getParameterTypeNames(Parameter[] parameters) {
        int n = parameters.length;
        if (n > 0) {
            char[][] names = new char[n][];
            for (int i = 0; i < n; i += 1) {
                names[i] = Signature.toCharArray(Signature.getTypeErasure(createTypeSignature(parameters[i].getType())));
            }
            return names;
        }
        return CharOperation.NO_CHAR_CHAR;
    }

    /**
     * Can be null if access restriction cannot be resolved for given type.
     */
    public static AccessRestriction getTypeAccessibility(IType type) {
        PackageFragmentRoot root = (PackageFragmentRoot) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        try {
            IClasspathEntry entry = root.getResolvedClasspathEntry();
            // Alternative:
            // entry = ((JavaProject) typeProject).getClasspathEntryFor(root.getPath());
            if (entry instanceof ClasspathEntry) {
                AccessRuleSet accessRuleSet = ((ClasspathEntry) entry).getAccessRuleSet();
                if (accessRuleSet != null) {
                    char[] packageName = type.getPackageFragment().getElementName().toCharArray();
                    char[][] packageChars = CharOperation.splitOn('.', packageName);
                    char[] fileWithoutExtension = type.getElementName().toCharArray();
                    return accessRuleSet.getViolatedRestriction(CharOperation.concatWith(packageChars, fileWithoutExtension, '/'));
                }
            }
        } catch (JavaModelException ignore) {
        }
        return null;
    }

    public static boolean hasWhitespace(char[] chars) {
        for (char c : chars) {
            if (CharOperation.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isContentAssistAutoActiavted() { // TODO: Determine if current invocation was auto-activated
        return JavaPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION);
    }

    /**
     * Match ignoring case and checking camel case.
     */
    public static boolean looselyMatches(String prefix, String target) {
        return matches(prefix, target, true, false);
    }

    public static boolean matches(String pattern, String candidate, boolean camelCaseMatch, boolean substringMatch) {
        if (pattern.isEmpty()) {
            return true;
        }
        if (camelCaseMatch && SearchPattern.camelCaseMatch(pattern, candidate)) {
            return true;
        }
        return substringMatch ? CharOperation.substringMatch(pattern, candidate) : candidate.startsWith(pattern);
    }
}
