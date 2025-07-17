package com.wordonline.server.game.service;

import com.wordonline.server.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MmrService {

    private final UserRepository userRepository;

    // TODO - Update Mmr
    //  with
    //  userRepository.setMmr(userId, mmr)
    //  userRepository.getMmr(userId)
}
