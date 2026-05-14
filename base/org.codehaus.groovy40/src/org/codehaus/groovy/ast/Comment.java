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
package org.codehaus.groovy.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a comment in groovy source. Subtypes are single line or multi line.
 * Contains factory methods called from the parser (GroovyRecognizer) that build the
 * comment subtypes.
 */
public abstract class Comment {

    protected static final boolean debug = false;

    protected static final int BLOCK = 0; // text surrounded by /* .. */
    protected static final int LINE = 1; // text prefixed with //
    protected static final int JAVADOC = 2; // text surrounded by /** .. */

    protected String comment;
    private int kind;

    // set when the comment is associated with a source element
    public boolean usedUp;

    // Start/Ends for line/columns
    // Lines are from 1..N
    // Columns are from 1..N
    public int sline, scol, eline, ecol;

    public Comment(int kind, int sline, int scol, int eline, int ecol, String string) {
        this.kind = kind;
        this.sline = sline;
        this.scol = scol;
        this.eline = eline;
        this.ecol = ecol;
        this.comment = string;
    }

    public int getLastLine() {
        return eline;
    }

    public static Comment makeSingleLineComment(int sline, int scol, int eline, int ecol, String string) {
        assert string != null && string.startsWith("//");
        return new SingleLineComment(sline, scol, eline, ecol, string);
    }

    public static Comment makeMultiLineComment(int sline, int scol, int eline, int ecol, String string) {
        assert string != null && string.startsWith("/*") && string.endsWith("*/");
        return new MultiLineComment(sline, scol, eline, ecol, string);
    }

    public abstract List<TaskEntry> getPositionsOf(String taskTag, String taskPriority, int[] lineseps, boolean caseSensitive);

    /**
     * Return the positions (offsets) that JDT wants to see.  Special rules here!  For a javadoc comment
     * both offsets are positive.  For a line comment '//' both are negative.  For a block comment only the
     * end is negative.
     */
    public int[] getPositions(int[] lineseps) {
        int offsetToStartLine = (sline == 1 ? 0 : lineseps[sline - 2] + 1);
        int start = offsetToStartLine + (scol - 1);
        int offsetToEndLine = (eline == 1 ? 0 : lineseps[eline - 2] + 1);
        int end = offsetToEndLine + (ecol - 1);
        if (kind == LINE) {
            return new int[]{-start, -end};
        } else if (kind == BLOCK) {
            return new int[]{start, -end};
        } else { // JAVADOC
            return new int[]{start, end};
        }
    }

    public String toString() {
        return comment;
    }

    protected boolean isValidStartLocationForTask(String text, int index, String taskTag) {
        int tagLen = taskTag.length();
        if (comment.charAt(index - 1) == '@') {
            return false;
        }

        // ensure tag is not leaded with letter if tag starts with a letter
        if (Character.isJavaIdentifierStart(comment.charAt(index))) {
            if (Character.isJavaIdentifierPart(comment.charAt(index - 1))) {
                return false;
            }
        }

        // ensure tag is not followed with letter if tag finishes with a
        // letter
        if ((index + tagLen) < comment.length() && Character.isJavaIdentifierStart(comment.charAt(index + tagLen - 1))) {
            if (Character.isJavaIdentifierPart(comment.charAt(index + tagLen))) {
                return false;
            }
        }
        return true;
    }

    protected int findTaskTag(String text, String tag, boolean caseSensitive, int fromIndex) {
        if (caseSensitive) {
            return text.indexOf(tag, fromIndex);
        } else {
            int taglen = tag.length();
            String lcTag = tag.toLowerCase();
            char firstChar = lcTag.charAt(0);
            for (int p = fromIndex, max = text.length() - tag.length() + 1; p < max; p++) {
                if (Character.toLowerCase(text.charAt(p)) == firstChar) {
                    // possible match
                    boolean matched = true;
                    for (int t = 1; t < taglen; t++) {
                        if (Character.toLowerCase(text.charAt(p + t)) != lcTag.charAt(t)) {
                            matched = false;
                            break;
                        }
                    }
                    if (matched) {
                        return p;
                    }
                }
            }
            return -1;
        }
    }

