package com.ticketing.event.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String zone;

    @Column(name = "row_num", nullable = false)
    private String rowNum;

    @Column(nullable = false)
    private int number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatGrade grade;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Version
    private int version;

    public static Seat create(Long eventId, String zone, String rowNum, int number,
                               SeatGrade grade, int price) {
        Seat seat = new Seat();
        seat.eventId = eventId;
        seat.zone = zone;
        seat.rowNum = rowNum;
        seat.number = number;
        seat.grade = grade;
        seat.price = price;
        seat.status = SeatStatus.AVAILABLE;
        return seat;
    }

    public void hold() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("AVAILABLE 상태의 좌석만 선점할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = SeatStatus.HELD;
    }

    public void release() {
        if (this.status == SeatStatus.BOOKED) return;
        this.status = SeatStatus.AVAILABLE;
    }

    public void book() {
        if (this.status != SeatStatus.HELD) {
            throw new IllegalStateException("HELD 상태의 좌석만 확정할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = SeatStatus.BOOKED;
    }

    public void cancel() {
        this.status = SeatStatus.AVAILABLE;
    }
}
