/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation, SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Thorsten Kamann - Initial implementation
 *     MelamedZ - Initial implementation
 *     Matt Chapman - AJDT Implementation of getTypeNameWithoutParameters()
 *     and typeNameChanged()
 *     Andrew Eisenberg - Copied from AJDT 
 *     org.eclipse.ajdt.internal.ui.wizards.NewAspectWizardPage
 *******************************************************************************/
package org.codehaus.groovy.eclipse.wizards;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;

/**
 * @author MelamedZ
 * @author Thorsten Kamann <thorsten.kamann@googlemail.com>
 */
public class NewClassWizardPage extends org.eclipse.jdt.ui.wizards.NewClassWizardPage {	
	
	/**
	 * Creates a new <code>NewClassWizardPage</code>
	 */
	public NewClassWizardPage() {
		super();
		setTitle("Groovy Class"); 
		setDescription("Create a new Groovy class"); 
	}

	@Override
	protected String getCompilationUnitName(String typeName) {
	    return typeName + ".groovy";
	}
	

	@Override
	protected IStatus typeNameChanged() {
        StatusInfo status = (StatusInfo) super.typeNameChanged();
        IPackageFragment pack = getPackageFragment();
        if (pack == null) {
            return status;
        }
        
        IJavaProject project = pack.getJavaProject();
        try {
            if (!project.getProject().hasNature(GroovyNature.GROOVY_NATURE)) {
                status.setError(project.getElementName() + " is not a groovy project");
            }
        } catch (CoreException e) {
            status.setError("Exception when accessing project natures for " + project.getElementName());
        }
        
        // must not exist as a .groovy file
        if (!isEnclosingTypeSelected()
                && (status.getSeverity() < IStatus.ERROR)) {
            if (pack != null) {
                IType type = null;
                try {
                    String typeName = getTypeNameWithoutParameters();
                    type = project.findType(pack.getElementName(), typeName);
                } catch (JavaModelException e) {
                    // can ignore
                }
                if (type != null) {
                    status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExists);
                }
            }
        }
        return status;
	}
	
	
   private String getTypeNameWithoutParameters() {
        String typeNameWithParameters= getTypeName();
        int angleBracketOffset= typeNameWithParameters.indexOf('<');
        if (angleBracketOffset == -1) {
            return typeNameWithParameters;
        } else {
            return typeNameWithParameters.substring(0, angleBracketOffset);
        }
    }

	
}
