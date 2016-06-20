
function requestChannels() {
    sendUserInfo("PHP/accounts/MyBroadcastingChannels.php", "displayChannels");
}

// called by sendUserInfo after it receives back the login credentials
function displayChannels(channels) {
    console.log(channels);
    var channelDiv = document.getElementById("viewChannels");
    var childData;
    
    if (channels == "You have not registered to any channels") {
        childData = document.createElement("p");
        childData.className = "cell";
        childData.innerHTML = "Not registered to any channels";
    } else if (channels && channels != "") {
        channelDiv.innerHTML = "";
        childData = document.createElement("div");
        channels = channels.split(" ");
        channels = channels.slice(3, channels.length);
        console.log(channels);
        // get channels from the php string
        for (var i = 0; i < channels.length; i++) {
            if (channels[i] != "") {
                var p = document.createElement("p");
                p.className = "cell";
                p.innerHTML = "Channel " + channels[i];
                childData.appendChild(p);
            }
        }
    } else if (channels == "Invalid Password") {
        childData = document.createElement("p");
        childData.className = "cell";
        childData.innerHTML = "Sign in to see your channels";
    } else {
        childData = document.createElement("p");
        childData.className = "cell";
        childData.innerHTML = "Something went wrong";
    }
    channelDiv.appendChild(childData);
}
