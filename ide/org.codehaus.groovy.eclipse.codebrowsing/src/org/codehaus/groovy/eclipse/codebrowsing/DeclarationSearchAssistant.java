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
package org.codehaus.groovy.eclipse.codebrowsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codebrowsing.impl.ClassExpressionProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.impl.ClassNodeProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.impl.FieldNodeProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.impl.MethodCallExpressionProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.impl.MethodPointerExpressionProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.impl.PropertyExpressionProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.impl.VariableExpressionProcessor;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
import org.codehaus.groovy.eclipse.editor.actions.CompilationUnitFacade;
import org.codehaus.groovy.eclipse.editor.actions.EditorPartFacade;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;

/**
 * Singleton implementation of IDeclarationSearchAssistant.
 * 
 * @author emp
 */
public class DeclarationSearchAssistant implements IDeclarationSearchAssistant {
	private static final String GROOVY_CONTEXT_ID = "org.codehaus.groovy";

	private static IDeclarationSearchAssistant instance;

	// [contextId: context]
	private static Map<String, IDeclarationSearchContext> mapContextIdToContext = new HashMap<String, IDeclarationSearchContext>();

	// [contextId : [astClassName : [processors]]
	private static Map<String, Map<String,List<IDeclarationSearchProcessor>>> mapContextIdToProcessorMap = 
	        new HashMap<String, Map<String,List<IDeclarationSearchProcessor>>>();

	static {
		registerContext(GROOVY_CONTEXT_ID, new GroovyContext());
		
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg
				.getExtensionPoint("org.codehaus.groovy.eclipse.codebrowsing.declarationSearch");
		// might be null if no extensions are registered.
		if (ep != null) {
		    IExtension[] extensions = ep.getExtensions();
		    for (int i = 0; i < extensions.length; i++) {
		        IExtension extension = extensions[i];
		        IConfigurationElement[] configElements = extension
		        .getConfigurationElements();
		        
		        // First get all the contexts.
		        for (int j = 0; j < configElements.length; j++) {
		            try {
		                IConfigurationElement element = configElements[j];
		                if (element.getName().equals("searchContext")) {
		                    IDeclarationSearchContext context = (IDeclarationSearchContext) element
		                    .createExecutableExtension("class");
		                    String contextId = element.getAttribute("contextId");
		                    registerContext(contextId, context);
		                }
		            } catch (CoreException e) {
		                GroovyCore.logException("Exception when initializing search assistant processors", e);
		            }
		        }
		        
		        // Now get all the processors.
		        for (int j = 0; j < configElements.length; j++) {
		            try {
		                IConfigurationElement element = configElements[j];
		                if (element.getName().equals("searchProcessor")) {
		                    String contextId = element.getAttribute("contextId");
		                    String astNodeClassName = element
		                    .getAttribute("astNodeClass");
		                    IDeclarationSearchProcessor processor = (IDeclarationSearchProcessor) element
		                    .createExecutableExtension("class");
		                    registerProcessor(contextId, astNodeClassName,
		                            processor);
		                }
		            } catch (CoreException e) {
		                GroovyCore.logException("Exception when initializing search assistant processors", e);
		            }
		        }
		    }
		    
		}
	}

	public static void registerProcessor(String cId,
			String astNodeClassName, IDeclarationSearchProcessor processor) {
	    String contextId = cId;
		if (contextId.equals("")) {
			contextId = GROOVY_CONTEXT_ID;
		}
		
		Map<String,List<IDeclarationSearchProcessor>> mapASTClassToProcessors = 
		        mapContextIdToProcessorMap.get(contextId);
		List<IDeclarationSearchProcessor> processors = mapASTClassToProcessors.get(astNodeClassName);
		if (processors == null) {
			processors = new ArrayList<IDeclarationSearchProcessor>();
			mapASTClassToProcessors.put(astNodeClassName, processors);
		}

		processors.add(processor);
	}

