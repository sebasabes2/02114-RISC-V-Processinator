.text
li x1, 0x4000   # Btn address
li x2, 0x3000   # UART address
li x3,  0x2000   # LED address
## x4 reserved
## x5 reserved
li x6, 'U'
li x7, 'R'
li x8, 'D'
li x9, 'L'


start:
lw x4, 0(x1)    # receive
beq x0, x4, start

nop
andi x5, x4, 0b1111
## check U
addi x10, x0, 1
beq x10, x5, U

## check R
addi x10, x0, 2
beq x10, x5, R

## check D
addi x10, x0, 4
beq x10, x5, D

## check L
addi x10, x0, 8
beq x10, x5, L

beq x0, x0, start

U:
sw x6, 0(x2)
beq x0, x0, start

R:
sw x7, 0(x2)
beq x0, x0, start

D:
sw x8, 0(x2)
beq x0, x0, start

L:
sw x9, 0(x2)
beq x0, x0, start
