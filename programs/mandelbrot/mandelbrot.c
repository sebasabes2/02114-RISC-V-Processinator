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

float map(float x, float x0, float x1, float v0, float v1) {
  return (x - x0) / (x1 - x0) * (v1 - v0) + v0;
}

int mandelbrot(float x, float y, float size, int max, int pixelSize) {
  for (int py = 0; py < 240; py += pixelSize) {
    for (int px = 0; px < 320; px += pixelSize) {
      float xSize = size / 2;
      float ySize = size / 320.0 * 240.0 / 2;
      float real = map(px, 0, 320, x - xSize, x + xSize);
      float imag = map(py, 0, 240, y - ySize, y + ySize);
      int iterations = iterate(real, imag, max);
      int color = iterations % max; 
      for (int pyo = 0; pyo < pixelSize; pyo ++) {
        for (int pxo = 0; pxo < pixelSize; pxo ++) {
          display(px + pxo, py + pyo, color);
        }
      }
      int buttons = buttonsPress();
      if (buttons != 0) {
        return buttons;
      }
    }
  }
  return 0;
}

void main() {
  float cx = -0.5;
  float cy = 0;
  float size = 2.5;
  int maxItt = 64;
  int buttons = 0;
  while (1) {
    if (buttons & 1) {
      cy -= size/16;
    }
    if (buttons & 2) {
      cx += size/16;
    }
    if (buttons & 4) {
      cy += size/16;
    }
    if (buttons & 8) {
      cx -= size/16;
    }
    if (buttons & 16) {
      maxItt <<= 1;
    }
    if (buttons & 32) {
      maxItt >>= 1;
    }
    if (buttons & 64) {
      size *= 2;
    }
    if (buttons & 128) {
      size /= 2;
    }
    buttons = mandelbrot(cx, cy, size, maxItt, 4);
    if (buttons != 0) {
      continue;
    }
    buttons = mandelbrot(cx, cy, size, maxItt, 1);
    if (buttons != 0) {
      continue;
    }
    while (buttons == 0) {
      buttons = buttonsPress();
    }
  }
}
