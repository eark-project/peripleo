define(function() {
  
  return {
    
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    /* General lifecycle events       */
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    
    /**
     * Initial page LOAD event. Fired after the page has loaded.
     * 
     * @param initial map bounds 
     */
    LOAD : 'load',
    
    
    
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    /* API-related events             */
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    
    /** The API returned an initial search result **/
    API_INITIAL_RESPONSE : 'initialLoad',
    
    /**
     * The API returned a search result
     * 
     * @param search result
     */
    API_SEARCH_RESPONSE : 'searchResponse',  
    
    /**
     * The API returned a result for a sub-search
     * 
     * @param search result
     */
    API_SUB_SEARCH_RESPONE : 'subSearchResponse',
    
    /**
     * The API returned a data to update the map view
     *
     * @param search result
     */
    API_VIEW_UPDATE : 'viewUpdate',    
    
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    /* UI events                      */
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    
    /**
     * The user changed the map viewport.
     * 
     * @param new map bounds
     */
    VIEW_CHANGED : 'viewChanged',
    
    /**
     * The user changed any of the search parameters, e.g. by typing & hitting ENTER in the search box
     * or by changing the filter settings
     * 
     * @param change
     */
    SEARCH_CHANGED : 'searchChanged',
    
    /**
     * The users issued a sub-search (i.e. a search with narrower paramters than the 
     * current 'main' search).
     * 
     * @param diff to the current main search
     */
    SUB_SEARCH : 'subSearch',
    
    /**
     * Requests a one-time search from the API. The result will not trigger the global
     * event pool; the search request will always be fired immediately (i.e. not affected
     * by caching or delay policies); and the response will be passed back to a callback
     * function to be provided as parameter.
     * 
     * @param any search parameter that should be different than the current search state
     * plus a callback function
     */   
    ONE_TIME_SEARCH : 'oneTimeSearch',
    
    /** 
     * The user changed the query phrase by typing and hitting ENTER in the search box
     * 
     * @param the new query phrase
     */
    QUERY_PHRASE_CHANGED : 'queryPhraseChanged',
    
    /** 
     * Event for showing the result list box. (Can either happen as a
     * user action or as a result of a search response.)
     */
    SHOW_ALL_RESULTS : 'showAllResults',

    /** 
     * Event for hiding the result list box.
     */
    HIDE_ALL_RESULTS : 'hideAllResults',
    
    /** 
     * Event for toggling the visibility of the result list box. (Happens
     * as a user action.)
     */
    TOGGLE_ALL_RESULTS : 'toggleAllResults',
    
    /**
     * The user hovers over a result in the list
     * 
     * @param the search result
     */
    MOUSE_OVER_RESULT : 'mouseOverResult',
    
    /**
     * The user selected a marker on the map
     * 
     * @param place or array of places
     */
    SELECT_MARKER : 'selectMarker',
    
    /**
     * The user selected a result in the list
     * 
     * @param the result
     */
    SELECT_RESULT : 'selectResult',
    
    /**
     *  Generic 'selection' event triggered when the users selected a marker or result
     */
    SELECTION : 'selection'
    
  };
    
});
