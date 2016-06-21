
// functions for the "View your channels" button
function requestChannels() {
    sendUserInfo("PHP/accounts/MyBroadcastingChannels.php", "displayChannels");
}

// called by sendUserInfo after it receives back the login credentials
function displayChannels(channels) {
    var channelDiv = document.getElementById("viewChannels");
    var childData;

    channelDiv.innerHTML = "";
    if (channels == "You have not registered to any channels") {
        childData = document.createElement("p");
        childData.className = "cell";
        childData.innerHTML = "Not registered to any channels";
    } else if (channels == "User does not exist"
        || channels == "Invalid Password") {
        childData = document.createElement("a");
        childData.setAttribute("href", "SignIn");
        childData.className = "cell";
        childData.innerHTML = "Sign in to see your channels";
    } else if (channels && channels != "") {
        childData = document.createElement("div");
        channels = channels.split(" ");
        channels = channels.slice(3, channels.length);
        // get channels from the php string
        for (var i = channels.length-1; i >= 0; i--) {
            if (channels[i] != "") {
                var p = document.createElement("p");
                p.className = "cell";
                p.innerHTML = "Channel " + channels[i];
                childData.appendChild(p);
            }
        }
    } else {
        childData = document.createElement("p");
        childData.className = "cell";
        childData.innerHTML = "Something went wrong";
    }
    channelDiv.appendChild(childData);
}
