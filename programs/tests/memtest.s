li s0, 0x3000   # UART address
li s1, 0x1000   # start of memory
li x1, 0xdeadbeef
sw x1, 0(s1)
# 0xef
lb x2, 0(s1)
sw x2, 0(s0)
# 0xbe
lb x3, 1(s1)
sw x3, 0(s0)
# 0xad
lb x4, 2(s1)
sw x4, 0(s0)
# 0xde
lb x5, 3(s1)
sw x5, 0(s0)
