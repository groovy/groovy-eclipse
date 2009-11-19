package core;

import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.CandidateCollector;

public class CandidateCollectorMock extends CandidateCollector {

	private final IGroovyFileProvider fileProvider;
	
	public CandidateCollectorMock(IGroovyDocumentProvider docProvider, UserSelection selection, IGroovyFileProvider fileProvider) {
		super(docProvider, selection);
		this.fileProvider = fileProvider;
	}

    protected IGroovyFileProvider getWSFileProvider() {
		return fileProvider;
	}
}
