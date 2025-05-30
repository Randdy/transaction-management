package com.hsbc.wpb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.wpb.exception.TransactionException;
import com.hsbc.wpb.model.Transaction;
import com.hsbc.wpb.model.TransactionRequest;
import com.hsbc.wpb.model.TransactionStatus;
import com.hsbc.wpb.model.TransactionType;
import com.hsbc.wpb.repository.InMemoryRepository;
import com.hsbc.wpb.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private InMemoryRepository repository;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new Transaction(
            1L,
            "123",
            "456",
            new BigDecimal("100.00"),
            TransactionType.TRANSFER,
            LocalDateTime.now(),
            TransactionStatus.COMPLETED
        );
    }

    @Test
    public void testCreateTransaction_Success() throws Exception {
        // 创建一个 TransactionRequest 对象
        TransactionRequest request = new TransactionRequest(
            "123",
            "456",
            new BigDecimal("100.00"),
            TransactionType.TRANSFER
        );

        // 设置 mock 行为
        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(sampleTransaction);

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateTransaction_InvalidAmount() throws Exception {
        // 创建一个无效金额的请求
        TransactionRequest request = new TransactionRequest(
            "123",
            "456",
            new BigDecimal("0.00"),
            TransactionType.TRANSFER
        );

        when(transactionService.createTransaction(any(Transaction.class)))
            .thenThrow(new TransactionException("Validation failed"));

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    public void testGetTransaction_Success() throws Exception {
        when(transactionService.getTransaction(anyLong())).thenReturn(sampleTransaction);

        mockMvc.perform(get("/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    public void testGetAllTransactions_Success() throws Exception {
        List<Transaction> transactions = Arrays.asList(
            sampleTransaction,
            new Transaction(2L, "789", "012", new BigDecimal("200.00"), 
                          TransactionType.DEPOSIT, LocalDateTime.now(), TransactionStatus.COMPLETED)
        );
        
        Page<Transaction> transactionPage = new PageImpl<>(transactions);
        when(transactionService.getAllTransactions(any(PageRequest.class))).thenReturn(transactionPage);

        mockMvc.perform(get("/transactions")
                .param("page", "0")
                .param("size", "2")
                .param("sortBy", "timestamp")
                .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));
    }

    @Test
    public void testDeleteTransaction_Success() throws Exception {
        // 设置 mock 行为
        when(transactionService.getTransaction(anyLong())).thenReturn(sampleTransaction);
        doNothing().when(transactionService).deleteTransaction(anyLong());

        mockMvc.perform(delete("/transactions/1"))
                .andExpect(status().isNoContent());

        verify(transactionService, times(1)).deleteTransaction(1L);
    }

    @Test
    public void testDeleteTransaction_NotFound() throws Exception {
        when(transactionService.getTransaction(anyLong())).thenReturn(null);

        mockMvc.perform(delete("/transactions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteTransaction_TooOld() throws Exception {
        Transaction oldTransaction = new Transaction(
            1L,
            "123",
            "456",
            new BigDecimal("100.00"),
            TransactionType.TRANSFER,
            LocalDateTime.now().minusHours(25),
            TransactionStatus.COMPLETED
        );
        
        when(transactionService.getTransaction(anyLong())).thenReturn(oldTransaction);
        doThrow(new TransactionException("Cannot delete transactions older than 24 hours"))
            .when(transactionService).deleteTransaction(anyLong());

        mockMvc.perform(delete("/transactions/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTransaction_InvalidType() throws Exception {
        // 创建一个无效类型的请求
        String invalidRequest = "{\"fromAccount\":\"123\",\"toAccount\":\"456\",\"amount\":100.00,\"type\":\"INVALID_TYPE\"}";

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTransaction_MissingRequiredFields() throws Exception {
        // 创建一个缺少必要字段的请求
        String invalidRequest = "{\"amount\":100.00,\"type\":\"TRANSFER\"}";

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.fromAccount").value("From account cannot be null"))
                .andExpect(jsonPath("$.errors.toAccount").value("To account cannot be null"))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    public void testCreateTransaction_InvalidRequest() throws Exception {
        // 创建一个无效的请求（缺少必要字段）
        String invalidRequest = "{\"amount\":100.00,\"type\":\"TRANSFER\"}";

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.fromAccount").value("From account cannot be null"))
                .andExpect(jsonPath("$.errors.toAccount").value("To account cannot be null"))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
