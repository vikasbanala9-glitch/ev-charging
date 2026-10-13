package com.ev;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
interface UserRepo extends JpaRepository<User,Long>{ User findByEmail(String e); }
interface StationRepo extends JpaRepository<Station,Long>{}
interface ChargerRepo extends JpaRepository<Charger,Long>{}
interface BookingRepo extends JpaRepository<Booking,Long>{
 List<Booking> findByUserIdOrderByStartTimeDesc(Long u);
 @Query("select count(b) from Booking b where b.chargerId=:c and b.status in ('BOOKED','CHARGING') and b.startTime<:e and b.endTime>:s")
 long overlaps(@Param("c") Long c,@Param("s") LocalDateTime s,@Param("e") LocalDateTime e);
}
