package com.andrew.hdss.api;

import com.andrew.hdss.api.swagger.AppDownloadApi;
import com.andrew.hdss.services.AppDownloadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
@Tag(name = "HDSS APK Download endpoint")
public class AppDownloadController implements AppDownloadApi {

    private final AppDownloadService appDownloadService;

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadApk() {
        Resource apk = appDownloadService.getApk();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.android.package-archive"
                ))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"hdss.apk\""
                )
                .body(apk);
    }
}