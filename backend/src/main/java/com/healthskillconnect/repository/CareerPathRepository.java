package com.healthskillconnect.repository;

import com.healthskillconnect.model.CareerPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareerPathRepository extends JpaRepository<CareerPath, String> {
    List<CareerPath> findByCategory(String category);
}
