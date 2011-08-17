package com.c2c.query;

import org.olap4j.OlapConnection;
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;

import java.sql.ResultSet;

public class MeasuresQuery extends AbstractQuery<ResultSet> {
    private final String cube;

    public MeasuresQuery(String jdbcConnection, String catalogDefFile, String cube) {
        super(jdbcConnection, catalogDefFile);

        this.cube = cube;
    }

    @Override
    protected ResultSet doExecute(OlapConnection olapConnection) throws OlapException {
        OlapDatabaseMetaData md = olapConnection.getMetaData();
        return md.getMeasures(null, null, cube, null, null);
    }


}