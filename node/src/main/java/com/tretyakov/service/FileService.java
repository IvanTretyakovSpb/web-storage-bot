package com.tretyakov.service;

import com.tretyakov.entity.AppDocument;
import com.tretyakov.entity.AppPhoto;
import com.tretyakov.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);

    AppPhoto processPhoto(Message telegramMessage);

    String generateLink(Long docId, LinkType linkType);
}
