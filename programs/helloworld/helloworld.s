.data
string:
.string "Hello, world!\n"

.text
.global _start
_start:

la s0, string
li s1, 0x3000   # UART address

loop:
lb a0, 0(s0)
jal ra, transmit
addi s0, s0, 1
bne x0, a0, loop

# Stop execution:
stop:
beq x0, x0, stop

transmit:
lw t0, 4(s1)
andi t0, t0, 1
beq x0, t0, transmit
sw a0, 0(s1)
jalr x0, 0(ra)
