<!DOCTYPE html>
<html lang="en">

<head>
    <title>SDK Test App / Role Managment</title>
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="0" />
    <link rel="stylesheet" type="text/css" href="./../styles/styles.css">
</head>

<body style="background: #191E25; color: white;">

<?php
    require_once __DIR__.'/../../hyperid/service/service.php';
    require_once __DIR__.'/../../hyperid/role_manager/role_manager.php';

    session_start();

    $clientId = 'your_client_id';
    $clientSecret = 'your_client_secret';
    $service = null;
    if(!isset($_SESSION['service'])) {
        $serviceInfo = new ServiceInfoBasic($clientId, $clientSecret);
        try {
            $service = new Service($serviceInfo, '', InfrastructureType::SANDBOX);
            $_SESSION['service'] = $service;
        } catch(Exception $e) {
            echo '<h1>Service not started</h1>';
            return;
        }
    } else {
        if($_SESSION['service'] instanceof Service) {
            $service = $_SESSION['service'];
        }
    }
    $authMethod     = $service->serviceInfo->authMethod->name;
    $isSelfSigned   = $service->serviceInfo->isSelfSigned;

    $roleManager = null;
    if(!isset($_SESSION['roleManager'])) {
        $roleManager = new RoleManager($service->discover->restApiTokenEndpoint);
        $_SESSION['roleManager'] = $roleManager;
    } else {
        if($_SESSION['roleManager'] instanceof RoleManager) {
            $roleManager = $_SESSION['roleManager'];
        }
    }
    $clientId = $service->serviceInfo->clientId;

    if (isset($_POST['destroy_session'])) {
        $service->logout();
        session_destroy();
        echo '<meta http-equiv="refresh" content="0">';
        return;
    }

    $isRefreshTokens = false;
    if (isset($_POST['refreshTokens']) && $service) {
        $service->refreshTokens();
        $isRefreshTokens    = true;
        $_SESSION["postdata"] = array('isRefreshTokens' => $isRefreshTokens);
        header( "Location: {$_SERVER['REQUEST_URI']}", true, 303 );
        exit();
    }
    if(isset($_SESSION['postdata']['isRefreshTokens'])) {
        $isRefreshTokens = $_SESSION['postdata']['isRefreshTokens'];
        unset($_SESSION['postdata']);
    }

    $accessToken  = $service->getAccessToken();
    $refreshToken = $service->getRefreshToken();

    $refreshTokenService = '';
    try {
        if($service) {
            $refreshTokenService = $service->getRefreshToken();
        }
    } catch (Exception $e) {
        echo "<h1>$e</h1>";
        return;
    }
    $accessTokenService = '';
    try {
        if($service) {
            $accessTokenService = $service->getAccessToken();
        }
    } catch (Exception $e) {
        echo "<h1>$e</h1>";
        return;
    }

    $roleIdCreate = '';
    $roleNameCreate = '';
    $resultCreate = null;
    if (isset($_POST['roleCreate']) && $service && $roleManager) {
        $roleNameCreate = $_POST['roleName'];
        if(!empty($roleNameCreate)) {
            try {
                $responseCreate = $roleManager->roleCreate($service->getAccessToken(), $roleNameCreate);
                $resultCreate   = $responseCreate['result'];
                $roleIdCreate   = $resultCreate == RoleCreateResult::SUCCESS ? $responseCreate['role_id'] : '';
            } catch (Exception $e) {
                echo "<h1>$e</h1>";
            }
            $_SESSION["postdata"] = array('roleIdCreate' => $roleIdCreate, 'roleNameCreate' => $roleNameCreate, 'resultCreate' => $resultCreate);
        }
        header( "Location: {$_SERVER['REQUEST_URI']}", true, 303 );
        exit();
    }

    $roleList = null;
    $resultGet = null;
    if (isset($_POST['rolesGet']) && $service && $roleManager) {
        try {
            $responseGet    = $roleManager->rolesGet($service->getAccessToken());
            $resultGet      = $responseGet['result'];
            $roleList       = $resultGet == RolesGetResult::SUCCESS ? $responseGet['roles'] : array();
        } catch (Exception $e) {
            echo "<h1>$e</h1>";
        }
        $_SESSION["postdata"] = array('roleList' => $roleList, 'resultGet' => $resultGet);
        header( "Location: {$_SERVER['REQUEST_URI']}", true, 303 );
        exit();
    }

    $roleIdDelete = '';
    $resultDelete = null;
    if (isset($_POST['roleDelete']) && $service && $roleManager) {
        $roleIdDelete = $_POST['roleId'];
        if(!empty($roleIdDelete)) {
            try {
                $responseDelete = $roleManager->roleDelete($service->getAccessToken(), $roleIdDelete);
                $resultDelete   = $responseDelete['result'];
            } catch (Exception $e) {
                echo "<h1>$e</h1>";
            }
            $_SESSION["postdata"] = array('roleIdDelete' => $roleIdDelete, 'resultDelete' => $resultDelete);
        }
        header( "Location: {$_SERVER['REQUEST_URI']}", true, 303 );
        exit();
    }

    $userIdAttach = '';
    $roleIdAttach = '';
    $resultAttach = null;
    if (isset($_POST['roleAttach']) && $service && $roleManager) {
        $userIdAttach = $_POST['userId'];
        $roleIdAttach = $_POST['roleId'];
        if(!empty($userIdAttach) && !empty($roleIdAttach)) {
            try {
                $responseAttach = $roleManager->userRoleAttach($service->getAccessToken(), $userIdAttach, '', $roleIdAttach);
                $resultAttach   = $responseAttach['result'];
            } catch (Exception $e) {
                echo "<h1>$e</h1>";
            }
            $_SESSION["postdata"] = array('userIdAttach' => $userIdAttach, 'roleIdAttach' => $roleIdAttach, 'resultAttach' => $resultAttach);
        }
        header( "Location: {$_SERVER['REQUEST_URI']}", true, 303 );
        exit();
    }

    $userIdDetach = '';
    $roleIdDetach = '';
    $resultDetach = null;
    if (isset($_POST['roleDetach']) && $service && $roleManager) {
        $userIdDetach = $_POST['userId'];
        $roleIdDetach = $_POST['roleId'];
        if(!empty($userIdDetach) && !empty($roleIdDetach)) {
            try {
                $responseDetach = $roleManager->userRoleDetach($service->getAccessToken(), $userIdDetach, $roleIdDetach);
                $resultDetach   = $responseDetach['result'];
            } catch (Exception $e) {
                echo "<h1>$e</h1>";
            }
            $_SESSION["postdata"] = array('userIdDetach' => $userIdDetach, 'roleIdDetach' => $roleIdDetach, 'resultDetach' => $resultDetach);
        }
        header( "Location: {$_SERVER['REQUEST_URI']}", true, 303 );
        exit();
    }

    $roleIdUsers    = '';
    $pageOffset     = 0;
    $pageSize       = 10;
    $resultUsers    = null;
    $userIds        = null;
    $nextPageOffset = 0;
    $nextPageSize   = 0;
    if (isset($_POST['usersByRole']) && $service && $roleManager) {
        $roleIdUsers = $_POST['roleId'];
        if(!empty($_POST['pageOffset'])) $pageOffset  = intval($_POST['pageOffset']);
        if(!empty($_POST['pageSize']))   $pageSize    = intval($_POST['pageSize']);
        if(!empty($roleIdUsers)) {
            try {
                $responseUsers  = $roleManager->usersByRoleGet($service->getAccessToken(), $roleIdUsers, $pageOffset, $pageSize);
                $resultUsers    = $responseUsers['result'];
                $userIds        = $resultUsers == UsersByRoleGetResult::SUCCESS ? $responseUsers['user_ids']            : array();
                $nextPageOffset = $resultUsers == UsersByRoleGetResult::SUCCESS ? $responseUsers['next_page_offset']    : 0;
                $nextPageSize   = $resultUsers == UsersByRoleGetResult::SUCCESS ? $responseUsers['next_page_size']      : 0;
            } catch (Exception $e) {
                echo "<h1>$e</h1>";
            }
            $_SESSION["postdata"] = array('roleIdUsers'     => $roleIdUsers,
                                          'resultUsers'     => $resultUsers,
                                          'userIds'         => $userIds,
                                          'nextPageOffset'  => $nextPageOffset,
                                          'nextPageSize'    => $nextPageSize);
        }
        header( "Location: {$_SERVER['REQUEST_URI']}", true, 303 );
        exit();
    }
