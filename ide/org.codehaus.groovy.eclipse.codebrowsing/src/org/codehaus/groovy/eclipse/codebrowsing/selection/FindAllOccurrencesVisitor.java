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
package org.codehaus.groovy.eclipse.codebrowsing.selection;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentFactory;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.BinaryExpressionFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.FragmentVisitor;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.MethodCallFragment;

/**
 * Finds all occurrences of the passed in expression in the module.
 *
 * @author andrew
 * @created May 12, 2010
 */
public class FindAllOccurrencesVisitor extends ClassCodeVisitorSupport {

    class FragmentMatcherVisitor extends FragmentVisitor {
        private boolean matchWasFound = false;

        @Override
        public boolean previsit(IASTFragment fragment) {
            IASTFragment matched = fragment.findMatchingSubFragment(toFind);
            if (matched.kind() != ASTFragmentKind.EMPTY) {
                // prevent double matching, which may occur in binary fragments when searching for a simple expression fragment
                if (occurrences.size() == 0 || 
                        occurrences.get(occurrences.size()-1).getStart() != matched.getStart()) {
                    occurrences.add(matched);
                    matchWasFound = true;
                }
            }

            // only continue for binary fragments since there may be multiple
            // matches inside of them
            return fragment.kind() == ASTFragmentKind.BINARY;
        }

        boolean matchWasFound() {
            boolean b = matchWasFound;
            matchWasFound = false;
            return b;
        }
    }

    class AssociatedExpressionMatcher extends FragmentVisitor {
        boolean ignoreNext = false;
        
        @Override
        public boolean previsit(IASTFragment fragment) {
            if (! ignoreNext) {
                fragment.getAssociatedExpression().visit(FindAllOccurrencesVisitor.this);
            } else {
                ignoreNext = false;
            }
            return true;
        }
        
        @Override
        public boolean visit(MethodCallFragment fragment) {
            fragment.getArguments().visit(FindAllOccurrencesVisitor.this);
            return true;
        }

    }

    private IASTFragment toFind;

    private List<IASTFragment> occurrences;

    private ModuleNode module;

    private AnnotatedNode limitTo;

    private ASTFragmentFactory factory;

    private FragmentMatcherVisitor fragmentMatcher;

    private AssociatedExpressionMatcher associatedExpressionMatcher;

    public FindAllOccurrencesVisitor(ModuleNode module) {
        this(module, null);
    }

    public FindAllOccurrencesVisitor(ModuleNode module, AnnotatedNode limitTo) {
        this.limitTo = limitTo;
        this.module = module;
        this.factory = new ASTFragmentFactory();
        this.fragmentMatcher = new FragmentMatcherVisitor();
        this.associatedExpressionMatcher = new AssociatedExpressionMatcher();
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    public List<IASTFragment> findOccurrences(IASTFragment fragment) {
        this.toFind = fragment;
        this.occurrences = new ArrayList<IASTFragment>();

        if (limitTo == null) {
            List<ClassNode> classes = module.getClasses();
            for (ClassNode classNode : classes) {
                visitClass(classNode);
            }
        } else {
            if (limitTo instanceof ClassNode) {
                visitClass((ClassNode) limitTo);
            } else if (limitTo instanceof MethodNode) {
                visitMethod((MethodNode) limitTo);
            } else if (limitTo instanceof FieldNode) {
                visitField((FieldNode) limitTo);
            } else {
                limitTo.visit(this);
            }
        }

        return this.occurrences;
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        // need to visit clinit so that we can access static initializers
        if (node.getEnd() == 0 && !node.getName().equals("<clinit>")) {
            return;
        }
        super.visitConstructorOrMethod(node, isConstructor);
    }

    @Override
    public void visitField(FieldNode node) {
        if (node.getEnd() == 0) {
            return;
        }
        // if (node.getInitialExpression() != null && isSame.isSame(toFind,
        // node.getInitialExpression())) {
        // occurrences.add(node.getInitialExpression());
        // return;
        // }
        super.visitField(node);
    }

    @Override
    public void visitProperty(PropertyNode node) {
    // ignore. don't want to visit the fields twice.
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitArgumentlistExpression(expression);
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitArrayExpression(expression);
    }

    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitAttributeExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);

        // If looking for a simple expression, then we have already visited the children
        // don't visit twice
//        if (toFind.kind() != ASTFragmentKind.SIMPLE_EXPRESSION) {
            // don't visit children directly because that may result in
            // unanticipated double matches
            // Don't ignore the first fragment
            associatedExpressionMatcher.ignoreNext = false;
            fragment.accept(associatedExpressionMatcher);
//        }
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitBitwiseNegationExpression(expression);
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitBooleanExpression(expression);
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitBytecodeExpression(expression);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitCastExpression(expression);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitClassExpression(expression);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitClosureExpression(expression);
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitClosureListExpression(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitConstantExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitConstructorCallExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitVariableExpression(expression);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitFieldExpression(expression);
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitGStringExpression(expression);
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitListExpression(expression);
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitMapEntryExpression(expression);
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitMapExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        // don't visit children directly because that may result in
        // unanticipated double matches
        // ignore the first fragment since that was visited above
        associatedExpressionMatcher.ignoreNext = true;
        fragment.accept(associatedExpressionMatcher);
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        // don't visit children directly because that may result in
        // unanticipated double matches
        // ignore the first fragment since that was visited above
        associatedExpressionMatcher.ignoreNext = true;
        fragment.accept(associatedExpressionMatcher);
    }

    @Override
    public void visitNotExpression(NotExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitNotExpression(expression);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitPostfixExpression(expression);
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitPrefixExpression(expression);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        // don't visit children directly because that may result in
        // unanticipated double matches
        // ignore the first fragment since that was visited above
        associatedExpressionMatcher.ignoreNext = true;
        fragment.accept(associatedExpressionMatcher);
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitRangeExpression(expression);
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitShortTernaryExpression(expression);
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitSpreadExpression(expression);
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitSpreadMapExpression(expression);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitStaticMethodCallExpression(expression);
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitTernaryExpression(expression);
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitTupleExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitUnaryMinusExpression(expression);
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        IASTFragment fragment = factory.createFragment(expression);
        fragment.accept(fragmentMatcher);
        if (!fragmentMatcher.matchWasFound())
            super.visitUnaryPlusExpression(expression);
    }
}
