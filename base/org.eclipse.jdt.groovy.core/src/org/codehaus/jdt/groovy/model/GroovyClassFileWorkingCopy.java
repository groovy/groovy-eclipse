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
package org.codehaus.jdt.groovy.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.AbstractClassFile;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.ClassFileWorkingCopy;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager.PerWorkingCopyInfo;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.ModularClassFile;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Disassembler;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Working copy for Groovy class files. Allows access to the ModuleNode if the
 * source is available. Copied from {@link ClassFileWorkingCopy}; changes marked.
 */
public class GroovyClassFileWorkingCopy extends GroovyCompilationUnit {

    public final ClassFile classFile;

    // GROOVY add
    private ModuleNode moduleNode;
    private ModuleNodeInfo moduleNodeInfo;
    private final PerWorkingCopyInfo copyInfo;
    private CompilationUnitElementInfo elementInfo;
    // GROOVY end

    public GroovyClassFileWorkingCopy(ClassFile classFile, WorkingCopyOwner owner) {
        super((PackageFragment) classFile.getParent(), sourceFileName(classFile), owner);
        this.classFile = classFile;
        // GROOVY add
        if (this.owner == null) {
            this.owner = DefaultWorkingCopyOwner.PRIMARY;
        }
        this.copyInfo = new PerWorkingCopyInfo(this, null);
        // GROOVY end
    }

    private static String sourceFileName(AbstractClassFile classFile) {
        if (classFile instanceof ModularClassFile) {
            return TypeConstants.MODULE_INFO_FILE_NAME_STRING;
        } else {
            return ((BinaryType) ((ClassFile) classFile).getType()).getSourceFileName(null/*no info available*/);
        }
    }

