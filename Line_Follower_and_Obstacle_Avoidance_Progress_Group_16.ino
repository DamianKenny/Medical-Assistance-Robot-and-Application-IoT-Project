#include <Wire.h>
#include <RTClib.h>
#include <Servo.h>

RTC_DS3231 rtc;
Servo trayServo; 
Servo usServo;  

#define BUZZER_PIN 2

// Medication Tray pin config
const int TRAY_SERVO_PIN = 13;
const int MEDICATION_HOUR = 0;     // Set medication hour
const int MEDICATION_MINUTE = 47;   // Set medication minute
const int TRAY_HOLD_TIME = 15000;   // 15 seconds
bool trayActivated = false;
unsigned long lastTrayActivation = 0;

// Bot Activation Configuration
const int BOT_HOUR = 3;            // Set bot start hour
const int BOT_MINUTE = 51;          // Set bot start minute
bool botActive = false;
bool botTriggered = false;          
int lastDay = -1;                   

// Line Following + Obstacle Avoidance pin config
#define enA 10
#define in1 9
#define in2 8
#define in3 7
#define in4 6
#define enB 5
#define L_S A0  // Left IR sensor
#define R_S A1  // Right IR sensor
#define echoPin A2
#define trigPin A3
#define US_SERVO_PIN A7  

// Back Ultrasonic Configuration (NEW)
#define echoPinBack A5  // Back echo pin
#define trigPinBack A4  // Back trig pin
#define US_SERVO_BACK_PIN A8  // Back servo pin

// Return journey control variables (NEW)
bool atLine = false;
unsigned long lineStopTime = 0;
bool returnMode = false;

int Set = 15; 
int distance_L, distance_F, distance_R;

// Motor calibration
#define RIGHT_MOTOR_OFFSET 0
#define LEFT_MOTOR_OFFSET 10
#define MOTOR_SPEED 100
#define REVERSE_SPEED 80  // Slower speed for better control in reverse

void setup() {
  Serial.begin(115200);
  
  // Initialize Buzzer
  pinMode(BUZZER_PIN, OUTPUT);
  noTone(BUZZER_PIN);  

  // Initialize RTC
  Wire.begin();
  if (!rtc.begin()) {
    Serial.println("Couldn't find RTC");
    while (1);
  }
  
  if (rtc.lostPower()) {
    Serial.println("RTC reset to compile time");
    rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
  }

  // Initialize Medication Tray Servo
  trayServo.attach(TRAY_SERVO_PIN);
  trayServo.write(78); // Initial position
  delay(500);
  trayServo.detach();

  // Initialize Bot Components
  pinMode(L_S, INPUT);
  pinMode(R_S, INPUT);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(enA, OUTPUT);
  pinMode(enB, OUTPUT);
  pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);
  pinMode(in3, OUTPUT);
  pinMode(in4, OUTPUT);
  pinMode(US_SERVO_PIN, OUTPUT);
  
  // Initialize Back Ultrasonic (NEW)
  pinMode(trigPinBack, OUTPUT);
  pinMode(echoPinBack, INPUT);
  pinMode(US_SERVO_BACK_PIN, OUTPUT);
  
  // Calibrate Front Ultrasonic Servo
  for (int angle = 70; angle <= 140; angle += 5) {
    servoPulse(US_SERVO_PIN, angle);
  }
  for (int angle = 140; angle >= 0; angle -= 5) {
    servoPulse(US_SERVO_PIN, angle);
  }
  for (int angle = 0; angle <= 70; angle += 5) {
    servoPulse(US_SERVO_PIN, angle);
  }
  
  // Center Back Servo (NEW)
  for (int i = 0; i < 10; i++) {
    servoPulseBack(90);
  }
  
  distance_F = readUltrasonic();
  Serial.println("System initialized");
}

void loop() {
  DateTime now = rtc.now();
  printTime(now);
  
  // Daily reset at midnight
  if (now.day() != lastDay) {
    lastDay = now.day();
    trayActivated = false;
    botTriggered = false;
    botActive = false;
    atLine = false;
    returnMode = false;
    Serial.println("Daily reset triggered");
  }

  // Check medication time
  if (now.hour() == MEDICATION_HOUR && 
      now.minute() == MEDICATION_MINUTE &&
      !trayActivated) {
    activateTray();
    trayActivated = true;
    lastTrayActivation = millis();
  }

  // Reset medication flag after operation
  if (trayActivated && (millis() - lastTrayActivation > TRAY_HOLD_TIME + 30000)) {
    trayActivated = false;
    trayServo.detach();
    Serial.println("Tray system reset");
  }

  // Check bot activation time
  if (now.hour() == BOT_HOUR && 
      now.minute() == BOT_MINUTE &&
      !botTriggered) {
    botActive = true;
    botTriggered = true;
    atLine = false;
    returnMode = false;
    Serial.println("Bot activated!");
  }

  // Run bot functionality when active
  if (botActive) {
    if (!returnMode) {
      runBotForward();
    } else {
      runBotReturn();
    }
  }
}

