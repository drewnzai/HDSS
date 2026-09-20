package com.andrew.hdss.api.swaggerinterfaces;

import com.andrew.hdss.dtos.CreateQuestionRequest;
import com.andrew.hdss.dtos.QuestionDto;
import com.andrew.hdss.dtos.ReorderQuestionsRequest;
import com.andrew.hdss.dtos.UpdateQuestionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface QuestionApi {

    @Operation(
            description = "Get the questions within a given form"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Questions retrieved successfully"
                    )
            }
    )
    List<QuestionDto> getQuestions(@PathVariable Long formId);

    @Operation(
            description = "Add a question to a form"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Question added successfully"
                    )
            }
    )
    QuestionDto addQuestion(
            @PathVariable Long formId,
            @RequestBody CreateQuestionRequest request
    );

    @Operation(
            summary = "Update a question",
            description = "Update an existing question in a form if the form has not received form responses yet"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Question updated successfully"
                    )
            }
    )
    QuestionDto updateQuestion(
            @PathVariable Long formId,
            @PathVariable Long questionId,
            @RequestBody UpdateQuestionRequest request
    );

    @Operation(
            summary = "Delete a question",
            description = "Delete an existing question in a form if the form has not received form responses yet"
    )
    void deleteQuestion(@PathVariable Long formId, @PathVariable Long questionId);

    @Operation(
            summary = "Reorder questions",
            description = "Can only reorder questions in a form if the form has not received form responses yet"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Questions reordered successfully"
                    )
            }
    )
    List<QuestionDto> reorderQuestions(
            @PathVariable Long formId,
            @RequestBody ReorderQuestionsRequest request
    );
}
