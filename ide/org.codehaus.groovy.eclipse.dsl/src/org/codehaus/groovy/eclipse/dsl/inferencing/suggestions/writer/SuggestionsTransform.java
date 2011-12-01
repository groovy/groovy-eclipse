/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovySuggestionDeclaringType;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager.ProjectSuggestions;
import org.codehaus.groovy.runtime.StringBufferWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-08-09
 */
public class SuggestionsTransform {

    private ProjectSuggestions suggestions;

    public SuggestionsTransform(ProjectSuggestions suggestions) {
        this.suggestions = suggestions;
    }

    /**
     * 
     * @return transformed suggestions model into a XML serialisation, or null
     *         if serialisation failed. Partial serialisation is supported.
     */
    public String transform() {

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            TransformElementFactory elementFactory = new TransformElementFactory();

            // Add a root element
            TransformElement rootElement = elementFactory.getRootElement();
            Node root = document.createElement(rootElement.getElementName());
            document.appendChild(root);

            for (GroovySuggestionDeclaringType declaringType : suggestions.getDeclaringTypes()) {
                TransformElement element = elementFactory.getDeclaringTypeWriterElement(declaringType);
                transform(element, root, document);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            DOMSource source = new DOMSource(document);

            StringBuffer buffer = new StringBuffer();
            StringBufferWriter bufferWriter = new StringBufferWriter(buffer);
            StreamResult result = new StreamResult(bufferWriter);
            transformer.transform(source, result);
            return buffer.toString();

        } catch (TransformerConfigurationException e) {
            GroovyDSLCoreActivator.logException(e);
        } catch (IllegalArgumentException e) {
            GroovyDSLCoreActivator.logException(e);
        } catch (ParserConfigurationException e) {
            GroovyDSLCoreActivator.logException(e);
        } catch (TransformerFactoryConfigurationError e) {
            GroovyDSLCoreActivator.logException(e);
        } catch (TransformerException e) {
            GroovyDSLCoreActivator.logException(e);
        }

        return null;

    }

    protected void transform(TransformElement element, Node parent, Document document) {
        String name = element.getElementName();
        Node node = document.createElement(name);
        String elementValue = element.getValue();

        if (elementValue != null) {
            node.setTextContent(element.getValue());
        }

        parent.appendChild(node);

        List<TransformElementProperty> properties = element.getProperties();
        if (properties != null) {

            NamedNodeMap attributes = node.getAttributes();
            for (TransformElementProperty property : properties) {
                Attr attr = document.createAttribute(property.getName());
                attr.setValue(property.getValue());
                attributes.setNamedItem(attr);
            }
        }

        List<TransformElement> children = element.getChildren();

        if (children != null) {
            for (TransformElement child : children) {
                transform(child, node, document);
            }
        }
    }

 
}
