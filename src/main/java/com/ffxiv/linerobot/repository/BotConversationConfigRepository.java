package com.ffxiv.linerobot.repository;

import com.ffxiv.linerobot.entity.BotConversationConfig;
import com.ffxiv.linerobot.entity.pk.BotConversationConfigPrimaryKey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotConversationConfigRepository
    extends JpaRepository<BotConversationConfig, BotConversationConfigPrimaryKey> {

  List<BotConversationConfig> findByTopicAndParentId(String topic, String parentId);

  List<BotConversationConfig> findByParentId(String parentId);
}
