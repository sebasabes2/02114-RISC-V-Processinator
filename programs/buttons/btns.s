.text
li x1, 0x4000   # Btn address
li x2, 0x2000   # LED address
li x4, '0'

start:
lw x4, 0(x1) # receive
nop
sw x4, 0(x2)
beq x0, x0, start
