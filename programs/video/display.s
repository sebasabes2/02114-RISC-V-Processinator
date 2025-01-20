.text
.global display
display:
li t0, 0x100000 # start of video memory
mv t1, a1
slli t1, t1, 10
add t1, t1, a0
add t1, t1, t0
sw a2, 0(t1)
jalr x0, 0(ra)
