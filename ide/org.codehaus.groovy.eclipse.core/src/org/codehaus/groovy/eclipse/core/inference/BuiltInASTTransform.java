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
package org.codehaus.groovy.eclipse.core.inference;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;
import org.objectweb.asm.Opcodes;

/**
 * Provides context information about the standard AST transforms.  Currently,
 * only Singleton is supported.
 *
 * This is used for inferencing and for content assist.
 *
 *
 * @author Andrew Eisenberg
 * @created May 4, 2010
 */
public abstract class BuiltInASTTransform {
    private static class SingletonASTTransform extends BuiltInASTTransform {

        static final String NAME = "groovy.lang.Singleton";

        private MethodNode singletonMethod;
        private FieldNode singletonField;
        private SingletonASTTransform(ClassNode thisNode) {
            super(thisNode);
        }

        static BuiltInASTTransform create(ClassNode declaringType) {
            if (declaringType != null) {
                try {
                    List<AnnotationNode> annotations = declaringType.getAnnotations();
                    for (AnnotationNode annotation : annotations) {
                        if (annotation.getClassNode().getName().equals(SingletonASTTransform.NAME)) {
                            return new SingletonASTTransform(declaringType);
                        }
                    }
                } catch (Throwable t) {
                    // STS-1255 NoClassDefError being thrown here. Ensure that
                    // this does not prevent normal
                    // functioning of inferencing
                    GroovyCore.logException("Error trying to find annotations on " + declaringType.getName(), t);
                }
            }
            return null;
        }

        @Override
        public TypeAndDeclaration symbolToDeclaration(String symbol) {
            if (thisClass instanceof ClassNode) {
                if (symbol.equals("getInstance")) {
                    return new TypeAndDeclaration(getSingletonMethod().getReturnType(), getSingletonMethod());
                }
                if (symbol.equals("instance")) {
                    getSingletonMethod();
                    return new TypeAndDeclaration(getSingletonField().getType(), getSingletonField());
                }
            }
            return null;
        }

        @Override
        public Collection<? extends AnnotatedNode> allIntroducedDeclarations() {
            return ListUtil.linked((AnnotatedNode) getSingletonField(), (AnnotatedNode) getSingletonMethod());
        }

        private MethodNode getSingletonMethod() {
            if (singletonMethod == null) {
                singletonMethod = createSingletonMethod();
            }
            return singletonMethod;
        }
        private FieldNode getSingletonField() {
            if (singletonField == null) {
                singletonField = createSingletonField();
            }
            return singletonField;
        }

        private FieldNode createSingletonField() {
            FieldNode f = new FieldNode("instance", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, (ClassNode) thisClass, (ClassNode) thisClass, null);
            f.setDeclaringClass(thisClass);
            return f;
        }

        private MethodNode createSingletonMethod() {
            MethodNode m = new MethodNode("getInstance", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    (ClassNode) thisClass, new Parameter[0], new ClassNode[0], new BlockStatement());
            m.setDeclaringClass(thisClass);
            return m;
        }

        @Override
        public String prettyName() {
            return "Singleton annotation";
        }
    }

    // GRECLIPSE-709 only singleton is supported
//    private static class DelegateASTTransform extends BuiltInASTTransform {
//
//        private static String NAME = "groovy.lang.Delegate";
//
//        List<FieldNode> delegates;
//        Map<String, MethodNode> delegateMap;
//
//        DelegateASTTransform(ClassNode thisClass, List<FieldNode> delegates) {
//            super(thisClass);
//            this.delegates = delegates;
//        }
//
//        static BuiltInASTTransform create(ClassNode declaringType) {
//            if (declaringType != null) {
//                List<FieldNode> fields = declaringType.getFields();
//                List<FieldNode> foundFields = new LinkedList<FieldNode>();
//                for (FieldNode field : fields) {
//                    List<AnnotationNode> annotations = field.getAnnotations();
//                    for (AnnotationNode annotation : annotations) {
//                        if (annotation.getClassNode().getName().equals(DelegateASTTransform.NAME)) {
//                            foundFields.add(field);
//                            break;
//                        }
//                    }
//                }
//                if (foundFields.size() > 0) {
//                    return new DelegateASTTransform(declaringType, foundFields);
//                }
//            }
//            return null;
//        }
//
//        @Override
//        public TypeAndDeclaration symbolToDeclaration(String symbol) {
//            initialize();
//            MethodNode m = delegateMap.get(symbol);
//            if (m != null) {
//                return new TypeAndDeclaration(thisClass, m);
//            }
//            return null;
//        }
//
//        @Override
//        public Collection<? extends AnnotatedNode> allIntroducedDeclarations() {
//            initialize();
//            return delegateMap.values();
//        }
//
//        private void initialize() {
//            if (delegateMap == null) {
//                delegateMap = new HashMap<String, MethodNode>();
//                for (FieldNode field : delegates) {
//                    ClassNode delegateType = field.getType();
//                    List<MethodNode> origMethods = delegateType.getMethods();
//                    for (MethodNode orig : origMethods) {
//                        delegateMap.put(orig.getName(), toDelegate(orig));
//                    }
//                }
//            }
//        }
//
//        /**
//         * @param orig
//         * @return
//         */
//        private MethodNode toDelegate(MethodNode orig) {
//            MethodNode delegate = new MethodNode(orig.getName(), orig.getModifiers(), orig.getReturnType(), orig.getParameters(), orig.getExceptions(), orig.getCode());
//            delegate.setDeclaringClass(thisClass);
//            return delegate;
//        }
//
//        @Override
//        public String prettyName() {
//            return "Delegate annotation";
//        }
//    }

    final ClassNode thisClass;
    BuiltInASTTransform(ClassNode thisClass) {
        this.thisClass = thisClass;
    }
    public abstract TypeAndDeclaration symbolToDeclaration(String symbol);
    public abstract Collection<? extends AnnotatedNode> allIntroducedDeclarations();
    public abstract String prettyName();

    public static BuiltInASTTransform[] createAll(ClassNode declaringType) {
        List<BuiltInASTTransform> transforms = new LinkedList<BuiltInASTTransform>();
        BuiltInASTTransform candidate = SingletonASTTransform.create(declaringType);
        if (candidate != null) {
            transforms.add(candidate);
        }
//        candidate = DelegateASTTransform.create(declaringType);
//        if (candidate != null) {
//            transforms.add(candidate);
//        }
        return transforms.toArray(new BuiltInASTTransform[transforms.size()]);
    }
    /**
     * @return
     */
}