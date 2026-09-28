package com.avob.openadr.server.common.vtn.models.user;

import com.avob.openadr.server.common.vtn.AbstractVtnTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.avob.openadr.server.common.vtn.ApplicationTest;
import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.common.vtn.service.OadrUserService;
import com.avob.openadr.server.common.vtn.service.VenService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationTest.class })
@WebAppConfiguration
@ActiveProfiles("test")
public class AbstractUserDaoTest extends AbstractVtnTest {

    @Resource
    private VenService venService;

    @Resource
    private OadrUserService userService;

    @Resource
    private AbstractUserDao abstractUserdao;

    @Test
    public void test() {
        Ven ven = venService.prepare("ven1", "password");
        venService.save(ven);

        OadrUser user = userService.prepare("user1", "password");
        user.setEmail("mail@mail.com");
        userService.save(user);

        long count = abstractUserdao.count();
        assertEquals(2L, count);

        AbstractUser findOne = abstractUserdao.findById(ven.getId()).get();
        assertTrue(findOne instanceof Ven);
        assertFalse(findOne instanceof OadrUser);

        findOne = abstractUserdao.findOneByUsername(ven.getUsername());
        assertTrue(findOne instanceof Ven);
        assertFalse(findOne instanceof OadrUser);

        findOne = abstractUserdao.findById(user.getId()).get();
        assertFalse(findOne instanceof Ven);
        assertTrue(findOne instanceof OadrUser);

        findOne = abstractUserdao.findOneByUsername(user.getUsername());
        assertFalse(findOne instanceof Ven);
        assertTrue(findOne instanceof OadrUser);

        venService.delete(ven);
        count = abstractUserdao.count();
        assertEquals(1L, count);

        findOne = abstractUserdao.findOneByUsername(ven.getUsername());
        assertNull(findOne);

    }
}
