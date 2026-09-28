package com.avob.openadr.server.common.vtn.service;

import com.avob.openadr.server.common.vtn.AbstractVtnTest;


import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.avob.openadr.server.common.vtn.ApplicationTest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationTest.class })
@WebAppConfiguration
@ActiveProfiles("test")
public class VenRequestCountServiceTest extends AbstractVtnTest {

    @Resource
    private VenRequestCountService venRequestCountService;

    @Test
    public void getAndIncreaseTest() {
        String venId = "ven1";
        Long andIncrease = venRequestCountService.getAndIncrease(venId);
		assertEquals(Long.valueOf(0), andIncrease);
        andIncrease = venRequestCountService.getAndIncrease(venId);
        assertEquals(Long.valueOf(1), andIncrease);
        andIncrease = venRequestCountService.getAndIncrease(venId);
        assertEquals(Long.valueOf(2), andIncrease);

    }
}
