/*
 * Copyright 2008-2013 the original author or authors.
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

package org.codehaus.groovy.transform;

import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.syntax.SyntaxException;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.GroovyException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
/**
 * This class handles the invocation of the ASTAnnotationTransformation
 * when it is encountered by a tree walk.  One instance of each exists
 * for each phase of the compilation it applies to.  Before invocation the
 * <p>
 * {@link org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor} will add a list
 * of annotations that this visitor should be concerned about.  All other
 * annotations are ignored, whether or not they are GroovyASTTransformation
 * annotated or not.
 * <p>
 * A Two-pass method is used. First all candidate annotations are added to a
 * list then the transformations are called on those collected annotations.
 * This is done to avoid concurrent modification exceptions during the AST tree
 * walk and allows the transformations to alter any portion of the AST tree.
 * Hence annotations that are added in this phase will not be processed as
 * transformations.  They will only be handled in later phases (and then only
 * if the type was in the AST prior to any AST transformations being run
 * against it).
 *
 * @author Danno Ferrin (shemnon)
 */
public final class ASTTransformationVisitor extends ClassCodeVisitorSupport {

    private final ASTTransformationsContext context;
    private final CompilePhase phase;
    private SourceUnit source;
    private List<ASTNode[]> targetNodes;
    private Map<ASTNode, List<ASTTransformation>> transforms;
    private Map<Class<? extends ASTTransformation>, ASTTransformation> transformInstances;
    private static CompilationUnit compUnit;
    private static Set<String> globalTransformNames = new HashSet<String>();

