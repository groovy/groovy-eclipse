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
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

public class GroovyDocumentSetupParticipant  implements IDocumentSetupParticipant {

    @Override
    public void setup(final IDocument document) {
        setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
    }

    /**
     * Sets up the Java document partitioner for the given document for the given partitioning.
     *
     * @param document the document to be set up
     * @param partitioning the document partitioning
     */
    private void setupJavaDocumentPartitioner(final IDocument document, final String partitioning) {
        IDocumentPartitioner partitioner = createDocumentPartitioner();
        if (document instanceof IDocumentExtension3) {
            IDocumentExtension3 extension3 = (IDocumentExtension3) document;
            extension3.setDocumentPartitioner(partitioning, partitioner);
        } else {
            document.setDocumentPartitioner(partitioner);
        }
        partitioner.connect(document);
    }

    private IDocumentPartitioner createDocumentPartitioner() {
        return new FastPartitioner(new GroovyPartitionScanner(), GroovyPartitionScanner.LEGAL_CONTENT_TYPES);
    }
}
