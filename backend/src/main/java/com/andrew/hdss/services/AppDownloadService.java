package com.andrew.hdss.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AppDownloadService {

    private final Path apkPath = Paths.get("downloads", "hdss.apk");

    public Resource getApk() {
        try {
            Resource resource = new UrlResource(apkPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("APK not found");
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not load APK", e);
        }
    }
}