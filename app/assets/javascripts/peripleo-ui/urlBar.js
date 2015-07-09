/** In charge of updating the URL bar hash segment with current map & search settings **/
define(['peripleo-ui/events/events'], function(Events) {
  
  // Make sure we don't update the URL bar too frequently as it
  // introduces noticable delays
  var SLEEP_DURATION = 1000;
  
  var URLBar = function(eventBroker) {
    
    var segments = {},
          
        busy = false,
          
        updatePending = false,
        
        /** Helper function to parse a bbox string **/
        parseBBox = function(bboxStr) {
          var values = bboxStr.split(',');
          return { north: parseFloat(values[3]), east: parseFloat(values[1]), 
                   south: parseFloat(values[2]), west: parseFloat(values[0]) };
        },
        
        parseURLHash = function(hash) {
          var keysVals = (hash.indexOf('#') === 0) ? hash.substring(1).split('&') : false,
              bbox;
              
          if (keysVals) {
            jQuery.each(keysVals, function(idx, keyVal) {
              var asArray = keyVal.split('='),
                  key = asArray[0],
                  value = asArray[1];
                       
              if (key === 'bbox') // Parse bbox
                bbox = parseBBox(value);
              
              segments[key] = value;
            });
            
            // Number parsing for timespan
            if (segments.from)
              segments.from = parseInt(segments.from);

            if (segments.to)
              segments.to = parseInt(segments.to);
              
            var settings = jQuery.extend({}, segments);
            if (bbox)
              settings.bbox = bbox;
            return settings;
          }
        },
        
        /** Updates a particular segment field with the value from the diff, if any **/
        setParam = function(name, diff) {
          if (diff.hasOwnProperty(name)) {
            
            // TODO this gets called three times on startup - investigate
            
            if (diff[name])
              segments[name] = diff[name];
            else
              delete segments[name];
          }          
        },
        
        /** Updates the URL field - NOW! **/
        updateNow = function() {
          var segment = jQuery.map(segments, function(val, key) {
            return key + '=' + val;
          });

          window.location.hash = segment.join('&');
        },
                
        updateURLField = function() {
          var scheduleUpdate = function() {
                busy = true;
                setTimeout(function() {
                  updateNow();
                  busy = false;
                  if (updatePending) {
                    updatePending = false;
                    scheduleUpdate();
                  }
                }, SLEEP_DURATION);
              };
          
          if (busy)
            updatePending = true;
          else
            scheduleUpdate();
        };
    
    eventBroker.addHandler(Events.VIEW_CHANGED, function(bounds) {
      var lat = (bounds.south + bounds.north) / 2,
          lon = (bounds.east + bounds.west) / 2; 
      
      segments.at = lat.toFixed(8) + ',' + lon.toFixed(8) + ',' + bounds.zoom;
      updateURLField()
    });
    
    eventBroker.addHandler(Events.SEARCH_CHANGED, function(diff) {
      setParam('query', diff);
      setParam('from', diff);
      setParam('to', diff);
      updateURLField();
    });
    
    eventBroker.addHandler(Events.CHANGE_LAYER, function(layer) {
      if (layer === 'awmc')
        delete segments.layer;
      else
        segments.layer = layer;
      updateNow();
    });
    
    eventBroker.addHandler(Events.SELECTION, function(selectedItems) {
      // TODO multi-select?
      var selection = (selectedItems) ? selectedItems[0] : false;
      if (selection)
        segments.places = encodeURIComponent(selection.identifier);
      else
        delete segments.places;
      updateNow();
    });
    
    eventBroker.addHandler(Events.SHOW_FILTERS, function() {
      segments.f = 'open';
      updateNow();
    });

    eventBroker.addHandler(Events.HIDE_FILTERS, function() {
      delete segments.f;
      updateNow();
    });
    
    eventBroker.addHandler(Events.START_EXPLORATION, function() {
      segments.ex = 'true';
      updateNow();
    });
    
    eventBroker.addHandler(Events.STOP_EXPLORATION, function() {
      delete segments.ex;
      updateNow();
    });
    
    this.parseURLHash = parseURLHash;
  };
  
  return URLBar;
    
});
