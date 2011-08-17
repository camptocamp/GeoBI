Ext.form.GroupComboBox = function(config) {
    config.store.groupBy(config.groupingField);
    
    Ext.form.GroupComboBox.superclass.constructor.call(this, config);
};

Ext.extend(Ext.form.GroupComboBox, Ext.form.ComboBox, {
    groupField: null,
    
    initList : function(){
        // check if list will be initialized by superclass initList
        var initList = false;
        if (!this.list) {
            initList = true;
        }
        
        Ext.form.GroupComboBox.superclass.initList.call(this);
        
        // if list was just initialialized, view class has to be changed
        if (initList) {
            var cls = 'x-combo-list';
            
            // view is changed to a grouping one @see GroupingView.js
            this.view = new Ext.form.GroupingView({
                applyTo: this.innerList,
                itemTpl: this.tpl,
                singleSelect: true,
                selectedClass: this.selectedClass,
                itemSelector: this.itemSelector || '.' + cls + '-item'
            });
//            this.view.on('click', this.onViewClick, this);

            this.bindStore(this.store, true);
        }
    }
});  