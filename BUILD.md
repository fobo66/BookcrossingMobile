---
layout: default
permalink: /build
---
# Building Bookcrossing Mobile

To build Bookcrossing Mobile application, you need to grab your API key for Google Maps
[here](http://bit.ly/2uZ5EWV) first.

1. Open your Terminal, `cd` to the project's directory and execute the following commands:

   ```bash
   cp keystore.properties.example keystore.properties
   cp api.properties.example api.properties
   cp app/google-services.json.example app/google-services.json
   ```

1. Replace stub values in `keystore.properties` and `api.properties` files with your actual credentials.

Alternatively, you can use your own Firebase project for testing purposes. In this case you don't
need to run last command from above, just register a new Firebase project for your app.
