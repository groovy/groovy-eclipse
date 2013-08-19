package org.codehaus.groovy.eclipse.test.ui;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.eclipse.editor.highlighting.IHighlightingExtender2;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.text.CombinedWordRule.CharacterBuffer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

public class Extender2 implements IHighlightingExtender2, IProjectNature {

    public static final String NATURE2 = "org.codehaus.groovy.eclipse.tests.testNature2";

    public static final IRule INITIAL_RULE = new WordRule(new WordDetectorMock("mainword"));
        
    public static final IRule RULE = new WordRule(new WordDetectorMock("secondaryword"));

    public List<String> getAdditionalGJDKKeywords() {
        return null;
    }

    public List<String> getAdditionalGroovyKeywords() {
        return null;
    }

    public List<IRule> getInitialAdditionalRules() {
        return Arrays.asList(INITIAL_RULE);
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
    
    private static final class WordDetectorMock implements IWordDetector {
       
        CharacterBuffer word;
       
        public WordDetectorMock(String word) {
            this.word = new CharacterBuffer(word);
        }
        
        public boolean isWordStart(char c) {
//            return c == word.charAt(0);
          return false;
        }
    
        public boolean isWordPart(char c) {
//            for (int i = 0; i < word.length(); i++) {
//                if (c == word.charAt(i)) {
//                    return true;
//                }
//            }
            return false;
        }
    } 
}
