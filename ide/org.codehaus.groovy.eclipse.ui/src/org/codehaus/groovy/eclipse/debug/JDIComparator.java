/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug;

import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * Static methods for overriding comparisons in evaluated scripts. This class
 * exists because Groovy's MOP does not let you take control over operations
 * like <, >, ==, etc. Instead, we walk the Script AST looking for these ops
 * and replace them with calls to methods in this class.
 *
 * @author Andrew Eisenberg
 */
public class JDIComparator {

    private final JDITargetDelegate delegate;

    private final IJavaClassType scriptBytecodeAdapter;

    public JDIComparator(JDITargetDelegate delegate) throws DebugException {
        this.delegate = delegate;
        this.scriptBytecodeAdapter = (IJavaClassType) delegate.getType("org.codehaus.groovy.runtime.ScriptBytecodeAdapter");
    }

    public Integer compareTo(Object o1, Object o2) throws DebugException {
        IJavaValue v1 = delegate.toJDIObject(o1);
        IJavaValue v2 = delegate.toJDIObject(o2);
        IJavaValue result = scriptBytecodeAdapter.sendMessage("compareTo",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Integer;", new IJavaValue[] {v1, v2}, delegate.getThread());
        return delegate.convertToInteger(result);
    }

    private boolean invoke(String methodName, Object o1, Object o2) throws DebugException {
        IJavaValue v1 = delegate.toJDIObject(o1);
        IJavaValue v2 = delegate.toJDIObject(o2);
        IJavaPrimitiveValue result = (IJavaPrimitiveValue) scriptBytecodeAdapter.sendMessage(methodName,
            "(Ljava/lang/Object;Ljava/lang/Object;)Z", new IJavaValue[] {v1, v2}, delegate.getThread());
        return result.getBooleanValue();
    }

    public boolean isEqual(Object o1, Object o2) throws DebugException {
        return invoke("compareEqual", o1, o2);
    }

    public boolean isGreaterThan(Object o1, Object o2) throws DebugException {
        return invoke("compareGreaterThan", o1, o2);
    }

    public boolean isGreaterThanOrEqual(Object o1, Object o2) throws DebugException {
        return invoke("compareGreaterThanEqual", o1, o2);
    }

    public boolean isIdentical(Object o1, Object o2) throws DebugException {
        return invoke("compareIdentical", o1, o2);
    }

    public boolean isLessThan(Object o1, Object o2) throws DebugException {
        return invoke("compareLessThan", o1, o2);
    }

    public boolean isLessThanOrEqual(Object o1, Object o2) throws DebugException {
        return invoke("compareLessThanEqual", o1, o2);
    }

    public boolean isNotEqual(Object o1, Object o2) throws DebugException {
        return invoke("compareNotEqual", o1, o2);
    }

    public boolean isNotIdentical(Object o1, Object o2) throws DebugException {
        return invoke("compareNotIdentical", o1, o2);
    }

    static String methodNameFor(Token operation) {
        switch (operation.getType()) {
        case Types.COMPARE_LESS_THAN:
            return "isLessThan";

        case Types.COMPARE_GREATER_THAN:
            return "isGreaterThan";

        case Types.COMPARE_LESS_THAN_EQUAL:
            return "isLessThanOrEqual";

        case Types.COMPARE_GREATER_THAN_EQUAL:
            return "isGreaterThanOrEqual";

        case Types.COMPARE_EQUAL:
            return "isEqual";

        case Types.COMPARE_NOT_EQUAL:
            return "isNotEqual";

        case Types.COMPARE_IDENTICAL:
            return "isIdentical";

        case Types.COMPARE_NOT_IDENTICAL:
            return "isNotIdentical";

        case Types.COMPARE_TO:
            return "compareTo";

        default:
            return null;
        }
    }
}
