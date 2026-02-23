/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.odooconnect.extensions;

import java.util.Map;
import java.util.function.BiFunction;

public interface SaleOrderParameterProvider {

    Map<String, Object> getAdditionalParams(SaleOrderContext context, BiFunction<String, Class<?>, Object> webClientGet);
}
