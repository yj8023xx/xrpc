package com.smallc.xrpc.registry.impl;

import com.smallc.xrpc.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.registry.impl
 */
public class JdbcRegistry implements Registry, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(JdbcRegistry.class);
    private static final Collection<String> schemes = Collections.singleton("jdbc");
    private static final String DDL_SQL_FILE_NAME = "ddl";
    private static final String GET_SERVICE_SQL_FILE_NAME = "get-service";
    private static final String ADD_SERVICE_SQL_FILE_NAME = "add-service";
    private Connection connection = null;
    private String subProtocol = null;

    @Override
    public Collection<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public void connect(URI nameServiceUri) {
        try {
            close();
            subProtocol = nameServiceUri.toString().split(":")[1];
            logger.info("Database: {}.", subProtocol);
            String username = System.getProperty("nameservice.jdbc.username");
            String password = System.getProperty("nameservice.jdbc.password");
            logger.info("Connecting to database: {}...", nameServiceUri);
            if(null == username) {
                connection = DriverManager.getConnection(nameServiceUri.toString());
            } else {
                connection = DriverManager.getConnection(nameServiceUri.toString(), username, password);
            }
            logger.info("Maybe execute ddl to init database...");
            maybeExecuteDDL(connection);
            logger.info("Database connected.");
        } catch (SQLException | IOException e) {
            logger.warn("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    private void maybeExecuteDDL(Connection connection) throws IOException, SQLException {
        try (Statement statement = connection.createStatement()) {
            String ddlSqlString = readSql(DDL_SQL_FILE_NAME);
            statement.execute(ddlSqlString);
        }
    }

    private String readSql(String filename) throws IOException {
        String ddlFile = toFileName(filename);
        try (InputStream in  = this.getClass().getClassLoader()
                .getResourceAsStream(ddlFile)) {
            if (null != in) {
                return inputStreamToString(in);
            } else {
                throw new IOException(ddlFile + " not found in classpath!");
            }
        }

    }

    private String toFileName(String filename) {
        return filename + "." + subProtocol + ".sql";
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void registerService(String serviceName, URI uri) throws IOException{
        try (PreparedStatement statement = connection.prepareStatement(readSql(ADD_SERVICE_SQL_FILE_NAME))) {
            statement.setString(1, serviceName);
            statement.setString(2, uri.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Exception: ", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<URI> getServiceAddress(String serviceName) throws IOException{
        try (PreparedStatement statement = connection.prepareStatement(readSql(GET_SERVICE_SQL_FILE_NAME))) {
            statement.setString(1, serviceName);
            ResultSet resultSet = statement.executeQuery();
            List<URI> uris = new ArrayList<>();
            while (resultSet.next()) {
                uris.add(URI.create(resultSet.getString(1)));
            }
            return uris;
        } catch (SQLException e) {
            logger.warn("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<URI>> broadcastServiceAddress() {
        return null;
    }

    @Override
    public void close() {
        try {
            if (null != connection) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.warn("Close exception: ", e);
        }
    }

}