    public boolean isJavadoc() {
        return kind == JAVADOC;
    }
}

/**
 * Represents a single line comment of the form '// blahblahblah'
 */
class SingleLineComment extends Comment {

    SingleLineComment(int sline, int scol, int eline, int ecol, String string) {
        super(LINE, sline, scol, eline, ecol, string);
        if (debug) {
            System.out.println("Lexer found SL comment: [" + string + "] at L" + sline + "C" + scol + ">L" + eline + "C" + ecol);
        }
    }

    public List<TaskEntry> getPositionsOf(String taskTag, String taskPriority, int[] lineseps, boolean caseSensitive) {
        int i = findTaskTag(comment, taskTag, caseSensitive, 0);
        if (debug) {
            System.out.println("searching slc: [" + comment + "] for '" + taskTag + "' " + i);
        }
        if (i == -1) {
            return Collections.emptyList();
        }
        List<TaskEntry> tasks = new ArrayList<>();
        while (i != -1) {
            if (isValidStartLocationForTask(comment, i, taskTag)) {
                int offsetToLineStart = (sline == 1 ? 0 : lineseps[sline - 2] + 1);
                int taskTagStart = offsetToLineStart + (scol - 1) + i;
                int taskEnd = offsetToLineStart + ecol - 2;
                TaskEntry taskEntry = new TaskEntry(taskTagStart, taskEnd, taskTag, taskPriority, comment, offsetToLineStart + scol - 1);
                if (debug) {
                    System.out.println("Built task entry " + taskEntry.toString());
                }
                tasks.add(taskEntry);
            }
            i = findTaskTag(comment, taskTag, caseSensitive, i + taskTag.length());
        }
        return tasks;
    }
}

/**
 * Represents a multi line comment of the form '/<star> blahblahblah <star>/'
 */
class MultiLineComment extends Comment {

    MultiLineComment(int sline, int scol, int eline, int ecol, String string) {
        super(string.charAt(2) == '*' ? JAVADOC : BLOCK, sline, scol, eline, ecol, string);
        if (debug) {
            System.out.println("Lexer found ML comment: [" + string + "] at L" + sline + "C" + scol + ">L" + eline + "C" + ecol);
        }
    }

    @Override
    public List<TaskEntry> getPositionsOf(String taskTag, String taskPriority, int[] lineseps, boolean caseSensitive) {
        int i = findTaskTag(comment, taskTag, caseSensitive, 0);
        if (debug) {
            System.out.println("searching mlc: [" + comment + "] for '" + taskTag + "' " + i);
        }
        if (i == -1) {
            return Collections.emptyList();
        }
        List<TaskEntry> taskPositions = new ArrayList<>();
        while (i != -1) {

            if (isValidStartLocationForTask(comment, i, taskTag)) {
                int offsetToCommentStart = (sline == 1 ? 0 : lineseps[sline - 2] + 1) + scol - 1;

                int taskTagStart = offsetToCommentStart + i;
                int taskEnd = taskTagStart;
                // find the end (end of comment or end of line)
                while (true) {
                    int pos = taskEnd - offsetToCommentStart;
                    char ch = comment.charAt(pos);
                    if (ch == '\n' || ch == '\r') {
                        break;
                    }
                    if ((pos + 2) > comment.length()) {
                        taskEnd--;
                        break;
                    }
                    taskEnd++;
                }
                TaskEntry taskEntry = new TaskEntry(taskTagStart, taskEnd - 1, taskTag, taskPriority, comment, offsetToCommentStart);
                if (debug) {
                    System.out.println("Built task entry " + taskEntry.toString());
                }
                taskPositions.add(taskEntry);
            }
            i = findTaskTag(comment, taskTag, caseSensitive, i + taskTag.length());
        }
        return taskPositions;
    }
}
