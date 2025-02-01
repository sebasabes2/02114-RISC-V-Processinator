#include "../processinator.h"

void main() {
  while (1) {
    int buttons = buttonsPress();
    if (buttons & 1) {
      transmit('U');
    }
    if (buttons & 2) {
      transmit('R');
    }
    if (buttons & 4) {
      transmit('D');
    }
    if (buttons & 8) {
      transmit('L');
    }
    if (buttons & 16) {
      transmit('0');
    }
    if (buttons & 32) {
      transmit('1');
    }
    if (buttons & 64) {
      transmit('2');
    }
    if (buttons & 128) {
      transmit('3');
    }
  }
}
