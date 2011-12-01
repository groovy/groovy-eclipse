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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager.ProjectSuggestions;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.MethodParameter;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionDescriptor;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-09-08
 */
public class SuggestionsReader {

    private String absoluteFile;

    private ProjectSuggestions projectSuggestions;

    public SuggestionsReader(IProject project, String absoluteFile) {
        this.projectSuggestions = InferencingSuggestionsManager.getInstance().getSuggestions(project);
        this.absoluteFile = absoluteFile;
    }

    public ProjectSuggestions read() {

        try {
            if (absoluteFile == null || projectSuggestions == null) {
                return null;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            Reader reader = new BufferedReader(new FileReader(absoluteFile));

            Document document = parser.parse(new InputSource(reader));

            if (document != null) {

                // register a new project selection
                projectSuggestions = projectSuggestions.registerNewProjectSuggestion();

                NodeList list = document.getChildNodes();

                // There should only be one root element

                if (list.getLength() > 0) {
                    Node node = list.item(0);
                    if (node instanceof Element) {
                        Element element = (Element) node;
                        if (element.getNodeName().equals(SuggestionElementStatics.ROOT)) {

                            // Next should be all the declaring types
                            NodeList declaringTypeList = element.getChildNodes();
                            if (declaringTypeList != null) {
                                handleDeclaringTypeNodes(declaringTypeList);
                            }
                        }
                    }
                }
            }
            return projectSuggestions;
        } catch (ParserConfigurationException e) {
            GroovyDSLCoreActivator.logException(e);
        } catch (IOException e) {
            GroovyDSLCoreActivator.logException(e);
        } catch (SAXException e) {
            GroovyDSLCoreActivator.logException(e);
        }
        return null;
    }

    protected void handleDeclaringTypeNodes(NodeList list) {
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (element.getNodeName().equals(SuggestionElementStatics.DECLARING_TYPE)) {
                    String declaringTypeName = element.getAttribute(SuggestionElementStatics.TYPE_ATT);
                    NodeList suggestions = element.getChildNodes();
                    for (int j = 0; j < suggestions.getLength(); j++) {
                        Node suggNode = suggestions.item(j);
                        if (suggNode instanceof Element) {
                            SuggestionDescriptor descriptor = getSuggestionDescriptor((Element) suggNode, declaringTypeName);
                            if (descriptor != null) {
                                projectSuggestions.addSuggestion(descriptor);
                            }
                        }
                    }
                }
            }
        }
    }

    protected SuggestionDescriptor getSuggestionDescriptor(Element element, String declaringTypeName) {
        if (element.getNodeName().equals(SuggestionElementStatics.METHOD)
                || element.getNodeName().equals(SuggestionElementStatics.PROPERTY)) {
            String suggestionName = element.getAttribute(SuggestionElementStatics.NAME_ATT);
            String suggestionType = element.getAttribute(SuggestionElementStatics.TYPE_ATT);
            boolean isStatic = new Boolean(element.getAttribute(SuggestionElementStatics.IS_STATIC_ATT)).booleanValue();
            boolean isActive = new Boolean(element.getAttribute(SuggestionElementStatics.IS_ACTIVE)).booleanValue();
            NodeList docNodes = element.getElementsByTagName(SuggestionElementStatics.DOC);
            String doc = null;
            // There should only be one doc node
            if (docNodes != null && docNodes.getLength() > 0) {
                Node docNode = docNodes.item(0);
                if (docNode instanceof Element) {
                    Element docElement = (Element) docNode;
                    doc = docElement.getNodeValue();
                }
            }

            SuggestionDescriptor descriptor = null;
            // Read method-specific elements and attributes
            if (element.getNodeName().equals(SuggestionElementStatics.METHOD)) {
                Element parametersElement = getParametersElement(element);
                boolean useNameArguments = false;
                if (parametersElement != null) {
                    useNameArguments = new Boolean(parametersElement.getAttribute(SuggestionElementStatics.USE_NAMED_ARGUMENTS_ATT))
                            .booleanValue();
                }
                List<MethodParameter> modelParameters = getParameters(parametersElement);
                descriptor = new SuggestionDescriptor(declaringTypeName, isStatic, suggestionName, doc, suggestionType,
                        useNameArguments, modelParameters, isActive);
            } else {
                descriptor = new SuggestionDescriptor(declaringTypeName, isStatic, suggestionName, doc, suggestionType, isActive);
            }
            return descriptor;
        }
        return null;
    }

    protected Element getParametersElement(Element element) {
        // Only one parameters section exists per method suggestion
        Node node = element.getFirstChild();
        if (node.getNodeName().equals(SuggestionElementStatics.PARAMETERS) && node instanceof Element) {
            return (Element) node;
        }
        return null;

    }

    protected List<MethodParameter> getParameters(Element parametersElement) {
        if (parametersElement == null) {
            return null;
        }

        List<MethodParameter> parameters = new ArrayList<MethodParameter>();
        NodeList parametersNodeList = parametersElement.getChildNodes();

        if (parametersNodeList != null) {
            for (int i = 0; i < parametersNodeList.getLength(); i++) {
                Node paramNode = parametersNodeList.item(i);
                if (paramNode instanceof Element && paramNode.getNodeName().equals(SuggestionElementStatics.PARAMETER)) {
                    Element paramElement = (Element) paramNode;
                    String nameParam = paramElement.getAttribute(SuggestionElementStatics.NAME_ATT);
                    String typeParam = paramElement.getAttribute(SuggestionElementStatics.TYPE_ATT);
                    parameters.add(new MethodParameter(nameParam, typeParam));
                }
            }
        }

        return parameters;
    }
}
