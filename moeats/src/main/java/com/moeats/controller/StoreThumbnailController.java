package com.moeats.controller;

import com.moeats.dto.StoreThumbnailDto;
import com.moeats.service.StoreThumbnailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/store-thumbnails")
public class StoreThumbnailController {

    private final StoreThumbnailService storeThumbnailService;

    public StoreThumbnailController(StoreThumbnailService storeThumbnailService) {
        this.storeThumbnailService = storeThumbnailService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "ok";
    }

    @GetMapping("/{storeIdx}")
    public ResponseEntity<StoreThumbnailDto> getStoreThumbnail(
            @PathVariable("storeIdx") Long storeIdx) {

        StoreThumbnailDto result = storeThumbnailService.getStoreThumbnail(storeIdx);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<StoreThumbnailDto>> getStoreThumbnails(
            @RequestParam(name = "storeIds", required = false) List<Long> storeIds) {

        if (storeIds == null || storeIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        return ResponseEntity.ok(storeThumbnailService.getStoreThumbnails(storeIds));
    }
}