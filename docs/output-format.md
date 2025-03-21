# Specification of the output format

The main task of the program is to convert the input .yml file into
an Arduino sketch (.ino), which will then be compiled using the `arduino-cli` and
flashed onto the target board. This document describes how the resulting `.ino` sketch
should be structured and the patterns for converting the `.yml` input into `C++` snippets used in Arduino sketches

After reading this document, please take a look at the example [input](./sample/input.yml) and [output](./sample/output.ino) files. The output file has comments labeling each block, but 
they are not required to be there when 

## Outline 
A typical arduino sketch has 3 sections:
 - global declaraions, at the top level scope in the C++ file, where variable declarations and preprocessor macros live
 - the `void setup()` function, for instructions that will be executed once, on startup
 - the `void loop()` function, where the logic is implemented

Later this document refers to the listed sections as `globals`,`setup` and `loop`

The input format, as defined in [input format specification](./input-format.md), has the following sections:
- board
- constants (optional)
- inputs
- outputs
- signals (optional)
- rules (optional)
- events (optional)

Each of these sections maps to code snippets in some sections of the sketch, as described later in this document. 

## Conventions

The following coding conventions are hereby proposed:
  - There is only one variable type, `uint16_t`
    - Analog inputs in arduino return the voltage read by the input pin, from the range 0-Vcc (for most boards Vcc = 5V), mapped to integer values from 0 to 1023, so uint8_t will be to small.
    - when evaluation boolean expressions any positive value should be treated as `true`, and 0 as `false`
  - `#define` macros should not be used to create labels for pins
    -  we can do the mapping of input/output pin numbers to the names of sensors/outputs in Kotlin with a `Map`
    - this way name of the input/output can be used as a variable name later, and will not clash with any `#define`
    - At the top of the output file there should be 2 macros, always:
      - `#define HIGH 1`
      - `#define LOW 0`
    - Use those when setting digital outputs, for readability
    

## Order of operations

Each section in the input format can map to code snippets in  any of the output sketch sections (`globals`, `setup` and `loop`). To avoid ambiguity, a strict ordering of those sections is hereby proposed (the contents of each section are described in the next section).

- globals:
    1. Constants
    2. Variables for input signals
    3. Variables for output signals
    4. Variables for internal signals
    5. Variables for the state of event handlers
- init:
  1. Input initialization (pinModes)
  2. Output initialization (pinModes)
- loop:
  1. Input reads
  2. Internals signals computation
  3. Rules evalutation
  4. Event handlers
  5. Output writes



\* For `globals` and `init` the order is irrelevant as far as behaviour is concerned, but lets 
keep it strict 


## Mapping of input sections to sketch sections

### `board`

Only one field from the `board` section maps to a code snippet in the output sketch. At the 
end of the `loop` section, there shall be a `delat(x)` instruction, the value of `x` should be the 
same as the `pooling_delay` field in the `board` section

```yml
board:
  - pooling_delay: 10
```
```cpp
void loop(){
  // ***
  delay(10);
}
```

### `inputs`

This section defines input signals from sensors connected to the board, and produces 
snippets in all 3 sections of the sketch. For each input signal an internal variable shall be declared in the `globals` section, a pin mode needs to be configured as `INPUT` in the `setup` section, and on each iteration of the main routine defined in the `loop` section a value must be read from the given pin and written to the variable. 

So, for an input declaration 
```yml
inputs:
  - name: button1
    mode: DIGITAL
    source: 5
```
in the sketch there should be 
```cpp
// ****
uint16_t button1;
// ****

void setup(){
  // ******
  pinMode(5,INPUT);
  // ******
}

void loop(){
  // ******
  button1 = digitalRead(5); // If the input mode was set to ANALOG, use `analogRead` instead
  // ******
}
```


#### `outputs`
Similar to the inputs section, every entry in the outputs section should result in a 
variable being declared in the `globals` section, a pin mode configuration in the `setup` and 
an action taken on each iteration of the `loop`, only this time it shall be a write operation.

Example:
```yml
#YML input
outputs:
  - name: led1
    mode: DIGITAL
    pin: 11

```
```cpp
// generated sketch
// ****
uint16_t led1;
// ****

void setup(){
  // ******
  pinMode(11,OUTPUT);
  // ******
}

void loop(){
  // ******
  digitalWrite(11,led1); 
  // ******
}
```

#### `constants`
Every declaration in the constants section should result in a preprocessor makro definition 
in the `globals` section, like so:
```yml
#YML input
constants:
  - name: EPSILON
    value: 5

  - name: PI
    value: 3
```

```cpp
#define EPSILON 5
#define PI 3
```

*Q: Why not use `const uint16_t PI = 3;` instead of `#define`?*

*A: Personal preference*


#### `signals`
This section defines internal signals which are derived from input signals and constants
(they can be constant, but then you may as well use a `constants` section).
For each entry a variable shall be declared in the `globals` section, and a value defined in the 
`expresion` field should be computed on each iteration of the `loop`, and written to the variable.

Example:
```
signals:
  - name: mySignalName
    expression: light_sensor > 50
```

```cpp
uint16_t mySignalName;

void loop(){
  //***
  mySignalName = light_sensor > 50;
  //***
}
```


#### `rules`

The rules section essentially define `if` statements that should be checked on every iteration of the loop, and an action to take when the condition is met. Each rule should translate to a snippet in the `loop` section 
```yml
rules:

  - if: touch_1_pressed
    then: SET led1 ON

  - if: !touch_1_pressed
    then: SET led1 OFF

```

```cpp

void loop(){
  //***
  if (touch_1_pressed) {
    led1 = 1;
  }
  if(!touch_1_pressed) {
    led1 = 0;
  }
  //***
}
```


#### `events`

Events are similar to `rules`, but they should only trigger an action when the condition is met
for the first time, and not trigger it again until the condition becomes false, and the true once more. To implement such functionality, each event shall have a variable defined in the `globals` section to keep track of the event handlers state, and a snippet in the `loop` section that checks the conditions and performs their respective actions.
Events do not have names, so the variables tracking the event handlers state will be labeled with the index of the event handler, in the order they are defined in the input file, i.e.
`event1_fired`

Example:

```yml
events:

  - when: light_sensor REACHES 87
    do: SET led2 OFF
```

```cpp
uint16_t event1_fired = false;

void loop(){
  // ***
  if (light_sensor >= 87) {
    if (!event1_fired) {
      led2 = 0;
      event1_fired = true;
    }
  } else {
    event1_fired = false;
  }
  // ***
}
```


