/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.util;

import static org.codehaus.groovy.eclipse.core.GroovyCore.logException;
import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Various shared core utilities.
 * 
 * @author empovazan
 */
public class CoreUtils {
    /**
     * @param file
     *            The file containing text for which to create a line to offset
     *            mapping.
     * @return A list of Integer offsets into the file. Note that index 0 ==
     *         line 1 offset == 0.
     * @throws CoreException
     * @throws IOException
     */
    public static List<Integer> createLineToOffsetMapping(final IFile file)
            throws IOException, CoreException {
        final List<Integer> offsets = newList();
        if (file == null)
            return offsets;
        int offset = 0;
        int line = 1;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                file.getContents(), file.getCharset()));
        try {
            offsets.add(new Integer(offset));
            int ch;
            while ((ch = reader.read()) != -1) {
                ++offset;
                if (ch == '\r') {
                    ch = reader.read();
                    ++offset;
                    if (ch == '\n')
                        offsets.add(new Integer(offset));
                    else
                        offsets.add(new Integer(offset - 1));
                    ++line;
                } else if (ch == '\n') {
                    offsets.add(new Integer(offset));
                    ++line;
                }
            }
        } finally {
            reader.close();
        }
        return offsets;
    }

    /**
     * @param text
     *            Text for which to create a line to offset mapping.
     * @return A list of Integer offsets into the file. Note that index 0 ==
     *         line 1 offset == 0.
     */
    public static List<Integer> createLineToOffsetMapping(final String text) {
        final List<Integer> offsets = newList();
        int offset = 0;
        int line = 1;
        final StringReader reader = new StringReader(text);
        try {
            offsets.add(new Integer(offset));
            int ch;
            while ((ch = reader.read()) != -1) {
                ++offset;
                if (ch == '\r') {
                    ch = reader.read();
                    ++offset;
                    if (ch == '\n')
                        offsets.add(new Integer(offset));
                    else
                        offsets.add(new Integer(offset - 1));
                    ++line;
                } else if (ch == '\n') {
                    offsets.add(new Integer(offset));
                    ++line;
                }
            }
        } catch (final IOException e) {
            logException("Internal error, please report", e);
            throw new RuntimeException(e);
        } finally {
            reader.close();
        }

        return offsets;
    }

}
