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
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion.MethodParameter;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyPropertySuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovySuggestionDeclaringType;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IGroovySuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
public class InferencingContributionDialogue extends AbstractDialogue {

    enum ControlTypes implements IDialogueControlDescriptor {
        NAME("Name", "Enter a type name"),

        DECLARING_TYPE("Declaring Type", "Enter or browse for a declaring type"),

        TYPE("Type", "Enter or browse for a type"),

        IS_STATIC("  Is static", "Is the type static"),

        PROPERTY("Property", "Is type a property"),

        METHOD("Method", "Is type a method"),

        PARAMETERS("Parameters", "Enter parameters"),

        USE_NAMED_ARGUMENTS("  Use named arguments", "Should use named arguments?"),

        JAVA_DOC("Java Doc", "Enter Java Doc"),

        ADD("Add", "Add a suggestion"),

        REMOVE("Remove", "Remove a suggestions"),

        UP("Up", "Move suggestion up"),

        DOWN("Down", "Move suggestion down");

        private String label;

        private String toolTipText;

        private ControlTypes(String label, String toolTipText) {
            this.label = label;
            this.toolTipText = toolTipText;
        }

        public String getLabel() {

            return label;
        }

        public String getToolTipText() {

            return toolTipText;
        }

    }

    private SuggestionChange suggestion;

    private IGroovySuggestion initialSuggestion;

    private boolean mutableDeclaringType = true;

    private boolean useNamedParameters;

    private ParameterTable table;

    private IProject project;

    //
    // private IProject contextProject;

    public InferencingContributionDialogue(Shell parentShell, IGroovySuggestion suggestion,
            GroovySuggestionDeclaringType declaringType, IProject project) {
        this(parentShell, declaringType, project);
        setSuggestion(suggestion);
    }

    public InferencingContributionDialogue(Shell parentShell, GroovySuggestionDeclaringType declaringType, IProject project) {
        super(parentShell);
        suggestion = new SuggestionChange();
        this.project = project;
        if (declaringType != null) {
            suggestion.declaringTypeName = declaringType.getName();
            mutableDeclaringType = false;
        }
    }

    /**
     * May be null if no declaring type is specified.
     * 
     * @return
     */
    public SuggestionChange getSuggestionChange() {
        return suggestion;
    }

    protected void setSuggestion(IGroovySuggestion sugg) {
        this.initialSuggestion = sugg;
        if (initialSuggestion != null) {

            suggestion.isStatic = initialSuggestion.isStatic();
            suggestion.name = initialSuggestion.getName();

            suggestion.javaDoc = initialSuggestion.getJavaDoc();
            suggestion.type = initialSuggestion.getType();
        }
    }

    @Override
    protected void createCommandArea(Composite parent) {
        createFieldAreas(parent);
        createDocumentationArea(parent);
    }

    protected IJavaProject getJavaProject() {
        return project != null ? JavaCore.create(project) : null;
    }

