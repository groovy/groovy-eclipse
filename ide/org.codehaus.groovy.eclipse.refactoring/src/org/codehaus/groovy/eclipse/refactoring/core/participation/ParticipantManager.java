/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.participation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.ExtractMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal.RenameLocalProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod.RenameMethodProvider;

/**
 * A <code>ParticipantManager</code> holds a set of <code>RefactoringParticipants</code>.
 * Each of those may register to one ore more implemented Groovy Refactorings.
 * 
 * @author Stefan Reinhard
 */
public class ParticipantManager {
	
	private HashMap<Class<? extends GroovyRefactoringParticipant>, Integer> participantMap = 
		new HashMap<Class<? extends GroovyRefactoringParticipant>, Integer>();

	/** No Refactoring selected */
	public static final int UNKNOWN = 0;
	
	public static final int RENAME_CLASS = 1 << 1;
	
	public static final int RENAME_FIELD = 1 << 2;
	
	public static final int RENAME_METHOD = 1 << 3;
	
	public static final int RENAME_LOCAL = 1 << 4;
	
	public static final int EXTRACT_METHOD = 1 << 5;
	
	public static final int INLINE_METHOD = 1 << 6;
	
	private static ParticipantManager defaulManager = new ParticipantManager();
	
	/**
	 * Returns the refactoring constant of the given provider
	 * @param provider
	 */
	public static int getType(RefactoringProvider provider) {
		if (provider instanceof RenameClassProvider) {
			return RENAME_CLASS;
		} else if (provider instanceof RenameFieldProvider) {
			return RENAME_FIELD;
		} else if (provider instanceof RenameMethodProvider) {
			return RENAME_METHOD;
		} else if (provider instanceof RenameLocalProvider) {
			return RENAME_LOCAL;
		} else if (provider instanceof ExtractMethodProvider) {
			return EXTRACT_METHOD;
		} else if (provider instanceof InlineMethodProvider) {
			return INLINE_METHOD;
		} else {
			return UNKNOWN;
		}
	}
	
	/**
	 * Returns the default participant manager of the plug-in
	 */
	public static ParticipantManager getDefaultManager() {
		return defaulManager;
	}
	
	/**
	 * Registers a new participant for all flagged Groovy Refactorings
	 * @param participant The Class of the Participant
	 * @param refactoringFlag Which Refactoring should this participant listen to
	 */
	public void add(Class<? extends GroovyRefactoringParticipant> participant, int refactoringFlag) {
		participantMap.put(participant, refactoringFlag);
	}
	
	/**
	 * Removes a participant from all registered events
	 */
	public void remove(Class<? extends GroovyRefactoringParticipant> participant) {
		participantMap.remove(participant);
	}
	
	/**
	 * Gets all participants registered for the passed flags
	 * @param refactoringFlag
	 */
	public List<Class<? extends GroovyRefactoringParticipant>> getActiveParticipants(int refactoringFlag) {
		LinkedList<Class<? extends GroovyRefactoringParticipant>> result = 
			new LinkedList<Class<? extends GroovyRefactoringParticipant>>();
		for (Class<? extends GroovyRefactoringParticipant> participant : participantMap.keySet()) {	
			if ((participantMap.get(participant) & refactoringFlag) == refactoringFlag) {
				result.add(participant);
			}
		}
		return result;
	}

}
