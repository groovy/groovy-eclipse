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
package org.codehaus.groovy.eclipse.quickassist;

import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;

class StringConstantFinder extends ASTNodeFinder {

    public StringConstantFinder(Region r) {
        super(r);
    }
    
    @Override
    public void visitGStringExpression(GStringExpression node) {
    	// if the selection is inside of a GString, then we don't
    	// want to check contained elements.  Either the entire 
    	// node matches or it doesn't
    	check(node);
    }
}