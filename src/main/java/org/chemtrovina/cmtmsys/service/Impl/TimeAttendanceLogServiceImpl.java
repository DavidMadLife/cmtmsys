package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.AbsentEmployeeDto;
import org.chemtrovina.cmtmsys.dto.TimeAttendanceLogDto;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.model.enums.AttendanceTimeStatus;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;
import org.chemtrovina.cmtmsys.model.enums.ScanMethod;
import org.chemtrovina.cmtmsys.repository.base.*;
import org.chemtrovina.cmtmsys.service.base.DepartmentService;
import org.chemtrovina.cmtmsys.service.base.ShiftPlanEmployeeService;
import org.chemtrovina.cmtmsys.service.base.ShiftTypeEmployeeService;
import org.chemtrovina.cmtmsys.service.base.TimeAttendanceLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class TimeAttendanceLogServiceImpl implements TimeAttendanceLogService {

    private final TimeAttendanceLogRepository logRepo;
    private final EmployeeRepository employeeRepo;
    private final DepartmentRepository departmentRepo;
    private final PositionRepository positionRepository;
    private final ShiftPlanEmployeeRepository shiftPlanEmployeeRepo;
    private final ShiftTypeEmployeeRepository shiftTypeEmployeeRepo ;
    private final EmployeeLeaveRepository employeeLeaveRepo;

    private static final DateTimeFormatter CODE_NOW_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    public TimeAttendanceLogServiceImpl(
            TimeAttendanceLogRepository logRepo,
            EmployeeRepository employeeRepo,
            DepartmentRepository departmentRepo,
            PositionRepository positionRepo,
            ShiftPlanEmployeeRepository shiftPlanEmployeeRepository,
            ShiftTypeEmployeeRepository shiftTypeEmployeeRepository,
            EmployeeLeaveRepository employeeLeaveRepo
    ) {
        this.logRepo = logRepo;
        this.employeeRepo = employeeRepo;
        this.departmentRepo = departmentRepo;
        this.positionRepository = positionRepo;
        this.shiftPlanEmployeeRepo = shiftPlanEmployeeRepository;
        this.shiftTypeEmployeeRepo = shiftTypeEmployeeRepository;
        this.employeeLeaveRepo = employeeLeaveRepo;
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

        // 1️⃣ Lấy dữ liệu log
        List<TimeAttendanceLog> logs = logRepo.findByScanDateRange(from, to);

        if (logs.isEmpty()) {
            return new ArrayList<>();
        }

        // 2️⃣ Lấy danh sách EmployeeId
        List<Integer> employeeIds = logs.stream()
                .map(TimeAttendanceLog::getEmployeeId)
                .distinct()
                .toList();

        // 3️⃣ Load Employee
        Map<Integer, Employee> employeeMap = employeeRepo.findByIds(employeeIds)
                .stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, e -> e));

        // 4️⃣ Bộ gộp DTO (KEY = employeeId + workDate)
        Map<String, TimeAttendanceLogDto> map = new LinkedHashMap<>();

        // 5️⃣ Duyệt từng log
        for (TimeAttendanceLog log : logs) {

            Employee emp = employeeMap.get(log.getEmployeeId());
            if (emp == null) continue;

            LocalDate scanDate = log.getScanDateTime().toLocalDate();
            LocalTime scanTime = log.getScanDateTime().toLocalTime();

            // ===== XÁC ĐỊNH SHIFT =====
            String shiftCode = shiftPlanEmployeeRepo
                    .findShiftCodeByEmployeeAndDate(emp.getEmployeeId(), scanDate);

            ShiftTypeEmployee shiftType = null;
            if (shiftCode != null) {
                shiftType = shiftTypeEmployeeRepo.findByCode(shiftCode);
            }

            // ===== WORK DATE (FIX CA ĐÊM) =====
            LocalDate workDate = scanDate;

            if (shiftType != null
                    && Boolean.TRUE.equals(shiftType.getIsOvernight())
                    && log.getScanAction() == ScanAction.OUT
                    && scanTime.isBefore(shiftType.getEndTime())) {

                // OUT ca đêm → thuộc ngày IN (ngày trước)
                workDate = scanDate.minusDays(1);
            }

            String key = emp.getEmployeeId() + "_" + workDate;

            TimeAttendanceLogDto dto = map.get(key);

            // ===== TẠO DTO MỚI =====
            if (dto == null) {
                dto = new TimeAttendanceLogDto();
                map.put(key, dto);

                // ===== GÁN THÔNG TIN NHÂN VIÊN =====
                dto.setEmployeeId(emp.getEmployeeId());
                dto.setMscnId1(emp.getMSCNID1());
                dto.setFullName(emp.getFullName());
                dto.setCompany(emp.getCompany());
                dto.setGender(emp.getGender());
                dto.setBirthDate(emp.getBirthDate());
                dto.setEntryDate(emp.getEntryDate());
                dto.setNote(emp.getNote());
                dto.setPhoneNumber(emp.getPhoneNumber());
                dto.setManagerName(emp.getManager());
                dto.setPositionName(emp.getPositionName());
                dto.setDepartmentName(emp.getDepartmentName());
                dto.setJobTitle(emp.getJobTitle());

                dto.setScanDate(workDate);

                if (shiftType != null) {
                    dto.setShiftCode(shiftType.getShiftCode());
                    dto.setShiftName(shiftType.getShiftName());
                } else {
                    dto.setShiftName("N/A");
                }
            }

            // ===== GÁN GIỜ IN / OUT =====
            if (log.getScanAction() == ScanAction.IN) {
                dto.setIn(scanTime.toString());
            } else if (log.getScanAction() == ScanAction.OUT) {
                dto.setOut(scanTime.toString());
            }

            // ===== CODE NOW =====
            if (log.getCreatedAt() != null) {
                dto.setCodeNow(log.getCreatedAt().format(CODE_NOW_FORMATTER));
            }

        }

        // ===== APPLY STATUS SAU KHI GOM XONG =====
        for (TimeAttendanceLogDto dto : map.values()) {

            if (dto.getShiftCode() == null) continue;

            ShiftTypeEmployee shift =
                    shiftTypeEmployeeRepo.findByCode(dto.getShiftCode());

            if (shift != null) {
                System.out.println(
                        dto.getFullName() +
                                " shift=" + dto.getShiftCode() +
                                " start=" + (shift != null ? shift.getStartTime() : null) +
                                " end=" + (shift != null ? shift.getEndTime() : null)
                );

                applyAttendanceStatus(dto, shift);
            }



        }



        // 6️⃣ Đánh số thứ tự
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

        LocalDate today = LocalDate.now();

        // =====================================================
        // 1️⃣ LẤY TOÀN BỘ LOG HÔM NAY CỦA NHÂN VIÊN
        // =====================================================
        var todayLogs = logRepo.findByEmployeeIdAndDate(
                emp.getEmployeeId(), today
        );

        boolean hasIn = todayLogs.stream()
                .anyMatch(l -> l.getScanAction() == ScanAction.IN);

        boolean hasOut = todayLogs.stream()
                .anyMatch(l -> l.getScanAction() == ScanAction.OUT);

        // =====================================================
        // 2️⃣ CHECK NGHIỆP VỤ
        // =====================================================
        if ("IN".equals(type)) {
            if (hasIn) {
                throw new RuntimeException("Nhân viên đã IN hôm nay rồi");
            }
        }

        if ("OUT".equals(type)) {
            if (hasOut) {
                throw new RuntimeException("Nhân viên đã OUT hôm nay rồi");
            }
        }

        // =====================================================
        // 3️⃣ TẠO LOG MỚI
        // =====================================================
        TimeAttendanceLog log = new TimeAttendanceLog();
        log.setEmployeeId(emp.getEmployeeId());
        log.setScanAction("IN".equals(type) ? ScanAction.IN : ScanAction.OUT);
        log.setScanDateTime(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        log.setScanMethod(ScanMethod.MANUAL);

        logRepo.insert(log);

        // =====================================================
        // 4️⃣ MAP DTO
        // =====================================================
        TimeAttendanceLogDto dto = new TimeAttendanceLogDto();

        LocalDateTime now = log.getScanDateTime();

        dto.setLogId(log.getLogId());
        dto.setEmployeeId(emp.getEmployeeId());
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

        dto.setDepartmentName(emp.getDepartmentName());
        dto.setPositionName(emp.getPositionName());

        if (log.getScanAction() == ScanAction.IN) {
            dto.setIn(now.toLocalTime().toString());
        } else {
            dto.setOut(now.toLocalTime().toString());
        }

        return dto;
    }

    @Override
    public List<AbsentEmployeeDto> getAbsentEmployees(LocalDate date) {

        Map<Integer, ShiftPlanEmployee> shiftPlanMap =
                shiftPlanEmployeeRepo.findByShiftDate(date)
                        .stream()
                        .collect(Collectors.toMap(
                                ShiftPlanEmployee::getEmployeeId,
                                sp -> sp
                        ));

        Map<Integer, EmployeeLeave> leaveMap =
                employeeLeaveRepo.findLeaveByDate(date)
                        .stream()
                        .collect(Collectors.toMap(
                                EmployeeLeave::getEmployeeId,
                                l -> l,
                                (a, b) -> a // phòng trường hợp trùng
                        ));


        // ✅ chỉ lấy IN (tối ưu hơn nữa sẽ nói bên dưới)
        Set<Integer> inEmployeeIds = logRepo.findByScanDateRange(date, date)
                .stream()
                .filter(l -> l.getScanAction() == ScanAction.IN)
                .map(TimeAttendanceLog::getEmployeeId)
                .collect(Collectors.toSet());

        Map<Integer, String> deptNameMap = departmentRepo.findAll()
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getDepartmentID(),
                        d -> d.getDepartmentName()
                ));

        // 🔥 QUAN TRỌNG
        List<Employee> allEmployees = employeeRepo.findAllActive();

        List<AbsentEmployeeDto> result = new ArrayList<>();
        int no = 1;

        for (Employee emp : allEmployees) {

            if (inEmployeeIds.contains(emp.getEmployeeId())) continue;

            ShiftPlanEmployee sp = shiftPlanMap.get(emp.getEmployeeId());

            AbsentEmployeeDto dto = new AbsentEmployeeDto();
            dto.setNo(no++);
            dto.setEmployeeId(emp.getEmployeeId());
            dto.setEmployeeCode(emp.getMSCNID1());
            dto.setFullName(emp.getFullName());
            dto.setDepartmentName(emp.getDepartmentName());

            EmployeeLeave leave = leaveMap.get(emp.getEmployeeId());

            if (leave != null) {

                // ✅ có nghỉ phép
                if (sp != null) {
                    dto.setShiftCode(sp.getShiftCode());
                } else {
                    dto.setShiftCode("N/A");
                }

                dto.setNote(leave.getLeaveType().name());

            } else if (sp != null) {

                // ❌ không nghỉ nhưng không IN
                dto.setShiftCode(sp.getShiftCode());
                dto.setNote("Không check-in");

            } else {

                // ❌ không shift, không nghỉ
                dto.setShiftCode("N/A");
                dto.setNote("");
            }



            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public void manualFixAttendance(
            int employeeId,
            LocalDate date,
            String time,
            ScanAction action
    ) {

        // 1️⃣ Parse time
        LocalDateTime scanDateTime;
        try {
            scanDateTime = LocalDateTime.of(
                    date,
                    LocalTime.parse(time) // HH:mm
            );
        } catch (Exception e) {
            throw new RuntimeException("Sai định dạng giờ (HH:mm)");
        }

        // 2️⃣ Check log đã tồn tại chưa
        TimeAttendanceLog existing =
                logRepo.findByEmployeeIdDateAndAction(
                        employeeId, date, action
                );

        if (existing != null) {
            // 3️⃣ UPDATE
            existing.setScanDateTime(scanDateTime);
            existing.setScanMethod(ScanMethod.MANUAL);
            logRepo.update(existing);

        } else {
            // 4️⃣ INSERT
            TimeAttendanceLog log = new TimeAttendanceLog();
            log.setEmployeeId(employeeId);
            log.setScanAction(action);
            log.setScanDateTime(scanDateTime);
            log.setCreatedAt(LocalDateTime.now());
            log.setScanMethod(ScanMethod.MANUAL);

            logRepo.insert(log);
        }
    }

    public void applyAttendanceStatus(
            TimeAttendanceLogDto dto,
            ShiftTypeEmployee shift
    ) {
        // ❌ KHÔNG SET OK
        if (shift.getStartTime() == null || shift.getEndTime() == null) {
            dto.setInStatus(null);
            dto.setOutStatus(null);
            return;
        }

        if (dto.getIn() != null) {
            dto.setInStatus(
                    checkIn(LocalTime.parse(dto.getIn()), shift)
            );
        }

        if (dto.getOut() != null) {
            dto.setOutStatus(
                    checkOut(LocalTime.parse(dto.getOut()), shift)
            );
        }
    }



    private AttendanceTimeStatus checkIn(
            LocalTime scanTime,
            ShiftTypeEmployee shift
    ) {
        // Sau giờ bắt đầu → trễ
        if (scanTime.isAfter(shift.getStartTime())) {
            return AttendanceTimeStatus.LATE;
        }
        return AttendanceTimeStatus.OK;
    }

    private AttendanceTimeStatus checkOut(
            LocalTime scanTime,
            ShiftTypeEmployee shift
    ) {
        if (!Boolean.TRUE.equals(shift.getIsOvernight())) {
            // Ca ngày / HC
            if (scanTime.isBefore(shift.getEndTime())) {
                return AttendanceTimeStatus.EARLY; // ❌ đỏ
            }
            return AttendanceTimeStatus.OK;
        }

        // ===== Ca đêm (20:00 -> 04:00) =====
        // OUT hợp lệ từ 00:00 → 04:00
        if (scanTime.isBefore(shift.getEndTime())) {
            return AttendanceTimeStatus.OK;
        }

        return AttendanceTimeStatus.EARLY; // ❌
    }



}