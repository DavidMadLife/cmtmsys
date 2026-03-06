package org.chemtrovina.cmtmsys.service.impl;

import org.chemtrovina.cmtmsys.dto.*;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.model.enums.AttendanceTimeStatus;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;
import org.chemtrovina.cmtmsys.model.enums.ScanMethod;
import org.chemtrovina.cmtmsys.repository.base.*;
import org.chemtrovina.cmtmsys.service.base.TimeAttendanceLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
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

        // 1) Load raw logs: lấy tới to+1 end of day => end exclusive = to+2 00:00
        List<TimeAttendanceLog> rawLogs = logRepo.findByScanDateRange(from, to.plusDays(2));
        if (rawLogs.isEmpty()) return new ArrayList<>();

        // 2) Preload shift plan cho các employee có trong log (từ from-1 đến to)
        List<Integer> empIdsInLogs = rawLogs.stream()
                .map(TimeAttendanceLog::getEmployeeId)
                .distinct()
                .toList();

        Map<String, String> shiftCodeByEmpDate = shiftPlanEmployeeRepo
                .findByEmployeesAndDateRange(empIdsInLogs, from.minusDays(1), to)
                .stream()
                .collect(Collectors.toMap(
                        sp -> sp.getEmployeeId() + "_" + sp.getShiftDate(),
                        ShiftPlanEmployee::getShiftCode,
                        (a, b) -> a
                ));

        // 3) Preload ShiftTypeEmployee theo list shiftCode (1 query)
        Set<String> shiftCodes = shiftCodeByEmpDate.values().stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toSet());

        Map<String, ShiftTypeEmployee> shiftTypeByCode = new HashMap<>();
        if (!shiftCodes.isEmpty()) {
            // Khuyến nghị: implement findByCodes(Set<String>) để query IN (...)
            List<ShiftTypeEmployee> shifts = shiftTypeEmployeeRepo.findByCodes(shiftCodes);
            for (ShiftTypeEmployee s : shifts) {
                shiftTypeByCode.put(s.getShiftCode(), s);
            }
        }

        // 4) Filter raw logs (không gọi DB nữa)
        List<TimeAttendanceLog> logs = rawLogs.stream()
                .filter(log -> {
                    LocalDate scanDate = log.getScanDateTime().toLocalDate();

                    // trong from..to => lấy
                    if (!scanDate.isBefore(from) && !scanDate.isAfter(to)) return true;


                    // chỉ lấy OUT ngày to+1 nếu thuộc ca đêm của ngày trước
                    if (scanDate.equals(to.plusDays(1)) && log.getScanAction() == ScanAction.OUT) {
                        String prevShiftCode = shiftCodeByEmpDate.get(log.getEmployeeId() + "_" + scanDate.minusDays(1));
                        if (prevShiftCode == null) return false;

                        ShiftTypeEmployee prevShift = shiftTypeByCode.get(prevShiftCode);
                        return prevShift != null && Boolean.TRUE.equals(prevShift.getIsOvernight());
                    }

                    return false;
                })
                .toList();

        if (logs.isEmpty()) return new ArrayList<>();

        // 5) Load employee (1 query)
        Map<Integer, Employee> employeeMap = employeeRepo.findByIds(
                        logs.stream().map(TimeAttendanceLog::getEmployeeId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, e -> e));

        // 6) Group DTO theo empId + workDate
        Map<String, TimeAttendanceLogDto> map = new LinkedHashMap<>();

        for (TimeAttendanceLog log : logs) {

            Employee emp = employeeMap.get(log.getEmployeeId());
            if (emp == null) continue;

            LocalDate scanDate = log.getScanDateTime().toLocalDate();
            LocalTime scanTime = log.getScanDateTime().toLocalTime();

            // 6.1) resolve workDate (OUT ca đêm thuộc ngày hôm qua)


            LocalDate workDate = scanDate;
            if (log.getScanAction() == ScanAction.OUT) {
                String prevShiftCode = shiftCodeByEmpDate.get(emp.getEmployeeId() + "_" + scanDate.minusDays(1));
                ShiftTypeEmployee prevShift = prevShiftCode == null ? null : shiftTypeByCode.get(prevShiftCode);

                if (prevShift != null && Boolean.TRUE.equals(prevShift.getIsOvernight())) {
                    workDate = scanDate.minusDays(1);
                }
            }

            String key = emp.getEmployeeId() + "_" + workDate;

            TimeAttendanceLogDto dto = map.get(key);
            if (dto == null) {
                dto = new TimeAttendanceLogDto();
                map.put(key, dto);

                dto.setEmployeeId(emp.getEmployeeId());
                dto.setMscnId1(emp.getMSCNID1());
                dto.setFullName(emp.getFullName());
                dto.setCompany(emp.getCompany());


                dto.setGender(emp.getGender());
                dto.setBirthDate(emp.getBirthDate());
                dto.setEntryDate(emp.getEntryDate());
                dto.setDepartmentName(emp.getDepartmentName());
                dto.setPositionName(emp.getPositionName());

                dto.setJobTitle(emp.getJobTitle());
                dto.setPhoneNumber(emp.getPhoneNumber());

                dto.setScanDate(workDate);

                // 6.2) attach shift theo workDate
                String shiftCode = shiftCodeByEmpDate.get(emp.getEmployeeId() + "_" + workDate);
                ShiftTypeEmployee shift = shiftCode == null ? null : shiftTypeByCode.get(shiftCode);

                if (shift != null) {
                    dto.setShiftCode(shift.getShiftCode());
                    dto.setShiftName(shift.getShiftName());
                } else {
                    dto.setShiftCode(shiftCode); // vẫn set để debug nếu cần
                    dto.setShiftName("N/A");
                }
            }

            // 6.3) set in/out
            if (log.getScanAction() == ScanAction.IN) dto.setIn(scanTime.toString());
            else dto.setOut(scanTime.toString());
        }

        // 7) Apply status (không query DB nữa)
        for (TimeAttendanceLogDto dto : map.values()) {
            if (dto.getShiftCode() == null) continue;

            ShiftTypeEmployee shift = shiftTypeByCode.get(dto.getShiftCode());
            if (shift != null) applyAttendanceStatus(dto, shift);
        }

        // 8) Index
        AtomicInteger index = new AtomicInteger(0);
        map.values().forEach(d -> d.setNo(index.incrementAndGet()));

        // 9) return theo workDate
        return map.values().stream()
                .filter(dto -> !dto.getScanDate().isBefore(from) && !dto.getScanDate().isAfter(to))
                .toList();
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
    @Transactional
    public TimeAttendanceLogDto processScanAuto(String input) {

        Employee emp = employeeRepo.findByMscnId1(input);
        if (emp == null) {
            throw new RuntimeException("Mã không tồn tại: " + input);
        }

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // 1) xác định workDate theo shift (fix ca đêm)
        LocalDate workDate = resolveWorkDate(emp.getEmployeeId(), today, nowTime);

        // 2) lấy log theo workDate (với ca đêm phải xét cả day+1 cho OUT)
        // - Cách đơn giản: lấy range [workDate, workDate+1]
        List<TimeAttendanceLog> logs =
                logRepo.findByScanDateRange(workDate, workDate.plusDays(1));

        // lọc riêng nhân viên này và gộp theo workDate
        boolean hasIn = logs.stream()
                .anyMatch(l -> l.getEmployeeId() == emp.getEmployeeId()
                        && l.getScanAction() == ScanAction.IN
                        && l.getScanDateTime().toLocalDate().equals(workDate));

        boolean hasOut = logs.stream()
                .anyMatch(l -> l.getEmployeeId() == emp.getEmployeeId()
                                && l.getScanAction() == ScanAction.OUT
                                && (
                                // OUT thường nằm workDate
                                l.getScanDateTime().toLocalDate().equals(workDate)
                                        // OUT ca đêm có thể nằm day+1 nhưng thuộc workDate
                                        || l.getScanDateTime().toLocalDate().equals(workDate.plusDays(1))
                        )
                );

        ScanAction action;
        if (!hasIn) action = ScanAction.IN;
        else if (!hasOut) action = ScanAction.OUT;
        else throw new RuntimeException("Nhân viên đã đủ IN/OUT cho ngày " + workDate);

        // 3) tạo log - lưu DB đúng ngày thực tế (realDate)
        LocalDate realDate = resolveRealDateForSave(emp.getEmployeeId(), workDate, today, nowTime, action);

        TimeAttendanceLog log = new TimeAttendanceLog();
        log.setEmployeeId(emp.getEmployeeId());
        log.setScanAction(action);
        log.setScanDateTime(LocalDateTime.of(realDate, nowTime));
        log.setCreatedAt(LocalDateTime.now());
        log.setScanMethod(ScanMethod.MANUAL);

        logRepo.insert(log);

        // 4) map DTO trả về (dto.scanDate nên là workDate)
        TimeAttendanceLogDto dto = new TimeAttendanceLogDto();
        dto.setEmployeeId(emp.getEmployeeId());
        dto.setMscnId1(emp.getMSCNID1());
        dto.setFullName(emp.getFullName());
        dto.setScanDate(workDate);

        if (action == ScanAction.IN) dto.setIn(nowTime.toString());
        else dto.setOut(nowTime.toString());

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
            LocalDate workDate,   // ngày hiển thị trên UI
            String time,
            ScanAction action
    ) {
        LocalTime localTime;
        try {
            localTime = LocalTime.parse(time);
        } catch (Exception e) {
            throw new RuntimeException("Sai định dạng giờ (HH:mm)");
        }

        // 1️⃣ Lấy SHIFT
        String shiftCode =
                shiftPlanEmployeeRepo
                        .findShiftCodeByEmployeeAndDate(employeeId, workDate);

        ShiftTypeEmployee shift = shiftCode != null
                ? shiftTypeEmployeeRepo.findByCode(shiftCode)
                : null;

        // 2️⃣ Xác định NGÀY LƯU DB
        LocalDate realDate = workDate;

        if (shift != null
                && Boolean.TRUE.equals(shift.getIsOvernight())
                && action == ScanAction.OUT) {

            realDate = workDate.plusDays(1); // 🔥 OUT ca đêm
        }

        LocalDateTime scanDateTime =
                LocalDateTime.of(realDate, localTime);

        // 3️⃣ TÌM LOG EXISTING (THEO REAL DATE)
        TimeAttendanceLog existing =
                logRepo.findByEmployeeIdDateAndAction(
                        employeeId,
                        realDate,
                        action
                );

        if (existing != null) {
            existing.setScanDateTime(scanDateTime);
            existing.setScanMethod(ScanMethod.MANUAL);
            logRepo.update(existing);
        } else {
            TimeAttendanceLog log = new TimeAttendanceLog();
            log.setEmployeeId(employeeId);
            log.setScanAction(action);
            log.setScanDateTime(scanDateTime);
            log.setCreatedAt(LocalDateTime.now());
            log.setScanMethod(ScanMethod.MANUAL);
            logRepo.insert(log);
        }
    }



    public void applyAttendanceStatus(TimeAttendanceLogDto dto, ShiftTypeEmployee shift) {

        final int WINDOW_MIN = 30;

        LocalDate workDate = dto.getScanDate();
        if (workDate == null || shift == null) return;

        boolean overnight = Boolean.TRUE.equals(shift.getIsOvernight());

        // planned datetime
        LocalDateTime shiftStartDT = LocalDateTime.of(workDate, shift.getStartTime());
        LocalDateTime shiftEndDT   = overnight
                ? LocalDateTime.of(workDate.plusDays(1), shift.getEndTime())
                : LocalDateTime.of(workDate, shift.getEndTime());

        // ========= IN =========
        // ========= IN =========
        if (dto.getIn() != null && !dto.getIn().isBlank()) {
            LocalTime inTime = LocalTime.parse(dto.getIn());
            LocalDateTime inDT = LocalDateTime.of(workDate, inTime);

            LocalDateTime inMin = shiftStartDT.minusMinutes(WINDOW_MIN); // mốc 30p trước
            LocalDateTime inMax = shiftStartDT;                          // giờ bắt đầu

            // ❌ đỏ nếu <= inMin  (tức 7:30 đỏ)
            if (!inDT.isAfter(inMin)) { // BEFORE or EQUAL
                dto.setInStatus(AttendanceTimeStatus.TOO_EARLY_IN);

                // ❌ đỏ nếu > inMax (tức sau giờ bắt đầu là trễ)
            } else if (inDT.isAfter(inMax)) {
                dto.setInStatus(AttendanceTimeStatus.LATE);

                // ✅ xanh nếu (inMin, inMax] => từ 7:31 đến 8:00
            } else {
                dto.setInStatus(AttendanceTimeStatus.OK);
            }
        }


        // ========= OUT =========
        if (dto.getOut() != null && !dto.getOut().isBlank()) {
            LocalTime outTime = LocalTime.parse(dto.getOut());

            LocalDate outDate = overnight ? workDate.plusDays(1) : workDate;
            LocalDateTime outDT = LocalDateTime.of(outDate, outTime);

            LocalDateTime endDT = shiftEndDT; // 17:00
            LocalDateTime okMaxDT = endDT.plusMinutes(29); // 17:29 (vẫn xanh)

            if (outDT.isBefore(endDT)) {
                dto.setOutStatus(AttendanceTimeStatus.EARLY);          // ❌ đỏ
            } else if (outDT.isAfter(okMaxDT)) {
                dto.setOutStatus(AttendanceTimeStatus.TOO_LATE_OUT);   // ❌ đỏ (>= 17:30)
            } else {
                dto.setOutStatus(AttendanceTimeStatus.OK);             // ✅ xanh (17:00..17:29)
            }
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


    public void recalculateStatus(TimeAttendanceLogDto dto) {

        String shiftCode =
                shiftPlanEmployeeRepo
                        .findShiftCodeByEmployeeAndDate(
                                dto.getEmployeeId(),
                                dto.getScanDate()
                        );

        if (shiftCode == null) return;

        ShiftTypeEmployee shift =
                shiftTypeEmployeeRepo.findByCode(shiftCode);

        if (shift != null) {
            applyAttendanceStatus(dto, shift);
        }
    }


    @Override
    public List<EmployeeScanViewDto> getTodayScannedForEmployeeView() {

        LocalDate today = LocalDate.now();

        // ✅ chỉ lấy log hôm nay
        List<TimeAttendanceLog> logs =
                logRepo.findByScanDateRange(today, today.plusDays(1));

        if (logs.isEmpty()) {
            return Collections.emptyList();
        }

        // ✅ map employee
        Map<Integer, Employee> employeeMap =
                employeeRepo.findByIds(
                        logs.stream()
                                .map(TimeAttendanceLog::getEmployeeId)
                                .distinct()
                                .toList()
                ).stream().collect(Collectors.toMap(
                        Employee::getEmployeeId,
                        e -> e
                ));

        AtomicInteger index = new AtomicInteger(0);

        return logs.stream()
                /*// 👉 chỉ show IN (khuyến nghị)
                .filter(l -> l.getScanAction() == ScanAction.IN)*/

                // 👉 sort theo giờ scan
                .sorted(Comparator.comparing(TimeAttendanceLog::getScanDateTime))

                .map(log -> {
                    Employee emp = employeeMap.get(log.getEmployeeId());
                    if (emp == null) return null;

                    EmployeeScanViewDto dto = new EmployeeScanViewDto();
                    dto.setNo(index.incrementAndGet());
                    dto.setEmployeeCode(emp.getMSCNID1());
                    dto.setId(log.getLogId());
                    dto.setFullName(emp.getFullName());
                    dto.setScanType(log.getScanAction().name());
                    dto.setScanTime(
                            log.getScanDateTime().toLocalTime().toString()
                    );
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public AttendanceSummaryDto getAttendanceSummary(LocalDate date) {

        if (date == null) date = LocalDate.now();

        // 1) Active employees theo ngày
        List<Employee> activeEmployees = employeeRepo.findAllActiveByDate(date);
        Set<Integer> activeIds = activeEmployees.stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toSet());

        // 2) Shift plan của ngày (ai có ca gì)
        Map<Integer, ShiftPlanEmployee> planMap =
                shiftPlanEmployeeRepo.findByShiftDate(date).stream()
                        .collect(Collectors.toMap(
                                ShiftPlanEmployee::getEmployeeId,
                                p -> p,
                                (a, b) -> a
                        ));

        // 3) IN trong ngày (chỉ lấy IN)
        // (hiện tại bạn chưa có repo method distinct IN -> tạm dùng cách này)
        Set<Integer> inIds = logRepo.findByScanDateRange(date, date).stream()
                .filter(l -> l.getScanAction() == ScanAction.IN)
                .map(TimeAttendanceLog::getEmployeeId)
                .collect(Collectors.toSet());

        // 4) Phân loại theo shiftCode: DAY / NIGHT / NA
        // -> xác định DAY/NIGHT bằng ShiftTypeEmployee (không hardcode giờ)
        // -> Cache shift type theo code để giảm query
        Map<String, ShiftTypeEmployee> shiftTypeCache = new HashMap<>();

        int dayTotal = 0, dayPresent = 0;
        int nightTotal = 0, nightPresent = 0;
        int naTotal = 0, naPresent = 0;

        for (Integer empId : activeIds) {

            ShiftPlanEmployee plan = planMap.get(empId);
            String shiftCode = (plan != null) ? plan.getShiftCode() : null;

            boolean hasIn = inIds.contains(empId);

            // Không có ca
            if (shiftCode == null || shiftCode.isBlank()) {
                naTotal++;
                if (hasIn) naPresent++;
                continue;

            }

            // Có ca -> lấy shiftType
            ShiftTypeEmployee st = shiftTypeCache.computeIfAbsent(
                    shiftCode,
                    c -> shiftTypeEmployeeRepo.findByCode(c)
            );

            // Nếu shiftCode lạ / không tồn tại trong ShiftTypeEmployee -> coi như NA
            if (st == null) {
                naTotal++;
                if (hasIn) naPresent++;
                continue;
            }

            // ===== Phân nhóm ca ngày / ca đêm =====
            // Cách phân: nếu isOvernight = true => ca đêm, ngược lại ca ngày
            if (Boolean.TRUE.equals(st.getIsOvernight())) {
                nightTotal++;
                if (hasIn) nightPresent++;
            } else {
                dayTotal++;
                if (hasIn) dayPresent++;
            }
        }

        AttendanceSummaryDto dto = new AttendanceSummaryDto();

        dto.setDayTotal(dayTotal);
        dto.setDayPresent(dayPresent);
        dto.setDayAbsent(dayTotal - dayPresent);

        dto.setNightTotal(nightTotal);
        dto.setNightPresent(nightPresent);
        dto.setNightAbsent(nightTotal - nightPresent);

        dto.setNaTotal(naTotal);
        dto.setNaPresent(naPresent);
        dto.setNaAbsent(naTotal - naPresent);

        return dto;
    }


    private LocalDate resolveWorkDate(int employeeId, LocalDate today, LocalTime nowTime) {
        String shiftCode = shiftPlanEmployeeRepo.findShiftCodeByEmployeeAndDate(employeeId, today);
        ShiftTypeEmployee shift = shiftCode == null ? null : shiftTypeEmployeeRepo.findByCode(shiftCode);

        if (shift != null && Boolean.TRUE.equals(shift.getIsOvernight())) {
            // ví dụ endTime 04:00 => từ 00:00 đến trước 04:00 thuộc OUT của hôm qua
            if (nowTime.isBefore(shift.getEndTime())) {
                return today.minusDays(1);
            }
        }
        return today;
    }

    private LocalDate resolveRealDateForSave(int employeeId, LocalDate workDate, LocalDate today, LocalTime nowTime, ScanAction action) {

        // realDate = workDate thường
        LocalDate realDate = workDate;

        String shiftCode = shiftPlanEmployeeRepo.findShiftCodeByEmployeeAndDate(employeeId, workDate);
        ShiftTypeEmployee shift = shiftCode == null ? null : shiftTypeEmployeeRepo.findByCode(shiftCode);

        // nếu ca đêm và đang OUT => lưu qua ngày +1
        if (shift != null && Boolean.TRUE.equals(shift.getIsOvernight()) && action == ScanAction.OUT) {
            realDate = workDate.plusDays(1);
        }

        return realDate;
    }


    @Override
    @Transactional
    public AttendanceImportResult importAttendanceFromExcel(File file, LocalDate workDate) {

        if (file == null) throw new RuntimeException("File import null");
        if (workDate == null) throw new RuntimeException("Ngày import không hợp lệ");

        List<String> messages = new ArrayList<>();
        int ok = 0, skip = 0, err = 0;

        List<AttendanceRow> rows = readAttendanceExcel(file); // bạn implement hàm này bên dưới

        for (AttendanceRow r : rows) {
            try {
                // ===== validate cơ bản =====
                if (isBlank(r.employeeCode)) {
                    skip++;
                    messages.add("Row " + r.rowIndex + ": EmployeeCode trống");
                    continue;
                }

                Employee emp = employeeRepo.findByMscnId1(r.employeeCode.trim());
                if (emp == null) {
                    skip++;
                    messages.add("Row " + r.rowIndex + ": Không tìm thấy mã NV: " + r.employeeCode);
                    continue;
                }

                // ===== check tên để tránh import sai người =====
                if (!isBlank(r.fullName)) {
                    String fileName = normalizeName(r.fullName);
                    String sysName = normalizeName(emp.getFullName());
                    if (!fileName.equals(sysName)) {
                        skip++;
                        messages.add("Row " + r.rowIndex + ": Tên không khớp. File='" + r.fullName
                                + "' | Sys='" + emp.getFullName() + "'");
                        continue;
                    }
                }

                // ===== upsert shift plan (nếu có shift) =====
                if (!isBlank(r.shiftCode)) {
                    // ghi note để trace
                    shiftPlanEmployeeRepo.saveOrUpdate(emp.getEmployeeId(), workDate, r.shiftCode.trim(), "IMPORT EXCEL");
                    // nếu bạn không có repo saveOrUpdate, dùng service:
                    // shiftPlanEmployeeService.saveOrUpdate(emp.getEmployeeId(), workDate, r.shiftCode.trim(), "IMPORT EXCEL");
                }

                // ===== upsert IN/OUT bằng manualFixAttendance (đã support ca đêm) =====
                if (!isBlank(r.inTime)) {
                    manualFixAttendance(emp.getEmployeeId(), workDate, normalizeTime(r.inTime), ScanAction.IN);
                }

                if (!isBlank(r.outTime)) {
                    manualFixAttendance(emp.getEmployeeId(), workDate, normalizeTime(r.outTime), ScanAction.OUT);
                }

                ok++;

            } catch (Exception ex) {
                err++;
                messages.add("Row " + r.rowIndex + ": " + ex.getMessage());
            }
        }

        return new AttendanceImportResult(ok, skip, err, messages);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isBlank();
    }

    private String normalizeName(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** chuẩn hoá về HH:mm (accept HH:mm:ss hoặc H:mm) */
    private String normalizeTime(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isBlank()) return null;

        // cho phép "HH:mm:ss" -> "HH:mm"
        if (s.matches("^\\d{1,2}:\\d{2}:\\d{2}$")) {
            return s.substring(0, 5);
        }
        // nếu "H:mm" -> LocalTime parse vẫn ok
        // manualFixAttendance dùng LocalTime.parse => cần "HH:mm" hoặc "H:mm"
        return s;
    }

    /** row model đơn giản */
    private static class AttendanceRow {
        int rowIndex;
        String employeeCode;
        String fullName;
        String shiftCode;
        String inTime;
        String outTime;
    }

    private List<AttendanceRow> readAttendanceExcel(File file) {
        try (var fis = new java.io.FileInputStream(file);
             var wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {

            var sheet = wb.getSheetAt(0);
            List<AttendanceRow> rows = new ArrayList<>();

            // bắt đầu từ row 1 nếu row 0 là header
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                var row = sheet.getRow(i);
                if (row == null) continue;

                AttendanceRow r = new AttendanceRow();
                r.rowIndex = i + 1; // excel line

                r.employeeCode = readString(row.getCell(0)); // A
                r.fullName     = readString(row.getCell(1)); // B
                r.shiftCode    = readString(row.getCell(2)); // C
                r.inTime       = readTime(row.getCell(3));   // D
                r.outTime      = readTime(row.getCell(4));   // E

                // bỏ qua dòng rỗng hoàn toàn
                if (isBlank(r.employeeCode) && isBlank(r.fullName) && isBlank(r.shiftCode)
                        && isBlank(r.inTime) && isBlank(r.outTime)) {
                    continue;
                }

                rows.add(r);
            }

            return rows;

        } catch (Exception e) {
            throw new RuntimeException("Đọc Excel thất bại: " + e.getMessage(), e);
        }
    }

    private String readString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        String s = cell.getStringCellValue();
        return s == null ? null : s.trim();
    }

    private String readTime(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;

        // cell time dạng numeric
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {

            var t = cell.getLocalDateTimeCellValue().toLocalTime();
            return t.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }

        // dạng string
        String s = cell.toString();
        return (s == null) ? null : s.trim();
    }



}