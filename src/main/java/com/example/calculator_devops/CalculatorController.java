package com.example.calculator_devops; 

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    @GetMapping("/add")
    public Map<String, Object> add(@RequestParam double a, @RequestParam double b) {
        return Map.of("operation", "add", "a", a, "b", b, "result", a + b);
    }

    @GetMapping("/subtract")
    public Map<String, Object> subtract(@RequestParam double a, @RequestParam double b) {
        return Map.of("operation", "subtract", "a", a, "b", b, "result", a - b);
    }

    @GetMapping("/multiply")
    public Map<String, Object> multiply(@RequestParam double a, @RequestParam double b) {
        return Map.of("operation", "multiply", "a", a, "b", b, "result", a * b);
    }

    @GetMapping("/divide")
    public ResponseEntity<?> divide(@RequestParam double a, @RequestParam double b) {
        if (b == 0) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Division by zero is not allowed", "status", 400)
            );
        }
        return ResponseEntity.ok(Map.of("operation", "divide", "a", a, "b", b, "result", a / b));
    }
}