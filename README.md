# krulvis-scripts

This repository contains a series of public scripts for PowBot.
The goal of this repository is to create a public knowledge base
from which people can draw inspiration and learn how to write scripts.

## How to run scripts

Make sure you have gradle installed and then clone this repository.
Make sure your phone is connected or your android emulation is running.
Port forward tcp:616669 to you device using the following adb command: `adb forward tcp:61666 tcp:61666`.
Every script contains a `main` function that starts the script using the `AbstractScript.StartScript` function.

## How to contribute?

Whenever you have work that you would like to contribute, create a PR into
the [develop](/powbot/krulvis-scripts/tree/develop) branch.
The main branch is what's currently live on the SDN.
I will not accept PR's in that branch as its set-up in a way
that the powbot automatically merges PR in that branch.

