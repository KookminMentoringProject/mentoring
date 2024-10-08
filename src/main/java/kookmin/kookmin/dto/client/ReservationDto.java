package kookmin.kookmin.dto.client;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {
    private String reservationId;
    private int askType;
    private String askContent;
    private Date desiredDate1;
    private Date desiredDate2;
    private Date reservationDate;
    private int reservationStatus;
    private String position;
    private String userId;
    private String mentoId;
    private String planTitle;
    private int reviewScore;
    private String reviewContent;
    private Date reviewDate;
    private Date refundTime;
    private String refundBankName;
    private String refundBankNum;
}
