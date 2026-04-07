package com.moeats.service;

import com.moeats.dto.StoreThumbnailDto;
import com.moeats.mapper.StoreThumbnailMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class StoreThumbnailServiceImpl implements StoreThumbnailService {

    private final StoreThumbnailMapper storeThumbnailMapper;

    public StoreThumbnailServiceImpl(StoreThumbnailMapper storeThumbnailMapper) {
        this.storeThumbnailMapper = storeThumbnailMapper;
    }

    @Override
    public StoreThumbnailDto getStoreThumbnail(Long storeIdx) {
        if (storeIdx == null) {
            return null;
        }
        return storeThumbnailMapper.findByStoreIdx(storeIdx);
    }

    @Override
    public List<StoreThumbnailDto> getStoreThumbnails(List<Long> storeIdxList) {
        if (storeIdxList == null || storeIdxList.isEmpty()) {
            return Collections.emptyList();
        }
        return storeThumbnailMapper.findByStoreIdxList(storeIdxList);
    }
}