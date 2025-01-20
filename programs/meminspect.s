li s3, 0x3000   # UART address

li s0, 0x1000
li s1, 0x1100

loop:
lw a0, 0(s0)
jal ra, transmit
srli a0, a0, 8
jal ra, transmit
srli a0, a0, 8
jal ra, transmit
srli a0, a0, 8
jal ra, transmit
addi s0, s0, 4
blt s0, s1, loop

stop:
beq x0, x0, stop

transmit:
lw t0, 4(s3)
andi t0, t0, 1
beq x0, t0, transmit
sw a0, 0(s3)
jalr x0, 0(ra)
