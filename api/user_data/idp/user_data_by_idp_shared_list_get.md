# User data / by identity provider / get list of keys

## Request

Value              | Description 
-------------------|---------------
URI                | https://api.hypersecureid.com/user-data/by-idp/list-get
Method             | POST 
Authorization      | Bearer AA.BB.CC
Content-type       | application/json
Scopes             | email, user-data-get

**Body Json Field**

Name               | Required | Type           | Description
-------------------|----------|----------------|---------------------
request_id         | false    | int64          | Opaque value used to maintain id between the request and response.
search_id          | false    | string         | Use the search id from the prior request to load the keys from the most recent load.
page_size          | false    | uint32         | Keys count in response list (default 100)
identity_provider  | true     | string         | See https://login.hypersecureid.com/auth/realms/HyperID/.well-known/openid-configuration, key identity_providers

**Example**

**Examples**

```HTTP
POST /user-data/by-idp/shared-list-get HTTP/1.1
Host: api.hypersecureid.com
Content-Type: application/json
Authorization: Bearer AA.BB.CC
Content-Length: 86

{
    "identity_provider": "telegram",
    "page_size": 200,
    "search_id": ""
}
```
```bash
curl --location 'https://api.hypersecureid.com/user-data/by-idp/shared-list-get' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer AA.BB.CC' \
--data '{
    "identity_provider": "telegram",
    "page_size": 200,
    "search_id": ""
}'
```
```JS
const myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Bearer AA.BB.CC");

const raw = JSON.stringify({
  "identity_provider": "telegram",
  "page_size": 200,
  "search_id": ""
});

const requestOptions = {
  method: "POST",
  headers: myHeaders,
  body: raw,
  redirect: "follow"
};

fetch("https://api.hypersecureid.com/user-data/by-idp/shared-list-get", requestOptions)
  .then((response) => response.text())
  .then((result) => console.log(result))
  .catch((error) => console.error(error));
```

## Response

**Body Json Field**

Name                    | Type          | Description
------------------------|---------------|---------------------
request_id              | int64         | Opaque value used to maintain id between the request and response.
result                  | int           | See table below
keys_shared             | array         | Array of strings
next_search_id          | string        | Use "search_id" in the subsequent request to load the following page; if all the keys are loaded, it will be empty.

**Result**

| Value  | Name 
| ------ | ----------------------------------- 
| 1      | success not found                   
| 0      | success                             
| -1     | fail by token invalid               
| -2     | fail by token expired               
| -3     | fail by access denied               
| -4     | fail by service temporary not valid 
| -5     | fail by invalid parameters          
| -6     | fail by identity provider not found 

**Example**

```HTTP
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{
    "result": 0,
    "keys_shared": [],
    "next_search_id": ""
}
```