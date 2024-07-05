# HyperID Wordpress plugin documentation

This repository contains the HyperID Wordpress plugin.
HyperID is a decentralized identity and access management platform that provides a seamless transition from the traditional Web 2.0 to the decentralized world of Web3. This plugin provides simple interface for authentication as well as examples of use.

## Help and Documentation
Visit out webpage for more information [HyperID Documentation](https://hyperid.gitbook.io/hyperid-dev-docs/)

## Requirements
To use plugin copy source files in your wordpress folder under `wp-content/plugins` and activate it through wp admin console(`plugins->HyperID login for WordPress.`).

## Modules

### Authentication

HyperID Wordpress plugin provides numerous authentication flows. Input valid HyperID client credentials and click `Save Configuration`. You can customize auth flow with additional parameters (depending on chosen flow).

After configuration been saved, you can test flow with `Test Configuration` button.

### Service

Service client is needed for API calls and role managment.
Most of the time you should use self signed token for service account as it is provides seamless use of HyperID API.
Make sure that your service client under same project as your authentication client or if you use same authentication client option `service account` enabled.

### Role Managment

For better use check `Use Service Account for auth` and hit `Save`.

Here you can create your custom roles, attach and detach user for this roles, and put custom attributes under specific roles.

## Example of use

Make sure that user_role mapper is added for authentication client.

For familiarization with Api provided with this plugin:

1. install wordpess;

2. copy plugin sourse code (check Requirements)

3. input valid client info for `Authentication` and `Service`

4. create 3 pages

5. On page1 add 2 buttons with text `Upgrade to SN2` and `Upgrade to SN3` with following links `/wordpress/?updateToHidRole=role2Upgrade` and `/wordpress/?updateToHidRole=role3Upgrade` (if your url for wordpess is different change the pass).

6. On page 2 add text `<hid_attribute_text_1>` and `<hid_attribute_text_2>`, this will be replaced with attributes value.

7. On page 3 add text `<hid_attribute_long_text_1>` and `<hid_attribute_long_text_2>`, this will be replaced with attributes value.

8. create roles `SN1` (basic role with no restrictions), `SN2` and `SN3`

9. in role `SN2` create attributes `text1` and `text2` with any values you want

10. in role `SN3` create attributes `long_text_1` and `long_text_2` with any values you want

11. copy file `functions_hid.php` to `wp-includes` folder and include it into `functions.php` with:
```php
    require_once 'functions_hid.php';
```

12. edit `functions_hid.php`, replace all page ids with one that you created.

### Wordpress use
In top menu bar `sign in`\\`sign-out` button should appear.

Access for page 2 and 3 will be restricted unless user have been attached to the respective roles. To do that user should go to page 1 and click on button to be attached for role.

In those pages after user have been attached for role, your attribute tags should be replaced with actual attributes for the role.

User authorization will be checked every minute, if user signed out from session he will be signed out from wordpress and should sign in again.

If you as admin of wordpress signed in with wordpress credentials through admin pannel but not with HyperID, you will not have access even for page 1.