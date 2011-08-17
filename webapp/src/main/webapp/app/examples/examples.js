Ext.onReady(function() {
    Ext.QuickTips.init();

    App.queryMgr.init();
    App.queryMgr.events.on({
        'queryregistered': function(id) {
            console.log("the registered query has the following id: ", id);
        }
    });

    var dh = Ext.DomHelper; // create shorthand alias
    dh.append('queries', 
        '<ul>Existing queries:' +
            '<li><a href="javascript:loadExistingQuery(1)">NUTS LEVEL 1 with France Centre Est and Italia Nord Ovest, CLC90 LEVEL1</a></li>' +
            '<li><a href="javascript:loadExistingQuery(2)">NUTS LEVEL 1 with France Centre Est and Italia Nord Ovest, CLC90 LEVEL1 (relative values)</a></li>' +
            '<li><a href="javascript:loadExistingQuery(3)">Regional Sea Basins, CLC90 LEVEL2 and Changes</a></li>' +
            '<li><a href="javascript:loadExistingQuery(4)">Regional Sea Basins and NUTS LEVEL 1 with France Centre Est, CLC90 LEVEL2 and Changes</a></li>' +
            '<li><a href="javascript:loadExistingQuery(5)">NUTS LEVEL 1 with France Centre Est and Regional Sea Basins, CLC90 LEVEL2 and Changes</a></li>' +
            '<li><a href="javascript:loadExistingQuery(6)">NUTS LEVEL 1 with France Centre Est and Regional Sea Basins, CLC90 LEVEL2 and Changes (relative values)</a></li>' +
            '<li><a href="javascript:loadExistingQuery(7)">NUTS0 (France, Spain, Italy, Portugal), CLC00 LEVEL 1 (All members)</a></li>' +
            '<li><a href="javascript:loadExistingQuery(8)">NUTS0 (Deutschland), CLC00 LEVEL 1 (All members)</a></li>' +
            '<li><a href="javascript:loadExistingQuery(9)">cube2 - NUTS0 (France) and River Basin Name (Rhone)</a></li>' +
            '<li><a href="javascript:loadExistingQuery(10)">NUTS0 (FR) and Sea Basins, CLC00 LEVEL 1 (1, 2) and LCF LEVEL 1 (Urban)</a></li>' +
            '<li><a href="javascript:loadExistingQuery(11)">NUTS0 (FR) and Sea Basins, CLC00 LEVEL 1 (1, 2) and LCF LEVEL 1 (Urban)</a></li>' +
        '</ul>'
    );


});

