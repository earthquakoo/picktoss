package com.picktoss.picktossserver.domain.star.facade;

import com.picktoss.picktossserver.domain.star.service.StarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StarFacade {

    private final StarService starService;
}
