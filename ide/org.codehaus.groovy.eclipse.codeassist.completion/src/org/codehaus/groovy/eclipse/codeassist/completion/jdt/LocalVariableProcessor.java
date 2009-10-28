package org.codehaus.groovy.eclipse.codeassist.completion.jdt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.core.DocumentSourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @author empovazan
 * @created May 22, 2009
 * 
 * This local variable processor should only be activated in a script context or in a closure context
 * Otherwise, Java completion will do the same job
 */
public class LocalVariableProcessor extends AbstractGroovyCompletionProcessor {
    private ISourceCodeContext sourceContext;
    
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        if (! (context instanceof JavaContentAssistInvocationContext)) {
            return Collections.EMPTY_LIST;
        }
        int offset = context.getInvocationOffset();
        if (offset - 1 < 0) {
            return Collections.EMPTY_LIST;
        }
        
        JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
        // don't do groovy content assist for Java files
        if (! (javaContext.getCompilationUnit() instanceof GroovyCompilationUnit)) {
            return Collections.EMPTY_LIST;
        }

        
        ModuleNode moduleNode = getCurrentModuleNode(javaContext);
        if (moduleNode == null) {
            return Collections.EMPTY_LIST;
        }
        
        ExpressionFinder finder = new ExpressionFinder();
        DocumentSourceBuffer buffer = new DocumentSourceBuffer(context.getDocument());
        String expr = findCompletionExpression(finder, offset, buffer);
        
        // No text found for expression
        if (expr == null) {
            //return null
            expr = "";
        }

        ISourceCodeContext[] contexts = createContexts(moduleNode, buffer, offset - expr.length());
        
        // Set the current context
        // should only be active in script and closure contexts
        sourceContext = contexts[contexts.length-1];
        if (!isScriptOrClosureContext(sourceContext)) {
            return Collections.EMPTY_LIST;
        }
                
        // Now we know expr is the expression to attempt to complete into a class, doit.
        
        Map<String,ClassNode> localNameTypes = completeLocalNames(expr, contexts);
        
        return createProposals(localNameTypes, offset - expr.length(), expr.length(), javaContext);
    }


    /**
     * Java search works on Groovy types too, and since only the type names are required, Java search will do.
     */
    private Map<String,ClassNode> completeLocalNames(String prefix, ISourceCodeContext[] contexts) {
         Map<String,ClassNode> nameTypeMap = new HashMap<String,ClassNode>();

         VariableScope scope = getVariableScope();
         while (scope != null) {
             for (Iterator<Variable> varIter = scope.getDeclaredVariablesIterator(); varIter.hasNext();) {
                 Variable var = (Variable) varIter.next();
                if (TypeUtil.looselyMatches(prefix, var.getName())) {
                    nameTypeMap.put(var.getName(), var.getType());
                }
             }
             scope = scope.getParent();
         }
         
         return nameTypeMap;
    }
    private VariableScope getVariableScope() {
        ASTNode astNode = sourceContext.getASTPath()[sourceContext.getASTPath().length-1];
        if (astNode instanceof MethodNode) {
            astNode = ((MethodNode) astNode).getCode();
        } else if (astNode instanceof ClosureExpression) {
            astNode = ((ClosureExpression) astNode).getCode();
        } else if (astNode instanceof MethodCallExpression) {
            ArgumentListExpression ale = (ArgumentListExpression) 
                    ((MethodCallExpression) astNode).getArguments();
            if (ale.getExpression(0) instanceof ClosureExpression) {
                astNode = ((ClosureExpression) ale.getExpression(0)).getCode();
            }
        }
        try {
            // copied from groovy code that doesn't care abot
            // static type
            Method getVariableScopeField = astNode.getClass().getMethod("getVariableScope");
            return (VariableScope) getVariableScopeField.invoke(astNode);
        } catch (Exception e) {
            return null;
        }
    }
    
    
    
    private List<ICompletionProposal> createProposals(Map<String,ClassNode> nameTypes, int offset, int replaceLength, JavaContentAssistInvocationContext context) {
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
        for (Entry<String,ClassNode> nameType : nameTypes.entrySet()) {
            String replaceName = nameType.getKey();
            CompletionProposal proposal = CompletionProposal.create(CompletionProposal.LOCAL_VARIABLE_REF, offset);
            proposal.setCompletion(replaceName.toCharArray());
            proposal.setReplaceRange(offset, offset + replaceLength);
            proposal.setSignature(Signature.createTypeSignature(nameType.getValue().getName(), true).toCharArray());
            proposal.setRelevance(25);
            proposals.add(new LazyJavaCompletionProposal(proposal, context));
        }
    
        return proposals;
    }
}