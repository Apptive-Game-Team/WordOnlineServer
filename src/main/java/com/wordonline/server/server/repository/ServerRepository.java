package com.wordonline.server.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wordonline.server.server.entity.Server;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    Optional<Server> findByDomainAndPort(String domain, Integer port);
}
