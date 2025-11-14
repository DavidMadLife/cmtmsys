package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.springframework.jdbc.core.RowMapper;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Generic RowMapper giúp tự động map cột → thuộc tính theo tên.
 * Cho phép override xử lý đặc biệt cho enum, date, v.v.
 */
public abstract class BaseRowMapper<T> implements RowMapper<T> {

    private final Class<T> mappedClass;

    protected BaseRowMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            T instance = mappedClass.getDeclaredConstructor().newInstance();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = rs.getObject(i);

                // Cho phép custom xử lý từng field
                Object processedValue = processColumn(columnName, value);
                if (processedValue == null) processedValue = value;

                setProperty(instance, columnName, processedValue);
            }

            return instance;
        } catch (Exception e) {
            throw new SQLException("Failed to map row for " + mappedClass.getSimpleName(), e);
        }
    }

    /**
     * Dành cho subclass override để xử lý logic riêng (ví dụ enum, date, v.v.)
     */
    protected Object processColumn(String columnName, Object value) {
        return value;
    }

    private void setProperty(T instance, String columnName, Object value) {
        String fieldName = toCamelCase(columnName);
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, mappedClass);
            Method setter = pd.getWriteMethod();
            if (setter != null) {
                setter.invoke(instance, value);
            }
        } catch (Exception ex) {
            System.out.println("[WARN] Skip column: " + columnName + " → field " + fieldName);
        }
    }


    private String toCamelCase(String columnName) {
        // Hỗ trợ cả "ProductID", "product_id", "PRODUCT_ID"
        if (columnName == null || columnName.isEmpty()) return columnName;
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;

        for (int i = 0; i < columnName.length(); i++) {
            char c = columnName.charAt(i);
            if (c == '_' || c == ' ') {
                upperNext = true;
            } else {
                if (i == 0) {
                    sb.append(Character.toLowerCase(c));
                } else if (upperNext) {
                    sb.append(Character.toUpperCase(c));
                    upperNext = false;
                } else {
                    sb.append(c);
                }
            }
        }

        // Xử lý trường hợp PascalCase: ProductID -> productID
        if (Character.isUpperCase(columnName.charAt(0)) && !columnName.contains("_")) {
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        }

        return sb.toString();
    }

}
