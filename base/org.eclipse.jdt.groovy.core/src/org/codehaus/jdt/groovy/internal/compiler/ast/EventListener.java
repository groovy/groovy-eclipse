/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Debug listener used by some classes that are internal to the compiler but for
 * which we'd like to externalize events that occur so we can test for them.
 */
public class EventListener {

    private List<String> events = new ArrayList<>();
    private Set<String> interestingEvents = new HashSet<>();

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
        if (!interestingEvents.isEmpty() && !interestingEvents.contains(key)) {
            // not interesting
            return;
        }
        events.add(event);
    }

    public void clear() {
        events.clear();
    }

    @Override
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
