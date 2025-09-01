# Keycloak Permission Mapper

## Task

The Permission Mapper allows users to add permissions related to the user to the Userinfo endpoint. Since it may be necessary for applications to check access to a resource at the permissions level, this mapper provides that functionality. Thus, in the Spring Security context, in addition to access checks at the role level (e.g., `@PreAuthorize("hasRole(...)")`), it is also possible to perform checks at the permissions level (e.g., `@PreAuthorize("hasAuthority(...)")`). If the permissions of the current user are needed for another client, the ClientId can be specified as a query parameter. The keyword used for this is `audience`.

## API

### `GET /auth/realms/permission/protocol/openid-connect/userinfo?audience=<clientId>`

Returns the permissions, roles, and user information (claims) of the current user for the specified client.

### `GET /auth/realms/permission/protocol/openid-connect/userinfo`

Returns the permissions, roles, and user information (claims) of the current user. The JSON array `authorities` contains the user's permissions, while the JSON array `user_roles` includes the application-specific roles of the user.

**Example Response:**

```json
{
  "sub": "b7925f6b-5fae-4a1b-ad3f-9f21f3a4f228",
  "email_verified": false,
  "user_name": "permission.test",
  "preferred_username": "permission.test",
  "user_roles": [
    "ROLE_clientrole_admin"
  ],
  "authorities": [
    "theclient_GUI_Overview",
    "sfmodel_WRITE_Manufacturers",
    "theclient_GUI_OwnApprovals",
    "sfmodel_WRITE_KeyPersons",
    "sfmodel_READ_ReleaseProcess",
    "sfmodel_READ_Applications",
    "theclient_GUI_NewApproval",
    "theclient_GUI_Approvals",
    "sfmodel_READ_Manufacturers",
    "sfmodel_DELETE_Manufacturers",
    "sfmodel_WRITE_Applications",
    "theclient_GUI_ShowApproval",
    "sfmodel_WRITE_ReleaseProcess",
    "theclient_GUI_AdminArea",
    "sfmodel_DELETE_Applications",
    "sfmodel_DELETE_KeyPersons",
    "Default Resource",
    "sfmodel_DELETE_ReleaseProcess",
    "sfmodel_READ_KeyPersons"
  ]
}
```

### Example Workflow using CURL

#### Retrieve Access Token

The Access Token is required to fetch the corresponding permissions, roles, and user information from Keycloak.

The following CURL command retrieves the Access Token from the Keycloak server to perform the subsequent request to the Userinfo endpoint.

