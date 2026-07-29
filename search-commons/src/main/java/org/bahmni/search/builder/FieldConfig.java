package org.bahmni.search.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bahmni.search.model.FieldType;

@Getter
@AllArgsConstructor
public class FieldConfig {

    private final String joinPath;
    private final String propertyName;
    private final FieldType fieldType;

}
