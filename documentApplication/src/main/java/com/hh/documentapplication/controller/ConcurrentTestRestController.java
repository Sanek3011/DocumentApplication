package com.hh.documentapplication.controller;

import com.hh.documentapplication.service.ConcurrencyTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
@RequiredArgsConstructor

@RestController
@RequestMapping("/api/v1/concurrent")
public class ConcurrentTestRestController {

    private final ConcurrencyTestService concurrencyTestService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> testConcurrency(@RequestParam int threads,
                                                               @RequestParam int attempts,
                                                               @RequestParam UUID uuid) throws InterruptedException {

        Map<String, Object> resultMap = concurrencyTestService.concurrentTest(threads, attempts, uuid);
        return ResponseEntity.ok(resultMap);
    }
}
