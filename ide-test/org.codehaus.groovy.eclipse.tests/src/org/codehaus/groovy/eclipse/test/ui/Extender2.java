package org.codehaus.groovy.eclipse.test.ui;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.eclipse.editor.highlighting.IHighlightingExtender;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.WordRule;

public class Extender2 implements IHighlightingExtender, IProjectNature {

    public static final String NATURE2 = "org.codehaus.groovy.eclipse.tests.testNature2";

    public static final IRule RULE = new WordRule(new IWordDetector() {
        
        public boolean isWordStart(char c) {
            return false;
        }
        
        public boolean isWordPart(char c) {
            return false;
        }
    });

    public List<String> getAdditionalGJDKKeywords() {
        return null;
    }

    public List<String> getAdditionalGroovyKeywords() {
        return null;
    }

    public List<IRule> getAdditionalRules() {
        return Arrays.asList(RULE);
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
