.section .text
.global _start
_start:
li sp, 0x1FF0
jal main
loop:
beq x0, x0, loop
