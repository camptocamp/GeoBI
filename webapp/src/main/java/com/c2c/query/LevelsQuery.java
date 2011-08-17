package com.c2c.query;

import org.olap4j.OlapConnection;
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;

import java.sql.ResultSet;

public class LevelsQuery extends AbstractQuery<ResultSet> {
    private final String cube;

    public LevelsQuery(String jdbcConnection, String catalogDefFile, String cube) {
        super(jdbcConnection, catalogDefFile);

        this.cube = cube;
    }

    @Override
    protected ResultSet doExecute(OlapConnection olapConnection) throws OlapException {
        OlapDatabaseMetaData md = olapConnection.getMetaData();
        // TODO : do we need here to narrow the search by specifying parameters
        // other than null ?
        return md.getLevels(null, null, cube, null, null, null);
    }


}