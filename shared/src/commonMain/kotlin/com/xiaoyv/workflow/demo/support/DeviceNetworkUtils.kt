package com.xiaoyv.workflow.demo.support

/**
 * 获取当前设备第一个可用的局域网 IPv4 地址（如 192.168.x.x 等非回环地址）。
 */
expect fun getDeviceIpV4(): String?
