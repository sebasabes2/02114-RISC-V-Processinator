import sys
import serial
import serial.tools.list_ports

readFile = sys.argv[1]

startCode = 0x00017373.to_bytes(4, 'little')
endCode = 0x00027373.to_bytes(4, 'little')

def intToBytes(input):
  return input.to_bytes(4, 'little')

with open(readFile, "rb") as file:
  try:
    port = serial.tools.list_ports.comports()[0].device
    ser = serial.Serial(port, 115200)
    ser.write(startCode)
    ser.write(intToBytes(0))
    ser.write(file.read())
    ser.write(endCode)
    ser.close()
  except:
    print("Unable to open Serial Port")
