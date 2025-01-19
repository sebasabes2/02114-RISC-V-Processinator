.text
li s0, 0x100000 # start of video memory

li s1, 1280 # max x-value
li s2, 960 # max y-value

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
jal x0, start

display:
mv t3, t1
slli t3, t3, 10
add t3, t3, t0
add t3, t3, s0
sw t0, 0(t3)
jalr x0, 0(ra)
