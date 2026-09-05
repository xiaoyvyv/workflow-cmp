package com.xiaoyv.workflow.demo.support

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.darwin.freeifaddrs
import platform.darwin.getifaddrs
import platform.darwin.ifaddrs
import platform.posix.AF_INET
import platform.posix.IFF_LOOPBACK
import platform.posix.IFF_UP
import platform.posix.NI_MAXHOST
import platform.posix.NI_NUMERICHOST
import platform.posix.getnameinfo

/**
 * iOS / Native 平台获取设备局域网 IPv4 地址实现。
 */
@OptIn(ExperimentalForeignApi::class)
actual fun getDeviceIpV4(): String? = memScoped {
    val ifap = allocPointerTo<ifaddrs>()
    if (getifaddrs(ifap.ptr) != 0) return null
    try {
        var curr = ifap.value
        while (curr != null) {
            val ifa = curr.pointed
            val addr = ifa.ifa_addr
            if (addr != null && addr.pointed.sa_family.toInt() == AF_INET) {
                val flags = ifa.ifa_flags.toInt()
                val isLoopback = (flags and IFF_LOOPBACK) != 0
                val isUp = (flags and IFF_UP) != 0
                if (isUp && !isLoopback) {
                    val host = allocArray<ByteVar>(NI_MAXHOST)
                    if (getnameinfo(
                            addr,
                            addr.pointed.sa_len.toUInt(),
                            host,
                            NI_MAXHOST.toUInt(),
                            null,
                            0u,
                            NI_NUMERICHOST,
                        ) == 0
                    ) {
                        val ip = host.toKString()
                        if (ip.isNotBlank() && ip != "127.0.0.1") {
                            return ip
                        }
                    }
                }
            }
            curr = ifa.ifa_next
        }
        null
    } finally {
        freeifaddrs(ifap.value)
    }
}
