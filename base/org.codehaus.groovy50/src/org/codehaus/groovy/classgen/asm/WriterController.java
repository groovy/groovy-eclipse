/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InterfaceHelperClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.indy.IndyBinHelper;
import org.codehaus.groovy.classgen.asm.indy.IndyCallSiteWriter;
import org.codehaus.groovy.classgen.asm.indy.InvokeDynamicWriter;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import groovyjarjarasm.asm.ClassVisitor;
import groovyjarjarasm.asm.Label;
import groovyjarjarasm.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.ClassNodeUtils.getNestHost;
import static org.codehaus.groovy.ast.ClassHelper.isGeneratedFunction;

public class WriterController {
    private AsmClassGenerator acg;
    private MethodVisitor methodVisitor;
    private CompileStack compileStack;
    private OperandStack operandStack;
    private ClassNode classNode;
    private CallSiteWriter callSiteWriter;
    private ClassVisitor cv;
    private ClosureWriter closureWriter;
    private LambdaWriter lambdaWriter;
    private String internalClassName;
    private InvocationWriter invocationWriter;
    private BinaryExpressionHelper binaryExpHelper, fastPathBinaryExpHelper;
    private UnaryExpressionHelper unaryExpressionHelper, fastPathUnaryExpressionHelper;
    private AssertionWriter assertionWriter;
    private String internalBaseClassName;
    private MethodNode methodNode;
    private ConstructorNode constructorNode;
    private GeneratorContext context;
    private InterfaceHelperClassNode interfaceClassLoadingClass;
    public boolean optimizeForInt = true;
    private StatementWriter statementWriter;
    private boolean fastPath;
    private TypeChooser typeChooser;
    private int bytecodeVersion = CompilerConfiguration.DEFAULT.getBytecodeVersion();
    private int lineNumber = -1;
    private int helperMethodIndex = 0;
    private List<String> superMethodNames = new ArrayList<>();
    private MethodPointerExpressionWriter methodPointerExpressionWriter;
    private MethodReferenceExpressionWriter methodReferenceExpressionWriter;

    public void init(final AsmClassGenerator asmClassGenerator, final GeneratorContext gcon, final ClassVisitor cv, final ClassNode cn) {
        CompilerConfiguration config = cn.getCompileUnit().getConfig();
        Map<String,Boolean> optOptions = config.getOptimizationOptions();
        boolean invokedynamic = true;
        if (optOptions.isEmpty()) {
            // IGNORE
        } else if (Boolean.FALSE.equals(optOptions.get("all"))) {
            invokedynamic = false;
            this.optimizeForInt = false;
            // set other optimizations options to false here
        } else {
            if (!config.isIndyEnabled()) invokedynamic = false;
            if (Boolean.FALSE.equals(optOptions.get("int"))) this.optimizeForInt = false;
            // set other optimizations options to false here
        }
        if (invokedynamic) this.optimizeForInt = false;

        this.classNode = cn;
        this.internalClassName = BytecodeHelper.getClassInternalName(cn);

        this.bytecodeVersion = config.getBytecodeVersion();

        if (invokedynamic) {
            this.invocationWriter = new InvokeDynamicWriter(this);
            this.callSiteWriter = new IndyCallSiteWriter(this);
            this.binaryExpHelper = new IndyBinHelper(this);
        } else {
            this.callSiteWriter = new CallSiteWriter(this);
            this.invocationWriter = new InvocationWriter(this);
            this.binaryExpHelper = new BinaryExpressionHelper(this);
        }

        this.unaryExpressionHelper = new UnaryExpressionHelper(this);
        if (this.optimizeForInt) {
            this.fastPathBinaryExpHelper = new BinaryExpressionMultiTypeDispatcher(this);
            // TODO: replace with a real fast path unary expression helper when available
            this.fastPathUnaryExpressionHelper = new UnaryExpressionHelper(this);
        } else {
            this.fastPathBinaryExpHelper = this.binaryExpHelper;
            this.fastPathUnaryExpressionHelper = new UnaryExpressionHelper(this);
        }

        this.operandStack = new OperandStack(this);
        this.assertionWriter = new AssertionWriter(this);
        this.closureWriter = new ClosureWriter(this);
        this.lambdaWriter = new LambdaWriter(this);
        this.methodPointerExpressionWriter = new MethodPointerExpressionWriter(this);
        this.methodReferenceExpressionWriter = new MethodReferenceExpressionWriter(this);
        this.internalBaseClassName = BytecodeHelper.getClassInternalName(cn.getSuperClass());
        this.acg = asmClassGenerator;
        this.context = gcon;
        this.compileStack = new CompileStack(this);
        this.cv = createClassVisitor(cv, config);
        if (this.optimizeForInt) {
            this.statementWriter = new OptimizingStatementWriter(this);
        } else {
            this.statementWriter = new StatementWriter(this);
        }
        this.typeChooser = new StatementMetaTypeChooser();
    }

    private static ClassVisitor createClassVisitor(final ClassVisitor cv, final CompilerConfiguration config) {
        // GRECLIPSE edit
        if (cv instanceof groovyjarjarasm.asm.util.TraceClassVisitor || !Boolean.getBoolean("groovy.log.classgen")) {
            return cv;
        }
        return new groovyjarjarasm.asm.util.TraceClassVisitor(cv, java.util.Optional.ofNullable(config.getOutput()).orElseGet(() -> new java.io.PrintWriter(System.out, true)));
        // GRECLIPSE end
    }

    //--------------------------------------------------------------------------

    public AsmClassGenerator getAcg() {
        return acg;
    }

    @Deprecated
    public ClassVisitor getCv() {
        return cv;
    }

