Ext.namespace('App');

App.DimensionSelector = function(dimension) {
    var config = {
        defaults: {
            xtype: 'container'
        },
        layout: 'column',
        width: '100%'
    };
    Ext.apply(this, config);

    this.dimension = App.cubeProperties.findDimensionByUniqueName(dimension.dimension);
    this.level = App.cubeProperties.findLevelByUniqueName(dimension.level);
    this.members = dimension.members;
    this.addEvents(

        /**
        * @event change
        * Fires when something changed (members list, or dimension)
        */
       'change',

        /**
        * @event remove
        * Fires when this dimension is to be removed 
        */
       'remove'
    );
    App.DimensionSelector.superclass.constructor.call(this);
};

Ext.extend(App.DimensionSelector, Ext.Container, {

    level: null,

    members: null,

    allMember: null,

    combo: null,

    initComponent: function() {
        App.DimensionSelector.superclass.initComponent.call(this);

        this.membersBtn = new Ext.Button({
            xtype: 'button',
            tooltip: 'Select the members for the chosen dimension',
            menu: {
                items: this.createMembersSelector(),
                listeners: {
                    hide: function(){
                        this.fireEvent('change');
                    },
                    scope: this
                }
            },
            scope: this
        });

        this.add({
            layout: 'column',
            width: '100%',
            items: [{
                xtype: 'container',
                columnWidth: 0.95,
                items: this.membersBtn
            },{
                xtype: 'button',
                iconCls: 'delete',
                tooltip: 'Remove this dimension',
                handler: function() {
                    this.fireEvent('remove', this.dimension.get('DIMENSION_UNIQUE_NAME'));
                },
                scope: this
            }]
        });

        this.add({
            ref: 'membersList',
            width: '100%',
            cls: 'members-list'
        });

        this.on('afterlayout', function() {
            this.setText();
        }, this);
    },

    createMembersSelector: function() {
        var membersSelector = new App.MemberSelector({
            level: this.level,
            members: this.members
        });
        membersSelector.on({
            'done': function(members) {
                this.members = (members.length > 0) ?
                    members : null;
                this.setText();
            },
            'close': function() {
                this.membersBtn.menu.hide();
            },
            scope: this
        });
        return membersSelector; 
    },

    setText: function() {
        var txt = [
            '<b>',
            this.level.get('LEVEL_NAME'),
            '</b>'
        ].join('');
        this.membersBtn.setText(txt);

        var members = [];
        Ext.each(this.members, function(member) {
            members.push(
                App.cubeProperties.findMemberByUniqueName(
                    this.dimension.get('DIMENSION_UNIQUE_NAME'),
                    member
                ).get('MEMBER_NAME')
            );
        }, this);

        txt = this.members ? members.join(', ') : 'All members';
        this.membersList.el.update(txt);
    }
});
