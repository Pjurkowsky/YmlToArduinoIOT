#define HIGH 1
#define LOW 0

// Constants

#define PI 3
#define EPSILON 5

// Variables for input signals

uint16_t btn1;
uint16_t btn2;
uint16_t photo1;

// Variables for output signals

uint16_t led1;
uint16_t led2;
uint16_t buzzer1;

// Variables for internal signals (the 'signals' section in the YML file)

uint16_t both_buttons_pressed;

// Variables for the state of event handlers

uint16_t event1_fired = 0;
uint16_t event2_fired = 0;

void init() {
  // Input init section
  pinMode(2, INPUT); // btn1
  pinMode(3, INPUT); // btn2
  pinMode(4, INPUT); // photo1

  // Output init section
  pinMode(20, OUTPUT); // led1
  pinMode(30, OUTPUT); // led2
  pinMode(40, OUTPUT); // buzzer1
}

void loop() {
  // Read input signals;

  btn1 = digitalRead(2);
  btn2 = digitalRead(3);
  photo1 = analogRead(4);

  // Compute internal signals
  both_buttons_pressed = btn1 && btn2;

  // Process rules

  if (btn1) {
    led1 = HIGH;
  }
  if (!btn1) {
    led1 = LOW;
  }
  if (both_buttons_pressed) {
    led2 = HIGH;
  }
  if (!both_buttons_pressed) {
    led2 = LOW;
  }

  // Process events
  if (photo1 >= 128) {
    if (!event1_fired) {
      led2 = LOW;
      event1_fired = true;
    }
  } else {
    event1_fired = false;
  }
  if (btn1) {
    if (!event2_fired) {
      buzzer1 = HIGH;
      event2_fired = true;
    }
  } else {
    event2_fired = false;
  }
  digitalWrite(20,led1);
  digitalWrite(30,led2);
  digitalWrite(40,buzzer1);
}
