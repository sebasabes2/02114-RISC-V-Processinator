.text
la s0, string
# li s0, 0x1000
li s1, 0x3000   # UART address

# loop:
# lw a0, 0(s0)
# jal ra, transmit
# addi s0, s0, 1
# bne x0, a0, loop

# li a0, 'H'
# jal ra, transmit
# li a0, 'e'
# jal ra, transmit
# li a0, 'l'
# jal ra, transmit
# li a0, 'l'
# jal ra, transmit
# li a0, 'o'
# jal ra, transmit
# li a0, '!'
# jal ra, transmit
# li a0, '\n'
# jal ra, transmit

mv a0, s0
jal ra, transmitword

nop
nop
nop
nop
nop
lw a0, 0(s0)
nop
nop
nop
nop
nop
jal ra, transmitword
nop
nop
nop
nop
nop

lw a0, 0x100(x0)
jal ra, transmitword

lw a0, 8(s0)
jal ra, transmitword

lw a0, 12(s0)
jal ra, transmitword

lw a0, 16(s0)
jal ra, transmitword

lw a0, 20(s0)
jal ra, transmitword

lw a0, 24(s0)
jal ra, transmitword

# jal ra, transmit
# lbu a0, 4(s0)
# jal ra, transmit
# lbu a0, 8(s0)
# jal ra, transmit
# lbu a0, 12(s0)
# jal ra, transmit
# lbu a0, 16(s0)
# jal ra, transmit
# lbu a0, 20(s0)
# jal ra, transmit
# lbu a0, 24(s0)
# jal ra, transmit


# Stop execution:
stop:
beq x0, x0, stop

transmitword:
mv t1, ra
jal ra, transmit
srli a0, a0, 8
jal ra, transmit
srli a0, a0, 8
jal ra, transmit
srli a0, a0, 8
jal ra, transmit
jalr x0, 0(t1)

transmit:
lw t0, 4(s1)
andi t0, t0, 1
beq x0, t0, transmit
sw a0, 0(s1)
jalr x0, 0(ra)

.data
string:
.string "Hello, world!\n"
