package com.andrew.hdss.api;

import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.dtos.UserDto;
import com.andrew.hdss.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BatchResponse<UserDto>> getUsers(@RequestParam(name = "page", required = false) Integer page,
                                                           @RequestParam(name = "size", required = false) Integer size){
        ResourceRequest resourceRequest = new ResourceRequest();

        if(page != null){
            resourceRequest.setPage(page);
        }

        if(size != null){
            resourceRequest.setSize(size);
        }

        return new ResponseEntity<>(userService.getUsers(resourceRequest), HttpStatus.OK);
    }
}
