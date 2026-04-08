package com.moeats.service;

import com.moeats.dto.StoreThumbnailDto;

import java.util.List;

public interface StoreThumbnailService {

    StoreThumbnailDto getStoreThumbnail(Long storeIdx);

    List<StoreThumbnailDto> getStoreThumbnails(List<Long> storeIdxList);
}