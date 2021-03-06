var util = util || {};

/** Loops through all elements with CSS class .number and formats using numeral.js **/
util.formatNumbers = function(opt_parent) {
  var elements = (opt_parent) ? $(opt_parent).find('.number') : $('.number');
  $.each(elements, function(idx, el) {
    var formatted = numeral($(el).text()).format('0,0');
    $(el).html(formatted);
  });
};

/** Renders an image icon corresponding to a specific license URL **/
util.licenseIcon = function(url) {
  if (url.indexOf('http://opendatacommons.org/licenses/odbl') == 0) {
    return '<a class="license" href="' + url + '" target="_blank" title="Open Data Commons Open Database License (ODbL)"><img src="/api-v3/static/images/open-data-generic.png"></a>';
  } else if (url.indexOf('http://creativecommons.org/publicdomain/zero/1.0') == 0) {
	return '<a class="license" href="' + url + '" target="_blank" title="CC0 Public Domain Dedication"><img src="/api-v3/static/images/cc-zero.png"></a>';
  } else if (url.indexOf('http://creativecommons.org/licenses/by-nc') == 0) {
	return '<a class="license" href="' + url + '" target="_blank" title="CC Attribution Non-Commercial (CC BY-NC)"><img src="/api-v3/static/images/cc-by-nc.png"></a>';
  } else {
    return '<a href="' + url + '">' + url + '</a>';
  }
};

util.formatGazetteerURI = function(uri) {
  if (uri.indexOf('http://pleiades.stoa.org/places/') > -1) {
    return 'pleiades:' + uri.substr(32);
  } else if (uri.indexOf('http://dare.ht.lu.se/places/') > -1) {
    return 'dare:' + uri.substr(28);
  } else if (uri.indexOf('http://gazetteer.dainst.org/place/') > -1) {
	  return 'dai:' + uri.substr(34);
  } else if (uri.indexOf('http://sws.geonames.org/') > -1) {
	  return 'geonames:' + uri.substr(24);
  } else if (uri.indexOf('http://vici.org/vici') > -1) {
	  return 'vici:' + uri.substr(21);
  } else if (uri.indexOf('http://www.trismegistos.org/place/') > -1) {
    return 'trismegistos:' + uri.substr(34);
  } else if (uri.indexOf('http://nomisma.org/') > -1) {
    return 'nomisma:' + uri.substr(22);
  } else if (uri.indexOf('http://data.pastplace.org') > -1) {
    return 'pastplace:' + uri.substr(35);
  } else if (uri.indexOf('http://www.wikidata.org/entity') > -1) {
    return 'wikidata:' + uri.substr(32);
  } else {
    return uri;
  }
};

/** From http://www.samaxes.com/2011/09/change-url-parameters-with-jquery/ **/
util.buildPageRequestURL = function(offset, limit) {
  var queryParameters = {},
      queryString = location.search.substring(1).replace(/\+/g, ' '),
      re = /([^&=]+)=([^&]*)/g,
      m;

   while (m = re.exec(queryString)) {
     queryParameters[decodeURIComponent(m[1])] = decodeURIComponent(m[2]);
   }

   queryParameters['offset'] = offset;
   queryParameters['limit'] = limit;

   return $.param(queryParameters);
};
