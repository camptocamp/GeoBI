Ext.namespace('App');

/**
 * A button with menu to select dimension
 */
App.DimensionChooser = Ext.extend(Ext.Button, {

    dimensionsStore: null,
    levelsStore: null,
    disabled: true,

    type: null,

    constructor: function(config) {
        this.addEvents(

            /**
            * @event select
            * Fires when a dimension is chosen
            * @param {Object} An object with dimension and hierarchy
            */
           'select'
        );
    
        this.menu = {};

        this.type = config.spatial ? 'spatial' : 'thematic';
        this.dimensionsStore = App.cubeProperties.dimensions; 
        this.levelsStore = App.cubeProperties.levels;

        var testLoad = function() {
            if (App.queryMgr.getQuery() && this.levelsStore.loaded && this.dimensionsStore.loaded) {
                clearInterval(interval);
                this.filter();
            }
        };
        var interval = window.setInterval(testLoad.createDelegate(this), 200);

        App.queryMgr.events.on({
            'update': function() {
                this.filter();
            },
            scope: this
        });

        App.DimensionChooser.superclass.constructor.call(this, config);
    },

    onDimensionSelect: function(item) {
        this.fireEvent('select', item.unique_name);
    },
    
    /**
     * Method: onDataLoad
     */
    onDataLoad: function() {
        this.enable();
        this.dimensionsStore.each(function(dim) {
            this.menu.add([
               '<b class="menu-title">' + dim.get('DIMENSION_NAME') + '</b>'
            ]);
            var levels = this.levelsStore.query('DIMENSION_UNIQUE_NAME',
                dim.get('DIMENSION_UNIQUE_NAME'));
            levels.each(function(level) {
                this.menu.add([{
                    text: level.get('LEVEL_NAME'),
                    unique_name: level.get('LEVEL_UNIQUE_NAME'),
                    handler: this.onDimensionSelect,
                    scope: this
                }]);
            }, this);
        }, this);
    },

    /**
     * Method: filter
     */
    filter: function() {
        var filter = function(item) {
            return App.queryMgr.getDimensions().indexOf(item.get('DIMENSION_UNIQUE_NAME'))==-1 &&
                item.get('type') == this.type;
        }.createDelegate(this);
        this.dimensionsStore.filterBy(filter);
        this.levelsStore.filterBy(filter);
        this.menu.removeAll();
        this.onDataLoad();
        this.dimensionsStore.clearFilter();
        this.levelsStore.clearFilter();
    }

});
