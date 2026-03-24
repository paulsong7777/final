package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.StoreMenu;

@Mapper
public interface StoreMenuMapper {
	List<StoreMenu> findAll();
	StoreMenu findByIdx(int menuIdx);
	List<StoreMenu> findByStore(int storeIdx);
	List<StoreMenu> findByName(String menuName);
	List<StoreMenu> findByNameStore(
		@Param("storeIdx") int storeIdx,
		@Param("menuName") String menuName);
	int insert(StoreMenu storeMenu);
	int update(StoreMenu storeMenu);
	int reorder(
		@Param("menuIdx") int menuIdx,
		@Param("displayOrder") int displayOrder);
	int available(int menuIdx);
	int soldOut(int menuIdx);
	int hide(int menuIdx);
}
