package com.airbnbspa.repository;

import com.airbnbspa.entity.CalendarFeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarFeedRepository extends JpaRepository<CalendarFeed, Long> {

    List<CalendarFeed> findByEnabledTrue();

    Optional<CalendarFeed> findByUrl(String url);

    boolean existsByUrl(String url);

    boolean existsByUrlAndIdNot(String url, Long id);
}
