package com.coopmanager.service;

import com.coopmanager.entity.Member;
import com.coopmanager.entity.MeetingMinutes;
import com.coopmanager.repository.MeetingMinutesRepository;
import com.coopmanager.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingMinutesService {

    private final MeetingMinutesRepository minutesRepo;
    private final MemberRepository memberRepository;

    @Transactional
    public MinutesResponse create(MinutesRequest request, Long authorId) {
        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        MeetingMinutes minutes = MeetingMinutes.builder()
                .title(request.getTitle())
                .meetingDate(request.getMeetingDate())
                .meetingType(request.getMeetingType())
                .agenda(request.getAgenda())
                .content(request.getContent())
                .decisions(request.getDecisions())
                .attendeesCount(request.getAttendeesCount())
                .published(false)
                .build();

        return toResponse(minutesRepo.save(minutes));
    }

    @Transactional
    public MinutesResponse publish(Long id, Long publisherId) {
        MeetingMinutes minutes = getOrThrow(id);
        Member publisher = memberRepository.findById(publisherId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        minutes.setPublished(true);
        minutes.setPublishedBy(publisher);
        minutes.setPublishedAt(LocalDateTime.now());
        return toResponse(minutesRepo.save(minutes));
    }

    @Transactional(readOnly = true)
    public List<MinutesResponse> findAll(boolean publishedOnly) {
        List<MeetingMinutes> all = publishedOnly
                ? minutesRepo.findByPublishedTrueOrderByMeetingDateDesc()
                : minutesRepo.findAllByOrderByMeetingDateDesc();
        return all.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MinutesResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public MinutesResponse update(Long id, MinutesRequest request) {
        MeetingMinutes minutes = getOrThrow(id);
        if (minutes.isPublished()) {
            throw new IllegalStateException("Cannot edit published minutes");
        }
        minutes.setTitle(request.getTitle());
        minutes.setMeetingDate(request.getMeetingDate());
        minutes.setMeetingType(request.getMeetingType());
        minutes.setAgenda(request.getAgenda());
        minutes.setContent(request.getContent());
        minutes.setDecisions(request.getDecisions());
        minutes.setAttendeesCount(request.getAttendeesCount());
        return toResponse(minutesRepo.save(minutes));
    }

    @Transactional
    public void delete(Long id) {
        MeetingMinutes minutes = getOrThrow(id);
        if (minutes.isPublished()) {
            throw new IllegalStateException("Cannot delete published minutes");
        }
        minutesRepo.delete(minutes);
    }

    private MeetingMinutes getOrThrow(Long id) {
        return minutesRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Minutes not found: " + id));
    }

    private MinutesResponse toResponse(MeetingMinutes m) {
        return MinutesResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .meetingDate(m.getMeetingDate())
                .meetingType(m.getMeetingType())
                .agenda(m.getAgenda())
                .content(m.getContent())
                .decisions(m.getDecisions())
                .attendeesCount(m.getAttendeesCount())
                .published(m.isPublished())
                .publishedByName(m.getPublishedBy() != null ? m.getPublishedBy().getFullName() : null)
                .publishedAt(m.getPublishedAt())
                .createdAt(m.getCreatedAt())
                .build();
    }

    @Data
    public static class MinutesRequest {
        private String title;
        private LocalDate meetingDate;
        private String meetingType;
        private String agenda;
        private String content;
        private String decisions;
        private Integer attendeesCount;
    }

    @Data
    @Builder
    public static class MinutesResponse {
        private Long id;
        private String title;
        private LocalDate meetingDate;
        private String meetingType;
        private String agenda;
        private String content;
        private String decisions;
        private Integer attendeesCount;
        private boolean published;
        private String publishedByName;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
    }
}
