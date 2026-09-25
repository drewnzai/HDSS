package com.andrew.hdss.services;

import com.andrew.hdss.dtos.ChoiceDto;
import com.andrew.hdss.dtos.CreateChoiceRequest;
import com.andrew.hdss.dtos.UpdateChoiceRequest;
import com.andrew.hdss.models.Choice;
import com.andrew.hdss.repositories.ChoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class ChoiceService {

    private final ChoiceRepository choiceRepository;

    @Transactional(readOnly = true)
    public List<ChoiceDto> getByListName(String listName) {
        return choiceRepository.findByListName(listName).stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(ChoiceDto::orderIndex))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getListNames() {
        return choiceRepository.findDistinctListNames();
    }

    @Transactional
    public ChoiceDto create(CreateChoiceRequest request) {
        if (choiceRepository.existsByListNameAndName(request.listName(), request.name())) {
            throw new IllegalArgumentException(
                    "Choice \"" + request.name() + "\" already exists in list \"" + request.listName() + "\""
            );
        }

        Choice choice = Choice.builder()
                .listName(request.listName())
                .name(request.name())
                .label(request.label())
                .orderIndex(request.orderIndex())
                .build();

        return toDto(choiceRepository.save(choice));
    }

    @Transactional
    public ChoiceDto update(Long id, UpdateChoiceRequest request) {
        Choice choice = choiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Choice not found: " + id));

        boolean nameChanged = !choice.getName().equals(request.name());
        if (nameChanged && choiceRepository.existsByListNameAndNameAndIdNot(
                choice.getListName(), request.name(), id)) {
            throw new IllegalArgumentException(
                    "Choice \"" + request.name() + "\" already exists in list \"" + choice.getListName() + "\""
            );
        }

        choice.setName(request.name());
        choice.setLabel(request.label());
        choice.setOrderIndex(request.orderIndex());

        return toDto(choiceRepository.save(choice));
    }

    @Transactional
    public void delete(Long id) {
        if (!choiceRepository.existsById(id)) {
            throw new IllegalArgumentException("Choice not found: " + id);
        }

        choiceRepository.deleteById(id);
    }

    private ChoiceDto toDto(Choice choice) {
        return new ChoiceDto(
                choice.getId(),
                choice.getListName(),
                choice.getName(),
                choice.getLabel(),
                choice.getOrderIndex()
        );
    }
}
