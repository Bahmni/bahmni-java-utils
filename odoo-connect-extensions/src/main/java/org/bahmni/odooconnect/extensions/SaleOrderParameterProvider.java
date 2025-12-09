package org.bahmni.odooconnect.extensions;

import java.util.Map;
import java.util.function.BiFunction;

public interface SaleOrderParameterProvider {

    Map<String, Object> getAdditionalParams(SaleOrderContext context, BiFunction<String, Class<?>, Object> webClientGet);
}
