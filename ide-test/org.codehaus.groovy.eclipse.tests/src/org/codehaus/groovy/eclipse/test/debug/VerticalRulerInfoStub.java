package org.codehaus.groovy.eclipse.test.debug;

import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Control;

/**
 * 
 */
public class VerticalRulerInfoStub implements IVerticalRulerInfo {
	
	private int fLineNumber = -1;
	
	public VerticalRulerInfoStub(int line) {
		fLineNumber = line;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getControl()
	 */
	public Control getControl() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity() {
		return fLineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getWidth()
	 */
	public int getWidth() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate) {
		return 0;
	}

}
