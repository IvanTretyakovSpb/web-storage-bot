package com.tretyakov.service;

import com.tretyakov.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
