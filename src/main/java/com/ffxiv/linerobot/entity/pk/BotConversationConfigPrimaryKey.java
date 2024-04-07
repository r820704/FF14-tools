package com.ffxiv.linerobot.entity.pk;

import java.io.Serializable;
import javax.persistence.Column;
import lombok.*;

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
