package com.moeats.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.StoreMenu;

@Mapper
public interface StoreMenuMapper {

    /**
     * [점주] 메뉴 리스트 조회
     */
    List<StoreMenu> menuList(@Param("storeIdx") int storeIdx);


    /**
     * [사용자] 메뉴 리스트 조회 (HIDDEN 제외)
     */
    List<StoreMenu> menuListForUser(@Param("storeIdx") int storeIdx);


    /**
     * [점주] 메뉴 검색
     */
    List<StoreMenu> searchMenu(
            @Param("storeIdx") int storeIdx,
            @Param("keyword") String keyword
    );


    /**
     * [사용자] 메뉴 검색 (HIDDEN 제외)
     */
    List<StoreMenu> searchMenuForUser(
            @Param("storeIdx") int storeIdx,
            @Param("keyword") String keyword
    );


    /**
     * 메뉴 단건 조회 (권한 체크용 / 수정용)
     */
    StoreMenu findByMenuIdx(
            @Param("storeIdx") int storeIdx,
            @Param("menuIdx") int menuIdx
    );


    /**
     * 메뉴 추가
     */
    void insertMenu(StoreMenu storeMenu);


    /**
     * 메뉴 수정
     */
    void updateMenu(StoreMenu storeMenu);


    /**
     * 메뉴 상태 변경 (AVAILABLE / SOLD_OUT / HIDDEN)
     */
    void updateStatus(
            @Param("storeIdx") int storeIdx,
            @Param("menuIdx") int menuIdx,
            @Param("menuStatus") String menuStatus
    );

}