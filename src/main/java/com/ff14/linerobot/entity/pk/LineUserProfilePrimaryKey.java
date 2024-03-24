package com.ff14.linerobot.entity.pk;

import javax.persistence.Column;
import java.io.Serializable;

public class LineUserProfilePrimaryKey implements Serializable {

    @Column(name = "channel")
    private String channel;

    @Column(name = "user_id")
    private String userId;

}
