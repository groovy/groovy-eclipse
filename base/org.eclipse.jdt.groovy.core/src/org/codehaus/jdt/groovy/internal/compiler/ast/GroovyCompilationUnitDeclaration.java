/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.GroovyRuntimeException;
import groovy.transform.PackageScopeTarget;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.Comment;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.TaskEntry;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.LocatedMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.syntax.CSTNode;
import org.codehaus.groovy.syntax.PreciseSyntaxException;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.tools.GroovyClass;
import org.codehaus.jdt.groovy.control.EclipseSourceUnit;
import org.codehaus.jdt.groovy.core.dom.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A subtype of JDT CompilationUnitDeclaration that represents a groovy source file. It overrides methods as appropriate, delegating
 * to the groovy infrastructure.
 */
public class GroovyCompilationUnitDeclaration extends CompilationUnitDeclaration {

    private static final boolean DEBUG_CODE_GENERATION = false;

    private static final boolean DEBUG_TASK_TAGS = false;

    public static boolean defaultCheckGenerics = false;

    private final CompilationUnit compilationUnit;

    private final CompilerOptions compilerOptions;

    private final SourceUnit groovySourceUnit;

    private final TraitHelper traitHelper = new TraitHelper();

    private boolean isScript; // see buildCompilationUnitScope

    public GroovyCompilationUnitDeclaration(
            ProblemReporter problemReporter,
            CompilationResult compilationResult,
            int sourceLength,
            CompilationUnit compilationUnit,
            SourceUnit groovySourceUnit,
            CompilerOptions compilerOptions) {
        super(problemReporter, compilationResult, sourceLength);
        this.compilationUnit = compilationUnit;
        this.groovySourceUnit = groovySourceUnit;
        this.compilerOptions = compilerOptions;
    }

