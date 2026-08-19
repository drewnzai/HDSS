package com.andrew.hdss.services;

import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.HouseholdDto;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.models.Household;
import com.andrew.hdss.repositories.HouseholdRepository;
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
public class HouseholdService {
    private final HouseholdRepository householdRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "households", key = "#resourceRequest.page + '-' + #resourceRequest.size")
    public BatchResponse<HouseholdDto> getHouseholds(ResourceRequest resourceRequest) {
        Pageable pageable = Util.getPageable(
                PaginationRequest.builder()
                        .page(resourceRequest.getPage())
                        .size(resourceRequest.getSize())
                        .build()
        );

        Page<Household> page = householdRepository.findAll(pageable);

        BatchResponse<HouseholdDto> response = new BatchResponse<>();
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setData(page.stream().map(HouseholdDto::from).toList());
        return response;
    }

}
