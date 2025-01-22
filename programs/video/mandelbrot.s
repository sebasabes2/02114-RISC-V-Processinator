.text

li s0, 320 # max x-value
li s1, 240 # max y-value

# clear screen:
li s3, 0 # y value
clear_yloop:
li s2, 0 # x value
clear_xloop:
mv a0, s2 # move x to x input
mv a1, s3 # move y to y input
li a2, 0x3f
jal ra, display

# end of loop
addi s2, s2, 1
blt s2, s0, clear_xloop
addi s3, s3, 1
blt s3, s1, clear_yloop

# Start of mandelbrot generation:
li s3, 0 # y value
yloop:
li s2, 0 # x value
xloop:
# loop pixels


# map x and y to fixed points by left shift by 5

mv a0, s2 # move x to x input
mv a1, s3 # move y to y input

slli a0, a0, 5
slli a1, a1, 5

jal ra, iterate
mv a2, a0

# li t0, 100
# li a2, 0x3f  # load color with white
# blt s2, t0, label1 
# li a2, 0  # load color with black
# label1:

mv a0, s2 # move x to x input
mv a1, s3 # move y to y input
jal ra, display

# end of loop
addi s2, s2, 1
blt s2, s0, xloop
addi s3, s3, 1
blt s3, s1, yloop

stop:
beq x0, x0, stop

display:
li t0, 0x100000  # start of video memory
mv t1, a1        # address = a1 << 10 + a0 + t0 
slli t1, t1, 9
add t1, t1, a0
add t1, t1, t0
sw a2, 0(t1)
jalr x0, 0(ra)

# iterate: # old "iterate". displays box in the middle of screen
# li t0, 50
# li t1, 150
# blt a0, t0, white
# blt t1, a0, white
# blt a1, t0, white
# blt t1, a1, white
# black:
# li a0, 0
# jalr x0, 0(ra)
# white:
# li a0, 0x3f
# jalr x0, 0(ra)

# 


iterate:
# a0 = fixed real (input)
# a1 = fixed imag (input)
# a4 = fixed real0
# a5 = fixed imag0
# a6 = ra
# a7 = temp
# t0 = fixed real
# t1 = fixed imag
# t2 = fixed realq
# t3 = fixed imagq
# t4 = int i
# t5 = int 50
# t6 = fixed 4.0

# save input
mv a4, a0
mv a5, a1

# save return address
mv a6, ra

# initialize real, imag
mv t0, a0
mv t1, a1

li t4, 0         # i = 0
li t5, 50        # t5 = 50
li t6, 4         # t6 = (int) 4
slli t6, t6, 13 # t6 = (fixed) 4.0

# for (i = 0; i < 50; i ++)
iterate_loop:

# realq = real * real
mv a0, t0
mv a1, t0
jal ra, mult_fixed
mv t2, a2

# imagq = imag * imag
mv a0, t1
mv a1, t1
jal ra, mult_fixed
mv t3, a2

## if (realq + imagq) > (fixed) 4 then break 
add a7, t2, t3
bge a7, t6, iterate_return

# imag = real * imag * 2 + imag0
mv a0, t0
mv a1, t1
jal ra, mult_fixed
mv t1, a2
slli t1, t1, 1
add t1, t1, a5

# real = realq - imagq + real0
sub t0, t2, t3
add t0, t0, a4

# i ++
addi t4, t4, 1
# for i < 50
blt t4, t5, iterate_loop

iterate_return:
mv a0, t4
bne t4, t5, iterate_return2
li a0, 0

iterate_return2:
# restore return address
mv ra, a6
jalr x0, 0(ra)



mult_fixed:

# # check0 if a0 is 0 return 0
# li a2, 0
# bne x0, a0, mult_check1
# jalr x0, 0(ra)

# mult_check1:
# # check1 if a0 is negative and a1 is negative, invert both and do pos_pos
# bge a0, x0, mult_check2
# bge a1, x0, mult_check2
# sub a0, x0, a0 # a0 = -a0
# sub a1, x0, a1 # a1 = -a1
# beq x0, x0, mult_fixed_pos_pos

# mult_check2:


# mult_fixed_pos_pos:
# add a2, a2, a1
# addi a0, a0, -1
# bne x0, a0, mult_fixed_pos_pos
# srai a2, a2, 13
# jalr x0, 0(ra)


li a2, 0

mult_fixed_loop:
beq a0, x0, mult_fixed_return
andi a3, a0, 1
beq x0, a3, mult_fixed_even
add a2, a2, a1
mult_fixed_even:
srli a0, a0, 1
slli a1, a1, 1
beq x0, x0, mult_fixed_loop
mult_fixed_return:
srai a2, a2, 13
jalr x0, 0(ra)

