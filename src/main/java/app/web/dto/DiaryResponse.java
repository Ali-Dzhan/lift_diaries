package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DiaryResponse {
    private UUID id;
    private String content;
    private String photoUrl;
    private LocalDateTime entryDate;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
