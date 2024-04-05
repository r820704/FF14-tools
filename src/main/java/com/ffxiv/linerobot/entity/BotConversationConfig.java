package com.ffxiv.linerobot.entity;


import com.ffxiv.linerobot.entity.pk.BotConversationConfigPrimaryKey;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "bot_conversation_config")
@IdClass(BotConversationConfigPrimaryKey.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class BotConversationConfig {

    @Id
    private String topic;

    @Column(name = "parent_id")
    private String parentId;

    @Id
    private String conversationId;

    @Column(name = "detail")
    private String detail;
}
