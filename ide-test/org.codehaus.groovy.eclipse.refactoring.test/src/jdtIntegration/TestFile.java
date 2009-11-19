/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Stefan Sidler
 *
 */
public class TestFile {
	private final String PACKAGE 	= "Package";
	private final String FILENAME	= "Filename";
	private final String ACCEPTLINE	= "AcceptLine";
	
	
	private Map<String, ArrayList<String>> filePropertiesBefore= new HashMap<String, ArrayList<String>>();
	private Map<String, ArrayList<String>> filePropertiesAfter = new HashMap<String, ArrayList<String>>();
	private String sourceBefore;
	private String sourceAfter;
	private String systemLineBreak = System.getProperty("line.separator");
	
	
	public void setFileBefore(Map<String, ArrayList<String>> properties, String source) {
		this.filePropertiesBefore 	= properties;
		sourceBefore = source;
	}
	
	public boolean isJavaFile() {
		return (getFilenameBefore().endsWith(".java"));
	}
	
	public void setFileExpected(Map<String, ArrayList<String>> properties, String source) {
		this.filePropertiesAfter = properties;
		sourceAfter = source;
	}

	public String getPackageAfter() {
		return filePropertiesAfter.get(PACKAGE).get(0);
	}
	
	public String getPackageBefore() {
		return filePropertiesBefore.get(PACKAGE).get(0);
	}
	
	public String getFilenameAfter() {
		return filePropertiesAfter.get(FILENAME).get(0);
	}
	
	public String getFilenameBefore() {
		return filePropertiesBefore.get(FILENAME).get(0);
	}

	public String getSourceBefore() {
		return sourceBefore;
	}
	
	public List<Integer[]> getAcceptedLines(){
		List<Integer[]> list = new ArrayList<Integer[]>(0);
		if (filePropertiesBefore.get(ACCEPTLINE) != null) {
			for (String line : filePropertiesBefore.get(ACCEPTLINE)) {
				Integer[] i = { Integer.valueOf(line).intValue()+2 , 0 };
				list.add(i);
			}
		}
		return list;
	}

	public String getSourceAfter() {
		StringBuilder source = new StringBuilder();
		source.append("package ");
		source.append(getPackageAfter());
		source.append(";"+systemLineBreak+systemLineBreak);
		source.append(sourceAfter);
		return source.toString();
	}

	public boolean isGroovyFile() {
		return (getFilenameBefore().endsWith(".groovy"));
	}
}
