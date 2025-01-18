.text
li s0, 0x1000   # Memory start 
li s1, 0x3000   # UART address

# Data forward to ALU
li t0, 'A'
li t1, 22
add a0, t0, t1
jal ra, transmit # expects 'W'

# Memory forward to ALU
li t0, 'A'
li t1, 14
sw t0, 0(s0)
sw t1, 4(s0)
lw t2, 0(s0)
lw t3, 4(s0)
add a0, t2, t3
jal ra, transmit # expects 'O'

# Data forward to memory
li t0, 'R'
sw t0, 0(s0)
nop
lw a0, 0(s0)
jal ra, transmit # expects 'R'

# Memory forward to memory
li t0, 'K'
nop
sw t0, 0(s0)
nop
lw t1, 0(s0)
sw t1, 4(s0)
nop
lw a0, 4(s0)
jal ra, transmit # expects 'K'

# Test of memory
li t0, 'I'
nop
sw t0, 0(s0)
lw a0, 0(s0)
jal ra, transmit # expects 'I'

# Branch flushing
li a0, 'N'
beq x0, x0, first
addi a0, a0, 1
addi a0, a0, 1
addi a0, a0, 1
first:
jal ra, transmit # expects 'N'

# Jal flushing
li a0, 'G'
jal x0, second
addi a0, a0, 1
addi a0, a0, 1
addi a0, a0, 1
second:
jal ra, transmit # expects 'G'

# Jalr flushing
li a0, '!'
auipc t0, 0
auipc t0, 0
jalr x0, 20(t0)
addi a0, a0, 1
addi a0, a0, 1
addi a0, a0, 1
auipc t1, 0
auipc t0, 0
nop
sw t0, 0(s0)
nop
lw t1, 0(s0)
jalr x0, 36(t1)
addi a0, a0, 1
addi a0, a0, 1
addi a0, a0, 1
jal ra, transmit # expects '!'

# End with new line
li a0, '\n'
jal ra, transmit 

# Stop execution:
loop:
beq x0, x0, loop

transmit:
lw t0, 4(s1)
nop
andi t0, t0, 1
beq x0, t0, transmit
sw a0, 0(s1)
jalr x0, 0(ra)
