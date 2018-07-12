(require 'cljs.build.api)

(cljs.build.api/build
  "src"
  {:main 'foo.core
   :output-to "target/main.js"
   :output-dir "target/main.out"
   :asset-path "main.out"
   :foreign-libs [{:file "es6/hello.js"
                   :provides ["hello"]
                   :module-type :es6}]
   :verbose true
   ;:debug true
   ;:optimizations :advanced
   :npm-deps {"@cljs-oss/module-deps" "*"}
   :install-deps true})
