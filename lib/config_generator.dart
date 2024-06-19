String configGenerator(
  String address,
  int port,
  String username,
  String password,
) =>
    '''
tunnel:
  # Interface name
  name: tun0
  # Interface MTU
  mtu: 8500
  # Multi-queue
  multi-queue: false
  # IPv4 address
  ipv4: 198.18.0.1
  # IPv6 address
  ipv6: 'fc00::1'

socks5:
  port: $port
  address: $address
  # Socks5 UDP relay mode (tcp|udp)
  udp: 'tcp'
  username: '$username'
  password: '$password'

# misc:
#    log-file: stdout
#    log-level: debug
''';
