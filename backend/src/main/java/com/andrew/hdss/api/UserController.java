package com.andrew.hdss.api;

import com.andrew.hdss.api.swaggerinterfaces.UserApi;
import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.dtos.UserCreationRequest;
import com.andrew.hdss.dtos.UserDto;
import com.andrew.hdss.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Tag(name = "User Management")
public class UserController implements UserApi {

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

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable(name = "username") String username){
        return new ResponseEntity<>(userService.getUser(username), HttpStatus.OK);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserCreationRequest request) throws Exception {
        userService.createUser(request);
        return new ResponseEntity<>("User created successfully",
                HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@RequestBody UserDto userDto) throws Exception {
        userService.deleteUser(userDto);
        return new ResponseEntity<>("User deleted successfully",
                HttpStatus.OK);
    }
}
