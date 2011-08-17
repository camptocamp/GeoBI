var cp = App.cubeProperties;
function log(string) {
    Ext.DomHelper.append(Ext.getBody(), {
        tag: 'div',
        html: string
    });
}
function init() {
    //log(cp.getLevelsMaxNumber('[NUTS]'));
    //log(cp.findLevel('[NUTS].[NUTS LEVEL 3]').get('LEVEL_UNIQUE_NAME'));
    log("[NUTS].[NUTS LEVEL 3] is drillable " + cp.isDrillable('[NUTS].[NUTS LEVEL 3]'));
    log("[NUTS].[NUTS LEVEL 2] is drillable " + cp.isDrillable('[NUTS].[NUTS LEVEL 2]'));
    log("[NUTS].[NUTS LEVEL 2] is rollable " + cp.isRollable('[NUTS].[NUTS LEVEL 2]'));
    log("[NUTS].[NUTS LEVEL 0] is rollable " + cp.isRollable('[NUTS].[NUTS LEVEL 0]'));
    log("[REGIONAL SEA BASINS.RSEA].[SEA BASINS] is rollable " + cp.isRollable("[REGIONAL SEA BASINS.RSEA].[SEA BASINS]"));
    log("get FR France member " + cp.findMemberByName('[NUTS]', 'FR France'));
    var member = cp.findMemberByName('[NUTS]', 'FR France');
    var children = cp.getMemberChildren('[NUTS]', member.get('MEMBER_UNIQUE_NAME'));
    log("get FR France member children " + children.join(', '));
    member = cp.findMemberByName('[NUTS]', 'IT Italia');
    children = cp.getMemberChildren('[NUTS]', member.get('MEMBER_UNIQUE_NAME'));
    log("get IT Italia member children " + children.join(', '));

    cp.findMemberParent('[NUTS]', '[NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST]');
}
Ext.onReady(function() {
    Ext.QuickTips.init();
    
    init.defer(2000);
});
