/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;


@Deprecated
public class EmailAppender extends AppenderBase<ILoggingEvent> {

	private static final Logger logger = LoggerFactory.getLogger(EmailAppender.class);

	@Override
	protected void append(ILoggingEvent eventObject) {
		logger.info("EmailAppender received log event: " + eventObject.getMessage());
	}
}
