package com.ff14.linerobot.repository;

import com.ff14.linerobot.entity.LineUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineUserRepository extends JpaRepository<LineUser, String> {
}
