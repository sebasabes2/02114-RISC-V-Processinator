steps to add clock wizard to vivado:
- Build project with sbt and add Top.v and Top.xdc to vivado 
- Go to project manager and open IP catalog
- Under "FPGA Features and Design" under "Clocking" double click "Clocking Wizard"
- Go to "Clocking Options" and rename "clk_in1" to "clk_in"
- Go to "Output Clocks" and rename "clk_out1" to "clk_out" and set requested frequency to "75.000"
- Press "ok" followed by "Generate"
- Build project
