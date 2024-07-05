<?php

function upgradeToRoles() {
	if(isset($_GET['updateToHidRole'])) {
		$role2Name = 'SN2';
		$role3Name = 'SN3';

		$page2Id = '3083';
		$page3Id = '3588';

		$userRoles = null;
		try {
			$userRoles = hyperIdClientController::getAuth()->getUserInfo()->user_roles;
		} catch(Exception $e) {
			if(!is_admin()) {
				wp_redirect(site_url());
				exit;
			}
		}

		if($_GET['updateToHidRole'] == 'role2Upgrade' && $userRoles && in_array($role2Name, $userRoles)) {
			wp_redirect(site_url()."?page_id=".$page2Id);
			exit;
		}
		if($_GET['updateToHidRole'] == 'role3Upgrade' && $userRoles && in_array($role3Name, $userRoles)) {
			wp_redirect(site_url()."?page_id=".$page3Id);
			exit;
		}

		$userId     = '';
		$userEmail  = '';
		try{
			$userId     = hyperIdClientController::getAuth(hyperIdClientController::getAuthRefreshToken())->getUserInfo()->user_id;
			$userEmail  = hyperIdClientController::getAuth(hyperIdClientController::getAuthRefreshToken())->getUserInfo()->user_email;
		} catch(Exception $e) {
			return;
		}

		$response = get_transient('project_roles');
		if($response === false) {
			$response   = hyperIdClientController::getRoleManager()->rolesGet(hyperIdClientController::getSaAccessToken());
			set_transient('project_roles', $response, 60);
		}
		$roleId2    = '';
		$roleId3    = '';
		if($response['result'] == RolesGetResult::SUCCESS) {
			$roleId2Ind = array_search($role2Name, array_column($response['roles'], 'name'));
			if(is_numeric($roleId2Ind)) {
				$roleId2 = $response['roles'][$roleId2Ind]['id'];
			}
			$roleId3Ind = array_search($role3Name, array_column($response['roles'], 'name'));
			if(is_numeric($roleId3Ind)) {
				$roleId3 = $response['roles'][$roleId3Ind]['id'];
			}
		}

		if($_GET['updateToHidRole'] == 'role2Upgrade') {
			hyperIdClientController::getRoleManager()->userRoleAttach(hyperIdClientController::getSaAccessToken(), $userId, $userEmail, $roleId2);
			hyperIdClientController::refreshAuthTokens();
			wp_redirect(site_url()."?page_id=".$page2Id);
			exit;
		}
		if($_GET['updateToHidRole'] == 'role3Upgrade') {
			hyperIdClientController::getRoleManager()->userRoleAttach(hyperIdClientController::getSaAccessToken(), $userId, $userEmail, $roleId3);
			hyperIdClientController::refreshAuthTokens();
			wp_redirect(site_url()."?page_id=".$page3Id);
			exit;
		}
	}
}
function upgradeToRolesSafe() {
    try {
        upgradeToRoles();
    } catch(Exception $e) {
    } catch(Error $e) {
    }
}
add_action("init", "upgradeToRolesSafe");

function checkAccess( $post_object ) {
	if(!is_singular() || !in_the_loop()) return;

	$role2Name = 'SN2';
	$role3Name = 'SN3';

	$page2Id = 3083;
	$page3Id = 3588;

	if($post_object->ID != $page2Id && $post_object->ID != $page3Id){
		return;
	}

	$userRoles = null;
	try {
		$userRoles = hyperIdClientController::getAuth()->getUserInfo()->user_roles;
	} catch(Exception $e) {
		wp_redirect(site_url());
		exit;
	}

	if($post_object->ID == $page2Id) {
		if(!is_admin() && (!$userRoles || !in_array($role2Name, $userRoles))) {
			wp_redirect(site_url());
			exit;
		}
	}
	if($post_object->ID == $page3Id) {
		if(!is_admin() && (!$userRoles || !in_array($role3Name, $userRoles))) {
			wp_redirect(site_url());
			exit;
		}
	}
}
function checkAccessSafe($post) {
	try {
		checkAccess($post);
	} catch(Exception $e) {
	} catch(Error $e) {
	}
}
add_action( 'the_post', 'checkAccessSafe' );

