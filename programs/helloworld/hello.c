void transmit(char byte);

void print(char *array) {
  while (array[0] != '\0') {
    transmit(array[0]);
    array ++;
  }
}

void main() {
  print("Hello, from C\n");
}
