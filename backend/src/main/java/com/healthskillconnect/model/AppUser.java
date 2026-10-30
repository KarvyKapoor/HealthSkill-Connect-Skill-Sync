package com.healthskillconnect.model;
import java.util.*;
public class AppUser {
 public String id, fullName, email, role, specialization, experienceLevel, location, avatarInitials, passwordHash, password;
 public List<String> skills=new ArrayList<>(), learningInterests=new ArrayList<>(); public int learningProgress;
 public AppUser(){} public Map<String,Object> publicMap(){Map<String,Object> m=new LinkedHashMap<>();m.put("id",id);m.put("fullName",fullName);m.put("email",email);m.put("role",role);m.put("specialization",specialization);m.put("experienceLevel",experienceLevel);m.put("location",location);m.put("avatarInitials",avatarInitials);m.put("skills",skills);m.put("learningInterests",learningInterests);m.put("learningProgress",learningProgress);return m;}
}
