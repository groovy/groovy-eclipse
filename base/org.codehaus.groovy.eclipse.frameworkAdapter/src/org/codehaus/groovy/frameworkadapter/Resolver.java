/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.frameworkadapter;

import java.util.Collection;

import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

public class Resolver implements org.osgi.framework.hooks.resolver.ResolverHook {

    @Override
    public void end() {
    }

    @Override
    public void filterMatches(final BundleRequirement requirement, final Collection<BundleCapability> candidates) {
        if (requirement.getResource().getSymbolicName().equals("ch.qos.logback.classic")) {
            candidates.removeIf(candidate -> candidate.getResource().getSymbolicName().equals("org.codehaus.groovy"));
        }
    }

    @Override
    public void filterResolvable(final Collection<BundleRevision> candidates) {
    }

    @Override
    public void filterSingletonCollisions(final BundleCapability capability, final Collection<BundleCapability> candidates) {
    }
}
