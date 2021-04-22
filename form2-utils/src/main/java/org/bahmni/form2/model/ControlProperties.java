package org.bahmni.form2.model;

public class ControlProperties {
    private boolean addMore;
    private boolean multiSelect;
    private boolean mandatory;
    private boolean allowFutureDates;

    public boolean isAddMore() {
        return addMore;
    }

    public void setAddMore(boolean addMore) {
        this.addMore = addMore;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isAllowFutureDates() {
        return allowFutureDates;
    }

    public void setAllowFutureDates(boolean allowFutureDates) {
        this.allowFutureDates = allowFutureDates;
    }
}
