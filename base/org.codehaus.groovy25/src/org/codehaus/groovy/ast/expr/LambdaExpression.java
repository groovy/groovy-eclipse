package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

public class LambdaExpression extends ClosureExpression {

    public LambdaExpression(Parameter[] args, Statement code) {
        super(args, code);
    }

}
