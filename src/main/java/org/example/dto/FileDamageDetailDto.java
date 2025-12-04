package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDamageDetailDto {
    private int offset;       // Смещение в байтах
    private int originalByte; // Оригинальный байт
    private int damagedByte;  // Поврежденный байт
}