package com.ebkb.everywhere.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot 서버가 정상 구동되고 요청을 처리하는지 확인하기 위한 테스트 컨트롤러입니다.
 * 외부 서비스(FastAPI)나 복잡한 DTO 파싱 없이 기본 통신만 테스트합니다.
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 기본 헬스 체크 엔드포인트: 서버 생존 여부 확인
     * GET http://localhost:8080/test/hello
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello Hackathon BE! Server is Running on 8080.";
    }

    /**
     * 파라미터 처리 및 간단한 DTO 반환 테스트
     * GET http://localhost:8080/test/echo?name=BE_Dev&id=123
     */
    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(
            @RequestParam String name,
            @RequestParam Long id) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Request received successfully.");
        response.put("received_name", name);
        response.put("received_id", id);

        return ResponseEntity.ok(response);
    }
}