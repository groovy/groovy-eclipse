/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.ui.decorators;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerLabelProvider;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class GroovyImageProvider extends JavaElementImageProvider {

    private IWindowListener listener = new IWindowListener() {
        @Override
        public void windowOpened(IWorkbenchWindow window) {
            window.getActivePage().addPartListener(new IPartListener2() {
                @Override
                public void partOpened(IWorkbenchPartReference partRef) {
                    IWorkbenchPart part = partRef.getPart(false);
                    if (part instanceof PackageExplorerPart) {
                        enhance((PackageExplorerPart) part);
                    }
                }

                @Override
                public void partClosed(IWorkbenchPartReference partRef) {
                    IWorkbenchPart part = partRef.getPart(false);
                    if (part instanceof PackageExplorerPart) {
                        // TODO?
                    }
                }

                @Override
                public void partActivated(IWorkbenchPartReference partRef) {}

                @Override
                public void partDeactivated(IWorkbenchPartReference partRef) {}

                @Override
                public void partHidden(IWorkbenchPartReference partRef) {}

                @Override
                public void partVisible(IWorkbenchPartReference partRef) {}

                @Override
                public void partBroughtToTop(IWorkbenchPartReference partRef) {}

                @Override
                public void partInputChanged(IWorkbenchPartReference partRef) {}
            });
        }

        @Override
        public void windowClosed(IWorkbenchWindow window) {}

        @Override
        public void windowActivated(IWorkbenchWindow window) {}

        @Override
        public void windowDeactivated(IWorkbenchWindow window) {}
    };

    //--------------------------------------------------------------------------

    public GroovyImageProvider() {
        PlatformUI.getWorkbench().addWindowListener(listener);

        listener.windowOpened(PlatformUI.getWorkbench().getActiveWorkbenchWindow());

        PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
        if (packageExplorer != null) {
            enhance(packageExplorer);
        }
    }

    public void disconnect() {
        PlatformUI.getWorkbench().removeWindowListener(listener);
    }

    protected void enhance(PackageExplorerPart packageExplorer) {
        PackageExplorerLabelProvider fLabelProvider = ReflectionUtils.getPrivateField(PackageExplorerPart.class, "fLabelProvider", packageExplorer);
        if (fLabelProvider != null) {
            JavaElementImageProvider fImageLabelProvider = ReflectionUtils.getPrivateField(JavaUILabelProvider.class, "fImageLabelProvider", fLabelProvider);
            if (fImageLabelProvider != null && fImageLabelProvider != this) {
                fImageLabelProvider.dispose();
                ReflectionUtils.setPrivateField(JavaUILabelProvider.class, "fImageLabelProvider", fLabelProvider, this);
            }
        }
    }

    @Override
    public ImageDescriptor getCUResourceImageDescriptor(IFile file, int flags) {
        if (ContentTypeUtils.isGroovyLikeFileName(file.getName())) {
            return GroovyPluginImages.DESC_GROOVY_FILE;
        }
        return super.getCUResourceImageDescriptor(file, flags);
    }

    @Override
    public ImageDescriptor getJavaImageDescriptor(IJavaElement element, int flags) {
        if (element instanceof GroovyCompilationUnit) {
            return GroovyPluginImages.DESC_GROOVY_FILE;
        }
        return super.getJavaImageDescriptor(element, flags);
    }
}