function addContent( $content ) {
	$page1Id = 3074;
	$page2Id = 3083;
	$page3Id = 3588;

	$role2Name = "SN2";
	$role3Name = "SN3";

	$responseRoles = get_transient('project_roles');
	if($responseRoles === false) {
		$responseRoles = hyperIdClientController::getRoleManager()->rolesGet(hyperIdClientController::getSaAccessToken());
		set_transient('project_roles', $responseRoles, 60);
	}
	$roleId2    = '';
	$roleId3    = '';
	if($responseRoles['result'] == RolesGetResult::SUCCESS) {
		$roleId2Ind = array_search($role2Name, array_column($responseRoles['roles'], 'name'));
		if(is_numeric($roleId2Ind)) {
			$roleId2 = $responseRoles['roles'][$roleId2Ind]['id'];
		}
		$roleId3Ind = array_search($role3Name, array_column($responseRoles['roles'], 'name'));
		if(is_numeric($roleId3Ind)) {
			$roleId3 = $responseRoles['roles'][$roleId3Ind]['id'];
		}
	}

	if(is_singular() && in_the_loop() && is_main_query()) {
		if(get_the_ID() == $page1Id) {
			$userRoles = null;
			try {
				$userRoles = hyperIdClientController::getAuth()->getUserInfo()->user_roles;
			} catch(Exception $e) {
				if(!is_admin()) {
					wp_redirect(site_url());
					exit;
				}
			}
			if(!is_admin() && $userRoles && in_array($role2Name, $userRoles)) {
				$content = str_replace("Upgrade to SN2", "Go To Page 2", wp_specialchars_decode($content));
			}
			if(!is_admin() && $userRoles && in_array($role3Name, $userRoles)) {
				$content = str_replace("Upgrade to SN3", "Go To Page 3", wp_specialchars_decode($content));
			}
			return $content;
		}
		if(get_the_ID() == $page2Id) {
			$attributeName1 = "text1";
			$attributeName2 = "text2";
		
			$responseAttribute1 = hyperIdClientController::getRoleManager()->roleAttributeGet(hyperIdClientController::getSaAccessToken(), $roleId2, $attributeName1);
			$attributeValue1 = $responseAttribute1['result'] == RoleAttributeGetResult::SUCCESS ? $responseAttribute1['value'] : "";
		
			$responseAttribute2 = hyperIdClientController::getRoleManager()->roleAttributeGet(hyperIdClientController::getSaAccessToken(), $roleId2, $attributeName2);
			$attributeValue2 = $responseAttribute2['result'] == RoleAttributeGetResult::SUCCESS ? $responseAttribute2['value'] : "";

			$content = str_replace("<hid_attribute_text_1>", stripslashes($attributeValue1), wp_specialchars_decode($content));
			$content = str_replace("<hid_attribute_text_2>", stripslashes($attributeValue2), wp_specialchars_decode($content));
			return $content;
		}
		if(get_the_ID() == $page3Id) {
			$attributeName1 = "long_text_1";
			$attributeName2 = "long_text_2";
		
			$responseAttribute1 = hyperIdClientController::getRoleManager()->roleAttributeGet(hyperIdClientController::getSaAccessToken(), $roleId3, $attributeName1);
			$attributeValue1 = $responseAttribute1['result'] == RoleAttributeGetResult::SUCCESS ? $responseAttribute1['value'] : "";
		
			$responseAttribute2 = hyperIdClientController::getRoleManager()->roleAttributeGet(hyperIdClientController::getSaAccessToken(), $roleId3, $attributeName2);
			$attributeValue2 = $responseAttribute2['result'] == RoleAttributeGetResult::SUCCESS ? $responseAttribute2['value'] : "";

			$content = str_replace("<hid_attribute_long_text_1>", stripslashes($attributeValue1), wp_specialchars_decode($content));
			$content = str_replace("<hid_attribute_long_text_2>", stripslashes($attributeValue2), wp_specialchars_decode($content));
			return $content;
		}
	}
	return $content;
}
function addContentSafe($content) {
	try {
		return addContent($content);
	} catch(Exception $e) {
	} catch(Error $e) {
	}
}
add_filter( 'the_content', 'addContentSafe' );

function add_login_menu_items() {
	if (is_user_logged_in()) {
		echo '
			<div style="width: 100%; background-color: black;">
				<div style="max-width: var(--wp--style--global--wide-size); margin: auto;">
					<li class="menu-item hid__menu-item" style="margin-left: auto; margin-right: 0; width: fit-content;">
						<a href="' . wp_logout_url( home_url() ) . '">
							<button class="btn__hid btn__hid-logout">
								<span>SIGN OUT</span>
							</button>
						</a>
						<span class="hid__email" style="color: white">' . wp_get_current_user()->user_email . '</span>
					</li>
				</div>
			</div>
		';
	} else {
		echo '
			<div style="width: 100%; background-color: black;">
				<div style="max-width: var(--wp--style--global--wide-size); margin: auto;">
					<li class="menu-item hid__menu-item" style="margin-left: auto; margin-right: 0; width: fit-content;">
						<a href="/wordpress/?hidLoginAction=hidLogin">
							<button class="btn__hid btn__hid-login">
								<span>SIGN IN</span>
							</button>
						</a>
					</li>
				</div>
			</div>
		';
	}
}
add_action('wp_head', 'add_login_menu_items');

function disable_admin_bar_for_users()
{
	if (current_user_can('administrator') || current_user_can('contributor')) {
		show_admin_bar(true);
	} else {
		show_admin_bar(false);
	}
}
add_action('after_setup_theme', 'disable_admin_bar_for_users');