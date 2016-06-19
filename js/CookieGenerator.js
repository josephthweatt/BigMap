var COOKIE_LIFESPAN = 12 * 3600 * 1000; // 12 hours
var cookieExpiration;

function cacheUserInformation () {
	var name = document.getElementById('username').value;
	var password = document.getElementById('password').value;

	document.cookie = "name=" + name + ";";
	document.cookie = "password=" + password + ";";
}

function getUserInformation() {
	return [getCookie("name"), getCookie("password")];
}

// this code was taken from this SO question:
// stackoverflow.com/questions/10730362/get-cookie-by-name
function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}

// And this code was taken from this SO question:
// stackoverflow.com/questions/2144386/javascript-delete-cookie
function createCookie(name,value,days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime()+(days*24*60*60*1000));
        var expires = "; expires="+date.toGMTString();
    }
    else var expires = "";
    document.cookie = name+"="+value+expires+"; path=/";
}

function setCookieExpire() {
	var date = new Date();
	var time = date.getTime();
	cookieExpiration = COOKIE_LIFESPAN + time;
	date.setTime(cookieExpiration);
	document.cookie = "expires="+ date.toGMTString() +";";

	return cookieExpiration;
}

function eraseAll() {
	var cookies = document.cookie.split(";");
	for (var i = 0; i < cookies.length; i++)
	  eraseCookie(cookies[i].split("=")[0]);
}

function eraseCookie(name) {
    createCookie(name,"",-1);
}