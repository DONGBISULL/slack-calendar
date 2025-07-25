package com.demo.slackcalendar.commons.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultSlackCacheService {

    @Lazy
    @Autowired
    private VaultPathCacheService self;

    private final VaultService service;


}
