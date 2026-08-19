package com.andrew.hdss.services;

import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.IndividualDto;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.models.Individual;
import com.andrew.hdss.repositories.IndividualRepository;
import com.andrew.hdss.utils.PaginationRequest;
import com.andrew.hdss.utils.Util;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class IndividualService {
    private final IndividualRepository individualRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "individuals", key = "#resourceRequest.page + '-' + #resourceRequest.size")
    public BatchResponse<IndividualDto> getIndividuals(ResourceRequest resourceRequest) {
        Pageable pageable = Util.getPageable(
                PaginationRequest.builder()
                        .page(resourceRequest.getPage())
                        .size(resourceRequest.getSize())
                        .build()
        );

        Page<Individual> page = individualRepository.findAll(pageable);

        BatchResponse<IndividualDto> response = new BatchResponse<>();
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setData(page.stream().map(IndividualDto::from).toList());
        return response;
    }
}
