package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moeats.domain.MenuImage;

@Mapper
public interface MenuImageMapper {
	List<MenuImage> findAll();
	MenuImage findByIdx(int menuImageIdx);
	List<MenuImage> findByMenuIdx(int menuIdx);
	MenuImage findPrimaryMenu(int menuIdx);
	int insert(MenuImage menuImage);
	int update(MenuImage menuImage);
	int delete(int menuImageIdx);
}
