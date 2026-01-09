package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.EmployeeLeaveFilter;
import org.chemtrovina.cmtmsys.dto.LeaveStatisticDeptDto;
import org.chemtrovina.cmtmsys.model.EmployeeLeave;
import org.chemtrovina.cmtmsys.repository.base.EmployeeLeaveRepository;
import org.chemtrovina.cmtmsys.service.base.EmployeeLeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeLeaveServiceImpl implements EmployeeLeaveService {

    private final EmployeeLeaveRepository leaveRepo;

    public EmployeeLeaveServiceImpl(EmployeeLeaveRepository leaveRepo) {
        this.leaveRepo = leaveRepo;
    }

    // =====================================================
    // QUERY
    // =====================================================

    @Override
    public EmployeeLeave getLeaveByEmployeeAndDate(
            int employeeId,
            LocalDate date
    ) {
        return leaveRepo.findByEmployeeAndDate(employeeId, date);
    }

    @Override
    public List<EmployeeLeave> getLeavesByEmployeeAndRange(
            int employeeId,
            LocalDate from,
            LocalDate to
    ) {
        return leaveRepo.findByEmployeeAndDateRange(
                employeeId, from, to
        );
    }

    // =====================================================
    // CRUD
    // =====================================================

    @Override
    @Transactional
    public void create(EmployeeLeave leave) {

        if (leave == null) return;

        if (leave.getFromDate() == null || leave.getToDate() == null) {
            throw new RuntimeException("Thiếu ngày nghỉ");
        }

        if (leave.getFromDate().isAfter(leave.getToDate())) {
            throw new RuntimeException("Từ ngày không được lớn hơn đến ngày");
        }

        leave.setCreatedAt(LocalDateTime.now());

        leaveRepo.insert(leave);
    }

    @Override
    @Transactional
    public void update(EmployeeLeave leave) {

        if (leave == null || leave.getLeaveId() <= 0) return;

        if (leave.getFromDate().isAfter(leave.getToDate())) {
            throw new RuntimeException("Từ ngày không được lớn hơn đến ngày");
        }

        leaveRepo.update(leave);
    }

    @Override
    @Transactional
    public void delete(int leaveId) {
        if (leaveId <= 0) return;
        leaveRepo.delete(leaveId);
    }

    @Override
    public List<LeaveStatisticDeptDto> statisticByDepartment(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        return leaveRepo.statisticByDepartment(fromDate, toDate);
    }

    @Override
    public List<EmployeeLeave> findByFilter(EmployeeLeaveFilter filter) {
        return leaveRepo.findByFilter(filter);
    }

}
