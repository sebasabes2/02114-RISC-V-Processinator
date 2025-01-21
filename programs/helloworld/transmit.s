.text
.global transmit
transmit:
lw t0, 4(s1)
andi t0, t0, 1
beq x0, t0, transmit
sw a0, 0(s1)
jalr x0, 0(ra)
