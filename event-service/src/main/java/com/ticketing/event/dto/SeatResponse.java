package com.ticketing.event.dto;

import com.ticketing.event.domain.Seat;
import lombok.Getter;

@Getter
public class SeatResponse {
    private final Long id;
    private final Long eventId;
    private final String zone;
    private final String rowNum;
    private final int number;
    private final String grade;
    private final int price;
    private final String status;

    public SeatResponse(Seat seat) {
        this.id = seat.getId();
        this.eventId = seat.getEventId();
        this.zone = seat.getZone();
        this.rowNum = seat.getRowNum();
        this.number = seat.getNumber();
        this.grade = seat.getGrade().name();
        this.price = seat.getPrice();
        this.status = seat.getStatus().name();
    }
}
