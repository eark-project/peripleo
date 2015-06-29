define(['peripleo-ui/events/events'], function(Events) {
  
  var SettingsEditor = function(eventBroker) {
    
    var element = jQuery(
          '<div class="clicktrap">' +
          '  <div class="modal-editor" id="settings-editor">' +
          '   <span class="close icon">&#xf057;</span>' +
          '   <ul id="baselayers">' + 
          
          '     <li class="baselayer" data-name="awmc">' +
          '       <div class="map-thumb-container"><img class="map-thumb" src="http://a.tiles.mapbox.com/v3/isawnyu.map-knmctlkh/7/68/47.png"></div>' +
          '       <h2>Empty Basemap</h2>' +
          '       <p>Geographically accurate basemap of the ancient world by the <a href="http://awmc.unc.edu/wordpress/tiles/" target="_blank">Ancient World Mapping Centre</a>,' +
          '         University of North Caronlina at Chapel Hill.</p>' +
          '     </li>' +

          '     <li class="baselayer" data-name="dare">' +
          '       <div class="map-thumb-container"><img class="map-thumb" src="http://pelagios.org/tilesets/imperium/7/68/47.png"></div>' +
          '       <h2>Digital Atlas of the Roman Empire Basemap</h2>' +
          '       <p>Roman Empire base map by the <a href="http://dare.ht.lu.se/" target="_blank">Digital Atlas of the Roman Empire</a>, Lund University, Sweden.</p>' +
          '     </li>' +
          
          '     <li class="baselayer" data-name="osm">' +
          '       <div class="map-thumb-container"><img class="map-thumb" src="http://a.tile.openstreetmap.org/7/68/47.png"></div>' +
          '       <h2>OpenStreetMap</h2>' +
          '       <p>Modern places and roads via <a href="http://www.openstreetmap.org" target="_blank">OpenStreetMap</a>.</p>' +
          '     </li>' +
          
          '     <li class="baselayer" data-name="satellite">' +
          '       <div class="map-thumb-container"><img class="map-thumb" src="http://api.tiles.mapbox.com/v4/mapbox.satellite/7/68/47.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6IlhHVkZmaW8ifQ.hAMX5hSW-QnTeRCMAy9A8Q"></div>' +
          '       <h2>Satellite</h2>' +
          '       <p>Aerial imagery via <a href="https://www.mapbox.com/" target="_blank">Mapbox</a>.</p>' +
          '     </li>' +
          
          '   </ul>' +
          '  </div>' +
          '</div>'
        ),
        
        btnClose = element.find('.close'),
        
        show = function() {
          element.show();  
        },
        
        close = function() {
          element.hide();
        };
        
    element.hide();
    jQuery(document.body).append(element);
            
    btnClose.click(close); 
    
    element.on('click', 'li', function(e) {
      var target = jQuery(e.target),
          li = target.closest('li'),
          layerName = li.data('name');
          
      eventBroker.fireEvent(Events.CHANGE_LAYER, layerName);
      close();
    });
    
    eventBroker.addHandler(Events.EDIT_MAP_SETTINGS, show);

  };
  
  return SettingsEditor;
  
});
