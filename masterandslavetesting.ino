#include <Wire.h>
#include <Servo.h>

#define US_TRIG A3
#define US_ECHO A2
#define SERVO_PIN A0
#define I2C_ADDRESS 8

Servo rearServo;
long lastDistance = 0;

void setup() {
  Wire.begin(I2C_ADDRESS); // Slave mode
  Wire.onRequest(sendDistance); // When Mega asks, send distance
  
  pinMode(US_TRIG, OUTPUT);
  pinMode(US_ECHO, INPUT);
  
  rearServo.attach(SERVO_PIN);
  rearServo.write(90); // Facing straight back
  delay(500);
  rearServo.detach();
}

void loop() {
  // Update distance every 100ms
  lastDistance = readUltrasonic();
  delay(100);
}

// Function to send distance when Mega requests
void sendDistance() {
  byte distanceToSend = (byte)constrain(lastDistance, 0, 255);
  Wire.write(distanceToSend);
}

// Function to read ultrasonic distance in cm
long readUltrasonic() {
  digitalWrite(US_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(US_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(US_TRIG, LOW);
  
  long duration = pulseIn(US_ECHO, HIGH, 30000); // 30ms timeout
  if (duration == 0) return 255; // Timeout = too far
  return duration / 29 / 2;
}
