package org.bahmni.module.service.impl;

import org.bahmni.module.exception.InvalidFormException;
import org.bahmni.module.model.Control;
import org.bahmni.module.model.Form2JsonMetadata;
import org.bahmni.module.service.Form2ReaderService;
import org.bahmni.module.service.Form2Service;
import org.bahmni.module.service.FormFieldPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class FormFieldPathServiceImpl implements FormFieldPathService {

    private Form2Service form2Service;
    private Form2ReaderService form2ReaderService;
    private Map<String, Map<String, String>> formNamesToFormFieldPathMap = new HashMap<>();

    @Autowired
    public FormFieldPathServiceImpl(Form2Service form2Service, Form2ReaderService form2ReaderService) {
        this.form2Service = form2Service;
        this.form2ReaderService = form2ReaderService;
    }

    @Override
    public String getFormFieldPath(List<String> orderedControlNames) {

        final String formName = getFormName(orderedControlNames);
        if (!formNamesToFormFieldPathMap.containsKey(formName)) {
            initializeControlFormFieldPaths(formName);
        }
        return formNamesToFormFieldPathMap.get(formName).get(orderedControlNames.toString());
    }

    private void initializeControlFormFieldPaths(String formName) {
        final String formJsonPath = form2Service.getFormPath(formName);
        if (StringUtils.isEmpty(formJsonPath)) {
            throw new InvalidFormException(format("%s not found", formName));
        }
        final Form2JsonMetadata form2JsonMetadata = form2ReaderService.read(formJsonPath);
        final Integer formLatestVersion = form2Service.getFormLatestVersion(formName);

        final String initialFormFieldPath = initializeFormFieldPath(formName, formLatestVersion);
        final Map<String, String> controlToFormFieldPath = new HashMap<>();
        formNamesToFormFieldPathMap.put(formName, controlToFormFieldPath);

        final List<String> orderedControlNames = asList(formName);
        form2JsonMetadata.getControls().forEach(control -> {
            initializeControlFormFieldPaths(control, orderedControlNames, initialFormFieldPath,
                    controlToFormFieldPath, isAddMore(control));
        });
    }

    private void initializeControlFormFieldPaths(Control control, List<String> orderedControlNames,
                                                 String formFieldPath, Map<String, String> controlToFormFieldPath, Boolean isControlAddMore) {
        final List<String> currentOrderedControlNames = new ArrayList<>(orderedControlNames);
        currentOrderedControlNames.add(control.getLabel().getValue());
        final String currentFormFieldPath = getFormFieldPath(control.getId(), formFieldPath);
        controlToFormFieldPath.put(currentOrderedControlNames.toString(), currentFormFieldPath);
        final List<Control> controls = control.getControls();
        if (controls != null) {
            for(Control childControl : controls ){
                if(isAddMore(control)){
                    isControlAddMore = true;
                }
                initializeControlFormFieldPaths(childControl, currentOrderedControlNames,
                        isControlAddMore ? currentFormFieldPath : formFieldPath,
                        controlToFormFieldPath, isControlAddMore);
            }
        }
    }

    private boolean isAddMore(Control control) {
        return control.getProperties().isAddMore();
    }

    private String getFormFieldPath(String controlId, String formFieldPath) {
        return format("%s/%s-0", formFieldPath, controlId);
    }

    private String initializeFormFieldPath(String formName, Integer formLatestVersion) {
        return formName + "." + formLatestVersion.toString();
    }

    private String getFormName(List<String> orderedPathToControl) {
        return !isEmpty(orderedPathToControl) ? orderedPathToControl.get(0) : "";
    }

}
