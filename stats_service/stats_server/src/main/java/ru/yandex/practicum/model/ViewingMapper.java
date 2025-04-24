package ru.yandex.practicum.model;

import ru.yandex.practicum.ViewingRequestDto;

public class ViewingMapper {

    public static Viewing mapToViewing(ViewingRequestDto viewingRequestDto) {
        return Viewing.builder()
                .app(viewingRequestDto.getApp())
                .uri(viewingRequestDto.getUri())
                .ip(viewingRequestDto.getIp())
                .timestamp(viewingRequestDto.getTimestamp())
                .build();
    }
}