// Forward journey functions
void runBotForward() {
  distance_F = readUltrasonic();
  Serial.print("Front Dist: "); Serial.println(distance_F);

  int rightIR = digitalRead(R_S);
  int leftIR = digitalRead(L_S);

  // Line detection logic
  if (!rightIR && !leftIR) {      // Both sensors on white
    if (distance_F > Set) {
      moveMotors(MOTOR_SPEED, MOTOR_SPEED); 
    } else {
      avoidObstacle();
    }
  } 
  else if (rightIR && !leftIR) {   // Left sensor on line
    moveMotors(0, MOTOR_SPEED);    // Turn right
  } 
  else if (!rightIR && leftIR) {   // Right sensor on line
    moveMotors(MOTOR_SPEED, 0);    // Turn left
  } 
  else {                           // Both sensors on black line
    moveMotors(0, 0);              // Stop immediately
  }

  // Detect black line for stop
  if (!atLine && rightIR && leftIR) {
    moveMotors(0, 0);
    atLine = true;
    lineStopTime = millis();
    Serial.println("Reached black line! Waiting 30 seconds...");
  }

  // Start return journey after 30 seconds
  if (atLine && (millis() - lineStopTime >= 30000)) {
    returnMode = true;
    // Center back servo
    for (int i = 0; i < 10; i++) {
      servoPulseBack(90);
    }
    Serial.println("Starting return journey!");
    
    // Reverse briefly to detach from line
    moveMotors(-REVERSE_SPEED, -REVERSE_SPEED);
    delay(300);
  }
  delay(10);
}

// CORRECTED REVERSE LINE FOLLOWING LOGIC
void runBotReturn() {
  // Read back ultrasonic for obstacles
  long distance_Back = readBackUltrasonic();
  Serial.print("Back Dist: "); Serial.println(distance_Back);

  int rightIR = digitalRead(R_S);
  int leftIR = digitalRead(L_S);

  // Check if returned to start (black line)
  if (rightIR && leftIR) {
    moveMotors(0, 0);
    botActive = false;
    Serial.println("Returned to start! Mission complete.");
    
    // Play completion sound
    for (int i = 0; i < 3; i++) {
      tone(BUZZER_PIN, 1500, 300);
      delay(500);
    }
    return;
  }

  // Obstacle avoidance
  if (distance_Back < Set) {
    avoidObstacleBack();
  } 
  // FIXED REVERSE LINE FOLLOWING
  else {
    // Both sensors on white: move straight backward
    if (!rightIR && !leftIR) {
      moveMotors(-REVERSE_SPEED, -REVERSE_SPEED);
    }
    // Right sensor on black: need to turn left while reversing
    else if (rightIR && !leftIR) {
      moveMotors(-REVERSE_SPEED/3, -REVERSE_SPEED); 
    }
    // Left sensor on black: need to turn right while reversing
    else if (!rightIR && leftIR) {
      moveMotors(-REVERSE_SPEED, -REVERSE_SPEED/3); 
    }
    // Both sensors on black: move backward
    else {
      moveMotors(-REVERSE_SPEED, -REVERSE_SPEED);
    }
  }
  delay(10);
}

// Back obstacle avoidance
void avoidObstacleBack() {
  moveMotors(0, 0); 
  delay(100);

  // Look right
  for (int angle = 70; angle <= 140; angle += 5) {
    servoPulseBack(angle);
    delay(50);
  }
  long distance_R_back = readBackUltrasonic();
  Serial.print("Back Right: "); Serial.println(distance_R_back);
  delay(100);

  // Look left
  for (int angle = 140; angle >= 0; angle -= 5) {
    servoPulseBack(angle);
    delay(50);
  }
  long distance_L_back = readBackUltrasonic();
  Serial.print("Back Left: "); Serial.println(distance_L_back);
  delay(100);

  // Return to center
  for (int angle = 0; angle <= 90; angle += 5) {
    servoPulseBack(angle);
    delay(50);
  }

  // Decide avoidance direction
  if (distance_L_back > distance_R_back) {
    // More space on left - turn left
    moveMotors(REVERSE_SPEED, -REVERSE_SPEED); 
    delay(500);
  } else {
    // More space on right - turn right
    moveMotors(-REVERSE_SPEED, REVERSE_SPEED); 
    delay(500);
  }
  
  // Continue moving backward after turn
  moveMotors(-REVERSE_SPEED, -REVERSE_SPEED);
  delay(800);
}

