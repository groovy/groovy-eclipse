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
package org.codehaus.groovy.eclipse.wizards;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.swt.widgets.Composite;

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
	protected void createTypeMembers(IType type, ImportsManager imports,
	        IProgressMonitor monitor) throws CoreException {
	    super.createTypeMembers(type, imports, monitor);
	    if (isCreateMain()) {
	        // replace main method with a more groovy version
	        IMethod main = type.getMethod("main", new String[] {"[QString;"} );
	        if (main != null && main.exists()) {
	            main.delete(true, monitor);
	            type.createMethod("static main(args) {\n\n}", null, true, monitor); 
	        }
	    }
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

   @Override
   public int getModifiers() {
       int modifiers = super.getModifiers();
       modifiers &= ~F_PUBLIC;
       modifiers &= ~F_PRIVATE;
       modifiers &= ~F_PROTECTED;
       return modifiers;
   }
}