    protected void createFieldAreas(Composite parent) {
        LabeledTextControl nameControl = new LabeledTextControl(ControlTypes.NAME, getOffsetLabelLocation(), suggestion.name);
        nameControl.createControlArea(parent);
        nameControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    suggestion.name = (String) selection;
                }
            }
        });

        JavaTextDialogueControl declaringTypeControl = new JavaTextDialogueControl( ControlTypes.DECLARING_TYPE,
                getOffsetLabelLocation(), suggestion.declaringTypeName, getJavaProject());
        declaringTypeControl.createControlArea(parent);
        if (!mutableDeclaringType) {
            declaringTypeControl.setEnabled(false);
        }
        declaringTypeControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    suggestion.declaringTypeName = (String) selection;
                }
            }
        });

        JavaTextDialogueControl typeControl = new JavaTextDialogueControl(ControlTypes.TYPE, getOffsetLabelLocation(),
                suggestion.type, getJavaProject());
        typeControl.createControlArea(parent);
        typeControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof String) {
                    suggestion.type = (String) selection;
                }
            }
        });

        ButtonDialogueControl isStaticButton = new ButtonDialogueControl(ControlTypes.IS_STATIC, SWT.CHECK, suggestion.isStatic);
        isStaticButton.createControlArea(parent);
        isStaticButton.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                Object selection = event.getSelectionData();
                if (selection instanceof Boolean) {
                    suggestion.isStatic = ((Boolean) selection).booleanValue();
                }
            }
        });

        // Set Property as the default selected button
        ControlTypes defaultSuggestionTypeButton = ControlTypes.PROPERTY;

        RadioSelectionDialogueControl radialSelection = new RadioSelectionDialogueControl(new IDialogueControlDescriptor[] {
                ControlTypes.PROPERTY, ControlTypes.METHOD }, defaultSuggestionTypeButton);

        radialSelection.createControlArea(parent);

        table = new ParameterTable();
        List<MethodParameter> arguments = getMethodArguments();
        if (arguments != null) {
            table.setInput(arguments);
        }
        table.createControlArea(parent);

        ButtonDialogueControl useName = new ButtonDialogueControl(ControlTypes.USE_NAMED_ARGUMENTS, SWT.CHECK, useNamedParameters);
        useName.createControlArea(parent);

        radialSelection.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                IDialogueControlDescriptor descriptor = event.getControlDescriptor();
                if (descriptor == ControlTypes.PROPERTY) {
                    table.setEnabled(false);

                } else if (descriptor == ControlTypes.METHOD) {
                    table.setEnabled(true);
                }
            }
        });

    }

    protected List<MethodParameter> getMethodArguments() {
        if (initialSuggestion instanceof GroovyMethodSuggestion) {
            return ((GroovyMethodSuggestion) initialSuggestion).getMethodArguments();
        }
        return null;
    }

    protected Point getOffsetLabelLocation() {

        int length = SWT.DEFAULT;
        int charLength = 0;
        IDialogueControlDescriptor[] descriptors = new IDialogueControlDescriptor[] { ControlTypes.DECLARING_TYPE,
                ControlTypes.IS_STATIC, ControlTypes.TYPE, ControlTypes.NAME

        };
        for (IDialogueControlDescriptor descriptor : descriptors) {
            // only compute label length of Base and Java editors
            String name = descriptor.getLabel();
            if (name != null) {
                int nameLength = name.length();
                if (nameLength > charLength) {
                    charLength = nameLength;
                }
            }
        }
        if (charLength > 0) {
            Control control = getShell();
            GC gc = new GC(control);
            Font requiredLabelFont = getRequiredParameterFont();
            gc.setFont(requiredLabelFont != null ? requiredLabelFont : control.getFont());
            FontMetrics fontMetrics = gc.getFontMetrics();
            // Increment the length by a few pixels to cover colon that may be
            // appended
            length = Dialog.convertWidthInCharsToPixels(fontMetrics, charLength);
            gc.dispose();
        }
        Point longestLabelWidth = new Point(length, -1);
        longestLabelWidth.x += getLabelNameSeparatorOffset();
        return longestLabelWidth;
    }

    protected int getLabelNameSeparatorOffset() {
        return 0;
    }

    protected Font getRequiredParameterFont() {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
    }

    protected void createDocumentationArea(Composite parent) {

        DocumentDialogueControl docControl = new DocumentDialogueControl(ControlTypes.JAVA_DOC, null);
        docControl.createControlArea(parent);
        docControl.addSelectionListener(new IControlSelectionListener() {

            public void handleSelection(ControlSelectionEvent event) {
                if (event.getSelectionData() instanceof String) {
                    suggestion.javaDoc = (String) event.getSelectionData();
                }
            }
        });

    }

    protected String iconLocation() {
        return "icons/full/mockup/newgroovyclass_wiz.gif";
    }

    public String getMessage() {
        return "Add a Groovy Inferencing Suggestion";
    }

    public String getTitle() {
        return "Groovy Inferencing Suggestion";
    }

    /**
     * 
     * @author Nieraj Singh
     * @created 2011-05-13
     */
    public class SuggestionChange {
        boolean isStatic;

        boolean isMethod = false;

        String name;

        String javaDoc;

        String declaringTypeName;

        String type;

        boolean useArgumentNames;

        public IGroovySuggestion getSuggestion() {
            GroovySuggestionDeclaringType declaringType = InferencingSuggestionsManager.getInstance().getDeclaringType(
                    declaringTypeName, project);
            if (declaringType != null) {
                return isMethod ? new GroovyMethodSuggestion(table.getMethodParameter(), useNamedParameters, name, type, isStatic,
                        javaDoc, declaringType) : new GroovyPropertySuggestion(name, type, isStatic, javaDoc, declaringType);
            }
            return null;

        }

    }

}
