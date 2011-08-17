Ext.onReady(function() {
    Ext.QuickTips.init();

    App.cube = {
        name: '[pg_CLC90_00]'
    };
    App.queryMgr.init();

    var queryBuilder = App.queryBuilder;
    queryBuilder.panel.render(Ext.getBody());
    App.queryMgr.events.on({
        'queryregistered': function(id) {
            console.log("the registered query has the following id: ", id);
        }
    });
});


var qm = App.queryMgr;

function loadExistingQuery(index) {
    switch (index) {
        case 0:
            App.queryMgr.init();
            qm.addRowDimension("[NUTS]", '[NUTS].[NUTS LEVEL 0]');
            qm.addColDimension("[LAND COVER 90]", "[LAND COVER 90.CLC_90].[CLC90 LEVEL 1]");
            qm.addMeasure(null, "[Measures].[AREAHA]");
            break;
        case 1:
            App.queryMgr.init();
            qm.addRowDimension("[NUTS]", '[NUTS].[NUTS LEVEL 1]');
            qm.setMembers("[NUTS]", ["[NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST]", "[NUTS].[All NUTS].[IT ITALIA].[ITC NORD-OVEST]"]);
            qm.addColDimension("[LAND COVER 90]", "[LAND COVER 90.CLC_90].[CLC90 LEVEL 1]");
            qm.addMeasure(null, "[Measures].[AREAHA]");
            break;
        case 2:
            App.queryMgr.init();
            qm.addRowDimension("[NUTS]", '[NUTS].[NUTS LEVEL 1]');
            qm.addColDimension("[LAND COVER 90]", "[LAND COVER 90.CLC_90].[CLC90 LEVEL 1]");
            qm.addMeasure(null, "[Measures].[AREAHA]");
            break;
        case 3:
            App.queryMgr.setQuery({"cube":{"name":"[pg_CLC90_00]"},"cols":[{"dimension":"[LAND COVER 90]","level":"[LAND COVER 90.CLC_90].[CLC90 LEVEL 2]","members":["[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[12 Industrial, commercial and transport units]","[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[13 Mine, dump and construction sites]"]},{"dimension":"[CHANGES 90-00]","level":"[CHANGES 90-00.CHANGES].[CHANGES]","members":["[CHANGES 90-00.CHANGES].[All changes].[Changes]","[CHANGES 90-00.CHANGES].[All changes].[No changes]"]}],"rows":[{"dimension":"[REGIONAL SEA BASINS]","level":"[REGIONAL SEA BASINS.RSEA].[SEA BASINS]"}],"measure":{"dimension":"","members":["[Measures].[AREAHA]"]}});
            break;
        case 4:
            App.queryMgr.setQuery({"cube":{"name":"[pg_CLC90_00]"},"cols":[{"dimension":"[LAND COVER 90]","level":"[LAND COVER 90.CLC_90].[CLC90 LEVEL 2]","members":["[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[12 Industrial, commercial and transport units]","[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[13 Mine, dump and construction sites]"]},{"dimension":"[CHANGES 90-00]","level":"[CHANGES 90-00.CHANGES].[CHANGES]","members":["[CHANGES 90-00.CHANGES].[All changes].[Changes]","[CHANGES 90-00.CHANGES].[All changes].[No changes]"]}],"rows":[{"dimension":"[REGIONAL SEA BASINS]","level":"[REGIONAL SEA BASINS.RSEA].[SEA BASINS]"}, {"dimension": "[NUTS]", "level": "[NUTS].[NUTS LEVEL 1]", "members": ["[NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST]"]}],"measure":{"dimension":"","members":["[Measures].[AREAHA]"]}});
            break;
        case 5:
            App.queryMgr.setQuery({"cube":{"name":"[pg_CLC90_00]"},"cols":[{"dimension":"[LAND COVER 90]","level":"[LAND COVER 90.CLC_90].[CLC90 LEVEL 2]","members":["[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[12 Industrial, commercial and transport units]","[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[13 Mine, dump and construction sites]"]},{"dimension":"[CHANGES 90-00]","level":"[CHANGES 90-00.CHANGES].[CHANGES]","members":["[CHANGES 90-00.CHANGES].[All changes].[Changes]","[CHANGES 90-00.CHANGES].[All changes].[No changes]"]}],"rows":[{"dimension": "[NUTS]", "level": "[NUTS].[NUTS LEVEL 1]", "members": ["[NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST]"]}, {"dimension":"[REGIONAL SEA BASINS]","level":"[REGIONAL SEA BASINS.RSEA].[SEA BASINS]"}],"measure":{"dimension":"","members":["[Measures].[AREAHA]"]}});
            break;
    }
}

function drillDown(index) {
    switch (index) {
        case 0:
            qm.drillDown({
                dimension: '[NUTS]',
                member: '[NUTS].[All NUTS].[FR FRANCE]'
            });
            break;
        case 1:
            qm.drillDown({
                dimension: '[LAND COVER 90]',
                member: '[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[12 Industrial, commercial and transport units]'
            });
            break;
    }
}

function rollUp(index) {
    switch (index) {
        case 0:
            qm.rollUp({
                dimension: '[NUTS]'
            });
            break;
        case 1:
            qm.rollUp({
                dimension: '[LAND COVER 90]'
            });
            break;
    }
}
