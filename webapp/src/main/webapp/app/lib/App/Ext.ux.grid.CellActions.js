// vim: ts=4:sw=4:nu:fdc=4:nospell
/*global Ext */
/**
 * @class Ext.ux.grid.CellActions
 * @extends Ext.util.Observable
 *
 * CellActions plugin for Ext grid
 *
 * CellActions plugin causes that column model recognizes the config property cellAcions
 * that is the array of configuration objects for that column. The documentationi follows.
 *
 * THE FOLLOWING CONFIG OPTIONS ARE FOR COLUMN MODEL COLUMN, NOT FOR CellActions ITSELF.
 *
 * @cfg {Array} cellActions Mandatory. Array of action configuration objects. The following
 * configuration options of action are recognized:
 *
 * - @cfg {Function} callback Optional. Function to call if the action icon is clicked.
 *   This function is called with same signature as action event and in its original scope.
 *   If you need to call it in different scope or with another signature use 
 *   createCallback or createDelegate functions. Works for statically defined actions. Use
 *   callbacks configuration options for store bound actions.
 *
 * - @cfg {Function} cb Shortcut for callback.
 *
 * - @cfg {String} iconIndex Optional, however either iconIndex or iconCls must be
 *   configured. Field name of the field of the grid store record that contains
 *   css class of the icon to show. If configured, shown icons can vary depending
 *   of the value of this field.
 *
 * - @cfg {String} iconCls. css class of the icon to show. It is ignored if iconIndex is
 *   configured. Use this if you want static icons that are not base on the values in the record.
 *
 * - @cfg {String} qtipIndex Optional. Field name of the field of the grid store record that 
 *   contains tooltip text. If configured, the tooltip texts are taken from the store.
 *
 * - @cfg {String} tooltip Optional. Tooltip text to use as icon tooltip. It is ignored if 
 *   qtipIndex is configured. Use this if you want static tooltips that are not taken from the store.
 *
 * - @cfg {String} qtip Synonym for tooltip
 *
 * - @cfg {String} style Optional. Style to apply to action icon container.
 *
 * The following css is required:
 *
 * .ux-cell-value {
 * 	position:relative;
 * 	zoom:1;
 * }
 * .ux-cell-actions {
 * 	position:absolute;
 * 	right:0;
 * 	top:-2px;
 * }
 * .ux-cell-actions-left {
 * 	left:0;
 * 	top:-2px;
 * }
 * .ux-cell-action {
 * 	width:16px;
 * 	height:16px;
 * 	float:left;
 * 	cursor:pointer;
 * 	margin: 0 0 0 4px;
 * }
 * .ux-cell-actions-left .ux-cell-action {
 * 	margin: 0 4px 0 0;
 * }
 * @author    Ing. Jozef Sakáloš
 * @date      22. March 2008
 * @version   1.0
 * @revision  $Id: Ext.ux.grid.CellActions.js 589 2009-02-21 23:30:18Z jozo $
 *
 * @license Ext.ux.grid.CellActions is licensed under the terms of
 * the Open Source LGPL 3.0 license.  Commercial use is permitted to the extent
 * that the code/component(s) do NOT become part of another Open Source or Commercially
 * licensed development library or toolkit without explicit permission.
 * 
 * <p>License details: <a href="http://www.gnu.org/licenses/lgpl.html"
 * target="_blank">http://www.gnu.org/licenses/lgpl.html</a></p>
 *
 * @forum     30411
 * @demo      http://cellactions.extjs.eu
 * @download  
 * <ul>
 * <li><a href="http://cellactions.extjs.eu/cellactions.tar.bz2">cellactions.tar.bz2</a></li>
 * <li><a href="http://cellactions.extjs.eu/cellactions.tar.gz">cellactions.tar.gz</a></li>
 * <li><a href="http://cellactions.extjs.eu/cellactions.zip">cellactions.zip</a></li>
 * </ul>
 *
 * @donate
 * <form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_blank">
 * <input type="hidden" name="cmd" value="_s-xclick">
 * <input type="hidden" name="hosted_button_id" value="3430419">
 * <input type="image" src="https://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" 
 * border="0" name="submit" alt="PayPal - The safer, easier way to pay online.">
 * <img alt="" border="0" src="https://www.paypal.com/en_US/i/scr/pixel.gif" width="1" height="1">
 * </form>
 */

