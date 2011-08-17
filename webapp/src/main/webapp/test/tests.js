Ext.ns("test");

test.bounds = function() {
    return Ext.get("boundsfield").getValue();
};
test.StartColor='FF6600';
test.EndColor='00CCFF';

test.sampleStyle = function() {
    var cInd = Ext.get("choropleths_indicator").getValue();
    var classification = Ext.get("classification_method").getValue();

    var sr = parseInt(test.StartColor.substring(0,2), 16);
    var sg = parseInt(test.StartColor.substring(2,4), 16);
    var sb = parseInt(test.StartColor.substring(4,6), 16);

    var er = parseInt(test.EndColor.substring(0,2), 16);
    var eg = parseInt(test.EndColor.substring(2,4), 16);
    var eb = parseInt(test.EndColor.substring(4,6), 16);

    var color = sr+" "+sg+" "+sb+","+er+" "+eg+" "+eb;
    var nbClasses = Ext.get("nb_classes").getValue();


    var oInd = Ext.get("overlay_indicators").getValue();
    var overlayCombo = Ext.get("overlay");
    var overlayType;
    if(overlayCombo.getValue() != 'none') {
        overlayType = overlayCombo.getValue();
    }
    return {
        choropleths_indicator: cInd,
        classification_method: classification,
        nb_classes: nbClasses,
        overlay_indicators: oInd,
        colors: color,
        overlay_type: overlayType
    };
};

