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
package org.codehaus.groovy.eclipse.codebrowsing.impl;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchInfo;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchProcessor;
import org.codehaus.groovy.eclipse.core.DocumentSourceBuffer;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.inference.internal.SourceContextInfo;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
import org.codehaus.groovy.eclipse.core.types.ClassType;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.Property;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * @author emp
 * @author andrew
 */
public class MethodCallExpressionProcessor implements
		IDeclarationSearchProcessor {
	protected ISourceCodeContext[] createContexts(IDeclarationSearchInfo info) {
        IDocumentFacade facade = info.getEditorFacade();
        IRegion r = info.getRegion();
        ISourceBuffer buffer = new DocumentSourceBuffer(facade.getDocument());
        SourceCodeContextFactory factory = new SourceCodeContextFactory();
        return factory.createContexts(buffer, facade.adapt(ModuleNode.class), r);
    }

    public IJavaElement[] getProposals(IDeclarationSearchInfo info) {
        
        ISourceBuffer buffer = new DocumentSourceBuffer(info.getEditorFacade().getDocument());
        int offset = info.getRegion().getOffset() + info.getRegion().getLength();
        GroovyProjectFacade project = info.getEditorFacade().getProjectFacade();
        
        SourceContextInfo sourceInfo = SourceContextInfo.create(info.getModuleNode(), project, offset, buffer, false);
        if (sourceInfo != null) {
            // FIXADE M2 we need a way to figure out if we are looking at property on a class or not
            // for now, assume that upper case first letter is a class.
            boolean isClass = Character.isUpperCase(sourceInfo.expression.charAt(0));
            String targetType = isClass ? sourceInfo.expression : sourceInfo.eval.getName();
            
            List<IJavaElement> elts = new ArrayList<IJavaElement>();
            try {
                Field[] fields = sourceInfo.lookup.lookupFields(targetType, sourceInfo.name, false, isClass, true);
                for (Field field : fields) {
                    ClassType declaring = field.getDeclaringClass();
                    IType type = project.getProject().findType(declaring.getName(), new NullProgressMonitor());
                    if (type != null && type.exists()) {
                        IField javaField = type.getField(field.getName());
                        if (javaField.exists()) {
                            elts.add(javaField);
                        }
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Error while code browsing on " + getMainClassName(info), e);
            }

            try {
                Method[] methods = sourceInfo.lookup.lookupMethods(targetType, sourceInfo.name, false, isClass, true);
                for (Method method : methods) {
                    ClassType declaring = method.getDeclaringClass();
                    IType type = project.getProject().findType(declaring.getName(), new NullProgressMonitor());
                    if (type != null && type.exists()) {
                        List<String> paramTypeSigs = new ArrayList<String>(method.getParameters().length);
                        for (Parameter param : method.getParameters()) {
                            paramTypeSigs.add(param.getSignature());
                        }
                        String[] paramTypeSigsArr;
                        if (!type.isReadOnly()) {
                            // source type, must convert method type signatures to be unresolved
                            // FIXADE M2 this could be a problem for source types that do not use qualified names
                            // maybe need to do a different way of finding method
                            paramTypeSigsArr = paramTypeSigs.toArray(new String[0]);
                            for (int i = 0; i < paramTypeSigsArr.length; i++) {
                                if (paramTypeSigsArr[i].charAt(0) == 'L') {
                                    paramTypeSigsArr[i] = "Q" + paramTypeSigsArr[i].substring(1);
                                }
                            }
                        } else {
                            paramTypeSigsArr = paramTypeSigs.toArray(new String[0]);
                        }
                         
                        IMethod javaMethod = type.getMethod(method.getName(), paramTypeSigsArr);
                        if (javaMethod.exists()) {
                            elts.add(javaMethod);
                        } else {
                            // might be a synthetic method or one added through AST transformations
                            // add the declaring class instead
                            elts.add(type);
                        }
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Error while code browsing on " + getMainClassName(info), e);
            }
            
            try {
                Property[] properties = sourceInfo.lookup.lookupProperties(targetType, sourceInfo.name, false, isClass, true);
                for (Property property : properties) {
                    ClassType declaring = property.getDeclaringClass();
                    IType type = project.getProject().findType(declaring.getName(), new NullProgressMonitor());
                    if (type != null && type.exists()) {
                        IMethod javaMethod = type.getMethod(property.getName(), new String[0]);
                        if (javaMethod.exists()) {
                            elts.add(javaMethod);
                        }
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Error while code browsing on " + getMainClassName(info), e);
            }
            
            return elts.toArray(new IJavaElement[0]);
        } else {
            return new IJavaElement[0];
        }
    }

    /**
     * @param info
     */
    private String getMainClassName(IDeclarationSearchInfo info) {
        try {
            return ((ClassNode) info.getModuleNode().getClasses().get(0)).getName();
        } catch (Exception e) {
            // probably no classes in this module node
            return info.getModuleNode().getPackageName();
        }
    }
    
    protected ISourceCodeContext[] createContexts(ModuleNode moduleNode, ISourceBuffer buffer, int offset) {
        SourceCodeContextFactory factory = new SourceCodeContextFactory();
        return factory.createContexts(buffer, moduleNode, new Region(offset, 0));
    }
}