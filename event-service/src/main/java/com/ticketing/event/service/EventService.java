package com.ticketing.event.service;

import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.exception.ErrorCode;
import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventCategory;
import com.ticketing.event.domain.Seat;
import com.ticketing.event.domain.SeatStatus;
import com.ticketing.event.dto.*;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.event.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "events", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<EventResponse> getEvents(EventCategory category, Pageable pageable) {
        Page<Event> events = (category == null)
                ? eventRepository.findAll(pageable)
                : eventRepository.findByStatusAndCategory(
                com.ticketing.event.domain.EventStatus.OPEN, category, pageable);

        return events.map(e -> {
            EventResponse res = new EventResponse(e);
            int available = seatRepository.countAvailableByEventId(e.getId());
            res.setGrades(List.of()); // 목록 조회에선 grade 상세 생략
            return res;
        });
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "event", key = "#eventId")
    public EventResponse getEvent(Long eventId) {
        Event event = findEvent(eventId);
        List<Seat> seats = seatRepository.findByEventId(eventId);

        Map<String, List<Seat>> byGrade = seats.stream()
                .collect(Collectors.groupingBy(s -> s.getGrade().name()));

        List<EventResponse.GradeInfo> grades = byGrade.entrySet().stream()
                .map(entry -> {
                    List<Seat> gradeSeat = entry.getValue();
                    long available = gradeSeat.stream()
                            .filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
                    int price = gradeSeat.get(0).getPrice();
                    return new EventResponse.GradeInfo(entry.getKey(), price, gradeSeat.size(), available);
                })
                .sorted(Comparator.comparing(EventResponse.GradeInfo::getGrade))
                .collect(Collectors.toList());

        EventResponse response = new EventResponse(event);
        response.setGrades(grades);
        return response;
    }

    @Transactional
    @CacheEvict(value = {"events", "event"}, allEntries = true)
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = Event.create(
                request.getTitle(), request.getVenue(), request.getCategory(),
                request.getDescription(), request.getEventDate(),
                request.getBookingOpenAt(), request.getBookingCloseAt()
        );
        event.open();
        return new EventResponse(eventRepository.save(event));
    }

    @Transactional
    @CacheEvict(value = {"events", "event"}, allEntries = true)
    public List<SeatResponse> createSeats(Long eventId, CreateSeatsRequest request) {
        Event event = findEvent(eventId);
        List<Seat> seats = request.getBlocks().stream()
                .flatMap(block -> IntStream.rangeClosed(1, block.getCount())
                        .mapToObj(n -> Seat.create(event.getId(), block.getZone(),
                                block.getRowNum(), n, block.getGrade(), block.getPrice())))
                .collect(Collectors.toList());
        return seatRepository.saveAll(seats).stream()
                .map(SeatResponse::new)
                .collect(Collectors.toList());
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }
}
