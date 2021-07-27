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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;

public class Activator implements BundleActivator {

    private ServiceRegistration<ResolverHookFactory> service;

    @Override
    public void start(final BundleContext context) {
        service = context.registerService(ResolverHookFactory.class, triggers -> new Resolver(), null);
    }

    @Override
    public void stop(final BundleContext context) {
        service.unregister();
    }
}
