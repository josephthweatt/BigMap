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
        textHttp.open("POST", getLocationURL);
        textHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

        var membersIds = getMembersIds();
        textHttp.send("channelId=" + channelId + "&" + membersIds);

        console.log("channelId=" + channelId + "&" + membersIds);
        //!!!!!!!!! Code is not entering here !!!!!!!!
        if (textHttp.readyState == 4 && textHttp.status == 200) {
            console.log("got here");
            getLocationsFromRequest();
            mapScope = new MapScope();
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
    for (var i = 0; i < segments; i += 3){
        if (segments[i] && segments[i + 1] && segments[i + 2]) {
            usersLocations.push(
                new UserLocation(segments[i], segments[i + 1], segments[i + 2]));
        }
    }
}

// returns URL-style list of member ids for this channel
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

/**********************************
 * Classes
 **********************************/
function UserLocation(userName, lat, long) {
    this.userName = userName;
    this.lat = lat;
    this.long = long;
}


// TODO: MapScope class will need to be constructed asynchronously so that the map gets re-centered
// the default scope of the map
function MapScope() {
    var scopeDimensions = this.findScopeDimensions();
    this.latLength = scopeDimensions[0];
    this.longLength = scopeDimensions[1];

    this.center = this.findCenter();
}

MapScope.prototype.findScopeDimensions = function() {
    var minLat = 0, maxLat = 0;
    var minLong = 0, maxLong = 0;

    // get the rectangular boundaries of all user locations
    for (var i = 0; i < usersLocations.length; i++) {
        if (usersLocations[i].lat < minLat) {
            minLat = usersLocations[i].lat;
        } else if (usersLocations[i].lat > maxLat) {
            maxLat = usersLocations[i].lat;
        }
        if (usersLocations[i].long < minLong) {
            minLong = usersLocations[i].long;
        } else if (usersLocations[i].long > maxLong) {
            maxLong = usersLocations[i].long;
        }
    }

    // get dimensions of the rectangle
    var latLength = maxLat - minLat;
    var longLength = maxLong - minLong;
    return [latLength, longLength];
};

MapScope.prototype.findCenter = function() {
    return [(this.latLength/2), (this.longLength/2)];
};

// notes
/**************** xmlHttpRequest *********************************************
readyState 	Holds the status of the XMLHttpRequest. Changes from 0 to 4:
            0: request not initialized
            1: server connection established
            2: request received
            3: processing request
            4: request finished and response is ready
 /****************************************************************************/