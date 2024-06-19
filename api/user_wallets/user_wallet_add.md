# User wallet / add

## Request

Value              | Description 
-------------------|---------------
URI                | https://api.hypersecureid.com/user-wallet/add
Method             | POST 
Authorization      | Bearer AA.BB.CC
Content-type       | application/json
Scopes             | email, user-data-set

**Body Json Field**

Name                | Required | Type           | Description
--------------------|----------|----------------|---------------------
request_id          | false    | int64          | Opaque value used to maintain id between the request and response.
wallet              | true     | object         | See table bellow
wallet_data_to_sign | true     | string         | String with text and date in UTC in following format "%s %04d-%02d-%02dT%02d:%02d:%02dZ"
wallet_sign         | true     | string         | Personal sign of wallet_data_to_sign

**Wallet**

Name      | Required | Type         | Description
--------  |----------|--------------|---------------------
chain     | true     | string       |
address   | true     | string       |
tags      | false    | array        |
label     | false    | string       | Array of strings

**Examples**

```HTTP
POST /user-wallet/add HTTP/1.1
Host: api.hypersecureid.com
Content-Type: application/json
Authorization: Bearer AA.BB.CC
Content-Length: 291

{
    "wallet_data_to_sign": "data 2023-03-14dT12:05:00Z",
    "wallet_sign": "0xfffffffffffff",
    "wallet": {
        "address": "0xffffffffffffffffffffffffffffffffffffffff",
        "chain": "1",
        "tags": [
            "tag"
        ],
        "label": "label"
    }
}
```
```bash
curl --location 'https://api.hypersecureid.com/user-wallet/add' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer AA.BB.CC' \
--data '{
    "wallet_data_to_sign": "data 2023-03-14dT12:05:00Z",
    "wallet_sign": "0xfffffffffffff",
    "wallet": {
        "address": "0xffffffffffffffffffffffffffffffffffffffff",
        "chain": "1",
        "tags": [
            "tag"
        ],
        "label": "label"
    }
}'
```
```JS
const myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Bearer AA.BB.CC");

const raw = JSON.stringify({
  "wallet_data_to_sign": "data 2023-03-14dT12:05:00Z",
  "wallet_sign": "0xfffffffffffff",
  "wallet": {
    "address": "0xffffffffffffffffffffffffffffffffffffffff",
    "chain": "1",
    "tags": [
      "tag"
    ],
    "label": "label"
  }
});

const requestOptions = {
  method: "POST",
  headers: myHeaders,
  body: raw,
  redirect: "follow"
};

fetch("https://api.hypersecureid.com/user-wallet/add", requestOptions)
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
| -6     | fail by wallet verification sign not valid 
| -7     | fail by wallet already has owner

**Example**

```HTTP
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{
    "result": 0,
}
```