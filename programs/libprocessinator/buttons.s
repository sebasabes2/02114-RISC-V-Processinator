.text
.global buttonsPress, buttonsHold

buttonsPress:
li t0, 0xa000  # Btn address
lw t1, 0(t0)   # Read board buttons
andi t1, t1, 0xf
lw t2, 4(t0)   # Read Pmod buttons
andi t2, t2, 0xf
slli t2, t2, 4
add a0, t2, t1
jalr x0, 0(ra)

buttonsHold:
li t0, 0xa000  # Btn address
lw t1, 0(t0)   # Read board buttons
srli t1, t1, 4
lw t2, 4(t0)   # Read Pmod buttons
andi t2, t2, 0xf0
add a0, t2, t1
jalr x0, 0(ra)
