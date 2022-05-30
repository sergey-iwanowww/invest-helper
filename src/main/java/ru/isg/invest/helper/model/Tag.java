package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Created by s.ivanov on 11.01.2022.
 */
@Entity
@Table(name = "tags")
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class Tag {

    @Id
    private UUID id = UUID.randomUUID();

    @Setter
    private String name;
}
