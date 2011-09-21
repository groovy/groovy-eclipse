/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovySuggestionDeclaringType;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IGroovySuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IValueCheckingRule;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.JavaValidTypeRule;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.MethodParameter;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.SuggestionDescriptor;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ValueStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * If the dialogue is opened with an existing declaring type, the declaring type
 * cannot be changed.
 * Therefore it means no support for refactor move of an existing suggestion
 * through the dialogue.
 * <p>
 * Refactor move of an existing suggestion from one declaring type to another
 * may be possible in the future
 * </p>
 * 
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class AddInferencingSuggestionDialogue extends AbstractDialogue {
    public static final DialogueDescriptor DIALOGUE_DESCRIPTOR = new DialogueDescriptor(
            "Add a Groovy inferencing suggestion", "Inferencing Suggestion", "icons/GROOVY.png");

    private Point labelControlOffset;

    private boolean isStatic;

    private boolean isMethod = false;

    private String suggestionName;

    private String javaDoc;

    private String declaringTypeName;

    private String suggestionType;

    private boolean isActive;

    private List<MethodParameter> initialParameters;

    private IGroovySuggestion currentSuggestion;

    private boolean useNamedArguments;

    private MethodParameterTable table;

    private IProject project;

    public AddInferencingSuggestionDialogue(Shell parentShell, SuggestionDescriptor descriptor, IProject project) {
        super(parentShell);
        this.project = project;
        setSuggestion(descriptor);
    }

    /**
     * This constructor is used to add new suggestion to an existing declaring
     * type
     */
    public AddInferencingSuggestionDialogue(Shell parentShell, GroovySuggestionDeclaringType declaringType, IProject project) {
        this(parentShell, project, null, declaringType, true);
    }

    /**
     * This constructor is used to add a new suggestion. The user is expected to
     * specify the declaring type in the UI controls.
     */
    public AddInferencingSuggestionDialogue(Shell parentShell, IProject project) {
        this(parentShell, project, null, null, true);
    }

    protected AddInferencingSuggestionDialogue(Shell parentShell, IProject project, IGroovySuggestion currentSuggestion,
            GroovySuggestionDeclaringType declaringType, boolean isActive) {
        super(parentShell);
        this.project = project;
        this.currentSuggestion = currentSuggestion;
        this.declaringTypeName = declaringType != null ? declaringType.getName() : null;
        this.isActive = isActive;
    }

    protected DialogueDescriptor getDialogueDescriptor() {
        return DIALOGUE_DESCRIPTOR;
    }

    /**
     * May be null if there is no suggestion that is being edited. If the
     * dialogue is
     * being used to add a new suggestion, current suggestion will return null,
     * as the dialogue
     * doesn't create a suggestion. Rather the dialogue creates a suggestion
     * descriptor that can be
     * used to either create the actual suggestion or edit an existing one
     * outside of the dialogue logic.
     * 
     * @return
     */
    public IGroovySuggestion getCurrentSuggestion() {
        return currentSuggestion;
    }

    public SuggestionDescriptor getSuggestionChange() {
        return isMethod ? new SuggestionDescriptor(declaringTypeName, isStatic, suggestionName, javaDoc, suggestionType,
                useNamedArguments, table.getMethodParameter(), isActive) : new SuggestionDescriptor(declaringTypeName, isStatic,
                suggestionName, javaDoc, suggestionType, isActive);
    }

    protected void setSuggestion(IGroovySuggestion suggestion) {
        this.currentSuggestion = suggestion;
        if (currentSuggestion != null) {
            isStatic = currentSuggestion.isStatic();
            suggestionName = currentSuggestion.getName();
            declaringTypeName = currentSuggestion.getDeclaringType().getName();
            javaDoc = currentSuggestion.getJavaDoc();
            suggestionType = currentSuggestion.getType();
            isActive = currentSuggestion.isActive();
            if (currentSuggestion instanceof GroovyMethodSuggestion) {
                GroovyMethodSuggestion method = (GroovyMethodSuggestion) currentSuggestion;
                initialParameters = method.getParameters();
                useNamedArguments = method.useNamedArguments();
                isMethod = true;
            }
        }
    }

    protected void setSuggestion(SuggestionDescriptor descriptor) {
        isStatic = descriptor.isStatic();
        suggestionName = descriptor.getName();
        declaringTypeName = descriptor.getDeclaringTypeName();
        javaDoc = descriptor.getJavaDoc();
        suggestionType = descriptor.getSuggestionType();
        isActive = descriptor.isActive();

        if (descriptor.isMethod()) {

            initialParameters = descriptor.getParameters();
            useNamedArguments = descriptor.isUseArgumentNames();
            isMethod = true;
        }
    }

    protected void createCommandArea(Composite parent) {
        createFieldAreas(parent);
        createDocumentationArea(parent);
    }

    protected IJavaProject getJavaProject() {
        return project != null ? JavaCore.create(project) : null;
    }

    protected void createFieldAreas(Composite parent) {

        JavaTextControl nameControl = new JavaTextControl(ControlTypes.NAME, getOffsetLabelLocation(), suggestionName);
        nameControl.createControlArea(parent);
        nameControl.addSelectionListener(new ValidatedValueSelectionListener(ControlTypes.NAME, suggestionName) {

            protected void handleValidatedValue(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    suggestionName = (String) selection;
                }
            }
        });

        JavaTypeBrowsingControl declaringTypeControl = new JavaTypeBrowsingControl(ControlTypes.DECLARING_TYPE,
                getOffsetLabelLocation(), declaringTypeName, getJavaProject()) {

            // Don't check for parameterized types as it not necessary for
            // declaring types
            protected IValueCheckingRule getCachedValidationRule() {
                return new JavaValidTypeRule(getJavaProject());
            }

        };
        declaringTypeControl.createControlArea(parent);

        declaringTypeControl.setEnabled(true);
        declaringTypeControl.addSelectionListener(new ValidatedValueSelectionListener(ControlTypes.DECLARING_TYPE,
                declaringTypeName) {

            protected void handleValidatedValue(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    declaringTypeName = (String) selection;
                }
            }
        });

        JavaTypeBrowsingControl suggestionTypeControl = new JavaTypeBrowsingControl(ControlTypes.TYPE, getOffsetLabelLocation(),
                suggestionType, getJavaProject()) {

            protected ValueStatus isControlValueValid(String value) {
                if (value == null || value.length() == 0) {
                    return ValueStatus.getValidStatus(value);
                }
                return super.isControlValueValid(value);
            }

        };
        suggestionTypeControl.createControlArea(parent);
        suggestionTypeControl.addSelectionListener(new ValidatedValueSelectionListener() {

            protected void handleValidatedValue(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    suggestionType = (String) selection;
                }
            }
        });

        ButtonDialogueControl isStaticButton = new ButtonDialogueControl(ControlTypes.IS_STATIC, SWT.CHECK, isStatic);
        isStaticButton.createControlArea(parent);
        isStaticButton.addSelectionListener(new ControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof Boolean) {
                    isStatic = ((Boolean) selection).booleanValue();
                }
            }
        });

        // Set Property as the default selected button
        ControlTypes defaultSuggestionTypeButton = isMethod ? ControlTypes.METHOD : ControlTypes.PROPERTY;

        RadioSelectionDialogueControl radioSelection = new RadioSelectionDialogueControl(new IDialogueControlDescriptor[] {
                ControlTypes.PROPERTY, ControlTypes.METHOD }, defaultSuggestionTypeButton);

        radioSelection.createControlArea(parent);

        table = new MethodParameterTable(getJavaProject(), initialParameters, useNamedArguments);

        table.createControlArea(parent);

        // If the default is not a method suggestion, disable the parameter tree
        if (!isMethod) {
            table.setEnabled(false);
        }

        table.addSelectionListener(new ControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (event.getControlDescriptor() == ControlTypes.USE_NAMED_ARGUMENTS && selection instanceof Boolean) {
                    useNamedArguments = ((Boolean) selection).booleanValue();
                }
            }
        });

        radioSelection.addSelectionListener(new ControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                IDialogueControlDescriptor descriptor = event.getControlDescriptor();
                if (descriptor == ControlTypes.PROPERTY) {
                    table.setEnabled(false);
                    isMethod = false;
                } else if (descriptor == ControlTypes.METHOD) {
                    table.setEnabled(true);
                    isMethod = true;
                }
            }
        });

    }

    protected Point getOffsetLabelLocation() {
        if (labelControlOffset == null) {
            IDialogueControlDescriptor[] descriptors = new IDialogueControlDescriptor[] { ControlTypes.DECLARING_TYPE,
                    ControlTypes.IS_STATIC, ControlTypes.TYPE, ControlTypes.NAME };
            String[] labelNames = new String[descriptors.length];
            for (int i = 0; i < descriptors.length; ++i) {
                labelNames[i] = descriptors[i].getLabel();
            }
            labelControlOffset = getOffsetLabelLocation(labelNames);
        }
        return labelControlOffset;
    }

    protected void createDocumentationArea(Composite parent) {

        DocumentDialogueControl docControl = new DocumentDialogueControl(ControlTypes.DOC, null, javaDoc);
        docControl.createControlArea(parent);
        docControl.addSelectionListener(new ControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                if (event.getSelectionData() instanceof String) {
                    javaDoc = (String) event.getSelectionData();
                }
            }
        });

    }

}
