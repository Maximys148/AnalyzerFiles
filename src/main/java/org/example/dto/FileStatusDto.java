package org.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileStatusDto {
    private String fileName;
    private String status;
    private int damageCount;
}