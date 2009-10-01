/*******************************************************************************
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
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codeassist.completion.jdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.completion.Activator;
import org.codehaus.groovy.eclipse.core.DocumentSourceBuffer;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.inference.internal.SourceContextInfo;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.Member;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.Property;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;
import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Code completion processor that attempts to complete expressions in the source code.
 * 
 * When not in script or closure, then JDT takes over some kinds of completions, such as
 * methods, and Types (not done are categories and inference)
 * 
 * @author empovazan
 * @author andrew
 */
public class GeneralGroovyCompletionProcessor extends AbstractGroovyCompletionProcessor {
    private boolean isGuessArguments;
    
    public GeneralGroovyCompletionProcessor() {
        IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
        isGuessArguments= preferenceStore.getBoolean(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS);
    }
    
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
	    if (!(context instanceof JavaContentAssistInvocationContext)) {
	        return Collections.EMPTY_LIST;
	    }
	    
	    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
	    if (isGuessArguments) {
	        ReflectionUtils.setPrivateField(InternalCompletionContext.class, 
	                "isExtended", javaContext.getCoreContext(), true);
	    }
	    
	    // don't do groovy content assist for Java files
	    if (! (javaContext.getCompilationUnit() instanceof GroovyCompilationUnit)) {
	        return Collections.EMPTY_LIST;
	    }
	    
	    int offset = context.getInvocationOffset();
	    if (offset - 1 < 0) {
			return Collections.EMPTY_LIST;
		}
		
		// Find the context within the file being editied.
		ModuleNode moduleNode = getCurrentModuleNode(javaContext);

		// Perhaps it hasn't compiled yet?
		if (moduleNode == null) {
			return Collections.EMPTY_LIST;
		}

