/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.util.squasher.radiology;

import java.sql.Timestamp;

public class Visit {
    private int visitId;
    private Timestamp dateStarted;
    private Timestamp dateStopped;
    private int patientId;

    public Visit(int visitId, Timestamp dateStarted, Timestamp dateStopped, int patientId) {
        this.visitId = visitId;
        this.dateStarted = dateStarted;
        this.dateStopped = dateStopped;
        this.patientId = patientId;
    }

    public int getVisitId() {
        return visitId;
    }

    public Timestamp getDateStarted() {
        return dateStarted;
    }

    public Timestamp getDateStopped() {
        return dateStopped;
    }

    public int getPatientId() {
        return patientId;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "visitId=" + visitId +
                ", dateStarted=" + dateStarted +
                ", dateStopped=" + dateStopped +
                ", patientId=" + patientId +
                '}';
    }
}
