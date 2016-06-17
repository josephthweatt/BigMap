#!/bin/bash
# When called, this script will change out Xampp's 'root' credentials
# with the admin's credentials on this server

find . -type f -exec sed -i 's/"root"/"<username>", "<password>"/g' {} +
find . -type f -exec sed -i 's/"ws://localhost:2000"/"ws://jathweatt.com:2000"/g' {} +

if ["$?" != "0"]; then
	echo "There was an error in changing out the credentials"
	exit 1
else 
	echo "credentials changed. running socket server"
fi

#run the socket server
php /PHP/ChannelSocket/socket.php &