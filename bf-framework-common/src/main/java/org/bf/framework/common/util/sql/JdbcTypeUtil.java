package org.bf.framework.common.util.sql;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.common.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.TimeZone;

@Slf4j
public class JdbcTypeUtil {

    public final static String  timeZone;    // 当前时区

    static {
        TimeZone localTimeZone = TimeZone.getDefault();
        int rawOffset = localTimeZone.getRawOffset();
        String symbol = "+";
        if (rawOffset < 0) {
            symbol = "-";
        }
        rawOffset = Math.abs(rawOffset);
        int offsetHour = rawOffset / 3600000;
        int offsetMinute = rawOffset % 3600000 / 60000;
        String hour = String.format("%1$02d", offsetHour);
        String minute = String.format("%1$02d", offsetMinute);
        timeZone = symbol + hour + ":" + minute;
        TimeZone.setDefault(TimeZone.getTimeZone("GMT" + timeZone));
    }
    public static Object getRSData(ResultSet rs, String columnName, int jdbcType) throws SQLException {
        if (jdbcType == Types.BIT || jdbcType == Types.BOOLEAN) {
            return rs.getByte(columnName);
        } else {
            return rs.getObject(columnName);
        }
    }

    public static Object getRSData(ResultSet rs, int column, int jdbcType) throws SQLException {
        if (jdbcType == Types.BIT || jdbcType == Types.BOOLEAN) {
            return rs.getByte(column);
        } else {
            return rs.getObject(column);
        }
    }

    public static Class<?> jdbcType2javaType(int jdbcType) {
        switch (jdbcType) {
            case Types.BIT:
            case Types.BOOLEAN:
                // return Boolean.class;
            case Types.TINYINT:
                return Byte.TYPE;
            case Types.SMALLINT:
                return Short.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.BIGINT:
                return Long.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return BigDecimal.class;
            case Types.REAL:
                return Float.class;
            case Types.FLOAT:
            case Types.DOUBLE:
                return Double.class;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return String.class;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return byte[].class;
            case Types.DATE:
                return Date.class;
            case Types.TIME:
                return Time.class;
            case Types.TIMESTAMP:
                return Timestamp.class;
            default:
                return String.class;
        }
    }

    private static boolean isText(String columnType) {
        return "LONGTEXT".equalsIgnoreCase(columnType) || "MEDIUMTEXT".equalsIgnoreCase(columnType)
               || "TEXT".equalsIgnoreCase(columnType) || "TINYTEXT".equalsIgnoreCase(columnType);
    }

    public static Object typeConvert(String columnName, String value, int sqlType, String mysqlType) {
        if (value == null
            || (value.equals("") && !(isText(mysqlType) || sqlType == Types.CHAR || sqlType == Types.VARCHAR || sqlType == Types.LONGVARCHAR))) {
            return null;
        }

        try {
            Object res;
            switch (sqlType) {
                case Types.INTEGER:
                    res = Integer.parseInt(value);
                    break;
                case Types.SMALLINT:
                    res = Short.parseShort(value);
                    break;
                case Types.BIT:
                case Types.TINYINT:
                    res = Byte.parseByte(value);
                    break;
                case Types.BIGINT:
                    if (mysqlType.startsWith("bigint") && mysqlType.endsWith("unsigned")) {
                        res = new BigInteger(value);
                    } else {
                        res = Long.parseLong(value);
                    }
                    break;
                // case Types.BIT:
                case Types.BOOLEAN:
                    res = !"0".equals(value);
                    break;
                case Types.DOUBLE:
                case Types.FLOAT:
                    res = Double.parseDouble(value);
                    break;
                case Types.REAL:
                    res = Float.parseFloat(value);
                    break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                    res = new BigDecimal(value);
                    break;
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.BLOB:
                    res = value.getBytes("ISO-8859-1");
                    break;
                case Types.DATE:
                    if (!value.startsWith("0000-00-00")) {
                        java.util.Date date = parseDate(value);
                        if (date != null) {
                            res = new Date(date.getTime());
                        } else {
                            res = null;
                        }
                    } else {
                        res = null;
                    }
                    break;
                case Types.TIME: {
                    java.util.Date date = parseDate(value);
                    if (date != null) {
                        res = new Time(date.getTime());
                    } else {
                        res = null;
                    }
                    break;
                }
                case Types.TIMESTAMP:
                    if (!value.startsWith("0000-00-00")) {
                        java.util.Date date = parseDate(value);
                        if (date != null) {
                            res = new Timestamp(date.getTime());
                        } else {
                            res = null;
                        }
                    } else {
                        res = null;
                    }
                    break;
                case Types.CLOB:
                default:
                    res = value;
                    break;
            }
            return res;
        } catch (Exception e) {
            log.error("column: {}, failed convert type {} to {}", columnName, value, sqlType);
            return value;
        }
    }

    public static java.util.Date parseDate(String datetimeStr) {
        if (StringUtils.isBlank(datetimeStr)) {
            return null;
        }
        datetimeStr = datetimeStr.trim();
        if (datetimeStr.contains("-")) {
            if (datetimeStr.contains(":")) {
                datetimeStr = datetimeStr.replace(" ", "T");
            }
        } else if (datetimeStr.contains(":")) {
            datetimeStr = "T" + datetimeStr;
        }
        DateTime dateTime = new DateTime(DateUtil.parseUTC(datetimeStr));
        return dateTime.toSqlDate();
    }
}
