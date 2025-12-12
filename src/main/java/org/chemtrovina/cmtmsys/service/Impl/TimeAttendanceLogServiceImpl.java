package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.TimeAttendanceLogDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.TimeAttendanceLog;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;
import org.chemtrovina.cmtmsys.model.enums.ScanMethod;
import org.chemtrovina.cmtmsys.repository.base.DepartmentRepository;
import org.chemtrovina.cmtmsys.repository.base.EmployeeRepository;
import org.chemtrovina.cmtmsys.repository.base.PositionRepository;
import org.chemtrovina.cmtmsys.repository.base.TimeAttendanceLogRepository;
import org.chemtrovina.cmtmsys.service.base.DepartmentService;
import org.chemtrovina.cmtmsys.service.base.TimeAttendanceLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TimeAttendanceLogServiceImpl implements TimeAttendanceLogService {

    private final TimeAttendanceLogRepository logRepo;
    private final EmployeeRepository employeeRepo;
    private final DepartmentRepository departmentRepo;
    private final PositionRepository positionRepository;
    private static final DateTimeFormatter CODE_NOW_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    public TimeAttendanceLogServiceImpl(TimeAttendanceLogRepository logRepo,
                                        EmployeeRepository employeeRepo,
                                        DepartmentRepository departmentRepo,
                                        PositionRepository positionRepo) {
        this.logRepo = logRepo;
        this.employeeRepo = employeeRepo;
        this.departmentRepo = departmentRepo;
        this.positionRepository = positionRepo;

    }

    // ===================================
    // CRUD OPERATIONS
    // ===================================

    @Override
    public List<TimeAttendanceLog> getAll() {
        return logRepo.findAll();
    }

    @Override
    public TimeAttendanceLog getById(int id) {
        // ID là số nguyên (int), không cần kiểm tra null hay isBlank
        if (id <= 0) return null;
        return logRepo.findById(id);
    }

    @Override
    @Transactional
    public void create(TimeAttendanceLog log) {
        if (log == null) return;

        // Có thể thêm logic kiểm tra dữ liệu trước khi chèn (nếu cần)
        logRepo.insert(log);
    }

    @Override
    @Transactional
    public void update(TimeAttendanceLog log) {
        // Đảm bảo đối tượng và ID hợp lệ
        if (log == null || log.getLogId() <= 0) return;
        logRepo.update(log);
    }

    @Override
    @Transactional
    public void delete(int id) {
        // ID là số nguyên (int), không cần kiểm tra null hay isBlank
        if (id <= 0) return;
        logRepo.delete(id);
    }

    @Override
    public List<TimeAttendanceLogDto> getLogDtosByDateRange(LocalDate from, LocalDate to) {

        // Lấy dữ liệu log
        List<TimeAttendanceLog> logs = logRepo.findByScanDateRange(from, to);

        // Lấy danh sách EmployeeId
        List<Integer> employeeIds = logs.stream()
                .map(TimeAttendanceLog::getEmployeeId)
                .distinct()
                .toList();

        // Tải thông tin Employee
        Map<Integer, Employee> employeeMap = employeeRepo.findByIds(employeeIds)
                .stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, e -> e));

        // Bộ chứa DTO đã gộp
        Map<String, TimeAttendanceLogDto> map = new LinkedHashMap<>();

        // Gộp IN/OUT vào cùng một DTO
        for (TimeAttendanceLog log : logs) {

            LocalDate scanDate = log.getScanDateTime().toLocalDate();
            String key = log.getEmployeeId() + "_" + scanDate;

            TimeAttendanceLogDto dto = map.get(key);

            if (dto == null) {
                dto = new TimeAttendanceLogDto();
                map.put(key, dto);

                Employee emp = employeeMap.get(log.getEmployeeId());

                // GÁN THÔNG TIN NHÂN VIÊN
                if (emp != null) {
                    dto.setMscnId1(emp.getMSCNID1());
                    dto.setFullName(emp.getFullName());
                    dto.setCompany(emp.getCompany());
                    dto.setGender(emp.getGender());
                    dto.setBirthDate(emp.getBirthDate());
                    dto.setEntryDate(emp.getEntryDate());
                    dto.setNote(emp.getNote());
                    dto.setPhoneNumber(emp.getPhoneNumber());
                    dto.setManagerName(emp.getManager());
                    dto.setPositionName(positionRepository.getName(emp.getPositionId()));
                    dto.setDepartmentName(departmentRepo.getName(emp.getDepartmentId()));
                }

                dto.setScanDate(scanDate);
            }

            // GÁN GIỜ IN / OUT
            if (log.getScanAction() == ScanAction.IN) {
                dto.setIn(log.getScanDateTime().toLocalTime().toString());
            } else if (log.getScanAction() == ScanAction.OUT) {
                dto.setOut(log.getScanDateTime().toLocalTime().toString());
            }

            // CODE NOW
            if (log.getCreatedAt() != null) {
                dto.setCodeNow(log.getCreatedAt().format(CODE_NOW_FORMATTER));
            }
        }

        // ĐÁNH SỐ THỨ TỰ
        AtomicInteger index = new AtomicInteger(0);
        map.values().forEach(d -> d.setNo(index.incrementAndGet()));

        return new ArrayList<>(map.values());
    }


    @Override
    @Transactional
    public TimeAttendanceLogDto processScan(String input, String type) {

        Employee emp = employeeRepo.findByMscnId1(input);
        if (emp == null) {
            throw new RuntimeException("Mã không tồn tại: " + input);
        }

        TimeAttendanceLog log = new TimeAttendanceLog();
        log.setEmployeeId(emp.getEmployeeId());
        log.setScanAction(type.equals("IN") ? ScanAction.IN : ScanAction.OUT);
        log.setScanDateTime(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        log.setScanMethod(ScanMethod.MANUAL);
        logRepo.insert(log);

        TimeAttendanceLogDto dto = new TimeAttendanceLogDto();

        LocalDateTime now = log.getScanDateTime();

        dto.setLogId(log.getLogId());
        dto.setScanDate(now.toLocalDate());
        dto.setScanTime(now.toLocalTime());
        dto.setMscnId1(emp.getMSCNID1());
        dto.setFullName(emp.getFullName());

        dto.setCompany(emp.getCompany());
        dto.setGender(emp.getGender());
        dto.setBirthDate(emp.getBirthDate());
        dto.setEntryDate(emp.getEntryDate());
        dto.setPhoneNumber(emp.getPhoneNumber());
        dto.setManagerName(emp.getManager());

        dto.setDepartmentName(departmentRepo.getName(emp.getDepartmentId()));
        dto.setPositionName(positionRepository.getName(emp.getPositionId()));

        if (log.getScanAction() == ScanAction.IN) {
            dto.setIn(now.toLocalTime().toString());
        } else {
            dto.setOut(now.toLocalTime().toString());
        }

        return dto;
    }


}