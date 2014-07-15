/*******************************************************************************
 * Copyright (c) 2009-2014 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.core.tests.basic;

import java.io.File;
import java.util.Iterator;

import junit.framework.Test;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.EventListener;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;


public class SingleTest extends AbstractGroovyRegressionTest {
	
    public void testGreclipse719_2() {
    	if (GroovyUtils.GROOVY_LEVEL < 18) {
    		return;
    	}
		this.runNegativeTest(new String[] {
			"MyDomainClass.groovy",
			"int anInt = 10;\n"+
			"def Method[][] methodMethodArray = anInt.class.methods;\n"+
			"println methodArray.name;"},
			"----------\n" + 
			"1. ERROR in MyDomainClass.groovy (at line 2)\n" + 
			"	def Method[][] methodMethodArray = anInt.class.methods;\n" + 
			"	    ^^^^^^^^^^\n" + 
			"Groovy:unable to resolve class Method[][] \n" + 
			"----------\n");
	}	


	public SingleTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(),ClassFileConstants.JDK1_5);
//		return buildUniqueComplianceTestSuite(testClass(),F_1_5);
//		return buildAllCompliancesTestSuite(testClass());
//		return buildMinimalComplianceTestSuite(testClass(),F_1_5);
	} 
	
	
	protected void setUp() throws Exception {
		super.setUp();
		complianceLevel = ClassFileConstants.JDK1_5;
	}

	public static Class testClass() {
		return SingleTest.class;
	}
	
    /**
     * Check the disassembly of a .class file for a particular piece of text
     */
	private void checkDisassemblyFor(String filename, String expectedOutput, int detail) {
		try {
			File f = new File(OUTPUT_DIR + File.separator + filename);
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(classFileBytes, "\n", detail);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	private void checkGCUDeclaration(String filename, String expectedOutput) {
		GroovyCompilationUnitDeclaration decl = (GroovyCompilationUnitDeclaration)((DebugRequestor)GroovyParser.debugRequestor).declarations.get(filename);
		String declarationContents = decl.print();
		if (expectedOutput==null || expectedOutput.length()==0) {
			System.out.println(Util.displayString(declarationContents,2));
		} else {
			int foundIndex = declarationContents.indexOf(expectedOutput);
			if (foundIndex==-1) {
				fail("Did not find expected output:\n"+expectedOutput+"\nin actual output:\n"+declarationContents);
			}
		}
	}
	
	/* for import recovery tests, if they get activated */
	private ModuleNode getModuleNode(String filename) {
		GroovyCompilationUnitDeclaration decl = (GroovyCompilationUnitDeclaration)((DebugRequestor)GroovyParser.debugRequestor).declarations.get(filename);
		if (decl!=null) {
			return decl.getModuleNode();
		} else {
			return null;
		}
	}
	
	private GroovyCompilationUnitDeclaration getCUDeclFor(String filename) {
		return (GroovyCompilationUnitDeclaration)((DebugRequestor)GroovyParser.debugRequestor).declarations.get(filename);
	}

	private String stringify(TypeReference type) {
		StringBuffer sb = new StringBuffer();
		stringify(type,sb);
		return sb.toString();		
	}
	private void stringify(TypeReference type, StringBuffer sb) {		
		if (type.getClass()==ParameterizedSingleTypeReference.class) {
			ParameterizedSingleTypeReference pstr = (ParameterizedSingleTypeReference)type;
			sb.append("("+pstr.sourceStart+">"+pstr.sourceEnd+")").append(pstr.token);
			TypeReference[] typeArgs = pstr.typeArguments;
			sb.append("<");
			for (int t=0;t<typeArgs.length;t++) {
				stringify(typeArgs[t],sb);
			}
			sb.append(">");
		} else if (type.getClass()==ParameterizedQualifiedTypeReference.class) {
			ParameterizedQualifiedTypeReference pqtr = (ParameterizedQualifiedTypeReference)type;
			sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
			long[] positions = pqtr.sourcePositions;
			TypeReference[][] allTypeArgs = pqtr.typeArguments;
			for (int i=0;i<pqtr.tokens.length;i++) {
				if (i>0) {
					sb.append('.');
				}
				sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(pqtr.tokens[i]);
				if (allTypeArgs[i]!=null) {
					sb.append("<");
					for (int t=0;t<allTypeArgs[i].length;t++) {
						stringify(allTypeArgs[i][t],sb);
					}
					sb.append(">");
				}
			}
			
		} else if (type.getClass()==ArrayTypeReference.class) {
			ArrayTypeReference atr = (ArrayTypeReference)type;
			// for a reference 'String[]' sourceStart='S' sourceEnd=']' originalSourceEnd='g'
			sb.append("("+atr.sourceStart+">"+atr.sourceEnd+" ose:"+atr.originalSourceEnd+")").append(atr.token);
			for (int d=0;d<atr.dimensions;d++) {
				sb.append("[]");
			}			
		} else if (type.getClass()==Wildcard.class) {
			Wildcard w = (Wildcard)type;
			if (w.kind== Wildcard.UNBOUND) {
				sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append('?');
			} else if (w.kind==Wildcard.SUPER) {
				sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append("? super ");
				stringify(w.bound,sb);
			} else if (w.kind==Wildcard.EXTENDS) {
				sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append("? extends ");
				stringify(w.bound,sb);
			}
		} else if (type.getClass()== SingleTypeReference.class) {
			sb.append("("+type.sourceStart+">"+type.sourceEnd+")").append(((SingleTypeReference)type).token);
		} else if (type instanceof ArrayQualifiedTypeReference) {
			ArrayQualifiedTypeReference aqtr = (ArrayQualifiedTypeReference)type;
			sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
			long[] positions = aqtr.sourcePositions;
			for (int i=0;i<aqtr.tokens.length;i++) {
				if (i>0) {
					sb.append('.');
				}
				sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(aqtr.tokens[i]);
			}
			for (int i=0;i<aqtr.dimensions();i++) { sb.append("[]"); }
		} else if (type.getClass()== QualifiedTypeReference.class) {
			QualifiedTypeReference qtr = (QualifiedTypeReference)type;
			sb.append("("+type.sourceStart+">"+type.sourceEnd+")");
			long[] positions = qtr.sourcePositions;
			for (int i=0;i<qtr.tokens.length;i++) {
				if (i>0) {
					sb.append('.');
				}
				sb.append("("+(int)(positions[i]>>>32)+">"+(int)(positions[i]&0x00000000FFFFFFFFL)+")").append(qtr.tokens[i]);
			}
		} else {
			throw new RuntimeException("Dont know how to print "+type.getClass());
		}
	}


	private FieldDeclaration grabField(GroovyCompilationUnitDeclaration decl, String fieldname) {
		FieldDeclaration[] fDecls = decl.types[0].fields;
		for (int i=0;i<fDecls.length;i++) { 
			if (new String(fDecls[i].name).equals(fieldname)) { 
				return fDecls[i];
			}
		}
		return null;
	}

	private void assertEventCount(int expectedCount, EventListener listener) {
		if (listener.eventCount()!=expectedCount) {
			fail("Expected "+expectedCount+" events but found "+listener.eventCount()+"\nEvents:\n"+listener.toString());
		}
	}

	private void assertEvent(String eventText, EventListener listener) {
		boolean found = false;
		Iterator eventIter = listener.getEvents().iterator();
		while (eventIter.hasNext()) {
			String s = (String) eventIter.next();
			if (s.equals(eventText)) {
				found=true;
				break;
			}
		}
		if (!found) {
			fail("Expected event '"+eventText+"'\nEvents:\n"+listener.toString());
		}
	}

}