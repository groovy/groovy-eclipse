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
package org.codehaus.groovy.eclipse.wizards;

import org.eclipse.osgi.util.NLS;

public final class WizardMessages extends NLS {

    private WizardMessages() {}

    public static String NewProjWizard_title;
    public static String NewProjWizard_page1_title;
    public static String NewProjWizard_page1_message;
    public static String NewProjWizard_page2_title;
    public static String NewProjWizard_page2_message;
    public static String NewProjWizard_error_title;
    public static String NewProjWizard_error_message;

    public static String NewTestWizard_title;

    public static String NewTypeWizard_title;
    public static String NewTypeWizard_page1_title;
    public static String NewTypeWizard_page1_message;
    public static String NewTypeWizard_page2_class_title;
    public static String NewTypeWizard_page2_class_message;
    public static String NewTypeWizard_page2_trait_title;
    public static String NewTypeWizard_page2_trait_message;
    public static String NewTypeWizard_page2_record_title;
    public static String NewTypeWizard_page2_record_message;
    public static String NewTypeWizard_page2_script_title;
    public static String NewTypeWizard_page2_script_message;
    public static String NewTypeWizard_page2_interface_title;
    public static String NewTypeWizard_page2_interface_message;
    public static String NewTypeWizard_page2_annotation_title;
    public static String NewTypeWizard_page2_annotation_message;
    public static String NewTypeWizard_page2_enumeration_title;
    public static String NewTypeWizard_page2_enumeration_message;

    public static String NewTypeWizard_page1_typeKind;
    public static String NewTypeWizard_page1_typeKind1;
    public static String NewTypeWizard_page1_typeKind2;
    public static String NewTypeWizard_page1_typeKind3;
    public static String NewTypeWizard_page1_typeKind4;
    public static String NewTypeWizard_page1_typeKind5;
    public static String NewTypeWizard_page1_typeKind6;
    public static String NewTypeWizard_page1_typeKind7;
    public static String NewTypeWizard_page1_error_java;
    public static String NewTypeWizard_page1_error_script;
    public static String NewTypeWizard_page1_info_groovyNature;
    public static String NewTypeWizard_page1_info_fileExtension;
    public static String NewTypeWizard_page1_warning_typeExcluded;

    public static String NewTypeWizard_page2_methods_main;
    public static String NewTypeWizard_page2_selftype_label;
    public static String NewTypeWizard_page2_basescript_label;

    static {
        initializeMessages(WizardMessages.class.getName(), WizardMessages.class);
    }
}
