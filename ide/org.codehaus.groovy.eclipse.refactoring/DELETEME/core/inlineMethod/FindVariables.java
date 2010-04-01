/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.core.inlineMethod;

import java.util.List;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTVisitorDecorator;

/**
 * @author mklenk
 *
 */
public class FindVariables extends ASTVisitorDecorator<List<Variable>> {

	public FindVariables(List<Variable> container) {
		super(container);
	}
		
	@Override
    public void visitVariableExpression(VariableExpression expression) {
		container.add(expression);		
		super.visitVariableExpression(expression);
	}
	
	@Override
    public void visitConstantExpression(ConstantExpression expression) {
		Object val = expression.getValue();
		if (val instanceof String) {
			val = "\"" + val + "\"";
		}
		
		container.add(new VariableExpression(val.toString()));
		super.visitConstantExpression(expression);
	}

}
