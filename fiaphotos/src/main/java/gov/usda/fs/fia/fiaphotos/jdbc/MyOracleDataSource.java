/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleConnectionBuilderImpl;
import oracle.jdbc.pool.OracleDataSource;

/**
 *
 * @author sdelucero
 */
public class MyOracleDataSource extends OracleDataSource {

    public MyOracleDataSource() throws SQLException {
        super();
    }

    public Connection getConnection() throws SQLException {
        return new MyConnection((OracleConnection)super.getConnection());
    }

    public Connection getConnection(String string, String string1) throws SQLException {
        return super.getConnection(string, string1);
    }

    public Connection getConnection(OracleConnectionBuilderImpl ocbi) throws SQLException {
        return super.getConnection(ocbi);
    }

}