Ext.ns('Ext.ux.grid');

// constructor and cellActions documentation
// {{{
/**
 * Creates new CellActions
 * @constructor
 * @param {Object} config A config object
 */
Ext.ux.grid.CellActions = function(config) {
	Ext.apply(this, config);

	this.addEvents(
		/**
		 * @event action
		 * Fires when user clicks a cell action
		 * @param {Ext.grid.GridPanel} grid
		 * @param {Ext.data.Record} record Record containing data of clicked cell
		 * @param {String} action Action clicked (equals iconCls);
		 * @param {Mixed} value Value of the clicke cell
		 * @param {String} dataIndex as specified in column model
		 * @param {Number} rowIndex Index of row clicked
		 * @param {Number} colIndex Incex of col clicked
		 */
		'action'
		/**
		 * @event beforeaction
		 * Fires when user clicks a cell action but before action event is fired. Return false to cancel the action;
		 * @param {Ext.grid.GridPanel} grid
		 * @param {Ext.data.Record} record Record containing data of clicked cell
		 * @param {String} action Action clicked (equals iconCls);
		 * @param {Mixed} value Value of the clicke cell
		 * @param {String} dataIndex as specified in column model
		 * @param {Number} rowIndex Index of row clicked
		 * @param {Number} colIndex Incex of col clicked
		 */
		,'beforeaction'
	);
	// call parent
	Ext.ux.grid.CellActions.superclass.constructor.call(this);

}; // eo constructor
// }}}

