package org.codehaus.groovy.eclipse.test.ui;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.eclipse.editor.highlighting.IHighlightingExtender;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.rules.IRule;

public class Extender1 implements IHighlightingExtender, IProjectNature {

    public static final String NATURE1 = "org.codehaus.groovy.eclipse.tests.testNature1";
    
    public static final String GJDK_KEYWORD = "extender1GJDKkeyword";
    public static final String GROOVY_KEYWORD = "extender1Groovykeyword";

    public List<String> getAdditionalGJDKKeywords() {
        return Arrays.asList(GJDK_KEYWORD);
    }

    public List<String> getAdditionalGroovyKeywords() {
        return Arrays.asList(GROOVY_KEYWORD);
    }

    public List<IRule> getAdditionalRules() {
        return null;
    }

    public void configure() throws CoreException {
    }

    public void deconfigure() throws CoreException {
    }

    IProject p;
    public IProject getProject() {
        return p;
    }

    public void setProject(IProject project) {
        this.p = project;
    }

}
