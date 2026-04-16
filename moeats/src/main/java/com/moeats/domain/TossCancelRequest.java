package com.moeats.domain;

public class TossCancelRequest {

    private String cancelReason;

    public TossCancelRequest() {
    }

    public TossCancelRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
