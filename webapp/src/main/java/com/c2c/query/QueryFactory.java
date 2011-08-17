package com.c2c.query;

/**
 * Factory for creating queries.  It is configured with the
 * default parameters which act as a base for creating specific queries
 * <p/>
 * User: jeichar
 * Date: Jul 2, 2010
 * Time: 12:11:56 PM
 */
public class QueryFactory {

    private final String jdbcConnection;
    private final String catalogDefFile;
    private final String simpleCatDefFile;

    public QueryFactory(String jdbcConnection, String catalogDefFile, String simpleCatDefFile) {
        this.jdbcConnection = jdbcConnection;
        this.catalogDefFile = QueryFactory.class.getClassLoader().getResource(catalogDefFile).getFile();
        this.simpleCatDefFile = QueryFactory.class.getClassLoader().getResource(simpleCatDefFile).getFile();
    }

    public DataQuery createDataQuery(String mdx) {
        return new DataQuery(jdbcConnection, catalogDefFile, mdx);
    }

    public DimensionsQuery createDimensionsQuery(String cube) {
        return new DimensionsQuery(jdbcConnection, simpleCatDefFile, cube);
    }

    public LevelsQuery createLevelsQuery(String cube) {
        return new LevelsQuery(jdbcConnection, simpleCatDefFile, cube);
    }

    public MeasuresQuery createMeasuresQuery(String cube) {
        return new MeasuresQuery(jdbcConnection, simpleCatDefFile, cube);
    }

    public MembersQuery createMembersQuery(String cube, String dim) {
        return new MembersQuery(jdbcConnection, simpleCatDefFile, cube, dim);
    }

}
