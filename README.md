# org.timmc/handy

Just some handy utilities. The API is any public function not marked ^:internal.

## Usage

If you use Leiningen, add the following to your project.clj dependencies:

`[org.timmc/handy "1.0.1"]`

The main namespace is `org.timmc.handy`:

`(use '[org.timmc.handy :only (lexicomp)]) ; e.g.`

Compatible with Clojure 1.2.1 and 1.3.0.

## Building

Built with Leiningen 1.x

## Changelog

### v1.0.0
* Initial release
* handy.clj: `lexicomp`, `version-norm`, `version<=`

### v1.0.1
* Compatibility down to Clojure 1.2.0

### v1.1.0
* Add `with-temp-ns`, a sandboxing macro (may move to a different ns in future)

### v1.2.0
* Add `index-on`, a generalization of group-by

## License

Copyright (C) 2012 Tim McCormack

Distributed under the Eclipse Public License, the same as Clojure.