// Medication Tray Functions
void activateTray() {
  Serial.println("\n=== MEDICATION TIME ===");
  trayServo.attach(TRAY_SERVO_PIN);
  
  for (int pos = 78; pos <= 170; pos++) {
    trayServo.write(pos);
    delay(30);
  }
  Serial.println("Tray open");
  
  // Play sound repeatedly every 3 seconds
  unsigned long startTime = millis();
  while (millis() - startTime < TRAY_HOLD_TIME) {
    playNotificationSound();
    delay(3000);
  }
  
  for (int pos = 170; pos >= 78; pos--) {
    trayServo.write(pos);
    delay(30);
  }
  Serial.println("Tray closed");
  trayServo.detach();
}

void playNotificationSound() {
  for (int i = 0; i < 2; i++) {
    tone(BUZZER_PIN, 1200);
    delay(200);
    noTone(BUZZER_PIN);
    delay(200);
  }
}

void printTime(const DateTime& dt) {
  char buf[20];
  snprintf(buf, sizeof(buf), "%02d:%02d:%02d",
           dt.hour(), dt.minute(), dt.second());
  Serial.print("Current time: "); Serial.println(buf);
}

// Motor Control
void moveMotors(int rightSpeed, int leftSpeed) {
  // Right motor control
  if (rightSpeed > 0) {
    digitalWrite(in3, LOW);
    digitalWrite(in4, HIGH);
  } else if (rightSpeed < 0) {
    digitalWrite(in3, LOW);
    digitalWrite(in4, HIGH);
  } else {
    digitalWrite(in3, LOW);
    digitalWrite(in4, LOW);
  }

  // Left motor control
  if (leftSpeed > 0) {
    digitalWrite(in1, LOW);
    digitalWrite(in2, HIGH);
  } else if (leftSpeed < 0) {
    digitalWrite(in1, LOW);
    digitalWrite(in2, HIGH);
  } else {
    digitalWrite(in1, LOW);
    digitalWrite(in2, LOW);
  }

  analogWrite(enB, constrain(abs(rightSpeed) + RIGHT_MOTOR_OFFSET, 0, 255));
  analogWrite(enA, constrain(abs(leftSpeed) + LEFT_MOTOR_OFFSET, 0, 255));
}

// Servo Control
void servoPulse(int pin, int angle) {
  int pulseWidth = (angle * 11) + 500;
  digitalWrite(pin, HIGH);
  delayMicroseconds(pulseWidth);
  digitalWrite(pin, LOW);
  delay(50); 
}

// Back servo control
void servoPulseBack(int angle) {
  int pulseWidth = (angle * 11) + 500;
  digitalWrite(US_SERVO_BACK_PIN, HIGH);
  delayMicroseconds(pulseWidth);
  digitalWrite(US_SERVO_BACK_PIN, LOW);
  delay(50);
}

// Back ultrasonic reading
long readBackUltrasonic() {
  digitalWrite(trigPinBack, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPinBack, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPinBack, LOW);
  return pulseIn(echoPinBack, HIGH) / 29 / 2;
}

// Front ultrasonic reading
long readUltrasonic() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  return pulseIn(echoPin, HIGH) / 29 / 2;
}

// Front obstacle avoidance
void avoidObstacle() {
  moveMotors(0, 0); 
  delay(100);
  
  // Look right
  for (int angle = 70; angle <= 140; angle += 5) {
    servoPulse(US_SERVO_PIN, angle);
  }
  distance_R = readUltrasonic();
  Serial.print("Right: "); Serial.println(distance_R);
  delay(100);
  
  // Look left
  for (int angle = 140; angle >= 0; angle -= 5) {
    servoPulse(US_SERVO_PIN, angle);
  }
  distance_L = readUltrasonic();
  Serial.print("Left: "); Serial.println(distance_L);
  delay(100);
  
  // Return to center
  for (int angle = 0; angle <= 70; angle += 5) {
    servoPulse(US_SERVO_PIN, angle);
  }
  
  // Decide avoidance direction
  if (distance_L > distance_R) {
    moveMotors(-MOTOR_SPEED, MOTOR_SPEED);  
    delay(500);
    moveMotors(MOTOR_SPEED, MOTOR_SPEED);   
    delay(600);
    moveMotors(MOTOR_SPEED, -MOTOR_SPEED);  
    delay(500);
    moveMotors(MOTOR_SPEED, MOTOR_SPEED);   
    delay(600);
    moveMotors(MOTOR_SPEED, -MOTOR_SPEED);  
    delay(400);
  } else {
    moveMotors(MOTOR_SPEED, -MOTOR_SPEED);  
    delay(500);
    moveMotors(MOTOR_SPEED, MOTOR_SPEED);    
    delay(600);
    moveMotors(-MOTOR_SPEED, MOTOR_SPEED);   
    delay(500);
    moveMotors(MOTOR_SPEED, MOTOR_SPEED);    
    delay(600);
    moveMotors(-MOTOR_SPEED, MOTOR_SPEED);   
    delay(400);
  }
}