?>

    <table style="width: 90%; margin: auto;">
        <tr style="vertical-align: top;">
            <th style="width: 50%;">
                <h2 style="color: white; margin: auto;">
                    Service account
                </h2>
                <p>
                    ClientId : <?php echo "$clientId"?> |
                    Auth Mechod : <?php echo "$authMethod"?> |
                    Is Self Signed : <?php var_export($isSelfSigned)?> |
                </p>
                <form action="" method="post">
                    <input type="hidden" name="destroy_session" value="1"/>
                    <button type="submit" class="button-custom" style="width: 80%;">
                        Destroy test session
                    </button>
                </form>
                <br>
                <button type="button" class="button-custom" style="width: 80%;" onclick="goToAuth();">
                    Go to Auth
                </button>
                <br>
                <?php
                    if(!$isSelfSigned) {
                ?>
                    <br>
                    <?php
                        if($refreshTokenService) {
                    ?>
                        <form action="" method="post">
                            <input type="hidden" name="refreshTokens" value="1"/>
                            <button type="submit" class="button-custom" style="width: 80%;">
                                Refresh Tokens
                            </button>
                        </form>
                    <?php } else { ?>
                        <form action="" method="post">
                            <input type="hidden" name="refreshTokens" value="1"/>
                            <button type="submit" disabled class="button-custom" style="width: 80%;">
                                Refresh Tokens
                            </button>
                            <div style="color: orange; margin-left: 10px;">
                                Seems like there are problems on server -_-
                            </div>
                        </form>
                    <?php } ?>
                <?php } ?>

                <br>
                <?php
                if($accessTokenService) {
                ?>
                    <button id="tokensBtn" type="button" class="button-custom" style="width: 80%;" onclick="toggleShowTokens();">
                        <?php if($isRefreshTokens) {
                        echo "Hide Tokens";
                        } else {
                        echo "Show Tokens";
                        } ?>
                    </button>
                    <br>
                <?php } else { ?>
                    <form action="" method="get">
                        <input type="hidden" name="showTokens" value="1"/>
                        <button disabled type="submit" class="button-custom" style="width: 80%;">
                            Show Tokens
                        </button>
                        <div style="color: orange; margin-left: 10px;">
                            Seems like there are problems on server -_-
                        </div>
                    </form>
                    <br>
                <?php } ?>

                <br>
                <div id="tokensInfo"
                    <?php if(!$isRefreshTokens) {
                        ?>style="display: none; width: 80%; margin: auto;"
                    <?php } else {
                        ?>style="display: block; width: 80%; margin: auto;"
                    <?php } ?>>
                    <p>==================================================================</p>
                    <?php if(!$isSelfSigned) { ?>
                        <p>refresh_token:</p><p><?php echo $refreshToken ?></p>
                    <?php } ?>
                    <p>access_token:</p><p><?php echo $accessToken ?></p>
                    <p>==================================================================</p>
                </div>
            </th>
            <th style="width: 50%;">
                <h2 style="color: white; margin: auto;">
                    Role Manager
                </h2>
                <br>
                <?php
                if(isset($_SESSION['postdata']) && !empty($_SESSION['postdata']['roleIdCreate'])) {
                    $resultCreate   = $_SESSION['postdata']['resultCreate'];
                    $roleNameCreate = $_SESSION['postdata']['roleNameCreate'];
                    $roleIdCreate   = $_SESSION['postdata']['roleIdCreate'];
                    echo "Result: ".$resultCreate->name;
                    echo "<br>";
                    echo "Role Name: ".$roleNameCreate;
                    echo "<br>";
                    echo "Role Id: ".$roleIdCreate;
                    unset($_SESSION['postdata']);
                }
                ?>
                <form action="" method="post" style="margin-top: 8px;">
                    <input type="hidden"    name="roleCreate"   value="1"/>
                    <input type="text"      name="roleName"     value="" style="width: 80%;" class="inputField" placeholder="Input role name"/>
                    <button type="submit" class="button-custom" style="width: 80%;">
                        Role Create
                    </button>
                </form>
                <br>
                <?php
                if(isset($_SESSION['postdata']) && isset($_SESSION['postdata']['roleList'])) {
                    $roleList = $_SESSION['postdata']['roleList'];
                    $resultGet = $_SESSION['postdata']['resultGet'];
                    echo "Result: ".$resultGet->name;
                    echo "<br>";
                    if(!empty($roleList)) {
                        echo "Roles:";
                        echo "<table style='width: 80%; margin: auto'>";
                        foreach ($roleList as $roleItem) {
                            echo "<tr>";
                                echo "<td> | </td>";
                            foreach ($roleItem as $key => $value) {
                                echo "<td>$key : </td><td>$value</td>";
                                echo "<td> | </td>";
                            }
                            echo "</tr>";
                        }
                        echo "</table>";
                    }
                    unset($_SESSION['postdata']);
                }
                ?>
                <form action="" method="post" style="margin-top: 8px;">
                    <input type="hidden" name="rolesGet" value="1"/>
                    <button type="submit" class="button-custom" style="width: 80%;">
                        Roles Get
                    </button>
                </form>
                <br>
                <?php
                if(isset($_SESSION['postdata']) && !empty($_SESSION['postdata']['roleIdDelete'])) {
                    $roleIdDelete = $_SESSION['postdata']['roleIdDelete'];
                    $resultDelete = $_SESSION['postdata']['resultDelete'];
                    echo "Result: ".$resultDelete->name;
                    echo "<br>";
                    echo "Role Id: ".$roleIdDelete;
                    unset($_SESSION['postdata']);
                }
                ?>
                <form action="" method="post" style="margin-top: 8px;">
                    <input type="hidden" name="roleDelete" value="1"/>
                    <input type="text"   name="roleId"     value="" style="width: 80%;" class="inputField" placeholder="Input role id"/>
                    <button type="submit" class="button-custom" style="width: 80%;">
                        Role Delete
                    </button>
                </form>
                <br>
                <?php
                if(isset($_SESSION['postdata']) && !empty($_SESSION['postdata']['roleIdAttach'])) {
                    $roleIdAttach = $_POST['roleIdAttach'];
                    $userIdAttach = $_POST['userIdAttach'];
                    $resultAttach = $_POST['resultAttach'];
                    echo "Result: ".$resultAttach->name;
                    echo "<br>";
                    echo "Role Id: ".$roleIdAttach;
                    echo "<br>";
                    echo "User Id: ".$userIdAttach;
                    unset($_SESSION['postdata']);
                }
                ?>
                <form action="" method="post" style="margin-top: 8px;">
                    <input type="hidden" name="roleAttach" value="1"/>
                    <input type="text"   name="userId"     value="" class="inputField" placeholder="Input user id" style="width: 80%;"/>
                    <input type="text"   name="roleId"     value="" class="inputField" placeholder="Input role id" style="width: 80%; margin-top: 8px"/>
                    <button type="submit" class="button-custom" style="width: 80%;">
                        Role Attach To User
                    </button>
                </form>
                <br>
                <?php
                if(isset($_SESSION['postdata']) && !empty($_SESSION['postdata']['roleIdDetach'])) {
                    $roleIdDetach = $_POST['roleIdDetach'];
                    $userIdDetach = $_POST['userIdDetach'];
                    $resultDetach = $_POST['resultDetach'];
                    echo "Result: ".$resultDetach->name;
                    echo "<br>";
                    echo "Role Id: ".$roleIdDetach;
                    echo "<br>";
                    echo "User Id: ".$userIdDetach;
                    unset($_SESSION['postdata']);
                }
                ?>
                <form action="" method="post" style="margin-top: 8px;">
                    <input type="hidden" name="roleDetach" value="1"/>
                    <input type="text"   name="userId"     value="" class="inputField" placeholder="Input user id" style="width: 80%;"/>
                    <input type="text"   name="roleId"     value="" class="inputField" placeholder="Input role id" style="width: 80%; margin-top: 8px"/>
                    <button type="submit" class="button-custom" style="width: 80%;">
                        Role Detach From User
                    </button>
                </form>
                <br>
                <?php
                if(isset($_SESSION['postdata']) && !empty($_SESSION['postdata']['roleIdUsers'])) {
                    $roleIdUsers = $_SESSION['postdata']['roleIdUsers'];
                    $nextPageOffset = $_SESSION['postdata']['nextPageOffset'];
                    $nextPageSize = $_SESSION['postdata']['nextPageSize'];
                    $userIds = $_SESSION['postdata']['userIds'];
                    $resultUsers = $_SESSION['postdata']['resultUsers'];
                    echo "Result: ".$resultUsers->name;
                    echo "<br>";
                    echo "Role Id: ".$roleIdUsers;
                    if(!empty($userIds)) {
                        if(count($userIds) == $nextPageSize) {
                            echo "<br>";
                            echo "Next page offset: ".$nextPageOffset;
                            echo "<br>";
                            echo "Next page size: ".$nextPageSize;
                        }
                        echo "<br>";
                        echo "Users id:";
                        echo "<br>";
                        foreach ($userIds as $userId) {
                            echo "$userId";
                            echo "<br>";
                        }
                    }
                    unset($_SESSION['postdata']);
                }
                ?>
                <form action="" method="post" style="margin-top: 8px;">
                    <input type="hidden" name="usersByRole" value="1"/>
                    <input type="text"   name="roleId"      value="" class="inputField" placeholder="Input role id"               style="width: 80%;"/>
                    <input type="number" name="pageOffset"  value="" class="inputField" placeholder="Input page offset (def : 0)" style="width: 80%; margin-top: 8px" min="0"/>
                    <input type="number" name="pageSize"    value="" class="inputField" placeholder="Input page size (def : 10)"  style="width: 80%; margin-top: 8px" min="10" max="100"/>
                    <button type="submit" class="button-custom" style="width: 80%;">
                        Users By Role Get
                    </button>
                </form>
            </th>
        </tr>
    </table>
    <script>
        <?php
        $url = '';
        if (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] == 'on') {
            $url.='https://';
        } else {
            $url.='http://';
        }
        $url.=$_SERVER['HTTP_HOST'];
        ?>
        let tokensShow = <?php var_export($isRefreshTokens)?>;
        function goToAuth() {
            window.location.href = "<?php echo $url?>";
        }
        function toggleShowTokens() {
            if(tokensShow) {
                tokensInfo.style.display = "none";
                tokensBtn.innerHTML = "Show Tokens"
                tokensShow = false;
            } else {
                tokensInfo.style.display = "block";
                tokensBtn.innerHTML = "Hide Tokens"
                tokensShow = true;
            }
        }
    </script>
</body>

</html>