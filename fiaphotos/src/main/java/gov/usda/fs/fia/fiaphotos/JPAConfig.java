/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gov.usda.fs.fia.fiaphotos.jdbc.MyProxyDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.support.oracle.ProxyDataSource;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author sdelucero
 */
@Configuration
@ConfigurationProperties(prefix = "oracle")
@EnableTransactionManagement
public class JPAConfig {


    // private OracleDataSource oracleDS;
    private String username;

    private String password;

    private String url;
    
    private String server;
    
    private String port;
    
    private String service;
    
    private int poolSize=10;

    /*
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws SQLException {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[]{"gov.usda.fs.fia.fiaphotos"});

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() throws SQLException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
     */
    @Bean
    public MetricRegistry metricRegistry() {
        MetricRegistry mr = new MetricRegistry();
        return mr;
    }
    @Bean
    public DataSource dataSource() throws SQLException {
        
        String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST="+getServer()+")(PORT="+getPort()+"))(CONNECT_DATA=(SERVICE_NAME="+getService()+")))";
        
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(getPoolSize());
        config.setDataSourceClassName("gov.usda.fs.fia.fiaphotos.jdbc.MyOracleDataSource");
        // config.addDataSourceProperty("url","jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=fsxrndx0167.wrk.fs.usda.gov)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=fia01d.wrk.fs.usda.gov)))");
        config.addDataSourceProperty("url",url);
        
        config.addDataSourceProperty("user", getUsername());
        config.addDataSourceProperty("password", getPassword());
        config.setPoolName("PLOTPHOTOS");
        
        
        config.setMetricRegistry(metricRegistry());

        /*
        config.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        config.setJdbcUrl("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=fsxrndx0167.wrk.fs.usda.gov)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=fia01d.wrk.fs.usda.gov)))");
        config.setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
        ConsoleReporter reporter = ConsoleReporter.forRegistry(mr)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, TimeUnit.SECONDS);
        */
        
        MyProxyDataSource ds = new MyProxyDataSource(config);
        //HikariDataSource ds = new HikariDataSource(config);

        return ds;
    }

    Properties additionalProperties() {
        Properties properties = new Properties();

        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");

        return properties;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * @return the poolSize
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * @param poolSize the poolSize to set
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

}
