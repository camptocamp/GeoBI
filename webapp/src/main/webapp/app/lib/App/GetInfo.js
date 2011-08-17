/**
 * Class: App.GetInfo
 * Gets vector features for locations underneath the mouse cursor. Uses an
 *     <OpenLayers.Protocol> that supports spatial filters to retrieve
 *     features from a server and fires events that notify applications of the
 *     selected features. 
 *     It's an adaptation of the GetFeature control but only for clicks. It 
 *     also sends the click event to the 'featureselected' listeners if they
 *     need the event position.
 *
 * Inherits from:
 *  - <OpenLayers.Control>
 */
App.GetInfo = OpenLayers.Class(OpenLayers.Control, {
    
    /**
     * APIProperty: protocol
     * {<OpenLayers.Protocol>} Required. The protocol used for fetching
     *     features.
     */
    protocol: null,
    
    /**
     * Property: filterType
     * {<String>} The type of filter to use when sending off a request. 
     *     Possible values: 
     *     OpenLayers.Filter.Spatial.<BBOX|INTERSECTS|WITHIN|CONTAINS>
     *     Defaults to: OpenLayers.Filter.Spatial.BBOX
     */
    filterType: OpenLayers.Filter.Spatial.BBOX,

    /**
     * APIProperty: clickTolerance
     * {Integer} Tolerance for the filter query in pixels. This has the
     *     same effect as the tolerance parameter on WMS GetFeatureInfo
     *     requests.  Will be ignored for box selections.  Applies only if
     *     <click> or <hover> is true.  Default is 5.  Note that this not
     *     only affects requests on click, but also on hover.
     */
    clickTolerance: 5,

    /**
     * Constant: EVENT_TYPES
     *
     * Supported event types:
     * beforerequest - Triggered when a request is about to be sent, if
     *      listener returns false, the request is not sent.
     * beforefeatureselected - Triggered when <click> is true before a
     *      feature is selected. The event object has a feature property with
     *      the feature about to select
     */
    EVENT_TYPES: ["beforerequest", "featureselected"],

    /**
     * Constructor: App.GetInfo
     * Create a new control for fetching remote features.
     *
     * Parameters:
     * options - {Object} A configuration object which at least has to contain
     *     a <protocol> property
     */
    initialize: function(options) {
        // concatenate events specific to vector with those from the base
        this.EVENT_TYPES =
            App.GetInfo.prototype.EVENT_TYPES.concat(
            OpenLayers.Control.prototype.EVENT_TYPES
        );

        OpenLayers.Control.prototype.initialize.apply(this, [options]);
        
        this.feature = {};
        
        this.handler = new OpenLayers.Handler.Hover(
            this,
            {'move': this.cancelHover, 'pause': this.selectHover},
            {'delay': 250}
        );
    },
    
    /**
     * Method: activate
     * Activates the control.
     * 
     * Returns:
     * {Boolean} The control was effectively activated.
     */
    activate: function () {
        if (!this.active) {
            this.handler.activate();
        }
        return OpenLayers.Control.prototype.activate.apply(
            this, arguments
        );
    },

    /**
     * Method: deactivate
     * Deactivates the control.
     * 
     * Returns:
     * {Boolean} The control was effectively deactivated.
     */
    deactivate: function () {
        if (this.active) {
            this.handler.deactivate();
        }
        return OpenLayers.Control.prototype.deactivate.apply(
            this, arguments
        );
    },
    
    /**
     * Method selectHover
     * Callback from the handlers.hover set up when <hover> selection is on
     *
     * Parameters:
     * evt {Object} - event object with an xy property
     */
    selectHover: function(evt) {
        var bounds = this.pixelToBounds(evt.xy);
        // here we send the evt in the request so that we can position a popup
        this.request(bounds, evt);
    },

    /**
     * Method: cancelHover
     * Callback from the handlers.hover set up when <hover> selection is on
     */
    cancelHover: function() {
        if (this.hoverResponse) {
            this.protocol.abort(this.hoverResponse);
            this.hoverResponse = null;

            OpenLayers.Element.removeClass(this.map.viewPortDiv, "olCursorWait");
        }
    },

    /**
     * Method: request
     * Sends a GetFeature request to the WFS
     * 
     * Parameters:
     * bounds - {<OpenLayers.Bounds>} bounds for the request's BBOX filter
     * evt - {<OpenLayers.Event>} the click event
     */
    request: function(bounds, evt) {
        if (this.events.triggerEvent("beforerequest") === false) {
            return;
        }
        var filter = new OpenLayers.Filter.Spatial({
            type: this.filterType, 
            value: bounds
        });
        
        // Set the cursor to "wait" to tell the user we're working.
        OpenLayers.Element.addClass(this.map.viewPortDiv, "olCursorWait");

        var response = this.protocol.read({
            filter: filter,
            callback: function(result) {
                if(result.success() && result.features[0]) {
                    this.events.triggerEvent("featureselected",
                    {feature: result.features[0], evt: evt});
                }
                // Reset the cursor.
                OpenLayers.Element.removeClass(this.map.viewPortDiv, "olCursorWait");
            },
            scope: this
        });
        this.hoverResponse = response;
    },
    
    /** 
     * Method: setMap
     * Set the map property for the control. 
     * 
     * Parameters:
     * map - {<OpenLayers.Map>} 
     */
    setMap: function(map) {
        for(var i in this.handlers) {
            this.handlers[i].setMap(map);
        }
        OpenLayers.Control.prototype.setMap.apply(this, arguments);
    },
    
    /**
     * Method: pixelToBounds
     * Takes a pixel as argument and creates bounds after adding the
     * <clickTolerance>.
     * 
     * Parameters:
     * pixel - {<OpenLayers.Pixel>}
     */
    pixelToBounds: function(pixel) {
        var llPx = pixel.add(-this.clickTolerance/2, this.clickTolerance/2);
        var urPx = pixel.add(this.clickTolerance/2, -this.clickTolerance/2);
        var ll = this.map.getLonLatFromPixel(llPx);
        var ur = this.map.getLonLatFromPixel(urPx);
        return new OpenLayers.Bounds(ll.lon, ll.lat, ur.lon, ur.lat);
    },

    CLASS_NAME: "App.GetInfo"
});
