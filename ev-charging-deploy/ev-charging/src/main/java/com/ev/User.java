package com.ev;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
@Entity @Table(name="users")
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String name; @Column(unique=true) public String email;
 @JsonIgnore public String passwordHash; public String role; // USER or ADMIN
}
