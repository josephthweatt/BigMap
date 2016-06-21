// PHP links
var signIn = "PHP/accounts/SignIn.php";
var viewChannelContent = "PHP/ViewChannelContent.php";
var broadcastingChannels = "PHP/accounts/MyBroadcastingChannels.php";

/****** functions for the "View your channels" button on the homepage *****/

function requestChannels() {
    sendUserInfo(broadcastingChannels, "displayChannels");
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
        childData.setAttribute("href", signIn);
        childData.className = "cell";
        childData.innerHTML = "Sign in to see your channels";
    } else if (channels && channels != "") {
        childData = document.createElement("div");
        channels = channels.split(" ");
        channels = channels.slice(3, channels.length);
        // get channels from the php string
        for (var i = channels.length-1; i >= 0; i--) {
            if (channels[i] != "") {
                //each channel will be made in a form
                var form = document.createElement("form");
                form.setAttribute("action", viewChannelContent);
                form.setAttribute("method", "post");
                var name = document.createElement("input");
                name.setAttribute("type", "hidden");
                name.setAttribute("name", "userInfo[]");
                name.setAttribute("value", getCookie("name"));
                var password = document.createElement("input");
                password.setAttribute("type", "hidden");
                password.setAttribute("name", "userInfo[]");
                password.setAttribute("value", getCookie("password"));
                var channelId = document.createElement("input");
                channelId.setAttribute("type", "hidden");
                channelId.setAttribute("name", "channelId");
                channelId.setAttribute("value", channels[i]);
                var input = document.createElement("input");
                input.setAttribute("type", "submit");
                input.setAttribute("value", "Channel " + channels[i]);
                input.className = "cell";

                form.appendChild(name);
                form.appendChild(password);
                form.appendChild(channelId);
                form.appendChild(input);
                childData.appendChild(form);
            }
        }
    } else {
        childData = document.createElement("p");
        childData.className = "cell";
        childData.innerHTML = "Something went wrong";
    }
    channelDiv.appendChild(childData);
}
