/**
 * Widget for the print to pdf form.
 * This widget is not standalone and requires that map and table widgets are
 * loaded.
 */
Ext.namespace('App');

App.report = function() {
    // The printProvider that connects us to the print service
    var printProvider = new GeoExt.data.PrintProvider({
        method: "POST",
        url: './report',
        autoLoad: true
    });

    printProvider.on({
        'beforeprint': function() {
            printButton.setIconClass('loading');
            var hostPort = "http://" + window.location.hostname +
                           (window.location.port === "" ? "" : ':' + window.location.port);
            var legendUrl = hostPort + './getlegend'+
                '?QUERYID='+App.queryId+
                '&STYLEID='+App.styleId+
                '&FORMAT_OPTIONS=dpi:254';
            var chartUrl = hostPort + App.chart.getChartUrl();

            // table
            var relativeOnly = App.table.isRelativeOnly();
            var groupHeaders = App.cubeProperties.getHeaderGroupRows(App.metadata, relativeOnly);
            var columns = App.cubeProperties.getColumns(App.metadata, relativeOnly);
            // spatial dimensions specific column(s)
            for (var len = App.metadata.rows.length - 1, j = len; j >= 0; j--) {
                var item = App.metadata.rows[j],
                    level = item.levels[0];
                var name = App.cubeProperties.findLevelByUniqueName(level.level_unique_name)
                              .get('LEVEL_NAME');
                var col = {
                    header: name,
                    dataIndex: item.dimension_name
                };
                columns.unshift(col);
            }
            var data = [];
            App.data.each(function(record) {
                data.push(record.data);
            });

            Ext.apply(printPage.customParams, {
                urlLegend: legendUrl,
                urlChart: chartUrl,
                groupHeaders: groupHeaders,
                columns: columns,
                data: data
            });
        },
        'print': function() {
            printButton.setIconClass(printButton.initialConfig.iconCls);
        }
    });

    var printForm;
    var printPage;

    var button = new Ext.Button({
        text: 'Export as PDF',
        cls: 'x-box x-btn-arrow-expand',
        iconCls: 'pdf',
        handler: function(button) {
            createForm();
        }
    });

    var container = new Ext.Container({
        border: false,
        disabled: true,
        items: [
            button
        ]
    });

    var printButton;
    /**
     * Method: createForm
     * Creates the SimplePrint form
     * Should happens only once the mapPanel is ready
     */
    var createForm = function() {
        if (!printForm) {
            printPage = new GeoExt.data.PrintPage({
                printProvider: printProvider
            });

            printButton = new Ext.Button({
                text: "Create PDF",
                iconCls: "pdf",
                handler: function() {
                    printProvider.print(App.map.mapPanel, printPage);
                }
            });


            // a simple print form
            printForm = new Ext.form.FormPanel({
                labelWidth: 65,
                defaults: {width: 115},
                items: [{
                    xtype: "textfield",
                    name: "mapTitle",
                    fieldLabel: "Title",
                    value: "A custom title",
                    plugins: new GeoExt.plugins.PrintPageField({
                        printPage: printPage
                    })
                }, {
                    xtype: "textarea",
                    fieldLabel: "Comment",
                    name: "comment",
                    value: "A custom comment",
                    plugins: new GeoExt.plugins.PrintPageField({
                        printPage: printPage
                    })
                }, {
                    xtype: "combo",
                    fieldLabel: "Layout",
                    store: printProvider.layouts,
                    displayField: "name",
                    typeAhead: true,
                    mode: "local",
                    forceSelection: true,
                    triggerAction: "all",
                    selectOnFocus: true,
                    plugins: new GeoExt.plugins.PrintProviderField({
                        printProvider: printProvider
                    })
                }],
                buttons: [printButton]
            });
            container.add(printForm);
            container.doLayout();
            App.map.mapPanel.map.events.on({
                "moveend": fitPage 
            });
            fitPage();
        } else {
            printForm.toggleCollapse();
        }
    };

    /**
     * Method
     * Fit the print page to the map, at startup and then each time the map is
     * moved
     */
    var fitPage = function() {
        printPage.fit(App.map.mapPanel.map);
    };

    App.mapStyler.events.on({
        'styleregistered': function(id) {
            if (App.data && App.data.reader.jsonData) {
                container.setDisabled(false);
            }
        }
    });
    App.queryMgr.events.on({
        'beforequeryregistered': function() {
            container.setDisabled(true);
        },
        'dataloaded': function() {
            if (App.styleId) {
                container.setDisabled(false);
            }
        }
    });

    return {
        panel: container
    };
}();
