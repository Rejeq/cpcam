set(MBEDTLS_PREFIX_DIR "${DEPS_DIR}/mbedtls")

function(MakeMbedtlsLib target)
    add_library(mbedtls_${target} STATIC IMPORTED)
    add_library(MbedTLS::${target} ALIAS mbedtls_${target})
    set_property(TARGET mbedtls_${target} PROPERTY IMPORTED_LOCATION "${MBEDTLS_PREFIX_DIR}/lib/${target}.a")
    target_include_directories(mbedtls_${target} INTERFACE ${MBEDTLS_PREFIX_DIR}/include)
endfunction()

MakeMbedtlsLib(libmbedtls)
MakeMbedtlsLib(libmbedcrypto)
MakeMbedtlsLib(libmbedx509)
