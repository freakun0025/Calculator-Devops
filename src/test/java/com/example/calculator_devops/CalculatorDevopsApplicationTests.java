package com.example.calculator_devops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-context integration tests: boots the whole application (including Actuator)
 * and hits real endpoints through MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CalculatorDevopsApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void addEndpointWorksEndToEnd() throws Exception {
		mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.operation").value("add"))
				.andExpect(jsonPath("$.result").value(5.0));
	}

	@Test
	void divideByZeroReturns400EndToEnd() throws Exception {
		mockMvc.perform(get("/api/v1/divide").param("a", "1").param("b", "0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Bad Request"));
	}

	@Test
	void actuatorHealthIsUp() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

}
