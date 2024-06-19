# User data / by user id / delete

## Request

Value              | Description 
-------------------|---------------
URI                | https://api.hypersecureid.com/user-data/by-user-id/delete
Method             | POST 
Authorization      | Bearer AA.BB.CC
Content-type       | application/json
Scopes             | email, user-data-set

**Body Json Field**

Name               | Required | Type           | Description
-------------------|----------|----------------|---------------------
request_id         | false    | int64          | Opaque value used to maintain id between the request and response.
value_keys         | true     | array          | Array of strings 

**Examples**

```HTTP
POST /user-data/by-user-id/delete HTTP/1.1
Host: api.hypersecureid.com
Content-Type: application/json
Authorization: Bearer AA.BB.CC
Content-Length: 47

{
    "value_keys": [
        "key"
    ]
}
```
```bash
curl --location 'https://api.hypersecureid.com/user-data/by-user-id/delete' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer AA.BB.CC' \
--data '{
    "value_keys": [
        "key"
    ]
}'
```
```JS
const myHeaders = new Headers();
myHeaders.append("Content-Type", "application/json");
myHeaders.append("Authorization", "Bearer AA.BB.CC");

const raw = JSON.stringify({
  "value_keys": [
    "key"
  ]
});

const requestOptions = {
  method: "POST",
  headers: myHeaders,
  body: raw,
  redirect: "follow"
};

fetch("https://api.hypersecureid.com/user-data/by-user-id/delete", requestOptions)
  .then((response) => response.text())
  .then((result) => console.log(result))
  .catch((error) => console.error(error));
```

## Response

**Body Json Field**

Name                | Type          | Description
--------------------|---------------|---------------------
request_id          | int64         | Opaque value used to maintain id between the request and response.
result              | int           | See table below
keys_delete_results | array         | Array of objects that contain: value_key, is_deleted.

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

**Example**

```HTTP
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{
    "result": 0,
    "keys_delete_results": [
        {
            "is_deleted": true,
            "value_key": "key"
        }
    ]
}
```