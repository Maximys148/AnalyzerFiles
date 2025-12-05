package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileStatusDto {
    private String fileName;
    private String status;
    private Integer damageCount;
    private List<FileDamageDetailDto> damages;
}
