/*
 * Copyright 2009-2023 the original author or authors.
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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.codehaus.jdt.groovy.model.JavaCoreUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.search.matching.DeclarationOfReferencedTypesPattern;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchPattern;
import org.eclipse.jdt.internal.core.search.matching.TypeReferencePattern;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Position;

public class TypeReferenceSearchRequestor implements ITypeRequestor {

    private final SearchRequestor requestor;
    private final SearchParticipant participant;

    private final boolean isCamelCase;
    private final boolean isCaseSensitive;
    private final boolean findDeclaration;

    private final char[] namePattern;
    private final char[] qualificationPattern;

    private final Set<Position> acceptedPositions = new HashSet<>();
    private char[] cachedContents; // see cachedContentsAvailable(IJavaElement)

    public TypeReferenceSearchRequestor(TypeReferencePattern pattern, SearchRequestor requestor, SearchParticipant participant) {
        this.requestor = requestor;
        this.participant = participant;

        this.isCamelCase = ((Boolean) ReflectionUtils.getPrivateField(JavaSearchPattern.class, "isCamelCase", pattern)).booleanValue();
        this.isCaseSensitive = ((Boolean) ReflectionUtils.getPrivateField(JavaSearchPattern.class, "isCaseSensitive", pattern)).booleanValue();
        this.findDeclaration = (pattern instanceof DeclarationOfReferencedTypesPattern);

        this.namePattern = extractArray(pattern, "simpleName");
        this.qualificationPattern = extractArray(pattern, "qualification");
    }

    protected final char[] extractArray(TypeReferencePattern pattern, String fieldName) {
        char[] arr = ReflectionUtils.getPrivateField(TypeReferencePattern.class, fieldName, pattern);
        if (!isCaseSensitive) arr = CharOperation.toLowerCase(arr);
        return arr != null && arr.length > 0 ? arr : null;
    }

    @Override
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (!hasValidSourceLocation(node)) {
            return VisitStatus.CONTINUE;
        }
        int start = -1, until = -1;

        if (node instanceof ImportNode && !findDeclaration && !isCamelCase) {
            String name;
            if (((ImportNode) node).getType() == null) {
                name = ((ImportNode) node).getPackageName();
            } else {
                name = ((ImportNode) node).getType().getName();
            }

            if (qualificationPattern != null && qualifiedNameMatches(name) && cachedContentsAvailable(enclosingElement)) {
                char[] pattern = CharOperation.concat(qualificationPattern, namePattern, '.');
                start = CharOperation.indexOf(pattern, cachedContents, isCaseSensitive, node.getStart(), node.getEnd());
                if (start != -1) { // constrain the matching range
                    start += (qualificationPattern.length + 1);
                    until = (start + namePattern.length);
                }
            }
            // else imports with type != null will have their ClassNode passed separately

        } else if (node instanceof ClassNode || node instanceof ClassExpression || node instanceof AnnotationNode) {

            ClassNode type;
            if (node instanceof ClassExpression) {
                type = ((ClassExpression) node).getType();
            } else if (node instanceof AnnotationNode) {
                type = ((AnnotationNode) node).getClassNode();
            } else /*if (node instanceof ClassNode)*/ {
                type = result.type;
            }
            type = GroovyUtils.getBaseType(type); // remove array wrapper(s)

            if (qualifiedNameMatches(type.getName())) {
                boolean rangeFound = false;

                if (node instanceof ClassExpression) {
                    start = node.getStart();
                    until = node.getEnd();
                } else if (node instanceof AnnotationNode) {
                    start = type.getStart();
                    until = type.getEnd();
                } else /*if (node instanceof ClassNode)*/ {
                    ClassNode classNode = (ClassNode) node;
                    if (classNode.getNameEnd() > 0) {
                        // we are actually dealing with a declaration
                        //start = classNode.getNameStart();
                        //end = classNode.getNameEnd() + 1;
                        //startEndFound = true;
                    } else if (classNode.redirect() == classNode) {
                        // this is a script declaration... ignore
                        rangeFound = true;
                    } else {
                        // ensure classNode has proper source location
                        classNode = maybeGetComponentType(classNode);
                        start = classNode.getStart();
                        until = classNode.getEnd();
                    }
                }

                if (!rangeFound) {
                    // we have a little more work to do before finding the real offset in the text
                    int[] range = getMatchLocation(type, enclosingElement, start, until);
                    if (range != null) {
                        start = range[0];
                        until = range[1];
                    } else {
                        // match really wasn't found
                        start = until = -1;
                    }
                }
            }
        }

        if (start >= 0 && until > 0) {
            // don't double accept nodes; this could happen with field and object
            // initializers that get pushed into multiple constructors
            Position position = new Position(start, until - start);
            if (!acceptedPositions.contains(position)) {
                acceptedPositions.add(position);

                IJavaElement element = enclosingElement;
                if (enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy) {
                    element = ((GroovyClassFileWorkingCopy) enclosingElement.getOpenable()).convertToBinary(enclosingElement);
                }
                try {
                    requestor.acceptSearchMatch(createMatch(result, element, start, until));
                } catch (CoreException e) {
                    Util.log(e, "Error accepting search match for " + element);
                }
            }
        }

        return VisitStatus.CONTINUE;
    }

    protected TypeReferenceMatch createMatch(TypeLookupResult result, IJavaElement enclosingElement, int start, int end) {
        IJavaElement element = enclosingElement;
        if (findDeclaration) {
            // don't use the enclosing element, but rather use the declaration of the type
            IJavaElement type = JavaCoreUtil.findType(GroovyUtils.getBaseType(result.type).getName(), enclosingElement);
            if (type != null) {
                element = type;
            }
        }
        return new TypeReferenceMatch(element, getAccuracy(result.confidence), start, end - start, false, participant, element.getResource());
    }

    private boolean hasValidSourceLocation(ASTNode node) {
        // find the correct ast node that has source locations on it
        // sometimes array nodes do not have source locations, so get around that here.
        ASTNode astNodeWithSourceLocation;
        if (node instanceof ClassNode) {
            astNodeWithSourceLocation = maybeGetComponentType((ClassNode) node);
        } else {
            astNodeWithSourceLocation = node;
        }
        return astNodeWithSourceLocation.getEnd() > 0;
    }

    /**
     * sometimes the underlying component type contains the source location, not the array type
     *
     * @param orig the original class node
     * @return the component type if that type contains source information, otherwise return the original
     */
    private ClassNode maybeGetComponentType(ClassNode orig) {
        if (orig.getComponentType() != null) {
            ClassNode componentType = orig.getComponentType();
            if (componentType.getColumnNumber() != -1) {
                return componentType;
            }
        }
        return orig;
    }

    private String[] splitQualifierAndSimpleName(String fullyQualifiedName) {
        int i = fullyQualifiedName.lastIndexOf('$');
        if (i < 0) i = fullyQualifiedName.lastIndexOf('.');

        return new String[] {
            (i <= 0 ? "" : fullyQualifiedName.substring(0, i).replace('$', '.')),
            fullyQualifiedName.substring(i + 1),
        };
    }

    private boolean qualifiedNameMatches(String fullyQualifiedName) {
        String[] tuple = splitQualifierAndSimpleName(fullyQualifiedName);
        String name = tuple[1], qualifier = tuple[0];
        boolean match = unqualifiedNameMatches(name);

        if (match) {
            if (isCamelCase) {
                match = CharOperation.camelCaseMatch(qualificationPattern, qualifier.toCharArray());
            } else {
                match = CharOperation.match(qualificationPattern, qualifier.toCharArray(), isCaseSensitive);
            }
        }

        // check for complete match within the qualifier
        if (!match && namePattern != null && qualificationPattern != null &&
                qualifier.length() > (namePattern.length + qualificationPattern.length)) {
            char[] q = qualifier.toCharArray();
            // TODO: This doesn't really account for '*' being present in either of the patterns.
            int qualEnd = qualificationPattern.length, nameEnd = qualificationPattern.length + namePattern.length + 1;
            if ((q[qualEnd] == '.' || q[qualEnd] == '$') && (q.length == nameEnd || q[nameEnd] == '.' || q[nameEnd] == '$') &&
                    CharOperation.match(CharOperation.concat(qualificationPattern, namePattern, '?'), q, isCaseSensitive)) {
                match = true;
            }
        }

        return match;
    }

    private boolean unqualifiedNameMatches(String unqualifiedName) {
        boolean match = true;
        if (isCamelCase) {
            match = CharOperation.camelCaseMatch(namePattern, unqualifiedName.toCharArray());
        } else {
            match = CharOperation.match(namePattern, unqualifiedName.toCharArray(), isCaseSensitive);
        }
        return match;
    }

    /**
     * The problem that this method gets around is that we can't tell exactly what the text is and exactly where or if there is a
     * match in the source location. For example, in the text, the type can be fully qualified, or array, or coming from an alias,
     * or a bound type parameter. On top of that, some source locations are off by one. All these will have location relative to the
     * offset provided in the start and end fields of the {@link ClassNode}.
     *
     * @param node the class node to find in the source
     * @param elem the element used to find the text
     * @return the start and end offsets of the actual match, or null if no match exists.
     */
    private int[] getMatchLocation(ClassNode node, IJavaElement elem, int maybeStart, int maybeEnd) {
        if (maybeEnd > 0 && cachedContentsAvailable(elem)) {
            return getMatchLocation0(node.getName(),  maybeStart, maybeEnd);
        }
        return null;
    }

    private int[] getMatchLocation0(String possiblyQualifiedName, int maybeStart, int maybeEnd) {
        // if here, the qualifier and name patterns matched; however it may be an outer type that was matched
        String[] tuple = splitQualifierAndSimpleName(possiblyQualifiedName);

        String simpleName = tuple[1];
        if (simpleName.length() <= (maybeEnd - maybeStart) && unqualifiedNameMatches(simpleName)) {
            int start, until;

            start = CharOperation.indexOf(possiblyQualifiedName.toCharArray(), cachedContents, isCaseSensitive, maybeStart, maybeEnd + 1);
            if (start != -1) {
                until = start + possiblyQualifiedName.length();
            } else {
                start = CharOperation.indexOf(simpleName.toCharArray(), cachedContents, isCaseSensitive, maybeStart, maybeEnd + 1);
                until = start + simpleName.length();
            }

            if (start != -1) {
                return new int[] {start, until};
            }
        } else if (tuple[0].length() > 0) {
            // try again with the name's qualifier
            return getMatchLocation0(tuple[0], maybeStart, maybeEnd);
        }
        return null;
    }

    private boolean cachedContentsAvailable(IJavaElement elem) {
        if (cachedContents == null) {
            CompilationUnit unit = (CompilationUnit) elem.getAncestor(IJavaElement.COMPILATION_UNIT);
            if (unit != null) {
                cachedContents = unit.getContents();
            }
        }
        return (cachedContents != null);
    }

    /**
     * Checks to see if this requestor has something to do with refactoring, if
     * so, we always want an accurate match otherwise we get complaints in the
     * refactoring wizard of "possible matches".
     */
    private boolean shouldAlwaysBeAccurate() {
        return (requestor.getClass().getPackage().getName().indexOf("refactoring") != -1);
    }

    private int getAccuracy(TypeConfidence confidence) {
        return (shouldAlwaysBeAccurate() || confidence == TypeConfidence.EXACT ? SearchMatch.A_ACCURATE : SearchMatch.A_INACCURATE);
    }
}
