package org.codehaus.groovy.frameworkadapter.util;

import org.eclipse.e4.core.di.annotations.Execute;

public class ModelProcessor {
    @Execute
    public void doit() {
        System.out.println("---YAY!---");
    }

}
