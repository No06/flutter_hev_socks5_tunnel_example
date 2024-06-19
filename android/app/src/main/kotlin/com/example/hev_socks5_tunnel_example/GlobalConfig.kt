package com.example.hev_socks5_tunnel_example

import java.io.File

class GlobalConfig {
    companion object {
        var CONFIG_PARENT_PATH: File? = null
        const val TPROXY_FILE_NAME = "tproxy.conf"

        val tproxyFile: File
            get() = File(CONFIG_PARENT_PATH, TPROXY_FILE_NAME)
    }
}