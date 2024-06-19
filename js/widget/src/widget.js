var sdk = null;
var externalScript = document.createElement('script');

const subscribeAsync = async (globSdk, containerId, flowMode, it) => {
    globSdk.on('tokensChanged', async function(at, rt)  {
        localStorage.setItem('access_token', at);
        localStorage.setItem('refresh_token', rt);
        const container = document.getElementById(containerId);
        const loginButton = document.getElementById('hyper-id-login');
        if (loginButton) {
            container.removeChild(loginButton);
            try {
                await createUserWidget(container, flowMode, it);
            } catch(e) {
                console.error(e);
                createSignInButton(container, flowMode);
            }
        }
    });
    try {
        const rt = localStorage.getItem('refresh_token');
        if(!rt || rt === undefined || rt === 'undefined') {
            await globSdk.init()
        } else {
            await globSdk.init(rt);
        }
    } catch(e) {
        console.error(e)
    }
    await initButton(containerId, flowMode);
    document.addEventListener('click', (event) => {
        const hidWidgetMain = document.getElementById('hid-widget-main');
        const hidWidgetBar = document.getElementById('hid-widget-bar');
        if(!hidWidgetMain) return;

        if (event.target !== hidWidgetMain && !hidWidgetMain.contains(event.target) &&
            event.target !== hidWidgetBar && !hidWidgetBar.contains(event.target)) {
            hidWidgetBar.style.display = 'none';
        }
    })
}

function init() {
	const clientId = document.currentScript.getAttribute('data-client-id');
	const clientSecret = document.currentScript.getAttribute('data-client-secret');
	const redirectUrl = document.currentScript.getAttribute('data-redirect-url');
	const containerId = document.currentScript.getAttribute('data-container-id');
    const flowMode = document.currentScript.getAttribute('data-flow-mode')
    const infrType = document.currentScript.getAttribute('data-infrastructure-type')
    const cssStyles = document.currentScript.getAttribute('data-css-styles')

	externalScript = document.createElement('script');
	externalScript.src = 'https://cdnpublicstorage.blob.core.windows.net/scripts/hyperIdSdk.js';
    document.head.appendChild(externalScript);
	externalScript.onload = async function() {
		let clientInfo = new clientInfoBasic(clientId,
                                             clientSecret,
                                             redirectUrl);
        let it = infrastructureType.PRODUCTION;
        if(infrType === 'sandbox') {
            it = infrastructureType.SANDBOX;
        }
        sdk = new hyperIdSdk(clientInfo, it);
        await subscribeAsync(sdk, containerId, flowMode, it);
        craeteStyles(cssStyles);
        window.onload = sdk.auth.handleOAuthCallback();
        window.hyperId = sdk;
	};
}

function createWidgetElement(type, id=null, classList=null, textContent=null, style=null) {
    const el = document.createElement(type);
    if(classList) { el.classList = classList; }
    if(id) { el.id = id; }
    if(textContent) { el.textContent = textContent;}
    if(style) { el.style = style;}
    return el;
}

async function createSignInButton(container, flowMode) {
    const buttonContainer = createWidgetElement('button', 'hyper-id-login', 'hyperid-widget-btn', 'Sign In', 'width: fit-content;');
    buttonContainer.addEventListener('click', async function () {
        let src = "";
        if(flowMode && flowMode == 'web3') {
            src = sdk.auth.startSignInWeb3();
        } else {
            src = sdk.auth.startSignInWeb2();
        }
        window.location.href = src;
    });
    container.appendChild(buttonContainer);
}

