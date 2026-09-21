package com.andrew.hdss.api;

import com.andrew.hdss.api.swagger.AnswerApi;
import com.andrew.hdss.dtos.AnswerDto;
import com.andrew.hdss.dtos.SubmitAnswerRequest;
import com.andrew.hdss.services.AnswerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/form-responses/{formResponseId}/answers")
@RequiredArgsConstructor
@Tag(name = "Answer Management")
public class AnswerController implements AnswerApi {

    private final AnswerService answerService;

    @GetMapping
    public List<AnswerDto> getAnswers(@PathVariable Long formResponseId) {
        return answerService.getAnswers(formResponseId);
    }

    @PostMapping
    public AnswerDto submitAnswer(
            @PathVariable Long formResponseId,
            @RequestBody SubmitAnswerRequest request
    ) {
        return answerService.submitAnswer(formResponseId, request);
    }
}
