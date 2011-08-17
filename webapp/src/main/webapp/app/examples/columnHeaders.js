/*!
 * Ext JS Library 3.2.0
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.onReady(function(){

    Ext.QuickTips.init();

    var xg = Ext.grid;

    var reader = new Ext.data.JsonReader({
        fields: [
            'NUTS',
            //{header: 'NUTS', dataIndex: 'nuts'},
            'REGIONAL SEA BASINS',
            'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD',
            'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_GFPKPONNCBPGLABKHPJPHGIIIOLAGHFO_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN',
            'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_GFPKPONNCBPGLABKHPJPHGIIIOLAGHFO_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD',
            'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN'
        ]
    });

    var store = new Ext.data.GroupingStore({
        reader: reader,
        // use local data
        data: app.grid.dummyData,
        sortInfo: {field: 'REGIONAL SEA BASINS', direction: 'ASC'},
        groupField: 'NUTS'
    });

    /*
     * clc90GroupRow at this point is:
     * [
     *     {header: 'Artificial surfaces', colspan: 2, align: 'center'},
     *     {header: 'Agricultural areas', colspan: 2, align: 'center'}
     * ]
     */
    var clc90GroupRow0 = [
          {header: '', colspan: 2, align: 'center'},
          {header: 'AREAHA', colspan: 4, align: 'center'}
    ];
    var clc90GroupRow1 = [
          {header: '', colspan: 2, align: 'center'},
          {header: 'LAND COVER 90 - CLC90 LEVEL 2', colspan: 4, align: 'center', rollup: true}
    ];
    var clc90GroupRow2 = [
          {header: '', colspan: 2, align: 'center'},
          {header: '1 Artificial surfaces', colspan: 2, align: 'center', rollup: true},
          {header: '2 Agricultural areas', colspan: 2, align: 'center', drilldown: true}
    ];
    var clc90GroupRow3 = [
          {header: '', colspan: 2, align: 'center'},
          {header: 'CHANGES 90-00 - CHANGES', colspan: 2, align: 'center'},
          {header: 'CHANGES 90-00 - CHANGES', colspan: 2, align: 'center'}
    ];

    var group = new Ext.ux.grid.ColumnHeaderGroup({
        rows: [clc90GroupRow0, clc90GroupRow1, clc90GroupRow2, clc90GroupRow3]
    });

    group.viewConfig.renderHeaders = function() {
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
    };
    group.viewConfig.templates = {
        gcell: new Ext.XTemplate(
            '<td class="x-grid3-hd x-grid3-gcell x-grid3-td-{id} ux-grid-hd-group-row-{row} {cls}" style="{style}">', 
            '<div {tooltip} class="x-grid3-hd-inner x-grid3-hd-{id}" unselectable="on" style="{istyle}">', 
            '<tpl if="drillable">', '<img class="icon-drill-down" src="', Ext.BLANK_IMAGE_URL, '" ext:qtip="drill down" />&nbsp;','</tpl>',
            '<tpl if="rollable">', '<img class="icon-roll-up" src="', Ext.BLANK_IMAGE_URL, '" ext:qtip="roll up" />&nbsp;','</tpl>',
            '{value}',
            '</div>',
            '</td>'
        )
    };

    var columns = [
        {header: 'NUTS', dataIndex: 'NUTS', cellActions:[{iconCls: 'icon-drill-down', qtip: 'Drill down'}], rollup: {unique_name: "nuts"}},
        //{header: 'NUTS', dataIndex: 'nuts'},
        {header: 'Regional Sea Basins', dataIndex: 'REGIONAL SEA BASINS'},
        {header: 'Changes', dataIndex: 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD'},
        {header: 'No Changes', dataIndex: 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_GFPKPONNCBPGLABKHPJPHGIIIOLAGHFO_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN'},
        {header: 'Changes', dataIndex: 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_GFPKPONNCBPGLABKHPJPHGIIIOLAGHFO_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD'},
        {header: 'No Changes', dataIndex: 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN'}
    ];

    var drillableHeader = new Ext.ux.grid.DrillableHeader();

    var cellActions = new Ext.ux.grid.CellActions({
        listeners:{
            action:function(grid, record, action, value, dataIndex, rowIndex, col) {
                console.log(grid, record, action, value, dataIndex, rowIndex, col);
            }
        },
        align:'left'
    }); 

    var grid = new Ext.grid.GridPanel({
        renderTo: Ext.getBody(), 
        width: 650,
        height: 360,
        frame: true,
        store: store,
        columns: columns,
        plugins: [group, drillableHeader, cellActions]
        ,viewConfig: { forceFit: true }
        //view: new Ext.grid.GroupingView({
            //forceFit: true,
            //showGroupName: false,
            //enableNoGroups: false,
            //enableGroupingMenu: false,
            //hideGroupedColumn: true
        //})
    });
});

// set up namespace for application
Ext.ns('app.grid');

app.grid.dummyData = 
[
    {
        "REGIONAL SEA BASINS": "Atlantic Ocean",
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD": 448,
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_GFPKPONNCBPGLABKHPJPHGIIIOLAGHFO_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN": 35120,
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN": 162368,
        "NUTS": "FR71 Rhône-Alpes"
    },
    {
        "REGIONAL SEA BASINS": "North Sea",
        "NUTS": "FR72 Auvergne"
    },
    {
        "REGIONAL SEA BASINS": "Baltic Sea",
        "NUTS": "FR71 Rhône-Alpes"
    },
    {
        "REGIONAL SEA BASINS": "Black Sea",
        "NUTS": "FR71 Rhône-Alpes"
    },
    {
        "REGIONAL SEA BASINS": "Mediterranean Sea",
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD": 664,
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_GFPKPONNCBPGLABKHPJPHGIIIOLAGHFO_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN": 296928,
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN": 1269592,
        "NUTS": "FR71 Rhône-Alpes",
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_GFPKPONNCBPGLABKHPJPHGIIIOLAGHFO_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD": 160
    },
    {
        "REGIONAL SEA BASINS": "North Sea",
        "NUTS": "FR71 Rhône-Alpes"
    },
    {
        "REGIONAL SEA BASINS": "Atlantic Ocean",
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD": 160,
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_GFPKPONNCBPGLABKHPJPHGIIIOLAGHFO_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN": 61936,
        "HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_DBOJPHONCEGGFIIAODMGLPEECOBAJPEP_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN": 449208,
        "NUTS": "FR72 Auvergne"
    },
    {
        "REGIONAL SEA BASINS": "Baltic Sea",
        "NUTS": "FR72 Auvergne"
    },
    {
        "REGIONAL SEA BASINS": "Black Sea",
        "NUTS": "FR72 Auvergne"
    },
    {
        "REGIONAL SEA BASINS": "Mediterranean Sea",
        "NUTS": "FR72 Auvergne"
    }
];
//app.grid.dummyData = [{
    //nuts: 'Spain',
    //region_sea_basins: 'Atlantic Ocean',
    //artificial_surfaces_changes: 362.603,
    //artificial_surfaces_nochanges: 36.04,
    //agricultural_areas_changes: 150,
    //agricultural_areas_nochanges: 150
//}, {
    //nuts: 'Spain',
    //region_sea_basins: 'Mediterranean Sea',
    //artificial_surfaces_changes: 362.603,
    //artificial_surfaces_nochanges: 36.04,
    //agricultural_areas_changes: 150,
    //agricultural_areas_nochanges: 150
//},{
    //nuts: 'France',
    //region_sea_basins: 'Atlantic Ocean',
    //artificial_surfaces_changes: 59.603,
    //artificial_surfaces_nochanges: 96.04,
    //agricultural_areas_changes: 130,
    //agricultural_areas_nochanges: 85
//}, {
    //nuts: 'France',
    //region_sea_basins: 'Mediterranean Sea',
    //artificial_surfaces_changes: 462.603,
    //artificial_surfaces_nochanges: 38.04,
    //agricultural_areas_changes: 150,
    //agricultural_areas_nochanges: 150
//},{
    //nuts: 'Portugal',
    //region_sea_basins: 'Atlantic Ocean',
    //artificial_surfaces_changes: 362.603,
    //artificial_surfaces_nochanges: 6.5,
    //agricultural_areas_changes: 150,
    //agricultural_areas_nochanges: 150
//}]
