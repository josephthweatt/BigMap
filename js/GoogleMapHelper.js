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
var getLocationURL = "../PHP/GetChannelUsersLocation.php";
var textHttp = createXMLHttpRequestObject();

function createXMLHttpRequestObject() {
    var textHttp;

    if (window.XMLHttpRequest) {
        // this is executed when a user isn't trying to ruin the app (they're not using IE)
        try {
            textHttp = new XMLHttpRequest();
        } catch (e) {
            textHttp = false;
        }
    } else {
        // this is code that apologizes for Internet Explorer's existence
        try {
            textHttp = new ActiveXObject("Microsoft.XMLHTTP");
        } catch (e) {
            textHttp = false;
        }
    }

    if (!textHttp) {
        alert("cannot create textHttp object");
    } else {
        return textHttp;
    }
}

// called in ViewChannelContent.php during 'onload'
function getUsersLocationForMap() {
    if (textHttp.readyState == 0 || textHttp.readyState == 4) {
        textHttp.open("POST", getLocationURL, true);
        textHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        var membersIds = getMembersIds();
        textHttp.send("channelId=" + channelId + "&" + membersIds);

        textHttp.onreadystatechange = function () {
            if (textHttp.readyState == 4 && textHttp.status == 200) {
                getLocationsFromRequest();
                if (mapScope) {
                    mapScope.findScopeDimensions();
                } else {
                    mapScope = new MapScope();
                }

                if (mapScope["reframeMap"]) {
                    initMap();
                    mapScope["reframeMap"] = false;
                }
            }
        }
    }
    setTimeout(getUsersLocationForMap, 1000);
}

// receives response from PHP/MySQL
function getLocationsFromRequest() {
    /* The response ought to return an array of the current user's locations
     * with this text structure:
     *      [userId] [current lat] [current long]\n
     *      [userId] [current lat] [current long]\n...
     * Then, it will store the response to usersLocation
     */
    var textResponse = textHttp.responseText;
    usersLocations = []; // resets after every request
    var segments = textResponse.split(" ");
    for (var i = 0, j = 0; i < segments.length; i += 3, j++){
        if (segments[i] && segments[i + 1] && segments[i + 2]) {
            usersLocations[j] =
                new UserLocation(segments[i], segments[i + 1], segments[i + 2]);
        }
    }
}

// returns URL-style list of member ids for this channel to send to PHP
function getMembersIds(){
    // the membersIdArray used here comes from the ViewChannelContent declaration
    var membersId = "";
    for (var i = 0; i < membersIdArray.length; i++) {
        var memberObj = membersIdArray[i];
        membersId += "membersId[]=" + memberObj.user_id;
        if (i != membersIdArray.length - 1)
            membersId += "&";
    }
    return membersId;
}

function getBounds() {
    // TODO: the 'for' will need to check if a user is broadcasting
    var bounds = new google.maps.LatLngBounds();
    for (var user in usersLocations) {
        var position = new google.maps.LatLng(
            parseFloat(usersLocations[user].lat), parseFloat(usersLocations[user].long));
        bounds.extend(position);
    }
    return bounds;
}

/**********************************
 * Classes
 **********************************/
function UserLocation(id, lat, long) {
    this.id = id;
    this.lat = lat;
    this.long = long;
}

// TODO: MapScope class will need to be constructed asynchronously so that the map gets re-centered
// the default scope of the map
function MapScope() {
    this.reframeMap = true; // set to true when the center of lat/long changes
    this.center = this.findScopeDimensions();

    // default scope (in the event of one user)
    this.latLength = 5;
    this.longLength = 5;
}

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
        this.reframeMap = true;
        this.latLength = latLength;
        this.longLength = longLength;
    }

    // center of the scope (not adjusted for the whole map)
    var center = [parseFloat(this.latLength/2) + parseFloat(minLat),
                    parseFloat(this.longLength/2) + parseFloat(minLong)];
    if ( !this.center
        || (this.center[0] != center[0] || this.center[1] != center[1])) {
        this.reframeMap = true;
        return center;
    }
};