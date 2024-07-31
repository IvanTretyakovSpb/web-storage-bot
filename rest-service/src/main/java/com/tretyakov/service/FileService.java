package com.tretyakov.service;

import com.tretyakov.entity.AppDocument;
import com.tretyakov.entity.AppPhoto;
import com.tretyakov.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String id);

    AppPhoto getPhoto(String id);

    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
