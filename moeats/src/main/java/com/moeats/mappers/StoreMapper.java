package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.Store;

@Mapper
public interface StoreMapper {
	List<Store> findAll();
	Store findByIdx(int storeIdx);
	List<Store> findByOwner(int ownerMemberIdx);
	List<Store> findByName(String storeName);
	List<Store> findByPosition(
		@Param("longitude") double longitude,
		@Param("latitude") double latitude);
	int insert(Store store);
	int update(Store store);
	int activate(int storeIdx);
	int inactivate(int storeIdx);
	int pause(int storeIdx);
}