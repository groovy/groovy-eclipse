package org.codehaus.groovy.eclipse.dsl.checker;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.Workbench;

public class StaticCheckerApplication implements IApplication {
    /**
     * 
     * @author andrew
     * @created Aug 31, 2011
     */
    public class CheckerWorkbenchAdvisor extends WorkbenchAdvisor {

        @Override
        public String getInitialWindowPerspectiveId() {
            return null;
        }
        
        
        @Override
        public void postStartup() {
            IStaticCheckerHandler handler = new SysoutStaticCheckerHandler();
            ResourceTypeChecker checker = new ResourceTypeChecker(handler, projectName);
            try {
                checker.doCheck(null);
            } catch (CoreException e) {
                e.printStackTrace();
            }
            Workbench.getInstance().close();
        }
    }

    private String projectName;

    public Object start(IApplicationContext context) throws Exception {
        processCommandLine((String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS));
        
        Display display = createDisplay();
        WorkbenchAdvisor advisor = new CheckerWorkbenchAdvisor();
        return PlatformUI.createAndRunWorkbench(display, advisor);
    }

    private void processCommandLine(String[] args) {
        projectName = args[args.length - 1];
    }
    
    private void printUsage(boolean isInvalid) {
        if (isInvalid) {
            System.out.println("Invalid command line.");
        }
        
        System.out.println("Usage:");
    }

    public void stop() {
        // do nothing
    }
    
    /**
     * Creates the display used by the application.
     * 
     * @return the display used by the application
     */
    protected Display createDisplay() {
        return PlatformUI.createDisplay();
    }


}
