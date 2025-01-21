void transmit(int byte);

void print(char *array) {
  for (int i = 0; i < 5; i ++) {
    int x = ((int*) array)[0];
    transmit(x);
    x = x >> 8;
    transmit(x);
    x = x >> 8;
    transmit(x);
    x = x >> 8;
    transmit(x);
  }
}

void main() {
  char string[] =  "Hello, world!\n";
  print("Hello, world!\n");
}