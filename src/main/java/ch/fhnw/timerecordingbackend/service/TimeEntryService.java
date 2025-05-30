package ch.fhnw.timerecordingbackend.service;

import ch.fhnw.timerecordingbackend.dto.time.TimeEntryRequest;
import ch.fhnw.timerecordingbackend.dto.time.TimeEntryResponse;

import java.util.List;
import java.util.Map;

public interface TimeEntryService {
    TimeEntryResponse createTimeEntry(TimeEntryRequest timeEntryRequest);
    void updateTimeEntry(Long id, TimeEntryRequest timeEntryRequest);
    void deleteTimeEntry(Long id);
    List<TimeEntryResponse> getCurrentUserTimeEntries();
    List<TimeEntryResponse> getUserTimeEntries(Long userId);
    List<TimeEntryResponse> getTeamTimeEntries();
    List<TimeEntryResponse> getAllTimeEntries();
    Map<String, Object> startTimeTracking(Long projectId);
    Map<String, Object> stopTimeTracking(Long entryId);
    void assignProject(Long timeEntryId, Long projectId);
}
