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
li x11, '0'
li x12, '1'
li x13, '2'
li x14, '3'


start:
lw x4, 0(x1)    # receive
# andi x5, x4, 0b1111
andi x4, x4, 0b1111
lw x5, 4(x1)    # receive
andi x5, x5, 0b1111
slli x5, x5, 4
add x5, x5, x4
beq x0, x5, start

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

## check 0
addi x10, x0, 16
beq x10, x5, B0

## check 1
addi x10, x0, 32
beq x10, x5, B1

## check 2
addi x10, x0, 64
beq x10, x5, B2

## check 3
addi x10, x0, 128
beq x10, x5, B3

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

B0:
sw x11, 0(x2)
beq x0, x0, start

B1:
sw x12, 0(x2)
beq x0, x0, start

B2:
sw x13, 0(x2)
beq x0, x0, start

B3:
sw x14, 0(x2)
beq x0, x0, start
