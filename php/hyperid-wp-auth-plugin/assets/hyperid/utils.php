<?php

require_once __DIR__.'/base/enum.php';

function httpGet(string $url, int $timeout = 0) : array {
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER,    true);
    curl_setopt($ch, CURLOPT_HEADER,            0);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST,    2);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER,    0);
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT,    $timeout);
    curl_setopt($ch, CURLOPT_TIMEOUT,           $timeout);
    try {
        $response = curl_exec($ch);
    } catch(Exception $e) {
        return ['response' => [], 'status' => 500];
    }
    $response_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return ['response' => json_decode($response, true), 'status' => $response_status];
}

function httpPost($url, array $params = [], $custom_headers = false, int $timeout = 0) : array {
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST,              1);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER,    true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST,    2);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER,    0);
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT,    $timeout);
    curl_setopt($ch, CURLOPT_TIMEOUT,           $timeout);
    curl_setopt($ch, CURLOPT_POSTFIELDS,        $params ? http_build_query($params) : "");
    curl_setopt($ch, CURLOPT_ENCODING,          '');
    curl_setopt($ch, CURLOPT_TCP_FASTOPEN,      true);
    if ($custom_headers) {
      curl_setopt($ch, CURLOPT_HTTPHEADER, $custom_headers);
    }
    try {
        $response = curl_exec($ch);
    } catch(Exception $e) {
        return ['response' => [], 'status' => 500];
    }
    $response_status    = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return ['response' => json_decode($response, true), 'status' => $response_status];
}

function httpPostJSON($url, array $params = [], array $custom_headers = [], int $timeout = 0) : array {
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST,              1);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER,    true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST,    2);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER,    0);
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT,    $timeout);
    curl_setopt($ch, CURLOPT_TIMEOUT,           $timeout);
    curl_setopt($ch, CURLOPT_POSTFIELDS,        $params ? json_encode($params) : "");
    curl_setopt($ch, CURLOPT_ENCODING,          '');
    curl_setopt($ch, CURLOPT_TCP_FASTOPEN,      true);

    $headers[] = 'Content-Type: application/json';
    if ($custom_headers) {
        array_push($headers, ...$custom_headers);
    }
    curl_setopt($ch, CURLOPT_HTTPHEADER, $custom_headers);
    try {
        $response = curl_exec($ch);
    } catch(Exception $e) {
        return ['response' => [], 'status' => 500];
    }
    $response_status    = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return ['response' => json_decode($response, true), 'status' => $response_status];
}

function generateJWT(array $payload, AuthorizationMethod $alg, string $secret) : string {
    switch($alg) {
        case AuthorizationMethod::HS256 : 
            $headers = ['alg'=>'HS256','typ'=>'JWT'];
            $headers_encoded = base64UrlEncode(json_encode($headers));
            $payload_encoded = base64UrlEncode(json_encode($payload));
            $signature = hash_hmac('sha256',"$headers_encoded.$payload_encoded", $secret, true);
            $signature_encoded = base64UrlEncode($signature);
            return "$headers_encoded.$payload_encoded.$signature_encoded";
            break;
        case AuthorizationMethod::RS256 : 
            $headers = ['alg'=>'RS256','typ'=>'JWT'];
            $headers_encoded = base64UrlEncode(json_encode($headers));
            $payload_encoded = base64UrlEncode(json_encode($payload));
            openssl_sign("$headers_encoded.$payload_encoded", $signature, $secret, 'sha256WithRSAEncryption'); 
            $signature_encoded = base64UrlEncode($signature);
            return "$headers_encoded.$payload_encoded.$signature_encoded";
    }
}

function base64UrlEncode($text) : string {
    return str_replace(
        ['+', '/', '='],
        ['-', '_', ''],
        base64_encode($text)
    );
}

function milliseconds() {
    $mt = explode(' ', microtime());
    return intval( $mt[1] * 1E3 ) + intval( round( $mt[0] * 1E3 ) );
}

?>