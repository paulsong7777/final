package com.moeats.geo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoService {

    @Value("${kakao.api-key}")
    private String KAKAO_API_KEY;

    private final RestTemplate restTemplate;

    public GeoPoint getLatLng(String address) {

        try {
            // 1. 주소 인코딩 (한글 깨짐 방지)
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);

            // 2. API URL 생성
            String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encodedAddress;

            // 3. 헤더 설정 (카카오 인증)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", KAKAO_API_KEY);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 4. API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // 5. 응답 검증
            if (response.getBody() == null) {
                throw new RuntimeException("카카오 API 응답 없음");
            }

            Map body = response.getBody();
            List<Map> documents = (List<Map>) body.get("documents");

            if (documents == null || documents.isEmpty()) {
                throw new RuntimeException("주소 변환 실패: " + address);
            }

            // 6. 첫 번째 결과 사용
            Map first = documents.get(0);

            Double lng = Double.parseDouble((String) first.get("x")); // 경도
            Double lat = Double.parseDouble((String) first.get("y")); // 위도

            return new GeoPoint(lat, lng);

        } catch (Exception e) {
            throw new RuntimeException("좌표 변환 중 오류 발생", e);
        }
    }
}