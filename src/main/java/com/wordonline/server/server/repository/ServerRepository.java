package com.wordonline.server.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wordonline.server.server.entity.Server;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

}
