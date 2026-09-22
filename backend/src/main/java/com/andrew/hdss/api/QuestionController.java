package com.andrew.hdss.api;

import com.andrew.hdss.api.swagger.QuestionApi;
import com.andrew.hdss.dtos.CreateQuestionRequest;
import com.andrew.hdss.dtos.QuestionDto;
import com.andrew.hdss.dtos.ReorderQuestionsRequest;
import com.andrew.hdss.dtos.UpdateQuestionRequest;
import com.andrew.hdss.services.QuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms/{formId}/questions")
@RequiredArgsConstructor
@Tag(name = "Question Management")
public class QuestionController implements QuestionApi {

    private final QuestionService questionService;

    @GetMapping
    public List<QuestionDto> getQuestions(@PathVariable Long formId) {
        return questionService.getQuestions(formId);
    }

    @PostMapping
    public QuestionDto addQuestion(
            @PathVariable Long formId,
            @RequestBody CreateQuestionRequest request
    ) {
        return questionService.addQuestion(formId, request);
    }

    @PutMapping("/{questionId}")
    public QuestionDto updateQuestion(
            @PathVariable Long formId,
            @PathVariable Long questionId,
            @RequestBody UpdateQuestionRequest request
    ) {
        return questionService.updateQuestion(formId, questionId, request);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long formId, @PathVariable Long questionId) {
        questionService.deleteQuestion(formId, questionId);
        return new ResponseEntity<>("Question deleted successfully",
                HttpStatus.OK);
    }

    @PutMapping("/reorder")
    public List<QuestionDto> reorderQuestions(
            @PathVariable Long formId,
            @RequestBody ReorderQuestionsRequest request
    ) {
        return questionService.reorderQuestions(formId, request);
    }
}