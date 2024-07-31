package com.tretyakov.service;

import com.tretyakov.entity.AppDocument;
import com.tretyakov.entity.AppPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);

    AppPhoto processPhoto(Message telegramMessage);
}
