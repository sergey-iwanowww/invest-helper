package ru.isg.invest.helper.dto;

import lombok.Value;

import java.util.UUID;

/**
 * Created by s.ivanov on 09.06.2022.
 */
@Value
public class IdeaFinishingEvent {
    UUID ideaId;
}
