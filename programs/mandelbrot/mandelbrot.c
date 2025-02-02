#include "../processinator.h"

int iterate(float creal, float cimag, float zreal0, float zimag0, int max) {
  float real = zreal0;
  float imag = zimag0;
  int i;

  for (i = 0; i < max; i ++) {
    float realsq = real*real;
    float imagsq = imag*imag;

    if (realsq + imagsq > 4.0) {
      break;
    } 

    imag = real * imag * 2 + cimag;
    real = realsq - imagsq + creal;
  }

  return i;
}

float map(float x, float x0, float x1, float v0, float v1) {
  return (x - x0) / (x1 - x0) * (v1 - v0) + v0;
}

void calculateSet(float x, float y, float cx, float cy, float size, int max, int type, int pixelSize, int *buttons) {
  for (int py = 0; py < 240; py += pixelSize) {
    for (int px = 0; px < 320; px += pixelSize) {
      float xSize = size / 2;
      float ySize = size / 320.0 * 240.0 / 2;
      float real = map(px, 0, 320, x - xSize, x + xSize);
      float imag = map(py, 0, 240, y - ySize, y + ySize);
      int iterations;
      if (type == 0) {
        iterations = iterate(real, imag, real, imag, max);
      } else {
        iterations = iterate(cx, cy, real, imag, max);
      }
      int color; 
      if (max >= 64) {
        color = iterations / (max / 64);
      }  else {
        color = iterations * (64 / max);
      }
      for (int pyo = 0; pyo < pixelSize; pyo ++) {
        for (int pxo = 0; pxo < pixelSize; pxo ++) {
          display(px + pxo, py + pyo, color);
        }
      }
      *buttons = buttonsPress();
      if (*buttons != 0) {
        return;
      } 
    }
  }
}

struct parms {
  float x;
  float y;
  float size;
};

void updateParms(int buttons, struct parms *parms, int *maxItt) {
  if (buttons & 1) {
    parms->y -= parms->size/16;
  }
  if (buttons & 2) {
    parms->x += parms->size/16;
  }
  if (buttons & 4) {
    parms->y += parms->size/16;
  }
  if (buttons & 8) {
    parms->x -= parms->size/16;
  }
  if (buttons & 16) {
    *maxItt <<= 1;
  }
  if (buttons & 32) {
    *maxItt >>= 1;
  }
  if (buttons & 64) {
    parms->size *= 2;
  }
  if (buttons & 128) {
    parms->size /= 2;
  }
}

void main() {
  struct parms mandelbrot = { .x = 0, .y = 0, .size = 3.2 };
  struct parms julia = { .x = 0, .y = 0, .size = 3.2 };
  struct parms *active = &mandelbrot;
  int maxItt = 64;
  int buttons = 0;
  int mode = 0;
  while (1) {
    int hold = buttonsHold();
    if ((hold & 16) && (buttons & 32)) {
      mode = !mode;
      maxItt = 128;
      if (mode == 0) {
        active = &mandelbrot;
      } else {
        active = &julia;
        julia.x = 0;
        julia.y = 0;
        julia.size = 3.2;
      }
    }
    updateParms(buttons, active, &maxItt);
    calculateSet(active->x, active->y, mandelbrot.x, mandelbrot.y, active->size, maxItt, mode, 4, &buttons);
    if (buttons != 0) {
      continue;
    }
    calculateSet(active->x, active->y, mandelbrot.x, mandelbrot.y, active->size, maxItt, mode, 1, &buttons);
    if (buttons != 0) {
      continue;
    }
    while (buttons == 0) {
      buttons = buttonsPress();
    }
  }
}
