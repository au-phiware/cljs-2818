Affects: 1.9.1033+

I have a minimal repro repo here: https://github.com/au-phiware/cljs-2818

## Description of problem

Prior to the update to Closure compiler in CLJS-2389, ES6 modules that used the `export { x as y } from './x'` or `export { default as y } from './x'` syntax compiled correctly. Other forms of this syntax, such as `export { default } from './x'`, did not.

Since 1.9.1033, the compiler no longer emits the `goog.require` statements nor does it emit a complete set of `goog.addDependency` statements in `cljs_deps.js`.

## Steps to reproduce the problem

Consider the following source files:

```clojure
(ns foo.core (:require [hello :refer [helloGreet]]))

(def ^:export sayHello
    (helloGreet "World"))

(sayHello)
```

```javascript
export {
    default as helloGreet
} from "./greet";
```

```javascript
export default function greet(m) {
    document.write("\nHello, " + m);
};
```

```clojure
(require 'cljs.build.api)

(cljs.build.api/build
  "src"
  {:main 'foo.core
   :output-to "target/main.js"
   :output-dir "target/main.out"
   :asset-path "main.out"
   :foreign-libs [{:file "es6/hello.js"
                   :provides ['hello]
                   :module-type :es6}]
   :verbose true
   :npm-deps {"@cljs-oss/module-deps" "*"}
   :install-deps true})
```

Execute `cljs`:

```
java -cp cljs.jar:src clojure.main build.clj
```

## Expected outcome

`cljs` should exit cleanly and write the following files (approximately).

```javascript
goog.addDependency("base.js", ['goog'], []);
goog.addDependency("../cljs/core.js", ['cljs.core'], ['goog.string', 'goog.Uri', 'goog.object', 'goog.math.Integer', 'goog.string.StringBuffer', 'goog.array', 'goog.math.Long']);
goog.addDependency("../process/env.js", ['process.env'], ['cljs.core']);
goog.addDependency("../es6/greet.js", ['module$usr$src$es6$greet'], []);
goog.addDependency("../es6/hello.js", ['module$usr$src$es6$hello'], ['module$usr$src$es6$greet']);
goog.addDependency("../foo/core.js", ['foo.core'], ['cljs.core', 'module$usr$src$es6$hello']);
```

```javascript
goog.provide("module$usr$src$es6$hello");
goog.require("module$usr$src$es6$greet");
module$usr$src$es6$hello.helloGreet=module$usr$src$es6$greet["default"]
```

## Actual outcome

The `cljs_deps.js` is missing the `es6/greet.js` dependency:

```javascript
goog.addDependency("base.js", ['goog'], []);
goog.addDependency("../cljs/core.js", ['cljs.core'], ['goog.string', 'goog.Uri', 'goog.object', 'goog.math.Integer', 'goog.string.StringBuffer', 'goog.array', 'goog.math.Long']);
goog.addDependency("../process/env.js", ['process.env'], ['cljs.core']);
goog.addDependency("../es6/hello.js", ['module$usr$src$es6$hello'], []);
goog.addDependency("../foo/core.js", ['foo.core'], ['cljs.core', 'module$usr$src$es6$hello']);
```

And the `es6/hello.js` file is missing the `goog.requires` statement:

```javascript
goog.provide("module$usr$src$es6$hello");
var module$usr$src$es6$hello={get helloGreet(){return module$usr$src$es6$greet["default"]}}
```

Furthermore, the browser console shows:

```
>>> module$usr$src$es6$hello.helloGreet
hello.js:2 Uncaught ReferenceError: module$usr$src$es6$greet is not defined
    at Object.get helloGreet [as helloGreet] (hello.js:2)
    at <anonymous>:1:65
```

## Attempted workaround

Explicitly adding the `:requires` option to `es6/hello.js` `:foreign-libs` entry did not remedy the problem (nor did using an entry for the whole `es6` directory or using an npm module, e.g. d3-scale). Neither did adding `[greet]` to `foo.core`'s requires.


