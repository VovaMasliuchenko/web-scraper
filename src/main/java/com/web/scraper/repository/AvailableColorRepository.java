package com.web.scraper.repository;

import com.web.scraper.model.AvailableColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvailableColorRepository extends JpaRepository<AvailableColor, Long> {
    Optional<AvailableColor> findByColor(String color);
}
