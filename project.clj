(defproject org.timmc/handy "1.4.0"
  :description "Common utilities to fill in the gaps"
  :url "https://github.com/timmc/handy"
  :license {:name "Eclipse Public License - v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :repl-options {:init-ns org.timmc.handy}
  :dependencies [[org.clojure/clojure "1.2.0"]]
  :profiles {:1.2.1 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.3.0 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4.0 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0-RC17"]]}}
  :aliases {"newer" ["with-profile" "1.2.1:1.3.0:1.4.0:1.5"]})
