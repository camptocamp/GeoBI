// create namespace for plugins
Ext.namespace('Ext.ux.grid');
 
/**
 * @class Ext.ux.grid.DrillableGridPanel
 * @extends Ext.grid.GridPanel
 */
Ext.ux.grid.DrillableGridPanel = function(config) {
    Ext.apply(this, config);
    this.addEvents('drilldown', 'rollup');
    Ext.ux.grid.DrillableGridPanel.superclass.constructor.call(this);
};

Ext.extend(Ext.ux.grid.DrillableGridPanel, Ext.grid.GridPanel);
