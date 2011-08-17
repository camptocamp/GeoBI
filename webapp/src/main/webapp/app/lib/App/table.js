Ext.namespace('App');

App.table = function() {
    // private
    var gridCt = new Ext.Panel({
        layout: 'fit'
    });

    // add a resize handle at the bottom of this widget
    gridCt.on('render', function() {
        el = gridCt.body.dom;
        el.resizer = new Ext.Resizable(el.id, {
            handles : 's',
            minHeight: 100
        });
        el.resizer.on('resize', function(r,w,h,e) {
            gridCt.setSize(w, h);
        });
    });

    var destroyGrid = function() {
        gridCt.removeAll();
    };

    var createGrid = function() {

        var relativeOnly = isRelativeOnly(); 

        var metadata = App.metadata;

        var group = new Ext.ux.grid.DrillableColumnHeaderGroup({
            rows: App.cubeProperties.getHeaderGroupRows(metadata, relativeOnly)
        });

        var drillableHeader = new Ext.ux.grid.DrillableHeader();

        var cellActions = new Ext.ux.grid.CellActions({
            listeners:{
                action:function(grid, record, action, value, dataIndex, rowIndex, col) {
                    col = grid.getColumnModel().config[col];
                    var dimension = col.dimension;
                    Ext.each(dimension.levels[0].members, function(member) {
                        if (member.member_name == value) {
                            App.queryMgr.drillDown({
                                dimension: dimension.dimension_unique_name,
                                member: member.member_unique_name
                            });
                            return false;
                        }
                    });
                }
            },
            align: 'left'
        });

        var grid = new Ext.ux.grid.DrillableGridPanel({
            border: false,
            store: App.data,
            columns: getColumns(metadata, relativeOnly),
            plugins: [group, drillableHeader, cellActions],
            stripeRows: true,
            viewConfig: {
            },
            listeners: {
                'drilldown': function(object) {
                    if (object) {
                        App.queryMgr.drillDown(object);
                    }
                },
                'rollup': function(object) {
                    if (object) {
                        App.queryMgr.rollUp(object);
                    }
                }
            }
        });
        gridCt.add(grid);
        gridCt.doLayout();

        // magic cell auto sizing !
        App.data.on('load', function() {
            // See http://www.sencha.com/forum/showthread.php?82965-Dblclick-to-autosize-grid-columns
            var view = grid.getView();
            var cm = grid.getColumnModel();
            var len = cm.getColumnCount();
            var width;
            for (var i = 0; i < len; i++) {
                // get the header cell width
                var el = view.getHeaderCell(i);
                el = el.firstChild;
                el.style.width = '0px';
                width = el.scrollWidth;
                el.style.width = 'auto';
                // get the wider content cell
                for (var rowIndex = 0, count = grid.getStore().getCount(); rowIndex < count; rowIndex++) {
                    el = view.getCell(rowIndex, i).firstChild;
                    el.style.width = '0px';
                    width = Math.max(width, el.scrollWidth);
                    el.style.width = 'auto';
                }
                // use the max width between header and content cells
                view.onColumnSplitterMoved(i, width + 8);
            }

            // also manage sort of a maxHeight, if data inside grid is short
            // then use this height instead of a fixed value 
            var headerHeight = Ext.select('.x-grid3-header', true, grid.body.dom).first().getHeight();
            var scrollerHeight = Ext.select('.x-grid3-body', true, grid.body.dom).first().getHeight(); 
            var height = headerHeight + scrollerHeight + 2;

            if (height < 400) {
                gridCt.setHeight(height);
            } else {
                gridCt.setHeight(400);
            }
        });

    };

    /**
     * Method: getColumns
     * Creates a valid Ext.grid.ColumnModel config object by reading the
     * metadata
     */
    var getColumns = function(metadata, relativeOnly) {

        var columns = [];

        var a = App.cubeProperties.getColumns(metadata, relativeOnly);
        for (var i = 0; i < a.length; i++) {
            columns.push({
                xtype: "numbercolumn",
                dataIndex: a[i].dataIndex,
                drilldown: a[i].drilldown,
                header: a[i].header,
                align: 'right',
                format: '0.0'
            });
        }


        // spatial dimensions specific column(s)
        for (var len = metadata.rows.length - 1, j = len; j >= 0; j--) {
            var item = metadata.rows[j],
                level = item.levels[0];
            var rollup = App.cubeProperties.isRollable(level.level_unique_name) ? {
                dimension: item.dimension_unique_name
            } : false;
            var name = App.cubeProperties.findLevelByUniqueName(level.level_unique_name)
                          .get('LEVEL_NAME');
            var col = {
                header: name,
                dataIndex: item.dimension_name,
                dimension: item,
                rollup: rollup
            };
            if (App.cubeProperties.isDrillable(level.level_unique_name)) {
                col.cellActions = [{iconCls: 'icon-drill-down', qtip: 'Drill down'}];
            }
            columns.unshift(col);
        }

        return columns;
    };

    var loadData = function() {
        destroyGrid();
        createGrid();
    };

    var relativeOnlyCheckbox = new Ext.form.Checkbox({
        boxLabel: 'Show absolute values',
        listeners: {
            check: loadData
        }
    });

    var container = new Ext.Container({
        collapsible: true,
        border: false,
        hidden: true,
        disabled: true,
        items: [
            {
                xtype: 'container',
                items: relativeOnlyCheckbox
            },
            gridCt
        ]
    });

    /**
     * APIMethod: isRelativeOnly
     * Tells if use wants to display relative values only, or absolute
     * + relatives values
     *
     * Returns:
     * {Boolean}
     */
    var isRelativeOnly = function() {
        return !relativeOnlyCheckbox.checked && App.queryMgr.getQuery().relative;
    };

    App.queryMgr.events.on({
        'beforedataloaded': loadData,
        'beforequeryregistered': function() {
            container.setDisabled(true);
            relativeOnlyCheckbox.setDisabled(true);
        },
        'queryregistered': function() {
            container.show(false);
            container.setDisabled(false);
            relativeOnlyCheckbox.setDisabled(!App.queryMgr.getQuery().relative);
        }
    });

    // public
    return { 
        panel: container,
        isRelativeOnly: isRelativeOnly
    };
}();
