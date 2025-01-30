package app.diary.service;

import app.diary.model.Diary;
import app.diary.repository.DiaryRepository;
import app.exception.DomainException;
import app.user.service.UserService;
import app.web.dto.DiaryRequest;
import app.web.dto.DiaryResponse;
import app.web.dto.DiarySummaryResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserService userService;

    @Autowired
    public DiaryService(DiaryRepository diaryRepository, UserService userService) {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
    }

    @Transactional
    public DiaryResponse createDiary(UUID userId, DiaryRequest diaryCreateRequest) {
        var user = userService.getById(userId);

        Diary diary = Diary.builder()
                .id(UUID.randomUUID())
                .user(user)
                .entryDate(diaryCreateRequest.getEntryDate())
                .content(diaryCreateRequest.getContent())
                .photoUrl(diaryCreateRequest.getPhotoURL())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        diaryRepository.save(diary);

        log.info("Diary entry created for user [{}] with ID [{}].", userId, diary.getId());

        return DiaryResponse.builder()
                .id(diary.getId())
                .entryDate(diary.getEntryDate())
                .content(diary.getContent())
                .photoUrl(diary.getPhotoUrl())
                .createdOn(diary.getCreatedOn())
                .updatedOn(diary.getUpdatedOn())
                .build();
    }


    public DiaryResponse getDiaryById(UUID diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DomainException("Diary with ID [%s] not found.".formatted(diaryId), HttpStatus.NOT_FOUND));

        return DiaryResponse.builder()
                .id(diary.getId())
                .entryDate(diary.getEntryDate())
                .content(diary.getContent())
                .photoUrl(diary.getPhotoUrl())
                .createdOn(diary.getCreatedOn())
                .updatedOn(diary.getUpdatedOn())
                .build();
    }

    public List<DiarySummaryResponse> getDiariesByUser(UUID userId) {
        List<Diary> diaries = diaryRepository.findAllByUserIdOrderByEntryDateDesc(userId);

        if (diaries.isEmpty()) {
            log.info("No diary entries found for user with ID [{}].", userId);
        }

        return diaries.stream()
                .map(diary -> DiarySummaryResponse.builder()
                        .id(diary.getId())
                        .entryDate(diary.getEntryDate())
                        .photoUrl(diary.getPhotoUrl())
                        .build())
                .collect(Collectors.toList());
    }


    @Transactional
    public DiaryResponse updateDiary(UUID diaryId, DiaryRequest diaryUpdateRequest) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DomainException("Diary with ID [%s] not found.".formatted(diaryId), HttpStatus.NOT_FOUND));

        diary.setEntryDate(diaryUpdateRequest.getEntryDate());
        diary.setContent(diaryUpdateRequest.getContent());
        diary.setPhotoUrl(diaryUpdateRequest.getPhotoURL());
        diary.setUpdatedOn(LocalDateTime.now());

        diaryRepository.save(diary);

        log.info("Diary entry updated with ID [{}].", diaryId);

        return DiaryResponse.builder()
                .id(diary.getId())
                .entryDate(diary.getEntryDate())
                .content(diary.getContent())
                .photoUrl(diary.getPhotoUrl())
                .createdOn(diary.getCreatedOn())
                .updatedOn(diary.getUpdatedOn())
                .build();
    }

    @Transactional
    public void deleteDiary(UUID diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DomainException("Diary with ID [%s] not found.".formatted(diaryId), HttpStatus.NOT_FOUND));

        diaryRepository.delete(diary);

        log.info("Diary entry deleted with ID [{}].", diaryId);
    }
}
