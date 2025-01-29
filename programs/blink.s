.text
li x1, 0        # loop index 
li x2, 0x6cf000 # max loop
li x3, 1        # LED on
li x4, 0x2000   # LED address

loop1:
addi x1, x1, 1
blt x1, x2, loop1
nop
nop

sw x3, 0(x4) # turn on LED

loop2:
addi x1, x1, -1
blt x0, x1, loop2
nop
nop

sw x0, 0(x4) # turn off LED
beq x0, x0, loop1
