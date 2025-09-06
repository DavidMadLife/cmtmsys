package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ModelLineRun;
import org.chemtrovina.cmtmsys.repository.base.ModelLineRunRepository;
import org.chemtrovina.cmtmsys.service.base.ModelLineRunService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ModelLineRunServiceImpl implements ModelLineRunService {

    private final ModelLineRunRepository repository;

    public ModelLineRunServiceImpl(ModelLineRunRepository repository) {
        this.repository = repository;
    }

    @Override
    public ModelLineRun createRun(int modelLineId) {
        // Cập nhật các run hôm nay thành Duplicate
        repository.markRunsAsDuplicate(modelLineId, LocalDate.now());

        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Lấy tất cả run trong ngày hôm nay
        List<ModelLineRun> allRunsToday = repository.findByModelLineId(modelLineId).stream()
                .filter(r -> r.getStartedAt() != null &&
                        r.getStartedAt().toLocalDate().equals(LocalDate.now()))
                .toList();

        int sequence = allRunsToday.size() + 1;
        String generatedCode = "R-" + datePart + "-" + sequence;

        // Tạo run mới
        ModelLineRun newRun = new ModelLineRun();
        newRun.setModelLineId(modelLineId);
        newRun.setStartedAt(now);
        newRun.setStatus("Running");
        newRun.setRunCode(generatedCode); // Gán runCode vào

        // Thêm vào DB
        repository.add(newRun);

        // Trả về bản ghi vừa tạo (nếu cần ID hoặc thông tin chính xác)
        return repository.findLatestRunByModelLineId(modelLineId);
    }



    @Override
    public void endRun(int runId) {
        ModelLineRun run = repository.findById(runId);
        if (run != null && !"Completed".equalsIgnoreCase(run.getStatus())) {
            run.setStatus("Completed");
            run.setEndedAt(LocalDateTime.now());
            repository.update(run);
        }
    }

    @Override
    public void reopenRun(int runId) {
        ModelLineRun run = repository.findById(runId);
        if (run != null && !"Running".equalsIgnoreCase(run.getStatus())) {
            run.setStatus("Running");
            run.setEndedAt(null); // <- mở lại thì bỏ mốc thời gian kết thúc
            repository.update(run);
        }
    }



    @Override
    public List<ModelLineRun> getRunsByModelLineId(int modelLineId) {
        return repository.findByModelLineId(modelLineId);
    }
}
