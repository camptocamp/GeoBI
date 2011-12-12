Ext.namespace('App');

Ext.layout.FormLayout.prototype.trackLabels = true;

App.mapStyler = function() {

    var _events = new Ext.util.Observable();

    var startColor = 'FFFF00';
    var endColor = 'FF8500';

    var indicatorsStore = new Ext.data.JsonStore({
        fields: ['data_index', 'name'],
        root: 'choropleths_indicators',
        listeners: {
            load: function(store, data) {
                indicatorsCombo.setValue(store.getAt(0).get(indicatorsCombo.valueField));
                overlayIndicatorsCombo.setValue(store.getAt(0).get(overlayIndicatorsCombo.valueField));
            }
        }
    });
    
    var indicatorsCombo = new Ext.form.ComboBox({
        hiddenName: 'choropleths_indicator',
        fieldLabel: 'Indicator',
        anchor: '100% -68',
        store: indicatorsStore,
        valueField: 'data_index',
        displayField: 'name',
        triggerAction: 'all',
        editable: false,
        mode: 'local'
    });

    var classificationStore = new Ext.data.ArrayStore({
        fields: ['value', 'label'],
        listeners: {
            load: function(store, data) {
                classificationCombo.setValue(store.getAt(0).get(classificationCombo.valueField));
            }
        }
    });

    var onClassificationChange = function(combo) {
        var unique = combo.getValue() == "UniqueInterval";
        nbClassesField.setVisible(!unique);
        nbClassesField.setDisabled(unique);
        formPanel.doLayout();
    };

    var classificationCombo = new Ext.form.ComboBox({
        hiddenName: 'classification_method',
        fieldLabel: 'Classif. method',
        anchor: '100% -68',
        store: classificationStore,
        mode: 'local',
        triggerAction: 'all',
        valueField: 'value',
        displayField: 'label',
        editable: false,
        listeners: {
            select: onClassificationChange
        }
    });

    classificationStore.loadData(
        [[
            'EqualInterval', 'Equal Interval'
        ], [
            'UniqueInterval', 'Unique Values'
        ], [
            'Quantile', 'Quantile'
        ]]
    );

    var _registerStyle = function(button) {
        // queryId is mandatory for the request to work
        var params = buildParams();
        if (!params.queryId) return;
        Ext.Ajax.request({
            url: './registermapstyle',
            success: function(response) {
                var id = Ext.util.JSON.decode(response.responseText).id;
                _events.fireEvent('styleregistered', id);
            },
            failure: function(response) {
                App.errorMgr.show(response, 'applying style');
            },
            params: params
        });
    };

    var buildParams = function() {
        var params = formPanel.getForm().getValues();

        var sr = parseInt(startColor.substring(0,2), 16);
        var sg = parseInt(startColor.substring(2,4), 16);
        var sb = parseInt(startColor.substring(4,6), 16);

        var er = parseInt(endColor.substring(0,2), 16);
        var eg = parseInt(endColor.substring(2,4), 16);
        var eb = parseInt(endColor.substring(4,6), 16);

        var color = sr+" "+sg+" "+sb+","+er+" "+eg+" "+eb;

        if (params.overlay_type == 'bar' || 
            params.overlay_type == 'pie') {
            if (App.queryMgr.getQuery().relative) {
                indicatorsStore.filterBy(function(record, id) {
                    if (record.get('name').indexOf('Total') != -1) {
                        return false;
                    }
                    if (relativeAbsoluteRadioGroup.getValue().inputValue == 'relative') {
                        if (record.get('name').indexOf('%') != -1) {
                            return true;
                        }
                    } else {
                        if (record.get('name').indexOf('%') == -1) {
                            return true;
                        }
                    }
                });
            }
            params.overlay_indicators = indicatorsStore.collect('data_index').join(',');
            indicatorsStore.clearFilter();
        } else if (params.overlay_type) {
            params.sizes = params.overlay_min_size + "," + params.overlay_max_size ;
        }
        return Ext.apply(params, {
            queryId: App.queryId,
            colors: color
        });
    };

    var nbClassesField = new Ext.form.SpinnerField({
        xtype: 'spinnerfield',
        fieldLabel: 'Nb classes',
        name: 'nb_classes',
        minValue: 1,
        maxValue: 100,
        incrementValue: 1,
        width: 50,
        value: 5
    });

    var choroplethsForm = {
        xtype: 'fieldset',
        layout: 'form',
        title: 'Choropleths',
        collapsible: true,
        titleCollapse: true,
        items: [
            indicatorsCombo,
            classificationCombo,
            nbClassesField,
        {
            xtype: 'compositefield',
            fieldLabel: 'Colors',
            items: [
                {xtype: 'displayfield', value: 'From'},
                {
                    xtype: 'button',
                    text: "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
                    menu: {
                        xtype: 'colormenu',
                        value: startColor,
                        listeners: {
                            select: function(menu, color) {
                                startColor = color;
                                menu.ownerCt.ownerCt.btnEl.setStyle("background", "#" + color);
                            }
                        }
                    },
                    listeners: {
                        render: function(button) {
                            button.btnEl.setStyle("background", "#" + startColor);
                        }
                    }
                },
                {xtype: 'displayfield', value: 'To'},
                {
                    xtype: 'button',
                    text: "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
                    menu: {
                        xtype: 'colormenu',
                        value: endColor,
                        listeners: {
                            select: function(menu, color) {
                                endColor = color;
                                menu.ownerCt.ownerCt.btnEl.setStyle("background", "#" + color);
                            }
                        }
                    },
                    listeners: {
                        render: function(button) {
                            button.btnEl.setStyle("background", "#" + endColor);
                        }
                    }

                }
            ]
        }],
        listeners: {
            collapse: function(fieldset) {
                disableChildren(fieldset, true);
                _registerStyle();
            },
            expand: function(fieldset) {
                disableChildren(fieldset, false);
                _registerStyle();
            }
        }
    };

    var overlayIndicatorsCombo = new Ext.form.ComboBox({
        fieldLabel: 'Indicator',
        hiddenName: 'overlay_indicators',
        anchor: '100% -68',
        store: indicatorsStore,
        valueField: 'data_index',
        displayField: 'name',
        triggerAction: 'all',
        editable: false,
        mode: 'local'
    });

    var overlaySize = new Ext.form.CompositeField({
        fieldLabel: 'Size',
        items: [
            {xtype: 'displayfield', value: 'From'},
            {
                xtype: 'spinnerfield',
                name: 'overlay_min_size',
                minValue: 1,
                maxValue: 100,
                incrementValue: 1,
                width: 50,
                value: 5
            },
            {xtype: 'displayfield', value: 'To'},
            {
                xtype: 'spinnerfield',
                name: 'overlay_max_size',
                minValue: 1,
                maxValue: 100,
                incrementValue: 1,
                width: 50,
                value: 20 
            }
        ]
    });

    var relativeAbsoluteRadioGroup = new Ext.form.RadioGroup({
        fieldLabel: 'Values',
        disabled: true,
        items: [
            {boxLabel: 'Absolute', inputValue: 'absolute', name: 'overlayRelativeAbsolute', checked: true},
            {boxLabel: 'Relative', inputValue: 'relative', name: 'overlayRelativeAbsolute'}
        ]
    });

    var onOverlayTypeChange = function(group, checked) {
        var type = group.getValue().inputValue;
        switch (type) {
            case "symbol":
                overlayIndicatorsCombo.show();
                overlayIndicatorsCombo.enable();
                overlaySize.show();
                overlaySize.enable();
                relativeAbsoluteRadioGroup.hide();
                relativeAbsoluteRadioGroup.disable();
                formPanel.doLayout();
                break;
            case "bar":
            case "pie":
                overlayIndicatorsCombo.hide();
                overlayIndicatorsCombo.disable();
                overlaySize.hide();
                overlaySize.disable();
                relativeAbsoluteRadioGroup.show();
                relativeAbsoluteRadioGroup.setDisabled(!App.queryMgr.getQuery().relative);
                formPanel.doLayout();
                break;
        }
    };

    var overlayForm = {
        xtype :'fieldset',
        ref: '../overlayForm',
        layout: 'form',
        title: 'Overlay symbols',
        collapsible: true,
        titleCollapse: true,
        layoutConfig: {
            trackLabels: true
        },
        onCheckClick: function() {
            this.setDisabled(!this.checkbox.dom.checked);
        },
        items: [{
            // Use the default, automatic layout to distribute the controls evenly
            // across a single row
            xtype: 'radiogroup',
            fieldLabel: 'Type',
            items: [
                {boxLabel: 'Prop. symbols', inputValue: 'symbol', name: 'overlay_type', checked: true},
                {boxLabel: 'Bars', inputValue: 'bar', name: 'overlay_type'},
                {boxLabel: 'Pies', inputValue: 'pie', name: 'overlay_type'}
            ],
            listeners: {
                change: onOverlayTypeChange
            }
        },
            overlayIndicatorsCombo,
            overlaySize,
            relativeAbsoluteRadioGroup
        ],
        listeners: {
            collapse: function(fieldset) {
                disableChildren(fieldset, true);
                _registerStyle();
            },
            expand: function(fieldset) {
                disableChildren(fieldset, false);
                _registerStyle();
            }
        }
    };

    /**
     * Method: Disables/Enables all nested components of the given container
     *
     * Parameters:
     * disable {Boolean} tells wether to disable or enable the children
     */
    var disableChildren = function(container, disable) {
        container.items.each(function(item) {
            if (item.isXType('container')) {
                disableChildren(item, disable);
            } else {
                item.setDisabled(disable);
            }
        });
        // the relative/absolute radio buttons do have a specific behavior
        relativeAbsoluteRadioGroup.setDisabled(!App.queryMgr.getQuery().relative);
    };

    var formPanel = new Ext.FormPanel({
        header: false,
        collapsed: false,
        collapsible: true,
        border: false,
        layout: 'hbox',
        height: 150,
        defaults: {
            style: 'padding: 5px;',
            border: false
        },
        layoutConfig: {
            trackLabels: true
        },
        items: [{
            layout: 'form',
            flex: 1,
            items: choroplethsForm
        },{
            layout: 'form',
            flex: 1,
            items: overlayForm
        }],
        listeners: {
            // fixes SpinnerField badly rendered
            afterlayout: function(panel) {
                panel.overlayForm.collapse();
                panel.collapse();
                relativeAbsoluteRadioGroup.hide();
            },
            single: true
        }
    });

    // Auto-commit changes
    formPanel.getForm().items.each(function(field) {
        field.on('spin', _registerStyle);
        field.on('change', _registerStyle);
        field.on('select', _registerStyle);
        if (field instanceof Ext.form.CompositeField) {
            Ext.each(field.items, function(subfield, i){
                var subfield = field.items[i] = Ext.create(subfield);
                subfield.on('spin', _registerStyle);
                subfield.on('change', _registerStyle);
                subfield.on('select', _registerStyle);
                subfield.on('menuhide', _registerStyle);
            });
        }
    });

    var loadIndicators = function() {
        indicatorsStore.loadData(App.metadata);
    };

    App.queryMgr.events.on({
        'metadataloaded': loadIndicators,
        'beforequeryregistered': function() {
            relativeAbsoluteRadioGroup.setDisabled(true);
        },
        'queryregistered': function() {
            relativeAbsoluteRadioGroup.setDisabled(!App.queryMgr.getQuery().relative);
        }
    });

    return {

        /**
         * APIProperty: events
         */
        events: _events,

        panel: formPanel,

        registerStyle: _registerStyle
    };
}();
