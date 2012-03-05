/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement     - Initial API and implementation
 *******************************************************************************/
 package org.codehaus.groovy.ast;

/**
 * Represent a single task entry - if it is adjacent to another (and so should share the same message), the isAdjacentTo field will be set.
 * 
 * @author Andy Clement
 *
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
		if (isAdjacentTo!=null) {
			return isAdjacentTo.getEnd();
		}
		return end;
	}
	
	public void setEnd(int end) {
		this.end= end;
	}

	public String getText() {
		// start/end are within the whole file
		// offsetToStartOfCommentTextInFile gives the offset to the start of the comment within the file
		if (isAdjacentTo!=null) {
			return isAdjacentTo.getText();
		} else {
			int commentStartIndex = start-offsetToStartOfCommentTextInFile+taskTag.length();
			int commentEndIndex = end-offsetToStartOfCommentTextInFile+1;
			return commentText.substring(commentStartIndex,commentEndIndex).trim();
		}
	}
	

	public String toString() {
		StringBuffer task = new StringBuffer();
		task.append("Task:" + taskTag + "[" + getText() + "] " + start + " > " + end+"("+getEnd()+")");
		return task.toString();
	}

}
