#include "../processinator.h"

int iterate(float real0, float imag0, int max) {
  float real = real0;
  float imag = imag0;
  int i;

  for (i = 0; i < max; i ++) {
    float realsq = real*real;
    float imagsq = imag*imag;

    if (realsq + imagsq > 4.0) {
      break;
    } 

    imag = real * imag * 2 + imag0;
    real = realsq - imagsq + real0;
  }

  return i;
}

void main() {
  for (int y = 0; y < 240; y ++) {
    for (int x = 0; x < 320; x ++) {
      float real = x / 320.0 * 2.0 - 1.5;
      float imag = y / 240.0 * 2.0 - 1.0;
      int iterations = mandelbrot(real, imag, 64);
      int color = iterations % 64;
      display(x, y, color);
    }
  }
}
