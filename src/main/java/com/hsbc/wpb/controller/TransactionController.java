/**
 * @author Edinw Zhang
 * @date 2025/05/30
 */
package com.hsbc.wpb.controller;

import com.hsbc.wpb.exception.DuplicateTransactionException;
import com.hsbc.wpb.exception.TransactionException;
import com.hsbc.wpb.model.Transaction;
import com.hsbc.wpb.model.TransactionType;
import com.hsbc.wpb.model.TransactionRequest;
import com.hsbc.wpb.model.TransactionUpdateRequest;
import com.hsbc.wpb.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing bank transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
        summary = "Create a new transaction",
        description = "Creates a new transaction with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transaction created successfully",
            content = @Content(schema = @Schema(implementation = Transaction.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid transaction data provided"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Duplicate transaction detected"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
        @Parameter(description = "Transaction details", required = true)
        @Valid @RequestBody TransactionRequest request
    ) {
        try {
            Transaction transaction = Transaction.create(
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getType()
            );

            return ResponseEntity.ok(transactionService.createTransaction(transaction));
        } catch (DuplicateTransactionException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (TransactionException e) {
            if (e.getMessage().contains("does not exist")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Get all transactions with pagination",
        description = "Retrieves a paginated list of all transactions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved transactions",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    @GetMapping
    public ResponseEntity<Page<Transaction>> getAllTransactions(
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "10") int size,
        
        @Parameter(description = "Sort field")
        @RequestParam(defaultValue = "timestamp") String sortBy,
        
        @Parameter(description = "Sort direction")
        @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(transactionService.getAllTransactions(pageRequest));
    }

    @Operation(
        summary = "Search transactions",
        description = "Search transactions with various criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved transactions",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    @GetMapping("/search")
    public ResponseEntity<Page<Transaction>> searchTransactions(
        @Parameter(description = "From account number")
        @RequestParam(required = false) String fromAccount,
        
        @Parameter(description = "To account number")
        @RequestParam(required = false) String toAccount,
        
        @Parameter(description = "Transaction type")
        @RequestParam(required = false) TransactionType type,
        
        @Parameter(description = "Minimum amount")
        @RequestParam(required = false) BigDecimal minAmount,
        
        @Parameter(description = "Maximum amount")
        @RequestParam(required = false) BigDecimal maxAmount,
        
        @Parameter(description = "Start date")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        
        @Parameter(description = "End date")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "10") int size,
        
        @Parameter(description = "Sort field")
        @RequestParam(defaultValue = "timestamp") String sortBy,
        
        @Parameter(description = "Sort direction")
        @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return ResponseEntity.ok(transactionService.searchTransactions(
            fromAccount,
            toAccount,
            type,
            minAmount,
            maxAmount,
            startDate,
            endDate,
            pageRequest
        ));
    }

    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieves a specific transaction by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the transaction",
            content = @Content(schema = @Schema(implementation = Transaction.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Transaction not found"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(
        @Parameter(description = "Transaction ID", required = true)
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @Operation(
        summary = "Modify transaction",
        description = "Modifies an existing transaction by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transaction modified successfully",
            content = @Content(schema = @Schema(implementation = Transaction.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid transaction data provided"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Transaction not found"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> modifyTransaction(
        @Parameter(description = "Transaction ID", required = true)
        @PathVariable Long id,
        @Parameter(description = "Updated transaction details", required = true)
        @Valid @RequestBody TransactionUpdateRequest request
    ) {
        Transaction transaction = Transaction.create(
            request.getFromAccount(),
            request.getToAccount(),
            request.getAmount(),
            request.getType()
        );
        return ResponseEntity.ok(transactionService.modifyTransaction(id, transaction));
    }

    @Operation(
        summary = "Delete transaction",
        description = "Deletes a specific transaction by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Transaction deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Transaction not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot delete transaction (e.g., too old)"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
        @Parameter(description = "Transaction ID", required = true)
        @PathVariable Long id
    ) {
        try {
            Transaction transaction = transactionService.getTransaction(id);
            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (TransactionException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("errors", errors);
        response.put("message", "Validation failed");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<Map<String, String>> handleTransactionException(TransactionException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        
        HttpStatus status = ex.getMessage().contains("not found") ? 
            HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            
        return ResponseEntity.status(status).body(response);
    }
}
