/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import oracle.jdbc.LogicalTransactionId;
import oracle.jdbc.LogicalTransactionIdEventListener;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleOCIFailover;
import oracle.jdbc.OracleSavepoint;
import oracle.jdbc.OracleShardingKey;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQNotificationRegistration;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import oracle.jdbc.pool.OracleConnectionCacheCallback;
import oracle.sql.ARRAY;
import oracle.sql.BINARY_DOUBLE;
import oracle.sql.BINARY_FLOAT;
import oracle.sql.DATE;
import oracle.sql.INTERVALDS;
import oracle.sql.INTERVALYM;
import oracle.sql.NUMBER;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPLTZ;
import oracle.sql.TIMESTAMPTZ;
import oracle.sql.TypeDescriptor;

/**
 *
 * @author sdelucero
 */
public class MyConnection implements Connection {
    private OracleConnection oraConn;
    public MyConnection(OracleConnection oraConn) {
        this.oraConn = oraConn;
    }
    
    public boolean isProxySession() {
        return oraConn.isProxySession();
    }
    public void openProxySession(int i, Properties prprts) throws SQLException {
        oraConn.openProxySession(i, prprts);
        
    }
    
    public void close(int i) throws SQLException {
        oraConn.close(i);
    }
    @Override
    public Statement createStatement() throws SQLException {
        return oraConn.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return oraConn.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return oraConn.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return oraConn.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        oraConn.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return oraConn.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        oraConn.commit();
    }

    @Override
    public void rollback() throws SQLException {
        oraConn.rollback();
    }

    @Override
    public void close() throws SQLException {
        oraConn.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return oraConn.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return oraConn.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        oraConn.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return oraConn.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        oraConn.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return oraConn.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        oraConn.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return oraConn.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return oraConn.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        oraConn.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return oraConn.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return oraConn.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return oraConn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return oraConn.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        oraConn.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        oraConn.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return oraConn.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return oraConn.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return oraConn.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        oraConn.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        oraConn.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return oraConn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return oraConn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return oraConn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return oraConn.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return oraConn.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return oraConn.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return oraConn.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return oraConn.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return oraConn.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return oraConn.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        boolean valid =  oraConn.isValid(timeout);
        return valid;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        oraConn.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        oraConn.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return oraConn.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return oraConn.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return oraConn.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return oraConn.createStruct(typeName, attributes);
    }
    
    @Override
    public void setSchema(String schema) throws SQLException {
        oraConn.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return oraConn.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        oraConn.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        oraConn.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return oraConn.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return oraConn.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return oraConn.isWrapperFor(iface);
    }
    
}
