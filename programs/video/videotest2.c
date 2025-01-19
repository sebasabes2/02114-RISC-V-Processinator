
void display(int x, int y, int color);

void main() {
  while (1) {
    float a = 0.12;
    display(0, 0, *((int*) &a));
    for (int y = 0; y < 960; y ++) {
      for (int x = 0; x < 1280; x ++) {
        display(x, y, x);
      }
    }
  }
}