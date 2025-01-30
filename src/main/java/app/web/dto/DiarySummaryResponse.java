package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DiarySummaryResponse {

    private UUID id;
    private LocalDateTime entryDate;
    private String photoUrl;
}
