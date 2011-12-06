/*
 * Copyright 2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.codehaus.groovy.eclipse.quickfix.proposals;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * A descriptor that represents a Java or Eclipse resource problem (compilation problem, etc..)
 * which the Groovy quick fix framework can understand.
 * 
 * @author Nieraj Singh
 * @author Andrew Eisenberg
 */
public enum ProblemType {
    // missing semi-colons will have different IProblem values in different places
    MISSING_SEMI_COLON_TYPE(IProblem.ParsingErrorInsertToComplete, (String[]) null),
    MISSING_SEMI_COLON_TYPE_VARIANT(IProblem.ParsingErrorInsertTokenAfter, (String[]) null),
    
    MISSING_IMPORTS_TYPE("Groovy:unable to resolve class"), 
    UNIMPLEMENTED_METHODS_TYPE("Groovy:Can't have an abstract method in a non-abstract class."), 
    MISSING_CLASSPATH_CONTAINER_TYPE(IProblem.IsClassPathCorrect, "groovy.lang.GroovyObject", "groovy.lang.MetaClass");

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
    public final String groovyProblemSnippets[];

    public static final int GROOVY_PROBLEM_ID = 0;
    
    /** Constructor for groovy problems. Can only be distinguished by */
    private ProblemType(String ... groovyProblemSnippets) {
        this(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
                GROOVY_PROBLEM_ID, groovyProblemSnippets);
    }

    private ProblemType(int problemID, String ... groovyProblemSnippets) {
        this(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, problemID,
                groovyProblemSnippets);
    }

    private ProblemType(String markerType, int problemID,
            String ... groovyProblemSnippets) {
        this.markerType = markerType;
        this.problemId = problemID;
        this.groovyProblemSnippets = groovyProblemSnippets;
    }

    private boolean matches(int problemID, String markerType, String[] messages) {
        if (this.problemId == problemID && this.markerType.equals(markerType)) {
            if (groovyProblemSnippets == null) {
                // we don't care about the snippet. let all problems match
                return true;
            }
            for (String message : messages) {
                for (String groovyProblemSnippet : groovyProblemSnippets) {
                    if (message != null && message.contains(groovyProblemSnippet)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ProblemType getProblemType(int problemID,
            String markerType, String[] messages) {
        for (ProblemType problemType : ProblemType.values()) {
            if (problemType.matches(problemID, markerType, messages)) {
                return problemType;
            }
        }
        return null;
    }

    /**
     * @param problemId2
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
