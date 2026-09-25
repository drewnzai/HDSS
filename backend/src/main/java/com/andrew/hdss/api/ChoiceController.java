package com.andrew.hdss.api;

import com.andrew.hdss.api.swagger.ChoiceApi;
import com.andrew.hdss.dtos.ChoiceDto;
import com.andrew.hdss.dtos.CreateChoiceRequest;
import com.andrew.hdss.dtos.UpdateChoiceRequest;
import com.andrew.hdss.services.ChoiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/choices")
@RequiredArgsConstructor
@Tag(name = "Choice Management")
public class ChoiceController implements ChoiceApi {

    private final ChoiceService choiceService;

    @GetMapping("/list-names")
    public List<String> getListNames() {
        return choiceService.getListNames();
    }

    @GetMapping("/list/{listName}")
    public List<ChoiceDto> getByListName(@PathVariable String listName) {
        return choiceService.getByListName(listName);
    }

    @PostMapping
    public ChoiceDto create(@RequestBody CreateChoiceRequest request) {
        return choiceService.create(request);
    }

    @PutMapping("/{id}")
    public ChoiceDto update(@PathVariable Long id, @RequestBody UpdateChoiceRequest request) {
        return choiceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        choiceService.delete(id);
        return new ResponseEntity<>("Choice deleted successfully",
                HttpStatus.OK);
    }
}
