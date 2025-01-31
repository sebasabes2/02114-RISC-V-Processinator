#include "../processinator.h"

float mandelbrot(float real, float imag) {
  return real * imag;
}

void main(int argc, int abcs) {
  int a = (int) mandelbrot(23.132, 329.123);
  for (int x = 0; x < 320; x ++) {
    for (int y = 0; y < 240; y ++) {
      display(x, y, 63);
    }
  }
}
