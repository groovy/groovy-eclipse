/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.fix.UnimplementedCodeFix;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.fix.UnimplementedCodeCleanUp;
import org.eclipse.jdt.internal.ui.text.correction.proposals.FixCorrectionProposal;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author Andrew Eisenberg
 * @created Oct 14, 2011
 */
public class AddUnimplementedResolver extends AbstractQuickFixResolver {

    protected AddUnimplementedResolver(QuickFixProblemContext problem) {
        super(problem);
    }

    public List<IJavaCompletionProposal> getQuickFixProposals() {
        
        Collection<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>(2);
        addUnimplementedMethodsProposals(
                getQuickFixProblem().getContext(), getQuickFixProblem().getLocation(), proposals);
        List<IJavaCompletionProposal> newProposals = new ArrayList<IJavaCompletionProposal>();
        for (Object command : proposals) {
            if (command instanceof IJavaCompletionProposal) {
                newProposals.add((IJavaCompletionProposal) command);
            }
        }
        return newProposals;
    }
    
    // Copied from LocalCorrectionsSubProcessor.addUnimplementedMethodsProposals
    // cannot call directly since ICommandAccess has changed packages between 3.7 and 4.2
    public static void addUnimplementedMethodsProposals(IInvocationContext context, IProblemLocation problem, Collection<IJavaCompletionProposal> proposals) {
        IProposableFix addMethodFix= UnimplementedCodeFix.createAddUnimplementedMethodsFix(context.getASTRoot(), problem);
        if (addMethodFix != null) {
            Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);

            Map<String, String> settings= new Hashtable<String, String>();
            settings.put(CleanUpConstants.ADD_MISSING_METHODES, CleanUpOptions.TRUE);
            ICleanUp cleanUp= new UnimplementedCodeCleanUp(settings);

            proposals.add(new FixCorrectionProposal(addMethodFix, cleanUp, 10, image, context));
        }

        IProposableFix makeAbstractFix= UnimplementedCodeFix.createMakeTypeAbstractFix(context.getASTRoot(), problem);
        if (makeAbstractFix != null) {
            Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);

            Map<String, String> settings= new Hashtable<String, String>();
            settings.put(UnimplementedCodeCleanUp.MAKE_TYPE_ABSTRACT, CleanUpOptions.TRUE);
            ICleanUp cleanUp= new UnimplementedCodeCleanUp(settings);

            proposals.add(new FixCorrectionProposal(makeAbstractFix, cleanUp, 5, image, context));
        }
    }
    
    // pacakge of ICommandAccess has changed between 3.7 and 4.2
    

    @Override
    protected ProblemType[] getTypes() {
        return new ProblemType[] { ProblemType.UNIMPLEMENTED_METHODS_TYPE };
    }

}
