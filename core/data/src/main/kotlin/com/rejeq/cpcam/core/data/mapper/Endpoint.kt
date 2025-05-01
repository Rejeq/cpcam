package com.rejeq.cpcam.core.data.mapper

import com.rejeq.cpcam.core.data.model.EndpointType
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.data.datastore.EndpointTypeProto
import com.rejeq.cpcam.data.datastore.ObsConfigProto

fun EndpointTypeProto?.fromDataStore() = when (this) {
    null,
    EndpointTypeProto.UNRECOGNIZED,
    EndpointTypeProto.ENDPOINT_TYPE_UNSPECIFIED,
    -> {
        EndpointType.OBS
    }

    EndpointTypeProto.ENDPOINT_TYPE_OBS -> {
        EndpointType.OBS
    }
}

fun EndpointType.toDataStore() = when (this) {
    EndpointType.OBS -> EndpointTypeProto.ENDPOINT_TYPE_OBS
}

fun ObsConfigProto.fromDataStore() = ObsConfig(
    url = this.url,
    port = this.port,
    password = this.password,
)

fun ObsConfig.toDataStore() = ObsConfigProto.newBuilder().let {
    it.url = this.url
    it.port = this.port
    it.password = this.password

    it.build()
}
