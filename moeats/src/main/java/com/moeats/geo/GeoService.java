package com.moeats.geo;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoService {

    @Value("${kakao.api-key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate;

    public GeoPoint getLatLng(String address) {
        return getLatLng(address, null);
    }
    
    
    public GeoPoint getLatLng(String roadAddress, String jibunAddress) {
        GeoPoint point = requestAddressPoint(roadAddress);
        if (point != null) {
            return point;
        }

        String normalizedRoad = normalizeAddress(roadAddress);
        if (!normalizedRoad.equals(roadAddress)) {
            point = requestAddressPoint(normalizedRoad);
            if (point != null) {
                return point;
            }
        }

        if (jibunAddress != null && !jibunAddress.isBlank()) {
            point = requestAddressPoint(jibunAddress);
            if (point != null) {
                return point;
            }

            String normalizedJibun = normalizeAddress(jibunAddress);
            if (!normalizedJibun.equals(jibunAddress)) {
                point = requestAddressPoint(normalizedJibun);
                if (point != null) {
                    return point;
                }
            }
        }

        System.out.println("roadAddress = [" + roadAddress + "]");
        System.out.println("normalizedRoad = [" + normalizedRoad + "]");
        System.out.println("jibunAddress = [" + jibunAddress + "]");

        throw new RuntimeException("주소 변환 실패: " + roadAddress);
    }

    private GeoPoint requestAddressPoint(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                    .queryParam("query", address)
                    .build()
                    .encode()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey.trim());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() == null) {
                System.out.println("request address = [" + address + "]");
                System.out.println("body = null");
                return null;
            }

            Map body = response.getBody();
            List<Map> documents = (List<Map>) body.get("documents");

            System.out.println("request address = [" + address + "]");
            System.out.println("documents = " + documents);

            if (documents == null || documents.isEmpty()) {
                return null;
            }

            Map first = documents.get(0);

            Double lng = Double.parseDouble(String.valueOf(first.get("x")));
            Double lat = Double.parseDouble(String.valueOf(first.get("y")));

            return new GeoPoint(lat, lng);

        } catch (Exception e) {
            System.out.println("request address = [" + address + "]");
            e.printStackTrace();
            return null;
        }
    }

    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }
        
        // 💡 [추가된 핵심 코드] 정규식을 사용해 괄호 '(' 와 ')' 사이의 모든 문자를 공백으로 치환 후 제거
        String normalized = address.replaceAll("\\(.*?\\)", "").trim();

        // 기존 연속된 공백 제거 로직
        normalized = normalized.replaceAll("\\s+", " ");

        if (normalized.startsWith("대구 ")) {
            normalized = normalized.replaceFirst("^대구\\s+", "대구광역시 ");
        } else if (normalized.startsWith("서울 ")) {
            normalized = normalized.replaceFirst("^서울\\s+", "서울특별시 ");
        } else if (normalized.startsWith("부산 ")) {
            normalized = normalized.replaceFirst("^부산\\s+", "부산광역시 ");
        }

        return normalized;
    }
}