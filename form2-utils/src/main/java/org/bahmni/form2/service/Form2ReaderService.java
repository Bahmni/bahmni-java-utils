package org.bahmni.form2.service;

import org.bahmni.form2.model.Form2JsonMetadata;

public interface Form2ReaderService {

    Form2JsonMetadata read(String jsonPath);
}
