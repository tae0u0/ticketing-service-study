package com.ticketing.event.dto;

import com.ticketing.event.domain.SeatGrade;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateSeatsRequest {

    @NotNull
    private List<SeatBlock> blocks;

    @Getter
    public static class SeatBlock {

        @NotBlank
        private String zone;

        @NotBlank
        private String rowNum;

        @NotNull
        private SeatGrade grade;

        @Min(1)
        private int price;

        @Min(1)
        private int count;
    }
}
