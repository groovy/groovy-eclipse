 /*
 * Copyright 2003-2011 the original author or authors.
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

package org.codehaus.groovy.eclipse.codeassist;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newEmptyList;

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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class ProposalUtils {

    // Taken from org.eclipse.jdt.ui.text.java.CompletionProposalCollector

    /** Triggers for method proposals without parameters. Do not modify. */
    public final static char[] METHOD_TRIGGERS = new char[] { ';', ',', '.', '\t', '[', ' ' };

    /** Triggers for method proposals. Do not modify. */
    public final static char[] METHOD_WITH_ARGUMENTS_TRIGGERS = new char[] { '(', '-', ' ' };

    /** Triggers for types. Do not modify. */
    // public final static char[] TYPE_TRIGGERS = new char[] { '.', '\t', '[',
    // '(', ' ' };
    // In groovy, types are valid expression, so add all the var triggers as
    // well
    public final static char[] TYPE_TRIGGERS = new char[] { '.', '\t', '[', '(', ' ', '\t', ' ', ';' };

    /** Triggers for variables. Do not modify. */
    public final static char[] VAR_TRIGGER = new char[] { '\t', ' ', '=', ';', '.' };

    public static final List<IGroovyProposal> NO_PROPOSALS = Collections.emptyList();

    public static final ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];

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
    private static final CompletionProposalLabelProvider labelProvider = new CompletionProposalLabelProvider();


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
	 * Can be null if access restriction cannot be resolved for given type
	 *
	 * @param type
	 * @param project
	 * @return
	 */
	public static AccessRestriction getTypeAccessibility(IType type) {

		PackageFragmentRoot root = (PackageFragmentRoot) type
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);

		try {
			IClasspathEntry entry = root.getResolvedClasspathEntry();
			// Alternative:
			// entry = ((JavaProject) typeProject).getClasspathEntryFor(root
			// .getPath());
			if (entry instanceof ClasspathEntry) {
				AccessRuleSet accessRuleSet = ((ClasspathEntry) entry)
						.getAccessRuleSet();
				if (accessRuleSet != null) {
					char[] packageName = type.getPackageFragment()
							.getElementName().toCharArray();
					char[][] packageChars = CharOperation.splitOn('.',
							packageName);
					char[] fileWithoutExtension = type.getElementName()
							.toCharArray();

					return accessRuleSet
							.getViolatedRestriction(CharOperation.concatWith(
									packageChars, fileWithoutExtension, '/'));

				}
			}
		} catch (JavaModelException e) {
			// nothing
		}

		return null;
	}

    /**
     * Includes named params. but not optional params
     *
     * @param node
     * @return
     */
    public static char[] createMethodSignature(MethodNode node) {
        return createMethodSignatureStr(node, 0).toCharArray();
    }

    /**
     * Includes named params. but not optional params
     *
     * @param node
     * @return
     */
    public static String createMethodSignatureStr(MethodNode node) {
        return createMethodSignatureStr(node, 0);
    }

    /**
     * Includes named params. but not optional params
     *
     * @param node
     * @param ignoreParameters number of parameters to ignore at the start
     * @return
     */
    public static char[] createMethodSignature(MethodNode node, int ignoreParameters) {
        return createMethodSignatureStr(node, ignoreParameters).toCharArray();
    }

    /**
     * Includes named params. but not optional params
     *
     * @param node
     * @param ignoreParameters number of parameters to ignore at the start
     * @return
     */
    public static String createMethodSignatureStr(MethodNode node, int ignoreParameters) {
        String returnType = createTypeSignatureStr(node.getReturnType());
        Parameter[] parameters;
        if (node instanceof NamedArgsMethodNode) {
            parameters = ((NamedArgsMethodNode) node).getVisibleParams();
        } else {
            parameters = node.getParameters();
        }
        String[] parameterTypes = new String[parameters.length-ignoreParameters];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = createTypeSignatureStr(parameters[i+ignoreParameters].getType());
        }
        return Signature.createMethodSignature(parameterTypes, returnType);
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
     * @param prefix
     * @param target
     * @return
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
     * Convert an input string into parts delimited by upper case characters. Used for camel case matches.
     * e.g. GroClaL = ['Gro','Cla','L'] to match say 'GroovyClassLoader'.
     * e.g. mA = ['m','A']
     * @param str
     * @return
     */
    private static String[] toCamelCaseParts(String str) {
        List<String> parts = newEmptyList();
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
     * Create a name for a field if this is a getter or a setter method name
     * @param methodName
     * @return
     */
    public static String createMockFieldName(String methodName) {
        int prefix = methodName.startsWith("is") ? 2 : 3;

        return methodName.length() > prefix ? Character.toLowerCase(methodName.charAt(prefix)) + methodName.substring(prefix + 1)
                : "$$$$$";
    }
    /**
     * Create a name for a field if this is a getter or a setter method name The resulting name is
     * capitalized
     * @param methodName
     * @return
     */
    public static String createCapitalMockFieldName(String methodName) {
        return methodName.length() > 3 ? methodName.substring(3) : "$$$$$";
    }
}
