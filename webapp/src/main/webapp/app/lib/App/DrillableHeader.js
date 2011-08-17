// create namespace for plugins
Ext.namespace('Ext.ux.grid');
 
/**
 * Ext.ux.plugins.DrillableHeader plugin for Ext.grid.GridView
 *
 * @author  Pierre GIRAUD

 *
 * @class Ext.ux.grid.DrillableHeader
 * @extends Ext.util.Observable
 */
 
// plugin code
Ext.ux.grid.DrillableHeader = function() {
    return {    
        /**
         *    init
         */
        init : function(grid) {
            this.grid = grid;
            var view = grid.getView();
            view.beforeMethod('initTemplates', this.initTemplates);
            view.afterMethod('handleHdDown', this.handleHdDown, this);
        },
        
        /**
         *    initTemplates
         * 
         *    Sets up the GridView template for the header cell. Adds a DIV with class "ux-grid3-hd-wrap"
         *  to handle vertically aligning the header text.
         */
        initTemplates : function(){
            var ts = this.templates || {};

            if(!ts.hcell){
                ts.hcell = new Ext.XTemplate(
                        '<td class="x-grid3-hd x-grid3-cell x-grid3-td-{id}" style="{style}"><div {attr} class="x-grid3-hd-inner x-grid3-hd-{id}" unselectable="on" style="{istyle}"><div {tooltip} class="ux-grid3-hd-wrap">', this.grid.enableHdMenu ? '<a class="x-grid3-hd-btn" href="#"></a>' : '',
                        '<tpl if="drillable">', '<img class="icon-drill-down" src="', Ext.BLANK_IMAGE_URL, '" ext:qtip="drill down" />&nbsp;','</tpl>',
                        '<tpl if="rollable">', '<img class="icon-roll-up" src="', Ext.BLANK_IMAGE_URL, '" ext:qtip="roll up" />&nbsp;','</tpl>',
                        '{value}<img class="x-grid3-sort-icon" src="', Ext.BLANK_IMAGE_URL, '" />',
                        "</div></div></td>"
                        );
            }
            
            this.templates = ts;
        },

        // private
        handleHdDown : function(e, t){
            var view = this.grid.getView();
            function getCellConfig() {
                var hd = view.findHeaderCell(t),
                    index = view.getCellIndex(hd);
                return view.cm.config[index];
            }
            var r, evtName;
            // user clicked on a group header cell
            if (Ext.fly(t).findParent('td.x-grid3-gcell', view.cellSelectorDepth)) {
                return;
            }
            if(Ext.fly(t).hasClass('icon-drill-down')) {
                r = getCellConfig.call(this);
                evtName = "drilldown";
            }
            if(Ext.fly(t).hasClass('icon-roll-up')) {
                r = getCellConfig.call(this);
                evtName = "rollup";
            }
            if (r) {
                this.grid.fireEvent(evtName, r[evtName], this.grid);
            }
        }
    };
};

// Manual overrides
Ext.override(Ext.grid.GridView, {

    renderHeaders : function() {
        var cm   = this.cm,
            ts   = this.templates,
            ct   = ts.hcell,
            cb   = [],
            p    = {},
            len  = cm.getColumnCount(),
            last = len - 1;

        for (var i = 0; i < len; i++) {
            p.id = cm.getColumnId(i);
            p.value = cm.getColumnHeader(i) || '';
            p.style = this.getColumnStyle(i, true);
            p.tooltip = this.getColumnTooltip(i);
            p.css = i === 0 ? 'x-grid3-cell-first ' : (i == last ? 'x-grid3-cell-last ' : '');
            p.drillable = this.isColumnDrillable(i);
            p.rollable = this.isColumnRollable(i);

            if (cm.config[i].align == 'right') {
                p.istyle = 'padding-right:16px';
            } else {
                delete p.istyle;
            }
            cb[cb.length] = ct.apply(p);
        }
        return ts.header.apply({cells: cb.join(''), tstyle:'width:'+this.getTotalWidth()+';'});
    },

    // private
    isColumnDrillable : function(col){
        return this.cm.config[col].drilldown;
    },

    // private
    isColumnRollable : function(col){
        return this.cm.config[col].rollup;
    }
});