    @Override
    public void commitWorkingCopy(boolean force, IProgressMonitor monitor) throws JavaModelException {
        throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this));
    }

    @Override
    public IBuffer getBuffer() throws JavaModelException {
        // GROOVY edit -- always use classFile's buffer
        //if (isWorkingCopy())
        //    return super.getBuffer();
        //else
        // GROOVY end
        return this.classFile.getBuffer();
    }

    @Override
    public char[] getContents() {
        try {
            IBuffer buffer = getBuffer();
            if (buffer == null) return CharOperation.NO_CHAR;
            char[] characters = buffer.getCharacters();
            if (characters == null) return CharOperation.NO_CHAR;
            return characters;
        } catch (JavaModelException e) {
            return CharOperation.NO_CHAR;
        }
    }

    // GROOVY add
    @Override
    public char[] getFileName() {
        return this.name.toCharArray();
    }
    // GROOVY end

    @Override
    public IPath getPath() {
        return this.classFile.getPath();
    }

    @Override
    public JavaElement getPrimaryElement(boolean checkOwner) {
        if (checkOwner && isPrimary()) return this;
        return new ClassFileWorkingCopy(this.classFile, DefaultWorkingCopyOwner.PRIMARY);
    }

    @Override
    public IResource resource(PackageFragmentRoot root) {
        if (root.isArchive())
            return root.resource(root);
        return this.classFile.resource(root);
    }

    /**
     * @see Openable#openBuffer(IProgressMonitor, Object)
     */
    @Override
    protected IBuffer openBuffer(IProgressMonitor pm, Object info) throws JavaModelException {

        // create buffer
        IBuffer buffer = BufferManager.createBuffer(this);

        // set the buffer source
        IBuffer classFileBuffer = this.classFile.getBuffer();
        if (classFileBuffer != null) {
            buffer.setContents(classFileBuffer.getCharacters());
        } else {
            // Disassemble
            IClassFileReader reader = ToolFactory.createDefaultClassFileReader(this.classFile, IClassFileReader.ALL);
            Disassembler disassembler = new Disassembler();
            String contents = disassembler.disassemble(reader, Util.getLineSeparator("", getJavaProject()), ClassFileBytesDisassembler.WORKING_COPY); //$NON-NLS-1$
            buffer.setContents(contents);
        }

        // add buffer to buffer cache
        BufferManager bufManager = getBufferManager();
        // GROOVY edit
        //bufManager.addBuffer(buffer);
        ReflectionUtils.executePrivateMethod(BufferManager.class, "addBuffer", new Class[] {IBuffer.class}, bufManager, new Object[] {buffer});
        // GROOVY end

        // listen to buffer changes
        buffer.addBufferChangedListener(this);

        return buffer;
    }

    @Override
    protected void toStringName(StringBuffer buffer) {
        buffer.append(this.classFile.getElementName());
    }

    // GROOVY add
    @Override
    public ModuleNode getModuleNode() {
        // ensure moduleNode is initialized
        getPerWorkingCopyInfo();
        return this.moduleNode;
    }

    @Override
    public ModuleNodeInfo getModuleInfo(boolean force) {
        if (this.moduleNodeInfo == null) {
            try {
                this.reconcile(true, null);
            } catch (JavaModelException e) {
                Util.log(e);
            }
        }
        return this.moduleNodeInfo;
    }

    @Override
    public ModuleNodeInfo getNewModuleInfo() {
        if (this.moduleNodeInfo == null) {
            try {
                this.open(null);
            } catch (JavaModelException e) {
                Util.log(e);
            }
        }
        return this.moduleNodeInfo;
    }

    @Override
    public PerWorkingCopyInfo getPerWorkingCopyInfo() {
        if (this.elementInfo == null) {
            try {
                this.elementInfo = (CompilationUnitElementInfo) createElementInfo();
                openWhenClosed(this.elementInfo, true, new NullProgressMonitor());
            } catch (JavaModelException e) {
                this.elementInfo = null;
                Util.log(e);
            }
        }
        return this.copyInfo;
    }

    @Override
    public boolean isOnBuildPath() {
        return true;
    }

    /**
     * Cache module node locally and not in the mapper.
     */
    @Override
    protected void maybeCacheModuleNode(PerWorkingCopyInfo perWorkingCopyInfo, GroovyCompilationUnitDeclaration compilationUnitDeclaration) {
        if (compilationUnitDeclaration != null) {
            this.moduleNode = compilationUnitDeclaration.getModuleNode();
            this.moduleNode.setDescription(this.name); // aka "source.groovy"
            this.moduleNodeInfo = new ModuleNodeInfo(this.moduleNode, (JDTResolver) compilationUnitDeclaration.getCompilationUnit().getResolveVisitor());
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Translates from the source element of this synthetic compilation unit to
     * a binary element of the underlying class file.
     *
     * @param source the source element to translate
     * @return binary form of element or the closest possible match if it doesn't exist
     */
    public IJavaElement convertToBinary(IJavaElement source) {
        if (source.isReadOnly()) {
            // already binary
            return source;
        }
        if (source.getElementType() == IJavaElement.COMPILATION_UNIT) {
            return this.classFile;
        }
        if (!(source instanceof IMember)) {
            return this.classFile;
        }

        // get ancestors to type root
        List<IJavaElement> srcAncestors = new ArrayList<>(3);
        IJavaElement srcCandidate = source;
        while (srcCandidate != null && srcCandidate != this) {
            srcAncestors.add(srcCandidate);
            srcCandidate = srcCandidate.getParent();
        }

        // now, traverse the classFile using the ancestor list in reverse order
        IJavaElement binCandidate = this.classFile;
        try {
            while (!srcAncestors.isEmpty()) {
                srcCandidate = srcAncestors.remove(srcAncestors.size() - 1);
                if (!(srcCandidate instanceof IParent)) {
                    break;
                }

                String candidateName = srcCandidate.getElementName();
                IJavaElement[] binChildren = ((IParent) binCandidate).getChildren();
                boolean found = false;
                for (IJavaElement binChild : binChildren) {
                    if (binChild.getElementName().equals(candidateName) ||
                            // check for implicit closure class
                            (binChild.getElementType() == IJavaElement.TYPE && binChild.getParent().getElementName().equals(candidateName + '$' + binChild.getElementName() + ".class"))) {
                        binCandidate = binChild;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    break;
                }
            }
        } catch (JavaModelException e) {
            Util.log(e);
        }

        return binCandidate;
    }
    // GROOVY end
}