	public static void registerContext(String contextId,
			IDeclarationSearchContext context) {
		mapContextIdToContext.put(contextId, context);
		mapContextIdToProcessorMap.put(contextId, new HashMap<String,List<IDeclarationSearchProcessor>>());
	}

	public static IDeclarationSearchAssistant getInstance() {
		if (instance == null) {
			instance = new DeclarationSearchAssistant();
		}
		return instance;
	}

	private DeclarationSearchAssistant() {
		// Register the built in processors.
		registerProcessor(GROOVY_CONTEXT_ID,
				VariableExpression.class.getName(),
				new VariableExpressionProcessor());
		registerProcessor(GROOVY_CONTEXT_ID,
				PropertyExpression.class.getName(),
				new PropertyExpressionProcessor());
//		registerProcessor(GROOVY_CONTEXT_ID, VariableExpression.class
//				.getName(), new MethodCallExpressionProcessor());
		registerProcessor(GROOVY_CONTEXT_ID, MethodCallExpression.class
		        .getName(), new MethodCallExpressionProcessor());
		registerProcessor(GROOVY_CONTEXT_ID, ConstantExpression.class
		        .getName(), new MethodCallExpressionProcessor());
		registerProcessor(GROOVY_CONTEXT_ID, MethodPointerExpression.class
				.getName(), new MethodPointerExpressionProcessor());
		registerProcessor(GROOVY_CONTEXT_ID, ClassExpression.class.getName(),
				new ClassExpressionProcessor());
		registerProcessor(GROOVY_CONTEXT_ID, ClassNode.class.getName(),
				new ClassNodeProcessor());
		registerProcessor(GROOVY_CONTEXT_ID, JDTClassNode.class.getName(),
		        new ClassNodeProcessor());
		registerProcessor(GROOVY_CONTEXT_ID, FieldNode.class.getName(),
				new FieldNodeProcessor());
	}


    private List<IJavaElement> internalFindProposals(IDocumentFacade facade,
            IRegion region, String identifier, ModuleNode moduleNode) {
        ASTSearchResult result = ASTNodeFinder.findASTNode(moduleNode,
				identifier, region, facade.getFile());
		if (result != null) {
			System.out.println("Found at offset " + result.getRegion().getOffset() + ", "
					+ ": " + result.getASTNode());
			return processAST(facade, region, result);
		}
		return null;
    }

	private List<IJavaElement> processAST(IDocumentFacade facade, IRegion region,
			ASTSearchResult result) {
		List<IJavaElement> results = new ArrayList<IJavaElement>();

		ASTNode node = result.getASTNode();
		
		for (String contextId : mapContextIdToContext.keySet()) {
			IDeclarationSearchContext context = (IDeclarationSearchContext) mapContextIdToContext.get(contextId);
			if (context.isActiveContext()) {
				Map<String, List<IDeclarationSearchProcessor>> mapASTClassNameToProcessors = mapContextIdToProcessorMap.get(contextId);
				List<IDeclarationSearchProcessor> processors = mapASTClassNameToProcessors.get(node.getClass().getName());
				if (processors != null) {
				    DeclarationSearchInfo info = new DeclarationSearchInfo(result, facade,
				            region);
					for (IDeclarationSearchProcessor processor : processors) {
                        IJavaElement[] proposals = processor
								.getProposals(info);
						results.addAll(Arrays.asList(proposals));
					}
				}
			}
		}
		
		return results;
	}

    public List<IJavaElement> getProposals(ICompilationUnit unit,
            IRegion region) {
        try {
            CompilationUnitFacade facade = new CompilationUnitFacade(unit);
            IRegion expandedRegion = facade.expandRegion(region);
            String identifier = facade.getText(expandedRegion.getOffset(), expandedRegion.getLength());
            ModuleNode moduleNode = facade.getModuleNode();
            return internalFindProposals(facade, expandedRegion, identifier, moduleNode);
        } catch (BadLocationException e) {
            GroovyCore.logException("Error during Code Select", e);
            return Collections.EMPTY_LIST;
        }
    }
}
