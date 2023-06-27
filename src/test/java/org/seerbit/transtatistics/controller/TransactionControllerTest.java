package org.seerbit.transtatistics.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Order(1)
    @Test
    void given_validTransactionDetailOlderThan30Seconds_when_postTransaction_should_returnHttpNO_CONTENT() throws Exception {

        String jsonRequest = "{ \"amount\" : 12.10, " +
                "\"timestamp\" : \""+LocalDateTime.now().minusSeconds(40L).format(DateTimeFormatter.ISO_DATE_TIME)+"\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

    }

    @Order(2)
    @Test
    void given_validTransactionDetailInTheFuture_when_postTransaction_should_returnHttpUNPROCESSABLE_ENTITY() throws Exception {

        String jsonRequest = "{ \"amount\" : 12.10, " +
                "\"timestamp\" : \""+LocalDateTime.now().plusSeconds(40L).format(DateTimeFormatter.ISO_DATE_TIME)+"\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());

    }

    @Order(3)
    @Test
    void given_unacceptableAmount_when_postTransaction_should_returnHttpUNPROCESSABLE_ENTITY() throws Exception {

        String jsonRequest = "{ \"amount\" : -12.10, " +
                "\"timestamp\" : \""+LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)+"\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());

    }

    @Order(4)
    @Test
    void given_validTransactionDetailWithin30Seconds_when_postTransaction_should_returnHttpCREATED() throws Exception {

        String jsonRequest = "{ \"amount\" : 12.1123, " +
                "\"timestamp\" : \""+LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)+"\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isCreated());

    }

    @Order(5)
    @Test
    void given_validRequest_when_getStatistics_should_returnHttpOK() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/statistics")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sum").value(12.11))
                .andExpect(MockMvcResultMatchers.jsonPath("$.avg").value(12.11))
                .andExpect(MockMvcResultMatchers.jsonPath("$.min").value(12.11))
                .andExpect(MockMvcResultMatchers.jsonPath("$.max").value(12.11))
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1));

    }

    @Order(6)
    @Test
    void given_validRequest_when_clearTransactions_should_returnHttpNOCONTENT() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/transaction")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

    }

    @Order(7)
    @Test
    void given_validRequest_when_getStatisticsAfterClearing_should_returnHttpOK() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/statistics")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sum").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.avg").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.min").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.max").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(0));

    }
}