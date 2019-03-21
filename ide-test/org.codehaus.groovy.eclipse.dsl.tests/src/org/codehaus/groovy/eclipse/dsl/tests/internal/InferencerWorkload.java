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
package org.codehaus.groovy.eclipse.dsl.tests.internal;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.groovy.tests.search.InferencingTestSuite;
import org.junit.Assert;

/**
 * Represents a test workload for the inferencer consisting of a number of inferencing tasks to be executed
 * all against the same compilation unit contents.
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
       DEFAULT_ALIASES.put("MAP", "java.util.Map");
       DEFAULT_ALIASES.put("O", "java.lang.Object");
    }

    /**
     * Represents a single inferencing 'task' in a workload. Contains information
     * about the location we want to inference the type for and the expected result
     * for that location.
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
    private boolean aliasesLocked = false; //Set to true when we start parsing the workloadDefinition

    @SuppressWarnings("deprecation")
    public InferencerWorkload(File workloadDefinitionFile, String ... extraAliases) throws Exception {
        this(DefaultGroovyMethods.getText(workloadDefinitionFile), extraAliases);
    }

    /**
     * Creates a workload from a 'definition'. The definition is the contents of some groovy file with
     * additional marker 'tags' inserted that contain the expected result and|or declaring type.
     * <p>
     * The tags will be stripped out during initialisation of the workload.
     * <p>
     * Tags look like (without the backslashes): <pre>\/*!ResultType:DeclaringType!*\/expression\/*!*\/</pre>.
     * <p>
     * In order to cut down on the length of the type specifications, there are aliases.
     * Default aliases are specified by {@value #DEFAULT_ALIASES}, but you can add your own
     * using the extraAliases argument.  It takes pairs of strings (alias, long name).
     * So, the length of extraAliases must be even.
     */
    public InferencerWorkload(String workloadDefinition, String ... extraAliases) {
        aliases = new HashMap<String, String>(DEFAULT_ALIASES);
        for (int i = 0; i < extraAliases.length; i++) {
            defAlias(extraAliases[i++], extraAliases[i]);
        }

        aliasesLocked = true; // Should allow changing aliases anymore from here onward.
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

    protected void defAlias(String name, String expansion) {
        Assert.assertTrue("Aliases must be defined *before* parsing the workload", !aliasesLocked);
        String existing = aliases.get(name);
        if (existing!=null) {
            Assert.fail("Multiple definitions for alias "+name+" first = "+expansion+" second = "+expansion);
        } else {
            aliases.put(name, expansion);
        }
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
     */
    public void perform(GroovyCompilationUnit unit, boolean assumeNoUnknowns) throws Exception {
        boolean doneSomething = false;
        try {
            unit.becomeWorkingCopy(null);
            StringBuilder sb = new StringBuilder();
            for (InferencerTask task : this) {
                doneSomething = true;
                String res = InferencingTestSuite.checkType(unit, task.start, task.end, task.expectedResultType, task.expectedDeclaringType, assumeNoUnknowns, false);
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
        assertTrue("Workload should have at least one annotated element", doneSomething);
    }

    public void perform(GroovyCompilationUnit unit) throws Exception {
        perform(unit, false);
    }
}
