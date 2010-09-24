package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.fix.TextEditFix;
import org.eclipse.jdt.internal.ui.fix.AbstractCleanUp;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Andrew Eisenberg
 * @created Aug 18, 2009
 *
 */
public class GroovyCodeFormatCleanUp extends AbstractCleanUp {

    private final FormatKind kind;
    public GroovyCodeFormatCleanUp(FormatKind kind) {
        this.kind = kind;
    }

    @Override
    public RefactoringStatus checkPreConditions(IJavaProject project,
            ICompilationUnit[] compilationUnits, IProgressMonitor monitor)
            throws CoreException {

        RefactoringStatus status = new RefactoringStatus();
        try {
            for (ICompilationUnit unit : compilationUnits) {
                if (! (unit instanceof GroovyCompilationUnit)) {
                    status.addError("Cannot use groovy formating on a non-groovy compilation unit: " + unit.getElementName());
                } else if (((GroovyCompilationUnit) unit).getModuleNode() == null) {
                    status.addError("Cannot find module node for compilation unit: " + unit.getElementName());
                }
            }

        } catch (Exception e) {
            GroovyCore.logException("Cannot perform code format.", e);
            status.addFatalError("Cannot perform code format. See error log. " + e.getMessage());
        }
        return new RefactoringStatus();
    }

    @Override
    public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
        ICompilationUnit unit = context.getCompilationUnit();
        if (unit instanceof GroovyCompilationUnit) {
            GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;

            char[] contents = gunit.getContents();
            Document doc = new Document(new String(contents));
            DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(new TextSelection(0, contents.length), doc,
                    new FormatterPreferences(gunit), kind == FormatKind.INDENT_ONLY);
            TextEdit edit = formatter.format();
            return new TextEditFix(edit, gunit, "Formatting groovy file");
        }
        return null;
    }

    @Override
    public CleanUpRequirements getRequirements() {
        return new CleanUpRequirements(false, false, false, null);
    }

    @Override
    public String[] getStepDescriptions() {
        return new String[] { "Format groovy source code." };
    }
    @Override
    public void setOptions(CleanUpOptions options) { }

}