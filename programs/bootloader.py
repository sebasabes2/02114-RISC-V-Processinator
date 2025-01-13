import sys
import serial
import serial.tools.list_ports

readFile = sys.argv[1]

startCode = 0x00017373.to_bytes(4, 'little')
endCode = 0x00027373.to_bytes(4, 'little')

def intToBytes(input):
  return input.to_bytes(4, 'little')

def getPort():
  ports = list(filter(lambda x: "USB Serial Port" in x.description, serial.tools.list_ports.comports()))
  if (len(ports) == 0):
    print("Unable to find Serial Port")
    return
  if (len(ports) != 1):
    print("Found multiple ports:\n" + '\n'.join(map(lambda x: x.description, ports)))
    return
  return ports[0].device

with open(readFile, "rb") as file:
  try:
    port = getPort()
    ser = serial.Serial(port, 115200)
    ser.write(startCode)
    ser.write(intToBytes(0))
    ser.write(file.read())
    ser.write(endCode)
    ser.close()
  except:
    print("Unable to open Serial Port")
