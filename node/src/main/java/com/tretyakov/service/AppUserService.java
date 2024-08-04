package com.tretyakov.service;

import com.tretyakov.entity.AppUser;

public interface AppUserService {
    String registerUser(AppUser appUser);

    String setEmail(AppUser appUser, String email);
}
