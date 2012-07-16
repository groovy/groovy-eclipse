/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.contributions;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;

/**
 * a parameter of a method contribution
 * @author andrew
 * @created Nov 17, 2010
 */
public class ParameterContribution {
    final String name;
    final String type;
    private Parameter cachedParameter;

    public ParameterContribution(String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    public ParameterContribution(Parameter cachedParameter) {
        this.cachedParameter = cachedParameter;
        this.name = cachedParameter.getName();
        this.type = DSLContributionGroup.getTypeName(cachedParameter.getType());
    }
    
    public ParameterContribution(String name) {
        this.name = name;
        this.type = null;
    }
    
    public Parameter toParameter(ResolverCache resolver) {
        if (cachedParameter == null) {
            if (resolver != null) {
                cachedParameter = new Parameter(resolver.resolve(type), name);
            } else {
                cachedParameter = new Parameter(ClassHelper.DYNAMIC_TYPE, name);
            }
        }
        return cachedParameter;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
    
    
}
