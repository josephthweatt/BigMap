// Functions to help interactions between javascript and PHP

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

/**
 * Sends userInfo to PHP in the typical [userid] [password] form
 * @param locationURL - The PHP file to send the information to.
 *                    - This file must exist in the PHP dir, and
 *                    - the url should start in the same dir
 * ! This function might not be used. I'll consider later whether
 * ! it is useful.
 */
function sendUserInfo(locationURL) {
    if (textHttp.readyState == 0 || textHttp.readyState == 4) {
        textHttp.open("POST", locationURL, true);
        textHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

        textHttp.send("user-info[]=" + getCookie("name") + "&user-info[]" + getCookie("password"));
    }
}

// will format a PHP array's JSON encoding so that it can be parsed by JS
function addEscapes(jsonString) {
    // 'for' will skip the first and last quotations
    for (var i = 1; i < jsonString.length - 1; i++) {
        if (jsonString[i] == "\"") {
            jsonString = jsonString.slice(0, i) +"\\"+ jsonString.slice(i, jsonString.length-1);
        }
    }
    return jsonString;
}
