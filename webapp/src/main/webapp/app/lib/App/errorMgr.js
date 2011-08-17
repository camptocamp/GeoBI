Ext.namespace('App');
App.errorMgr = function() {
    return {
        show: function(response, msg) {
            App.messageMgr.msg('Error', 'Something went wrong when ' + 
                msg
            );
        }
    }
}();