    public ClassVisitor getClassVisitor() {
        return cv;
    }

    public MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    public void setMethodVisitor(final MethodVisitor methodVisitor) {
        this.methodVisitor = methodVisitor;
    }

    public GeneratorContext getContext() {
        return context;
    }

    public CompileStack getCompileStack() {
        return compileStack;
    }

    public OperandStack getOperandStack() {
        return operandStack;
    }

    public SourceUnit getSourceUnit() {
        return getAcg().getSourceUnit();
    }

    public TypeChooser getTypeChooser() {
        return typeChooser;
    }

    public UnaryExpressionHelper getUnaryExpressionHelper() {
        if (fastPath) {
            return fastPathUnaryExpressionHelper;
        } else {
            return unaryExpressionHelper;
        }
    }

    public BinaryExpressionHelper getBinaryExpressionHelper() {
        if (fastPath) {
            return fastPathBinaryExpHelper;
        } else {
            return binaryExpHelper;
        }
    }

    //--------------------------------------------------------------------------

    public AssertionWriter getAssertionWriter() {
        return assertionWriter;
    }

    public CallSiteWriter getCallSiteWriter() {
        return callSiteWriter;
    }

    public ClosureWriter getClosureWriter() {
        return closureWriter;
    }

    public LambdaWriter getLambdaWriter() {
        return lambdaWriter;
    }

    public StatementWriter getStatementWriter() {
        return statementWriter;
    }

    public InvocationWriter getInvocationWriter() {
        return invocationWriter;
    }

    public MethodPointerExpressionWriter getMethodPointerExpressionWriter() {
        return methodPointerExpressionWriter;
    }

    public MethodReferenceExpressionWriter getMethodReferenceExpressionWriter() {
        return methodReferenceExpressionWriter;
    }

    //--------------------------------------------------------------------------

    public String getClassName() {
        String className;
        if (!classNode.isInterface() || interfaceClassLoadingClass == null) {
            className = internalClassName;
        } else {
            className = BytecodeHelper.getClassInternalName(interfaceClassLoadingClass);
        }
        return className;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public void setMethodNode(final MethodNode methodNode) {
        this.methodNode = methodNode;
        this.constructorNode = null;
    }

    public ConstructorNode getConstructorNode() {
        return constructorNode;
    }

    public void setConstructorNode(final ConstructorNode constructorNode) {
        this.constructorNode = constructorNode;
        this.methodNode = null;
    }

    public ClassNode getThisType() {
        ClassNode thisType = getClassNode();
        while (isGeneratedFunction(thisType)) {
            thisType = thisType.getOuterClass();
        }
        return thisType;
    }

    public ClassNode getReturnType() {
        if (methodNode != null) {
            return methodNode.getReturnType();
        } else if (constructorNode != null) {
            return constructorNode.getReturnType();
        } else {
            throw new GroovyBugError("I spotted a return that is neither in a method nor in a constructor... I can not handle that");
        }
    }

    public ClassNode getOutermostClass() {
        return getNestHost(classNode);
    }

    public String getInternalClassName() {
        return internalClassName;
    }

    public String getInternalBaseClassName() {
        return internalBaseClassName;
    }

    public List<String> getSuperMethodNames() {
        return superMethodNames;
    }

    public InterfaceHelperClassNode getInterfaceClassLoadingClass() {
        return interfaceClassLoadingClass;
    }

    public void setInterfaceClassLoadingClass(final InterfaceHelperClassNode ihc) {
        interfaceClassLoadingClass = ihc;
    }

    //

    public boolean isStaticContext() {
        if (isConstructor()) { // GROOVY-11483
            return compileStack.isInSpecialConstructorCall();
        }
        if (compileStack.getScope() != null) {
            return compileStack.getScope().isInStaticContext();
        }
        throw new IllegalStateException("out-of-scope static check");
    }

    public boolean isStaticMethod() {
        return methodNode != null && methodNode.isStatic();
    }

    public boolean isNotClinit() {
        return methodNode == null || !methodNode.isStaticConstructor();
    }

    public boolean isStaticConstructor() {
        return methodNode != null && methodNode.isStaticConstructor();
    }

    public boolean isConstructor() {
        return constructorNode != null;
    }

    public boolean isInGeneratedFunction() {
        return classNode.getOuterClass() != null && isGeneratedFunction(classNode);
    }

    public boolean isInGeneratedFunctionConstructor() {
        return isConstructor() && isInGeneratedFunction();
    }

    /**
     * @return true if we are in a script body, where all variables declared are no longer
     *         local variables but are properties
     */
    public boolean isInScriptBody() {
        return classNode.isScriptBody() || (methodNode != null && methodNode.isScriptBody());
    }

    public boolean shouldOptimizeForInt() {
        return optimizeForInt;
    }

    public void switchToFastPath() {
        fastPath = true;
        resetLineNumber();
    }

    public void switchToSlowPath() {
        fastPath = false;
        resetLineNumber();
    }

    public boolean isFastPath() {
        return fastPath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void resetLineNumber() {
        setLineNumber(-1);
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void visitLineNumber(final int lineNumber) {
        if (lineNumber > 0 && lineNumber != this.lineNumber) {
            setLineNumber(lineNumber);

            MethodVisitor mv = getMethodVisitor();
            if (mv != null) {
                Label label = new Label();
                mv.visitLabel(label);
                mv.visitLineNumber(lineNumber, label);
            }
        }
    }

    public int getBytecodeVersion() {
        return bytecodeVersion;
    }

    public int getNextHelperMethodIndex() {
        return helperMethodIndex += 1;
    }
}
