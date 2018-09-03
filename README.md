# life

This is a Conway's Game of Life implementation using HTML5 Canvas and Clojurescript.

## Usage

``` bash
# run dev server with repl + hot-reloading
lein figwheel

# production build
lein do clean, cljsbuild once min

# clean files
lein clean
```

## Algorithm

Doesn't implement Hashlife, which compresses time, because each intermediary step is rendered to the screen.

## License

Copyright © 2018 Luke Turner

Distributed under the MIT License (SPDX:MIT)