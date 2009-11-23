package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Debug listener used by some classes that are internal to the compiler but for which we'd like to externalize events that occur so
 * we can test for them.
 * 
 * @author Andy Clement
 */
public class EventListener {

	private List<String> events = new ArrayList<String>();
	private Set<String> interestingEvents = new HashSet<String>();

	public EventListener() {

	}

	public EventListener(String key) {
		interestingEvents.add(key);
	}

	public EventListener(String... keys) {
		if (keys != null) {
			for (String k : keys) {
				interestingEvents.add(k);
			}
		}
	}

	public void record(String event) {
		String key = event.substring(0, event.indexOf(':')).toLowerCase();
		if (interestingEvents.size() > 0 && !interestingEvents.contains(key)) {
			// not interesting
			return;
		}
		events.add(event);
	}

	public void clear() {
		events.clear();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String event : events) {
			sb.append(event).append("\n");
		}
		return sb.toString();
	}

	public int eventCount() {
		return events.size();
	}

	public List<String> getEvents() {
		return Collections.unmodifiableList(events);
	}
}
