package org.codehaus.groovy.eclipse.codeassist.completion.jdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.completion.Activator;
import org.codehaus.groovy.eclipse.core.DocumentSourceBuffer;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.impl.ReverseSourceBuffer;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.ITypeEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Modifiers;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.SymbolTableRegistry;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluationContextBuilder;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;
import org.codehaus.groovy.eclipse.core.types.impl.TypeCategoryLookup;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ParseException;
import org.codehaus.groovy.eclipse.core.util.ReflectionUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

/**
 * Completion for Default groovy methods
 * 
 * @author andrew
 */
public class DefaultGroovyMethodProcessor implements IJavaCompletionProposalComputer {
    private static final char[] DGM_CLASS_SIG = Signature.createTypeSignature(DefaultGroovyMethods.class.getCanonicalName(), true).toCharArray();;
    private boolean isGuessArguments;
    
    public DefaultGroovyMethodProcessor() {
        IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
        isGuessArguments= preferenceStore.getBoolean(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS);
    }
    
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
	    if (!(context instanceof JavaContentAssistInvocationContext)) {
	        return Collections.EMPTY_LIST;
	    }
	    
	    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

	    // don't do groovy content assist for Java files
	    if (! (javaContext.getCompilationUnit() instanceof GroovyCompilationUnit)) {
	        return Collections.EMPTY_LIST;
	    }
	    
	    int offset = context.getInvocationOffset();
	    if (offset - 1 < 0) {
			return Collections.EMPTY_LIST;
		}
		
		ISourceBuffer buffer = new DocumentSourceBuffer(context.getDocument());
		
		// Fix for GROOVY-1830: oddly no NPE in current release, but cleaning up acceptible completions here too.
		// FUTURE: emp - extend contexts to be aware of the code they are in to deal with scripts vs classes. This would
		// really be easier with a custom parser than hacking around searching for imports. 
		// This is hacky - Groovy scripts include the imports area into the current 'class'.
		// We don't want to try complete on imports (thinks 'com.' is a dynamic property), so need to see if this is an import line.
		String regex = "$[.\\w\\s]*" + new StringBuffer("import").reverse().toString() + "\\s*";
		ReverseSourceBuffer reverseBuffer = new ReverseSourceBuffer(buffer, offset - 1);
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(reverseBuffer);
		if (matcher.find()) {
			return Collections.EMPTY_LIST;
		}

		ExpressionFinder finder = new ExpressionFinder();
		String expression = findCompletionExpression(finder, offset, buffer);

		// Not a completable expression - done.
		if (expression == null) {
			expression = "";
		}

		// Find the context within the file being editied.
        ModuleNode moduleNode = getCurrentModuleNode(javaContext);

        // Move offset to beginning of expression. This way the context is more likely to match the AST before edits.
        ISourceCodeContext[] contexts = createContexts(moduleNode, buffer, offset - expression.length());

        if (contexts.length == 0) {
            return Collections.EMPTY_LIST;
        }
        
        // Make sure the scope is correct for this completion processor.
        boolean foundClassScope = false;
        for (int i = 0; i < contexts.length; ++i) {
            if (contexts[i].getId() == ISourceCodeContext.CLASS
                    || contexts[i].getId() == ISourceCodeContext.CLASS_SCOPE) {
                foundClassScope = true;
                break;
            }
        }
        if (!foundClassScope) {
            return Collections.EMPTY_LIST;
        }

        // only create the proposals if the current context is in a method or closure
        ISourceCodeContext sourceContext = contexts[contexts.length - 1];
        if (! (sourceContext.getId() == ISourceCodeContext.METHOD_SCOPE || 
               sourceContext.getId() == ISourceCodeContext.CONSTRUCTOR_SCOPE || 
               sourceContext.getId() == ISourceCodeContext.CLOSURE_SCOPE)) {
            return Collections.EMPTY_LIST;
        }
        
