Ext.namespace('App');

/**
 * Constructor: App.MDX
 * A class to write or read MDX format.
 *
 * Parameters:
 * options - {Object} Options
 */

App.MDX = function(options) {
    // private

    /**
     * Writes a set (using curly braces)
     */
    var writeSet = function(set) {
        return '{' + set + '}';
    };

    /**
     * Writes a tuple (using parenthesis)
     */
    var writeTuple = function(tuple) {
        return '(' + tuple + ')';
    };

    /**
     * Writes a crossJoin function
     */
    var writeCrossJoin = function(sets) {
        //var s = [];
        //for (var i = 0; i < sets.length; i++) {
            //s.push(writeSet(getMembers(sets[i])));
        //}
        return 'CROSSJOIN(' + sets + ')';
    };

    /**
     * 
     */
    var getMembers = function(set) {
        if (set.members) {
            return set.members;
        } else if (set.drilldown) {
            return [[set.drilldown, 'Children'].join('.')];
        } else {
            return [[set.level, 'Members'].join('.')];
        }
    };
    
    var needsCrossJoin = function(set) {
        return !set.members || set.members.length > 1;
    };
    
    var writeUnion = function(members) {
        var a = [];
        Ext.each(members, function(member) {
            a.push(member + '.Children');
        });
        var write = function(pair) {
            return 'UNION(' + pair.join(', ') + ')';
        };
        var b = write([a[0], a [1]]); 
        for (var i = 2; i < a.length - 1; i++) {
            b = write ([a[i], b]); 
        }
        return b;
    };

    var writeSets = function(sets) {
        // several sets
        sets.reverse();

        var crossJoin = false;
        var obj;
        Ext.each(sets, function(set, index) {
            if (set.rollup) {
                sets[index] = {
                    type: 'union',
                    members: set.members
                };
            }
        });

        if (sets.length == 1) { // members for one dimension 
            obj = sets[0]; 
        }
        for (var i = 1; i < sets.length; i++) {
            if (i == 1 ) {
                crossJoin = needsCrossJoin(sets[i - 1]);
                obj = sets[0];
            }
            //} else if (sets[i].rollup) {
                //obj = {
                    //type: "union",
                    //sets: [sets[i].members] 
                //};

            if (crossJoin || needsCrossJoin(sets[i])) {
                obj = {
                    type: "crossjoin",
                    sets: [sets[i], obj] 
                };
            } else {
                var s = [sets[i]];
                s.push(obj);
                obj = {
                    type: "tuple",
                    sets: s 
                };
            }
            // we assume that if there's more than 2 dimensions, we will
            // eventually need a crossjoin
            crossJoin = true;
        }

        return writeBlock(obj);
    };

    var writeBlock = function(obj) {
        if (typeof obj == "object" && obj.type == "crossjoin") {
            return writeCrossJoin(writeBlock(obj.sets));
        } else if (typeof obj == "object" && obj.type == "union") {
            return writeUnion(obj.members);
        } else if (typeof obj == "object" && obj.type == "tuple") {
            var members = [];
            Ext.each(obj.sets, function(set) {
                members.push(getMembers(set));
            });
            return writeSet(writeTuple(members.join(', ')));
        } else if (Ext.isArray(obj)) {
            var arr = [];
            Ext.each(obj, function(set) {
                arr.push(writeBlock(set));
            });
            return arr.join(', ');
        } else {
            return writeSet(getMembers(obj).join(', '));
        }
    };

    var writeRows = function(rows) {
        return writeSets(rows) + ' ON ROWS';
    };
    var writeCols = function(cols) {
        return writeSets(cols) + ' ON COLUMNS';
    };

    /**
     * Method: writeAdditionalMember
     *
     */
    var writeAdditionalMember = function(name, operation) {
        return [
            'MEMBER ',
            name,
            ' AS ',
            '\'(',
            operation,
            ')\''].join('');
    };

    /**
     * Method: writeAdditionalMembers
     * Adds members for relative values, returns string and
     * modifies the obj.cols configuration.
     *
     * Returns:
     * {String} the "with members" MDX part string
     */
    var writeAdditionalMembers = function(obj) {
        var members = [];
        if (obj.relative) {
            Ext.each(obj.additionalMembers, function(member) {
                members.push(writeAdditionalMember(member.name, member.operation));
            });
            return 'WITH ' + members.join(' ') + ' ';
        }
        return '';
    };

    // public
    Ext.apply(this, options);

    /**
     * Method: write
     * Writes a MDX string
     * 
     * Parameters:
     * data {Object} the data to write MDX for
     */
    this.write = function(obj) {
        return writeAdditionalMembers(obj) + 'SELECT ' +  writeCols(obj.cols) + ', ' + writeRows(obj.rows) +
               ' FROM ' + obj.cube.name;
    };
};
