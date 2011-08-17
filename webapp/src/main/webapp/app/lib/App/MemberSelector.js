Ext.namespace('App');

App.MemberSelector = Ext.extend(Ext.tree.TreePanel, {
    
    width: 300,

    height: 300,

    members: null,

    autoScroll: true,

    /**
     * Property: level
     * The level
     */
    level: null,

    maskConfig: { msg: "Loading members..." },

    initComponent: function(){
        this.addEvents(

            /**
            * @event done
            * Fires when finish button is clicked
            * @param {Tree} tree The window
            * @param {Array} members The selected members
            */
           'done',

            /**
            * @event done
            * Fires when close button is clicked
            * @param {Tree} tree The window
            */
           'close'
        );

        this.loader = new Ext.tree.TreeLoader({
            preloadChildren: true,
            clearOnLoad: false
        });
        this.root = new Ext.tree.TreeNode({
            text:'Ext JS',
            id:'root'
        });

        this.rootVisible = false;
        this.bbar = [ ' ',
        {
            text: 'Expand all',
            //iconCls: 'icon-expand-all',
            tooltip: 'Expand All',
            handler: function(){ this.root.expand(true); },
            scope: this
        }, '-', {
            text: 'Collapse all',
            //iconCls: 'icon-collapse-all',
            tooltip: 'Collapse All',
            handler: function(){ this.root.collapse(true); },
            scope: this
        }, '->', {
            text: 'Close',
            tooltip: 'Close',
            handler: function() {
                this.fireEvent('close'); 
            },
            scope: this
        }];

        App.MemberSelector.superclass.initComponent.call(this);
        this.members = this.members || [];

        this.on({
            'checkchange': this.getSelectedMembers
        });
        this.on('afterlayout', this.buildTree, this, {single: true});
    },

    getSelectedMembers: function() {
        var selNodes = this.getChecked();
        var members = [];
        Ext.each(selNodes, function(node) {
             members.push(node.id);    
        });
        this.fireEvent('done', members);
    },

    /**
     * Method: buildTree
     * Creates the nodes for the given level
     */
    buildTree: function() {
        var nodeConfig;
        var dimension = App.cubeProperties.findDimensionByUniqueName(
            this.level.get('DIMENSION_UNIQUE_NAME'));
        var members = dimension.members;
            
        members.each(function(item) {
            nodeConfig = {
                text: item.get('MEMBER_NAME'),
                id: item.get('MEMBER_UNIQUE_NAME'),
                loaded: true,
                expanded: item.get('LEVEL_NUMBER') === '0'
            };
            if (item.get('LEVEL_NUMBER') == this.level.get('LEVEL_NUMBER')) {
                Ext.apply(nodeConfig, {
                    leaf: true,
                    checked: this.members.indexOf(item.get('MEMBER_UNIQUE_NAME')) != -1
                });
            }

            if (item.get('LEVEL_NUMBER') === '0') {
                this.root.appendChild(nodeConfig);
            } else {
                var parent = this.getNodeById(item.get('PARENT_UNIQUE_NAME'));
                parent.appendChild(nodeConfig);
            }
        }, this);
    }
});
