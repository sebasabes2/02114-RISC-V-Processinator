.data
string:
.string "Hello, world!\n"

.text
la x1, string
li x2, 0x3000   # UART address

loop:
lw x3, 0(x1)
addi x1, x1, 1
nop
nop
sw x3, 0(x2)
bne x0, x3, loop
