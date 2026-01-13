package org.chemtrovina.cmtmsys.dto;

import java.util.List;

public record AttendanceImportResult(
        int success,
        int skip,
        int error,
        List<String> messages
) {}
