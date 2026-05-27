# Manhattan Address Decoder

A mobile-first, offline web tool for NYC delivery riders navigating Manhattan by address logic and compass direction.

## What It Does

- Decodes avenue addresses into the nearest numbered cross street using the Manhattan address formula.
- Estimates avenue blocks for numbered street addresses.
- Shows Manhattan grid direction from the phone compass with the 29 degree grid offset applied.
- Includes quick one-way street and avenue rules for riders.
- Runs as one self-contained HTML file with no external dependencies.

## Use It

Open `manhattan-decoder.html` in a browser.

On a phone, use Safari or Chrome and add it to the home screen:

- iPhone: Share button, then Add to Home Screen.
- Android: three-dot menu, then Add to Home Screen.

## Address Formula

For avenue addresses:

1. Drop the last digit of the building number.
2. Divide by 2 and round.
3. Add the avenue key number.

Example: `435 Lexington Ave`

- Drop last digit: `43`
- Divide by 2: `21.5`, rounded to `22`
- Add Lexington key `+22`
- Nearest cross street: `44th Street`

## Avenue Keys

| Avenue | Key |
| --- | ---: |
| 1st Ave | +3 |
| 2nd Ave | +3 |
| 3rd Ave | +10 |
| Lexington Ave | +22 |
| Park Ave | +35 |
| Madison Ave | +26 |
| 5th Ave | +13 |
| 6th Ave | -12 |
| 7th Ave | +12 |
| 8th Ave | +10 |
| 9th Ave | +13 |
| 10th Ave | +14 |
| Amsterdam Ave | +60 |
| Columbus Ave | +60 |

## Notes

The compass uses `DeviceOrientationEvent`. iOS requires a tap before asking for sensor permission, so the app starts compass tracking from the Start Compass button.
