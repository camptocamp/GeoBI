Ext.namespace('App');
App.queryMgr = function(options) {

    var _events = new OpenLayers.Events(null, null, [
        'update',
        'queryregistered',
        'beforequeryregistered',
        'metadataloaded',
        'beforedataloaded',
        'dataloaded'
    ]);

    /**
     * Property: mdxFormat
     */
    var mdxFormat = new App.MDX();

    /**
     * Property: queries
     * {Array(Objects)}
     *
     * An array of hash object literals representing the queries. The last one
     * is the current one.
     * example:
     *
    {
        cube: {name: '[Sales]'},
        cols: [{
            dimension: '[Measures]',
            members: [
                '[Measures].[Unit Sales]',
                '[Measures].[Store Cost]',
                '[Measures].[Store Sales]'
            ]
        }],
        rows: [{
            dimension: '[Customers]',
            members: [
                '[Customers].[All Customers].[USA].[CA].[Altadena]',
                '[Customers].[All Customers].[USA].[CA].[Arcadia]'
            ]
        }]
    }
     */
    var queries = [];

    /**
     * Property: query
     * {Object} The current query
     */
    var query;

    /**
     * Method: createNewQuery
     * Create a new query and adds it to the queries
     *
     * Parameters:
     * config {Object} A config object to build the query with (optional)
     */
    var createNewQuery = function(config) {
        queries.push({
            cube: App.cube,
            cols: [],
            rows: []
        });
        query = queries[queries.length - 1];
    };

    /**
     * Method: addMeasure 
     * Adds a new measure to the current query, only one measure can be added.
     *
     * Parameters:
     * dimension {String} The dimension unique_name
     * measure {String} The measure unique_name
     */
    var addMeasure = function(dimension, measure) {
        query['measure'] = {
            dimension: dimension,
            members: [measure]
        };
    };

    /**
     * Method: useRelativeValues
     *
     * Parameters:
     * t {Boolean} Whether to use relative values or not
     */
    var useRelativeValues = function(t) {
        query.relative = t;
    };

    /**
     * Method: addDimension 
     * Adds a new dimension to the current query
     *
     * Parameters:
     * dimension {String} The dimension unique_name
     * level {String} The level unique_name
     * direction {String} The direction, either 'cols' or 'rows'
     * position {Integer} The index where to add the new dimension, the
     *     dimension will be put at the last position if not given (optional)
     */
    var addDimension = function(dimension, level, direction, position) {
        var obj = {
            dimension: dimension,
            level: level
        };
        if (typeof position == 'undefined') {
            position = query[direction].length;
        }
        query[direction].splice(position, 0, obj);
    };

    /**
     * Method: addColDimension 
     * Adds a new col dimension to the current query
     *
     * Parameters:
     * dimension {String} The dimension unique_name
     * level {String} The level unique_name
     * position {Integer} The index where to add the new dimension, the
     *     dimension will be put at the last position if not given (optional)
     */
    var addColDimension = function(dimension, level, position) {
        addDimension(dimension, level, 'cols', position);
        _events.triggerEvent('update');
    };

    /**
     * Method: addRowDimension 
     * Adds a new row dimension to the current query
     *
     * Parameters:
     * dimension {String} The dimension unique_name
     * level {String} The level unique_name
     * position {Integer} The index where to add the new dimension, the
     *     dimension will be put at the last position if not given (optional)
     */
    var addRowDimension = function(dimension, level, position) {
        addDimension(dimension, level, 'rows', position);
        _events.triggerEvent('update');
    };

    /**
     * Method: removeDimension
     * Removes a dimension from the current query
     *
     * Parameters:
     * dimension {String} The dimension unique_name
     */
    var removeDimension = function(dimension) {
        findDimension(dimension, function(dir, index) {
            query[dir].splice(index, 1);
        });
        _events.triggerEvent('update');
    };

    /**
     * Method: findDimension
     * Finds a dimension and returns an object telling where it has been found.
     *
     * Parameters:
     * dimension {String} The dimension unique_name
     * callback {Function} The function to call for the found dimension, it is
     *     passed the direction and the index as arguments.
     *
     * Returns:
     * {Object} An object with direction and index properties
     */
    var findDimension = function(dimension, callback) {
        var directions = ['cols', 'rows'],
            i, j, dir;

        for (j = 0; j < directions.length; j++) { 
            dir = directions[j];
            for (i = 0; i < query[dir].length; i++) {
                if (query[dir][i].dimension == dimension) {
                    callback.call(null, dir, i);
                }
            }
        }
    };

    /**
     * Method: setMembers 
     * Adds members to a dimension in the current query
     *
     * Parameters:
     * dimension {String} The dimension unique_name
     * members {Array(String)} The members unique_names to add
     */
    var setMembers = function(dimension, members) {
        findDimension(dimension, function(dir, index) {
            query[dir][index].members = members;
        });
        _events.triggerEvent('update');
    };

    /**
     * Method: clearMembers
     * Removes all the members from a dimension
     * 
     * Parameters:
     * dimension {String} The dimension unique_name
     */
    var clearMembers = function(dimension) {
    };

    /**
     * Method: getQuery
     * Returns the latest query
     *
     * Returns:
     * {Object}
     */
    var getQuery = function() {
        return query;
    };

    /**
     * Method: setQuery
     *
     * Parameters:
     * obj {Object} new query hash
     */
    var setQuery = function(obj) {
        query = obj;
        _events.triggerEvent('update');
    };

    /**
     * Method: getDimensions
     * Returns the list of currently selected dimensions
     *
     * Returns:
     * {Array(String)} Array of the dimensions unique_name
     */
    var getDimensions = function() {
        var dimensions = [],
            i;
        for (i = 0; i < query.cols.length; i++) {
            dimensions.push(query.cols[i].dimension);
        }
        for (i = 0; i < query.rows.length; i++) {
            dimensions.push(query.rows[i].dimension);
        }
        return dimensions;
    };

    /**
     * Method: executeQuery
     * Executes the query, ie. writes the MDX and sends a XHR request
     */
    var executeQuery = function() {
        _events.triggerEvent('beforequeryregistered');
        Ext.Ajax.request({
            url: '/webbi/registerquery',
            success: function(response) {
                var id = Ext.util.JSON.decode(response.responseText).id;
                if (id) {
                    App.queryId = id;
                    Ext.Ajax.request({
                        url: '/webbi/getmetadata',
                        method: 'GET',
                        success: function(response) {
                            App.metadata = Ext.util.JSON.decode(response.responseText);
                            _events.triggerEvent('metadataloaded');
                            loadData();
                        },
                        failure: function(response) {
                            App.errorMgr.show(response, 'getting info about query results');
                        },
                        params: {
                            queryId: App.queryId
                        }
                    });
                }
                _events.triggerEvent('queryregistered', {id: id});
            },
            failure: function(response) {
                App.errorMgr.show(response, 'executing query');
                _events.triggerEvent('queryregistered');
            },
            params: {
                query: writeMDX()
            },
            timeout: 120000
        });
    };

    /**
     * Method: loadData
     * Calls the getdata service
     */
    var loadData = function() {
        App.data && App.data.destroy();
        App.data = new Ext.data.JsonStore({
            url: "/webbi/getdata?queryId=" + App.queryId,
            sortInfo: {
                field: App.metadata.rows[0].dimension_name
            },
            autoLoad: true,
            listeners: {
                load: function(store, records) {
                    _events.triggerEvent('dataloaded');
                }
            }
        });
        _events.triggerEvent('beforedataloaded');
    };

    /**
     * Method: writeMDX 
     * Write the MDX string 
     */
    var writeMDX = function() {
        var additionalMembers,
            cols = [query.measure].concat(query.cols);
        if (query.relative) {
            var col = cols[cols.length - 1];
            var obj = prepareForRelativeValues(col);
            cols[cols.length - 1] = obj.col;
            additionalMembers = obj.additionalMembers;
        }
        // measure is to be used as the first col 
        var q = {
            additionalMembers: additionalMembers,
            relative: query.relative,
            cube: query.cube,
            cols: cols, 
            rows: [].concat(query.rows) // clone
        };
        return mdxFormat.write(q);
    };

    /**
     * Method: prepareForRelativeValues
     * Prepares the columns members for relative values before we write the MDX
     * string
     *
     * Parameters
     * col {Array} The column
     */
    var prepareForRelativeValues = function(col) {
        var _col = {},
            additionalMembers = [];
        // we first need to find the allMember member for the last
        // dimension
        var dim = App.cubeProperties
                .findDimensionByUniqueName(col.dimension);
        _col.allMember = dim.members.getAt(0).get('MEMBER_UNIQUE_NAME');

        // in the case no member was actually chosen for this dimension, we
        // need to find all the members for the given dimension/level
        if (!col.members || col.members.length === 0) {
            _col.members = [];
            var levelNumber = App.cubeProperties.findLevelByUniqueName(col.level)
                .get('LEVEL_NUMBER');
            dim.members.query('LEVEL_NUMBER', levelNumber)
                .each(function(item, index) {
                    _col.members.push(item.get('MEMBER_UNIQUE_NAME'));
            });
        } else {
            _col.members = [].concat(col.members); 
        }
        // and we add an additionnal member for the relative values
        Ext.each([].concat(_col.members), function(member, index) {
            var name = member.slice(0, member.lastIndexOf(']')) + ' %]';
            var operation = '100.0 * ' + member + 
                ' / ' + _col.allMember;
            additionalMembers.push({
                name: name,
                operation: operation
            });
            // add the additinal member every 2 columns
            _col.members.splice(index * 2 + 1, 0, name);
        });

        // add the total member
        var totalName = _col.allMember + '.[Total]';
        additionalMembers.push({
            name: totalName,
            operation: _col.allMember
        });
        _col.members.splice(0, 0, totalName);

        return {col: _col, additionalMembers: additionalMembers};
    };

    /**
     * Method: drillDown
     *
     * Parameters:
     * config {Object} the configuration object with dimension and member to
     *     do the drill-down with.
     */
    var drillDown = function(config) {
        var dimension = config.dimension;
        var children = App.cubeProperties.getMemberChildren(dimension, config.member);
        findDimension(dimension, function(dir, index) {
            removeDimension(dimension);
            var member = App.cubeProperties.findMemberByUniqueName(dimension, config.member);
            var number = parseInt(member.get('LEVEL_NUMBER'), 0) + 1;
            var newLevel = App.cubeProperties.findLevelByNumber(dimension, number)
                           .get('LEVEL_UNIQUE_NAME');
            addDimension(dimension, newLevel, dir, index);
            setMembers(dimension, children);
        });

        executeQuery();
    };

    /**
     * Method: rollUp
     *
     * Parameters:
     * config {Object} the configuration object with dimension to do the
     *     roll-up with.
     * 
     */
    var rollUp = function(config) {
        var dimension = config.dimension;
        findDimension(dimension, function(dir, index) {
            var d = query[dir][index],
                parents = [],
                curLevel = App.cubeProperties.findLevelByUniqueName(d.level),
                number = parseInt(curLevel.get('LEVEL_NUMBER'), 0) -1,
                newLevel = App.cubeProperties.findLevelByNumber(dimension, number)
                              .get('LEVEL_UNIQUE_NAME');
            if (d.members) {
                // in the case there are members selected, we just want the
                // parents of those members to be selected in the above level
                var member, parent;
                for (var i = 0; i < d.members.length; i++) {
                    member = App.cubeProperties
                        .findMemberByUniqueName(dimension, d.members[i]);
                    parent = member.get('PARENT_UNIQUE_NAME');

                    // avoid duplicates
                    if (parents.indexOf(parent) == -1)  {
                        parents.push(parent);
                    }
                }
            } else {
                // in the case 'All' members of the current level are selected,
                // we want 'All' the members of the above level to be selected
                // after the roll up
                removeDimension(dimension);
            }
            removeDimension(dimension);
            addDimension(dimension, newLevel, dir, index);

            if (parents) {
                setMembers(dimension, parents);
            }
        });
        executeQuery();
    };

    return {
        init: function() {
            createNewQuery();
        },
        events: _events,
        queries: queries,
        getQuery: getQuery,
        setQuery: setQuery,
        createNewQuery: createNewQuery,
        useRelativeValues: useRelativeValues,
        addMeasure: addMeasure,
        addColDimension: addColDimension,
        addRowDimension: addRowDimension,
        removeDimension: removeDimension,
        setMembers: setMembers,
        executeQuery: executeQuery,
        getDimensions: getDimensions,
        drillDown: drillDown,
        rollUp: rollUp
    };
}();
