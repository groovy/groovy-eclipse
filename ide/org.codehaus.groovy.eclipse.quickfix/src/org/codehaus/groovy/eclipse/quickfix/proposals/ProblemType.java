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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;

import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * A descriptor that represents a Java or Eclipse resource problem (compilation
 * problem, etc..) which the Groovy quick fix framework can understand.
 */
public enum ProblemType {
    FINAL_METHOD_OVERRIDE("Groovy:You are not allowed to override the final method"),
    WEAKER_ACCESS_OVERRIDE("attempting to assign weaker access privileges; was "),
    GROOVY_KEYWORD_TYPE1(IProblem.UndefinedType, "as", "def", "trait"),
    GROOVY_KEYWORD_TYPE2(IProblem.ParsingError, "in", "trait"),
    MISSING_SEMI_COLON_TYPE(IProblem.ParsingErrorInsertToComplete, ";", "}"),
    MISSING_SEMI_COLON_TYPE_VARIANT(IProblem.ParsingErrorInsertTokenAfter), // TODO: Add test case(s) for this
    MISSING_CLASSPATH_CONTAINER_TYPE(IProblem.IsClassPathCorrect, "groovy.lang.GroovyObject", "groovy.lang.MetaClass"),
    MISSING_IMPORTS_TYPE("Groovy:unable to resolve class", " is not an annotation in @", "Groovy:[Static type checking] - The variable "),
    UNIMPLEMENTED_METHODS_TYPE("Can't have an abstract method in a non-abstract class", "Can't have an abstract method in enum constant"),
    STATIC_TYPE_CHECKING_CANNOT_ASSIGN("Groovy:[Static type checking] - Cannot assign value of type", "Groovy:[Static type checking] - Cannot return value of type");

    /**
     * The {@link IMarker} type of the problem.
     */
    public final String markerType;

    /**
     * {@link IProblem} id of problem. Note that problem ids are defined per problem marker type. See
     * {@link org.eclipse.jdt.core.compiler.IProblem} for id definitions for problems of type
     * <code>org.eclipse.jdt.core.problem</code> and <code>org.eclipse.jdt.core.task</code>.
     */
    public final int problemId;

    /**
     * A bit of text that uniquely describes the groovy compiler problem
     * Only necessary because groovy problems do not have a unique id.
     */
    public final String[] groovyProblemSnippets;

    public static final int GROOVY_PROBLEM_ID = 0;

    /** Constructor for groovy problems. Can only be distinguished by */
    ProblemType(String... groovyProblemSnippets) {
        this(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, GROOVY_PROBLEM_ID, groovyProblemSnippets);
    }

    ProblemType(int problemID, String... groovyProblemSnippets) {
        this(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, problemID, groovyProblemSnippets);
    }

    ProblemType(String markerType, int problemId, String... groovyProblemSnippets) {
        this.markerType = markerType;
        this.problemId = problemId;
        this.groovyProblemSnippets = groovyProblemSnippets;
    }

    private boolean matches(int problemId, String markerType, String[] messages) {
        if (this.problemId == problemId && this.markerType.equals(markerType)) {
            if (!asBoolean(groovyProblemSnippets)) {
                // don't care about the snippet; let all problems match
                return true;
            }
            for (String message : messages) {
                for (String groovyProblemSnippet : groovyProblemSnippets) {
                    if (message.contains(groovyProblemSnippet)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ProblemType getProblemType(int problemId, String markerType, String[] messages) {
        for (ProblemType problemType : values()) {
            if (problemType.matches(problemId, markerType, messages)) {
                return problemType;
            }
        }
        return null;
    }

    /**
     * @return true iff the problemId is recognized by at least one of the problem types
     * This not entirely useful since all Groovy problems have the same problemId regardless of
     * whether or not they can be handled by a QuickFix handler
     */
    public static boolean isRecognizedProblemId(int problemId) {
        for (ProblemType problemType : values()) {
            if (problemType.problemId == problemId) {
                return true;
            }
        }
        return false;
    }
}
