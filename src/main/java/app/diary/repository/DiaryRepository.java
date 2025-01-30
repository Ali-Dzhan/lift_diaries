package app.diary.repository;

import app.diary.model.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiaryRepository extends JpaRepository<Diary, UUID> {

    List<Diary> findAllByUserIdOrderByEntryDateDesc(UUID userId);
}
