# File Damage Analyzer

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)

## Overview
Web application for comparing files between original and damaged directories. Detects byte-level differences and provides detailed damage analysis with REST API and clean UI.

## Features
- Asynchronous directory comparison
- Byte-by-byte file damage detection  
- File status: OK, DAMAGED, MISSING
- Detailed damage reports: offset, bytes, delta, damage type
- REST API + Bootstrap/Thymeleaf UI

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Main HTML page |
| `/api/v1/analyze` | POST | Start analysis `{originalDir, damagedDir}` |
| `/api/v1/results` | GET | Get results + status |
| `/api/v1/details/{filename}` | GET | File damage details |

## Quick Start (Development)
git clone <repository>
cd file-damage-analyzer
mvn clean package
java -jar target/*.jar

## Production Installation (ALT Workstation K 11.1+)

### 1. Build RPM
- rpmdev-setuptree
- cp -r * ~/rpmbuild/SOURCES/
- cp rpm/*.spec ~/rpmbuild/SPECS/
- rpmbuild -ba ~/rpmbuild/SPECS/file-damage-analyzer.spec

### 2. Install & Run
sudo rpm -ivh ~/rpmbuild/RPMS/x86_64/file-damage-analyzer-*.rpm
sudo systemctl enable --now file-damage-analyzer

## Testing Guide

### 1. Create Test Data
Using a Python script, create corrupted data, drag the whole data into one folder, and the corrupted data into the second

### 2. Test Workflow
1. **Original**: `/tmp/original`
2. **Damaged**: `/tmp/damaged` 
3. **Start Analysis** → **Get Results** → **Show Details**

### Expected Output
File: ls | Status: DAMAGED | Damages: 3
61 0x3D → 57 0x39 Δ=4
34 0x22 → 19 0x13 Δ=15

## Damage Details

| Field | Meaning |
|-------|---------|
| `[444]` | Byte offset |
| `61` | Original (DEC) |
| `0x3D` | Original (HEX) |
| `→` | Changed to |
| `57` | Damaged (DEC) |
| `0x39` | Damaged (HEX) |
| `Δ=4` | Difference |
