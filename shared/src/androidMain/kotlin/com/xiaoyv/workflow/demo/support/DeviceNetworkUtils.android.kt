package com.xiaoyv.workflow.demo.support

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Android 平台获取设备局域网 IPv4 地址实现。
 */
actual fun getDeviceIpV4(): String? = runCatching {
    val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
    for (intf in interfaces) {
        if (intf.isLoopback || !intf.isUp) continue
        for (addr in intf.inetAddresses) {
            if (!addr.isLoopbackAddress && addr is Inet4Address) {
                val host = addr.hostAddress
                if (!host.isNullOrBlank() && host != "127.0.0.1") {
                    return host
                }
            }
        }
    }
    null
}.getOrNull()
