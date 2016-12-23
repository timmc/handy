# org.timmc/handy

Just some handy utilities. The API is any var with `:added` metadata,
indicating the version of handy in which the var was introduced.
Generally, this covers any public var not marked `:internal`.

## Usage

If you use Leiningen, add the following to your project.clj dependencies:

`[org.timmc/handy "1.7.1"]`

The main namespace is `org.timmc.handy`.

Compatible with Clojure 1.2.0 through 1.8.0.

## Building

Built with Leiningen 2, but should be buildable with 1.x as well.

Test against all supported Clojure versions with `lein all-clj
test`. Release to clojars with `lein release`. Mark changes in
`CHANGELOG.md`.

## License

Copyright (C) 2012–2016 Tim McCormack

Distributed under the Eclipse Public License, the same as Clojure.
