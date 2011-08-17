
// create namespace for plugins
Ext.namespace('Ext.ux.grid');
 
/**
 * Ext.ux.plugins.DrillableHeader plugin for Ext.grid.GridView
 *
 * @author  Pierre GIRAUD
 * @date June, 2 2010
 *
 * @class Ext.ux.grid.DrillableColumnHeaderGroup
 * @extends Ext.ux.grid.ColumnHeaderGroup
 */
Ext.ux.grid.DrillableColumnHeaderGroup = function(config) {
    Ext.ux.grid.DrillableColumnHeaderGroup.superclass.constructor.call(this, config);
    this.addEvents('drilldown', 'rollup');

    Ext.apply(this.viewConfig, {
        // Overrides renderHeaders to pass drillable & rollable to the template
        renderHeaders: function() {
            var ts = this.templates, headers = [], cm = this.cm, rows = cm.rows, tstyle = 'width:' + this.getTotalWidth() + ';';

            for(var row = 0, rlen = rows.length; row < rlen; row++){
                var r = rows[row], cells = [];
                for(var i = 0, gcol = 0, len = r.length; i < len; i++){
                    var group = r[i];
                    group.colspan = group.colspan || 1;
                    var id = this.getColumnId(group.dataIndex ? cm.findColumnIndex(group.dataIndex) : gcol), gs = Ext.ux.grid.ColumnHeaderGroup.prototype.getGroupStyle.call(this, group, gcol);
                    cells[i] = ts.gcell.apply({
                        cls: 'ux-grid-hd-group-cell',
                        id: id,
                        row: row,
                        style: 'width:' + gs.width + ';' + (gs.hidden ? 'display:none;' : '') + (group.align ? 'text-align:' + group.align + ';' : ''),
                        tooltip: group.tooltip ? (Ext.QuickTips.isEnabled() ? 'ext:qtip' : 'title') + '="' + group.tooltip + '"' : '',
                        istyle: group.align == 'right' ? 'padding-right:16px' : '',
                        btn: this.grid.enableHdMenu && group.header,
                        value: group.header || '&nbsp;',
                        drillable: group.drilldown,
                        rollable: group.rollup
                    });
                    gcol += group.colspan;
                }
                headers[row] = ts.header.apply({
                    tstyle: tstyle,
                    cells: cells.join('')
                });
            }
            headers.push(this.constructor.prototype.renderHeaders.apply(this, arguments));
            return headers.join('');
        },

        // Overrides gcell templates for drillable/rollable actions
        templates: {
            gcell: new Ext.XTemplate(
                '<td class="x-grid3-hd x-grid3-gcell x-grid3-td-{id} ux-grid-hd-group-row-{row} {cls}" style="{style}">',
                '<div {tooltip} class="x-grid3-hd-inner x-grid3-hd-{id}" unselectable="on" style="{istyle}">',
                '<tpl if="drillable">', '<img class="icon-drill-down" src="', Ext.BLANK_IMAGE_URL, '" ext:qtip="drill down" />&nbsp;','</tpl>',
                '<tpl if="rollable">', '<img class="icon-roll-up" src="', Ext.BLANK_IMAGE_URL, '" ext:qtip="roll up" />&nbsp;','</tpl>',
                '{value}',
                '</div>',
                '</td>'
            )
        },

        handleHdDown: function(e, t){
            function getCellConfig() {
                var cell = this.findHeaderCell(t),
                    view = this.grid.getView(),
                    m = cell.className.match(view.colRe),
                    colIndex = (m && m[1]) ? m[1] : -1, 
                    rowIndex = Ext.ux.grid.ColumnHeaderGroup.prototype
                        .getGroupRowIndex.call(view, cell),
                    row = this.cm.rows[rowIndex];
                var r = false;
                Ext.each(row, function(cell) {
                    if (cell.colIndex == colIndex) {
                        r = cell;
                    }
                });
                return r; 
            }

            var r, evtName;
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
    });
};

Ext.extend(Ext.ux.grid.DrillableColumnHeaderGroup, Ext.ux.grid.ColumnHeaderGroup, {});
