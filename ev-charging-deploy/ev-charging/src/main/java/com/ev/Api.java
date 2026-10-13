package com.ev;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api")
public class Api {
 final UserRepo users; final StationRepo stations; final ChargerRepo chargers; final BookingRepo bookings;
 Api(UserRepo u,StationRepo s,ChargerRepo c,BookingRepo b){users=u;stations=s;chargers=c;bookings=b;}
 static String hash(String s){ try{ return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes())); }catch(Exception e){ throw new RuntimeException(e);} }
 static ResponseStatusException err(HttpStatus h,String m){ return new ResponseStatusException(h,m); }
 User auth(Long id){ return id==null?null:users.findById(id).orElse(null); }
 User need(Long id){ User u=auth(id); if(u==null) throw err(HttpStatus.UNAUTHORIZED,"Please log in"); return u; }
 void admin(Long id){ if(!"ADMIN".equals(need(id).role)) throw err(HttpStatus.FORBIDDEN,"Admin only"); }
 Booking mine(Long uid,Long id){ User u=need(uid); Booking b=bookings.findById(id).orElseThrow(()->err(HttpStatus.NOT_FOUND,"No such booking"));
  if(!b.userId.equals(u.id)&&!"ADMIN".equals(u.role)) throw err(HttpStatus.FORBIDDEN,"Not your booking"); return b; }

 // ---- Auth ----
 @PostMapping("/register") User register(@RequestBody Map<String,String> b){
  if(users.findByEmail(b.get("email"))!=null) throw err(HttpStatus.BAD_REQUEST,"Email already registered");
  User u=new User(); u.name=b.get("name"); u.email=b.get("email"); u.passwordHash=hash(b.get("password")); u.role="USER"; return users.save(u); }
 @PostMapping("/login") User login(@RequestBody Map<String,String> b){
  User u=users.findByEmail(b.get("email"));
  if(u==null||!u.passwordHash.equals(hash(b.get("password")))) throw err(HttpStatus.UNAUTHORIZED,"Invalid credentials"); return u; }

 // ---- Stations & chargers ----
 @GetMapping("/stations") List<Station> stations(){ return stations.findAll(); }
 @GetMapping("/chargers") List<Charger> chargers(){ return chargers.findAll(); }
 @PostMapping("/stations") Station addStation(@RequestHeader(value="X-User-Id",required=false) Long uid,@RequestBody Station s){ admin(uid); s.id=null; return stations.save(s); }
 @PostMapping("/chargers") Charger addCharger(@RequestHeader(value="X-User-Id",required=false) Long uid,@RequestBody Charger c){ admin(uid); c.id=null; c.status="AVAILABLE"; return chargers.save(c); }
 @PutMapping("/chargers/{id}/status") Charger setStatus(@RequestHeader(value="X-User-Id",required=false) Long uid,@PathVariable Long id,@RequestParam String value){
  admin(uid); Charger c=chargers.findById(id).orElseThrow(); c.status=value; return chargers.save(c); }

 // ---- Bookings ----
 @PostMapping("/bookings") Booking book(@RequestHeader(value="X-User-Id",required=false) Long uid,@RequestBody Map<String,Object> b){
  User u=need(uid);
  Charger c=chargers.findById(Long.valueOf(b.get("chargerId").toString())).orElseThrow(()->err(HttpStatus.BAD_REQUEST,"No such charger"));
  if("OFFLINE".equals(c.status)) throw err(HttpStatus.BAD_REQUEST,"Charger is offline");
  LocalDateTime s=LocalDateTime.parse(b.get("start").toString());
  LocalDateTime e=s.plusMinutes((long)(Double.parseDouble(b.get("hours").toString())*60));
  if(s.isBefore(LocalDateTime.now().minusMinutes(5))) throw err(HttpStatus.BAD_REQUEST,"Start time is in the past");
  if(bookings.overlaps(c.id,s,e)>0) throw err(HttpStatus.CONFLICT,"Slot already booked for this charger");
  Booking k=new Booking(); k.userId=u.id; k.chargerId=c.id; k.startTime=s; k.endTime=e; return bookings.save(k); }
 @GetMapping("/bookings/mine") List<Booking> myBookings(@RequestHeader(value="X-User-Id",required=false) Long uid){ return bookings.findByUserIdOrderByStartTimeDesc(need(uid).id); }
 @GetMapping("/bookings") List<Booking> all(@RequestHeader(value="X-User-Id",required=false) Long uid){ admin(uid); return bookings.findAll(); }
 @PostMapping("/bookings/{id}/start") Booking start(@RequestHeader(value="X-User-Id",required=false) Long uid,@PathVariable Long id){
  Booking b=mine(uid,id); if(!"BOOKED".equals(b.status)) throw err(HttpStatus.BAD_REQUEST,"Not in BOOKED state");
  Charger c=chargers.findById(b.chargerId).orElseThrow(); c.status="OCCUPIED"; chargers.save(c); b.status="CHARGING"; return bookings.save(b); }
 @PostMapping("/bookings/{id}/stop") Booking stop(@RequestHeader(value="X-User-Id",required=false) Long uid,@PathVariable Long id,@RequestParam(required=false) Double energyKwh){
  Booking b=mine(uid,id); if(!"CHARGING".equals(b.status)) throw err(HttpStatus.BAD_REQUEST,"Not charging");
  Charger c=chargers.findById(b.chargerId).orElseThrow();
  double hrs=Duration.between(b.startTime,b.endTime).toMinutes()/60.0;
  b.energyKwh=energyKwh!=null?energyKwh:Math.round(c.powerKw*0.8*hrs*100)/100.0; // simulated meter
  b.amount=Math.round(b.energyKwh*c.ratePerKwh*100)/100.0; b.status="COMPLETED";
  c.status="AVAILABLE"; chargers.save(c); return bookings.save(b); }
 @PostMapping("/bookings/{id}/cancel") Booking cancel(@RequestHeader(value="X-User-Id",required=false) Long uid,@PathVariable Long id){
  Booking b=mine(uid,id); if(!"BOOKED".equals(b.status)) throw err(HttpStatus.BAD_REQUEST,"Only BOOKED can be cancelled"); b.status="CANCELLED"; return bookings.save(b); }
 @PostMapping("/bookings/{id}/pay") Booking pay(@RequestHeader(value="X-User-Id",required=false) Long uid,@PathVariable Long id){ // simulated payment
  Booking b=mine(uid,id); if(!"COMPLETED".equals(b.status)||b.paid) throw err(HttpStatus.BAD_REQUEST,"Nothing to pay"); b.paid=true; return bookings.save(b); }

 // ---- Reports ----
 @GetMapping("/reports") Map<String,Object> report(@RequestHeader(value="X-User-Id",required=false) Long uid){
  admin(uid); List<Booking> l=bookings.findAll(); Map<String,Object> m=new LinkedHashMap<>();
  m.put("users",users.count()); m.put("stations",stations.count()); m.put("chargers",chargers.count()); m.put("totalBookings",l.size());
  m.put("completed",l.stream().filter(b->"COMPLETED".equals(b.status)).count());
  m.put("energyKwh",l.stream().mapToDouble(b->b.energyKwh).sum());
  m.put("revenue",l.stream().filter(b->b.paid).mapToDouble(b->b.amount).sum()); return m; }
}
