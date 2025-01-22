.text
li s0, 0x100000 # start of video memory

li s1, 640 # max x-value
li s2, 480 # max y-value
li s3, 0x4000 # Buttons

start:
li t1, 0 # y value
yloop:
li t0, 0 # x value
xloop:
addi t0, t0, 1
jal ra, display
blt t0, s1, xloop
addi t1, t1, 1
blt t1, s2, yloop

wait:
lw t2, 0(s3)
andi t2, t2, 0xf
beq x0, t2, wait

beq x0, x0, start

stop:
beq x0, x0, stop

display:
mv t3, t1
slli t3, t3, 10
add t3, t3, t0
add t3, t3, s0
srli t4, t0, 3
sw t4, 0(t3)
jalr x0, 0(ra)