```bash
curl -i -v -X POST -d 'client_id=<client-id>' -d 'username=test.admin' -d 'password=Test1234' -d 'grant_type=password' -d 'client_secret=<client_secret>' 'https://<base-url>/realms/<realm>/protocol/openid-connect/token'
````

From the response of the above CURL command, the Access Token from the JSON body should be copied using the key `access_token` and assigned to the variable `TOKEN`.

```bash
TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSl...1lIjoid
```

#### Call the Userinfo Endpoint

With the following command, the endpoint can then be called.

##### When using the global Permission Provider

##### When using a locally running test instance of the Permission Provider

```bash
curl -i -v -X GET -H "Authorization: Bearer $TOKEN" "https://<base-url>/auth/realms/<realm>/protocol/openid-connect/userinfo"
```

## Configuration
### In Keycloak

To use the Permission Mapper, a new mapper of type User's authorities must simply be added under Mappers in the corresponding client.



# Keycloak Permission Mapper

## Aufgabe

Der Permission Mapper ermöglicht es dem Nutzer, dem Userinfo-Endpunkt dem Nutzer zugehörigen Rechte (Permissions) hinzuzufügen.
Da es bei Anwendungen fachlich oder auch technisch erforderlich sein kann, einen Zugriff auf eine Resource auf Ebene der Permissions (Rechte) zu prüfen, stellt dieser Mapper diese bereit.
Somit ist beispielsweise im Spring-Security-Context neben der Zugriffsprüfung auf Rollenebene (z.B. `@PreAuthorize("hasRole(...)")`) auch eine ebensolche Prüfung auf Rechteebene (z.B. `@PreAuthorize("hasAuthority(...)")`) möglich.
Werden die Rechte des aktuellen Nutzers für einen anderen Client benötigt, so kann ClientId als QueryParam angegeben werden. Das Schlüsselweort ist dabei `audience`

## API

### `GET /auth/realms/permission/protocol/openid-connect/userinfo?audience=<clientId>`

Liefert die Permissions, Rollen und Userinformationen (claim) des aktuellen Users für den angegebenen Client zurück.

### `GET /auth/realms/permission/protocol/openid-connect/userinfo`

Liefert die Permissions, Rollen und Userinformationen (claim) des aktuellen Users zurück.
Die JSON-Arrays `authorities` beinhaltet jeweils die Permissions des Nutzers. 
Im JSON-Array `user_roles` sind die anwendungsbezogenen Rollen des Users enthalten.

Beispiel-Response:

```json
{
    "sub": "b7925f6b-5fae-4a1b-ad3f-9f21f3a4f228",
    "email_verified": false,
    "user_name": "permission.test",
    "preferred_username": "permission.test",
    "user_roles": [
        "ROLE_clientrole_admin"
    ],
    "authorities": [
        "theclient_GUI_Overview",
        "sfmodel_WRITE_Manufacturers",
        "theclient_GUI_OwnApprovals",
        "sfmodel_WRITE_KeyPersons",
        "sfmodel_READ_ReleaseProcess",
        "sfmodel_READ_Applications",
        "theclient_GUI_NewApproval",
        "theclient_GUI_Approvals",
        "sfmodel_READ_Manufacturers",
        "sfmodel_DELETE_Manufacturers",
        "sfmodel_WRITE_Applications",
        "theclient_GUI_ShowApproval",
        "sfmodel_WRITE_ReleaseProcess",
        "theclient_GUI_AdminArea",
        "sfmodel_DELETE_Applications",
        "sfmodel_DELETE_KeyPersons",
        "Default Resource",
        "sfmodel_DELETE_ReleaseProcess",
        "sfmodel_READ_KeyPersons"
    ]
}
```

### Beispielhafter Ablauf mittels CURL

#### Access Token abrufen

Der Access Token ist erforderlich, um die entsprechenden Permissions, Rollen und Userinformationen vom Keycloak zu holen.

Der nachfolgende CURL-Befehl holt den Access-Token vom Keycloak-Server um den darauf folgendenden Request an den Userinfo-Endpunkt tätigen zu können.

```bash
curl -i -v -X POST -d 'client_id=<client-id>' -d 'username=test.admin' -d 'password=Test1234' -d 'grant_type=password' -d 'client_secret=<client_secret>' 'https://ssodev.muenchen.de/auth/realms/A52/protocol/openid-connect/token'
```

Aus der Response des obigen CURL-Befehls ist der Access-Token aus dem JSON-Body vom Datum mit dem Schlüssel `access_token` zu kopieren und der Variablen `TOKEN` zuzuweisen. 

```bash
TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSl...1lIjoid
```

#### Userinfo-Endpunkt aufrufen

Mit dem folgenden Befehlen kann dann der -Endpunkt aufgerufen werden.

##### Bei Verwendung des globalen Permission Provider

##### Bei Verwendung einer lokal laufenden Testinstanz des Permission Provider

```bash
curl -i -v -X GET -H "Authorization: Bearer $TOKEN" "https://ssodev.muenchen.de/auth/realms/A52/protocol/openid-connect/userinfo"
```

## Konfiguration

### Im KeyCloak

Um den Permission Mapper zu verwenden, muss im entsprechenden Client unter Mappers lediglich ein neuer Mapper vom Typ User's authorities hinzugefügt werden.
