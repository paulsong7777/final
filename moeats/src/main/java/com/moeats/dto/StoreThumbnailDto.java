package com.moeats.dto;

public class StoreThumbnailDto {

    private Long storeIdx;
    private String storeThumbnailUrl;

    public StoreThumbnailDto() {
    }

    public StoreThumbnailDto(Long storeIdx, String storeThumbnailUrl) {
        this.storeIdx = storeIdx;
        this.storeThumbnailUrl = storeThumbnailUrl;
    }

    public Long getStoreIdx() {
        return storeIdx;
    }

    public void setStoreIdx(Long storeIdx) {
        this.storeIdx = storeIdx;
    }

    public String getStoreThumbnailUrl() {
        return storeThumbnailUrl;
    }

    public void setStoreThumbnailUrl(String storeThumbnailUrl) {
        this.storeThumbnailUrl = storeThumbnailUrl;
    }
}