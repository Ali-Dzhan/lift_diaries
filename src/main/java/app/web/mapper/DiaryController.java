package app.web.mapper;

import app.diary.service.DiaryService;
import app.web.dto.DiaryRequest;
import app.web.dto.DiaryResponse;
import app.web.dto.DiarySummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /**
     * Create a new diary entry for a user.
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<DiaryResponse> createDiary(
            @PathVariable UUID userId,
            @RequestBody DiaryRequest diaryRequest) {
        DiaryResponse createdDiary = diaryService.createDiary(userId, diaryRequest);
        return ResponseEntity.status(201).body(createdDiary);
    }

    /**
     * Get a specific diary entry by ID.
     */
    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryResponse> getDiaryById(@PathVariable UUID diaryId) {
        DiaryResponse diary = diaryService.getDiaryById(diaryId);
        return ResponseEntity.ok(diary);
    }

    /**
     * Get all diary entries for a user, ordered by entry date (descending).
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DiarySummaryResponse>> getDiariesByUser(@PathVariable UUID userId) {
        List<DiarySummaryResponse> diaries = diaryService.getDiariesByUser(userId);
        return ResponseEntity.ok(diaries);
    }

    /**
     * Update an existing diary entry by ID.
     */
    @PutMapping("/{diaryId}")
    public ResponseEntity<DiaryResponse> updateDiary(
            @PathVariable UUID diaryId,
            @RequestBody DiaryRequest diaryRequest) {
        DiaryResponse updatedDiary = diaryService.updateDiary(diaryId, diaryRequest);
        return ResponseEntity.ok(updatedDiary);
    }

    /**
     * Delete a diary entry by ID.
     */
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable UUID diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.noContent().build();
    }
}
