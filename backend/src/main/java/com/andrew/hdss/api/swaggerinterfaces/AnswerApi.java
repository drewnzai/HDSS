package com.andrew.hdss.api.swaggerinterfaces;

import com.andrew.hdss.dtos.AnswerDto;
import com.andrew.hdss.dtos.SubmitAnswerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AnswerApi {

    @Operation(summary = "Get answers")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Answers retrieved successfully"
                    )
            }
    )
    List<AnswerDto> getAnswers(@PathVariable Long formResponseId);

    @Operation(summary = "Submit an answer to a question")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Answer saved successfully"
                    )
            }
    )
    AnswerDto submitAnswer(
            @PathVariable Long formResponseId,
            @RequestBody SubmitAnswerRequest request
    );
}
