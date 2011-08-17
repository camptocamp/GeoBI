Ext.namespace('App');

App.chart = function() {
    // private

    var onItemCheck = function(item) {
        type = item.charttype;
        updateChart();
    };

    var styleBtn = new Ext.Button({
        text: 'Choose chart type',
        tooltip: 'Modify chart styling',
        cls: 'x-box',
        iconCls: 'style',
        menu: {
            items: [{
                text: 'Pie by row',
                charttype: 'piebyrow',
                checked: true,
                group: 'chart-type',
                checkHandler: onItemCheck
            }, {
                text: 'Pie by column',
                charttype: 'pie',
                checked: false,
                group: 'chart-type',
                checkHandler: onItemCheck
            }, {
                text: 'Vertical bars',
                charttype: 'bar',
                checked: false,
                group: 'chart-type',
                checkHandler: onItemCheck
            }, {
                text: 'Horizontal bars',
                charttype: 'horizontalbar',
                checked: false,
                group: 'chart-type',
                checkHandler: onItemCheck
            }]
        }
    });

    var relativeAbsoluteBtn = new Ext.CycleButton({
        showText: true,
        prependText: 'Values: ',
        tooltip: 'Display relative/absolute values',
        cls: 'x-box',
        disabled: true,
        items: [{ 
            text: 'relative',
            checked: true
        }, {
            text: 'absolute'
        }],
        changeHandler: function(btn, item) {
            updateChart();
        }
    });
    
    var type = "piebyrow";

    var container = new Ext.Container({
        collapsible: true,
        border: false,
        hidden: true,
        disabled: true,
        items: [
            {
                xtype: 'toolbar',
                cls: 'x-toolbar-no-style',
                items: [styleBtn, relativeAbsoluteBtn]
            },
            {
                ref: 'resizableCmp',
                height: 600,
                border: false,
                layout: 'fit',
                style: 'background-color: #dfdfdf',
                items: [{
                    ref: '../chartImg'
                }, {
                    ref: '../chartMaparea'
                }]
            },{
                height: 100,
                border: false
            }
        ]
    });

    // add a resize handle at the bottom of this widget
    container.resizableCmp.on('render', function() {
        el = Ext.get(container.resizableCmp.body.dom);
        el.resizer = new Ext.Resizable(el.id, {
            handles : 's',
            minHeight: 100
        });
        el.resizer.on('resize', function(r,w,h,e) {
            container.resizableCmp.setSize(w, h); 
            updateChart.defer(100);
        });
    });

    var indicatorsStore = new Ext.data.JsonStore({
        fields: ['data_index', 'name'],
        root: 'choropleths_indicators'
    });

    var updateChart = function() {
        var id = Ext.id();
        var url = getChartUrl();
        container.chartImg.update('<img id="' + id + '_img" usemap="#' + id + '" src="' + url + '" />');

        container.chartMaparea.getEl().getUpdater().update({
            url: url + '&map=true&imagemapId=' + id,
            scripts: false
        });
    };

    /**
     * getChartUrl
     */
    var getChartUrl = function() {
        var size = container.chartImg.body.getSize();
        if (App.queryMgr.getQuery().relative) {
            indicatorsStore.filterBy(function(record, id) {
                if (record.get('name').indexOf('Total') != -1) {
                    return false;
                }
                if (relativeAbsoluteBtn.getActiveItem().text == 'relative') {
                    if (record.get('name').indexOf('%') != -1) {
                        return true;
                    }
                } else {
                    if (record.get('name').indexOf('%') == -1) {
                        return true;
                    }
                }
            });
            relativeAbsoluteBtn.setDisabled(false);
        }
        var params = {
            queryId: App.queryId,
            indicators: indicatorsStore.collect('data_index').join(','),
            width: size.width - 2,
            height: size.height - 2,
            type: type
        };
        return '/webbi/getchart?' + Ext.urlEncode(params);
    };

    var loadIndicators = function() {
        indicatorsStore.loadData(App.metadata);
        updateChart();
    };

    App.queryMgr.events.on({
        'metadataloaded': loadIndicators,
        'beforequeryregistered': function() {
            container.setDisabled(true);
            relativeAbsoluteBtn.setDisabled(true);
        },
        'queryregistered': function() {
            container.show(false);
            container.setDisabled(false);
        }
    });

    // public
    return {
        init: function() {
        },

        panel: container,

        getChartUrl: getChartUrl
    };
}();
