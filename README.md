# cmtmsys
---

## 📦 cmtmsys – Material Incoming Inspection System

**cmtmsys** is a desktop application designed to support the inspection and warehouse management of incoming materials in a manufacturing workflow. Built with JavaFX (JDK 24) following Clean Architecture principles, the app enables barcode scanning, Maker Part Number verification, error detection for mismatched or duplicate reels, and efficient management of MOQ data from suppliers.

---

### 🔧 Technologies Used:
- JavaFX + FXML UI
- Spring JDBC / Hibernate JPA (depending on module)
- SQL Server
- Lombok, MapStruct (AutoMapper-style mapping like in C#)

### 💡 Key Features:
- Scan incoming material barcodes and validate against system data.
- Detect and handle duplicate, mismatched, or invalid reels.
- Provide options to **Continue** or **Re-Scan** during scanning.
- Import MOQ data from Excel files.
- Search and filter material data quickly and efficiently.

---
