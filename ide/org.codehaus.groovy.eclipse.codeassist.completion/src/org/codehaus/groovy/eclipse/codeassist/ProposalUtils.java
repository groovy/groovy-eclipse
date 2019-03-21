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
package org.codehaus.groovy.eclipse.codeassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.completions.NamedArgsMethodNode;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.ui.text.java.CompletionProposalLabelProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class ProposalUtils {

    private ProposalUtils() {}

    // See org.eclipse.jdt.ui.text.java.CompletionProposalCollector

    /** Triggers for method proposals without parameters. Do not modify! */
    public static final char[] METHOD_TRIGGERS = { ';', ',', '.', '[', ' ', '\t' };

    /** Triggers for method proposals. Do not modify! */
    public static final char[] METHOD_WITH_ARGUMENTS_TRIGGERS = { '(', '{', '-', ' ' };

    /** Triggers for types. Do not modify! */
    public static final char[] TYPE_TRIGGERS = { ';', '.', '=', '[', '(', ' ', '\t' };

    /** Triggers for variables. Do not modify! */
    public static final char[] VAR_TRIGGER = new char[] { ';', '.', '=', '[', '(', '{', ' ', '\t' };

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

    public static char[] createTypeSignature(ClassNode node) {
        return createTypeSignatureStr(node).toCharArray();
    }

    public static String createTypeSignatureStr(ClassNode node) {
        if (node == null) {
            node = VariableScope.OBJECT_CLASS_NODE;
        }
        String name = node.getName();
        if (name.startsWith("[")) {
            return name;
        } else {
            return Signature.createTypeSignature(name, true);
        }
    }

    public static String createUnresolvedTypeSignatureStr(ClassNode node) {
        String name = node.getNameWithoutPackage();
        if (name.startsWith("[")) {
            return name;
        } else {
            return Signature.createTypeSignature(name, false);
        }
    }

    /**
     * Can be null if access restriction cannot be resolved for given type.
     */
    public static AccessRestriction getTypeAccessibility(IType type) {
        PackageFragmentRoot root = (PackageFragmentRoot) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        try {
            IClasspathEntry entry = root.getResolvedClasspathEntry();
            // Alternative:
            // entry = ((JavaProject) typeProject).getClasspathEntryFor(root
            // .getPath());
            if (entry instanceof ClasspathEntry) {
                AccessRuleSet accessRuleSet = ((ClasspathEntry) entry).getAccessRuleSet();
                if (accessRuleSet != null) {
                    char[] packageName = type.getPackageFragment().getElementName().toCharArray();
                    char[][] packageChars = CharOperation.splitOn('.', packageName);
                    char[] fileWithoutExtension = type.getElementName().toCharArray();
                    return accessRuleSet.getViolatedRestriction(CharOperation.concatWith(packageChars, fileWithoutExtension, '/'));
                }
            }
        } catch (JavaModelException e) {
        }
        return null;
    }

    /**
     * Includes named params but not optional params.
     */
    public static char[] createMethodSignature(MethodNode node) {
        return createMethodSignatureStr(node, 0).toCharArray();
    }

    /**
     * Includes named params but not optional params.
     */
    public static String createMethodSignatureStr(MethodNode node) {
        return createMethodSignatureStr(node, 0);
    }

    /**
     * Includes named params but not optional params.
     *
     * @param ignoreParameters number of parameters to ignore at the start
     */
    public static char[] createMethodSignature(MethodNode node, int ignoreParameters) {
        return createMethodSignatureStr(node, ignoreParameters).toCharArray();
    }

    /**
     * Includes named params but not optional params.
     *
     * @param ignoreParameters number of parameters to ignore at the start
     */
    public static String createMethodSignatureStr(MethodNode node, int ignoreParameters) {
        String returnType = createTypeSignatureStr(node.getReturnType());
        Parameter[] parameters;
        if (node instanceof NamedArgsMethodNode) {
            parameters = ((NamedArgsMethodNode) node).getVisibleParams();
        } else {
            parameters = node.getParameters();
        }
        String[] parameterTypes = new String[parameters.length - ignoreParameters];
        for (int i = 0, n = parameterTypes.length; i < n; i += 1) {
            parameterTypes[i] = createTypeSignatureStr(parameters[i + ignoreParameters].getType());
        }
        return  node.getName() + Signature.createMethodSignature(parameterTypes, returnType);
    }

    public static char[] createSimpleTypeName(ClassNode node) {
        String name = node.getName();
        if (name.startsWith("[")) {
            int arrayCount = Signature.getArrayCount(name);
            String noArrayName = Signature.getElementType(name);
            String simpleName = Signature.getSignatureSimpleName(noArrayName);
            StringBuilder sb = new StringBuilder();
            sb.append(simpleName);
            for (int i = 0; i < arrayCount; i++) {
                sb.append("[]");
            }
            return sb.toString().toCharArray();
        } else {
            return node.getNameWithoutPackage().toCharArray();
        }
    }

    private static final CompletionProposalLabelProvider labelProvider = new CompletionProposalLabelProvider();

    public static Image getImage(CompletionProposal proposal) {
        return registry.get(labelProvider.createImageDescriptor(proposal));
    }

    public static Image getParameterImage() {
        return registry.get(JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE);
    }

    public static StyledString createDisplayString(CompletionProposal proposal) {
        return labelProvider.createStyledLabel(proposal);
    }

    /**
     * Match ignoring case and checking camel case.
     */
    public static boolean looselyMatches(String prefix, String target) {
        if (target == null || prefix == null) {
            return false;
        }

        // Zero length string matches everything.
        if (prefix.length() == 0) {
            return true;
        }

        // Exclude a bunch right away
        if (prefix.charAt(0) != target.charAt(0)) {
            return false;
        }

        if (target.startsWith(prefix)) {
            return true;
        }

        String lowerCase = target.toLowerCase();
        if (lowerCase.startsWith(prefix)) {
            return true;
        }

        // Test for camel characters in the prefix.
        if (prefix.equals(prefix.toLowerCase())) {
            return false;
        }

        String[] prefixParts = toCamelCaseParts(prefix);
        String[] targetParts = toCamelCaseParts(target);

        if (prefixParts.length > targetParts.length) {
            return false;
        }

        for (int i = 0; i < prefixParts.length; ++i) {
            if (!targetParts[i].startsWith(prefixParts[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts an input string into parts delimited by upper case characters. Used for camel case matches.
     * e.g. GroClaL = ['Gro','Cla','L'] to match say 'GroovyClassLoader'.
     * e.g. mA = ['m','A']
     */
    private static String[] toCamelCaseParts(String str) {
        List<String> parts = new ArrayList<String>();
        for (int i = str.length() - 1; i >= 0; --i) {
            if (Character.isUpperCase(str.charAt(i))) {
                parts.add(str.substring(i));
                str = str.substring(0, i);
            }
        }
        if (str.length() != 0) {
            parts.add(str);
        }
        Collections.reverse(parts);
        return parts.toArray(new String[parts.size()]);
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

    public static boolean hasWhitespace(char[] chars) {
        for (char c : chars) {
            if (CharOperation.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }

    /** Checks '.&' operator before replacement offset. */
    public static boolean isMethodPointerCompletion(IDocument document, int replacementOffset) {
        try {
            boolean seenAmpersand = false;
            while (--replacementOffset > 0) {
                char c = document.getChar(replacementOffset);
                if (Character.isJavaIdentifierPart(c) || (!Character.isWhitespace(c) && c != '&' && c != '.')) break;
                if (c == '&') {
                    if (seenAmpersand) break;
                    seenAmpersand = true;
                } else if (c == '.') {
                    if (seenAmpersand)
                        return true;
                    break;
                }
            }
        } catch (BadLocationException e) {
        }
        return false;
    }
}
