// A groupingStore is required for a GroupingView
Ext.form.GroupingView = Ext.extend(Ext.DataView, {
    
    /**
     * @cfg {String} groupTextTpl The template used to render the group text
     */
    groupTextTpl : '{text}',
    groupTpl: null,
    itemTpl: null,
    
    // private
    initTemplates : function(){
        if(!this.groupTpl){
            this.groupTpl = new Ext.XTemplate('<div class="x-combo-list-group">' +this.groupTextTpl+ '</div>');
        }
        
        if(typeof this.itemTpl == "string"){
            this.itemTpl = new Ext.XTemplate(this.itemTpl);
        }
    },
    
    // private
    initComponent : function(){
        this.initTemplates();
        Ext.form.GroupingView.superclass.initComponent.apply(this, arguments);
    },

    /**
     * Refreshes the view by reloading the data from the store and re-rendering the template.
     */
    refresh : function(){
        this.clearSelections(false, true);
        this.el.update("");
        
        var records = this.store.getRange();
        if(records.length < 1){
            this.el.update(this.emptyText);
            this.all.clear();
            return;
        }
        var groupField = this.store.getSortState().field;
        var curGroup;
        var buf = [];
        for (var i =0; i < records.length; i++) {
            var r = records[i];
            var gvalue = r.data[groupField];
            // add list items for group names
            if (!curGroup || !curGroup.text ||
                curGroup.text != r.data[groupField]) {
                curGroup = {
                    text: r.data[groupField]
                };
                buf[buf.length] = this.groupTpl.apply(curGroup);
            }
            buf[buf.length] = this.itemTpl.apply(r.data);
        }
        this.el.update(buf.join(''));
        this.all.fill(Ext.query(this.itemSelector, this.el.dom));
        this.updateIndexes(0);
    }
});