package org.openhab.binding.forcomfort.handler;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

public class UpdateStatusHolder {

    private ThingStatus thingStatus;
    private ThingStatusDetail thingStatusDetail;
    private String description;

    public UpdateStatusHolder(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail, String description) {
        this.thingStatus = thingStatus;
        this.thingStatusDetail = thingStatusDetail;
        this.description = description;
    }

    public void setStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail, String description) {
        this.description = description;
        this.thingStatus = thingStatus;
        this.thingStatusDetail = thingStatusDetail;
    }

    public ThingStatus getThingStatus() {
        return thingStatus;
    }

    public void setThingStatus(ThingStatus thingStatus) {
        this.thingStatus = thingStatus;
    }

    public ThingStatusDetail getThingStatusDetail() {
        return thingStatusDetail;
    }

    public void setThingStatusDetail(ThingStatusDetail thingStatusDetail) {
        this.thingStatusDetail = thingStatusDetail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
