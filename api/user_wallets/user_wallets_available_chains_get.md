# User wallets / available chains get

## Request

Value              | Description 
-------------------|---------------
URI                | https://api.hypersecureid.com/user-wallets/available-chains-get
Method             | POST 
Authorization      | Bearer AA.BB.CC
Content-type       | application/json
Scopes             | email, user-data-get

**Body Json Field**

Name               | Required | Type           | Description
-------------------|----------|----------------|---------------------
request_id         | false    | int64          | Opaque value used to maintain id between the request and response.

**Examples**

```HTTP
POST /user-wallets/available-chains-get HTTP/1.1
Host: api.hypersecureid.com
Content-Type: application/json
Authorization: Bearer AA.BB.CC
Content-Length: 2

{}
```
```bash
curl --location 'https://api.hypersecureid.com/user-wallets/available-chains-get' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer AA.BB.CC' \
--data '{}'
```
```JS
const myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Bearer AA.BB.CC");

const raw = JSON.stringify({});

const requestOptions = {
  method: "POST",
  headers: myHeaders,
  body: raw,
  redirect: "follow"
};

fetch("https://api.hypersecureid.com/user-wallets/available-chains-get", requestOptions)
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
wallet_chains | array         | Array of available chain ids

**Result**

| Value  | Name 
| ------ | ----------------------------------- 
| 0      | success                             
| -1     | fail by token invalid               
| -2     | fail by token expired               
| -3     | fail by access denied               
| -4     | fail by service temporary not valid 
| -5     | fail by invalid parameters          

**Example**

```HTTP
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{
    "request_id": -1,
    "result": 0,
    "wallet_chains": [
        "1",
        "8",
        "10",
        "19",
        "20",
        "24",
        "25",
        "30",
        "40",
        "50",
        "52",
        "55",
        "56",
        "57",
        "60",
        "61",
        "66",
        "70",
        "82",
        "87",
        "88",
        "100",
        "106",
        "108",
        "122",
        "128",
        "137",
        "200",
        "246",
        "250",
        "269",
        "288",
        "311",
        "314",
        "321",
        "336",
        "361",
        "369",
        "416",
        "534",
        "592",
        "820",
        "888",
        "1088",
        "1116",
        "1231",
        "1234",
        "1284",
        "1285",
        "2000",
        "2222",
        "4689",
        "5050",
        "5551",
        "6969",
        "7700",
        "8217",
        "9001",
        "10000",
        "32520",
        "32659",
        "39815",
        "42161",
        "42170",
        "42220",
        "42262",
        "43114",
        "47805",
        "55555",
        "71402",
        "333999",
        "420420",
        "888888",
        "11155111",
        "1313161554",
        "1666600000",
        "5",
        "97",
        "941",
        "943",
        "43113",
        "80001",
        "101",
        "102",
        "808088",
        "808089"
    ]
}
```