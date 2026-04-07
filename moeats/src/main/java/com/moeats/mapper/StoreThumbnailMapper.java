package com.moeats.mapper;

import com.moeats.dto.StoreThumbnailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreThumbnailMapper {

    StoreThumbnailDto findByStoreIdx(@Param("storeIdx") Long storeIdx);

    List<StoreThumbnailDto> findByStoreIdxList(@Param("storeIdxList") List<Long> storeIdxList);
}