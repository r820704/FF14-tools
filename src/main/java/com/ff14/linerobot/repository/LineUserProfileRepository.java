package com.ff14.linerobot.repository;

import com.ff14.linerobot.entity.LineUserProfile;
import com.ff14.linerobot.entity.pk.LineUserProfilePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineUserProfileRepository extends JpaRepository<LineUserProfile, LineUserProfilePrimaryKey> {
}
