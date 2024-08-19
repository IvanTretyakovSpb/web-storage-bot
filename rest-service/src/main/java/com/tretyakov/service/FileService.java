package com.tretyakov.service;

import com.tretyakov.entity.AppDocument;
import com.tretyakov.entity.AppPhoto;

public interface FileService {
    AppDocument getDocument(String id);

    AppPhoto getPhoto(String id);
}
