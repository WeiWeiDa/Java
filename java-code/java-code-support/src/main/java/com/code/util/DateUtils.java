package com.code.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hengjian
 * @date 2019/2/25
 */
public final class DateUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

    public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * @param dateStr 要验证的日期字符串
     * @return boolean
     */
    public static boolean isValidDate(String dateStr) {
        return parseDate(dateStr, DEFAULT_DATE_FORMAT_PATTERN) != null;
    }

    /**
     * @param dateStr 要验证的日期字符串
     * @param pattern 日期格式
     * @return boolean
     */
    public static boolean isValidDate(String dateStr, String pattern) {
        return parseDate(dateStr, pattern) != null;
    }

    /**
     * @param dateStr 要解析的日期字符串
     * @return java.util.Date
     */
    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, DEFAULT_DATE_FORMAT_PATTERN);
    }

    /**
     * @param date 要格式化的日期
     * @return java.lang.String
     */
    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_DATE_FORMAT_PATTERN);
    }

    /**
     * @param dateStr 要解析的日期字符串
     * @param pattern 日期格式
     * @return java.util.Date
     */
    public static Date parseDate(String dateStr, String pattern) {
        try {
            return DateUtils.DateFormatHolder.getDateFormat(pattern).parse(dateStr);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * @param date    要格式化的日期
     * @param pattern 日期格式
     * @return java.lang.String
     */
    public static String formatDate(Date date, String pattern) {
        try {
            return DateUtils.DateFormatHolder.getDateFormat(pattern).format(date);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private static final class DateFormatHolder {
        private static final Map<String, SimpleDateFormat> dateFormatCache = new ConcurrentHashMap<String, SimpleDateFormat>();

        public static SimpleDateFormat getDateFormat(String pattern) {
            SimpleDateFormat simpleDateFormat = dateFormatCache.get(pattern);
            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat(pattern);
                dateFormatCache.put(pattern, simpleDateFormat);
            }
            return simpleDateFormat;
        }
    }
}
