package org.bahmni.module.service;

import org.bahmni.module.model.Form2JsonMetadata;

public interface Form2ReaderService {

    Form2JsonMetadata read(String jsonPath);
}
