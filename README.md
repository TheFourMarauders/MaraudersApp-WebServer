#MaraudersMap-WebServer

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
- Retrieves the friend list for the given "user"
- Response: Code 200, JSON list of objects with fields (username, firstName, lastName)

######GET  "/api/services/user/:username/locations"

######PUT - "/api/services/user/:username/locations"

######POST "api/services/group/create"

######GET "api/services/user/:username/groups"

######GET  "api/services/group/:id"

######GET "api/services/group/:id/user/:username"

######DELETE "api/services/group/:id/user/:username"

######GET  "api/services/group/:id/locations"
