package com.ffxiv.linerobot.repository;

import com.ffxiv.linerobot.entity.LineUserProfile;
import com.ffxiv.linerobot.entity.pk.LineUserProfilePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineUserProfileRepository extends JpaRepository<LineUserProfile, LineUserProfilePrimaryKey> {
}
