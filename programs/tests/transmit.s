.text
.global transmit
transmit:
li t1, 0x3000 # UART address
lw t0, 4(t1)
andi t0, t0, 1
beq x0, t0, transmit
sw a0, 0(t1)
jalr x0, 0(ra)
