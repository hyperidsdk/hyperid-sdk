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
identity_provider  | true     | string         | See https://login.hypersecureid.com/auth/realms/HyperID/.well-known/openid-configuration, key identity_providers

**Examples**

```HTTP
POST /user-data/by-idp/list-get HTTP/1.1
Host: api.hypersecureid.com
Content-Type: application/json
Authorization: Bearer AA.BB.CC
Content-Length: 41

{
    "identity_provider": "telegram"
}
```
```bash
curl --location 'https://api.hypersecureid.com/user-data/by-idp/list-get' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer AA.BB.CC' \
--data '{
    "identity_provider": "telegram"
}'
```
```JS
const myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Bearer AA.BB.CC");

const raw = JSON.stringify({
  "identity_provider": "telegram"
});

const requestOptions = {
  method: "POST",
  headers: myHeaders,
  body: raw,
  redirect: "follow"
};

fetch("https://api.hypersecureid.com/user-data/by-idp/list-get", requestOptions)
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
keys_private            | array         | Array of strings
keys_public             | array         | Array of strings

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
| -6     | fail by identity providers not found

**Example**

```HTTP
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{
    "result": 0,
    "keys_private": [
        "key"
    ],
    "keys_public": []
}
```