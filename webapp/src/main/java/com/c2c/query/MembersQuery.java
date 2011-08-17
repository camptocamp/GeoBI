package com.c2c.query;

import org.olap4j.OlapConnection;
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;

import java.sql.ResultSet;

public class MembersQuery extends AbstractQuery<ResultSet> {
    private final String dimension;
    private String cube;

    public MembersQuery(String jdbcConnection, String catalogDefFile, String cube, String dim) {
        super(jdbcConnection, catalogDefFile);

        this.cube = cube;
        dimension = dim;
    }

    @Override
    protected ResultSet doExecute(OlapConnection connection) throws OlapException {
        OlapDatabaseMetaData md = connection.getMetaData();
        return md.getMembers(null, null, cube, dimension, null, null, null, null);
    }


}