# Changelog

## Unreleased
- 

## 1.7.1 - 2016-12-23
- Confirmed Clojure 1.7.0, 1.8.0 support
- Remove reflection warnings

## 1.7.0 - 2014-10-17
- Add `tabular-delta` to help with vary-one-thing midje tests

## 1.6.0 - 2014-05-24
- Add `matching-arity` to check if a var is callable

## 1.5.0 - 2013-03-23
- Added REPL utilities namespace, org.timmc.handy.repl:
    - `show` replaces the old contrib repl fn of the same name,
      printing the structure of a JVM class
- Added reflection namespace, org.timmc.handy.reflect
- Renamed :since metadata to :added (match clojure.core convention)
- Tested up to Clojure 1.6.0

## 1.4.0 - 2013-02-24
- Add `if-let+` and `paging`
- Tested up to Clojure 1.5.0-RC17

## 1.3.0 - 2013-01-29
- Add `split-atom!` and `deterministic`
- Tested up to Clojure 1.5.0-RC2

## 1.2.0 - 2012-04-05
- Add `index-on`, a generalization of group-by

## 1.1.0 - 2012-02-19
- Add `with-temp-ns`, a sandboxing macro (may move to a different ns in future)

## 1.0.1 - 2012-01-29
- Compatibility down to Clojure 1.2.0

## 1.0.0 - 2012-01-28
- Initial release
- handy.clj: `lexicomp`, `version-norm`, `version<=`
