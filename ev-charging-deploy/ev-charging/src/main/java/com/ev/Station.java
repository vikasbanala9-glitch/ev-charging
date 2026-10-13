package com.ev;
import jakarta.persistence.*;
@Entity
public class Station {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String name, location; public double lat, lng;
}
