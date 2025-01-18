.text
li x11, 0x1000   # Memory start 
li x13, 0x3000   # UART address

# Data forward to ALU
li x1, 'A'
li x2, 22
add x3, x1, x2
nop
sw x3, 0(x13) # expects 'W'

# Memory forward to ALU
li x1, 'A'
li x2, 14
sw x1, 0(x11)
sw x2, 4(x11)
lw x3, 0(x11)
lw x4, 4(x11)
add x5, x3, x4
nop
sw x5, 0(x13) # expects 'O'

# Data forward to memory
li x1, 'R'
sw x1, 0(x11)
nop
lw x2, 0(x11)
nop
sw x2, 0(x13) # expects 'R'

# Memory forward to memory
li x1, 'K'
nop
sw x1, 0(x11)
nop
lw, x2, 0(x11)
sw x2, 0(x13) # expects 'K'

# Test of memory
li x1, 'S'
nop
sw x1, 0(x11)
lw x2, 0(x11)
nop
sw x2, 0(x13) # expects 'S'

# Branch flushing
li x1, '!'
beq x0, x0, first
addi x1, x1, 1
addi, x1, x1, 1
addi, x1, x1, 1
print:
sw x1, 0(x13)

loop:
beq x0, x0, loop
