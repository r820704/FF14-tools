package com.ffxiv.linerobot.entity.pk;

import lombok.*;

import javax.persistence.Column;
import java.io.Serializable;

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
