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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.groovy.tests.search.AbstractInferencingTest;

/**
 * Represents a test workload for the inferencer consisting of a number of inferencing tasks to be executed
 * all against the same compilation unit contents.
 * 
 * @author Kris De Volder
 */
public class InferencerWorkload implements Iterable<InferencerWorkload.InferencerTask> {
	
	private static final String BEG_MARK_START = "/*!";
	private static final String BEG_MARK_SEPARATOR = ":" ;
	private static final String BEG_MARK_END = "!*/" ;
	private static final String END_MARK = "/*!*/" ;
	
	private static final Map<String, String> DEFAULT_ALIASES = new HashMap<String, String>();
	static {
	   DEFAULT_ALIASES.put("B", "java.lang.Byte");
	   DEFAULT_ALIASES.put("C", "java.lang.Character");
	   DEFAULT_ALIASES.put("D", "java.lang.Double");
	   DEFAULT_ALIASES.put("F", "java.lang.Float");
	   DEFAULT_ALIASES.put("I", "java.lang.Integer");
	   DEFAULT_ALIASES.put("L", "java.lang.Long");
	   DEFAULT_ALIASES.put("S", "java.lang.Short");
	   DEFAULT_ALIASES.put("V", "java.lang.Void");
	   DEFAULT_ALIASES.put("Z", "java.lang.Boolean");
	   DEFAULT_ALIASES.put("STR", "java.lang.String");
	   DEFAULT_ALIASES.put("LIST", "java.util.List");
	   DEFAULT_ALIASES.put("MAP", "java.util.MAP");
	   DEFAULT_ALIASES.put("O", "java.lang.Object");
	   
	}
	
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
		@Override
		public String toString() {
		    return "Type: " + expectedResultType + "\nDeclaring: " + expectedDeclaringType + "\nContents: " + getContents().substring(start, end);
		}
	}
	
	private List<InferencerTask> tasks;
	private String contents;
	private final Map<String,String> aliases;
	
	public InferencerWorkload(File workloadDefinitionFile, String ... extraAliases) throws Exception {
	    this(extractContents(workloadDefinitionFile), extraAliases);
	}
	
	/**
     * @param workloadDefinitionFile
     * @return
	 * @throws Exception 
     */
    private static String extractContents(File workloadDefinitionFile) throws Exception {
        Reader r = new FileReader(workloadDefinitionFile);
        BufferedReader br = new BufferedReader(r);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line  = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    /**
	 * Creates a workload from a 'definition'. The definition is the contents of some groovy file with
	 * additional marker 'tags' inserted that contain the expected result and|or declaring type.
	 * <p>
	 * The tags will be stripped out during initialisation of the workload.
	 * <p>
	 * Tags look like <**ResultType:DeclaringType**>expression<***>.
	 * <p>
	 * In order to cut down on the length of the type specifications, there are aliases.
	 * Default aliases are specified by {@value #DEFAULT_ALIASES}, but you can add your own
	 * using the extraAliases argument.  It takes pairs of strings (alias, long name).
	 * So, the length of extraAliases must be even.
	 */
	public InferencerWorkload(String workloadDefinition, String ... extraAliases) {
	    aliases = new HashMap<String, String>(DEFAULT_ALIASES);
	    for (int i = 0; i < extraAliases.length; i++) {
	        aliases.put(extraAliases[i++], extraAliases[i]);
        }
	    
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
				if (aliases.containsKey(resultType)) {
				    resultType = aliases.get(resultType);
				}
                if (resultType.length() == 0) {
                    resultType = null;
                }
				
				//Extract declType
				start = separator+BEG_MARK_SEPARATOR.length();
				end = headEnd;
				String declType = workloadDefinition.substring(start, end);
                if (aliases.containsKey(declType)) {
                    declType = aliases.get(declType);
                }
                if (declType.length() == 0) {
                    declType = null;
                }
				
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
	
	/**
	 * Performs inferencing on the given compilation unit.
	 * It is assumed that the contents of the compilation unit
	 * matches the contents of this inferencer task
	 * @param unit
	 */
	public void perform(GroovyCompilationUnit unit, boolean assumeNoUnknowns) throws Exception {
	    try {
            unit.becomeWorkingCopy(null);
            StringBuilder sb = new StringBuilder();
            for (InferencerTask task : this) {
                String res = AbstractInferencingTest.checkType(unit, task.start, task.end, task.expectedResultType, task.expectedDeclaringType, assumeNoUnknowns, false);
                if (res != null) {
                    sb.append("\n\nInferencing failure:\n" + res);
                }
                // only look for unknowns the first time
                assumeNoUnknowns = false;
            }
            if (sb.length() > 0) {
                Assert.fail(sb.toString());
            }
        } finally {
            unit.discardWorkingCopy();
        }
	}
}