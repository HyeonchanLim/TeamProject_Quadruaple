package com.green.project_quadruaple.booking.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Schema
public class BookingListGetRes {
    private long bookingId;
    private String checkIn;
    private String checkOut;
    private long menuId;
    private int menuPrice;
    private String strfTitle;

}
