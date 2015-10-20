import jssc.SerialPort

object Main {
  def main(args: Array[String]) {
    println("Hello World")
    val serialPort = new SerialPort("/dev/ttyUSB0")
    serialPort.openPort()
    serialPort.setParams(
      SerialPort.BAUDRATE_9600, 
      SerialPort.DATABITS_8,
      SerialPort.STOPBITS_1,
      SerialPort.PARITY_NONE
    )

    serialPort.writeBytes("COMU?\n\r".getBytes)
    serialPort.writeBytes("COMU:OVER\n\r".getBytes)
    serialPort.writeBytes("COMU:OFF\n\r".getBytes)
    serialPort.closePort()
  }
}
