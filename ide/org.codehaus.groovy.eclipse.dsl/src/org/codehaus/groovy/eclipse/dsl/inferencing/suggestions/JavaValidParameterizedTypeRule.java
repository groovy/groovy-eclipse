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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;

/**
 * Handles primitive types as well.
 * 
 * @author Nieraj Singh
 * @created 2011-09-13
 */
public class JavaValidParameterizedTypeRule extends AbstractJavaTypeVerifiedRule {

    public JavaValidParameterizedTypeRule(IJavaProject project) {
        super(project);
    }

    public ValueStatus checkValidity(Object value) {
        if (!(value instanceof String)) {
            return ValueStatus.getErrorStatus(value);
        }
        String typeToCheck = (String) value;

        // This only checks if the value is syntactically correct Java. It does
        // not check if
        // the type or the parameters of the parameterised type exists. Further
        // checks need to be performed below.
        StringBuffer source = new StringBuffer();

        Type astType = getASTType(typeToCheck, source);

        // Check the parameters to see if they are valid Java types. The
        // ASTNode only checks for syntactic
        // correctness, not whether the type actually exists or not,
        // therefore an IType is needed.
        List<String> allNonExistantTypes = new ArrayList<String>();
        if (astType != null) {
            try {
                boolean isValid = allTypesExist(astType, source, allNonExistantTypes);
                if (!isValid) {
                    String message = composeErrorMessage(allNonExistantTypes);
                    return ValueStatus.getErrorStatus(value, message);
                } else {
                    return ValueStatus.getValidStatus(value);
                }

            } catch (JavaModelException e) {
                GroovyDSLCoreActivator.logException(e);
                return ValueStatus.getErrorStatus(value, e.getLocalizedMessage());
            }

        }
        return ValueStatus.getErrorStatus(value, INVALID_JAVA);
    }

    protected String getTypeName(Type type, StringBuffer source) {
        String name = null;
        name = source.substring(type.getStartPosition(), ASTNodes.getExclusiveEnd(type));
        if (type instanceof ParameterizedType) {

            // Strip everything after the first <
            int index = name.indexOf('<');
            if (index >= 0) {
                name = name.substring(0, index);
            }
        }
        return name;
    }

    protected boolean allTypesExist(Type type, StringBuffer source, List<String> nonExistantTypes) throws JavaModelException {

        // Type exists, but must still check parameters if it is a
        // parameterized type
        String typeName = getTypeName(type, source);
        IType actualType = getActualType(typeName);
        if (actualType != null) {
            if (type instanceof ParameterizedType) {
                List<?> parameterisedNodes = ((ParameterizedType) type).typeArguments();
                // Must check the actual arguments for the parameterised
                // type to see if they exists

                if (parameterisedNodes != null) {
                    boolean allParamsValid = true;
                    for (Object node : parameterisedNodes) {
                        if (!(node instanceof Type) || !allTypesExist((Type) node, source, nonExistantTypes)) {
                            allParamsValid = false;
                            break;
                        }
                    }
                    return allParamsValid;
                }
            }
            return true;
        } else if (type instanceof PrimitiveType || type instanceof ArrayType) {
            return true;
        } else {
            if (nonExistantTypes != null && !nonExistantTypes.contains(typeName)) {
                nonExistantTypes.add(typeName);
            }
        }
        return false;
    }

    protected Type getASTType(String typeToCheck, StringBuffer sourceBuffer) {

        sourceBuffer.append("class __C__ {"); //$NON-NLS-1$

        int valueOffset = sourceBuffer.length();

        sourceBuffer.append(typeToCheck).append(" v;");

        sourceBuffer.append("}");

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(sourceBuffer.toString().toCharArray());

        Map<String, String> options = new HashMap<String, String>();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        parser.setCompilerOptions(options);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        ASTNode selected = NodeFinder.perform(cu, valueOffset, typeToCheck.length());
        if (selected instanceof Name) {
            selected = selected.getParent();
        }
        if (selected instanceof Type) {
            return (Type) selected;
        }
        return null;
    }
}
