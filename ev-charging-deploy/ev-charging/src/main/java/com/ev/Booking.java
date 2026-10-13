package com.ev;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
public class Booking {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long userId, chargerId; public LocalDateTime startTime, endTime;
 public String status = "BOOKED"; // BOOKED, CHARGING, COMPLETED, CANCELLED
 public double energyKwh, amount; public boolean paid;
}