function loadExistingQuery(index) {
    switch (index) {
        case 1:
            App.queryMgr.init();
            App.queryMgr.addRowDimension("[NUTS]", '[NUTS].[NUTS LEVEL 1]');
            App.queryMgr.setMembers("[NUTS]", ["[NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST]", "[NUTS].[All NUTS].[IT ITALIA].[ITC NORD-OVEST]"]);
            App.queryMgr.addColDimension("[LAND COVER 90]", "[LAND COVER 90.CLC_90].[CLC90 LEVEL 1]");
            App.queryMgr.addMeasure(null, "[Measures].[AREAHA]");
            break;
        case 2:
            App.queryMgr.init();
            App.queryMgr.addRowDimension("[NUTS]", '[NUTS].[NUTS LEVEL 1]');
            App.queryMgr.setMembers("[NUTS]", ["[NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST]", "[NUTS].[All NUTS].[IT ITALIA].[ITC NORD-OVEST]"]);
            App.queryMgr.addColDimension("[LAND COVER 90]", "[LAND COVER 90.CLC_90].[CLC90 LEVEL 1]");
            App.queryMgr.addMeasure(null, "[Measures].[AREAHA]");
            App.queryMgr.useRelativeValues(true);
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
        case 6:
            App.queryMgr.setQuery({"cube":{"name":"[pg_CLC90_00]"},"cols":[{"dimension":"[LAND COVER 90]","level":"[LAND COVER 90.CLC_90].[CLC90 LEVEL 2]","members":["[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[12 Industrial, commercial and transport units]","[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[13 Mine, dump and construction sites]"]},{"dimension":"[CHANGES 90-00]","level":"[CHANGES 90-00.CHANGES].[CHANGES]","members":["[CHANGES 90-00.CHANGES].[All changes].[Changes]","[CHANGES 90-00.CHANGES].[All changes].[No changes]"]}],"rows":[{"dimension": "[NUTS]", "level": "[NUTS].[NUTS LEVEL 1]", "members": ["[NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST]"]}, {"dimension":"[REGIONAL SEA BASINS]","level":"[REGIONAL SEA BASINS.RSEA].[SEA BASINS]"}],"measure":{"dimension":"","members":["[Measures].[AREAHA]"]}, "relative": true});
            break;
        case 7:
            App.queryMgr.setQuery({"cube":{"name":"[pg_CLC90_00]"},"cols":[{"dimension":"[LAND COVER 00]","level":"[LAND COVER 00.CLC_00].[CLC00 LEVEL 1]"}],"rows":[{"dimension":"[NUTS]","level":"[NUTS].[NUTS LEVEL 0]","members":["[NUTS].[All NUTS].[ES ESPAÑA]","[NUTS].[All NUTS].[FR FRANCE]","[NUTS].[All NUTS].[IT ITALIA]","[NUTS].[All NUTS].[PT PORTUGAL]"]}],"measure":{"dimension":"","members":["[Measures].[AREAHA]"]},"relative":true});
            break;
        case 8:
            App.queryMgr.setQuery({"cube":{"name":"[pg_CLC90_00]"},"cols":[{"dimension":"[LAND COVER 00]","level":"[LAND COVER 00.CLC_00].[CLC00 LEVEL 1]"}],"rows":[{"dimension":"[NUTS]","level":"[NUTS].[NUTS LEVEL 0]","members":["[NUTS].[All NUTS].[DE DEUTSCHLAND]"]}],"measure":{"dimension":"","members":["[Measures].[AREAHA]"]}});
            break;
        case 9:
            App.queryMgr.setQuery({"cube":{"name":"[CLC90_00_06]"},"cols":[],"rows":[{"dimension":"[NUTS]","level":"[NUTS].[NUTS LEVEL 0]","members":["[NUTS].[All NUTS].[FR France]"]},{"dimension":"[RIVER BASIN DISTRICTS]","level":"[RIVER BASIN DISTRICTS.RBD].[River Basin Name]","members":["[RIVER BASIN DISTRICTS.RBD].[All River Basin Districts].[Mediterranean Sea].[Western Mediterranean Basin].[Rhone and Coastal Mediterranean]","[RIVER BASIN DISTRICTS.RBD].[All River Basin Districts].[Mediterranean Sea].[Western Mediterranean Basin].[Sardinia]"]}],"measure":{"dimension":"","members":["[Measures].[ha]"]}});
            break;
        case 10:
            App.queryMgr.setQuery({"cube":{"name":"pg_CLC90_00"},"cols":[{"dimension":"[LAND COVER 90]","level":"[LAND COVER 90.CLC_90].[CLC90 LEVEL 1]","members":["[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces]","[LAND COVER 90.CLC_90].[All Land Covers 90].[2 Agricultural areas]"]},{"dimension":"[LAND COVER FLOWS 90-00]","level":"[LAND COVER FLOWS 90-00.LCF].[LCF LEVEL 1]","members":["[LAND COVER FLOWS 90-00.LCF].[All Land Cover Flows].[Urban land management]"]}],"rows":[{"dimension":"[NUTS]","level":"[NUTS].[NUTS LEVEL 0]","members":["[NUTS].[All NUTS].[FR FRANCE]"]},{"dimension":"[REGIONAL SEA BASINS]","level":"[REGIONAL SEA BASINS.RSEA].[SEA BASINS]"}],"measure":{"dimension":"","members":["[Measures].[AREAHA]"]}});
            break;
        case 11:
            App.queryMgr.setQuery({"cube":{"name":"pg_CLC90_00"},"cols":[{"dimension":"[LAND COVER 90]","level":"[LAND COVER 90.CLC_90].[CLC90 LEVEL 2]","members":["[LAND COVER 90.CLC_90].[All Land Covers 90].[1 Artificial surfaces].[11 Urban fabric]"]},{"dimension":"[LAND COVER FLOWS 90-00]","level":"[LAND COVER FLOWS 90-00.LCF].[LCF LEVEL 1]","members":["[LAND COVER FLOWS 90-00.LCF].[All Land Cover Flows].[Urban land management]"]}],"rows":[{"dimension":"[NUTS]","level":"[NUTS].[NUTS LEVEL 0]","members":["[NUTS].[All NUTS].[FR FRANCE]"]},{"dimension":"[REGIONAL SEA BASINS]","level":"[REGIONAL SEA BASINS.RSEA].[SEA BASINS]"}],"measure":{"dimension":"","members":["[Measures].[AREAHA]"]}});
            break;
    }
    App.queryMgr.executeQuery();
}
