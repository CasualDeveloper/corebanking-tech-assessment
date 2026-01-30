package com.assessment.corebanking.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAspectTest {

    @Test
    void maskCardNumberRedactsExceptLastFour() {
        LoggingAspect aspect = new LoggingAspect(new ObjectMapper());
        String input = "{\"cardNumber\":\"4293127308501088\",\"cardholderName\":\"Danial\"}";

        String output = aspect.maskCardNumber(input);

        assertThat(output).contains("\"cardNumber\":\"************1088\"");
        assertThat(output).doesNotContain("4293127308501088");
        assertThat(output).contains("\"cardholderName\":\"Danial\"");
    }
}
