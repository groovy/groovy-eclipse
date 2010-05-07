package org.codehaus.groovy.eclipse.codeassist.proposals;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.processors.IProposalProvider;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.core.inference.BuiltInASTTransform;

public class StandardASTTransformProposalProvider implements IProposalProvider {
    
    private BuiltInASTTransform[] transforms;
    
    public StandardASTTransformProposalProvider() {
    }

    public List<String> getNewFieldProposals(ContentAssistContext context) {
        return null;
    }

    public List<MethodNode> getNewMethodProposals(ContentAssistContext context) {
        return null;
    }

    public List<IGroovyProposal> getStatementAndExpressionProposals(
            ContentAssistContext context, ClassNode completionType,
            boolean isStatic, Set<ClassNode> categories) {
        transforms = BuiltInASTTransform.createAll(completionType);
        List<IGroovyProposal> newProposals = new LinkedList<IGroovyProposal>();
        for (BuiltInASTTransform transform : transforms) {
            Collection<? extends AnnotatedNode> decls = transform.allIntroducedDeclarations();
            for (AnnotatedNode decl : decls) {
                if (decl instanceof MethodNode && ((MethodNode) decl).getName().startsWith(context.completionExpression)) {
                    newProposals.add(new GroovyMethodProposal((MethodNode) decl, transform.prettyName()));
                } else if (decl instanceof FieldNode && ((FieldNode) decl).getName().startsWith(context.completionExpression)) {
                    newProposals.add(new GroovyFieldProposal((FieldNode) decl, transform.prettyName()));
                }
            }
        }
        return newProposals;
    }

}
