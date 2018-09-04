# G.O.L.EM.

> A **G**ame **O**f **L**ife **EM**ulator

Conway's Game of Life in the browser, written as an experiment in ClojureScript.

## Features

- Increase/decrease the rate of time.
- Click on the board to swap cells between alive and dead.
- RLE-encoded pattern import/export.

## Usage

``` bash
# run dev server
lein figwheel

# build js
lein do clean, cljsbuild once min

# clean files
lein clean
```

## License

Copyright © 2018 Luke Turner

Distributed under the MIT License (SPDX:MIT)