package ru.isg.invest.helper.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

import static javax.persistence.EnumType.STRING;

/**
 * Created by s.ivanov on 13.11.2021.
 */
@Entity
@Table(name = "portfolios")
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class Portfolio {

    @Id
    private UUID id = UUID.randomUUID();

    @Setter
    private String name;

    @Setter
    @Enumerated(STRING)
    private Brokers broker;

    @Setter
    private String externalId;
}
