import jssc.SerialPort
import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener


class RS232Connection(port: String, baudRate: Int) {

  def processResponse(callback: String => Any) {
    val eventMask = 
      SerialPortEvent.BREAK + SerialPortEvent.CTS + SerialPortEvent.DSR + SerialPortEvent.ERR +
      SerialPortEvent.RING + SerialPortEvent.RLSD + SerialPortEvent.RXCHAR + SerialPortEvent.RXFLAG + 
      SerialPortEvent.TXEMPTY 

    val eventListener = new SerialPortEventListener {

      val buffer = new StringBuffer

      def processInputBuffer(byteCounts: Int) {
        val data = serialPort.readString().filter(_ != '\r')

        data.foreach { character =>
          if (character == '\n') {
            callback(buffer.toString)
            buffer.setLength(0)
          } else {
            buffer.append(character)
          }
        }
      }

      override def serialEvent(event: SerialPortEvent) {
        event.getEventType match {
          case SerialPortEvent.BREAK => 
            println("SerialPortEvent.BREAK" + event.getEventValue)
          case SerialPortEvent.CTS   => 
            println("SerialPortEvent.CTS:" + event.getEventValue)
          case SerialPortEvent.DSR   => 
            println("SerialPortEvent.DSR:" + event.getEventValue)
          case SerialPortEvent.ERR   => 
            println("SerialPortEvent.ERR:" + event.getEventValue)
          case SerialPortEvent.RING  => 
            println("SerialPortEvent.RING:" + event.getEventValue)
          case SerialPortEvent.RLSD    => 
            println("SerialPortEvent.RLSD:" + event.getEventValue)
          case SerialPortEvent.RXFLAG  => 
            println("SerialPortEvent.RXFLAG:" + event.getEventValue)
          case SerialPortEvent.TXEMPTY => 
            println("SerialPortEvent.TXEMPTY:" + event.getEventValue)
          case SerialPortEvent.RXCHAR  => 
            processInputBuffer(event.getEventValue)
        }
      }
    }

    serialPort.setEventsMask(eventMask)
    serialPort.addEventListener(eventListener)
  }

  val serialPort = new SerialPort("/dev/ttyUSB0")

  serialPort.openPort()
  serialPort.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)

  def sendCommand(command: String) {
    serialPort.writeBytes(s"$command\n\r".getBytes)
  }

  def close() {
    serialPort.closePort()
  }

}


object Main {


  def main(args: Array[String]) {

    println("Hello World")

    val rsConnection = new RS232Connection("/dev/ttyUSB0", SerialPort.BAUDRATE_38400)

    rsConnection.processResponse { line =>
      println("Got data...:" + line)
    }

    Iterator
      .continually { 
        print("Prompt> ")
        readLine
      }
      .takeWhile(_ != "CLOSE.")
      .foreach { line =>
        println("Got command from CONSOLE:" + line)
        rsConnection.sendCommand(line)
      }

    rsConnection.close()


  }
}