    private ASTTransformationVisitor(final CompilePhase phase, final ASTTransformationsContext context) {
        this.phase = phase;
        this.context = context;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Main loop entry.
     * <p>
     * First, it delegates to the super visitClass so we can collect the
     * relevant annotations in an AST tree walk.
     * <p>
     * Second, it calls the visit method on the transformation for each relevant
     * annotation found.
     *
     * @param classNode the class to visit
     */
    public void visitClass(ClassNode classNode) {
        // only descend if we have annotations to look for
        Map<Class<? extends ASTTransformation>, Set<ASTNode>> baseTransforms = classNode.getTransforms(phase);
        if (!baseTransforms.isEmpty()) {
            final Map<Class<? extends ASTTransformation>, ASTTransformation> transformInstances = new HashMap<Class<? extends ASTTransformation>, ASTTransformation>();
            for (Class<? extends ASTTransformation> transformClass : baseTransforms.keySet()) {
                try {
                    transformInstances.put(transformClass, transformClass.newInstance());
                } catch (InstantiationException e) {
                    source.getErrorCollector().addError(
                            new SimpleMessage(
                                    "Could not instantiate Transformation Processor " + transformClass
                                    , //+ " declared by " + annotation.getClassNode().getName(),
                                    source));
                } catch (IllegalAccessException e) {
                    source.getErrorCollector().addError(
                            new SimpleMessage(
                                    "Could not instantiate Transformation Processor " + transformClass
                                    , //+ " declared by " + annotation.getClassNode().getName(),
                                    source));
                }
            }



            // invert the map, is now one to many
            transforms = new HashMap<ASTNode, List<ASTTransformation>>();
            for (Map.Entry<Class<? extends ASTTransformation>, Set<ASTNode>> entry : baseTransforms.entrySet()) {
                for (ASTNode node : entry.getValue()) {
                    List<ASTTransformation> list = transforms.get(node);
                    if (list == null)  {
                        list = new ArrayList<ASTTransformation>();
                        transforms.put(node, list);
                    }
                    list.add(transformInstances.get(entry.getKey()));
                }
            }

            targetNodes = new LinkedList<ASTNode[]>();

            // first pass, collect nodes
            super.visitClass(classNode);

            // second pass, call visit on all of the collected nodes
            for (ASTNode[] node : targetNodes) {
                for (ASTTransformation snt : transforms.get(node[0])) {
                //GRECLIPSE start was unmarked (but looks like logging crap, but uses greclipse logger se must be greclipse
                	try {
                		long stime = System.nanoTime();
                		boolean okToSet = source!=null && source.getErrorCollector()!=null;
                		try {
                			if (okToSet) {
                				source.getErrorCollector().transformActive=true; 
                			}
                //GRECLIPSE end
                			if (snt instanceof CompilationUnitAware) {
                        		((CompilationUnitAware)snt).setCompilationUnit(context.getCompilationUnit());
                   			}
	                		snt.visit(node, source);
                //GRECLIPSE start was unmarked (but looks like logging crap, but uses greclipse logger se must be greclipse
                		} finally {
                			if (okToSet) {
                				source.getErrorCollector().transformActive=false;
                			}
                		}
                		long etime = System.nanoTime(); 
                		if (GroovyLogManager.manager.hasLoggers()) {
                			try {
	                			GroovyLogManager.manager.log(TraceCategory.AST_TRANSFORM,"Local transform "+snt.getClass().getName()+" on "+classNode.getName()+":"+node[1]+" = "+((etime-stime)/1000000)+"ms");
	            			} catch (Throwable t) {
	            				t.printStackTrace();
	            			}
                		}
                //GRECLIPSE end
                	// GRECLIPSE-977 - start
                	} catch (NoClassDefFoundError ncdfe) {
                		String transformName = snt.getClass().getName();
                		StringBuilder sb = new StringBuilder();
                		sb.append("Unable to run AST transform "+transformName+": missing class "+ncdfe.getMessage());
                		sb.append(": are you attempting to use groovy classes in an AST transform in the same project in which it is defined? http://groovy.codehaus.org/Eclipse+Plugin+2.0.0+FAQ#EclipsePlugin2.0.0FAQ-Q.DoesitsupportcustomASTtransformations%3F");
                		source.addError(new SyntaxException(sb.toString(), ncdfe, 0, 0));
                	}
                	// GRECLIPSE - end
                }
            }
        }
    }

    /**
     * Adds the annotation to the internal target list if a match is found.
     *
     * @param node the node to be processed
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (transforms.containsKey(annotation)) {
                targetNodes.add(new ASTNode[]{annotation, node});
            }
        }
    }
    

    public static void addPhaseOperations(final CompilationUnit compilationUnit) {
        final ASTTransformationsContext context = compilationUnit.getASTTransformationsContext();
        addGlobalTransforms(context);
        // GRECLIPSE
        // want a subset to run during a reconcile...
//        if (!compilationUnit.allowTransforms && compilationUnit.localTransformsToRunOnReconcile.size()==0) {
//        	return;
//        }
        // GRECLIPSE

        compilationUnit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTTransformationCollectorCodeVisitor collector = 
                	//GRECLIPSE? (was unmarked but looks like added params to control AST transform execution)
                    new ASTTransformationCollectorCodeVisitor(source, compilationUnit.getTransformLoader(),compilationUnit.allowTransforms,compilationUnit.localTransformsToRunOnReconcile);
                    //GRECLIPSE end
                collector.visitClass(classNode);
            }
        }, Phases.SEMANTIC_ANALYSIS);
        for (CompilePhase phase : CompilePhase.values()) {
            final ASTTransformationVisitor visitor = new ASTTransformationVisitor(phase, context);
            switch (phase) {
                case INITIALIZATION:
                case PARSING:
                case CONVERSION:
                    // with transform detection alone these phases are inaccessible, so don't add it
                    break;

                default:
                    compilationUnit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
                        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                            visitor.source = source;
                            visitor.visitClass(classNode);
                        }
                    }, phase.getPhaseNumber());
                    break;

            }
        }
    }

    public static void addGlobalTransformsAfterGrab(ASTTransformationsContext context) {
        doAddGlobalTransforms(context, false);
    }
    
    public static void addGlobalTransforms(ASTTransformationsContext context) {
        doAddGlobalTransforms(context, true);
    }

    private static void doAddGlobalTransforms(ASTTransformationsContext context, boolean isFirstScan) {
        final CompilationUnit compilationUnit = context.getCompilationUnit();
    	// GRECLIPSE: added check to determine what is allowed through:
    	ensurelobalTransformsAllowedInReconcileInitialized();
        GroovyClassLoader transformLoader = compilationUnit.getTransformLoader();
        Map<String, URL> transformNames = new LinkedHashMap<String, URL>();
        try {
            Enumeration<URL> globalServices = transformLoader.getResources("META-INF/services/org.codehaus.groovy.transform.ASTTransformation");
            while (globalServices.hasMoreElements()) {
                URL service = globalServices.nextElement();
                String className;
                
                // GRECLIPSE: start: don't consume our own META-INF entries - bit of a hack...
//                try {
//                	// this unfortunately also prevents the execution of transforms from project dependencies!
//					String file = service.getFile().toString();
//					System.out.println("TRANSFORM: Processing META-INF file "+file);					
//					if (file.indexOf("jar!")==-1) {
//						// it is a 'local' services file
//						// eg. /META-INF/services/org.codehaus.groovy.transform.ASTTransformation
//						// whereas a jar related one would be:
//						// eg. file:/N:/workspaces/groovy35/org.codehaus.groovy/lib/groovy-all-1.7-rc-1.jar!/META-INF/services/org.codehaus.groovy.transform.ASTTransformation
//						continue;
//					}
//                } catch (Throwable t) {
//                	t.printStackTrace();
//                }
                // end
                // GRECLIPSE start
                // was
                // BufferedReader svcIn = new BufferedReader(new InputStreamReader(service.openStream()));
                // now
           
           		BufferedReader svcIn = null;
                InputStream is = service.openStream();
                try {
	                svcIn = new BufferedReader(new InputStreamReader(is));               
 	               // end
   	               try {
    	               className = svcIn.readLine();
        	       } catch (IOException ioe) {
            	       compilationUnit.getErrorCollector().addError(new SimpleMessage(
                       	"IOException reading the service definition at "
                       + service.toExternalForm() + " because of exception " + ioe.toString(), null));
                    	continue;
                   }
                Set<String> disabledGlobalTransforms = compilationUnit.getConfiguration().getDisabledGlobalASTTransformations();
                if (disabledGlobalTransforms==null) disabledGlobalTransforms=Collections.emptySet();
                while (className != null) {
                    if (!className.startsWith("#") && className.length() > 0) {
                        if (!disabledGlobalTransforms.contains(className)) {
                            if (transformNames.containsKey(className)) {
                                if (!service.equals(transformNames.get(className))) {
                                    compilationUnit.getErrorCollector().addWarning(
                                        WarningMessage.POSSIBLE_ERRORS,
                                        "The global transform for class " + className + " is defined in both "
                                            + transformNames.get(className).toExternalForm()
                                            + " and "
                                            + service.toExternalForm()
                                            + " - the former definition will be used and the latter ignored.",
                                        null,
                                        null);
                                }
                                
                            } else {
                        	// GRECLIPSE: start
                        	/*old{
                        		transformNames.put(className, service);
                        	}new*/
                        	    if (compilationUnit.allowTransforms || globalTransformsAllowedInReconcile.contains(className)) {
                        		    
                        		    transformNames.put(className, service);
                        	    }
                        	// GRECLIPSE: end
                            }
                        }
                    }
                    try {
                        className = svcIn.readLine();
                    } catch (IOException ioe) {
                        compilationUnit.getErrorCollector().addError(new SimpleMessage(
                            "IOException reading the service definition at "
                            + service.toExternalForm() + " because of exception " + ioe.toString(), null));
                        //noinspection UnnecessaryContinue
                        continue;
                    }
                }
                } finally {
                  if (svcIn!=null) 
                  svcIn.close();
                }
                // GRECLIPSE: start
                is.close();
                // end
            }
        } catch (IOException e) {
            //FIXME the warning message will NPE with what I have :(
            compilationUnit.getErrorCollector().addError(new SimpleMessage(
                "IO Exception attempting to load global transforms:" + e.getMessage(),
                null));
        }
        try {
            Class.forName("java.lang.annotation.Annotation"); // test for 1.5 JVM
        } catch (Exception e) {
            // we failed, notify the user
            StringBuffer sb = new StringBuffer();
            sb.append("Global ASTTransformations are not enabled in retro builds of groovy.\n");
            sb.append("The following transformations will be ignored:");
            for (Map.Entry<String, URL> entry : transformNames.entrySet()) {
                sb.append('\t');
                sb.append(entry.getKey());
                sb.append('\n');
            }
            compilationUnit.getErrorCollector().addWarning(new WarningMessage(
                WarningMessage.POSSIBLE_ERRORS, sb.toString(), null, null));
            return;
        }
        
        // record the transforms found in the first scan, so that in the 2nd scan, phase operations 
        // can be added for only for new transforms that have come in 
        if(isFirstScan) {
            for (Map.Entry<String, URL> entry : transformNames.entrySet()) {
                context.getGlobalTransformNames().add(entry.getKey());
            }
            addPhaseOperationsForGlobalTransforms(context.getCompilationUnit(), transformNames, isFirstScan);
        } else {
            Iterator<Map.Entry<String, URL>> it = transformNames.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String, URL> entry = it.next();
                if(!context.getGlobalTransformNames().add(entry.getKey())) {
                    // phase operations for this transform class have already been added before, so remove from current scan cycle
                    it.remove(); 
                }
            }
            addPhaseOperationsForGlobalTransforms(context.getCompilationUnit(), transformNames, isFirstScan);
        }
    }
    
    // GRECLIPSE: start
    private static List<String> globalTransformsAllowedInReconcile = null;
    
    private static void ensurelobalTransformsAllowedInReconcileInitialized() {
    	if (globalTransformsAllowedInReconcile==null) {    		
	    	globalTransformsAllowedInReconcile=new ArrayList<String>();
	    	try {
	    		String s = System.getProperty("greclipse.globalTransformsInReconcile","");
	    		StringTokenizer st = new StringTokenizer(s,",");
	    		while (st.hasMoreElements()) {
	    			String classname = st.nextToken();
	    			globalTransformsAllowedInReconcile.add(classname);
	    		}
	    	} catch (Exception e) {
	    		
	    		// presumed security exception
	    	}
	    	globalTransformsAllowedInReconcile.add("groovy.grape.GrabAnnotationTransformation");
    	}
    }
    // GRECLIPSE: end
    
    private static void addPhaseOperationsForGlobalTransforms(CompilationUnit compilationUnit, 
            Map<String, URL> transformNames, boolean isFirstScan) {
        GroovyClassLoader transformLoader = compilationUnit.getTransformLoader();
        for (Map.Entry<String, URL> entry : transformNames.entrySet()) {
            try { //Greclipse?
                Class gTransClass = transformLoader.loadClass(entry.getKey(), false, true, false);
                //no inspection unchecked
                GroovyASTTransformation transformAnnotation = (GroovyASTTransformation) gTransClass.getAnnotation(GroovyASTTransformation.class);
                if (transformAnnotation == null) {
                    compilationUnit.getErrorCollector().addWarning(new WarningMessage(
                        WarningMessage.POSSIBLE_ERRORS,
                        "Transform Class " + entry.getKey() + " is specified as a global transform in " + entry.getValue().toExternalForm()
                        + " but it is not annotated by " + GroovyASTTransformation.class.getName()
                        + " the global transform associated with it may fail and cause the compilation to fail.", 
                        null,
                        null));
                    continue;
                }
                if (ASTTransformation.class.isAssignableFrom(gTransClass)) {
                	try {
                	final ASTTransformation instance = (ASTTransformation)gTransClass.newInstance();
                	if (instance instanceof CompilationUnitAware) {
                        ((CompilationUnitAware)instance).setCompilationUnit(compilationUnit);
                    }
                    CompilationUnit.SourceUnitOperation suOp = new CompilationUnit.SourceUnitOperation() {
                		// GRECLIPSE: start
                    	private boolean isBuggered = false;
                		// end
                        public void call(SourceUnit source) throws CompilationFailedException {
                    		// GRECLIPSE: start
                        	if (isBuggered) return;
                        	try { 
                            long stime = System.nanoTime();
                            boolean okToSet = source!=null && source.getErrorCollector()!=null;

                    		try {
                    			if (okToSet) {
                    				source.getErrorCollector().transformActive=true; 
                    			}
                            // end
                                instance.visit(new ASTNode[] {source.getAST()}, source);
                    		// GRECLIPSE: start
                    		} finally {
                    			if (okToSet) {
                    				source.getErrorCollector().transformActive=false;
                    			}
                    		}
                            long etime = System.nanoTime(); 
                    		if (GroovyLogManager.manager.hasLoggers()) {
                    			long timetaken = (etime-stime)/1000000;
                    			if (timetaken>0) {
	                    			try {
	                    				GroovyLogManager.manager.log(TraceCategory.AST_TRANSFORM,"Global transform "+instance.getClass().getName()+" on "+source.getName()+" = "+timetaken+"ms");
	                    			} catch (Throwable t) {
	                    				t.printStackTrace();
	                    			}
                    			}
                    		}
                        	} catch (NoClassDefFoundError ncdfe) {
                        		// Suggests that the transform is written in Java but has dependencies on Groovy source
                        		// within the same project - as this has yet to be compiled, we can't find the class.
                        		new RuntimeException("Transform "+instance.toString()+" cannot be run",ncdfe).printStackTrace();
                        		source.addException(new GroovyException(
                        				"Transform "+instance.toString()+" cannot be run",ncdfe)); 
                        		// disable this visitor because it is BUGGERED
                        		isBuggered = true;
                        	}
                        	// end
                        }
                    }; 
                    if(isFirstScan) {
                        compilationUnit.addPhaseOperation(suOp, transformAnnotation.phase().getPhaseNumber());
                    } else {
                        compilationUnit.addNewPhaseOperation(suOp, transformAnnotation.phase().getPhaseNumber());
                    }
                    //GRECLIPSE? start
                	} catch (Throwable t) {
                		// unexpected problem with the transformation. Could be:
                		// - problem instantiating the transformation class
                		compilationUnit.getErrorCollector().addError(new SimpleMessage(
                				"Unexpected problem with AST transform: "+t.getMessage(),null));
                		t.printStackTrace(); // temporary...
                	}
                    //GRECLIPSE? end
                } else {
                    compilationUnit.getErrorCollector().addError(new SimpleMessage(
                        "Transform Class " + entry.getKey() + " specified at "
                        + entry.getValue().toExternalForm() + " is not an ASTTransformation.", null));
                }
            } catch (Exception e) {
                compilationUnit.getErrorCollector().addError(new SimpleMessage(
                    "Could not instantiate global transform class " + entry.getKey() + " specified at "
                    + entry.getValue().toExternalForm() + "  because of exception " + e.toString(), null));
            }
        }
    }
}

