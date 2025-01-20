.text
li x1, 0x4000   # Btn address
li x2, 0x3000   # UART address
li x3, 0x0021

start:
lw x4, 0(x1) # receive
andi x4, x4, 0b1111

beq x0, x4, start

nop
sw x3, 0(x2)
beq x0, x0, start
