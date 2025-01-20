.data
string:
.string "Hello, world!\n"

.text
la s0, string
# li s0, 0x1000
li s1, 0x3000   # UART address

loop:
lbu a0, 0(s0)
jal ra, transmit
addi s0, s0, 1
bne x0, a0, loop

# Stop execution:
stop:
beq x0, x0, stop

transmit:
lw t0, 4(s1)
nop
andi t0, t0, 1
beq x0, t0, transmit
sw a0, 0(s1)
jalr x0, 0(ra)

