package io.rashed.finance.api.dto.transaction;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Regression test for a real 500 hit via the UI: Java records use their
 * canonical (all-args) constructor for Jackson deserialization, so a JSON
 * property that is simply absent (not just explicitly null) is still
 * supplied as null to that constructor. For a primitive-typed component
 * that throws — {@code startsNewSalaryCycle} must stay boxed.
 */
class CreateTransactionRequestTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void deserializes_whenStartsNewSalaryCycleIsOmittedEntirely() throws Exception {

        String json = """
                {
                  "transactionType": "EXPENSE",
                  "transactionDate": "2026-07-25",
                  "amount": 400,
                  "description": "Groceries",
                  "categoryId": "909388d5-b3f6-43e6-96ed-73154785dcd0",
                  "fromAccountId": "66e1d69d-0d78-4c58-9bdb-cd2d6b0581da",
                  "salaryCycleId": "86c836e4-fca3-4c3c-b86b-15a60844744f"
                }
                """;

        CreateTransactionRequest request = objectMapper.readValue(json, CreateTransactionRequest.class);

        assertEquals("Groceries", request.description());
        assertFalse(request.startsNewSalaryCycle());
    }

    @Test
    void deserializes_whenStartsNewSalaryCycleIsExplicitNull() throws Exception {

        String json = """
                {
                  "transactionType": "INCOME",
                  "transactionDate": "2026-07-25",
                  "amount": 400,
                  "toAccountId": "66e1d69d-0d78-4c58-9bdb-cd2d6b0581da",
                  "categoryId": "909388d5-b3f6-43e6-96ed-73154785dcd0",
                  "startsNewSalaryCycle": null
                }
                """;

        CreateTransactionRequest request = objectMapper.readValue(json, CreateTransactionRequest.class);

        assertFalse(request.startsNewSalaryCycle());
    }
}
