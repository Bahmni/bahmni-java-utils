package org.bahmni.odooconnect.extensions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SaleOrderContext {

    private final String patientUuid;
    private final String encounterUuid;
}
