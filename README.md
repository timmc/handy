# org.timmc/handy

Just some handy utilities. The API is any var with :added metadata,
indicating the version of handy in which the var was introduced.
Generally, this covers any public var not marked :internal.

## Usage

If you use Leiningen, add the following to your project.clj dependencies:

`[org.timmc/handy "1.4.0"]`

The main namespace is `org.timmc.handy`.

Compatible with Clojure 1.2.0 through 1.5.1.

## Building

Built with Leiningen 2, but should be buildable with 1.x as well.

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

### v1.3.0
* Add `split-atom!` and `deterministic`
* Tested up to Clojure 1.5.0-RC2

### v1.4.0
* Add `if-let+` and `paging`
* Tested up to Clojure 1.5.0-RC17

## License

Copyright (C) 2012–2013 Tim McCormack

Distributed under the Eclipse Public License, the same as Clojure.
