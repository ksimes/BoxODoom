#include <Arduino.h>
#include "messages.h"

// Message format
boolean crlf = true;    // If the laser scanner produces a cr/lf sequence rather than a cr or lf at the end of a line


// message processing from the serial interface
Messages::Messages() {
  // Initialise the buffers for the messages
  for (int i = 0; i < BUFFER_SIZE; i++) {
    msgs[i].reserve(MAX_MSG_SIZE);
    msgs[i] = "";
  }
}

void Messages::anySerialEvent() {
  while (Serial.available() > 0) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\r') {
      msgAvailable = true;

//      Serial.println("Arduino received [" + msgs[bufferPointer] + "]");

      bufferPointer ++;
      if (bufferPointer == BUFFER_SIZE) {
        bufferPointer = 0;
      }

      // Clean the next buffer so there is no corruption from old messages.
      msgs[bufferPointer] = "";

      msgCount ++;
//      Serial.println("msgCount [+" + String(msgCount) + "]");

      //      if (crlf) { // If expecting a cr/lf sequence then check the next character
      //        if (Serial.available()) {
      //          char inChar = (char)Serial.read();
      //          if (inChar != 10) {   // If it is not a lf then add it to the buffer
      //            msgs[lastMsg] += inChar;
      //          }   // else discard
      //          finished = true;
      //        }
      //    }
    }
    else {
      // else add it to the inputString:
      msgs[bufferPointer] += inChar;
    }
  }
}

/* Has the other machine sent a message? */
String Messages::read(boolean blocking)
{
  String result = "";

  if (blocking) {
    while (!msgAvailable)  {
      anySerialEvent();
    } // wait for next message
  }

  if (msgAvailable) {
    // Find the last msg read.
    if (lastRead == BUFFER_SIZE) {
      lastRead = 0;
    }

    result = String(msgs[lastRead]);
//    Serial.println("result [" + result + "]");

    msgCount --;
//    Serial.println("msgCount [-" + String(msgCount) + "]");

    if (msgCount <= 0) {
      msgAvailable = false;
      msgCount = 0;
    }

    lastRead ++;
    return result;
  }
  else {
    return "";
  }
}

