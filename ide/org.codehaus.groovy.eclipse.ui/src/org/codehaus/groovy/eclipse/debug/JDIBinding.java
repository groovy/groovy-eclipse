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

import static org.eclipse.jdt.internal.debug.core.model.LambdaUtils.getLambdaFrameVariables;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import groovy.lang.Binding;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * A binding that will properly update fields and variables in the currently debugged application.
 *
 * @author Andrew Eisenberg
 */
public class JDIBinding extends Binding {

    private final JDITargetDelegate delegate;

    JDIBinding(JDITargetDelegate delegate, IJavaStackFrame stackFrame, IJavaObject thisObject) throws DebugException {
        super(variables(delegate, stackFrame, thisObject));
        this.delegate = delegate;

        setMetaClass(new JDIMetaClass(getThis(), delegate));
    }

    private static Map<String, Object> variables(JDITargetDelegate delegate, IJavaStackFrame stackFrame, IJavaObject thisObject) throws DebugException {
        Map<String, Object> map = new HashMap<>();
        if (stackFrame != null) {
            for (IVariable variable : stackFrame.getLocalVariables()) {
                map.put(variable.getName(), variable);
            }
            for (IVariable variable : getLambdaFrameVariables(stackFrame)) {
                var variableName = variable.getName();
                if (variableName.startsWith("val$")) {
                    map.put(variableName.substring(4), variable);
                }
            }
            map.put("this", stackFrame.isStatic() ? stackFrame.getReferenceType().getClassObject() : stackFrame.getThis());
        } else {
            map.put("this", thisObject);
        }
        map.put("__comparator", new JDIComparator(delegate)); // JDIScriptLoader
        return map;
    }

    //--------------------------------------------------------------------------

    IJavaObject getThis() {
        return (IJavaObject) super.getVariable("this");
    }

    @Override
    public Map getVariables() {
        return ((Map<String, Object>) super.getVariables())
            .keySet().stream().filter(name -> !"this".equals(name))
            .collect(Collectors.toMap(name -> name, this::getVariable));
    }

    @Override
    public Object getVariable(String variableName) {
        Object o = super.getVariable(variableName);
        if (o instanceof IVariable) {
            try {
                IJavaValue value = (IJavaValue) ((IVariable) o).getValue();
                return delegate.createProxyFor(value);
            } catch (DebugException e) {
                throw new RuntimeException(e);
            }
        }
        return o;
    }

    @Override
    public void removeVariable(String variableName) {
    }

    @Override
    public void setVariable(String variableName, Object newValue) {
        if ("this".equals(variableName) || "__comparator".equals(variableName)) {
            throw new IllegalArgumentException(variableName);
        }
        try {
            IJavaValue jdiValue = delegate.toJDIObject(newValue);
            if (hasVariable(variableName)) {
                IVariable variable = (IVariable) super.getVariable(variableName);
                if (((IJavaVariable) variable).getSignature().length() == 1) {
                    jdiValue = delegate.toJDIValue(jdiValue); // unbox
                }
                variable.setValue(jdiValue);
            } else {
                getMetaClass().setProperty(getThis(), variableName, jdiValue);
            }
        } catch (DebugException e) {
            throw new RuntimeException(e);
        }
    }
}
