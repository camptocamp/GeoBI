Ext.namespace('App');

App.cubeProperties = function() {

    var measures = new Ext.data.JsonStore({
        proxy: new Ext.data.HttpProxy({
            url: './getcubeproperties?datasource=Provider%3DMondrian%3BDataSource%3DMondrianUab%3B&requestType=measures',
            disableCaching: false,
            method: 'GET'
        }),
        baseParams: {
            cubeName: App.cube.name
        },
        fields: ['DIMENSION_UNIQUE_NAME', 'MEASURE_NAME', 'MEASURE_UNIQUE_NAME'],
        autoLoad: true
    });

    var dimensions = new Ext.data.JsonStore({
        proxy: new Ext.data.HttpProxy({
            url: './getcubeproperties?datasource=Provider%3DMondrian%3BDataSource%3DMondrianUab%3B&requestType=dimensions',
            disableCaching: false,
            method: 'GET',
            failure: function(response) {
                App.errorMgr.show(response, 'getting the list of available dimensions');
            }
        }),
        baseParams: {
            cubeName: App.cube.name
        },
        root: null,
        fields: ['DIMENSION_NAME', 'DIMENSION_UNIQUE_NAME', 'type'],
        autoLoad: true,
        listeners: {
            'load': function(store) {
                store.loaded = true;
                store.each(function(record) {
                    addMembers(record);
                });
            }
        }
    });

    var levels = new Ext.data.JsonStore({
        proxy: new Ext.data.HttpProxy({
            url: './getcubeproperties?datasource=Provider%3DMondrian%3BDataSource%3DMondrianUab%3B&requestType=levels',
            disableCaching: false,
            method: 'GET',
            failure: function(response) {
                App.errorMgr.show(response, 'getting the list of available levels');
            }
        }),
        baseParams: {
            cubeName: App.cube.name
        },
        root: null,
        fields: ['DIMENSION_UNIQUE_NAME', 'LEVEL_NAME', 'LEVEL_UNIQUE_NAME', 'LEVEL_NUMBER'],
        autoLoad: true,
        listeners: {
            'load': function(store) {
                store.loaded = true;
            }
        },
        sortInfo: {field: 'LEVEL_NUMBER'}
    });
    
    /**
     * Method: addMembers
     * Adds a new dimension in the dimensions hash
     */
    var addMembers = function(dimension) {
        var store = new Ext.data.JsonStore({
            proxy: new Ext.data.HttpProxy({
                url: './getcubeproperties?datasource=Provider%3DMondrian%3BDataSource%3DMondrianUab%3B&requestType=members',
                method: 'GET',
                failure: function(response) {
                    App.errorMgr.show(response, 'getting the list of members');
                }
            }),
            baseParams: {
                dimensionUniqueName: dimension.get('DIMENSION_UNIQUE_NAME'),
                cubeName: App.cube.name
            },
            root: null,
            fields: ['MEMBER_NAME', 'MEMBER_UNIQUE_NAME', 'LEVEL_NUMBER', 'PARENT_UNIQUE_NAME'],
            autoLoad: true
        });

        // here we create a new custom property to store the members for the
        // given dimension
        dimension.members = store;
    };

    /**
     * Method: getLevelsMaxNumber 
     *
     * Parameters:
     * unique_name {String} - The dimension unique_name
     *
     * Returns
     * integer - The max level number for the given dimension
     */
    var getLevelsMaxNumber = function(unique_name) {
        var max = 0;
        levels.each(function(level) {
            if (level.get('DIMENSION_UNIQUE_NAME') == unique_name) {
                max = Math.max(max, level.get('LEVEL_NUMBER'));
            }
        });
        return max;
    };

    /**
     * Method: findLevelByUniqueName
     *
     * Parameters:
     * unique_name {String} - The level unique_name
     *
     * Returns
     * {Ext.data.Record} - The found level
     */
    var findLevelByUniqueName = function(unique_name) {
        var index = levels.find("LEVEL_UNIQUE_NAME", unique_name);
        return levels.getAt(index);
    };

    /**
     * Method: findLevelByNumber
     *
     * Parameters:
     * dimension {String} The dimension unique_name
     * number {Integer} The level number
     *
     * Returns
     * {Ext.data.Record} - The found level
     */
    var findLevelByNumber = function(dimension, number) {
        var index = levels.findBy(function(record) {
            return record.get('DIMENSION_UNIQUE_NAME') == dimension &&
                   record.get("LEVEL_NUMBER") == number;
        });
        return levels.getAt(index) || false;
    };

    /**
     * Method: isDrillable 
     * Determines if a level is drillable
     *
     * Parameters:
     * unique_name {String} - The level unique_name
     *
     * Returns
     * {Boolean} - Whether the level is drillable or not
     */
    var isDrillable = function(unique_name) {
        var level = findLevelByUniqueName(unique_name);
        return level.get("LEVEL_NUMBER") < getLevelsMaxNumber(level.get('DIMENSION_UNIQUE_NAME'));
    };

    /**
     * Method: isRollable
     * Determines if a level is rollable
     *
     * Parameters:
     * unique_name {String} - The level unique_name
     *
     * Returns
     * {Boolean} - Whether the level is rollable or not
     */
    var isRollable = function(unique_name) {
        var level = findLevelByUniqueName(unique_name);
        return level.get("LEVEL_NUMBER") > 1 &&
               getLevelsMaxNumber(level.get('DIMENSION_UNIQUE_NAME')) > 1;
    };

    /**
     * Method: findDimensionByName
     * 
     * Parameters:
     * name {String}
     *
     * Returns
     * {Object}
     */
    var findDimensionByName = function(name) {
        var index = dimensions.find("DIMENSION_NAME", name);
        return (index != -1) ? dimensions.getAt(index) : false;
    };

    /**
     * Method: findDimensionByUniqueName
     * 
     * Parameters:
     * unique_name {String}
     *
     * Returns
     * {Object}
     */
    var findDimensionByUniqueName = function(unique_name) {
        var index = dimensions.find("DIMENSION_UNIQUE_NAME", unique_name);
        return (index != -1) ? dimensions.getAt(index) : false;
    };

    /**
     * Method: findMemberByName
     * 
     * Parameters:
     * dimension {String} the dimension unique_name
     * name {String} the member name
     *
     * Returns
     * {Object}
     */
    var findMemberByName = function(dimension, name) {
        dimension = findDimensionByUniqueName(dimension);
        if (!dimension) {
            return;
        }
        var members = dimension.members;
        var index = members.find('MEMBER_NAME', name);
        return members.getAt(index);
    };

    /**
     * Method: findMemberByUniqueName
     * 
     * Parameters:
     * dimension {String} the dimension unique_name
     * name {String} the member unique_name
     *
     * Returns
     * {Object}
     */
    var findMemberByUniqueName = function(dimension, name) {
        dimension = findDimensionByUniqueName(dimension);
        if (!dimension) {
            return;
        }
        var members = dimension.members;
        var index = members.find('MEMBER_UNIQUE_NAME', name);
        return members.getAt(index);
    };

    /**
     * Method: findMemberParent
     * 
     * Paremeters:
     * dimension {String} the dimension unique_name
     * member {String} the member unique_name
     */
    var findMemberParent = function(dimension, member) {
        dimension = findDimensionByUniqueName(dimension);
        if (!dimension) {
            return;
        }
        var members = dimension.members;
        var index = members.find('MEMBER_UNIQUE_NAME',
                            member.get('PARENT_UNIQUE_NAME'));
        return members.getAt(index);
    };

    /**
     * Method: getMemberChildren
     * Get the children (members) of a given member in a given dimension
     *
     * Parameters:
     * dimension {String} the dimension unique_name
     * member {String} the member unique_name
     */
    var getMemberChildren = function(dimension, member) {
        dimension = findDimensionByUniqueName(dimension);
        if (!dimension) {
            return;
        }
        var members = dimension.members;
        members.filter('PARENT_UNIQUE_NAME', member, false, false, true);
        var children = members.collect('MEMBER_UNIQUE_NAME');
        members.clearFilter();
        return children;
    };

    /**
     * Method: getColumns
     * Extract the columns from a metadata object
     *
     * Parameters:
     * metadata {Object}
     * relativeOnly {Boolean} If set to true only relative values are returned.
     */
    var getColumns = function(metadata, relativeOnly) {
        var cols = metadata.columns,
            multiplier = 1,
            count, members;

        var a = [],
            b,
            dataIndexes,
            drillable,
            drilldown;

        a = [{dataIndex: null}];

        // fixes filtering breaks level match
        App.cubeProperties.levels.clearFilter();

        Ext.each(cols, function(col, colIdx) {
            b = [];
            Ext.each(a, function(item) {
                Ext.each(col.levels, function(level) {
                    drillable = isDrillable(level.level_unique_name);
                    Ext.each(level.members, function(member) {
                        if (colIdx < cols.length - 1 || !relativeOnly ||
                            member.member_unique_name.indexOf('%') != -1) {
                            dataIndexes = item.dataIndex ?
                                [item.dataIndex, member.data_index] : [member.data_index];
                            // a member is considered drillable if its level is
                            // drillable and if it exists in the cube (ie. is
                            // not an additional member) 
                            drilldown = (drillable &&
                                findMemberByUniqueName(col.dimension_unique_name, member.member_unique_name)) ? {
                                dimension: col.dimension_unique_name,
                                level: level.level_unique_name,
                                member: member.member_unique_name
                            } : false;
                            b.push({
                                header: member.member_name,
                                dataIndex: dataIndexes.join('_'),
                                drilldown: drilldown
                            });
                        }
                    });
                });
            });
            a = b;
        });
        return a;
    };

    /**
     * Method: getHeaderGroupRows
     * Creates the config object for ColumnHeaderGroup plugin by reading the
     * metadata
     *
     * It should look like the following :
     * [
     *    {header: '', colspan: 2, align: 'center'},
     *    {header: 'LAND COVER 90', colspan: 4, align: 'center'}
     *  ], [
     *    {header: '', colspan: 2, align: 'center'},
     *    {header: 'Artificial surfaces', colspan: 2, align: 'center'},
     *    {header: 'Agricultural areas', colspan: 2, align: 'center'}
     *  ], [
     *    {header: '', colspan: 2, align: 'center'},
     *    {header: 'CHANGES 90-00', colspan: 2, align: 'center'},
     *    {header: 'CHANGES 90-00', colspan: 2, align: 'center'}
     *  ]
     *
     */
    var getHeaderGroupRows = function(metadata, relativeOnly) {
        var cols = metadata.columns,
            multiplier = 1,
            _cols = [],
            gpRows = [],
            gpHRow,
            gpRow,
            len,
            i,
            j,
            level,
            members,
            count,
            drillable,
            drilldown,
            rollable,
            rollup;


        //Ext.each(cols[cols.length - 1].levels, function(level) {
            //multiplier *= level.members.length;
        //});

        // Note: the colIndex is a way to retrieve the column index as computed
        // by ColumnHeaderGroup::findHeaderIndex
        var nbRows = metadata.rows.length;
        for (len = cols.length - 1, i = len; i >= 0; i--) {
            gpRow = [];
            // add the header group for the (OLAP) rows
            gpRow.push({
                header: '',
                colspan: metadata.rows.length,
                colIndex: 0 // see above
            });
            level = cols[i].levels[0];
            if (i != len) { // not the last col
                drillable = App.cubeProperties.isDrillable(level.level_unique_name);
                Ext.each(level.members, function(member, memberIndex) {
                    drilldown = (drillable) ? {
                        dimension: cols[i].dimension_unique_name,
                        level: level.level_unique_name,
                        member: member.member_unique_name
                    } : false;
                    gpRow.push({
                        header: member.member_name,
                        colspan: multiplier,
                        align: 'center',
                        drilldown: drilldown,
                        colIndex: (memberIndex * multiplier) + nbRows // see above
                    });
                });
                gpRows.unshift(gpRow);
            }
            var n;
            if (relativeOnly && i == len) { // last column
                n = 0;
                Ext.each(level.members, function(member) {
                    if (i < cols.length - 1 || !relativeOnly ||
                        member.member_unique_name.indexOf('%') != -1) {
                        n++;
                    }
                });
            } else {
                n = level.members.length;
            }
            multiplier *= n; 

            // number of members in the previous dimension
            count = cols[i - 1] ? cols[i - 1].levels[0].members.length : 1;
            gpRow = [];
            // add the header group for the (OLAP) rows
            gpRow.push({
                header: '',
                colspan: nbRows,
                colIndex: 0 // see above
            });
            name = findLevelByUniqueName(level.level_unique_name)
                    .get('LEVEL_NAME');
            rollup = isRollable(level.level_unique_name) ? {
                dimension: cols[i].dimension_unique_name
            } : false;
            for (j = 0; j < count; j ++) {
                gpRow.push({
                    header: name,
                    colspan: multiplier,
                    align: 'center',
                    rollup: rollup,
                    colIndex: j + nbRows // see above
                });
            }
            gpRows.unshift(gpRow);
        }
        return gpRows;
    };

    return {
        measures: measures,
        dimensions: dimensions,
        levels: levels,
        isDrillable: isDrillable,
        isRollable: isRollable,
        getLevelsMaxNumber: getLevelsMaxNumber,
        findLevelByUniqueName: findLevelByUniqueName,
        findDimensionByName: findDimensionByName,
        findDimensionByUniqueName: findDimensionByUniqueName,
        findMemberByName: findMemberByName,
        findMemberByUniqueName: findMemberByUniqueName,
        findMemberParent: findMemberParent,
        getMemberChildren: getMemberChildren,
        getColumns: getColumns,
        getHeaderGroupRows: getHeaderGroupRows,
        findLevelByNumber: findLevelByNumber
    };
}();