async function createUserWidget(container, flowMode, it) {
    const hidWidgetDiv = createWidgetElement('div', 'hid-widget-main', 'hyperid-widget-profile-wrapper');
    hidWidgetDiv.addEventListener('click', async () => {
        let hidWidget = document.getElementById("hid-widget-bar");
        if(hidWidget) {
            hidWidget.style.display = "flex";
        }
    });

    const hidWidgetProfileDiv = document.createElement('div');
    hidWidgetProfileDiv.classList = "hyperid-widget-profile";

    const infoWrapperDiv = document.createElement('div');
    infoWrapperDiv.classList = "hyperid-info-wrapper";

    hidWidgetProfileDiv.appendChild(infoWrapperDiv);
    const loginUserNameP = document.createElement('p');
    loginUserNameP.classList = "hyperid-login-username-title";

    let userInfo = null;
    try {
        userInfo = await sdk.auth.userInfo();
    } catch(e) { console.error(e);
        createSignInButton(container, flowMode);
        return;
    }
    
    loginUserNameP.textContent = "Unknown";
    const isGuest = userInfo.is_guest;
    if(userInfo) {
        if(isGuest) {
            loginUserNameP.textContent = "Guest"
        } else {
            const email = userInfo.email;
            const emailLenght = email.length;
            loginUserNameP.textContent = email;
            if(emailLenght > 18) {
                const atIndex = email.indexOf('@');
                const localPart = email.substring(0, atIndex);
                const hiddenLocalPart = localPart.substring(0, 4) + '...' + localPart.substring(localPart.length - 4, localPart.length);
                const domain = email.substring(atIndex);
                const part = hiddenLocalPart + domain;
                if(part.length <= 18) {
                    loginUserNameP.textContent = part;
                } else {
                    const domainHiddenPart = domain.substring(0, 3) + '...' + domain.substring(domain.length - 3, domain.length);
                    loginUserNameP.textContent = hiddenLocalPart + domainHiddenPart;
                }

            }
        }

        if(flowMode === 'web3') {
            const userWalletAddressP = document.createElement('p');
            userWalletAddressP.classList = "hyperid-login-user-wallet"; 
            const pref = userInfo.wallet.address.substring(0, 8);
            const suf = userInfo.wallet.address.substring(userInfo.wallet.address.length - 6);
            userWalletAddressP.textContent = `${pref}...${suf}`;
            infoWrapperDiv.appendChild(userWalletAddressP);
        }
    }
    infoWrapperDiv.appendChild(loginUserNameP);

    const profileIconDiv = document.createElement('div');
    profileIconDiv.classList = "hyperid-profile-icon";
    const hidWidgetBarDiv = createWidgetElement('div', 'hid-widget-bar', 'hyperid-widget', null, 'display: none;');
    if(userInfo) {
        let kycStatus = null;
        try {
            kycStatus = await sdk.kyc.getUserStatusTopLevel(localStorage.getItem('access_token'));
        } catch(e) {}
        const userKyc = document.createElement('p');
        userKyc.classList = "hyperid-login-user-kyc-title";
        userKyc.textContent = "KYC " + "not passed";
        if(kycStatus) {
            switch (kycStatus.user_status) {
                case kycUserStatus.PENDING: userKyc.textContent = "KYC " + "is pending"; break;
                case kycUserStatus.COMPLETE_SUCCESS: {
                    if(kycStatus.verification_level === verificationLevel.KYC_BASIC) {
                        userKyc.textContent = "KYC " + "passed, basic";
                    } else if(kycStatus.verification_level === verificationLevel.KYC_FULL) {
                        userKyc.textContent = "KYC " + "passed, full";
                    }
                } break;
                case kycUserStatus.COMPLETE_FAIL_RETRYABLE: userKyc.textContent = "KYC " + "fail, can retry"; break;
                case kycUserStatus.COMPLETE_FAIL_FINAL: userKyc.textContent = "KYC " + "fail, no retry"; break;
                case kycUserStatus.DELETED: userKyc.textContent = "KYC " + "deleted"; break;
            }
        }
        hidWidgetBarDiv.appendChild(userKyc);
    }
    const settingsButtonDiv = createWidgetElement('div', null, 'hyperid-inside-btn', null, 'padding-bottom: 16px');
    
    if(isGuest) {
        const guestUpgradeButton = createWidgetElement('button', null, 'hyperid-widget-btn', 'Upgrade');
        guestUpgradeButton.addEventListener('click', async () => {
            let src = sdk.auth.startSingInGuestUpgrade();
            window.location.href = src;
        });
        settingsButtonDiv.appendChild(guestUpgradeButton);
    } else {
        const settingsButton = createWidgetElement('button', null, 'hyperid-widget-btn', 'Settings');
        settingsButton.addEventListener('click', async () => {
            const url = sdk.auth.getDiscover().issuer + "/account/";
            window.open(url, '_blank');
        });
        settingsButtonDiv.appendChild(settingsButton);
    }
    
    if(flowMode === 'web3') {
        const sources = [1,2,3,5];
        if(sources.includes(userInfo.wallet.source)) {
            const openWalletAddress = createWidgetElement('button', null, 'hyperid-widget-btn', 'Open Wallet');
            openWalletAddress.addEventListener('click', async () => {
                if(userInfo.wallet.source === 1) {
                    if (window.ethereum && window.ethereum.isMetaMask) {
                        await window.ethereum.request({ method: 'wallet_requestPermissions',  params: [{ eth_accounts: {} }]});
                      }
                }
                if(userInfo.wallet.source === 2) {
                    if (window.solana && window.solana.isPhantom) {
                        await window.solana.connect();
                      }
                }
                if(userInfo.wallet.source === 3 || userInfo.wallet.source === 5) {
                    if(it === 'sandbox') {
                        window.open("https://app-sandbox.cyberwallet.ai", '_blank');
                    } else {
                        window.open("https://app.cyberwallet.ai", '_blank');
                    }
                    
                }
            });

            settingsButtonDiv.appendChild(openWalletAddress);
        }

        const changeWalletButton = createWidgetElement('button', null, 'hyperid-widget-btn', 'Change Wallet');
        changeWalletButton.addEventListener('click', async () => {
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            let src = sdk.auth.startSignInWalletGet(walletGetMode.WALLET_GET_FULL);
            window.location.href = src;
        });
        settingsButtonDiv.appendChild(changeWalletButton);

        const copyWalletButton = createWidgetElement('button', null, 'hyperid-widget-btn', 'Copy address');
        copyWalletButton.addEventListener('click', async () => {
            navigator.clipboard.writeText(userInfo.wallet.address);
        });
        settingsButtonDiv.appendChild(copyWalletButton);
    }

    const signOutButtonDiv = document.createElement('div');
    signOutButtonDiv.classList = "hyperid-inside-btn";
    const signOutButton = createWidgetElement('button', null, 'hyperid-widget-btn-logout', 'Sign Out');
    signOutButton.addEventListener('click', async () => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        await sdk.auth.logout();
        container.removeChild(hidWidgetDiv);
        await createSignInButton(container, flowMode);
    });

    signOutButtonDiv.appendChild(signOutButton);
    hidWidgetBarDiv.appendChild(settingsButtonDiv);
    hidWidgetBarDiv.appendChild(signOutButtonDiv);
    hidWidgetProfileDiv.appendChild(profileIconDiv);
    hidWidgetDiv.appendChild(hidWidgetProfileDiv);
    hidWidgetDiv.appendChild(hidWidgetBarDiv);
    container.appendChild(hidWidgetDiv);
}

