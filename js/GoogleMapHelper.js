/*************************************************************************
 * This script is to implement AJAX Framework when using the
 * google maps API for the web browser. This was learned in
 * TheNewBoston's AJAX tutorial, the code of which can be found here:
 * http://stackoverflow.com/questions/25598468/ajax-tutorial-not-working
 * 
 * TODO: Since the current version of this code requires no verification
 * TODO: of the existing client, it is VERY EASY to inject this code with
 * TODO: other channelId's and member Ids, thus probing the system for 
 * TODO: data that should otherwise be restricted. Fix this before
 * TODO: the code is made public!
 *************************************************************************/
var open = false;
// TODO: make sure this is the correct socket
var socket = new WebSocket("ws://192.169.148.214:2000");

// map variables
var bounds;
var map, broadcastingUsers;
var userMarkers = {};
var purpleDot = '../Images/purple-dot.png'; // default marker for user's location

/********************
 * Socket functions
 ********************/
socket.onopen = function() {
    open = true;
    socket.send("connect-browser " + userId +" "+ channelId);

    console.log("Connected");
};

/*
 * @param {string} evt - where PHP returns a string of users & locations
 */
socket.onmessage = function(evt) {
    getLocationsFromRequest(evt.data);
    if (mapScope) {
        mapScope.findScopeDimensions();
    } else {
        mapScope = new MapScope();
    }
    if (mapScope["reframeMap"]) {
        initMap();
        mapScope["reframeMap"] = false;
    }
    reloadMarkers();
};

socket.onclose = function() {
    open = false;
    console.log("Disconnected from channel socket");

    if (socket.readyState == 3) {
        broadcastingUsers = 0;
        initMap();
        document.getElementById('cant_connect').style.visibility = "visible";
    }
};


/**********************************************
 * Functions to create and update user markers
 **********************************************/
// receives response from PHP/MySQL
function getLocationsFromRequest(data) {
    /* The response ought to return an array of the current user's location
     * with this text structure:
     *      [userId] [current lat] [current long]
     * Then, it will store the response to usersLocation.
     * Inputs with '0' after the id means that they stopped broadcasting
     */
    usersLocations = []; // resets after every request
    var segments = data.split(" ");
    if (segments.length != 2 && segments[1] != "0") {
        if (segments[0] && segments[1] && segments[2]) {
            usersLocations[segments[0]] =
                new UserLocation(segments[0], segments[1], segments[2], true); // deal with is_broadcasting later
        }
    } else if (usersLocations[segments[0]] != null) {
        // if there already is a userId logged in usersLocations, delete it
        usersLocations[segments[0]] = null;
    }
}

function reloadMarkers() {
    var id;
    for (var i = 0, j = 1; i < usersLocations.length; i++, j++) {
        id = usersLocations[i].id;
        if (usersLocations[i].isBroadcasting) {
            if (!(id in userMarkers) || !userMarkers[id].getMap()) {
                addMarker(i, id);
            } else if (locationChanged(i, id)) {
                deleteMarker(id);
                addMarker(i, id);
            }
        } else if (id in userMarkers) {
            deleteMarker(id);
        }
    }
}

function addMarker(i, id) {
    var position = new google.maps.LatLng(
        usersLocations[i].lat, usersLocations[i].long);

    userMarkers[id] = new google.maps.Marker({
        position: position,
        map: map,
        icon: purpleDot
    });
}

function deleteMarker(id) {
    userMarkers[id].setMap(null);
}

// returns true if the user's location has been updated since the last broadcast
function locationChanged(i, id) {
    var position = new google.maps.LatLng(
        usersLocations[i].lat, usersLocations[i].long);
    return (position.lat() != userMarkers[id].getPosition().lat()
            || position.lng() != userMarkers[id].getPosition().lng());
}

/**************************
 * Create the map
 **************************/
function initMap() {
    bounds = getBounds();
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 30, lng: 0},
        zoom: 2
    });

    if (broadcastingUsers > 0) {
        if (broadcastingUsers == 1) {
            map.setCenter(bounds.getCenter());
            map.setZoom(16);
        } else {
            map.fitBounds(bounds);
        }
    }
}

/*
 *  @returns {Number|LatLngBounds} 1 if only one user is broadcasting,
 *                                 0 if no users broadcasting,
 *                                 LatLngBounds if multiple users are broadcasting
 */
function getBounds() {
    broadcastingUsers = 0; // count the broadcasting users
    if (usersLocations) {
        var bounds = new google.maps.LatLngBounds();
        for (var i = 0; i < usersLocations.length; i++) {
            if (usersLocations[i].isBroadcasting) {
                var position = new google.maps.LatLng(
                    usersLocations[i].lat, usersLocations[i].long);
                bounds.extend(position);
                broadcastingUsers++;
            }
        }
        return bounds;
    }
}

/**********************************
 * Classes
 **********************************/
function UserLocation(id, lat, long, isBroadcasting) {
    this.id = parseInt(id);
    this.lat = parseFloat(lat);
    this.long = parseFloat(long);
    this.isBroadcasting = parseInt(isBroadcasting);
}

// the default scope of the map
function MapScope() {
    // default scope (in the event of one user)
    this.latLength = 5;
    this.longLength = 5;

    this.reframeMap = true; // set to true when the center of lat/long changes
    if (usersLocations) {
        this.center = this.findScopeDimensions();
    }
}

/*
 * findScopeDimensions does two things:
 *  1. check that the dimension length is the same (changes lengths if they're not)
 *  2. check that the center is the same (returns center if it's not)
 */
MapScope.prototype.findScopeDimensions = function() {
    var minLat = 90, maxLat = -90;
    var minLong = 180, maxLong = -180;
    // get the rectangular boundaries of all user locations
    for (var i = 0; i < usersLocations.length; i++) {
        if (usersLocations[i].lat < minLat) {
            minLat = usersLocations[i].lat;
        }
        if (usersLocations[i].lat > maxLat) {
            maxLat = usersLocations[i].lat;
        }
        if (usersLocations[i].long < minLong) {
            minLong = usersLocations[i].long;
        }
        if (usersLocations[i].long > maxLong) {
            maxLong = usersLocations[i].long;
        }
    }
    // get dimensions of the rectangle
    var latLength = maxLat - minLat;
    var longLength = maxLong - minLong;
    if (this.latLength != latLength || this.longLength != longLength) {
        this.latLength = latLength;
        this.longLength = longLength;
    }

    // center of the scope (not adjusted for the whole map)
    var center = [parseFloat(this.latLength/2) + parseFloat(minLat),
                    parseFloat(this.longLength/2) + parseFloat(minLong)];
    if ( !this.center
        || (this.center[0] != center[0] || this.center[1] != center[1])) {
        return center;
    }
};
