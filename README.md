# org.timmc/handy

Just some handy utilities. The API is any var with :added metadata,
indicating the version of handy in which the var was introduced.
Generally, this covers any public var not marked :internal.

## Usage

If you use Leiningen, add the following to your project.clj dependencies:

`[org.timmc/handy "1.6.0"]`

The main namespace is `org.timmc.handy`.

Compatible with Clojure 1.2.0 through 1.6.0.

## Building

Built with Leiningen 2, but should be buildable with 1.x as well.

Test against all supported Clojure versions with `lein all-clj test`.

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

### v1.5.0
* Added REPL utilities namespace, org.timmc.handy.repl:
    * `show` replaces the old contrib repl fn of the same name,
      printing the structure of a JVM class
* Added reflection namespace, org.timmc.handy.reflect
* Renamed :since metadata to :added (match clojure.core convention)
* Tested up to Clojure 1.6.0

### v1.6.0
* Add `matching-arity` to check if a var is callable

## License

Copyright (C) 2012–2014 Tim McCormack

Distributed under the Eclipse Public License, the same as Clojure.
