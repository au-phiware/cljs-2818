(ns foo.core (:require [hello :refer [helloGreet]]))

(def ^:export sayHello
    (helloGreet "World"))

(sayHello)
