package com.ffxiv.linerobot.entity;

import com.ffxiv.linerobot.entity.pk.LineUserProfilePrimaryKey;
import javax.persistence.*;
import lombok.*;

/*
https://developers.line.biz/en/docs/messaging-api/getting-user-ids/
每個帳號的User ID在同一個provider底下都是唯一值
不同provider時同個帳號會有不同的User Id
例: A的user Id在Messaging API Channel跟LINE Login channel會是不同的值
*/
@Entity
@Table(name = "line_user_profile")
@IdClass(LineUserProfilePrimaryKey.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class LineUserProfile {

  @Id private String channel;

  @Id private String userId;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "language")
  private String language;

  @Column(name = "picture_url")
  private String pictureUrl;

  @Column(name = "status_message")
  private String statusMessage;
}
