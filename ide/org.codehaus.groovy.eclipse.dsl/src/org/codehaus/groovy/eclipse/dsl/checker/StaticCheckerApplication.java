package org.codehaus.groovy.eclipse.dsl.checker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.Workbench;

public class StaticCheckerApplication implements IApplication {
    
    class CheckerJob extends Job {

        public CheckerJob() {
            super("Checker Job");
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            // now ensure that the classpath containers and variables are initialized
            try {
                JavaCore.initializeAfterLoad(new NullProgressMonitor());
            } catch (CoreException e) {
                e.printStackTrace();
            }

            
            // create the project if required
            try {
                createProject();
            } catch (CoreException e) {
                System.err.println("Failed to create project " + projectName + " at location " + projectFolderPath);
                e.printStackTrace();
                return e.getStatus();
            }
            
            // ensure project is open
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            try {
                project.open(null);
            } catch (CoreException e) {
                System.err.println("Failed to open project " + projectName);
                e.printStackTrace();
                return e.getStatus();
            }
            
            // Add the extra dslds to the workspace inside of the target project
            addExtraDslds();
            
            // Ensure that dslds are all available
            GroovyDSLCoreActivator.getDefault().getContextStoreManager().initialize(project, true);
            
            System.out.println("Performing static type checking on project " + projectName);
            boolean success = false;
            try {
                IStaticCheckerHandler handler = new SysoutStaticCheckerHandler(resultFile == null ? System.out : createOutStream(resultFile));
                ResourceTypeChecker checker = new ResourceTypeChecker(handler, projectName, inclusionFilters, exclusionFilters, assertionsOnly);
                success = checker.doCheck(null);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                removeExtraDslds();
            }
            display.asyncExec(new Runnable() {
                public void run() {
                    Workbench.getInstance().close();
                }
            });
            
            // FIXADE Is this OK to do?
            System.exit(success ? 0 : -1);
            
            // won't get here
            return Status.OK_STATUS;
        }
        
    }
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
            CheckerJob checkerJob = new CheckerJob();
            checkerJob.schedule();
         }
        
        @Override
        public void postShutdown() {
            super.postShutdown();
        }
    }

    private String projectName;
    private char[][] inclusionFilters;
    private char[][] exclusionFilters;
    private boolean assertionsOnly;
    private String[] extraDslds;
    private IFile[] extraDsldFiles;
    private String projectFolderPath;
    Display display;
    private String resultFile;
    
    public Object start(IApplicationContext context) throws Exception {
        processCommandLine((String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS));
        try {
            display = createDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        WorkbenchAdvisor advisor = new CheckerWorkbenchAdvisor();
        return PlatformUI.createAndRunWorkbench(display, advisor);
    }

    public PrintStream createOutStream(String fileName) throws FileNotFoundException {
        return new PrintStream(new File(fileName));
    }

    public void stop() {
        removeExtraDslds();
    }

    private void addExtraDslds() {
        if (extraDslds != null) {
            extraDsldFiles = new IFile[extraDslds.length];
            for (int i = 0; i < extraDslds.length; i++) {
                File file = new File(extraDslds[i]);
                if (file.exists()) {
                    IFile linkedFile = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(file.getName());
                    if (linkedFile.exists() && linkedFile.isLinked()) {
                        try {
                            linkedFile.delete(true, null);
                        } catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!linkedFile.exists()) {
                        try {
                            System.out.println("Adding " + file.toURI());
                            linkedFile.createLink(file.toURI(), IResource.NONE, null);
                            extraDsldFiles[i] = linkedFile;
                        } catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("Warning: DSLD file " + extraDslds[i] + " doesn't exist.  Ignoring.");
                }
            }
        }
    }
    
    
    private void createProject() throws CoreException {
        if (projectFolderPath == null) {
            // nothing to do
            return;
        }
        
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (project.exists()) {
            if (project.getLocation().toOSString().equals(projectFolderPath)) {
                // project already exists do nothing
                return;
            } else {
                // delete existing project from workspace, but not the filesystem 
                project.delete(IResource.NEVER_DELETE_PROJECT_CONTENT | IResource.FORCE, null);
            }
        }
        
        IPath dotProjectPath = new Path(projectFolderPath).append(".project");
        IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(dotProjectPath);
        description.setName(projectName);
        project.create(description, null);
    }

    private void processCommandLine(String[] args) {
        boolean doHelp = false;
        String excludes = null;
        String includes = null;
        if (args.length < 1) {
            printUsage(true);
            Workbench.getInstance().close();
            return;
        }
        projectName = args[args.length - 1];

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-h") || arg.equals("--help")) {
                doHelp = true;
                break;
            } else if (arg.equals("--assertions_only")) {
                assertionsOnly = true;
            } else if (arg.equals("--excludes")) {
                if (i == args.length-1) {
                    System.err.println("Missing --excludes argument");
                    doHelp = true;
                    break;
                }
                excludes = args[++i];
            } else if (arg.equals("--includes")) {
                if (i == args.length-1) {
                    System.err.println("Missing --includes argument");
                    doHelp = true;
                    break;
                }
                includes = args[++i];
            } else if (arg.equals("--extra_dslds")) {
                if (i == args.length-1) {
                    System.err.println("Missing --extraDslds argument");
                    doHelp = true;
                    break;
                }
                extraDslds = args[++i].split("\\|");
            } else if (arg.equals("--project_path")) {
                if (i == args.length-1) {
                    System.err.println("Missing --project_path argument");
                    doHelp = true;
                    break;
                }
                projectFolderPath = args[++i];
            } else if (arg.equals("--result_file")) {
                if (i == args.length-1) {
                    System.err.println("Missing --result_file argument");
                    doHelp = true;
                    break;
                }
                resultFile = args[++i];
            }
        }
        
        inclusionFilters = convertToCharChar(includes);
        exclusionFilters = convertToCharChar(excludes);
        
        if (doHelp) {
            printUsage(false);
            Workbench.getInstance().close();
        }
    }
    
    private char[][] convertToCharChar(String str) {
        if (str == null) {
            return null;
        }
        String[] splits = str.split("\\|");
        char[][] chars = new char[splits.length][];
        for (int i = 0; i < splits.length; i++) {
            chars[i] = ("/" + projectName + "/" + splits[i]).toCharArray();
        }
        return chars;
    }

    private void printUsage(boolean isInvalid) {
        if (isInvalid) {
            System.out.println("Invalid command line.");
        }
        
        System.out.println("Usage:");
        System.out.println("eclipse -application org.codehause.groovy.eclipse.staticCheck [--help] [-h] [--extra_dslds <FILES>] [--assertions_only] [--excludes <PATH>] [--includes <PATH>] [--project_path <PATH>] <PROJECT_NAME>");
        System.out.println("where:");
        System.out.println("\t--help OR -h  Print this message and exit.");
        System.out.println("\t--extra_dslds  list of extra dsld files to be included in this check.  Use '|' as a file separator.");
        System.out.println("\t--assertions_only  Don't report unknown types.  Only look for type assertions");
        System.out.println("\t--excludes  Project-relative exclusion filters.");
        System.out.println("\t--includes  Project-relative inclusion filters.");
        System.out.println("\t--project_path  File system path to the project to check (only required if project is not already in workspace).");
        System.out.println("\t--result_file  File to send static checking results to.  If not specified, then results sent to sysout.");
        System.out.println("\t<PROJECT_NAME>  Name of a project to type check.  If not already in workspace, then must also use '--project_path'.");
        System.out.println();
        System.out.println("Ant style filters are allowed.  Eg, src/org/codehaus/groovy/**/*.groovy means all files with groovy extensions in the org.codehaus.groovy package or below will be ex/included   Filters can be concentenated using '|'.");
    }

    private void removeExtraDslds() {
        if (extraDsldFiles != null) {
            for (IFile file : extraDsldFiles) {
                if (file != null && file.exists()) {
                    try {
                        System.out.println("\nRemoving " + file.getLocation().toFile().toURI());
                        file.delete(true, null);
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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
