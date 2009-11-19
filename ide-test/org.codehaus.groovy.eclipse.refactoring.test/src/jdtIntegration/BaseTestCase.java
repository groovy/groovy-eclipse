/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants.AmbiguousSelectionAction;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.FilePartReader;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Stefan Sidler
 *
 */
public abstract class BaseTestCase extends EclipseTestCase{
	protected ArrayList<TestFile> testFiles = new ArrayList<TestFile>();
	protected HashMap<String, String> properties= new HashMap<String, String>();
	protected Map<String, List<Integer[]>> simulateUserAction = new HashMap<String, List<Integer[]>>();
	protected UserSelection selection;
	private String newLine;
	private File file;
	protected String testName;

	

	private final Pattern origRegExp, expRegExp, propertiesRegExp;
	private final String FILE_DELIMITER = "#NEXT";
	private final String FILE_INFO_DELI = ":::";
	private String systemNewLine;
		
	public BaseTestCase(String name, File fileToTest) {
		// Set Method to call for JUnit
		setName("testRefactoring");
		this.testName = name;
		file			= fileToTest;
		systemNewLine	= System.getProperty("line.separator");
		newLine			= systemNewLine; //FilePartReader.getLineDelimiter(file);
		origRegExp		= Pattern.compile("###src" + newLine + "(.*)" + newLine + "###exp",Pattern.DOTALL);
		expRegExp		= Pattern.compile("###exp" + newLine + "(.*)" + newLine + "###end",Pattern.DOTALL);
		propertiesRegExp= Pattern.compile("###prop" + newLine + "(.*)" + newLine + "###src",Pattern.DOTALL);
	}
	
	public void setUp() throws Exception {
		super.setUp();
		readFile();
		mockUIs();
	}
	
	protected void addFilesToProject() throws CoreException, JavaModelException {
		for (TestFile file : testFiles) {

			IPackageFragment pack = testProject.createPackage(file
					.getPackageBefore());
			GroovyRuntime.addGroovyRuntime(testProject.getProject());
			if (file.isJavaFile()) {
				testProject.createJavaType(pack, file.getFilenameBefore(), file
						.getSourceBefore());
			} else if (file.isGroovyFile()) {
				testProject.createGroovyType(pack, file.getFilenameBefore(),
						file.getSourceBefore());
			}
		}
	}

	private void mockUIs() {
		ProgrammaticalRenameRefactoringMOCK.simulateUserInput = simulateUserAction ;
		AmbiguousSelectionAction.setRefactoring(ProgrammaticalRenameRefactoringMOCK.class);
	}
	
	public String getName() {
		return testName;
	}
	
	protected String getSelectedFilePath() {
		String name = properties.get("selectionInFile");
		TestFile file = new TestFile();
		for (TestFile testfile: testFiles) {
			if (testfile.getFilenameBefore().equals(name))
				file = testfile;
		}
		String pack = file.getPackageBefore();
		return "src/" + pack + "/" + name;
	}
	
	protected void compareWithExpected() {		
		checkUIMocks();
		checkSources();
		
	}

	private void checkSources() {
		String actualSource;
		for (TestFile file : testFiles) {
			actualSource = getFileContent(file.getPackageAfter(), file.getFilenameAfter());
			assertEquals(file.getSourceAfter(), actualSource);
		}
	}

	private void checkUIMocks() {
		for (List<Integer[]> list : ProgrammaticalRenameRefactoringMOCK.simulateUserInput.values()) {
			for (Integer[] i : list) {
				String errorMsg = "No ambiguous candidate found";
				assertSame(errorMsg, 1, i[1].intValue());
			}
		}
	}
	
	protected String getFileContent(String pack, String fileName) {
		IFile file = testProject.getProject().getFile("src/"+pack+"/"+fileName);
		return new WorkspaceDocumentProvider(file).getDocumentContent();
	}
	
	protected boolean isJavaFile(String filename) {
		if (filename.endsWith(".java"))
			return true;
		else
			return false;
	}
		
	private void readFile() {
		String[] orig = getFileArea(origRegExp).split(FILE_DELIMITER);
		String[] exp  = getFileArea(expRegExp).split(FILE_DELIMITER);
		assertEquals(orig.length, exp.length);
		
		readProperties();
		
		for (int i =0; i< orig.length; i++) {
			orig[i] = orig[i].trim();
			exp[i]	= exp[i].trim();
			TestFile file = new TestFile();
						
			file.setFileBefore(readFileProperties(orig[i]), 
							   readSource(orig[i]));
			
			file.setFileExpected(readFileProperties(exp[i]),
								 readSource(exp[i]));
			testFiles.add(file);
		}
		
		setUserInput();
	}
	
	private void setUserInput() {
		for (TestFile file : testFiles) {
			simulateUserAction.put(file.getFilenameBefore(), file.getAcceptedLines());
		}
	}

	private Map<String, ArrayList<String>> readFileProperties(String fileContent) {
		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		String[] props = fileContent.split(FILE_INFO_DELI)[0].split(newLine);
		
		for (String line : props) {
			ArrayList<String> list = new ArrayList<String>(0);
			String[] prop = line.split("=");
			assertEquals("Initialisation of testproperties failed! (" + line + ")",2, prop.length);

			prop[0] = prop[0].trim();
			prop[1] = prop[1].trim();
			
			if (map.get(prop[0])!= null) {
				map.get(prop[0]).add(prop[1]);
			}
			else {
				list.add(prop[1]);
				map.put(prop[0], list);
			}
		}
		return map;
	}

	private String getFileArea(Pattern regExpression){
		String filecontent = getContent(file);
		Matcher match = regExpression.matcher(filecontent);
		if(match.find()) {
			return ASTTools.getDocumentWithSystemLineBreak(match.group(1)).get();
		}
        return "";
	}
	
	private String readSource(String fileContent) {
		return fileContent.split(FILE_INFO_DELI)[1].trim();
	}
	
	private void readProperties() {
		Matcher propertiesSection = propertiesRegExp.matcher(getContent(file));	
		if(propertiesSection.find()) {
			String[] reults = propertiesSection.group(1).split(systemNewLine);
			for (String line : reults) {
				String[] prop = line.split("=");
				assertEquals("Initialisation of testproperties failed! (${prop})",2, prop.length);
				
				properties.put(prop[0],prop[1]);
			}
		}
	}
	
	protected String getContent(IFile iFile) {
		
		IPath location = iFile.getLocation();
		File file = location.toFile();
		return getContent(file);
	}
	
	protected String getContent(File file) {
		StringBuilder content = new StringBuilder();
		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			try {
				String line = null; // not declared within while loop

				while ((line = input.readLine()) != null) {
					content.append(line);
					content.append(newLine);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return content.toString();
	}
	
	protected int build() throws Exception {
		int count = 0;
		do {
			fullProjectBuild();
			count++;
		} while (getFailureMarkers().length != 0 && count < 10);
		if (count >= 10) {
			throw new RuntimeException("Build errors in test");
		}
		return count;
	}
	
	public abstract void testRefactoring() throws Exception;

}
