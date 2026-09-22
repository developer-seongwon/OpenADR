package com.avob.openadr.server.common.vtn.models.vengroup;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface VenGroupDao extends JpaRepository<VenGroup, Long> {

    public VenGroup findOneByName(String name);

    public List<VenGroup> findByNameIn(List<String> name);

}
