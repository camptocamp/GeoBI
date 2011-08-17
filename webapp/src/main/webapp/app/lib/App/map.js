Ext.namespace('App');

App.map = function() {
    // private
    var mapStyler = App.mapStyler;

    var styleBtn = new Ext.Button({
        text: 'Change map style',
        cls: 'x-box x-btn-arrow-expand',
        iconCls: 'style',
        disabled: true,
        handler: function(button){
            mapStyler.panel.toggleCollapse(false);
            var collapsed = mapStyler.panel.collapsed;
            button.removeClass('x-btn-arrow' + ((collapsed) ? '-collapse' : '-expand'));
            button.addClass('x-btn-arrow' + ((collapsed) ? '-expand' : '-collapse'));
            updateLegend(true);
            updateMap();
        }
    });

    mapStyler.events.on({
        'styleregistered': function(id) {
            applyStyle(id);
            App.styleId = id;
            updateLegend();
        }
    });
    var updateMap = function() {
        mapPanel && mapPanel.map.updateSize();
    };

    var layer;

    var loadingPanel;

    /**
     * Property: navigables
     * {Array} Represents the list of drillable or rollable spatial dimensions
     */
    var navigables = null;

    /**
     * Property: currentNavigable 
     * {Array} Represents the currently choosen dimension for navigation (drill
     * or roll)
     */
    var currentNavigable = null;

    /**
     * Method: applyStyle
     * Parameters
     * id {Integer} The registered style
     */
    var applyStyle = function(id) {
        if (!layer) {
            layer = new OpenLayers.Layer.WMS(
                layer,
                "/webbi/getmap",
                {
                    queryId: App.queryId,
                    styleId: id,
                    format: 'image/png'
                },{
                    singleTile: true,
                    transitionEffect: 'resize'
                }
            );
            mapPanel.map.addLayer(layer);
        } else {
            layer.mergeNewParams({
                queryId: App.queryId,
                styleId: id
            });
        }
        layer.setVisibility(true);
    };

    var getInfoControl;

    var addControls = function() {
        getInfoControl = new App.GetInfo({
            protocol: new OpenLayers.Protocol.HTTP({
                url: "/webbi/getinfo",
                format: new OpenLayers.Format.GeoJSON(),
                params: {
                    queryId: null,
                    styleId: null
                }
            })
        });
        mapPanel.map.addControl(getInfoControl);
        getInfoControl.events.on({
            'beforerequest': function() {
                container.info.update('');
            },
            'featureselected': function(obj) {
                displayInfo(obj.feature, obj.evt.xy);
            }
        });
        mapStyler.events.on('styleregistered', function(id) {
            styleBtn.enable();
            getInfoControl.protocol.params.styleId = id;
            getInfoControl.protocol.params.queryId = App.queryId;
            getInfoControl.activate();
        });

        loadingPanel = new OpenLayers.Control.LoadingPanel();
        mapPanel.map.addControl(loadingPanel);

        App.queryMgr.events.on({
            'beforequeryregistered': function() {
                styleBtn.disable();
                container.info.update('');
                loadingPanel.maximizeControl();
                getInfoControl.deactivate();
            }
        });
    };

    /**
     * Method: configureControls
     * Configures and activates the controls with the metadata
     */
    var configureControls = function(metadata) {
        getNavigableDimensions(metadata);

        addDrillDownControl();
        addRollupControl();
    };

    /**
     * Method: getNavigableDimensions
     * Get the dimensions that are drillable or rollable for
     *     the current query (ie. metadata)
     *
     *  Parameters:
     *  metadata {Object}
     */
    var getNavigableDimensions = function(metadata) {
        navigables = {}; 
        var drillable, rollable;
        // check if there's at least one drillable level
        for (i = 0; i < metadata.rows.length; i++) {
            row = metadata.rows[i]; 
            for (j = 0; j < row.levels.length; j++) {
                level = row.levels[j];
                drillable = App.cubeProperties.isDrillable(level.level_unique_name);
                rollable = App.cubeProperties.isRollable(level.level_unique_name);

                if (drillable || rollable) {
                    navigables[row.dimension_unique_name] = [drillable, rollable, row.dimension_name];
                }
            }
        }

        // FIXME chooses the last one
        if (!currentNavigable || !navigables[currentNavigable]) {
            for (var i in navigables) {
                currentNavigable = i;
            }
        }

        var menu = navigableChooser.menu;
        menu.removeAll();
        for (var i in navigables) {
            var checkItem = new Ext.menu.CheckItem({
                text: navigables[i][2],
                group: 'navigable',
                checked: currentNavigable == i,
                listeners: {
                    checkchange: (function(item, checked, i) {
                        if (checked) {
                            currentNavigable = i;
                            addDrillDownControl();
                            addRollupControl();
                        }
                    }).createDelegate(null, i, true)
                }
            });
            menu.add(checkItem);
        }
    };

    /**
     * Method: addRollupControl
     * Optionaly adds the roll up control
     */
    var addRollupControl = function() {
        container.rollupBtn.setDisabled(!navigables[currentNavigable][1]);
        container.rollupBtn.setHandler(function(button) {
            loadingPanel.maximizeControl();
            App.queryMgr.rollUp({
                dimension: currentNavigable
            });
        });
    };

    var drillDownControl = null;

    /**
     * Method: addDrillDownControl
     * Optionaly adds the drill down control
     */
    var addDrillDownControl = function() {
        if (navigables[currentNavigable][0]) {

            // FIXME we should put this in a separate file, and add a updateConfig
            // method
            // We should avoid the drillDownControl destroy and recreation
            drillDownControl = new (OpenLayers.Class(OpenLayers.Control, {
                clickTolerance: 0,
                initialize: function(options) {
                    OpenLayers.Control.prototype.initialize.apply(this, arguments); 
                    this.handler = new OpenLayers.Handler.Click(
                        this, {
                            'click': this.onClick
                        }
                    );
                }, 
                destroy: function() {
                    this.handler && this.handler.destroy();
                    OpenLayers.Control.prototype.destroy.call(this);
                },
                onClick: function(evt) {
                    loadingPanel.maximizeControl();
                    var bounds = OpenLayers.Control.GetFeature.prototype.pixelToBounds.call(this, evt.xy); // I'm not proud of this one
                    OpenLayers.Request.GET({
                        url: '/webbi/getmember',
                        params: {
                            queryId: App.queryId,
                            bbox: bounds.toBBOX(),
                            dimension: currentNavigable
                        },
                        success: function(request) {
                            if (request.responseText) {
                                var member = App.cubeProperties.findMemberByName(currentNavigable, request.responseText);
                                App.queryMgr.drillDown({
                                    dimension: currentNavigable,
                                    member: member.get('MEMBER_UNIQUE_NAME')
                                });
                            } else {
                                loadingPanel.minimizeControl();
                            }
                        },
                        failure: function(request) {
                            loadingPanel.minimizeControl();
                        }
                    });
                }
            }))();
            mapPanel.map.addControl(drillDownControl);
            drillDownControl.activate();
        }
    };

    /**
     * Method: displayInfo
     * Parameters:
     * feature {OpenLayers.Feature.Vector}
     */
    var displayInfo = function(feature, position) {
        var html = '<h2>' + feature.attributes.member + '</h2>';
        html += '<table>';
        for (var k in feature.attributes) {
            if (k != 'boundedBy' && k != 'msGeometry' &&
                k != 'point' && k!= 'member') {
                html += '<tr>' +
                    '<th>' + k + '</th>' +
                    '<td>' + Ext.util.Format.number(feature.attributes[k], '1,000') + '</td>' +
                    '</tr>';
            }
        }
        html += '</table>';

        if (navigables[currentNavigable][0]) {
            html += '<div class="drillinfo" onclick="alert(\'not here! on the map of course!\');">Click on the map to drill down.</div>';
        }

        container.info.update(html);
    };

    /**
     * Property: mapPanel
     * {GeoExt.MapPanel}
     */
    var mapPanel = new GeoExt.MapPanel({
        region: 'center',
        border: false,
        map: {
            maxExtent: new OpenLayers.Bounds(943657.8520000001,941523.6410000001,7601956.61,6824985.259000001),
            units: 'm',
            maxResolution: 'auto',
            controls: [new OpenLayers.Control.Navigation()]
        },
        layers: [
            new OpenLayers.Layer.WMS(
                'countries',
                '/webbi/getbaselayer',
                {
                    format: 'image/png'
                },
                {
                    isBaseLayer: true,
                    singleTile: true
                }
            )
        ]
    });

    var legend = new Ext.Panel({
        region: 'east',
        width: '25%',
        autoScroll: true,
        html: '',
        border: false
    });

    var navigableChooser = new Ext.menu.Item({
        text: 'Dimension to drill down / roll up',
        menu: new Ext.menu.Menu({}) 
    });

    var container = new Ext.Container({
        items: [
            {
                xtype: 'container',
                items: styleBtn
            },
            //{
                //xtype: 'box',
                //html: '<h3>Map</h3>'
            //},
            mapStyler.panel,
            {
                ref: 'resizableCmp',
                layout: 'fit',
                items: [{
                    border: false,
                    layout: 'border',
                    height: 400,
                    items: [
                        mapPanel,
                        legend
                    ]
                }, {
                    xtype: 'container',
                    cls: 'map-left-tbar',
                    autoEl: {
                        tag: 'div'
                    },
                    items: [
                        new Ext.Button({
                            iconCls: 'plus',
                            tooltip: 'Zoom in',
                            handler: function() {
                                mapPanel && mapPanel.map && mapPanel.map.zoomIn();
                            }
                        }),
                        new Ext.Button({
                            iconCls: 'minus',
                            tooltip: 'Zoom out',
                            handler: function() {
                                mapPanel && mapPanel.map && mapPanel.map.zoomOut();
                            }
                        }),
                        new Ext.Button({
                            ref: '../../rollupBtn',
                            text: "Roll up",
                            tooltip: "Click to roll up",
                            iconCls: 'roll-up',
                            disabled: true
                        }),
                        new Ext.Button({
                            text: 'Options',
                            menu: [navigableChooser]
                        })
                    ]
                }, {
                    ref: '../info',
                    bodyStyle: 'padding: 2px;',
                    autoEl: {
                        tag: 'div',
                        style: 'position: absolute; top: 4px; left: 4px;'
                    }
                }]
            }
        ]
    });

    container.on('render', function() {
        var el = container.getEl();
        el.on('mouseleave', function() {
            container.info.update('');
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
            });
        });
    });

    var updateLegend = function(onlyPosition) {
        var uri = '/webbi/getlegend'+
            '?QUERYID='+App.queryId+
            '&STYLEID='+App.styleId;
        legend.body.update('<img src="'+uri+'" style="display:block"/>', false);
    };

    /**
     * Method: loadMap
     */
    var loadMap = function() {
        layer && layer.setVisibility(false);
        if (drillDownControl) {
            mapPanel.map.removeControl(drillDownControl);
            drillDownControl.destroy();
        }

        var b = App.metadata.bbox;
        mapPanel.map.zoomToExtent(new OpenLayers.Bounds(b[0], b[1], b[2], b[3]));

        configureControls(App.metadata);
        
        // register the default style
        mapStyler.registerStyle();
    };

    App.queryMgr.events.on({
        'metadataloaded': loadMap
    });

    // public
    return {
        init: function(){
            addControls();
        },

        panel: container,

        mapPanel: mapPanel
    };
}();
