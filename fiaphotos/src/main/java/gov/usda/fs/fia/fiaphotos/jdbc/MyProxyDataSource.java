/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariProxyConnection;
import gov.usda.fs.fia.fiaphotos.controller.UserSession;
import oracle.jdbc.pool.OracleDataSource;

import org.springframework.data.jdbc.support.ConnectionContextProvider;
import org.springframework.jdbc.datasource.SmartDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import java.io.PrintWriter;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.driver.LogicalConnection;
import org.springframework.data.jdbc.support.oracle.ProxyConnectionPreparer;
import org.springframework.jdbc.support.nativejdbc.JBossNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.Jdbc4NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.OracleJdbc4NativeJdbcExtractor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class MyProxyDataSource extends HikariDataSource {

    public MyProxyDataSource(HikariConfig config) {
        super(config);

    }

    public Connection getConnection() throws SQLException {
        Connection conn = super.getConnection();
        prepareConnection(conn, getProxyUsername());
        
        return conn;

    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = super.getConnection(username, password);
        prepareConnection(conn, getProxyUsername());
        
        return conn;
    }

    public void validateProxyUser(String proxyUser) throws SQLException {
        Connection conn = super.getConnection();
        try {
            prepareConnection(conn, proxyUser);
        }
        finally {
            conn.close();
        }
    }
    public void prepareConnection(Connection conn, String proxyUser) throws SQLException {

        if (conn.isWrapperFor(MyConnection.class)) {
            MyConnection myconn = (MyConnection) conn.unwrap(MyConnection.class);
            if (myconn.isProxySession()) {
                myconn.close(OracleConnection.PROXY_SESSION);
            }
            if (proxyUser != null) {
                Properties proxyProperties = new java.util.Properties();
                proxyProperties.setProperty(OracleConnection.PROXY_USER_NAME, proxyUser);
                try {
                    myconn.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, proxyProperties);
                }
                catch (SQLException sqle) {
                    try {
                        if (myconn.isProxySession()) {
                            myconn.close(OracleConnection.PROXY_SESSION);
                        }
                    } catch (SQLException xx) {
                        
                    }
                    sqle.printStackTrace();
                    throw sqle;
                }
                finally {
                }
            }
        }
    }

    public String getProxyUsername() {
        String un = null;
        UserSession session = UserSession.getInstance();
        if (session != null) {
            un = session.getUsername();
        }
        return un;
    }
}