test.sampleQueries = [['select {[Measures].[AREAHA]} ON COLUMNS, ' + 
        '{[BIOGEOGRAPHIC REGIONS.RBIO].[All Biogeographic Regions].Children} ON ROWS ' + 
        'from [pg_CLC90_00]', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK'],
    //['SELECT {[Measures].[AREAHA]} ON COLUMNS, ' +
        //'CROSSJOIN({[NUTS].[NUTS LEVEL 0].Members}, {[REGIONAL SEA BASINS.RSEA].[SEA BASINS].Members}) ' +
        //'ON ROWS FROM [pg_CLC90_00]', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK'],
    ['SELECT {[Measures].[AREAHA]} ON COLUMNS, ' + 
        'CROSSJOIN({[NUTS].[All NUTS].[FR FRANCE]}, ' +
        '{[REGIONAL SEA BASINS.RSEA].[SEA BASINS].Members}) ON ROWS FROM [pg_CLC90_00]', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK'],
    ['select Crossjoin({[Measures].[AREAHA]}, {[LAND COVER 00.CLC_00].[All Land Covers 00].[1 Artificial surfaces], [LAND COVER 00.CLC_00].[All Land Covers 00].[2 Agricultural areas]}) ON COLUMNS,' +
        ' {[NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST].[FR71 Rhône-Alpes].[FR715 Loire], [NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST].[FR71 Rhône-Alpes].[FR716 Rhône], [NUTS].[All NUTS].[FR FRANCE].[FR7 CENTRE-EST].[FR71 Rhône-Alpes].[FR717 Savoie]} ON ROWS' +
        ' from [pg_CLC90_00]', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_PLIMOBBBADFGBKKHLNHHFBPAFKNNEBIO', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_PLIMOBBBADFGBKKHLNHHFBPAFKNNEBIO,HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_PJDJNBMIDODGDHABGGGNAOAKIJJDAKJL'],
    ['SELECT CROSSJOIN({[Measures].[AREAHA]}, {[CHANGES 90-00.CHANGES].[All changes].[Changes], [CHANGES 90-00.CHANGES].[All changes].[No changes]}) ON COLUMNS, {[REGIONAL SEA BASINS.RSEA].[SEA BASINS].Members} ON ROWS FROM [pg_CLC90_00]', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD', 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD,HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_IGIGDEIIKGPEJMFHHJEHBMDBOMHKHLMN']];

test.queryTextArea=null;
Ext.onReady( function() {
    test.queryTextArea = new Ext.form.FormPanel({
        renderTo: "query",
        height: 300,
        items: [{
           id: 'overlay',
           fieldLabel: "Overlay Type",
            typeAhead: true,
            triggerAction: 'all',
            lazyRender:true,
            mode: 'local',
           xtype:"combo",
           store: new Ext.data.ArrayStore({
            id: 'none',
            fields: [
                'id',
                'text'
            ],
            data: [[1,'none'], [2,'pie'],[3,'bar']]
           }),
           displayField: 'text',
           valueField: 'id',
           anchor: '50%',
           value: 'none'

        }, {
            id: 'overlay_indicators',
            fieldLabel: "Overlay Members",
            xtype:"textfield",
            anchor: '100%',
            value: test.sampleQueries[0][2]
        }, {
            id: 'boundsfield',
            fieldLabel: "Map Bounds",
            xtype:"textfield",
            anchor: '100%',
            value: '943657.8520000001,941523.6410000001,7601956.61,6824985.259000001'
        },{
            id: 'mdxQuery',
            fieldLabel: "Query",
            xtype:"textarea",
            anchor: '100%',
            value: test.sampleQueries[0][0]
        }, {
            id: 'choropleths_indicator',
            fieldLabel: "Cho. Member",
            xtype:"textfield",
            anchor: '100%',
            value: test.sampleQueries[0][1]
//            value: '{[Measures].[AREAHA]}.{[CHANGES 90-00.CHANGES].[All changes].[Changes]}'
            //value: 'HGKCPMGGBBBPMOMLNICINMIHINHOEMIK_IPOBEPNAINJFCLALLBEKJJDJCKANKLHD'

        }, {
            id: 'classification_method',
            fieldLabel: "Classification",
            xtype:"combo",
            mode: 'local',
            forceSelection: true,
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: [
                    'txt'
                ],
                data: [['EqualInterval'],['StandardDeviation'],['UniqueInterval'],['Quantile']]
            }),
            valueField: 'txt',
            displayField: 'txt',
            triggerAction: 'all',
            value: 'EqualInterval'
            
        },{
           id: 'nb_classes',
           fieldLabel: "Num Classes",
           xtype:"numberfield",
           anchor: '50%',
           value: 100

        },{
            fieldLabel: "Colors",
            xtype:'panel',
            anchor: '100%',
            layout: 'hbox',
            height: 100,
            items: [{
                id: 's_color',
                fieldLabel: "start",
                xtype: "colormenu",
                floating:false,
                allowReselect: false,
                hideOnClick: false,
                value: test.StartColor,
                listeners: {
                    select: function(x,color) {
                        test.StartColor = color;
                    }
                }
                },{
                id: 'e_color',
                fieldLabel: "end",
                xtype: "colormenu",
                floating:false,
                allowReselect: false,
                hideOnClick: false,
                value: test.EndColor,
                listeners: {
                    select: function(x,color) {
                        test.EndColor = color;
                    }
                }
            }]
        }]
    });

    var combo = new Ext.form.ComboBox({
        store: new Ext.data.SimpleStore({
            fields: ['mdx', 'choropleths_indicator', 'overlay_indicator'],
            data : test.sampleQueries
        }),
        displayField:'mdx',
        mode: 'local',
        editable: false,
        triggerAction: 'all',
        emptyText:'Select a query...',
        renderTo: 'predefined_queries',
        listeners: {
            select: function(combo, record, value) {
                Ext.getCmp('mdxQuery').setValue(record.get('mdx'));
                Ext.getCmp('choropleths_indicator').setValue(record.get('choropleths_indicator'));
                Ext.getCmp('overlay_indicators').setValue(record.get('overlay_indicator'));
            }
        }
    });
});

test.queryId = null;

test.registerQuery = function(success) {
    Ext.get("imgresults").dom.innerHTML = "";
    Ext.get("results").dom.innerHTML = "Loading...";
    var queryText = Ext.get("mdxQuery");
    var query = queryText.getValue();
    Ext.Ajax.request({
        url: '/webbi/registerquery',
        method: 'POST',
        params: {
            query: query
        },
        success: function(res) {
            Ext.get("results").dom.innerHTML=res.responseText;
            test.queryId = Ext.util.JSON.decode(res.responseText).id;
            if (success !== undefined) {
                success(res);
            }
        },
        failure: function(response) {
            Ext.Msg.show({
               title:'Failure',
               msg: "Failed to register query: "+response.responseText,
               buttons: Ext.Msg.OK,
               icon: Ext.MessageBox.INFO,
               minWidth: 800,
               autoWidth: true
            });
        },
        timeout: 120000
    });
};

test.getData = function() {
    test.registerQuery(function() {
        Ext.get("results").load({
            url: '/webbi/getdata?queryId='+test.queryId
        });
    });
};

test.getMetadata = function() {
    test.registerQuery(function() {
        Ext.get("results").load({
            url: '/webbi/getmetadata?queryId='+test.queryId
        });
    });
};

