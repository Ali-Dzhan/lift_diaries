package app.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiaryRequest {

    private String content;
    private String photoURL;
    private LocalDateTime entryDate;
}
