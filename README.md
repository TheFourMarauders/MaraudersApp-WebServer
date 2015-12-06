#MaraudersMap-WebServer

##Dependencies
###Minimum versions
- jdk 1.8.0_60 (at least)
- apache maven 3.3.9

###Recommended for Deployment
- mongodb 3.0.7

##Authentication
The webserver uses HTTP Basic authentication.  All requests to /api/services/* must have authentication. Methods along these routes will return either a 403 or 404 error code depending on the type of authorization failure.


##Routes
######POST  "/api/create-user"
- Attempts to create a user in the system.
- Request Body: JSON document with fields: username, password, firstName, and lastName.
- Response: Code 200: Success, Code 400: Bad JSON document, Code 409: username already taken


######POST  "/api/services/user/:username/send-friend-request/:targetusername"
- Attempts to send a friend request from the "username' to the "targetusername"
- Response: Code 200 if success, otherwise Code 400 for bad request

######GET - "/api/services/user/:username/incoming-friend-requests"
 - Retrieves the set of incoming friend requests
 - Response: Code 200 if success, Body: JSON list of objects containing fields (username, firstName, lastName).
 
######PUT - "/api/services/user/:username/accept-friend/:targetfriend"
- Accepts the friend request for "username" from "targetfriend"
- Response: Code 200 if success, Code 400 for invalid targetfriend

######DELETE  "/api/services/user/:username/delete-friend/:targetfriend"
- Mutually removes both username and targetfriend from eachothers respective friends list
- Response: Code 200

######GET  "/api/services/user/:username/friends"
- Retrieves the friend list for the given "username"
- Response: Code 200, JSON list of objects with fields (username, firstName, lastName)

######GET  "/api/services/user/:username/locations"
- Retrieves the set of locations for the given "username"
- Query Parameters (optional): start - a time\*, end - a time\*
- Response: Code 200, Body: Json list of objects with fields (latitude, longitude, time*)

######PUT - "/api/services/user/:username/locations"
- Puts a list of locations into your location history
- Body: json list of objects with fields (latitude, longitude, and time*)
- Response: Code 200 if sucess, Code 400 if bad request json

######POST "api/services/group/create"
- Creates a group with the given name
- Query parameter (mandatory): groupname - the naem of the group
- Response: Code 200, Body - GUID corresponding to new group

######GET "api/services/user/:username/groups"
- gets a list of group info for groups that "username" is a member of
- Response: Code 200, Body - json array of group infos

######GET  "api/services/group/:id"
- get a single group info
- Response: Code 200, Body - json with groupname and usernames of members

######PUT "api/services/group/:id/user/:username"
- adds user "username" to group with id "id" (*note: id is not the groupname*)
- Response: Code 200 if success

######DELETE "api/services/group/:id/user/:username"
- removes user "username" from group with id "id"
- Response: Code 200 if success

######GET  "api/services/group/:id/locations"
- get the locations of all friendly group members in "id"
- Query Parameters (optional): start - a time\*, end - a time\*
- Response: Code 200, Body - json array with all user locations between start and end time, organized by user

*all times in ISO 8061 format
