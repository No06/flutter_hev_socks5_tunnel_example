package hev.htproxy

class Preferences {
    val dnsIpv4 = "223.5.5.5"

    var dnsIpv6 = "2001:4860:4860::8888"

    var ipv4 = true

    var ipv6 = false

    var global = true

    var apps: Set<String> = HashSet()

    val tunnelMtu: Int
        get() = 8500

    val tunnelIpv4Address: String
        get() = "198.18.0.1"

    val tunnelIpv4Prefix: Int
        get() = 32

    val tunnelIpv6Address: String
        get() = "fc00::1"

    val tunnelIpv6Prefix: Int
        get() = 128
}