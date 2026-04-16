package com.moeats.domain;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    // 가게 정보
    private int storeIdx;                  // 가게번호
    private int ownerMemberIdx;            // 점주 회원번호
    private String storeName;              // 매장명
    private String storeCategory;          // 가게 카테고리
    private String storeDescription;       // 가게 소개
    private String storePhone;             // 가게 전화
    private int minimumOrderAmount;        // 배달 최소 주문 금액
    private String storeAddress1;          // 기본 주소
    private String storeAddress2;          // 상세 주소
    private Boolean supportsDelivery;      // 배달지원여부
    private Boolean supportsOnsite;        // 현장지원여부
    private String storeStatus;            // 가게 상태
    private Double longitude;              // 경도
    private Double latitude;               // 위도
    private Integer deliveryRadiusM;       // 배달 가능 반경(m)

    private Timestamp createdAt;           // 생성 시각
    private Timestamp updatedAt;           // 수정 시각
    
    // 가게상세 페이지 상단 히어로용 대표 이미지(썸네일)
    private String heroImageUrl;
    
    // FE-01 대표 메뉴 칩용
    private String menuPreviewText;
}