Ext.extend(Ext.ux.grid.CellActions, Ext.util.Observable, {

	/**
	 * @cfg {String} actionEvent Event to trigger actions, e.g. click, dblclick, mouseover (defaults to 'click')
	 */
	 actionEvent:'click'

	/**
	 * @cfg {Number} actionWidth Width of action icon in pixels. Has effect only if align:'left'
	 */
	,actionWidth:20

	/**
	 * @cfg {String} align Set to 'left' to put action icons before the cell text. (defaults to undefined, meaning right)
	 */

	/**
	 * @private
	 * @cfg {String} tpl Template for cell with actions
	 */
	,tpl:'<div class="ux-cell-value" style="padding-left:{padding}px">'
			+'<tpl if="\'left\'!==align">{value}</tpl>'
		 	+'<div class="ux-cell-actions<tpl if="\'left\'===align"> ux-cell-actions-left</tpl>" style="width:{width}px">'
				+'<tpl for="actions"><div class="ux-cell-action {cls}" qtip="{qtip}" style="{style}">&#160;</div></tpl>'
			+'</div>'
			+'<tpl if="\'left\'===align">{value}</tpl>'
		+'<div>'
		
	/**
	 * Called at the end of processActions. Override this if you need it.
	 * @param {Object} c Column model configuration object
	 * @param {Object} data See this.processActions method for details
	 */
	,userProcessing:Ext.emptyFn

	// {{{
	/**
	 * Init function
	 * @param {Ext.grid.GridPanel} grid Grid this plugin is in
	 */
	,init:function(grid) {
		this.grid = grid;
//		grid.on({scope:this, render:this.onRenderGrid});
		grid.afterRender = grid.afterRender.createSequence(this.onRenderGrid, this);

		var cm = this.grid.getColumnModel();
		Ext.each(cm.config, function(c, idx) {
			if('object' === typeof c.cellActions) {
				c.origRenderer = cm.getRenderer(idx);
				c.renderer = this.renderActions.createDelegate(this);
			}
		}, this);


	} // eo function init
	// }}}
	// {{{
	/**
	 * grid render event handler, install actionEvent handler on view.mainBody
	 * @private
	 */
	,onRenderGrid:function() {

		// install click event handler on view mainBody
		this.view = this.grid.getView();
		var cfg = {scope:this};
		cfg[this.actionEvent] = this.onClick;
		this.view.mainBody.on(cfg);

	} // eo function onRender
	// }}}
	// {{{
	/**
	 * Returns data to apply to template. Override this if needed
	 * @param {Mixed} value 
	 * @param {Object} cell object to set some attributes of the grid cell
	 * @param {Ext.data.Record} record from which the data is extracted
	 * @param {Number} row row index
	 * @param {Number} col col index
	 * @param {Ext.data.Store} store object from which the record is extracted
	 * @returns {Object} data to apply to template
	 */
	,getData:function(value, cell, record, row, col, store) {
		return record.data || {};
	}
	// }}}
	// {{{
	/**
	 * replaces (but calls) the original renderer from column model
	 * @private
	 * @param {Mixed} value 
	 * @param {Object} cell object to set some attributes of the grid cell
	 * @param {Ext.data.Record} record from which the data is extracted
	 * @param {Number} row row index
	 * @param {Number} col col index
	 * @param {Ext.data.Store} store object from which the record is extracted
	 * @returns {String} markup of cell content
	 */
	,renderActions:function(value, cell, record, row, col, store) {

		// get column config from column model
		var c = this.grid.getColumnModel().config[col];

		// get output of the original renderer
		var val = c.origRenderer(value, cell, record, row, col, store);

		// get actions template if we need but don't have one
		if(c.cellActions && !c.actionsTpl) {
			c.actionsTpl = this.processActions(c);
			c.actionsTpl.compile();
		}
		// return original renderer output if we don't have actions
		else if(!c.cellActions) {
			return val;
		}

		// get and return final markup
		var data = this.getData.apply(this, arguments);
		data.value = val;
		return c.actionsTpl.apply(data);

	} // eo function renderActions
	// }}}
	// {{{
	/**
	 * processes the actions configs from column model column, saves callbacks and creates template
	 * @param {Object} c column model config of one column
	 * @private
	 */
	,processActions:function(c) {

		// callbacks holder
		this.callbacks = this.callbacks || {};

		// data for intermediate template
		var data = {
			 align:this.align || 'right'
			,width:this.actionWidth * c.cellActions.length
			,padding:'left' === this.align ? this.actionWidth * c.cellActions.length : 0
			,value:'{value}'
			,actions:[]
		};

		// cellActions loop
		Ext.each(c.cellActions, function(a, i) {

			// save callback
			if(a.iconCls && 'function' === typeof (a.callback || a.cb)) {
				this.callbacks[a.iconCls] = a.callback || a.cb;
			}

			// data for intermediate xtemplate action
			var o = {
				 cls:a.iconIndex ? '{' + a.iconIndex + '}' : (a.iconCls ? a.iconCls : '')
				,qtip:a.qtipIndex ? '{' + a.qtipIndex + '}' : (a.tooltip || a.qtip ? a.tooltip || a.qtip : '')
				,style:a.style ? a.style : ''
			};
			data.actions.push(o);

		}, this); // eo cellActions loop

		this.userProcessing(c, data);

		// get and return final template
		var xt = new Ext.XTemplate(this.tpl);
		return new Ext.Template(xt.apply(data));

	} // eo function processActions
	// }}}
	// {{{
	/**
	 * Grid body actionEvent event handler
	 * @private
	 */
	,onClick:function(e, target) {

		// collect all variables for callback and/or events
		var t = e.getTarget('div.ux-cell-action');
		var row = e.getTarget('.x-grid3-row');
		var col = this.view.findCellIndex(target.parentNode.parentNode);
		var c = this.grid.getColumnModel().config[col];
		var record, dataIndex, value, action;
		if(t) {
			record = this.grid.store.getAt(row.rowIndex);
			dataIndex = c.dataIndex;
			value = record.get(dataIndex);
			action = t.className.replace(/ux-cell-action /, '');
		}

		// check if we've collected all necessary variables
		if(false !== row && false !== col && record && dataIndex && action) {

			// call callback if any
			if(this.callbacks && 'function' === typeof this.callbacks[action]) {
				this.callbacks[action](this.grid, record, action, value, dataIndex, row.rowIndex, col);
			}

			// fire events
			if(true !== this.eventsSuspended && false === this.fireEvent('beforeaction', this.grid, record, action, value, dataIndex, row.rowIndex, col)) {
				return;
			}
			else if(true !== this.eventsSuspended) {
				this.fireEvent('action', this.grid, record, action, value, dataIndex, row.rowIndex, col);
			}

		}
	} // eo function onClick
	// }}}

});

// register xtype
Ext.reg('cellactions', Ext.ux.grid.CellActions);

// eof
