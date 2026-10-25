package com.example.calculator_devops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring MVC slice tests (MockMvc) for the REST layer, including validation errors.
 */
@WebMvcTest(CalculatorController.class)
@Import({CalculatorService.class, GlobalExceptionHandler.class})
public class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAdd() throws Exception {
        mockMvc.perform(get("/api/v1/add?a=5&b=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.a").value(5.0))
                .andExpect(jsonPath("$.b").value(3.0))
                .andExpect(jsonPath("$.result").value(8.0));
    }

    @Test
    public void testSubtract() throws Exception {
        mockMvc.perform(get("/api/v1/subtract?a=10&b=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("subtract"))
                .andExpect(jsonPath("$.result").value(6.0));
    }

    @Test
    public void testMultiply() throws Exception {
        mockMvc.perform(get("/api/v1/multiply?a=4&b=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("multiply"))
                .andExpect(jsonPath("$.result").value(12.0));
    }

    @Test
    public void testDivideSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/divide?a=10&b=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("divide"))
                .andExpect(jsonPath("$.result").value(5.0));
    }

    @Test
    public void testDivideByZero() throws Exception {
        mockMvc.perform(get("/api/v1/divide?a=10&b=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed"))
                .andExpect(jsonPath("$.path").value("/api/v1/divide"));
    }

    @Test
    public void testMissingParameter() throws Exception {
        mockMvc.perform(get("/api/v1/add?a=5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing required parameter 'b'"));
    }

    @Test
    public void testNonNumericParameter() throws Exception {
        mockMvc.perform(get("/api/v1/multiply?a=abc&b=2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parameter 'a' must be a valid number, got 'abc'"));
    }

    @Test
    public void testNonFiniteParameter() throws Exception {
        mockMvc.perform(get("/api/v1/subtract?a=NaN&b=2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
