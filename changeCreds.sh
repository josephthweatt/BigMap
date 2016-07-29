#!/bin/bash
# When called, this script will change out Xampp's 'root' credentials
# with the admin's credentials on this server

find . -type f -exec sed -i 's/"localhost"/"localhost", "<username>", "<password>"/g' {} +

if ["$?" != "0"]; then
	echo "There was an error in changing out the credentials"
	exit 1
else 
	echo "credentials changed."
fi

