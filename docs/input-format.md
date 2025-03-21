# Syntax spec (Work in progress)

Overall, the input format for the tool is YAML-based
Any valid input file is also valid YAML, but not all valid YAML
files are valid as input.
The input file should adhere to the following schema:

At the top level, seven section types are supported. Some are optional, some
mandatory:

- board
- constants (optional)
- inputs
- outputs
- signals (optional)
- rules (optional)
- events (optional)

So, a minimal input file should have

```yaml
board:
# Board configuration details
inputs:
# input types and pins
outputs:
# Output types and pins
rules:
# rules relating inputs to outputs
```

The order of sections does not matter, but for readability
it is recommended to follow the order in which they appear on the list
above.

## Structure of sections

### `board`

This section contains the configuration info required to build the binary and flash it onto the
users microcontroller of choice. Supported fields are listed below, all of which are mandatory

- `platform` - name of the board group the users microcontroller is registered under in the Arduino ecosystem, i.e.
  `arduino:avr` or `esp32:esp32`
- `type` - identifier of the specific board in the given group, i.e. `unowifi`. The `platform` and `type` fields
  together form the device `FQBN`
- `port` - the name of the port a board is connected to. On UNIX-based system this will most likely be
  `/dev/ttyUSB[0-9]`, while on Windows it would be `COM[0-9]`
  - `polling_delay`

So, for the Arduino UNO microcontroller connected to `/dev/ttyUSB0` this section would read

```yaml
board:

  - platform: arduino-avr
  - type: uno
  - port: /dev/ttyUSB0
```

### `constants`

This optional section lets the user define constants to be used later in the `rules` and `signals` sections.
Each entry in this section should have a `name` and `value` fields, i.e.

```yaml
constants:

  - name: PI
    value: 3.14

  - name: EPSILON
    value: 0.001

  - name: CORE_FREQUENCY,
    value: 100000
```

### `inputs`

This section describes the inputs connected to the board. Each defined input source
should have the following fields:

- `name` - human-readable, meaningful name of the input source, to be used in other sections of the file
- `mode` - operating mode of the input source. Either `ANALOG` or `DIGITAL`
- `source` - pin to which the input is connected

An example input definition would look like so:

```yaml
inputs:

  - name: button_1
    mode: DIGITAL
    source: 5
```

### `outputs`

This section describes the outputs connected to the board. Each defined output
should have the following fields:

- `name` - human-readable, meaningful name of the output, to be used in other sections of the file
- `mode` - operating mode of the input source. Either `ANALOG` or `DIGITAL`
- `source` - pin to which the output is connected

An example input definition would look like so:

```yaml
outputs:

  - name: buzzer_1
    mode: ANALOG
    pin: GPIO11


```

### `signals`

In the signals section the user can define control signals
which are derived from inputs, i.e. a signal may depend on 2 inputs,'a' and 'b' and
assume the value of `true` if input 'a' is greater than 'b'.

Each signal in this section must have a `name` and `expression`, i.e.

```yaml

signals:

  - name: touch_1_pressed
    expression: touch_sensor1 > 0.5

  - name: touch_2_pressed
    expression: touch_sensor1 > EPSILON
```

### `rules`

Each entry in this section describes what value an output should assume based on
known inputs,signals and constants. Each entry should have a `if` field and a `then` field. The conditions defined in
rules are checked every cycle, and if the condition defined in the `if` field is satisfied, the instruction in the
`then` field will be executed.

```yaml
rules:

  - if: touch_1_pressed
    then: SET led_1 ON

  - if: !touch_1_pressed
    then: SET led_1 OFF

```

### `events`

Events are similar to rules. They have a `when` field with a condition, and a `then` field with instructions on that
should happen when the condition is satisfied. However, while the instructions defined in the `rules` section are
executed every cycle as long as the condition is fulfilled, the instructions in this sections are only executed once,
when the condition becomes true, and will not be execute again until the condition becomes false, and then true again.

```yaml
events:

  - when: light_sensor REACHES 0.87
    do: SET led_2 OFF

  - when: button_1 PRESSED
    do: PULSE buzzer_1

```


