## Keycloak realm
To load realm settings, insert **realm-export.json** in current directory.  
That file in default contains 2 clients:

|Name|Flow type|Scope|
|--:|--:|--:|
|vue-edit|Authorization Code Flow|soundscribe-edit  <br />soundscribe-read|
|vue-read|Client Credentials Flow|soundscribe-read|

## Admin panel
To get inside admin console, go to {{Keycloak host}}/{{Keycloak port}}
and use admin password and login passed in docker-compose.yml file.
