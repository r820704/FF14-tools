package com.ffxiv.linerobot.repository;

import com.ffxiv.linerobot.entity.BotConversationConfig;
import com.ffxiv.linerobot.entity.pk.BotConversationConfigPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotConversationConfigRepository extends JpaRepository<BotConversationConfig, BotConversationConfigPrimaryKey> {

    List<BotConversationConfig> findByTopicAndParentId(String topic, String parentId);

    List<BotConversationConfig> findByParentId(String parentId);
}
