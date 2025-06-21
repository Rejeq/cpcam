package com.rejeq.cpcam.core.common

interface CodeVerifier {
    fun verifyCode(value: String): Boolean
}

interface QrScannableComponent {
    fun handleQrCode(value: String)
}
