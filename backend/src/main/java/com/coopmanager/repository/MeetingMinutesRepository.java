package com.coopmanager.repository;

import com.coopmanager.entity.MeetingMinutes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingMinutesRepository extends JpaRepository<MeetingMinutes, Long> {

    List<MeetingMinutes> findByPublishedTrueOrderByMeetingDateDesc();

    List<MeetingMinutes> findAllByOrderByMeetingDateDesc();
}
