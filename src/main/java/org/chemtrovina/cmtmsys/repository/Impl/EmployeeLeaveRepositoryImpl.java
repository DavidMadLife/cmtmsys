package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.EmployeeLeaveFilter;
import org.chemtrovina.cmtmsys.dto.LeaveStatisticDeptDto;
import org.chemtrovina.cmtmsys.model.EmployeeLeave;
import org.chemtrovina.cmtmsys.model.enums.LeaveType;
import org.chemtrovina.cmtmsys.repository.base.EmployeeLeaveRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EmployeeLeaveRepositoryImpl implements EmployeeLeaveRepository {

    private final JdbcTemplate jdbc;
    private final BeanPropertyRowMapper<EmployeeLeave> mapper =
            new BeanPropertyRowMapper<>(EmployeeLeave.class);

    public EmployeeLeaveRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =====================================================

    @Override
    public EmployeeLeave findByEmployeeAndDate(int employeeId, LocalDate date) {

        String sql = """
            SELECT *
            FROM EmployeeLeave
            WHERE EmployeeId = ?
              AND ? BETWEEN FromDate AND ToDate
        """;

        List<EmployeeLeave> list =
                jdbc.query(sql, mapper, employeeId, date);

        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<EmployeeLeave> findByEmployeeAndDateRange(
            int employeeId,
            LocalDate from,
            LocalDate to
    ) {

        String sql = """
            SELECT *
            FROM EmployeeLeave
            WHERE EmployeeId = ?
              AND FromDate <= ?
              AND ToDate >= ?
            ORDER BY FromDate
        """;

        return jdbc.query(sql, mapper, employeeId, to, from);
    }

    // =====================================================

    @Override
    public int insert(EmployeeLeave leave) {

        String sql = """
            INSERT INTO EmployeeLeave (
                EmployeeId,
                FromDate,
                ToDate,
                LeaveType,
                Reason,
                Description,
                CreatedBy,
                CreatedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        return jdbc.update(
                sql,
                leave.getEmployeeId(),
                leave.getFromDate(),
                leave.getToDate(),
                leave.getLeaveType().name(),
                leave.getReason(),
                leave.getDescription(),
                leave.getCreatedBy(),
                leave.getCreatedAt()
        );
    }

    @Override
    public int update(EmployeeLeave leave) {

        String sql = """
            UPDATE EmployeeLeave
            SET FromDate = ?,
                ToDate = ?,
                LeaveType = ?,
                Reason = ?,
                Description = ?
            WHERE LeaveId = ?
        """;

        return jdbc.update(
                sql,
                leave.getFromDate(),
                leave.getToDate(),
                leave.getLeaveType().name(),
                leave.getReason(),
                leave.getDescription(),
                leave.getLeaveId()
        );
    }

    @Override
    public int delete(int leaveId) {
        return jdbc.update(
                "DELETE FROM EmployeeLeave WHERE LeaveId = ?",
                leaveId
        );
    }

    @Override
    public List<LeaveStatisticDeptDto> statisticByDepartment(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        System.out.println("==== STATISTIC BY DEPARTMENT ====");
        System.out.println("FromDate = " + fromDate);
        System.out.println("ToDate   = " + toDate);

        String sql = """
            SELECT
                e.DepartmentName AS departmentName,
            
                COUNT(DISTINCT CASE
                    WHEN l.LeaveType = N'Nghỉ_phép'
                    THEN l.EmployeeId END) AS leavePermit,
            
                COUNT(DISTINCT CASE
                    WHEN l.LeaveType = N'Nghỉ_không_phép'
                    THEN l.EmployeeId END) AS leaveNoPermit,
            
                COUNT(DISTINCT CASE
                    WHEN l.LeaveType = N'Nghỉ_bệnh'
                    THEN l.EmployeeId END) AS leaveSick,
            
                COUNT(DISTINCT CASE
                    WHEN l.LeaveType = N'Việc_riêng'
                    THEN l.EmployeeId END) AS leavePrivate,
            
                COUNT(DISTINCT CASE
                    WHEN l.LeaveType = N'Khác'
                    THEN l.EmployeeId END) AS leaveOther
            
            FROM Employee e
            LEFT JOIN EmployeeLeave l
                   ON l.EmployeeId = e.EmployeeId
                  AND ( ? IS NULL OR l.FromDate <= ? )
                  AND ( ? IS NULL OR l.ToDate   >= ? )
            
            WHERE e.DepartmentName IS NOT NULL
              AND e.DepartmentName <> ''
            
            GROUP BY e.DepartmentName
            ORDER BY e.DepartmentName
            """;

        return jdbc.query(
                sql,
                (rs, i) -> {
                    LeaveStatisticDeptDto dto = new LeaveStatisticDeptDto();
                    dto.setDepartmentName(rs.getString("departmentName"));
                    dto.setLeavePermit(rs.getInt("leavePermit"));
                    dto.setLeaveNoPermit(rs.getInt("leaveNoPermit"));
                    dto.setLeaveSick(rs.getInt("leaveSick"));
                    dto.setLeavePrivate(rs.getInt("leavePrivate"));
                    dto.setLeaveOther(rs.getInt("leaveOther"));
                    return dto;
                },
                toDate, toDate,   // <= toDate
                fromDate, fromDate // >= fromDate
        );

    }

    public List<EmployeeLeave> findByFilter(EmployeeLeaveFilter f) {

        StringBuilder sql = new StringBuilder("""
        SELECT
            LeaveId,
            EmployeeId,
            FromDate,
            ToDate,
            LeaveType,
            Reason,
            Description,
            CreatedAt
        FROM EmployeeLeave
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (f.getEmployeeId() != null) {
            sql.append(" AND EmployeeId = ?");
            params.add(f.getEmployeeId());
        }

        if (f.getLeaveType() != null) {
            sql.append(" AND LeaveType = ?");
            params.add(f.getLeaveType().name());
        }

        // ✅ lọc theo khoảng ngày (chỉ khi có)
        if (f.getFromDate() != null && f.getToDate() != null) {
            sql.append(" AND FromDate <= ? AND ToDate >= ?");
            params.add(f.getToDate());
            params.add(f.getFromDate());

        } else if (f.getFromDate() != null) {
            sql.append(" AND ToDate >= ?");
            params.add(f.getFromDate());

        } else if (f.getToDate() != null) {
            sql.append(" AND FromDate <= ?");
            params.add(f.getToDate());
        }

        sql.append(" ORDER BY FromDate DESC");

        return jdbc.query(
                sql.toString(),
                (rs, i) -> {
                    EmployeeLeave l = new EmployeeLeave();
                    l.setLeaveId(rs.getInt("LeaveId"));
                    l.setEmployeeId(rs.getInt("EmployeeId"));
                    l.setFromDate(rs.getDate("FromDate").toLocalDate());
                    l.setToDate(rs.getDate("ToDate").toLocalDate());
                    l.setLeaveType(LeaveType.valueOf(rs.getString("LeaveType")));
                    l.setReason(rs.getString("Reason"));
                    l.setDescription(rs.getString("Description"));
                    return l;
                },
                params.toArray()
        );
    }



    @Override
    public List<EmployeeLeave> findLeaveByDate(LocalDate date) {

        String sql = """
            SELECT *
            FROM EmployeeLeave
            WHERE FromDate <= ?
              AND ToDate   >= ?
        """;

        return jdbc.query(
                sql,
                (rs, i) -> {
                    EmployeeLeave l = new EmployeeLeave();
                    l.setLeaveId(rs.getInt("LeaveId"));
                    l.setEmployeeId(rs.getInt("EmployeeId"));
                    l.setFromDate(rs.getDate("FromDate").toLocalDate());
                    l.setToDate(rs.getDate("ToDate").toLocalDate());
                    l.setLeaveType(
                            LeaveType.valueOf(rs.getString("LeaveType"))
                    );
                    l.setReason(rs.getString("Reason"));
                    l.setDescription(rs.getString("Description"));
                    return l;
                },
                date,
                date
        );
    }

}
