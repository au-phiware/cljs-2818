.PHONY: clean open test

target: cljs.jar build.clj $(shell find src es6 resources node_modules 2>/dev/null)
	rsync -r --del resources/ target/
	java -cp cljs.jar:src clojure.main build.clj

open: target
	open target/index.html

test: target
	chrome --headless --dump-dom file://$(PWD)/target/index.html | grep '^Hello, 'World$

cljs.jar:
	ln -s $(CLJS_HOME)/target/cljs.jar

clean:
	rm -rf target node_modules
