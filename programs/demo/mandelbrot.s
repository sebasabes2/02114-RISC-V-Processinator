.text

li s0, 320 # max x-value
li s1, 240 # max y-value

li s4, -240 # initial x offset
li s5, -120 # initial y offset

li s6, 32 # move amount
li s7, 6 # zoom amount

clear_screen:
# # clear screen
# li s3, 0 # y value
# clear_yloop:
# li s2, 0 # x value
# clear_xloop:
# mv a0, s2 # move x to x input
# mv a1, s3 # move y to y input
# li a2, 0x3f
# jal ra, display

# # end of loop
# addi s2, s2, 1
# blt s2, s0, clear_xloop
# addi s3, s3, 1
# blt s3, s1, clear_yloop

# Low resolution preview:
li s3, 0 # y value
low_yloop:
li s2, 0 # x value
low_xloop:

# loop pixels
mv a0, s2 # move x to x input
mv a1, s3 # move y to y input

add a0, a0, s4
add a1, a1, s5

sll a0, a0, s7
sll a1, a1, s7

jal ra, iterate
mv a2, a0

mv a0, s2 # move x to x input
mv a1, s3 # move y to y input
jal ra, display_16

# end of loop
addi s2, s2, 4
blt s2, s0, low_xloop
addi s3, s3, 4
blt s3, s1, low_yloop

# Start of mandelbrot generation:
li s3, 0 # y value
yloop:
li s2, 0 # x value
xloop:

# loop pixels
mv a0, s2 # move x to x input
mv a1, s3 # move y to y input

add a0, a0, s4
add a1, a1, s5

sll a0, a0, s7
sll a1, a1, s7

jal ra, iterate
mv a2, a0

mv a0, s2 # move x to x input
mv a1, s3 # move y to y input
jal ra, display

jal ra, check_for_input # restart if input is given

# end of loop
addi s2, s2, 1
blt s2, s0, xloop
addi s3, s3, 1
blt s3, s1, yloop

wait_for_input:
jal ra, check_for_input
beq x0, x0, wait_for_input

check_for_input:
li t0, 0x4000  # Btn address
lw t1, 0(t0)   # Read board buttons
srli t1, t1, 4
lw t2, 4(t0)   # Read Pmod buttons
# li t0, 0
andi t2, t2, 0xc0
# slli t2, t2, 4
add t2, t2, t1
bne x0, t2, handle_input
jalr x0, 0(ra)

handle_input:

# Check U
andi t3, t2, 1
beq x0, t3, skip_button_U
sub s5, s5, s6
skip_button_U:

# Check R
andi t3, t2, 2
beq x0, t3, skip_button_R
add s4, s4, s6
skip_button_R:

# Check D
andi t3, t2, 4
beq x0, t3, skip_button_D
add s5, s5, s6
skip_button_D:

# Check L
andi t3, t2, 8
beq x0, t3, skip_button_L
sub s4, s4, s6
skip_button_L:

# Check 2
andi t3, t2, 64
beq x0, t3, skip_button_2
srai s4, s4, 1
srai s5, s5, 1
addi s7, s7, 1
skip_button_2:

# Check 3
andi t3, t2, 128
beq x0, t3, skip_button_3
slli s4, s4, 1
slli s5, s5, 1
addi s7, s7, -1
skip_button_3:

# rerender image:
nop         # This nop is essential
beq x0, x0, clear_screen

display_16:
mv a4, ra
addi t2, a0, 4
addi t3, a1, 4
display_16_yloop:
display_16_xloop:
jal ra, display
addi a0, a0, 1
blt a0, t2, display_16_xloop
addi a1, a1, 1
addi a0, a0, -4
blt a1, t3, display_16_yloop
mv ra, a4
jalr x0, 0(ra)

display:
li t0, 0x100000  # start of video memory
mv t1, a1        # address = a1 << 10 + a0 + t0 
slli t1, t1, 9
add t1, t1, a0
add t1, t1, t0
sw a2, 0(t1)
jalr x0, 0(ra)

iterate:
# a0 = fixed real (input)
# a1 = fixed imag (input)
# a4 = fixed real0
# a5 = fixed imag0
# a6 = ra
# a7 = temp
# t0 = fixed real
# t1 = fixed imag
# t2 = fixed realsq
# t3 = fixed imagsq
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

# initialize i, 50, 4.0
li t4, 0        # i = 0
li t5, 64       # t5 = 50
li t6, 4        # t6 = (int) 4
slli t6, t6, 13 # t6 = (fixed) 4.0

# for (i = 0; i < 50; i ++)
iterate_loop:

# realsq = real * real
mv a0, t0
mv a1, t0
jal ra, mult_fixed
mv t2, a2

# imagsq = imag * imag
mv a0, t1
mv a1, t1
jal ra, mult_fixed
mv t3, a2

## if (realsq + imagsq) > (fixed) 4 then break 
add a7, t2, t3
bge a7, t6, iterate_return

# imag = real * imag * 2 + imag0
mv a0, t0
mv a1, t1
jal ra, mult_fixed
mv t1, a2
slli t1, t1, 1
add t1, t1, a5

# real = realsq - imagsq + real0
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


# Multiplication subroutine:

mult_fixed:
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
