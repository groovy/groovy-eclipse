/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Kris De Volder - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a test workload for the inferencer consisting of a number of inferencing tasks to be executed
 * all against the same compilation unit contents.
 * 
 * @author Kris De Volder
 */
public class InferencerWorkload implements Iterable<InferencerWorkload.InferencerTask> {
	
	private static final String BEG_MARK_START = "<**";
	private static final String BEG_MARK_SEPARATOR = ":" ;
	private static final String BEG_MARK_END = "**>" ;
	private static final String END_MARK = "<***>" ;
	
	/**
	 * Represents a single inferencing 'task' in a workload. Contains information
	 * about the location we want to inference the type for and the expected result
	 * for that location.
	 * 
	 * @author Kris De Volder
	 */
	public class InferencerTask {
		public final int start;
		public final int end;
		public final String expectedResultType;
		public final String expectedDeclaringType;
		
		public InferencerTask(int start, int end, String expectResultType, String expectDeclaringType) {
			this.start = start;
			this.end = end;
			this.expectedResultType = expectResultType;
			this.expectedDeclaringType = expectDeclaringType;
		}
		
		/**
		 * Contents of the file in which we are trying to do inference.
		 */
		public String getContents() {
			return InferencerWorkload.this.getContents();
		}
	}
	
	private List<InferencerTask> tasks;
	private String contents; 

	/**
	 * Creates a workload from a 'definition'. The definition is the contents of some groovy file with
	 * additional marker 'tags' inserted that contain the expected result and|or declaring type.
	 * <p>
	 * The tags will be stripped out during initialisation of the workload.
	 * <p>
	 * Tags look like <**ResultType:DeclaringType**>expression<***>.
	 */
	public InferencerWorkload(String workloadDefinition) {
		StringBuilder stripped = new StringBuilder(); // The contents of the file minus the tags.
		tasks = new ArrayList<InferencerWorkload.InferencerTask>();
		int readPos = 0; //Boundary between processed and unprocessed input in workloadDefinition
		while (readPos >= 0 && readPos < workloadDefinition.length() ) {
			int headStart = workloadDefinition.indexOf(BEG_MARK_START, readPos);
			int separator = -1;
			int headEnd = -1; 
			int tail = -1; 
			if (headStart>=0) {
				separator = workloadDefinition.indexOf(BEG_MARK_SEPARATOR, headStart); 
				headEnd = workloadDefinition.indexOf(BEG_MARK_END, headStart);
				tail = workloadDefinition.indexOf(END_MARK, headStart);
			}
			
			//Well formatted tag looks like this:
			// <**resultType:declType**>expression<***>
			//So if one was found, then the various positions of found markers must be in a specific order:
			if (headStart>=0 && separator>headStart && headEnd>separator && tail>headEnd) {
				//Copy text in front of tag into 'stripped' contents buffer
				int start = readPos;
				int end = headStart;
				stripped.append(workloadDefinition.substring(start, end));
				
				//Extract resultType:
				start = headStart + BEG_MARK_START.length();
				end = separator;
				String resultType = workloadDefinition.substring(start, end);

				//Extract declType
				start = separator+BEG_MARK_SEPARATOR.length();
				end = headEnd;
				String declType = workloadDefinition.substring(start, end);
				
				//Extract expression
				start = headEnd+BEG_MARK_END.length();
				end = tail;
				String expression = workloadDefinition.substring(start, end);

				//Copy expression and compute start and end positions in 'stripped' buffer.
				start = stripped.length();
				stripped.append(expression);
				end = stripped.length();
				
				tasks.add(new InferencerTask(start, end, resultType, declType));
				
				readPos = tail + END_MARK.length();
			} else {
				//No tag was found so we are done, but don't forget to copy remaining text
				stripped.append(workloadDefinition.substring(readPos));
				readPos = -1;
			}
		}
		
		contents = stripped.toString();
	}

	/**
	 * @return The text of the file, without the workload marker tags.
	 */
	public String getContents() {
		return contents;
	}
	
	public Iterator<InferencerTask> iterator() {
		return tasks.iterator();
	}
}