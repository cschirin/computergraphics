/*
Custom extension to the Ardulink project by Philipp Jenke:
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/
*/

#include <Servo.h>
#include "I2Cdev.h"
#include "MPU6050.h"

#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
#include "Wire.h"
#endif

// int intensity = 0;               // led intensity this is needed just as example for this sketch
String inputString = "";         // a string to hold incoming data (this is general code you can reuse)
boolean stringComplete = false;  // whether the string is complete (this is general code you can reuse)

#define digitalPinListeningNum 14 // Change 14 if you have a different number of pins.
#define analogPinListeningNum 6 // Change 6 if you have a different number of pins.
boolean digitalPinListening[digitalPinListeningNum]; // Array used to know which pins on the Arduino must be listening.
boolean analogPinListening[analogPinListeningNum]; // Array used to know which pins on the Arduino must be listening.
int digitalPinListenedValue[digitalPinListeningNum]; // Array used to know which value is read last time.
int analogPinListenedValue[analogPinListeningNum]; // Array used to know which value is read last time.

Servo servo;
String* tokens;
MPU6050 accelgyro;
int16_t ax, ay, az;
int16_t gx, gy, gz;
int distanceTriggerPin = -1;
int distanceEchoPin = -1;

void setup() {
  // initialize serial: (this is general code you can reuse)
  Serial.begin(115200);

  //set to false all listen variable
  int index = 0;
  for (index = 0; index < digitalPinListeningNum; index++) {
    digitalPinListening[index] = false;
    digitalPinListenedValue[index] = -1;
  }
  for (index = 0; index < analogPinListeningNum; index++) {
    analogPinListening[index] = false;
    analogPinListenedValue[index] = -1;
  }

  // Turn off everything (not on RXTX)
  for (index = 2; index < digitalPinListeningNum; index++) {
    pinMode(index, OUTPUT);
    digitalWrite(index, LOW);
  }

  // Turn off LED this is needed just as example for this sketch
  //  analogWrite(11, intensity);

  // Read from 4 this is needed just as example for this sketch
  //  pinMode(4, INPUT);

  // In order to work with analog input signal you have to set pinMode to INPUT please add Ax pinMode statement if you need for it
  //pinMode(A0, INPUT);

  // MPU 6050
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
  Wire.begin();
#elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
  Fastwire::setup(400, true);
#endif
  accelgyro.initialize();
  Serial.println(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");

  // Servo
  tokens = new String[10];

  servo.attach(2);
  servo.write(30);
}

/**
 * Create an array of tokens for a String with a separation char (" ").
 */
String* tokenize(String input, int& numberOfTokens ) {
  int index = 0;
  String separator = " ";
  // Count number of tokens
  numberOfTokens = 0;
  while (index < input.length() ) {
    int nextIndex = input.indexOf(separator, index + 1);
    if ( nextIndex != -1 ) {
      input.substring(index, nextIndex);
      index = nextIndex;
    } else {
      input.substring(index);
      index = input.length();
    }
    numberOfTokens++;
  }
  // Create array of tokens
  index = 0;
  for ( int i = 0; i < numberOfTokens; i++ ) {
    int nextIndex = input.indexOf(separator, index + 1);
    if ( nextIndex != -1 ) {
      tokens[i] = input.substring(index, nextIndex);
      index = nextIndex + 1;
    } else {
      tokens[i] = input.substring(index);
      index = input.length();
    }
  }
  return tokens;
}

void initDistance(int triggerPin, int echoPin){
  distanceTriggerPin = triggerPin;
  distanceEchoPin = echoPin;
  pinMode(distanceTriggerPin, OUTPUT);
  pinMode(distanceEchoPin, INPUT);
}

double measureDistance(){
  digitalWrite(distanceTriggerPin, LOW); 
  delayMicroseconds(2); 
  
  digitalWrite(distanceTriggerPin, HIGH);
  delayMicroseconds(10); 
  
  digitalWrite(distanceTriggerPin, LOW);
  double duration = pulseIn(distanceEchoPin, HIGH);
 
  //Calculate the distance (in cm) based on the speed of sound.
  double distance = duration/58.2;
  return distance;
}

void loop() {

//  Serial.print("Hello");
//      Serial.write(255);
//      Serial.flush();



  // when a newline arrives:
  if (stringComplete) {
    if (inputString.startsWith("alp://")) { // OK is a message I know (this is general code you can reuse)
      boolean msgRecognized = true;

//      // Debugging
//      Serial.print(inputString);
//      Serial.write(255);
//      Serial.flush();
//      inputString = "";
//      stringComplete = false;
//      return;

      if ( inputString.indexOf("led") != -1) {
        int numberOfTokens = 0;
        String* tokens = tokenize(inputString, numberOfTokens);
        int ledPin = tokens[1].toInt();
        String ledCommand = tokens[2];
        pinMode(ledPin, OUTPUT);
        if ( ledCommand.indexOf("on") != -1 ){
          digitalWrite(ledPin, HIGH);  
        } else {
          digitalWrite(ledPin, LOW);    
        }
      } else if ( inputString.indexOf("distance") != -1) {
        int numberOfTokens = 0;
        String* tokens = tokenize(inputString, numberOfTokens);
        int triggerPin = tokens[1].toInt();
        int echoPin = tokens[2].toInt();
        if (distanceEchoPin != triggerPin || distanceEchoPin != echoPin ){
           initDistance(triggerPin, echoPin);
        }
        String distanceString = "distance " + String(measureDistance());
        Serial.print(distanceString);
        Serial.write(255);
        Serial.flush();
        //Serial.print("debug " + String(distanceTriggerPin) + " "  + String(distanceEchoPin));
        //Serial.write(255);
        //Serial.flush();
      } else if ( inputString.indexOf("gyro") != -1) {
        // Read gyro
        accelgyro.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
        String gyroString = "gyro " +  String(ax) + " " + String(ay) + " " + String(az) + " " + String(gx) + " " + String(gy) + " " + String(gz);
        Serial.print(gyroString);
        Serial.write(255);
        Serial.flush();
      } else if ( inputString.indexOf("servo") != -1) {
        // Servo handling
        //String servoMessage = "Servo ok";
        int numberOfTokens = 0;
        String* tokens = tokenize(inputString, numberOfTokens);
        if ( numberOfTokens < 2 ) {
          //servoMessage = "To few tokens";
        } else {
          if ( tokens[1].equals("attach") ) {
            // alp://servo attach " + pin
            int servoPin = tokens[2].toInt();
            //servoMessage = "Attached!";
            servo.attach(servoPin);
          } else if (tokens[1].equals("detach")) {
            // alp://servo detach " + pin
            //servoMessage = "Detached";
            servo.detach();
          } else if (tokens[1].equals("degree")) {
            // "alp://servo degree " + pin + " " + degree
            if ( numberOfTokens < 4 ) {
              //servoMessage = "To few tokens for degree";
            } else {
              int degree = tokens[3].toInt();
              //servoMessage = "Set degree: " + String(degree);
              servo.write(degree);
            }
          } else {
            //servoMessage = "Invalid command: " + tokens[1];
          }
        }
        // Servo message
//        Serial.print(servoMessage);
//        Serial.write(255);
//        Serial.flush();

      } else if (inputString.substring(6, 10) == "kprs") { // KeyPressed
        // here you can write your own code. For instance the commented code change pin intensity if you press 'a' or 's'
        // take the command and change intensity on pin 11 this is needed just as example for this sketch
        //char commandChar = inputString.charAt(14);
        //if(commandChar == 'a' and intensity > 0) { // If press 'a' less intensity
        //  intensity--;
        //  analogWrite(11,intensity);
        //} else if(commandChar == 's' and intensity < 125) { // If press 's' more intensity
        //  intensity++;
        //  analogWrite(11,intensity);
        //}
      } else if (inputString.substring(6, 10) == "ppin") { // Power Pin Intensity (this is general code you can reuse)
        int separatorPosition = inputString.indexOf('/', 11 );
        String pin = inputString.substring(11, separatorPosition);
        String intens = inputString.substring(separatorPosition + 1);
        pinMode(pin.toInt(), OUTPUT);
        analogWrite(pin.toInt(), intens.toInt());
      } else if (inputString.substring(6, 10) == "ppsw") { // Power Pin Switch (this is general code you can reuse)
        int separatorPosition = inputString.indexOf('/', 11 );
        String pin = inputString.substring(11, separatorPosition);
        String power = inputString.substring(separatorPosition + 1);
        pinMode(pin.toInt(), OUTPUT);
        if (power.toInt() == 1) {
          digitalWrite(pin.toInt(), HIGH);
        } else if (power.toInt() == 0) {
          digitalWrite(pin.toInt(), LOW);
        }
      } else if (inputString.substring(6, 10) == "tone") { // tone request (this is general code you can reuse)
        int firstSlashPosition = inputString.indexOf('/', 11 );
        int secondSlashPosition = inputString.indexOf('/', firstSlashPosition + 1 );
        int pin = inputString.substring(11, firstSlashPosition).toInt();
        int frequency = inputString.substring(firstSlashPosition + 1, secondSlashPosition).toInt();
        int duration = inputString.substring(secondSlashPosition + 1).toInt();
        if (duration == -1) {
          tone(pin, frequency);
        } else {
          tone(pin, frequency, duration);
        }
      } else if (inputString.substring(6, 10) == "notn") { // no tone request (this is general code you can reuse)
        int firstSlashPosition = inputString.indexOf('/', 11 );
        int pin = inputString.substring(11, firstSlashPosition).toInt();
        noTone(pin);
      } else if (inputString.substring(6, 10) == "srld") { // Start Listen Digital Pin (this is general code you can reuse)
        String pin = inputString.substring(11);
        digitalPinListening[pin.toInt()] = true;
        digitalPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
        pinMode(pin.toInt(), INPUT);
      } else if (inputString.substring(6, 10) == "spld") { // Stop Listen Digital Pin (this is general code you can reuse)
        String pin = inputString.substring(11);
        digitalPinListening[pin.toInt()] = false;
        digitalPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
      } else if (inputString.substring(6, 10) == "srla") { // Start Listen Analog Pin (this is general code you can reuse)
        String pin = inputString.substring(11);
        analogPinListening[pin.toInt()] = true;
        analogPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
      } else if (inputString.substring(6, 10) == "spla") { // Stop Listen Analog Pin (this is general code you can reuse)
        String pin = inputString.substring(11);
        analogPinListening[pin.toInt()] = false;
        analogPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
      } else {
        msgRecognized = false; // this sketch doesn't know other messages in this case command is ko (not ok)
      }


      // Prepare reply message if caller supply a message id (this is general code you can reuse)
      int idPosition = inputString.indexOf("?id=");
      if (idPosition != -1) {
        String id = inputString.substring(idPosition + 4);
        // print the reply
        Serial.print("alp://rply/");
        if (msgRecognized) { // this sketch doesn't know other messages in this case command is ko (not ok)
          Serial.print("ok?id=");
        } else {
          Serial.print("ko?id=");
        }
        //Serial.print(id);
        Serial.print(inputString);
        Serial.write(255); // End of Message
        Serial.flush();
      }
    }

    // clear the string:
    inputString = "";
    stringComplete = false;
  }

  // Send listen messages
  int index = 0;
  for (index = 0; index < digitalPinListeningNum; index++) {
    if (digitalPinListening[index] == true) {
      int value = digitalRead(index);
      if (value != digitalPinListenedValue[index]) {
        digitalPinListenedValue[index] = value;
        Serial.print("alp://dred/");
        Serial.print(index);
        Serial.print("/");
        Serial.print(value);
        Serial.write(255); // End of Message
        Serial.flush();
      }
    }
  }
  for (index = 0; index < analogPinListeningNum; index++) {
    if (analogPinListening[index] == true) {
      int value = highPrecisionAnalogRead(index);
      if (value != analogPinListenedValue[index]) {
        analogPinListenedValue[index] = value;
        Serial.print("alp://ared/");
        Serial.print(index);
        Serial.print("/");
        Serial.print(value);
        Serial.write(255); // End of Message
        Serial.flush();
      }
    }
  }
}

// Reads 4 times and computes the average value
int highPrecisionAnalogRead(int pin) {
  int value1 = analogRead(pin);
  int value2 = analogRead(pin);
  int value3 = analogRead(pin);
  int value4 = analogRead(pin);

  int retvalue = (value1 + value2 + value3 + value4) / 4;
}

/*
  SerialEvent occurs whenever a new data comes in the
 hardware serial RX.  This routine is run between each
 time loop() runs, so using delay inside loop can delay
 response.  Multiple bytes of data may be available.
 This is general code you can reuse.
 */
void serialEvent() {

  while (Serial.available() && !stringComplete) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
    }
  }
}


