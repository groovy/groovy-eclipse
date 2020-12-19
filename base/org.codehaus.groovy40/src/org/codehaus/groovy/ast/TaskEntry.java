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

/**
 * Represents a single task entry. If it is adjacent to another (and so should
 * share the same message), the isAdjacentTo field will be set.
 */
public class TaskEntry {

    public int start;
    private int end;
    public String taskTag;
    public String taskPriority;
    public TaskEntry isAdjacentTo;
    private String commentText;
    private int offsetToStartOfCommentTextInFile;

    public TaskEntry(int startOffset, int endOffset, String taskTag, String taskPriority, String commentText, int offsetToStartOfCommentTextInFile) {
        this.start = startOffset;
        this.end = endOffset;
        this.taskTag = taskTag;
        this.taskPriority = taskPriority;
        this.commentText = commentText;
        this.offsetToStartOfCommentTextInFile = offsetToStartOfCommentTextInFile;
    }

    public int getEnd() {
        if (isAdjacentTo != null) {
            return isAdjacentTo.getEnd();
        }
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getText() {
        // start/end are within the whole file
        // offsetToStartOfCommentTextInFile gives the offset to the start of the comment within the file
        if (isAdjacentTo != null) {
            return isAdjacentTo.getText();
        } else {
            int commentStartIndex = start - offsetToStartOfCommentTextInFile + taskTag.length();
            int commentEndIndex = end - offsetToStartOfCommentTextInFile + 1;
            return commentText.substring(commentStartIndex, commentEndIndex).trim();
        }
    }

    public String toString() {
        return "Task:" + taskTag + "[" + getText() + "] " + start + " > " + end + "(" + getEnd() + ")";
    }
}
