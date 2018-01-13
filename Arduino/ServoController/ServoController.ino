#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

#include "messages.h"

#define VERSION "1.7"

//  Code for processing messages coming from an external source (Rasp Pi or otherwise) and transmits the resulting command to control
//  servos. Introduced for a Ardunio Nano as I could not get the equivalent servo control code for Rasp Pi to work properly.
//  Messages are in the form of a string "{<command> <servo> <speed>}" where command is a three letter code, servo is a
//  number between 0 and 15 (depending on chained PCA9685 boards) and the speed is a value between 0 and 10. The speed introduces
//  a delay beween steps in the servo turn of up to 100 milliseconds. So zero is fastest and 10 is slowest.
//  Commands implemented:
//    SWP - Sweeps the servo from the 0 angle to the 160 degree angle, wait a half second and sweep back again to 0 angle.
//    OPN - Sweeps the servo from the 0 angle to the 160 degree angle.
//    CLS - Sweeps the servo from the 160 angle to the 0 degree angle, servo should already have been opened.


//  The PCA9685 boards use I2C to communicate, 2 pins are required to
//  interface. For Arduino Nanos, thats SCL -> Analog 5, SDA -> Analog 4
//  Power should be 3.3 volts for the control chip. V+ supplies the servos
//  and should not be supplied from the Nano. This is because Servos can put
//  a lot of noise on the bus and can scrable signals from the nano.

// called this way, it uses the default address 0x40
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();
// you can also call it with a different address you want
//Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(0x41);

// Depending on your servo make, the pulse width min and max may vary, you
// want these to be as small/large as possible without hitting the hard stop
// for max range. You'll have to tweak them as necessary to match the servos you
// have!

#define MIN_PULSE_WIDTH       650
#define MAX_PULSE_WIDTH       2350
#define DEFAULT_PULSE_WIDTH   1500

#define MAX_ANGLE   160

#define SERIAL_SPEED 115200

#define FREQUENCY             60

// Message processor comming in from Rasp Pi
Messages *messages;

// Command structure to be executed, populated from message string sent
struct Command {
  String cmd;
  int servo;
  int speed;
};

// Extracts a number from a string
static int getNumber(String data)
{
  int result = 0;
  char carray[10];
  data.toCharArray(carray, sizeof(carray));
  result = atoi(carray);

  return result;
}

static void openHorn(struct Command command) {
  for (uint16_t pulselen = 0; pulselen < MAX_ANGLE; pulselen++) {
    pwm.setPWM(command.servo, 0, pulseWidth(pulselen));
    delay(command.speed * 10);
  }
}

static void closeHorn(struct Command command) {
  for (uint16_t pulselen = MAX_ANGLE; pulselen > 0; pulselen--) {
    pwm.setPWM(command.servo, 0, pulseWidth(pulselen));
    delay(command.speed * 10);
  }
}

static void processCommand(struct Command command) {

  if (command.cmd.compareTo("SWP") == 0) {
    // Drive the selected servo to sweep open and then back at a certain speed
    openHorn(command);
    delay(500);
    closeHorn(command);
  } else if (command.cmd.compareTo("OPN") == 0) {
    // Drive the selected servo to open at a certain speed
    openHorn(command);
  } else if (command.cmd.compareTo("CLS") == 0) {
    // Drive the selected servo to close at a certain speed
    closeHorn(command);
  }
}

/*
   Note that all commands are in the form {command servo speed}
   command is always a three letter acroynm
*/
static void processCommand(String msg) {

  msg.trim();  // Trim off white space from both ends (usually the lf or cr).

  if ((msg.length() > 5) && msg.startsWith("{") && msg.endsWith("}")) {

    String body = msg.substring(1, msg.length() - 1);   // Trim off opening brace
    body.trim();  // Trim off any white space from both ends (may be some at beginning of message).

    //    Serial.println("Body = [" + body + "]");

    // Get the actual command
    struct Command command;

    int nextSpace = body.indexOf(" ");
    if (nextSpace == -1) {  // No space found
      command.cmd = body.substring(0);
    }
    else {
      command.cmd = body.substring(0, nextSpace);
    }

    // All commands are three characters.
    if (command.cmd.length() != 3) {
      return;   // return and ignore if invalid command
    } else
    {
      //      Serial.println("Cmd = [" + command.cmd + "]");

      body = body.substring(nextSpace + 1);
      body.trim();  // Trim off any white space from both ends (may be some at beginning of message).

      nextSpace = body.indexOf(" ");
      String numb;
      if (nextSpace == -1) {  // No space found
        numb = body.substring(0);
      }
      else {
        numb = body.substring(0, nextSpace + 1);
      }
      command.servo = getNumber(numb);
      //      Serial.println("Servo = [" + String(command.servo) + "]");

      nextSpace = body.indexOf(" ");
      body = body.substring(nextSpace + 1);
      body.trim();  // Trim off any white space from both ends (may be some at beginning of message).

      if (nextSpace == -1) {  // No space found
        numb = body.substring(0);
      }
      else {
        numb = body.substring(0, nextSpace + 1);
      }
      command.speed = getNumber(numb);
      //      Serial.println("Speed = [" + String(command.speed) + "]");

      processCommand(command);
    }
  }
}

// Calculate the pulse width that needs to be sent to the servo to correspond to the angle requested.
static int pulseWidth(int angle)
{
  int pulse_wide, analog_value;
  pulse_wide   = map(angle, 0, 180, MIN_PULSE_WIDTH, MAX_PULSE_WIDTH);
  analog_value = int(float(pulse_wide) / 1000000 * FREQUENCY * 4096);
  return analog_value;
}

void setup() {
  Serial.begin(SERIAL_SPEED);
  Serial.print("Servo Controller version ");
  Serial.println(VERSION);

  messages = new Messages();

  pwm.begin();

  pwm.setPWMFreq(FREQUENCY);    // Analog servos run at ~60 Hz updates
  yield();
}

// Wait for any servo messages comming in from the Rasp Pi and process them
void loop() {

  String msg = messages->read(false);

  if (msg.length() > 0) {
    processCommand(msg);
    Serial.println("Arduino processed [" + msg + "]");
  }
}

/*
  SerialEvent occurs whenever a new data comes in the
  hardware serial RX.  This routine is run between each
  time loop() runs, so using delay inside loop can delay
  response.  Multiple bytes of data may be available.
*/
void serialEvent() {
  messages->anySerialEvent();
}
