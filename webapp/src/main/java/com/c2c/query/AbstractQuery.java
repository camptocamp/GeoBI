package com.c2c.query;

import org.olap4j.OlapConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

abstract class AbstractQuery<R> implements Query<R> {
    private final String connectionParams;


    public AbstractQuery(String jdbcConnection, String catalogDefFile) {
        this.connectionParams = jdbcConnection + ";Catalog=" + catalogDefFile + ";JdbcDrivers=org.postgis.DriverWrapper;UseSchemaPool=true;";
    }

    public final R execute() {
        OlapConnection connection = null;
        try {
            connection = connect();
        	return doExecute(connection);
        } catch (Exception e) {
            // TODO need to fix this so it sends a more specific exception
            throw new RuntimeException(e);
        } finally {
            close(connection);
        }
    }

    protected abstract R doExecute(OlapConnection connection) throws IOException, Exception;

    private OlapConnection connect() throws IOException {
        try {
            Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
            Connection connection =
                    DriverManager.getConnection(connectionParams);

            return connection.unwrap(OlapConnection.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void close(Connection olapConnection) {
        if (olapConnection == null) {
            return;
        }

        try {
            olapConnection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}