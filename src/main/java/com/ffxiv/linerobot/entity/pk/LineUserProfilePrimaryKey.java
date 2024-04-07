package com.ffxiv.linerobot.entity.pk;

import java.io.Serializable;
import javax.persistence.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class LineUserProfilePrimaryKey implements Serializable {

  @Column(name = "channel")
  private String channel;

  @Column(name = "user_id")
  private String userId;
}
