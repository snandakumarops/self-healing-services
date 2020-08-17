package com.redhat.riskvalidationservice.datamodels;

public class SensuEvents {

    private String checkType;
    private Integer eventDate;
    private String status;
    private String hostName;

    @Override
    public String toString() {
        return "SensuEvents{" +
                "checkType='" + checkType + '\'' +
                ", eventDate=" + eventDate +
                ", status='" + status + '\'' +
                ", hostName='" + hostName + '\'' +
                '}';
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public Integer getEventDate() {
        return eventDate;
    }

    public void setEventDate(Integer eventDate) {
        this.eventDate = eventDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
