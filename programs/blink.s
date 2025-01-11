li x1, 0        # loop index 
li x2, 0x6cf000 # max loop
li x3, 1        # LED on
li x4, 0x2000   # LED address

loop1:
addi x1, x1, 1
nop
nop
nop
blt x1, x2, loop1
nop
nop
nop

# turn on LED
sw x3, 0(x4)

loop2:
addi x1, x1, -1
nop
nop
nop
blt x0, x1, loop2
nop
nop
nop

# turn on LED
sw x0, 0(x4)
beq x0, x0, loop1
