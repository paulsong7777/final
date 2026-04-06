package com.moeats.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreSearchCond {
	// 키워드 검색 or 카테고리 클릭시 가게 리스트 변환에 필요
    private String category;
    private String keyword;
    
    // 👇 추가 - 사용자가 요청할때 사용하는 좌표
    private Double userLat;
    private Double userLng;
}