import com.embeddedunveiled.serial.SerialComManager
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE
import com.embeddedunveiled.serial.SerialComManager.DATABITS
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL
import com.embeddedunveiled.serial.SerialComManager.PARITY
import com.embeddedunveiled.serial.SerialComManager.STOPBITS
import java.io._
import scala.io.Source

class LCRMeter(port: String, baudrate: SerialComManager.BAUDRATE) {
  val scm = new SerialComManager
  val handle = scm.openComPort("/dev/ttyUSB0", true, true, false)
  scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, baudrate, 0)
  scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false)
  var isClosed: Boolean = false

  def printOutput(): Thread = {
    val thread = new Thread() {
      override def run() {
        println(s"Start monitor device $port....")
        val bufferedReader = new BufferedReader(new InputStreamReader(scm.createInputByteStream(handle)))
        var line = bufferedReader.readLine()
        while (!isClosed && line != null) {
          println(s"\nResposne from $port: $line")
          line = bufferedReader.readLine()
        }
      }
    }

    thread.start()
    thread
  }

  def sendCommand(command: String): Boolean = {
    val commandWithEOL = s"\n\r$command\n\r"
    scm.writeString(handle, commandWithEOL, 0)
  }

  def close() {
    println("Com port closed:" + scm.closeComPort(handle))
    isClosed = true
  }
}


object BasicRS232 {

  def main(args: Array[String]) {

    val lcrMeter = new LCRMeter("/dev/ttyUSB0", BAUDRATE.B38400)
    val meter1OutputThread = lcrMeter.printOutput()

    Iterator.continually {
      print("Command> ")
      readLine
    }.takeWhile(_ != "CLOSE.").foreach{ line => 
      lcrMeter.sendCommand(line)
    }

    lcrMeter.close()
  }
}
