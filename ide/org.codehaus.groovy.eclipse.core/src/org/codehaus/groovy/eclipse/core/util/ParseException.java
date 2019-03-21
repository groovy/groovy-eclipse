/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.core.util;

/**
 * Exception thrown by the ExpressionFinder if the expression cannot be parsed.
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = -3475185632147480495L;

    private Token token;

    public ParseException(Token token) {
        super("Unexpected token: " + token.toString());
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
