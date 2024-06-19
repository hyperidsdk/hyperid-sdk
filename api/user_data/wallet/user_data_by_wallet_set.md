# User data / by wallet / set

## Request

Value              | Description 
-------------------|---------------
URI                | https://api.hypersecureid.com/user-data/by-wallet/set
Method             | POST 
Authorization      | Bearer AA.BB.CC
Content-type       | application/json
Scopes             | email, user-data-set

**Body Json Field**

Name               | Required | Type           | Description
-------------------|----------|----------------|---------------------
request_id         | false    | int64          | Opaque value used to maintain id between the request and response.
value_key          | true     | string         | 
value_data         | true     | string         | 
access_scope       | false    | int32          | See table below
wallet_address     | false    | string         | 

**Access Scope**

| Value  | Name 
| ------ | ----------------------------------- 
| 0      | private                             
| 1      | public (default)                    

**Examples**

```HTTP
POST /user-data/by-wallet/set HTTP/1.1
Host: api.hypersecureid.com
Content-Type: application/json
Authorization: Bearer AA.BB.CC
Content-Length: 149

{
    "wallet_address": "0xffffffffffffffffffffffffffffffffffffffff",
    "value_key": "key",
    "value_data": "data",
    "access_scope" : 0
}
```
```bash
curl --location 'https://api.hypersecureid.com/user-data/by-wallet/set' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer AA.BB.CC' \
--data '{
    "wallet_address": "0xffffffffffffffffffffffffffffffffffffffff",
    "value_key": "key",
    "value_data": "data",
    "access_scope" : 0
}'
```
```JS
const myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Bearer AA.BB.CC");

const raw = JSON.stringify({
  "wallet_address": "0xffffffffffffffffffffffffffffffffffffffff",
  "value_key": "key",
  "value_data": "data",
  "access_scope": 0
});

const requestOptions = {
  method: "POST",
  headers: myHeaders,
  body: raw,
  redirect: "follow"
};

fetch("https://api.hypersecureid.com/user-data/by-wallet/set", requestOptions)
  .then((response) => response.text())
  .then((result) => console.log(result))
  .catch((error) => console.error(error));
```

## Response

**Body Json Field**

Name          | Type          | Description
--------------|---------------|---------------------
request_id    | int64         | Opaque value used to maintain id between the request and response.
result        | int           | See table below

**Result**

| Value  | Name 
| ------ | ----------------------------------- 
| 0      | success                             
| -1     | fail by token invalid               
| -2     | fail by token expired               
| -3     | fail by access denied               
| -4     | fail by service temporary not valid 
| -5     | fail by invalid parameters          
| -6     | fail by wallet not exists           
| -7     | fail by key access denied           
| -8     | fail by key invalid                 

**Example**

```HTTP
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{
    "result": 0,
}
```