# CS Bouira Drive API Documentation

## Overview

This API provides access to [CSBouira's](https://csbouira.xyz/) academic resources organized by year level, semester, and module. The API supports query parameters and path-based navigation through the hierarchy.

**Last Updated:** 18 November 2025  
**API Version:** 2.1 (with file counts, multiple link types, and online resources)

## Base URL

```
https://api.csbouira.xyz/api/drive
```

---

## Root Structure

### Available Root Years

The API has **9 root levels** representing different academic years:

| Root Name     | Semesters |
| ------------- | --------- |
| Licence 1     | S01, S02  |
| Licence 2     | S03, S04  |
| Licence 3 SI  | S05, S06  |
| Master 1 GSI  | S07, S08  |
| Master 1 ISIL | S07, S08  |
| Master 1 IA   | S07, S08  |
| Master 2 GSI  | S09       |
| Master 2 ISIL | S09       |
| Master 2 IA   | S09       |

---

## Hierarchy Structure

```
Root Year
├── Semester (S01, S02, etc.)
│   └── Module (Course Name)
│       ├── Cours
│       ├── Exams
│       ├── Résumé
│       ├── TDs & TPs
│       └── Tests
└── Books & Exercices (Optional)
    └── Subject folders
        └── Files
```

**Note:** Each module consistently has these 5 folders. Empty folders are marked with `(empty)` suffix.

---

## Standard Module Structure

### Consistent Folder Organization

**Every module** follows this consistent structure with exactly 5 standard folders:

1. **Cours** - Lecture materials and course notes
2. **Exams** - Past examination papers
3. **Résumé** - Summary documents and study guides
4. **TDs & TPs** - Tutorial exercises (Travaux Dirigés) and Practical work (Travaux Pratiques)
5. **Tests** - Test papers and quizzes

### Empty Folder Notation

When a folder contains no content, it will be marked with `(empty)` in its name:

```json
{
  "Résumé (empty)": {
    "link": "https://drive.google.com/drive/folders/...",
    "subfolders": {},
    "files": []
  }
}
```

---

## Response Format

### Standard Response Object

```json
{
  "link": "https://drive.google.com/drive/folders/...",
  "subfolders": {
    "FolderName": {
      "link": "...",
      "subfolders": { ... },
      "files": [ ... ]
    }
  },
  "files": [
    {
      "name": "filename.pdf",
      "link": "https://drive.google.com/file/d/FILE_ID/view",
      "previewLink": "https://drive.google.com/file/d/FILE_ID/preview",
      "downloadLink": "https://drive.google.com/uc?export=download&id=FILE_ID"
    }
  ]
}
```

### Response Fields

- **`link`**: Direct Google Drive folder/file URL (original format - view/edit)
- **`previewLink`**: Direct preview URL for embedding files in iframes/WebView
- **`downloadLink`**: Direct download URL that triggers file download
- **`subfolders`**: Object containing nested folders (can be deeply nested)
- **`files`**: Array of file objects with `name`, `link`, `previewLink`, and `downloadLink` properties

---

## URL Encoding (using cURL)

### Special Characters

When using `curl`, paths containing spaces or special characters must be **URL encoded** — this is only required because `curl` needs properly encoded URLs.

> **Note:** I'm using URL encoding here just for demonstration.
> When developing in environments like **Python (`requests`)** or **JavaScript (`fetch`)** or any library, you can usually write the path _normally_ (e.g. `"Licence 1/S02"`) — these libraries handle URL encoding automatically.

**Examples**:

| Character | Encoded  |
| --------- | -------- |
| Space     | `%20`    |
| &         | `%26`    |
| É         | `%C3%89` |
| è         | `%C3%A8` |

---

## API Endpoints & Parameters

### 1. Get All Data (Root)

Returns the complete hierarchy with all years and file counts.

**Endpoint:**

```bash
GET https://api.csbouira.xyz/api/drive
```

**Example:**

```bash
curl "https://api.csbouira.xyz/api/drive"
```

**Response:**

```json
{
  "Licence 1": { ... },
  "Licence 2": { ... },
  "Licence 3 SI": { ... },
  "Master 1 GSI": { ... },
  "Master 1 ISIL": { ... },
  "Master 1 IA": { ... },
  "Master 2 GSI": { ... },
  "Master 2 ISIL": { ... },
  "Master 2 IA": { ... },
  "_fileCounts": {
    "Licence 1": 450,
    "Licence 2": 320,
    "Licence 3 SI": 280,
    "Master 1 GSI": 150,
    "Master 1 ISIL": 140,
    "Master 1 IA": 130,
    "Master 2 GSI": 80,
    "Master 2 ISIL": 75,
    "Master 2 IA": 70
  }
}
```

---

### 2. Get File Counts Only

Use the special `_fileCounts` path to get only the file count summary.

**Endpoint:**

```bash
GET https://api.csbouira.xyz/api/drive?path=_fileCounts
```

**Example:**

```bash
curl "https://api.csbouira.xyz/api/drive?path=_fileCounts"
```

**Response:**

```json
{
  "Licence 1": 450,
  "Licence 2": 320,
  "Licence 3 SI": 280,
  "Master 1 GSI": 150,
  "Master 1 ISIL": 140,
  "Master 1 IA": 130,
  "Master 2 GSI": 80,
  "Master 2 ISIL": 75,
  "Master 2 IA": 70
}
```

**Use Case:** Perfect for displaying badges or counters in your UI without loading the entire data structure.

---

### 3. Get Specific Year (Query Parameter)

Use the `year` query parameter to get data for a specific year.

**Endpoint:**

```bash
GET https://api.csbouira.xyz/api/drive?year={YEAR_NAME}
```

**Example:**

```bash
curl "https://api.csbouira.xyz/api/drive?year=Licence%201"
```

**Response:**

```json
{
  "link": "https://drive.google.com/drive/folders/1dxrIrtFZsAwG8YU3rps-cLP47E-kGXt6",
  "subfolders": {
    "S01": { ... },
    "S02": { ... },
    "Books & Exercices": { ... }
  },
  "files": []
}
```

---

### 4. Navigate Using Path Parameter

Use the `path` query parameter to navigate through nested folders using `>subfolders>` as separator.

**Endpoint:**

```bash
GET https://api.csbouira.xyz/api/drive?path={PATH}
```

**Path Format:** `Year>subfolders>Semester>subfolders>Module>subfolders>Folder>subfolders>Subfolder`

**Important:** You must include `>subfolders>` between each level to access nested content. Use `>files>` to access specific files by index.

---

### 5. Get Semester Data

**Example:** Get S02 from Licence 1

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02"
```

**Response:**

```json
{
  "link": "https://drive.google.com/drive/folders/1_b6bYOHfvIHCcgmPWz3gXAjHbLmB3wbt",
  "subfolders": {
    "Algère 2": { ... },
    "Algorithmique Et Structure De Données 2": { ... },
    "Analyse 2": { ... },
    "Outils De Programmation Pour Les Mathématiques": { ... },
    "Physique 2": { ... },
    "Probabilités Et Statistique Descriptive": { ... },
    "Structure Machine 2": { ... },
    "Technologie De L'Information Et De La Communication": { ... }
  },
  "files": []
}
```

---

### 6. Get Module Data

**Example:** Get Module OPM (Outils De Programmation Pour Les Mathématiques) from Licence 1 → S02

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Outils%20De%20Programmation%20Pour%20Les%20Math%C3%A9matiques"
```

**Response:**

```json
{
  "link": "https://drive.google.com/drive/folders/1ZPawCiTNnLkEW6VGMowgHjU_wMj_q1Sc",
  "subfolders": {
    "Cours": {
      "link": "...",
      "subfolders": { ... },
      "files": [ ... ]
    },
    "Exams": { ... },
    "Résumé (empty)": {
      "link": "...",
      "subfolders": {},
      "files": []
    },
    "TDs & TPs": { ... },
    "Tests": { ... }
  },
  "files": []
}
```

---

### 7. Get Standard Folder Content

**Example:** Get Cours folder from OPM Module

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Outils%20De%20Programmation%20Pour%20Les%20Math%C3%A9matiques>subfolders>Cours"
```

**Response:**

```json
{
  "link": "https://drive.google.com/drive/folders/1eouxrL6TDWweSNW8k1Ocv7L2kumq9Ltk",
  "subfolders": {
    "[YEAR] 2024-2025": {
      "link": "https://drive.google.com/drive/folders/19QDEuBllZ3ME-yyN06ZDZGNHdwui6wu3",
      "subfolders": {},
      "files": [
        {
          "name": "PTM01 Introduction Course.pdf",
          "link": "https://drive.google.com/file/d/1sU5ACiHQnDanBddyq--YmH1az7p84nHC/view",
          "previewLink": "https://drive.google.com/file/d/1sU5ACiHQnDanBddyq--YmH1az7p84nHC/preview",
          "downloadLink": "https://drive.google.com/uc?export=download&id=1sU5ACiHQnDanBddyq--YmH1az7p84nHC"
        },
        {
          "name": "PTM02 Python as a calculator.pdf",
          "link": "https://drive.google.com/file/d/1z4ncqtTjII_Cuii9Uml3EPCOsUMeJ2qM/view",
          "previewLink": "https://drive.google.com/file/d/1z4ncqtTjII_Cuii9Uml3EPCOsUMeJ2qM/preview",
          "downloadLink": "https://drive.google.com/uc?export=download&id=1z4ncqtTjII_Cuii9Uml3EPCOsUMeJ2qM"
        }
      ]
    }
  },
  "files": [
    {
      "name": "Course-05-Numpy-arrays-2.pdf",
      "link": "https://drive.google.com/file/d/12PACL6whfM9MaOPkblAFDjCzqDpOXfhb/view",
      "previewLink": "https://drive.google.com/file/d/12PACL6whfM9MaOPkblAFDjCzqDpOXfhb/preview",
      "downloadLink": "https://drive.google.com/uc?export=download&id=12PACL6whfM9MaOPkblAFDjCzqDpOXfhb"
    },
    {
      "name": "Tracé de courbes — Cours Python.webloc",
      "link": "https://drive.google.com/file/d/1a6VPLWi-kCsV-MR8yIdQENi9Yshq3cWW/view?usp=drivesdk"
      "previewLink": "https://drive.google.com/file/d/1a6VPLWi-kCsV-MR8yIdQENi9Yshq3cWW/preview",
      "downloadLink": "https://drive.google.com/uc?export=download&id=1a6VPLWi-kCsV-MR8yIdQENi9Yshq3cWW"
    }
  ]
}
```

---

### 8. Get Nested Subfolders

**Example:** Get Cours → "[YEAR] 2024-2025" subfolder Of OPM Module

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Outils%20De%20Programmation%20Pour%20Les%20Math%C3%A9matiques>subfolders>Cours>subfolders>[YEAR]%202024-2025"
```

**Response:**

```json
{
  "link": "https://drive.google.com/drive/folders/19QDEuBllZ3ME-yyN06ZDZGNHdwui6wu3",
  "subfolders": {},
  "files": [
    {
      "name": "PTM01 Introduction Course.pdf",
      "link": "https://drive.google.com/file/d/1sU5ACiHQnDanBddyq--YmH1az7p84nHC/view",
      "previewLink": "https://drive.google.com/file/d/1sU5ACiHQnDanBddyq--YmH1az7p84nHC/preview",
      "downloadLink": "https://drive.google.com/uc?export=download&id=1sU5ACiHQnDanBddyq--YmH1az7p84nHC"
    },
    {
      "name": "PTM02 Python as a calculator.pdf",
      "link": "https://drive.google.com/file/d/1z4ncqtTjII_Cuii9Uml3EPCOsUMeJ2qM/view",
      "previewLink": "https://drive.google.com/file/d/1z4ncqtTjII_Cuii9Uml3EPCOsUMeJ2qM/preview",
      "downloadLink": "https://drive.google.com/uc?export=download&id=1z4ncqtTjII_Cuii9Uml3EPCOsUMeJ2qM"
    }
  ]
}
```

---

### 9. Get Files

**Example:** Get files from Cours folder of OPM Module

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Outils%20De%20Programmation%20Pour%20Les%20Math%C3%A9matiques>subfolders>Cours>files"
```

**Response:**

```json
[
  {
    "name": "Course-05-Numpy-arrays-2.pdf",
    "link": "https://drive.google.com/file/d/12PACL6whfM9MaOPkblAFDjCzqDpOXfhb/view",
    "previewLink": "https://drive.google.com/file/d/12PACL6whfM9MaOPkblAFDjCzqDpOXfhb/preview",
    "downloadLink": "https://drive.google.com/uc?export=download&id=12PACL6whfM9MaOPkblAFDjCzqDpOXfhb"
  },
  {
    "name": "Course-05-NumPy-Arrays.pdf",
    "link": "https://drive.google.com/file/d/1NkaAnpyI5FUT3gUbbLiONgC3YJQDU13N/view",
    "previewLink": "https://drive.google.com/file/d/1NkaAnpyI5FUT3gUbbLiONgC3YJQDU13N/preview",
    "downloadLink": "https://drive.google.com/uc?export=download&id=1NkaAnpyI5FUT3gUbbLiONgC3YJQDU13N"
  }
]
```

---

### 10. Access Specific Files by Index

**Example:** Get first file from Cours folder of OPM Module

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Outils%20De%20Programmation%20Pour%20Les%20Math%C3%A9matiques>subfolders>Cours>files>0"
```

**Response:**

```json
{
  "name": "Course-05-Numpy-arrays-2.pdf",
  "link": "https://drive.google.com/file/d/12PACL6whfM9MaOPkblAFDjCzqDpOXfhb/view",
  "previewLink": "https://drive.google.com/file/d/12PACL6whfM9MaOPkblAFDjCzqDpOXfhb/preview",
  "downloadLink": "https://drive.google.com/uc?export=download&id=12PACL6whfM9MaOPkblAFDjCzqDpOXfhb"
}
```

---

## New Feature: Online Resources

### Online Resources Structure

A new `_onlineResources` section has been added under `_fileCounts` containing curated educational resources organized by year and subject:

```json
{
  "_fileCounts": {
    "Licence 1": 450,
    "Licence 2": 320,
    ...
  },
  "_onlineResources": {
    "Licence 1": {
      "Algorithm": [
        {
          "name": "Hassan EL BAHI",
          "url": "https://www.youtube.com/c/hassanbahi/playlists",
          "type": "Youtube Playlist",
          "language": "AR"
        },
        {
          "name": "GeeksforGeeks",
          "url": "https://www.geeksforgeeks.org/",
          "type": "Website",
          "language": "ENG"
        }
      ],
      "Probabilites": [
        {
          "name": "15 Min Math Lr",
          "url": "https://www.youtube.com/@15MinMathLr/videos",
          "type": "Youtube Video",
          "language": "AR"
        },
        {
          "name": "Ishaq Ghanem",
          "url": "https://www.youtube.com/playlist?list=PLlaFpJcuzvllMcU6DLqNr-7s2HxeplpC3",
          "type": "Youtube Playlist",
          "language": "AR"
        }
      ],
      "TIC": [
        {
          "name": "TIC Playlist",
          "url": "https://youtube.com/playlist?list=PLBz3Gvf8y1Hc__g7m48HTleqxDvfPQ8z4",
          "type": "Youtube Playlist",
          "language": "AR"
        }
      ]
    },
    "Licence 2": {
      "Architecture Des Ordinateurs": [
        {
          "name": "Cours Architecture des ordinateurs (univ oeb)",
          "url": "https://www.youtube.com/playlist?list=PLcVVbtKKK8EUX9FMl9WscHqRNoAPULmbv",
          "type": "Youtube Playlist",
          "language": "AR"
        }
      ],
      "Logique Mathématique": [
        {
          "name": "الأستاذ شهرالدين (Chapitre 1)",
          "url": "https://www.youtube.com/playlist?list=PLLC3KXQMBRm9op2DMVLd1b8g-6adO_G3E",
          "type": "Youtube Playlist",
          "language": "AR"
        }
      ]
    }
  }
}
```

### Resource Object Format

Each online resource contains:

- **`name`**: Name of the resource/channel/website
- **`url`**: Direct link to the resource
- **`type`**: Resource type (`Youtube Playlist`, `Youtube Video`, `Website`, etc.)
- **`language`**: Language of content (`AR` for Arabic, `FR` for French, `ENG` for English)

---

### 11. Get Online Resources Only

Use the special `_onlineResources` path to get only the curated educational resources.

**Endpoint:**

```bash
GET https://api.csbouira.xyz/api/drive?path=_onlineResources
```

**Example:**

```bash
curl "https://api.csbouira.xyz/api/drive?path=_onlineResources"
```

**Response:**

```json
{
  "Licence 1": {
    "Algorithm": [
      {
        "name": "Hassan EL BAHI",
        "url": "https://www.youtube.com/c/hassanbahi/playlists",
        "type": "Youtube Playlist",
        "language": "AR"
      },
      {
        "name": "GeeksforGeeks",
        "url": "https://www.geeksforgeeks.org/",
        "type": "Website",
        "language": "ENG"
      }
    ],
    "Probabilites": [ ... ],
    "TIC": [ ... ]
  },
  "Licence 2": {
    "Architecture Des Ordinateurs": [ ... ],
    "Logique Mathématique": [ ... ]
  }
}
```

**Use Case:** Perfect for displaying supplementary learning materials and video resources.

---

### 12. Get Online Resources for Specific Year

**Endpoint:**

```bash
GET https://api.csbouira.xyz/api/drive?path=_onlineResources>{YEAR_NAME}
```

**Example:**

```bash
curl "https://api.csbouira.xyz/api/drive?path=_onlineResources>Licence%201"
```

**Response:**

```json
{
  "Algorithm": [
    {
      "name": "Hassan EL BAHI",
      "url": "https://www.youtube.com/c/hassanbahi/playlists",
      "type": "Youtube Playlist",
      "language": "AR"
    },
    {
      "name": "GeeksforGeeks",
      "url": "https://www.geeksforgeeks.org/",
      "type": "Website",
      "language": "ENG"
    }
  ],
  "Probabilites": [ ... ],
  "TIC": [ ... ]
}
```

---

### 13. Get Online Resources for Specific Subject

**Endpoint:**

```bash
GET https://api.csbouira.xyz/api/drive?path=_onlineResources>{YEAR_NAME}>{SUBJECT_NAME}
```

**Example:**

```bash
curl "https://api.csbouira.xyz/api/drive?path=_onlineResources>Licence%201>Algorithm"
```

**Response:**

```json
[
  {
    "name": "Hassan EL BAHI",
    "url": "https://www.youtube.com/c/hassanbahi/playlists",
    "type": "Youtube Playlist",
    "language": "AR"
  },
  {
    "name": "GeeksforGeeks",
    "url": "https://www.geeksforgeeks.org/",
    "type": "Website",
    "language": "ENG"
  }
]
```

---

## File Link Types Explained

Each file in the API response includes three different link types:

### 1. `link` (Original View Link)

- Format: `ORIGINAL FORMAT`
- Opens the file in Google Drive's viewer
- Best for: User-facing "Open in Drive" buttons
- Supports: All file types

### 2. `previewLink` (Embedded Preview)

- Format: `https://drive.google.com/file/d/FILE_ID/preview`
- Optimized for embedding in iframe/WebView
- Best for: In-app file viewers, modal previews

### 3. `downloadLink` (Direct Download)

- Format: `https://drive.google.com/uc?export=download&id=FILE_ID`
- Triggers immediate file download
- Best for: Download buttons, automated downloads

---

## Error Responses

### 404 Not Found

When the path doesn't exist or is incorrectly formatted:

```bash
curl "https://api.csbouira.xyz/api/drive?path=InvalidYear/S01"
```

**Response:**

```json
{
  "error": "Not found",
  "path": "InvalidYear/S01"
}
```

**Common cause:** Missing `>subfolders>` in path

```bash
# Wrong - Missing subfolders
curl "https://api.csbouira.xyz/api/drive?path=Licence%201/S02"

# Correct - With subfolders
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02"
```

### 500 Server Error

When there's an internal error:

```json
{
  "error": "Failed to load JSON",
  "details": "Error message here"
}
```

---

## Advanced Usage with jq

### Get File Counts

**Get total files for all years:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=_fileCounts" | jq '.'
```

**Get file count for specific year:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=_fileCounts" | jq '.["Licence 1"]'
```

**Calculate total files across all years:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=_fileCounts" | jq 'add'
```

### Filter and Process Responses

**Get all module names in a semester:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02" | jq '.subfolders | keys'
```

**Get all file names from a folder:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Tests" | jq '.files[].name'
```

**Get all download links from a folder:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Exams" | jq '.files[].downloadLink'
```

**Get all preview links:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Cours" | jq '.files[].previewLink'
```

**Count files in a folder:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Tests" | jq '.files | length'
```

**Check for empty folders:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S01>subfolders>Structure%20Machine%201" | jq '.subfolders | keys[] | select(contains("empty"))'
```

**Extract file info with all link types:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Tests>files" | jq '.[] | {name, view: .link, preview: .previewLink, download: .downloadLink}'
```

**Find PDFs with specific text in name:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Tests" | jq '.files[] | select(.name | contains("2024"))'
```

**Pretty print with colors:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?year=Licence%201" | jq -C '.'
```

**Save to file:**

```bash
curl -s "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02" | jq '.' > semester_data.json
```

---

## CORS Support

The API includes CORS headers for browser-based requests:

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET,OPTIONS
```

This allows frontend applications to call the API directly from JavaScript.

---

## Common Use Cases

### 1. List All Years

```bash
curl "https://api.csbouira.xyz/api/drive" | jq 'keys | map(select(. != "_fileCounts"))'
```

### 2. Get All Semesters for a Year

```bash
curl "https://api.csbouira.xyz/api/drive?year=Licence%201" | jq '.subfolders | keys'
```

### 3. Get All Modules in a Semester

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02" | jq '.subfolders | keys'
```

### 4. Get All Files from a Module's Tests

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Tests" | jq '.files'
```

### 5. Get File Counts

```bash
curl "https://api.csbouira.xyz/api/drive?path=_fileCounts" | jq '.'
```

### 6. Check if a Folder is Empty

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S01>subfolders>Structure%20Machine%201" | jq '.subfolders | to_entries[] | select(.key | contains("empty")) | .key'
```

### 7. Get Download Links Only

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Exams" | jq '.files[].downloadLink'
```

### 8. Get Preview Links for Embedding

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Cours" | jq '.files[].previewLink'
```

### 9. Access a Specific File by Index

```bash
curl "https://api.csbouira.xyz/api/drive?path=Licence%201>subfolders>S02>subfolders>Structure%20Machine%202>subfolders>Tests>files>0"
```

---

## Best Practices

1. **Use the `path` parameter** for deep navigation instead of parsing the entire response
2. **Cache `_fileCounts` data** - it changes infrequently and is perfect for UI badges
3. **Use `previewLink` for embedding** - it's optimized for iframe/WebView displays
4. **Use `downloadLink` for downloads** - it triggers immediate downloads without opening Drive
5. **Check for `(empty)` suffix** in folder names to identify empty folders
6. **Handle 404 errors** gracefully for invalid paths
7. **Respect rate limits** - avoid excessive concurrent requests
8. **Choose the right link type** for your use case:
   - User wants to view in Drive → `link`
   - Embedding in your app → `previewLink`
   - Triggering a download → `downloadLink`

---

## Rate Limiting

While there are no strict rate limits, please be considerate:

- **Cache responses** when possible
- **Avoid polling** - the data doesn't change frequently
- **Use `_fileCounts` endpoint** for lightweight status checks
- **Batch operations** when downloading multiple files

---

## Support & Feedback

For issues, suggestions, or contributions:

- Email: [zedsalim@proton.me](mailto:zedsalim@proton.me)
- Report issues with specific paths or missing data
- Suggest improvements to the API structure
- Submit additional online resources for inclusion

---

## Changelog

### Version 2.1 (18 November 2025)

- Added `_onlineResources` section with curated educational resources (YouTube playlists, videos, and educational websites)

### Version 2.0 (09 November 2025)

- Added file counts with `_fileCounts` endpoint
- Enhanced file links with `previewLink` and `downloadLink`

### Version 1.0

- Initial API release with basic file structure navigation