async function initButton(containerId, flowMode) {
    const container = document.getElementById(containerId);
    if(!container) return;

    if(container) {
        let isInit = await isInitialized();
        if(isInit) {
            await createUserWidget(container, flowMode);
        } else {
            await createSignInButton(container, flowMode);
        }
    }
}

async function isInitialized() {
    let isExpired = await sdk.auth.isTokenExpired(localStorage.getItem('access_token'));
    return isExpired ? false : true;
}

function craeteStyles(cssStyle) {
	const style = document.createElement('style');
	let baseStyles = `
        @import url('https://fonts.googleapis.com/css2?family=Barlow:wght@400;500;600;700&display=swap');
        .hyperid-inside-btn {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            justify-content: space-between;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-widget-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #75CBDC;
            border: 2px solid #75CBDC;
            border-radius: 24px;
            font-weight: 600;
            outline: none;
            padding: 8px 12px;
            font-size: 16px;
            width: 100%;
            cursor: pointer;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-widget-btn-logout {
            border: 2px solid #75CBDC;
            border-radius: 24px;
            color: #75CBDC;
            background-color: transparent;
            font-size: 16px;
            font-weight: 600;
            padding: 8px 16px;
            width: 100%;
            cursor: pointer;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-widget-btn:hover,
        .hyperid-widget-btn-logout:hover {
            color: #75CBDC;
            background: rgba(117, 203, 220, 0.20);
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-info-wrapper{
            display: flex;
            flex-direction: column;
            min-width: 0;
            text-align: end;
            width: 140px;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-login-username-title {
            color: grey;
            font-size: 16px;
            margin-block: 0px;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-login-user-wallet {
            font-size: 16px;
            font-weight: 500;
            margin-block: 0px;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-widget-profile-wrapper {
            max-width: 200px;
            position: relative;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-widget-profile p {
            white-space: nowrap;
            overflow: hidden;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-widget-profile {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            color: white;
            cursor: pointer;
            width: 180px;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-profile-icon {
            margin-left: 8px;
            max-width: 30px;
            width: 100%;
            height: 30px;
            background-image: url(https://cdnpublicstorage.blob.core.windows.net/picts/profile.svg);
            background-size: cover;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-widget {
            width: 180px;
            padding: 16px;
            flex-direction: column;
            background-color: #21262E;
            border-top-left-radius: 12px;
            border-bottom-left-radius: 12px;
            border-bottom-right-radius: 12px;
            border: 1px solid #313C45;
            position: absolute;
            right: 0;
            top: 50px;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
        .hyperid-login-user-kyc-title {
            outline: none;
            border: none;
            background: none;
            color: white;
            font-size: 12px;
            margin-block-start: 0px;
            margin-block-end: 0px;
            text-align: center;
            padding-bottom: 10px;
            font-family: 'Barlow', sans-serif;
            box-sizing: border-box;
        }
	`.replace(/^\s+|\n/gm, '');

    if(cssStyle) {
        const classNames = cssStyle.match(/\.\s*([^\s{]+)/g);
        classNames.forEach(className => {
            const regex = new RegExp(`\\${className}\\s*{[^}]*}`);
            const match = baseStyles.match(regex);
            const matchGiven = cssStyle.match(regex);
            if (match) {
                baseStyles = baseStyles.replace(regex, matchGiven);
            }
        });
    }
    style.innerHTML = baseStyles;
	document.head.appendChild(style);
}
init()