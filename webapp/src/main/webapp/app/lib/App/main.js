/**
 * This file represents the application's entry point.
 * OpenLayers and Ext globals are set, and the page
 * layout is created.
 */

window.onload = function() {

    Ext.QuickTips.init();
    App.queryMgr.init();

    var content = new Ext.Panel({
        border: false,
        columnWidth: 1,
        defaults: {
            style: 'padding: 5px',
            frame: true
        },
        items: [
            App.map.panel,
            App.table.panel,
            App.chart.panel
        ]
    });

    var layout = new Ext.Container({
        layout: 'column',
        items: [{
            xtype: 'container',
            width: 300,
            items: [
                {
                    xtype: 'component',
                    contentEl: 'logo'
                },
                App.queryBuilder.panel,
                App.report.panel
            ]
        }, 
            content
        ],
        renderTo: Ext.getBody()
    });

    App.map.init();

    Ext.EventManager.onWindowResize(
        function() {
            layout.doLayout();
        }
    );

    App.queryMgr.events.on({
        'queryregistered': function() {
            layout.doLayout();
        }
    });
};
