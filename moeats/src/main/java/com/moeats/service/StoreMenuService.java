package com.moeats.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moeats.domain.StoreMenu;
import com.moeats.mapper.StoreMenuMapper;

@Service
@Transactional
public class StoreMenuService {

    @Autowired
    private StoreMenuMapper storeMenuMapper;

    /**
     * [점주] 메뉴 삭제
     */
    @Transactional // 🚨 여러 테이블을 건드리므로 트랜잭션 처리가 안전합니다.
    public boolean deleteMenu(int storeIdx, int menuIdx) {
        // 1. 이미지 테이블에서 해당 메뉴의 이미지 정보 먼저 삭제 (필요 시)
        // storeMenuMapper.deleteMenuImages(menuIdx); 

        // 2. 메뉴 삭제
        int count = storeMenuMapper.deleteMenu(storeIdx, menuIdx);
        
        return count > 0;
    }
    
    /**
     * [점주] 메뉴 리스트 조회
     */
    public List<StoreMenu> menuList(int storeIdx) {
        return storeMenuMapper.menuList(storeIdx);
    }


    /**
     * [사용자] 메뉴 리스트 조회 (HIDDEN 제외)
     */
    public List<StoreMenu> menuListForUser(int storeIdx) {
        return storeMenuMapper.menuListForUser(storeIdx);
    }


    /**
     * [점주] 메뉴 검색
     */
    public List<StoreMenu> searchMenu(int storeIdx, String keyword) {
        return storeMenuMapper.searchMenu(storeIdx, keyword);
    }


    /**
     * [사용자] 메뉴 검색
     */
    public List<StoreMenu> searchMenuForUser(int storeIdx, String keyword) {
        return storeMenuMapper.searchMenuForUser(storeIdx, keyword);
    }


    /**
     * 메뉴 단건 조회 (검증 포함)
     */
    public StoreMenu getMenu(int storeIdx, int menuIdx) {

        StoreMenu menu = storeMenuMapper.findByMenuIdx(storeIdx, menuIdx);

        if (menu == null) {
            throw new RuntimeException("메뉴 없음 또는 권한 없음");
        }

        return menu;
    }


    /**
     * 메뉴 등록
     */
    public void insertMenu(StoreMenu storeMenu) {

        // 기본 상태값 세팅
        if (storeMenu.getMenuStatus() == null) {
            storeMenu.setMenuStatus("AVAILABLE");
        }

        validateStatus(storeMenu.getMenuStatus());

        storeMenuMapper.insertMenu(storeMenu);
    }


    /**
     * 메뉴 수정
     */
    public void updateMenu(StoreMenu storeMenu) {

        // 1. 존재 + 권한 체크
        getMenu(storeMenu.getStoreIdx(), storeMenu.getMenuIdx());

        // 2. 상태값 검증
        validateStatus(storeMenu.getMenuStatus());

        storeMenuMapper.updateMenu(storeMenu);
    }


    /**
     * 메뉴 상태 변경 (버튼용)
     */
    public void updateStatus(int storeIdx, int menuIdx, String menuStatus) {

        // 1. 존재 + 권한 체크
        getMenu(storeIdx, menuIdx);

        // 2. 상태값 검증
        validateStatus(menuStatus);

        storeMenuMapper.updateStatus(storeIdx, menuIdx, menuStatus);
    }


    /**
     * 상태값 검증 (핵심 로직)
     */
    private void validateStatus(String menuStatus) {

        if (menuStatus == null ||
           (!menuStatus.equals("AVAILABLE") &&
            !menuStatus.equals("SOLD_OUT") &&
            !menuStatus.equals("HIDDEN"))) {

            throw new IllegalArgumentException("잘못된 메뉴 상태값: " + menuStatus);
        }
    }

}