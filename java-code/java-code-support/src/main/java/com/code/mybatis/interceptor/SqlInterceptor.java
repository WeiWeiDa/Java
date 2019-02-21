package com.code.mybatis.interceptor;

import com.alibaba.fastjson.JSON;
import com.mchange.v1.lang.BooleanUtils;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.DefaultParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

@Intercepts(value = {
        @Signature(type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class})})
public class SqlInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlInterceptor.class);
    private static final String CLASS_NAME = SqlInterceptor.class.getSimpleName();

    /**
     * 是否输出SQL
     */
    private boolean isLogSql;
    /**
     * 是否校验SQL语法
     */
    private boolean isValidateSql;
    /**
     * 是否执行SQL计划
     */
    private boolean isExplainSql;
    /**
     * 全表语句是否停止执行
     */
    private boolean isStopProceed;
    /**
     * 是否输出SQL执行时间
     */
    private boolean isLogTimeCost;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = null;
        if (invocation.getTarget() instanceof Executor) {
            Method method = invocation.getMethod();
            //获取SQL语句
            String sql = getStatementSql(invocation);
            if (isLogSql) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("[{}] execute [{}] sql [{}]", CLASS_NAME, method.getName(), sql);
                }
            }
            //校验SQL语法
            if (isValidateSql) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("[{}] execute sql validate", CLASS_NAME);
                }
                try {
//                    SqlParserUtils.validate(sql);
                } catch (Throwable e) {
                    throw new SQLException("sql syntax analysis error", e);
                }
            }
            //SQL执行计划分析
            if (isExplainSql) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("[{}] execute sql explain", CLASS_NAME);
                }
                try {
                    sqlExplain(invocation);
                } catch (Throwable e) {
                    throw new SQLException("sql explain error", e);
                }
            }
            //监控SQL执行时间
            long start = System.currentTimeMillis();
            try {
                result = invocation.proceed();
            } catch (Throwable e) {
                throw e;
            } finally {
                long end = System.currentTimeMillis();
                if (isLogTimeCost) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("[{}] execute [{}] cost [{}] ms", CLASS_NAME, method.getName(), (end - start));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[{}] properties [{}]", CLASS_NAME, JSON.toJSONString(properties));
        }
        this.isLogSql = BooleanUtils.parseBoolean(properties.getProperty("isLogSql"));
        this.isValidateSql = BooleanUtils.parseBoolean(properties.getProperty("isValidateSql"));
        this.isExplainSql = BooleanUtils.parseBoolean(properties.getProperty("isExplainSql"));
        this.isStopProceed = BooleanUtils.parseBoolean(properties.getProperty("isStopProceed"));
        this.isLogTimeCost = BooleanUtils.parseBoolean(properties.getProperty("isLogTimeCost"));
    }

    /**
     * SQL计划执行
     *
     * @param invocation
     * @return
     * @throws SQLException
     */
    private void sqlExplain(Invocation invocation) throws SQLException {
        Executor executor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Configuration configuration = mappedStatement.getConfiguration();
        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Connection connection = executor.getTransaction().getConnection();

        StringBuilder explain = new StringBuilder("EXPLAIN ");
        explain.append(boundSql.getSql());
        String sqlExplain = explain.toString();
        StaticSqlSource sqlSource = new StaticSqlSource(configuration, sqlExplain, boundSql.getParameterMappings());
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, "explain_sql", sqlSource,
                SqlCommandType.SELECT);
        builder.resultMaps(mappedStatement.getResultMaps()).resultSetType(mappedStatement.getResultSetType())
                .statementType(mappedStatement.getStatementType());
        MappedStatement queryStatement = builder.build();
        DefaultParameterHandler handler = new DefaultParameterHandler(queryStatement, parameter, boundSql);
        try (PreparedStatement stmt = connection.prepareStatement(sqlExplain)) {
            handler.setParameters(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String extra = rs.getString("Extra");
                    if (!StringUtils.isEmpty(extra) && !"Using where".equals(extra)) {
                        if (isStopProceed) {
                            throw new SQLException("Error: Full table operation is prohibited. SQL: " + boundSql.getSql());
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    /**
     * SQL语句生成
     *
     * @param invocation
     * @return String
     */
    private String getStatementSql(Invocation invocation) {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();

        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql;
    }

    private String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(obj) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value;
    }

}
