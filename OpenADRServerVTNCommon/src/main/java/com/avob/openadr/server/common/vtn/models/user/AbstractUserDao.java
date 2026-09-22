package com.avob.openadr.server.common.vtn.models.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AbstractUserDao extends JpaRepository<AbstractUser, Long> {

    public List<AbstractUser> findByUsernameIn(List<String> username);

    public AbstractUser findOneByUsername(String username);
}
