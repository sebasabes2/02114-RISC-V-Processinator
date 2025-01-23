# 02114 Design of a RISC-V microprocessor
This project goal is to design and implement a RISC-V microprocessor and running the processor on a FPGA

>**Note**: This entire project was made and tested on a Basys-3 board.

## Setting up

Before you are able to run any programs on the microprocessor, a few setup steps are required as listed below.

---
### Chisel
Because this entire project is made with chisel, you are able to build and or run our all chisel tests with
``` bash
sbt run
```
for building or

``` bash
sbt test
```
for all Chisel Tests.

### Clock wizard
Due to the timing of the critical path, we decided to reduce the clock frequency of the microprocessor from the standard 100 MHz to 75 MHz. This means that you need to follow the following steps to change the clock frequency.

---
### Build project
- Build project with sbt and add Top.v and Top.xdc to vivado
- Go to project manager and open IP catalog
- Under "FPGA Features and Design" under "Clocking" double click "Clocking Wizard"
- Go to "Clocking Options" and rename "clk_in1" to "clk_in"
- Go to "Output Clocks" and rename "clk_out1" to "clk_out" and set requested frequency to "75.000"
- Press "ok" followed by "Generate"
- Synthesize with Vivado
---
### Upload program
- The directory “programs” is full of tests and demo programs written in assembly or C. Not all of them work, however.
- To assemble/compile your own programs use bash scripts ./assemble.sh and ./compile.sh. ./assemble.sh will create a flat binary assumed to start at address 0x0. ./compile.sh will create an ELF-file. If other segments than .text are used in a program, only ./compile.sh will work.
- Program the fpga board with the bitstream made by vivado.
- Reset the microprocessor by pressing the middle button / BTNC (U18)
- Run python program bootloader.py with the path to the desired binary or ELF-file to upload it. Note: this will require python package pyserial which can be installed with: “pip install pyserial”