test.getInfo = function() {
    test.registerStyle(function() {
        Ext.get("results").load({
            url: '/webbi/getinfo?queryId='+test.queryId+'&styleId='+test.styleId+'&bbox=3557560.2941055,2491774.0454961,3687605.1917227,2621818.9431133'
        });
    });
};

test.getMember = function() {
    test.registerStyle(function() {
        Ext.get("results").load({
            url: '/webbi/getmember?queryId='+test.queryId+'&dimension=[NUTS]&bbox=3557560.2941055,2491774.0454961,3687605.1917227,2621818.9431133'
        });
    });
};

test.getMap = function() {
    test.registerStyle(function() {
        Ext.get("results").dom.innerHTML = "";
        Ext.get("imgresults").dom.innerHTML = '<img src="/webbi/getmap?QUERYID='+test.queryId+'&STYLEID='+test.styleId+'&BBOX='+test.bounds()+'&WIDTH=400&HEIGHT=300"/>';
    });
};

test.getChart = function() {
    test.registerQuery(function() {
        Ext.get("results").dom.innerHTML = "";
        var indicators = Ext.getCmp('overlay_indicators').getValue();
        Ext.get("imgresults").dom.innerHTML = '<img src="/webbi/getchart?queryId='+test.queryId+'&indicators=' + indicators + '&type=pie"/>';
    });
};
test.registerStyle = function(success) {
    test.registerQuery(function() {
        Ext.Ajax.request({
            url: '/webbi/registermapstyle',
            method: 'POST',
            params: Ext.apply(test.sampleStyle(), {queryId: test.queryId}),
            success: function(res) {
                Ext.get("results").dom.innerHTML=res.responseText;
                test.styleId = Ext.util.JSON.decode(res.responseText).id;
                if (success !== undefined) {
                    success(res);
                }
            }
        });
    });
};

test.getStyle = function() {
    test.registerStyle(function() {
        Ext.get("results").dom.innerHTML = "Loading...";
        Ext.Ajax.request({
            url: '/webbi/getmapstyle?styleId='+test.styleId,
            success: function(res) {
                var xml = res.responseText.replace(/</g,"&lt;").replace(/>/g,"&gt;");
                Ext.get("results").dom.innerHTML = xml;                    
            }
        });
    });
};

test.registerStyleAndMap = function() {
    test.registerStyle(function() {
        Ext.get("imgresults").dom.innerHTML = '<img src="/webbi/getmap?QUERYID='+test.queryId+'&STYLEID='+test.styleId+'&BBOX='+test.bounds()+'&WIDTH=400&HEIGHT=300"/>';
    });
};

test.print = function() {
    test.registerQuery(function() {

        var indicators = Ext.getCmp('overlay_indicators').getValue();
        var chartURL = 'http://localhost:8080/webbi/getchart?queryId='+test.queryId+'&indicators=' + indicators + '&type=pie';
        var spec = {
            layout: 'A4',
            title: 'GeoBI PDF Reporting Tool',
            srs: 'EPSG:900913',
            units: 'dd',
            dpi: 254,
            outputFilename: 'mapfish-print',
            layers: [
                {
                    type: 'WMS',
                    format: 'image/png',
                    layers: ['places_07','places_06','places-noidx3','places-noidx2','places-noidx1','places_05','places_04','places_03','places_02','places_01','oneways','roadsfar_03','roadsfar_02','roadsfar_01','roadsclose_08','railways_08','roadsclose_07','railways_07','roadsclose_06','railways_06','roadsclose_05','railways_05','roadsclose_04','railways_04','roadsclose_03','railways_03','roadsclose_02','railways_02','roadsclose_01','railways_01','landuse_layer6','landuse_layer5','landuse_layer4','landuse_layer3','landuse_layer2','buildings','landuse_layer1','land'],
                    baseURL: 'http://maps.qualitystreetmap.org/osm'
                }
            ],
            pages: [
                {
                    center: [283596.18669, 6002714.54432],
                    dpi: 254,
                    scale: 256000000,
                    urlLegend: 'http://beneth.fr/~pedrov/geobi/mockup-legend.png',
                    urlChart:  chartURL,
                    mapTitle: "UABEEA GeoBI PDF Reporting tool",
                    comment: "map FR / ES CLC90"
                }
            ]
        };

        var url = Ext.urlAppend('/webbi/report/print.pdf',
                "spec=" + encodeURIComponent(Ext.encode(spec)));
        window.location.href = url; 
    });
};
