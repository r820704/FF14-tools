package com.ffxiv.linerobot.entity.pk;

import lombok.*;

import javax.persistence.Column;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class BotConversationConfigPrimaryKey implements Serializable {

    @Column(name = "topic")
    private String topic;

    @Column(name = "conversation_id")
    private String conversationId;
}