        // Ready to complete - extract the completion prefix and do it.
        String[] parts = finder.splitForCompletion(expression.trim());
        if (parts != null) {
            
            IMemberLookup lookup = createMemberLookup(javaContext, contexts);
            if (lookup == null) {
                return Collections.EMPTY_LIST;
            }
            ISymbolTable table = SymbolTableRegistry.createSymbolTable(contexts);
            ((IGroovyProjectAware) table).setGroovyProject(new GroovyProjectFacade(javaContext.getProject()));
            ((IGroovyProjectAware) lookup).setGroovyProject(new GroovyProjectFacade(javaContext.getProject()));

     
            // There is just an identifier, assume it is a field or method name and add 'this.' or 'ClassName.'.
            boolean inStaticContext = true;
            if (parts[1] == null) {
                ASTNode[] path = sourceContext.getASTPath();
                for (int i = 0; i < path.length; i++) {
                    if (path[i] instanceof MethodNode) {
                        MethodNode node = (MethodNode) path[i];
                        parts[1] = parts[0];
                        if (node.isStatic()) {
                            parts[0] = node.getDeclaringClass().getNameWithoutPackage();
                            inStaticContext = true;
                        } else {
                            parts[0] = "this";
                            inStaticContext = false;
                        }
                    }
                }
            }
            TypeEvaluator eval;
            
            try {
                ITypeEvaluationContext typeContext = new TypeEvaluationContextBuilder()
                        .location(new Region(offset, 0))
                        .sourceCodeContext(contexts[contexts.length - 1])
                        .symbolTable(table)
                        .memberLookup(lookup)
                        .classLoader(new GroovyProjectFacade(javaContext.getProject()).getProjectClassLoader())
                        .imports(new String[0])
                        .done();
                eval = new TypeEvaluator(typeContext);
                EvalResult result  = null;
                result = eval.evaluate(parts[0]);
                String type = box(result.getName());

                
                // Move offset to beginning of expression. This way the context is more likely to match the AST before edits.
                Method[] methods = lookup.lookupMethods(result.getName(), parts[1], false, result.isClass(), false);
                
                return createCompletionProposals(methods, offset, parts[1]  == null ? parts[0] : parts[1], javaContext, inStaticContext);
            } catch (Exception e) {
                Activator.logError(e);
            }

        }
        return Collections.EMPTY_LIST;
	}
	
	
   protected IMemberLookup createMemberLookup(JavaContentAssistInvocationContext javaContext, ISourceCodeContext[] contexts) {
        try {
            IType type = javaContext.getProject().findType(DefaultGroovyMethods.class.getName(), new NullProgressMonitor());
            IMemberLookup dgmLookup = new TypeCategoryLookup(type);
            return dgmLookup;
        } catch (JavaModelException e) {
            Activator.logError("Error creating member lookup for project", e);
        }
        return null;
   }

    private List<ICompletionProposal> createCompletionProposals(Method[] methods, int offset, String expression, JavaContentAssistInvocationContext javaContext, boolean inStaticContext) {
        int replaceLength = expression.length();
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(methods.length);
		for (Method method : methods) {
			String replaceString = createReplaceString(method);
			if (replaceString.startsWith(expression)) {
                CompletionProposal proposal = CompletionProposal.create(CompletionProposal.METHOD_REF, offset);
                proposal.setCompletion(replaceString.toCharArray());
                proposal.setDeclarationSignature(DGM_CLASS_SIG);
                proposal.setName(method.getName().toCharArray());
                proposal.setParameterNames(createParameterNames(method));
                ReflectionUtils.setPrivateField(InternalCompletionProposal.class, "parameterTypeNames", proposal, createParameterTypeNames(method));
                proposal.setReplaceRange(offset - replaceLength, offset);
                proposal.setFlags(method.getModifiers());
                proposal.setKey(getMethodSignature(method));
                proposal.setAdditionalFlags(CompletionFlags.Default);
                proposal.setSignature(getMethodSignature(method));  // this one might be a real type signature
                proposal.setRelevance(1);
                if (inStaticContext) {
                    proposal.setFlags(proposal.getFlags() | Flags.AccStatic);
                }
                if (javaContext.getCoreContext().isExtended()) {
                    proposals.add(ParameterGuessingProposal.createProposal(proposal, javaContext, isGuessArguments));
                } else {
                    proposals.add(new JavaMethodCompletionProposal(proposal, javaContext));
                }
			}
		}
		return proposals;
	}



    protected String findCompletionExpression(ExpressionFinder finder, int offset, ISourceBuffer buffer) {
        try{
            return finder.findForCompletions(buffer, offset - 1);
        } catch (ParseException e) {
            // can ignore.  probably just invalid code that is being completed at
            GroovyCore.trace("Cannot complete code:" + e.getMessage());
        }
        return null;
    }

    private char[][] createParameterTypeNames(Method method) {
        char[][] typeNames = new char[method.getParameters().length][];
        int i = 0;
        for (Parameter param : method.getParameters()) {
//            typeNames[i] = Signature.createTypeSignature(param.getSignature(), true).toCharArray();
            typeNames[i] = param.getSignature().toCharArray();
            i++;
        }
        return typeNames;
    }

    private char[] getMethodSignature(Method method) {
        String returnTypeSig = Signature.createTypeSignature(method.getReturnType(), true);
        Parameter[] params = method.getParameters();
        String[] paramTypeSigs = new String[params.length];
        for (int i = 0; i < paramTypeSigs.length; i++) {
//            paramTypeSigs[i] = Signature.createTypeSignature(params[i].getSignature(), true);
            paramTypeSigs[i] = params[i].getSignature();
        }
        return Signature.createMethodSignature(paramTypeSigs, returnTypeSig).toCharArray();
    }

    private char[][] createParameterNames(Method method) {
        Parameter[] params = method.getParameters();
        int numParams = params.length;
        char[][] paramNames = new char[numParams][];
        for (int i = 0; i < numParams; i++) {
            paramNames[i] = (params[i].getName()).toCharArray();
        }
        return paramNames;
    }
    
    
    protected Image getImageForType(Method method) {
        int modifiers = method.getModifiers();
        if (test(modifiers, Modifiers.ACC_PUBLIC)) {
            return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
        } else if (test(modifiers, Modifiers.ACC_PROTECTED)) {
            return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);
        } else if (test(modifiers, Modifiers.ACC_PRIVATE)) {
            return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE);
        }
        return JavaPluginImages.get(JavaPluginImages.IMG_MISC_DEFAULT);
    }

    protected boolean test(int flags, int mask) {
        return (flags & mask) != 0;
    }

    protected String createReplaceString(Method method) {
        return method.getName() + "()";
    }
    
    protected ModuleNode getCurrentModuleNode(JavaContentAssistInvocationContext context) {
        return ((GroovyCompilationUnit) context.getCompilationUnit()).getModuleNode();
    }

    protected ISourceCodeContext[] createContexts(ModuleNode moduleNode, ISourceBuffer buffer, int offset) {
        SourceCodeContextFactory factory = new SourceCodeContextFactory();
        return factory.createContexts(buffer, moduleNode, new Region(offset, 0));
    }

    private static final List<IContextInformation> NO_CONTEXTS= Collections.emptyList();
    public List<IContextInformation> computeContextInformation(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return NO_CONTEXTS;
    }

    public String getErrorMessage() {
        return null;
    }

    public void sessionEnded() { }

    public void sessionStarted() { }

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
}
