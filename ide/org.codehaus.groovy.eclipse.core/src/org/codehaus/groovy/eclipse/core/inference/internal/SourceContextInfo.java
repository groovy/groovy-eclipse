 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.inference.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.impl.ReverseSourceBuffer;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.ITypeEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.MemberLookupRegistry;
import org.codehaus.groovy.eclipse.core.types.SymbolTableRegistry;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluationContextBuilder;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;
import org.codehaus.groovy.eclipse.core.types.impl.CategoryLookup;
import org.codehaus.groovy.eclipse.core.types.impl.ClassLoaderMemberLookup;
import org.codehaus.groovy.eclipse.core.types.impl.CompositeLookup;
import org.codehaus.groovy.eclipse.core.types.impl.GroovyProjectMemberLookup;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ParseException;
import org.eclipse.jface.text.Region;

/**
 * 
 * @author Andrew Eisenberg
 * @author emp
 * @created Aug 1, 2009
 *
 */
public class SourceContextInfo {
    public static SourceContextInfo create(ModuleNode module, GroovyProjectFacade project, int offset, ISourceBuffer buffer, boolean ignoreFirstParameter) {
           // Fix for GROOVY-1830: oddly no NPE in current release, but cleaning up acceptible completions here too.
        // FUTURE: emp - extend contexts to be aware of the code they are in to deal with scripts vs classes. This would
        // really be easier with a custom parser than hacking around searching for imports. 
        // This is hacky - Groovy scripts include the imports area into the current 'class'.
        // We don't want to try complete on imports (thinks 'com.' is a dynamic property), so need to see if this is an import line.
        String regex = "$[.\\w\\s]*" + new StringBuffer("import").reverse().toString() + "\\s*";
        ReverseSourceBuffer reverseBuffer = new ReverseSourceBuffer(buffer, offset - 1);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(reverseBuffer);
        if (matcher.find()) {
            return null;
        }

        ExpressionFinder finder = new ExpressionFinder();
        String expression = findCompletionExpression(finder, offset, buffer);

        // Not a completable expression - done.
        if (expression == null) {
            expression = "";
        }

        int start = offset - expression.length();
        // Move offset to beginning of expression. This way the context is more likely to match the AST before edits.
        ISourceCodeContext[] contexts = createContexts(module, buffer, start);

        if (contexts.length == 0) {
            return null;
        }
        
        // The project for the current file being editied will be used for completion.
        
        // Ready to complete - extract the completion prefix and do it.
        String[] parts = finder.splitForCompletion(expression);
        if (parts != null) {
            IMemberLookup lookup = createMemberLookup(project, contexts, ignoreFirstParameter);
            if (lookup == null) {
                return null;
            }
            ISymbolTable table = SymbolTableRegistry.createSymbolTable(contexts);
            ((IGroovyProjectAware)table).setGroovyProject(project);
            ((IGroovyProjectAware)lookup).setGroovyProject(project);
            ((ISourceCodeContextAware)lookup).setSourceCodeContext(contexts[contexts.length - 1]);

            // There is just an identifier, assume it is a field or method name and add 'this.' or 'ClassName.'.
            if (parts[1] == null) {
                ISourceCodeContext sourceContext = contexts[contexts.length - 1];
                if (sourceContext.getId() == ISourceCodeContext.METHOD_SCOPE || sourceContext.getId() == ISourceCodeContext.CLOSURE_SCOPE || sourceContext.getId() == ISourceCodeContext.CONSTRUCTOR_SCOPE) {
                    ASTNode[] path = sourceContext.getASTPath();
                    for (int i = 0; i < path.length; i++) {
                        if (path[i] instanceof MethodNode) {
                            MethodNode node = (MethodNode) path[i];
                            parts[1] = parts[0];
                            if (node.isStatic()) {
                                parts[0] = node.getDeclaringClass().getNameWithoutPackage();
                            } else {
                                parts[0] = "this";
                            }
                        }
                    }
                }
            }
            
            String[] imports = createImportsArray(module);
            TypeEvaluator eval;
            
            try {
                ITypeEvaluationContext typeContext = new TypeEvaluationContextBuilder()
                        .project(project)
                        .sourceCodeContext(contexts[contexts.length - 1])
                        .symbolTable(table)
                        .memberLookup(lookup)
                        .classLoader(project.getProjectClassLoader())
                        .imports(imports)
                        .location(new Region(start, expression.length()))
                        .done();
                eval = new TypeEvaluator(typeContext);
                EvalResult result  = null;
                result = eval.evaluate(parts[0]);
                if (result != null) {
                    return new SourceContextInfo(lookup, result, parts[0], parts[1]);
                }
            } catch (Exception e) {
                List classes = module.getClasses();
                GroovyCore.logException("Exception while browsing in " + (classes.size() > 0 ? 
                        ((ClassNode) classes.get(0)).getName() : 
                            module.getDescription()), e);
            }
        }
        return null;
    }

    public final IMemberLookup lookup;
    public final EvalResult eval;
    public final String expression;
    public final String name;

    private SourceContextInfo(IMemberLookup lookup, EvalResult eval,
            String expression, String name) {
        this.lookup = lookup;
        this.eval = eval;
        this.expression = expression;
        this.name = name;
    }

    private static String[] createImportsArray(ModuleNode moduleNode) {
        List<ImportNode> imports = moduleNode.getImports();
        List<String> importPackages = moduleNode.getImportPackages();
        List<String> results = new ArrayList<String>();
        
        if (moduleNode.getPackageName() != null) {
            results.add(moduleNode.getPackageName() + "*");
        }
        
        for (Iterator<ImportNode> iter = imports.iterator(); iter.hasNext();) {
            ImportNode importNode = (ImportNode) iter.next();
            results.add(importNode.getType().getName());
        }
        for (Iterator<String> iter = importPackages.iterator(); iter.hasNext();) {
            results.add(((String) iter.next()) + '*');
        }
        
        return (String[]) results.toArray(new String[results.size()]);
    }
    
    protected static String findCompletionExpression(ExpressionFinder finder, int offset, ISourceBuffer buffer) {
        try{
            return finder.findForCompletions(buffer, offset - 1);
        } catch (ParseException e) {
            // can ignore.  probably just invalid code that is being completed at
            GroovyCore.trace("Cannot complete code:" + e.getMessage());
        }
        return null;
    }
    
    
    protected static ISourceCodeContext[] createContexts(ModuleNode moduleNode, ISourceBuffer buffer, int offset) {
        SourceCodeContextFactory factory = new SourceCodeContextFactory();
        return factory.createContexts(buffer, moduleNode, new Region(offset, 0));
    }
    
    /**
     * @param javaContext
     * @param contexts 
     * @param inScriptOrClosure 
     * @return The lookup or null if it could not be created.
     */
    protected static IMemberLookup createMemberLookup(GroovyProjectFacade project, ISourceCodeContext[] contexts, boolean ignoreFirstParameter) {
        CategoryLookup categoryLookup = new CategoryLookup();
        categoryLookup.setIgnoreFirstParameter(ignoreFirstParameter);
        
        GroovyProjectMemberLookup classNodeLookup = new GroovyProjectMemberLookup(project);
            
        IMemberLookup registeredLookups = MemberLookupRegistry.createMemberLookup(contexts);
        ClassLoaderMemberLookup classLookup = new ClassLoaderMemberLookup(project.getProjectClassLoader());
        return new CompositeLookup(new IMemberLookup[] { 
                classNodeLookup, classLookup, categoryLookup, registeredLookups });
    }

}