    /**
     * Drives the Groovy Compilation Unit for this project through to the specified phase. Yes on a call for one groovy file to
     * processToPhase(), all the groovy files in the project proceed to that phase. This isn't ideal but doesn't necessarily cause a
     * problem. But it does mean progress reporting for the compilation is incorrect as it jumps rather than smoothly going from 1
     * to 100%.
     *
     * @param phase the phase to process up to
     * @return true if clean processing, false otherwise
     */
    public boolean processToPhase(int phase) {
        if (phase == Phases.CANONICALIZATION && groovySourceUnit instanceof EclipseSourceUnit) {
            IFile file = ((EclipseSourceUnit) groovySourceUnit).getEclipseFile(); if (file != null) {
                // TODO: Surgically remove about-to-be-compiled class(es) from transform loader cache
            }
        }

        boolean alreadyHasErrors = compilationResult.hasErrors();
        ReferenceContext referenceContext = problemReporter.referenceContext;
        // replacement error collector doesn't cause an exception, instead errors are checked post 'compile'
        try {
            problemReporter.referenceContext = this;
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(compilationUnit.getTransformLoader());
                compilationUnit.compile(phase);
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }

            if (groovySourceUnit.getErrorCollector().hasErrors()) {
                recordProblems(groovySourceUnit.getErrorCollector().getErrors());
                return false;
            } else {
                return true;
            }
        } catch (MultipleCompilationErrorsException mce) {
            fixGroovyRuntimeException(mce);

            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.COMPILER, mce.getMessage());
            }

            ErrorCollector collector = mce.getErrorCollector();
            if (collector.getErrorCount() == 1 && mce.getErrorCollector().getError(0) instanceof ExceptionMessage) {
                Exception cause = ((ExceptionMessage) mce.getErrorCollector().getError(0)).getCause();
                if (cause instanceof AbortCompilation) {
                    throw (AbortCompilation) cause;
                }
            }

            recordProblems(mce.getErrorCollector().getErrors());
        } catch (GroovyBugError gbe) {
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.COMPILER, gbe.getBugText());
            }

            if (gbe.getCause() instanceof AbortCompilation) {
                AbortCompilation abort = (AbortCompilation) gbe.getCause();
                if (!abort.isSilent) {
                    if (abort.problem != null) {
                        problemReporter.record(abort.problem, compilationResult, this, true);
                    } else {
                        throw abort;
                    }
                }
            } else if (alreadyHasErrors) {
                Util.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
                    "Ignoring GroovyBugError since it is likely caused by earlier issues", gbe));
            } else {
                Util.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Groovy compiler error", gbe));

                // Need to record these problems as compiler errors since some users will not think to check the log.
                // This is mostly a fix for problems like those in GRECLIPSE-1420, where a GBE is thrown when it is really just a syntax problem.
                SyntaxErrorMessage syntaxError = new SyntaxErrorMessage(new SyntaxException("Groovy compiler error: " + gbe.getBugText(), gbe, 1, 0), groovySourceUnit);
                ErrorCollector collector = groovySourceUnit.getErrorCollector();
                collector.addError(syntaxError);

                recordProblems(collector.getErrors());
            }
        } finally {
            problemReporter.referenceContext = referenceContext;
        }

        return false;
    }

    /** Unwraps any SyntaxExceptions embedded within a GroovyRuntimeException. */
    private void fixGroovyRuntimeException(MultipleCompilationErrorsException mce) {
        List<SyntaxException> syntaxErrors = new ArrayList<>();

        for (Iterator<? extends Message> it = mce.getErrorCollector().getErrors().iterator(); it.hasNext();) {
            Message m = it.next();
            if (m instanceof ExceptionMessage) {
                ExceptionMessage em = (ExceptionMessage) m;
                if (em.getCause() instanceof GroovyRuntimeException &&
                        ((GroovyRuntimeException) em.getCause()).getCause() instanceof SyntaxException) {
                    syntaxErrors.add((SyntaxException) em.getCause().getCause());
                    it.remove();
                }
            }
        }

        for (SyntaxException se : syntaxErrors) {
            mce.getErrorCollector().addError(se, groovySourceUnit);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the Groovy compilation unit shared by all files in the same project.
     */
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    /**
     * Populates the compilation unit based on the successful parse.
     */
    public void populateCompilationUnitDeclaration() {
        UnitPopulator populator = new UnitPopulator();
        populator.populate(this);
    }

    // FIXASC are costly regens being done for all the classes???
    @Override
    public void generateCode() {
        boolean successful = processToPhase(Phases.ALL);
        if (successful) {

            // At the end of this method we want to make this call for each of the classes generated during processing
            //
            // compilationResult.record(classname.toCharArray(), new GroovyClassFile(classname, classbytes, foundBinding, path));
            //
            // For each generated class (in compilationUnit.getClasses()) we know:
            // String classname = groovyClass.getName(); = this is the name of the generated type (doesn't matter where the
            // declaration was)
            // byte[] classbytes = groovyClass.getBytes(); = duh
            // String path = groovyClass.getName().replace('.', '/'); = where to put it on disk

            // The only tricky piece of information is discovering the binding that gave rise to the type. This is complicated
            // in groovy because it is not policing that the package name matches the directory structure.
            // Effectively the connection between the TypeDeclaration (which points to the binding) and the
            // groovy created component is lost - if that were maintained we would not have to go hunting for it.
            // On finishing processing we have access to the generated classes but GroovyClassFile objects have no idea what
            // their originating ClassNode was.

            // Under eclipse I've extended GroovyClassFile objects to remember their sourceUnit and ClassNode - this means
            // we have to do very little hunting for the binding and don't have to mess around with strings (chopping off
            // packages, etc).

            // This returns all of them, for all source files
            List<GroovyClass> classes = compilationUnit.getClasses();

            if (DEBUG_CODE_GENERATION) {
                log("Processing sourceUnit " + groovySourceUnit.getName());
            }

            for (GroovyClass clazz : classes) {
                ClassNode classNode = clazz.getClassNode();
                if (DEBUG_CODE_GENERATION) {
                    log("Looking at class " + clazz.getName());
                    log("ClassNode where it came from " + classNode);
                }
                // Only care about those coming about because of this groovySourceUnit
                if (clazz.getSourceUnit() == groovySourceUnit) {
                    if (DEBUG_CODE_GENERATION) {
                        log("It is from this source unit");
                    }
                    // Worth continuing
                    String classname = clazz.getName();
                    SourceTypeBinding binding = null;
                    if (types != null && types.length != 0) {
                        binding = findBinding(types, clazz.getClassNode());
                    }
                    if (DEBUG_CODE_GENERATION) {
                        log("Binding located? " + (binding != null));
                    }
                    if (binding == null) {
                        // closures will be represented as InnerClassNodes
                        ClassNode current = classNode;
                        while ((current = current.getOuterClass()) != null && binding == null) {
                            binding = findBinding(types, current);
                            if (DEBUG_CODE_GENERATION) {
                                log("Had another look within enclosing class; found binding? " + (binding != null));
                            }
                        }
                    }

                    boolean isScript = false;
                    // Suppress class file output if it is a script
                    // null binding implies synthetic type, which we assume cannot be a script
                    if (binding != null && binding.scope != null && (binding.scope.parent instanceof GroovyCompilationUnitScope)) {
                        GroovyCompilationUnitScope gcuScope = (GroovyCompilationUnitScope) binding.scope.parent;
                        if (gcuScope.isScript()) {
                            isScript = true;
                        }
                    }
                    if (!isScript) {
                        byte[] classbytes = clazz.getBytes();
                        String path = clazz.getName().replace('.', '/');
                        GroovyClassFile classFile = new GroovyClassFile(classname, classbytes, binding, path);
                        char[] classNameChars = classname.toCharArray();
                        if (binding == null) {
                            // GRECLIPSE-1653 this type likely added by AST transform and is synthetic
                            Map<char[], ClassFile> compiledTypes = Map.class.cast(compilationResult.compiledTypes);
                            compiledTypes.put(classNameChars, classFile);
                        } else {
                            compilationResult.record(classNameChars, classFile);
                        }
                    }
                }
            }
        } else if (types != null) {
            // GRECLIPSE-1773
            // We should create problem types if some types are not compiled successfully as it is done for Java types.
            // Otherwise incremental builder is not able to recompile dependencies of broken types.
            for (TypeDeclaration type : types) {
                if (type.binding != null) {
                    ClassFile.createProblemType(type, compilationResult);
                }
            }
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static SourceTypeBinding findBinding(TypeDeclaration[] typedeclarations, ClassNode cnode) {
        for (TypeDeclaration typedeclaration : typedeclarations) {
            GroovyTypeDeclaration groovyTypeDeclaration = (GroovyTypeDeclaration) typedeclaration;
            if (groovyTypeDeclaration.getClassNode().equals(cnode)) {
                return groovyTypeDeclaration.binding;
            }
            if (typedeclaration.memberTypes != null) {
                SourceTypeBinding binding = findBinding(typedeclaration.memberTypes, cnode);
                if (binding != null) {
                    return binding;
                }
            }
        }
        return null;
    }

    // here be dragons
    private void recordProblems(List<?> errors) {
        // FIXASC look at this error situation (described below), surely we need to do it?
        // Due to the nature of driving all groovy entities through compilation together, we can accumulate messages for other
        // compilation units whilst processing the one we wanted to. Per GRE396 this can manifest as recording the wrong thing
        // against the wrong type. That is the only case I have seen of it, so I'm not putting in the general mechanism for all
        // errors yet, I'm just dealing with RuntimeParserExceptions. The general strategy would be to compare the ModuleNode
        // for each message with the ModuleNode currently being processed - if they differ then this isn't a message for this
        // unit and so we ignore it. If we do deal with it then we remember that we did (in errorsRecorded) and remove it from
        // the list of those to process.

        List<Message> errorsRecorded = new ArrayList<>();
        // FIXASC poor way to get the errors attached to the files
        // FIXASC does groovy ever produce warnings? How are they treated here?
        for (Iterator<?> iterator = errors.iterator(); iterator.hasNext();) {
            Message message = (Message) iterator.next();
            SyntaxException syntaxException = null;
            StringWriter sw = new StringWriter();
            message.write(new PrintWriter(sw));
            String msg = sw.toString();
            CategorizedProblem p = null;
            int line = 0;
            int sev = 0;
            int scol = 0;
            int ecol = 0;
            // LocatedMessage instances are produced sometimes, e.g. by grails ast transforms, use the context for position
            if (message instanceof LocatedMessage) {
                CSTNode context = ((LocatedMessage) message).getContext();
                if (context instanceof Token) {
                    line = context.getStartLine();
                    scol = context.getStartColumn();
                    String text = ((Token) context).getText();
                    ecol = scol + (text == null ? 1 : (text.length() - 1));
                }
            }
            if (message instanceof SimpleMessage) {
                SimpleMessage simpleMessage = (SimpleMessage) message;
                sev |= ProblemSeverities.Error;
                String simpleText = simpleMessage.getMessage();
                if (simpleText.length() > 1 && simpleText.charAt(0) == '\n') {
                    simpleText = simpleText.substring(1);
                }
                msg = "Groovy:" + simpleText;
                if (msg.indexOf("\n") != -1) {
                    msg = msg.substring(0, msg.indexOf("\n"));
                }
            }
            if (message instanceof SyntaxErrorMessage) {
                SyntaxErrorMessage errorMessage = (SyntaxErrorMessage) message;
                syntaxException = errorMessage.getCause();
                sev |= ProblemSeverities.Error;
                // FIXASC in the short term, prefixed groovy to indicate
                // where it came from
                String actualMessage = syntaxException.getMessage();
                if (actualMessage.length() > 1 && actualMessage.charAt(0) == '\n') {
                    actualMessage = actualMessage.substring(1);
                }
                msg = "Groovy:" + actualMessage;
                if (msg.indexOf("\n") != -1) {
                    msg = msg.substring(0, msg.indexOf("\n"));
                }
                line = syntaxException.getLine();
                scol = errorMessage.getCause().getStartColumn();
                ecol = errorMessage.getCause().getEndColumn() - 1;
            }
            int soffset = -1;
            int eoffset = -1;
            if (message instanceof ExceptionMessage) {
                ExceptionMessage em = (ExceptionMessage) message;
                sev |= ProblemSeverities.Error;
                if (em.getCause() instanceof RuntimeParserException) {
                    RuntimeParserException rpe = (RuntimeParserException) em.getCause();
                    sev |= ProblemSeverities.Error;
                    msg = "Groovy:" + rpe.getMessage();
                    if (msg.indexOf("\n") != -1) {
                        msg = msg.substring(0, msg.indexOf("\n"));
                    }
                    ModuleNode errorModuleNode = rpe.getModule();
                    ModuleNode thisModuleNode = this.getModuleNode();
                    if (!errorModuleNode.equals(thisModuleNode)) {
                        continue;
                    }
                    soffset = rpe.getNode().getStart();
                    eoffset = rpe.getNode().getEnd() - 1;
                    // need to work out the line again as it may be wrong
                    line = 0;
                    while (compilationResult.lineSeparatorPositions[line] < soffset &&
                            line < compilationResult.lineSeparatorPositions.length) {
                        line += 1;
                    }
                    line += 1; // from an array index to a real 'line number'
                }
            }
            if (syntaxException instanceof PreciseSyntaxException) {
                soffset = ((PreciseSyntaxException) syntaxException).getStartOffset();
                eoffset = ((PreciseSyntaxException) syntaxException).getEndOffset();
                // need to work out the line again as it may be wrong
                line = 0;
                while (line < compilationResult.lineSeparatorPositions.length &&
                    compilationResult.lineSeparatorPositions[line] < soffset) {
                    line += 1;
                }
                line += 1; // from an array index to a real 'line number'
            } else {
                if (soffset == -1) {
                    soffset = getOffset(compilationResult.lineSeparatorPositions, line, scol);
                }
                if (eoffset == -1) {
                    eoffset = getOffset(compilationResult.lineSeparatorPositions, line, ecol);
                }
            }
            if (soffset > eoffset) {
                eoffset = soffset;
            }
            if (soffset > sourceEnd) {
                soffset = sourceEnd;
                eoffset = sourceEnd;
            }

            p = new DefaultProblemFactory().createProblem(getFileName(), 0, new String[] {msg}, 0, new String[] {msg}, sev, soffset, eoffset, line, scol);
            problemReporter.record(p, compilationResult, this, false);
            errorsRecorded.add(message);
        }
        errors.removeAll(errorsRecorded);
    }

    private static int getOffset(int[] lineSeparatorPositions, int line, int column) {
        if (column < 1) column = 1;

        if (lineSeparatorPositions.length > (line - 2) && line > 1) {
            return (lineSeparatorPositions[line - 2] + column);
        }
        return (column - 1);
    }

    //--------------------------------------------------------------------------

    @Override
    public CompilationUnitScope buildCompilationUnitScope(LookupEnvironment lookupEnvironment) {
        GroovyCompilationUnitScope gcus = new GroovyCompilationUnitScope(this, lookupEnvironment);
        gcus.setIsScript(isScript);
        return gcus;
    }

    public ModuleNode getModuleNode() {
        return java.util.Optional.ofNullable(getSourceUnit()).map(SourceUnit::getAST).orElse(null);
    }

    public SourceUnit getSourceUnit() {
        return groovySourceUnit;
    }

    @Override // TODO: Find a better home for this?
    public org.eclipse.jdt.core.dom.CompilationUnit getSpecialDomCompilationUnit(org.eclipse.jdt.core.dom.AST ast) {
        return new GroovyCompilationUnit(ast);
    }

    // for testing
    public String print() {
        return toString();
    }

    @Override
    public void resolve() {
        processToPhase(Phases.SEMANTIC_ANALYSIS);
        checkForTags();
        setComments();
    }

    /**
     * Check any comments from the source file for task tag markers.
     */
    private void checkForTags() {
        if (this.compilerOptions == null) {
            return;
        }
        List<Comment> comments = groovySourceUnit.getComments();
        if (comments == null || comments.isEmpty()) {
            return;
        }
        char[][] taskTags = this.compilerOptions.taskTags;
        char[][] taskPriorities = this.compilerOptions.taskPriorities;
        boolean caseSensitiveTags = this.compilerOptions.isTaskCaseSensitive;
        try {
            if (taskTags != null) {
                // For each comment find all task tags within it and cope with
                for (Comment comment : comments) {
                    List<TaskEntry> allTasksInComment = new ArrayList<>();
                    for (int t = 0; t < taskTags.length; t++) {
                        String taskTag = String.valueOf(taskTags[t]);
                        String taskPriority = null;
                        if (taskPriorities != null) {
                            taskPriority = String.valueOf(taskPriorities[t]);
                        }
                        allTasksInComment.addAll(comment.getPositionsOf(taskTag, taskPriority,
                                compilationResult.lineSeparatorPositions, caseSensitiveTags));
                    }
                    if (!allTasksInComment.isEmpty()) {
                        // Need to check quickly for clashes
                        for (int t1 = 0; t1 < allTasksInComment.size(); t1++) {
                            for (int t2 = 0; t2 < allTasksInComment.size(); t2++) {
                                if (t1 == t2)
                                    continue;
                                TaskEntry taskOne = allTasksInComment.get(t1);
                                TaskEntry taskTwo = allTasksInComment.get(t2);
                                if (DEBUG_TASK_TAGS) {
                                    log("Comparing " + taskOne.toString() + " and " + taskTwo.toString());
                                }
                                if ((taskOne.start + taskOne.taskTag.length() + 1) == taskTwo.start) {
                                    // Adjacent tags
                                    taskOne.isAdjacentTo = taskTwo;
                                } else {
                                    if ((taskOne.getEnd() > taskTwo.start) && (taskOne.start < taskTwo.start)) {
                                        taskOne.setEnd(taskTwo.start - 1);
                                        if (DEBUG_TASK_TAGS) {
                                            log("trim " + taskOne.toString() + " and " + taskTwo.toString());
                                        }
                                    } else if (taskTwo.getEnd() > taskOne.start && taskTwo.start < taskOne.start) {
                                        taskTwo.setEnd(taskOne.start - 1);
                                        if (DEBUG_TASK_TAGS) {
                                            log("trim " + taskOne.toString() + " and " + taskTwo.toString());
                                        }
                                    }
                                }
                            }
                        }
                        for (TaskEntry taskEntry : allTasksInComment) {
                            this.problemReporter.referenceContext = this;
                            if (DEBUG_TASK_TAGS) {
                                log("Adding task " + taskEntry.toString());
                            }
                            problemReporter.task(taskEntry.taskTag, taskEntry.getText(), taskEntry.taskPriority, taskEntry.start, taskEntry.getEnd());
                        }
                    }
                }
            }
        } catch (AbortCompilation ac) {
            // that is ok... probably cancelled
        } catch (Throwable t) {
            Util.log(t, "Unexpected problem processing task tags in " + groovySourceUnit.getName());
            new RuntimeException("Unexpected problem processing task tags in " + groovySourceUnit.getName(), t).printStackTrace();
        }
    }

    /**
     * Takes the comments information from the parse and applies it to the compilation unit.
     */
    private void setComments() {
        List<Comment> groovyComments = groovySourceUnit.getComments();
        if (groovyComments != null && !groovyComments.isEmpty()) {
            comments = groovyComments.stream().map(groovyComment ->
                groovyComment.getPositions(compilationResult.lineSeparatorPositions)
            ).toArray(int[][]::new);
        }
    }

    @Override
    public void analyseCode() {
        processToPhase(Phases.CANONICALIZATION);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (groovySourceUnit instanceof EclipseSourceUnit) {
            ((EclipseSourceUnit) groovySourceUnit).resolver.cleanUp();
        }
    }

    /*
    @Override
    public void abort(int abortLevel, CategorizedProblem problem) {
        // FIXASC look at callers of this, should we be following the abort on first problem policy?
        super.abort(abortLevel, problem);
    }

    @Override
    public void checkUnusedImports() {
        super.checkUnusedImports();
    }

    @Override
    public CompilationResult compilationResult() {
        return super.compilationResult();
    }

    @Override
    public TypeDeclaration declarationOfType(char[][] typeName) {
        return super.declarationOfType(typeName);
    }

    @Override
    public void finalizeProblems() {
        super.finalizeProblems();
    }

    @Override
    public char[] getFileName() {
        return super.getFileName();
    }

    @Override
    public char[] getMainTypeName() {
        // FIXASC necessary to return something for groovy?
        return super.getMainTypeName();
    }

    @Override
    public boolean hasErrors() {
        return super.hasErrors();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean isPackageInfo() {
        return super.isPackageInfo();
    }

    @Override
    public StringBuffer print(int indent, StringBuffer output) {
        // FIXASC additional stuff to print?
        return super.print(indent, output);
    }

    @Override
    public void propagateInnerEmulationForAllLocalTypes() {
        // FIXASC anything to do here for groovy inner types?
        super.propagateInnerEmulationForAllLocalTypes();
    }

    @Override
    public void record(LocalTypeBinding localType) {
        super.record(localType);
    }

    @Override
    public void recordStringLiteral(StringLiteral literal, boolean fromRecovery) {
        // FIXASC assert not called for groovy, surely
        super.recordStringLiteral(literal, fromRecovery);
    }

    @Override
    public void tagAsHavingErrors() {
        super.tagAsHavingErrors();
    }

    @Override
    public void traverse(ASTVisitor visitor, CompilationUnitScope unitScope) {
        // FIXASC are we well formed enough for this?
        super.traverse(visitor, unitScope);
    }

    @Override
    public ASTNode concreteStatement() {
        // FIXASC assert not called for groovy, surely
        return super.concreteStatement();
    }

    @Override
    public boolean isImplicitThis() {
        // FIXASC assert not called for groovy, surely
        return super.isImplicitThis();
    }

    @Override
    public boolean isSuper() {
        // FIXASC assert not called for groovy, surely
        return super.isSuper();
    }

    @Override
    public boolean isThis() {
        // FIXASC assert not called for groovy, surely
        return super.isThis();
    }

    @Override
    public int sourceEnd() {
        return super.sourceEnd();
    }

    @Override
    public int sourceStart() {
        return super.sourceStart();
    }

    @Override
    public String toString() {
        // FIXASC anything to add?
        return super.toString();
    }

    @Override
    public void traverse(ASTVisitor visitor, BlockScope scope) {
        // FIXASC in a good state for traversal? what would cause this to trigger?
        super.traverse(visitor, scope);
    }
    */

    public void tagAsScript() {
        this.isScript = true;
    }

    //--------------------------------------------------------------------------

    /**
     * Helps check if some class node is trait.
     */
    private class TraitHelper {

        private boolean lookForTraitAlias;
        private boolean toBeInitialized = true;

        private void initialize() {
            if (imports != null) {
                for (ImportReference i : imports) {
                    String importedType = i.toString();
                    if ("groovy.transform.Trait".equals(importedType)) {
                        lookForTraitAlias = true;
                        break;
                    }
                    if (importedType.endsWith(".Trait")) {
                        lookForTraitAlias = false;
                        break;
                    }
                    if ("groovy.transform.*".equals(importedType)) {
                        lookForTraitAlias = true;
                    }
                }
                toBeInitialized = true;
            }
        }

        private boolean isTrait(ClassNode classNode) {
            if (classNode == null) {
                return false;
            }
            if (toBeInitialized) {
                initialize();
            }
            List<AnnotationNode> annotations = classNode.getAnnotations();
            if (!annotations.isEmpty()) {
                for (AnnotationNode annotation : annotations) {
                    if ("groovy.transform.Trait".equals(annotation.getClassNode().getName())) {
                        return true;
                    }
                    if (lookForTraitAlias && "Trait".equals(annotation.getClassNode().getName())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Support code for {@link #populateCompilationUnitDeclaration}.
     */
    public static class UnitPopulator {

        private Janitor janitor;
        private SourceUnit sourceUnit;
        private GroovyCompilationUnitDeclaration unitDeclaration;

        private Map<ClassNode, Object> anonymousLocations;
        private boolean checkGenerics = defaultCheckGenerics;

        void populate(GroovyCompilationUnitDeclaration unit) {
            sourceUnit = unit.getSourceUnit();
            unitDeclaration = unit;

            unit.sourceEnds = new HashtableOfObjectToInt();
            ModuleNode moduleNode = unit.getModuleNode();
            try {
                createPackageDeclaration(moduleNode);
                createImportDeclarations(moduleNode);
                createTypeDeclarations(moduleNode);
            } finally {
                if (janitor != null) {
                    janitor.cleanup();
                    janitor = null;
                }
            }
        }

        private void createPackageDeclaration(ModuleNode moduleNode) {
            if (moduleNode.hasPackageName()) {
                String packageName = moduleNode.getPackageName();
                if (packageName.endsWith(".")) {
                    packageName = packageName.substring(0, packageName.length() - 1);
                }
                PackageNode packageNode = moduleNode.getPackage();
                char[][] splits = CharOperation.splitOn('.', packageName.toCharArray());
                long[] positions = positionsFor(splits, startOffset(packageNode), endOffset(packageNode));
                ImportReference ref = new ImportReference(splits, positions, true, Flags.AccDefault);
                ref.annotations = createAnnotations(packageNode.getAnnotations());
                ref.declarationEnd = ref.sourceEnd + trailerLength(packageNode);
                ref.declarationSourceStart = Math.max(0, ref.sourceStart - "package ".length());
                ref.declarationSourceEnd = ref.sourceEnd;

                unitDeclaration.currentPackage = ref;
            }
        }

        private void createImportDeclarations(ModuleNode moduleNode) {
            List<ImportNode> importNodes = moduleNode.getImports();
            List<ImportNode> importPackages = moduleNode.getStarImports();
            Map<String, ImportNode> importStatics = moduleNode.getStaticImports();
            Map<String, ImportNode> importStaticStars = moduleNode.getStaticStarImports();
            int importCount = importNodes.size() + importPackages.size() + importStatics.size() + importStaticStars.size();
            if (importCount > 0) {
                Map<String, ImportReference> importReferences = new TreeMap<>();

                // type imports
                for (ImportNode importNode : importNodes) {
                    int endOffset = endOffset(importNode), nameEndOffset = -2, nameStartOffset = -1;
                    if (endOffset > 0) {
                        nameEndOffset = importNode.getNameEnd() + 1;
                        nameStartOffset = importNode.getNameStart();
                        if (nameStartOffset < 1) {
                            continue; // incomplete import created during recovery
                        }
                    }
                    char[][] splits = CharOperation.splitOn('.', importNode.getClassName().toCharArray());
                    ImportReference ref;
                    if (importNode.getAlias() == null || importNode.getAlias().length() < 1 ||
                            importNode.getAlias().equals(String.valueOf(splits[splits.length - 1]))) {
                        endOffset = nameEndOffset; // endOffset may include extras before ;
                        long[] positions = positionsFor(splits, nameStartOffset, endOffset);
                        ref = new ImportReference(splits, positions, false, Flags.AccDefault);
                    } else {
                        long[] positions = positionsFor(splits, nameStartOffset, endOffset);
                        ref = new AliasImportReference(importNode.getAlias().toCharArray(), splits, positions, false, Flags.AccDefault);
                    }
                    ref.annotations = createAnnotations(importNode.getAnnotations());

                    ref.sourceEnd = Math.max(endOffset - 1, ref.sourceStart); // For error reporting, Eclipse wants -1
                    if (ref.sourceEnd < 0) {
                        // synthetic node; set all source positions to "unknown"
                        ref.declarationSourceStart = -1;
                        ref.declarationSourceEnd = -2;
                        ref.declarationEnd = -2;
                        ref.sourceEnd = -2;
                    } else {
                        ref.declarationEnd = ref.sourceEnd + trailerLength(importNode);
                        ref.declarationSourceStart = startOffset(importNode);
                        ref.declarationSourceEnd = ref.sourceEnd;
                    }

                    importReferences.put(lexicalKey(ref), ref);
                }

                // star imports
                for (ImportNode importPackage : importPackages) {
                    int endOffset = endOffset(importPackage), nameEndOffset = -2, nameStartOffset = -1;
                    if (endOffset > 0) {
                        nameEndOffset = importPackage.getNameEnd() + 1;
                        nameStartOffset = importPackage.getNameStart();
                    }
                    char[][] splits = CharOperation.splitOn('.', importPackage.getPackageName().substring(0, importPackage.getPackageName().length() - 1).toCharArray());
                    ImportReference ref = new ImportReference(splits, positionsFor(splits, nameStartOffset, nameEndOffset), true, Flags.AccDefault);
                    ref.annotations = createAnnotations(importPackage.getAnnotations());

                    ref.sourceEnd = Math.max(endOffset - 1, ref.sourceStart); // For error reporting, Eclipse wants -1
                    if (ref.sourceEnd < 0) {
                        // synthetic node; set all source positions to "unknown"
                        ref.declarationSourceStart = -1;
                        ref.declarationSourceEnd = -2;
                        ref.declarationEnd = -2;
                        ref.sourceEnd = -2;
                    } else {
                        ref.declarationEnd = ref.sourceEnd + trailerLength(importPackage);
                        ref.declarationSourceStart = importPackage.getStart();
                        ref.declarationSourceEnd = ref.sourceEnd;
                    }

                    importReferences.put(lexicalKey(ref), ref);
                }

                // static imports
                for (Map.Entry<String, ImportNode> importStatic : importStatics.entrySet()) {
                    ImportNode importNode = importStatic.getValue();
                    int endOffset = endOffset(importNode), nameEndOffset = -2, nameStartOffset = -1;
                    if (endOffset > 0) {
                        nameEndOffset = importNode.getNameEnd() + 1;
                        nameStartOffset = importNode.getNameStart();
                    }
                    char[][] splits = CharOperation.splitOn('.', (importNode.getClassName() + '.' + importNode.getFieldName()).toCharArray());
                    long[] positions = positionsFor(splits, nameStartOffset, nameEndOffset);
                    ImportReference ref;
                    if (importNode.getAlias() == null || importNode.getAlias().length() < 1 || importNode.getAlias().equals(importNode.getFieldName())) {
                        ref = new ImportReference(splits, positions, false, Flags.AccDefault | Flags.AccStatic);
                    } else {
                        ref = new AliasImportReference(importNode.getAlias().toCharArray(), splits, positions, false, Flags.AccDefault | Flags.AccStatic);
                    }
                    ref.annotations = createAnnotations(importNode.getAnnotations());

                    ref.sourceEnd = Math.max(endOffset - 1, ref.sourceStart); // For error reporting, Eclipse wants -1
                    if (ref.sourceEnd < 0) {
                        // synthetic node; set all source positions to "unknown"
                        ref.declarationSourceStart = -1;
                        ref.declarationSourceEnd = -2;
                        ref.declarationEnd = -2;
                        ref.sourceEnd = -2;
                    } else {
                        ref.declarationEnd = ref.sourceEnd + trailerLength(importNode);
                        ref.declarationSourceStart = startOffset(importNode);
                        ref.declarationSourceEnd = ref.sourceEnd;
                    }

                    importReferences.put(lexicalKey(ref), ref);
                }

                // static star imports
                for (Map.Entry<String, ImportNode> importStaticStar : importStaticStars.entrySet()) {
                    String classname = importStaticStar.getKey();
                    ImportNode importNode = importStaticStar.getValue();
                    int endOffset = endOffset(importNode), nameEndOffset = -2, nameStartOffset = -1;
                    if (endOffset > 0) {
                        nameEndOffset = importNode.getNameEnd() + 1;
                        nameStartOffset = importNode.getNameStart();
                    }
                    char[][] splits = CharOperation.splitOn('.', classname.toCharArray());
                    long[] positions = positionsFor(splits, nameStartOffset, nameEndOffset);
                    ImportReference ref = new ImportReference(splits, positions, true, Flags.AccDefault | Flags.AccStatic);
                    ref.annotations = createAnnotations(importNode.getAnnotations());

                    ref.sourceEnd = Math.max(endOffset - 1, ref.sourceStart); // For error reporting, Eclipse wants -1
                    if (ref.sourceEnd < 0) {
                        // synthetic node; set all source positions to "unknown"
                        ref.declarationSourceStart = -1;
                        ref.declarationSourceEnd = -2;
                        ref.declarationEnd = -2;
                        ref.sourceEnd = -2;
                    } else {
                        ref.declarationEnd = ref.sourceEnd + trailerLength(importNode);
                        ref.declarationSourceStart = importNode.getStart();
                        ref.declarationSourceEnd = ref.sourceEnd;
                    }

                    importReferences.put(lexicalKey(ref), ref);
                }

                if (!importReferences.isEmpty()) {
                    ImportReference[] refs = importReferences.values().toArray(new ImportReference[0]);
                    for (ImportReference ref : refs) {
                        if (ref.declarationSourceStart > 0 && (ref.declarationEnd - ref.declarationSourceStart + 1) < 0) {
                            throw new IllegalStateException(String.format(
                                "Import reference alongside class %s will trigger later failure: %s declSourceStart=%d declEnd=%d",
                                  moduleNode.getClasses().get(0), ref.toString(), ref.declarationSourceStart, ref.declarationEnd));
                        }
                    }
                    unitDeclaration.imports = refs;
                }
            }
        }

        private void createTypeDeclarations(ModuleNode moduleNode) {
            List<ClassNode> moduleClassNodes = moduleNode.getClasses();
            for (ClassNode classNode : moduleClassNodes) {
                if (classNode.isPrimaryClassNode() && GroovyUtils.isAnonymous(classNode)) {
                    anonymousLocations = new HashMap<>();
                    break;
                }
            }
            List<TypeDeclaration> typeDeclarations = new ArrayList<>();
            Map<ClassNode, TypeDeclaration> fromClassNodeToDecl = new HashMap<>();

            char[] mainName = toMainName(unitDeclaration.compilationResult.getFileName());
            Map<ClassNode, List<TypeDeclaration>> innersToRecord = new HashMap<>();
            for (ClassNode classNode : moduleClassNodes) {
                if (!classNode.isPrimaryClassNode()) {
                    continue;
                }

                GroovyTypeDeclaration typeDeclaration = new GroovyTypeDeclaration(unitDeclaration.compilationResult, classNode);
                typeDeclaration.annotations = createAnnotations(classNode.getAnnotations());

                boolean isInner;
                if (classNode.getOuterClass() != null) {
                    isInner = true;
                } else {
                    isInner = false;
                    typeDeclaration.name = classNode.getNameWithoutPackage().toCharArray();
                    if (!CharOperation.equals(typeDeclaration.name, mainName)) {
                        typeDeclaration.bits |= ASTNode.IsSecondaryType;
                    }
                }

                typeDeclaration.modifiers = getModifiers(classNode, isInner);
                fixupSourceLocationsForTypeDeclaration(typeDeclaration, classNode);
                GenericsType[] generics = classNode.getGenericsTypes();
                if (generics != null && generics.length > 0) {
                    typeDeclaration.typeParameters = createTypeParametersForGenerics(classNode.getGenericsTypes());
                }

                boolean isEnum = classNode.isEnum();
                configureSuperClass(typeDeclaration, classNode.getSuperClass(), isEnum, isTrait(classNode));
                configureSuperInterfaces(typeDeclaration, classNode);
                typeDeclaration.fields = createFieldDeclarations(classNode, isEnum);
                typeDeclaration.methods = createConstructorAndMethodDeclarations(classNode, isEnum, typeDeclaration);

                for (Statement statement : classNode.getObjectInitializerStatements()) {
                    if (statement.getEnd() > 0 && typeDeclaration.methods.length > 0)
                        statement.visit(new AnonInnerFinder(typeDeclaration.methods[0]));
                }

                if (isInner) {
                    InnerClassNode innerClassNode = (InnerClassNode) classNode;
                    ClassNode outerClassNode = innerClassNode.getOuterClass();
                    // record that we need to set the parent of this inner type later
                    innersToRecord.computeIfAbsent(outerClassNode, x -> new ArrayList<>()).add(typeDeclaration);

                    if (innerClassNode.isAnonymous()) {
                        typeDeclaration.name = CharOperation.NO_CHAR;
                        typeDeclaration.bits |= (ASTNode.IsAnonymousType | ASTNode.IsLocalType);
                        //typeDeclaration.bits |= (typeDeclaration.superclass.bits & ASTNode.HasTypeAnnotations);
                        QualifiedAllocationExpression allocation = new QualifiedAllocationExpression(typeDeclaration);
                        allocation.sourceStart = isEnum ? typeDeclaration.sourceStart : typeDeclaration.sourceStart - 4; // approx. offset of "new"
                        allocation.sourceEnd = typeDeclaration.bodyEnd;
                        if (!isEnum) allocation.type = typeDeclaration.superclass;
                        // TODO: allocation.typeArguments = something
                    } else {
                        typeDeclaration.name = innerClassNode.getNameWithoutPackage().substring(outerClassNode.getNameWithoutPackage().length() + 1).toCharArray();
                    }
                } else {
                    typeDeclarations.add(typeDeclaration);
                }
                fromClassNodeToDecl.put(classNode, typeDeclaration);
                unitDeclaration.sourceEnds.put(typeDeclaration, typeDeclaration.sourceEnd);
            }

            // now attach local types to their parents; this was not done earlier as sometimes
            // the types are processed in such an order that inners are dealt with before outers
            for (Map.Entry<ClassNode, List<TypeDeclaration>> entry : innersToRecord.entrySet()) {
                TypeDeclaration outerTypeDeclaration = fromClassNodeToDecl.get(entry.getKey());
                if (outerTypeDeclaration == null) {
                    throw new GroovyEclipseBug("Failed to find the type declaration for " + entry.getKey().getText());
                }

                List<TypeDeclaration> memberTypes = entry.getValue();
                for (Iterator<TypeDeclaration> iterator = memberTypes.iterator(); iterator.hasNext();) {
                    GroovyTypeDeclaration innerTypeDeclaration = (GroovyTypeDeclaration) iterator.next();
                    if ((innerTypeDeclaration.bits & ASTNode.IsAnonymousType) != 0) {
                        iterator.remove(); // remove local type from member type list

                        Object location = anonymousLocations.get(innerTypeDeclaration.getClassNode());
                        if (location instanceof AbstractMethodDeclaration) {
                            AbstractMethodDeclaration methodDeclaration = (AbstractMethodDeclaration) location;
                            methodDeclaration.bits |= ASTNode.HasLocalType;
                            methodDeclaration.statements = (org.eclipse.jdt.internal.compiler.ast.Statement[]) ArrayUtils.add(methodDeclaration.statements != null
                                        ? methodDeclaration.statements : new org.eclipse.jdt.internal.compiler.ast.Statement[0], innerTypeDeclaration.allocation);
                        } else if (location instanceof FieldDeclaration) {
                            FieldDeclarationWithInitializer fieldDeclaration = (FieldDeclarationWithInitializer) location;
                            fieldDeclaration.bits |= ASTNode.HasLocalType;
                            if (innerTypeDeclaration.getClassNode().isEnum()) {
                                innerTypeDeclaration.allocation.enumConstant = fieldDeclaration;
                                fieldDeclaration.initialization = innerTypeDeclaration.allocation;
                            } else if (fieldDeclaration.initialization == null) {
                                if (CharOperation.equals(fieldDeclaration.type.getLastToken(), TypeConstants.OBJECT) || GroovyUtils.isAnonymous(fieldDeclaration.initializer.getType())) {
                                    fieldDeclaration.initialization = innerTypeDeclaration.allocation;
                                } else { // in case of indirect anon. inner like "Type foo = bar(1, '2', new Baz() { ... })", fool JDT with "Type foo = (Type) (Object) new Baz() { ... }"
                                    fieldDeclaration.initialization = new CastExpression(innerTypeDeclaration.allocation, createTypeReferenceForClassNode(ClassHelper.OBJECT_TYPE));
                                    fieldDeclaration.initialization.sourceStart = innerTypeDeclaration.sourceStart;
                                    fieldDeclaration.initialization.sourceEnd = innerTypeDeclaration.sourceEnd;

                                    fieldDeclaration.initialization = new CastExpression(fieldDeclaration.initialization, fieldDeclaration.type);
                                    fieldDeclaration.initialization.sourceStart = innerTypeDeclaration.sourceStart;
                                    fieldDeclaration.initialization.sourceEnd = innerTypeDeclaration.sourceEnd;
                                }
                            } else {
                                throw new GroovyEclipseBug("Can't handle more than one anon. inner class in field initializer");
                            }
                        } else {
                            throw new GroovyEclipseBug("Enclosing scope not found for anon. inner class: " + innerTypeDeclaration.getClassNode().getName());
                        }

                        ((GroovyTypeDeclaration) outerTypeDeclaration).addAnonymousType(innerTypeDeclaration);
                    }
                }
                outerTypeDeclaration.memberTypes = memberTypes.toArray(new TypeDeclaration[memberTypes.size()]);
            }

            // clean up
            anonymousLocations = null;

            unitDeclaration.types = typeDeclarations.toArray(new TypeDeclaration[typeDeclarations.size()]);
        }

        /**
         * Build JDT representations of all the fields on the Groovy type.
         */
        private FieldDeclaration[] createFieldDeclarations(ClassNode classNode, boolean isEnum) {
            List<FieldDeclaration> fieldDeclarations = new ArrayList<>();
            List<FieldNode> fieldNodes = classNode.getFields();
            if (fieldNodes != null && !fieldNodes.isEmpty()) {
                boolean isTrait = isTrait(classNode);
                for (FieldNode fieldNode : fieldNodes) {
                    if (isTrait && !(fieldNode.isPublic() && fieldNode.isStatic() && fieldNode.isFinal())) {
                        continue;
                    }
                    if (isEnum && (fieldNode.getName().equals("MAX_VALUE") || fieldNode.getName().equals("MIN_VALUE"))) {
                        continue;
                    }
                    if (fieldNode.getStart() == fieldNode.getNameStart() && ClassHelper.void_WRAPPER_TYPE.equals(fieldNode.getType())) {
                        continue;
                    }
                    boolean isEnumField = fieldNode.isEnum();
                    boolean isSynthetic = GroovyUtils.isSynthetic(fieldNode);
                    if (!isSynthetic) {
                        // JavaStubGenerator ignores private fields but I don't think we want to here
                        FieldDeclarationWithInitializer fieldDeclaration = new FieldDeclarationWithInitializer(fieldNode.getName().toCharArray(), 0, 0);
                        fieldDeclaration.annotations = createAnnotations(fieldNode.getAnnotations());
                        if (!isEnumField) {
                            fieldDeclaration.modifiers = getModifiers(fieldNode);
                            fieldDeclaration.initializer = fieldNode.getInitialExpression();
                            if (fieldNode.isStatic() && fieldNode.isFinal() && fieldNode.getInitialExpression() instanceof ConstantExpression) {
                                // this needs to be set for static finals to correctly determine constant status
                                fieldDeclaration.initialization = createConstantExpression((ConstantExpression) fieldNode.getInitialExpression());
                            }
                            fieldDeclaration.type = createTypeReferenceForClassNode(fieldNode.getType());

                            if (anonymousLocations != null && fieldNode.getInitialExpression() != null) {
                                fieldNode.getInitialExpression().visit(new AnonInnerFinder(fieldDeclaration));
                            }
                        } else if (anonymousLocations != null) {
                            MethodNode clinit = classNode.getMethod("<clinit>", Parameter.EMPTY_ARRAY);
                            if (clinit != null && clinit.getCode() != null) {
                                clinit.getCode().visit(new CodeVisitorSupport() {
                                    private void checkForEnumConstantInitialization(MethodCall call, ClassNode thisType) {
                                        if (call.getMethodAsString().equals("$INIT") && GroovyUtils.isAnonymous(thisType) &&
                                                fieldNode.getName().equals(((ArgumentListExpression) call.getArguments()).getExpression(0).getText())) {
                                            anonymousLocations.put(thisType, fieldDeclaration);
                                        }
                                    }
                                    @Override
                                    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                                        checkForEnumConstantInitialization(call, call.getOwnerType());
                                        super.visitStaticMethodCallExpression(call);
                                    }
                                    @Override
                                    public void visitMethodCallExpression(MethodCallExpression call) {
                                        checkForEnumConstantInitialization(call, call.getType());
                                        super.visitMethodCallExpression(call);
                                    }
                                });
                            }
                        }
                        fixupSourceLocationsForFieldDeclaration(fieldDeclaration, fieldNode);

                        if (fieldDeclarations.add(fieldDeclaration)) {
                            unitDeclaration.sourceEnds.put(fieldDeclaration, fieldDeclaration.sourceEnd);
                        }
                    }
                }
            }
            return fieldDeclarations.toArray(new FieldDeclaration[fieldDeclarations.size()]);
        }

        /**
         * Build JDT representations of all the constructors and methods on the Groovy type.
         */
        private AbstractMethodDeclaration[] createConstructorAndMethodDeclarations(ClassNode classNode, boolean isEnum,
                GroovyTypeDeclaration typeDeclaration) {
            List<AbstractMethodDeclaration> methodDeclarations = new ArrayList<>();
            createConstructorDeclarations(classNode, isEnum, methodDeclarations);
            createMethodDeclarations(classNode, isEnum, typeDeclaration, methodDeclarations);
            return methodDeclarations.toArray(new AbstractMethodDeclaration[methodDeclarations.size()]);
        }

        /**
         * Build JDT representations of all the constructors on the Groovy type.
         */
        private void createConstructorDeclarations(ClassNode classNode, boolean isEnum, List<AbstractMethodDeclaration> methodDeclarations) {
            List<ConstructorNode> constructorNodes = classNode.getDeclaredConstructors();

            char[] ctorName; boolean isAnon = false;
            if (classNode instanceof InnerClassNode) {
                isAnon = ((InnerClassNode) classNode).isAnonymous();
                int qualLength = classNode.getOuterClass().getNameWithoutPackage().length() + 1;
                ctorName = classNode.getNameWithoutPackage().substring(qualLength).toCharArray();
            } else {
                ctorName = classNode.getNameWithoutPackage().toCharArray();
            }

            // add default constructor if no other constructors exist (and not trait/interface/anonymous)
            if (constructorNodes.isEmpty() && !isAnon && !classNode.isInterface() && !isTrait(classNode)) {
                ConstructorDeclaration constructorDecl = new ConstructorDeclaration(unitDeclaration.compilationResult);
                LinkedList<Statement> initializerStatements = (LinkedList<Statement>) classNode.getObjectInitializerStatements();
                if (initializerStatements.isEmpty()) {
                    constructorDecl.bits |= ASTNode.IsDefaultConstructor;
                } else {
                    constructorDecl.declarationSourceStart = initializerStatements.getFirst().getStart();
                    constructorDecl.declarationSourceEnd = initializerStatements.getLast().getEnd() - 1;
                    constructorDecl.sourceStart = constructorDecl.declarationSourceStart + 1;
                    constructorDecl.sourceEnd = constructorDecl.declarationSourceStart;
                    constructorDecl.bodyStart = constructorDecl.declarationSourceStart;
                    constructorDecl.bodyEnd = constructorDecl.declarationSourceEnd - 1;
                }
                if (isEnum) {
                    constructorDecl.modifiers = Flags.AccPrivate;
                } else {
                    int modifiers = getModifiers(classNode, classNode.getOuterClass() != null);
                    constructorDecl.modifiers = modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
                }
                constructorDecl.selector = ctorName;

                if (methodDeclarations.add(constructorDecl)) {
                    unitDeclaration.sourceEnds.put(constructorDecl, constructorDecl.sourceEnd);
                }
            }

            for (ConstructorNode constructorNode : constructorNodes) {
                ConstructorDeclaration constructorDecl = new ConstructorDeclaration(unitDeclaration.compilationResult);
                fixupSourceLocationsForConstructorDeclaration(constructorDecl, constructorNode);
                constructorDecl.annotations = createAnnotations(constructorNode.getAnnotations());
                constructorDecl.arguments = createArguments(constructorNode.getParameters());
                constructorDecl.modifiers = isEnum ? Flags.AccPrivate : getModifiers(constructorNode);
                constructorDecl.selector = ctorName;
                constructorDecl.thrownExceptions = createTypeReferencesForClassNodes(constructorNode.getExceptions());

                if (methodDeclarations.add(constructorDecl)) {
                    unitDeclaration.sourceEnds.put(constructorDecl, constructorNode.getNameEnd());
                }

                if (constructorNode.getCode() != null) {
                    Map<String, VariableExpression> variables = new HashMap<>();
                    constructorNode.getCode().visit(new LocalVariableFinder(variables));
                    if (!variables.isEmpty()) constructorDecl.statements = createStatements(variables.values());
                }
                if (constructorNode.hasDefaultValue()) {
                    for (Argument[] variantArgs : getVariantsAllowingForDefaulting(constructorNode.getParameters(), constructorDecl.arguments)) {
                        ConstructorDeclaration variantDecl = new ConstructorDeclaration(unitDeclaration.compilationResult);
                        variantDecl.annotations = constructorDecl.annotations;
                        variantDecl.arguments = variantArgs;
                        variantDecl.javadoc = constructorDecl.javadoc;
                        variantDecl.modifiers = constructorDecl.modifiers;
                        variantDecl.selector = constructorDecl.selector;
                        variantDecl.sourceEnd = constructorDecl.sourceEnd;
                        variantDecl.sourceStart = constructorDecl.sourceStart;
                        variantDecl.thrownExceptions = constructorDecl.thrownExceptions;

                        variantDecl.declarationSourceStart = 0;
                        variantDecl.declarationSourceEnd = -1;
                        variantDecl.modifiersSourceStart = 0;
                        variantDecl.bodyStart = 0;
                        variantDecl.bodyEnd = -1;

                        if (addUnlessDuplicate(methodDeclarations, variantDecl)) {
                            unitDeclaration.sourceEnds.put(variantDecl, constructorNode.getNameEnd());
                        }
                    }
                }

                if (anonymousLocations != null && constructorNode.getCode() != null) {
                    constructorNode.getCode().visit(new AnonInnerFinder(constructorDecl));
                }
            }
        }

        /**
         * Build JDT representations of all the methods on the Groovy type.
         */
        private void createMethodDeclarations(ClassNode classNode, boolean isEnum, GroovyTypeDeclaration typeDeclaration, List<AbstractMethodDeclaration> methodDeclarations) {
            List<MethodNode> methodNodes = classNode.getMethods();
            if (methodNodes != null && !methodNodes.isEmpty()) {
                boolean isTrait = isTrait(classNode);
                for (MethodNode methodNode : methodNodes) {
                    if (isEnum && methodNode.isSynthetic()) {
                        continue;
                    }
                    if (isTrait && (!methodNode.isPublic() || methodNode.isStatic())) {
                        continue;
                    }

                    AbstractMethodDeclaration methodDecl = createMethodDeclaration(classNode, isEnum, methodNode);
                    if (methodDeclarations.add(methodDecl)) {
                        unitDeclaration.sourceEnds.put(methodDecl, methodNode.getNameEnd());
                    }

                    if (methodNode.isAbstract()) {
                        typeDeclaration.bits |= ASTNode.HasAbstractMethods;
                    } else if (methodNode.getCode() != null) {
                        Map<String, VariableExpression> variables = new HashMap<>();
                        methodNode.getCode().visit(new LocalVariableFinder(variables));
                        if (!variables.isEmpty()) methodDecl.statements = createStatements(variables.values());
                    }
                    if (methodNode.hasDefaultValue()) {
                        for (Argument[] variantArgs : getVariantsAllowingForDefaulting(methodNode.getParameters(), methodDecl.arguments)) {
                            AbstractMethodDeclaration variantDecl = createMethodDeclaration(classNode, isEnum, methodNode);
                            variantDecl.arguments = variantArgs;

                            variantDecl.declarationSourceStart = 0;
                            variantDecl.declarationSourceEnd = -1;
                            variantDecl.modifiersSourceStart = 0;
                            // preserve sourceStart/sourceEnd
                            variantDecl.bodyStart = 0;
                            variantDecl.bodyEnd = -1;

                            if (addUnlessDuplicate(methodDeclarations, variantDecl)) {
                                unitDeclaration.sourceEnds.put(variantDecl, methodNode.getNameEnd());
                            }
                        }
                    }

                    if (anonymousLocations != null && methodNode.getCode() != null) {
                        methodNode.getCode().visit(new AnonInnerFinder(methodDecl));
                    }
                }
            }
        }

        /**
         * Create a JDT {@link MethodDeclaration} that represents a Groovy {@link MethodNode}.
         */
        private AbstractMethodDeclaration createMethodDeclaration(ClassNode classNode, boolean isEnum, MethodNode methodNode) {
            if (classNode.isAnnotationDefinition()) {
                AnnotationMethodDeclaration methodDeclaration = new AnnotationMethodDeclaration(unitDeclaration.compilationResult);
                methodDeclaration.annotations = createAnnotations(methodNode.getAnnotations());
                methodDeclaration.selector = methodNode.getName().toCharArray();
                methodDeclaration.modifiers = getModifiers(methodNode);
                if (methodNode.hasAnnotationDefault()) {
                    methodDeclaration.modifiers |= ClassFileConstants.AccAnnotationDefault;
                    methodDeclaration.defaultValue = createAnnotationMemberExpression(
                        ((ExpressionStatement) methodNode.getCode()).getExpression());
                }
                methodDeclaration.returnType = createTypeReferenceForClassNode(methodNode.getReturnType());
                fixupSourceLocationsForMethodDeclaration(methodDeclaration, methodNode);
                return methodDeclaration;
            } else {
                AbstractMethodDeclaration methodDeclaration = methodNode.isStaticConstructor()
                    ? new Clinit(unitDeclaration.compilationResult) : new MethodDeclaration(unitDeclaration.compilationResult);
                methodDeclaration.annotations = createAnnotations(methodNode.getAnnotations());
                methodDeclaration.selector = methodNode.getName().toCharArray();

                // Note: modifiers for the MethodBinding constructed for this declaration will be created marked
                // with AccVarArgs if the bitset for the type reference in the final argument is marked IsVarArgs
                int modifiers = getModifiers(methodNode);
                Parameter[] params = methodNode.getParameters();
                ClassNode returnType = methodNode.getReturnType();
                // 'static main(args)' would become 'static Object main(Object args)' so make it 'static void main(String[] args)'
                if (Flags.isStatic(modifiers) && "main".equals(methodNode.getName()) && params != null && params.length == 1) {
                    Parameter p = params[0];
                    if (p.getType() == null || p.getType().getName().equals(ClassHelper.OBJECT)) {
                        params = new Parameter[] {new Parameter(ClassHelper.STRING_TYPE.makeArray(), p.getName())};
                        params[0].setSourcePosition(p);
                        if (returnType.getName().equals(ClassHelper.OBJECT)) {
                            returnType = ClassHelper.VOID_TYPE;
                        }
                    }
                }

                methodDeclaration.modifiers = modifiers;
                methodDeclaration.arguments = createArguments(params);
                if (methodDeclaration instanceof MethodDeclaration) {
                    GenericsType[] generics = methodNode.getGenericsTypes();
                    if (generics != null && generics.length > 0) {
                        ((MethodDeclaration) methodDeclaration).typeParameters = createTypeParametersForGenerics(generics);
                    }
                    ((MethodDeclaration) methodDeclaration).returnType = createTypeReferenceForClassNode(returnType);
                }
                methodDeclaration.thrownExceptions = createTypeReferencesForClassNodes(methodNode.getExceptions());
                fixupSourceLocationsForMethodDeclaration(methodDeclaration, methodNode);
                return methodDeclaration;
            }
        }

        //----------------------------------------------------------------------

        private void configureSuperClass(TypeDeclaration typeDeclaration, ClassNode superclass, boolean isEnum, boolean isTrait) {
            if ((isEnum && superclass.getName().equals("java.lang.Enum")) || isTrait) {
                // Don't wire it in, JDT will do it
                typeDeclaration.superclass = null;
            } else {
                // If the start position is 0 the superclass wasn't actually declared, it was added by Groovy
                if (!(superclass.getStart() == 0 && superclass.equals(ClassHelper.OBJECT_TYPE))) {
                    typeDeclaration.superclass = createTypeReferenceForClassNode(superclass);
                }
            }
        }

        private void configureSuperInterfaces(TypeDeclaration typeDeclaration, ClassNode classNode) {
            ClassNode[] interfaces = classNode.getInterfaces();
            if (interfaces != null && interfaces.length > 0) {
                typeDeclaration.superInterfaces = new TypeReference[interfaces.length];
                for (int i = 0, n = interfaces.length; i < n; i += 1) {
                    typeDeclaration.superInterfaces[i] = createTypeReferenceForClassNode(interfaces[i]);
                }
            } else {
                typeDeclaration.superInterfaces = new TypeReference[0];
            }
        }

        private Annotation[] createAnnotations(List<AnnotationNode> groovyAnnotations) {
            if (groovyAnnotations != null && !groovyAnnotations.isEmpty()) {
                List<Annotation> annotations = new ArrayList<>(groovyAnnotations.size());

                for (AnnotationNode annotationNode : groovyAnnotations) {
                    ClassNode annoType = annotationNode.getClassNode();
                    TypeReference annotationReference = createTypeReferenceForClassNode(annoType);
                    annotationReference.sourceStart = annotationNode.getStart();
                    annotationReference.sourceEnd = annotationNode.getEnd() - 1;

                    Map<String, Expression> memberValuePairs = annotationNode.getMembers();
                    if (memberValuePairs == null || memberValuePairs.isEmpty()) {
                        MarkerAnnotation annotation = new MarkerAnnotation(annotationReference, annotationReference.sourceStart);
                        annotations.add(annotation);
                    } else if (memberValuePairs.size() == 1 && memberValuePairs.containsKey("value")) {
                        SingleMemberAnnotation annotation = new SingleMemberAnnotation(annotationReference, annotationReference.sourceStart);
                        annotation.memberValue = createAnnotationMemberExpression(memberValuePairs.get("value"));
                        annotations.add(annotation);
                    } else {
                        NormalAnnotation annotation = new NormalAnnotation(annotationReference, annotationReference.sourceStart);
                        annotation.memberValuePairs = createAnnotationMemberValuePairs(memberValuePairs);
                        annotations.add(annotation);
                    }
                    // TODO: declarationSourceEnd should be rparen position; antlr2 includes any trailing comment
                    annotations.get(annotations.size() - 1).declarationSourceEnd = annotationReference.sourceEnd;
                }

                return annotations.toArray(new Annotation[annotations.size()]);
            }
            return null;
        }

        private org.eclipse.jdt.internal.compiler.ast.Expression createAnnotationMemberExpression(Expression expr) {
            if (expr instanceof ListExpression) {
                ListExpression list = (ListExpression) expr;
                ArrayInitializer arrayInitializer = new ArrayInitializer();
                arrayInitializer.sourceStart = expr.getStart();
                arrayInitializer.sourceEnd = expr.getEnd();

                int n = list.getExpressions().size();
                arrayInitializer.expressions = new org.eclipse.jdt.internal.compiler.ast.Expression[n];
                for (int i = 0; i < n; i += 1) {
                    arrayInitializer.expressions[i] = createAnnotationMemberExpression(list.getExpression(i));
                }
                return arrayInitializer;

            } else if (expr instanceof AnnotationConstantExpression) {
                Annotation[] annos = createAnnotations(Collections.singletonList(
                    (AnnotationNode) ((AnnotationConstantExpression) expr).getValue()));
                assert annos != null && annos.length == 1;
                return annos[0];

            } else if (expr instanceof ConstantExpression) {
                Literal literal = createConstantExpression((ConstantExpression) expr);
                if (literal != null) return literal;

            } else if (expr instanceof VariableExpression) {
                String name = ((VariableExpression) expr).getName();
                // could be a class literal; Groovy does not require ".class" -- resolved in MemberValuePair
                return new SingleNameReference(name.toCharArray(), toPos(expr.getStart(), expr.getEnd() - 1));

            } else if (expr instanceof PropertyExpression) {
                PropertyExpression prop = (PropertyExpression) expr;
                final int propertyEnd = prop.getProperty().getEnd();
                if ("class".equals(prop.getPropertyAsString())) {
                    return new ClassLiteralAccess(propertyEnd, createTypeReferenceForClassLiteral(prop));
                }
                // could still be a class literal; Groovy does not require ".class" -- resolved in MemberValuePair
                char[] text = sourceUnit.readSourceRange(prop.getStart(), propertyEnd - prop.getStart());
                if (text == null || text.length == 0) text = prop.getText().toCharArray();
                char[][] toks = CharOperation.splitOn('.',  text);

                if (toks.length == 1) return new SingleNameReference(toks[0], toPos(prop.getStart(), propertyEnd - 1));
                return new QualifiedNameReference(toks, positionsFor(toks, prop.getStart(), propertyEnd), prop.getStart(), propertyEnd);

            } else if (expr instanceof ClassExpression) {
                char[] text = sourceUnit.readSourceRange(expr.getStart(), expr.getLength());
                if (text == null || text.length == 0) text = expr.getText().toCharArray();
                char[][] toks = CharOperation.splitOn('.', text);

                int n = "class".equals(String.valueOf(toks[toks.length - 1]).trim()) ? toks.length - 1 : toks.length;
                long[] poss = positionsFor(toks, expr.getStart(), expr.getEnd());

                return new ClassLiteralAccess(expr.getEnd(), n == 1 ? new SingleTypeReference(toks[0], poss[0])
                    : new QualifiedTypeReference(Arrays.copyOfRange(toks, 0, n), Arrays.copyOfRange(poss, 0, n)));

            } else if (expr instanceof ClosureExpression) {
                // annotation is something like "@Tag(value = { -> ... })" return "Closure.class" to appease JDT
                return new ClassLiteralAccess(expr.getEnd(), new SingleTypeReference("Closure".toCharArray(), toPos(expr.getStart(), expr.getEnd())));

            } else if (expr instanceof BinaryExpression) {
                // annotation is something like "@Tag(value = List<String)" (incomplete generics specification)

            } else {
                Util.log(IStatus.WARNING, "Unhandled annotation value type: " + expr.getClass().getSimpleName());
            }

            // must be non-null or there will be NPEs in MVP
            return new NullLiteral(expr.getStart(), expr.getEnd());
        }

        private org.eclipse.jdt.internal.compiler.ast.MemberValuePair[] createAnnotationMemberValuePairs(Map<String, Expression> memberValuePairs) {
            List<org.eclipse.jdt.internal.compiler.ast.MemberValuePair> mvps =
                new ArrayList<>(memberValuePairs.size());

            for (Map.Entry<String, Expression> memberValuePair : memberValuePairs.entrySet()) {
                char[] name = memberValuePair.getKey().toCharArray();
                // TODO: What to do when the value expression lacks source position information?
                int start = Math.max(0, memberValuePair.getValue().getStart() - name.length - 1), until = memberValuePair.getValue().getEnd();
                org.eclipse.jdt.internal.compiler.ast.Expression value = createAnnotationMemberExpression(memberValuePair.getValue());
                mvps.add(new org.eclipse.jdt.internal.compiler.ast.MemberValuePair(name, start, until, value));
            }

            return mvps.toArray(new org.eclipse.jdt.internal.compiler.ast.MemberValuePair[mvps.size()]);
        }

        private Literal createConstantExpression(ConstantExpression expr) {
            int start = expr.getStart(), until = expr.getEnd();
            String type = expr.getType().getName();
            Object value = expr.getValue();

            if ("java.lang.Object".equals(type)) {
                assert value == null;
                return new NullLiteral(start, until);
            } else if ("java.lang.String".equals(type)) {
                return new StringLiteral(((String) value).toCharArray(), start, until - 1, expr.getLineNumber());
            } else if ("boolean".equals(type) || "java.lang.Boolean".equals(type)) {
                if ("true".equals(value.toString())) {
                    return new TrueLiteral(start, until);
                } else {
                    return new FalseLiteral(start, until);
                }
            } else if ("int".equals(type) || "java.lang.Integer".equals(type)) {
                // TODO: Should return a unary minus expression for negative values...
                char[] chars = String.valueOf(Math.abs((Integer) value)).toCharArray();
                return IntLiteral.buildIntLiteral(chars, start, start + chars.length);
            } else if ("long".equals(type) || "java.lang.Long".equals(type)) {
                char[] chars = (String.valueOf(Math.abs((Long) value)) + 'L').toCharArray();
                return LongLiteral.buildLongLiteral(chars, start, start + chars.length);
            } else if ("double".equals(type) || "java.lang.Double".equals(type)) {
                return new DoubleLiteral(value.toString().toCharArray(), start, until);
            } else if ("float".equals(type) || "java.lang.Float".equals(type)) {
                return new FloatLiteral(value.toString().toCharArray(), start, until);
            }/* else if ("byte".equals(type) || "java.lang.Byte".equals(type) ||
                        "short".equals(type) || "java.lang.Short".equals(type)) {
                char[] chars = value.toString().toCharArray();
                return IntLiteral.buildIntLiteral(chars, start, start + chars.length);
            }*/ else if ("char".equals(type) || "java.lang.Character".equals(type)) {
                return new CharLiteral(value.toString().toCharArray(), start, until);
            } else if ("java.math.BigDecimal".equals(type)) {
                return new DoubleLiteral(value.toString().toCharArray(), start, until);
            } else if ("java.math.BigInteger".equals(type)) {
                long thing = ((BigInteger) value).abs().longValue();
                char[] chars = (String.valueOf(thing) + 'L').toCharArray();
                return LongLiteral.buildLongLiteral(chars, start, start + chars.length);
            } else {
                Util.log(IStatus.WARNING, "Unhandled annotation constant type: " + expr.getType().getName());
                return null;
            }
        }

        /**
         * Creates JDT Argument representations of Groovy parameters.
         */
        private Argument[] createArguments(Parameter[] ps) {
            if (ps == null || ps.length == 0) {
                return null;
            }
            Argument[] arguments = new Argument[ps.length];
            for (int i = 0; i < ps.length; i++) {
                Parameter parameter = ps[i];
                TypeReference parameterTypeReference = createTypeReferenceForClassNode(parameter.getType());
                long pos;
                int pstart = parameter.getStart();
                if (parameter.getStart() == 0 && parameter.getEnd() == 0) {
                    pos = toPos(-1, -2);
                    pstart = -1;
                } else {
                    pos = toPos(parameter.getStart(), parameter.getEnd() - 1);
                }
                arguments[i] = new Argument(parameter.getName().toCharArray(), pos, parameterTypeReference, Flags.AccDefault);
                arguments[i].annotations = createAnnotations(parameter.getAnnotations());
                arguments[i].declarationSourceStart = pstart;
            }
            if (isVargs(ps)) {
                arguments[ps.length - 1].type.bits |= ASTNode.IsVarArgs;
            }
            return arguments;
        }

        private org.eclipse.jdt.internal.compiler.ast.Statement[] createStatements(Collection<VariableExpression> expressions) {
            final int n = expressions.size();
            Iterator<VariableExpression> it = expressions.iterator();
            org.eclipse.jdt.internal.compiler.ast.Statement[] statements = new org.eclipse.jdt.internal.compiler.ast.Statement[n];

            for (int i = 0; i < n; i += 1) {
                VariableExpression variableExpression = it.next();

                LocalDeclaration variableDeclaration = new LocalDeclaration(variableExpression.getName().toCharArray(), variableExpression.getStart(), variableExpression.getEnd() - 1);
                variableDeclaration.type = createTypeReferenceForClassNode(variableExpression.getOriginType());
                variableDeclaration.bits |= (variableDeclaration.type.bits & ASTNode.HasTypeAnnotations);

                statements[i] = variableDeclaration;
            }

            return statements;
        }

        /**
         * Creates JDT TypeReference that represents the given array ClassNode.
         * The name of the  node is expected to be like 'java.lang.String[][]'.
         * Primitives should be handled by the other create method (sig like '[[I').
         */
        private TypeReference createTypeReferenceForArrayNameTrailingBrackets(ClassNode node, int start, int until) {
            String name = node.getName();
            int dim = 0;
            int pos = name.length() - 2;
            ClassNode componentType = node;
            // jump back counting dimensions
            while (pos > 0 && name.charAt(pos) == '[') {
                dim += 1;
                pos -= 2;
                componentType = componentType.getComponentType();
            }
            if (componentType.isPrimitive()) {
                Integer ii = charToTypeId.get(name.charAt(dim));
                if (ii == null) {
                    throw new IllegalStateException("node " + node + " reported it had a primitive component type, but it does not...");
                } else {
                    TypeReference baseTypeReference = TypeReference.baseTypeReference(ii, dim);
                    baseTypeReference.sourceStart = start;
                    baseTypeReference.sourceEnd = start + componentType.getName().length();
                    return baseTypeReference;
                }
            }
            if (dim == 0) {
                throw new IllegalStateException("Array classnode with dimensions 0?? node:" + node.getName());
            }
            // array component is something like La.b.c; ... or sometimes just [[Z (where Z is a type, not primitive)
            String arrayComponentTypename = name.substring(0, pos + 2);
            return createTypeReferenceForArrayName(arrayComponentTypename, componentType, dim, start, until);
        }

        /**
         * Creates JDT TypeReference that represents the given array ClassNode.
         * Format will be '[[I' or '[[Ljava.lang.String;'.  This latter form is
         * really not right but Groovy can produce it so we need to cope with it.
         */
        private TypeReference createTypeReferenceForArrayNameLeadingBrackets(ClassNode node, int start, int until) {
            String name = node.getName();
            int dim = 0;
            ClassNode componentType = node;
            while (name.charAt(dim) == '[') {
                dim += 1;
                componentType = componentType.getComponentType();
            }
            if (componentType.isPrimitive()) {
                Integer ii = charToTypeId.get(name.charAt(dim));
                if (ii == null) {
                    throw new IllegalStateException("node " + node + " reported it had a primitive component type, but it does not...");
                } else {
                    TypeReference baseTypeReference = TypeReference.baseTypeReference(ii, dim);
                    baseTypeReference.sourceStart = start;
                    baseTypeReference.sourceEnd = start + componentType.getName().length();
                    return baseTypeReference;
                }
            } else {
                String arrayComponentTypename = name.substring(dim);
                if (arrayComponentTypename.charAt(arrayComponentTypename.length() - 1) == ';') {
                    arrayComponentTypename = name.substring(dim + 1, name.length() - 1); // chop off '['s 'L' and ';'
                }
                return createTypeReferenceForArrayName(arrayComponentTypename, componentType, dim, start, until);
            }
        }

        private TypeReference createTypeReferenceForArrayName(String typeName, ClassNode typeNode, int dim, int start, int until) {
            if (!typeNode.isUsingGenerics()) {
                if (typeName.indexOf('.') < 0) {
                    // For a single array reference, for example 'String[]' start will be 'S' and end will be the char after ']'. When the
                    // ArrayTypeReference is built we need these positions for the result: sourceStart - the 'S'; sourceEnd - the ']';
                    // originalSourceEnd - the 'g'
                    ArrayTypeReference atr = new ArrayTypeReference(typeName.toCharArray(), dim, toPos(start, until - 1));
                    atr.originalSourceEnd = atr.sourceStart + typeName.length() - 1;
                    return atr;
                } else {
                    // For a qualified array reference, for example 'java.lang.Number[][]' start will be 'j' and end will be the char after ']'.
                    // When the ArrayQualifiedTypeReference is built we need these positions for the result: sourceStart - the 'j'; sourceEnd - the
                    // final ']'; the positions computed for the reference components would be j..a l..g and N..r
                    char[][] compoundName = CharOperation.splitOn('.', typeName.toCharArray());
                    ArrayQualifiedTypeReference aqtr = new ArrayQualifiedTypeReference(compoundName, dim,
                        positionsFor(compoundName, start, (until == -2 ? -2 : until - dim * 2)));
                    aqtr.sourceEnd = until == -2 ? -2 : until - 1;
                    return aqtr;
                }
            } else {
                GenericsType[] generics = typeNode.getGenericsTypes();
                TypeReference[] typeArgs = new TypeReference[generics.length];
                for (int i = 0; i < generics.length; i += 1) {
                    typeArgs[i] = createTypeReferenceForGenerics(generics[i]);
                }

                if (typeName.indexOf('.') < 0) {
                    ParameterizedSingleTypeReference pstr = new ParameterizedSingleTypeReference(typeName.toCharArray(), typeArgs, dim, toPos(start, until - 1));
                    pstr.originalSourceEnd = pstr.sourceStart + typeName.length() - 1; // TODO: Should this include generics?
                    return pstr;
                } else {
                    char[][] compoundName = CharOperation.splitOn('.', typeName.toCharArray());
                    TypeReference[][] compoundArgs = new TypeReference[compoundName.length][];
                    compoundArgs[compoundName.length - 1] = typeArgs;
                    ParameterizedQualifiedTypeReference pqtr = new ParameterizedQualifiedTypeReference(compoundName, compoundArgs, dim,
                        positionsFor(compoundName, start, (until == -2 ? -2 : until - dim * 2)));
                    pqtr.sourceEnd = until == -2 ? -2 : until - 1;
                    return pqtr;
                }
            }
        }

        private TypeReference createTypeReferenceForClassLiteral(PropertyExpression expression) {
            // FIXASC ignore type parameters for now
            Expression candidate = expression.getObjectExpression();
            List<char[]> nameParts = new LinkedList<>();
            while (candidate instanceof PropertyExpression) {
                nameParts.add(0, ((PropertyExpression) candidate).getPropertyAsString().toCharArray());
                candidate = ((PropertyExpression) candidate).getObjectExpression();
            }
            if (candidate instanceof VariableExpression) {
                nameParts.add(0, ((VariableExpression) candidate).getName().toCharArray());
            }
            char[][] namePartsArr = nameParts.toArray(new char[nameParts.size()][]);
            long[] poss = positionsFor(namePartsArr, expression.getObjectExpression().getStart(), expression.getObjectExpression().getEnd());

            TypeReference ref;
            if (namePartsArr.length > 1) {
                ref = new QualifiedTypeReference(namePartsArr, poss);
            } else if (namePartsArr.length == 1) {
                ref = new SingleTypeReference(namePartsArr[0], poss[0]);
            } else { // should not happen
                ref = TypeReference.baseTypeReference(nameToPrimitiveTypeId.get("void"), 0);
            }
            return ref;
        }

        private TypeReference[] createTypeReferencesForClassNodes(ClassNode[] classNodes) {
            if (classNodes == null) return null;
            final int n = classNodes.length;
            if (n == 0) return null;

            TypeReference[] refs = new TypeReference[n];
            for (int i = 0; i < n; i += 1) {
                refs[i] = createTypeReferenceForClassNode(classNodes[i]);
            }
            return refs;
        }

        private TypeReference createTypeReferenceForClassNode(ClassNode classNode) {
            return createTypeReferenceForClassNode(classNode, startOffset(classNode), endOffset(classNode));
        }

        private TypeReference createTypeReferenceForClassNode(ClassNode classNode, int start, int end) {
            List<TypeReference> typeArguments = null;

            // need to distinguish between raw usage of a type 'List' and generics usage 'List<T>'
            // it basically depends upon whether the type variable reference can be resolved within
            // the current 'scope' - if it cannot then this is probably a raw reference (yes?)

            if (classNode.isUsingGenerics()) {
                GenericsType[] genericsInfo = classNode.getGenericsTypes();
                if (genericsInfo != null) {
                    for (int g = 0; g < genericsInfo.length; g++) {
                        TypeReference tr = createTypeReferenceForGenerics(genericsInfo[g]);
                        if (tr != null) {
                            if (typeArguments == null) {
                                typeArguments = new ArrayList<>();
                            }
                            typeArguments.add(tr);
                        }
                    }
                }
            }

            String name = classNode.getName();

            if (name.length() == 1 && name.charAt(0) == '?') {
                TypeReference tr = new Wildcard(Wildcard.UNBOUND);
                tr.sourceStart = start;
                tr.sourceEnd = end;
                return tr;
            }

            int arrayLoc = name.indexOf("[");
            if (arrayLoc == 0) {
                return createTypeReferenceForArrayNameLeadingBrackets(classNode, start, end);
            } else if (arrayLoc > 0) {
                return createTypeReferenceForArrayNameTrailingBrackets(classNode, start, end);
            }

            if (nameToPrimitiveTypeId.containsKey(name)) {
                TypeReference tr = TypeReference.baseTypeReference(nameToPrimitiveTypeId.get(name), 0);
                tr.sourceStart = start;
                tr.sourceEnd = end;
                return tr;
            }

            if (name.indexOf(".") == -1) {
                if (typeArguments == null) {
                    TypeReference tr = verify(new SingleTypeReference(name.toCharArray(), toPos(start, end - 1)));
                    if (!checkGenerics) {
                        tr.bits |= ASTNode.IgnoreRawTypeCheck;
                    }
                    return tr;
                } else {
                    // FIXASC determine when array dimension used in this case, is it 'A<T[]> or some silliness?
                    long l = toPos(start, end - 1);
                    return new ParameterizedSingleTypeReference(name.toCharArray(),
                            typeArguments.toArray(new TypeReference[typeArguments.size()]), 0, l);
                }
            } else {
                char[][] compoundName = CharOperation.splitOn('.', name.toCharArray());
                if (typeArguments == null) {
                    TypeReference tr = new QualifiedTypeReference(compoundName, positionsFor(compoundName, start, end));
                    if (!checkGenerics) {
                        tr.bits |= ASTNode.IgnoreRawTypeCheck;
                    }
                    return tr;
                } else {
                    // FIXASC support individual parameterization of component references A<String>.B<Wibble>
                    TypeReference[][] typeReferences = new TypeReference[compoundName.length][];
                    typeReferences[compoundName.length - 1] = typeArguments.toArray(new TypeReference[typeArguments.size()]);
                    return new ParameterizedQualifiedTypeReference(compoundName, typeReferences, 0, positionsFor(compoundName, start, end));
                }
            }
        }

        private TypeReference createTypeReferenceForGenerics(GenericsType genericsType) {
            if (genericsType.isWildcard()) {
                ClassNode[] bounds = genericsType.getUpperBounds();
                if (bounds != null) {
                    // FIXASC other bounds?
                    // positions example: (29>31)Set<(33>54)? extends (43>54)Serializable>
                    TypeReference boundReference = createTypeReferenceForClassNode(bounds[0]);
                    Wildcard wildcard = new Wildcard(Wildcard.EXTENDS);
                    wildcard.sourceStart = genericsType.getStart();
                    wildcard.sourceEnd = boundReference.sourceEnd();
                    wildcard.bound = boundReference;
                    return wildcard;
                } else if (genericsType.getLowerBound() != null) {
                    // positions example: (67>69)Set<(71>84)? super (79>84)Number>
                    TypeReference boundReference = createTypeReferenceForClassNode(genericsType.getLowerBound());
                    Wildcard wildcard = new Wildcard(Wildcard.SUPER);
                    wildcard.sourceStart = genericsType.getStart();
                    wildcard.sourceEnd = boundReference.sourceEnd();
                    wildcard.bound = boundReference;
                    return wildcard;
                } else {
                    Wildcard w = new Wildcard(Wildcard.UNBOUND);
                    w.sourceStart = genericsType.getStart();
                    w.sourceEnd = genericsType.getStart();
                    return w;
                }
                // FIXASC what does the check on this next really line mean?
            } else if (!genericsType.getType().isGenericsPlaceHolder()) {
                TypeReference typeReference = createTypeReferenceForClassNode(genericsType.getType());
                return typeReference;
            } else {
                // this means it is a placeholder. As an example, if the reference is to 'List'
                // then the genericsType info may include a placeholder for the type variable (as the user
                // didn't fill it in as anything) and so for this example the genericsType is 'E extends java.lang.Object'
                // I don't think we need a type reference for this as the type references we are constructed
                // here are representative of what the user did in the source, not the resolved result of that.
                // throw new GroovyEclipseBug();
                return null;
            }
        }

        /**
         * <b>Example:</b> {@code Foo<T extends Number & I>} <br>
         *   the type parameter is T, the 'type' is Number and the bounds for the
         *   type parameter are just the extra bound I
         */
        private TypeParameter[] createTypeParametersForGenerics(GenericsType[] generics) {
            final int n = generics.length;
            TypeParameter[] typeParameters = new TypeParameter[n];

            for (int i = 0; i < n; i += 1) {
                TypeParameter typeParameter = new TypeParameter();
                typeParameters[i] = typeParameter;
                typeParameter.name = generics[i].getName().toCharArray();

                int offset = generics[i].getStart(),
                    length = typeParameter.name.length;
                typeParameter.sourceStart = offset;
                typeParameter.sourceEnd = offset + length;

                ClassNode[] upperBounds = generics[i].getUpperBounds();
                if (upperBounds != null && upperBounds.length > 0) {
                    String source = String.valueOf(sourceUnit.readSourceRange(generics[i].getStart(), generics[i].getLength()));
                    // recheck offset with each bound because many will have sloc
                    int _start = startOffset(upperBounds[0]),
                        _until = endOffset(upperBounds[0]);
                    if (_until > 0) {
                        source = source.substring(_start - offset);
                        length = _until - _start;
                        offset = _start;
                    } else {
                        // move past T
                        offset += length;
                        source = source.substring(length);
                        // move past "extends" and w.spaces
                        Matcher m = EXTENDS.matcher(source);
                        if (m.find()) {
                            length = m.group().length();
                            offset += length;
                            source = source.substring(length);
                        }

                        length = upperBounds[0].getName().length();
                        // TODO: Is this correct for qualified and unqualified occurrences?
                        String name = GroovyUtils.splitName(upperBounds[0])[1];
                        assert length == source.indexOf(name) + name.length();

                        // Would a ClassNode with its own generics ever be missing sloc?
                        assert source.length() == length || source.charAt(length) != '<';
                    }

                    typeParameter.type = createTypeReferenceForClassNode(upperBounds[0], offset, offset + length);
                    for (int j = 1, k = upperBounds.length; j < k; j += 1) {
                        if (j == 1) typeParameter.bounds = new TypeReference[k - 1];

                        _start = startOffset(upperBounds[j]);
                        _until = endOffset(upperBounds[j]);
                        if (_until > 0) {
                            source = source.substring(_start - offset);
                            length = _until - _start;
                            offset = _start;
                        } else { // it appears only MetaClass or GeneratedClosure could get here...
                            offset += length;
                            // move past bounds type
                            source = source.substring(length);
                            // move past "&" and w.spaces
                            Matcher m = AND.matcher(source);
                            if (m.find()) {
                                length = m.group().length();
                                offset += length;
                                source = source.substring(length);
                            }

                            length = upperBounds[0].getName().length();
                        }

                        typeParameter.bounds[j - 1] = createTypeReferenceForClassNode(upperBounds[j], offset, offset + length);
                        typeParameter.bounds[j - 1].bits |= ASTNode.IsSuperType;
                    }
                }
            }
            return typeParameters;
        }
        private static final Pattern AND = Pattern.compile("^\\s*&\\s*");
        private static final Pattern EXTENDS = Pattern.compile("^\\s*extends\\s+");

        private static final Map<Character, Integer> charToTypeId = new HashMap<>();
        private static final Map<String, Integer> nameToPrimitiveTypeId = new HashMap<>();
        static {
            charToTypeId.put('D', TypeIds.T_double);
            charToTypeId.put('I', TypeIds.T_int);
            charToTypeId.put('F', TypeIds.T_float);
            charToTypeId.put('J', TypeIds.T_long);
            charToTypeId.put('Z', TypeIds.T_boolean);
            charToTypeId.put('B', TypeIds.T_byte);
            charToTypeId.put('C', TypeIds.T_char);
            charToTypeId.put('S', TypeIds.T_short);
            nameToPrimitiveTypeId.put("double", TypeIds.T_double);
            nameToPrimitiveTypeId.put("int", TypeIds.T_int);
            nameToPrimitiveTypeId.put("float", TypeIds.T_float);
            nameToPrimitiveTypeId.put("long", TypeIds.T_long);
            nameToPrimitiveTypeId.put("boolean", TypeIds.T_boolean);
            nameToPrimitiveTypeId.put("byte", TypeIds.T_byte);
            nameToPrimitiveTypeId.put("char", TypeIds.T_char);
            nameToPrimitiveTypeId.put("short", TypeIds.T_short);
            nameToPrimitiveTypeId.put("void", TypeIds.T_void);
        }

        //----------------------------------------------------------------------

        /**
         * For some input array (usually representing a reference), work out the offset positions, assuming they are dotted. <br>
         * Currently this uses the size of each component to move from start towards end. For the very last one it makes the end
         * position 'end' because in some cases just adding 1+length of previous reference isn't enough. For example in java.util.List[]
         * the end will be the end of [] but reference will only contain 'java' 'util' 'List'
         * <p>
         * Because the 'end' is quite often wrong right now (for example on a return type 'java.util.List[]' the end can be two
         * characters off the end (set to the start of the method name...) - we are just computing the positional information from the
         * start.
         * <p>
         * FIXASC: seems that sometimes, especially for types that are defined as 'def', but are converted to java.lang.Object, end
         * < start. This causes no end of problems. I don't think it is so much the 'declaration' as the fact that is no reference and
         * really what is computed here is the reference for something actually specified in the source code. Coming up with fake
         * positions for something not specified is not entirely unreasonable we should check
         * if the reference in particular needed creating at all in the first place...
         */
        private long[] positionsFor(char[][] reference, long start, long end) {
            long[] result = new long[reference.length];
            if (start == -1 && end == -2) {
                for (int i = 0, max = result.length; i < max; i++) {
                    result[i] = ((-1L << 32) | -2L);
                }
                return result;
            }
            if (start < end) {
                // Do the right thing
                long pos = start;
                for (int i = 0, max = result.length; i < max; i++) {
                    long s = pos;
                    pos = pos + reference[i].length - 1; // jump to the last char of the name
                    result[i] = ((s << 32) | pos);
                    pos += 2; // jump onto the following '.' then off it
                }
            } else {
                // FIXASC this case shouldn't happen (end<start) - uncomment following if to collect diagnostics
                long pos = (start << 32) | start;
                for (int i = 0, max = result.length; i < max; i++) {
                    result[i] = pos;
                }
            }
            return result;
        }

        /** Check for trailing semicolons, spaces, tabs, etc. */
        private int trailerLength(org.codehaus.groovy.ast.ASTNode node) {
            int length = 0;

            if (node.getLastLineNumber() > 0 && sourceUnit != null) {
                if (janitor == null) janitor = new Janitor();
                ReaderSource source = sourceUnit.getSource();
                String line = source.getLine(node.getLastLineNumber(), janitor);

                StringBuilder sb = null;
                int c = 0, endPos = node.getLastColumnNumber() - 1;
                while (endPos < line.length() && ((c = line.charAt(endPos++)) == ';' || c == ' ' || c == '\t')) {
                    (sb != null ? sb : (sb = new StringBuilder())).appendCodePoint(c);
                }
                if (sb != null) length += sb.length();
            }

            return length;
        }

        private int startOffset(org.codehaus.groovy.ast.ASTNode node) {
            int s = node.getStart();
            int e = node.getEnd();
            if (s == 0 && e == 0) {
                return -1;
            } else {
                return s;
            }
        }

        private int endOffset(org.codehaus.groovy.ast.ASTNode node) {
            int s = node.getStart();
            int e = node.getEnd();
            if (s == 0 && e == 0) {
                return -2;
            } else {
                return e;
            }
        }

        private int getModifiers(ClassNode node, boolean isInner) {
            int modifiers = node.getModifiers();
            if (isTrait(node)) {
                modifiers |= Flags.AccInterface;
            }
            if (node.isInterface()) {
                modifiers &= ~Flags.AccAbstract;
            }
            if (node.isEnum()) {
                modifiers &= ~(Flags.AccAbstract | Flags.AccFinal);
                if (isInner && ((InnerClassNode) node).isAnonymous()) {
                    modifiers &= ~(Flags.AccEnum | Flags.AccPublic);
                }
            }
            if (!isInner) {
                modifiers &= ~(Flags.AccProtected | Flags.AccPrivate | Flags.AccStatic);
            }
            if (/*node.isSyntheticPublic() &&*/ hasPackageScopeXform(node, PackageScopeTarget.CLASS)) {
                modifiers &= ~Flags.AccPublic;
            }
            return modifiers;
        }

        private int getModifiers(FieldNode node) {
            int modifiers = node.getModifiers();
            if (node.getDeclaringClass().getProperty(node.getName()) != null && hasPackageScopeXform(node, PackageScopeTarget.FIELDS)) {
                modifiers &= ~Flags.AccPrivate;
            }
            return modifiers;
        }

        private int getModifiers(MethodNode node) {
            int modifiers = node.getModifiers();
            modifiers &= ~(Flags.AccSynthetic | Flags.AccTransient);
            if (node.getCode() == null) {
                modifiers |= ExtraCompilerModifiers.AccSemicolonBody;
            }
            /*if (!node.isAbstract() && !node.isPrivate() && isTrait(node.getDeclaringClass())) {
                modifiers |= Flags.AccDefaultMethod;
            }*/
            if (node.isSyntheticPublic() && hasPackageScopeXform(node, PackageScopeTarget.METHODS)) {
                modifiers &= ~Flags.AccPublic;
            }
            return modifiers;
        }

        private int getModifiers(ConstructorNode node) {
            int modifiers = node.getModifiers();
            if (node.isSyntheticPublic() && hasPackageScopeXform(node, PackageScopeTarget.CONSTRUCTORS)) {
                modifiers &= ~Flags.AccPublic;
            }
            return modifiers;
        }

        private boolean hasPackageScopeXform(AnnotatedNode node, final PackageScopeTarget type) {
            boolean member = (!(node instanceof ClassNode) && type != PackageScopeTarget.CLASS);
            for (AnnotationNode anno : node.getAnnotations()) {
                if (isType("groovy.transform.PackageScope", anno.getClassNode().getName())) {
                    Expression expr = anno.getMember("value");
                    if (expr == null) {
                        // if empty @PackageScope, node type and target type must be in alignment
                        return member || (node instanceof ClassNode && type == PackageScopeTarget.CLASS);
                    }

                    final boolean[] val = new boolean[1];
                    expr.visit(new CodeVisitorSupport() {
                        @Override
                        public void visitPropertyExpression(PropertyExpression property) {
                            if (isType("groovy.transform.PackageScopeTarget", property.getObjectExpression().getText()) &&
                                    property.getPropertyAsString().equals(type.name())) {
                                val[0] = true;
                            }
                        }
                        @Override
                        public void visitVariableExpression(VariableExpression variable) {
                            if (variable.getName().equals(type.name())) {
                                ModuleNode mod = sourceUnit.getAST();
                                ImportNode imp = mod.getStaticImports().get(type.name());
                                if (imp != null && isType("groovy.transform.PackageScopeTarget", imp.getType().getName())) {
                                    val[0] = true;
                                } else if (imp == null && mod.getStaticStarImports().get("groovy.transform.PackageScopeTarget") != null) {
                                    val[0] = true;
                                }
                            }
                        }
                    });
                    return val[0];
                }
            }
            if (member) { // check for @PackageScope(XXX) on class
                return hasPackageScopeXform(node.getDeclaringClass(), type);
            }
            return false;
        }

        private boolean isTrait(ClassNode classNode) {
            return unitDeclaration.traitHelper.isTrait(classNode);
        }

        /**
         * @return true if this is varargs, using the same definition as in AsmClassGenerator.isVargs(Parameter[])
         */
        private boolean isVargs(Parameter[] parameters) {
            if (parameters.length == 0) {
                return false;
            }
            Parameter last = parameters[parameters.length - 1];
            ClassNode type = last.getType();
            return type.isArray();
        }

        /**
         * @param expect fully-qualified type name
         * @param actual fully-qualified or unqualified type name (may be resolved against imports)
         */
        private boolean isType(String expect, String actual) {
            if (actual.equals(expect)) {
                return true;
            }
            int dot = expect.lastIndexOf('.');
            if (dot != -1 && actual.equals(expect.substring(dot + 1))) {
                ModuleNode mod = sourceUnit.getAST();
                ClassNode imp = mod.getImportType(actual);
                if (imp != null) {
                    return imp.getName().equals(expect);
                }
                String pkg = expect.substring(0, dot + 1);
                for (ImportNode sin : mod.getStarImports()) {
                    if (sin.getPackageName().equals(pkg)) {
                        return true;
                    }
                }
                // TODO: check default imports
            }
            return false;
        }

        /**
         * Ensures lexical ordering and synthetic de-duplication.
         */
        private static String lexicalKey(ImportReference ref) {
            StringBuilder key = new StringBuilder();
            key.append(Prefix.format(ref.declarationSourceStart));
            key.append(CharOperation.concatWith(ref.tokens, '.'));
            if ((ref.bits & ASTNode.OnDemand) == 0) {
                key.append(" as ").append(ref.getSimpleName());
            }
            return key.toString();
        }
        private static final NumberFormat Prefix;
        static {
            Prefix = NumberFormat.getInstance();
            Prefix.setMinimumIntegerDigits(5);
            Prefix.setGroupingUsed(false);
        }

        private static char[] toMainName(char[] fileName) {
            if (fileName == null) {
                return CharOperation.NO_CHAR;
            }

            int start = CharOperation.lastIndexOf('/', fileName) + 1;
            if (start == 0 || start < CharOperation.lastIndexOf('\\', fileName))
                start = CharOperation.lastIndexOf('\\', fileName) + 1;

            int end = CharOperation.lastIndexOf('.', fileName);
            if (end == -1)
                end = fileName.length;

            return CharOperation.subarray(fileName, start, end);
        }

        // because 'length' is computed as 'end-start+1' and start==-1 indicates it does not exist, then
        // to have a length of 0 the end must be -2.
        private static long NON_EXISTENT_POSITION = ((-1L << 32) | -2L);

        /**
         * Pack start and end positions into a long - no adjustments are made to the values passed in, the caller must make any required
         * adjustments.
         */
        private static long toPos(long start, long end) {
            if (start == 0 && end <= 0) {
                return NON_EXISTENT_POSITION;
            } else if (start < 0 || end < 0) {
                return NON_EXISTENT_POSITION;
            }
            return ((start << 32) | end);
        }

        /**
         * Find any javadoc that terminates on one of the two lines before the specified line, return the first bit encountered. A
         * little crude but will cover a lot of common cases... <br>
         */
        // FIXASC when the parser correctly records javadoc for nodes alongside them during a parse, we will not have to search
        private Javadoc findJavadoc(int line) {
            for (Comment comment : sourceUnit.getComments()) {
                if (comment.isJavadoc()) {
                    if (comment.getLastLine() + 1 == line || (comment.getLastLine() + 2 == line && !comment.usedUp)) {
                        int[] pos = comment.getPositions(unitDeclaration.compilationResult.lineSeparatorPositions);
                        comment.usedUp = true;
                        return new Javadoc(pos[0], pos[1]);
                    }
                }
            }
            return null;
        }

        /**
         * Try to get the source locations for type declarations to be as correct as possible
         */
        private void fixupSourceLocationsForTypeDeclaration(GroovyTypeDeclaration typeDeclaration, ClassNode classNode) {
            if (GroovyUtils.isAnonymous(classNode)) {
                // offset of type name
                typeDeclaration.sourceStart = classNode.getNameStart();
                // offset of ')'
                typeDeclaration.sourceEnd = classNode.getStart() - 2;
                // offset of '{' plus 1
                typeDeclaration.bodyStart = classNode.getStart() + 1;
                // offset of '}'
                typeDeclaration.bodyEnd = classNode.getEnd() - 1;

                typeDeclaration.declarationSourceStart = typeDeclaration.sourceStart;
                typeDeclaration.declarationSourceEnd = typeDeclaration.bodyEnd;
            } else {
                // start and end of the type name; scripts do not have a name, so use start instead
                typeDeclaration.sourceStart = Math.max(classNode.getNameStart(), classNode.getStart());
                typeDeclaration.sourceEnd = Math.max(classNode.getNameEnd(), classNode.getStart()); // incl. extends/implements?

                // start and end of the entire declaration including Javadoc and ending at the last close bracket
                Javadoc doc = findJavadoc(classNode.getLineNumber());
                if (doc != null) {
                    if (unitDeclaration.imports != null && unitDeclaration.imports.length > 0) {
                        if (doc.sourceStart < unitDeclaration.imports[unitDeclaration.imports.length - 1].sourceStart) {
                            // ignore the doc if it should be associated with and import statement
                            doc = null;
                        }
                    } else if (unitDeclaration.currentPackage != null) {
                        if (doc.sourceStart < unitDeclaration.currentPackage.sourceStart) {
                            // ignore the doc if it should be associated with the package statement
                            doc = null;
                        }
                    }
                    typeDeclaration.javadoc = doc;
                }

                typeDeclaration.declarationSourceStart = (doc != null ? doc.sourceStart : classNode.getStart());
                // without the -1 we can hit AIOOBE in org.eclipse.jdt.internal.core.Member.getJavadocRange where it
                // calls getText() because the source range length causes us to ask for more data than is in the buffer
                // What does this mean? For hovers, the AIOOBE is swallowed and you just see no hover box.
                typeDeclaration.declarationSourceEnd = classNode.getEnd() - 1;

                // TODO: start past the opening brace and end before the closing brace
                //       except that scripts do not have a name, use the start instead
                //typeDeclaration.bodyStart = typeDeclaration.sourceEnd;
                typeDeclaration.bodyEnd = typeDeclaration.declarationSourceEnd;

                // start of the modifiers after the javadoc
                typeDeclaration.modifiersSourceStart = classNode.getStart();
            }
        }

        /**
         * Try to get the source locations for constructor declarations to be as correct as possible
         */
        private void fixupSourceLocationsForConstructorDeclaration(ConstructorDeclaration constructorDecl, ConstructorNode constructorNode) {
            Javadoc doc = findJavadoc(constructorNode.getLineNumber());
            constructorDecl.javadoc = doc;

            constructorDecl.declarationSourceStart = (doc != null ? doc.sourceStart : constructorNode.getStart());
            constructorDecl.modifiersSourceStart = constructorNode.getStart();
            constructorDecl.declarationSourceEnd = constructorNode.getEnd()-1;
            constructorDecl.sourceStart = constructorNode.getNameStart();
            constructorDecl.sourceEnd = rparenOffset(constructorNode);

            // opening bracket -- should it be first character after?
            constructorDecl.bodyStart = (constructorNode.getCode() != null ? constructorNode.getCode().getStart()
                : constructorDecl.sourceEnd + 1); // approximate position of anticipated '{'

            // last character before closing bracket
            constructorDecl.bodyEnd = constructorDecl.declarationSourceEnd - 1;
        }

        /**
         * Try to get the source locations for method declarations to be as correct as possible
         */
        private void fixupSourceLocationsForMethodDeclaration(AbstractMethodDeclaration methodDecl, MethodNode methodNode) {
            Javadoc doc = findJavadoc(methodNode.getLineNumber());
            methodDecl.javadoc = doc;

            methodDecl.declarationSourceStart = (doc != null ? doc.sourceStart : methodNode.getStart());
            methodDecl.modifiersSourceStart = methodNode.getStart();
            methodDecl.declarationSourceEnd = methodNode.getEnd()-1;

            // script run() methods have no name, so use the start of the method instead
            methodDecl.sourceStart = Math.max(methodNode.getNameStart(), methodNode.getStart());
            methodDecl.sourceEnd = Math.max(rparenOffset(methodNode), methodNode.getStart());

            // opening bracket -- abstract methods, annotation methods, and script run() methods have no opening bracket
            methodDecl.bodyStart = (methodNode.getCode() != null ? methodNode.getCode().getStart() : methodDecl.sourceEnd + 1);

            // last character before closing bracket or semicolon
            methodDecl.bodyEnd = methodDecl.declarationSourceEnd - 1;
            if (methodDecl instanceof AnnotationMethodDeclaration) {
                methodDecl.bodyEnd += 1; // no '}' and usually no ';'
            }
        }

        /**
         * Try to get the source locations for field declarations to be as correct as possible
         */
        private void fixupSourceLocationsForFieldDeclaration(FieldDeclaration fieldDecl, FieldNode fieldNode) {
            Javadoc doc = findJavadoc(fieldNode.getLineNumber());
            fieldDecl.javadoc = doc;

            fieldDecl.declarationSourceStart = (doc != null ? doc.sourceStart : fieldNode.getStart());
            fieldDecl.modifiersSourceStart = fieldNode.getStart();
            fieldDecl.declarationSourceEnd = fieldNode.getEnd()-1;

            fieldDecl.sourceStart = fieldNode.getNameStart();
            fieldDecl.sourceEnd = fieldNode.getNameEnd();

            // Here, we distinguish between the declaration and the fragment, e.g.- def x = 9, y = "l"
            // 'x = 9,' and 'y = "l"' are the fragments and 'def x = 9, y = "l"' is the declaration

            // end of the entire field declaration (after all fragments and including ';' if exists)
            fieldDecl.declarationEnd = fieldNode.getEnd();

            // TODO (groovy) each area marked with a '*' is only approximate
            // and can be revisited to make more precise

            // * end of the type declaration part of the declaration (the same for each fragment)
            // eg- int x, y corresponds to the location after 'int'
            fieldDecl.endPart1Position = fieldNode.getNameStart();

            // * just before the start of the next fragment
            // (or the end of the entire declaration if it is the last one)
            // (how is this different from declarationSourceEnd?)
            fieldDecl.endPart2Position = fieldNode.getEnd() - 1;
        }

        /**
         * In the given list of groovy parameters, some are defined as defaulting to an initial value. This method computes all the
         * variants of defaulting parameters allowed and returns a List of Argument arrays. Each argument array represents a variation.
         */
        private static List<Argument[]> getVariantsAllowingForDefaulting(Parameter[] groovyParameters, Argument[] javaArguments) {
            List<Argument[]> variants = new ArrayList<>();

            final int nParams = groovyParameters.length;
            Parameter[] wipableParameters = new Parameter[nParams];
            System.arraycopy(groovyParameters, 0, wipableParameters, 0, nParams);

            // Algorithm: wipableParameters is the 'full list' of parameters at the start. As the loop is repeated, all the non-null
            // values in the list indicate a parameter variation. On each repeat we null the last one in the list that
            // has an initial expression. This is repeated until there are no more left to null.

            List<Argument> variantArgs = new ArrayList<>(nParams);
            int nextToLetDefault;
            do {
                variantArgs.clear();
                nextToLetDefault = -1;
                // create a variation based on the non-null entries left in the array
                for (int p = 0; p < nParams; p += 1) {
                    if (wipableParameters[p] != null) {
                        variantArgs.add(javaArguments[p]);
                        if (wipableParameters[p].hasInitialExpression()) {
                            nextToLetDefault = p;
                        }
                    }
                }
                if (nextToLetDefault != -1) {
                    wipableParameters[nextToLetDefault] = null;
                }
                int nArgs = variantArgs.size();
                if (nArgs < nParams) { // no need to return the original arguments
                    variants.add(nArgs == 0 ? null : variantArgs.toArray(new Argument[nArgs]));
                }
            } while (nextToLetDefault != -1);

            return variants;
        }

        /**
         * Add the new declaration to the list of those already built unless it clashes with an existing one. This can happen where the
         * default parameter mechanism causes creation of a variant that collides with an existing declaration. I'm not sure if Groovy
         * should be reporting an error when this occurs, but Grails does actually do it and gets no error.
         */
        private boolean addUnlessDuplicate(List<AbstractMethodDeclaration> methodDeclarations, AbstractMethodDeclaration newDeclaration) {
            boolean isDuplicate = false;

            for (AbstractMethodDeclaration aMethodDecl : methodDeclarations) {
                if (CharOperation.equals(aMethodDecl.selector, newDeclaration.selector)) {
                    Argument[] mdArgs = aMethodDecl.arguments;
                    Argument[] vmdArgs = newDeclaration.arguments;
                    int mdArgsLen = mdArgs == null ? 0 : mdArgs.length;
                    int vmdArgsLen = vmdArgs == null ? 0 : vmdArgs.length;
                    if (mdArgsLen == vmdArgsLen) {
                        boolean argsTheSame = true;
                        for (int i = 0; i < mdArgsLen; i += 1) {
                            // FIXASC this comparison can fail if some are fully qualified and some not - in fact it
                            // suggests that default param variants should be built by augmentMethod() in a similar
                            // way to the GroovyObject methods, rather than during type declaration construction
                            if (!CharOperation.equals(mdArgs[i].type.getTypeName(), vmdArgs[i].type.getTypeName())) {
                                argsTheSame = false;
                                break;
                            }
                        }
                        if (argsTheSame) {
                            isDuplicate = true;
                            break;
                        }
                    }
                }
            }

            return !isDuplicate ? methodDeclarations.add(newDeclaration) : false;
        }

        private static int rparenOffset(MethodNode methodNode) {
            Integer rparenOffset = methodNode.getNodeMetaData("rparen.offset");
            if (rparenOffset != null) return rparenOffset.intValue();
            int nameEnd = methodNode.getNameEnd();
            if (nameEnd > 0) nameEnd += 2;
            return nameEnd;
        }

        /**
         * Check the supplied TypeReference. If there are problems with the construction of a TypeReference then these may not surface
         * until it is used later, perhaps when reconciling. The easiest way to check there will not be problems later is to check it at
         * construction time.
         *
         * @param toVerify the type reference to check
         * @param does the type reference really exist in the source or is it conjured up based on the source
         * @return the verified type reference
         * @throws IllegalStateException if the type reference is malformed
         */
        private static TypeReference verify(TypeReference toVerify) {
            if (toVerify.getClass().equals(SingleTypeReference.class)) {
                SingleTypeReference str = (SingleTypeReference) toVerify;
                if (str.sourceStart == -1) {
                    if (str.sourceEnd != -2) {
                        throw new IllegalStateException("TypeReference '" + String.valueOf(str.token) + " should end at -2");
                    }
                } else {
                    if (str.sourceEnd < str.sourceStart) {
                        throw new IllegalStateException("TypeReference '" + String.valueOf(str.token) + " should end at " + str.sourceStart + " or later");
                    }
                }
            } else {
                throw new IllegalStateException("Cannot verify type reference of this class " + toVerify.getClass());
            }
            return toVerify;
        }

        private class AnonInnerFinder extends CodeVisitorSupport {
            private AnonInnerFinder(Object enclosingDecl) {
                this.enclosingDecl = enclosingDecl;
            }
            private final Object enclosingDecl;

            @Override
            public void visitConstructorCallExpression(ConstructorCallExpression call) {
                if (call.isUsingAnonymousInnerClass()) {
                    anonymousLocations.put(call.getType(), enclosingDecl);
                }
                super.visitConstructorCallExpression(call);
            }
        }

        private static class LocalVariableFinder extends CodeVisitorSupport {
            private LocalVariableFinder(Map<String, VariableExpression> variables) {
                this.variables = variables;
            }
            private final Map<String, VariableExpression> variables;

            @Override
            public void visitConstructorCallExpression(ConstructorCallExpression expression) {
                // don't visit anon. inner types
            }

            @Override
            public void visitDeclarationExpression(DeclarationExpression expression) {
                if (!expression.isMultipleAssignmentDeclaration()) {
                    variables.putIfAbsent(expression.getVariableExpression().getName(), expression.getVariableExpression());
                } else {
                    for (Expression expr : expression.getTupleExpression().getExpressions()) {
                        variables.putIfAbsent(((VariableExpression) expr).getName(), (VariableExpression) expr);
                    }
                }
                super.visitDeclarationExpression(expression);
            }
        }
    }

    /**
     * Holds on to the groovy initializer so we can return it later.
     * This is much easier than translating it into a JDT initializer and back again later.
     */
    public static class FieldDeclarationWithInitializer extends FieldDeclaration {
        private Expression initializer;

        public FieldDeclarationWithInitializer(char[] name, int sourceStart, int sourceEnd) {
            super(name, sourceStart, sourceEnd);
        }

        public Expression getGroovyInitializer() {
            return initializer;
        }
    }
}
