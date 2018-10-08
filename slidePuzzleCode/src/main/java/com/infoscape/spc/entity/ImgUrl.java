package com.infoscape.spc.entity;

public class ImgUrl {
    private String mattUrl;
    private String bcUrl;

    public ImgUrl() {
    }

    public ImgUrl(String mattUrl, String bcUrl) {
        this.mattUrl = mattUrl;
        this.bcUrl = bcUrl;
    }

    public String getMattUrl() {
        return mattUrl;
    }

    public void setMattUrl(String mattUrl) {
        this.mattUrl = mattUrl;
    }

    public String getBcUrl() {
        return bcUrl;
    }

    public void setBcUrl(String bcUrl) {
        this.bcUrl = bcUrl;
    }
}
