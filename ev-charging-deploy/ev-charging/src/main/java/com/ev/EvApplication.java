package com.ev;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication
public class EvApplication {
 public static void main(String[] a){ SpringApplication.run(EvApplication.class,a); }
 @Bean CommandLineRunner seed(UserRepo u,StationRepo s,ChargerRepo c){ return x->{
  if(u.count()>0) return;
  User admin=new User(); admin.name="Admin"; admin.email="admin@ev.com"; admin.passwordHash=Api.hash(System.getenv().getOrDefault("ADMIN_PASSWORD","admin123")); admin.role="ADMIN"; u.save(admin);
  String[][] st={{"City Mall Hub","Banjara Hills"},{"Highway Plaza","ORR Exit 4"}};
  for(String[] d:st){ Station t=new Station(); t.name=d[0]; t.location=d[1]; s.save(t);
   for(int i=0;i<2;i++){ Charger ch=new Charger(); ch.stationId=t.id; ch.type=i==0?"AC":"DC"; ch.powerKw=i==0?7.2:50; ch.ratePerKwh=i==0?12:18; c.save(ch);} }
 };}
}
