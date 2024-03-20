
package com.ff14.linerobot.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "line_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class LineUser {

    @Id
    @Column(name = "USERID")
    private String userId;

    @Column(name = "USERNAME")
    private String userName;
}
