<?php
class TransactionData {
    public string $to;
    public string $chain;
    public ?string $from = null;
    public ?string $value       = null;
    public ?string $data        = null;
    public ?string $gas         = null;
    public ?string $nonce       = null;

    function __construct($addressTo,
                         $chainId,
                         $addressFrom   = null,
                         $value         = null,
                         $data          = null,
                         $gas           = null,
                         $nonce         = null) {
        $this->to       = $addressTo;
        $this->chain    = $chainId;

        if($addressFrom) {
            $this->from = $addressFrom;
        }
        if($value) {
            $this->value = $value;
        }
        if($data) {
            $this->data = $data;
        }
        if($gas) {
            $this->gas = $gas;
        }
        if($nonce) {
            $this->nonce = $nonce;
        }
    }

    function isValid() {
        if(!isHex($this->to)) return false;
        if(!is_numeric($this->chain)) return false;
        if($this->from && !isHex($this->from)) return false;
        if(!$this->value && !$this->data) return false;
        if($this->value && !is_numeric($this->value)) return false;
        if($this->data && !isHex($this->data)) return false;
        return true;
    }

    function toJson() {
        return json_encode(array_filter(get_object_vars($this)));
    }
}

function isHex(?string $hexString) : bool {
    return $hexString && (str_starts_with($hexString, '0x') || str_starts_with($hexString, '0X')) && ctype_xdigit(substr($hexString, 2));
}

?>