package com.ev;
import jakarta.persistence.*;
@Entity
public class Charger {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long stationId; public String type; // AC or DC
 public double powerKw, ratePerKwh; public String status = "AVAILABLE"; // AVAILABLE, OCCUPIED, OFFLINE
}
