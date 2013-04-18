/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.frameworkadapter.util;

import org.eclipse.e4.core.di.annotations.Execute;
import org.osgi.framework.BundleException;

/**
 * This class exists only to ensure that this bundle is loaded before the
 * e4 model is loaded. Only applicable on e4.  Class will not be loaded in e3.
 * 
 * @author Andrew Eisenberg
 * @created Apr 17, 2013
 */
public class ModelProcessor {
    @Execute
    public void doit() {
//        try {
//            ResolverActivator.getDefault().getChooser().initialize(ResolverActivator.getContext());
//        } catch (BundleException e) {
//            e.printStackTrace();
//        }
    }

}
