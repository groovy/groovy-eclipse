/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.pointcuts;

/**
 * Thrown when an invalid pointcut is found during the pointcut verification phase.
 * 
 * FIXADE It would be nice if I could somehow get the line number in here.
 * @author andrew
 * @created Mar 9, 2011
 */
public class PointcutVerificationException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private final IPointcut pointcut;

    public PointcutVerificationException(String message, IPointcut pointcut) {
        super(message);
        this.pointcut = pointcut;
    }

    public IPointcut getPointcut() {
        return pointcut;
    }

    /**
     * Combines the message and the pointcut to create a useful error message for users
     * @return
     */
    public String getPointcutMessage() {
        return "Invalid Pointcut: " + pointcut.getPointcutDebugName() + 
               "\nin: " + pointcut.getContainerIdentifier() + 
               "\nreason: " + getMessage();
    }
}
