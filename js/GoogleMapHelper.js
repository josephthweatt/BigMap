/*************************************************************************
 * This script is to implement AJAX Framework when using the
 * google maps API for the web browser. This was learned in
 * TheNewBoston's AJAX tutorial, the code of which can be found here:
 * http://stackoverflow.com/questions/25598468/ajax-tutorial-not-working
 *************************************************************************/
var getLocationURL = "../PHP/GetChannelUsersLocation.php";
var textHttp = createXMLHttpRequestObject();

var channelId;
var membersId;

var usersLocations; // will be array of userLocation

function UserLocation(userName, lat, long) {
    this.userName = userName;
    this.lat = lat;
    this.long = long;
}

function createXMLHttpRequestObject() {
    var textHttp;

    if (window.ActiveXObject) {
        // this is code that apologizes for Internet Explorer's existence
        try {
            textHttp = new ActiveXObject("Microsoft.XMLHTTP");
        } catch (e) {
            textHttp = false;
        }
    } else {
        // this is executed when a user isn't trying to ruin the app (they're not using IE)
        try {
            textHttp = new XMLHttpRequest();
        } catch (e) {
            textHttp = false;
        }
    }

    if (!textHttp) {
        alert("cannot create textHttp object");
    } else {
        getInfoFromServer();
        return textHttp;
    }
}


function getUsersLocationForMap() {
    if (textHttp.readyState == 0 || textHttp.readyState == 4){
        textHttp.open("POST", getLocationURL);
        textHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        textHttp.send("channelId=" + channelId + "&" + membersId);

        getLocationsFromRequest();
    }

    setTimeout('getUsersLocationForMap()', 1000);
}

function getInfoFromServer() {
    channelId = "<?php echo $_POST[\"channelId\"]?>";

    var membersIdArray = "<?php echo getChannelMembers(" + channelId + ")?>";
    membersId = "";
    for (var i = 0; i < membersIdArray.length; i++) {
        membersId += "membersId[]=" + membersIdArray[i];
        if (i != membersIdArray.length - 1)
            membersId += "&";
    }
}

function getLocationsFromRequest() {
    if (textHttp == 4) {
        if (textHttp == 200) {
            /* the response ought to return an array of the current user's locations
             * with this text structure:
             *      [user] [current lat] [current long]\n
             *      [user] [current lat] [current long]\n...
             * Then, it will store the response to usersLocation
             */
            var textResponse = textHttp.responseText;

            usersLocations = new Array(); // resets after every request
            var lines = textResponse.split("\n");
            for (var i = 0; i < lines; i++){
                var userData = lines[i].split(" ");
                usersLocations.push(
                    new UserLocation(userData[0], userData[1], userData[2]));
            }

        } else {
            alert("Something went wrong getting location information");
        }
    }
}

/**************** xmlHttpRequest *********************************************
readyState 	Holds the status of the XMLHttpRequest. Changes from 0 to 4:
            0: request not initialized
            1: server connection established
            2: request received
            3: processing request
            4: request finished and response is ready
 /****************************************************************************/