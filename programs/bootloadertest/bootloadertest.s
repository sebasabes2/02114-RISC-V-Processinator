.text
.global _start

li a0, 'A'
alternative:
addi a0, a0, 1
addi a0, a0, 1
addi a0, a0, 1

jal ra, transmit

loop:
beq x0, x0, loop

_start:
li a0, 'a'
beq x0, x0, alternative
