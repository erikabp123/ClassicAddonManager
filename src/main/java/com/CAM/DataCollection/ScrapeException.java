package com.CAM.DataCollection;

import com.CAM.AddonManagement.Addon;
import com.CAM.HelperTools.AddonSource;

public class ScrapeException extends Exception {

    private Exception exception;
    private AddonSource source;
    private Addon addon;
    private String message;

    public ScrapeException(AddonSource source, String message) {
        super(message);
        this.setMessage(message);
        this.exception = this;
        this.source = source;
    }

    public ScrapeException(AddonSource source, Exception e){
        super();
        this.exception = e;
        this.source = source;
    }

    public ScrapeException(Addon addon, Exception e){
        super();
        this.exception = e;
        this.addon = addon;
        this.source = addon.getAddonSource();
    }


    public Exception getException() {
        return exception;
    }

    public Class<?> getType() {
        return exception.getClass();
    }

    public AddonSource getSource() {
        return source;
    }

    public void setSource(AddonSource source) {
        this.source = source;
    }

    public Addon getAddon() {
        return addon;
    }

    public void setAddon(Addon addon) {
        this.addon = addon;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