		ISourceBuffer buffer = new DocumentSourceBuffer(context.getDocument());
		try {
		    SourceContextInfo info = SourceContextInfo.create(moduleNode, new GroovyProjectFacade(javaContext.getProject()), offset, buffer, true);
		    
		    if (info != null && info.eval != null && !info.eval.getName().equals("void")) {
		        if (info.name != null) {
		            // Complete the property expression.
		            return createCompletionProposals(info.lookup, info.eval, info.expression, info.name, offset, javaContext);
		        }
		    }
		} catch (Exception e) {
		    Activator.logError(e);
		}
		return Collections.EMPTY_LIST;
	}

	
	private List<ICompletionProposal> createCompletionProposals(IMemberLookup lookup, EvalResult result, String expression, String name, int offset, JavaContentAssistInvocationContext javaContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		
		String type = box(result.getName());
		
		Property[] properties = lookup.lookupProperties(type, name, false, result.isClass(), false);
		properties = (Property[]) mergeTypes(properties);
		proposals.addAll(createCompletionProposals(properties, offset - name.length(), name.length(), javaContext));
		
		Field[] fields = lookup.lookupFields(type, name, false, result.isClass(), false);
		fields = (Field[]) mergeTypes(fields);
		proposals.addAll(createCompletionProposals(fields, offset - name.length(), name.length(), javaContext));
		
		Method[] methods = lookup.lookupMethods(type, name, false, result.isClass(), false);
		methods = (Method[]) mergeTypes(methods);
		proposals.addAll(createCompletionProposals(methods, offset - name.length(), name.length(), javaContext));
		
		List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
		if (!proposals.isEmpty()) {
		    
		    // condense -- omit dups and nulls
		    // nulls signify a problem happened somewhere.  Log the null
		    int i = 0;
		    int numNullsFound = 0;
		    ICompletionProposal prevProposal = proposals.get(i);
		    while (i < proposals.size()) {
		        i++;
		        if (prevProposal != null) {
		            results.add(prevProposal);
		            break;
		        } else {
		            numNullsFound++;
		            prevProposal = proposals.get(i);
		        }
		    }
			while (i < proposals.size()) {
				ICompletionProposal proposal = proposals.get(i);
				if (proposal == null) {
                    numNullsFound++;
				}
				if (proposal != null && 
				        !proposal.toString().equals(prevProposal.toString())){
					results.add(proposal);
				}
				prevProposal = proposal;
				i++;
			}
			if (numNullsFound > 0) {
			    GroovyCore.logWarning("" + numNullsFound + 
			            " Null completion proposals found when looking for" +
			            name + "." + expression);
			}
		}
		return results;
	}

	/**
	 * If type is a primitive type name then boxes the type in with the object name
	 * Otherwise, just return type.
     * @param type
     */
    private String box(String type) {
        if (type.equals("int")) {
            return "java.lang.Integer";
        } else if (type.equals("boolean")) {
            return "java.lang.Boolean";
        } else if (type.equals("char")) {
            return "java.lang.Character";
        } else if (type.equals("long")) {
            return "java.lang.Long";
        } else if (type.equals("double")) {
            return "java.lang.Double";
        } else if (type.equals("short")) {
            return "java.lang.Short";
        } else if (type.equals("float")) {
            return "java.lang.Float";
        } else if (type.equals("byte")) {
            return "java.lang.Byte";
        }
        return type;
    }

    private List<ICompletionProposal> createCompletionProposals(Member[] members, int offset, int replaceLength, JavaContentAssistInvocationContext javaContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(members.length);
		for (int i = 0; i < members.length; ++i) {
			String replaceString = members[i].getName();
			if (replaceString.indexOf('$') == -1 && !replaceString.startsWith("__timeStamp")) {
    			proposals.add(
    			        new JavaCompletionProposal(replaceString, offset, replaceLength,
    			                getImageForType(members[i]), createDisplayString(members[i]), getRelevance(members[i].getName().toCharArray())));
			}
		}
		return proposals;
	}

    private int getRelevance(char[] name) {
        return name[0] == '$' || name[0] == '_'
            ? 1 : 2;
    }
	
    private List<ICompletionProposal> createCompletionProposals(Method[] methods, int offset, int replaceLength, JavaContentAssistInvocationContext javaContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(methods.length);
		for (int i = 0; i < methods.length; ++i) {
			String replaceString = createReplaceString(methods[i]);
			if (replaceString.indexOf('$') == -1) {
                CompletionProposal proposal = CompletionProposal.create(CompletionProposal.METHOD_REF, offset+replaceLength);
                proposal.setCompletion(replaceString.toCharArray());
                proposal.setDeclarationSignature(methods[i].getDeclaringClass().getSignature().toCharArray());
                proposal.setName(methods[i].getName().toCharArray());
                proposal.setParameterNames(createParameterNames((Method) methods[i]));
                ReflectionUtils.setPrivateField(InternalCompletionProposal.class, "parameterTypeNames", proposal, createParameterTypeNames(methods[i]));
                proposal.setReplaceRange(offset, offset + replaceLength);
                proposal.setFlags(methods[i].getModifiers());
                proposal.setKey(methods[i].getSignature().toCharArray());
                proposal.setAdditionalFlags(CompletionFlags.Default);
                proposal.setSignature(getMethodSignature(methods[i]));  // this one might be a real type signature
                proposal.setRelevance(getRelevance(proposal.getName()));  // should set lower
                // we don't support guessing arguments yet.
                if (false) {
//                if (isGuessArguments) {
                    proposals.add(ParameterGuessingProposal.createProposal(proposal, javaContext, isGuessArguments));
                } else {
                    proposals.add(new GroovyMethodCompletionProposal(proposal, javaContext));
                }
			}
		}
		return proposals;
	}


    private char[][] createParameterTypeNames(Method method) {
        char[][] typeNames = new char[method.getParameters().length][];
        int i = 0;
        for (Parameter param : method.getParameters()) {
        	// Type signatures are used for array types only.
        	// all others uses type names
    		if (Signature.getArrayCount(param.getSignature()) > 0) {
    		    typeNames[i] = Signature.getSignatureSimpleName(param.getSignature().toCharArray());
    		} else {
    		    // this is a plain old qualified name
    		    String sig = param.getSignature();
    		    String[] splits = sig.split("\\.");
    		    typeNames[i] = (splits.length > 0 ? splits[splits.length-1] : sig).toCharArray();
    			
    		}
            i++;
        }
        return typeNames;
    } 

    private char[] getMethodSignature(Method method) {
        String returnTypeSig = getTypeSignature(method.getReturnType());
        Parameter[] params = method.getParameters();
        String[] paramTypeSigs = new String[params.length];
        for (int i = 0; i < paramTypeSigs.length; i++) {
            paramTypeSigs[i] = getTypeSignature(params[i].getSignature());
        }
        return Signature.createMethodSignature(paramTypeSigs, returnTypeSig).toCharArray();
    }

    private String getTypeSignature(String typeName) {
        String typeSig;
        // check to see if we have a type signature, or a type name
        // will be a type signature if the typeName is an array
        boolean isTypeSignature ;
        try {
            // may raise an exception if not proper signature
            int arrayCount = Signature.getArrayCount(typeName);
            isTypeSignature = arrayCount > 0;
        } catch (IllegalArgumentException e) {
            isTypeSignature = false;
        } catch (ArrayIndexOutOfBoundsException e) {
            isTypeSignature = false;
        }
        
        if (isTypeSignature) {  
            typeSig = typeName;
        } else {
            typeSig = Signature.createTypeSignature(typeName, false);
        }
        return typeSig;
    }

    private char[][] createParameterNames(Method method) {
        Parameter[] params = method.getParameters();
        char[][] paramNames = new char[params.length][];
        for (int i = 0; i < params.length; i++) {
            paramNames[i] = params[i].getName().toCharArray();
        }
        return paramNames;
    }
}
