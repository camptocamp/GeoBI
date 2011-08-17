Ext.namespace('App');

App.queryBuilder = function(options) {

    // private
    var _events = new Ext.util.Observable();

    /**
     * Method: build
     * Builds the builder (yeah!) using the current query
     */
    var build = function() {
        var query = App.queryMgr.getQuery(),
            cols = query.cols,
            rows = query.rows,
            i;

        // remove all the DimensionSelectors, ie. clear the queryBuilder
        Ext.ComponentMgr.all.each(function(component) {
            if (component instanceof App.DimensionSelector) {
                component.destroy();
            }
        });

        for (i = 0; i < rows.length; i++) {
            addDimension(spatialBlock, rows[i]);
        }
        for (i = 0; i < cols.length; i++) {
            addDimension(thematicBlock, cols[i]);
        }
    };

    App.queryMgr.events.on({
        'update': function() {
            build();
            var query = App.queryMgr.getQuery();
            queryButton.setDisabled(query.rows.length < 1);
        }
    });

    /**
     * adds a new DimensionSelector to a block
     *
     * Parameters:
     * block - the block to insert the new dimension in
     * config - the config object for the dimension to add
     */
    var addDimension = function(block, config) {
        var selector = new App.DimensionSelector(config);
        selector.on({
            'remove': function() {
                App.queryMgr.removeDimension(config.dimension);
            },
            'change': function() {
                App.queryMgr.setMembers(config.dimension, selector.members);
            }
        });
        block.insert(
            block.items.getCount() - 1,
            selector
        );
        block.doLayout();
    };

    /**
     * the spatial dimension chooser
     */
    var spatialBlock = new Ext.Panel({
        items:[{
            xtype: 'box',
            html: '<h3>Spatial dimension(s)</h3>'
        },
            new App.DimensionChooser({
                text: 'Add new dimension',
                iconCls: 'add',
                spatial: true,
                listeners: {
                    'select': function(level) {
                        var dimension = App.cubeProperties
                            .findLevelByUniqueName(level)
                            .get('DIMENSION_UNIQUE_NAME');
                        App.queryMgr.addRowDimension(dimension, level);
                    }
                }
            })
        ]
    });

    var thematicBlock = new Ext.Panel({
        items:[{
            xtype: 'box',
            html: '<h3>Thematic dimension(s)</h3>'
        },
            new App.DimensionChooser({
                text: 'Add new dimension',
                iconCls: 'add',
                listeners: {
                    'select': function(level) {
                        var dimension = App.cubeProperties
                            .findLevelByUniqueName(level)
                            .get('DIMENSION_UNIQUE_NAME');
                        App.queryMgr.addColDimension(dimension, level);
                    }
                }
            })
        ]
    });

    var measureCombo = new Ext.form.ComboBox({
        store: App.cubeProperties.measures,
        mode: 'local',
        triggerAction: 'all',
        valueField: 'MEASURE_UNIQUE_NAME',
        displayField: 'MEASURE_NAME',
        listeners: {
            'select': function(combo, r) {
                App.queryMgr.addMeasure(
                    r.get('DIMENSION_UNIQUE_NAME'),
                    r.get('MEASURE_UNIQUE_NAME')
                );
            },
            'render': function(combo) {
                if (combo.store.getTotalCount() === 0) {
                    App.cubeProperties.measures.on({
                        load: selectFirstMeasure
                    });
                } else {
                    selectFirstMeasure();
                }
            }
        }
    });

    var selectFirstMeasure = function() {
        var r = measureCombo.store.getAt(0);
        var value = r.get('MEASURE_UNIQUE_NAME');
        measureCombo.setValue(value);
        App.queryMgr.addMeasure(
            r.get('DIMENSION_UNIQUE_NAME'),
            value);
    };


    var relativeCheckBox = new Ext.form.Checkbox({
        boxLabel: 'Relative values (%)'
    });
    relativeCheckBox.on({
        'check': function(checkbox, checked) {
            App.queryMgr.useRelativeValues(checked);
        }
    });

    var measureBlock = new Ext.Panel({
        items:[{
            xtype: 'box',
            html: '<h3>Measure</h3>'
        }, {
            xtype: 'container',
            layout: 'form',
            hideLabels: true,
            items: [
                measureCombo,
                relativeCheckBox
            ]
        }]
    });

    var mdxInfo = new Ext.form.TextArea({
        width: '98%',
        height: 150,
        editable: false
    });
    var mdxInfoBlock = new Ext.Container({
        autoEl: {
            tag: 'div',
            style: 'display: none'
        },
        items: [
            mdxInfo
        ]
    });

    var queryButton = new Ext.Button({
        text: 'Execute query',
        iconCls: 'execute',
        handler: App.queryMgr.executeQuery,
        disabled: true
    });

    App.queryMgr.events.on({
        'beforequeryregistered': function() {
            queryButton.setIconClass('loading');
        },
        'queryregistered': function() {
            queryButton.setIconClass(queryButton.initialConfig.iconCls);
        }
    });

    // public
    return {

        events: _events,

        /**
         * APIProperty: panel
         */
        panel: new Ext.Panel({
            border: false,
            bodyStyle: 'padding: 2px;',
            defaults: {
                baseCls: "x-box",
                frame: true,
                style: "padding: 2px;"
            },
            items:[
                spatialBlock,
                thematicBlock,
                measureBlock,
                mdxInfoBlock
            ],
            buttons: [queryButton]
        })
    };
}();

